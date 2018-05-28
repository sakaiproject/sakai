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
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      <samigo:script path="/jsf/widget/colorpicker/colorpicker.js"/>
      <samigo:script path="/../library/js/lang-datepicker/lang-datepicker.js"/>
      <samigo:script path="/js/authoring.js"/>        

      <script type="text/javascript">
      if (needJQuery) {
         document.write('\x3Clink href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css?version=" rel="stylesheet">');
      }
      </script>

      <script type="text/javascript">
        $(document).ready(function() {
          // set up the accordion for settings
          var accordionPanel = 1;
          var itemName = "samigo_publishedsettings_" + <h:outputText value="#{publishedSettings.assessmentId}"/>;
          if (window.sessionStorage && window.sessionStorage.getItem(itemName)) {
              accordionPanel = parseInt(window.sessionStorage.getItem(itemName));
          }
          $("#jqueryui-accordion").accordion({
              heightStyle: "content",
              activate: function(event, ui) {
                  if (window.sessionStorage) {
                      window.sessionStorage.setItem(itemName, $("#jqueryui-accordion").accordion("option", "active"));
                  }
              },
              active: accordionPanel,
              collapsible: true
          });
          // This is a sub-accordion inside of the About the Assessment Panel
          $("#jqueryui-accordion-metadata").accordion({ heightStyle: "content",collapsible: true,active: false });
          // This is a sub-accordion inside of the Availability and Submission Panel
          $("#jqueryui-accordion-security").accordion({ heightStyle: "content",collapsible: true,active: false });
          // adjust the height of the iframe to accomodate the expansion from the accordion
          $("body").height($("body").outerHeight() + 900);

          checkNav = function() {
              QuesFormatRadios = ["assessmentSettingsAction\\:assessmentFormat\\:0", "assessmentSettingsAction\\:assessmentFormat\\:1", "assessmentSettingsAction\\:assessmentFormat\\:2"];

              enabled = true;
              if ($("#assessmentSettingsAction\\:itemNavigation\\:0").is(":checked")) {
                enabled = false;
              }

              if (enabled) {
                  $('#assessmentSettingsAction\\:markForReview1').removeAttr("disabled");
                  $('#assessmentSettingsAction\\:markForReview1').parent().toggleClass("placeholder");
                  QuesFormatRadios.forEach( function(v, i, a) {
                      $('label[for="' + v + '"]').toggleClass("placeholder");
                      $("#" + v).removeAttr("disabled");
                  });
              } else {
                  $('#assessmentSettingsAction\\:markForReview1').attr("disabled", true);
                  $('#assessmentSettingsAction\\:markForReview1').attr("checked", false);
                  $('#assessmentSettingsAction\\:markForReview1').parent().toggleClass("placeholder");
                  QuesFormatRadios.forEach( function(v, i, a) {
                      $('#assessmentSettingsAction\\:assessmentFormat\\:0').click();
                      $('label[for="' + v + '"]').toggleClass("placeholder");
                      $("#" + v).attr("disabled", true);
                  });
              }
          };

          $('#assessmentSettingsAction\\:itemNavigation\\:0').change(checkNav);
          $('#assessmentSettingsAction\\:itemNavigation\\:1').change(checkNav);
          checkNav();

          // SAM-2323 jquery-UI datepicker
          localDatePicker({
              input: '#assessmentSettingsAction\\:startDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.startDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'startDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:endDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.dueDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'endDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:retractDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.retractDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'retractDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:feedbackDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.feedbackDate}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'feedbackDateISO8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:newEntry-start_date',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.extendedTimeStartString}"/>',
              ashidden: { iso8601: 'newEntry-start_date-iso8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:newEntry-due_date',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.extendedTimeDueString}"/>',
              ashidden: { iso8601: 'newEntry-due_date-iso8601' }
          });
          localDatePicker({
              input: '#assessmentSettingsAction\\:newEntry-retract_date',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '<h:outputText value="#{publishedSettings.extendedTimeRetractString}"/>',
              ashidden: { iso8601: 'newEntry-retract_date-iso8601' }
          });

          // SAM-2121: Lockdown the question layout and mark for review if necessary
          var navVal = $('#assessmentSettingsAction\\:itemNavigation input:radio:checked').val();
          lockdownQuestionLayout(navVal);
          lockdownMarkForReview(navVal);
          showHideReleaseGroups();
          initTimedCheckBox();
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

