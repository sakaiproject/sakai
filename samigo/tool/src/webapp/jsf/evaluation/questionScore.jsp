<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<!--
$Id$
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
     basename="org.sakaiproject.tool.assessment.bundle.EvaluationMessages"
     var="msg"/>
     <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{msg.title_question}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{questionScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{questionScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
   
  <h:outputText value="#{msg.part} #{questionScores.partName}#{msg.comma} #{msg.question} #{questionScores.itemName} (#{totalScores.assessmentName}) "/>
  </h3>
  <p class="navViewAction">
    <h:commandLink title="#{msg.t_totalScores}" action="totalScores" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{msg.title_total}" />
    </h:commandLink>
    <h:outputText value=" | " />
      <h:outputText value="#{msg.q_view}" />
    <h:outputText value=" | " rendered="#{!totalScores.hasRandomDrawPart}" />
    <h:commandLink title="#{msg.t_histogram}" action="histogramScores" immediate="true" rendered="#{!totalScores.hasRandomDrawPart}">
      <h:outputText value="#{msg.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>
  </p>
<div class="tier1">
  <h:messages styleClass="validation"/>

  <h:dataTable value="#{questionScores.sections}" var="partinit">
    <h:column>
      <h:outputText value="#{msg.part} #{partinit.partNumber}#{msg.column}" />
    </h:column>
    <h:column>
      <samigo:dataLine value="#{partinit.questionNumberList}" var="iteminit" separator=" | " first="0" rows="100">
        <h:column>
          <h:commandLink title="#{msg.t_questionScores}"action="questionScores" immediate="true"
            rendered="#{iteminit.linked}" >
            <h:outputText value="#{msg.q} #{iteminit.partNumber} " />
            <f:actionListener
              type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
            <f:param name="newItemId" value="#{iteminit.id}" />
          </h:commandLink>
          <h:outputText value="#{msg.q} #{iteminit.partNumber} "
             rendered="#{!iteminit.linked}" />
        </h:column>
      </samigo:dataLine>
    </h:column>
  </h:dataTable>

<!--h:panelGrid styleClass="navModeQuestion" columns="2" columnClasses="alignLeft,alignCenter" width="100%" -->

<h:panelGrid styleClass="navModeAction" columns="2" columnClasses="navView,navList" width="100%">

     <h:panelGroup rendered="#{questionScores.typeId == '7'}">
         <h:outputText value="#{msg.q_aud}"/>
     </h:panelGroup>
      <h:panelGroup rendered="#{questionScores.typeId == '6'}">
         <h:outputText value="#{msg.q_fu}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '8'}">
         <h:outputText value="#{msg.q_fib}"/>
     </h:panelGroup>
      <h:panelGroup rendered="#{questionScores.typeId == '9'}">
         <h:outputText value="#{msg.q_match}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '2'}">
         <h:outputText value="#{msg.q_mult_mult}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '4'}">
         <h:outputText value="#{msg.q_tf}"/>
     </h:panelGroup>

     <h:panelGroup rendered="#{questionScores.typeId == '5'}">
         <h:outputText value="#{msg.q_short_ess}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '1' || questionScores.typeId == '3'}">
    <h:outputText value="#{msg.q_mult_sing}"/>
      </h:panelGroup>


     <h:outputText value="#{questionScores.maxPoint}" style="instruction"/>

 </h:panelGrid>


  <h:dataTable value="#{questionScores.deliveryItem}" var="question">
  <h:column>
  <h:panelGroup rendered="#{questionScores.typeId == '7'}">
    <f:subview id="displayAudioRecording">
      <%@ include file="/jsf/evaluation/item/displayAudioRecordingQuestion.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '6'}">
    <f:subview id="displayFileUpload">
    <%@ include file="/jsf/evaluation/item/displayFileUploadQuestion.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '8'}">
    <f:subview id="displayFillInTheBlank">
    <%@ include file="/jsf/evaluation/item/displayFillInTheBlank.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '9'}">
    <f:subview id="displayMatching">
    <%@ include file="/jsf/evaluation/item/displayMatching.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '2'}">
    <f:subview id="displayMultipleChoiceMultipleCorrect">
  <%@ include file="/jsf/evaluation/item/displayMultipleChoiceMultipleCorrect.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup
    rendered="#{questionScores.typeId == '1' ||
                questionScores.typeId == '3'}">
    <f:subview id="displayMultipleChoiceSingleCorrect">
    <%@ include file="/jsf/evaluation/item/displayMultipleChoiceSingleCorrect.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '5'}">
    <f:subview id="displayShortAnswer">
   <%@ include file="/jsf/evaluation/item/displayShortAnswer.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '4'}">
    <f:subview id="displayTrueFalse">
    <%@ include file="/jsf/evaluation/item/displayTrueFalse.jsp" %>
    </f:subview>
  </h:panelGroup>
  </h:column>
  </h:dataTable>
 <p class="navModeQuestion">
<h:outputText value="Responses"/>
</p>

  <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX  -->

     <!-- h:outputText value="#{msg.max_score_poss}" style="instruction"/-->
     <!-- h:outputText value="#{questionScores.maxScore}" style="instruction"/-->

     <h:outputText value="#{msg.view}:" />


     <h:selectOneMenu value="#{questionScores.selectedSectionFilterValue}" id="sectionpicker" rendered="#{totalScores.anonymous eq 'false'}"
     required="true" onchange="document.forms[0].submit();" >
        <f:selectItems value="#{totalScores.sectionFilterSelectItems}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{questionScores.allSubmissions}" id="allSubmissionsL"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="2" itemLabel="#{msg.last_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{questionScores.allSubmissions}" id="allSubmissionsH"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="1" itemLabel="#{msg.highest_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
     </h:selectOneMenu>

<%--  THIS MIGHT BE FOR NEXT RELEASE

  <span class="rightNav">
    <!-- Need to hook up to the back end, DUMMIED -->
    <samigo:pagerButtons  formId="editTotalResults" dataTableId="myData"
      firstItem="1" lastItem="10" totalItems="50"
      prevText="Previous" nextText="Next" numItems="10" />
 </span>
END OF TEMPORARY OUT FOR THIS RELEASE --%>

   <h:panelGroup rendered="#{questionScores.anonymous eq 'false'}">
   <f:verbatim> <span class="abc"></f:verbatim> 
      <samigo:alphaIndex initials="#{questionScores.agentInitials}" />
  <f:verbatim> </span></f:verbatim> 
   </h:panelGroup>

  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
<div class="tier2">
  <h:dataTable cellpadding="0" cellspacing="0" id="questionScoreTable" value="#{questionScores.agents}"
    var="description" styleClass="listHier" columnClasses="textTable">

    <!-- NAME/SUBMISSION ID -->
    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType eq 'lastName'}">
     <f:facet name="header">
        <h:outputText value="#{msg.name}" />
     </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.firstName}" />
         <h:outputText value=" " />
         <h:outputText value="#{description.lastName}" />
	 <h:outputText value="#{description.idString}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType ne 'lastName'}">
     <f:facet name="header">
        <h:commandLink title="#{msg.t_sortLastName}" id="lastName" action="questionScores">
          <h:outputText value="#{msg.name}" />
        <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="lastName" />
        </h:commandLink>
     </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.firstName}" />
         <h:outputText value=" " />
         <h:outputText value="#{description.lastName}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{questionScores.anonymous eq 'true' && questionScores.sortType ne 'assessmentGradingId'}">
     <f:facet name="header">
        <h:commandLink title="#{msg.t_sortSubmissionId}" action="questionScores">
          <h:outputText value="#{msg.sub_id}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="assessmentGradingId" />
        </h:commandLink>
     </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>

     </h:panelGroup>
    </h:column>

    <h:column rendered="#{questionScores.anonymous eq 'true' && questionScores.sortType eq 'assessmentGradingId'}">
     <f:facet name="header">
          <h:outputText value="#{msg.sub_id}" />
     </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

   <!-- STUDENT ID -->
    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType!='idString'}" >
     <f:facet name="header">
       <h:commandLink title="#{msg.t_sortUserId}" id="idString" action="questionScores" >
          <h:outputText value="#{msg.uid}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="idString" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.idString}" />
    </h:column>

    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType eq 'idString'}" >
     <f:facet name="header">
       <h:outputText value="#{msg.uid}" />
     </f:facet>
        <h:outputText value="#{description.idString}" />
    </h:column>

    <!-- ROLE -->
    <h:column rendered="#{questionScores.sortType ne 'role'}">
     <f:facet name="header" >
        <h:commandLink title="#{msg.t_sortRole}" id="role" action="questionScores">
          <h:outputText value="#{msg.role}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="role" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

    <h:column rendered="#{questionScores.sortType eq 'role'}">
     <f:facet name="header" >
       <h:outputText value="#{msg.role}" />
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

    <!-- DATE -->
    <h:column rendered="#{questionScores.sortType!='submittedDate'}">
     <f:facet name="header">
        <h:commandLink title="#{msg.t_sortSubmittedDate}" id="submittedDate" action="questionScores">
          <h:outputText value="#{msg.date}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="submittedDate" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.submittedDate}">
         <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>

        <h:outputText styleClass="red" value="#{msg.all_late}" escape="false"
          rendered="#{description.isLate}"/>
    </h:column>

    <h:column rendered="#{questionScores.sortType=='submittedDate'}">
     <f:facet name="header">
       <h:outputText value="#{msg.date}" />
     </f:facet>
        <h:outputText value="#{description.submittedDate}">
         <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
        <h:outputText styleClass="red" value="#{msg.all_late}" escape="false"
          rendered="#{description.isLate}"/>
    </h:column>

    <!-- SCORE -->
    <h:column rendered="#{questionScores.sortType!='totalAutoScore'}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortScore}" id="score" action="questionScores">
          <h:outputText value="#{msg.score}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="totalAutoScore" />
        </h:commandLink>
      </f:facet>
      <h:inputText value="#{description.totalAutoScore}" size="5" id="qscore" required="false">
