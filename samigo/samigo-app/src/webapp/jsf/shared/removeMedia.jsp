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
          <h:outputText value="#{deliveryMessages.remove_p_conf}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <h:form>
            <h:inputHidden id="showTimer" value="#{delivery.showTimer}"/>
            <h:inputHidden id="mediaId" value="#{mediaBean.mediaId}"/>
            <h3>
              <jsp:text> </jsp:text>
              <h:outputText value="#{deliveryMessages.remove_media_conf}"/>
            </h3>
            <div class="sak-banner-warn">
              <h:outputText value="#{deliveryMessages.cert_rem_media}"/>
            </div>
            <p>
              <h:outputLink title="#{deliveryMessages.t_media}" value="#" onclick="window.open('#{mediaBean.mediaUrl}','new_window');" onkeypress="window.open('#{mediaBean.mediaUrl}','new_window');">
                <h:outputText value="#{mediaBean.filename}"/>
              </h:outputLink>
            </p>
            <p class="act">
              <h:commandButton id="remove" type="submit" value="#{commonMessages.remove_action}" action="#{delivery.getOutcome}" styleClass="active">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.RemoveMediaListener"/>
              </h:commandButton>
              <h:commandButton id="cancel" value="#{commonMessages.cancel_action}" type="submit" action="takeAssessment">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.UpdateTimerListener"/>
              </h:commandButton>
              <h:commandButton id="save" styleClass="d-none" type="submit" value="#{commonMessages.action_save}" action="#{delivery.saveWork}" rendered="#{delivery.actionString=='previewAssessment'&#10;                 || delivery.actionString=='takeAssessment'&#10;                 || delivery.actionString=='takeAssessmentViaUrl'}"/>
            </p>
            <!-- HEADING  -->
            <jsp:directive.include file="/jsf/delivery/questionProgress.jspf"/>
            <jsp:directive.include file="/jsf/delivery/assessmentDeliveryTimer.jsp"/>
          </h:form>
          <!-- end content  -->
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
