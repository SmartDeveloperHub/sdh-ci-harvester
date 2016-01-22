<?xml version="1.0" encoding="UTF-8"?>
<!--

    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      This file is part of the Smart Developer Hub Project:
        http://www.smartdeveloperhub.org/

      Center for Open Middleware
        http://www.centeropenmiddleware.com/
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
      Copyright (C) 2015-2016 Center for Open Middleware.
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
      Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
      Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
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
	<xsl:template match="mavenModule">
		<xsl:variable name="name" select="name"/>
		<xsl:variable name="url" select="url"/>
		<xsl:variable name="segments" select="tokenize($url,'/')"/>
		<xsl:variable name="segNumber" select="count($segments)-3" as="xs:integer"/>
		<xsl:variable name="service" select="concat(string-join(subsequence($segments,0,$segNumber),'/'),'/')"/>
		<ci:subJob
			instance="{$service}"
			parent="{substring($url,0,string-length($url)-string-length($name))}"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.sdh.org/harvester/ci/v1 http://www.sdh.org/harvester/ci/v1/schema.xsd" >
			<url><xsl:value-of select="$url"/></url>
			<id><xsl:value-of select="$name"/></id>
			<xsl:if test="exists(displayName)">
				<title><xsl:value-of select="displayName"/></title>
			</xsl:if>
			<xsl:if test="exists(description)">
				<description><xsl:value-of select="description"/></description>
			</xsl:if>
			<type><xsl:value-of select="fn:local-name()"/></type>
			<xsl:if test="exists(scm)">
				<codebase>
					<location><xsl:value-of select="scm/userRemoteConfig/url"/></location>
					<branch><xsl:value-of select="scm/branche/name"/></branch>
				</codebase>
			</xsl:if>
			<runnable><xsl:value-of select="buildable"/></runnable>
			<runs>
				<xsl:for-each select="build">
					<xsl:sort select="number" data-type="number" order="ascending"/>
					<xsl:variable name="number" select="number"/>
					<run id="{$number}"><xsl:value-of select="url"/></run>
				</xsl:for-each>
			</runs>
		</ci:subJob>
	</xsl:template>
</xsl:stylesheet>
