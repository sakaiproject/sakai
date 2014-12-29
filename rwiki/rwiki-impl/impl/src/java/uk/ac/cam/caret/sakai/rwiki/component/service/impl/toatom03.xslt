<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://purl.org/atom/ns#" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!--
    /**********************************************************************************
    * $URL:  $
    * $Id:  $
    ***********************************************************************************
    *
    * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
    *
    * Licensed under the Educational Community License, Version 1.0 (the "License");
    * you may not use this file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    *      http://www.opensource.org/licenses/ecl1.php
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    *
    **********************************************************************************/
-->    
    <xsl:template match="/">
        <xsl:variable name="baseurl" select="/entity-service/request-properties/@server-url"/>
        <feed version="0.3" >
            <title>
                <xsl:value-of select="/entity-service/entity/properties/property[@name='_title']"/>
            </title>
            <tagline>Sakai Wiki Changes</tagline>
            <link rel="alternate" type="text/html" href="{$baseurl}"/>
            <id>
                <xsl:value-of select="$baseurl"/>
            </id>
            <modified>
                <xsl:value-of
                    select="/entity-service/entity/properties/property[@name='_datestamp']"/>
            </modified>
            <generator>Sakai Wiki Atom Generator</generator>
            <xsl:for-each select="/entity-service/entity/changes/change">
                <entry>
                    <title>
                        <xsl:value-of select="@local-name"/> (Revision <xsl:value-of select="@revision"/>)
                    </title>
                    <link rel="alternate" type="text/html"
                        href="{concat($baseurl, '/wiki', @name, '.html')}"/>
                    <created>
                        <xsl:value-of select="@last-modified"/>
                    </created>
                    <issued>
                        <xsl:value-of select="@last-modified"/>
                    </issued>
                    <modified>
                        <xsl:value-of select="@last-modified"/>
                    </modified>
                    <id>
                        <xsl:value-of
                            select="concat($baseurl, '/wiki', @name, '.html')"/>
                    </id>
                    <summary>Last edited by <xsl:value-of select="@user-display"/> at <xsl:value-of select="@last-modified"/>&lt;hr/&gt;
                    <xsl:copy-of select="content/rendered-cdata/node()"/></summary>
                </entry>
            </xsl:for-each>
        </feed>
    </xsl:template>
</xsl:stylesheet>
