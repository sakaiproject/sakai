<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
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
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <f:view>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages"
     var="msg"/>
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.TemplateMessages"
     var="summary_msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.sakai_assessment_manager} - #{msg.settings}" /></title>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      <samigo:script path="/jsf/widget/datepicker/datepicker.js"/>
      <samigo:script path="/jsf/widget/colorpicker/colorpicker.js"/>

<script language="javascript" style="text/JavaScript">
<!--
function validateUrl(){
  var list =document.getElementsByTagName("input");
  for (var i=0; i<list.length; i++){
    if (list[i].id.indexOf("finalPageUrl") >=0){
      var finalPageUrl = list[i].value;
      window.open(finalPageUrl,'validateUrl');
    }
  }
}


function validateUrl0(){
  alert("hu");
  var finalPageUrl = document.getElementsById("assessmentSettingsAction:finalPageUrl");
  alert("hello"+finalPageUrl.value);
  window.open(finalPageUrl.value,'validateUrl');
}
// By convention we start all feedback JSF ids with "feedback".
var feedbackIdFlag = "assessmentSettingsAction:feedback";
var noFeedback = "3";

// If we select "No Feedback will be displayed to the student"
// it will disable and uncheck feedback as well as blank out text, otherwise,
// if a different radio button is selected, we reenable feedback checkboxes & text.
function disableAllFeedbackCheck(feedbackType)
{
  var feedbacks = document.getElementsByTagName('INPUT');
  for (i=0; i<feedbacks.length; i++)
  {
    if (feedbacks[i].name.indexOf(feedbackIdFlag)==0)
    {
      if (feedbackType == noFeedback)
      {
        if (feedbacks[i].type == 'checkbox')
        {
          feedbacks[i].checked = false;
          feedbacks[i].disabled = true;
        }
        else if (feedbacks[i].type == 'text')
        {
          feedbacks[i].value = "";
          feedbacks[i].disabled = true;
        }
      }
      else
      {
        feedbacks[i].disabled = false;
      }
    }
  }
}

function checkTimeSelect(){
  var inputList= document.getElementsByTagName("INPUT");
  for (i = 0; i <inputList.length; i++) {
    if(inputList[i].type=='checkbox'){
      if(inputList[i].id.indexOf("selTimeAssess")>=0)
        timedAssessmentId= inputList[i].id;
      if(inputList[i].id.indexOf("automatic")>=0)
        autoSubmitId=inputList[i].id;
    }
  }
  if(!document.getElementById(timedAssessmentId).checked)
    document.getElementById(autoSubmitId).disabled=true;
  else
    document.getElementById(autoSubmitId).disabled=false;

}

//-->
</script>



      </head>
    <body onload="hideUnhideAllDivsWithWysiwyg('none');checkTimeSelect();<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">

<!-- content... -->
<h:form id="assessmentSettingsAction">

  <h:inputHidden id="assessmentId" value="#{assessmentSettings.assessmentId}"/>

  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>

    <h3>
     <h:outputText value="#{msg.settings}" />
     <h:outputText value=" - " />
     <h:outputText value="#{assessmentSettings.title}" />
    </h3>
<p>
<f:verbatim><font color="red"></f:verbatim>
  <h:messages styleClass="validation"/>
<f:verbatim></font></f:verbatim>

</p>

<div class="indnt1">
  <!-- *** GENERAL TEMPLATE INFORMATION *** -->

<h:panelGroup rendered="#{assessmentSettings.valueMap.templateInfo_isInstructorEditable==true and !assessmentSettings.noTemplate}" >
  <samigo:hideDivision id="div0" title="#{msg.heading_template_information}" >
