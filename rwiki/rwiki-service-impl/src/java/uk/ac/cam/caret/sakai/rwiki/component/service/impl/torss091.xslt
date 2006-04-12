<?xml version="1.0" encoding="UTF-8"?>
<!-- generator="FeedCreator 1.7.2" -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
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
                            <xsl:value-of select="@name"/>
                        </title>
                        <link>
                            <xsl:value-of
                                select="concat($baseurl,'/access/wiki',@name,',',@revision,'.html')"
                            />
                        </link>
                        <description>
                            <xsl:value-of select="content/contentdigest"/>
                        </description>
                    </item>
                </xsl:for-each>
            </channel>
        </rss>
    </xsl:template>
</xsl:stylesheet>
