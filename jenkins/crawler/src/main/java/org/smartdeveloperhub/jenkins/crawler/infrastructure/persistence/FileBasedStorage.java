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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.ResourceRepository;
import org.smartdeveloperhub.jenkins.ResponseBody;
import org.smartdeveloperhub.jenkins.client.JenkinsClientException;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.EntityRepository;
import org.smartdeveloperhub.jenkins.crawler.xml.jenkins.ResourceDescriptorDocument;
import org.smartdeveloperhub.jenkins.crawler.xml.persistence.Storage;
import org.smartdeveloperhub.jenkins.crawler.xml.persistence.StorageDescriptor;
import org.smartdeveloperhub.jenkins.crawler.xml.persistence.StorageEntryType;
import org.smartdeveloperhub.jenkins.util.xml.XmlUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class FileBasedStorage implements EntityRepository, ResourceRepository {

	interface StorageAction {
		String id();
		File targetFile();
		boolean isXML();
		Source dataSource();
		String rawData();
	}

	private static final class ImmutableStorageAction implements StorageAction {

		private final Source source;
		private final File file;
		private final String id;
		private final String rawData;

		private ImmutableStorageAction(String rawData, Source source, File file, String id) {
			this.rawData = rawData;
			this.source = source;
			this.file = file;
			this.id = id;
		}

		@Override
		public String id() {
			return this.id;
		}

		@Override
		public File targetFile() {
			return this.file;
		}

		@Override
		public Source dataSource() {
			return this.source;
		}

		@Override
		public boolean isXML() {
			return this.source!=null;
		}

		@Override
		public String rawData() {
			return this.rawData;
		}
	}

	private final class StorageActionBuilder {

		private final class ContentSelectorBuilder {

			private String id;
			private File file;

			private ContentSelectorBuilder(String id, File file) {
				this.id = id;
				this.file = file;
			}

			private void createXmlAction(String rawData) {
				StreamSource source = new StreamSource(new StringReader(rawData));
				actions.add(new ImmutableStorageAction(rawData,source,this.file,this.id));
			}

			StorageActionBuilder withEntity(Object entity) {
				if(entity==null) {
					LOGGER.warn("No entity available [{}]",id);
				} else {
					String rawData=
						XmlUtils.
							marshall(
								entity,
								new JenkinsClientException("Could not marshall entity "+entity));
					createXmlAction(rawData);
				}
				return StorageActionBuilder.this;
			}

			StorageActionBuilder withXmlData(String rawData) {
				if(rawData==null) {
					LOGGER.warn("No data available [{}]",id);
				} else {
					createXmlAction(rawData);
				}
				return StorageActionBuilder.this;
			}

			StorageActionBuilder withPlainTextData(String rawData) {
				if(rawData==null) {
					LOGGER.warn("No data available [{}]",id);
				} else {
					actions.add(new ImmutableStorageAction(rawData,null,file,id));
				}
				return StorageActionBuilder.this;
			}

		}

		private final List<StorageAction> actions;
		private final URI location;
		private final JenkinsEntityType entity;

		private StorageActionBuilder(URI location, JenkinsEntityType entity) {
			this.location=location;
			this.entity=entity;
			this.actions=Lists.newArrayList();
		}

		StorageActionBuilder addResource(JenkinsResource resource) {
			final File rawDataFile=FileBasedStorage.this.strategy.allocateArtifact(this.location,this.entity,resource.artifact());
			final File resourceFile=FileBasedStorage.this.strategy.allocateResource(this.location,this.entity,resource.artifact());
			Optional<ResponseBody> body=resource.metadata().response().body();
			if(body.isPresent()) {
				ResponseBody responseBody = body.get();
				String content = responseBody.content();
				if(responseBody.contentType().equals("application/xml")) {
					new ContentSelectorBuilder("raw data",rawDataFile).withXmlData(content);
				} else {
					new ContentSelectorBuilder("raw data",rawDataFile).withPlainTextData(content);
				}
			}
			new ContentSelectorBuilder("resource",resourceFile).
				withEntity(StorageUtils.toResourceDescriptorDocument(resource, rawDataFile));
			return this;
		}

		StorageActionBuilder addEntity(Object entity) {
			final File file=FileBasedStorage.this.strategy.allocateEntity(this.location,this.entity,JenkinsArtifactType.RESOURCE);
			new ContentSelectorBuilder("entity",file).
				withEntity(entity);
			return this;
		}

		StorageAction[] build() {
			return this.actions.toArray(new StorageAction[this.actions.size()]);
		}

	}

	private static final class StoreOperation implements LockManager.RunnableOperation<IOException> {

		private StorageAction[] actions;
		private URI location;

		private StoreOperation(URI location,StorageAction... actions) {
			this.location = location;
			this.actions = actions;
		}

		private void preparePath(File targetFile) {
			File parent = targetFile.getParentFile();
			if(!parent.exists()) {
				parent.mkdirs();
			}
		}

		@Override
		public void execute() throws IOException {
			for(StorageAction action:actions) {
				preparePath(action.targetFile());
				if(action.isXML()) {
					XmlUtils.marshall(action.targetFile(),action.dataSource());
				} else {
					FileUtils.write(action.targetFile(),action.rawData());
				}
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Stored resource {} {} at file {}",this.location,action.id(),action.targetFile().getAbsolutePath());
				}
			}
		}

	}

	public static final class FileBasedStorageBuilder {

		private File workingDirectory;
		private File configFile;
		private StorageAllocationStrategy strategy;

		private FileBasedStorageBuilder() {
			this.workingDirectory=new File(".");
			this.configFile=new File("repository.xml");
			this.strategy=new DefaultStorageAllocationStrategy();
		}

		public FileBasedStorageBuilder withWorkingDirectory(File workingDirectory) {
			if(workingDirectory!=null) {
				this.workingDirectory = workingDirectory;
			}
			return this;
		}

		public FileBasedStorageBuilder withConfigFile(File configFile) {
			if(configFile!=null) {
				this.configFile=configFile;
			}
			return this;
		}

		public FileBasedStorageBuilder withStrategy(StorageAllocationStrategy strategy) {
			if(strategy!=null) {
				this.strategy=strategy;
			}
			return this;
		}

		public FileBasedStorage build() throws IOException {
			FileBasedStorage result=new FileBasedStorage();
			if(this.configFile.exists()) {
				checkArgument(this.configFile.isFile(),"Configuration file path %s should point to a file",configFile.getAbsolutePath());
				checkArgument(this.configFile.canWrite(),"Configuration file %s cannot be written",configFile.getAbsolutePath());
				result.fromConfigFile(this.configFile);
			} else {
				StorageUtils.prepareWorkingDirectory(this.workingDirectory);
				this.strategy.setWorkingDirectory(this.workingDirectory);
				result.
					setConfigFile(this.configFile).
					setStrategy(this.strategy);
			}
			return result;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(FileBasedStorage.class);

	private final LockManager locker;
	private final Map<URI,StorageEntry> registeredEntities;

	private StorageAllocationStrategy strategy;
	private File configFile;

	private FileBasedStorage() {
		this.locker=new LockManager();
		this.registeredEntities=Maps.newLinkedHashMap();
	}

	private FileBasedStorage setStrategy(StorageAllocationStrategy strategy) {
		this.strategy=strategy;
		return this;
	}

	private FileBasedStorage setConfigFile(File configFile) {
		this.configFile = configFile;
		return this;
	}

	private StorageEntry entry(JenkinsResource resource) {
		return entry(resource.location(),resource.entity());
	}

	private StorageEntry entry(URI location, JenkinsEntityType entityType) {
		StorageEntry entry = this.registeredEntities.get(location);
		if(entry==null) {
			entry=new StorageEntry(location,entityType);
			registerEntry(entry);
		}
		return entry;
	}

	private void registerEntry(StorageEntry entry) {
		this.registeredEntities.put(entry.location(), entry);
	}

	private Storage toDescriptor() {
		StorageDescriptor descriptor=new StorageDescriptor();
		descriptor.
			withWorkingDirectory(this.strategy.workingDirectory().toURI()).
			withStrategy(this.strategy.getClass().getCanonicalName());
		for(StorageEntry entry:this.registeredEntities.values()) {
			descriptor.getEntries().add(entry.toDescriptor());
		}
		return descriptor;
	}

	private void fromConfigFile(File configFile) throws IOException {
		Storage descriptor=
			XmlUtils.
				unmarshall(
					configFile,
					Storage.class,
					new IOException("Could not parse configuration file '"+configFile.getAbsolutePath()+"'"));


		File workingDirectory=new File(descriptor.getWorkingDirectory());
		StorageUtils.prepareWorkingDirectory(workingDirectory);

		StorageAllocationStrategy strategy=
			StorageUtils.
				instantiateStrategy(descriptor.getStrategy());
		strategy.setWorkingDirectory(workingDirectory);

		setConfigFile(configFile).
		setStrategy(strategy);

		for(StorageEntryType entry:descriptor.getEntries()) {
			registerEntry(StorageEntry.fromDescriptor(entry));
		}

	}

	public StorageAllocationStrategy strategy() {
		return this.strategy;
	}

	public File workingDirectory() {
		return this.strategy.workingDirectory();
	}

	public File configFile() {
		return this.configFile;
	}

	public void save() throws IOException {
		XmlUtils.
			marshall(
				toDescriptor(),
				this.configFile,
				new IOException("Could not persist repository configuration"));
	}

	@Override
	public void saveResource(JenkinsResource resource) throws IOException {
		URI location = resource.location();
		StorageAction[] actions =
			new StorageActionBuilder(location,resource.entity()).
				addResource(resource).
				build();
		this.locker.write(location,new StoreOperation(location,actions));
		entry(resource).persistArtifact(resource.artifact());
	}

	@Override
	public JenkinsResource findResource(URI location, JenkinsArtifactType artifact) throws IOException {
		StorageEntry entry=this.registeredEntities.get(location);
		if(entry!=null && entry.isArtifactPersisted(artifact)) {
			File file=this.strategy.allocateResource(entry.location(), entry.type(), artifact);
			ResourceDescriptorDocument descriptor=
				XmlUtils.
					unmarshall(
						file,
						ResourceDescriptorDocument.class,
						new AssertionError("File '"+file.getAbsolutePath()+"' should store a "+ResourceDescriptorDocument.class));
			return StorageUtils.toJenkinsResource(descriptor);
		}
		return null;
	}

	@Override
	public void saveEntity(Entity entity, JenkinsEntityType entityType) throws IOException {
		URI location = entity.getUrl();
		StorageAction[] actions =
			new StorageActionBuilder(location,entityType).
				addEntity(entity).
				build();
		this.locker.write(location,new StoreOperation(location,actions));
		entry(location,entityType).persistEntity();;
	}

	@Override
	public <T extends Entity> T entityOfId(URI location, Class<? extends T> entityClass) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	public static FileBasedStorageBuilder builder() {
		return new FileBasedStorageBuilder();
	}
}
