<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:t="http://myfaces.apache.org/tomahawk"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <!--
$Id: fullShortAnswer.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $

***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

 -->
  <f:view>
    <f:subview id="preview" rendered="#{searchQuestionBean.userOwns(param.idString) == true}">
      <h:dataTable value="#{searchQuestionBean.getItem(param.idString)}" var="itemValue" width="100%">
        <h:column>
          <h:panelGrid>
            <h:panelGroup rendered="#{param.typeId == '9'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/Matching.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '11'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/FillInNumeric.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '8'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/FillInTheBlank.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '7'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/AudioRecording.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '6'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/FileUpload.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '5'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/ShortAnswer.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '4'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/TrueFalse.jsp"/>
            </h:panelGroup>
            <!-- same as multiple choice single  -->
            <h:panelGroup rendered="#{param.typeId == '3'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/MultipleChoiceSurvey.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '2'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/MultipleChoiceMultipleCorrect.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '1'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/MultipleChoiceSingleCorrect.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '12'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/MultipleChoiceMultipleCorrect.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '14'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/ExtendedMatchingItems.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '13'}">
              <jsp:directive.include file="/jsf/author/search_preview_item/MatrixChoicesSurvey.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '15'}">
              <!-- // CALCULATED_QUESTION  -->
              <jsp:directive.include file="/jsf/author/search_preview_item/CalculatedQuestion.jsp"/>
            </h:panelGroup>
            <h:panelGroup rendered="#{param.typeId == '16'}">
              <!-- // IMAGEMAP_QUESTION  -->
              <jsp:directive.include file="/jsf/author/search_preview_item/ImageMapQuestion.jsp"/>
            </h:panelGroup>
          </h:panelGrid>
          <br/>
          <h:outputLabel value="#{authorMessages.origins}"/>
          <jsp:text><![CDATA[</br>

                ]]></jsp:text>
          <t:dataList value="#{searchQuestionBean.originFull(itemValue.hash)}" var="origin" layout="unorderedList">
            <f:verbatim>
              <jsp:text><![CDATA[<span>]]></jsp:text>
            </f:verbatim>
            <h:outputText value="#{origin}"/>
            <f:verbatim>
              <jsp:text><![CDATA[</span></br>  ]]></jsp:text>
            </f:verbatim>
          </t:dataList>
        </h:column>
      </h:dataTable>
    </f:subview>
  </f:view>
</jsp:root>
