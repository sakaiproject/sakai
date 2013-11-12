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
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{assessmentSettingsMessages.sakai_assessment_manager} #{assessmentSettingsMessages.dash} #{assessmentSettingsMessages.settings}" /></title>
      <script type="text/javascript" src="/library/js/jquery/jquery-1.9.1.min.js"></script>
      <script type="text/javascript" src="/library/js/jquery/ui/1.10.3/jquery-ui.1.10.3.full.min.js"></script>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      <samigo:script path="/jsf/widget/datepicker/datepicker.js"/>
      <samigo:script path="/jsf/widget/colorpicker/colorpicker.js"/>
      <samigo:script path="/js/authoring.js"/>
      <link type="text/css" href="/samigo-app/css/ui-lightness/jquery-ui-1.7.2.custom.css" rel="stylesheet" media="all"/>

      <script type="text/javascript">
        $(document).ready(function() {
          // set up the accordion for settings
          $("#jqueryui-accordion").accordion({ heightStyle: "content",collapsible: true });
          // This is a sub-accordion inside of the Availability and Submission Panel
          $("#jqueryui-accordion-security").accordion({ heightStyle: "content",collapsible: true,active: false });
          // adjust the height of the iframe to accomodate the expansion from the accordion
          $("body").height($("body").outerHeight() + 800);

          // SAM-2121: Lockdown the question layout and mark for review if necessary
          var navVal = $('#assessmentSettingsAction\\:itemNavigation input:radio:checked').val();
          lockdownQuestionLayout(navVal);
          lockdownMarkForReview(navVal);
          showHideReleaseGroups();
        });
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

    <h3>
     <h:outputText id="x1" value="#{assessmentSettingsMessages.settings} #{assessmentSettingsMessages.dash} #{publishedSettings.title}"/>
    </h3>
<p>
  <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
</p>

<div class="tier1" id="jqueryui-accordion">

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_about}" >


  <!-- *** ASSESSMENT INTRODUCTION *** -->
  <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_assessment_introduction} </h4>" />
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
          <samigo:wysiwyg rows="140" value="#{publishedSettings.description}" hasToggle="yes" >
           <f:validateLength maximum="4000"/>
         </samigo:wysiwyg>
        </h:panelGrid>

       <!-- ASSESSMENT ATTACHMENTS -->
       <h:panelGroup>
         <h:panelGrid columns="1">
           <%@ include file="/jsf/author/publishedSettings_attachment.jsp" %>
         </h:panelGrid>
       </h:panelGroup>

    </h:panelGrid>
  </div>

 <!-- *** META *** -->
<h:panelGroup rendered="#{publishedSettings.valueMap.metadataAssess_isInstructorEditable==true}">
  <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_metadata} </h4>" />
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

