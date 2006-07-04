<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="file:///C:/docbook-xsl-1.70.0/html/chunk.xsl"/>
    <xsl:param name="html.stylesheet">styles/pos.css styles/styles.css</xsl:param>
    <xsl:param name="root.filename">install-overview</xsl:param>
    <xsl:param name="use.id.as.filename">1</xsl:param>
    <xsl:param name="base.dir">releaseweb/</xsl:param>
    <xsl:param name="chunker.output.indent">yes</xsl:param>
    <xsl:param name="section.autolabel">1</xsl:param>
    <xsl:param name="variablelist.as.table">1</xsl:param>
    <xsl:param name="css.decoration">0</xsl:param>
    <xsl:param name="spacing.paras">0</xsl:param>
    <xsl:param name="html.cleanup">1</xsl:param>
    
    <!-- Hacky manipulations for "pretty version" -->
    <xsl:template name="user.header.navigation">
        <!-- frame div should enclose entire page.  Edit this div tag and the one noted below at the bottom -->
        <div id="frame"/>
    </xsl:template>
    <xsl:template name="user.footer.navigation">
        
        <br clear="all"/> <!-- Needed to force frame to enclose everything -->
        
        <!-- Close frame div by editing tag below to be a closing tag only, e.g. </div> -->
        <div id="closeframe"/>
    </xsl:template>
    
</xsl:stylesheet>
