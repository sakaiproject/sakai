<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

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
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title>
      <h:outputText value="#{delivery.assessmentTitle}"/>

      </title>

      <%@ include file="/jsf/delivery/deliveryjQuery.jsp" %>
      <script src="/library/js/spinner.js"></script>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>;">
 
<div class="portletBody container-fluid">
 <h:outputText value="<div style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

 <!-- content... -->
<h:form id="takeAssessmentForm">
  <h:inputHidden id="timerId" value="#{delivery.timerId}" rendered="#{delivery.timerId!=null}" />

<!-- Secure Delivery HTML (aka Proctoring or Lockdown)-->
<h:panelGroup rendered="#{delivery.actionString!='previewAssessment'}">
  <h:outputText value="#{delivery.secureDeliveryHTMLFragment}" escape="false" />
</h:panelGroup>

<!-- DONE BUTTON FOR PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
  <div class="sak-banner-info mb-5">
    <h:outputText value="#{deliveryMessages.ass_preview}" escape="false" />
    <h:panelGroup rendered="#{delivery.sebSetup}" layout="block">
      <br/><br/>
      <h:outputText value="#{deliveryMessages.seb_preview_info}" escape="false" />
    </h:panelGroup>
    <br/>
    <h:commandButton value="#{deliveryMessages.exit_preview}"
      styleClass="exit-preview-button"
      action="#{person.cleanResourceIdListInPreview}"
      type="submit"
      onclick="return returnToHostUrl(\"#{delivery.selectURL}\");" />
    </div>
</h:panelGroup>

  <h1>
    <h:outputText value="#{deliveryMessages.begin_assessment_}" rendered="#{delivery.firstTimeTaking}"/>
    <h:outputText value="#{deliveryMessages.continue_assessment_}" rendered="#{!delivery.firstTimeTaking && !delivery.timeExpired}"/>
  </h1>

  <div class="lead">
    <h:outputText value="\"#{delivery.assessmentTitle}\" #{deliveryMessages.t_for} #{delivery.courseName} " escape="false"/>
  </div>

  <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
  
  <div class="sak-banner-info">
    <!-- ASSESSMENT INTRODUCTION -->
    <h:outputText value="#{delivery.instructorMessage}" escape="false" rendered="#{delivery.instructorMessage != null && delivery.instructorMessage != ''}"/>

  <!-- ASSESSMENT ATTACHMENTS -->
  <%@ include file="/jsf/delivery/assessment_attachment.jsp" %>


