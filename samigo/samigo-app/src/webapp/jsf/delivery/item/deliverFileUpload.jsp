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
<%-- $Id$
include file for delivering file upload questions
should be included in file importing DeliveryMessages
--%>

<h:outputText value="#{question.text} <br/>"  escape="false"/>
<!-- ATTACHMENTS -->
<%@ include file="/jsf/delivery/item/attachment.jsp" %>

<h:panelGroup rendered="#{delivery.actionString=='takeAssessment' 
                       || delivery.actionString=='takeAssessmentViaUrl'}">
  <h:outputText escape="false" value="#{deliveryMessages.upload_instruction} " />
  <h:outputText escape="false" value=" (#{deliveryMessages.max_file_size} #{delivery.fileUploadSizeMax}#{deliveryMessages.unit}) <br />" styleClass="validate"/>

  <h:outputText value="#{deliveryMessages.file} " />
  <!-- note that target represent the location where the upload medis will be temporarily stored -->
  <!-- For ItemGradingData, it is very important that target must be in this format: -->
  <!-- assessmentXXX/questionXXX/agentId -->
  <!-- please check the valueChangeListener to get the final destination -->
  <corejsf:upload
    target="jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}"
    valueChangeListener="#{delivery.addMediaToItemGrading}" />
  <f:verbatim>&nbsp;&nbsp;</f:verbatim>
  <h:commandButton id="upl" value="#{deliveryMessages.upload}" action="#{delivery.getOutcome}" onclick="showNotif('submitnotif',this.name,'takeAssessmentForm');disableShowTimeWarning();"/>
</h:panelGroup>
<h:outputText escape="false" value="<span id=\"submitnotif\" style=\"visibility:hidden\"> #{deliveryMessages.processing}</span>"/>
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment' 
                       || delivery.actionString=='gradeAssessment'}">
  <h:outputText value="#{deliveryMessages.file}" />
  <!-- note that target represent the location where the upload medis will be temporarily stored -->
  <!-- For ItemGradingData, it is very important that target must be in this format: -->
  <!-- assessmentXXX/questionXXX/agentId -->
  <!-- please check the valueChangeListener to get the final destination -->
  <h:inputText size="50" />
  <h:outputText value="  " />
  <h:commandButton value="#{deliveryMessages.browse}" type="button"/>
  <h:outputText value="  " />
  <h:commandButton value="#{deliveryMessages.upload}" type="button"/>
</h:panelGroup>

<f:verbatim><br /></f:verbatim>

      <%-- media list, note that question is ItemContentBean --%>
<h:panelGroup rendered="#{question!=null and question.mediaArray!=null}">
      <h:dataTable value="#{question.mediaArray}" var="media">
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink title="#{deliveryMessages.t_uploadedFile}" value="/samigo-app/servlet/ShowMedia?mediaId=#{media.mediaId}&sam_fileupload_siteId=#{delivery.siteId}" target="new_window">
             <h:outputText value="#{media.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
         <h:outputText value="#{deliveryMessages.open_bracket}"/>
         	<h:outputText value="#{media.fileSizeKBFormat} #{generalMessages.kb}"/>
         <h:outputText value="#{deliveryMessages.close_bracket}"/>
        </h:column>
        <h:column rendered="#{delivery.actionString=='takeAssessment' 
                           || delivery.actionString=='takeAssessmentViaUrl'}">
          <h:commandLink title="#{deliveryMessages.t_removeMedia}" action="confirmRemoveMedia"
            id="removeMedia" onmouseup="saveTime();">
            <h:outputText value="#{commonMessages.remove_action}" />
            <f:param name="mediaId" value="#{media.mediaId}"/>
            <f:param name="mediaUrl" value="/samigo-app/servlet/ShowMedia?mediaId=#{media.mediaId}"/>
            <f:param name="mediaFilename" value="#{media.filename}"/>
            <f:param name="itemGradingId" value="#{media.itemGradingData.itemGradingId}"/>
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.ConfirmRemoveMediaListener" />
          </h:commandLink>
        </h:column>
      </h:dataTable>
</h:panelGroup>

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
  <h:panelGrid rendered="#{delivery.feedbackComponent.showItemLevel && question.feedbackIsNotEmpty}">
    <h:panelGroup>
      <h:outputLabel for="feedSC" styleClass="answerkeyFeedbackCommentLabel" value="#{commonMessages.feedback}: " />
      <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
    </h:panelGroup>
    <h:outputText value=" " />
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
