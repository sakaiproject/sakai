<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
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
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages"
     var="msg"/>
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.TemplateMessages"
     var="summary_msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.sakai_assessment_manager} - #{msg.settings}" /></title>
      <samigo:script path="/jsf/widget/colorpicker/colorpicker.js"/>
      <samigo:script path="/jsf/widget/datepicker/datepicker.js"/>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      </head>
    <body onload="hideUnhideAllDivsWithWysiwyg('none');;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<h:form id="assessmentSettingsAction">
  <h:inputHidden id="assessmentId" value="#{publishedSettings.assessmentId}"/>

  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>

    <h3>
     <h:outputText id="x1"
       value="#{msg.settings} - " />
     <h:outputText value="#{publishedSettings.title}" />
    </h3>

<div class="indnt1">
  <!-- *** GENERAL TEMPLATE INFORMATION *** -->

  <samigo:hideDivision id="div1" title="#{msg.heading_assessment_introduction}" >
<div class="indnt2">
    <h:panelGrid columns="2" columnClasses="shorttext"
      summary="#{summary_msg.enter_template_info_section}">
        <h:outputLabel value="#{msg.assessment_title}"/>
        <h:inputText size="80" value="#{publishedSettings.title}"  disabled="true" />

        <h:outputLabel value="#{msg.assessment_creator}"/>

        <h:outputText value="#{publishedSettings.creator}"/>

        <h:outputLabel value="#{msg.assessment_authors}"/>

       <%-- this disabled is so weird - daisyf --%>
        <h:inputText size="80" value="#{publishedSettings.authors}" disabled="true"/>

        <h:outputLabel value="#{msg.assessment_description}"/>

          <h:outputText value="#{publishedSettings.description}<br /><br /><br />"
            escape="false"/>

    </h:panelGrid>
<f:verbatim></div></f:verbatim>
  </samigo:hideDivision>


  <!-- *** DELIVERY DATES *** -->
  <samigo:hideDivision id="div2" title="#{msg.heading_assessment_delivery_dates}" >
    <div class="indnt2">
    <h:panelGrid columns="3" columnClasses="shorttext"
      summary="#{summary_msg.delivery_dates_sec}">

      <h:selectBooleanCheckbox
        value="#{publishedSettings.valueMap.hasAvailableDate}"/>
      <h:outputText value="#{msg.assessment_available_date}" />
      <samigo:datePicker value="#{publishedSettings.startDateString}" size="25" id="startDate" />

      <h:selectBooleanCheckbox
        value="#{publishedSettings.valueMap.dueDate}"/>
      <h:outputText value="#{msg.assessment_due_date}" />
      <samigo:datePicker value="#{publishedSettings.dueDateString}" size="25" id="endDate" />

      <h:selectBooleanCheckbox
        value="#{publishedSettings.valueMap.hasRetractDate}"/>
      <h:outputText value="#{msg.assessment_retract_date}" />
      <samigo:datePicker value="#{publishedSettings.retractDateString}" size="25" id="retractDate" />
    </h:panelGrid>
    </div>
  </samigo:hideDivision>

  <!-- *** RELEASED TO *** -->

  <samigo:hideDivision title="#{msg.heading_released_to}" id="div3">
<div class="indnt2">
    <h:panelGrid   summary="#{summary_msg.released_to_info_sec}">
<%--
      <h:selectManyCheckbox disabled="true" layout="pagedirection" value="#{publishedSettings.targetSelected}">
        <f:selectItems value="#{assessmentSettings.publishingTargets}" />
      </h:selectManyCheckbox>
--%>
      <h:selectOneRadio disabled="true" layout="pagedirection" value="#{publishedSettings.firstTargetSelected}">
        <f:selectItems value="#{assessmentSettings.publishingTargets}" />
      </h:selectOneRadio>
      <h:panelGroup styleClass="longtext">
    <h:outputLabel value="#{msg.published_assessment_url}: " />
        <h:outputText value="#{publishedSettings.publishedUrl}" />
      </h:panelGroup>
    </h:panelGrid>
</div>
<%-- dublicate information

    <h:panelGrid columns="2">
      <h:outputText value="#{msg.published_assessment_url}: " />
      <h:outputLink value="#{publishedSettings.publishedUrl}" target="newWindow">
        <h:outputText value="#{publishedSettings.publishedUrl}" />
      </h:outputLink>
    </h:panelGrid>
--%>
  </samigo:hideDivision>

  <!-- *** HIGH SECURITY *** -->
  <samigo:hideDivision title="#{msg.heading_high_security}" id="div4">
