<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id: Matching.jsp 59563 2009-04-02 15:18:05Z arwhyte@umich.edu $
include file for delivering matching questions
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
  <!-- ATTACHMENTS  -->
  <jsp:directive.include file="/jsf/author/preview_item/attachment.jsp"/>
  <h:outputText escape="false" value="#{question.instruction}"/>
  <h:dataTable id="items" value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:panelGrid columns="1">
        <h:outputText escape="false" value="#{itemText.sequence}. #{itemText.text}"/>
      </h:panelGrid>
    </h:column>
    <h:column>
      <h:dataTable id="answers" value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:outputText escape="false" value="&lt;input type='hidden' id='hiddenSerializedCoords_#{partBean.number}_#{question.number}_#{itemText.sequence}' value='#{answer.text}' /&gt;"/>
        </h:column>
      </h:dataTable>
    </h:column>
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
          <h:panelGroup rendered="#{answer.isCorrect &amp;&amp; answer.correctAnswerFbIsNotEmpty &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
            <h:outputLabel value="#{authorMessages.correct}: "/>
            <h:outputText escape="false" value="#{answer.correctAnswerFeedback}"/>
          </h:panelGroup>
          <h:panelGroup rendered="#{answer.isCorrect &amp;&amp; answer.correctAnswerFbIsNotEmpty &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
            <h:outputLabel value="#{authorMessages.correct}: "/>
            <h:outputText escape="false" value="#{answer.correctAnswerFeedback}"/>
          </h:panelGroup>
        </h:column>
      </h:dataTable>
    </h:column>
    <h:column>
      <h:outputText value=""/>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
          <h:panelGroup rendered="#{answer.isCorrect &amp;&amp; answer.incorrectAnswerFbIsNotEmpty &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
            <h:outputLabel value="#{authorMessages.incorrect}: "/>
            <h:outputText escape="false" value="#{answer.inCorrectAnswerFeedback}"/>
          </h:panelGroup>
          <h:panelGroup rendered="#{answer.isCorrect &amp;&amp; answer.incorrectAnswerFbIsNotEmpty &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
            <h:outputLabel value="#{authorMessages.incorrect}: "/>
            <h:outputText escape="false" value="#{answer.inCorrectAnswerFeedback}"/>
          </h:panelGroup>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>
  <!-- 2. print out the matching text (none) -->
  <!-- answer key  -->
  <h:panelGroup>
    <h:outputLabel value="#{authorMessages.answerKey}: "/>
    <jsp:text><![CDATA[
    <div id="imageMapContainer_]]></jsp:text>
    <h:outputText value="#{partBean.number}_#{question.number}"/>
    <jsp:text><![CDATA[" class='authorImageContainer'>
        <img id='img' src=']]></jsp:text>
    <h:outputText value="#{question.itemData.imageMapSrc}"/>
    <jsp:text>' alt='</jsp:text>
    <h:outputText value="#{question.itemData.imageMapAltText}"/>
    <jsp:text><![CDATA['/>
    </div>
    ]]></jsp:text>
    <br/>
  </h:panelGroup>
  <h:panelGroup rendered="#{question.itemData.correctItemFbIsNotEmpty &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.correct}:"/>
    <h:outputText value="#{question.itemData.correctItemFeedback}" escape="false"/>
    <f:verbatim>
      <br/>
    </f:verbatim>
  </h:panelGroup>
  <h:panelGroup rendered="#{question.itemData.incorrectItemFbIsNotEmpty &amp;&amp; author.isEditPendingAssessmentFlow &amp;&amp; assessmentSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.incorrect}:"/>
    <h:outputText value="#{question.itemData.inCorrectItemFeedback}" escape="false"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{question.itemData.correctItemFbIsNotEmpty &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.correct}:"/>
    <h:outputText value="#{question.itemData.correctItemFeedback}" escape="false"/>
    <f:verbatim>
      <br/>
    </f:verbatim>
  </h:panelGroup>
  <h:panelGroup rendered="#{question.itemData.incorrectItemFbIsNotEmpty &amp;&amp; !author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.incorrect}:"/>
    <h:outputText value="#{question.itemData.inCorrectItemFeedback}" escape="false"/>
  </h:panelGroup>
  <jsp:directive.include file="/jsf/author/preview_item/tags.jsp"/>
</jsp:root>