<div class="portletBody container-fluid">

<!-- content... -->
<h:form id="assessmentSettingsAction" onsubmit="return editorCheck();">
  <h:inputHidden id="assessmentId" value="#{publishedSettings.assessmentId}"/>
  <h:inputHidden id="blockDivs" value="#{publishedSettings.blockDivs}"/>
  <h:inputHidden id="itemNavigationUpdated" value="false" />
  
  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>
  <h1>
     <h:outputText value="#{assessmentSettingsMessages.settings} #{assessmentSettingsMessages.dash} #{publishedSettings.title}"/>
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
    <div class="form-group row">
        <h:outputLabel for="assessment_title" value="#{assessmentSettingsMessages.assessment_title}" 
                            styleClass="form-control-label col-md-2"/>
        <div class=" col-md-10">
            <h:inputText id="assessment_title" size="80" maxlength="255" value="#{publishedSettings.title}" />
        </div>
    </div>
        
    <div class="form-group row">
        <h:outputLabel value="#{assessmentSettingsMessages.published_assessment_url}: " 
                        styleClass="form-control-label col-md-2"/>
        <div class=" col-md-10">
            <h:outputText value="#{publishedSettings.publishedUrl}" />
        </div>
    </div>

    <div class="form-group row">
        <h:outputLabel value="#{assessmentSettingsMessages.assessment_creator}"  rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"
                        styleClass="form-control-label col-md-2"/>
        <div class=" col-md-10">
            <h:outputText value="#{publishedSettings.creator}"  rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>
        </div>
    </div>

    <div class="form-group row">
        <h:outputLabel for="assessment_author" rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}" value="#{assessmentSettingsMessages.assessment_authors}"
                styleClass="form-control-label col-md-2"/>
        <div class=" col-md-10">
            <h:inputText id="assessment_author" size="80" maxlength="255" value="#{publishedSettings.authors}" rendered="#{publishedSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>
        </div>
    </div>

    <div class="form-group row">
        <h:outputLabel value="#{assessmentSettingsMessages.assessment_description}" rendered="#{publishedSettings.valueMap.description_isInstructorEditable==true}"
            styleClass="form-control-label col-md-2"/>
        <div class=" col-md-10">
            <h:panelGrid rendered="#{publishedSettings.valueMap.description_isInstructorEditable==true}">
                <samigo:wysiwyg rows="100" columns="400" value="#{publishedSettings.description}" hasToggle="yes" mode="author" >
                    <f:validateLength maximum="60000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </div>

       <!-- Honor Pledge -->
    <div class="form-group row">
		<h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.honor_pledge}" rendered="#{publishedSettings.valueMap.honorpledge_isInstructorEditable==true}"/>
        <div class="col-md-10">
			<h:selectBooleanCheckbox id="honor_pledge" value="#{publishedSettings.honorPledge}" rendered="#{publishedSettings.valueMap.honorpledge_isInstructorEditable==true}"/>
			<h:outputText value="#{assessmentSettingsMessages.honor_pledge_add}" rendered="#{publishedSettings.valueMap.honorpledge_isInstructorEditable==true}"/>
        </div>
    </div>

    <!-- ASSESSMENT ATTACHMENTS -->
    <%@ include file="/jsf/author/publishedSettings_attachment.jsp" %>
       
  </div>
  
<div id="jqueryui-accordion-metadata"><!-- This is sub-accordion for metadata -->  

 <!-- *** META *** -->
