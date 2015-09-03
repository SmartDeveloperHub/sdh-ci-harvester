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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.1.0
 *   Bundle      : ci-jenkins-crawler-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.infrastructure.transformation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationException;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationService;
import org.smartdeveloperhub.util.xml.XmlUtils;

import com.google.common.collect.Maps;

public final class TransformationManager implements TransformationService {

	private static final Logger LOGGER=LoggerFactory.getLogger(TransformationManager.class);

	private final TransformerFactory transformerFactory;
	private final Map<String,Templates> templates;
	private final Lock lock;

	private final JAXBContext context;

	private TransformationManager(JAXBContext context) {
		this.context = context;
		this.transformerFactory = TransformerFactory.newInstance();
		this.templates=Maps.newLinkedHashMap();
		this.lock=new ReentrantLock();
	}

	private Templates getTemplate(String entity) throws TransformationException {
		this.lock.lock();
		try {
			Templates result = this.templates.get(entity);
			if(result==null) {
				URL styleSheetResource = findResource(entity);
				result=loadResource(styleSheetResource);
				this.templates.put(entity, result);
				LOGGER.trace("Memoized transformation template for entity '{}' using stylesheet at '{}'.",entity,styleSheetResource);
			}
			return result;
		} finally {
			this.lock.unlock();
		}
	}

	private Templates loadResource(URL styleSheetResource) throws TransformationException {
		LOGGER.trace("Loading stylesheet at '{}'...",styleSheetResource);
		InputStream is=null;
		try {
			is=styleSheetResource.openStream();
			StreamSource source=new StreamSource(is);
			LOGGER.trace("Stylesheet at '{}' loaded.",styleSheetResource);
			return this.transformerFactory.newTemplates(source);
		} catch (IOException e) {
			throw new TransformationException("Could not load stylesheet at '"+styleSheetResource+"'",e);
		} catch (TransformerConfigurationException e) {
			throw new TransformationException("Could not prepare stylesheet at '"+styleSheetResource+"'",e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private URL findResource(String entity) throws TransformationException {
		LOGGER.trace("Looking for a stylesheet for entity '{}'",entity);
		URL styleSheetResource=
			Thread.
				currentThread().
					getContextClassLoader().
						getResource("xslt/"+entity+".xslt");
		if(styleSheetResource==null) {
			LOGGER.error("Could not find a stylesheet for entity '{}' (xslt/{}.xslt)",entity,entity);
			throw new TransformationException("Could not find a stylesheet for entity '"+entity+"'");
		}
		LOGGER.trace("Found stylesheet for entity '{}' at '{}'",entity,styleSheetResource);
		return styleSheetResource;
	}

	private void doTransform(String entity, Source source, Result result, Map<String, Object> parameters) throws TransformationException {
		Templates template=getTemplate(entity);
		try {
			Transformer transformer=template.newTransformer();
			if(parameters!=null) {
				for(Entry<String,Object> entry:parameters.entrySet()) {
					transformer.setParameter(entry.getKey(),entry.getValue());
				}
			}
			transformer.transform(source,result);
		} catch (TransformerConfigurationException e) {
			throw new TransformationException("Could not prepare transformation",e);
		} catch (TransformerException e) {
			throw new TransformationException("Transformation failed",e);
		}
	}

	@Override
	public String transform(String entity, Source data, Map<String,Object> parameters) throws TransformationException {
		StringResultAdapter adapter = new StringResultAdapter();
		doTransform(entity, data, adapter.getResult(), parameters);
		return adapter.getValue();
	}

	@Override
	public <T> T transform(String entity, Source data, Map<String,Object> parameters, Class<? extends T> clazz) throws TransformationException {
		try {
			JAXBResult result=new JAXBResult(this.context);
			doTransform(entity,data,result,parameters);
			return clazz.cast(result.getResult());
		} catch (ClassCastException e) {
			throw new TransformationException("Could not transform data to '"+clazz.getCanonicalName()+"'",e);
		} catch (JAXBException e) {
			throw new TransformationException("Could not transform data to '"+clazz.getCanonicalName()+"'",e);
		}
	}

	public static TransformationManager newInstance() {
		return new TransformationManager(XmlUtils.context());
	}

}