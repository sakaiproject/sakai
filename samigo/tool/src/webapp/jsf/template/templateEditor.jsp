<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>


<!-- $Id$
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
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  <f:view>
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.TemplateMessages"
     var="msg"/>
 
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.template_editor}" /></title>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      </head>
      <body onload="javascript:hideUnhideAllDivsWithWysiwyg('none');;<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">
  <h:form id="templateEditorForm">
  
   <p class="navIntraTool" >
   <h:commandLink action="author" id="authorLink" immediate="true">
      <h:outputText value="#{msg.link_assessments}" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="template" id="templateLink" immediate="true">
      <h:outputText value="#{msg.index_templates}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" id="poolLink" immediate="true">
      <h:outputText value="#{msg.link_pool}" />
    </h:commandLink>
   </p>
<h3><h:outputText value="#{msg.template_editor}"/>
     <h:outputText value="#{template.templateName}"/>
</h3>
 <h:outputText value="#{msg.template_instructions}"/>
 <h:messages styleClass="validation"/>
  <!-- *** GENERAL TEMPLATE INFORMATION *** -->
  <div class="indnt1">
  <samigo:hideDivision id="div1" title="#{msg.template_inform}" >
    <div class="indnt2">
       <h:selectBooleanCheckbox id="infocanbeviewed"
          value="#{template.valueMap.templateInfo_isInstructorEditable}"/>
     <h:outputText value="#{msg.template_canbeviewed}"/>
  <div class="indnt3">
 <h:panelGrid columns="2" columnClasses="shorttext"
      summary="#{msg.enter_template_info_section}">


      <h:outputLabel for="template_title" value="#{msg.template_title}"/>
      <h:inputText id="template_title" value="#{template.templateName}" size="30"/>


      <h:outputLabel for="author_opt" value="#{msg.author_opt}"/>
      <h:inputText id="author_opt" value="#{template.templateAuthor}" size="30"/>

      <!-- WYSIWYG -->

      <h:outputLabel for="description_intro_opt" value="#{msg.description_intro_opt}"/>
     <samigo:wysiwyg rows="140" value="#{template.templateDescription}"  >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
    </h:panelGrid>
    </div></div>
  </samigo:hideDivision>

  <!-- *** AUTHORSHIP *** -->
  <samigo:hideDivision title="#{msg.introduction}" id="div2">
   <div class="indnt2">
    <div class="longtext"><h:outputLabel for="template_edited" value="#{msg.template_canbeedited}"/></div>
 <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.introduction_sec}">

      <h:selectBooleanCheckbox id="authors"
          value="#{template.valueMap.assessmentAuthor_isInstructorEditable}"/>
      <h:outputLabel for="authors" value="#{msg.authors}"/>

<%-- bug# SAM156 - i don't think creator should be editable, daisyf 01/26/05
      <h:selectBooleanCheckbox id="creator"
          value="#{template.valueMap.assessmentCreator_isInstructorEditable}"/>
      <h:outputLabel for="creator" value="#{msg.creator}"/>
--%>
      <h:selectBooleanCheckbox id="description_intro"
          value="#{template.valueMap.description_isInstructorEditable}"/>
      <h:outputLabel for="description_intro" value="#{msg.description_intro}"/>
    </h:panelGrid>
    </div> </div>
  </samigo:hideDivision>

