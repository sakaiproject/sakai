<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
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
              allowEmptyDate: true,
              val: '<h:outputText value="#{assessmentSettings.startDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'startDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:endDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{assessmentSettings.dueDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'endDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:retractDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{assessmentSettings.retractDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'retractDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:feedbackDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{assessmentSettings.feedbackDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'feedbackDateISO8601' }
          });

          // SAM-2121: Lockdown the question layout and mark for review if necessary
          var navVal = $('#assessmentSettingsAction\\:itemNavigation input:radio:checked').val();
          lockdownQuestionLayout(navVal);
          lockdownMarkForReview(navVal);
          
          var releaseToVal = $('#assessmentSettingsAction\\:releaseTo').val();
          lockdownAnonyGrading(releaseToVal);
          lockdownGradebook(releaseToVal);
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
    <body onload="checkTimeSelect(); <%= request.getAttribute("html.body.onload") %>">

<div class="portletBody container-fluid">

<!-- content... -->
<h:form id="assessmentSettingsAction" onsubmit="return editorCheck();">
  <h:inputHidden id="assessmentId" value="#{assessmentSettings.assessmentId}"/>
  <h:inputHidden id="blockDivs" value="#{assessmentSettings.blockDivs}"/>
  <h:inputHidden id="itemNavigationUpdated" value="false" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>
  <h1>
     <h:outputText value="#{assessmentSettingsMessages.settings} #{assessmentSettingsMessages.dash} #{assessmentSettings.title}"/>
  </h1>

  <div class="pull-right">
      <a href="javascript:void(0)" id="expandLink" onclick="expandAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
          <h:outputText value="#{assessmentSettingsMessages.expandAll}"/>
      </a>
      <a href="javascript:void(0)" id="collapseLink" style="display:none" onclick="collapseAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
          <h:outputText value="#{assessmentSettingsMessages.collapseAll}"/>
      </a>
  </div>
  <br/>
  
  <p>
    <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
  </p>

<div class="tier1" id="jqueryui-accordion">

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_about}" >

<!-- *** ASSESSMENT INTRODUCTION *** -->
  <div class="tier2" id="assessment-intro">
      
    <!-- *** GENERAL TEMPLATE INFORMATION *** -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.templateInfo_isInstructorEditable==true and !assessmentSettings.noTemplate and template.showAssessmentTypes}" >
        <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.heading_template_information}"/>
        <div class="col-md-10">
          <h:outputText escape="false" rendered="#{assessmentSettings.templateDescription!=null}" value="#{assessmentSettings.templateDescription}" />
        </div>
    </h:panelGroup>
    <div class="form-group row">
        <h:outputLabel styleClass="col-md-2" for="creator" value="#{assessmentSettingsMessages.assessment_creator}" rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>
        <div class="col-md-10">
            <h:outputText id="creator" value="#{assessmentSettings.creator}" rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>
        </div>
    </div>

    <div class="form-group row">
        <h:outputLabel styleClass="col-md-2" for="assessment_title" value="#{assessmentSettingsMessages.assessment_title}"/>
        <div class="col-md-10">
            <h:inputText styleClass="form-control" id="assessment_title" size="80" maxlength="255" value="#{assessmentSettings.title}" />
        </div>
    </div>

    <div class="form-group row hidden">
        <h:outputLabel styleClass="col-md-2" for="assessment_author" rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}" value="#{assessmentSettingsMessages.assessment_authors}"/>
        <div class="col-md-10">
            <h:inputText styleClass="form-control" id="assessment_author" value="#{assessmentSettings.authors}" rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>
        </div>
    </div>

    <div class="form-group row">
        <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.assessment_description}" rendered="#{assessmentSettings.valueMap.description_isInstructorEditable==true}"/>

        <div class="col-md-10">
          <samigo:wysiwyg rows="100" columns="400" value="#{assessmentSettings.description}" hasToggle="yes" mode="author">
           <f:validateLength maximum="60000"/>
           </samigo:wysiwyg>
        </div>
    </div>

    <!-- Honor Pledge -->
    <div class="form-group row">
        <h:outputLabel styleClass="col-md-2" for="honor_pledge" value="#{assessmentSettingsMessages.honor_pledge}" />
        <div class="col-md-10">
            <h:selectBooleanCheckbox id="honor_pledge" value="#{assessmentSettings.valueMap.honorpledge_isInstructorEditable}"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputText  value="#{assessmentSettingsMessages.honor_pledge_add}" />
        </div>
    </div>

    <!-- ASSESSMENT ATTACHMENTS -->
    <div class="form-group row">
         <%@ include file="/jsf/author/authorSettings_attachment.jsp" %>
    </div>

  </div>