<f:verbatim> <div class="indnt2"></f:verbatim>
 <h:panelGrid columns="2" columnClasses="shorttext">
        <h:outputLabel value="#{msg.template_title}"/>
        <h:outputText escape="false" value="#{assessmentSettings.templateTitle}" />
        <h:outputLabel value="#{msg.template_authors}"/>
        <h:outputText escape="false" value="#{assessmentSettings.templateAuthors}" />
        <h:outputLabel value="#{msg.template_description}"/>
        <h:outputText escape="false" value="#{assessmentSettings.templateDescription}" />
    </h:panelGrid>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** ASSESSMENT INTRODUCTION *** -->
<f:subview id="intro">
  <samigo:hideDivision id="div1" title="#{msg.heading_assessment_introduction}" > <div class="indnt2">
    <h:panelGrid columns="2" columnClasses="shorttext" id="first"
      summary="#{summary_msg.enter_template_info_section}">

        <h:outputLabel value="#{msg.assessment_title}"/>
        <h:inputText size="80" value="#{assessmentSettings.title}" />

        <h:outputLabel value="#{msg.assessment_creator}"/>

        <h:outputText value="#{assessmentSettings.creator}"
          rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>

        <h:outputLabel value="#{msg.assessment_authors}"/>

        <h:inputText size="80" value="#{assessmentSettings.authors}"
          disabled="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable!=true}"/>

        <h:outputLabel value="#{msg.assessment_description}"/>

        <%-- SAM-363: this is a work around given samigo:wysiwyg does not support disabled --%>
        <h:panelGroup rendered="#{assessmentSettings.valueMap.description_isInstructorEditable!=true}">
          <h:inputTextarea value="#{assessmentSettings.description}" cols="60"
            disabled="#{assessmentSettings.valueMap.description_isInstructorEditable!=true}"/>
        </h:panelGroup>
        <h:panelGroup rendered="#{assessmentSettings.valueMap.description_isInstructorEditable==true}">
          <samigo:wysiwyg rows="140" value="#{assessmentSettings.description}"  >
           <f:validateLength maximum="4000"/>
         </samigo:wysiwyg>
        </h:panelGroup>

    </h:panelGrid>
</div>
  </samigo:hideDivision>
</f:subview>

  <!-- *** DELIVERY DATES *** -->
  <samigo:hideDivision id="div2" title="#{msg.heading_assessment_delivery_dates}"> <div class="indnt2">
    <h:panelGrid columns="2" columnClasses="shorttext"
      summary="#{summary_msg.delivery_dates_sec}">
    <h:outputLabel  value="#{msg.assessment_available_date}"/>
      <samigo:datePicker value="#{assessmentSettings.startDateString}" size="25" id="startDate" />

     <h:outputLabel rendered="#{assessmentSettings.valueMap.dueDate_isInstructorEditable==true}" value="#{msg.assessment_due_date}" />
        <h:panelGroup rendered="#{assessmentSettings.valueMap.dueDate_isInstructorEditable==true}">

          <samigo:datePicker value="#{assessmentSettings.dueDateString}" size="25" id="endDate"/>

        </h:panelGroup>

    <h:outputLabel value="#{msg.assessment_retract_date}" rendered="#{assessmentSettings.valueMap.retractDate_isInstructorEditable==true}" />
        <h:panelGroup rendered="#{assessmentSettings.valueMap.retractDate_isInstructorEditable==true}">
          <samigo:datePicker value="#{assessmentSettings.retractDateString}" size="25" id="retractDate" />
        </h:panelGroup>
    </h:panelGrid>
 </div>
  </samigo:hideDivision>

  <!-- *** RELEASED TO *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.anonymousRelease_isInstructorEditable==true or assessmentSettings.valueMap.authenticatedRelease_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{msg.heading_released_to}" id="div3">
    <f:verbatim><div class="indnt2"></f:verbatim>
    <h:panelGrid summary="#{summary_msg.released_to_info_sec}">
      <h:selectOneRadio layout="pagedirection" value="#{assessmentSettings.firstTargetSelected}"
        required="true" >
        <f:selectItems value="#{assessmentSettings.publishingTargets}" />
      </h:selectOneRadio>
    </h:panelGrid>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** HIGH SECURITY *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true or assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{msg.heading_high_security}" id="div4">
    <f:verbatim><div class="indnt2"></f:verbatim>
    <h:panelGrid border="0" columns="3"
        summary="#{summary_msg.high_security_sec}">
       <h:selectBooleanCheckbox
         rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}"
         value="#{assessmentSettings.valueMap.hasSpecificIP}"/>
      <h:outputText value="#{msg.high_security_allow_only_specified_ip}"
        rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}"/>
      <%-- no WYSIWYG for IP addresses --%>
      <h:inputTextarea value="#{assessmentSettings.ipAddresses}" cols="40" rows="5"
        rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}"/>
      <h:selectBooleanCheckbox
         rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}"
         value="#{assessmentSettings.valueMap.hasUsernamePassword}"/>
      <h:outputText value="#{msg.high_security_secondary_id_pw}"
        rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}"/>
      <h:panelGrid border="0" columns="2"  columnClasses="longtext"
        rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}">
        <h:outputLabel value="#{msg.high_security_username}"/>
        <h:inputText size="20" value="#{assessmentSettings.username}"/>
        <h:outputLabel value="#{msg.high_security_password}"/>
        <h:inputText size="20" value="#{assessmentSettings.password}"/>
      </h:panelGrid>
    </h:panelGrid>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** TIMED *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.timedAssessment_isInstructorEditable==true or assessmentSettings.valueMap.timedAssessmentAutoSubmit_isInstructorEditable==true}" >
  <samigo:hideDivision id="div5" title="#{msg.heading_timed_assessment}">
    <f:verbatim><div class="indnt2"></f:verbatim>
