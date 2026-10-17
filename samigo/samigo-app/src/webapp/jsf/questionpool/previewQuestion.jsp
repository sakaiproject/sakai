<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk"
    xmlns:corejsf="http://java.sun.com/upload">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[



<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


  ]]></jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{questionPoolMessages.t_previewQuestion}"/>
        </title>
        <!-- stylesheet and script widgets  -->
        <script language="javascript" type="text/JavaScript">
          <jsp:text><![CDATA[
<!--
]]></jsp:text>
          <jsp:directive.include file="/js/samigotree.js"/>
          <jsp:text><![CDATA[
//-->
]]></jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <div class="portletBody">
            <h:form id="previewQuestion">
              <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
              <h3>
                <h:outputText value="#{questionPoolMessages.t_previewQuestion}"/>
              </h3>
              <div class="longtext tier2">
                <h:dataTable id="questions" value="#{questionpool.itemBean}" var="question">
                  <h:column>
                    <h:panelGrid columns="2" border="1" width="100%">
                      <h:panelGroup>
                        <h:outputText styleClass="tier1" value="#{authorMessages.q}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 1}" value="#{authorMessages.multiple_choice_type}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 2}" value="#{authorMessages.multiple_choice_type}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 3}" value="#{authorMessages.multiple_choice_surv}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 4}" value="#{authorMessages.true_false}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 5}" value="#{authorMessages.short_answer_essay}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 6}" value="#{authorMessages.file_upload}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 7}" value="#{authorMessages.audio_recording}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 8}" value="#{authorMessages.fill_in_the_blank}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 9}" value="#{authorMessages.matching}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 11}" value="#{authorMessages.fill_in_numeric}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 12}" value="#{authorMessages.multiple_choice_type}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 13}" value="#{authorMessages.matrix_choices_surv}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 14}" value="#{authorMessages.extended_matching_items}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 15}" value="#{authorMessages.calculated_question}"/>
                        <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 16}" value="#{authorMessages.image_map_question}"/>
                        <h:outputText styleClass="tier1" value="#{question.itemData.score}"/>
                        <h:outputText styleClass="tier1" value="#{authorMessages.points_lower_case}"/>
                      </h:panelGroup>
                    </h:panelGrid>
                    <f:verbatim>
                      <jsp:text><![CDATA[<div class="samigo-question-callout">]]></jsp:text>
                    </f:verbatim>
                    <h:panelGroup rendered="#{question.itemData.typeId == 16}">
                      <jsp:directive.include file="/jsf/author/preview_item/ImageMapQuestion.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 15}">
                      <jsp:directive.include file="/jsf/author/preview_item/CalculatedQuestion.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 14}">
                      <jsp:directive.include file="/jsf/author/preview_item/ExtendedMatchingItems.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 13}">
                      <jsp:directive.include file="/jsf/author/preview_item/MatrixChoicesSurvey.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 9}">
                      <jsp:directive.include file="/jsf/author/preview_item/Matching.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 11}">
                      <jsp:directive.include file="/jsf/author/preview_item/FillInNumeric.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 8}">
                      <jsp:directive.include file="/jsf/author/preview_item/FillInTheBlank.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 7}">
                      <jsp:directive.include file="/jsf/author/preview_item/AudioRecording.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 6}">
                      <jsp:directive.include file="/jsf/author/preview_item/FileUpload.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 5}">
                      <jsp:directive.include file="/jsf/author/preview_item/ShortAnswer.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 4}">
                      <jsp:directive.include file="/jsf/author/preview_item/TrueFalse.jsp"/>
                    </h:panelGroup>
                    <!-- same as multiple choice single  -->
                    <h:panelGroup rendered="#{question.itemData.typeId == 3}">
                      <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceSurvey.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 2}">
                      <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 1}">
                      <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceSingleCorrect.jsp"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{question.itemData.typeId == 12}">
                      <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp"/>
                    </h:panelGroup>
                    <f:verbatim>
                      <jsp:text><![CDATA[</div>]]></jsp:text>
                    </f:verbatim>
                  </h:column>
                </h:dataTable>
                <h:panelGroup styleClass="h3text">
                  <h:outputText value="#{questionPoolMessages.t_historical}"/>
                </h:panelGroup>
                <h:dataTable id="historical" value="#{questionpool.itemBean.get(0).itemData.itemHistorical}" var="historical" styleClass="table table-striped table-bordered">
                  <h:column>
                    <h:outputText value="#{historical.modifiedBy}">
                      <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.UsernameConverter"/>
                    </h:outputText>
                  </h:column>
                  <h:column>
                    <h:outputText value="#{historical.modifiedDate}">
                      <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                    </h:outputText>
                  </h:column>
                </h:dataTable>
              </div>
              <p class="act">
                <h:commandButton id="cancel" value="#{questionPoolMessages.return_action}" action="#{questionpool.editPool}">
                  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
                </h:commandButton>
              </p>
            </h:form>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
