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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:0.3.0
 *   Bundle      : ci-jenkins-client-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * This example demonstrates how to create secure connections with a custom SSL
 * context.
 */
public class ClientCustomSSL {

	public final static void main(final String[] args) throws Exception {
		// System.setProperty("org.apache.commons.logging.diagnostics.dest","STDOUT");
		System.out.println("Default client:");
		doGet(HttpClients.createDefault());
		System.out.println("Secure client:");
		doGet(secureClient());
	}

	private static CloseableHttpClient customClient() throws Exception {
		final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		// Trust own CA and all self-signed certs
		final SSLContext sslcontext =
			SSLContexts.
				custom().
					loadTrustMaterial(
						trustStore,
						new TrustSelfSignedStrategy()).
					build();
		// Allow TLSv1 protocol only
		final SSLConnectionSocketFactory sslsf =
			new SSLConnectionSocketFactory(
				sslcontext,
				null,
				null,
				new HostnameVerifier() {
					@Override
					public boolean verify(final String hostname, final SSLSession session) {
						return true;
					}
				});
		return
			HttpClients.
				custom().
					setSSLSocketFactory(sslsf).
					build();
	}

	private static CloseableHttpClient secureClient() throws Exception {
		// Trust own CA and all self-signed certs
		final SSLContext sslcontext =
			SSLContexts.
				custom().
					loadTrustMaterial(
						new File("src/test/resources/testing.keystore"),
						"testing".toCharArray(),
						new TrustSelfSignedStrategy()).
					build();
		// Allow TLSv1 protocol only
		final SSLConnectionSocketFactory sslsf =
			new SSLConnectionSocketFactory(
				sslcontext,
				new String[] { "TLSv1" },
				null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		return
			HttpClients.
				custom().
					setSSLSocketFactory(sslsf).
					build();
	}

	private static void doGet(final CloseableHttpClient httpclient) {
		try {
			final HttpGet httpget = new HttpGet("https://ci.jenkins.io/api/xml");
			System.out.println("Executing request " + httpget.getRequestLine());
			final CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				final HttpEntity entity = response.getEntity();
				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				System.out.println(EntityUtils.toString(entity));
			} finally {
				response.close();
			}
		} catch(final Exception e) {
			e.printStackTrace();
 		} finally {
			try {
				httpclient.close();
			} catch (final IOException e) {
			}
		}
	}

}
