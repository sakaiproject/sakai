<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

	<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
	%>
	
	
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{assessmentSettingsMessages.sakai_assessment_manager} #{assessmentSettingsMessages.dash} #{assessmentSettingsMessages.settings}" /></title>
      <script type="text/javascript" src="../../js/extendedTime.js"/>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      <samigo:script path="/jsf/widget/colorpicker/colorpicker.js"/>
      <script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>
      <samigo:script path="/js/authoring.js"/>        

      <script type="text/javascript">
        $(document).ready(function() {
          // set up the accordion for settings
          $("#jqueryui-accordion").accordion({ heightStyle: "content", collapsible: true, active: 1 });
          // This is a sub-accordion inside of the About the Assessment Panel
          $("#jqueryui-accordion-metadata").accordion({ heightStyle: "content",collapsible: true,active: false });
          // This is a sub-accordion inside of the Availability and Submission Panel
          $("#jqueryui-accordion-security").accordion({ heightStyle: "content",collapsible: true,active: false });
          // adjust the height of the iframe to accomodate the expansion from the accordion
          $("body").height($("body").outerHeight() + 900);

          // SAM-2323 jquery-UI datepicker
          localDatePicker({
              input: '#assessmentSettingsAction\\:startDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              val: '<h:outputText value="#{publishedSettings.startDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'startDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:endDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              val: '<h:outputText value="#{publishedSettings.dueDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'endDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:retractDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              val: '<h:outputText value="#{publishedSettings.retractDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'retractDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:feedbackDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              val: '<h:outputText value="#{publishedSettings.feedbackDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'feedbackDateISO8601' }
          });

          // SAM-2121: Lockdown the question layout and mark for review if necessary
          var navVal = $('#assessmentSettingsAction\\:itemNavigation input:radio:checked').val();
          lockdownQuestionLayout(navVal);
          lockdownMarkForReview(navVal);
          showHideReleaseGroups();
          initTimedCheckBox();
          extendedTimeInitialize();
          checkUncheckTimeBox();
          checkLastHandling();
        });
		function expandAccordion(iframId){
			$('.ui-accordion-content').show();
			mySetMainFrameHeight(iframId);
			$("#collapseLink").show();
			$("#expandLink").hide();
		}

		function collapseAccordion(iframId){
			$('.ui-accordion-content').hide();
			mySetMainFrameHeight(iframId);
			$("#collapseLink").hide();
			$("#expandLink").show();
		}
      </script>

      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">

<!-- content... -->
<h:form id="assessmentSettingsAction" onsubmit="return editorCheck();">
  <h:inputHidden id="assessmentId" value="#{publishedSettings.assessmentId}"/>
  <h:inputHidden id="blockDivs" value="#{publishedSettings.blockDivs}"/>
  <h:inputHidden id="itemNavigationUpdated" value="false" />
  
  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>

<p>
  <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
  </p>
<p>
  <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
</p>

<span>
    <h3>
     <h:outputText id="x1" value="#{assessmentSettingsMessages.settings} #{assessmentSettingsMessages.dash} #{publishedSettings.title}"/>
    </h3>
    <f:verbatim>
	<span style="float: right">
		<a href="javascript:void(0)" id="expandLink" onclick="expandAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
			</f:verbatim>
				<h:outputText value="#{assessmentSettingsMessages.expandAll}"/>
			<f:verbatim>
		</a>
		<a href="javascript:void(0)" id="collapseLink" style="display:none" onclick="collapseAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
			</f:verbatim>
				<h:outputText value="#{assessmentSettingsMessages.collapseAll}"/>
			<f:verbatim>
		</a>
	</span>
	</f:verbatim>
	<br/>