<h:panelGroup rendered="#{publishedSettings.valueMap.metadataAssess_isInstructorEditable==true}">
  <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_metadata} </a> </h3>" /> 
    <div class="tier2">
        <div class="samigo-subheading">
            <h:outputLabel value="#{assessmentSettingsMessages.assessment_metadata}" /> 
        </div>
    
        <div class="form-group row">
            <h:outputLabel for="keywords" value="#{assessmentSettingsMessages.metadata_keywords}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-10">
                <h:inputText id="keywords" size="80" value="#{publishedSettings.keywords}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="objectives" value="#{assessmentSettingsMessages.metadata_objectives}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-10">
                <h:inputText id="objectives" value="#{publishedSettings.objectives}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="rubrics" value="#{assessmentSettingsMessages.metadata_rubrics}" styleClass="col-md-2 form-control-label"/>
            <div class="col-md-10">
                <h:inputText id="rubrics" value="#{publishedSettings.rubrics}" styleClass="form-control"/>
            </div>
        </div>

        <div class="samigo-subheading">
            <h:outputLabel value="#{assessmentSettingsMessages.record_metadata}" />
        </div>
        <div class="samigo-checkbox">
            <h:selectBooleanCheckbox rendered="#{publishedSettings.valueMap.metadataQuestions_isInstructorEditable==true}"
            value="#{publishedSettings.valueMap.hasMetaDataForQuestions}"/>
            <h:outputLabel value="#{assessmentSettingsMessages.metadata_questions}" rendered="#{publishedSettings.valueMap.metadataQuestions_isInstructorEditable==true}" />
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
        <h:selectOneMenu id="releaseTo" disabled="true" value="#{publishedSettings.firstTargetSelected}" >
        <f:selectItems value="#{publishedSettings.publishingTargets}" />
      </h:selectOneMenu>
      </div>
  </div>

  <div id="groupDiv" class="groupTable">
    <h:selectBooleanCheckbox id="checkUncheckAllReleaseGroups" disabled="true" />
    <h:outputText value="#{assessmentSettingsMessages.select_all_groups}" />
    <h:selectManyCheckbox id="groupsForSite" disabled="true"  layout="pagedirection" value="#{publishedSettings.groupsAuthorized}">
      <f:selectItems value="#{publishedSettings.groupsForSite}" />
    </h:selectManyCheckbox>
  </div>
  
    <!-- NUMBER OF SUBMISSIONS -->
  <h:panelGroup styleClass="row" layout="block" rendered="#{publishedSettings.valueMap.submissionModel_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.submissions_allowed}" />
    <div class="col-md-10 form-inline">
          <div class="radio">
              <!-- Use the custom Tomahawk layout spread to style this radio http://myfaces.apache.org/tomahawk-project/tomahawk12/tagdoc/t_selectOneRadio.html -->
              <t:selectOneRadio id="unlimitedSubmissions" value="#{publishedSettings.unlimitedSubmissions}" layout="spread">
                <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.unlimited_submission}"/>
                <f:selectItem itemValue="0" itemLabel="#{assessmentSettingsMessages.only}" />
              </t:selectOneRadio>
              <ul class="submissions-allowed">
                <li><t:radio for="unlimitedSubmissions" index="0" /></li>
                <li>
                  <t:radio for="unlimitedSubmissions" index="1" />
                  <span class="submissions-allowed">
                    <h:inputText size="5" id="submissions_Allowed" value="#{publishedSettings.submissionsAllowed}" />
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
        <h:inputText value="#{publishedSettings.startDateString}" size="25" id="startDate" />
      </div>
    </div>
    <div class="form-group row">
      <h:outputLabel styleClass="col-md-2" for="endDate" value="#{assessmentSettingsMessages.assessment_due}" />
      <div class="col-md-10">
        <h:inputText value="#{publishedSettings.dueDateString}" size="25" id="endDate"/>
  
  <!-- *** TIMED *** -->
      <h:panelGroup rendered="#{publishedSettings.valueMap.timedAssessment_isInstructorEditable==true}" >
        <h:outputText value="#{assessmentSettingsMessages.has_time_limit} " />
        <h:selectBooleanCheckbox id="selTimeAssess" onclick="checkUncheckTimeBox();setBlockDivs();" value="#{publishedSettings.valueMap.hasTimeAssessment}" />
        <h:outputText value="&#160;" escape="false" />
        <h:selectOneMenu id="timedHours" value="#{publishedSettings.timedHours}" >
          <f:selectItems value="#{publishedSettings.hours}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_hours} " />
        <h:selectOneMenu id="timedMinutes" value="#{publishedSettings.timedMinutes}">
          <f:selectItems value="#{publishedSettings.mins}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_minutes} " />
      </h:panelGroup>
    </div>
  </div>
  
  <!-- LATE HANDLING -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.lateHandling_isInstructorEditable==true}">
      <div class="row">
        <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.late_accept}" />
        <div class="col-md-10">
          <t:selectOneRadio id="lateHandling" onclick="checkLastHandling();" value="#{publishedSettings.lateHandling}"  layout="spread">
            <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.no_late}"/>
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.yes_late}"/>
          </t:selectOneRadio>
        <ul class="late-handling">
          <li><t:radio for="lateHandling" index="0" /></li>
          <li>
            <t:radio for="lateHandling" index="1" />
            <h:outputText value="&#160;" escape="false" />
            <h:inputText value="#{publishedSettings.retractDateString}" size="25" id="retractDate"/>
          </li>
        </ul>
        <div>
	        <h:commandButton type="submit" value="#{assessmentSettingsMessages.button_stop_accepting_now}" action="confirmAssessmentRetract"  styleClass="active" />
        </div>
        <h:outputLabel styleClass="help-block info-text small" value="#{assessmentSettingsMessages.late_accept_help}" />
      </div>
    </div>
  </h:panelGroup>

  <!-- AUTOMATIC SUBMISSION -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.automaticSubmission_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.auto_submit}" />
    <div class="col-md-4">
      <h:selectBooleanCheckbox id="automaticSubmission" value="#{publishedSettings.autoSubmit}"/>
      <h:outputLabel styleClass="help-block info-text small" value="#{assessmentSettingsMessages.auto_submit_help}" />
    </div>
  </h:panelGroup>

  <!-- SUBMISSION EMAILS -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.instructorNotification_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.instructorNotification}" />
    <div class="col-md-10">
      <t:selectOneRadio id="notificationEmailChoices" value="#{publishedSettings.instructorNotification}" layout="spread">
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
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.displayScores_isInstructorEditable==true}">
        <h:outputLabel styleClass="col-md-2" for="displayScores" value="#{assessmentSettingsMessages.displayScores}" />
        <div class="col-md-10">
          <t:selectOneRadio id="displayScores" value="#{publishedSettings.displayScoreDuringAssessments}"  layout="spread">
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.displayScores_show}"/>
            <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.displayScores_hide}"/>
          </t:selectOneRadio>
          <ul class="display-scores">
            <li><t:radio for="displayScores" index="0" /></li>
            <li><t:radio for="displayScores" index="1" /></li>
          </ul>
       </div>
    </h:panelGroup>

