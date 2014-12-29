<?xml version="1.0" encoding="UTF-8" ?>
<!--
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p>Description: QTI Persistence XML to XML Transform for Import</p>
 * @author <a href="mailto:esmiley@stanford.edu">Ed Smiley</a>
 * @version $Id: extractItem.xsl,v 1.20 2005/05/13 22:33:35 esmiley.stanford.edu Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
 doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

<xsl:template match="/">

<itemData>
  <ident><xsl:value-of select="//item/@ident" /></ident>
  <label><xsl:value-of select="//item/@label" /></label>
  <title><xsl:value-of select="//item/@title" /></title>
  <duration><xsl:value-of select="//item/duration" /></duration>
  <instruction></instruction>
  <createdBy></createdBy>
  <createdDate></createdDate>
  <lastModifiedBy></lastModifiedBy>
  <lastModifiedDate></lastModifiedDate>
  <presentationLabel><xsl:value-of select="//item/presentation/@label" /></presentationLabel>
  <score>
    <xsl:value-of
      select="//resprocessing/outcomes/decvar[@varname='SCORE']/@maxvalue"/>
    <!-- Respondus multiple correct answer -->
    <xsl:value-of
      select="//resprocessing/outcomes/decvar[@varname='que_score']/@maxvalue"/>
    <!-- Respondus single correct answer -->
    <xsl:for-each select="//respcondition">
      <xsl:if test="setvar/@varname='que_score' and setvar/@action='Set'">
      <xsl:value-of select="setvar"/><!--  if not single adds innocuous '0' -->
      </xsl:if>
    </xsl:for-each>
  </score>
  <discount>
    <xsl:value-of
     select="//resprocessing/outcomes/decvar[@varname='SCORE']/@minvalue"/>
    <!-- Respondus multiple correct answer -->
    <xsl:value-of
     select="//resprocessing/outcomes/decvar[@varname='que_score']/@minvalue"/>
    <!-- Respondus single correct answer -->
  </discount>
  <hint></hint>
  <!-- <hasRationale></hasRationale>rshastri :SAK-1824-->
  <status></status>
  <!-- item text -->
 <itemText type="list">
  <xsl:for-each select="//presentation//material/mattext">
   <xsl:if test="position()=1">
      <xsl:apply-templates mode="itemRichText" />
   </xsl:if>
  </xsl:for-each>
 </itemText>
  <!-- FIB item text, also used for numerical response questions (FIN) -->
  <xsl:for-each select="//presentation//material/mattext">
    <itemFibText type="list">
      <xsl:apply-templates mode="itemRichText" />
    </itemFibText>
  </xsl:for-each>
  <!-- Calculated Questions - variables -->
    <xsl:for-each select="//variables/variable/name">
        <variableNames type="list"><xsl:value-of select="."/></variableNames>
    </xsl:for-each>
    <xsl:for-each select="//variables/variable/min">
        <variableMins type="list"><xsl:value-of select="."/></variableMins>
    </xsl:for-each>
    <xsl:for-each select="//variables/variable/max">
        <variableMaxs type="list"><xsl:value-of select="."/></variableMaxs>
    </xsl:for-each>
    <xsl:for-each select="//variables/variable/decimalPlaces">
        <variableDecimalPlaces type="list"><xsl:value-of select="."/></variableDecimalPlaces>
    </xsl:for-each>
  <!-- Calculated Questions - formulas -->
    <xsl:for-each select="//formulas/formula/name">
        <formulaNames type="list"><xsl:value-of select="."/></formulaNames>
    </xsl:for-each>
    <xsl:for-each select="//formulas/formula/formula">
        <formulaTexts type="list"><xsl:value-of select="."/></formulaTexts>
    </xsl:for-each>
    <xsl:for-each select="//formulas/formula/tolerance">
        <formulaTolerances type="list"><xsl:value-of select="."/></formulaTolerances>
    </xsl:for-each>
    <xsl:for-each select="//formulas/formula/decimalPlaces">
        <formulaDecimalPlaces type="list"><xsl:value-of select="."/></formulaDecimalPlaces>
    </xsl:for-each>
    
  <!-- MATCHING item text, answers-->
  <xsl:for-each select="//presentation//response_grp//material/mattext">
    <xsl:choose>
      <xsl:when test="../../@match_group">
        <xsl:variable name="src-ident"><xsl:value-of select="../../@ident"/></xsl:variable>
        <itemMatchSourceText type="list">
        <xsl:choose>
          <xsl:when test="./*">
            <xsl:copy-of select="./*"/>
           </xsl:when>
           <xsl:when test="string-length(.)">
             <xsl:value-of select="."/>
           </xsl:when>
        </xsl:choose>
        </itemMatchSourceText>
        <xsl:for-each select="//respcondition/conditionvar/varequal">
          <xsl:variable name="curr-ident"><xsl:value-of select="." /></xsl:variable>
           <xsl:if test="$src-ident=$curr-ident">
             <itemMatchIndex type="list"><xsl:value-of select="../../displayfeedback/@linkrefid" /></itemMatchIndex>
           </xsl:if>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <itemMatchTargetText type="list">
        <xsl:choose>
          <xsl:when test="./*">
            <xsl:copy-of select="./*"/>
           </xsl:when>
           <xsl:when test="string-length(.)">
             <xsl:value-of select="."/>
           </xsl:when>
        </xsl:choose>
        </itemMatchTargetText>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:for-each>
  <!-- label(s) for correct answer(s), if any -->
  <xsl:for-each select="//respcondition">
    <xsl:if test="displayfeedback/@linkrefid='Correct'">
      <itemAnswerCorrectLabel type="list"><xsl:value-of select="conditionvar/varequal"/></itemAnswerCorrectLabel>
    </xsl:if>
  </xsl:for-each>
  <!-- alternate for Respondus questions -->
  <xsl:for-each select="//respcondition">
    <xsl:if test="setvar/@varname='que_score' and setvar/@action='Set'">
      <itemAnswerCorrectLabel type="list"><xsl:value-of select="conditionvar/varequal"/></itemAnswerCorrectLabel>
    </xsl:if>
  </xsl:for-each>
  <!-- matching feedback -->
  <xsl:for-each select="//respcondition">
    <xsl:if test="displayfeedback/@linkrefid='CorrectMatch'">
      <itemMatchCorrectFeedback type="list"><xsl:value-of select="displayfeedback"/></itemMatchCorrectFeedback>
    </xsl:if>
    <xsl:if test="displayfeedback/@linkrefid='InCorrectMatch'">
      <itemMatchIncorrectFeedback type="list"><xsl:value-of select="displayfeedback"/></itemMatchIncorrectFeedback>
    </xsl:if>
  </xsl:for-each>

  <!-- answer feedback -->
  <xsl:for-each select="//respcondition/displayfeedback">
   <xsl:if test="@linkrefid='AnswerFeedback'">
     <itemAnswerFeedback type="list"><xsl:value-of select="."/></itemAnswerFeedback>
   </xsl:if>
  </xsl:for-each>

  <!-- answers -->
  <xsl:for-each select="//presentation//response_lid/render_choice/response_label/material/mattext" >
      <itemAnswer type="list"><xsl:apply-templates mode="itemRichText" /></itemAnswer>
  </xsl:for-each>
  <xsl:for-each select="//respcondition/conditionvar/*[name()='or']/varequal">
	<xsl:choose>
    	<xsl:when test="./*">
      		<itemFibAnswer type="list"><xsl:copy-of select="./*"/></itemFibAnswer>
    	</xsl:when>
    	<xsl:when test="string-length(.)">
     		<itemFibAnswer type="list"><xsl:value-of select="."/></itemFibAnswer>
    	</xsl:when>
  	</xsl:choose>
  </xsl:for-each>
  <!-- feedback -->

  <xsl:for-each select="//itemfeedback">
    <xsl:choose>
       <xsl:when test="@ident = 'InCorrect'">
        <xsl:for-each select="flow_mat/material/mattext">
        <xsl:choose>
        <xsl:when test="./*">
          <incorrectItemFeedback><xsl:copy-of select="./*"/></incorrectItemFeedback>
        </xsl:when>
        <xsl:when test="string-length(.)">
         <incorrectItemFeedback><xsl:value-of select="."/></incorrectItemFeedback>
        </xsl:when>
        </xsl:choose>
        </xsl:for-each>
     </xsl:when>
     <xsl:when test="@ident = 'Correct'">
        <xsl:for-each select="flow_mat/material/mattext">
        <xsl:choose>
        <xsl:when test="./*">
          <correctItemFeedback><xsl:copy-of select="./*"/></correctItemFeedback>
        </xsl:when>
        <xsl:when test="string-length(.)">
         <correctItemFeedback><xsl:value-of select="."/></correctItemFeedback>
        </xsl:when>
        </xsl:choose>
        </xsl:for-each>
     </xsl:when>
     <xsl:when test="@ident = 'AllCorrect'">
        <xsl:for-each select="flow_mat/material/mattext">
        <xsl:choose>
        <xsl:when test="./*">
          <correctItemFeedback><xsl:copy-of select="./*"/></correctItemFeedback>
        </xsl:when>
        <xsl:when test="string-length(.)">
         <correctItemFeedback><xsl:value-of select="."/></correctItemFeedback>
        </xsl:when>
        </xsl:choose>
        </xsl:for-each>
     </xsl:when>
     <xsl:when test="@ident = //item/@ident">
      <xsl:for-each select="flow_mat/material/mattext">
      <xsl:choose>
        <xsl:when test="./*">
          <generalItemFeedback><xsl:copy-of select="./*"/></generalItemFeedback>
        </xsl:when>
        <xsl:when test="string-length(.)">
         <generalItemFeedback><xsl:value-of select="."/></generalItemFeedback>
        </xsl:when>
      </xsl:choose>
      </xsl:for-each>
     </xsl:when>
     <xsl:otherwise>
      <xsl:for-each select="flow_mat/material/mattext">
      <xsl:choose>
        <xsl:when test="./*">
          <itemFeedback type="list"><xsl:copy-of select="./*"/></itemFeedback>
        </xsl:when>
        <xsl:when test="string-length(.)">
         <itemFeedback type="list"><xsl:value-of select="."/></itemFeedback>
        </xsl:when>
      </xsl:choose>
      </xsl:for-each>
     </xsl:otherwise>
    </xsl:choose>
  </xsl:for-each>

  <!-- media id for file upload and recording types, otherwise blank -->
  <!--  TODO debug -->
  <itemMedia type="list">
