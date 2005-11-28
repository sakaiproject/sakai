<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

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
        value="#{msg.title_total}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!--
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
 <div class="portletBody">
<!-- content... -->
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{msg.title_total}"/>
    <h:outputText value=": "/>
    <h:outputText value="#{totalScores.assessmentName} "/>
  </h3>
  <p class="navModeAction">
    <h:commandLink action="submissionStatus" immediate="true"
      rendered="#{totalScores.anonymous eq 'true'}" >
      <h:outputText value="#{msg.sub_status}" />
      <f:param name="allSubmissions" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
    </h:commandLink>
    <h:outputText value=" | " rendered="#{totalScores.anonymous eq 'true'}" />
    <h:outputText value="#{msg.title_total}" />
    <h:outputText value=" | " rendered="#{totalScores.firstItem ne ''}" />
    <h:commandLink action="questionScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{msg.q_view}" />
      <f:param name="allSubmissions" value="3"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
    </h:commandLink>
    <h:outputText value=" | " rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" />
    <h:commandLink action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" >
      <h:outputText value="#{msg.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>
  </p>

  <h:messages styleClass="validation"/>

  <!-- only shows Max Score Possible if this assessment does not contain random dawn parts -->
  <h:panelGroup rendered="#{!totalScores.hasRandomDrawPart}">
  <span class="indnt1">
     <h:outputText value="#{msg.max_score_poss}" style="instruction"/>
     <h:outputText value="#{totalScores.maxScore}" style="instruction"/></span>
     <f:verbatim><br /></f:verbatim> </span>
  </h:panelGroup>

  <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX  -->
  <span class="indnt1">
     <h:outputText value="#{msg.view}" />
     <h:outputText value=": " />
     <h:selectOneMenu value="#{totalScores.selectedSectionFilterValue}" id="sectionpicker"
        required="true" onchange="document.forms[0].submit();" >
	<f:selectItems value="#{totalScores.sectionFilterSelectItems}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsL"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="2" itemLabel="#{msg.last_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsH"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="1" itemLabel="#{msg.highest_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>

  </span>

<%--  THIS MIGHT BE FOR NEXT RELEASE

  <span class="rightNav">
    <!-- Need to hook up to the back end, DUMMIED -->
    <samigo:pagerButtons  formId="editTotalResults" dataTableId="myData"
      firstItem="1" lastItem="10" totalItems="50"
      prevText="Previous" nextText="Next" numItems="10" />
    </span>
END OF TEMPORARY OUT FOR THIS RELEASE --%>

  <h:panelGroup rendered="#{totalScores.anonymous eq 'false'}">
   <f:verbatim><span class="abc"></f:verbatim> 
     <samigo:alphaIndex initials="#{totalScores.agentInitials}" />
   <f:verbatim></span></f:verbatim> 
  </h:panelGroup>

  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
  <h:dataTable id="totalScoreTable" value="#{totalScores.agents}"
    var="description" style="listHier indnt2" columnClasses="textTable">
    <!-- NAME/SUBMISSION ID -->

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'lastName'}">

     <f:facet name="header">
        <h:outputText value="#{msg.name}" />
     </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1'}" />
         <h:outputText value=" " rendered="#{description.assessmentGradingId eq '-1'}"/>
         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1'}" />
         <h:outputText value="#{description.idString}" rendered="#{description.lastInitial eq 'Anonymous' && description.assessmentGradingId eq '-1'}" />
       <h:commandLink action="studentScores" rendered="#{description.assessmentGradingId ne '-1'}" >
         <h:outputText value="#{description.firstName}" />
         <h:outputText value=" " />
         <h:outputText value="#{description.lastName}" />
         <h:outputText value="#{description.idString}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.firstName} #{description.lastName}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType ne 'lastName'}">
     <f:facet name="header">
        <h:commandLink immediate="true" id="lastName" action="totalScores">
          <h:outputText value="#{msg.name}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="lastName" />
        </h:commandLink>
     </f:facet>
     <h:panelGroup> <span class="indnt2">
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1'}" />
         <h:outputText value=" " rendered="#{description.assessmentGradingId eq '-1'}"/>
         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1'}" />
         <h:outputText value="#{description.idString}" rendered="#{description.lastInitial eq 'Anonymous' && description.assessmentGradingId eq '-1'}" />
       <h:commandLink action="studentScores" immediate="true" rendered="#{description.assessmentGradingId ne '-1'}" >
         <h:outputText value="#{description.firstName}" />
         <h:outputText value=" " />
         <h:outputText value="#{description.lastName}" />
         <h:outputText value="#{description.idString}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.firstName} #{description.lastName}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
