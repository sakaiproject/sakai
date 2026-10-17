<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[



<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
]]></jsp:text>
  <!-- $Id$

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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{questionPoolMessages.q_mgr}"/>
        </title>
        <script>
          <jsp:directive.include file="/js/samigotree.js"/>
        </script>
        <script>
          <jsp:text>
              function flagFolders() {
                  collapseAllRows();
              }
              window.onload = flagFolders;
          </jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false">collapseAllRows();<jsp:expression> request.getAttribute("html.body.onload") </jsp:expression>;disabledButton()</jsp:attribute>
        <jsp:body>
          <h:panelGroup layout="block" styleClass="portletBody container-fluid">
            <!-- content...  -->
            <h:form id="questionpool">
              <ul class="navIntraTool actionToolbar" role="menu">
                <h:panelGroup rendered="#{authorization.createAssessment eq true or authorization.editAnyAssessment eq true or authorization.editOwnAssessment eq true or authorization.gradeAnyAssessment eq true or authorization.gradeOwnAssessment eq true}">
                  <li role="menuitem">
                    <h:panelGroup styleClass="menuitem">
                      <h:commandLink title="#{generalMessages.t_assessment}" rendered="#{questionpool.importToAuthoring eq true}" action="author" immediate="true">
                        <h:outputText value="#{generalMessages.assessment}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelImportToAssessmentListener"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetResultsCalculatedListener"/>
                      </h:commandLink>
                      <h:commandLink title="#{generalMessages.t_assessment}" rendered="#{questionpool.importToAuthoring eq false}" action="author" immediate="true">
                        <h:outputText value="#{generalMessages.assessment}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetResultsCalculatedListener"/>
                      </h:commandLink>
                    </h:panelGroup>
                  </li>
                </h:panelGroup>
                <h:panelGroup rendered="#{authorization.createAssessment eq true}">
                  <li role="menuitem">
                    <h:panelGroup styleClass="menuitem">
                      <h:commandLink title="#{generalMessages.add}" action="#{author.getOutcome}" immediate="true">
                        <f:param name="action" value="create_assessment_title"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener"/>
                        <h:outputText value="#{generalMessages.add}"/>
                      </h:commandLink>
                    </h:panelGroup>
                  </li>
                </h:panelGroup>
                <h:panelGroup rendered="#{authorization.adminTemplate eq true and template.showAssessmentTypes eq true}">
                  <li role="menuitem">
                    <h:panelGroup styleClass="menuitem">
                      <h:commandLink title="#{generalMessages.t_template}" rendered="#{questionpool.importToAuthoring eq false}" action="template" immediate="true">
                        <h:outputText value="#{generalMessages.template}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener"/>
                      </h:commandLink>
                      <h:commandLink title="#{generalMessages.t_questionPool}" rendered="#{questionpool.importToAuthoring eq true}" action="template" immediate="true">
                        <h:outputText value="#{generalMessages.template}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelImportToAssessmentListener"/>
                      </h:commandLink>
                    </h:panelGroup>
                  </li>
                </h:panelGroup>
                <li role="menuitem">
                  <h:panelGroup styleClass="current">
                    <h:outputText value="#{questionPoolMessages.qps}"/>
                  </h:panelGroup>
                </li>
                <li role="menuitem">
                  <h:panelGroup styleClass="menuitem">
                    <h:commandLink id="evnetLogLink" accesskey="#{generalMessages.a_log}" title="#{generalMessages.t_eventLog}" action="eventLog" immediate="true" rendered="#{authorization.adminQuestionPool eq true}">
                      <h:outputText value="#{generalMessages.eventLog}"/>
                      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener"/>
                    </h:commandLink>
                  </h:panelGroup>
                </li>
                <li role="menuitem">
                  <h:panelGroup styleClass="menuitem">
                    <h:commandLink id="sectionActivity" accesskey="#{generalMessages.a_section_activity}" title="#{generalMessages.section_activity}" action="sectionActivity" immediate="true" rendered="#{authorization.adminQuestionPool eq true}">
                      <h:outputText value="#{generalMessages.section_activity}"/>
                      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener"/>
                    </h:commandLink>
                  </h:panelGroup>
                </li>
                <jsp:directive.include file="/jsf/author/permissionsNav.jsp"/>
                <h:panelGroup rendered="#{authorization.adminAssessment eq true}">
                  <li role="menuitem">
                    <h:panelGroup styleClass="menuitem">
                      <h:commandLink id="restoreAssessments" accesskey="#{generalMessages.a_restore_assessments}" title="#{generalMessages.t_restore_assessments}" action="restoreAssessments" immediate="true">
                        <h:outputText value="#{generalMessages.restore_assessments}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RestoreAssessmentsListener"/>
                      </h:commandLink>
                    </h:panelGroup>
                  </li>
                </h:panelGroup>
              </ul>
              <h:panelGroup layout="block" styleClass="page-header">
                <h1>
                  <h:outputText value="#{generalMessages.questionPool}"/>
                </h1>
              </h:panelGroup>
              <h:outputText rendered="#{questionpool.importToAuthoring eq true}" value="#{questionPoolMessages.msg_imp_poolmanager}"/>
              <h:outputText escape="false" rendered="#{questionpool.importToAuthoring eq false and authorization.createQuestionPool eq true}" value="&lt;p class=&quot;navViewAction&quot;&gt;"/>
              <h:commandLink title="#{questionPoolMessages.t_addPool}" rendered="#{questionpool.importToAuthoring eq false and authorization.createQuestionPool eq true}" id="add" immediate="true" action="#{questionpool.addPool}">
                <h:outputText value="#{questionPoolMessages.add_new_pool}"/>
                <f:param name="qpid" value="0"/>
                <f:param name="outCome" value="poolList"/>
              </h:commandLink>
              <h:outputText value=" #{generalMessages.separator}" rendered="#{questionpool.importToAuthoring eq false and authorization.createQuestionPool eq true}"/>
              <h:outputText value="&amp;#160;" escape="false"/>
              <h:commandLink title="#{questionPoolMessages.t_importPool}" rendered="#{questionpool.importToAuthoring eq false and authorization.createQuestionPool eq true}" id="import" immediate="true" action="importPool">
                <h:outputText value="#{questionPoolMessages.t_importPool}"/>
                <f:param name="qpid" value="0"/>
                <f:param name="outCome" value="poolList"/>
              </h:commandLink>
              <!-- SAM-2049  -->
              <h:outputText value=" #{generalMessages.separator}" rendered="#{questionpool.importToAuthoring eq false and authorization.createQuestionPool eq true}"/>
              <h:outputText value="&amp;#160;" escape="false"/>
              <h:commandLink title="#{questionPoolMessages.t_transferPool}" rendered="#{questionpool.importToAuthoring eq false and authorization.createQuestionPool eq true}" id="transfer" immediate="true" action="#{questionpool.transferPool}">
                <h:outputText value="#{questionPoolMessages.transfer_pool_ownership}"/>
                <f:param name="qpid" value="0"/>
                <f:param name="outCome" value="poolList"/>
              </h:commandLink>
              <h:outputText rendered="#{questionpool.importToAuthoring eq false and authorization.createQuestionPool eq true}" escape="false" value="&lt;/p&gt;"/>
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <jsp:directive.include file="/jsf/questionpool/poolTreeTable.jsp"/>
              <h:panelGroup layout="block" styleClass="act">
                <h:commandButton rendered="#{questionpool.importToAuthoring eq false and authorization.deleteOwnQuestionPool eq true}" type="submit" immediate="true" id="Submit" value="#{questionPoolMessages.delete}" action="#{questionpool.startRemovePool}" styleClass="d-none">
                </h:commandButton>
                <h:commandButton rendered="#{questionpool.importToAuthoring eq true}" type="submit" immediate="true" id="cancel" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelImport}">
                </h:commandButton>
              </h:panelGroup>
            </h:form>
          </h:panelGroup>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
