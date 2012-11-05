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
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>; isMacintosh_Netscape();">
 <!--h:outputText value="<body #{delivery.settings.bgcolor} #{delivery.settings.background}>" escape="false" /-->
<!--div class="portletBody"-->
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>


<%@ include file="/js/delivery.js" %>
<!--JAVASCRIPT -->
<script type="text/JavaScript">
<%@ include file="/js/browser.js" %>
</script>

 <!-- content... -->
<h:form id="takeAssessmentForm">
<h:inputHidden id="isMacNetscapeBrowser" value="#{person.isMacNetscapeBrowser}" />
<h:inputHidden id="timerId" value="#{delivery.timerId}" rendered="#{delivery.timerId!=null}" />

<!-- DONE BUTTON FOR PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="previewMessage"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h3><h:outputText value="#{deliveryMessages.begin_assessment_}" /></h3>
<div class="tier1">
 <h4> <h:outputText value="\"#{delivery.assessmentTitle}\" #{deliveryMessages.for} #{delivery.courseName} " escape="false"/></h4>
<div class="tier2">
<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
 
<!-- ASSESSMENT INTRODUCTION-->
<div class="assessmentIntroduction"><h:outputText value="#{delivery.instructorMessage}" escape="false"/></div>
   
  <!-- ASSESSMENT ATTACHMENTS -->
  <%@ include file="/jsf/delivery/assessment_attachment.jsp" %>

<h:panelGrid columns="1" border="0">
	<h:outputText value="#{deliveryMessages.begin_assessment_msg_due} <b>#{delivery.dayDueDateString}</b>." rendered="#{delivery.dueDate!=null && delivery.dueDate ne ''}" escape="false"/>
    <h:outputText value="#{deliveryMessages.begin_assessment_msg_no_due}" rendered="#{delivery.dueDate==null || delivery.dueDate eq ''}" escape="false"/>
    
    <h:outputText value=" "/>
    <h:outputText value=" "/>
    
    <h:panelGroup rendered="#{delivery.hasTimeLimit}">
	    <h:outputText value="#{deliveryMessages.begin_assessment_msg_timed_1}" escape="false"/>
    	<h:outputText value="<b>#{delivery.timeLimit_hour} #{deliveryMessages.time_limit_hour} #{delivery.timeLimit_minute} #{deliveryMessages.time_limit_minute}</b> " escape="false"/>
		<h:outputText value="#{deliveryMessages.begin_assessment_msg_timed_2}" escape="false"/>
    </h:panelGroup>
    <h:outputText value="#{deliveryMessages.begin_assessment_msg_no_time_limit}" rendered="#{!delivery.hasTimeLimit}" escape="false"/>
     
    <h:outputText value=" "/>
    <h:outputText value=" "/>
    
    <h:panelGrid rendered="#{delivery.navigation == 1}">  
	    <h:outputText value="#{deliveryMessages.begin_assessment_msg_linear}" escape="false"/>
    	<h:outputText value=" "/>
    	<h:outputText value=" "/>
    </h:panelGrid>
      
    <h:panelGroup>
	    <h:outputText value="#{deliveryMessages.begin_assessment_msg_unlimited_submission} " rendered="#{delivery.settings.unlimitedAttempts && !delivery.anonymousLogin}" escape="false"/>
		<h:outputText value="#{deliveryMessages.submission_allowed_1} #{delivery.settings.maxAttempts} #{deliveryMessages.submission_allowed_2} " rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions==0 && !delivery.anonymousLogin}" escape="false"/>
		<h:outputText value="#{deliveryMessages.submission_allowed_1} #{delivery.submissionsRemaining} #{deliveryMessages.submission_allowed_3} " rendered="#{!delivery.settings.unlimitedAttempts && delivery.totalSubmissions!=0 && !delivery.anonymousLogin}" escape="false"/>
		
		<h:outputText value="#{deliveryMessages.begin_assessment_msg_highest}" rendered="#{delivery.scoringType == 1}" escape="false"/>
		<h:outputText value="#{deliveryMessages.begin_assessment_msg_latest}" rendered="#{delivery.scoringType == 2}" escape="false"/>
		<h:outputText value="#{deliveryMessages.begin_assessment_msg_average}" rendered="#{delivery.scoringType == 4}" escape="false"/>
    </h:panelGroup>
    
    <h:outputText value=" "/>
    <h:outputText value=" "/>
    
    <h:panelGroup>
      <h:outputText value="#{deliveryMessages.begin_assessment_msg_feedback_upon_submission}" rendered="#{delivery.feedbackComponent.showOnSubmission}"/>
      <h:outputText value="#{deliveryMessages.begin_assessment_msg_feedback_during_assessment}" rendered="#{delivery.feedbackComponent.showImmediate}"/>
      <h:panelGroup rendered="#{delivery.feedbackComponent.showDateFeedback}">
      	<h:outputText value="#{deliveryMessages.begin_assessment_msg_feedback_provide_on} " />
      	<h:outputText value="#{delivery.settings.feedbackDate}">
        	<f:convertDateTime pattern="#{generalMessages.output_date_no_sec}"/>
      	</h:outputText>
      	<h:outputText value="."/>
      </h:panelGroup>
      <h:outputText value="#{deliveryMessages.begin_assessment_msg_feedback_no}" rendered="#{delivery.feedbackComponent.showNoFeedback}"/>
    </h:panelGroup>
</h:panelGrid>
	
<h:panelGrid columns="2" border="0">
    <h:outputText value=" "/>
    <h:outputText value=" "/>
    <h:outputLabel for="baUserName"  value="#{deliveryMessages.username}" rendered="#{delivery.settings.username ne ''}" />
    <h:inputText id="baUserName" value="#{delivery.username}" size="20" rendered="#{delivery.settings.username ne ''}" />

    <h:outputLabel for="baPassword" value="#{deliveryMessages.password}" rendered="#{delivery.settings.username ne ''}" />
    <h:inputSecret id="baPassword" value="#{delivery.password}" size="20" rendered="#{delivery.settings.username ne ''}" />
</h:panelGrid>
 </div></div>

<p class="act">

<!-- BEGIN ASSESSMENT BUTTON -->
<!-- When previewing, we don't need to check security. When take the assessment for real, we do -->
 <h:commandButton id="beginAssessment1" value="#{deliveryMessages.begin_assessment_}" 
    action="#{delivery.validate}" type="submit" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl')
			 && delivery.navigation != 1}"
	onclick="disableBeginAssessment1();">
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

 <h:commandButton id="beginAssessment2" value="#{deliveryMessages.begin_assessment_}" 
    action="#{delivery.validate}" type="submit" styleClass="active" 
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl')
			 && delivery.navigation == 1}"
	onclick="disableBeginAssessment2();">
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener" />
  </h:commandButton>

 <h:commandButton id="beginAssessment3" value="#{deliveryMessages.begin_assessment_}" action="#{delivery.pvalidate}" type="submit" styleClass="active" rendered="#{delivery.actionString=='previewAssessment'}" onclick="disableBeginAssessment3();">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>


<!-- CANCEL BUTTON -->
  <h:commandButton id="cancel1" value="#{commonMessages.cancel_action}"  action="select" type="submit"
     rendered="#{delivery.actionString=='previewAssessment'
             || delivery.actionString=='takeAssessment'}"
     disabled="#{delivery.actionString=='previewAssessment'}" onclick="disableCancel1();">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
  </h:commandButton>

</p>

<!-- DONE BUTTON, FOR PREVIEW ONLY --> 
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="previewMessage"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>

