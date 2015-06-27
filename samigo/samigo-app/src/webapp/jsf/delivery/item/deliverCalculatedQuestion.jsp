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
<%@ include file="/jsf/delivery/item/attachment.jsp" %>

<samigo:dataLine value="#{question.finArray}" var="answer"
  separator=" " first="0" rows="100">
  <h:column>
      <h:outputText value="#{answer.text} " escape="false" />
      <h:graphicImage alt="#{deliveryMessages.alt_correct}" id="image"
        rendered="#{delivery.feedback eq 'true' &&
                    delivery.feedbackComponent.showCorrectResponse &&
                    answer.isCorrect && answer.hasInput && !delivery.noFeedback=='true'}" url="/images/checkmark.gif">
      </h:graphicImage>
      <h:graphicImage alt="#{deliveryMessages.alt_incorrect}" id="image2"
        rendered="#{delivery.feedback eq 'true' &&
                    delivery.feedbackComponent.showCorrectResponse &&
                    answer.isCorrect != null && !answer.isCorrect && answer.hasInput && !delivery.noFeedback=='true'}" url="/images/crossmark.gif">
      </h:graphicImage>      
	  <h:inputText size="10" rendered="#{answer.hasInput 
		&& delivery.actionString !='gradeAssessment' 
		&& delivery.actionString !='reviewAssessment'}"
        disabled="#{delivery.actionString=='previewAssessment'}" value="#{answer.response}" onkeypress="return noenter()"/>
      <h:outputText style="text-decoration: underline" 
		rendered="#{delivery.actionString=='gradeAssessment' 
			|| delivery.actionString=='reviewAssessment'}"
             value="#{answer.response}"/>

  </h:column>
</samigo:dataLine>

<f:verbatim><br /></f:verbatim>

<h:panelGroup rendered="#{(delivery.actionString=='previewAssessment'
                || delivery.actionString=='takeAssessment' 
                || delivery.actionString=='takeAssessmentViaUrl')
             && delivery.navigation ne '1' && delivery.displayMardForReview }">
<h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review" />
	<h:outputLabel for="mark_for_review" value="#{deliveryMessages.mark}" />
	<h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('../author/markForReviewPopUp.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');">
		<h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
	</h:outputLink>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.feedback eq 'true' || delivery.actionString=='previewAssessment'}">
  <f:verbatim><br /></f:verbatim>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}" >
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="answerKeyMC" value="#{deliveryMessages.ans_key}: " />
      <f:verbatim></b></f:verbatim>
    <h:outputText id="answerKeyMC"
       value="#{question.key}" escape="false"/>

  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && !delivery.noFeedback=='true' && question.feedbackIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{commonMessages.feedback}#{deliveryMessages.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.actionString !='gradeAssessment' && delivery.feedbackComponent.showGraderComment && !delivery.noFeedback=='true' && question.gradingCommentIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{deliveryMessages.comment}#{deliveryMessages.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}"
      escape="false" />
  </h:panelGroup>
</h:panelGroup>
