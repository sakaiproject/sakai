<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
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
 <div class="portletBody">

 <!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<!-- content... -->
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{msg.title_total}"/>
    <h:outputText value="#{msg.column} "/>
    <h:outputText value="#{totalScores.assessmentName} "/> 
  </h3>

  <p class="navViewAction">
    <h:commandLink title="#{msg.t_submissionStatus}" action="submissionStatus" immediate="true"
      rendered="#{totalScores.anonymous eq 'true'}" >
      <h:outputText value="#{msg.sub_status}" />
      <f:param name="allSubmissions" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
    </h:commandLink>
    <h:outputText value=" #{msg.separator} " rendered="#{totalScores.anonymous eq 'true'}" />
    <h:outputText value="#{msg.title_total}" />
    <h:outputText value=" #{msg.separator} " rendered="#{totalScores.firstItem ne ''}" />
    <h:commandLink title="#{msg.t_questionScores}" action="questionScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{msg.q_view}" />
      <f:param name="allSubmissions" value="3"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetQuestionScoreListener" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScorePagerListener" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
    </h:commandLink>
    <h:outputText value=" #{msg.separator} " rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" />
    <h:commandLink title="#{msg.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" >
      <h:outputText value="#{msg.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>
  </p>
<div class="tier1">
  <h:messages styleClass="validation"/>
  <!-- only shows Max Score Possible if this assessment does not contain random dawn parts -->

<sakai:flowState bean="#{totalScores}" />
<h:panelGrid columns="2" columnClasses="samLeftNav,samRightNav" width="100%" rendered="#{totalScores.anonymous eq 'false'}">
  <h:panelGroup>
    <h:panelGrid columns="1" columnClasses="samLeftNav" width="100%">
	  <h:panelGroup rendered="#{!totalScores.hasRandomDrawPart}">
        <h:outputText value="#{msg.max_score_poss}" style="instruction"/>
        <h:outputText value="#{totalScores.maxScore}" style="instruction"/>
      </h:panelGroup>
	  
	  <h:panelGroup>
        <!-- SECTION AWARE -->
        <h:outputText value="#{msg.view}"/>
        <h:outputText value="#{msg.column}"/>
        <h:selectOneMenu value="#{totalScores.selectedSectionFilterValue}" id="sectionpicker" required="true" onchange="document.forms[0].submit();">
          <f:selectItems value="#{totalScores.sectionFilterSelectItems}"/>
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener"/>
        </h:selectOneMenu>

	
     <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsL1"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2' && totalScores.multipleSubmissionsAllowed eq 'true' }">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="2" itemLabel="#{msg.last_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsH1"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1' && totalScores.multipleSubmissionsAllowed eq 'true' }">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="1" itemLabel="#{msg.highest_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>
      </h:panelGroup>

	  <h:panelGroup>
 	        <h:outputText value="#{msg.search}"/>
            <h:outputText value="#{msg.column}"/>
			<h:inputText
				id="searchString"
				value="#{totalScores.searchString}"
				onfocus="clearIfDefaultString(this, '#{msg.search_default_student_search_string}')"
				onkeypress="return submitOnEnter(event, 'editTotalResults:searchSubmitButton');"/>
			<f:verbatim> </f:verbatim>
			<h:commandButton actionListener="#{totalScores.search}" value="#{msg.search_find}" id="searchSubmitButton" />
			<f:verbatim> </f:verbatim>
			<h:commandButton actionListener="#{totalScores.clear}" value="#{msg.search_clear}"/>
	  </h:panelGroup>
    </h:panelGrid>
  </h:panelGroup>
   
   <h:panelGroup>
	<sakai:pager id="pager1" totalItems="#{totalScores.dataRows}" firstItem="#{totalScores.firstRow}" pageSize="#{totalScores.maxDisplayedRows}" textStatus="#{msg.paging_status}" />
  </h:panelGroup>
</h:panelGrid>

