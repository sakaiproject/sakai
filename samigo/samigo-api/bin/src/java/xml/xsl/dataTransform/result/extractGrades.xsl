<?xml version="1.0" encoding="UTF-8" ?>
<!--
 * <p>Title: NavigoProject.org</p>
 * <p>Description: QTI Persistence XML to XML Transform</p>
 * <p>Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.</p>
 * @author <a href="mailto:esmiley@stanford.edu">Ed Smiley</a>
 * @version $Id: extractGrades.xsl,v 1.1.1.1 2004/07/28 21:32:09 rgollub.stanford.edu Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
 doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>

<xsl:template match="/">
  <resultData>
    <!-- realization -->
    <assessmentResultId><xsl:value-of select="//qti_result_report/result/assessment_result/@ident_ref"/></assessmentResultId>
    <!-- QTI date and time usage example: 2001-08-07T14:00:00 -->
    <xsl:variable name="date-time"><xsl:value-of select="//xmlDeliveryActionForm/currentTime"/></xsl:variable>

    <!-- student -->
    <student><xsl:value-of select="//qti_result_report/result/context/name"/></student>
    <studentId><xsl:value-of select="//qti_result_report/result/context/generic_identifier/identifier_string"/></studentId>

    <!-- assessment score -->
    <score><xsl:value-of select="sum(//qti_result_report/result//item_result/outcomes/score/score_value)"/></score>
    <maxScore><xsl:value-of select="sum(//qti_result_report/result//item_result/outcomes/score/score_max)"/></maxScore>

    <!-- item and item scores -->
    <xsl:apply-templates select="//qti_result_report/result/assessment_result//item_result"/>

  </resultData>
</xsl:template>

<!--
set these item elements to type "list",  values can be scalar (default) or list
-->
<xsl:template match="item_result">
    <xsl:variable name="item-result-id"><xsl:value-of select="@ident_ref"/></xsl:variable>
    <xsl:variable name="item-score"><xsl:value-of select="outcomes/score/score_value"/></xsl:variable>
    <xsl:variable name="item-max-score"><xsl:value-of select="outcomes/score/score_max"/></xsl:variable>
    <xsl:variable name="response-label"><xsl:value-of select="response/response_value"/></xsl:variable>

    <!-- get the grading information -->
    <itemId type="list"><xsl:value-of select="$item-result-id"/></itemId>
    <itemScore type="list"><xsl:value-of select="$item-score"/></itemScore>
    <itemMaxScore type="list"><xsl:value-of select="$item-max-score"/></itemMaxScore>

    <!-- get the question and answer information -->
    <xsl:variable name="item-answer-text">
      <xsl:for-each select="//assessment/section/item">
        <xsl:variable name="item-id"><xsl:value-of select="@ident"/></xsl:variable>
        <xsl:if test="$item-id = $item-result-id">
          <xsl:for-each select=".//response_label">
            <xsl:variable
              name="answer"><xsl:value-of select="material/mattext/comment()"/></xsl:variable>
            <xsl:variable name="identity"><xsl:value-of select="@ident"/></xsl:variable>
            <xsl:for-each select="//qti_result_report/result//item_result">
              <xsl:if test="$identity = response/response_value">
               <xsl:if test="@ident_ref = $item-id">
                <xsl:value-of select="$answer"/>|
               </xsl:if>
              </xsl:if>
            </xsl:for-each>
          </xsl:for-each>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <itemAnswerText type="list"><xsl:value-of select="$item-answer-text"/></itemAnswerText>

    <xsl:for-each select="//assessment/section/item">
      <xsl:variable name="item-id"><xsl:value-of select="@ident"/></xsl:variable>
      <xsl:if test="$item-id = $item-result-id">

      <!-- find out what type of item we have -->
      <xsl:variable name="item-type">
      <xsl:for-each select="itemmetadata/qtimetadata/qtimetadatafield">
        <xsl:if test="fieldlabel = 'qmd_itemtype'">
          <xsl:value-of select="fieldentry"/>
          </xsl:if>
      </xsl:for-each>
      </xsl:variable>
      <itemType type="list"><xsl:value-of select="$item-type"/></itemType>

      <!-- media id for file upload and recording types, otherwise blank -->
      <itemMedia type="list">
      <xsl:if test="$item-type='File Upload' or $item-type='Audio Recording'">
        <xsl:call-template name="extract-media-id">
          <xsl:with-param name="raw-answer">
            <xsl:value-of select="$item-answer-text"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:if>
      </itemMedia>

      <!-- now determine the correct answer label -->
      <itemCorrect type="list">
      <xsl:for-each select="resprocessing/respcondition">
        <xsl:if test="displayfeedback/@linkrefid='Correct'">
          <xsl:variable name="correct-answer-label"><xsl:value-of select="conditionvar/varequal"/>
          </xsl:variable>
          <!-- now label if it is correct, we consider it correct if it matches,
               note that this is essentially not very helpful on a multi-answer
               question as getting only one of the answers is neither fully
               correct, nor, incorrect, this is useful for simple questions.-->
          <xsl:if test="$correct-answer-label = $response-label">True</xsl:if>
          <xsl:if test="$correct-answer-label != $response-label">False</xsl:if>
        </xsl:if>
      </xsl:for-each>
      </itemCorrect>

      <!-- don't need this but might be nice in the future -->
      <!-- we just get the first answer for now-->
      <itemCorrectAnswerText type="list">
      <xsl:for-each select="resprocessing/respcondition">
        <xsl:if test="displayfeedback/@linkrefid='Correct'">
         <xsl:for-each
          select="../../presentation/flow/response_lid/render_choice/response_label">
          <xsl:variable name="correct"><xsl:value-of
            select="@ident"/></xsl:variable>
          <xsl:if test="$correct = $response-label">
            <xsl:value-of select="material/mattext/comment()"/>
          </xsl:if>
         </xsl:for-each>
        </xsl:if>
      </xsl:for-each>
      </itemCorrectAnswerText>

      <!-- don't need this but might be nice in the future -->
      <itemQuestionText type="list"><xsl:value-of select=".//mattext/comment()"
         disable-output-escaping="yes"/></itemQuestionText>
     </xsl:if>
    </xsl:for-each>
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