<%-- get rid of delivery dates according to the new mockup,
  <!-- *** DELIVERY DATES *** -->
  <samigo:hideDivision title="#{msg.delivery_dates}" id="div3">
   <div class="indnt2">
  <div class="longtext">
    <h:outputLabel for="deli_edited" value="#{msg.template_canbeedited}"/></div>
 <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.delivery_dates_sec}">

      <h:selectBooleanCheckbox id="due_date"
        value="#{template.valueMap.dueDate_isInstructorEditable}"/>
      <h:outputLabel for="due_date" value="#{msg.due_date}"/>

      <h:selectBooleanCheckbox id="retract_date"
        value="#{template.valueMap.retractDate_isInstructorEditable}"/>
      <h:outputLabel for="retract_date" value="#{msg.retract_date}"/>

    </h:panelGrid>
   </div></div>
  </samigo:hideDivision>
 --%>

  <!-- *** RELEASED TO  *** -->
  <samigo:hideDivision title="#{msg.released_to}" id="div4">
   <div class="indnt2">
  <div class="longtext">
    <h:outputLabel for="released_edited" value="#{msg.template_canbeedited}"/></div>
   <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.released_to_info_sec}">

      <h:selectBooleanCheckbox id="anon"
        value="#{template.valueMap.anonymousRelease_isInstructorEditable}"/>
      <h:outputLabel for="anon" value="#{msg.anon}"/>

      <h:selectBooleanCheckbox id="auth_users"
        value="#{template.valueMap.authenticatedRelease_isInstructorEditable}"/>
      <h:outputLabel for="auth_users" value="#{msg.auth_users}"/>

    </h:panelGrid>
  </div></div>
  </samigo:hideDivision>

  <!-- *** HIGH SECURITY *** -->
  <samigo:hideDivision title="#{msg.high_security}" id="div5">
    <div class="indnt2">
   <div class="longtext">
    <h:outputLabel for="sec_edited" value="#{msg.template_canbeedited}"/></div>
    <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.high_security_sec}">

      <h:selectBooleanCheckbox id="allow_only_specified_ip"
        value="#{template.valueMap.ipAccessType_isInstructorEditable}"/>
      <h:outputLabel for="allow_only_specified_ip" value="#{msg.allow_only_specified_ip}"/>

      <h:selectBooleanCheckbox id="secondary_id_pw"
        value="#{template.valueMap.passwordRequired_isInstructorEditable}"/>
      <h:outputLabel for="secondary_id_pw" value="#{msg.secondary_id_pw}"/>

    </h:panelGrid>
     </div></div>
  </samigo:hideDivision>

  <!-- *** TIMED ASSESSMENTS *** -->
  <samigo:hideDivision title="#{msg.timed_assmt}" id="div6">
    <div class="indnt2">
  <div class="longtext">
    <h:outputLabel for="time_edited" value="#{msg.template_canbeedited}"/></div>
    <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.timed_assmt_sec}">

      <h:selectBooleanCheckbox id="timed_assmt"
        value="#{template.valueMap.timedAssessment_isInstructorEditable}"/>
      <h:outputLabel for="timed_assmt" value="#{msg.timed_assmt}"/>

      <h:selectBooleanCheckbox id="auto_submit_expires"
        value="#{template.valueMap.timedAssessmentAutoSubmit_isInstructorEditable}"/>
      <h:outputLabel for="auto_submit_expires" value="#{msg.auto_submit_expires}"/>

    </h:panelGrid>
   </div></div>
  </samigo:hideDivision>


  <!-- *** ASSESSMENT ORGANIZATION *** -->
  <samigo:hideDivision title="#{msg.organization}" id="div7">

    <!-- h:panelGrid columns="1" columnClasses="indnt2"
      summary="#{msg.organization}" -->

    <!-- NAVIGATION -->
 <div class="indnt2">
      <div class="longtext"><h:outputLabel for="nav" value="#{msg.navigation}"/></div>
      <div class="indnt3">
     <h:panelGrid columns="2">
        <h:selectBooleanCheckbox id="navigation"
          value="#{template.valueMap.itemAccessType_isInstructorEditable}"/>
        <h:outputLabel for="navigation" value="#{msg.template_canbedefault}"/>

        <h:outputText value=" "/>
         <h:panelGroup>
        <!-- h:panelGroup style="indnt4" -->
         <h:selectOneRadio layout="pageDirection" value="#{template.itemAccessType}" required="true">
         <f:selectItem itemValue="1"
          itemLabel="#{msg.linear_access_no_return}"/>
         <f:selectItem itemValue="2"
          itemLabel="#{msg.random_access_questions_toc}"/>
         </h:selectOneRadio>
        </h:panelGroup>
      </h:panelGrid>
     </div>

    <!-- QUESTION LAYOUT -->
     <div class="longtext"><h:outputLabel for="layout" value="#{msg.question_layout}"/></div>
    <div class="indnt3">
     <h:panelGrid columns="2" summary="#{msg.question_layout_sub}">

        <h:selectBooleanCheckbox id="question_layout"
          value="#{template.valueMap.displayChunking_isInstructorEditable}"/>
        <h:outputLabel for="question_layout" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
       <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.displayChunking}" required="true">
      <f:selectItem itemValue="1"
        itemLabel="#{msg.quest_sep_page}"/>
      <f:selectItem itemValue="2"
        itemLabel="#{msg.part_sep_page}"/>
      <f:selectItem itemValue="3"
        itemLabel="#{msg.complete_displayed_on_one_page}"/>
    </h:selectOneRadio>
    </h:panelGroup>
    </h:panelGrid>
    </div>
    <!-- NUMBERING -->
      <div class="longtext"><h:outputLabel for="mum" value="#{msg.numbering}"/></div>
      <div class="indnt3">
     <h:panelGrid columns="2" summary="#{msg.numbering_sub}">

        <h:selectBooleanCheckbox id="numbering"
          value="#{template.valueMap.displayNumbering_isInstructorEditable}"/>
        <h:outputLabel for="numbering" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
       <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.questionNumbering}" required="true">
      <f:selectItem itemValue="1" itemLabel="#{msg.continuous_num_parts}"/>
      <f:selectItem itemValue="2" itemLabel="#{msg.restart_num_part}"/>
    </h:selectOneRadio>
    </h:panelGroup>
    </h:panelGrid>

