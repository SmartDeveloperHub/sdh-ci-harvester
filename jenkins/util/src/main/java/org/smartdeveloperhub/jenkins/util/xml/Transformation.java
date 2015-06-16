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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import com.google.common.collect.ImmutableMap;

public final class Transformation {

	static final class ResetOnCloseInputStream extends InputStream {

		private final InputStream decorated;

		public ResetOnCloseInputStream(InputStream anInputStream) {
			if (!anInputStream.markSupported()) {
				throw new IllegalArgumentException("marking not supported");
			}

			anInputStream.mark(1 << 24); // magic constant: BEWARE
			decorated = anInputStream;
		}

		@Override
		public void close() throws IOException {
			decorated.reset();
		}

		@Override
		public int read() throws IOException {
			return decorated.read();
		}
	}

	interface ResultAdapter<T> extends Result {

		public T value();

	}

	interface MemoizedSource extends Source {

		MemoizedSource copy();

	}

	interface MemoizingTemplates extends Templates {

		Source styleSheet();

	}

	private static final TransformerFactory transformerFactory=TransformerFactory.newInstance();
	private final MemoizingTemplates styleSheet;
	private final ImmutableMap<String, Object> parameters;
	private TransformerFactory factory;

	private Transformation(MemoizingTemplates styleSheet, ImmutableMap<String, Object> parameters, TransformerFactory factory) {
		this.styleSheet = styleSheet;
		this.parameters = parameters;
		this.factory = factory;
	}

	<T> Transformation setStyleSheet(T styleSheet) throws TransformerConfigurationException {
		return createNew(styleSheet,this.factory,this.parameters);
	}

	<T> Transformation setParameter(String parameter, T object) {
		return new Transformation(this.styleSheet,ImmutableMap.<String,Object>builder().putAll(parameters).put(parameter,object).build(),this.factory);
	}

	Transformation setTransformerFactory(TransformerFactory factory) throws TransformerConfigurationException {
		return createNew(this.styleSheet.styleSheet(),factory,this.parameters);
	}

	<T,R> void execute(T source, R result) throws TransformerException {
		Transformer newTransformer = styleSheet.newTransformer();
		for(Entry<String, Object> entry:this.parameters.entrySet()) {
			newTransformer.setParameter(entry.getKey(), entry.getValue());
		}
		newTransformer.transform(toSource(source), toResult(result));
	}

	<T,R> R execute(T source, Class<? extends R> result) throws TransformerException {
		Transformer newTransformer = styleSheet.newTransformer();
		for(Entry<String, Object> entry:this.parameters.entrySet()) {
			newTransformer.setParameter(entry.getKey(), entry.getValue());
		}
		ResultAdapter<R> adapter=toResultAdapter(result);
		newTransformer.transform(toSource(source), adapter);
		return adapter.value();
	}

	public static <T> Transformation newTransformation(T styleSheet) throws TransformerConfigurationException {
		return createNew(styleSheet, transformerFactory, ImmutableMap.<String,Object>of());
	}

	private static <T> Transformation createNew(T styleSheet, TransformerFactory factory, ImmutableMap<String, Object> parameters) throws TransformerConfigurationException {
		final MemoizedSource source=toSource(styleSheet);
		final Templates templates = factory.newTemplates(source);
		MemoizingTemplates tmp=new MemoizingTemplates() {

			@Override
			public Transformer newTransformer() throws TransformerConfigurationException {
				return templates.newTransformer();
			}

			@Override
			public Properties getOutputProperties() {
				return templates.getOutputProperties();
			}

			@Override
			public MemoizedSource styleSheet() {
				return source.copy();
			}

		};
		return new Transformation(tmp,parameters,factory);
	}

	private static <R> ResultAdapter<R> toResultAdapter(Class<? extends R> result) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	private static <R> Result toResult(R result) {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	private static <T> MemoizedSource toSource(T rawSource) {
		throw new UnsupportedOperationException("Method not supported yet");
	}

}