<div class="indnt2">
    <h:panelGrid border="0" columns="3" columnClasses="longtext"
        summary="#{summary_msg.high_security_sec}">
      <h:selectBooleanCheckbox value="#{assessmentSettings.valueMap.hasSpecificIP}"
         disabled="true"/>
      <h:outputText value="#{msg.high_security_allow_only_specified_ip}" />
      <h:inputTextarea value="#{publishedSettings.ipAddresses}" cols="40" rows="5"
        disabled="true"/>
      <h:selectBooleanCheckbox  disabled="true"
         value="#{publishedSettings.valueMap.hasUsernamePassword}"/>
      <h:outputText value="#{msg.high_security_secondary_id_pw}"/>
      <h:panelGrid border="0" columns="2"  >
        <h:outputLabel value="#{msg.high_security_username}"/>
        <h:inputText size="20" value="#{publishedSettings.username}"
          disabled="true"/>

        <h:outputLabel value="#{msg.high_security_password}"/>
        <h:inputText size="20" value="#{publishedSettings.password}"
          disabled="true"/>
      </h:panelGrid>
    </h:panelGrid>
</div>
  </samigo:hideDivision>


  <!-- *** TIMED *** -->
  <samigo:hideDivision id="div5" title="#{msg.heading_timed_assessment}">
<div class="indnt2">
<%--DEBUGGING:
     Time Limit= <h:outputText value="#{publishedSettings.timeLimit}" /> ;
     Hours= <h:outputText value="#{publishedSettings.timedHours}" /> ;
     Min= <h:outputText value="#{publishedSettings.timedMinutes}" /> ;
     hasQuestions?= <h:outputText value="#{not publishedSettings.hasQuestions}" />
--%>
    <h:panelGrid
        summary="#{summary_msg.timed_assmt_sec}">
      <h:panelGroup>
        <h:selectBooleanCheckbox  disabled="true"
         value="#{publishedSettings.valueMap.hasTimeAssessment}"/>
        <h:outputText value="#{msg.timed_assessment}" />
        <h:selectOneMenu id="timedHours" value="#{publishedSettings.timedHours}"
          disabled="true">
          <f:selectItems value="#{publishedSettings.hours}" />
        </h:selectOneMenu>
        <h:outputText value="#{msg.timed_hours}." />
        <h:selectOneMenu id="timedMinutes" value="#{publishedSettings.timedMinutes}"
           disabled="true">
          <f:selectItems value="#{publishedSettings.mins}" />
        </h:selectOneMenu>
        <h:outputText value="#{msg.timed_minutes}." />
      </h:panelGroup>
    </h:panelGrid>
    <h:panelGrid  >
      <h:panelGroup>
       <h:selectBooleanCheckbox  disabled="true"
         value="#{publishedSettings.valueMap.hasAutosubmit}"/>
        <h:outputText value="#{msg.auto_submit}" />
     </h:panelGroup>
    </h:panelGrid>
</div>
  </samigo:hideDivision>

  <!-- *** ASSESSMENT ORGANIZATION *** -->
  <samigo:hideDivision id="div6" title="#{msg.heading_assessment_organization}" >
<%--     DEBUGGING:  Layout= <h:outputText value="#{publishedSettings.assessmentFormat}" /> ;
     navigation= <h:outputText value="#{publishedSettings.itemNavigation}" /> ;
     numbering= <h:outputText value="#{publishedSettings.itemNumbering}" />
--%>
    <!-- NAVIGATION -->
   <div class="indnt2">
    <div class="longtext"><h:outputLabel value="#{msg.navigation}" /></div><div class="indnt3">

      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="itemNavigation"  disabled="true"
           value="#{publishedSettings.itemNavigation}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.linear_access}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.random_access}"/>
        </h:selectOneRadio>
      </h:panelGrid>
   </div>
    <!-- QUESTION LAYOUT -->
    <div class="longtext"><h:outputLabel value="#{msg.question_layout}" /></div><div class="indnt3">

      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="assessmentFormat"  disabled="true"
            value="#{publishedSettings.assessmentFormat}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.layout_by_question}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.layout_by_part}"/>
          <f:selectItem itemValue="3" itemLabel="#{msg.layout_by_assessment}"/>
        </h:selectOneRadio>
      </h:panelGrid>
    </div>

    <!-- NUMBERING -->
    <div class="longtext"><h:outputLabel value="#{msg.numbering}" /></div><div class="indnt3">

       <h:panelGrid columns="2"  >
         <h:selectOneRadio id="itemNumbering"  disabled="true"
             value="#{publishedSettings.itemNumbering}"  layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{msg.continous_numbering}"/>
           <f:selectItem itemValue="2" itemLabel="#{msg.part_numbering}"/>
         </h:selectOneRadio>
      </h:panelGrid>
    </div></div>
  </samigo:hideDivision>

  <!-- *** SUBMISSIONS *** -->
  <samigo:hideDivision id="div7" title="#{msg.heading_submissions}" >
