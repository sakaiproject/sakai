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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{evaluationMessages.title_download_file_submissions}" /></title>
		<style type="text/css">
			.disabled
			{
				background-color: #f1f1f1;
			}
		</style> 
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <div class="portletBody">

<script>
	$(document).ready(function(){
		$(".allSections").hide();
	});
    
	function toggleChecked() {
		$('input[type=checkbox]').each( function() {
			//alert('name = ' + this.name);
			if (this.name.match(/downloadFileSubmissions.*questionCheckbox/) != null) {
				$(this).attr("checked", "checked");
			}
		})
	}

	

	function showHideReleaseSections(){
		var releaseRadio =$("input[name='downloadFileSubmissions:siteSection']:checked");
		//alert("releaseRadio.val(): " + releaseRadio.val());
		if(releaseRadio.val() === "sections") {
			$(".allSections").show(); 
		}
		else {
			$(".allSections").hide();
		}
	}
	
</script>
 <!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<!-- content... -->
<h:form id="downloadFileSubmissions">
  <h:inputHidden id="publishedId" value="#{downloadFileSubmissions.publishedAssessmentId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h:panelGrid columns="1">
    <h:panelGroup>
      <f:verbatim><h3></f:verbatim>
  	    <h:outputText value="#{evaluationMessages.title_download_file_submissions}#{evaluationMessages.column} " escape="false"/>
        <f:verbatim><span style="font-weight:normal !important;"></f:verbatim>
  	      <h:outputText value="(#{totalScores.assessmentName}) " escape="false"/>
        <f:verbatim></span></f:verbatim>
      <f:verbatim></h3></f:verbatim>
    </h:panelGroup>
  </h:panelGrid>
  
  <!-- Per UX, for formatting -->
  <div class="textBelowHeader">
    <h:outputText value=""/>
  </div>  

  <h:outputText value="<ul class='navIntraTool actionToolbar' role='menu'>" escape="false"/>
    <h:outputText value="<li role='menuitem' class='firstToolBarItem'><span>" escape="false"/>
    <h:commandLink title="#{evaluationMessages.t_submissionStatus}" action="submissionStatus" immediate="true">
      <h:outputText value="#{evaluationMessages.sub_status}" />
      <f:param name="allSubmissions" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
    </h:commandLink>

	<h:outputText value="<li role='menuitem'><span>" escape="false"/>
    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{commonMessages.total_scores}" />
    </h:commandLink>

    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.firstItem ne ''}" />
    <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.q_view}" />
      <f:param name="allSubmissions" value="3"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetQuestionScoreListener" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScorePagerListener" />
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

	<h:outputText value="</span><li role='menuitem'><span class='current'>" escape="false" />
	<h:outputText value="#{evaluationMessages.title_download_file_submissions}" />
	
  <h:outputText value="</span></li></ul>" escape="false"/>

  <f:verbatim><br /></f:verbatim>
  <f:verbatim><div class="tier1"></f:verbatim>
  <h:messages infoClass="messageSamigo" warnClass="messageSamigo" errorClass="messageSamigo" fatalClass="messageSamigo"/>
  
  <h:outputText value="#{evaluationMessages.download_responses_to}" escape="false"/>
  <f:verbatim></div></f:verbatim>
  
  <f:verbatim><div style="margin-left: 30px;"></f:verbatim>
  <h:panelGrid columns="1">
  <h:outputLink title="#{evaluationMessages.select_all}" onclick="toggleChecked()" value="#" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize > 1}">
          <h:outputText value="#{evaluationMessages.select_all}" />        
  </h:outputLink>
  </h:panelGrid>
  <f:verbatim></div></f:verbatim>
  
  <f:verbatim><div style="margin-left: 24px;"></f:verbatim>
  <h:dataTable value="#{downloadFileSubmissions.fileUploadQuestionList}" var="question" columnClasses="downloanQuestionCheckbox,downloanQuestionDescription">
    <h:column>
      <h:selectManyCheckbox value="" id="questionCheckbox" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize > 1}">
      	<f:selectItem itemValue="#{question.itemIdString}" />
       </h:selectManyCheckbox>
    </h:column>

    <h:column>
      <h:panelGrid columnClasses="samLeftNav," border="0">
        <h:outputText value="<b>#{evaluationMessages.part} #{question.section.sequence}: #{evaluationMessages.question} #{question.section.sequence}</b> - #{evaluationMessages.q_fu}" escape="false"/>
        <h:outputText value="#{question.text}" escape="false"/>
      </h:panelGrid>
    </h:column>
  </h:dataTable>
  <f:verbatim></div></f:verbatim>

  <f:verbatim><div style="margin-left: 2px;"></f:verbatim>
  	 <h:panelGrid border="0">
      <h:selectOneRadio id="siteSection" layout="pagedirection" value="#{downloadFileSubmissions.firstTargetSelected}" onclick="showHideReleaseSections();"
        required="true" rendered="#{downloadFileSubmissions.availableSectionSize > 0 }">
        <f:selectItems value="#{downloadFileSubmissions.siteSectionItems}" />
      </h:selectOneRadio>
    </h:panelGrid>
  <f:verbatim></div></f:verbatim>
    
  <f:verbatim><div class="allSections" style="margin-left: 24px;"></f:verbatim>
  <h:selectManyCheckbox id="sectionsForSite" layout="pagedirection" value="#{downloadFileSubmissions.sectionsSelected}" rendered="#{downloadFileSubmissions.availableSectionSize > 1 }">
     <f:selectItems value="#{downloadFileSubmissions.availableSectionItems}" />
    </h:selectManyCheckbox>
  <f:verbatim></div></f:verbatim>
  
    <f:verbatim><div class="tier1"></f:verbatim> 
<p class="act">

   <%-- <h:commandButton value="#{evaluationMessages.save_exit}" action="author"/> --%> 
   <h:commandButton	value="#{evaluationMessages.download}" actionListener="#{downloadFileSubmissions.downloadFiles}" style="active" />
   
</p>
<f:verbatim></div></f:verbatim>
</h:form>

</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