</span>
<div class="tier1" id="jqueryui-accordion">

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_about}" >


  <!-- *** ASSESSMENT INTRODUCTION *** -->
  <h:outputLabel escape="false" value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_assessment_introduction} </h4>" />
  <div class="tier2" id="assessment-intro">
    <h:panelGrid columns="2" columnClasses="samigoCell" id="first" summary="#{templateMessages.enter_template_info_section}">

        <h:outputLabel for="assessment_title" value="#{assessmentSettingsMessages.assessment_title}"/>
        <h:inputText id="assessment_title" size="80" maxlength="255" value="#{publishedSettings.title}" />

        <h:outputLabel value="#{assessmentSettingsMessages.published_assessment_url}: " />
        <h:outputText value="#{publishedSettings.publishedUrl}" />

        <h:outputLabel value="#{assessmentSettingsMessages.assessment_creator}"  rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>

        <h:outputText value="#{publishedSettings.creator}"  rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>

        <h:outputLabel for="assessment_author" rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}" value="#{assessmentSettingsMessages.assessment_authors}"/>

        <h:inputText id="assessment_author" size="80" maxlength="255" value="#{publishedSettings.authors}" rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>

        <h:outputLabel value="#{assessmentSettingsMessages.assessment_description}" rendered="#{publishedSettings.valueMap.description_isInstructorEditable==true}"/>

        <h:panelGrid rendered="#{publishedSettings.valueMap.description_isInstructorEditable==true}">
           <samigo:wysiwyg rows="100" columns="400" value="#{assessmentSettings.description}" hasToggle="yes" mode="author" >
           <f:validateLength maximum="4000"/>
         </samigo:wysiwyg>
        </h:panelGrid>

       <!-- ASSESSMENT ATTACHMENTS -->
       <h:panelGroup>
         <h:panelGrid columns="1">
           <%@ include file="/jsf/author/publishedSettings_attachment.jsp" %>
         </h:panelGrid>
       </h:panelGroup>
       <h:outputText value=""/>
       
       <!-- Honor Pledge -->
		<h:outputLabel value="#{assessmentSettingsMessages.honor_pledge}"/>
		<h:panelGroup>
			<h:selectBooleanCheckbox id="honor_pledge" value="#{publishedSettings.valueMap.honorpledge_isInstructorEditable}"/>
			<h:outputText value="#{assessmentSettingsMessages.honor_pledge_add}"/>
		</h:panelGroup>

    </h:panelGrid>
  <f:verbatim></div></f:verbatim>
  
<f:verbatim><div id="jqueryui-accordion-metadata"></f:verbatim><!-- This is sub-accordion for metadata -->  

 <!-- *** META *** -->
<h:panelGroup rendered="#{publishedSettings.valueMap.metadataAssess_isInstructorEditable==true}">
  <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_metadata} </a> </h3>" /> 
   <f:verbatim><div class="tier2"></f:verbatim>
   <f:verbatim><div class="samigo-subheading"></f:verbatim> <h:outputLabel value="#{assessmentSettingsMessages.assessment_metadata}" /> <f:verbatim></div><div class="tier3"></f:verbatim>
    <h:panelGrid columns="2" columnClasses="samigoCell">
      <h:outputLabel for="keywords" value="#{assessmentSettingsMessages.metadata_keywords}"/>
      <h:inputText id="keywords" size="80" value="#{publishedSettings.keywords}"/>

    <h:outputLabel for="objectives" value="#{assessmentSettingsMessages.metadata_objectives}"/>
      <h:inputText id="objectives" size="80" value="#{publishedSettings.objectives}"/>

      <h:outputLabel for="rubrics" value="#{assessmentSettingsMessages.metadata_rubrics}"/>
      <h:inputText id="rubrics" size="80" value="#{publishedSettings.rubrics}"/>
    </h:panelGrid>
   <f:verbatim></div><div class="samigo-subheading"></f:verbatim>   <h:outputLabel value="#{assessmentSettingsMessages.record_metadata}" /> <f:verbatim></div><div class="tier3"></f:verbatim>
    <h:panelGrid columns="2"  >
     <h:selectBooleanCheckbox
       rendered="#{publishedSettings.valueMap.metadataQuestions_isInstructorEditable==true}"
       value="#{publishedSettings.valueMap.hasMetaDataForQuestions}"/>
     <h:outputText value="#{assessmentSettingsMessages.metadata_questions}"
       rendered="#{publishedSettings.valueMap.metadataQuestions_isInstructorEditable==true}" />
    </h:panelGrid>
    <f:verbatim></div></div></f:verbatim>
  </h:panelGroup>

