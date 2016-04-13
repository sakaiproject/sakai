<%-- $Id$
include file for delivering audio questions
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

<f:verbatim><br /></f:verbatim>

<%-- this invisible text is a trick to get the value set in the component tree
     without displaying it; audioMediaUploadPath will get this to the back end
--%>
<h:outputText escape="false" value="
<input type=\"hidden\" name=\"mediaLocation_#{question.itemData.itemId}\" value=\"jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}.au\"/>" />

<h:outputText value="#{question.text} "  escape="false"/>
<!-- ATTACHMENTS -->
<%@ include file="/jsf/delivery/item/attachment.jsp" %>

<f:verbatim><br /></f:verbatim>
<f:verbatim><br /></f:verbatim>
<f:verbatim><div id="</f:verbatim><h:outputText value="question#{question.itemData.itemId}" /><f:verbatim>" style="</f:verbatim><h:outputText value="display:none;" rendered="#{question==null or question.hasNoMedia}" /><f:verbatim>" ></f:verbatim>

  <h:panelGrid cellpadding="10" columns="1">
    <h:panelGroup>
		<h:outputText escape="false" value="
          <script tye=\"text/javascript\">
            var audio = new Audio();
            if (!audio.canPlayType(\"audio/wav\")) {
               document.write('<object><param name=\"autostart\" value=\"false\"/><param name=\"autoplay\" value=\"false\"/><param name=\"controller\" value=\"true\"/><embed src=\"#{delivery.protocol}/samigo-app/servlet/ShowMedia?mediaId=#{question.mediaArray[0].mediaId}\" volume=\"50\" height=\"25\" width=\"300\" autostart=\"false\" autoplay=\"false\" controller=\"true\" type=\"audio/basic\"/></object>');
            }
            else {
                document.write('<audio controls=\"controls\"><source src=\"#{delivery.protocol}/samigo-app/servlet/ShowMedia?mediaId=#{question.mediaArray[0].mediaId}\" type=\"audio/wav\"/></audio>');
            }
          </script>" 
        />

      <f:verbatim><br /></f:verbatim>
      <h:outputText value="#{deliveryMessages.open_bracket}"/>
      <f:verbatim><span id="</f:verbatim><h:outputText value="details#{question.itemData.itemId}" /><f:verbatim>"></f:verbatim>
		<h:outputText value="#{question.mediaArray[0].duration} #{deliveryMessages.secs}, #{deliveryMessages.recorded_on} " rendered="#{!question.mediaArray[0].durationIsOver}" />
		<h:outputText value="#{question.mediaArray[0].duration} #{deliveryMessages.secs}, #{deliveryMessages.recorded_on} " rendered="#{question.mediaArray[0].durationIsOver}" />
      <h:outputText value="#{question.mediaArray[0].createdDate}">
        <f:convertDateTime pattern="#{deliveryMessages.delivery_date_format}" />
      </h:outputText>
      <f:verbatim></span></f:verbatim>
      <h:outputText value="#{deliveryMessages.close_bracket}"/>
      <f:verbatim><br /></f:verbatim>
      <h:outputFormat value=" #{deliveryMessages.can_you_hear}" escape="false">
		<f:param value="<a href=\"#{delivery.protocol}/samigo-app/servlet/ShowMedia?mediaId=#{question.mediaArray[0].mediaId}&setMimeType=false\"/> #{deliveryMessages.can_you_hear_2}</a>" />
      </h:outputFormat>
    </h:panelGroup>
  </h:panelGrid>
<f:verbatim></div></f:verbatim>

<h:panelGroup rendered="#{question.attemptsRemaining == null || question.attemptsRemaining > 0}">
  <h:outputLink title="#{assessmentSettingsMessages.record_your_answer}" value="#" rendered="#{delivery.actionString!='reviewAssessment'}"  onclick="javascript:window.open('../author/audioRecordingPopup.faces?questionId=#{question.itemData.itemId}&duration=#{question.duration}&triesAllowed=#{question.triesAllowed}&attemptsRemaining=#{question.attemptsRemaining}&questionNumber=#{question.number}&questionTotal=#{part.questions}','AudioRecordingApplet','width=950,height=700,scrollbars=no, resizable=no');" >
	<h:outputText value=" #{assessmentSettingsMessages.record_your_answer}"/>
  </h:outputLink>
</h:panelGroup>

<h:panelGroup rendered="#{question.attemptsRemaining != null && question.attemptsRemaining < 1}">
  <h:outputText value=" #{assessmentSettingsMessages.record_no_more_attempts}"/>
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

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGrid rendered="#{delivery.feedbackComponent.showItemLevel && question.feedbackIsNotEmpty}">
    <h:panelGroup>
      <h:outputLabel for="feedSC" styleClass="answerkeyFeedbackCommentLabel" value="#{commonMessages.feedback}#{deliveryMessages.column} " />
      <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
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

