<?xml version="1.0" encoding="UTF-8" ?>
<!--
 * <p>Copyright: Copyright (c) 2010 Sakai</p>
 * <p>Description: QTI Remove Default Namespace For Easy Processing</p>
 * @author $Author$
 * @version $Id$
-->
<xsl:stylesheet 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 version="1.0">
<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
 doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

 <xsl:template match="*[namespace-uri() = 'http://www.imsglobal.org/xsd/ims_qtiasiv1p2']">
  <xsl:element name="{name()}" namespace="">
   <xsl:apply-templates select="@* | node()" />
  </xsl:element>
 </xsl:template>
 
 <xsl:template match="node() | @*">
  <xsl:copy>
   <xsl:apply-templates select="node() | @*" />
  </xsl:copy> 
 </xsl:template>
 
</xsl:stylesheet>
