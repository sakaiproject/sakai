<?xml version="1.0" encoding="UTF-8" ?>
<!--
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p>Description: QTI Persistence XML to XML Transform for Import</p>
 * @author <a href="mailto:esmiley@stanford.edu">Ed Smiley</a>
 * @version $Id: extractSection.xsl,v 1.3 2005/04/27 02:38:35 esmiley.stanford.edu Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
 doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

<xsl:template match="/">
  <sectionData>
   <ident><xsl:value-of select="//section/@ident" /></ident>
   <title><xsl:value-of select="//section/@title" /></title>
  </sectionData>
</xsl:template>

</xsl:stylesheet>