</samigo:hideDivision><!-- End the About this Assessment category -->

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_availability}"> 

  <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_released_to} </h4>" />
  <!-- *** RELEASED TO *** -->
  <div class="tier2">
    <h:panelGrid   summary="#{templateMessages.released_to_info_sec}">
      <h:selectOneRadio disabled="true" layout="pagedirection" value="#{publishedSettings.firstTargetSelected}">
        <f:selectItems value="#{assessmentSettings.publishingTargets}" />
      </h:selectOneRadio>
    </h:panelGrid>
      
  <f:verbatim><div id="groupDiv" class="tier3"></f:verbatim>
  <f:verbatim><table bgcolor="#CCCCCC"><tr><td></f:verbatim>  
  
  <f:verbatim></td><td></f:verbatim>
  <h:outputText value="#{assessmentSettingsMessages.select_all_groups}" />
  <f:verbatim></td></tr></table></f:verbatim>
  
  <h:selectManyCheckbox disabled="true" id="groupsForSite" layout="pagedirection" value="#{publishedSettings.groupsAuthorized}">
     <f:selectItems value="#{publishedSettings.groupsForSite}" />
  </h:selectManyCheckbox>
  </div>
 </div>

    <!-- NUMBER OF SUBMISSIONS -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.submissionModel_isInstructorEditable==true}">
      <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.submissions} </h4>" />
      <f:verbatim> <div class="tier3"></f:verbatim>
	 <f:verbatim><table><tr><td></f:verbatim>
        <h:selectOneRadio id="unlimitedSubmissions" value="#{publishedSettings.unlimitedSubmissions}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.unlimited_submission}"/>
          <f:selectItem itemValue="0" itemLabel="#{assessmentSettingsMessages.only}" />
        </h:selectOneRadio>
        
        <f:verbatim></td><td valign="bottom"></f:verbatim>
        <h:panelGroup>
          <h:inputText size="5" id="submissions_Allowed" value="#{publishedSettings.submissionsAllowed}"/>
          <h:outputLabel for="submissions_Allowed" value="#{assessmentSettingsMessages.limited_submission}" />
        </h:panelGroup>
      <f:verbatim></td></tr></table></div></f:verbatim>
   </h:panelGroup>
    
  <!-- *** DELIVERY DATES *** -->
  <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_assessment_delivery_dates} </h4>" />
  <div class="tier2">
    <h:panelGrid columns="2" columnClasses="samigoCell" summary="#{templateMessages.delivery_dates_sec}" border="0">
      <h:outputLabel for="startDate" value="#{assessmentSettingsMessages.assessment_available_date}"/>
      <samigo:datePicker value="#{publishedSettings.startDateString}" size="25" id="startDate" />
      <h:outputText value="" />
      <h:outputText value="#{assessmentSettingsMessages.available_date_note}" />

	<!-- For formatting -->
	<h:outputText value="" />
	<h:outputText value="" />
	<h:outputText value="" />
	<h:outputText value="" />
	  
      <h:outputLabel for="endDate" value="#{assessmentSettingsMessages.assessment_due_date}" />
      <samigo:datePicker value="#{publishedSettings.dueDateString}" size="25" id="endDate"/>
      <h:outputText value="" />
	  <h:outputText value="#{assessmentSettingsMessages.assessment_due_date_note}" />

	<!-- For formatting -->
	<h:outputText value="" />
	<h:outputText value="" />
	<h:outputText value="" />
	<h:outputText value="" />
	  
      <h:outputLabel for="retractDate" value="#{assessmentSettingsMessages.assessment_retract_date}"/>
  	  <h:panelGroup>
        <samigo:datePicker value="#{publishedSettings.retractDateString}" size="25" id="retractDate" />
      <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
	  <h:outputText value="#{assessmentSettingsMessages.word_or}"/>
	  <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:commandButton type="submit" value="#{assessmentSettingsMessages.button_retract_now}" action="confirmAssessmentRetract"  styleClass="active" />
      </h:panelGroup>
      <h:outputText value="" />
      <h:outputText value="#{assessmentSettingsMessages.assessment_retract_date_note}" />

    </h:panelGrid>
 </div>

  <!-- *** TIMED *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.timedAssessment_isInstructorEditable==true}" >
  <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_timed_assessment} </h4>" />
    <f:verbatim><div class="tier2"></f:verbatim>
    <h:panelGrid summary="#{templateMessages.timed_assmt_sec}">
	  <h:panelGroup>
        <h:selectBooleanCheckbox id="selTimeAssess" onclick="checkUncheckTimeBox();setBlockDivs();" value="#{publishedSettings.valueMap.hasTimeAssessment}">
		</h:selectBooleanCheckbox>
        <h:outputText value="#{assessmentSettingsMessages.timed_assessment} " />
		<h:selectOneMenu id="timedHours" value="#{publishedSettings.timedHours}" disabled="#{!publishedSettings.valueMap.hasTimeAssessment}" >
		  <f:selectItems value="#{publishedSettings.hours}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_hours} " />
        <h:selectOneMenu id="timedMinutes" value="#{publishedSettings.timedMinutes}" disabled="#{!publishedSettings.valueMap.hasTimeAssessment}">
          <f:selectItems value="#{publishedSettings.mins}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_minutes} " />
       <f:verbatim><br/></f:verbatim>
        <h:outputText value="#{assessmentSettingsMessages.auto_submit_description}" />
      </h:panelGroup>
    </h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

  <!-- *** SUBMISSIONS *** -->
