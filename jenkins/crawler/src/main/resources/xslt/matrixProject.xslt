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
      Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:1.0.0-SNAPSHOT
      Bundle      : ci-jenkins-crawler-1.0.0-SNAPSHOT.jar
    #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#

-->
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:ci="http://www.sdh.org/harvester/ci/v1"
	exclude-result-prefixes="fn xsl xs">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="matrixProject">
		<xsl:variable name="name" select="name"/>
		<xsl:variable name="url" select="url"/>
		<ci:compositeBuild 
			service="{substring($url,0,string-length($url)-string-length(concat('job/',$name)))}" 
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
			<runnable><xsl:value-of select="buildable"/></runnable>
			<runs>
				<xsl:for-each select="build">
					<xsl:variable name="number" select="number"/>
					<run id="{$number}"><xsl:value-of select="url"/></run>
				</xsl:for-each>
			</runs>
			<subBuilds>
				<xsl:for-each select="activeConfiguration">
					<xsl:variable name="name" select="name"/>
					<build id="{$name}"><xsl:value-of select="url"/></build>
				</xsl:for-each>
			</subBuilds>
		</ci:compositeBuild>
	</xsl:template>
</xsl:stylesheet>