<div id="jqueryui-accordion-metadata"><!-- This is sub-accordion for metadata -->

    <!-- *** META *** -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.metadataAssess_isInstructorEditable==true}">
      <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_metadata} </a> </h3>" /> 
      <div class="tier2">
        <div class="samigo-subheading">
            <h:outputLabel value="#{assessmentSettingsMessages.assessment_metadata}" /> 
        </div>
        <div class="tier3">
            <h:panelGrid columns="2" columnClasses="samigoCell">
            <h:outputLabel for="keywords" value="#{assessmentSettingsMessages.metadata_keywords}"/>
            <h:inputText id="keywords" size="80" value="#{assessmentSettings.keywords}"/>

            <h:outputLabel for="objectives" value="#{assessmentSettingsMessages.metadata_objectives}"/>
            <h:inputText id="objectives" value="#{assessmentSettings.objectives}"/>

            <h:outputLabel for="rubrics" value="#{assessmentSettingsMessages.metadata_rubrics}"/>
            <h:inputText id="rubrics" value="#{assessmentSettings.rubrics}"/>
            </h:panelGrid>
        </div>
        <div class="samigo-subheading">
            <h:outputLabel value="#{assessmentSettingsMessages.record_metadata}" />
        </div>
         <div class="tier3">
         <h:selectBooleanCheckbox rendered="#{assessmentSettings.valueMap.metadataQuestions_isInstructorEditable==true}"
            value="#{assessmentSettings.valueMap.hasMetaDataForQuestions}"/>
         <h:outputText value="#{assessmentSettingsMessages.metadata_questions}" rendered="#{assessmentSettings.valueMap.metadataQuestions_isInstructorEditable==true}" />
       </div>
      </div>
    </h:panelGroup>
  </div><!-- This is the end of the sub-accordion -->

