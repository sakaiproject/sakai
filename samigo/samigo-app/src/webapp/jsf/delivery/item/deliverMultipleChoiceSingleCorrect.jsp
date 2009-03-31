<%-- $Id$
include file for delivering multiple choice questions
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
<h:outputText value="<script>" escape="false" />
<h:outputText value="var selectedRadioButton#{question.itemData.itemId};" escape="false" />
<h:outputText value="function uncheckRadioButtons#{question.itemData.itemId}(radioButton) {" escape="false" />
<h:outputText value="if (selectedRadioButton#{question.itemData.itemId} != null) {" escape="false" />
<h:outputText value="selectedRadioButton#{question.itemData.itemId}.checked = false;" escape="false" />
<h:outputText value="}" escape="false" />
<h:outputText value="selectedRadioButton#{question.itemData.itemId} = radioButton;" escape="false" />
<h:outputText value="selectedRadioButton#{question.itemData.itemId}.checked = true;" escape="false" />
<h:outputText value="}" escape="false" />
<h:outputText value="</script>" escape="false" />

  <h:outputText value="#{question.text}"  escape="false"/>
  <!-- ATTACHMENTS -->
  <%@ include file="/jsf/delivery/item/attachment.jsp" %>

  <h:dataTable value="#{question.selectionArray}" var="selection">
    <h:column rendered="#{delivery.feedback eq 'true' &&
       delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}">
      <h:graphicImage id="image"
        rendered="#{selection.answer.isCorrect eq 'true' && selection.response}"
        alt="#{deliveryMessages.alt_correct}" url="/images/checkmark.gif" >
      </h:graphicImage>
      <h:graphicImage id="image2"
        rendered="#{selection.answer.isCorrect ne 'true' && selection.response}"
        width="16" height="16"
        alt="#{deliveryMessages.alt_incorrect}" url="/images/delivery/spacer.gif">
      </h:graphicImage>
    </h:column>
    <h:column>

     <h:selectOneRadio onfocus="if (this.defaultChecked) { uncheckRadioButtons#{question.itemData.itemId}(this) };" onclick="uncheckRadioButtons#{question.itemData.itemId}(this);" onkeypress="uncheckRadioButtons#{question.itemData.itemId}(this);" required="false" 
        disabled="#{delivery.actionString=='reviewAssessment'
                 || delivery.actionString=='gradeAssessment'}" 
       value="#{question.responseId}" layout="pageLayout">
       <f:selectItem itemValue="#{selection.answerId}" />
     </h:selectOneRadio>

    </h:column>
    <h:column>
     <h:outputText value=" #{selection.answer.label}" escape="false" />
     <h:outputText value="." rendered="#{selection.answer.label ne ''}" />
     <h:outputText value=" #{selection.answer.text}" escape="false" />
    </h:column>
    <h:column>
      <h:panelGroup rendered="#{delivery.feedback eq 'true' &&
       delivery.feedbackComponent.showSelectionLevel && question.itemData.typeId != 3 &&
	   selection.answer.generalAnswerFeedback != 'null' && selection.answer.generalAnswerFeedback != null && selection.answer.generalAnswerFeedback != '' && selection.response}" >
   	   <!-- The above != 'null' is for SAK-5475. Once it gets fixed, we can remove this condition -->
       <f:verbatim><br /></f:verbatim>
       <h:outputText value="#{deliveryMessages.feedback}#{deliveryMessages.column} " />
       <h:outputText value="#{selection.answer.generalAnswerFeedback}" escape="false" />
      </h:panelGroup>
    </h:column>
  </h:dataTable>

  <h:panelGroup
    rendered="#{question.itemData.hasRationale && question.itemData.typeId != 3}" >
    <f:verbatim><br /></f:verbatim>
    <h:outputLabel for="rationale" value="#{deliveryMessages.rationale}" />
    <f:verbatim><br /></f:verbatim>
    <h:inputTextarea id="rationale" value="#{question.rationale}" rows="5" cols="40" 
        rendered="#{delivery.actionString!='reviewAssessment' 
                 && delivery.actionString!='gradeAssessment'}" />
    <h:outputText id="rationale2" value="#{question.rationale}" 
        rendered="#{delivery.actionString=='reviewAssessment'
                 || delivery.actionString=='gradeAssessment'}"/>
  </h:panelGroup>

<h:commandLink id="cmdclean" value="#{deliveryMessages.reset_selection}" action="#{delivery.cleanRadioButton}" 
	rendered="#{(delivery.actionString=='previewAssessment' || delivery.actionString=='previewAssessmentPublished'
                || delivery.actionString=='takeAssessment'
                || delivery.actionString=='takeAssessmentViaUrl')}">
	<f:param name="radioId" value="#{question.itemData.itemId}" />
</h:commandLink>

<f:verbatim><br /></f:verbatim>
<f:verbatim><br /></f:verbatim>

<h:panelGroup rendered="#{(delivery.actionString=='previewAssessment'
                || delivery.actionString=='takeAssessment' 
                || delivery.actionString=='takeAssessmentViaUrl')
             && delivery.navigation ne '1' && delivery.displayMardForReview }">
<h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review" />
	<h:outputLabel for="mark_for_review" value="#{deliveryMessages.mark}" />
	<h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('../author/markForReviewPopUp.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('../author/markForReviewTipText.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');" >
		<h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
	</h:outputLink>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <f:verbatim><br /></f:verbatim>

  <h:panelGroup rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true' && question.itemData.typeId != 3}" >
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="answerKeyMC" value="#{deliveryMessages.ans_key}#{deliveryMessages.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="answerKeyMC"
       value="#{question.key}" escape="false" />

  </h:panelGroup>

  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && !delivery.noFeedback=='true' && question.feedbackIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{deliveryMessages.feedback}#{deliveryMessages.column} " />
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
