<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
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
* Copyright (c) 2004, 2005, 2006, 2007 The Sakai Foundation.
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
        <!-- designed to be in  popup window  -->
        <title>
          <h:outputText value="#{authorImportExport.export_a}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <div class="tier1">
            <h:form id="exportAssessmentForm">
              <h:outputText escape="false" value="&lt;input type='hidden' name='assessmentId' value='#{param.exportAssessmentId}'"/>
              <h3 style="insColor insBak">
                <h:outputText value="#{authorImportExport.export_a}"/>
              </h3>
              <div class="validation">
                <h:outputText value="#{authorImportExport.export_instructions}" escape="false"/>
              </div>
              <h:panelGrid columns="2" rendered="false">
                <h:outputText value="#{authorImportExport.im_ex_version_choose}"/>
                <h:selectOneRadio layout="lineDirection">
                  <f:selectItem itemLabel="#{authorImportExport.im_ex_version_12}" itemValue="1"/>
                  <f:selectItem itemLabel="#{authorImportExport.im_ex_version_20}" itemValue="2"/>
                </h:selectOneRadio>
              </h:panelGrid>
              <p class="act">
                <h:commandButton value="#{authorImportExport.export_action}" type="submit" action="#{xml.getOutcome}" immediate="true">
                  <f:param name="assessmentId" value="lastModifiedDate"/>
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ExportAssessmentListener"/>
                </h:commandButton>
                <h:commandButton value="#{commonMessages.cancel_action}" type="reset" onclick="window.close()" style="act" action="author"/>
              </p>
            </h:form>
          </div>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