<h:panelGrid columns="2" columnClasses="samLeftNav,samRightNav" width="100%" rendered="#{totalScores.anonymous eq 'true'}">
  <h:panelGroup>
    <h:panelGrid columns="1" columnClasses="samLeftNav" width="100%">
	  <h:panelGroup rendered="#{!totalScores.hasRandomDrawPart}">
        <h:outputText value="#{msg.max_score_poss}" style="instruction"/>
        <h:outputText value="#{totalScores.maxScore}" style="instruction"/>
      </h:panelGroup>

      <h:panelGroup>
        <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsL2"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2' && totalScores.multipleSubmissionsAllowed eq 'true' }">
        <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
        <f:selectItem itemValue="2" itemLabel="#{msg.last_sub}" />
        <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        </h:selectOneMenu>

        <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsH2"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1' && totalScores.multipleSubmissionsAllowed eq 'true' }">
          <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
          <f:selectItem itemValue="1" itemLabel="#{msg.highest_sub}" />
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        </h:selectOneMenu>
      </h:panelGroup>
    </h:panelGrid>
  </h:panelGroup>
  
  <h:panelGroup>
	<sakai:pager id="pager2" totalItems="#{totalScores.dataRows}" firstItem="#{totalScores.firstRow}" pageSize="#{totalScores.maxDisplayedRows}" textStatus="#{msg.paging_status}" />
  </h:panelGroup>
</h:panelGrid>

  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