<%--DEBUGGING:
     Time Limit= <h:outputText value="#{assessmentSettings.timeLimit}" /> ;
     Hours= <h:outputText value="#{assessmentSettings.timedHours}" /> ;
     Min= <h:outputText value="#{assessmentSettings.timedMinutes}" /> ;
     hasQuestions?= <h:outputText value="#{not assessmentSettings.hasQuestions}" />
--%>
    <h:panelGrid
        summary="#{summary_msg.timed_assmt_sec}">
      <h:panelGroup rendered="#{assessmentSettings.valueMap.timedAssessment_isInstructorEditable==true}">
        <h:selectBooleanCheckbox id="selTimeAssess" onclick="checkTimeSelect();"
         value="#{assessmentSettings.valueMap.hasTimeAssessment}"/>
        <h:outputText value="#{msg.timed_assessment}" />
        <h:selectOneMenu id="timedHours" value="#{assessmentSettings.timedHours}">
          <f:selectItems value="#{assessmentSettings.hours}" />
        </h:selectOneMenu>
        <h:outputText value="#{msg.timed_hours}." />
        <h:selectOneMenu id="timedMinutes" value="#{assessmentSettings.timedMinutes}">
          <f:selectItems value="#{assessmentSettings.mins}" />
        </h:selectOneMenu>
        <h:outputText value="#{msg.timed_minutes}." />
      </h:panelGroup>
    </h:panelGrid>
    <h:panelGrid  >
      <h:panelGroup rendered="#{assessmentSettings.valueMap.timedAssessmentAutoSubmit_isInstructorEditable==true}">
       <h:selectBooleanCheckbox id="automatic"
         value="#{assessmentSettings.autoSubmit}"/>
        <h:outputText value="#{msg.auto_submit}" />
     </h:panelGroup>
    </h:panelGrid>
       <h:outputText value="#{msg.autoSummit_warning}" />
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** ASSESSMENT ORGANIZATION *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.itemAccessType_isInstructorEditable==true or assessmentSettings.valueMap.displayChucking_isInstructorEditable==true or assessmentSettings.valueMap.displayNumbering_isInstructorEditable==true }" >
  <samigo:hideDivision id="div6" title="#{msg.heading_assessment_organization}" >
   <f:verbatim><div class="indnt2"></f:verbatim>
    <!-- NAVIGATION -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.itemAccessType_isInstructorEditable==true}">
  <f:verbatim> <div class="longtext"></f:verbatim> <h:outputLabel value="#{msg.navigation}" /><f:verbatim></div><div class="indnt3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="itemNavigation" value="#{assessmentSettings.itemNavigation}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.linear_access}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.random_access}"/>
        </h:selectOneRadio>
      </h:panelGrid>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- QUESTION LAYOUT -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.displayChunking_isInstructorEditable==true}">
    <f:verbatim><div class="longtext"></f:verbatim><h:outputLabel value="#{msg.question_layout}" /><f:verbatim></div><div class="indnt3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="assessmentFormat" value="#{assessmentSettings.assessmentFormat}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.layout_by_question}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.layout_by_part}"/>
          <f:selectItem itemValue="3" itemLabel="#{msg.layout_by_assessment}"/>
        </h:selectOneRadio>
      </h:panelGrid>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- NUMBERING -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.displayNumbering_isInstructorEditable==true}">
     <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel value="#{msg.numbering}" /> <f:verbatim> </div><div class="indnt3"> </f:verbatim>
       <h:panelGrid columns="2"  >
         <h:selectOneRadio id="itemNumbering" value="#{assessmentSettings.itemNumbering}"  layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{msg.continous_numbering}"/>
           <f:selectItem itemValue="2" itemLabel="#{msg.part_numbering}"/>
         </h:selectOneRadio>
      </h:panelGrid>
 <f:verbatim></div></f:verbatim>
    </h:panelGroup>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** SUBMISSIONS *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.submissionModel_isInstructorEditable==true or assessmentSettings.valueMap.lateHandling_isInstructorEditable==true or assessmentSettings.valueMap.autoSave_isInstructorEditable==true}" >
  <samigo:hideDivision id="div7" title="#{msg.heading_submissions}" >
 <f:verbatim><div class="indnt2"></f:verbatim>

    <!-- NUMBER OF SUBMISSIONS -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.submissionModel_isInstructorEditable==true}">
      <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel value="#{msg.submissions}" /> <f:verbatim> </div> <div class="indnt3"></f:verbatim>
      <f:verbatim><table><tr><td></f:verbatim>
        <h:selectOneRadio id="unlimitedSubmissions" value="#{assessmentSettings.unlimitedSubmissions}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.unlimited_submission}"/>
          <f:selectItem itemValue="0" itemLabel="#{msg.only}" />
        </h:selectOneRadio>
      <f:verbatim></td><td valign="bottom"></f:verbatim>
        <h:panelGroup>
          <h:inputText size="5"  id="submissions_Allowed" value="#{assessmentSettings.submissionsAllowed}" />
          <h:outputLabel value="#{msg.limited_submission}" />
        </h:panelGroup>
      <f:verbatim></td></tr></table></div></f:verbatim>
    </h:panelGroup>

    <!-- LATE HANDLING -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.lateHandling_isInstructorEditable==true}">
      <f:verbatim> <div class="longtext"> </f:verbatim> <h:outputLabel value="#{msg.late_handling}" /> <f:verbatim> </div></f:verbatim>
