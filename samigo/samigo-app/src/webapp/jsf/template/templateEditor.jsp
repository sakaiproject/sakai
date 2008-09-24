<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{templateMessages.template_editor}" /></title>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">
  <h:form id="templateEditorForm">
  
   <p class="navIntraTool" >
   <h:commandLink title="#{generalMessages.t_assessment}" action="author" id="authorLink" immediate="true">
      <h:outputText value="#{generalMessages.assessment}" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>
    <h:outputText value=" #{generalMessages.separator} " />
    <h:commandLink title="#{generalMessages.t_template}" action="template" id="templateLink" immediate="true">
      <h:outputText value="#{generalMessages.template}" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
    </h:commandLink>
    <h:outputText value=" #{generalMessages.separator} " />
    <h:commandLink title="#{generalMessages.t_questionPool}" action="poolList" id="poolLink" immediate="true">
      <h:outputText value="#{templateMessages.link_pool}" />
    </h:commandLink>
   </p>
<h3><h:outputText value="#{templateMessages.template_editor}"/>
     <h:outputText value="#{template.templateName}"/>
</h3>
 <h:outputText escape="false" value="#{templateMessages.template_instructions}"/>
 <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
  <!-- *** GENERAL TEMPLATE INFORMATION *** -->
  <div class="tier1">
<p>
<h:outputLink title="#{templateMessages.t_showDivs}" value="#" onclick="showDivs();" onkeypress="showDivs();" >
<h:outputText value="#{templateMessages.open}"/>
</h:outputLink>
<h:outputText value=" | " />
<h:outputLink title="#{templateMessages.t_hideDivs}" value="#" onclick="hideDivs();" onkeypress="hideDivs();">
<h:outputText value="#{templateMessages.close}"/>
</h:outputLink>
 <h:outputText value="#{templateMessages.allMenus}"/>
</p>
  <samigo:hideDivision id="div1" title="#{templateMessages.template_inform}" >
    <div class="tier2">
       <h:selectBooleanCheckbox id="infocanbeviewed"
          value="#{template.valueMap.templateInfo_isInstructorEditable}"/>
     <h:outputText value="#{templateMessages.template_canbeviewed}"/>
  <div class="tier3">
 <h:panelGrid columns="2" columnClasses="shorttext"
      summary="#{templateMessages.enter_template_info_section}">


      <h:outputLabel for="template_title" value="#{templateMessages.template_title}"/>
      <h:inputText id="template_title" value="#{template.templateName}" size="30"/>


      <h:outputLabel for="author" value="#{templateMessages.author_opt}"/>
      <h:inputText id="author" value="#{template.templateAuthor}" size="30"/>

      <!-- WYSIWYG -->

      <h:outputLabel value="#{templateMessages.description_intro_opt}"/>
     <samigo:wysiwyg rows="140" value="#{template.templateDescription}" hasToggle="yes" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
    </h:panelGrid>
    </div></div>
  </samigo:hideDivision>

  <!-- *** AUTHORSHIP *** -->
  <samigo:hideDivision title="#{templateMessages.introduction}" id="div2">
   <div class="tier2">
    <div class="longtext"><h:outputLabel value="#{templateMessages.template_canbeedited}"/></div>
 <div class="tier3">
    <h:panelGrid columns="2"
      summary="#{templateMessages.introduction_sec}">

      <h:selectBooleanCheckbox id="authors"
          value="#{template.valueMap.assessmentAuthor_isInstructorEditable}"/>
      <h:outputLabel for="authors" value="#{templateMessages.authors}"/>

<%-- bug# SAM156 - i don't think creator should be editable, daisyf 01/26/05
      <h:selectBooleanCheckbox id="creator"
          value="#{template.valueMap.assessmentCreator_isInstructorEditable}"/>
      <h:outputLabel for="creator" value="#{templateMessages.creator}"/>
--%>
      <h:selectBooleanCheckbox id="description_intro"
          value="#{template.valueMap.description_isInstructorEditable}"/>
      <h:outputLabel for="description_intro" value="#{templateMessages.description_intro}"/>
    </h:panelGrid>
    </div> </div>
  </samigo:hideDivision>

