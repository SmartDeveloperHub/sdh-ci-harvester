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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-util:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-util-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.util.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.util.xml.spi.XmlRegistryProvider;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;

public final class XmlUtils {

	private static final Logger LOGGER=LoggerFactory.getLogger(XmlUtils.class);

	private static final JAXBContext CONTEXT;

	private static final XPathFactory FACTORY;

	static {
		FACTORY=XPathFactory.newInstance();
		List<Class<?>> xmlRegistries = getXmlRegistries();
		try {
			CONTEXT=
				JAXBContext.
					newInstance(xmlRegistries.toArray(new Class<?>[0]));
			LOGGER.debug("Created JAXB context using {}",getContextPath(xmlRegistries));
		} catch (JAXBException e) {
			String errorMessage=String.format("Could not initialize JAXB context using %s",getContextPath(xmlRegistries));
			LOGGER.warn(errorMessage.concat(". Full stacktrace follows"),e);
			throw new IllegalStateException(errorMessage,e);
		}
	}

	private XmlUtils() {
	}

	private static List<Class<?>> getXmlRegistries() {
		ServiceLoader<XmlRegistryProvider> providers=ServiceLoader.load(XmlRegistryProvider.class);
		List<Class<?>> result=Lists.newArrayList();
		for(XmlRegistryProvider provider:providers) {
			LOGGER.trace("Loading XmlRegistry classes from {}",provider.getClass().getCanonicalName());
			for(Class<?> xmlRegistry:provider.getXmlRegistries()) {
				if(xmlRegistry.getAnnotation(XmlRegistry.class)!=null) {
					result.add(xmlRegistry);
					LOGGER.trace(" - Added XmlRegistry {}",xmlRegistry.getCanonicalName());
				} else {
					LOGGER.trace(" - Discarded invalid class {}",xmlRegistry.getCanonicalName());
				}
			}
		}
		return result;
	}

	private static String getContextPath(List<Class<?>> classes) {
		StringBuilder builder=new StringBuilder();
		String next="";
		for(Class<?> xmlRegistry:classes) {
			builder.append(next).append(xmlRegistry.getCanonicalName());
			next=", ";
		}
		return builder.toString();
	}

	private static Transformer getTransformer() throws TransformerConfigurationException {
		TransformerFactory transformerFactory=TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"false");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "true");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "true");
		return transformer;
	}

	public static JAXBContext context() {
		return XmlUtils.CONTEXT;
	}

	public static String evaluateXPath(String xpath, String source) throws XmlProcessingException {
		XPath newXPath = FACTORY.newXPath();
		try {
			XPathExpression compile = newXPath.compile(xpath);
			return compile.evaluate(new InputSource(new StringReader(source)));
		} catch (XPathExpressionException e) {
			throw new XmlProcessingException("Invalid XPath expression",e);
		}
	}

	public static String evaluateXPath(String xpath, Document content) throws XmlProcessingException {
		XPath newXPath = FACTORY.newXPath();
		try {
			XPathExpression compile = newXPath.compile(xpath);
			return compile.evaluate(content);
		} catch (XPathExpressionException e) {
			throw new XmlProcessingException("Invalid XPath expression",e);
		}
	}

	public static String toString(Document document) throws XmlProcessingException {
		try {
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			DOMSource source=new DOMSource(document);
			getTransformer().
				transform(source, result);
			return writer.toString();
		} catch(Exception e) {
			throw new XmlProcessingException("Resource storage failure",e);
		}
	}

	public static Document toDocument(String body) throws XmlProcessingException {
		try {
			DocumentBuilder builder =
				DocumentBuilderFactory.
					newInstance().
						newDocumentBuilder();
			return
				builder.
					parse(new ByteArrayInputStream(body.getBytes()));
		} catch (Exception e) {
			throw new XmlProcessingException("Resource storage failure",e);
		}
	}

	public static <T, E extends Throwable> T unmarshall(File source, Class<? extends T> clazz, E throwable) throws E {
		try {
			Unmarshaller unmarshaller=CONTEXT.createUnmarshaller();
			Object unmarshal = unmarshaller.unmarshal(new FileInputStream(source));
			return clazz.cast(unmarshal);
		} catch (Exception e) {
			throwable.initCause(e);
			throw throwable;
		}
	}

	public static <E extends Throwable> void marshall(Object entity, File target, E throwable) throws E {
		try {
			Marshaller marshaller=CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
			marshaller.marshal(entity,target);
		} catch (JAXBException e) {
			throwable.initCause(e);
			throw throwable;
		}
	}

	public static <T, E extends Throwable> T unmarshall(String content, Class<? extends T> clazz, E throwable) throws E {
		try {
			Unmarshaller unmarshaller=CONTEXT.createUnmarshaller();
			Object unmarshal = unmarshaller.unmarshal(new StringReader(content));
			return clazz.cast(unmarshal);
		} catch (Exception e) {
			throwable.initCause(e);
			throw throwable;
		}
	}

	public static <E extends Throwable> String marshall(Object entity, E throwable) throws E {
		try {
			Marshaller marshaller=CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
			StringWriter writer = new StringWriter();
			marshaller.marshal(entity,writer);
			return writer.toString();
		} catch (JAXBException e) {
			throwable.initCause(e);
			throw throwable;
		}
	}

	public static void marshall(File file, Source source) throws XmlProcessingException {
		try {
			getTransformer().
				transform(source, new StreamResult(file));
		} catch(Exception e) {
			throw new XmlProcessingException("Resource storage failure",e);
		}
	}

}