<!--  <xsl:if test="$item-type='File Upload' or $item-type='Audio Recording'"> -->
    <xsl:call-template name="extract-media-id">
      <xsl:with-param name="raw-answer">
        <!-- <xsl:value-of select="$item-answer-text"/> -->
      </xsl:with-param>
    </xsl:call-template>
<!--  </xsl:if> -->
  </itemMedia>

  <xsl:for-each select="//itemmetadata/qtimetadata/qtimetadatafield">
    <xsl:variable name="metadata">meta</xsl:variable>
    <xsl:element name="metadata">
     <xsl:attribute name="type">list</xsl:attribute>
     <xsl:value-of select="fieldlabel"/>|<xsl:value-of select="fieldentry"/>
    </xsl:element>
  </xsl:for-each>
  <!-- Respondus -->
  <xsl:for-each select="//itemmetadata/qmd_itemtype">
    <xsl:element name="metadata">
     <xsl:attribute name="type">list</xsl:attribute>qmd_itemtype|<xsl:value-of select="."/>
    </xsl:element>
  </xsl:for-each>
   <!-- Respondus //rshastri :SAK-1824 -->
  <xsl:for-each select="//itemmetadata/hasRationale">
    <xsl:element name="metadata">
     <xsl:attribute name="type">list</xsl:attribute>hasRationale|<xsl:value-of select="."/>
    </xsl:element>
  </xsl:for-each>
 <!-- //end rshastri :SAK-1824-->
  <!-- if other methods of determining type don't work, attempt to determine from structure-->
  <!-- NOT guaranteed to be accurate, this is a fallback if none in metadata, title -->
  <!-- DEPENDENCY WARNING: syncs with type strings in AuthoringConstantStrings.java -->
  <xsl:for-each select="//item">
    <xsl:variable name="labels"><xsl:for-each select=".//response_label"><xsl:value-of select="@ident"/></xsl:for-each></xsl:variable>
    <itemIntrospect>
      <xsl:choose>
        <xsl:when test=".//render_choice and .//response_grp and .//response_labl[@match_max>1]">Survey Matrix</xsl:when>
        <xsl:when test=".//variables and ..//formulas">Calculated Question</xsl:when>
        <xsl:when test=".//render_choice and .//response_grp">Matching</xsl:when>
        <xsl:when test=".//resprocessing and .//render_fib">Fill In the Blank</xsl:when>
        <xsl:when test=".//resprocessing and .//render_fin">Numeric Response</xsl:when>
        <!-- this is lame, but true false acts like a 2 answer MCSC with answers True, False -->
        <xsl:when test=".//render_choice and $labels='TF'">True False</xsl:when>
        <xsl:when test=".//render_choice and @title='Multiple Correct'">Multiple Correct Answer</xsl:when>
        <xsl:when test=".//render_choice and @title='Multiple Correct Single Selection'">Multiple Correct Single Selection</xsl:when>
        <xsl:when test=".//render_choice and @title='Multiple Choice'">Multiple Choice</xsl:when>
        <xsl:when test=".//render_choice and @title='Matrix Choices Survey'">Matrix Choice</xsl:when>
        <xsl:otherwise>Short Answers/Essay</xsl:otherwise>
      </xsl:choose>
    </itemIntrospect>
  </xsl:for-each>
  <!-- for partial credit in  Multiple choice we need to import values for other answers as well in addition to the correct -mustansar -->
  <xsl:for-each select="//respcondition">
         <xsl:if test="//itemmetadata/qtimetadata/qtimetadatafield/fieldentry='Multiple Choice'"> 
      <answerScore type="list"><xsl:value-of select="setvar"/></answerScore>
      </xsl:if>
  </xsl:for-each>
</itemData>
</xsl:template>

<!-- these templates match a rich text mattext and render the whole tree -->
<xsl:template match="mattext" mode="itemRichText">
  <xsl:apply-templates mode="itemRichText"/>
</xsl:template>

<xsl:template match="*" mode="itemRichText">
  <xsl:copy-of select="." />
</xsl:template>


<!-- this template exists to strip the "id=" parameter off of a file upload type
the current contract is that this type will have an answer text value that uses
an id URL with the id of the media data record, if this is no longer the case,
this template will need to be revised.
-->

<xsl:template name="extract-media-id">
  <xsl:param name="raw-answer"/>
    <xsl:value-of
     select="substring-before(substring-after($raw-answer, '?id='), '&quot;')"/>
</xsl:template>

</xsl:stylesheet>