<f:validateDoubleRange/>
</h:inputText>
<br />
 <h:message for="qscore" style="color:red"/>
 </h:column>
    <h:column rendered="#{questionScores.sortType=='totalAutoScore'}">
      <f:facet name="header">
        <h:outputText value="#{msg.score}" />
      </f:facet>
      <h:inputText value="#{description.totalAutoScore}" size="5"  id="qscore2" required="false">
<f:validateDoubleRange/>
</h:inputText>

 <h:message for="qscore2" style="color:red"/>
    </h:column>

    <!-- ANSWER -->
    <h:column rendered="#{questionScores.sortType!='answer'}">
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{msg.stud_resp}" 
             rendered="#{questionScores.typeId == '6' || questionScores.typeId == '7' }"/>
          <h:commandLink title="#{msg.t_sortResponse}" id="answer" action="questionScores" >
            <h:outputText value="#{msg.stud_resp}" 
               rendered="#{questionScores.typeId != '6' && questionScores.typeId != '7' }"/>
            <f:actionListener
               type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
            <f:actionListener
               type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
            <f:param name="sortBy" value="answer" />
          </h:commandLink>
        </h:panelGroup>
      </f:facet>
      <!-- display of answer to file upload question is diffenent from other types - daisyf -->
      <h:outputText value="#{description.answer}" escape="false" rendered="#{questionScores.typeId != '6' and questionScores.typeId != '7'}" />
     <f:verbatim><br/></f:verbatim>
   <!--h:outputLink rendered="#{questionScores.typeId == '5'}" value="#" onclick="javascript:window.alert('#{description.fullAnswer}');"-->