<f:verbatim></div></f:verbatim><!-- This is the end of the sub-accordion -->

</samigo:hideDivision><!-- End the About this Assessment category -->

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_availability}"> 

  <h:outputLabel escape="false" value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_released_to} </h4>" />
  <!-- *** RELEASED TO *** -->
  <h:panelGroup>
    <h:outputText value="#{assessmentSettingsMessages.released_to} " />
    <h:selectOneMenu id="releaseTo" disabled="true" value="#{publishedSettings.firstTargetSelected}" >
      <f:selectItems value="#{assessmentSettings.publishingTargets}" />
    </h:selectOneMenu>
  </h:panelGroup>

  <f:verbatim><div id="groupDiv" class="groupTable"></f:verbatim>
  <f:verbatim><table border="0" bgcolor="#CCCCCC"><tr><td></f:verbatim>  
  <h:selectBooleanCheckbox id="checkUncheckAllReleaseGroups" disabled="true" />
      
  <f:verbatim></td><td></f:verbatim>
  <h:outputText value="#{assessmentSettingsMessages.select_all_groups}" />
  <f:verbatim></td></tr></table></f:verbatim>
  
  <h:selectManyCheckbox id="groupsForSite" disabled="true"  layout="pagedirection" value="#{publishedSettings.groupsAuthorized}">
    <f:selectItems value="#{publishedSettings.groupsForSite}" />
  </h:selectManyCheckbox>
  <f:verbatim></div></f:verbatim>
  
  <!-- Extended Time -->
  <%@ include file="inc/publishedExtendedTime.jspf"%>

    <!-- NUMBER OF SUBMISSIONS -->
  <h:panelGrid columns="2" columnClasses="alignTop" border="0" rendered="#{publishedSettings.valueMap.submissionModel_isInstructorEditable==true}">
    <h:outputText style="position: relative; top: 7px;" value="#{assessmentSettingsMessages.submissions_allowed}" />
    <h:panelGrid columns="3" border="0" columnClasses="alignBottom">
      <h:selectOneRadio id="unlimitedSubmissions" value="#{publishedSettings.unlimitedSubmissions}" layout="pageDirection">
        <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.unlimited_submission}"/>
        <f:selectItem itemValue="0" itemLabel="#{assessmentSettingsMessages.only}" />
      </h:selectOneRadio>

	  <h:panelGroup>
        <h:inputText size="5"  id="submissions_Allowed" value="#{publishedSettings.submissionsAllowed}" />
        <h:outputLabel for="submissions_Allowed" value="#{assessmentSettingsMessages.limited_submission}" />
      </h:panelGroup>
    </h:panelGrid> 
  </h:panelGrid>
    
  <!-- *** DELIVERY DATES *** -->
  <h:panelGrid columns="1" columnClasses="samigoCell" border="0">
    <h:panelGroup>
      <h:outputLabel for="startDate" value="#{assessmentSettingsMessages.assessment_available}"/>
      <h:inputText value="#{publishedSettings.startDateString}" size="25" id="startDate" />
	  <h:outputText value="" />
	  <h:outputText value="" />
	  
      <h:outputLabel for="endDate" value="#{assessmentSettingsMessages.assessment_due}" />
      <h:inputText value="#{publishedSettings.dueDateString}" size="25" id="endDate"/>
	  <h:outputText value="" />
	  <h:outputText value="" />
  
  <!-- *** TIMED *** -->
      <h:panelGroup rendered="#{publishedSettings.valueMap.timedAssessment_isInstructorEditable==true}" >
        <h:outputText value="#{assessmentSettingsMessages.has_time_limit} " />
        <h:selectBooleanCheckbox id="selTimeAssess" onclick="checkUncheckTimeBox();setBlockDivs();" value="#{publishedSettings.valueMap.hasTimeAssessment}" />
        <h:selectOneMenu id="timedHours" value="#{publishedSettings.timedHours}" >
          <f:selectItems value="#{publishedSettings.hours}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_hours} " />
        <h:selectOneMenu id="timedMinutes" value="#{publishedSettings.timedMinutes}">
          <f:selectItems value="#{publishedSettings.mins}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_minutes} " />
        <f:verbatim><br/></f:verbatim>
      </h:panelGroup>
    </h:panelGroup>
  </h:panelGrid>
  
  <!-- LATE HANDLING -->
  <h:panelGrid columns="1" rendered="#{publishedSettings.valueMap.lateHandling_isInstructorEditable==true}" border="0">
    <h:outputText value="#{assessmentSettingsMessages.late_accept}" />
    <h:panelGrid columns="4" border="0" columnClasses="alignBottom">
      <f:verbatim>&nbsp;&nbsp;</f:verbatim>
      <h:selectOneRadio id="lateHandling" onclick="checkLastHandling();" value="#{publishedSettings.lateHandling}"  layout="pageDirection">
        <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.no_late}"/>
        <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.yes_late}"/>
      </h:selectOneRadio>

	  <h:inputText value="#{publishedSettings.retractDateString}" size="25" id="retractDate"/>
	  
	  <h:commandButton type="submit" value="#{assessmentSettingsMessages.button_stop_accepting_now}" action="confirmAssessmentRetract"  styleClass="active" />
    </h:panelGrid>
  </h:panelGrid>
  
  <!-- AUTOMATIC SUBMISSION -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.automaticSubmission_isInstructorEditable==true}">
    <h:selectBooleanCheckbox id="automaticSubmission" value="#{publishedSettings.autoSubmit}"/>
    <h:outputLabel value="#{assessmentSettingsMessages.auto_submit}"/>
  </h:panelGroup>

    <!-- SUBMISSION EMAILS -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.submissionModel_isInstructorEditable==true}">
        <h:outputLabel value="#{assessmentSettingsMessages.instructorNotification}" />
        <f:verbatim><div class="tier1"></f:verbatim>
        <h:selectOneRadio id="notificationEmailChoices" value="#{publishedSettings.instructorNotification}" layout="pageDirection">
            <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.oneEmail}" />
            <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.digestEmail}" />
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.noEmail}" />
        </h:selectOneRadio>
        <f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- Display Scores -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.displayScores_isInstructorEditable==true}">
      <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel for="displayScores" value="#{assessmentSettingsMessages.displayScores}" /> <f:verbatim> </div><div class="tier3"> </f:verbatim>
        <h:panelGrid columns="2"  >
          <h:selectOneRadio id="displayScores" value="#{publishedSettings.displayScoreDuringAssessments}"  layout="pageDirection">
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.displayScores_show}"/>
            <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.displayScores_hide}"/>
          </h:selectOneRadio>
       </h:panelGrid>
       <f:verbatim></div></f:verbatim>
    </h:panelGroup>

