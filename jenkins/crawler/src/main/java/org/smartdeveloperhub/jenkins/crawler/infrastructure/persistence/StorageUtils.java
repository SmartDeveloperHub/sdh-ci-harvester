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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.smartdeveloperhub.jenkins.Digest;
import org.smartdeveloperhub.jenkins.DigestService;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.JenkinsResource.Metadata;
import org.smartdeveloperhub.jenkins.ResponseBody;
import org.smartdeveloperhub.jenkins.ResponseBodyBuilder;
import org.smartdeveloperhub.jenkins.ResponseExcerpt;
import org.smartdeveloperhub.jenkins.Status;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.BodyType;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.DigestType;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.RepresentationDescriptor;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.ResourceDescriptorDocument;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.ResponseDescriptor;
import org.smartdeveloperhub.jenkins.util.xml.XmlUtils;
import org.w3c.dom.Document;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

final class StorageUtils {

	private static final class PersistentJenkinsResource implements JenkinsResource {

		private final ResourceDescriptorDocument resource;
		private final ResponseDescriptor response;
		private final RepresentationDescriptor representation;

		private ResponseBody body;
		private Document content;
		private Throwable failure;

		private Date lastModified;

		private PersistentJenkinsResource(ResourceDescriptorDocument resource) {
			this.resource = resource;
			this.representation = this.resource.getRepresentation();
			this.response = this.representation.getResponse();
			this.lastModified=date(response.getLastModified());
		}

		private PersistentJenkinsResource withBody(ResponseBody body) {
			this.body=body;
			return this;
		}

		private PersistentJenkinsResource withFailure(Throwable failure) {
			this.failure = failure;
			return this;
		}

		private PersistentJenkinsResource withContent(Document content) {
			this.content = content;
			return this;
		}

		@Override
		public URI location() {
			return resource.getUrl();
		}

		@Override
		public JenkinsEntityType entity() {
			return resource.getEntity();
		}

		@Override
		public JenkinsArtifactType artifact() {
			return resource.getArtifact();
		}

		@Override
		public Status status() {
			return representation.getStatus();
		}

		@Override
		public Optional<Throwable> failure() {
			return Optional.fromNullable(failure);
		}

		@Override
		public Optional<Document> content() {
			return Optional.fromNullable(content);
		}

		@Override
		public Metadata metadata() {
			return new Metadata() {
				@Override
				public Date retrievedOn() {
					return representation.getRetrievedOn().toDate();
				}
				@Override
				public String serverVersion() {
					return response.getServerVersion();
				}

				@Override
				public ResponseExcerpt response() {
					return new ResponseExcerpt() {
						@Override
						public String etag() {
							return response.getEtag();
						}
						@Override
						public Date lastModified() {
							return lastModified;
						}
						@Override
						public Optional<ResponseBody> body() {
							return Optional.fromNullable(body);
						}
						@Override
						public int statusCode() {
							return response.getStatusCode();
						}
						@Override
						public String toString() {
							return
								MoreObjects.
									toStringHelper(getClass()).
										omitNullValues().
										add("statusCode",statusCode()).
										add("etag",etag()).
										add("lastModified",lastModified).
										add("body",body).
										toString();
						}
					};
				}

				@Override
				public String toString() {
					return
						MoreObjects.
							toStringHelper(getClass()).
								omitNullValues().
								add("retrievedOn",retrievedOn()).
								add("serverVersion",serverVersion()).
								add("response",response()).
								toString();
				}
			};
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
						add("location",location()).
						add("entity",entity()).
						add("artifact",artifact()).
						add("status",status()).
						add("failure",this.failure).
						add("content",this.content).
						add("metadata",metadata()).
						toString();
		}

	}

	private StorageUtils() {
	}

	private static DateTime dateTime(Date date) {
		DateTime result=null;
		if(date!=null) {
			result=new DateTime(date.getTime());
		}
		return result;
	}

	private static Date date(DateTime dateTime) {
		Date result=null;
		if(dateTime!=null) {
			result=dateTime.toDate();
		}
		return result;
	}

