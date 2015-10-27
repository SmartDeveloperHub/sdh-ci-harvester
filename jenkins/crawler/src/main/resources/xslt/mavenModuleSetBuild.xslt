<?xml version="1.0" encoding="UTF-8"?>
<!--

    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      This file is part of the Smart Developer Hub Project:
        http://www.smartdeveloperhub.org/

      Center for Open Middleware
        http://www.centeropenmiddleware.com/
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      Copyright (C) 2015 Center for Open Middleware.
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

                http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0-SNAPSHOT
      Bundle      : ci-jenkins-crawler-0.2.0-SNAPSHOT.jar
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#

-->
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:ci="http://www.smartdeveloperhub.org/harvester/ci/model/v1"
	exclude-result-prefixes="fn xsl xs">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="mavenModuleSetBuild">
		<xsl:variable name="num" select="number"/>
		<xsl:variable name="url" select="url"/>
		<ci:run job="{substring($url,0,string-length($url)-string-length($num))}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.sdh.org/harvester/ci/v1 http://www.sdh.org/harvester/ci/v1/schema.xsd">
			<url><xsl:value-of select="url"/></url>
			<id><xsl:value-of select="$num"/></id>
			<xsl:if test="exists(fullDisplayName)">
				<title><xsl:value-of select="fullDisplayName"/></title>
			</xsl:if>
			<xsl:if test="exists(description)">
				<description><xsl:value-of select="description"/></description>
			</xsl:if>
			<type><xsl:value-of select="local-name()"/></type>
			<timestamp><xsl:value-of select="timestamp"/></timestamp>
			<xsl:if test="not(exists(result))">
				<status>RUNNING</status>
			</xsl:if>
			<xsl:if test="exists(result)">
				<status>FINISHED</status>
				<result>
					<status><xsl:value-of select="result"/></status>
					<duration><xsl:value-of select="duration"/></duration>
				</result>
			</xsl:if>
			<xsl:if test="exists(//action/lastBuiltRevision)">
				<codebase>
					<location><xsl:value-of select="//action/lastBuiltRevision/../remoteUrl"/></location>
					<branch><xsl:value-of select="//action/lastBuiltRevision/branch/name"/></branch>
				</codebase>
				<commit><xsl:value-of select="//action/lastBuiltRevision/SHA1"/></commit>
			</xsl:if>
		</ci:run>
	</xsl:template>
</xsl:stylesheet>
