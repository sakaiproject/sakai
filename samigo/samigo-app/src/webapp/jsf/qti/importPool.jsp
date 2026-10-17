<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:corejsf="http://java.sun.com/upload">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <!-- 
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
          <h:outputText value="#{authorImportExport.import_qp}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody">
            <!-- content...  -->
            <h:form id="importPoolForm" enctype="multipart/form-data">
              <!-- HEADINGS  -->
              <jsp:directive.include file="/jsf/author/editAssessmentHeadings.jsp"/>
              <h:inputHidden value="#{xmlImport.importType}"/>
              <h3>
                <h:outputText value="#{authorImportExport.import_qp}"/>
              </h3>
              <div class="tier1">
                <div class="tier2">
                  <div class="form_label">
                    <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
                    <!-- currently import pool mirrors import assessment  -->
                    <h:outputText value="#{authorImportExport.import_instructions}" escape="false"/>
                  </div>
                  <br/>
                  <h:panelGroup>
                    <h:outputLabel styleClass="form_label" value="#{authorImportExport.choose_file}"/>
                    <!-- target represents location where import will be temporarily stored
          check valueChangeListener for final destination  -->
                    <corejsf:upload target="jsf/upload_tmp/qti_imports/#{person.id}" valueChangeListener="#{xmlImport.importPool}"/>
                  </h:panelGroup>
                  <f:verbatim>
                    <jsp:text><![CDATA[&nbsp;]]></jsp:text>
                  </f:verbatim>
                  <h:outputText value="#{authorImportExport.file_is}"/>
                  <h:panelGroup>
                    <h:selectOneRadio id="sourceType" layout="pageDirection" value="1" styleClass="samigo-inline-form">
                      <f:selectItem itemLabel="#{authorImportExport.from_samigo}" itemValue="1"/>
                      <f:selectItem itemLabel="#{authorImportExport.from_respondus}" itemValue="2"/>
                    </h:selectOneRadio>
                  </h:panelGroup>
                </div>
                <br/>
                <h:outputText escape="false" value="#{authorImportExport.importExport_warning2}"/>
                <br/>
                <br/>
                <!-- activates the valueChangeListener  -->
                <h:commandButton id="questionPoolLink" value="#{authorImportExport.import_action}" type="submit" style="act" action="poolList">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener"/>
                </h:commandButton>
                <!-- immediate=true bypasses the valueChangeListener  -->
                <h:commandButton value="#{commonMessages.cancel_action}" type="submit" style="act" action="poolList" immediate="true"/>
              </div>
            </h:form>
          </div>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
