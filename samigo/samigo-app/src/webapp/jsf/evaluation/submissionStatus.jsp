<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  

<!--
$Id$
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

<f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{evaluationMessages.sub_status}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->

<!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<div class="portletBody container-fluid">
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>
  
  <div class="page-header">
    <h1>
  	  <h:outputText value="#{evaluationMessages.sub_status}#{evaluationMessages.column} " escape="false"/>
      <small>
  	    <h:outputText value="#{totalScores.assessmentName} " escape="false"/>
      </small>
    </h1>
  </div>
  
  <h:outputText value="<ul class='navIntraTool actionToolbar' role='menu'>" escape="false"/>
    <h:outputText value="<li role='menuitem' class='firstToolBarItem'><span class='current'>" escape="false"/>
    <h:outputText value="#{evaluationMessages.sub_status}" />
    
    <h:outputText value="</span><li role='menuitem'><span>" escape="false"/>
    
    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{commonMessages.total_scores}" />
    </h:commandLink>
    
    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.firstItem ne ''}" />
    
    <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.q_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
    </h:commandLink>
    
    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.firstItem ne ''}" />
   
    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>

    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.firstItem ne ''}" />
    
    <h:commandLink title="#{evaluationMessages.t_itemAnalysis}" action="detailedStatistics" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.item_analysis}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>

    <h:outputText value="</span><li role='menuitem'><span>" escape="false" />
    
    <h:commandLink title="#{commonMessages.export_action}" action="exportResponses" immediate="true">
      <h:outputText value="#{commonMessages.export_action}" />
  	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportResponsesListener" />
    </h:commandLink>

    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.hasFileUpload}"/>

    <h:commandLink title="#{evaluationMessages.t_title_download_file_submissions}" action="downloadFileSubmissions" immediate="true" rendered="#{totalScores.hasFileUpload}">
      <h:outputText value="#{evaluationMessages.title_download_file_submissions}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetQuestionScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.DownloadFileSubmissionsListener" />
    </h:commandLink>

  <h:outputText value="</span></li></ul>" escape="false"/>

  <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
  
<sakai:flowState bean="#{submissionStatus}" />

  <h:panelGroup rendered="#{!totalScores.hasRandomDrawPart}">
    <h:outputText value="<h2>#{evaluationMessages.max_score_poss}<small>: #{totalScores.maxScore}</small></h2>" escape="false"/>
  </h:panelGroup>

  <h:panelGroup styleClass="row" layout="block">
	<h:panelGroup styleClass="col-md-6" layout="block">
	  <div class="form-group row">
        <h:outputLabel styleClass="col-md-2" value="#{evaluationMessages.view}" rendered="#{totalScores.availableSectionSize > 0}" />
        <!-- SECTION AWARE -->
        <h:outputLabel styleClass="col-md-2" value="#{evaluationMessages.view}&nbsp;#{evaluationMessages.all_sections}" escape="false" rendered="#{totalScores.availableSectionSize < 1}"/>
        <div class="col-md-10">
     	  <h:selectOneMenu value="#{submissionStatus.selectedSectionFilterValue}" id="sectionpicker" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.availableSectionSize >= 1}">
            <f:selectItems value="#{totalScores.sectionFilterSelectItems}"/>
            <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener"/>
          </h:selectOneMenu>
        </div>
      </div>
	
	  <h:panelGroup styleClass="form-group row search-box" layout="block">
	    <h:outputLabel styleClass="col-md-2" value="#{evaluationMessages.search}"/>
        <div class="col-md-10">
			<h:inputText
				id="searchString"
				value="#{submissionStatus.searchString}"
				onfocus="clearIfDefaultString(this, '#{evaluationMessages.search_default_student_search_string}')"
				onkeypress="return submitOnEnter(event, 'editTotalResults:searchSubmitButton');"/>
			<h:outputText value="&nbsp;" escape="false" />
			<h:commandButton actionListener="#{submissionStatus.search}" value="#{evaluationMessages.search_find}" id="searchSubmitButton" />
			<h:outputText value="&nbsp;" escape="false" />
			<h:commandButton actionListener="#{submissionStatus.clear}" value="#{evaluationMessages.search_clear}"/>
	    </div>
	  </h:panelGroup>
    </h:panelGroup>
	<div class="pager-holder col-md-6" style="text-align: right">
	  <sakai:pager id="pager" totalItems="#{submissionStatus.dataRows}" firstItem="#{submissionStatus.firstRow}" pageSize="#{submissionStatus.maxDisplayedRows}" textStatus="#{evaluationMessages.paging_status}" />
	</div>
  </h:panelGroup>

  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
