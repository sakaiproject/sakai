<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
 doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

<xsl:template match="/">

<itemData>
  <ident><xsl:value-of select="//item/@ident" /></ident>
  <title><xsl:value-of select="//item/@title" /></title>
  <instruction></instruction>
  <createdBy></createdBy>
  <createdDate></createdDate>
  <lastModifiedBy></lastModifiedBy>
  <lastModifiedDate></lastModifiedDate>	
	
  <!-- type -->	
  <itemRcardinality>
    <xsl:value-of select="//presentation/response_lid/@rcardinality " />
  </itemRcardinality>

  <type>
	<xsl:choose>
      <xsl:when test="//itemmetadata/qmd_itemtype"><xsl:value-of select="//itemmetadata/qmd_itemtype"/></xsl:when>
      <xsl:when test="//render_fib and //resprocessing/respcondition/conditionvar/varequal">FIB</xsl:when>
    </xsl:choose>
  </type>	
	
  <!-- score -->	
  <scoreList type="list">
    <xsl:for-each select="//resprocessing/respcondition/setvar">
      <xsl:if test="@varname='que_score' and @action='Set'">
        <xsl:value-of select="."/>
      </xsl:if>
    </xsl:for-each>
  </scoreList>	

  <score>
    <xsl:value-of
    select="//resprocessing/outcomes/decvar[@varname='que_score']/@maxvalue"/>	
  </score>
	
  <!-- item text -->
  <itemText type="list">
  <xsl:for-each select="//presentation/material/*">
	<xsl:if test="name()='mattext'">
		<xsl:value-of select="." />
	</xsl:if>
	<xsl:if test="name()='matimage'">
		<xsl:variable name="mimage" select="@uri" />
		<img alt="" src="{$mimage}"/>
	</xsl:if>
  </xsl:for-each>
  </itemText>
  

  <!-- answers -->
  <xsl:for-each select="//presentation/response_lid/render_choice/response_label" >
    <itemAnswer type="list">
	  <xsl:value-of select="@ident"/>:::<xsl:for-each select="./material/*">
		<xsl:if test="name()='mattext'">
			<xsl:value-of select="." />
		</xsl:if>
		<xsl:if test="name()='matimage'">
			<xsl:variable name="mimage" select="@uri" />
			<img alt="" src="{$mimage}"/>
		</xsl:if>
	  </xsl:for-each>
	</itemAnswer>
  </xsl:for-each>

  <xsl:for-each select="//resprocessing/respcondition/conditionvar/varequal" >
    <itemFibAnswer type="list"><xsl:value-of select="."/></itemFibAnswer>
  </xsl:for-each>

  <xsl:for-each select="//presentation/response_lid">
    <itemMatchSourceText type="list">
	  <xsl:value-of select="@ident"/>:::<xsl:for-each select="./material/*">
		<xsl:if test="name()='mattext'">
			<xsl:value-of select="." />
		</xsl:if>
		<xsl:if test="name()='matimage'">
			<xsl:variable name="mimage" select="@uri" />
			<img alt="" src="{$mimage}"/>
		</xsl:if>
	  </xsl:for-each>
	</itemMatchSourceText>
  </xsl:for-each>

  <!-- label(s) for correct answer(s), if any -->
  <xsl:for-each select="//resprocessing/respcondition/displayfeedback">
    <xsl:if test="contains(@linkrefid, '_C')">
	  <itemAnswerCorrectLabel type="list">
	    <xsl:value-of select="../conditionvar/varequal"/>
	  </itemAnswerCorrectLabel>
    </xsl:if>
  </xsl:for-each>

  <xsl:for-each select="//resprocessing/respcondition/setvar">
    <xsl:if test="@varname='Respondus_Correct'">
	  <itemMatchingAnswerCorrect type="list">
	    <xsl:value-of select="../conditionvar/varequal/@respident"/>:::<xsl:value-of select="../conditionvar/varequal"/>
	  </itemMatchingAnswerCorrect>
    </xsl:if>
  </xsl:for-each>

  <!-- feedback -->
  <xsl:for-each select="//itemfeedback">
    <xsl:if test="contains(@ident, '_ALL')">
      <allFeedback>
        <xsl:for-each select="./material/*">
	      <xsl:if test="name()='mattext'">
		    <xsl:value-of select="." />
	      </xsl:if>
	      <xsl:if test="name()='matimage'">
		    <xsl:variable name="mimage" select="@uri" />
		    <img alt="" src="{$mimage}"/>
	      </xsl:if>
        </xsl:for-each>
      </allFeedback>
    </xsl:if>
  </xsl:for-each>

  <xsl:for-each select="//itemfeedback">
    <xsl:if test="contains(@ident, '_C')">
      <correctFeedback>
        <xsl:for-each select="./material/*">
	      <xsl:if test="name()='mattext'">
		    <xsl:value-of select="." />
	      </xsl:if>
	      <xsl:if test="name()='matimage'">
		    <xsl:variable name="mimage" select="@uri" />
		    <img alt="" src="{$mimage}"/>
	      </xsl:if>
        </xsl:for-each>
      </correctFeedback>
    </xsl:if>
  </xsl:for-each>

  <xsl:for-each select="//itemfeedback">
    <xsl:if test="contains(@ident, '_IC')">
      <incorrectFeedback>
        <xsl:for-each select="./material/*">
	      <xsl:if test="name()='mattext'">
		    <xsl:value-of select="." />
	      </xsl:if>
	      <xsl:if test="name()='matimage'">
		    <xsl:variable name="mimage" select="@uri" />
		    <img alt="" src="{$mimage}"/>
	      </xsl:if>
        </xsl:for-each>
      </incorrectFeedback>
    </xsl:if>
  </xsl:for-each>
  
<xsl:for-each select="//itemfeedback/material/*">
    <allFeedbacks type="list">
	
		<xsl:choose>
			<xsl:when test="name()='mattext'"><xsl:value-of select="../../@ident"/>:::mattext:::<xsl:value-of select="." /></xsl:when>
		    <xsl:when test="name()='matimage'"><xsl:value-of select="../../@ident"/>:::matimage:::<xsl:value-of select="@imagtype" />:::<xsl:value-of select="@uri" /></xsl:when>
	    </xsl:choose>
	</allFeedbacks>
  </xsl:for-each>

  <!-- varequal linkrefid mapping -->
  <xsl:for-each select="//resprocessing/respcondition/displayfeedback">
	<xsl:choose>
	  <xsl:when test="contains(@linkrefid, '_C') or contains(@linkrefid, '_IC')">
	    <varequalLinkrefidMapping type="list"><xsl:value-of select="../conditionvar/varequal"/>:::<xsl:value-of select="@linkrefid"/></varequalLinkrefidMapping>
	  </xsl:when>
	</xsl:choose>
  </xsl:for-each>


</itemData>
</xsl:template>

<xsl:template match="mattext" mode="itemRichText">
  <xsl:apply-templates mode="itemRichText"/>
</xsl:template>

<xsl:template match="*" mode="itemRichText">
  <xsl:copy-of select="." />
</xsl:template>

</xsl:stylesheet>
