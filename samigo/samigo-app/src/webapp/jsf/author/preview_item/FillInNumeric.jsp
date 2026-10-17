<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id:  $
include file for delivering fill in the blank questions
should be included in file importing DeliveryMessages
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
  <h:outputText escape="false" value="#{question.itemData.text}"/>
  <!-- ATTACHMENTS  -->
  <jsp:directive.include file="/jsf/author/preview_item/attachment.jsp"/>
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
    </h:column>
  </h:dataTable>
  <h:panelGrid columns="2" styleClass="longtext">
    <h:outputLabel value="#{authorMessages.answerKey}: "/>
    <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
      <h:column>
        <samigo:dataLine value="#{itemText.answerArraySorted}" var="answer" separator=", " first="0" rows="100">
          <h:column>
            <h:outputText escape="false" value="#{answer.text}"/>
          </h:column>
        </samigo:dataLine>
      </h:column>
    </h:dataTable>
    <h:outputLabel rendered="#{answer.text != null &amp;&amp; answer.text ne ''}" value="#{authorMessages.preview_model_short_answer}: "/>
    <h:outputText rendered="#{answer.text != null &amp;&amp; answer.text ne ''}" escape="false" value="#{answer.text}"/>
    <h:outputLabel rendered="#{question.itemData.correctItemFeedback != null &amp;&amp; question.itemData.correctItemFeedback ne '' &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp;  assessmentSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.correctItemFeedback}: "/>
    <h:outputText rendered="#{question.itemData.correctItemFeedback != null &amp;&amp; question.itemData.correctItemFeedback ne '' &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2'}" value="#{question.itemData.correctItemFeedback}" escape="false"/>
    <h:outputLabel rendered="#{question.itemData.inCorrectItemFeedback != null &amp;&amp; question.itemData.inCorrectItemFeedback ne '' &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.incorrectItemFeedback}: "/>
    <h:outputText rendered="#{question.itemData.inCorrectItemFeedback != null &amp;&amp; question.itemData.inCorrectItemFeedback ne '' &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2'}" value="#{question.itemData.inCorrectItemFeedback}" escape="false"/>
    <h:outputLabel rendered="#{question.itemData.correctItemFeedback != null &amp;&amp; question.itemData.correctItemFeedback ne '' &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp;  publishedSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.correctItemFeedback}: "/>
    <h:outputText rendered="#{question.itemData.correctItemFeedback != null &amp;&amp; question.itemData.correctItemFeedback ne '' &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'}" value="#{question.itemData.correctItemFeedback}" escape="false"/>
    <h:outputLabel rendered="#{question.itemData.inCorrectItemFeedback != null &amp;&amp; question.itemData.inCorrectItemFeedback ne '' &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.incorrectItemFeedback}: "/>
    <h:outputText rendered="#{question.itemData.inCorrectItemFeedback != null &amp;&amp; question.itemData.inCorrectItemFeedback ne '' &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'}" value="#{question.itemData.inCorrectItemFeedback}" escape="false"/>
  </h:panelGrid>
  <jsp:directive.include file="/jsf/author/preview_item/tags.jsp"/>
</jsp:root>
