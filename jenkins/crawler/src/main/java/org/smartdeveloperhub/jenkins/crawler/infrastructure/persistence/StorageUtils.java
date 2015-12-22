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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.Digest;
import org.smartdeveloperhub.jenkins.DigestService;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.JenkinsResource.Metadata;
import org.smartdeveloperhub.jenkins.JenkinsResource.Metadata.Filter;
import org.smartdeveloperhub.jenkins.ResponseBody;
import org.smartdeveloperhub.jenkins.ResponseBodyBuilder;
import org.smartdeveloperhub.jenkins.ResponseExcerpt;
import org.smartdeveloperhub.jenkins.Status;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.BodyType;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.DigestType;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.FilterType;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.RepresentationDescriptor;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.ResourceDescriptor;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.ResourceDescriptor.Filters;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.ResourceDescriptorDocument;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.ResponseDescriptor;
import org.smartdeveloperhub.util.xml.XmlUtils;
import org.w3c.dom.Document;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

final class StorageUtils {

	private static final class PersistentJenkinsResource implements JenkinsResource {

		private static final class ImmutableFilter implements Filter {

			private final String expression;

			private ImmutableFilter(final String expression) {
				this.expression = expression;
			}

			@Override
			public String expression() {
				return this.expression;
			}

			@Override
			public String toString() {
				return this.expression;
			}

		}

		private final class ImmutableResponseExcerpt implements ResponseExcerpt {

			@Override
			public String etag() {
				return PersistentJenkinsResource.this.response.getEtag();
			}

			@Override
			public Date lastModified() {
				return PersistentJenkinsResource.this.lastModified;
			}

			@Override
			public Optional<ResponseBody> body() {
				return Optional.fromNullable(PersistentJenkinsResource.this.body);
			}

			@Override
			public int statusCode() {
				return PersistentJenkinsResource.this.response.getStatusCode();
			}

			@Override
			public String toString() {
				return
					MoreObjects.
						toStringHelper(getClass()).
							omitNullValues().
							add("statusCode",statusCode()).
							add("etag",etag()).
							add("lastModified",PersistentJenkinsResource.this.lastModified).
							add("body",PersistentJenkinsResource.this.body).
							toString();
			}
		}

		private final class ImmutableMetadata implements Metadata {

			private final ResponseExcerpt responseExcerpt;
			private final List<Filter> filters;

			private ImmutableMetadata() {
				this.responseExcerpt = new ImmutableResponseExcerpt();
				this.filters=createFilters(PersistentJenkinsResource.this.resource.getFilters().getFilters());
			}

			private List<Filter> createFilters(final List<FilterType> filterDefinitions) {
				final Builder<Filter> result=ImmutableList.builder();
				for(final FilterType filterDefinition:filterDefinitions) {
					result.add(new ImmutableFilter(filterDefinition.getExpression()));
				}
				return result.build();
			}

			@Override
			public List<Filter> filters() {
				return this.filters;
			}

			@Override
			public Date retrievedOn() {
				return PersistentJenkinsResource.this.representation.getRetrievedOn().toDate();
			}

			@Override
			public String serverVersion() {
				return PersistentJenkinsResource.this.response.getServerVersion();
			}

			@Override
			public ResponseExcerpt response() {
				return this.responseExcerpt;
			}

			@Override
			public String toString() {
				return
					MoreObjects.
						toStringHelper(getClass()).
							omitNullValues().
							add("filters",this.filters).
							add("retrievedOn",retrievedOn()).
							add("serverVersion",serverVersion()).
							add("response",response()).
							toString();
			}
		}

		private final ResourceDescriptorDocument resource;
		private final ResponseDescriptor response;
		private final RepresentationDescriptor representation;
		private final Metadata metadata;

		private ResponseBody body;
		private Document content;
		private Throwable failure;

		private final Date lastModified;

		private PersistentJenkinsResource(final ResourceDescriptorDocument resource) {
			this.resource = resource;
			this.representation = this.resource.getRepresentation();
			this.response = this.representation.getResponse();
			this.lastModified=date(this.response.getLastModified());
			this.metadata = new ImmutableMetadata();
		}

		private PersistentJenkinsResource withBody(final ResponseBody body) {
			this.body=body;
			return this;
		}

		private PersistentJenkinsResource withFailure(final Throwable failure) {
			this.failure = failure;
			return this;
		}

		private PersistentJenkinsResource withContent(final Document content) {
			this.content = content;
			return this;
		}

		@Override
		public URI location() {
			return this.resource.getUrl();
		}

		@Override
		public JenkinsEntityType entity() {
			return this.resource.getEntity();
		}

		@Override
		public JenkinsArtifactType artifact() {
			return this.resource.getArtifact();
		}

		@Override
		public Status status() {
			return this.representation.getStatus();
		}

		@Override
		public Optional<Throwable> failure() {
			return Optional.fromNullable(this.failure);
		}

		@Override
		public Optional<Document> content() {
			return Optional.fromNullable(this.content);
		}