<div>
<h:panelGrid columns="1" border="0">
    <h:panelGroup rendered="#{delivery.sebSetup}">
      <h:outputText value="#{deliveryMessages.begin_assessment_msg_seb_required}" escape="false"/>
    </h:panelGroup>
    <!--  Due Message -->
    <h:panelGroup rendered="#{delivery.firstTimeTaking}">
        <h:panelGroup rendered="#{(delivery.dueDate != null && delivery.dueDate ne '')}">
            <h:outputText value="#{deliveryMessages.begin_assessment_msg_due} " escape="false"/>
            <h:outputText value="#{delivery.dueDate}">
                <f:convertDateTime dateStyle="full" timeStyle="short" timeZone="#{delivery.userTimeZone}" />
            </h:outputText>
            <h:outputText value="." />
        </h:panelGroup>

        <h:outputText value="#{deliveryMessages.begin_assessment_msg_no_due}" rendered="#{(delivery.dueDate==null || delivery.dueDate eq '')}" escape="false"/>
    </h:panelGroup>
    
	<h:outputText value="#{deliveryMessages.begin_assessment_msg_no_due}" rendered="#{(delivery.dueDate==null || delivery.dueDate eq '') && !delivery.firstTimeTaking && !delivery.hasTimeLimit}" escape="false"/>
    
    <h:outputText value=" "/>
    <h:outputText value=" "/>

    <!--  Time Limit Message -->
    <h:panelGroup rendered="#{delivery.hasTimeLimit && delivery.firstTimeTaking}">
    	<h:outputFormat value="#{deliveryMessages.begin_assessment_msg_timed_w_due_date}" escape="false" rendered="#{delivery.deadline != null && delivery.deadline != ''}">
                <f:param value="#{delivery.timeLimitString}"/>
                <f:param value="#{delivery.deadlineString}"/>
        </h:outputFormat>
        
        <h:outputFormat value="#{deliveryMessages.begin_assessment_msg_timed_wo_due_date}" escape="false" rendered="#{delivery.deadline == null}">
                <f:param value="#{delivery.timeLimitString}"/>
        </h:outputFormat>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{delivery.trackingQuestions}">
      <h:outputText value="#{deliveryMessages.begin_assessment_msg_tracking_questions}"/>
    </h:panelGroup>

    <h:panelGroup rendered="#{delivery.trackingQuestions}">
      <h:outputText value="#{deliveryMessages.begin_assessment_msg_tracking_questions_2}"/>
    </h:panelGroup>

    <h:outputFormat value="#{deliveryMessages.begin_assessment_msg_timed_continue}" escape="false" rendered="#{delivery.hasTimeLimit && !delivery.firstTimeTaking}">
                <f:param value="#{delivery.beginTimeString}"/>
                <f:param value="#{delivery.adjustedTimedAssesmentDueDateString}"/>
    </h:outputFormat>
    
    <h:outputFormat value="#{deliveryMessages.time_expired2}" escape="false" rendered="#{delivery.hasTimeLimit && !delivery.firstTimeTaking && delivery.timeExpired}" />
    
    <h:outputText value="#{deliveryMessages.begin_assessment_msg_no_time_limit}" rendered="#{!delivery.hasTimeLimit}" escape="false"/>
    
    <h:outputText value=" "/>
    <h:outputText value=" "/>

	<!--  Assessment Organization Message -->
	<h:panelGroup>
	<h:outputText value="#{deliveryMessages.begin_assessment_msg_linear}" escape="false" rendered="#{delivery.navigation == 1}"/>
	<h:outputText value=" #{deliveryMessages.begin_assessment_msg_linear_continue}" escape="false" rendered="#{delivery.navigation == 1 && !delivery.firstTimeTaking}"/>
	</h:panelGroup>

    <h:outputText value=" " rendered="#{delivery.navigation == 1}"/>
    <h:outputText value=" " rendered="#{delivery.navigation == 1}"/>
      
    <h:panelGroup>
    
    <!--  Submissions Allowed Message -->
    <h:panelGroup rendered="#{delivery.firstTimeTaking}">
    	<h:outputText value="#{deliveryMessages.begin_assessment_msg_unlimited_submission} " rendered="#{delivery.settings.unlimitedAttempts && !delivery.anonymousLogin}" escape="false"/>
    	
    	<h:outputFormat value="#{deliveryMessages.submission_allowed_1}" escape="false" rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions==0 && !delivery.anonymousLogin}">
                <f:param value="#{delivery.settings.maxAttempts}"/>
    	</h:outputFormat>
    	
    	<h:outputFormat value="#{deliveryMessages.submission_allowed_2}" escape="false" rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions!=0 && !delivery.anonymousLogin}">
                <f:param value="#{delivery.submissionsRemaining}"/>
    	</h:outputFormat>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{!delivery.firstTimeTaking}">
    
    	<h:outputText value="#{deliveryMessages.begin_assessment_msg_unlimited_submission} " rendered="#{delivery.settings.unlimitedAttempts && !delivery.anonymousLogin && (delivery.deadline == null || delivery.deadline == '')}" escape="false"/>
    	
    	<h:outputFormat value="#{deliveryMessages.begin_assessment_msg_unlimited_submission_continue}" escape="false" rendered="#{delivery.settings.unlimitedAttempts && !delivery.anonymousLogin && delivery.deadlineString != null && delivery.deadlineString != ''}">
                <f:param value="#{delivery.deadlineString}"/>
    	</h:outputFormat>
    	
    	<h:outputFormat value="#{deliveryMessages.submission_allowed_1}" escape="false" rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions==0 && !delivery.anonymousLogin && (delivery.deadlineString == null || delivery.deadlineString == '')}">
                <f:param value="#{delivery.settings.maxAttempts}"/>
    	</h:outputFormat>
    	
    	<h:outputFormat value="#{deliveryMessages.submission_allowed_2}" escape="false" rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions!=0 && !delivery.anonymousLogin && (delivery.deadlineString == null || delivery.deadlineString == '')}">
                <f:param value="#{delivery.submissionsRemaining}"/>
    	</h:outputFormat>
    	
    	<h:outputFormat value="#{deliveryMessages.submission_allowed_1_continue}" escape="false" rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions==0 && !delivery.anonymousLogin && delivery.deadlineString != null && delivery.deadlineString != ''}">
                <f:param value="#{delivery.settings.maxAttempts}"/>
                <f:param value="#{delivery.deadlineString}"/>
    	</h:outputFormat>
    	
    	<h:outputFormat value="#{deliveryMessages.submission_allowed_2_continue}" escape="false" rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions!=0 && !delivery.anonymousLogin && delivery.deadlineString != null && delivery.deadlineString != ''}">
                <f:param value="#{delivery.submissionsRemaining}"/>
                <f:param value="#{delivery.deadlineString}"/>
    	</h:outputFormat>
    </h:panelGroup>
    
	    <!--  Grading Message -->
		<h:outputText value=" #{deliveryMessages.begin_assessment_msg_highest}" rendered="#{delivery.scoringType == 1 && (delivery.settings.unlimitedAttempts || delivery.settings.maxAttempts > 1)}" escape="false"/>
		<h:outputText value=" #{deliveryMessages.begin_assessment_msg_latest}" rendered="#{delivery.scoringType == 2 && (delivery.settings.unlimitedAttempts || delivery.settings.maxAttempts > 1)}" escape="false"/>
		<h:outputText value=" #{deliveryMessages.begin_assessment_msg_average}" rendered="#{delivery.scoringType == 4 && (delivery.settings.unlimitedAttempts || delivery.settings.maxAttempts > 1)}" escape="false"/>
    </h:panelGroup>

    <%-- Autosubmit information --%>
    <h:panelGroup rendered="#{delivery.settings.autoSubmit}">
        <h:outputFormat value="#{deliveryMessages.begin_assessment_msg_autosubmit_info}" escape="false">
            <f:param value="#{delivery.deadlineString}" />
        </h:outputFormat>
    </h:panelGroup>

    <h:outputText value=" "/>
    <h:outputText value=" "/>
    
    <h:panelGroup rendered="#{delivery.recURL != null && delivery.recURL != ''}">
 	    <h:outputText value="#{deliveryMessages.please_read_1} " />
		<h:outputLink value="#{delivery.recURL}" target="_blank"><h:outputText value="#{deliveryMessages.please_read_2}"/></h:outputLink >
		<h:outputText value=" #{deliveryMessages.please_read_3}" /> 
	</h:panelGroup>
	