<f:verbatim><div class="indnt3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="lateHandling" value="#{assessmentSettings.lateHandling}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{msg.not_accept_latesubmission}"/>
          <f:selectItem itemValue="1" itemLabel="#{msg.accept_latesubmission}"/>
        </h:selectOneRadio>
      </h:panelGrid>
   <f:verbatim> </div> </f:verbatim>
    </h:panelGroup>

    <!-- AUTOSAVE -->
<%-- hide for 1.5 release SAM-148
    <h:panelGroup rendered="#{assessmentSettings.valueMap.autoSave_isInstructorEditable==true}">
      <h:outputText value="#{msg.auto_save}" />
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="autoSave" value="#{assessmentSettings.submissionsSaved}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.user_click_save}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.save_automatically}"/>
        </h:selectOneRadio>
      </h:panelGrid>
    </h:panelGroup>
--%>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** SUBMISSION MESSAGE *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.submissionMessage_isInstructorEditable==true or assessmentSettings.valueMap.finalPageURL_isInstructorEditable==true}" >
  <samigo:hideDivision id="div8" title="#{msg.heading_submission_message}" >
   <f:verbatim><div class="indnt2"></f:verbatim>
    <h:panelGroup rendered="#{assessmentSettings.valueMap.submissionMessage_isInstructorEditable==true}">
    <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel value="#{msg.submission_message}" /> <f:verbatim><br/></f:verbatim>

