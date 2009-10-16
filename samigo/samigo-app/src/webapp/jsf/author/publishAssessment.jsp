<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{assessmentSettingsMessages.check_settings_and_add_notification}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
 <!-- content... -->
 <h:form id="publishAssessmentForm">
   <h:inputHidden id="assessmentId" value="#{assessmentSettings.assessmentId}"/>
   <h3>
      <h:outputText  value="#{assessmentSettingsMessages.check_settings_and_add_notification}"/>
   </h3>
<div class="tier1">

  <!-- Error publishing assessment -->
  <h:messages globalOnly="true" infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

<h:panelGrid border="0" width="100%">
  <h:outputText value=" " />
  <h:panelGroup rendered="#{author.isEditPendingAssessmentFlow}">
    <h:panelGrid  columns="1">
	   <h:outputText value="#{assessmentSettingsMessages.publish_confirm_message_1} <b>#{assessmentSettingsMessages.publish_confirm_message_2}</b> #{assessmentSettingsMessages.publish_confirm_message_3}" escape="false"/>
       <h:outputText value="#{assessmentSettingsMessages.cancel_message_1} <b>#{assessmentSettingsMessages.cancel_message_2}</b> #{assessmentSettingsMessages.cancel_message_3}" escape="false"/>
    </h:panelGrid>
  </h:panelGroup>

  <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow && !author.isRepublishAndRegrade}">
	<h:panelGrid  columns="1">
   	   <h:outputText value="#{assessmentSettingsMessages.republish_confirm_message_1} <b>#{assessmentSettingsMessages.republish_confirm_message_2}</b> #{assessmentSettingsMessages.republish_confirm_message_3}" escape="false"/>
       <h:outputText value="#{assessmentSettingsMessages.cancel_message_1} <b>#{assessmentSettingsMessages.cancel_message_2}</b> #{assessmentSettingsMessages.cancel_message_3}" escape="false"/>    
	</h:panelGrid>
  </h:panelGroup>

  <h:outputText value="#{assessmentSettingsMessages.started_or_submitted}" rendered="#{!author.isEditPendingAssessmentFlow && author.isRepublishAndRegrade}" styleClass="validation"/> 

<h:panelGrid rendered="#{!author.isEditPendingAssessmentFlow && author.isRepublishAndRegrade}">
    <h:outputText value="#{assessmentSettingsMessages.score_discrepancies_note}" rendered="#{publishedSettings.itemNavigation ne '2' || !assessmentBean.hasSubmission}"/> 
    <h:outputText value="#{assessmentSettingsMessages.score_discrepancies_note_non_linear}" rendered="#{publishedSettings.itemNavigation eq '2' && assessmentBean.hasSubmission}"/> 
  <h:panelGroup rendered="#{publishedSettings.itemNavigation eq '2' && assessmentBean.hasSubmission}">
        <h:selectBooleanCheckbox id="updateMostCurrentSubmissionCheckbox" value="#{publishedSettings.updateMostCurrentSubmission}" />
        <h:outputText value="#{assessmentSettingsMessages.update_most_current_submission_checkbox}" />
  </h:panelGroup>
</h:panelGrid>
</h:panelGrid>


 <f:verbatim><p class="act"></f:verbatim>
 <!-- Cancel button -->
   <h:commandButton value="#{assessmentSettingsMessages.button_cancel}" type="submit" action="editAssessment" rendered="#{author.isEditPendingAssessmentFlow}"/>
   <h:commandButton value="#{assessmentSettingsMessages.button_cancel}" type="submit" action="editAssessment" rendered="#{!author.isEditPendingAssessmentFlow}">
	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
   </h:commandButton>

   <!-- Publish, Republishe and Regrade, or Republish button -->
   <h:commandButton id="publish" value="#{assessmentSettingsMessages.button_save_and_publish}" type="submit"
	 styleClass="active" action="publishAssessment" onclick="toggle()" onkeypress="toggle()" rendered="#{author.isEditPendingAssessmentFlow}">
	  <f:actionListener
		type="org.sakaiproject.tool.assessment.ui.listener.author.PublishAssessmentListener" />
   </h:commandButton>

	<h:commandButton  value="#{authorMessages.button_republish_and_regrade}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow && author.isRepublishAndRegrade}" action="publishAssessment">
		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RepublishAssessmentListener" />
	</h:commandButton>

	<h:commandButton  value="#{authorMessages.button_republish}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow && !author.isRepublishAndRegrade}" action="publishAssessment">
		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RepublishAssessmentListener" />
	</h:commandButton>

	<h:selectOneMenu id="number" value="1" onchange="document.forms[0].submit();">
          <f:selectItems value="#{publishRepublishNotification.notificationLevelChoices}" />
          <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.PublishRepublishNotificationListener" />
    </h:selectOneMenu>

  <f:verbatim></p></f:verbatim>

