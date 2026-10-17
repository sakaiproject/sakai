<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!--
include file for preview calculated questions
should be included in file search preview
 -->
  <!--

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
  <h:outputLabel escape="false" value="#{authorMessages.calculated_question}"/>
  <jsp:text><![CDATA[</br>

]]></jsp:text>
  <h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
    <h:column>
      <h:outputText escape="false" value="#{sitemValue.instruction}"/>
      <!-- 1. print out the matching choices  -->
      <br/>
      <br/>
      <h:outputLabel escape="false" value="#{authorMessages.calc_question_var_and_formula_label}"/>
      <jsp:text><![CDATA[</br>

]]></jsp:text>
      <h:dataTable value="#{item.itemTextArraySorted}" var="itemText">
        <h:column>
          <h:dataTable value="#{itemText.answerArraySorted}" var="answer" rendered="#{itemText.sequence==1}">
            <h:column>
              <h:panelGrid columns="2">
                <h:outputText escape="false" value="#{answer.label}. "/>
                <h:outputText escape="false" value="#{answer.text}"/>
              </h:panelGrid>
            </h:column>
          </h:dataTable>
        </h:column>
      </h:dataTable>
      <jsp:text><![CDATA[

</br>

]]></jsp:text>
      <!-- ATTACHMENTS  -->
      <jsp:directive.include file="/jsf/author/search_preview_item/attachment.jsp"/>
      <h:panelGroup rendered="#{item.correctItemFbIsNotEmpty &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2'}">
        <h:outputLabel value="#{authorMessages.correctItemFeedback}:"/>
        <h:outputText value="#{item.correctItemFeedback}" escape="false"/>
        <f:verbatim>
          <br/>
        </f:verbatim>
      </h:panelGroup>
      <h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2'}">
        <h:outputLabel value="#{authorMessages.incorrectItemFeedback}:"/>
        <h:outputText value="#{item.inCorrectItemFeedback}" escape="false"/>
      </h:panelGroup>
      <h:panelGroup rendered="#{item.correctItemFbIsNotEmpty &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'}">
        <h:outputLabel value="#{authorMessages.correctItemFeedback}:"/>
        <h:outputText value="#{item.correctItemFeedback}" escape="false"/>
        <f:verbatim>
          <br/>
        </f:verbatim>
      </h:panelGroup>
      <h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'}">
        <h:outputLabel value="#{authorMessages.incorrectItemFeedback}:"/>
        <h:outputText value="#{item.inCorrectItemFeedback}" escape="false"/>
      </h:panelGroup>
      <jsp:directive.include file="/jsf/author/search_preview_item/tags.jsp"/>
    </h:column>
  </h:dataTable>
</jsp:root>
