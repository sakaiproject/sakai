<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text>&#13;
&#13;
&#13;
&#13;
&#13;<![CDATA[
<!DOCTYPE html]]>&#13;
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"&#13;<![CDATA[
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">]]>&#13;
&#13;
</jsp:text>
  <!--
$Id: fullShortAnswer.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $

***********************************************************************************
*
* Copyright (c) 2008 The Sakai Foundation.
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
          <h:outputText value="#{assessmentSettingsMessages.mark_for_review}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <h:form>
            <h:panelGrid columns="1" border="0">
              <h:outputText value="#{assessmentSettingsMessages.mark_for_review_pop_up_text_1}"/>
              <h:outputText value="&amp;nbsp;" escape="false"/>
              <h:panelGroup>
                <f:verbatim>
                  <jsp:text><![CDATA[<b>]]></jsp:text>
                </f:verbatim>
                <h:outputText value="#{assessmentSettingsMessages.mark_for_review_pop_up_text_2}"/>
                <f:verbatim>
                  <jsp:text><![CDATA[</b>]]></jsp:text>
                </f:verbatim>
              </h:panelGroup>
              <h:outputText value="&amp;nbsp;" escape="false"/>
              <h:commandButton id="close" onclick="window.close();" onkeypress="window.close();" value="#{assessmentSettingsMessages.button_close}"/>
            </h:panelGrid>
          </h:form>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