<div id="jqueryui-accordion-security"><!-- This is sub-accordion for high security and submission message -->

  <!-- *** HIGH SECURITY *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.ipAccessType_isInstructorEditable==true or publishedSettings.valueMap.passwordRequired_isInstructorEditable==true or publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true}" >
    <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_high_security} </a> </h3><div>" />
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.ipAccessType_isInstructorEditable==true}">
        <h:outputLabel value="#{assessmentSettingsMessages.high_security_allow_only_specified_ip}" styleClass="col-md-2 form-control-label"/>
        <%-- no WYSIWYG for IP addresses --%>
        <div class="col-md-10">
            <h:inputTextarea value="#{publishedSettings.ipAddresses}" cols="40" rows="5"/>
            <h:outputText escape="false" value="<br/>#{assessmentSettingsMessages.ip_note} <br/>#{assessmentSettingsMessages.ip_example}#{assessmentSettingsMessages.ip_ex}<br/>"/> 
        </div>
    </h:panelGroup>
    
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.passwordRequired_isInstructorEditable==true}">
        <h:outputLabel value="#{assessmentSettingsMessages.high_security_secondary_id_pw}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-10">
            <h:outputLabel for="password" value="#{assessmentSettingsMessages.high_security_password}"/>
            <h:inputText id="password" size="20" value="#{publishedSettings.password}" styleClass="form-control"/>
        </div>
    </h:panelGroup>
    
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.lockedBrowser_isInstructorEditable==true && publishedSettings.secureDeliveryAvailable}">
        <h:outputLabel value="#{assessmentSettingsMessages.require_secure_delivery}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-10">
            <h:selectOneRadio id="secureDeliveryModule" value="#{publishedSettings.secureDeliveryModule}"  layout="pageDirection" onclick="setBlockDivs();">
                <f:selectItems value="#{publishedSettings.secureDeliveryModuleSelections}" />
            </h:selectOneRadio>
            <h:panelGrid border="0" columns="2"  columnClasses="samigo-security" >	
                <h:outputLabel for="secureDeliveryModuleExitPassword" value="#{assessmentSettingsMessages.secure_delivery_exit_pwd}"/>
                <h:inputText id="secureDeliveryModuleExitPassword" size="20" value="#{publishedSettings.secureDeliveryModuleExitPassword}" disabled="#{publishedSettings.secureDeliveryModule == 'SECURE_DELIVERY_NONE_ID'}" maxlength="14" />      	
            </h:panelGrid>
        </div>
    </h:panelGroup>
    <f:verbatim></div></f:verbatim>
  </h:panelGroup>

  <!-- *** SUBMISSION MESSAGE *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.submissionMessage_isInstructorEditable==true or publishedSettings.valueMap.finalPageURL_isInstructorEditable==true}" >
   <h:outputText escape="false" value="<h3> <a class=\"jqueryui-hideDivision\" href=\"#\"> #{assessmentSettingsMessages.heading_submission_message} </a> </h3><div>" />
    <h:panelGroup layout="block" styleClass="form-group row" rendered="#{publishedSettings.valueMap.submissionMessage_isInstructorEditable==true}">
        <h:outputLabel value="#{assessmentSettingsMessages.submission_message}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-10">
            <h:panelGrid>
                <samigo:wysiwyg rows="140" value="#{publishedSettings.submissionMessage}" hasToggle="yes" mode="author" >
                    <f:validateLength maximum="4000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </h:panelGroup>
    <h:panelGroup layout="block" styleClass="form-group row" rendered="#{publishedSettings.valueMap.finalPageURL_isInstructorEditable==true}">
        <h:outputLabel for="finalPageUrl" value="#{assessmentSettingsMessages.submission_final_page_url}" styleClass="col-md-2 form-control-label"/>
        <div class="col-md-10">
            <h:inputText size="80" id="finalPageUrl" value="#{publishedSettings.finalPageUrl}" styleClass="form-control"/>
            <h:commandButton value="#{assessmentSettingsMessages.validateURL}" type="button" onclick="javascript:validateUrl();"/>
        </div>
    </h:panelGroup>
   </div>