<%-- TODO: DETERMINE IF WE CAN USE RENDERED --%>
       <samigo:wysiwyg rows="140" value="#{assessmentSettings.submissionMessage}"  >
         <f:validateLength maximum="4000"/>
       </samigo:wysiwyg>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>
    <f:verbatim><br/></f:verbatim>
    <h:panelGroup rendered="#{assessmentSettings.valueMap.finalPageURL_isInstructorEditable==true}">
     <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel value="#{msg.submission_final_page_url}" /> <f:verbatim><br/></f:verbatim>
      <h:inputText size="80" id="finalPageUrl" value="#{assessmentSettings.finalPageUrl}" />
      <h:commandButton value="Validate URL" type="button" onclick="javascript:validateUrl();"/>
   <f:verbatim></div></f:verbatim>
    </h:panelGroup>
    <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** FEEDBACK *** -->
  <samigo:hideDivision id="div9" title="#{msg.heading_feedback}" >
<h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackType_isInstructorEditable==true or assessmentSettings.valueMap.feedbackComponents_isInstructorEditable==true}" >

  <!-- FEEDBACK AUTHORING -->
   <f:verbatim> <div class="indnt2"><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{msg.feedback_authoring}"/>
    <f:verbatim> </div> </f:verbatim>
     <f:verbatim> <div class="indnt3"> </f:verbatim>
    <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackAuthoring_isInstructorEditable==true}">
     <h:panelGrid border="0" columns="1">
         <h:selectOneRadio id="feedbackAuthoring" value="#{assessmentSettings.feedbackAuthoring}" layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{msg.questionlevel_feedback}"/>
           <f:selectItem itemValue="2" itemLabel="#{msg.sectionlevel_feedback}"/>
           <f:selectItem itemValue="3" itemLabel="#{msg.both_feedback}"/>
         </h:selectOneRadio>
     </h:panelGrid>
    </h:panelGroup>
    <f:verbatim> </div> </f:verbatim>

 <!-- FEEDBACK DELIVERY -->
 <f:verbatim><div class="longtext"></f:verbatim>
   <h:outputLabel value="#{msg.feedback_delivery}" /> 
    <f:verbatim></div><div class="indnt3"></f:verbatim>
    <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackType_isInstructorEditable==true}">
      <h:panelGrid border="0" columns="1"  >
        <h:selectOneRadio id="feedbackDelivery" value="#{assessmentSettings.feedbackDelivery}"
           layout="pageDirection" onclick="disableAllFeedbackCheck(this.value);">
          <f:selectItem itemValue="1" itemLabel="#{msg.immediate_feedback}"/>
          <f:selectItem itemValue="3" itemLabel="#{msg.no_feedback}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.feedback_by_date}"/>
        </h:selectOneRadio>

      <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackType_isInstructorEditable==true}" >
        <samigo:datePicker value="#{assessmentSettings.feedbackDateString}" size="25" id="feedbackDate" >
          <f:convertDateTime pattern="MM/dd/yyyy" />
        </samigo:datePicker>
      </h:panelGroup>

      </h:panelGrid>
    </h:panelGroup>