<h:panelGroup rendered="#{publishedSettings.valueMap.submissionModel_isInstructorEditable==true or publishedSettings.valueMap.lateHandling_isInstructorEditable==true or publishedSettings.valueMap.autoSave_isInstructorEditable==true}" >
	<!-- LATE HANDLING -->
   <h:panelGroup rendered="#{publishedSettings.valueMap.lateHandling_isInstructorEditable==true}">
      <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.late_handling} </h4>" />
   <f:verbatim><div class="tier3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="lateHandling" value="#{publishedSettings.lateHandling}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.not_accept_latesubmission}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.accept_latesubmission}"/>
        </h:selectOneRadio>
      </h:panelGrid>
   <f:verbatim> </div> </f:verbatim>
   </h:panelGroup>

    <!-- AUTOMATIC SUBMISSION -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.automaticSubmission_isInstructorEditable==true}">
      <f:verbatim> <h4 class="samigo-category-subhead"> </f:verbatim> 
      <h:outputLabel value="#{assessmentSettingsMessages.automatic_submission}" />
      <f:verbatim> </h4></f:verbatim>
      <f:verbatim><div class="tier3"></f:verbatim>
      <h:panelGrid columns="1" border="0">
	    <h:panelGroup>
	      <h:selectBooleanCheckbox id="automaticSubmission" value="#{publishedSettings.autoSubmit}"/>
          <h:outputLabel value="#{assessmentSettingsMessages.auto_submit}"/>
        </h:panelGroup>
		<h:panelGroup>
          <f:verbatim>&nbsp;</f:verbatim>
          <h:outputText value="#{assessmentSettingsMessages.automatic_submission_note_1}"/>
		</h:panelGroup>
      </h:panelGrid>
      <f:verbatim> </div> </f:verbatim>
   </h:panelGroup>

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
        <samigo:wysiwyg rows="140" value="#{publishedSettings.submissionMessage}" hasToggle="yes" >
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

<samigo:hideDivision title="#{assessmentSettingsMessages.heading_grading}" >

  <!-- *** GRADING *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.testeeIdentity_isInstructorEditable==true or publishedSettings.valueMap.toGradebook_isInstructorEditable==true or publishedSettings.valueMap.recordedScore_isInstructorEditable==true}" >
  <f:verbatim><div class="tier2"></f:verbatim>
  <h:panelGroup rendered="#{publishedSettings.valueMap.testeeIdentity_isInstructorEditable==true}">
    <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.student_identity} </h4>" />
  <f:verbatim><div class="tier3"> </f:verbatim>
        <h:panelGrid columns="2" rendered="#{publishedSettings.firstTargetSelected != 'Anonymous Users'}">
          <h:selectOneRadio id="anonymousGrading1" value="#{publishedSettings.anonymousGrading}"  layout="pageDirection" disabled="#{publishedSettings.editPubAnonyGradingRestricted}">
            <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.not_anonymous}"/>
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.anonymous}"/>
          </h:selectOneRadio>
        </h:panelGrid>
        <h:panelGrid columns="2" rendered="#{publishedSettings.firstTargetSelected == 'Anonymous Users'}">
          <h:selectOneRadio id="anonymousGrading2" value="1"  layout="pageDirection" disabled="true">
            <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.not_anonymous}"/>
            <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.anonymous}"/>
          </h:selectOneRadio>
        </h:panelGrid>