<f:verbatim><div id="jqueryui-accordion-security"></f:verbatim><!-- This is sub-accordion for high security and submission message -->

  <!-- *** HIGH SECURITY *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.ipAccessType_isInstructorEditable==true or publishedSettings.valueMap.passwordRequired_isInstructorEditable==true or publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true}" >
    <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_high_security} </a> </h3><div>" />
    <h:panelGrid border="0" columns="2" summary="#{templateMessages.high_security_sec}">
      <h:outputText value="#{assessmentSettingsMessages.high_security_allow_only_specified_ip}" rendered="#{publishedSettings.valueMap.ipAccessType_isInstructorEditable==true}"/>
      <%-- no WYSIWYG for IP addresses --%>
	  <h:panelGroup rendered="#{publishedSettings.valueMap.ipAccessType_isInstructorEditable==true}">
      <h:inputTextarea value="#{publishedSettings.ipAddresses}" cols="40" rows="5"/>
 	  <h:outputText escape="false" value="<br/>#{assessmentSettingsMessages.ip_note} <br/>#{assessmentSettingsMessages.ip_example}#{assessmentSettingsMessages.ip_ex}<br/>"/> 
      </h:panelGroup>
      <h:outputText value="#{assessmentSettingsMessages.high_security_secondary_id_pw}" rendered="#{publishedSettings.valueMap.passwordRequired_isInstructorEditable==true}"/>
      <h:panelGrid border="0" columns="2"  columnClasses="samigo-security" rendered="#{publishedSettings.valueMap.passwordRequired_isInstructorEditable==true}">
        <h:outputLabel for="username" value="#{assessmentSettingsMessages.high_security_username}"/>
        <h:inputText id="username" size="20" value="#{publishedSettings.username}"/>
        <h:outputLabel for="password" value="#{assessmentSettingsMessages.high_security_password}"/>
        <h:inputText id="password" size="20" value="#{publishedSettings.password}"/>
      </h:panelGrid>

	  <h:outputText value="#{assessmentSettingsMessages.require_secure_delivery}" rendered="#{publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true && publishedSettings.secureDeliveryAvailable}"/>
	  <h:panelGrid border="0" columns="1"  columnClasses="samigo-security" rendered="#{publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true && publishedSettings.secureDeliveryAvailable}">
	    <h:selectOneRadio id="secureDeliveryModule" value="#{publishedSettings.secureDeliveryModule}"  layout="pageDirection" onclick="setBlockDivs();">
			<f:selectItems value="#{publishedSettings.secureDeliveryModuleSelections}" />
		</h:selectOneRadio>
		<h:panelGrid border="0" columns="2"  columnClasses="samigo-security" rendered="#{publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true && publishedSettings.secureDeliveryAvailable}">	
		   <h:outputLabel for="secureDeliveryModuleExitPassword" value="#{assessmentSettingsMessages.secure_delivery_exit_pwd}"/>
		   <h:inputText id="secureDeliveryModuleExitPassword" size="20" value="#{publishedSettings.secureDeliveryModuleExitPassword}" disabled="#{publishedSettings.secureDeliveryModule == 'SECURE_DELIVERY_NONE_ID'}" maxlength="14" />      	
		</h:panelGrid>
	  </h:panelGrid>
    </h:panelGrid>
    <f:verbatim></div></f:verbatim>
  </h:panelGroup>

  <!-- *** SUBMISSION MESSAGE *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.submissionMessage_isInstructorEditable==true or publishedSettings.valueMap.finalPageURL_isInstructorEditable==true}" >
   <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_submission_message} </a> </h3><div>" />
    <h:panelGrid rendered="#{publishedSettings.valueMap.submissionMessage_isInstructorEditable==true}">
    <f:verbatim><div class="samigo-submission-message"></f:verbatim> <h:outputLabel value="#{assessmentSettingsMessages.submission_message}" /> <f:verbatim><br/></f:verbatim>
        <samigo:wysiwyg rows="140" value="#{publishedSettings.submissionMessage}" hasToggle="yes" mode="author" >
         <f:validateLength maximum="4000"/>
        </samigo:wysiwyg>
       <f:verbatim></div></f:verbatim>
	</h:panelGrid>
	 <f:verbatim><br/></f:verbatim>
      <h:panelGroup rendered="#{publishedSettings.valueMap.finalPageURL_isInstructorEditable==true}">
     <f:verbatim><div class="samigo-submission-message"></f:verbatim> <h:outputLabel for="finalPageUrl" value="#{assessmentSettingsMessages.submission_final_page_url}" /> <f:verbatim><br/></f:verbatim>
      <h:inputText size="80" id="finalPageUrl" value="#{publishedSettings.finalPageUrl}" />
      <h:commandButton value="#{assessmentSettingsMessages.validateURL}" type="button" onclick="javascript:validateUrl();"/>
   <f:verbatim></div></f:verbatim>
      </h:panelGroup>
    <f:verbatim></div></f:verbatim>