<div class="table-responsive">
  <h:dataTable styleClass="table table-striped table-bordered" id="totalScoreTable" value="#{submissionStatus.agents}"
    var="description">
    <!-- NAME/SUBMISSION ID -->

    <h:column rendered="#{submissionStatus.sortType ne 'lastName'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" immediate="true" id="lastName" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.name}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="lastName" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:outputText value="#{description.lastName}" />
       <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
       <h:outputText value="#{description.firstName}" />
       <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
       <f:verbatim><br/></f:verbatim>
	   <h:panelGroup styleClass="itemAction" rendered="#{totalScores.anonymous eq 'false' && description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}">
		 <h:outputText value="<a href=\"mailto:" escape="false" />
	     <h:outputText value="#{description.email}" escape="false" />
	     <h:outputText value="?subject=" escape="false" />
		 <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
         <h:outputText value="  #{evaluationMessages.email}" escape="false"/>
         <h:outputText value="</a>" escape="false" />
	   </h:panelGroup>
     </h:panelGroup>

	 <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.anonymous eq 'false' && description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != '' &&  description.retakeAllowed}" />

	 <span class="itemAction">
     <h:commandLink title="#{evaluationMessages.t_retake}" id="retakeAssessment1" immediate="true" 
        rendered="#{description.retakeAllowed}"
        action="confirmRetake">
        <h:outputText value="#{evaluationMessages.allow_retake}"/>
        <f:param name="agentIdString" value="#{description.idString}" />
        <f:param name="publishedAssessmentId" value="#{totalScores.publishedId}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ConfirmRetakeAssessmentListener" />
     </h:commandLink>
	 </span>
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'lastName' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.name}" />
          <f:param name="sortBy" value="lastName" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
       <h:outputText value="#{description.firstName}" />
       <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
       <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:panelGroup rendered="#{totalScores.anonymous eq 'false' && description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}">
		 <h:outputText value="<a href=\"mailto:" escape="false" />
	     <h:outputText value="#{description.email}" escape="false" />
	     <h:outputText value="?subject=" escape="false" />
		 <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
         <h:outputText value="  #{evaluationMessages.email}" escape="false"/>
         <h:outputText value="</a>" escape="false" />
	   </h:panelGroup>
	 </span>
     </h:panelGroup>
	
	 <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.anonymous eq 'false' && description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != '' &&  description.retakeAllowed}" />

	 <span class="itemAction">
     <h:commandLink title="#{evaluationMessages.t_retake}" id="retakeAssessment2" immediate="true" 
        rendered="#{description.retakeAllowed}"
        action="confirmRetake">
        <h:outputText value="#{evaluationMessages.allow_retake}"/>
        <f:param name="agentIdString" value="#{description.idString}" />
        <f:param name="publishedAssessmentId" value="#{totalScores.publishedId}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ConfirmRetakeAssessmentListener" />
     </h:commandLink>
	 </span>

    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'lastName' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.name}" />
          <f:param name="sortBy" value="lastName" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
       <h:outputText value="#{description.firstName}" />
       <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
       <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:panelGroup rendered="#{totalScores.anonymous eq 'false' && description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}">
		 <h:outputText value="<a href=\"mailto:" escape="false" />
	     <h:outputText value="#{description.email}" escape="false" />
	     <h:outputText value="?subject=" escape="false" />
		 <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
         <h:outputText value="  #{evaluationMessages.email}" escape="false"/>
         <h:outputText value="</a>" escape="false" />
	   </h:panelGroup>
	 </span>
     </h:panelGroup>
	
	<h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.anonymous eq 'false' && description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != '' &&  description.retakeAllowed}" />

	 <span class="itemAction">
     <h:commandLink title="#{evaluationMessages.t_retake}" id="retakeAssessment3" immediate="true" 
        rendered="#{description.retakeAllowed}"
        action="confirmRetake">
        <h:outputText value="#{evaluationMessages.allow_retake}"/>
        <f:param name="agentIdString" value="#{description.idString}" />
        <f:param name="publishedAssessmentId" value="#{totalScores.publishedId}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ConfirmRetakeAssessmentListener" />
     </h:commandLink>
	 </span>

    </h:column>


   <!-- STUDENT ID -->
    <h:column  rendered="#{submissionStatus.sortType ne 'agentDisplayId'}" >
     <f:facet name="header">
       <h:commandLink title="#{evaluationMessages.t_sortUserId}" id="agentDisplayId" action="submissionStatus" >
          <h:outputText value="#{evaluationMessages.uid}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="agentDisplayId" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.agentDisplayId}" />
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'agentDisplayId' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.uid}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.agentDisplayId}" />
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'agentDisplayId' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.uid}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.agentDisplayId}" />
    </h:column>


    <!-- ROLE -->
    <h:column rendered="#{submissionStatus.sortType ne 'role'}">
     <f:facet name="header" >
        <h:commandLink title="#{evaluationMessages.t_sortRole}" id="role" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.role}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="role" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'role' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortRole}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.role}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortRoleDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.role}" />
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'role' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortRole}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.role}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortRoleAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.role}" />
    </h:column>

    <!-- DATE -->
    <h:column rendered="#{submissionStatus.sortType ne 'submittedDate'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" id="submittedDate" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.date}" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="submittedDate" />
        </h:commandLink>
     </f:facet>
        <h:outputText rendered="#{description.submittedDate !=null && description.submittedDate ne ''}" value="#{description.submittedDate}">
          <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
		<h:outputText rendered="#{description.submittedDate == null || description.submittedDate eq ''}" value="#{evaluationMessages.no_submission}"/>
    </h:column>
	
	<h:column rendered="#{submissionStatus.sortType eq 'submittedDate' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.date}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText rendered="#{description.submittedDate !=null && description.submittedDate ne ''}" value="#{description.submittedDate}">
           <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
		<h:outputText rendered="#{description.submittedDate == null || description.submittedDate eq ''}" value="#{evaluationMessages.no_submission}"/>
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'submittedDate' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.date}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText rendered="#{description.submittedDate !=null && description.submittedDate ne ''}" value="#{description.submittedDate}">
           <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
		<h:outputText rendered="#{description.submittedDate == null || description.submittedDate eq ''}" value="#{evaluationMessages.no_submission}"/>
    </h:column>

  </h:dataTable>
</div>

</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
