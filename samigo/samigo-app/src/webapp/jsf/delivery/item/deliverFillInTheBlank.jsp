<%-- $Id$
include file for delivering matching questions
should be included in file importing DeliveryMessages
--%>
<!--
* $Id$
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

<div class="sr-only">
  <h:outputFormat value="#{deliveryMessages.fib_sr_explanation}" escape="false">
    <f:param value="#{question.fibArray.size()-1}" />
  </h:outputFormat>
</div>

<samigo:dataLine value="#{question.fibArray}" var="answer" separator=" " first="0" rows="100">
  <h:column>
      <h:outputText id="fib-question-text" styleClass="fib-question-text" value="#{answer.text} " escape="false" />
      <f:verbatim>&nbsp;</f:verbatim>
      <h:panelGroup styleClass="icon-sakai--check feedBackCheck" id="image"
        rendered="#{delivery.feedback eq 'true' &&
                    delivery.feedbackComponent.showCorrectResponse &&
                    answer.isCorrect && answer.hasInput && !delivery.noFeedback=='true'}" >
      </h:panelGroup>
      <h:panelGroup styleClass="icon-sakai--delete feedBackCross" id="ximage"
        rendered="#{delivery.feedback eq 'true' &&
                    delivery.feedbackComponent.showCorrectResponse &&
                    answer.isCorrect != null && !answer.isCorrect && answer.hasInput && !delivery.noFeedback=='true'}">
      </h:panelGroup>
      <h:panelGroup rendered="#{answer.hasInput && delivery.actionString !='gradeAssessment' && delivery.actionString !='reviewAssessment'}">
        <h:outputLabel styleClass="sr-only" for="fib" value="#{deliveryMessages.fib_sr_answer_label_part1} #{question.answerCounter}. #{deliveryMessages.fib_sr_answer_label_part2}" />
        <h:inputText size="20" value="#{answer.response}" onkeypress="return noenter()" id="fib" />
      </h:panelGroup>
      <h:outputText style="text-decoration: underline" rendered="#{delivery.actionString=='gradeAssessment' || delivery.actionString=='reviewAssessment'}"
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
	<h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('../author/markForReviewPopUp.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');" >
		<h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
	</h:outputLink>
</h:panelGroup>

<f:verbatim><br /></f:verbatim>

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGrid rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}" >
    <h:panelGroup>
      <h:outputLabel for="answerKeyMC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.ans_key}: " />
      <h:outputText id="answerKeyMC" value="#{question.key}" escape="false"/>
    </h:panelGroup>
    <h:outputText value=" "/>
  </h:panelGrid>
  
  <h:panelGrid rendered="#{delivery.feedbackComponent.showItemLevel && !delivery.noFeedback=='true' && question.feedbackIsNotEmpty}">
    <h:panelGroup>
      <h:outputLabel for="feedSC" styleClass="answerkeyFeedbackCommentLabel" value="#{commonMessages.feedback}#{deliveryMessages.column} " />
      <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
    </h:panelGroup>
    <h:outputText value=" "/>
  </h:panelGrid>
  
  <h:panelGrid rendered="#{delivery.actionString !='gradeAssessment' && delivery.feedbackComponent.showGraderComment && !delivery.noFeedback=='true' && (question.gradingCommentIsNotEmpty || question.hasItemGradingAttachment)}" columns="1" border="0">
    <h:panelGroup>
      <h:outputLabel for="commentSC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.comment}#{deliveryMessages.column} " />
  	  <h:outputText id="commentSC" value="#{question.gradingComment}" escape="false" rendered="#{question.gradingCommentIsNotEmpty}"/>
    </h:panelGroup>

	<h:panelGroup rendered="#{question.hasItemGradingAttachment}">
      <h:dataTable value="#{question.itemGradingAttachmentList}" var="attach">
        <h:column>
          <%@ include file="/jsf/shared/mimeicon.jsp" %>
        </h:column>
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink value="#{attach.location}" target="new_window">
            <h:outputText value="#{attach.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
        </h:column>
      </h:dataTable>
    </h:panelGroup>
  </h:panelGrid>
</h:panelGroup>
