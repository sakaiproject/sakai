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
      <title><h:outputText value="#{assessmentSettingsMessages.publish_assessment_confirmation}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
 <!-- content... -->
 <h:form id="publishAssessmentForm">
   <h:inputHidden id="assessmentId" value="#{assessmentSettings.assessmentId}"/>
   <h3>
      <h:outputText  value="#{assessmentSettingsMessages.publish_assessment_confirmation}" rendered="#{author.isEditPendingAssessmentFlow}"/>
      <h:outputText  value="#{assessmentSettingsMessages.republish_assessment_confirmation}" rendered="#{!author.isEditPendingAssessmentFlow && !author.isRepublishAndRegrade}"/>
      <h:outputText  value="#{assessmentSettingsMessages.regrade_republish_assessment_confirmation}" rendered="#{!author.isEditPendingAssessmentFlow && author.isRepublishAndRegrade}"/>
   </h3>
<div class="tier1">

  <!-- Error publishing assessment -->
  <h:messages globalOnly="true" infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

<h:panelGrid border="0" width="100%">
  <h:outputText value=" " />
  <h:panelGroup rendered="#{author.isEditPendingAssessmentFlow}">
    <h:panelGrid  columns="1">
	   <h:outputText value="#{assessmentSettingsMessages.publish_confirm_message}" />
       <h:outputText value="#{assessmentSettingsMessages.cancel_message}"/>
    </h:panelGrid>
  </h:panelGroup>

  <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow && !author.isRepublishAndRegrade}">
	<h:panelGrid  columns="1">
	   <h:outputText value="#{assessmentSettingsMessages.republish_confirm_message}" />
       <h:outputText value="#{assessmentSettingsMessages.cancel_message}"/>
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

<h:panelGrid columns="1" border="0">
  <h:outputText value="#{assessmentSettingsMessages.notification}" rendered="#{publishRepublishNotification.sendNotification}"/>
  <h:outputText value="#{assessmentSettingsMessages.subject} #{publishRepublishNotification.notificationSubject}" rendered="#{publishRepublishNotification.sendNotification}"/>

  <h:inputTextarea id="message1" value="#{publishRepublishNotification.prePopulateText}" styleClass='prePopulateText' onmousedown="clearText1()" rows="2" cols="70" rendered="#{publishRepublishNotification.sendNotification && author.isEditPendingAssessmentFlow}"/>
  <h:inputTextarea id="message2" value="#{publishRepublishNotification.prePopulateText}" styleClass='prePopulateText' onmousedown="clearText2()" rows="2" cols="70" rendered="#{publishRepublishNotification.sendNotification && !author.isEditPendingAssessmentFlow}"/>
</h:panelGrid>