</h:panelGroup>

<f:verbatim></div></f:verbatim><!-- This is the end of the sub-accordion -->

</samigo:hideDivision><!-- END the Availabity and Submissions category -->

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_grading_feedback}" >

  <!-- *** GRADING *** -->
  <f:verbatim><h4 class="samigo-category-subhead-2"></f:verbatim>
  <h:outputText escape="false" value="#{commonMessages.grading}"/>
  <f:verbatim></h4></f:verbatim>  
  <f:verbatim><div class="tier3"></f:verbatim>
  <!-- RECORDED SCORE AND MULTIPLES -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.recordedScore_isInstructorEditable==true}">
      <h:panelGrid columns="2"  >
       <h:outputText value="#{assessmentSettingsMessages.recorded_score} " />
        <h:selectOneMenu value="#{publishedSettings.scoringType}" id="scoringType1" rendered="#{author.canRecordAverage}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
          <f:selectItem itemValue="4" itemLabel="#{assessmentSettingsMessages.average_score}"/>
        </h:selectOneMenu>
        <h:selectOneMenu value="#{publishedSettings.scoringType}" id="scoringType2" rendered="#{!author.canRecordAverage}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
        </h:selectOneMenu>
      </h:panelGrid>
    </h:panelGroup>
  
    <!--  ANONYMOUS OPTION -->  
    <h:panelGroup rendered="#{publishedSettings.valueMap.testeeIdentity_isInstructorEditable==true}"> 
      <h:selectBooleanCheckbox value="#{publishedSettings.anonymousGrading}" disabled="#{publishedSettings.firstTargetSelected == 'Anonymous Users' || publishedSettings.editPubAnonyGradingRestricted}"/>
      <h:outputLabel value="#{assessmentSettingsMessages.student_identity}"/>
    </h:panelGroup>
    
    <f:verbatim><br /></f:verbatim>
    
    <!-- GRADEBOOK OPTION -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.toGradebook_isInstructorEditable==true && publishedSettings.gradebookExists==true}">
      <h:selectBooleanCheckbox value="#{publishedSettings.toDefaultGradebook}" disabled="#{publishedSettings.firstTargetSelected == 'Anonymous Users'}"/>
      <h:outputLabel value="#{assessmentSettingsMessages.gradebook_options}"/>
    </h:panelGroup>
    <f:verbatim></div></f:verbatim>
    <f:verbatim><br /></f:verbatim>

 <!-- *** FEEDBACK *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true or publishedSettings.valueMap.feedbackType_isInstructorEditable==true or publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}" >
  <f:verbatim><h4 class="samigo-category-subhead-2"></f:verbatim>
  <h:outputText escape="false" value="#{assessmentSettingsMessages.heading_feedback}"/>
  <f:verbatim></h4></f:verbatim>
  <f:verbatim><div class="tier3"></f:verbatim>
  <!-- FEEDBACK AUTHORING -->
  <h:outputText escape="false" value="<b>#{assessmentSettingsMessages.feedback_authoring}</b>"/>
  <h:panelGrid columns="1" border="0" rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true}">
         <h:outputLabel for="feedbackAuthoring" value="#{assessmentSettingsMessages.feedback_level}"/>
         <h:selectOneRadio id="feedbackAuthoring" value="#{publishedSettings.feedbackAuthoring}" layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{commonMessages.question_level_feedback}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.sectionlevel_feedback}"/>
           <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.both_feedback}"/>
         </h:selectOneRadio>
  </h:panelGrid>

 <!-- FEEDBACK DELIVERY -->
 <h:outputText escape="false" value="<b>#{assessmentSettingsMessages.feedback_student}</b>"/>
 <h:panelGrid columns="2" border="0" rendered="#{publishedSettings.valueMap.feedbackType_isInstructorEditable==true}" columnClasses="feedbackColumn1,feedbackColumn2">
    <h:panelGroup>
        <h:outputLabel for="feedbackDelivery" value="#{assessmentSettingsMessages.feedback_type}"/>
        <h:selectOneRadio id="feedbackDelivery" value="#{publishedSettings.feedbackDelivery}" onclick="setBlockDivs();disableAllFeedbackCheck(this.value);" layout="pageDirection">
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.no_feedback}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.immediate_feedback}"/>
          <f:selectItem itemValue="4" itemLabel="#{commonMessages.feedback_on_submission}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.feedback_by_date}"/>
        </h:selectOneRadio>
    </h:panelGroup>
    <h:panelGroup>
        <h:inputText value="#{publishedSettings.feedbackDateString}" size="25" id="feedbackDate" />
    </h:panelGroup>
  </h:panelGrid>

    <!-- FEEDBACK COMPONENTS -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}">
       <h:panelGrid columns="1">
        <h:outputLabel for="feedbackComponentOption" value="#{assessmentSettingsMessages.feedback_components}"/>
        <h:selectOneRadio id="feedbackComponentOption" value="#{publishedSettings.feedbackComponentOption}" onclick="setBlockDivs();disableOtherFeedbackComponentOption(this);"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{templateMessages.feedback_components_totalscore_only}"/>
          <f:selectItem itemValue="2" itemLabel="#{templateMessages.feedback_components_select}"/>
        </h:selectOneRadio>
      </h:panelGrid>
  
      <f:verbatim> <div class="tier3 respChoice"></f:verbatim>
      <h:panelGrid columns="1">
       <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox11" value="#{publishedSettings.showStudentResponse}"/>
          <h:outputLabel for="feedbackCheckbox11" value="#{commonMessages.student_response}" />
        </h:panelGroup>
       <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox13" value="#{publishedSettings.showCorrectResponse}"/>
          <h:outputLabel for="feedbackCheckbox13" value="#{commonMessages.correct_response}" />
       </h:panelGroup>
       <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox12" value="#{publishedSettings.showQuestionLevelFeedback}"/>
          <h:outputLabel for="feedbackCheckbox12" value="#{commonMessages.question_level_feedback}" />
       </h:panelGroup>
       <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox14" value="#{publishedSettings.showSelectionLevelFeedback}"/>
          <h:outputLabel for="feedbackCheckbox14" value="#{commonMessages.selection_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox16" value="#{publishedSettings.showGraderComments}"/>
          <h:outputLabel for="feedbackCheckbox16" value="#{assessmentSettingsMessages.grader_comments}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox17" value="#{publishedSettings.showStudentQuestionScore}"/>
          <h:outputLabel for="feedbackCheckbox17" value="#{assessmentSettingsMessages.student_question_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox15" value="#{publishedSettings.showStudentScore}"/>
          <h:outputLabel for="feedbackCheckbox15" value="#{assessmentSettingsMessages.student_assessment_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox18" value="#{publishedSettings.showStatistics}"/>
          <h:outputLabel for="feedbackCheckbox18" value="#{commonMessages.statistics_and_histogram}" />
        </h:panelGroup>
      </h:panelGrid>
      <f:verbatim></div></f:verbatim>
    </h:panelGroup>
	<f:verbatim></div></f:verbatim>
  </h:panelGroup>

  </samigo:hideDivision>

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_layout}" >

  <!-- *** ASSESSMENT ORGANIZATION *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.itemAccessType_isInstructorEditable==true or publishedSettings.valueMap.displayChunking_isInstructorEditable==true or publishedSettings.valueMap.displayNumbering_isInstructorEditable==true }" >
  <f:verbatim> <div class="tier2"></f:verbatim>
  <!-- NAVIGATION -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.itemAccessType_isInstructorEditable==true}">
  <f:verbatim> <h4 class="samigo-category-subhead"></f:verbatim> <h:outputLabel for="itemNavigation" value="#{assessmentSettingsMessages.navigation}" /><f:verbatim></h4><div class="tier3"></f:verbatim>
    <h:panelGrid columns="1">
      <h:selectOneRadio id="itemNavigation" value="#{publishedSettings.itemNavigation}"  layout="pageDirection" onclick="setBlockDivs();updateItemNavigation(true);lockdownQuestionLayout(this.value);lockdownMarkForReview(this.value);">
        <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.linear_access}"/>
        <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.random_access}"/>
      </h:selectOneRadio>
      <h:panelGroup>
        <f:verbatim> <div class="samigo-linear-access-warning"></f:verbatim>
        <h:outputText value="#{assessmentSettingsMessages.linear_access_warning} "/>
        <f:verbatim> </div></f:verbatim>
        </h:panelGroup>
    </h:panelGrid>
