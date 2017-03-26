<%-- $Id$
include file for delivering multiple choice single correct survey questions
should be included in file importing DeliveryMessages
--%>
<!--
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

  <h:outputText escape="false" value="#{question.itemData.text}" />
  <!-- ATTACHMENTS -->
  <%@ include file="/jsf/author/preview_item/attachment.jsp" %>

  <f:verbatim><br/></f:verbatim>
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:graphicImage id="image2"
             alt="#{authorMessages.not_correct}" url="/images/radiounchecked.gif" >
          </h:graphicImage>
          <h:outputText escape="false" value="#{answer.text}" >
          	<f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" /> 
          </h:outputText>
        </h:column>
      </h:dataTable>

<h:panelGroup rendered="#{question.itemData.generalItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.generalItemFeedback}: " />
  <h:outputText value="#{question.itemData.generalItemFeedback}" escape="false" />
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.generalItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.generalItemFeedback}: " />
  <h:outputText value="#{question.itemData.generalItemFeedback}" escape="false" />
</h:panelGroup>

</h:column>
</h:dataTable>

<%@ include file="/jsf/author/preview_item/tags.jsp" %>
