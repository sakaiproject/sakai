<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	version="1.0">
<xsl:import href="scheduleUtil.xsl"/>
<xsl:template match="schedule">
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">  
  <!-- defines page layout -->
  <fo:layout-master-set>

    <!-- layout for the calendar page -->
    <fo:simple-page-master master-name="calendar"
                  margin-top="0.1cm" 
                  margin-bottom="0.1cm" 
                  margin-left="0.0cm" 
                  margin-right="0.2cm">
	<xsl:attribute name="page-height">
		<xsl:choose>
			<xsl:when test="$col-cnt &gt; 1">21.5cm</xsl:when>
			<xsl:otherwise>29.7cm</xsl:otherwise>
		</xsl:choose>
	</xsl:attribute>
	<xsl:attribute name="page-width">
		<xsl:choose>
			<xsl:when test="$col-cnt &gt; 1">29.7cm</xsl:when>
			<xsl:otherwise>21.5cm</xsl:otherwise>
		</xsl:choose>
	</xsl:attribute>

    <fo:region-body margin-top="{$topMarginPt}cm" margin-left="{$leftBarPt}cm" 
      			      margin-right="{$rightMarginPt}cm"/>	
      <fo:region-before precedence="true" extent="{$topMarginPt}cm"/>
      <fo:region-start extent="{$leftBarPt}cm"/>
    </fo:simple-page-master>

  </fo:layout-master-set>
  <!-- end: defines page layout -->

  <!-- actual layout -->
  <fo:page-sequence master-reference="calendar" initial-page-number="1">
    <fo:static-content flow-name="xsl-region-before">
   		<xsl:variable name="frm-len" select="$frmlen"/>
		<fo:block font-size="12pt" 
            font-family="sans-serif"
            font-weight="bold" 
            line-height="0.5cm"
            space-after.optimum="1pt"
            color="black"
            text-align="center"
            padding-top="0pt">
        	Calendar for <xsl:value-of select="uid"/> 
    	</fo:block>    	 
   </fo:static-content> 

   <fo:static-content flow-name="xsl-region-start">
		<!-- 10 time slots for the left-side bar. -->	
		 	<!-- skip a line to align up with coloum headings -->
	
		<fo:block-container 
		height="20.5cm" width="{$leftBarPt - 0.05}cm" top="0cm" left="0cm" position="absolute">
		<fo:block line-height="{$heightPerTimeslot div 2}cm"> 	
	 		<fo:leader leader-pattern="dots" 
                   rule-thickness="1.0pt"          
                   leader-length="0.1cm"/> 
    	</fo:block>
	
		<xsl:variable name="intimePt" select="$begin + $hoursPerPage"/>
		<xsl:call-template name="left-bar">
  			<xsl:with-param name="intimePt" select="$intimePt"/>		
		</xsl:call-template> 

		</fo:block-container>
		
    </fo:static-content> 

    <fo:flow flow-name="xsl-region-body">
	<xsl:choose>
		<xsl:when test="list">
			<xsl:apply-templates select="list[1]">
				<xsl:with-param name="containerPt" select="0"/>
			</xsl:apply-templates>
		</xsl:when>
		<xsl:otherwise>
			<fo:block></fo:block>
	      </xsl:otherwise>
	</xsl:choose>
    </fo:flow> 
  </fo:page-sequence>
</fo:root>
</xsl:template>
</xsl:stylesheet>

