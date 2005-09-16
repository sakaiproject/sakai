<?xml version="1.0" encoding="UTF-8" ?>
<!--
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p>Description: QTI Persistence XML to XML Transform for Import</p>
 * @author <a href="mailto:esmiley@stanford.edu">Ed Smiley</a>
 * @version $Id: extractItem.xsl,v 1.2 2005/01/28 22:14:03 esmiley.stanford.edu Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
 doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

<xsl:template match="/">

<itemData>
  <ident><xsl:value-of select="//assessmentItem/@ident" /></ident>
  <duration></duration>
  <triesAllowed></triesAllowed>
  <instruction></instruction>
  <typeId></typeId>
  <grade></grade>
  <score></score>
  <hint></hint>
  <hasRationale></hasRationale>
  <status></status>
  <createdBy></createdBy>
  <createdDate></createdDate>
  <lastModifiedBy></lastModifiedBy>
  <lastModifiedDate></lastModifiedDate>
  <itemText type="list"></itemText>
  <itemAnswer type="list"></itemAnswer>
  <itemFeedback type="list"></itemFeedback>
  <xsl:for-each select="//itemmetadata/qtimetadata/qtimetadatafield">
    <xsl:variable name="metadata">meta</xsl:variable>
    <xsl:element name="metadata">
     <xsl:attribute name="type">list</xsl:attribute>
     <xsl:value-of select="fieldlabel"/>|<xsl:value-of select="fieldentry"/>
    </xsl:element>
  </xsl:for-each>


</itemData>

</xsl:template>


</xsl:stylesheet>
