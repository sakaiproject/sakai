<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:template match="/">
        <!-- <xsl:call-template name="header"/> -->
        <xsl:apply-templates match="section"/>
        <!-- <xsl:call-template name="footer"/> -->
</xsl:template>

<xsl:template match="section"><xsl:text>
h2. </xsl:text><xsl:value-of select="title/text( )" /><xsl:text>
</xsl:text>
	<xsl:apply-templates match="para" />
</xsl:template>

<xsl:template match="para">
	<xsl:apply-templates mode="para" />
</xsl:template>

<xsl:template match="text( )" mode="para">
	<xsl:value-of select="current( )" />
</xsl:template>

<xsl:template match="programlisting" mode="para">
	<xsl:text>{noformat}</xsl:text>
	<xsl:value-of select="current( )/text( )" />
	<xsl:text>{noformat}</xsl:text>
</xsl:template>

<xsl:template match="screenshot" mode="para">
	<!-- TODO -->
</xsl:template>

<xsl:template match="itemizedlist" mode="para">
	<xsl:apply-templates match="listitem/para"/>
</xsl:template>

<xsl:template match="orderedlist" mode="para">
	<xsl:apply-templates match="listitem/para"/>
</xsl:template>

<xsl:template match="listitem/para">
	<xsl:text>* </xsl:text><xsl:value-of select="current( )/text( )" />
<xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="text( )" />

</xsl:stylesheet>
