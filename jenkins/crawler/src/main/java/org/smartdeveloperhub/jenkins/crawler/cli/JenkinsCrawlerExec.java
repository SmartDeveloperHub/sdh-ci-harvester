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
package org.smartdeveloperhub.jenkins.crawler.cli;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawler;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawlerException;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.util.Consoles;

public final class JenkinsCrawlerExec {

	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsCrawlerExec.class);

	public static void main(String[] args) {
		File tmpDirectory = new File("target","jenkins"+new Date().getTime());
		tmpDirectory.deleteOnExit();
		String location="http://ci.jenkins-ci.org/";
		try {
			JenkinsCrawler crawler=
				JenkinsCrawler.
					builder().
						withDirectory(tmpDirectory).
						withLocation(location).
						build();
			JenkinsEventListener jenkinsEventListener = new ConsoleLoggingJenkinsEventListener();
			CrawlerEventListener crawlerEventListener = new ConsoleLoggingCrawlerEventListener();
			crawler.
				registerListener(jenkinsEventListener).
				registerListener(crawlerEventListener);
			crawler.start();
			Consoles.defaultConsole().printf("<<HIT ENTER TO STOP THE CRAWLER>>%n");
			Consoles.defaultConsole().readLine();
			crawler.stop();
			crawler.
				deregisterListener(crawlerEventListener).
				deregisterListener(jenkinsEventListener);
			FileBasedStorage tmp=
					FileBasedStorage.
						builder().
							withConfigFile(new File(tmpDirectory,"repository.xml")).
							build();
			JenkinsResource resource=
				tmp.
					findResource(
						URI.create("http://ci.jenkins-ci.org/job/jenkins_rc_branch/423/"),
						JenkinsArtifactType.RESOURCE);
			if(resource!=null) {
				LOGGER.info(resource.toString());
			}
		} catch (JenkinsCrawlerException e) {
			LOGGER.error("Could not create crawler",e);
		} catch (IOException e) {
			LOGGER.error("Unexpected failure",e);
		}
	}

}