<%--     DEBUGGING:
     Unlimited= <h:outputText value="#{publishedSettings.unlimitedSubmissions}" /> ;
     Submissions= <h:outputText value="#{publishedSettings.submissionsAllowed}" /> ;
     lateHandling= <h:outputText value="#{publishedSettings.lateHandling}" />
--%>
<div class="indnt2">
    <!-- NUMBER OF SUBMISSIONS -->
     <div class="longtext"><h:outputLabel value="#{msg.submissions}" /></div> <div class="indnt3"><f:verbatim><table><tr><td></f:verbatim>

        <h:selectOneRadio id="unlimitedSubmissions"  disabled="true"
            value="#{publishedSettings.unlimitedSubmissions}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.unlimited_submission}"/>
          <f:selectItem itemValue="0" itemLabel="#{msg.only}" />

        </h:selectOneRadio>
             <f:verbatim></td><td valign="bottom"></f:verbatim>
            <h:panelGroup>
              <h:inputText size="5"  disabled="true"
                  value="#{publishedSettings.submissionsAllowed}" />
              <h:outputLabel value="#{msg.limited_submission}" />
            </h:panelGroup>
    <f:verbatim></td></tr></table></f:verbatim>
     </div>
    <!-- LATE HANDLING -->

   <div class="longtext"><h:outputLabel value="#{msg.late_handling}" /></div><div class="indnt3">
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="lateHandling"  disabled="true"
            value="#{publishedSettings.lateHandling}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{msg.not_accept_latesubmission}"/>
          <f:selectItem itemValue="1" itemLabel="#{msg.accept_latesubmission}"/>
        </h:selectOneRadio>
      </h:panelGrid>
    </div>

    <!-- AUTOSAVE -->
<%-- hide for 1.5 release SAM-148
    <div class="longtext"><h:outputLabel value="#{msg.auto_save}" /></div>
    <div class="indnt3">
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="autoSave"  disabled="true"
            value="#{publishedSettings.submissionsSaved}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.user_click_save}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.save_automatically}"/>
        </h:selectOneRadio>
      </h:panelGrid>
    </div>
--%>
</div>
  </samigo:hideDivision>

  <!-- *** SUBMISSION MESSAGE *** -->
  <samigo:hideDivision id="div8" title="#{msg.heading_submission_message}" >
    <div class="indnt2"><div class="longtext">
      <h:outputLabel value="#{msg.submission_message}" />
      <br/>
      <h:panelGrid width="630" border="1">
        <h:outputText value="#{publishedSettings.submissionMessage}<br /><br /><br />"
          escape="false"/>
      </h:panelGrid>
<%--
      <h:inputTextarea cols="80" rows="5"  disabled="true"
          value="#{publishedSettings.submissionMessage}" />
--%>

    <br/>
 </div>
  <div class="longtext">
      <h:outputLabel value="#{msg.submission_final_page_url}" /><br/>
      <h:inputText size="80"  disabled="true" value="#{publishedSettings.finalPageUrl}" />
</div></div>

  </samigo:hideDivision>

  <!-- *** FEEDBACK *** -->
  <samigo:hideDivision id="div9" title="#{msg.heading_feedback}" >
   <div class="indnt2">
    <div class="longtext"><h:outputLabel value="#{msg.feedback_delivery}" /></div><div class="indnt3">
    <h:panelGroup>
      <h:panelGrid columns="1"  >
        <h:selectOneRadio id="feedbackDelivery"  disabled="true"
             value="#{publishedSettings.feedbackDelivery}"
           layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.immediate_feedback}"/>
          <f:selectItem itemValue="3" itemLabel="#{msg.no_feedback}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.feedback_by_date}"/>
        </h:selectOneRadio>
        <h:panelGroup>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
<h:inputText value="#{publishedSettings.feedbackDateString}" size="25" disabled="true"/>

<%-- REMOVED AS NOT ABLE TO DISABLE DATEPICKER
        <samigo:datePicker value="#{publishedSettings.feedbackDateString}" size="25" id="feedbackDate"/>
      --%>
        </h:panelGroup>
      </h:panelGrid>
    </h:panelGroup>