<h:outputLink rendered="#{questionScores.typeId == '5'}" value="#" onclick="javascript:window.open('fullShortAnswer.faces?idString=#{description.assessmentGradingId}','fullShortAnswer','width=600,height=600,scrollbars=yes, resizable=yes');">


    <h:outputText  value="(#{msg.click_shortAnswer})" />
    </h:outputLink>
    <h:outputLink 
      rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '4') && description.rationale ne ''}" 
      value="#" onclick="javascript:window.alert('#{description.rationale}');" >
    <h:outputText  value="(#{msg.click_rationale})"/>
    </h:outputLink>

      <h:panelGroup rendered="#{questionScores.typeId == '6'}">
        <f:subview id="displayFileUpload2">
          <%@ include file="/jsf/evaluation/item/displayFileUploadAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>

      <h:panelGroup rendered="#{questionScores.typeId == '7'}">
        <f:subview id="displayAudioRecording2">
          <%@ include file="/jsf/evaluation/item/displayAudioRecordingAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>
    </h:column>

    <h:column rendered="#{questionScores.sortType=='answer'}">
      <f:facet name="header">
        <h:outputText value="#{msg.stud_resp}" />
      </f:facet>
      <h:outputText value="#{description.answer}" escape="false" />
    </h:column>

    <!-- COMMENT -->
    <h:column rendered="#{questionScores.sortType!='comments'}">
     <f:facet name="header">
      <h:commandLink title="#{msg.t_sortComment}" id="comments" action="questionScores">
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <h:outputText value="#{msg.comment}"/>
        <f:param name="sortBy" value="comments" />
      </h:commandLink>
     </f:facet>
 <h:inputTextarea value="#{description.comments}" rows="3" cols="30"/>
<%-- temp replaced by inputTextArea until resize is introduced
     <samigo:wysiwyg rows="140" value="#{description.comments}" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
--%>
    </h:column>

    <h:column rendered="#{questionScores.sortType=='comments'}">
     <f:facet name="header">
        <h:outputText value="#{msg.comment}" />
     </f:facet>
 <h:inputTextarea value="#{description.comments}" rows="3" cols="30"/>
<%-- temp replaced by inputTextArea until resize is introduced
     <samigo:wysiwyg rows="140" value="#{description.comments}" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
--%>
    </h:column>
  </h:dataTable>
</div>
<p class="act">
   <%-- <h:commandButton value="#{msg.save_exit}" action="author"/> --%>
   <h:commandButton accesskey="#{msg.a_update}" styleClass="active" value="#{msg.save_cont}" action="questionScores" type="submit" >
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
   </h:commandButton>
   <h:commandButton accesskey="#{msg.a_cancel}" value="#{msg.cancel}" action="totalScores" immediate="true"/>
</div>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
