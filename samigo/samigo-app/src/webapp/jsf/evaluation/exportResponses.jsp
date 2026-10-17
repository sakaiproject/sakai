<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text>&#13;
&#13;
&#13;
&#13;
&#13;
&#13;<![CDATA[
<!DOCTYPE html]]>&#13;
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"&#13;<![CDATA[
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">]]>&#13;
</jsp:text>
  <!--

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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{commonMessages.export_action}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <script>
            <jsp:text>&#13;<![CDATA[
  document.addEventListener("DOMContentLoaded", () => {]]>&#13;
    // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.&#13;
    const currentLink = document.getElementById("editTotalResults:exportResponsesMenuLink");&#13;
    if (currentLink) {&#13;
      currentLink.classList.add("current");&#13;
      const anchor = currentLink.querySelector("a");&#13;
      if (anchor) {&#13;
        currentLink.textContent = anchor.textContent;&#13;
      }&#13;
    }&#13;
    showHideReleaseSections();&#13;
  });&#13;
&#13;
  function toggleChecked() {&#13;<![CDATA[
    document.querySelectorAll("input[type=checkbox]").forEach((input) => {]]>&#13;<![CDATA[
      if (input.name && /downloadFileSubmissions.*questionCheckbox/.test(input.name)) {]]>&#13;
        input.checked = true;&#13;
      }&#13;
    });&#13;
  }&#13;
&#13;
  function showHideReleaseSections() {&#13;
    const selected = document.querySelector("input[name='downloadFileSubmissions:siteSection']:checked");&#13;
    const sections = document.querySelector(".samigo-download-sections");&#13;
    if (sections) {&#13;<![CDATA[
      sections.classList.toggle("is-open", !!(selected && selected.value === "sections"));]]>&#13;
    }&#13;
  }&#13;
</jsp:text>
          </script>
          <div class="portletBody">
            <!-- content...  -->
            <h:form id="editTotalResults">
              <h:inputHidden id="publishedId" value="#{totalScores.publishedId}"/>
              <h:inputHidden id="itemId" value="#{totalScores.firstItem}"/>
              <!-- HEADINGS  -->
              <jsp:directive.include file="/jsf/evaluation/evaluationHeadings.jsp"/>
              <h:panelGroup layout="block" styleClass="page-header">
                <h1>
                  <h:outputText value="#{commonMessages.export_action}#{evaluationMessages.column} " escape="false"/>
                  <small>
                    <h:outputText value="#{exportResponses.assessmentName} " escape="false"/>
                  </small>
                </h1>
              </h:panelGroup>
              <!-- EVALUATION SUBMENU  -->
              <jsp:directive.include file="/jsf/evaluation/evaluationSubmenu.jsp"/>
              <h:panelGroup layout="block" styleClass="samigo-export-block">
                <h:outputText value="#{evaluationMessages.export_msg}" styleClass="samigo-export-lead"/>
                <p class="act">
                  <h:commandButton actionListener="#{exportResponses.exportExcel}" value="#{commonMessages.export_action}" id="exportButton" styleClass="active"/>
                </p>
              </h:panelGroup>
            </h:form>
            <h:form id="downloadFileSubmissions" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize &gt; 0}">
              <h:inputHidden id="publishedId" value="#{downloadFileSubmissions.publishedAssessmentId}"/>
              <h:panelGroup layout="block" styleClass="samigo-export-files">
                <h2 class="samigo-download-heading">
                  <h:outputText value="#{evaluationMessages.title_download_file_submissions}"/>
                </h2>
                <h:outputLink title="#{evaluationMessages.select_all}" styleClass="samigo-download-select-all" onclick="toggleChecked(); return false;" value="#" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize &gt; 1}">
                  <h:outputText value="#{evaluationMessages.select_all}"/>
                </h:outputLink>
                <h:dataTable value="#{downloadFileSubmissions.fileUploadQuestionList}" var="question" styleClass="samigo-download-questions" columnClasses="#{downloadFileSubmissions.fileUploadQuestionListSize &gt; 1 ? 'downloanQuestionCheckbox,downloanQuestionDescription' : 'downloanQuestionDescription'}">
                  <h:column rendered="#{downloadFileSubmissions.fileUploadQuestionListSize &gt; 1}">
                    <h:selectManyCheckbox value="" id="questionCheckbox">
                      <f:selectItem itemValue="#{question.itemIdString}"/>
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
                <h:panelGroup layout="block" styleClass="samigo-download-audience" rendered="#{downloadFileSubmissions.availableSectionSize &gt; 0}">
                  <h:selectOneRadio id="siteSection" layout="pageDirection" value="#{downloadFileSubmissions.firstTargetSelected}" onclick="showHideReleaseSections();" required="true">
                    <f:selectItems value="#{downloadFileSubmissions.siteSectionItems}"/>
                  </h:selectOneRadio>
                  <h:panelGroup layout="block" styleClass="samigo-download-sections" rendered="#{downloadFileSubmissions.availableSectionSize &gt; 1}">
                    <h:selectManyCheckbox id="sectionsForSite" layout="pageDirection" value="#{downloadFileSubmissions.sectionsSelected}">
                      <f:selectItems value="#{downloadFileSubmissions.availableSectionItems}"/>
                    </h:selectManyCheckbox>
                  </h:panelGroup>
                </h:panelGroup>
                <p class="act">
                  <h:commandButton value="#{evaluationMessages.download}" actionListener="#{downloadFileSubmissions.downloadFiles}" styleClass="active"/>
                </p>
              </h:panelGroup>
            </h:form>
          </div>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
