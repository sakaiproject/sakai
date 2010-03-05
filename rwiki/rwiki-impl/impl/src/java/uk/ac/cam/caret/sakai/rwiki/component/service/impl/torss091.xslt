<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
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
        <rss version="0.91">
            <channel>
                <title>
                    <xsl:value-of
                        select="/entity-service/entity/properties/property[@name='_title']"/>
                </title>
                <description>
                    <xsl:value-of
                        select="/entity-service/entity/properties/property[@name='_description']"/>
                </description>
                <link>
                    <xsl:value-of select="$baseurl"/>
                </link>
                <lastBuildDate>
                    <xsl:value-of
                        select="/entity-service/entity/properties/property[@name='_datestamp']"/>
                </lastBuildDate>
                <generator>Sakai RWiki RSS Generator</generator>
                <xsl:for-each select="/entity-service/entity/changes/change">
                    <item>
                        <title>
                            <xsl:value-of select="@local-name"/> (Revision <xsl:value-of select="@revision"/>)
                        </title>
                        <link>
                            <xsl:value-of
                                select="concat($baseurl,'/wiki',@name,'.html')"
                            />
                        </link>
                        <description>
			  Last edited by <xsl:value-of select="@user-display"/> at <xsl:value-of select="@last-modified"/>&lt;hr/&gt;
                            <xsl:value-of select="content/rendered-cdata/node()"/>
                        </description>
                    </item>
                </xsl:for-each>
            </channel>
        </rss>
    </xsl:template>
</xsl:stylesheet>