<f:verbatim></div></f:verbatim>
</h:panelGroup>
    <!-- GRADEBOOK OPTIONS -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.toGradebook_isInstructorEditable==true && publishedSettings.gradebookExists==true}">
     <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.gradebook_options} </h4>" />
	 <f:verbatim> <div class="tier3"> </f:verbatim>
      <h:panelGrid columns="2" rendered="#{publishedSettings.firstTargetSelected != 'Anonymous Users'}">
        <h:selectOneRadio id="toDefaultGradebook1" value="#{publishedSettings.toDefaultGradebook}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.to_no_gradebook}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.to_default_gradebook}"/>
        </h:selectOneRadio>
      </h:panelGrid>

      <h:panelGrid columns="2" rendered="#{publishedSettings.firstTargetSelected == 'Anonymous Users'}">
        <h:selectOneRadio id="toDefaultGradebook2" disabled="true" value="2"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.to_no_gradebook}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.to_default_gradebook}"/>
        </h:selectOneRadio>
      </h:panelGrid>

	<f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- RECORDED SCORE AND MULTIPLES -->
    <h:panelGroup rendered="#{publishedSettings.valueMap.recordedScore_isInstructorEditable==true}">
   <f:verbatim>  <h4 class="samigo-category-subhead">  </f:verbatim> <h:outputLabel for="scoringType1" value="#{assessmentSettingsMessages.recorded_score}" rendered="#{author.canRecordAverage}"/><h:outputLabel for="scoringType2" value="#{assessmentSettingsMessages.recorded_score}" rendered="#{!author.canRecordAverage}"/><f:verbatim></h4> <div class="tier3"> </f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio value="#{publishedSettings.scoringType}" id="scoringType1" layout="pageDirection" rendered="#{author.canRecordAverage}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
          <f:selectItem itemValue="4" itemLabel="#{assessmentSettingsMessages.average_score}"/>
        </h:selectOneRadio>
        <h:selectOneRadio value="#{publishedSettings.scoringType}" id="scoringType2" layout="pageDirection" rendered="#{!author.canRecordAverage}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
        </h:selectOneRadio>
      </h:panelGrid>
	  <f:verbatim></div></f:verbatim>
    </h:panelGroup>

  <f:verbatim></div></f:verbatim>
</h:panelGroup>

  <!-- *** FEEDBACK *** -->
  <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true or publishedSettings.valueMap.feedbackType_isInstructorEditable==true or publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}" >
  <f:verbatim><div class="tier2"></f:verbatim>

 <!-- FEEDBACK AUTHORING -->
   <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable==true}">
   <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{commonMessages.feedback_authoring} </h4>" />
     <f:verbatim> <div class="tier3"> </f:verbatim>
      <h:panelGrid border="0" columns="1">
        <h:selectOneRadio id="feedbackAuthoring" value="#{publishedSettings.feedbackAuthoring}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{commonMessages.question_level_feedback}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.sectionlevel_feedback}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.both_feedback}"/>
        </h:selectOneRadio>
      </h:panelGrid>

  <f:verbatim> </div> </f:verbatim>
  </h:panelGroup>
  
 <!-- FEEDBACK DELIVERY -->
	<h:panelGroup rendered="#{publishedSettings.valueMap.feedbackType_isInstructorEditable==true}">
    <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{commonMessages.feedback_delivery} </h4>" />
	<f:verbatim><div class="tier3"></f:verbatim>

    <h:panelGroup>
      <h:panelGrid columns="1" rendered="#{publishedSettings.valueMap.feedbackAuthoring_isInstructorEditable!=true}" >
        <h:selectOneRadio id="feedbackDelivery1"  disabled="true" 
             value="#{publishedSettings.feedbackDelivery}"
           layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.immediate_feedback}"/>
          <f:selectItem itemValue="4" itemLabel="#{commonMessages.feedback_on_submission}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.no_feedback}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.feedback_by_date}"/>
        </h:selectOneRadio>

        <h:panelGroup>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
		<h:inputText value="#{publishedSettings.feedbackDateString}" size="25" disabled="true"/>
        </h:panelGroup>
      </h:panelGrid>
    </h:panelGroup>

      <h:panelGrid border="0" columns="1">
  		<h:selectOneRadio id="feedbackDelivery2" value="#{publishedSettings.feedbackDelivery}"
           layout="pageDirection" onclick="setBlockDivs();disableAllFeedbackCheck(this.value);">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.immediate_feedback}"/>
		  <f:selectItem itemValue="4" itemLabel="#{commonMessages.feedback_on_submission}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.no_feedback}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.feedback_by_date}"/>
        </h:selectOneRadio>

	    <h:panelGrid columns="7" >
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
          <samigo:datePicker value="#{publishedSettings.feedbackDateString}" size="25" id="feedbackDate" >
            <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
          </samigo:datePicker>
        </h:panelGrid>

	    <h:panelGrid columns="7" >
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
          <h:outputText value="#{assessmentSettingsMessages.gradebook_note_f}" />
        </h:panelGrid>
      </h:panelGrid>

