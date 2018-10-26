<?xml version="1.0" encoding="UTF-8"?>
<!--

Copyright Antenna House, Inc. (http://www.antennahouse.com) 2001, 2002.

Since this stylesheet is originally developed by Antenna House to be used with XSL Formatter, it may not be compatible with another XSL-FO processors.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, provided that the above copyright notice(s) and this permission notice appear in all copies of the Software and that both the above copyright notice(s) and this permission notice appear in supporting documentation.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xalan="http://xml.apache.org/xalan"  >

    <xsl:output method="xml" 
              encoding="UTF-8"
              indent="yes" 
              xalan:indent-amount="2"/>
            
  <!--======================================================================
      Parameters
  =======================================================================-->

  <!-- page size -->
  <xsl:param name="page-width">auto</xsl:param>
  <xsl:param name="page-height">auto</xsl:param>
  <xsl:param name="page-margin-top">1in</xsl:param>
  <xsl:param name="page-margin-bottom">1in</xsl:param>
  <xsl:param name="page-margin-left">1in</xsl:param>
  <xsl:param name="page-margin-right">1in</xsl:param>

  <!-- page header and footer -->
  <xsl:param name="page-header-margin">0.5in</xsl:param>
  <xsl:param name="page-footer-margin">0.5in</xsl:param>
  <xsl:param name="title-print-in-header">true</xsl:param>
  <xsl:param name="page-number-print-in-footer">true</xsl:param>

  <!-- multi column -->
  <xsl:param name="column-count">1</xsl:param>
  <xsl:param name="column-gap">12pt</xsl:param>

  <!-- writing-mode: lr-tb | rl-tb | tb-rl -->
  <xsl:param name="writing-mode">lr-tb</xsl:param>

  <!-- text-align: justify | start -->
  <xsl:param name="text-align">start</xsl:param>

  <!-- hyphenate: true | false -->
  <xsl:param name="hyphenate">false</xsl:param>
  
  <!-- The default font to use when rendering -->
  <!-- This should be supplied but keep a default to make the xslt easier to test. -->
  <xsl:param name="default-font">Helvetica</xsl:param>
  


  <!--======================================================================
      Attribute Sets
  =======================================================================-->

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Root
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="root">
    <xsl:attribute name="writing-mode"><xsl:value-of select="$writing-mode"/></xsl:attribute>
    <xsl:attribute name="hyphenate"><xsl:value-of select="$hyphenate"/></xsl:attribute>
    <xsl:attribute name="text-align"><xsl:value-of select="$text-align"/></xsl:attribute>
    <xsl:attribute name="font-family"><xsl:value-of select="$default-font"/></xsl:attribute>
    <!-- specified on fo:root to change the properties' initial values -->
  </xsl:attribute-set>

  <xsl:attribute-set name="page">
    <xsl:attribute name="page-width"><xsl:value-of select="$page-width"/></xsl:attribute>
    <xsl:attribute name="page-height"><xsl:value-of select="$page-height"/></xsl:attribute>
    <!-- specified on fo:simple-page-master -->
  </xsl:attribute-set>

  <xsl:attribute-set name="body">
    <!-- specified on fo:flow's only child fo:block -->
  </xsl:attribute-set>

  <xsl:attribute-set name="page-header">
    <!-- specified on (page-header)fo:static-content's only child fo:block -->
    <xsl:attribute name="font-size">small</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="page-footer">
    <!-- specified on (page-footer)fo:static-content's only child fo:block -->
    <xsl:attribute name="font-size">small</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Block-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="h1">
    <xsl:attribute name="font-size">2em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">0.67em</xsl:attribute>
    <xsl:attribute name="space-after">0.67em</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h2">
    <xsl:attribute name="font-size">1.5em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">0.83em</xsl:attribute>
    <xsl:attribute name="space-after">0.83em</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h3">
    <xsl:attribute name="font-size">1.17em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h4">
    <xsl:attribute name="font-size">1em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">1.17em</xsl:attribute>
    <xsl:attribute name="space-after">1.17em</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h5">
    <xsl:attribute name="font-size">0.83em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">1.33em</xsl:attribute>
    <xsl:attribute name="space-after">1.33em</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="h6">
    <xsl:attribute name="font-size">0.67em</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="space-before">1.67em</xsl:attribute>
    <xsl:attribute name="space-after">1.67em</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="p">
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
    <!-- e.g.,
    <xsl:attribute name="text-indent">1em</xsl:attribute>
    -->
  </xsl:attribute-set>

  <xsl:attribute-set name="p-initial" use-attribute-sets="p">
    <!-- initial paragraph, preceded by h1..6 or div -->
    <!-- e.g.,
    <xsl:attribute name="text-indent">0em</xsl:attribute>
    -->
  </xsl:attribute-set>

  <xsl:attribute-set name="p-initial-first" use-attribute-sets="p-initial">
    <!-- initial paragraph, first child of div, body or td -->
  </xsl:attribute-set>

  <xsl:attribute-set name="blockquote">
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 24pt</xsl:attribute>
    <xsl:attribute name="end-indent">inherited-property-value(end-indent) + 24pt</xsl:attribute>
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="pre">
    <xsl:attribute name="font-size">0.83em</xsl:attribute>
    <xsl:attribute name="font-family">monospace</xsl:attribute>
    <xsl:attribute name="white-space">pre</xsl:attribute>
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="address">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="hr">
    <xsl:attribute name="border">1px inset</xsl:attribute>
    <xsl:attribute name="space-before">0.67em</xsl:attribute>
    <xsl:attribute name="space-after">0.67em</xsl:attribute>
  </xsl:attribute-set>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       List
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="ul">
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="ul-nested">
    <xsl:attribute name="space-before">0pt</xsl:attribute>
    <xsl:attribute name="space-after">0pt</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="ol">
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="ol-nested">
    <xsl:attribute name="space-before">0pt</xsl:attribute>
    <xsl:attribute name="space-after">0pt</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="ul-li">
    <!-- for (unordered)fo:list-item -->
    <xsl:attribute name="relative-align">baseline</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="ol-li">
    <!-- for (ordered)fo:list-item -->
    <xsl:attribute name="relative-align">baseline</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="dl">
    <xsl:attribute name="space-before">1em</xsl:attribute>
    <xsl:attribute name="space-after">1em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="dt">
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="dd">
    <xsl:attribute name="start-indent">inherited-property-value(start-indent) + 24pt</xsl:attribute>
  </xsl:attribute-set>

  <!-- list-item-label format for each nesting level -->

  <xsl:param name="ul-label-1">&#x2022;</xsl:param>
  <xsl:attribute-set name="ul-label-1">
    <xsl:attribute name="font">1em serif</xsl:attribute>
  </xsl:attribute-set>

  <xsl:param name="ul-label-2">o</xsl:param>
  <xsl:attribute-set name="ul-label-2">
    <xsl:attribute name="font">0.67em monospace</xsl:attribute>
    <xsl:attribute name="baseline-shift">0.25em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:param name="ul-label-3">-</xsl:param>
  <xsl:attribute-set name="ul-label-3">
    <xsl:attribute name="font">bold 0.9em sans-serif</xsl:attribute>
    <xsl:attribute name="baseline-shift">0.05em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:param name="ol-label-1">1.</xsl:param>
  <xsl:attribute-set name="ol-label-1"/>

  <xsl:param name="ol-label-2">a.</xsl:param>
  <xsl:attribute-set name="ol-label-2"/>

  <xsl:param name="ol-label-3">i.</xsl:param>
  <xsl:attribute-set name="ol-label-3"/>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Table
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="inside-table">
    <!-- prevent unwanted inheritance -->
    <xsl:attribute name="start-indent">0pt</xsl:attribute>
    <xsl:attribute name="end-indent">0pt</xsl:attribute>
    <xsl:attribute name="text-indent">0pt</xsl:attribute>
    <xsl:attribute name="last-line-end-indent">0pt</xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="text-align-last">relative</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="table-and-caption" >
    <!-- horizontal alignment of table itself
    <xsl:attribute name="text-align">center</xsl:attribute>
    -->
    <!-- vertical alignment in table-cell -->
    <xsl:attribute name="display-align">center</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="table">
    <xsl:attribute name="border-collapse">separate</xsl:attribute>
    <xsl:attribute name="border-spacing">2px</xsl:attribute>
    <xsl:attribute name="border">1px</xsl:attribute>
    <xsl:attribute name="table-layout">fixed</xsl:attribute>
    <!--
    <xsl:attribute name="border-style">outset</xsl:attribute>
    -->
  </xsl:attribute-set>

  <xsl:attribute-set name="table-caption" use-attribute-sets="inside-table">
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="table-column">
  </xsl:attribute-set>

  <xsl:attribute-set name="thead" use-attribute-sets="inside-table">
  </xsl:attribute-set>

  <xsl:attribute-set name="tfoot" use-attribute-sets="inside-table">
  </xsl:attribute-set>

  <xsl:attribute-set name="tbody" use-attribute-sets="inside-table">
  </xsl:attribute-set>

  <xsl:attribute-set name="tr">
  </xsl:attribute-set>

  <xsl:attribute-set name="th">
    <xsl:attribute name="font-weight">bolder</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="border">1px</xsl:attribute>
    <!--
    <xsl:attribute name="border-style">inset</xsl:attribute>
    -->
    <xsl:attribute name="padding">1px</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="td">
    <xsl:attribute name="border">1px</xsl:attribute>
    <!--
    <xsl:attribute name="border-style">inset</xsl:attribute>
    -->
    <xsl:attribute name="padding">1px</xsl:attribute>
  </xsl:attribute-set>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Inline-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="b">
    <xsl:attribute name="font-weight">bolder</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="strong">
    <xsl:attribute name="font-weight">bolder</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="strong-em">
    <xsl:attribute name="font-weight">bolder</xsl:attribute>
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="i">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="cite">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="em">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="var">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="dfn">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="tt">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="code">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="kbd">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="samp">
    <xsl:attribute name="font-family">monospace</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="big">
    <xsl:attribute name="font-size">larger</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="small">
    <xsl:attribute name="font-size">smaller</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="sub">
    <xsl:attribute name="baseline-shift">sub</xsl:attribute>
    <xsl:attribute name="font-size">smaller</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="sup">
    <xsl:attribute name="baseline-shift">super</xsl:attribute>
    <xsl:attribute name="font-size">smaller</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="s">
    <xsl:attribute name="text-decoration">line-through</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="strike">
    <xsl:attribute name="text-decoration">line-through</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="del">
    <xsl:attribute name="text-decoration">line-through</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="u">
    <xsl:attribute name="text-decoration">underline</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="ins">
    <xsl:attribute name="text-decoration">underline</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="abbr">
    <!-- e.g.,
    <xsl:attribute name="font-variant">small-caps</xsl:attribute>
    <xsl:attribute name="letter-spacing">0.1em</xsl:attribute>
    -->
  </xsl:attribute-set>

  <xsl:attribute-set name="acronym">
    <!-- e.g.,
    <xsl:attribute name="font-variant">small-caps</xsl:attribute>
    <xsl:attribute name="letter-spacing">0.1em</xsl:attribute>
    -->
  </xsl:attribute-set>

  <xsl:attribute-set name="q"/>
  <xsl:attribute-set name="q-nested"/>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Image
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="img">
  </xsl:attribute-set>

  <xsl:attribute-set name="img-link">
    <xsl:attribute name="border">2px solid</xsl:attribute>
  </xsl:attribute-set>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Link
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:attribute-set name="a-link">
    <xsl:attribute name="text-decoration">underline</xsl:attribute>
    <xsl:attribute name="color">blue</xsl:attribute>
  </xsl:attribute-set>


  <!--======================================================================
      Templates
  =======================================================================-->

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Root
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="content">
    <fo:root xsl:use-attribute-sets="root">
      <xsl:call-template name="process-common-attributes"/>
      <xsl:call-template name="make-layout-master-set"/>
      <xsl:apply-templates/>
    </fo:root>
  </xsl:template>
  
  <xsl:template match="contentdigest | properties | request-properties" />

  <xsl:template name="make-layout-master-set">
    <fo:layout-master-set>
      <fo:simple-page-master master-name="all-pages"
                             xsl:use-attribute-sets="page">
        <fo:region-body margin-top="{$page-margin-top}"
                        margin-right="{$page-margin-right}"
                        margin-bottom="{$page-margin-bottom}"
                        margin-left="{$page-margin-left}"
                        column-count="{$column-count}"
                        column-gap="{$column-gap}"/>
        <xsl:choose>
          <xsl:when test="$writing-mode = 'tb-rl'">
            <fo:region-before extent="{$page-margin-right}"
                              precedence="true"/>
            <fo:region-after  extent="{$page-margin-left}"
                              precedence="true"/>
            <fo:region-start  region-name="page-header"
                              extent="{$page-margin-top}"
                              writing-mode="lr-tb"
                              display-align="before"/>
            <fo:region-end    region-name="page-footer"
                              extent="{$page-margin-bottom}"
                              writing-mode="lr-tb"
                              display-align="after"/>
          </xsl:when>
          <xsl:when test="$writing-mode = 'rl-tb'">
            <fo:region-before region-name="page-header"
                              extent="{$page-margin-top}"
                              display-align="before"/>
            <fo:region-after  region-name="page-footer"
                              extent="{$page-margin-bottom}"
                              display-align="after"/>
            <fo:region-start  extent="{$page-margin-right}"/>
            <fo:region-end    extent="{$page-margin-left}"/>
          </xsl:when>
          <xsl:otherwise><!-- $writing-mode = 'lr-tb' -->
            <fo:region-before region-name="page-header"
                              extent="{$page-margin-top}"
                              display-align="before"/>
            <fo:region-after  region-name="page-footer"
                              extent="{$page-margin-bottom}"
                              display-align="after"/>
            <fo:region-start  extent="{$page-margin-left}"/>
            <fo:region-end    extent="{$page-margin-bottom}"/>
          </xsl:otherwise>
        </xsl:choose>
      </fo:simple-page-master>
    </fo:layout-master-set>
  </xsl:template>

  <xsl:template match="head | script"/>

  <xsl:template match="rendered">
    <fo:page-sequence master-reference="all-pages"  >
      <!-- fo:title>
        Page: 
        <xsl:value-of select="/entity-service/entity/properties/property[@name='_title']"/>
      </fo:title -->
      <fo:static-content flow-name="page-header">
        <fo:block space-before.conditionality="retain"
                  space-before="{$page-header-margin}"
                  xsl:use-attribute-sets="page-header">
          <xsl:if test="$title-print-in-header = 'true'">
            Page: 
            <xsl:value-of select="/entity-service/entity/properties/property[@name='_title']"/>
          </xsl:if>
        </fo:block>
      </fo:static-content>
      <fo:static-content flow-name="page-footer">
        <fo:block space-after.conditionality="retain"
                  space-after="{$page-footer-margin}"
                  xsl:use-attribute-sets="page-footer">
          <xsl:if test="$page-number-print-in-footer = 'true'">
            <xsl:text>- </xsl:text>
            <fo:page-number/>
            <xsl:text> -</xsl:text>
          </xsl:if>
        </fo:block>
      </fo:static-content>
      <fo:flow flow-name="xsl-region-body">
        <fo:block xsl:use-attribute-sets="body">
          <xsl:call-template name="process-common-attributes"/>
          <xsl:apply-templates/>
        </fo:block>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
   process common attributes and children
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template name="process-common-attributes-and-children">
    <xsl:call-template name="process-common-attributes"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template name="process-common-attributes">
    <xsl:attribute name="role">
      <xsl:value-of select="concat('', local-name())"/>
    </xsl:attribute>

    <xsl:choose>
      <xsl:when test="@xml:lang">
        <xsl:attribute name="xml:lang">
          <xsl:value-of select="@xml:lang"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:when test="@lang">
        <xsl:attribute name="xml:lang">
          <xsl:value-of select="@lang"/>
        </xsl:attribute>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:when test="self::a/@name">
        <xsl:attribute name="id">
          <xsl:value-of select="@name"/>
        </xsl:attribute>
      </xsl:when>
    </xsl:choose>

    <xsl:if test="@align">
      <xsl:choose>
        <xsl:when test="self::caption">
        </xsl:when>
        <xsl:when test="self::img or self::object">
          <xsl:if test="@align = 'bottom' or @align = 'middle' or @align = 'top'">
            <xsl:attribute name="vertical-align">
              <xsl:value-of select="@align"/>
            </xsl:attribute>
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="process-cell-align">
            <xsl:with-param name="align" select="@align"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="@valign">
      <xsl:call-template name="process-cell-valign">
        <xsl:with-param name="valign" select="@valign"/>
      </xsl:call-template>
    </xsl:if>

    <xsl:if test="@style">
      <xsl:call-template name="process-style">
        <xsl:with-param name="style" select="@style"/>
      </xsl:call-template>
    </xsl:if>

  </xsl:template>

  <xsl:template name="process-style">
    <xsl:param name="style"/>
    <!-- e.g., style="text-align: center; color: red"
         converted to text-align="center" color="red" -->
    <xsl:variable name="name"
                  select="normalize-space(substring-before($style, ':'))"/>
    <xsl:if test="$name">
      <xsl:variable name="value-and-rest"
                    select="normalize-space(substring-after($style, ':'))"/>
      <xsl:variable name="value">
        <xsl:choose>
          <xsl:when test="contains($value-and-rest, ';')">
            <xsl:value-of select="normalize-space(substring-before(
                                  $value-and-rest, ';'))"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$value-and-rest"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$name = 'width' and (self::col or self::colgroup)">
          <xsl:attribute name="column-width">
            <xsl:value-of select="$value"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="$name = 'vertical-align' and (
                                 self::table or self::caption or
                                 self::thead or self::tfoot or
                                 self::tbody or self::colgroup or
                                 self::col or self::tr or
                                 self::th or self::td)">
          <xsl:choose>
            <xsl:when test="$value = 'top'">
              <xsl:attribute name="display-align">before</xsl:attribute>
            </xsl:when>
            <xsl:when test="$value = 'bottom'">
              <xsl:attribute name="display-align">after</xsl:attribute>
            </xsl:when>
            <xsl:when test="$value = 'middle'">
              <xsl:attribute name="display-align">center</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="display-align">auto</xsl:attribute>
              <xsl:attribute name="relative-align">baseline</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="{$name}">
            <xsl:value-of select="$value"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:variable name="rest"
                  select="normalize-space(substring-after($style, ';'))"/>
    <xsl:if test="$rest">
      <xsl:call-template name="process-style">
        <xsl:with-param name="style" select="$rest"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Block-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="h1">
    <fo:block xsl:use-attribute-sets="h1">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="h2">
    <fo:block xsl:use-attribute-sets="h2">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="h3">
    <fo:block xsl:use-attribute-sets="h3">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="h4">
    <fo:block xsl:use-attribute-sets="h4">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="h5">
    <fo:block xsl:use-attribute-sets="h5">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="h6">
    <fo:block xsl:use-attribute-sets="h6">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="p">
    <fo:block xsl:use-attribute-sets="p">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <!-- initial paragraph, preceded by h1..6 or div -->
  <xsl:template match="p[preceding-sibling::*[1][
                       self::h1 or self::h2 or self::h3 or
                       self::h4 or self::h5 or self::h6 or
                       self::div]]">
    <fo:block xsl:use-attribute-sets="p-initial">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <!-- initial paragraph, first child of div, body or td -->
  <xsl:template match="p[not(preceding-sibling::*) and (
                       parent::div or parent::body or
                       parent::td)]">
    <fo:block xsl:use-attribute-sets="p-initial-first">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="blockquote">
    <fo:block xsl:use-attribute-sets="blockquote">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="pre">
    <fo:block xsl:use-attribute-sets="pre">
      <xsl:call-template name="process-pre"/>
    </fo:block>
  </xsl:template>

  <xsl:template name="process-pre">
    <xsl:call-template name="process-common-attributes"/>
    <!-- remove leading CR/LF/CRLF char -->
    <xsl:variable name="crlf"><xsl:text>&#xD;&#xA;</xsl:text></xsl:variable>
    <xsl:variable name="lf"><xsl:text>&#xA;</xsl:text></xsl:variable>
    <xsl:variable name="cr"><xsl:text>&#xD;</xsl:text></xsl:variable>
    <xsl:for-each select="node()">
      <xsl:choose>
        <xsl:when test="position() = 1 and self::text()">
          <xsl:choose>
            <xsl:when test="starts-with(., $lf)">
              <xsl:value-of select="substring(., 2)"/>
            </xsl:when>
            <xsl:when test="starts-with(., $crlf)">
              <xsl:value-of select="substring(., 3)"/>
            </xsl:when>
            <xsl:when test="starts-with(., $cr)">
              <xsl:value-of select="substring(., 2)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="."/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="address">
    <fo:block xsl:use-attribute-sets="address">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="hr">
    <fo:block xsl:use-attribute-sets="hr">
      <xsl:call-template name="process-common-attributes"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="div">
    <!-- need fo:block-container? or normal fo:block -->
    <xsl:variable name="need-block-container">
      <xsl:call-template name="need-block-container"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$need-block-container = 'true'">
        <fo:block-container>
          <xsl:if test="@dir">
            <xsl:attribute name="writing-mode">
              <xsl:choose>
                <xsl:when test="@dir = 'rtl'">rl-tb</xsl:when>
                <xsl:otherwise>lr-tb</xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
          </xsl:if>
          <xsl:call-template name="process-common-attributes"/>
          <fo:block start-indent="0pt" end-indent="0pt">
            <xsl:apply-templates/>
          </fo:block>
        </fo:block-container>
      </xsl:when>
      <xsl:otherwise>
        <!-- normal block -->
        <fo:block>
          <xsl:call-template name="process-common-attributes"/>
          <xsl:apply-templates/>
        </fo:block>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="need-block-container">
    <xsl:choose>
      <xsl:when test="@dir">true</xsl:when>
      <xsl:when test="@style">
        <xsl:variable name="s"
                      select="concat(';', translate(normalize-space(@style),
                                                    ' ', ''))"/>
        <xsl:choose>
          <xsl:when test="contains($s, ';width:') or
                          contains($s, ';height:') or
                          contains($s, ';position:absolute') or
                          contains($s, ';position:fixed') or
                          contains($s, ';writing-mode:')">true</xsl:when>
          <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="center">
    <fo:block text-align="center">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="fieldset | form | dir | menu">
    <fo:block space-before="1em" space-after="1em">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       List
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="ul">
    <xsl:if test="li">
    <fo:list-block xsl:use-attribute-sets="ul">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:list-block>      
    </xsl:if>
  </xsl:template>

  <xsl:template match="li//ul">
    <xsl:if test="li">
      
    <fo:list-block xsl:use-attribute-sets="ul-nested">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:list-block>
    </xsl:if>
  </xsl:template>

  <xsl:template match="ol">
    <xsl:if test="li">
      <fo:list-block xsl:use-attribute-sets="ol">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:list-block>
    </xsl:if>
  </xsl:template>

  <xsl:template match="li//ol">
    <xsl:if test="li">
      
    <fo:list-block xsl:use-attribute-sets="ol-nested">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:list-block>
    </xsl:if>
  </xsl:template>

  <xsl:template match="ul/li">
    <fo:list-item xsl:use-attribute-sets="ul-li">
      <xsl:call-template name="process-ul-li"/>
    </fo:list-item>
  </xsl:template>

  <xsl:template name="process-ul-li">
    <xsl:call-template name="process-common-attributes"/>
    <fo:list-item-label end-indent="label-end()"
                        text-align="end" wrap-option="no-wrap">
      <fo:block>
        <xsl:variable name="depth" select="count(ancestor::ul)" />
        <xsl:choose>
          <xsl:when test="$depth = 1">
            <fo:inline xsl:use-attribute-sets="ul-label-1">
              <xsl:value-of select="$ul-label-1"/>
            </fo:inline>
          </xsl:when>
          <xsl:when test="$depth = 2">
            <fo:inline xsl:use-attribute-sets="ul-label-2">
              <xsl:value-of select="$ul-label-2"/>
            </fo:inline>
          </xsl:when>
          <xsl:otherwise>
            <fo:inline xsl:use-attribute-sets="ul-label-3">
              <xsl:value-of select="$ul-label-3"/>
            </fo:inline>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:list-item-label>
    <fo:list-item-body start-indent="body-start()">
      <fo:block>
        <xsl:apply-templates/>
      </fo:block>
    </fo:list-item-body>
  </xsl:template>

  <xsl:template match="ol/li">
    <fo:list-item xsl:use-attribute-sets="ol-li">
      <xsl:call-template name="process-ol-li"/>
    </fo:list-item>
  </xsl:template>

  <xsl:template name="process-ol-li">
    <xsl:call-template name="process-common-attributes"/>
    <fo:list-item-label end-indent="label-end()"
                        text-align="end" wrap-option="no-wrap">
      <fo:block>
        <xsl:variable name="depth" select="count(ancestor::ol)" />
        <xsl:choose>
          <xsl:when test="$depth = 1">
            <fo:inline xsl:use-attribute-sets="ol-label-1">
              <xsl:number format="{$ol-label-1}"/>
            </fo:inline>
          </xsl:when>
          <xsl:when test="$depth = 2">
            <fo:inline xsl:use-attribute-sets="ol-label-2">
              <xsl:number format="{$ol-label-2}"/>
            </fo:inline>
          </xsl:when>
          <xsl:otherwise>
            <fo:inline xsl:use-attribute-sets="ol-label-3">
              <xsl:number format="{$ol-label-3}"/>
            </fo:inline>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:list-item-label>
    <fo:list-item-body start-indent="body-start()">
      <fo:block>
        <xsl:apply-templates/>
      </fo:block>
    </fo:list-item-body>
  </xsl:template>

  <xsl:template match="dl">
    <fo:block xsl:use-attribute-sets="dl">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="dt">
    <fo:block xsl:use-attribute-sets="dt">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="dd">
    <fo:block xsl:use-attribute-sets="dd">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:block>
  </xsl:template>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Table
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="table-ignore">
    <fo:table-and-caption xsl:use-attribute-sets="table-and-caption">
      <xsl:call-template name="make-table-caption"/>
      <fo:table xsl:use-attribute-sets="table">
        <xsl:call-template name="process-table"/>
      </fo:table>
    </fo:table-and-caption>
  </xsl:template>
  
  <xsl:template match="table">
      <fo:table xsl:use-attribute-sets="table">
        <xsl:call-template name="process-table"/>
      </fo:table>
  </xsl:template>

  <xsl:template name="make-table-caption">
    <xsl:if test="caption/@align">
      <xsl:attribute name="caption-side">
        <xsl:value-of select="caption/@align"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="caption"/>
  </xsl:template>

  <xsl:template name="process-table">
    <xsl:if test="@width">
      <xsl:attribute name="inline-progression-dimension">
        <xsl:choose>
          <xsl:when test="contains(@width, '%')">
            <xsl:value-of select="@width"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@width"/>px</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@border or @frame">
      <xsl:choose>
        <xsl:when test="@border &gt; 0">
          <xsl:attribute name="border">
            <xsl:value-of select="@border"/>px</xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="@border = '0' or @frame = 'void'">
          <xsl:attribute name="border-style">hidden</xsl:attribute>
        </xsl:when>
        <xsl:when test="@frame = 'above'">
          <xsl:attribute name="border-style">outset hidden hidden hidden</xsl:attribute>
        </xsl:when>
        <xsl:when test="@frame = 'below'">
          <xsl:attribute name="border-style">hidden hidden outset hidden</xsl:attribute>
        </xsl:when>
        <xsl:when test="@frame = 'hsides'">
          <xsl:attribute name="border-style">outset hidden</xsl:attribute>
        </xsl:when>
        <xsl:when test="@frame = 'vsides'">
          <xsl:attribute name="border-style">hidden outset</xsl:attribute>
        </xsl:when>
        <xsl:when test="@frame = 'lhs'">
          <xsl:attribute name="border-style">hidden hidden hidden outset</xsl:attribute>
        </xsl:when>
        <xsl:when test="@frame = 'rhs'">
          <xsl:attribute name="border-style">hidden outset hidden hidden</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="border-style">outset</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="@cellspacing">
      <xsl:attribute name="border-spacing">
        <xsl:value-of select="@cellspacing"/>px</xsl:attribute>
      <xsl:attribute name="border-collapse">separate</xsl:attribute>
    </xsl:if>
    <xsl:if test="@rules and (@rules = 'groups' or
                      @rules = 'rows' or
                      @rules = 'cols' or
                      @rules = 'all' and (not(@border or @frame) or
                          @border = '0' or @frame and
                          not(@frame = 'box' or @frame = 'border')))">
      <xsl:attribute name="border-collapse">collapse</xsl:attribute>
      <xsl:if test="not(@border or @frame)">
        <xsl:attribute name="border-style">hidden</xsl:attribute>
      </xsl:if>
    </xsl:if>
    <xsl:call-template name="process-common-attributes"/>
    <xsl:apply-templates select="col | colgroup"/>
    <xsl:apply-templates select="thead"/>
    <xsl:apply-templates select="tfoot"/>
    <xsl:choose>
      <xsl:when test="tbody">
        <xsl:apply-templates select="tbody"/>
      </xsl:when>
      <xsl:otherwise>
        <fo:table-body xsl:use-attribute-sets="tbody">
          <xsl:apply-templates select="tr"/>
        </fo:table-body>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="caption">
    <fo:table-caption xsl:use-attribute-sets="table-caption">
      <xsl:call-template name="process-common-attributes"/>
      <fo:block>
        <xsl:apply-templates/>
      </fo:block>
    </fo:table-caption>
  </xsl:template>

  <xsl:template match="thead">
    <fo:table-header xsl:use-attribute-sets="thead">
      <xsl:call-template name="process-table-rowgroup"/>
    </fo:table-header>
  </xsl:template>

  <xsl:template match="tfoot">
    <fo:table-footer xsl:use-attribute-sets="tfoot">
      <xsl:call-template name="process-table-rowgroup"/>
    </fo:table-footer>
  </xsl:template>

  <xsl:template match="tbody">
    <fo:table-body xsl:use-attribute-sets="tbody">
      <xsl:call-template name="process-table-rowgroup"/>
    </fo:table-body>
  </xsl:template>

  <xsl:template name="process-table-rowgroup">
    <xsl:if test="ancestor::table[1]/@rules = 'groups'">
      <xsl:attribute name="border">1px solid</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="process-common-attributes-and-children"/>
  </xsl:template>

  <xsl:template match="colgroup">
    <fo:table-column xsl:use-attribute-sets="table-column">
      <xsl:call-template name="process-table-column"/>
    </fo:table-column>
  </xsl:template>

  <xsl:template match="colgroup[col]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="col">
    <fo:table-column xsl:use-attribute-sets="table-column">
      <xsl:call-template name="process-table-column"/>
    </fo:table-column>
  </xsl:template>

  <xsl:template name="process-table-column">
    <xsl:if test="parent::colgroup">
      <xsl:call-template name="process-col-width">
        <xsl:with-param name="width" select="../@width"/>
      </xsl:call-template>
      <xsl:call-template name="process-cell-align">
        <xsl:with-param name="align" select="../@align"/>
      </xsl:call-template>
      <xsl:call-template name="process-cell-valign">
        <xsl:with-param name="valign" select="../@valign"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="@span">
      <xsl:attribute name="number-columns-repeated">
        <xsl:value-of select="@span"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:call-template name="process-col-width">
      <xsl:with-param name="width" select="@width"/>
      <!-- it may override parent colgroup's width -->
    </xsl:call-template>
    <xsl:if test="ancestor::table[1]/@rules = 'cols'">
      <xsl:attribute name="border">1px solid</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="process-common-attributes"/>
    <!-- this processes also align and valign -->
  </xsl:template>

  <xsl:template match="tr">
    <fo:table-row xsl:use-attribute-sets="tr">
      <xsl:call-template name="process-table-row"/>
    </fo:table-row>
  </xsl:template>

  <xsl:template match="tr[parent::table and th and not(td)]">
    <fo:table-row xsl:use-attribute-sets="tr" keep-with-next="always">
      <xsl:call-template name="process-table-row"/>
    </fo:table-row>
  </xsl:template>

  <xsl:template name="process-table-row">
    <xsl:if test="ancestor::table[1]/@rules = 'rows'">
      <xsl:attribute name="border">1px solid</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="process-common-attributes-and-children"/>
  </xsl:template>

  <xsl:template match="th">
    <fo:table-cell xsl:use-attribute-sets="th">
      <xsl:call-template name="process-table-cell"/>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="td">
    <fo:table-cell xsl:use-attribute-sets="td">
      <xsl:call-template name="process-table-cell"/>
    </fo:table-cell>
  </xsl:template>

  <xsl:template name="process-table-cell">
    <xsl:if test="@colspan">
      <xsl:attribute name="number-columns-spanned">
        <xsl:value-of select="@colspan"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@rowspan">
      <xsl:attribute name="number-rows-spanned">
        <xsl:value-of select="@rowspan"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:for-each select="ancestor::table[1]">
      <xsl:if test="(@border or @rules) and (@rules = 'all' or
                    not(@rules) and not(@border = '0'))">
        <xsl:attribute name="border-style">inset</xsl:attribute>
      </xsl:if>
      <xsl:if test="@cellpadding">
        <xsl:attribute name="padding">
          <xsl:choose>
            <xsl:when test="contains(@cellpadding, '%')">
              <xsl:value-of select="@cellpadding"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@cellpadding"/>px</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(@align or ../@align or
                      ../parent::*[self::thead or self::tfoot or
                      self::tbody]/@align) and
                  ancestor::table[1]/*[self::col or
                      self::colgroup]/descendant-or-self::*/@align">
      <xsl:attribute name="text-align">from-table-column()</xsl:attribute>
    </xsl:if>
    <xsl:if test="not(@valign or ../@valign or
                      ../parent::*[self::thead or self::tfoot or
                      self::tbody]/@valign) and
                  ancestor::table[1]/*[self::col or
                      self::colgroup]/descendant-or-self::*/@valign">
      <xsl:attribute name="display-align">from-table-column()</xsl:attribute>
      <xsl:attribute name="relative-align">from-table-column()</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="process-common-attributes"/>
    <fo:block>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template name="process-col-width">
    <xsl:param name="width"/>
    <xsl:if test="$width and $width != '0*'">
      <xsl:attribute name="column-width">
        <xsl:choose>
          <xsl:when test="contains($width, '*')">
            <xsl:text>proportional-column-width(</xsl:text>
            <xsl:value-of select="substring-before($width, '*')"/>
            <xsl:text>)</xsl:text>
          </xsl:when>
          <xsl:when test="contains($width, '%')">
            <xsl:value-of select="$width"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$width"/>px</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template name="process-cell-align">
    <xsl:param name="align"/>
    <xsl:if test="$align">
      <xsl:attribute name="text-align">
        <xsl:choose>
          <xsl:when test="$align = 'char'">
            <xsl:choose>
              <xsl:when test="$align/../@char">
                <xsl:value-of select="$align/../@char"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'.'"/>
                <!-- todo: it should depend on xml:lang ... -->
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$align"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template name="process-cell-valign">
    <xsl:param name="valign"/>
    <xsl:if test="$valign">
      <xsl:attribute name="display-align">
        <xsl:choose>
          <xsl:when test="$valign = 'middle'">center</xsl:when>
          <xsl:when test="$valign = 'bottom'">after</xsl:when>
          <xsl:when test="$valign = 'baseline'">auto</xsl:when>
          <xsl:otherwise>before</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:if test="$valign = 'baseline'">
        <xsl:attribute name="relative-align">baseline</xsl:attribute>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Inline-level
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="b">
    <fo:inline xsl:use-attribute-sets="b">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="strong">
    <fo:inline xsl:use-attribute-sets="strong">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="strong//em | em//strong">
    <fo:inline xsl:use-attribute-sets="strong-em">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="i">
    <fo:inline xsl:use-attribute-sets="i">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="cite">
    <fo:inline xsl:use-attribute-sets="cite">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="em">
    <fo:inline xsl:use-attribute-sets="em">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="var">
    <fo:inline xsl:use-attribute-sets="var">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="dfn">
    <fo:inline xsl:use-attribute-sets="dfn">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="tt">
    <fo:inline xsl:use-attribute-sets="tt">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="code">
    <fo:inline xsl:use-attribute-sets="code">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="kbd">
    <fo:inline xsl:use-attribute-sets="kbd">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="samp">
    <fo:inline xsl:use-attribute-sets="samp">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="big">
    <fo:inline xsl:use-attribute-sets="big">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="small">
    <fo:inline xsl:use-attribute-sets="small">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="sub">
    <fo:inline xsl:use-attribute-sets="sub">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="sup">
    <fo:inline xsl:use-attribute-sets="sup">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="s">
    <fo:inline xsl:use-attribute-sets="s">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="strike">
    <fo:inline xsl:use-attribute-sets="strike">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="del">
    <fo:inline xsl:use-attribute-sets="del">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="u">
    <fo:inline xsl:use-attribute-sets="u">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="ins">
    <fo:inline xsl:use-attribute-sets="ins">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="abbr">
    <fo:inline xsl:use-attribute-sets="abbr">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="acronym">
    <fo:inline xsl:use-attribute-sets="acronym">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="span">
    <fo:inline>
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="span[@dir]">
    <fo:bidi-override direction="{@dir}" unicode-bidi="embed">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:bidi-override>
  </xsl:template>

  <xsl:template match="span[@style and contains(@style, 'writing-mode')]">
    <fo:inline-container alignment-baseline="central"
                         text-indent="0pt"
                         last-line-end-indent="0pt"
                         start-indent="0pt"
                         end-indent="0pt"
                         text-align="center"
                         text-align-last="center">
      <xsl:call-template name="process-common-attributes"/>
      <fo:block wrap-option="no-wrap" >
        <xsl:apply-templates/>
      </fo:block>
    </fo:inline-container>
  </xsl:template>

  <xsl:template match="bdo">
    <fo:bidi-override direction="{@dir}" unicode-bidi="bidi-override">
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:bidi-override>
  </xsl:template>

  <xsl:template match="br">
    <fo:block>
      <xsl:call-template name="process-common-attributes"/>
    </fo:block>
  </xsl:template>

  <xsl:template match="q">
    <fo:inline xsl:use-attribute-sets="q">
      <xsl:call-template name="process-common-attributes"/>
      <xsl:choose>
        <xsl:when test="lang('ja')">
          <xsl:text>「</xsl:text>
          <xsl:apply-templates/>
          <xsl:text>」</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <!-- lang('en') -->
          <xsl:text>“</xsl:text>
          <xsl:apply-templates/>
          <xsl:text>”</xsl:text>
          <!-- todo: other languages ...-->
        </xsl:otherwise>
      </xsl:choose>
    </fo:inline>
  </xsl:template>

  <xsl:template match="q//q">
    <fo:inline xsl:use-attribute-sets="q-nested">
      <xsl:call-template name="process-common-attributes"/>
      <xsl:choose>
        <xsl:when test="lang('ja')">
          <xsl:text>『</xsl:text>
          <xsl:apply-templates/>
          <xsl:text>』</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <!-- lang('en') -->
          <xsl:text>‘</xsl:text>
          <xsl:apply-templates/>
          <xsl:text>’</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </fo:inline>
  </xsl:template>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Image
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="img">
    <fo:external-graphic xsl:use-attribute-sets="img">
      <xsl:call-template name="process-img"/>
    </fo:external-graphic>
  </xsl:template>

  <xsl:template match="img[ancestor::a/@href]">
    <fo:external-graphic xsl:use-attribute-sets="img-link">
      <xsl:call-template name="process-img"/>
    </fo:external-graphic>
  </xsl:template>

  <xsl:template name="process-img">
    <xsl:attribute name="src">
      <xsl:text>url('</xsl:text>
      <xsl:value-of select="@src"/>
      <xsl:text>')</xsl:text>
    </xsl:attribute>
    <xsl:if test="@alt">
      <xsl:attribute name="role">
        <xsl:value-of select="@alt"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@width">
      <xsl:choose>
        <xsl:when test="contains(@width, '%')">
          <xsl:attribute name="width">
            <xsl:value-of select="@width"/>
          </xsl:attribute>
          <xsl:attribute name="content-width">scale-to-fit</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="content-width">
            <xsl:value-of select="@width"/>px</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="@height">
      <xsl:choose>
        <xsl:when test="contains(@height, '%')">
          <xsl:attribute name="height">
            <xsl:value-of select="@height"/>
          </xsl:attribute>
          <xsl:attribute name="content-height">scale-to-fit</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="content-height">
            <xsl:value-of select="@height"/>px</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="@border">
      <xsl:attribute name="border">
        <xsl:value-of select="@border"/>px solid</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="process-common-attributes"/>
  </xsl:template>

  <xsl:template match="object">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="param"/>
  <xsl:template match="map"/>
  <xsl:template match="area"/>
  <xsl:template match="label"/>
  <xsl:template match="input"/>
  <xsl:template match="select"/>
  <xsl:template match="optgroup"/>
  <xsl:template match="option"/>
  <xsl:template match="textarea"/>
  <xsl:template match="legend"/>
  <xsl:template match="button"/>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Link
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="a">
    <fo:inline>
      <xsl:call-template name="process-common-attributes-and-children"/>
    </fo:inline>
  </xsl:template>

  <xsl:template match="a[@href]">
    <fo:basic-link xsl:use-attribute-sets="a-link">
      <xsl:call-template name="process-a-link"/>
    </fo:basic-link>
  </xsl:template>

  <xsl:template name="process-a-link">
    <xsl:call-template name="process-common-attributes"/>
    <xsl:choose>
      <xsl:when test="starts-with(@href,'#')">
        <xsl:attribute name="internal-destination">
          <xsl:value-of select="substring-after(@href,'#')"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="external-destination">
          <xsl:text>url('</xsl:text>
          <xsl:value-of select="@href"/>
          <xsl:text>')</xsl:text>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="@title">
      <xsl:attribute name="role">
        <xsl:value-of select="@title"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:apply-templates/>
  </xsl:template>

  <!--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
       Ruby
  =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-->

  <xsl:template match="ruby">
    <fo:inline-container alignment-baseline="central"
                         block-progression-dimension="1em"
                         text-indent="0pt"
                         last-line-end-indent="0pt"
                         start-indent="0pt"
                         end-indent="0pt"
                         text-align="center"
                         text-align-last="center">
      <xsl:call-template name="process-common-attributes"/>
      <fo:block font-size="50%"
                wrap-option="no-wrap"
                space-before.conditionality="retain"
                space-before="-1.1em"
                space-after="0.1em"
                role="rt">
        <xsl:for-each select="rt | rtc[1]/rt">
          <xsl:call-template name="process-common-attributes"/>
          <xsl:apply-templates/>
        </xsl:for-each>
      </fo:block>
      <fo:block wrap-option="no-wrap"  role="rb">
        <xsl:for-each select="rb | rbc[1]/rb">
          <xsl:call-template name="process-common-attributes"/>
          <xsl:apply-templates/>
        </xsl:for-each>
      </fo:block>
      <xsl:if test="rtc[2]/rt">
        <fo:block font-size="50%"
                  wrap-option="no-wrap"
                  space-before="0.1em"
                  space-after.conditionality="retain"
                  space-after="-1.1em"
                  role="rt">
          <xsl:for-each select="rt | rtc[2]/rt">
            <xsl:call-template name="process-common-attributes"/>
            <xsl:apply-templates/>
          </xsl:for-each>
        </fo:block>
      </xsl:if>
    </fo:inline-container>
  </xsl:template>
</xsl:stylesheet>
