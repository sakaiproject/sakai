<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:t="http://myfaces.apache.org/tomahawk">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[



<!DOCTYPE html>
]]></jsp:text>
  <!-- $Id: importAssessment.jsp 20403 2007-01-18 04:15:06Z ktsao@stanford.edu $

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
          <h:outputText value="#{authorImportExport.export_a} #{authorImportExport.dash} #{assessmentBean.title}"/>
        </title>
        <script>
          <jsp:text>
function getSelectedType(qtiUrl, cpUrl, emtUrl, e2mt){
  if ( $("#exportAssessmentForm\\:exportType\\:1").prop("checked") ) {
    window.open( qtiUrl, '_qti_export', 'toolbar=yes,menubar=yes,personalbar=no,width=600,height=500,scrollbars=yes,resizable=yes');
  }
  else if ($("#exportAssessmentForm\\:exportType\\:0").prop("checked")) {
    window.location = cpUrl;
  }
  else if (e2mt === 'false') {
    window.location = emtUrl;
  } 
  else if (confirm('</jsp:text>
          <h:outputText value="#{authorImportExport.export_confirm}"/>
          <jsp:text>')) {
    window.location = emtUrl;  
  }
}
</jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <!-- content...  -->
            <h:form id="exportAssessmentForm">
              <h:inputHidden id="assessmentBaseId" value="#{assessmentBean.assessmentId}"/>
              <h1>
                <h:outputText value="#{authorImportExport.export_a}" escape="false"/>
                <small>
                  <h:outputText value="#{authorImportExport.dash} #{assessmentBean.title}" escape="false"/>
                </small>
              </h1>
              <div class="form_label">
                <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
                <p class="">
                  <h:outputText value="#{authorImportExport.choose_type_1}" escape="true"/>
                  <h:outputText value="&amp;#160;" escape="false"/>
                  <h:outputLink value="#" onclick="window.open('http://www.imsglobal.org/question/')" onkeypress="window.open('http://www.imsglobal.org/question/')">
                    <h:outputText value="#{authorImportExport.ims_qti}"/>
                  </h:outputLink>
                  <h:outputText value="&amp;#160;" escape="false"/>
                  <h:outputText value="," escape="true"/>
                  <h:outputText value="&amp;#160;" escape="false"/>
                  <h:outputLink value="#" onclick="window.open('http://www.imsglobal.org/content/packaging/')" onkeypress="window.open('http://www.imsglobal.org/content/packaging/')">
                    <h:outputText value="#{authorImportExport.ims_cp}"/>
                  </h:outputLink>
                  <h:outputText value=" #{authorImportExport.choose_type_2} " escape="true"/>
                  <h:outputText value="#{authorImportExport.markup_text}" escape="true"/>
                  <h:outputText value="#{authorImportExport.choose_type_3}" escape="true"/>
                  <h:outputText value="#{authorImportExport.markup_text_note}" escape="true"/>
                  <br/>
                </p>
                <p>
                  <h:outputText value="#{authorImportExport.importExport_warningHeader}" escape="false"/>
                </p>
                <p class="sak-banner-warn">
                  <h:outputText value="#{authorImportExport.importExport_warning1}" escape="false"/>
                </p>
                <p class="sak-banner-warn">
                  <h:outputText value="#{authorImportExport.importExport_warning2}" escape="false"/>
                </p>
                <p class="sak-banner-warn">
                  <h:outputText value="#{authorImportExport.cp_message}"/>
                </p>
              </div>
              <h:panelGroup layout="block">
                <h:outputLabel value="#{authorImportExport.choose_export_type}" for="exportType"/>
                <t:selectOneRadio id="exportType" layout="spread" value="2">
                  <f:selectItem itemLabel="#{authorImportExport.content_packaging}" itemValue="2"/>
                  <f:selectItem itemLabel="#{authorImportExport.qti12}" itemValue="1"/>
                  <f:selectItem itemLabel="#{authorImportExport.markup_text}" itemValue="3"/>
                </t:selectOneRadio>
                <h:panelGrid>
                  <t:radio renderLogicalId="true" for="exportType" index="0"/>
                  <t:radio renderLogicalId="true" for="exportType" index="1"/>
                  <t:radio renderLogicalId="true" for="exportType" index="2"/>
                </h:panelGrid>
              </h:panelGroup>
              <p class="act">
                <!-- activates the valueChangeListener  -->
                <h:commandButton value="#{authorImportExport.export}" type="submit" styleClass="active" onclick="getSelectedType( '/portal/tool/#{requestScope['sakai.tool.placement.id']}/jsf/qti/exportAssessment.faces?exportAssessmentId=#{assessmentBean.assessmentId}',&#10;       '/samigo-app/servlet/DownloadCP?&amp;assessmentId=#{assessmentBean.assessmentId}', &#10;       '/samigo-app/servlet/ExportMarkupText?&amp;assessmentId=#{assessmentBean.assessmentId}', '#{!assessmentBean.exportable2MarkupText}'); return false;"/>
                <!-- immediate=true bypasses the valueChangeListener  -->
                <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="author" immediate="true"/>
              </p>
            </h:form>
          </div>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