<h:panelGrid columns="1" border="2" width="55%">
<h:panelGrid columns="1" rowClasses="shorttextPadding" rendered="#{author.isEditPendingAssessmentFlow}" border="0">
     <h:outputText value="#{assessmentSettings.title}" rendered="#{assessmentSettings.title ne null}"/>

     <h:outputText value="#{assessmentSettingsMessages.available} #{assessmentSettings.startDateString}" rendered="#{assessmentSettings.startDate ne null}" />
     <h:outputText rendered="#{assessmentSettings.startDate eq null}" value="#{assessmentSettingsMessages.available_immediately}" />
     <h:outputText value="#{assessmentSettingsMessages.due} #{assessmentSettings.dueDateString}" rendered="#{assessmentSettings.dueDate ne null}" />
     <h:outputText value="#{assessmentSettingsMessages.retract} #{assessmentSettings.retractDateString}" rendered="#{assessmentSettings.retractDate ne null}" />

     <h:outputText value="#{assessmentSettingsMessages.time_assessment_1} #{assessmentSettings.timedHours} #{assessmentSettingsMessages.hours}, #{assessmentSettings.timedMinutes} #{assessmentSettingsMessages.minutes}. #{assessmentSettingsMessages.time_assessment_2}" rendered="#{assessmentSettings.valueMap.hasTimeAssessment eq 'true' && assessmentSettings.timedMinutes != 0}"/>
     <h:outputText value="#{assessmentSettingsMessages.time_assessment_1} #{assessmentSettings.timedHours} #{assessmentSettingsMessages.hours}. #{assessmentSettingsMessages.time_assessment_2}" rendered="#{assessmentSettings.valueMap.hasTimeAssessment eq 'true' && assessmentSettings.timedMinutes == 0}"/>
	 <h:outputText rendered="#{assessmentSettings.valueMap.hasTimeAssessment ne 'true'}" value="#{assessmentSettingsMessages.no_time_limit}" />

     <h:outputText value="#{assessmentSettingsMessages.unlimited_submission_allowed}" rendered="#{assessmentSettings.unlimitedSubmissions eq '1'}" />
     <h:outputText value="#{assessmentSettings.submissionsAllowed} #{assessmentSettingsMessages.submission_allowed}" rendered="#{assessmentSettings.unlimitedSubmissions eq '0'}" />

     <h:panelGroup>
       <h:outputText value=" #{assessmentSettingsMessages.immediate_feedback}" rendered="#{assessmentSettings.feedbackDelivery eq '1'}" />
       <h:outputText value=" #{assessmentSettingsMessages.feedback_on_submission}" rendered="#{assessmentSettings.feedbackDelivery eq '4'}" />
       <h:outputText value=" #{assessmentSettingsMessages.no_feedback_short}" rendered="#{assessmentSettings.feedbackDelivery eq '3'}" />
       <h:outputText value=" #{assessmentSettingsMessages.feedback_available_on} #{assessmentSettings.feedbackDateString}"
          rendered="#{assessmentSettings.feedbackDelivery eq '2'}" >
       </h:outputText>
     </h:panelGroup>

     <h:outputLabel value="#{assessmentSettingsMessages.released_to_3} #{authorFrontDoorMessages.entire_site}" rendered="#{assessmentSettings.releaseTo ne 'Anonymous Users' && assessmentSettings.releaseTo ne 'Selected Groups'}"/>
	 <h:outputLabel value="#{assessmentSettingsMessages.released_to_3} #{assessmentSettings.releaseToGroupsAsString}" rendered="#{assessmentSettings.releaseTo eq 'Selected Groups'}"/>
	</h:panelGrid>

	<h:panelGrid columns="1" rowClasses="shorttextPadding" rendered="#{!author.isEditPendingAssessmentFlow}" border="0">
	 <h:outputText value="#{publishedSettings.title}" rendered="#{publishedSettings.title ne null}"/>

	 <h:outputText value="#{assessmentSettingsMessages.available} #{publishedSettings.startDateString}" rendered="#{publishedSettings.startDate ne null}" />
     <h:outputText rendered="#{publishedSettings.startDate eq null}" value="#{assessmentSettingsMessages.available_immediately}" />


     <h:outputText value="#{assessmentSettingsMessages.due} #{publishedSettings.dueDateString}" rendered="#{publishedSettings.dueDate ne null}" />

     <h:outputText value="#{assessmentSettingsMessages.retract} #{publishedSettings.retractDateString}" rendered="#{publishedSettings.retractDate ne null}" />

     <h:outputText value="#{assessmentSettingsMessages.time_assessment_1} #{publishedSettings.timedHours} #{assessmentSettingsMessages.hours}, #{publishedSettings.timedMinutes} #{assessmentSettingsMessages.minutes}. #{assessmentSettingsMessages.time_assessment_2}" rendered="#{publishedSettings.valueMap.hasTimeAssessment eq 'true' && publishedSettings.timedMinutes != 0}"/>
	   <h:outputText value="#{assessmentSettingsMessages.time_assessment_1} #{publishedSettings.timedHours} #{assessmentSettingsMessages.hours}. #{assessmentSettingsMessages.time_assessment_2}" rendered="#{publishedSettings.valueMap.hasTimeAssessment eq 'true' && publishedSettings.timedMinutes == 0}"/>
	 <h:outputText rendered="#{publishedSettings.valueMap.hasTimeAssessment ne 'true'}" value="#{assessmentSettingsMessages.no_time_limit}" />

     <h:outputText value="#{assessmentSettingsMessages.unlimited_submission_allowed}" rendered="#{publishedSettings.unlimitedSubmissions eq '1'}" />
     <h:outputText value="#{publishedSettings.submissionsAllowed} #{assessmentSettingsMessages.submission_allowed}" rendered="#{publishedSettings.unlimitedSubmissions eq '0'}" />

     <h:panelGroup>
       <h:outputText value=" #{assessmentSettingsMessages.immediate_feedback}" rendered="#{publishedSettings.feedbackDelivery eq '1'}" />
       <h:outputText value=" #{assessmentSettingsMessages.feedback_on_submission}" rendered="#{publishedSettings.feedbackDelivery eq '4'}" />
       <h:outputText value=" #{assessmentSettingsMessages.no_feedback_short}" rendered="#{publishedSettings.feedbackDelivery eq '3'}" />
       <h:outputText value=" #{assessmentSettingsMessages.feedback_available_on} #{publishedSettings.feedbackDateString}"
          rendered="#{publishedSettings.feedbackDelivery eq '2'}" >
       </h:outputText>
     </h:panelGroup>

     <h:outputText value="#{assessmentSettingsMessages.released_to_3} #{authorFrontDoorMessages.entire_site}" rendered="#{publishedSettings.releaseTo ne 'Anonymous Users' && publishedSettings.releaseTo ne 'Selected Groups'}"/>
 	 <h:outputText value="#{assessmentSettingsMessages.released_to_3} #{publishedSettings.releaseToGroupsAsString}" rendered="#{publishedSettings.releaseTo eq 'Selected Groups'}"/>

</h:panelGrid>
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