<f:verbatim></div></f:verbatim>
  </h:panelGroup>
    
    <!-- QUESTION LAYOUT -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.displayChunking_isInstructorEditable==true}">
    <f:verbatim><h4 class="samigo-category-subhead"></f:verbatim><h:outputLabel for="assessmentFormat" value="#{assessmentSettingsMessages.question_layout}" /><f:verbatim></h4><div class="tier3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="assessmentFormat" value="#{publishedSettings.assessmentFormat}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.layout_by_question}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.layout_by_part}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.layout_by_assessment}"/>
        </h:selectOneRadio>
	 </h:panelGrid>
    <f:verbatim></div></f:verbatim>
  </h:panelGroup>

    <!-- NUMBERING -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.displayNumbering_isInstructorEditable==true}">
     <f:verbatim><h4 class="samigo-category-subhead"></f:verbatim> <h:outputLabel for="itemNumbering" value="#{assessmentSettingsMessages.numbering}" /> <f:verbatim> </h4><div class="tier3"> </f:verbatim>
       <h:panelGrid columns="2"  >
         <h:selectOneRadio id="itemNumbering" value="#{publishedSettings.itemNumbering}"  layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.continous_numbering}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.part_numbering}"/>
         </h:selectOneRadio>
      </h:panelGrid>
    <f:verbatim></div></f:verbatim>
  </h:panelGroup>
  <f:verbatim></div></f:verbatim>