<%-- get rid of delivery dates according to the new mockup,
  <!-- *** DELIVERY DATES *** -->
  <samigo:hideDivision title="#{templateMessages.delivery_dates}" id="div3">
   <div class="tier2">
  <div class="longtext">
    <h:outputLabel value="#{templateMessages.template_canbeedited}"/></div>
 <div class="tier3">
    <h:panelGrid columns="2"
      summary="#{templateMessages.delivery_dates_sec}">

      <h:selectBooleanCheckbox id="due_date"
        value="#{template.valueMap.dueDate_isInstructorEditable}"/>
      <h:outputLabel for="due_date" value="#{templateMessages.due_date}"/>

      <h:selectBooleanCheckbox id="retract_date"
        value="#{template.valueMap.retractDate_isInstructorEditable}"/>
      <h:outputLabel for="retract_date" value="#{templateMessages.retract_date}"/>

    </h:panelGrid>
   </div></div>
  </samigo:hideDivision>
 --%>

  <!-- *** RELEASED TO  *** -->
  <samigo:hideDivision title="#{templateMessages.released_to}" id="div4">
   <div class="tier3">
     <h:selectOneRadio layout="pageDirection" value="#{template.valueMap.releaseTo}" required="true">
       <f:selectItem itemValue="ANONYMOUS_USERS"
          itemLabel="#{templateMessages.anon}"/>
       <f:selectItem itemValue="SITE_MEMBERS"
          itemLabel="#{templateMessages.auth_users}"/>
       <f:selectItem itemValue="SELECTED_GROUPS"
          itemLabel="#{templateMessages.selected_groups}"/>
     </h:selectOneRadio>
     
     <f:verbatim> Testing Testing </f:verbatim>
     
     
     
  </div>
  </samigo:hideDivision>

  <!-- *** HIGH SECURITY *** -->
  <samigo:hideDivision title="#{templateMessages.high_security}" id="div5">
    <div class="tier2">
   <div class="longtext">
    <h:outputLabel value="#{templateMessages.template_canbeedited}"/></div>
    <div class="tier3">
    <h:panelGrid columns="2"
      summary="#{templateMessages.high_security_sec}">

      <h:selectBooleanCheckbox id="allow_only_specified_ip"
        value="#{template.valueMap.ipAccessType_isInstructorEditable}"/>
      <h:outputLabel for="allow_only_specified_ip" value="#{templateMessages.allow_only_specified_ip}"/>

      <h:selectBooleanCheckbox id="secondary_id_pw"
        value="#{template.valueMap.passwordRequired_isInstructorEditable}"/>
      <h:outputLabel for="secondary_id_pw" value="#{templateMessages.secondary_id_pw}"/>

    </h:panelGrid>
     </div></div>
  </samigo:hideDivision>

  <!-- *** TIMED ASSESSMENTS *** -->
  <samigo:hideDivision title="#{templateMessages.timed_assmt}" id="div6">
    <div class="tier2">
  <div class="longtext">
    <h:outputLabel value="#{templateMessages.template_canbeedited}"/></div>
    <div class="tier3">
    <h:panelGrid columns="2"
      summary="#{templateMessages.timed_assmt_sec}">

      <h:selectBooleanCheckbox id="timed_assmt"
        value="#{template.valueMap.timedAssessment_isInstructorEditable}"/>
      <h:outputLabel for="timed_assmt" value="#{templateMessages.timed_assmt}"/>

<%-- SAK-3578: auto submit when time expired will always be true, 
     so no need to provide "can edit" option
      <h:selectBooleanCheckbox id="auto_submit_expires"
        value="#{template.valueMap.timedAssessmentAutoSubmit_isInstructorEditable}"/>
      <h:outputLabel for="auto_submit_expires" value="#{templateMessages.auto_submit_expires}"/>