<h:panelGrid columns="1" border="0" width="78%" styleClass="settings">
<h:panelGrid columns="1" border="0">
<h:panelGrid columns="1" border="0">
<h:outputText value="#{assessmentSettingsMessages.notification}" styleClass="notification" rendered="#{publishRepublishNotification.sendNotification}" escape="false"/>
<h:outputText value="#{assessmentSettingsMessages.subject} #{publishRepublishNotification.notificationSubject}" rendered="#{publishRepublishNotification.sendNotification}"/>

  <h:inputTextarea id="message1" value="#{publishRepublishNotification.prePopulateText}" styleClass='prePopulateText' onmousedown="clearText1()" rows="2" cols="70" rendered="#{publishRepublishNotification.sendNotification && author.isEditPendingAssessmentFlow}"/>
  <h:inputTextarea id="message2" value="#{publishRepublishNotification.prePopulateText}" styleClass='prePopulateText' onmousedown="clearText2()" rows="2" cols="70" rendered="#{publishRepublishNotification.sendNotification && !author.isEditPendingAssessmentFlow}"/>
</h:panelGrid>

<h:panelGrid columns="1" rowClasses="shorttextPadding" rendered="#{author.isEditPendingAssessmentFlow}" border="0">
	<h:panelGroup>
		<h:panelGroup rendered="#{assessmentSettings.title ne null}"> 
			<f:verbatim><b></f:verbatim>
			<h:outputText value="\"#{assessmentSettings.title}\"" />
			<f:verbatim></b></f:verbatim>
		</h:panelGroup>
		
		<h:panelGroup rendered="#{assessmentSettings.startDate ne null}">
			<h:outputText value=" #{assessmentSettingsMessages.will_be}" />
			<f:verbatim><b></f:verbatim>
			<h:outputText value=" #{assessmentSettingsMessages.available_on} #{assessmentSettings.startDateString}" />
			<f:verbatim></b></f:verbatim>
		</h:panelGroup>
		
		<h:panelGroup rendered="#{assessmentSettings.startDate eq null}" >
			<h:outputText value=" #{assessmentSettingsMessages.is}" />
			<f:verbatim><b></f:verbatim>
			<h:outputText value=" #{assessmentSettingsMessages.available_immediately_2}" />
			<f:verbatim></b></f:verbatim>
		</h:panelGroup>
		<h:outputText value="." />
	</h:panelGroup>
    
	<h:panelGroup> 
		<h:outputLabel value="#{assessmentSettingsMessages.to_take_anonymously}" rendered="#{assessmentSettings.releaseTo eq 'Anonymous Users'}"/>
		<h:outputLabel value="#{assessmentSettingsMessages.to_the_entire_class}" rendered="#{assessmentSettings.releaseTo ne 'Anonymous Users' && assessmentSettings.releaseTo ne 'Selected Groups'}"/>
		<h:outputLabel value="#{assessmentSettingsMessages.to} #{assessmentSettings.releaseToGroupsAsString}" rendered="#{assessmentSettings.releaseTo eq 'Selected Groups'}"/>
		<h:outputText value=" #{assessmentSettingsMessages.at} #{assessmentSettings.publishedUrl}." />
	</h:panelGroup>
	
	<h:panelGroup  rendered="#{assessmentSettings.dueDate ne null}" > 
		<h:outputText value="#{assessmentSettingsMessages.it_is}"/>
		<f:verbatim><b></f:verbatim>
		<h:outputText value=" #{assessmentSettingsMessages.due} #{assessmentSettings.dueDateString}"/>
		<f:verbatim></b></f:verbatim>
		<h:outputText value="." />
	</h:panelGroup>
	<f:verbatim><br/></f:verbatim>    

	<h:panelGroup>
	<h:outputText value="#{assessmentSettingsMessages.the_time_limit_is} #{assessmentSettings.timedHours} #{assessmentSettingsMessages.hours}, #{assessmentSettings.timedMinutes} #{assessmentSettingsMessages.minutes}. #{assessmentSettingsMessages.submit_when_time_is_up}" rendered="#{assessmentSettings.valueMap.hasTimeAssessment eq 'true' && assessmentSettings.timedMinutes != 0}"/>
	<h:outputText value="#{assessmentSettingsMessages.the_time_limit_is} #{assessmentSettings.timedHours} #{assessmentSettingsMessages.hours}. #{assessmentSettingsMessages.submit_when_time_is_up}" rendered="#{assessmentSettings.valueMap.hasTimeAssessment eq 'true' && assessmentSettings.timedMinutes == 0}"/>
	<h:outputText rendered="#{assessmentSettings.valueMap.hasTimeAssessment ne 'true'}" value="#{assessmentSettingsMessages.there_is_no_time_limit}" />
		
		<h:outputText value=" #{assessmentSettingsMessages.student_submit_unlimited_times}" rendered="#{assessmentSettings.unlimitedSubmissions eq '1'}" />
		<h:outputText value=" #{assessmentSettingsMessages.student_submit} #{assessmentSettings.submissionsAllowed} #{assessmentSettingsMessages.times}" rendered="#{assessmentSettings.unlimitedSubmissions eq '0'}" />	
		<h:outputText value=" #{assessmentSettingsMessages.record_highest}" rendered="#{assessmentSettings.scoringType eq '1'}" />	
		<h:outputText value=" #{assessmentSettingsMessages.record_last}" rendered="#{assessmentSettings.scoringType eq '2'}" />	
	</h:panelGroup>
		
	<f:verbatim><br/></f:verbatim>   
		
	<h:panelGroup>
		<h:outputText value=" #{assessmentSettingsMessages.students_will_receive}"/>
		<h:panelGroup rendered="#{assessmentSettings.feedbackDelivery ne '2'}">
			<f:verbatim><b></f:verbatim>
			<h:outputText value=" #{assessmentSettingsMessages.immediate_feedback_2}" rendered="#{assessmentSettings.feedbackDelivery eq '1'}" />
			<h:outputText value=" #{assessmentSettingsMessages.feedback_on_submission_1}" rendered="#{assessmentSettings.feedbackDelivery eq '4'}" />
			<h:outputText value=" #{assessmentSettingsMessages.no_feedback_short_2}" rendered="#{assessmentSettings.feedbackDelivery eq '3'}" />
			<f:verbatim></b></f:verbatim>
		</h:panelGroup>
				
		<h:panelGroup rendered="#{assessmentSettings.feedbackDelivery eq '2'}" >
			<f:verbatim><b></f:verbatim>
			<h:outputText value=" #{assessmentSettingsMessages.feedback_2}" />
			<f:verbatim></b></f:verbatim>
			<h:outputText value=" #{assessmentSettingsMessages.at}" />
			<f:verbatim><b></f:verbatim>
			<h:outputText value=" #{assessmentSettings.feedbackDateString}" />
			<f:verbatim></b></f:verbatim>
		</h:panelGroup>
		<h:outputText value="." />
	</h:panelGroup>

	</h:panelGrid>

	<h:panelGrid columns="1" rowClasses="shorttextPadding" rendered="#{!author.isEditPendingAssessmentFlow}" border="0">
		<h:panelGroup>
			<h:panelGroup rendered="#{publishedSettings.title ne null}"> 
				<f:verbatim><b></f:verbatim>
				<h:outputText value="\"#{publishedSettings.title}\"" />
				<f:verbatim></b></f:verbatim>
			</h:panelGroup>

			<h:panelGroup rendered="#{publishedSettings.startDate ne null}">
				<h:outputText value=" #{assessmentSettingsMessages.will_be}" />
				<f:verbatim><b></f:verbatim>
				<h:outputText value=" #{assessmentSettingsMessages.available_on} #{publishedSettings.startDateString}" />
				<f:verbatim></b></f:verbatim>
			</h:panelGroup>

			<h:panelGroup rendered="#{publishedSettings.startDate eq null}" >
				<h:outputText value=" #{assessmentSettingsMessages.is}" />
				<f:verbatim><b></f:verbatim>
				<h:outputText value=" #{assessmentSettingsMessages.available_immediately_2}" />
				<f:verbatim></b></f:verbatim>
			</h:panelGroup>
			<h:outputText value="." />
		</h:panelGroup>
		
		<h:panelGroup> 
			<h:outputLabel value="#{assessmentSettingsMessages.to_take_anonymously}" rendered="#{publishedSettings.releaseTo eq 'Anonymous Users'}"/>
			<h:outputLabel value="#{assessmentSettingsMessages.to_the_entire_class}" rendered="#{publishedSettings.releaseTo ne 'Anonymous Users' && publishedSettings.releaseTo ne 'Selected Groups'}"/>
			<h:outputLabel value="#{assessmentSettingsMessages.to} #{publishedSettings.releaseToGroupsAsString}" rendered="#{publishedSettings.releaseTo eq 'Selected Groups'}"/>
			<h:outputText value=" #{assessmentSettingsMessages.at} #{publishedSettings.publishedUrl}." />
		</h:panelGroup>

		<h:panelGroup  rendered="#{publishedSettings.dueDate ne null}" > 
			<h:outputText value="#{assessmentSettingsMessages.it_is}"/>
			<f:verbatim><b></f:verbatim>
			<h:outputText value=" #{assessmentSettingsMessages.due} #{publishedSettings.dueDateString}"/>
			<f:verbatim></b></f:verbatim>
			<h:outputText value="." />
		</h:panelGroup>
		<f:verbatim><br/></f:verbatim>
		
		<h:panelGroup>
		<h:outputText value="#{assessmentSettingsMessages.the_time_limit_is} #{publishedSettings.timedHours} #{assessmentSettingsMessages.hours}, #{publishedSettings.timedMinutes} #{assessmentSettingsMessages.minutes}. #{assessmentSettingsMessages.submit_when_time_is_up}" rendered="#{publishedSettings.valueMap.hasTimeAssessment eq 'true' && publishedSettings.timedMinutes != 0}"/>
		<h:outputText value="#{assessmentSettingsMessages.the_time_limit_is} #{publishedSettings.timedHours} #{assessmentSettingsMessages.hours}. #{assessmentSettingsMessages.submit_when_time_is_up}" rendered="#{publishedSettings.valueMap.hasTimeAssessment eq 'true' && publishedSettings.timedMinutes == 0}"/>
		<h:outputText rendered="#{publishedSettings.valueMap.hasTimeAssessment ne 'true'}" value="#{assessmentSettingsMessages.there_is_no_time_limit}" />

			<h:outputText value=" #{assessmentSettingsMessages.student_submit_unlimited_times}" rendered="#{publishedSettings.unlimitedSubmissions eq '1'}" />
			<h:outputText value=" #{assessmentSettingsMessages.student_submit} #{publishedSettings.submissionsAllowed} #{assessmentSettingsMessages.times}" rendered="#{publishedSettings.unlimitedSubmissions eq '0'}" />	
			<h:outputText value=" #{assessmentSettingsMessages.record_highest}" rendered="#{publishedSettings.scoringType eq '1'}" />	
			<h:outputText value=" #{assessmentSettingsMessages.record_last}" rendered="#{publishedSettings.scoringType eq '2'}" />	
		</h:panelGroup>
		
		<h:panelGroup>
			<h:outputText value=" #{assessmentSettingsMessages.students_will_receive}"/>
			<h:panelGroup rendered="#{publishedSettings.feedbackDelivery ne '2'}">
				<f:verbatim><b></f:verbatim>
				<h:outputText value=" #{assessmentSettingsMessages.immediate_feedback_2}" rendered="#{publishedSettings.feedbackDelivery eq '1'}" />
				<h:outputText value=" #{assessmentSettingsMessages.feedback_on_submission_1}" rendered="#{publishedSettings.feedbackDelivery eq '4'}" />
				<h:outputText value=" #{assessmentSettingsMessages.no_feedback_short_2}" rendered="#{publishedSettings.feedbackDelivery eq '3'}" />
				<f:verbatim></b></f:verbatim>
			</h:panelGroup>
				
			<h:panelGroup rendered="#{publishedSettings.feedbackDelivery eq '2'}" >
				<f:verbatim><b></f:verbatim>
				<h:outputText value=" #{assessmentSettingsMessages.feedback_2}" />
				<f:verbatim></b></f:verbatim>
				<h:outputText value=" #{assessmentSettingsMessages.at}" />
				<f:verbatim><b></f:verbatim>
				<h:outputText value=" #{publishedSettings.feedbackDateString}" />
				<f:verbatim></b></f:verbatim>
			</h:panelGroup>
			<h:outputText value="." />
		</h:panelGroup>
		