</h:panelGrid>
	
<h:panelGrid rendered="#{!delivery.sebSetup}" columns="2" border="0">
    <h:outputText value=" "/>
    <h:outputText value=" "/>

    <h:outputLabel for="baPassword" value="#{deliveryMessages.password}" rendered="#{delivery.settings.password ne ''}" />
    <h:inputSecret id="baPassword" value="#{delivery.password}" size="20" rendered="#{delivery.settings.password ne ''}" />
</h:panelGrid>

 </div></div>

 <h:panelGroup layout="block" styleClass="sak-banner-warn" rendered="#{!delivery.sebSetup}">
	<h:outputText value="#{deliveryMessages.begin_assessment_msg_warn_tabs}" />
 </h:panelGroup>
 
 <h:panelGroup layout="block" styleClass="honor-container" rendered="#{delivery.honorPledge && delivery.firstTimeTaking && !delivery.sebSetup}">
	<h:selectBooleanCheckbox id="honor_pledge" />
	<h:outputLabel for="honor_pledge" value="#{deliveryMessages.honor_pledge_detail}"/>
</h:panelGroup>
    <h:outputText id="honorPledgeRequired" value="#{deliveryMessages.honor_pledge_select}" styleClass="sak-banner-error" style="display:none"/>

    <!-- autosubmit - present confirmation when resubmissions can overwrite scores -->
    <h:panelGroup layout="block" styleClass="sak-banner-warn" rendered="#{delivery.totalSubmissions > 0 && delivery.settings.autoSubmit && (delivery.scoringType == 2 || delivery.scoringType == 4)}">
        <h:outputText value="#{deliveryMessages.begin_assessment_msg_attempt_autosubmit_warn_average}" rendered="#{delivery.scoringType == 4}" escape="false" />
        <h:outputText value="#{deliveryMessages.begin_assessment_msg_attempt_autosubmit_warn_last}" rendered="#{delivery.scoringType == 2}" escape="false" />
    </h:panelGroup>
    <h:panelGroup layout="block" styleClass="honor-container" rendered="#{delivery.totalSubmissions > 0 && delivery.settings.autoSubmit && (delivery.scoringType == 2 || delivery.scoringType == 4)}">
        <h:selectBooleanCheckbox id="understand_pledge" />
        <h:outputLabel for="understand_pledge" value="#{deliveryMessages.begin_assessment_msg_attempt_autosubmit_understand_checkbox}"/>
    </h:panelGroup>
    <h:outputText id="understandPledgeRequired" value="#{deliveryMessages.begin_assessment_msg_attempt_autosubmit_understand_required}" styleClass="sak-banner-error" style="display:none"/>