--%>

    </h:panelGrid>
   </div></div>
  </samigo:hideDivision>


  <!-- *** ASSESSMENT ORGANIZATION *** -->
  <samigo:hideDivision title="#{templateMessages.organization}" id="div7">

    <!-- NAVIGATION -->
 <div class="tier2">
      <div class="longtext"><h:outputLabel value="#{templateMessages.navigation}"/></div>
      <div class="tier3">
     <h:panelGrid columns="2">
        <h:selectBooleanCheckbox id="navigation"
          value="#{template.valueMap.itemAccessType_isInstructorEditable}"/>
        <h:outputLabel for="navigation" value="#{templateMessages.template_canbedefault}"/>

        <h:outputText value=" "/>
         <h:panelGroup>
        <!-- h:panelGroup style="tier4" -->
         <h:selectOneRadio layout="pageDirection" value="#{template.itemAccessType}" required="true">
         <f:selectItem itemValue="1"
          itemLabel="#{templateMessages.linear_access_no_return}"/>
         <f:selectItem itemValue="2"
          itemLabel="#{templateMessages.random_access_questions_toc}"/>
         </h:selectOneRadio>
        </h:panelGroup>
      </h:panelGrid>
     </div>

    <!-- QUESTION LAYOUT -->
     <div class="longtext"><h:outputLabel value="#{templateMessages.question_layout}"/></div>
    <div class="tier3">
     <h:panelGrid columns="2" summary="#{templateMessages.question_layout_sub}">

        <h:selectBooleanCheckbox id="question_layout"
          value="#{template.valueMap.displayChunking_isInstructorEditable}"/>
        <h:outputLabel for="question_layout" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
       <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.displayChunking}" required="true">
      <f:selectItem itemValue="1"
        itemLabel="#{templateMessages.quest_sep_page}"/>
      <f:selectItem itemValue="2"
        itemLabel="#{templateMessages.part_sep_page}"/>
      <f:selectItem itemValue="3"
        itemLabel="#{templateMessages.complete_displayed_on_one_page}"/>
    </h:selectOneRadio>
    </h:panelGroup>
    </h:panelGrid>
    </div>
    <!-- NUMBERING -->
      <div class="longtext"><h:outputLabel value="#{templateMessages.numbering}"/></div>
      <div class="tier3">
     <h:panelGrid columns="2" summary="#{templateMessages.numbering_sub}">

        <h:selectBooleanCheckbox id="numbering"
          value="#{template.valueMap.displayNumbering_isInstructorEditable}"/>
        <h:outputLabel for="numbering" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
       <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.questionNumbering}" required="true">
      <f:selectItem itemValue="1" itemLabel="#{templateMessages.continuous_num_parts}"/>
      <f:selectItem itemValue="2" itemLabel="#{templateMessages.restart_num_part}"/>
    </h:selectOneRadio>
    </h:panelGroup>
    </h:panelGrid>

</div></div>
  </samigo:hideDivision>

  <!-- *** SUBMISSIONS *** -->

  <samigo:hideDivision title="#{templateMessages.submissions}" id="div8">
 <div class="tier2">

    <!-- NUMBER OF SUBMISSIONS -->

    <div class="longtext"><h:outputLabel value="#{templateMessages.number_of_sub_allowed}"/></div>
       <div class="tier3">

     <h:panelGrid columns="2" 
      summary="#{templateMessages.number_of_sub_allowed_sub}">

        <h:selectBooleanCheckbox id="number_of_sub_allowed"
          value="#{template.valueMap.submissionModel_isInstructorEditable}"/>
        <h:outputLabel for="number_of_sub_allowed" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
    <h:panelGroup>
     <h:selectOneRadio layout="pageDirection" value="#{template.submissionModel}" required="true">
       <f:selectItem itemValue="1" itemLabel="#{templateMessages.unlimited}"/>
       <f:selectItem itemValue="0" itemLabel="#{templateMessages.only}"/>
    </h:selectOneRadio>
    <h:inputText value="#{template.submissionNumber}"
       id="submissions_allowed" size="5"/> <h:outputText value=" #{templateMessages.submissions_allowed}"/>
    </h:panelGroup>
    </h:panelGrid>
</div>
    <!-- LATE HANDLING -->
    <div class="longtext"><h:outputLabel value="#{templateMessages.late_handling}"/></div>
        <div class="tier3">
     <h:panelGrid columns="2"
      summary="#{templateMessages.late_handling_sub">

        <h:selectBooleanCheckbox id="late_handling"
          value="#{template.valueMap.lateHandling_isInstructorEditable}"/>
        <h:outputLabel for="late_handling" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.lateHandling}" required="true" >
      <f:selectItem itemValue="2"
        itemLabel="#{templateMessages.late_sub_not_accepted}"/>
      <f:selectItem itemValue="1"
        itemLabel="#{templateMessages.late_submissions_accepted_tagged_late}"/>
    </h:selectOneRadio>
    </h:panelGroup>
    </h:panelGrid>
