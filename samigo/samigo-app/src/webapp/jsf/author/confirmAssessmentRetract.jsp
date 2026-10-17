<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text><![CDATA[<html>]]>&#13;
</jsp:text>
  <jsp:directive.page language="java"/>
  <jsp:text>&#13;
&#13;
&#13;
&#13;<![CDATA[
<!DOCTYPE html]]>&#13;
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"&#13;<![CDATA[
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">]]>&#13;
&#13;
</jsp:text>
  <!-- $Id: confirmAssessmentRetract.jsp 17095 2006-10-12 22:32:50Z ktsao@stanford.edu $

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
          <h:outputText value="#{assessmentSettingsMessages.retract_heading}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <jsp:text>&#13;<![CDATA[
 <div class="portletBody">]]>&#13;
  </jsp:text>
          <h3>
            <h:outputText value="#{assessmentSettingsMessages.retract_conf}"/>
          </h3>
          <h:form id="retractAssessmentForm">
            <h:inputHidden id="templateId" value="#{template.idString}"/>
            <br/>
            <jsp:text>&#13;<![CDATA[
     <div>]]>&#13;
       </jsp:text>
            <h:outputText value="#{assessmentSettingsMessages.retract_for_sure}" styleClass="sak-banner-warn"/>
            <p class="act">
              <h:commandButton id="retract" value="#{assessmentSettingsMessages.button_stop_accepting}" type="submit" styleClass="active" action="author">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SavePublishedSettingsListener"/>
              </h:commandButton>
              <h:commandButton value="#{commonMessages.cancel_action}" type="submit" style="act" action="editPublishedAssessmentSettings"/>
            </p>
          </h:form>
          <!-- end content  -->
          <jsp:text>&#13;<![CDATA[
</div>]]>&#13;
      </jsp:text>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
  <jsp:text>&#13;<![CDATA[
</html>]]>&#13;
</jsp:text>
</jsp:root>
