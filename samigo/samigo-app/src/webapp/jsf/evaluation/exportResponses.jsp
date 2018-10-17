<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2007 The Sakai Foundation.
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
        value="#{commonMessages.export_action}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <div class="portletBody">

<!-- content... -->
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h:panelGrid columns="1">
  <h:panelGroup>
  <f:verbatim><h3></f:verbatim>
  	<h:outputText value="#{commonMessages.export_action}#{evaluationMessages.column} " escape="false"/>
  <f:verbatim><span style="font-weight:normal !important;"></f:verbatim>
  	<h:outputText value="#{exportResponses.assessmentName} " escape="false"/>
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

    <h:outputText value="</span><li role='menuitem'><span>" escape="false"/>
    
    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{commonMessages.total_scores}" />
    </h:commandLink>

    <h:outputText value="</span><li role='menuitem'><span>" rendered="#{totalScores.firstItem ne ''}" escape="false" />
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

    <h:outputText value="</span><li role='menuitem'><span>" rendered="#{totalScores.firstItem ne ''}" escape="false" />

    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>

    <h:outputText value="</span><li role='menuitem'><span>" rendered="#{totalScores.firstItem ne ''}" escape="false"/>

    <h:commandLink title="#{evaluationMessages.t_itemAnalysis}" action="detailedStatistics" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.item_analysis}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>

    <h:outputText value="</span><li role='menuitem'><span class='current'>" escape="false" />
    <h:outputText value="#{commonMessages.export_action}" />

    <h:outputText value="</span><li role='menuitem'><span>" escape="false" rendered="#{totalScores.hasFileUpload}"/>
   
    <h:commandLink title="#{evaluationMessages.t_title_download_file_submissions}" action="downloadFileSubmissions" immediate="true" rendered="#{totalScores.hasFileUpload}">
      <h:outputText value="#{evaluationMessages.title_download_file_submissions}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetQuestionScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.DownloadFileSubmissionsListener" />
    </h:commandLink>

  <h:outputText value="</span></li></ul>" escape="false"/>
  
<f:verbatim><br /></f:verbatim>  
<div class="tier1">
<h:panelGrid columns="1">
<h:panelGroup>
<h:outputText value="#{evaluationMessages.export_msg}"/>
</h:panelGroup>
<h:outputText value=" "/>
<h:outputText value=" "/>
<h:panelGroup>
<h:commandButton actionListener="#{exportResponses.exportExcel}" value="#{commonMessages.export_action}" id="exportButton" />
</h:panelGroup>
</h:panelGrid>
</div>
</h:form>

</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