<f:verbatim></div></f:verbatim>


    <!-- FEEDBACK COMPONENTS -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackComponents_isInstructorEditable==true}">
     <f:verbatim> <div class="longtext"> </f:verbatim>  <h:outputLabel value="#{summary_msg.feedback_components_sub}" /> <f:verbatim> </div> <div class="indnt3"></f:verbatim>
      <h:panelGrid columns="2"  >

        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentResponse}" id="feedbackCheckbox1"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.student_response}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showQuestionLevelFeedback}" id="feedbackCheckbox2"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.question_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showCorrectResponse}" id="feedbackCheckbox3"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.correct_response}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showSelectionLevelFeedback}" id="feedbackCheckbox4"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.selection_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentScore}" id="feedbackCheckbox5"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.student_assessment_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showGraderComments}" id="feedbackCheckbox6"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.grader_comments}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentQuestionScore}" id="feedbackCheckbox7"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.student_question_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStatistics}" id="feedbackCheckbox8"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{msg.statistics_and_histogram}" />
        </h:panelGroup>

      </h:panelGrid>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
 </samigo:hideDivision>

  <!-- *** GRADING *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.testeeIdentity_isInstructorEditable==true or assessmentSettings.valueMap.toGradebook_isInstructorEditable==true or assessmentSettings.valueMap.recordedScore_isInstructorEditable==true}" >
  <samigo:hideDivision id="div10" title="#{msg.heading_grading}" >
 <f:verbatim><div class="indnt2"></f:verbatim>
<%--     DEBUGGING:
     AnonymousGrading= <h:outputText value="#{assessmentSettings.anonymousGrading}" /> ;
--%>

    <h:panelGroup rendered="#{assessmentSettings.valueMap.testeeIdentity_isInstructorEditable==true}"> <f:verbatim> <div class="longtext"></f:verbatim>  <h:outputLabel value="#{msg.student_identity}" /><f:verbatim></div><div class="indnt3"> </f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="anonymousGrading" value="#{assessmentSettings.anonymousGrading}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{msg.not_anonymous}"/>
          <f:selectItem itemValue="1" itemLabel="#{msg.anonymous}"/>
        </h:selectOneRadio>
      </h:panelGrid>

<f:verbatim></div></f:verbatim>
</h:panelGroup>
    <!-- GRADEBOOK OPTIONS -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.toGradebook_isInstructorEditable==true && assessmentSettings.gradebookExists==true}">
     <f:verbatim>  <div class="longtext">  </f:verbatim> <h:outputLabel value="#{msg.gradebook_options}" /><f:verbatim></div> <div class="indnt3"> </f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="toDefaultGradebook" value="#{assessmentSettings.toDefaultGradebook}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{msg.to_no_gradebook}"/>
          <f:selectItem itemValue="1" itemLabel="#{msg.to_default_gradebook}"/>
        </h:selectOneRadio>
      </h:panelGrid>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- RECORDED SCORE AND MULTIPLES -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.recordedScore_isInstructorEditable==true}">
   <f:verbatim>  <div class="longtext">  </f:verbatim> <h:outputLabel value="#{msg.recorded_score}" /><f:verbatim></div> <div class="indnt3"> </f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="scoringType" value="#{assessmentSettings.scoringType}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.last_score}"/>
        </h:selectOneRadio>
      </h:panelGrid>
     <f:verbatim></div></f:verbatim>
    </h:panelGroup>

   <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>


  <!-- *** COLORS AND GRAPHICS	*** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.bgColor_isInstructorEditable==true or assessmentSettings.valueMap.bgImage_isInstructorEditable==true}" >
  <samigo:hideDivision id="div11" title="#{msg.heading_graphics}" >
    <f:verbatim><div class="indnt2"></f:verbatim>
    <h:panelGrid columns="1" columnClasses="shorttext" >
      <h:panelGroup rendered="#{assessmentSettings.valueMap.bgColor_isInstructorEditable==true}">
        <h:outputLabel value="#{msg.background_color}"/></b>
        <samigo:colorPicker value="#{assessmentSettings.bgColor}" size="10" id="pickColor"/>
      </h:panelGroup>
      <h:panelGroup rendered="#{assessmentSettings.valueMap.bgImage_isInstructorEditable==true}">
        <h:outputLabel value="#{msg.background_image}"/></b>
        <h:inputText size="80" value="#{assessmentSettings.bgImage}"/>
      </h:panelGroup>
    </h:panelGrid>
    <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** META *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.metadataAssess_isInstructorEditable==true}">
  <samigo:hideDivision title="#{msg.heading_metadata}" id="div13">
    <f:verbatim><div class="indnt2"></f:verbatim>
   <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel value="#{msg.assessment_metadata}" /> <f:verbatim></div><div class="indnt3"></f:verbatim>
    <h:panelGrid columns="2" columnClasses="shorttext">
      <h:outputLabel value="#{msg.metadata_keywords}"/>
      <h:inputText size="80" value="#{assessmentSettings.keywords}"/>

      <h:outputLabel value="#{msg.metadata_objectives}"/>
      <h:inputText size="80" value="#{assessmentSettings.objectives}"/>

      <h:outputLabel value="#{msg.metadata_rubrics}"/>
      <h:inputText size="80" value="#{assessmentSettings.rubrics}"/>
    </h:panelGrid>
   <f:verbatim></div><div class="longtext"></f:verbatim>   <h:outputLabel value="#{msg.record_metadata}" /> <f:verbatim></div><div class="indnt3"></f:verbatim>
    <h:panelGrid columns="2" >