</div>

    <!-- AUTOMATIC SUBMISSION -->
    <div class="longtext"><h:outputLabel value="#{templateMessages.automatic_submission}" rendered="#{templateIndex.automaticSubmissionEnabled}"/></div>
        <div class="tier3">
     <h:panelGrid columns="2"
      summary="#{templateMessages.automatic_submission" rendered="#{templateIndex.automaticSubmissionEnabled}">

        <h:selectBooleanCheckbox id="automatic_submission"
          value="#{template.valueMap.automaticSubmission_isInstructorEditable}"/>
        <h:outputLabel for="automatic_submission" value="#{templateMessages.automatic_submission}"/>
    </h:panelGrid>
    </div></div>

    <!-- AUTOSAVE -->
<%-- hide for 1.5 release SAM-148
    <h:outputText style="h4" styleClass="tier2"
      value="#{templateMessages.auto_save}"/>

     <h:panelGrid columns="2"
      summary="#{templateMessages.auto_save_sub}">

        <h:selectBooleanCheckbox id="auto_save"
          value="#{template.valueMap.autoSave_isInstructorEditable}"/>
        <h:outputLabel for="auto_save" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.autoSave}" required="true">
      <f:selectItem itemValue="0" itemLabel="#{templateMessages.user_must_click}"/>
      <f:selectItem itemValue="1" itemLabel="#{templateMessages.all_input_saved_auto}"/>
    </h:selectOneRadio>
   </h:panelGroup>
   </h:panelGrid>
--%>
  </samigo:hideDivision>

  <!-- *** SUBMISSION MESSAGE *** -->
  <samigo:hideDivision title="#{templateMessages.submission_message}" id="div9">
 <div class="tier2">
  <div class="longtext">
    <h:outputLabel value="#{templateMessages.template_canbeedited}"/></div>
     <div class="tier3">
    <h:panelGrid columns="2"
      summary="#{templateMessages.timed_assmt_sec}">

      <h:selectBooleanCheckbox id="submission_message"
        value="#{template.valueMap.submissionMessage_isInstructorEditable}"/>
      <h:outputLabel for="submission_message" value="#{templateMessages.submission_message}"/>

      <h:selectBooleanCheckbox id="final_page_url"
        value="#{template.valueMap.finalPageURL_isInstructorEditable}"/>
      <h:outputLabel for="final_page_url" value="#{templateMessages.final_page_url}"/>
    </h:panelGrid>
    </div></div>
  </samigo:hideDivision>

  <!-- *** FEEDBACK *** -->

  <samigo:hideDivision title="#{templateMessages.feedback}" id="div10">
 <div class="tier2">

  <!-- FEEDBACK AUTHORING--> 
     <div class="longtext">
    <h:outputLabel value="#{templateMessages.feedback_authoring}"/></div>
    <div class="tier3">
     <h:panelGrid columns="2"
      summary="#{templateMessages.feedback_authoring_sub}">

        <h:selectBooleanCheckbox id="feedback_authoring"
          value="#{template.valueMap.feedbackAuthoring_isInstructorEditable}"/>
        <h:outputLabel for="feedback_authoring" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.feedbackAuthoring}"
      required="true">
      <f:selectItem itemValue="1"
        itemLabel="#{templateMessages.questionlevel_feedback}"/>
      <f:selectItem itemValue="2"
        itemLabel="#{templateMessages.sectionlevel_feedback}"/>
      <f:selectItem itemValue="3"
        itemLabel="#{templateMessages.both_feedback}"/>
    </h:selectOneRadio>
   </h:panelGroup>
   </h:panelGrid>
</div>

    <!-- FEEDBACK DELIVERY -->
     <div class="longtext">
    <h:outputLabel value="#{templateMessages.feedback_delivery}"/></div>
 <div class="tier3">
     <h:panelGrid columns="2" 
      summary="#{templateMessages.feedback_delivery_sub}">

        <h:selectBooleanCheckbox id="feedback_delivery"
          value="#{template.valueMap.feedbackType_isInstructorEditable}"/>
        <h:outputLabel for="feedback_delivery" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.feedbackType}"
      required="true">
      <f:selectItem itemValue="1"
        itemLabel="#{templateMessages.immediate_feedback}"/>
      <f:selectItem itemValue="4"
        itemLabel="#{templateMessages.feedback_on_submission} #{assessmentSettingsMessages.note_of_feedback_on_submission}"/>
      <f:selectItem itemValue="2"
        itemLabel="#{templateMessages.feedback__disp_spec_date}"/>
      <f:selectItem itemValue="3"
        itemLabel="#{templateMessages.no_feedback}"/>
    </h:selectOneRadio>
   </h:panelGroup>
   </h:panelGrid>