<f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- FEEDBACK COMPONENTS -->
       <h:panelGrid columns="2"  >
        <h:selectOneRadio id="feedbackComponentOption" value="#{publishedSettings.feedbackComponentOption}"
        onclick="setBlockDivs();disableOtherFeedbackComponentOption(this);"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{templateMessages.feedback_components_totalscore_only}"/>
          <f:selectItem itemValue="2" itemLabel="#{templateMessages.feedback_components_select}"/>
        </h:selectOneRadio>
      </h:panelGrid>
  
   <f:verbatim> <div class="tier3"></f:verbatim>
    <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackComponents_isInstructorEditable!=true}">
      <h:panelGrid columns="2">
       <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox11"
              value="#{publishedSettings.showStudentResponse}"/>
          <h:outputText value="#{commonMessages.student_response}" />
        </h:panelGroup>
       <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox12"
              value="#{publishedSettings.showQuestionLevelFeedback}"/>
          <h:outputText value="#{commonMessages.question_level_feedback}" />
       </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox13"
              value="#{publishedSettings.showCorrectResponse}"/>
          <h:outputText value="#{commonMessages.correct_response}" />
        </h:panelGroup>
       <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox14"
             value="#{publishedSettings.showSelectionLevelFeedback}"/>
          <h:outputText value="#{commonMessages.selection_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox15"
              value="#{publishedSettings.showStudentScore}"/>
          <h:outputText value="#{assessmentSettingsMessages.student_assessment_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox16"
              value="#{publishedSettings.showGraderComments}"/>
          <h:outputText value="#{assessmentSettingsMessages.grader_comments}" />
        </h:panelGroup>

        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox17"
              value="#{publishedSettings.showStudentQuestionScore}"/>
          <h:outputText value="#{assessmentSettingsMessages.student_question_score}" />
        </h:panelGroup>
       
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true" id="feedbackCheckbox18"
              value="#{publishedSettings.showStatistics}"/>
          <h:outputText value="#{commonMessages.statistics_and_histogram}" />
        </h:panelGroup>


      </h:panelGrid>
    </h:panelGroup>
   
    <h:panelGroup rendered="#{publishedSettings.valueMap.feedbackComponents_isInstructorEditable==true}">
      <h:panelGrid columns="2"  >
       <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox21" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
              value="#{publishedSettings.showStudentResponse}"/>
          <h:outputText value="#{commonMessages.student_response}" />
        </h:panelGroup>
       <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox22" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
              value="#{publishedSettings.showQuestionLevelFeedback}"/>
          <h:outputText value="#{commonMessages.question_level_feedback}" />
       </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox23" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
              value="#{publishedSettings.showCorrectResponse}"/>
          <h:outputText value="#{commonMessages.correct_response}" />
        </h:panelGroup>
       <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox24" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
             value="#{publishedSettings.showSelectionLevelFeedback}"/>
          <h:outputText value="#{commonMessages.selection_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox25" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
              value="#{publishedSettings.showStudentScore}"/>
          <h:outputText value="#{assessmentSettingsMessages.student_assessment_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox26" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
              value="#{publishedSettings.showGraderComments}"/>
          <h:outputText value="#{assessmentSettingsMessages.grader_comments}" />
        </h:panelGroup>

        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox27" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
              value="#{publishedSettings.showStudentQuestionScore}"/>
          <h:outputText value="#{assessmentSettingsMessages.student_question_score}" />
        </h:panelGroup>
       
        <h:panelGroup>
          <h:selectBooleanCheckbox id="feedbackCheckbox28" disabled="#{publishedSettings.feedbackDelivery==3 || publishedSettings.feedbackComponentOption ==1}"
              value="#{publishedSettings.showStatistics}"/>
          <h:outputText value="#{commonMessages.statistics_and_histogram}" />
        </h:panelGroup>
   
      </h:panelGrid>
    </h:panelGroup>
	<f:verbatim></div></div></f:verbatim>
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
  <h:outputLabel value="<h4 class=\"samigo-category-subhead\"> #{assessmentSettingsMessages.heading_background} </h4>" />
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
