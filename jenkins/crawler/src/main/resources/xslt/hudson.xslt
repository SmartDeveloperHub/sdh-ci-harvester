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
	<xsl:param name="serviceUrl"/>
	<xsl:template match="hudson">
		<ci:service xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.sdh.org/harvester/ci/v1 http://www.sdh.org/harvester/ci/v1/schema.xsd">
			<url><xsl:value-of select="$serviceUrl"/></url>
			<xsl:if test="exists(nodeName)">
				<title><xsl:value-of select="nodeName"/></title>
			</xsl:if>
			<xsl:if test="exists(nodeDescription)">
				<description><xsl:value-of select="nodeDescription"/></description>
			</xsl:if>
			<builds>
				<xsl:for-each select="job">
					<build id="{name}"><xsl:value-of select="url"/></build>
				</xsl:for-each>
			</builds>
		</ci:service>
	</xsl:template>
</xsl:stylesheet>
