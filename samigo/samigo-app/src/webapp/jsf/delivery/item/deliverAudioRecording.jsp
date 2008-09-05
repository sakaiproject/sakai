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
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
  <h:panelGrid cellpadding="10" columns="2">
    <h:panelGroup>
      <h:outputText escape="false" value="
	    <object classid=\"clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B\"  codebase=\"http://www.apple.com/qtactivex/qtplugin.cab\" width=\"300\" height=\"25\">
		    <param id=\"mediaSrc#{question.itemData.itemId}\" name=\"src\" value=\"#{delivery.protocol}/samigo/servlet/ShowMedia?mediaId=#{question.mediaArray[0].mediaId}\" /> 
		    <param name=\"controller\" value=\"true\" /> 
		    <param name=\"autoplay\" value=\"false\" />

		<!--[if !IE]>-->

		<object type=\"audio/basic\"
			    id=\"object#{question.itemData.itemId}\" 
		        data=\"#{delivery.protocol}/samigo/servlet/ShowMedia?mediaId=#{question.mediaArray[0].mediaId}\" 
		        width=\"300\" height=\"25\">
		        <param name=\"autoplay\" value=\"false\" />
		        <param name=\"controller\" value=\"true\" />
		        </object>
		<!--<![endif]-->
		    </object>
         " />

      <f:verbatim><br /></f:verbatim>
      <h:outputText value="#{deliveryMessages.open_bracket}"/>
      <f:verbatim><span id="</f:verbatim><h:outputText value="details#{question.itemData.itemId}" /><f:verbatim>"></f:verbatim>
      <h:outputText value="#{question.mediaArray[0].duration} sec, recorded on " rendered="#{!question.mediaArray[0].durationIsOver}" />
      <h:outputText value="#{question.mediaArray[0].duration} sec, recorded on " rendered="#{question.mediaArray[0].durationIsOver}" />
      <h:outputText value="#{question.mediaArray[0].createdDate}">
        <f:convertDateTime pattern="#{deliveryMessages.delivery_date_format}" />
      </h:outputText>
      <f:verbatim></span></f:verbatim>
      <h:outputText value="#{deliveryMessages.close_bracket}"/>
      <f:verbatim><br /></f:verbatim>
      <h:outputText value="#{deliveryMessages.can_you_hear_1}"  escape="false"/>
      <f:verbatim><a id="</f:verbatim><h:outputText value="link#{question.itemData.itemId}" /><f:verbatim>" href="</f:verbatim><h:outputText value="#{delivery.protocol}/samigo/servlet/ShowMedia?mediaId=#{question.mediaArray[0].mediaId}&setMimeType=false" /><f:verbatim>" ></f:verbatim><h:outputText value=" #{deliveryMessages.can_you_hear_2} " escape="false" /><f:verbatim></a></f:verbatim>
      <h:outputText value="#{deliveryMessages.can_you_hear_3}"  escape="false"/>
    </h:panelGroup>
      <h:commandLink 
	      title="#{deliveryMessages.t_removeMedia}" 
	      action="confirmRemoveMedia"
	      immediate="true">
        <h:outputText value="   #{deliveryMessages.remove}" />
        <f:param name="mediaId" value="#{question.mediaArray[0].mediaId}"/>
        <f:param name="mediaUrl" value="/samigo/servlet/ShowMedia?mediaId=#{question.mediaArray[0].mediaId}"/>
        <f:param name="mediaFilename" value="#{question.mediaArray[0].filename}"/>
        <f:param name="itemGradingId" value="#{question.itemGradingDataArray[0].itemGradingId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.ConfirmRemoveMediaListener" />
      </h:commandLink>
  </h:panelGrid>
<f:verbatim></div></f:verbatim>

<%-- <%@ include file="/jsf/delivery/item/audioObject.jsp" %> --%>
<%-- <%@ include file="/jsf/delivery/item/audioApplet.jsp" %> --%>
<h:outputLink title="#{assessmentSettingsMessages.record_your_answer}" value="#" onclick="javascript:window.open('../author/audioRecordingPopup.faces?questionId=#{question.itemData.itemId}&duration=#{question.duration}&triesAllowed=#{question.triesAllowed}&attemptsRemaining=#{question.attemptsRemaining}','AudioRecordingApplet','width=448,height=400,scrollbars=no, resizable=no');" >
	<h:outputText  value=" #{assessmentSettingsMessages.record_your_answer}"/>
</h:outputLink>

<f:verbatim><br /></f:verbatim>

<h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review"
   rendered="#{(delivery.actionString=='takeAssessment'|| delivery.actionString=='takeAssessmentViaUrl')
            && delivery.navigation ne '1' }" />
<h:outputLabel for="mark_for_review" value="#{deliveryMessages.mark}"
  rendered="#{(delivery.actionString=='takeAssessment'|| delivery.actionString=='takeAssessmentViaUrl')
            && delivery.navigation ne '1'}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && question.feedbackIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{deliveryMessages.feedback}#{deliveryMessages.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingCommentIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{deliveryMessages.comment}#{deliveryMessages.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}" escape="false" />
  </h:panelGroup>
</h:panelGroup>

