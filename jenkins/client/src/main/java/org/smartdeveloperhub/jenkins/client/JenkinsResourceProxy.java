/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:0.2.0-SNAPSHOT
 *   Bundle      : ci-jenkins-client-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.Status;
import org.smartdeveloperhub.util.xml.XmlUtils;
import org.w3c.dom.Document;

import com.google.common.base.MoreObjects;

public final class JenkinsResourceProxy {

	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsResourceProxy.class);

	private final URI location;

	private final boolean useHttps;

	private final JenkinsEntityType entity;

	private JenkinsResourceProxy(URI location, JenkinsEntityType entity, boolean useHttps) {
		this.useHttps = useHttps;
		this.location = location;
		this.entity = entity;
	}

	public JenkinsResourceProxy withLocation(URI location) {
		checkNotNull(location,"Resource location cannot be null");
		return new JenkinsResourceProxy(location,this.entity,this.useHttps);
	}

	public JenkinsResourceProxy withEntity(JenkinsEntityType entity) {
		checkNotNull(entity,"Resource entity cannot be null");
		return new JenkinsResourceProxy(this.location,entity,this.useHttps);
	}

	public JenkinsResourceProxy withUseHttps(boolean useHttps) {
		return new JenkinsResourceProxy(this.location,this.entity,useHttps);
	}

	public URI location() {
		return this.location;
	}

	public JenkinsResource get(JenkinsArtifactType artifact) throws IOException {
		checkNotNull(artifact,"Artifact cannot be null");
		InMemoryJenkinsResource resource=
			new InMemoryJenkinsResource(new Date()).
			withEntity(this.entity).
				withLocation(this.location).
				withEntity(this.entity). // Temporary
				withArtifact(artifact);
		CloseableHttpClient httpClient=HttpClients.createDefault();
		CloseableHttpResponse httpResponse=null;
		HttpGet httpGet = createGetMethod(artifact.locate(location));
		try {
			httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			resource.
				metadata().
					withServerVersion(HttpResponseUtil.retrieveServiceVersion(httpResponse)).
					response().
						withStatusCode(statusCode).
						withEtag(HttpResponseUtil.retrieveEntityTag(httpResponse)).
						withLastModified(HttpResponseUtil.retrieveLastModified(httpResponse));

			ContentType contentType=processResponseBody(resource,httpResponse.getEntity());

			if(statusCode!=200) {
				resource.
					withStatus(
						Status.fromHttpStatusCode(statusCode),
						HttpResponseUtil.getInvalidResponseFailureMessage(httpResponse));
			} else {
				processResourceEntity(
					resource,
					contentType);
			}
		} catch (Exception cause) {
			LOGGER.error("Communication with the server failed",cause);
			throw new IOException("Communication with the server failed",cause);
		} finally {
			IOUtils.closeQuietly(httpResponse);
			IOUtils.closeQuietly(httpClient);
		}
		return resource;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("location",this.location).
					add("entity",this.location).
					add("useHttps",this.useHttps).
					toString();
	}

	private ContentType processResponseBody(InMemoryJenkinsResource resource, HttpEntity httpEntity) throws IOException {
		if(httpEntity==null) {
			return null;
		}

		ContentType contentType =
			ContentType.
				parse(httpEntity.getContentType().getValue());

		Charset charset = contentType.getCharset();
		if(charset==null) {
			LOGGER.debug(
				"No encoding specified for {} ({}). Resorting to UTF-8",
				resource.location(),
				contentType);
			charset=StandardCharsets.UTF_8;
		}

		resource.
			metadata().
				response().
					withBody(
						HttpResponseUtil.
							toResponseBody(
								httpEntity.getContent(),
								contentType.getMimeType(),
								charset));

		return contentType;
	}

	private void processResourceEntity(InMemoryJenkinsResource resource, ContentType contentType) {
		try {
			if(resource.metadata().serverVersion()==null && resource.artifact().isModelObject()) {
				resource.
					withStatus(
						Status.INCOMPATIBLE_RESOURCE,
						"Not a Jenkins resource");
			} else if(!ContentType.APPLICATION_XML.getMimeType().equals(contentType.getMimeType())) {
				resource.
					withStatus(
						Status.INCOMPATIBLE_RESOURCE,
						"Not an XML representation of a Jenkins resource (%s)",contentType);
			} else {
				updateRepresentationContent(resource);
			}
		} catch (Exception cause) {
			resource.
				withStatus(
					Status.UNPROCESSABLE_RESOURCE,cause,"Could not process response body");
		}
	}

	private void updateRepresentationContent(InMemoryJenkinsResource resource) {
		String body = resource.metadata().response().body().get().content();
		try {
			Document content = XmlUtils.toDocument(body);
			String localName=content.getFirstChild().getNodeName();
			if(JenkinsArtifactType.RESOURCE.equals(resource.artifact())) {
				JenkinsEntityType receivedEntity=JenkinsEntityType.fromNode(localName);
				if(receivedEntity==null) {
					resource.withStatus(
						Status.UNSUPPORTED_RESOURCE,
						"Unsupported Jenkins resource type '%s'",localName);
				} else if(!this.entity.isCompatible(receivedEntity)) {
					resource.withStatus(
						Status.INVALID_RESOURCE,
						"Invalid Jenkins entity type. Expected '%s' but got '%s'",this.entity,receivedEntity);
				} else {
					resource.
						withEntity(receivedEntity).
						withStatus(Status.AVAILABLE);
				}
			} else {
				resource.
					withStatus(Status.AVAILABLE);
			}
			resource.withContent(content);
		} catch (Exception cause) {
			LOGGER.debug("Could not process response body {}",body,cause);
			resource.withStatus(Status.UNPROCESSABLE_RESOURCE,cause,"Could not process response body");
		}
	}

	private HttpGet createGetMethod(URI buildLocation) {
		String newURI=buildLocation.toString();
		if(this.useHttps) {
			newURI=newURI.replace("http://", "https://");
		}
		HttpGet httpGet = new HttpGet(newURI);
		httpGet.setHeader("Accept", "application/xml; charset=utf-8");
		httpGet.setHeader("User-Agent", " Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
		RequestConfig config =
			RequestConfig.
				custom().
					setConnectTimeout(5000).
					setRedirectsEnabled(true).
					setCircularRedirectsAllowed(false).
					build();
		httpGet.setConfig(config);
		return httpGet;
	}

	public static JenkinsResourceProxy create(URI location) {
		checkNotNull(location,"Resource location cannot be null");
		return new JenkinsResourceProxy(location,JenkinsEntityType.INSTANCE,false);
	}

}