<div class="tier2">
  <h:dataTable cellpadding="0" cellspacing="0" id="totalScoreTable" value="#{totalScores.agents}"
    var="description" styleClass="listHier" columnClasses="textTable">
    
    <!-- NAME/SUBMISSION ID -->
    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType ne 'lastName'}">
     <f:facet name="header">
        <h:commandLink title="#{msg.t_sortLastName}" immediate="true" id="lastName" action="totalScores">
          <h:outputText value="#{msg.name}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="lastName" />
        <f:param name="sortAscending" value="true"/>        
        </h:commandLink>
     </f:facet>
     <h:panelGroup> <span class="tier2">
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1' || description.submittedDate==null}" />
         <h:outputText value=", " rendered="#{(description.assessmentGradingId eq '-1' || description.submittedDate==null) && description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1' || description.submittedDate==null}" />
         <h:outputText value="#{msg.na}" rendered="#{description.lastInitial eq 'Anonymous' && (description.assessmentGradingId eq '-1' || description.submittedDate==null)}" />
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true" 
          rendered="#{description.submittedDate!=null &&  description.assessmentGradingId ne '-1'}" >
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{msg.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
     <f:verbatim><br/></f:verbatim>
	 <span class="itemAction">
	  <h:commandLink title="#{msg.t_fullShortAnswer}" immediate="true" id="createEmail1"  onmouseup="javascript:window.open('../evaluation/createNewEmail.faces','createNewEmail','width=600,height=600,scrollbars=yes, resizable=yes');">
    	<h:outputText value="  #{msg.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toName" value="#{description.firstName} #{description.lastName}" />
  		  <f:param name="toEmailAddress" value="#{description.email}" />
  		  <f:param name="toFirstName" value="#{description.firstName}" />
	  </h:commandLink>
	  </span>
   	</span>
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'lastName' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortLastName}" action="totalScores">
          <h:outputText value="#{msg.name}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortLastNameDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup> <span class="tier2">
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1' || description.submittedDate==null}" />
         <h:outputText value=", " rendered="#{(description.assessmentGradingId eq '-1' || description.submittedDate==null) && description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1' || description.submittedDate==null}" />
         <h:outputText value="#{msg.na}" rendered="#{description.lastInitial eq 'Anonymous' && (description.assessmentGradingId eq '-1' || description.submittedDate==null)}" />
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true" 
          rendered="#{description.submittedDate!=null && description.assessmentGradingId ne '-1'}" >
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{msg.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
     <f:verbatim><br/></f:verbatim>
	 <span class="itemAction">
	  <h:commandLink title="#{msg.t_fullShortAnswer}" immediate="true" id="createEmail2"  onmouseup="javascript:window.open('../evaluation/createNewEmail.faces','createNewEmail','width=600,height=600,scrollbars=yes, resizable=yes');">
    	<h:outputText value="  #{msg.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toName" value="#{description.firstName} #{description.lastName}" />
  		  <f:param name="toEmailAddress" value="#{description.email}" />
  		  <f:param name="toFirstName" value="#{description.firstName}" />
	  </h:commandLink>
	  </span>
   	</span>
    </h:column>
    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'lastName' && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortLastName}" action="totalScores">
        <h:outputText value="#{msg.name}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortLastNameAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
            <h:panelGroup> <span class="tier2">
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1' || description.submittedDate==null}" />
         <h:outputText value=", " rendered="#{(description.assessmentGradingId eq '-1' || description.submittedDate==null) && description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1' || description.submittedDate==null}" />
         <h:outputText value="#{msg.na}" rendered="#{description.lastInitial eq 'Anonymous' && (description.assessmentGradingId eq '-1' || description.submittedDate==null)}" />
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true" 
          rendered="#{description.submittedDate!=null && description.assessmentGradingId ne '-1'}" >
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{msg.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
     <f:verbatim><br/></f:verbatim>
     <f:verbatim><br/></f:verbatim>
	 <span class="itemAction">
	  <h:commandLink title="#{msg.t_fullShortAnswer}" immediate="true" id="createEmail3"  onmouseup="javascript:window.open('../evaluation/createNewEmail.faces','createNewEmail','width=600,height=600,scrollbars=yes, resizable=yes');">
    	<h:outputText value="  #{msg.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toName" value="#{description.firstName} #{description.lastName}" />
  		  <f:param name="toEmailAddress" value="#{description.email}" />
  		  <f:param name="toFirstName" value="#{description.firstName}" />
	  </h:commandLink>
	  </span>
   	</span>
	</h:column>
    

    <!-- ANONYMOUS and ASSESSMENTGRADINGID -->
    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType ne 'assessmentGradingId'}">
     <f:facet name="header">
        <h:commandLink title="#{msg.t_sortSubmissionId}" action="totalScores" >
          <h:outputText value="#{msg.sub_id}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="assessmentGradingId" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
     <h:panelGroup >
       <h:commandLink title="#{msg.t_student}" action="studentScores">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}"/>
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType eq 'assessmentGradingId' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortSubmissionId}" action="totalScores">
          <h:outputText value="#{msg.sub_id}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortSubmissionIdDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>
    
    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType eq 'assessmentGradingId' && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortSubmissionId}" action="totalScores">
        <h:outputText value="#{msg.sub_id}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortSubmissionIdAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{msg.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
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
       <h:commandLink title="#{msg.t_sortUserId}" id="idString" action="totalScores" >
          <h:outputText value="#{msg.uid}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="idString" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.agentEid}" />
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'idString' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortUserId}" action="totalScores">
          <h:outputText value="#{msg.uid}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortUserIdDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.agentEid}" />
    </h:column>
    
    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'idString' && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortUserId}" action="totalScores">
        <h:outputText value="#{msg.uid}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortUserIdAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
        <h:outputText value="#{description.agentEid}" />
    </h:column>
 

    <!-- ROLE -->
    <h:column rendered="#{totalScores.sortType ne 'role'}">
     <f:facet name="header" >
        <h:commandLink title="#{msg.t_sortRole}" id="role" action="totalScores">
          <h:outputText value="#{msg.role}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="role" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}" 
             rendered="#{totalScores.anonymous eq 'false' || description.assessmentGradingId ne '-1'}" />
    </h:column>

    <h:column rendered="#{totalScores.sortType=='role' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortRole}" action="totalScores">
          <h:outputText value="#{msg.role}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortRoleDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.role}" 
             rendered="#{totalScores.anonymous eq 'false' || description.assessmentGradingId ne '-1'}" />
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='role'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortRole}" action="totalScores">
        <h:outputText value="#{msg.role}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortRoleAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
       <h:outputText value="#{description.role}" 
             rendered="#{totalScores.anonymous eq 'false' || description.assessmentGradingId ne '-1'}" />
    </h:column>
    

    <!-- DATE -->
    <h:column rendered="#{totalScores.sortType!='submittedDate'}">
     <f:facet name="header">
        <h:commandLink title="#{msg.t_sortSubmittedDate}" id="submittedDate" action="totalScores">
          <h:outputText value="#{msg.submit_date}" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="submittedDate" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.submittedDate}" rendered="#{description.attemptDate != null && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}" >
          <f:convertDateTime pattern="#{genMsg.output_data_picker_w_sec}"/>
        </h:outputText>
		<h:panelGroup rendered="#{description.isLate == 'true' && description.attemptDate != null
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}">
			<f:verbatim><br/></f:verbatim>
			<h:outputText style="color:red" value="#{msg.late}"/>
		</h:panelGroup>
		
      <h:outputText value="#{msg.no_submission}"
         rendered="#{description.attemptDate == null && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='submittedDate' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortSubmittedDate}" action="totalScores">
          <h:outputText value="#{msg.submit_date}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortSubmittedDateDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.submittedDate}" rendered="#{description.attemptDate != null && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}" >
          <f:convertDateTime pattern="#{genMsg.output_data_picker_w_sec}"/>
        </h:outputText>
		<h:panelGroup rendered="#{description.isLate == 'true' && description.attemptDate != null
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}">
			<f:verbatim><br/></f:verbatim>
			<h:outputText style="color:red" value="#{msg.late}"/>
		</h:panelGroup>

        <h:outputText value="#{msg.no_submission}"
         rendered="#{description.attemptDate == null && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>

    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='submittedDate'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortSubmittedDate}" action="totalScores">
        <h:outputText value="#{msg.submit_date}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortSubmittedDateAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
        <h:outputText value="#{description.submittedDate}" rendered="#{description.attemptDate != null && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}" >
          <f:convertDateTime pattern="#{genMsg.output_data_picker_w_sec}"/>
        </h:outputText>
		<h:panelGroup rendered="#{description.isLate eq 'true' && description.attemptDate != null
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}">
			<f:verbatim><br/></f:verbatim>
			<h:outputText style="color:red" value="#{msg.late}"/>
		</h:panelGroup>
        <h:outputText value="#{msg.no_submission}"
         rendered="#{description.attemptDate == null && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
    </h:column>
    

    <!-- STATUS -->
    <h:column rendered="#{totalScores.sortType!='status'}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortStatus}" id="status" action="totalScores">
          <h:outputText value="#{msg.status}" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="status" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
      </f:facet>
      <h:outputText value="#{msg.auto_scored}" 
         rendered="#{description.status == 2 && description.attemptDate != null 
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
      <h:outputText value=" " 
         rendered="#{description.status == 3 && description.attemptDate != null
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
    </h:column>

	<h:column rendered="#{totalScores.sortType=='status' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortStatus}" action="totalScores">
          <h:outputText value="#{msg.status}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortStatusDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