</h:panelGrid>
</h:panelGrid>
<h:panelGrid />
<h:panelGrid />
</h:panelGrid>

<f:verbatim><p></p></f:verbatim>

<script language="javascript" type="text/JavaScript">
<!--
var clicked = 'false';
function toggle(){
  if (clicked == 'false'){
    clicked = 'true'
  }
  else{ // any subsequent click disable button & action
    document.forms[0].elements['publishAssessmentForm:publish'].disabled=true;
  }
}

var entered = 'false';
function clearText1(){
  if (entered == 'false'){
    document.forms[0].elements['publishAssessmentForm:message1'].value='';
	document.forms[0].elements['publishAssessmentForm:message1'].className='simple_text_area';
	document.forms[0].elements['publishAssessmentForm:message1'].focus();
    entered = 'true'
  }
}

function clearText2(){
  if (entered == 'false'){
    document.forms[0].elements['publishAssessmentForm:message2'].value='';
	document.forms[0].elements['publishAssessmentForm:message2'].className='simple_text_area';
	document.forms[0].elements['publishAssessmentForm:message2'].focus();
    entered = 'true'
  }
}
//-->
</script>


<f:verbatim><p></p></f:verbatim>

 </h:form>
 <!-- end content -->
</div>

      </body>
    </html>

  </f:view>