</h:panelGroup>

</div><!-- This is the end of the sub-accordion -->

</samigo:hideDivision><!-- END the Availabity and Submissions category -->

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_extended_time}" >
  <!-- Extended Time -->
  <%@ include file="inc/publishedExtendedTime.jspf"%>
</samigo:hideDivision>

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_grading_feedback}" >

  <!-- *** GRADING *** -->
  <div class="tier3">
  <!-- RECORDED SCORE AND MULTIPLES -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.recordedScore_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.recorded_score} " />
      <div class="col-md-10 form-inline">
        <t:selectOneRadio value="#{publishedSettings.scoringType}" id="scoringType1" rendered="#{author.canRecordAverage}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
          <f:selectItem itemValue="4" itemLabel="#{assessmentSettingsMessages.average_score}"/>
        </t:selectOneRadio>
        <t:selectOneRadio value="#{publishedSettings.scoringType}" id="scoringType2" rendered="#{!author.canRecordAverage}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
        </t:selectOneRadio>
      </div>
    </h:panelGroup>
  
    <!--  ANONYMOUS OPTION -->  
    <h:panelGroup styleClass="row" layout="block" rendered="#{publishedSettings.valueMap.testeeIdentity_isInstructorEditable==true}"> 
      <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.student_identity_label}"/>
      <div class="col-md-10 samigo-checkbox">
        <h:selectBooleanCheckbox value="#{publishedSettings.anonymousGrading}" disabled="#{publishedSettings.firstTargetSelected == 'Anonymous Users' || publishedSettings.editPubAnonyGradingRestricted}"/>
        <h:outputLabel value="#{assessmentSettingsMessages.student_identity}" />
      </div>
    </h:panelGroup>
    
    <!-- GRADEBOOK OPTION -->
    <h:panelGroup styleClass="row" layout="block" rendered="#{publishedSettings.valueMap.toGradebook_isInstructorEditable==true && publishedSettings.gradebookExists==true}">
      <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.gradebook_options}"/>
      <div class="col-md-10 samigo-checkbox">
        <h:selectBooleanCheckbox id="toDefaultGradebook" value="#{publishedSettings.toDefaultGradebook}" disabled="#{publishedSettings.firstTargetSelected == 'Anonymous Users'}"/>
        <h:outputLabel value="#{assessmentSettingsMessages.gradebook_options_help}" for="toDefaultGradebook" />
      </div>
    </h:panelGroup>

    </div>

    <!-- *** FEEDBACK *** -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true or publishedSettings.valueMap.feedbackType_isInstructorEditable==true or publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}" >

      <div class="page-header">
        <h4 class="samigo-category-subhead-2">
          <h:outputText escape="false" value="#{assessmentSettingsMessages.heading_feedback}"/>
        </h4>
      </div>

    <!-- FEEDBACK AUTHORING -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="feedbackAuthoring" value="#{assessmentSettingsMessages.feedback_level}"/>
      <div class="col-md-10">
         <t:selectOneRadio id="feedbackAuthoring" value="#{publishedSettings.feedbackAuthoring}" layout="spread">
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
    <h:panelGroup styleClass="form-group row" layout="block"  rendered="#{publishedSettings.valueMap.feedbackType_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="feedbackDelivery" value="#{assessmentSettingsMessages.feedback_type}"/>
      <div class="col-md-10">
        <t:selectOneRadio id="feedbackDelivery" value="#{publishedSettings.feedbackDelivery}" onclick="setBlockDivs();disableAllFeedbackCheck(this.value);" layout="spread">
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
            <h:inputText value="#{publishedSettings.feedbackDateString}" size="25" id="feedbackDate" />
	  </li>
        </ul>
      </div>
    </h:panelGroup>

    <!-- FEEDBACK COMPONENTS -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}">

        <div class="form-group row">
            <h:outputLabel styleClass="col-md-2" for="feedbackComponentOption" value="#{assessmentSettingsMessages.feedback_components}"/>
            <div class="col-md-10">
                <t:selectOneRadio id="feedbackComponentOption" value="#{publishedSettings.feedbackComponentOption}" onclick="setBlockDivs();disableOtherFeedbackComponentOption(this);"  layout="pageDirection">
                    <f:selectItem itemValue="1" itemLabel="#{templateMessages.feedback_components_totalscore_only}"/>
                    <f:selectItem itemValue="2" itemLabel="#{templateMessages.feedback_components_select}"/>
                </t:selectOneRadio>
                <div class="respChoice indent1">
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox11" value="#{publishedSettings.showStudentResponse}"/>
                        <h:outputLabel for="feedbackCheckbox11" value="#{commonMessages.student_response}" />
                    </h:panelGroup>
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox13" value="#{publishedSettings.showCorrectResponse}"/>
                        <h:outputLabel for="feedbackCheckbox13" value="#{commonMessages.correct_response}" />
                    </h:panelGroup>
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox12" value="#{publishedSettings.showQuestionLevelFeedback}"/>
                        <h:outputLabel for="feedbackCheckbox12" value="#{commonMessages.question_level_feedback}" />
                    </h:panelGroup>
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox14" value="#{publishedSettings.showSelectionLevelFeedback}"/>
                        <h:outputLabel for="feedbackCheckbox14" value="#{commonMessages.selection_level_feedback}" />
                    </h:panelGroup>
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox16" value="#{publishedSettings.showGraderComments}"/>
                        <h:outputLabel for="feedbackCheckbox16" value="#{assessmentSettingsMessages.grader_comments}" />
                    </h:panelGroup>
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox17" value="#{publishedSettings.showStudentQuestionScore}"/>
                        <h:outputLabel for="feedbackCheckbox17" value="#{assessmentSettingsMessages.student_question_score}" />
                    </h:panelGroup>
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox15" value="#{publishedSettings.showStudentScore}"/>
                        <h:outputLabel for="feedbackCheckbox15" value="#{assessmentSettingsMessages.student_assessment_score}" />
                    </h:panelGroup>
                    <h:panelGroup styleClass="form-inline" layout="block">
                        <h:selectBooleanCheckbox id="feedbackCheckbox18" value="#{publishedSettings.showStatistics}"/>
                        <h:outputLabel for="feedbackCheckbox18" value="#{commonMessages.statistics_and_histogram}" />
                    </h:panelGroup>
                </div>
            </div>
        </div>
    </h:panelGroup>

  </h:panelGroup>

  </samigo:hideDivision>

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_layout}" >

  <!-- *** ASSESSMENT ORGANIZATION *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.itemAccessType_isInstructorEditable==true or publishedSettings.valueMap.displayChunking_isInstructorEditable==true or publishedSettings.valueMap.displayNumbering_isInstructorEditable==true }" >

    <!-- NAVIGATION -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.itemAccessType_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="itemNavigation" value="#{assessmentSettingsMessages.navigation}" />
      <div class="col-md-10">
        <t:selectOneRadio id="itemNavigation" value="#{publishedSettings.itemNavigation}"  layout="spread" onclick="setBlockDivs();updateItemNavigation(true);lockdownQuestionLayout(this.value);lockdownMarkForReview(this.value);">
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
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.displayChunking_isInstructorEditable==true}">
      <h:outputLabel styleClass="col-md-2" for="assessmentFormat" value="#{assessmentSettingsMessages.question_layout}" />
      <div class="col-md-10">
        <t:selectOneRadio id="assessmentFormat" value="#{publishedSettings.assessmentFormat}"  layout="spread">
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
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.displayNumbering_isInstructorEditable==true}">
       <h:outputLabel styleClass="col-md-2" for="itemNumbering" value="#{assessmentSettingsMessages.numbering}" />
       <div class="col-md-10">
         <t:selectOneRadio id="itemNumbering" value="#{publishedSettings.itemNumbering}"  layout="spread">
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
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.markForReview_isInstructorEditable==true}">
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.mark_for_review}"/>
    <div class="col-md-10">
        <h:selectBooleanCheckbox id="markForReview1" value="#{publishedSettings.isMarkForReview}"/>
        <h:outputText value="&#160;" escape="false" />
        <h:outputText value="#{assessmentSettingsMessages.mark_for_review_label}"/>
	</div>
  </h:panelGroup>

  <!-- *** COLORS AND GRAPHICS	*** -->
  <h:panelGroup styleClass="form-group row" layout="block" rendered="#{publishedSettings.valueMap.bgColor_isInstructorEditable==true}" >
    <h:outputLabel styleClass="col-md-2" value="#{assessmentSettingsMessages.background_label}" />
	<div class="col-md-10">
        <h:selectOneRadio onclick="uncheckOther(this)" id="background_color" value="#{publishedSettings.bgColorSelect}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_color}"/>
       </h:selectOneRadio>
      <samigo:colorPicker value="#{publishedSettings.bgColor}" size="10" id="pickColor"/>
       <h:selectOneRadio onclick="uncheckOther(this)" id="background_image" value="#{publishedSettings.bgImageSelect}"  >
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_image}"/>
       </h:selectOneRadio>  
       <h:inputText size="80" value="#{publishedSettings.bgImage}"/>
    </div>
  </h:panelGroup>

</samigo:hideDivision><!-- END Layout and Appearance Category -->

</div>

<p class="act">

  <!-- Save button -->
  <h:commandButton type="submit" value="#{commonMessages.action_save}" action="#{publishedSettings.getOutcome}"  styleClass="active" onclick="setBlockDivs();updateItemNavigation(false);" >
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