<h:outputText value="#{msg.auto_scored}" 
         rendered="#{description.status == 2 && description.attemptDate != null 
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
      <h:outputText value=" " 
         rendered="#{description.status == 3 && description.attemptDate != null
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='status'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortStatus}" action="totalScores">
        <h:outputText value="#{msg.status}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortStatusAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:outputText value="#{msg.auto_scored}" 
         rendered="#{description.status == 2 && description.attemptDate != null 
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
      <h:outputText value=" " 
         rendered="#{description.status == 3 && description.attemptDate != null
                    && (totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1')}"/>
    </h:column>


    <!-- TOTAL -->
    <h:column rendered="#{totalScores.sortType!='totalAutoScore'}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortScore}" id="totalAutoScore" action="totalScores">
          <h:outputText value="#{msg.tot}" />
          <f:param name="sortBy" value="totalAutoScore" />
          <f:param name="sortAscending" value="true"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        </h:commandLink>
      </f:facet>
      <h:outputText value="#{description.roundedTotalAutoScore}" rendered="#{totalScores.anonymous eq 'false' || description.assessmentGradingId ne '-1'}" />
    </h:column>

    <h:column rendered="#{totalScores.sortType=='totalAutoScore' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortScore}" action="totalScores">
          <h:outputText value="#{msg.tot}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortAdjustScoreDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:outputText value="#{description.roundedTotalAutoScore}" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='totalAutoScore'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortScore}" action="totalScores">
        <h:outputText value="#{msg.tot}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortAdjustScoreAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:outputText value="#{description.roundedTotalAutoScore}" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>
    </h:column>
    
    <!-- ADJUSTMENT -->
    <h:column rendered="#{totalScores.sortType!='totalOverrideScore'}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortAdjustScore}" id="totalOverrideScore" action="totalScores">
    	    <h:outputText value="#{msg.adj}" />
        	<f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
	        <f:param name="sortBy" value="totalOverrideScore" />
	        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal" required="false" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}" >
		<f:validateDoubleRange/>
	 </h:inputText>
     <h:message for="adjustTotal" style="color:red"/>
   </h:column>


    <h:column rendered="#{totalScores.sortType=='totalOverrideScore' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortAdjustScore}" action="totalScores">
          <h:outputText value="#{msg.adj}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortScoreDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal2" required="false" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}" >
		<f:validateDoubleRange/>
	 </h:inputText>
     <h:message for="adjustTotal2" style="color:red"/>
   </h:column>
    
    <h:column rendered="#{totalScores.sortType=='totalOverrideScore'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortAdjustScore}" action="totalScores">
        <h:outputText value="#{msg.adj}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortScoreAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal3" required="false" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}" >
		<f:validateDoubleRange/>
	 </h:inputText>
     <h:message for="adjustTotal3" style="color:red"/>
   </h:column>
  

    <!-- FINAL SCORE -->
    <h:column rendered="#{totalScores.sortType!='finalScore'}">
     <f:facet name="header">
      <h:commandLink title="#{msg.t_sortFinalScore}" id="finalScore" action="totalScores" >
        <h:outputText value="#{msg.final}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="finalScore" />
        <f:param name="sortAscending" value="true"/>
      </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.roundedFinalScore}" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='finalScore' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortFinalScore}" action="totalScores">
          <h:outputText value="#{msg.final}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortFinalScoreDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:outputText value="#{description.roundedFinalScore}" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='finalScore'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortFinalScore}" action="totalScores">
        <h:outputText value="#{msg.final}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortFinalScoreAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:outputText value="#{description.roundedFinalScore}" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>
    </h:column>


    <!-- COMMENT -->
    <h:column rendered="#{totalScores.sortType!='comments'}">
     <f:facet name="header">
      <h:commandLink title="#{msg.t_sortComments}" id="comments" action="totalScores">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />    
        <h:outputText value="#{msg.comment}"/>
        <f:param name="sortBy" value="comments" />
        <f:param name="sortAscending" value="true"/>
      </h:commandLink>
     </f:facet>
   <h:inputTextarea value="#{description.comments}" rows="3" cols="30" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>