</span>
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType ne 'assessmentGradingId'}">
     <f:facet name="header">
        <h:commandLink action="totalScores" >
          <h:outputText value="#{msg.sub_id}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="assessmentGradingId" />
        </h:commandLink>
     </f:facet>
     <h:panelGroup>
       <h:outputText value="Anonymous" rendered="#{description.assessmentGradingId eq '-1'}" />
       <h:commandLink action="studentScores" rendered="#{description.assessmentGradingId ne '-1'}" >
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType eq 'assessmentGradingId'}">
     <f:facet name="header">
          <h:outputText value="#{msg.sub_id}" />
     </f:facet>
     <h:panelGroup>
       <h:outputText value="Anonymous" rendered="#{description.assessmentGradingId eq '-1'}" />
       <h:commandLink action="studentScores" immediate="true" rendered="#{description.assessmentGradingId ne '-1'}">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

   <!-- STUDENT ID -->
    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType!='idString'}" >
     <f:facet name="header">
       <h:commandLink id="idString" action="totalScores" >
          <h:outputText value="#{msg.uid}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="idString" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.idString}" />
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'idString'}" >
     <f:facet name="header">
       <h:outputText value="#{msg.uid}" />
     </f:facet>
        <h:outputText value="#{description.idString}" />
    </h:column>

    <!-- ROLE -->
    <h:column rendered="#{totalScores.sortType ne 'role'}">
     <f:facet name="header" >
        <h:commandLink id="role" action="totalScores">
          <h:outputText value="#{msg.role}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="role" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

    <h:column rendered="#{totalScores.sortType eq 'role'}">
     <f:facet name="header" >
       <h:outputText value="#{msg.role}" />
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

    <!-- DATE -->
    <h:column rendered="#{totalScores.sortType!='submittedDate'}">
     <f:facet name="header">
        <h:commandLink id="submittedDate" action="totalScores">
          <h:outputText value="#{msg.date}" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="submittedDate" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.submittedDate}">
          <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='submittedDate'}">
     <f:facet name="header">
       <h:outputText value="#{msg.date}" />
     </f:facet>
        <h:outputText value="#{description.submittedDate}">
           <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
    </h:column>

    <!-- STATUS -->
    <h:column rendered="#{totalScores.sortType!='status'}">
      <f:facet name="header">
        <h:commandLink id="status" action="totalScores">
          <h:outputText value="#{msg.status}" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="status" />
        </h:commandLink>
      </f:facet>
      <h:outputText value="#{msg.submitted}" 
         rendered="#{description.status == 2 && description.attemptDate != null}"/>
      <h:outputText value=" " 
         rendered="#{description.status == 3 && description.attemptDate != null}"/>
      <h:outputText value="#{msg.late}" 
         rendered="#{description.status == 4 && description.attemptDate != null}"/>
      <h:outputText value="#{msg.no_submission}"
         rendered="#{description.attemptDate == null}"/>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='status'}">
      <f:facet name="header">
        <h:outputText value="#{msg.status}" />
      </f:facet>
      <h:outputText value="#{msg.submitted}"
         rendered="#{description.status == 2 && description.attemptDate != null}"/>
      <h:outputText value=" "
         rendered="#{description.status == 3 && description.attemptDate != null}"/>
      <h:outputText value="#{msg.late}"
         rendered="#{description.status == 4 && description.attemptDate != null}"/>
      <h:outputText value="#{msg.no_submission}"
         rendered="#{description.attemptDate == null}"/>
    </h:column>

    <!-- TOTAL -->
    <h:column rendered="#{totalScores.sortType!='totalAutoScore'}">
      <f:facet name="header">
        <h:commandLink id="totalAutoScore" action="totalScores">
          <h:outputText value="#{msg.tot}" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="totalAutoScore" />
        </h:commandLink>
      </f:facet>
      <h:outputText value="#{description.totalAutoScore}" />
    </h:column>

    <h:column rendered="#{totalScores.sortType=='totalAutoScore'}">
      <f:facet name="header">
        <h:outputText value="#{msg.tot}" />
      </f:facet>
      <h:outputText value="#{description.totalAutoScore}" />
    </h:column>

    <!-- ADJUSTMENT -->
    <h:column rendered="#{totalScores.sortType!='totalOverrideScore'}">
      <f:facet name="header">
        <h:commandLink id="totalOverrideScore" action="totalScores">
          <h:outputText value="#{msg.adj}" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="totalOverrideScore" />
        </h:commandLink>
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal" required="false">
<f:validateDoubleRange/>
</h:inputText>
<br />
     <h:message for="adjustTotal" style="color:red"/>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='totalOverrideScore'}">
      <f:facet name="header">
        <h:outputText value="#{msg.adj}"/>
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal2" required="false">
<f:validateDoubleRange/>
</h:inputText>
<br />
     <h:message for="adjustTotal2" style="color:red"/>
    </h:column>

    <!-- FINAL SCORE -->
    <h:column rendered="#{totalScores.sortType!='finalScore'}">
     <f:facet name="header">
      <h:commandLink id="finalScore" action="totalScores" >
        <h:outputText value="#{msg.final}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="finalScore" />
      </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.finalScore}" />
    </h:column>

    <h:column rendered="#{totalScores.sortType=='finalScore'}">
     <f:facet name="header">
        <h:outputText value="#{msg.final}" />
     </f:facet>
        <h:outputText value="#{description.finalScore}" />
    </h:column>

    <!-- COMMENT -->
    <h:column rendered="#{totalScores.sortType!='comments'}">
     <f:facet name="header">
      <h:commandLink id="comments" action="totalScores">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <h:outputText value="#{msg.comment}"/>
        <f:param name="sortBy" value="comments" />
      </h:commandLink>
     </f:facet>
   <h:inputTextarea value="#{description.comments}" rows="3" cols="30"/>

<%-- temporary replaced by inputTextArea util toggle small/large produced
     <samigo:wysiwyg rows="140" value="#{description.comments}" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
--%>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='comments'}">
     <f:facet name="header">
        <h:outputText value="#{msg.comment}" />
     </f:facet>
<h:inputTextarea value="#{description.comments}" rows="3" cols="30"/>

<%--temporary replaced by inputTextArea util toggle small/large produced
     <samigo:wysiwyg rows="140" value="#{description.comments}" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
--%>
    </h:column>
  </h:dataTable>

<p class="act">

   <%-- <h:commandButton value="#{msg.save_exit}" action="author"/> --%>
   <h:commandButton styleClass="active" value="#{msg.save_cont}" action="totalScores" type="submit" >
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
   </h:commandButton>
   <h:commandButton value="#{msg.cancel}" action="author" immediate="true"/>

</p>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