	private static void populateFailure(PersistentJenkinsResource result, URI entity, byte[] rawFailure) {
		if(rawFailure==null) {
			return;
		}
		try {
			result.withFailure(SerializationUtils.deserialize(rawFailure,Throwable.class));
		} catch (Exception e) {
			throw new IllegalStateException("Could not load failure for resource '"+entity+"'",e);
		}
	}

	private static void populateBodyAndContent(PersistentJenkinsResource result, URI entity, BodyType body) {
		try {
			String rawBody=IOUtils.toString(body.getExternal());
			result.withBody(
				new ResponseBodyBuilder().
					withContent(rawBody).
					withDigest(toDigest(body.getDigest())).
					withContentType(body.getContentType()).
					build()
			);
			if(body.getContentType().equals("application/xml")) {
				result.withContent(XmlUtils.toDocument(rawBody));
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not load body of '"+entity+"' from '"+body.getExternal()+"'",e);
		}
	}

	private static Digest toDigest(DigestType digestDescriptor) {
		if(digestDescriptor==null) {
			return null;
		}
		return
			DigestService.
				assembleDigest(
					digestDescriptor.getValue(),
					digestDescriptor.getAlgorithm());
	}

	static ResourceDescriptorDocument toResourceDescriptorDocument(JenkinsResource resource, File external) {
		checkNotNull(resource,"Representation cannot be null");

		Metadata metadata = resource.metadata();
		ResponseExcerpt response = metadata.response();

		ResponseDescriptor responseDescriptor =
			new ResponseDescriptor().
				withStatusCode(response.statusCode()).
				withEtag(response.etag()).
				withLastModified(dateTime(response.lastModified())).
				withServerVersion(metadata.serverVersion());

		if(response.body().isPresent()) {
			ResponseBody rBody = response.body().get();
			DigestType digest=
				new DigestType().
					withAlgorithm(rBody.digest().algorithm()).
					withValue(rBody.digest().value());

			BodyType body=
				new BodyType().
					withDigest(digest).
					withContentType(rBody.contentType()).
					withExternal(external.toURI());

			responseDescriptor.setBody(body);
		}

		RepresentationDescriptor representationDescriptor=
			new RepresentationDescriptor().
				withRetrievedOn(dateTime(metadata.retrievedOn())).
				withStatus(resource.status()).
				withResponse(responseDescriptor);

		if(resource.failure().isPresent()) {
			representationDescriptor.
				withFailure(SerializationUtils.serialize(resource.failure().get()));
		}

		ResourceDescriptorDocument resourceDescriptor=
			new ResourceDescriptorDocument().
				withUrl(resource.location()).
				withEntity(resource.entity()).
				withArtifact(resource.artifact()).
				withRepresentation(representationDescriptor);

		return resourceDescriptor;
	}

	static JenkinsResource toJenkinsResource(ResourceDescriptorDocument descriptor) {
		PersistentJenkinsResource result = new PersistentJenkinsResource(descriptor);
		URI entity = descriptor.getUrl();
		RepresentationDescriptor representation = descriptor.getRepresentation();
		if(representation!=null) {
			BodyType body = representation.getResponse().getBody();
			if(body!=null) {
				populateBodyAndContent(result,entity,body);
			}
			populateFailure(result,entity,representation.getFailure());
		}
		return result;
	}

	static void prepareWorkingDirectory(File workingDirectory) {
		if(!workingDirectory.exists()) {
			workingDirectory.mkdirs();
		}
		checkArgument(workingDirectory.isDirectory(),"Working directory path %s is not a directory",workingDirectory.getAbsolutePath());
		checkArgument(workingDirectory.canWrite(),"Working directory %s cannot be written",workingDirectory.getAbsolutePath());
	}

	static StorageAllocationStrategy instantiateStrategy(String className) throws IOException {
		try {
			return StorageAllocationStrategy.class.cast(Class.forName(className).newInstance());
		} catch (Exception e) {
			throw new IOException("Could not instantiate strategy '"+className+"'",e);
		}
	}

}