		@Override
		public Metadata metadata() {
			return this.metadata;
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
						add("metadata",this.metadata).
						toString();
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(StorageUtils.class);

	private StorageUtils() {
	}

	private static DateTime dateTime(final Date date) {
		DateTime result=null;
		if(date!=null) {
			result=new DateTime(date.getTime());
		}
		return result;
	}

	private static Date date(final DateTime dateTime) {
		Date result=null;
		if(dateTime!=null) {
			result=dateTime.toDate();
		}
		return result;
	}

	private static void populateFailure(final PersistentJenkinsResource result, final URI entity, final byte[] rawFailure) {
		if(rawFailure==null) {
			return;
		}
		try {
			result.withFailure(SerializationUtils.deserialize(rawFailure,Throwable.class));
		} catch (final Exception e) {
			throw new IllegalStateException("Could not load failure for resource '"+entity+"'",e);
		}
	}

	private static void populateBodyAndContent(final PersistentJenkinsResource result, final URI entity, final BodyType body) {
		try {
			final String rawBody=IOUtils.toString(body.getExternal());
			result.withBody(
				new ResponseBodyBuilder().
					withContent(rawBody).
					withDigest(toDigest(body.getDigest())).
					withContentType(body.getContentType()).
					withEncoding(body.getEncoding()).
					build()
			);
			if("application/xml".equals(body.getContentType())) {
				result.withContent(XmlUtils.toDocument(rawBody));
			}
		} catch (final Exception e) {
			throw new IllegalStateException("Could not load body of '"+entity+"' from '"+body.getExternal()+"'",e);
		}
	}

	private static Digest toDigest(final DigestType digestDescriptor) {
		if(digestDescriptor==null) {
			return null;
		}
		return
			DigestService.
				assembleDigest(
					digestDescriptor.getValue(),
					digestDescriptor.getAlgorithm());
	}

	static ResourceDescriptorDocument toResourceDescriptorDocument(final JenkinsResource resource, final File external) {
		checkNotNull(resource,"Representation cannot be null");

		final Metadata metadata = resource.metadata();
		final ResponseExcerpt response = metadata.response();

		final ResponseDescriptor responseDescriptor =
			new ResponseDescriptor().
				withStatusCode(response.statusCode()).
				withEtag(response.etag()).
				withLastModified(dateTime(response.lastModified())).
				withServerVersion(metadata.serverVersion());

		if(response.body().isPresent()) {
			final ResponseBody rBody = response.body().get();
			final DigestType digest=
				new DigestType().
					withAlgorithm(rBody.digest().algorithm()).
					withValue(rBody.digest().value());

			final BodyType body=
				new BodyType().
					withDigest(digest).
					withContentType(rBody.contentType()).
					withEncoding(rBody.encoding()).
					withExternal(external.toURI());

			responseDescriptor.setBody(body);
		}

		final RepresentationDescriptor representationDescriptor=
			new RepresentationDescriptor().
				withRetrievedOn(dateTime(metadata.retrievedOn())).
				withStatus(resource.status()).
				withResponse(responseDescriptor);

		if(resource.failure().isPresent()) {
			try {
				representationDescriptor.
					withFailure(SerializationUtils.serialize(resource.failure().get()));
			} catch (final IOException e) {
				logAndDiscard(resource, e);
			}
		}

		final Filters filters=new ResourceDescriptor.Filters();
		for(final Filter filter:resource.metadata().filters()) {
			filters.
				getFilters().
					add(new FilterType().withExpression(filter.expression()));
		}

		return
			new ResourceDescriptorDocument().
				withUrl(resource.location()).
				withFilters(filters).
				withEntity(resource.entity()).
				withArtifact(resource.artifact()).
				withRepresentation(representationDescriptor);
	}

	private static void logAndDiscard(final JenkinsResource resource, final Throwable e) {
		LOGGER.error("Could not serialize failure ({}):",e.getMessage(),resource.failure().get());
	}

	static JenkinsResource toJenkinsResource(final ResourceDescriptorDocument descriptor) {
		final PersistentJenkinsResource result = new PersistentJenkinsResource(descriptor);
		final URI entity = descriptor.getUrl();
		final RepresentationDescriptor representation = descriptor.getRepresentation();
		if(representation!=null) {
			final BodyType body = representation.getResponse().getBody();
			if(body!=null) {
				populateBodyAndContent(result,entity,body);
			}
			populateFailure(result,entity,representation.getFailure());
		}
		return result;
	}

	static void prepareWorkingDirectory(final File workingDirectory) {
		if(!workingDirectory.exists()) {
			workingDirectory.mkdirs();
		}
		checkArgument(workingDirectory.isDirectory(),"Working directory path %s is not a directory",workingDirectory.getAbsolutePath());
		checkArgument(workingDirectory.canWrite(),"Working directory %s cannot be written",workingDirectory.getAbsolutePath());
	}

	static StorageAllocationStrategy instantiateStrategy(final String className) throws IOException {
		try {
			return StorageAllocationStrategy.class.cast(Class.forName(className).newInstance());
		} catch (final Exception e) {
			throw new IOException("Could not instantiate strategy '"+className+"'",e);
		}
	}

}