</div>
    <!-- FEEDBACK COMPONENTS -->
     <div class="longtext"><h:outputLabel value="#{templateMessages.select_feedback_comp}"/></div>
      <div class="tier3">
     <h:panelGrid columns="2" 
      summary="#{templateMessages.feedback_components_sub}">

        <h:selectBooleanCheckbox id="select_feedback_comp"
          value="#{template.valueMap.feedbackComponents_isInstructorEditable}"/>
        <h:outputLabel for="select_feedback_comp" value="#{templateMessages.template_canbedefault}"/>

      <h:outputText value=" "/>
      <h:panelGrid columns="2">

      <h:panelGroup>
        <h:selectBooleanCheckbox id="student_response"
          value="#{template.feedbackComponent_StudentResp}"/>
        <h:outputLabel for="student_response" value="#{templateMessages.student_response}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="question_level"
          value="#{template.feedbackComponent_QuestionLevel}"/>
        <h:outputLabel for="question_level" value="#{templateMessages.question_level}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="correct_response"
          value="#{template.feedbackComponent_CorrectResp}"/>
        <h:outputLabel for="correct_response" value="#{templateMessages.correct_response}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="selection_level"
          value="#{template.feedbackComponent_SelectionLevel}"/>
        <h:outputLabel for="selection_level" value="#{templateMessages.selection_level}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="student_score"
          value="#{template.feedbackComponent_StudentScore}"/>
        <h:outputLabel for="student_score" value="#{templateMessages.student_score}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="graders_comments"
          value="#{template.feedbackComponent_GraderComments}"/>
        <h:outputLabel for="graders_comments" value="#{templateMessages.graders_comments}"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectBooleanCheckbox id="question_score" 
          value="#{template.feedbackComponent_StudentQuestionScore}"/>
        <h:outputLabel for="student_question_score" value="#{templateMessages.student_questionscore}"/>
      </h:panelGroup>
   
      <h:panelGroup>
        <h:selectBooleanCheckbox id="statistics_hist"
          value="#{template.feedbackComponent_Statistics}"/>
        <h:outputLabel for="statistics_hist" value="#{templateMessages.statistics_hist}"/>
      </h:panelGroup>

     </h:panelGrid>
    </h:panelGrid>
   </div></div>
  </samigo:hideDivision>

  <!-- *** GRADING *** -->
  <samigo:hideDivision title="#{templateMessages.grading}" id="div11">
  <div class="tier2">

    <!-- ANON GRADING-->
    <div class="longtext"> <h:outputLabel value="#{templateMessages.testeeIdentity}"/></div>
 <div class="tier3">
     <h:panelGrid columns="2"
      summary="#{templateMessages.feedback_components_sub}">

        <h:selectBooleanCheckbox id="testeeIdentity"
          value="#{template.valueMap.testeeIdentity_isInstructorEditable}"/>
        <h:outputLabel for="testeeIdentity" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.anonymousGrading}"
      required="true" >
      <f:selectItem itemValue="2" itemLabel="#{templateMessages.grades_ident}"/>
      <f:selectItem itemValue="1" itemLabel="#{templateMessages.grades_anon}"/>
    </h:selectOneRadio>

      </h:panelGroup>
     </h:panelGrid>
   </div>
    <!-- GRADEBOOK OPTIONS -->
     <div class="longtext"><h:outputLabel value="#{templateMessages.gradebook_options}"/></div>
      <div class="tier3">
     <h:panelGrid columns="2" 
      summary="#{templateMessages.gradebook_options_sub}">

        <h:selectBooleanCheckbox id="gradebook_options"
          value="#{template.valueMap.toGradebook_isInstructorEditable}"/>
        <h:outputLabel for="gradebook_options" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.toGradebook}"
      required="true">
      <f:selectItem itemValue="2" itemLabel="#{templateMessages.grades_to_none}"/>
      <f:selectItem itemValue="1" itemLabel="#{templateMessages.grades_to_default}"/>
    </h:selectOneRadio>
      </h:panelGroup>
     </h:panelGrid>
    </div>


    <!-- RECORDED SCORE AND MULTIPLES -->
    <div class="longtext"> <h:outputLabel value="#{templateMessages.record_score_if_multi}"/></div>
      <div class="tier3">
     <h:panelGrid columns="2"
      summary="#{templateMessages.record_multi_sub}">

        <h:selectBooleanCheckbox id="record_score_if_multi"
          value="#{template.valueMap.recordedScore_isInstructorEditable}"/>
        <h:outputLabel for="record_score_if_multi" value="#{templateMessages.template_canbedefault}"/>

       <h:outputText value=" "/>
      <h:panelGroup>
    <h:selectOneRadio layout="pageDirection" value="#{template.recordedScore}"
      required="true">
      <f:selectItem itemValue="1" itemLabel="#{templateMessages.record_highest}"/>
      <f:selectItem itemValue="2" itemLabel="#{templateMessages.record_last}"/>
    </h:selectOneRadio>
      </h:panelGroup>
    </h:panelGrid>
    </div>