</div><div class="longtext">
   <h:outputLabel value="#{msg.feedback_components}" /></div><div class="indnt3">
    <h:panelGroup>
      <h:panelGrid columns="2"  >
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
              value="#{publishedSettings.showQuestionText}"/>
          <h:outputText value="#{msg.question_text}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
              value="#{publishedSettings.showQuestionLevelFeedback}"/>
          <h:outputText value="#{msg.question_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
              value="#{publishedSettings.showStudentResponse}"/>
          <h:outputText value="#{msg.student_response}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
             value="#{publishedSettings.showSelectionLevelFeedback}"/>
          <h:outputText value="#{msg.selection_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
              value="#{publishedSettings.showCorrectResponse}"/>
          <h:outputText value="#{msg.correct_response}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
              value="#{publishedSettings.showStudentScore}"/>
          <h:outputText value="#{msg.student_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
              value="#{publishedSettings.showGraderComments}"/>
          <h:outputText value="#{msg.grader_comments}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox  disabled="true"
              value="#{publishedSettings.showStatistics}"/>
          <h:outputText value="#{msg.statistics_and_histogram}" />
        </h:panelGroup>
      </h:panelGrid>
    </h:panelGroup></div></div>
  </samigo:hideDivision>

  <!-- *** GRADING *** -->
  <samigo:hideDivision id="div10" title="#{msg.heading_grading}" >
<div class="indnt2">
    <div class="longtext"><h:outputLabel value="#{msg.student_identity}" /></div><div class="indnt3">
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="anonymousGrading"  disabled="true"
            value="#{publishedSettings.anonymousGrading}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{msg.not_anonymous}"/>
          <f:selectItem itemValue="1" itemLabel="#{msg.anonymous}"/>
        </h:selectOneRadio>
      </h:panelGrid>
</div>
    <!-- GRADEBOOK OPTIONS -->
    <div class="longtext"><h:outputLabel value="#{msg.gradebook_options}" /></div><div class="indnt3">
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="toDefaultGradebook"
            value="#{publishedSettings.toDefaultGradebook}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{msg.to_no_gradebook}"/>
          <f:selectItem itemValue="1" itemLabel="#{msg.to_default_gradebook}"/>
        </h:selectOneRadio>
      </h:panelGrid>
</div>
    <!-- RECORDED SCORE AND MULTIPLES -->
    <div class="longtext"><h:outputLabel value="#{msg.recorded_score}" /></div><div class="indnt3">
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="scoringType"  disabled="true"
            value="#{publishedSettings.scoringType}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{msg.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{msg.last_score}"/>
        </h:selectOneRadio>
      </h:panelGrid>
</div></div>
  </samigo:hideDivision>

  <!-- *** COLORS AND GRAPHICS	*** -->
  <samigo:hideDivision id="div11" title="#{msg.heading_graphics}" >
<div class="indnt2">
    <h:panelGrid columns="2" columnClasses="shorttext" >
      <h:outputLabel value="#{msg.background_color}" />
      <h:inputText size="80" value="#{publishedSettings.bgColor}"
          disabled="true" />

      <h:outputLabel value="#{msg.background_image}"/>
      <h:inputText size="80" value="#{publishedSettings.bgImage}"
         disabled="true" />
    </h:panelGrid>
</div>
  </samigo:hideDivision>

  <!-- *** META *** -->

  <samigo:hideDivision title="#{msg.heading_metadata}" id="div13">
   <div class="indnt2"><div class="longtext"> <h:outputLabel value="#{msg.assessment_metadata}" /> </div><div class="indnt3">
    <h:panelGrid columns="2" columnClasses="shorttext">
      <h:outputLabel value="#{msg.metadata_keywords}"/>
      <h:inputText size="80" value="#{publishedSettings.keywords}"  disabled="true"/>

    <h:outputLabel value="#{msg.metadata_objectives}"/>
      <h:inputText size="80" value="#{publishedSettings.objectives}"  disabled="true"/>

      <h:outputLabel value="#{msg.metadata_rubrics}"/>
      <h:inputText size="80" value="#{publishedSettings.rubrics}"  disabled="true"/>
    </h:panelGrid></div>
    <div class="longtext"> <h:outputLabel value="#{msg.record_metadata}" /></div><div class="indnt3">
    <h:panelGrid columns="2"  >
<%-- see bug# SAM-117 -- no longer required in Samigo
     <h:selectBooleanCheckbox  disabled="true"
       value="#{publishedSettings.valueMap.hasMetaDataForPart}"/>
     <h:outputText value="#{msg.metadata_parts}"/>
--%>
     <h:selectBooleanCheckbox disabled="true"
       value="#{publishedSettings.valueMap.hasMetaDataForQuestions}"/>
 <h:outputText value="#{msg.metadata_questions}" />
    </h:panelGrid>
</div></div>
  </samigo:hideDivision>

</div>

<p class="act">
  <h:commandButton  type="submit" value="#{msg.button_save_settings}" action="saveSettings"  styleClass="active">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SavePublishedSettingsListener" />
  </h:commandButton>
  <h:commandButton  value="#{msg.button_cancel}" type="submit" action="author"  />
</p>
</h:form>
<!-- end content -->
</div>

      </body>
    </html>
  </f:view>
