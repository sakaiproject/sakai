<%-- $Id$
include file for delivering multiple choice single correct survey questions
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
  <h:outputText value="#{question.text}"  escape="false"/>
  <!-- ATTACHMENTS -->
  <%@ include file="/jsf/delivery/item/attachment.jsp" %>

  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
   <h:column rendered="#{delivery.feedback eq 'true' &&
           delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}">
      <h:dataTable value="#{question.answers}" var="answer">
        <h:column>
          <h:panelGroup id="image" rendered="#{answer.description eq 'true' && question.responseId eq answer.value}"
            styleClass="icon-sakai--check feedBackCheck" >
          </h:panelGroup>
          <h:panelGroup id="image2" rendered="#{answer.description ne 'true' && question.responseId eq answer.value}"
            styleClass="icon-sakai--delete feedBackCross">
          </h:panelGroup>
       </h:column>
     </h:dataTable>
   </h:column>
   <h:column>
      <h:selectOneRadio id="question" value="#{question.responseId}" layout="pagedirection" 
        disabled="#{delivery.actionString=='reviewAssessment'
                 || delivery.actionString=='gradeAssessment'}" >
        <f:selectItems value="#{question.answers}" />
      </h:selectOneRadio>
   </h:column>
  </h:dataTable>

  <h:panelGroup rendered="#{question.itemData.hasRationale}">
    <f:verbatim><br /></f:verbatim>
    <h:outputLabel for="rationale" value="#{deliveryMessages.rationale}" />
    <f:verbatim><br /></f:verbatim>
    <h:inputTextarea id="rationale" value="#{question.rationale}" rows="5" cols="40" 
        rendered="#{delivery.actionString!='reviewAssessment' 
                 && delivery.actionString!='gradeAssessment'}" />
    <h:outputText id="rationale2" value="#{question.rationaleForDisplay}" 
        rendered="#{delivery.actionString=='reviewAssessment'
                 || delivery.actionString=='gradeAssessment'}" escape="false"/>
  </h:panelGroup>

<h:panelGroup>
	<h:commandLink id="cmdclean" value="#{deliveryMessages.reset_selection}" action="#{delivery.cleanRadioButton}" onclick="saveTime(); serializeImagePoints();"
		rendered="#{(delivery.actionString=='previewAssessment' || delivery.actionString=='previewAssessmentPublished'
                || delivery.actionString=='takeAssessment' 
                || delivery.actionString=='takeAssessmentViaUrl')}">
		<f:param name="radioId" value="#{question.itemData.itemId}" />
	</h:commandLink> 
</h:panelGroup>

<f:verbatim><br /></f:verbatim>
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

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGrid rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}" >
    <h:panelGroup>
      <h:outputLabel for="answerKeyMC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.ans_key}#{deliveryMessages.column} " />
      <h:outputText id="answerKeyMC" escape="false" value="#{question.key}"/>
    </h:panelGroup>
    <h:outputText value=" "/>
  </h:panelGrid>

  <h:panelGrid rendered="#{delivery.feedbackComponent.showItemLevel && !delivery.noFeedback=='true' && question.feedbackIsNotEmpty}">
    <h:panelGroup>
    <h:outputLabel for="feedSC" styleClass="answerkeyFeedbackCommentLabel" value="#{commonMessages.feedback}#{deliveryMessages.column} " />
      <f:verbatim></b></f:verbatim>
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