</div>
  </samigo:hideDivision>

 <!-- *** HUONG COLORS AND GRAPHICS	*** -->
 <samigo:hideDivision title="#{templateMessages.graphics}" id="div12">
    <div class="tier2">
 <div class="longtext"><h:outputLabel value="#{templateMessages.template_canbeedited}"/></div>
 <div class="tier3">
        <h:selectBooleanCheckbox id="graphics"
          value="#{template.valueMap.bgColor_isInstructorEditable}"/>
        <h:outputLabel for="graphics" value="#{templateMessages.bg}"/>
        </div>

    </div>
  </samigo:hideDivision>

  <!-- *** META *** -->
  <samigo:hideDivision title="#{templateMessages.metadata}" id="div13">
  <div class="tier2">
   <div class="longtext">
    <h:outputLabel value="#{templateMessages.record_data_for}"/></div>
    <div class="tier3">
    <h:panelGrid columns="2"
      summary="#{templateMessages.metadata_sec}">

      <h:selectBooleanCheckbox id="record_meta_full"
        value="#{template.valueMap.metadataAssess_isInstructorEditable}"/>
      <h:outputLabel for="record_meta_full" value="#{templateMessages.record_meta_full}"/>
<%-- see bug# SAM-117 -- no longer required in Samigo
      <h:selectBooleanCheckbox id="record_meta_parts"
        value="#{template.valueMap.metadataParts_isInstructorEditable}"/>
      <h:outputLabel for="record_meta_parts" value="#{templateMessages.record_meta_parts}"/>
--%>
      <h:selectBooleanCheckbox id="record_meta_questions"
        value="#{template.valueMap.metadataQuestions_isInstructorEditable}"/>
      <h:outputLabel for="record_meta_questions" value="#{templateMessages.record_meta_questions}"/>

    </h:panelGrid>
    </div></div>
  </samigo:hideDivision>
</div>
  <h:inputHidden id="templateId" value="#{template.idString}"/>
  <h:inputHidden id="createdBy" value="#{template.createdBy}"/>
  <h:inputHidden id="createdDate" value="#{template.createdDate}"/>



  <p class="act">
  <h:panelGroup rendered="#{(template.idString ne '1' && template.typeId ne '142') || person.isAdmin}">
    <h:commandButton accesskey="#{templateMessages.a_save}" type="submit" id="Submit" value="#{templateMessages.save}"
      action="#{templateIndex.getOutcome}" styleClass="active">
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateUpdateListener" />
                </h:commandButton>
    <h:outputText escape="false" value="&nbsp;&nbsp;" />
    <h:commandButton accesskey="#{templateMessages.a_cancel}" type="submit" id="Cancel" value="#{templateMessages.cancel}"
      action="template"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{template.typeId eq '142' && !person.isAdmin}">
    <h:commandButton accesskey="#{templateMessages.a_cancel}" type="submit" id="Exit" value="#{templateMessages.cancel}"
      action="template"/>
  </h:panelGroup>
  </p>
</h:form>
<!-- end content -->
</div>
<script type="text/javascript" language="JavaScript">hideUnhideAllDivsWithWysiwyg('none');</script>
      </body>
    </html>
  </f:view>