<p class="act">

<!-- BEGIN ASSESSMENT BUTTON -->
<!-- When previewing, we don't need to check security. When take the assessment for real, we do -->
 <h:commandButton id="beginAssessment1" value="#{deliveryMessages.begin_assessment_}"
    action="#{delivery.validate}" type="submit" styleClass="active"
    rendered="#{(delivery.actionString=='takeAssessment'
        || delivery.actionString=='takeAssessmentViaUrl')
        && delivery.navigation != 1 && delivery.firstTimeTaking
        && !delivery.sebSetup}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

 <h:commandButton id="beginAssessment2" value="#{deliveryMessages.begin_assessment_}"
    action="#{delivery.validate}" type="submit" styleClass="active"
    rendered="#{(delivery.actionString=='takeAssessment'
        || delivery.actionString=='takeAssessmentViaUrl')
        && delivery.navigation == 1 && delivery.firstTimeTaking
        && !delivery.sebSetup}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener" />
  </h:commandButton>

  <h:commandButton id="continueAssessment1" value="#{deliveryMessages.continue_assessment_}"
    action="#{delivery.validate}" type="submit" styleClass="active"
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl')
        && delivery.navigation != 1 && !delivery.firstTimeTaking && !delivery.timeExpired
        && !delivery.sebSetup}">

    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

  <h:commandButton id="continueAssessment2" value="#{deliveryMessages.continue_assessment_}"
    action="#{delivery.validate}" type="submit" styleClass="active"
    rendered="#{(delivery.actionString=='takeAssessment'
        || delivery.actionString=='takeAssessmentViaUrl')
        && delivery.navigation == 1 && !delivery.firstTimeTaking && !delivery.timeExpired
        && !delivery.sebSetup}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener" />
  </h:commandButton>

  <h:commandButton id="beginAssessment3" value="#{deliveryMessages.begin_assessment_}" action="#{delivery.pvalidate}"
      type="submit" styleClass="active" rendered="#{delivery.actionString=='previewAssessment' && !delivery.sebSetup}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

  <h:panelGroup rendered="#{delivery.sebSetup}">
    <h:panelGroup rendered="#{delivery.actionString != 'previewAssessment'}">
      <a id="sebLaunchSeb" class="active" onclick="SPNR.insertSpinnerAfter(this, ['sebDownloadSeb', 'sebDownloadConfiguration', 'takeAssessmentForm:cancel1'], null);">
        <h:outputText value="#{deliveryMessages.seb_launch_seb}"/>
      </a>
      <a id="sebDownloadConfiguration" class="button">
        <h:outputText value="#{deliveryMessages.seb_download_configuration}"/>
      </a>
    </h:panelGroup>
    <h:panelGroup rendered="#{delivery.actionString == 'previewAssessment'}">
      <a id="sebLaunchSeb" class="button disabled">
        <h:outputText value="#{deliveryMessages.seb_launch_seb}"/>
      </a>
      <a id="sebDownloadConfiguration" class="button disabled">
        <h:outputText value="#{deliveryMessages.seb_download_configuration}"/>
      </a>
    </h:panelGroup>

  </h:panelGroup>

  <h:commandButton id="resetViewHidden" styleClass="hidden" value="begin" action="takeAssessmentViaUrl" type="submit"
      rendered="#{delivery.sebSetup && delivery.actionString == 'takeAssessmentViaUrl'}" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.SebSetupActionListener" />
    <f:param name="sebSetup" value="false" />
  </h:commandButton>

<!-- CANCEL BUTTON -->
  <h:commandButton id="cancel1" value="#{commonMessages.cancel_action}" action="select" type="submit"
      rendered="#{delivery.actionString=='previewAssessment'
      || delivery.actionString=='takeAssessment'}"
      disabled="#{delivery.actionString=='previewAssessment'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
  </h:commandButton>

</p>

</h:form>

  <h:panelGroup rendered="#{delivery.sebSetup}">
    <h:panelGroup rendered="#{delivery.actionString != 'previewAssessment'}">
      <h:outputLink id="sebDownloadSeb" value="#{delivery.sebDownloadLink}" target="_blank">
        <h:outputText value="#{deliveryMessages.seb_download_seb}"/>
      </h:outputLink >
    </h:panelGroup>
  </h:panelGroup>
  <!-- end content -->
  </div>
</div>
      </body>
    </html>
  </f:view>