</samigo:hideDivision><!-- End the About this Assessment category -->

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_availability}"> 
  <!-- *** RELEASED TO *** -->
  <div class="form-group row">
      <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.released_to} " />
      <div class="col-md-10">
          <h:selectOneMenu id="releaseTo" value="#{assessmentSettings.firstTargetSelected}" onclick="setBlockDivs();lockdownAnonyGrading(this.value);lockdownGradebook(this.value);" onchange="showHideReleaseGroups();">
              <f:selectItems value="#{assessmentSettings.publishingTargets}" />
          </h:selectOneMenu>
       </div>
  </div>

  <div id="groupDiv" class="groupTable">
    <h:selectBooleanCheckbox id="checkUncheckAllReleaseGroups" onclick="checkUncheckAllReleaseGroups();"/>
    <h:outputText value="#{assessmentSettingsMessages.select_all_groups}" />
    <h:selectManyCheckbox id="groupsForSite" layout="pagedirection" value="#{assessmentSettings.groupsAuthorized}">
      <f:selectItems value="#{assessmentSettings.groupsForSite}" />
    </h:selectManyCheckbox>
  </div>
  
  <!-- Extended Time -->
  <%@ include file="inc/extendedTime.jspf"%>

  <!-- NUMBER OF SUBMISSIONS -->
  <h:panelGroup styleClass="row" layout="block" rendered="#{assessmentSettings.valueMap.submissionModel_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.submissions_allowed}" />
      <div class="col-md-10 form-inline">
          <div class="radio">
              <!-- Use the custom Tomahawk layout spread to style this radio http://myfaces.apache.org/tomahawk-project/tomahawk12/tagdoc/t_selectOneRadio.html -->
              <t:selectOneRadio id="unlimitedSubmissions" value="#{assessmentSettings.unlimitedSubmissions}" layout="spread">
                <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.unlimited_submission}"/>
                <f:selectItem itemValue="0" itemLabel="#{assessmentSettingsMessages.only}" />
              </t:selectOneRadio>
              <ul class="submissions-allowed">
                <li><t:radio for="unlimitedSubmissions" index="0" /></li>
                <li>
                  <t:radio for="unlimitedSubmissions" index="1" />
                  <span class="submissions-allowed">
                    <h:inputText size="5" id="submissions_Allowed" value="#{assessmentSettings.submissionsAllowed}" />
                    <h:outputText value="&#160;" escape="false" />
                    <h:outputLabel for="submissions_Allowed" value="#{assessmentSettingsMessages.limited_submission}" />
                  </span>
                </li>
              </ul>
          </div>
      </div>
  </h:panelGroup>
      
  <!-- *** DELIVERY DATES *** -->
      <div class="form-group row">
          <h:outputLabel styleClass="col-md-2" for="startDate" value="#{assessmentSettingsMessages.assessment_available}"/>
          <div class="col-md-10">
              <h:inputText value="#{assessmentSettings.startDateString}" size="25" id="startDate" />
          </div>
      </div>
      <div class="form-group row">
          <h:outputLabel styleClass="col-md-2" for="endDate" value="#{assessmentSettingsMessages.assessment_due}" />
          <div class="col-md-10">
              <h:inputText value="#{assessmentSettings.dueDateString}" size="25" id="endDate"/>
              <h:outputText value="&#160;" escape="false" />
              <h:graphicImage value="/images/crossmark.gif"  onclick="resetDatePicker('endDate');"  alt="#{assessmentSettingsMessages.clear_calendar_alt}"/>
              <h:outputText value="&#160;" escape="false" />

    <!-- *** TIMED *** -->
      <h:panelGroup rendered="#{assessmentSettings.valueMap.timedAssessment_isInstructorEditable==true}" >
        <h:outputText value="#{assessmentSettingsMessages.has_time_limit} " />
        <h:selectBooleanCheckbox id="selTimeAssess" onclick="checkUncheckTimeBox();setBlockDivs();" value="#{assessmentSettings.valueMap.hasTimeAssessment}" />
        <h:outputText value="&#160;" escape="false" />
        <h:selectOneMenu id="timedHours" value="#{assessmentSettings.timedHours}" >
          <f:selectItems value="#{assessmentSettings.hours}" />
        </h:selectOneMenu>
        <h:outputText value="&#160;" escape="false" />
        <h:outputText value="#{assessmentSettingsMessages.timed_hours} " />
        <h:selectOneMenu id="timedMinutes" value="#{assessmentSettings.timedMinutes}" >
          <f:selectItems value="#{assessmentSettings.mins}" />
        </h:selectOneMenu>
        <h:outputText value="&#160;" escape="false" />
        <h:outputText value="#{assessmentSettingsMessages.timed_minutes} " />
        </h:panelGroup>
          </div>
      </div>

    <!-- LATE HANDLING -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.lateHandling_isInstructorEditable==true}">
      <div class="row">
        <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.late_accept}" />
        <div class="col-md-10">
        <t:selectOneRadio id="lateHandling" value="#{assessmentSettings.lateHandling}" onclick="checkLastHandling();" layout="spread">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.no_late}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.yes_late}"/>
        </t:selectOneRadio>
        <ul class="late-handling">
          <li><t:radio for="lateHandling" index="0" /></li>
          <li>
            <t:radio for="lateHandling" index="1" />
            <h:outputText value="&#160;" escape="false" />
            <h:inputText value="#{assessmentSettings.retractDateString}" size="25" id="retractDate"/>
            <h:outputText value="&#160;" escape="false" />
            <h:graphicImage value="/images/crossmark.gif"  onclick="resetDatePicker('retractDate');" alt="#{assessmentSettingsMessages.clear_calendar_alt}"/>
          </li>
        </ul>
        <h:outputLabel styleClass="help-block info-text small" value="#{assessmentSettingsMessages.late_accept_help}" />
      </div>
    </div>
  </h:panelGroup>

  <!-- AUTOMATIC SUBMISSION -->
  <h:panelGroup styleClass="radio row" layout="block" rendered="#{assessmentSettings.valueMap.automaticSubmission_isInstructorEditable==true}">
    <h:outputLabel value="#{assessmentSettingsMessages.auto_submit}">
      <h:selectBooleanCheckbox id="automaticSubmission" value="#{assessmentSettings.autoSubmit}" />
    </h:outputLabel>
  </h:panelGroup>

  <!-- SUBMISSION EMAILS -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.submissionModel_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.instructorNotificationLabel}" />
    <div class="col-md-10">
      <t:selectOneRadio id="notificationEmailChoices" value="#{assessmentSettings.instructorNotification}" layout="spread">
        <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.oneEmail}" />
        <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.digestEmail}" />
        <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.noEmail}" />
      </t:selectOneRadio>
      <ul class="email-notification">
        <li><t:radio for="notificationEmailChoices" index="0" /></li>
        <li><t:radio for="notificationEmailChoices" index="1" /></li>
        <li><t:radio for="notificationEmailChoices" index="2" /></li>
      </ul>
      <h:outputLabel styleClass="help-block info-text small" value="#{assessmentSettingsMessages.instructorNotification}" />
    </div>
  </h:panelGroup>

    <!-- Display Scores -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.displayScores_isInstructorEditable==true}">
     <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel for="displayScores" value="#{assessmentSettingsMessages.displayScores}" /> <f:verbatim> </div><div class="tier3"> </f:verbatim>
       <h:panelGrid columns="2"  >
         <h:selectOneRadio id="displayScores" value="#{assessmentSettings.displayScoreDuringAssessments}"  layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.displayScores_show}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.displayScores_hide}"/>
         </h:selectOneRadio>
      </h:panelGrid>
      <f:verbatim></div></f:verbatim>
    </h:panelGroup>