</div></div>
  </samigo:hideDivision>

  <!-- *** SUBMISSIONS *** -->

  <samigo:hideDivision title="#{msg.submissions}" id="div8">
 <div class="indnt2">

    <!-- NUMBER OF SUBMISSIONS -->

    <div class="longtext"><h:outputLabel for="subs" value="#{msg.number_of_sub_allowed}"/></div>
       <div class="indnt3">

     <h:panelGrid columns="2"
      summary="#{msg.number_of_sub_allowed_sub}">

        <h:selectBooleanCheckbox id="number_of_sub_allowed"
          value="#{template.valueMap.submissionModel_isInstructorEditable}"/>
        <h:outputLabel for="number_of_sub_allowed" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
    <h:panelGroup>
     <h:selectOneRadio layout="pageDirection" value="#{template.submissionModel}" required="true">
       <f:selectItem itemValue="1" itemLabel="#{msg.unlimited}"/>
       <f:selectItem itemValue="0" itemLabel="#{msg.only}"/>
    </h:selectOneRadio>
    <h:inputText value="#{template.submissionNumber}"
       id="submissions_allowed" size="5"/> <h:outputText value=" #{msg.submissions_allowed}"/>
    </h:panelGroup>
    </h:panelGrid>
</div>
    <!-- LATE HANDLING -->
    <div class="longtext"><h:outputLabel for="latehandling" value="#{msg.late_handling}"/></div>
        <div class="indnt3">
     <h:panelGrid columns="2"
      summary="#{msg.late_handling_sub">

        <h:selectBooleanCheckbox id="late_handling"
          value="#{template.valueMap.lateHandling_isInstructorEditable}"/>
        <h:outputLabel for="late_handling" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.lateHandling}" required="true" >
      <f:selectItem itemValue="2"
        itemLabel="#{msg.late_sub_not_accepted}"/>
      <f:selectItem itemValue="1"
        itemLabel="#{msg.late_submissions_accepted_tagged_late}"/>
    </h:selectOneRadio>
    </h:panelGroup>
    </h:panelGrid>
</div></div>
    <!-- AUTOSAVE -->
<%-- hide for 1.5 release SAM-148
    <h:outputText style="h4" styleClass="indnt2,plain"
      value="#{msg.auto_save}"/>

     <h:panelGrid columns="2"
      summary="#{msg.auto_save_sub}">

        <h:selectBooleanCheckbox id="auto_save"
          value="#{template.valueMap.autoSave_isInstructorEditable}"/>
        <h:outputLabel for="auto_save" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.autoSave}" required="true">
      <f:selectItem itemValue="0" itemLabel="#{msg.user_must_click}"/>
      <f:selectItem itemValue="1" itemLabel="#{msg.all_input_saved_auto}"/>
    </h:selectOneRadio>
   </h:panelGroup>
   </h:panelGrid>
--%>
  </samigo:hideDivision>

  <!-- *** SUBMISSION MESSAGE *** -->
  <samigo:hideDivision title="#{msg.submission_message}" id="div9">
 <div class="indnt2">
  <div class="longtext">
    <h:outputLabel for="sub_mess" value="#{msg.template_canbeedited}"/></div>
     <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.timed_assmt_sec}">

      <h:selectBooleanCheckbox id="submission_message"
        value="#{template.valueMap.submissionMessage_isInstructorEditable}"/>
      <h:outputLabel for="submission_message" value="#{msg.submission_message}"/>

      <h:selectBooleanCheckbox id="final_page_url"
        value="#{template.valueMap.finalPageURL_isInstructorEditable}"/>
      <h:outputLabel for="final_page_url" value="#{msg.final_page_url}"/>
    </h:panelGrid>
    </div></div>
  </samigo:hideDivision>

  <!-- *** FEEDBACK *** -->

  <samigo:hideDivision title="#{msg.feedback}" id="div10">
 <div class="indnt2">

  <!-- FEEDBACK AUTHORING--> 
     <div class="longtext">
    <h:outputLabel for="fb_deli" value="#{msg.feedback_authoring}"/></div>
    <div class="indnt3">
     <h:panelGrid columns="2"
      summary="#{msg.feedback_authoring_sub}">

        <h:selectBooleanCheckbox id="feedback_authoring"
          value="#{template.valueMap.feedbackAuthoring_isInstructorEditable}"/>
        <h:outputLabel for="feedback_authoring" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.feedbackAuthoring}"
      required="true">
      <f:selectItem itemValue="1"
        itemLabel="#{msg.questionlevel_feedback}"/>
      <f:selectItem itemValue="2"
        itemLabel="#{msg.sectionlevel_feedback}"/>
      <f:selectItem itemValue="3"
        itemLabel="#{msg.both_feedback}"/>
    </h:selectOneRadio>
   </h:panelGroup>
   </h:panelGrid>
