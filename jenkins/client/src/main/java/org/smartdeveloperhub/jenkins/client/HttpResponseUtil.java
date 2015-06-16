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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-client-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class HttpResponseUtil {

	private static final Logger LOGGER=LoggerFactory.getLogger(HttpResponseUtil.class);

	private HttpResponseUtil() {
	}

	static String getInvalidResponseFailureMessage(HttpResponse response) {
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		String error=null;
		if(statusCode>500) {
			error="Could not load service because of a server failure: "+statusLine;
		} else if(statusCode>400) {
			error="Unexpected Jenkins Remote API failure on GET: "+statusLine;
		} else if(statusCode>300) {
			Header header = response.getFirstHeader("Location");
			String redirection=header!=null?header.getValue():null;
			error="Unexpected redirection: "+statusLine +" ("+redirection+")";
		} else if(statusCode==204){
			error="Unexpected server response: body should be included ("+statusCode+")";
		} else {
			error="Invalid server response: "+statusLine;
		}
		return error;
	}

	static Date retrieveLastModified(HttpResponse response) {
		Header[] headers = response.getHeaders("Last-Modified");
		if(headers==null || headers.length==0) {
			return null;
		}
		Date lastModified=null;
		String rawLastModified = headers[0].getValue();
		try {
			lastModified = HttpDateUtils.parse(rawLastModified);
		} catch (UnknownHttpDateFormatException e) { // NOSONAR
			LOGGER.warn(
				"Ignoring invalid response last modified date '{}' ({})",
				rawLastModified,
				e.getMessage());
		}
		return lastModified;
	}

	static String retrieveEntityTag(HttpResponse response) {
		Header[] headers = response.getHeaders("Etag");
		String etag=null;
		if(headers!=null && headers.length>0) {
			etag = headers[0].getValue();
		}
		return etag;
	}

	static String retrieveServiceVersion(HttpResponse response) {
		Header[] vHeaders = response.getHeaders("X-Jenkins");
		String serviceVersion=null;
		if(vHeaders!=null && vHeaders.length>0) {
			serviceVersion = vHeaders[0].getValue();
		}
		return serviceVersion;
	}

}