</h:panelGroup>

<!-- *** MARK FOR REVIEW *** -->
<!-- *** (disabled for linear assessment) *** -->
<h:panelGroup rendered="#{publishedSettings.valueMap.markForReview_isInstructorEditable==true}">
    <f:verbatim><div class="tier2"></f:verbatim>
    <h:panelGrid columns="1">
      <h:panelGroup>
        <h:selectBooleanCheckbox id="markForReview1" value="#{publishedSettings.isMarkForReview}"/>
        <h:outputLabel value="#{assessmentSettingsMessages.mark_for_review_label}"/>
      </h:panelGroup>
    </h:panelGrid>
	<f:verbatim></div></f:verbatim>
</h:panelGroup>

  <!-- *** COLORS AND GRAPHICS	*** -->
<h:panelGroup rendered="#{publishedSettings.valueMap.bgColor_isInstructorEditable==true}" >
  <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_background} </h4>" escape="false" />
	<f:verbatim><div class="tier2"></f:verbatim>

        <h:selectOneRadio onclick="uncheckOther(this)" id="background_color" value="#{publishedSettings.bgColorSelect}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_color}"/>
       </h:selectOneRadio>

      <samigo:colorPicker value="#{publishedSettings.bgColor}" size="10" id="pickColor"/>
       <h:selectOneRadio onclick="uncheckOther(this)" id="background_image" value="#{publishedSettings.bgImageSelect}"  >
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_image}"/>
       </h:selectOneRadio>  
   
       <h:inputText size="80" value="#{publishedSettings.bgImage}"/>
   <f:verbatim></div></f:verbatim>
  </h:panelGroup>

</samigo:hideDivision><!-- END Layout and Appearance Category -->

</div>

<p class="act">

  <!-- Save button -->
  <h:commandButton type="submit" value="#{commonMessages.action_save}" action="#{publishedSettings.getOutcome}"  styleClass="active" onclick="extendedTimeCombine();setBlockDivs();updateItemNavigation(false);" >
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SavePublishedSettingsListener" />
  </h:commandButton>
  
  <!-- Cancel button -->
  <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="#{author.getFromPage}" rendered="#{author.fromPage != 'editAssessment'}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetPublishedAssessmentAttachmentListener" />
  </h:commandButton>

  <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="editAssessment" rendered="#{author.fromPage == 'editAssessment'}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetPublishedAssessmentAttachmentListener" />
	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

</p>
</h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>