</div>

    <!-- FEEDBACK DELIVERY -->
     <div class="longtext">
    <h:outputLabel for="fb_deli" value="#{msg.feedback_delivery}"/></div>
 <div class="indnt3">
     <h:panelGrid columns="2"
      summary="#{msg.feedback_delivery_sub}">

        <h:selectBooleanCheckbox id="feedback_delivery"
          value="#{template.valueMap.feedbackType_isInstructorEditable}"/>
        <h:outputLabel for="feedback_delivery" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.feedbackType}"
      required="true">
      <f:selectItem itemValue="1"
        itemLabel="#{msg.immediate_feedback}"/>
      <f:selectItem itemValue="2"
        itemLabel="#{msg.feedback__disp_spec_date}"/>
      <f:selectItem itemValue="3"
        itemLabel="#{msg.no_feedback}"/>
    </h:selectOneRadio>
   </h:panelGroup>
   </h:panelGrid>
</div>
    <!-- FEEDBACK COMPONENTS -->
     <div class="longtext"><h:outputLabel for="fb_comp" value="#{msg.select_feedback_comp}"/></div>
      <div class="indnt3">
     <h:panelGrid columns="2"
      summary="#{msg.feedback_components_sub}">

        <h:selectBooleanCheckbox id="select_feedback_comp"
          value="#{template.valueMap.feedbackComponents_isInstructorEditable}"/>
        <h:outputLabel for="select_feedback_comp" value="#{msg.template_canbedefault}"/>

      <h:outputText value=" "/>
      <h:panelGrid columns="2">

      <h:panelGroup>
        <h:selectBooleanCheckbox id="student_response"
          value="#{template.feedbackComponent_StudentResp}"/>
        <h:outputLabel for="student_response" value="#{msg.student_response}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="question_level"
          value="#{template.feedbackComponent_QuestionLevel}"/>
        <h:outputLabel for="question_level" value="#{msg.question_level}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="correct_response"
          value="#{template.feedbackComponent_CorrectResp}"/>
        <h:outputLabel for="correct_response" value="#{msg.correct_response}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="selection_level"
          value="#{template.feedbackComponent_SelectionLevel}"/>
        <h:outputLabel for="selection_level" value="#{msg.selection_level}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="student_score"
          value="#{template.feedbackComponent_StudentScore}"/>
        <h:outputLabel for="student_score" value="#{msg.student_score}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="graders_comments"
          value="#{template.feedbackComponent_GraderComments}"/>
        <h:outputLabel for="graders_comments" value="#{msg.graders_comments}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="question_text" 
          value="#{template.feedbackComponent_QuestionText}"/>
        <h:outputLabel for="question_text" value="#{msg.student_questionscore}"/>
      </h:panelGroup>
   
      <h:panelGroup>
        <h:selectBooleanCheckbox id="statistics_hist"
          value="#{template.feedbackComponent_Statistics}"/>
        <h:outputLabel for="statistics_hist" value="#{msg.statistics_hist}"/>
      </h:panelGroup>

     </h:panelGrid>
    </h:panelGrid>
   </div></div>
  </samigo:hideDivision>

  <!-- *** GRADING *** -->
  <samigo:hideDivision title="#{msg.grading}" id="div11">
  <div class="indnt2">

    <!-- ANON GRADING-->
    <div class="longtext"> <h:outputLabel for="anon_grad" value="#{msg.testeeIdentity}"/></div>
 <div class="indnt3">
     <h:panelGrid columns="2"
      summary="#{msg.feedback_components_sub}">

        <h:selectBooleanCheckbox id="testeeIdentity"
          value="#{template.valueMap.testeeIdentity_isInstructorEditable}"/>
        <h:outputLabel for="testeeIdentity" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.anonymousGrading}"
      required="true" >
      <f:selectItem itemValue="2" itemLabel="#{msg.grades_ident}"/>
      <f:selectItem itemValue="1" itemLabel="#{msg.grades_anon}"/>
    </h:selectOneRadio>

      </h:panelGroup>
     </h:panelGrid>
   </div>
    <!-- GRADEBOOK OPTIONS -->
     <div class="longtext"><h:outputLabel for="grade_option" value="#{msg.gradebook_options}"/></div>
      <div class="indnt3">
     <h:panelGrid columns="2"
      summary="#{msg.gradebook_options_sub}">

        <h:selectBooleanCheckbox id="gradebook_options"
          value="#{template.valueMap.toGradebook_isInstructorEditable}"/>
        <h:outputLabel for="gradebook_options" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.toGradebook}"
      required="true">
      <f:selectItem itemValue="2" itemLabel="#{msg.grades_to_none}"/>
      <f:selectItem itemValue="1" itemLabel="#{msg.grades_to_default}"/>
    </h:selectOneRadio>
      </h:panelGroup>
     </h:panelGrid>
    </div>


    <!-- RECORDED SCORE AND MULTIPLES -->
    <div class="longtext"> <h:outputLabel for="recorded_score" value="#{msg.record_score_if_multi}"/></div>
      <div class="indnt3">
     <h:panelGrid columns="2"
      summary="#{msg.record_multi_sub}">

        <h:selectBooleanCheckbox id="record_score_if_multi"
          value="#{template.valueMap.recordedScore_isInstructorEditable}"/>
        <h:outputLabel for="record_score_if_multi" value="#{msg.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.recordedScore}"
      required="true">
      <f:selectItem itemValue="1" itemLabel="#{msg.record_highest}"/>
      <f:selectItem itemValue="2" itemLabel="#{msg.record_last}"/>
    </h:selectOneRadio>
      </h:panelGroup>
    </h:panelGrid>
    </div>