<div id="jqueryui-accordion-security"><!-- This is sub-accordion for high security and submission message -->

  <!-- *** HIGH SECURITY *** -->
  <h:panelGroup rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true or assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true or publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true}" >
    <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_high_security} </a> </h3><div>" />
    <h:panelGrid border="0" columns="2" summary="#{templateMessages.high_security_sec}">
      <h:outputText value="#{assessmentSettingsMessages.high_security_allow_only_specified_ip}" rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}"/>
      <%-- no WYSIWYG for IP addresses --%>
      <h:panelGroup rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}">
      <h:inputTextarea value="#{assessmentSettings.ipAddresses}" cols="40" rows="5"/>
      <h:outputText escape="false" value="<br/>#{assessmentSettingsMessages.ip_note} <br/>#{assessmentSettingsMessages.ip_example}#{assessmentSettingsMessages.ip_ex}<br/>"/> 
     </h:panelGroup>
      <h:outputText value="#{assessmentSettingsMessages.high_security_secondary_id_pw}" rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}"/>
      <h:panelGrid border="0" columns="2"  columnClasses="samigo-security" rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}">
        <h:outputLabel for="username" value="#{assessmentSettingsMessages.high_security_username}"/>
        <h:inputText id="username" size="20" value="#{assessmentSettings.username}"/>
        <h:outputLabel for="password" value="#{assessmentSettingsMessages.high_security_password}"/>
        <h:inputText id="password" size="20" value="#{assessmentSettings.password}"/>
      </h:panelGrid>
      
      <h:outputText value="#{assessmentSettingsMessages.require_secure_delivery}" rendered="#{assessmentSettings.valueMap.lockedBrowser_isInstructorEditable==true && assessmentSettings.secureDeliveryAvailable}"/>
	  <h:panelGrid border="0" columns="1"  columnClasses="samigo-security" rendered="#{assessmentSettings.valueMap.lockedBrowser_isInstructorEditable==true && assessmentSettings.secureDeliveryAvailable}">
	  	<h:selectOneRadio id="secureDeliveryModule" value="#{assessmentSettings.secureDeliveryModule}"  layout="pageDirection" onclick="setBlockDivs();">
			<f:selectItems value="#{assessmentSettings.secureDeliveryModuleSelections}" />
	  	</h:selectOneRadio>
	  	<h:panelGrid border="0" columns="2"  columnClasses="samigo-security" rendered="#{assessmentSettings.valueMap.lockedBrowser_isInstructorEditable==true && assessmentSettings.secureDeliveryAvailable}">	
			<h:outputLabel for="secureDeliveryModuleExitPassword" value="#{assessmentSettingsMessages.secure_delivery_exit_pwd}"/>
			<h:inputText id="secureDeliveryModuleExitPassword" size="20" value="#{assessmentSettings.secureDeliveryModuleExitPassword}" disabled="#{assessmentSettings.secureDeliveryModule == 'SECURE_DELIVERY_NONE_ID'}" maxlength="14"/>      	
	  	</h:panelGrid>
	  </h:panelGrid>
    </h:panelGrid>
    <f:verbatim></div></f:verbatim>
  </h:panelGroup>

  <!-- *** SUBMISSION MESSAGE *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.submissionMessage_isInstructorEditable==true or assessmentSettings.valueMap.finalPageURL_isInstructorEditable==true}" >
   <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_submission_message} </a> </h3><div>" />
    <h:panelGrid rendered="#{assessmentSettings.valueMap.submissionMessage_isInstructorEditable==true}">
    <div class="samigo-submission-message">
        <h:outputLabel value="#{assessmentSettingsMessages.submission_message}" /> 
       <samigo:wysiwyg rows="140" value="#{assessmentSettings.submissionMessage}" hasToggle="yes" mode="author">
         <f:validateLength maximum="4000"/>
       </samigo:wysiwyg>
       </div>
    </h:panelGrid>
    <h:panelGroup rendered="#{assessmentSettings.valueMap.finalPageURL_isInstructorEditable==true}">
    <div class="samigo-submission-message">
      <h:outputLabel for="finalPageUrl" value="#{assessmentSettingsMessages.submission_final_page_url}" />
      <h:inputText size="80" id="finalPageUrl" value="#{assessmentSettings.finalPageUrl}" />
      <h:commandButton value="#{assessmentSettingsMessages.validateURL}" type="button" onclick="javascript:validateUrl();"/>
    </div>
    </h:panelGroup>
   </div>
