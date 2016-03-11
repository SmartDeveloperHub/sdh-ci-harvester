/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-xml:0.3.0-SNAPSHOT
 *   Bundle      : ci-util-xml-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.xml;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.smartdeveloperhub.util.xml.spi.XmlRegistryProvider;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;

public final class XmlUtils {

	private static final String RESOURCE_STORAGE_FAILURE = "Resource storage failure";
	private static final String INVALID_XPATH_EXPRESSION = "Invalid XPath expression '%s'";

	private static final Logger LOGGER=LoggerFactory.getLogger(XmlUtils.class);

	private static final JAXBContext CONTEXT;
	private static final XPathFactory FACTORY;

	static {
		FACTORY=XPathFactory.newInstance();
		final List<Class<?>> xmlRegistries = getXmlRegistries();
		try {
			CONTEXT=
				JAXBContext.
					newInstance(xmlRegistries.toArray(new Class<?>[0]));
			LOGGER.debug("Created JAXB context using context path [{}]",getContextPath(xmlRegistries));
		} catch (final JAXBException e) {
			final String errorMessage=String.format("Could not initialize JAXB context using %s",getContextPath(xmlRegistries));
			LOGGER.warn(errorMessage.concat(". Full stacktrace follows"),e);
			throw new IllegalStateException(errorMessage,e);
		}
	}

	private XmlUtils() {
	}

	private static List<Class<?>> getXmlRegistries() {
		final ServiceLoader<XmlRegistryProvider> providers=ServiceLoader.load(XmlRegistryProvider.class);
		final List<Class<?>> result=Lists.newArrayList();
		for(final XmlRegistryProvider provider:providers) {
			LOGGER.trace("Loading XmlRegistry classes from {}",provider.getClass().getCanonicalName());
			for(final Class<?> xmlRegistry:provider.getXmlRegistries()) {
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

	private static String getContextPath(final List<Class<?>> classes) {
		final StringBuilder builder=new StringBuilder();
		String next="";
		for(final Class<?> xmlRegistry:classes) {
			builder.append(next).append(xmlRegistry.getCanonicalName());
			next=", ";
		}
		return builder.toString();
	}

	private static Transformer getTransformer() throws TransformerConfigurationException {
		final TransformerFactory transformerFactory=TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"false");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "true");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "true");
		return transformer;
	}

	private static void closeQuietly(final File source, final FileInputStream is) {
		if(is!=null) {
			try {
				is.close();
			} catch (final IOException e) {
				LOGGER.warn("Could not close output file '"+source.getAbsolutePath()+"'",e);
			}
		}
	}

	private static void tryClose(final Unmarshaller unmarshaller) {
		if(unmarshaller instanceof Closeable) {
			final Closeable closeable=(Closeable)unmarshaller;
			try {
				closeable.close();
			} catch (final Exception e) {
				LOGGER.warn("Could not close unmarshaller file "+unmarshaller,e);
			}
		}
	}

	private static boolean isValidXmlCharacter(final int current) {
		return
			isAllowedControlCharacter(current)              ||
			((current >= 0x20) && (current <= 0xD7FF))      ||
			((current >= 0xE000) && (current <= 0xFFFD))    ||
			((current >= 0x10000) && (current <= 0x10FFFF));
	}

	private static boolean isAllowedControlCharacter(final int current) {
		return (current == 0x9) || (current == 0xA) || (current == 0xD);
	}