</div>
  </samigo:hideDivision>

  <!-- *** COLORS AND GRAPHICS	*** -->
  <samigo:hideDivision title="#{msg.graphics}" id="div12">
    <div class="indnt2">
  <div class="longtext">
    <h:outputLabel for="graphics" value="#{msg.template_canbeedited}"/></div>
     <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.graphics_sec}">

      <h:selectBooleanCheckbox id="bg_color"
        value="#{template.valueMap.bgColor_isInstructorEditable}"/>
      <h:outputLabel for="bg_color" value="#{msg.bg_color}"/>

      <h:selectBooleanCheckbox id="bg_image"
        value="#{template.valueMap.bgImage_isInstructorEditable}"/>
      <h:outputLabel for="bg_image" value="#{msg.bg_image}"/>

    </h:panelGrid>
    </div></div>
  </samigo:hideDivision>

  <!-- *** META *** -->
  <samigo:hideDivision title="#{msg.metadata}" id="div13">
  <div class="indnt2">
   <div class="longtext">
    <h:outputLabel for="meta" value="#{msg.record_data_for}"/></div>
    <div class="indnt3">
    <h:panelGrid columns="2"
      summary="#{msg.metadata_sec}">

      <h:selectBooleanCheckbox id="record_meta_full"
        value="#{template.valueMap.metadataAssess_isInstructorEditable}"/>
      <h:outputLabel for="record_meta_full" value="#{msg.record_meta_full}"/>
<%-- see bug# SAM-117 -- no longer required in Samigo
      <h:selectBooleanCheckbox id="record_meta_parts"
        value="#{template.valueMap.metadataParts_isInstructorEditable}"/>
      <h:outputLabel for="record_meta_parts" value="#{msg.record_meta_parts}"/>
--%>
      <h:selectBooleanCheckbox id="record_meta_questions"
        value="#{template.valueMap.metadataQuestions_isInstructorEditable}"/>
      <h:outputLabel for="record_meta_questions" value="#{msg.record_meta_questions}"/>

    </h:panelGrid>
    </div></div>
  </samigo:hideDivision>
</div>
  <h:inputHidden id="templateId" value="#{template.idString}"/>
  <h:inputHidden id="createdBy" value="#{template.createdBy}"/>
  <h:inputHidden id="createdDate" value="#{template.createdDate}"/>

  <p class="act">
  <h:panelGroup rendered="#{template.idString ne '1' || person.isAdmin}">
    <h:commandButton type="submit" id="Submit" value="#{msg.save}"
      action="#{template.getOutcome}" styleClass="active">
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateUpdateListener" />
                </h:commandButton>
    <h:outputText escape="false" value="&nbsp;&nbsp;" />
    <h:commandButton type="submit" id="Cancel" value="#{msg.cancel}"
      action="template"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{template.idString eq '1' && !person.isAdmin}">
    <h:commandButton type="submit" id="Exit" value="#{msg.cancel}"
      action="template"/>
  </h:panelGroup>
  </p>
</h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>
