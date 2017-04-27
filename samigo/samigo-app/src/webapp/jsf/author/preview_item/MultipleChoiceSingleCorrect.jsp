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

  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText" width="100%">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer" width="100%">
        <h:column>
         <h:panelGroup rendered="#{answer.text !=null && answer.text!=''}">
          <h:graphicImage id="image1" rendered="#{answer.isCorrect}" alt="#{authorMessages.correct}" url="/images/radiochecked.gif"/>
         
          <h:graphicImage id="image2" rendered="#{!answer.isCorrect}" alt="#{authorMessages.not_correct}" url="/images/radiounchecked.gif"/>
        
          <h:outputText escape="false" value="#{answer.label}. " />
          <h:outputText escape="false" value="#{answer.text}" styleClass="mcAnswerText"/>
</h:panelGroup>
</h:column><h:column>
 
          <h:panelGroup rendered="#{answer.text ne null && answer.text ne '' && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1' && answer.generalAnswerFbIsNotEmpty}">
          <h:outputLabel value="          #{commonMessages.feedback}:  "/>
        
          <h:outputText escape="false" value="#{answer.generalAnswerFeedback}" />
		  </h:panelGroup>
	          <h:panelGroup rendered="#{answer.text ne null && answer.text ne '' && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1' && answer.generalAnswerFbIsNotEmpty}">
          <h:outputLabel value="          #{commonMessages.feedback}:  "/>
        
          <h:outputText escape="false" value="#{answer.generalAnswerFeedback}" />
	      </h:panelGroup>

        </h:column>
      </h:dataTable>
  </h:column>
  </h:dataTable>


<h:panelGroup>
  <h:outputLabel value="#{authorMessages.answerKey}: "/>
  <h:outputText escape="false" value="#{question.itemData.answerKey}" />
  <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.correctItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
  <h:outputText  value="#{question.itemData.correctItemFeedback}" escape="false" />
 <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.incorrectItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
  <h:outputText value="#{question.itemData.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<h:panelGroup rendered="#{question.itemData.correctItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
  <h:outputText  value="#{question.itemData.correctItemFeedback}" escape="false" />
 <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.incorrectItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
  <h:outputText value="#{question.itemData.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<%@ include file="/jsf/author/preview_item/tags.jsp" %>



