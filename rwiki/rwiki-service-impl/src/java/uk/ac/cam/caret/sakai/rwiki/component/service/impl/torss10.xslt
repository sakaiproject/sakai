<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <xsl:variable name="baseurl" select="/entity-service/request-properties/@server-url"/>
        <rdf:RDF xmlns="http://purl.org/rss/1.0/"
            xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            xmlns:slash="http://purl.org/rss/1.0/modules/slash/"
            xmlns:dc="http://purl.org/dc/elements/1.1/">
            <channel rdf:about="{$baseurl}">
                <title>
                    <xsl:value-of select="/entity-service/entity/properties/property[@name='_title']"/>
                </title>
                <description>
                    <xsl:value-of select="/entity-service/entity/properties/property[@name='_description']"
                    />
                </description>
                <link>
                    <xsl:value-of select="$baseurl"/>
                </link>
                <dc:date>
                    <xsl:value-of select="/entity-service/entity/properties/property[@name='_datestamp']"/>
                </dc:date>
                <items>
                    <rdf:Seq>
                        <xsl:for-each select="/entity-service/entity/changes/change">
                            <rdf:li rdf:resource="{concat($baseurl,'/access/wiki',@name,',',@revision,'.html')}"/>
                        </xsl:for-each>
                    </rdf:Seq>
                </items>
            </channel>
            <xsl:for-each select="/entity-service/entity/changes/change">
                <item rdf:about="{concat($baseurl,'/access/wiki',@name,',',@revision,'.html')}">
                    <dc:format>text/html</dc:format>
                    <dc:source>
                        <xsl:value-of select="$baseurl"/>
                    </dc:source>
                    <title>
                        <xsl:value-of select="@name"/>
                    </title>
                    <link>
                        <xsl:value-of select="concat($baseurl,'/access/wiki',@name,',',@revision,'.html')"/>
                    </link>
                    <description>
                        <xsl:value-of select="content/contentdigest"/>
                    </description>
                </item>
            </xsl:for-each>
        </rdf:RDF>
    </xsl:template>
</xsl:stylesheet>
