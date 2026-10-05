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
<script>
  document.addEventListener("DOMContentLoaded", () => {
    // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
    const currentLink = document.getElementById("editTotalResults:exportResponsesMenuLink");
    if (currentLink) {
      currentLink.classList.add("current");
      const anchor = currentLink.querySelector("a");
      if (anchor) {
        currentLink.textContent = anchor.textContent;
      }
    }
    showHideReleaseSections();
  });

  function toggleChecked() {
    document.querySelectorAll("input[type=checkbox]").forEach((input) => {
      if (input.name && /downloadFileSubmissions.*questionCheckbox/.test(input.name)) {
        input.checked = true;
      }
    });
  }

  function showHideReleaseSections() {
    const selected = document.querySelector("input[name='downloadFileSubmissions:siteSection']:checked");
    const sections = document.querySelector(".samigo-download-sections");
    if (sections) {
      sections.classList.toggle("is-open", !!(selected && selected.value === "sections"));
    }
  }
</script>
 <div class="portletBody">
<!-- content... -->
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h:panelGroup layout="block" styleClass="page-header">
    <h1>
      <h:outputText value="#{commonMessages.export_action}#{evaluationMessages.column} " escape="false"/>
      <small><h:outputText value="#{exportResponses.assessmentName} " escape="false"/></small>
    </h1>
  </h:panelGroup>

  <!-- EVALUATION SUBMENU -->
  <%@ include file="/jsf/evaluation/evaluationSubmenu.jsp" %>

  <h:panelGroup layout="block" styleClass="samigo-export-block">
    <h:outputText value="#{evaluationMessages.export_msg}" styleClass="samigo-export-lead"/>
    <p class="act">
      <h:commandButton actionListener="#{exportResponses.exportExcel}" value="#{commonMessages.export_action}" id="exportButton" styleClass="active" />
    </p>
  </h:panelGroup>
</h:form>

<h:form id="downloadFileSubmissions" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize > 0}">
  <h:inputHidden id="publishedId" value="#{downloadFileSubmissions.publishedAssessmentId}" />
  <h:panelGroup layout="block" styleClass="samigo-export-files">
    <h2 class="samigo-download-heading">
      <h:outputText value="#{evaluationMessages.title_download_file_submissions}"/>
    </h2>
    <h:outputLink title="#{evaluationMessages.select_all}" styleClass="samigo-download-select-all" onclick="toggleChecked(); return false;" value="#" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize > 1}">
      <h:outputText value="#{evaluationMessages.select_all}" />
    </h:outputLink>
    <h:dataTable value="#{downloadFileSubmissions.fileUploadQuestionList}" var="question" styleClass="samigo-download-questions" columnClasses="downloanQuestionCheckbox,downloanQuestionDescription">
      <h:column rendered="#{downloadFileSubmissions.fileUploadQuestionListSize > 1}">
        <h:selectManyCheckbox value="" id="questionCheckbox">
          <f:selectItem itemValue="#{question.itemIdString}" />
        </h:selectManyCheckbox>
      </h:column>
      <h:column>
        <h:panelGroup styleClass="samigo-download-qmeta">
          <h:outputText value="#{evaluationMessages.part} #{question.section.sequence}"/>
          <h:outputText value="#{evaluationMessages.column} "/>
          <h:outputText value="#{evaluationMessages.question} #{question.sequence}"/>
        </h:panelGroup>
        <h:outputText value="#{evaluationMessages.q_fu}" styleClass="samigo-download-qtype" rendered="#{question.typeId == 6}"/>
        <h:outputText value="#{evaluationMessages.q_aud}" styleClass="samigo-download-qtype" rendered="#{question.typeId == 7}"/>
        <h:panelGroup layout="block" styleClass="samigo-download-qtext">
          <h:outputText value="#{question.text}" escape="false"/>
        </h:panelGroup>
      </h:column>
    </h:dataTable>

    <h:panelGroup layout="block" styleClass="samigo-download-audience" rendered="#{downloadFileSubmissions.availableSectionSize > 0}">
      <h:selectOneRadio id="siteSection" layout="pageDirection" value="#{downloadFileSubmissions.firstTargetSelected}" onclick="showHideReleaseSections();" required="true">
        <f:selectItems value="#{downloadFileSubmissions.siteSectionItems}" />
      </h:selectOneRadio>
      <h:panelGroup layout="block" styleClass="samigo-download-sections" rendered="#{downloadFileSubmissions.availableSectionSize > 1}">
        <h:selectManyCheckbox id="sectionsForSite" layout="pageDirection" value="#{downloadFileSubmissions.sectionsSelected}">
          <f:selectItems value="#{downloadFileSubmissions.availableSectionItems}" />
        </h:selectManyCheckbox>
      </h:panelGroup>
    </h:panelGroup>

    <p class="act">
      <h:commandButton value="#{evaluationMessages.download}" actionListener="#{downloadFileSubmissions.downloadFiles}" styleClass="active" />
    </p>
  </h:panelGroup>
</h:form>

</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