	/**
	 * This method ensures that the output String has only valid XML unicode
	 * characters as specified by the XML 1.0 standard. For reference, please
	 * see <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	 * standard</a>. This method will return an empty String if the input is
	 * null or empty.
	 *
	 * @param in
	 *            The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public static String stripNonValidXMLCharacters(final String in) {
		if(in == null || ("".equals(in))) {
			return ""; // vacancy test.
		}
		final StringBuffer out = new StringBuffer();
		final int length = in.length();
		int i=0;
		while(i<length) {
			final int current=Character.codePointAt(in,i);
			if(isValidXmlCharacter(current)) {
				for(final char character:Character.toChars(current)) {
					out.append(character);
				}
			}
			i+=Character.charCount(current);
		}
		return out.toString();
	}

	public static JAXBContext context() {
		return XmlUtils.CONTEXT;
	}

	public static String evaluateXPath(final String xpath, final String source) throws XmlProcessingException {
		final XPath newXPath = FACTORY.newXPath();
		try {
			final XPathExpression compile = newXPath.compile(xpath);
			return compile.evaluate(new InputSource(new StringReader(source)));
		} catch (final XPathExpressionException e) {
			throw new XmlProcessingException(String.format(INVALID_XPATH_EXPRESSION,xpath),e);
		}
	}

	public static String evaluateXPath(final String xpath, final Document content) throws XmlProcessingException {
		final XPath newXPath = FACTORY.newXPath();
		try {
			final XPathExpression compile = newXPath.compile(xpath);
			return compile.evaluate(content);
		} catch (final XPathExpressionException e) {
			throw new XmlProcessingException(String.format(INVALID_XPATH_EXPRESSION,xpath),e);
		}
	}

	public static String toString(final Document document) throws XmlProcessingException {
		try {
			final StringWriter writer = new StringWriter();
			final StreamResult result = new StreamResult(writer);
			final DOMSource source=new DOMSource(document);
			getTransformer().
				transform(source, result);
			return writer.toString();
		} catch(final Exception e) {
			throw new XmlProcessingException("Could not marshall document",e);
		}
	}

	public static Document toDocument(final String body) throws XmlProcessingException {
		final String curatedBody = stripNonValidXMLCharacters(body);
		if(curatedBody.length()!=body.length()) {
			throw new XmlProcessingException("Input data has invalid XML characters");
		}
		try {
			final DocumentBuilder builder =
				DocumentBuilderFactory.
					newInstance().
						newDocumentBuilder();
			return
				builder.
					parse(new InputSource(new StringReader(curatedBody)));
		} catch (final Exception e) {
			throw new XmlProcessingException("Could not unmarshall document",e);
		}
	}

	public static <T, E extends Throwable> T unmarshall(final File source, final Class<? extends T> clazz, final E throwable) throws E {
		FileInputStream is=null;
		Unmarshaller unmarshaller=null;
		try {
			unmarshaller=CONTEXT.createUnmarshaller();
			is=new FileInputStream(source);
			final Object unmarshal = unmarshaller.unmarshal(is);
			return clazz.cast(unmarshal);
		} catch (final Exception e) {
			throwable.initCause(e);
			throw throwable;
		} finally {
			closeQuietly(source,is);
			tryClose(unmarshaller);
		}
	}

	public static <E extends Throwable> void marshall(final Object entity, final File target, final E throwable) throws E {
		try {
			final Marshaller marshaller=CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
			marshaller.marshal(entity,target);
		} catch (final Exception e) {
			throwable.initCause(e);
			throw throwable;
		}
	}

	public static <T, E extends Throwable> T unmarshall(final String content, final Class<? extends T> clazz, final E throwable) throws E {
		Unmarshaller unmarshaller=null;
		try {
			unmarshaller=CONTEXT.createUnmarshaller();
			final Object unmarshal=unmarshaller.unmarshal(new StringReader(content));
			return clazz.cast(unmarshal);
		} catch (final Exception e) {
			throwable.initCause(e);
			throw throwable;
		} finally {
			tryClose(unmarshaller);
		}
	}

	public static <E extends Throwable> String marshall(final Object entity, final E throwable) throws E {
		try {
			final Marshaller marshaller=CONTEXT.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
			final StringWriter writer = new StringWriter();
			marshaller.marshal(entity,writer);
			return writer.toString();
		} catch (final JAXBException e) {
			throwable.initCause(e);
			throw throwable;
		}
	}

	public static void marshall(final File file, final Source source) throws XmlProcessingException {
		try {
			getTransformer().
				transform(source, new StreamResult(file));
		} catch(final Exception e) {
			throw new XmlProcessingException(RESOURCE_STORAGE_FAILURE,e);
		}
	}

}
