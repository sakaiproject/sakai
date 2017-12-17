<%-- $Id: deliverMatrixChoicesSurvey.jsp 74302 2010-03-05 20:24:50Z kimhuang@rutgers.edu $
should be included in file importing DeliveryMessages
--%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

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
<h:messages layout="table" styleClass="error"/>

 <h:outputText value="#{question.itemData.text}"  escape="false"/>
 
  <f:verbatim><br /></f:verbatim>
  
  <!-- ATTACHMENTS -->
  <%@ include file="/jsf/delivery/item/attachment.jsp" %>
  <!-- deliverMatrixChoice.jsp -->
<!-- question:ItemContentsBean -->
<!-- delivery:DeliveryBean -->
<!-- selection: SelectionBean.java under delivery answer is AnswerIfc -->
<div class="tier1">
<f:verbatim><br /></f:verbatim>
<f:verbatim><br /></f:verbatim>
 <t:dataTable id="matrixSurveyRadioTable"
                         value="#{question.matrixArray}"
                         var="matrixBean"
                         rowIndexVar="rowIndex"
                         columnClasses="Tablecolumn"
                         rowClasses=""
                         frame="border"
                         rules="rows"
                         styleClass="TableClass">
                <t:column headerstyle="#{question.itemData.relativeWidthStyle}" style="text-align:right;padding-left:0.3em" >
                    <f:facet name="header">
                        <t:outputText value="                "/>
                    </f:facet>
                    <h:outputText value="#{matrixBean.itemText.text}"/>
                </t:column>
		 <t:columns value="#{question.columnIndexList}" var="colIndex" styleClass="center" headerstyleClass="center matrixSurvey" >
		 <f:facet name="header">
                        <t:outputText value="#{question.columnArray[colIndex]}"/>
                    </f:facet>
		 <samigo:radioButton 
			id="myRadioId1"
			name="myRadioRow"
       			value="#{matrixBean.responseId}"
       			disabled="#{delivery.actionString=='reviewAssessment'|| delivery.actionString=='gradeAssessment'}"
       			itemValue="#{matrixBean.answerSid[colIndex]}"
       			onClick="if( whichradio(this) !== true ) {return false;}" />
         </t:columns>
         </t:dataTable>
     <f:verbatim><br /></f:verbatim>      
 <h:panelGrid rendered="#{question.addComment}"> 
 	<h:outputText value="#{question.commentField}" />
 	<h:inputTextarea id="commentField" 
 							value="#{question.studentComment}" 
 							rows="6" 
 							cols="54"
 							immediate="true"
 							disabled="#{delivery.actionString=='reviewAssessment'|| delivery.actionString=='gradeAssessment'}" />
</h:panelGrid>
<h:inputHidden value="#{question.forceRanking}" id="forceRanking"/>
</div>
  
  <h:panelGroup rendered="#{question.itemData.hasRationale}" >
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

  <h:commandLink id="cmdclean" value="#{deliveryMessages.reset_selection}" action="#{delivery.cleanAndSaveRadioButton}" onclick="saveTime(); serializeImagePoints();" 
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
	<h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('../author/markForReviewPopUp.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');">
		<h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
	</h:outputLink>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGrid rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true' && question.itemData.typeId != 3}" >
    <h:panelGroup>
      <h:outputLabel for="answerKeyMC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.ans_key}#{deliveryMessages.column} " />
      <h:outputText id="answerKeyMC" value="#{question.key}" escape="false" />
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
