<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text><![CDATA[<html>
]]></jsp:text>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[



<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
]]></jsp:text>
  <!--
* $Id: timeout.jsp 4069 2005-11-21 19:33:41Z hquinn@stanford.edu $

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
          <h:outputText value="#{deliveryMessages.time_expired_title}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody">
            <!-- content...  -->
            <h3>
              <h:outputText value="#{deliveryMessages.time_expired_title}"/>
            </h3>
            <h:outputText value="#{deliveryMessages.time_expired}"/>
            <h:form id="timeExpired">
              <p class="act">
                <h:commandButton value="#{deliveryMessages.button_return}" type="submit" styleClass="active" action="select" rendered="#{delivery.actionString!='takeAssessmentViaUrl'}">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener"/>
                </h:commandButton>
              </p>
            </h:form>
            <!-- end content  -->
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
  <jsp:text><![CDATA[
</html>
]]></jsp:text>
</jsp:root>
