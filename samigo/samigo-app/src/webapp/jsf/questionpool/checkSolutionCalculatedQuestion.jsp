<%-- $Id: deliverCalculatedQuestion.jsp 59565 2009-04-02 15:30:35Z arwhyte@umich.edu $
include file for delivering calculated questions
should be included in file importing DeliveryMessages
--%>
<!--
* $Id: deliverCalculatedQuestion.jsp 59565 2009-04-02 15:30:35Z arwhyte@umich.edu $
<%--
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
--%>
-->
  <!-- ATTACHMENTS -->
<%@ include file="/jsf/author/preview_item/attachment.jsp" %>

<div class="sr-only">
  <h:outputFormat value="#{deliveryMessages.calcq_sr_explanation}" escape="false">
    <f:param value="#{question.finArray.size()-1}" />
  </h:outputFormat>
</div>

<samigo:dataLine value="#{question.finArray}" var="answer" separator=" " first="0" rows="100">
  <h:column>
      <h:outputText id="calcq-question-text" styleClass="calcq-question-text" value="#{answer.text} " escape="false" >
        <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.SecureContentWrapper" />
      </h:outputText>
      <f:verbatim>&nbsp;</f:verbatim>
      <h:panelGroup styleClass="icon-sakai--check feedBackCheck" id="image" rendered="#{answer.isCorrect}">
      </h:panelGroup>
      <h:outputText style="text-decoration: underline" value="#{answer.response}"/>
  </h:column>
</samigo:dataLine>

<f:verbatim><br /></f:verbatim>

<h:panelGroup>
  <f:verbatim><br /></f:verbatim>
  <h:panelGroup>
    <h:outputLabel for="answerKeyMC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.ans_key}: " />
    <h:outputText id="answerKeyMC" value="#{question.key}" escape="false"/>
  </h:panelGroup>
  <h:panelGroup>
    <f:verbatim><br /></f:verbatim>
    <h:panelGroup styleClass="icon-sakai--check feedBackCheck" id="imageCheck"></h:panelGroup>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{authorMessages.correctItemFeedback}#{deliveryMessages.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.itemData.correctItemFeedbackValue}" escape="false" />
    <f:verbatim><br /></f:verbatim>
    <h:panelGroup styleClass="icon-sakai--delete feedBackCross" id="imageCross"></h:panelGroup>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSCI" value="#{authorMessages.incorrectItemFeedback}#{deliveryMessages.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSCI" value="#{question.itemData.inCorrectItemFeedbackValue}" escape="false" />
  </h:panelGroup>
</h:panelGroup>
