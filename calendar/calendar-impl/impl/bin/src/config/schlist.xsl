<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
   xmlns:ResourceBundle="http://xml.apache.org/xalan/java/java.util.ResourceBundle"
	version="1.0">

<xsl:output encoding='utf-8'/>

<xsl:param name="sched"/>
<xsl:param name="site"/>
<xsl:param name="event"/>
<xsl:param name="location"/>
<xsl:param name="type"/>
<xsl:param name="from"/>
        
<xsl:template match="schedule">
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="DejaVuSans">

  <fo:layout-master-set>
    <!-- page layout -->
    <fo:simple-page-master master-name="first"
                  page-height="29.7cm" 
                  page-width="21cm"
                  margin-top="2.5cm" 
                  margin-bottom="2cm" 
                  margin-left="2.5cm" 
                  margin-right="2.5cm">
      <fo:region-body margin-top="1.5cm" />
      <fo:region-before precedence="true" extent="1.5cm"/>
      <fo:region-after extent="0.5cm"/>

    </fo:simple-page-master>
  </fo:layout-master-set>
  <!-- end: defines page layout -->

  <!-- actual layout -->
  <fo:page-sequence master-reference="first">

  <fo:static-content flow-name="xsl-region-before">
   
	
	<fo:block font-size="12pt" 
            font-family="DejaVuSans" 
            line-height="1cm"
            space-after.optimum="1pt"
            color="black"
            text-align="left"
            padding-top="0pt">
		<xsl:value-of select="$sched"/><xsl:text> </xsl:text><xsl:value-of select="uid"/> - <fo:page-number/> 
    	</fo:block>    	
   </fo:static-content> 
	  
  <!--<fo:static-content flow-name="xsl-region-after">-->
	<!--<fo:block text-align="end"-->
			<!--font-size="7pt" font-family="verdana,sans-serif" line-height="1em + 2pt">-->
			<!--<xsl:value-of select="ResourceLoader:getString($rb, 'sched.for')"/><xsl:text> </xsl:text><xsl:value-of select="uid"/> - <fo:page-number/>-->
      <!--</fo:block>-->
  <!--</fo:static-content>-->


    <fo:flow flow-name="xsl-region-body">
	<xsl:choose>
	<xsl:when test="list">
		<xsl:apply-templates select="list"/>
	</xsl:when>
	<xsl:otherwise>
		<fo:block> </fo:block>
	</xsl:otherwise>
	</xsl:choose>	
    </fo:flow> 
  </fo:page-sequence>
</fo:root>
</xsl:template>


<xsl:template match="list">
   <xsl:if test="event">
      <!-- list start -->
      <!-- use provisional-distance-between-starts to define 
           the distance between the start of the label and the item text 
           
           use provisional-label-separation to define the distance between
           the end of the item label and the start of item text
      -->
      <fo:block>
      <fo:list-block provisional-distance-between-starts="2cm"
                     provisional-label-separation="2mm">
        
        <!-- list item -->
        <fo:list-item>
          <!-- insert a bullet -->
          <fo:list-item-label end-indent="label-end()">
          	<fo:block><fo:inline   font-size="7pt" font-family="DejaVuSans">
			    <xsl:value-of select="@dt"/></fo:inline></fo:block>
          </fo:list-item-label>
          <!-- list text --> 
          <fo:list-item-body start-indent="body-start()">
		 	<xsl:apply-templates select="event"/>
          </fo:list-item-body>
        </fo:list-item>
    	</fo:list-block>
    		</fo:block>
    </xsl:if>
    <!-- list end --> 
</xsl:template>

<xsl:template match="event">
	<xsl:choose>
	<xsl:when test="row">
		<xsl:for-each select="row">
			<xsl:for-each select="col">
				<xsl:for-each select="subEvent">
				<xsl:call-template name="evitem">
				<xsl:with-param name="ev" select="."/>
				</xsl:call-template>
				</xsl:for-each>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:when>
	<xsl:otherwise>
		<xsl:call-template name="evitem">
			<xsl:with-param name="ev" select="."/>
		</xsl:call-template>
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="evitem">
<xsl:param name="ev"/>
	<fo:block font-weight="bold" font-size="8pt" text-align="start">
			<xsl:value-of select="$site"/><xsl:text> </xsl:text><xsl:value-of select="grp"/>
	</fo:block>
	<fo:block font-size="8pt" text-align="start">
	  <xsl:value-of select="$event"/><xsl:text> </xsl:text><xsl:value-of select="title"/>
	</fo:block>
	<fo:block font-size="8pt" text-align="start">
		<fo:inline>
			<xsl:value-of select="@dt"/><xsl:value-of select="$from"/><xsl:text> </xsl:text></fo:inline>
		<xsl:call-template name="dtconv">
		 <xsl:with-param name="fromdt" select="@from"/>
		</xsl:call-template>-		
		<xsl:call-template name="dtconv">
			<xsl:with-param name="fromdt" select="@to"/>
		</xsl:call-template>		
	</fo:block>
	<xsl:if test="place !=''">
		<fo:block font-size="8pt" text-align="start">
			<xsl:value-of select="$location"/><xsl:text> </xsl:text><xsl:value-of select="place"/>
		</fo:block>
	</xsl:if>
	<fo:block font-size="8pt" text-align="start">
		<xsl:value-of select="faculty"/>
	</fo:block>
	
	<fo:block font-size="8pt" text-align="start">
	 <xsl:value-of select="$type"/><xsl:text> </xsl:text><xsl:value-of select="type"/>
	</fo:block>
	<xsl:if test="description !=''">
		<fo:block font-size="7pt" text-align="start" padding="2mm">
			<xsl:value-of select="description"/>
		</fo:block>
	</xsl:if>		
	
	
	<fo:block font-size="8pt" text-align="start" space-after="3mm">
	    <fo:leader leader-pattern="dots" 
                   leader-length="10cm"/>
    </fo:block>
</xsl:template>

<xsl:template name="dtconv">
<xsl:param name="fromdt"/>
	
	<xsl:variable name="tfromdt" select="substring-before($fromdt, ':')"/>
	<xsl:choose>
	<xsl:when test="$tfromdt = 0 ">
		12:00AM
	</xsl:when>
	<xsl:when test="$tfromdt &lt; 12">
		<xsl:value-of select="$fromdt"/>AM
	</xsl:when>
		
	<xsl:when test="$tfromdt = 12">
		<xsl:value-of select="$fromdt"/>PM
	</xsl:when>
	
	<xsl:when test="$tfromdt &gt; 12">
		<xsl:value-of select="$tfromdt - 12"/>:<xsl:value-of select="substring-after($fromdt, ':')"/>PM
	</xsl:when>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
  