</h:panelGroup>

</div><!-- This is the end of the sub-accordion -->

</samigo:hideDivision><!-- END the Availabity and Submissions category -->

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_grading_feedback}" >

  <!-- *** GRADING *** -->
  <div class="tier3">
  <!-- RECORDED SCORE AND MULTIPLES -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.recordedScore_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" escape="false" value="#{commonMessages.grading}"/>
      <div class="col-md-10 form-inline">
        <h:outputLabel value="#{assessmentSettingsMessages.recorded_score} " />
        <h:selectOneRadio value="#{assessmentSettings.scoringType}" id="scoringType1" rendered="#{author.canRecordAverage}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
          <f:selectItem itemValue="4" itemLabel="#{assessmentSettingsMessages.average_score}"/>
        </h:selectOneRadio>
        <h:selectOneRadio value="#{assessmentSettings.scoringType}" id="scoringType2" rendered="#{!author.canRecordAverage}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
        </h:selectOneRadio>
      </div>
    </h:panelGroup>
  
    <!--  ANONYMOUS OPTION -->
    <h:panelGroup styleClass="row" layout="block" rendered="#{assessmentSettings.valueMap.testeeIdentity_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.student_identity_label}"/>
      <div class="col-md-10">
        <h:outputLabel value="#{assessmentSettingsMessages.student_identity}">
          <h:selectBooleanCheckbox id="anonymousGrading" value="#{assessmentSettings.anonymousGrading}"/>
        </h:outputLabel>
      </div>
    </h:panelGroup>
    
    <!-- GRADEBOOK OPTION -->
    <h:panelGroup styleClass="row" layout="block" rendered="#{assessmentSettings.valueMap.toGradebook_isInstructorEditable==true && assessmentSettings.gradebookExists==true}">
      <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.gradebook_options}"/>
      <div class="col-md-10">
        <h:selectBooleanCheckbox id="toDefaultGradebook" value="#{assessmentSettings.toDefaultGradebook}"/>
      </div>
    </h:panelGroup>

  </div>

    <!-- *** FEEDBACK *** -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackAuthoring_isInstructorEditable==true or assessmentSettings.valueMap.feedbackType_isInstructorEditable==true or assessmentSettings.valueMap.feedbackComponents_isInstructorEditable==true}" >

      <h4 class="samigo-category-subhead-2">
        <h:outputText escape="false" value="#{assessmentSettingsMessages.heading_feedback}"/>
      </h4>

    <!-- FEEDBACK AUTHORING -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.feedbackAuthoring_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="feedbackAuthoring" value="#{assessmentSettingsMessages.feedback_level}"/>
      <div class="col-md-10">
        <t:selectOneRadio id="feedbackAuthoring" value="#{assessmentSettings.feedbackAuthoring}" layout="spread">
           <f:selectItem itemValue="1" itemLabel="#{commonMessages.question_level_feedback}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.sectionlevel_feedback}"/>
           <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.both_feedback}"/>
        </t:selectOneRadio>
        <ul class="feedback-authoring">
          <li><t:radio for="feedbackAuthoring" index="0" /></li>
          <li><t:radio for="feedbackAuthoring" index="1" /></li>
          <li><t:radio for="feedbackAuthoring" index="2" /></li>
        </ul>
      </div>
    </h:panelGroup>

    <!-- FEEDBACK DELIVERY -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.feedbackType_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="feedbackDelivery" value="#{assessmentSettingsMessages.feedback_type}"/>
      <div class="col-md-10">
        <t:selectOneRadio id="feedbackDelivery" value="#{assessmentSettings.feedbackDelivery}" onclick="setBlockDivs();disableAllFeedbackCheck(this.value);" layout="spread">
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.no_feedback}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.immediate_feedback}"/>
          <f:selectItem itemValue="4" itemLabel="#{commonMessages.feedback_on_submission}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.feedback_by_date}"/>
        </t:selectOneRadio>
        <ul class="feedback-delivery">
          <li><t:radio for="feedbackDelivery" index="0" /></li>
          <li><t:radio for="feedbackDelivery" index="1" /></li>
          <li><t:radio for="feedbackDelivery" index="2" /></li>
	  <li>
	    <t:radio for="feedbackDelivery" index="3" />
            <h:outputText value="&#160;" escape="false" />
            <h:inputText value="#{assessmentSettings.feedbackDateString}" size="25" id="feedbackDate" />
            <h:outputText value="&#160;" escape="false" />
            <h:graphicImage value="/images/crossmark.gif"  onclick="resetDatePicker('feedbackDate');"  alt="#{assessmentSettingsMessages.clear_calendar_alt}"/>
	  </li>
        </ul>
      </div>
    </h:panelGroup>
 
    <!-- FEEDBACK COMPONENTS -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackComponents_isInstructorEditable==true}">

      <div class="form-group row">
        <h:outputLabel styleClass="col-md-2" for="feedbackComponentOption" value="#{assessmentSettingsMessages.feedback_components}"/>
        <div class="col-md-10">
          <t:selectOneRadio id="feedbackComponentOption" value="#{assessmentSettings.feedbackComponentOption}" onclick="setBlockDivs();disableOtherFeedbackComponentOption(this);" layout="spread">
            <f:selectItem itemValue="1" itemLabel="#{templateMessages.feedback_components_totalscore_only}"/>
            <f:selectItem itemValue="2" itemLabel="#{templateMessages.feedback_components_select}"/>
          </t:selectOneRadio>
          <ul class="feedback-component">
            <li><t:radio for="feedbackComponentOption" index="0" /></li>
            <li><t:radio for="feedbackComponentOption" index="1" /></li>
          </ul>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentResponse}" id="feedbackCheckbox1"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox1" value="#{commonMessages.student_response}" />
          </h:panelGroup>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showCorrectResponse}" id="feedbackCheckbox3"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox3" value="#{commonMessages.correct_response}" />
          </h:panelGroup>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showQuestionLevelFeedback}" id="feedbackCheckbox2"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox2" value="#{commonMessages.question_level_feedback}" />
          </h:panelGroup>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showSelectionLevelFeedback}" id="feedbackCheckbox4"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox4" value="#{commonMessages.selection_level_feedback}" />
          </h:panelGroup>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showGraderComments}" id="feedbackCheckbox6"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox6" value="#{assessmentSettingsMessages.grader_comments}" />
          </h:panelGroup>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentQuestionScore}" id="feedbackCheckbox7"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox7" value="#{assessmentSettingsMessages.student_question_score}" />
          </h:panelGroup>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentScore}" id="feedbackCheckbox5"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox5" value="#{assessmentSettingsMessages.student_assessment_score}" />
          </h:panelGroup>
          <h:panelGroup styleClass="form-inline" layout="block">
            <h:selectBooleanCheckbox value="#{assessmentSettings.showStatistics}" id="feedbackCheckbox8"/>
            <h:outputText value="&#160;" escape="false" />
            <h:outputLabel for="feedbackCheckbox8" value="#{commonMessages.statistics_and_histogram}" />
          </h:panelGroup>
        </div>
      </div>

     </h:panelGroup>
   </h:panelGroup>
 </samigo:hideDivision>

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_layout}" >

  <!-- *** ASSESSMENT ORGANIZATION *** -->
  <h:panelGroup rendered="#{assessmentSettings.valueMap.itemAccessType_isInstructorEditable==true or assessmentSettings.valueMap.displayChunking_isInstructorEditable==true or assessmentSettings.valueMap.displayNumbering_isInstructorEditable==true }" >

    <!-- NAVIGATION -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.itemAccessType_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="itemNavigation" value="#{assessmentSettingsMessages.navigation}" />
      <div class="col-md-10">
        <t:selectOneRadio id="itemNavigation" value="#{assessmentSettings.itemNavigation}" layout="spread" onclick="setBlockDivs();updateItemNavigation(true);lockdownQuestionLayout(this.value);lockdownMarkForReview(this.value);">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.linear_access}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.random_access}"/>
        </t:selectOneRadio>
        <ul class="layout-navigation">
          <li><t:radio for="itemNavigation" index="0" /></li>
          <li><t:radio for="itemNavigation" index="1" /></li>
        </ul>
        <div class="info-text help-block small">
          <h:outputText value="#{assessmentSettingsMessages.linear_access_warning} "/>
        </div>
      </div>
    </h:panelGroup>

    <!-- QUESTION LAYOUT -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.displayChunking_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="assessmentFormat" value="#{assessmentSettingsMessages.question_layout}" />
      <div class="col-md-10">
        <t:selectOneRadio id="assessmentFormat" value="#{assessmentSettings.assessmentFormat}" layout="spread">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.layout_by_question}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.layout_by_part}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.layout_by_assessment}"/>
        </t:selectOneRadio>
        <ul class="layout-format">
          <li><t:radio for="assessmentFormat" index="0" /></li>
          <li><t:radio for="assessmentFormat" index="1" /></li>
          <li><t:radio for="assessmentFormat" index="2" /></li>
        </ul>
      </div>
    </h:panelGroup>

    <!-- NUMBERING -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.displayNumbering_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="itemNumbering" value="#{assessmentSettingsMessages.numbering}" />
      <div class="col-md-10">
         <t:selectOneRadio id="itemNumbering" value="#{assessmentSettings.itemNumbering}" layout="spread">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.continous_numbering}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.part_numbering}"/>
         </t:selectOneRadio>
         <ul class="layout-numbering">
           <li><t:radio for="itemNumbering" index="0" /></li>
           <li><t:radio for="itemNumbering" index="1" /></li>
         </ul>
      </div>
    </h:panelGroup>
  </h:panelGroup>

  <!-- *** MARK FOR REVIEW *** -->
  <!-- *** (disabled for linear assessment) *** -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.markForReview_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.mark_for_review}" />
    <div class="col-md-10">
      <h:selectBooleanCheckbox id="markForReview1" value="#{assessmentSettings.isMarkForReview}"/>
      <h:outputText value="&#160;" escape="false" />
      <h:outputText value="#{assessmentSettingsMessages.mark_for_review_label}"/>
    </div>
  </h:panelGroup>
 
  <!-- *** COLORS AND GRAPHICS	*** -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{assessmentSettings.valueMap.bgColor_isInstructorEditable==true}" >
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.background_label}" />
    <div class="col-md-10">
      <h:selectOneRadio onclick="uncheckOther(this)" id="background_color" value="#{assessmentSettings.bgColorSelect}">
        <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_color}"/>
      </h:selectOneRadio>
      <samigo:colorPicker value="#{assessmentSettings.bgColor}" size="10" id="pickColor"/>
       <h:selectOneRadio onclick="uncheckOther(this)" id="background_image" value="#{assessmentSettings.bgImageSelect}"  >
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_image}"/>
       </h:selectOneRadio>  
      <h:inputText size="80" value="#{assessmentSettings.bgImage}"/>
    </div>
  </h:panelGroup>