<%-- see bug# SAM-117 -- no longer required in Samigo
     <h:selectBooleanCheckbox
       rendered="#{assessmentSettings.valueMap.metadataParts_isInstructorEditable==true}"
       value="#{assessmentSettings.valueMap.hasMetaDataForPart}"/>
     <h:outputText value="#{msg.metadata_parts}"
       rendered="#{assessmentSettings.valueMap.metadataParts_isInstructorEditable==true}"/>
--%>
     <h:selectBooleanCheckbox
       rendered="#{assessmentSettings.valueMap.metadataQuestions_isInstructorEditable==true}"
       value="#{assessmentSettings.valueMap.hasMetaDataForQuestions}"/>
     <h:outputText value="#{msg.metadata_questions}"
       rendered="#{assessmentSettings.valueMap.metadataQuestions_isInstructorEditable==true}" />
    </h:panelGrid>
    <f:verbatim></div></div></f:verbatim>
  </samigo:hideDivision>
 </h:panelGroup>
</div>
 <p class="act">
  <h:commandButton  value="#{msg.button_save_and_publish}" type="submit" styleClass="active" rendered="#{assessmentSettings.hasQuestions}"
      action="#{assessmentSettings.getOutcomePublish}" disabled="#{not assessmentSettings.hasQuestions}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmPublishAssessmentListener" />
  </h:commandButton>
<h:commandButton  value="#{msg.button_save_and_publish}" type="submit" rendered="#{not assessmentSettings.hasQuestions}"
      action="#{assessmentSettings.getOutcomePublish}" disabled="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmPublishAssessmentListener" />
  </h:commandButton>

<h:commandButton type="submit" value="#{msg.button_save_settings}" action="#{assessmentSettings.getOutcomeSave}" rendered="#{not assessmentSettings.hasQuestions}" styleClass="active">
      <f:param name="assessmentId" value="#{assessmentSettings.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SaveAssessmentSettingsListener" />
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_save_settings}" action="#{assessmentSettings.getOutcomeSave}" rendered="#{assessmentSettings.hasQuestions}">
      <f:param name="assessmentId" value="#{assessmentSettings.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SaveAssessmentSettingsListener" />
  </h:commandButton>
  <h:commandButton value="#{msg.button_cancel}" type="submit" action="editAssessment" >
      <f:param name="assessmentId" value="#{assessmentSettings.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>
</p>
</h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>