<%-- temporary replaced by inputTextArea util toggle small/large produced
     <samigo:wysiwyg rows="140" value="#{description.comments}" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
--%>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='comments' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{msg.t_sortComments}" action="totalScores">
          <h:outputText value="#{msg.comment}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortCommentDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
  	    </h:commandLink>    
      </f:facet>
   <h:inputTextarea value="#{description.comments}" rows="3" cols="30" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>

<%-- temporary replaced by inputTextArea util toggle small/large produced
     <samigo:wysiwyg rows="140" value="#{description.comments}" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
--%>
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='comments'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{msg.t_sortComments}" action="totalScores">
        <h:outputText value="#{msg.comment}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{msg.alt_sortCommentAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />              
      </h:commandLink> 
      </f:facet>
   <h:inputTextarea value="#{description.comments}" rows="3" cols="30" rendered="#{totalScores.anonymous eq 'false'  || description.assessmentGradingId ne '-1'}"/>

<%-- temporary replaced by inputTextArea util toggle small/large produced
     <samigo:wysiwyg rows="140" value="#{description.comments}" >
       <f:validateLength maximum="4000"/>
     </samigo:wysiwyg>
--%>
    </h:column>
  </h:dataTable>

<h:outputText value="#{msg.mult_sub_highest}" rendered="#{totalScores.scoringOption eq '1'&& totalScores.multipleSubmissionsAllowed eq 'true' }"/>
<h:outputText value="#{msg.mult_sub_last}" rendered="#{totalScores.scoringOption eq '2' && totalScores.multipleSubmissionsAllowed eq 'true' }"/>
</div>
<p class="act">

   <%-- <h:commandButton value="#{msg.save_exit}" action="author"/> --%>
   <h:commandButton accesskey="#{msg.a_save}" styleClass="active" value="#{msg.save_cont}" action="totalScores" type="submit" >
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
   </h:commandButton>
   <h:commandButton value="#{msg.cancel}" action="author" immediate="true"/>

</p>
</div>
</h:form>

</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
