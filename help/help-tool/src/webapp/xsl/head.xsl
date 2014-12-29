<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Namespace declaration and output method -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" encoding="ASCII" omit-xml-declaration="yes"/>



<!-- <xsl:param name="titleSuffix"/>  Text appended to page titles -->
<!-- <xsl:param name="suppressPrefix" />  don't show ARCHIVED:, DRAFT:, etc -->

<!-- Import Common Templates
<xsl:include href="common.xsl"/>

<xsl:include href="substitutions.xsl"/>
-->


<!--  from common.xsl -->

<!-- Common Variables -->

<!-- <xsl:param name="kbstaff"/>   Is the user 'kbstaff'? -->

<!-- Named Templates -->

<xsl:template match="document">
       <xsl:apply-templates/>
</xsl:template>

<!-- Convert a String to Uppercase -->
<xsl:template name="toUpper">
  <xsl:param name="instr"/>
  <xsl:value-of select="translate($instr, 'abcdefghijklmnopqrstuvwxyz',
    'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
</xsl:template>

<!-- Determine if a Document is INTERNAL or not -->
<!-- NOTE: the xsl:value-of element ALWAYS returns a string, so later when
     we evaluate the return of this template, compare it to the STRING
     'true' or 'false', not the boolean value (the boolean of a non-empty
     string will always return true). -->
<xsl:template name="isInternal">
  <xsl:param name="refnode"/>
  <xsl:param name="domnode" select="0"/>

  <xsl:choose>
    <xsl:when test="$domnode > count (/document/config/internal_domains)">
      <xsl:value-of select="false()"/>
    </xsl:when>
    <xsl:when test="$refnode/domain[position() = $domnode] = 
      /document/config/internal_domains/text()">
      <xsl:value-of select="true()"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="isInternal">
        <xsl:with-param name="refnode" select="$refnode"/>
        <xsl:with-param name="domnode" select="$domnode + 1"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Generate INTERNAL and/or visibility prefixes for a title -->
<xsl:template name="makeTitlePrefix">
  <xsl:param name="refnode"/>
  <xsl:param name="vis" select="$refnode/visibility/text()"/>
  <xsl:variable name="visUpr">
    <xsl:call-template name="toUpper">
      <xsl:with-param name="instr" select="$vis"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="found">
    <xsl:call-template name="isInternal">
      <xsl:with-param name="refnode" select="$refnode"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:if test="not($visUpr = 'VISIBLE')">
    <xsl:choose>
      <xsl:when test="$visUpr = 'NOSEARCH'">
        <xsl:if test="boolean($kbstaff)">
          <xsl:text>NOSEARCH: </xsl:text>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$visUpr"/>
        <xsl:text>: </xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
  <xsl:if test="$found = 'true'">
    <xsl:text>INTERNAL (</xsl:text>
    <xsl:for-each select="$refnode/domain/text()">
      <xsl:if test="not(position() = 1)">
        <xsl:text>, </xsl:text>
      </xsl:if>
      <xsl:call-template name="toUpper">
        <xsl:with-param name="instr" select="current()"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:text>): </xsl:text>
  </xsl:if>
  
</xsl:template>

<!--  end from common.xsl -->

<xsl:template name="makeImageCSS">
    /* Style for specific inline images */ 
    <xsl:for-each select="/document/kbml/body//image">
        <xsl:text>div.image</xsl:text>
        <xsl:value-of select="@uniqinstance" />
        <xsl:text> { </xsl:text>
        <xsl:choose>
            <xsl:when test="description">
                <xsl:text>text-align: center; margin: auto auto; </xsl:text>
                <xsl:if test="description/@width">
                    <xsl:text>width: </xsl:text>
                    <xsl:value-of select="description/@width" />
                    <xsl:text>em; </xsl:text>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>display: inline; </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>}
        </xsl:text>
    </xsl:for-each>
</xsl:template>

<xsl:template name="makeOrderedListCSS">
    /* Style for specific ordered lists */
    <xsl:for-each select="/document/kbml/body//ol">
        <xsl:if test="@type">
            <xsl:text>ol#</xsl:text>
            <xsl:value-of select="@uniqinstance" />
            <xsl:text> { list-style-type: </xsl:text>
            <xsl:choose>
                <xsl:when test="@type = 'a'">
                    <xsl:text>lower-alpha</xsl:text>
                </xsl:when>
                <xsl:when test="@type = 'A'">
                    <xsl:text>upper-alpha</xsl:text>
                </xsl:when>
                <xsl:when test="@type = 'i'">
                    <xsl:text>lower-roman</xsl:text>
                </xsl:when>
                <xsl:when test="@type = 'I'">
                    <xsl:text>upper-roman</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>decimal</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>; }
            </xsl:text>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="makeTableCellCSS">
    /* Style for specific table cells */
    <xsl:for-each select="/document/kbml/body//table/tr/*">
        <xsl:if test="@bgcolor">
            <xsl:text>td#</xsl:text>
            <xsl:value-of select="@uniqinstance" />
            <xsl:text> { background-color: </xsl:text>
            <xsl:value-of select="@bgcolor" />
            <xsl:text>; }
            </xsl:text>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template match="/">
  <title>
        <xsl:if test="not(boolean($suppressPrefix))">
            <xsl:call-template name="makeTitlePrefix">
                <xsl:with-param name="refnode" select="/document/metadata"/>
                <xsl:with-param name="span" select="0"/>
            </xsl:call-template>
        </xsl:if>
        <!-- <xsl:call-template name="globalReplace">
            <xsl:with-param name="outputText">
                <xsl:value-of select="/document/kbml/kbq" />
            </xsl:with-param>
        </xsl:call-template>
        -->
        <xsl:text> - </xsl:text>
        <xsl:value-of select="$titleSuffix" />
  </title>
  
  <style type="text/css">
      <xsl:call-template name="makeImageCSS" />
      <xsl:call-template name="makeOrderedListCSS" />
      <xsl:call-template name="makeTableCellCSS" />
  </style>
</xsl:template>

</xsl:stylesheet>