</samigo:hideDivision><!-- END Layout and Appearance Category -->

</div>
 <p class="act">

 <!-- save & publish -->
  <h:commandButton  value="#{assessmentSettingsMessages.button_unique_save_and_publish}" type="submit" styleClass="active" rendered="#{assessmentSettings.hasQuestions}"
      action="#{assessmentSettings.getOutcomePublish}" onclick="extendedTimeCombine();setBlockDivs();updateItemNavigation(false);" >
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmPublishAssessmentListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.PublishAssessmentListener" />
  </h:commandButton>

  <h:commandButton  value="#{assessmentSettingsMessages.button_unique_save_and_publish}" type="submit" rendered="#{not assessmentSettings.hasQuestions}"
      action="#{assessmentSettings.getOutcomePublish}" disabled="true" />
      
<!-- save -->
  <h:commandButton type="submit" value="#{commonMessages.action_save}" action="#{assessmentSettings.getOutcomeSave}"  onclick="extendedTimeCombine();setBlockDivs();updateItemNavigation(false);">
      <f:param name="assessmentId" value="#{assessmentSettings.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SaveAssessmentSettingsListener"/>
  </h:commandButton>

  <!-- cancel -->
  <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="editAssessment" rendered="#{author.firstFromPage == 'editAssessment'}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetAssessmentAttachmentListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

    <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="author" rendered="#{author.firstFromPage == 'author'}">
	      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetAssessmentAttachmentListener" />
  </h:commandButton>

</p>
</h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>
