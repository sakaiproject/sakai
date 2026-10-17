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
          <h:outputText value="#{questionPoolMessages.mv_p}"/>
        </title>
        <!-- stylesheet and script widgets  -->
        <script>
          <jsp:directive.include file="/js/samigotree.js"/>
          <jsp:text>
              function flagFolders() {
	          collapseAllRowsForSelectList();
              }
              window.onload = flagFolders;
</jsp:text>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/js/spinner.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false">collapseAllRowsForSelectList();<jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <div class="portletBody">
            <h:form id="movePool">
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <h3>
                <h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionPoolMessages.mv_p}"/>
                <h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{questionPoolMessages.mv_q}"/>
                <h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{questionPoolMessages.sol_q}"/>
              </h3>
              <div class="tier1">
                <h:outputText value="#{questionPoolMessages.sel_dest_move} "/>
                <h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionpool.currentPool.displayName}"/>
                <h:outputText rendered="#{questionpool.actionType == 'item' &amp;&amp; questionpool.currentItems[1] == null}" escape="false" value="#{questionpool.currentItems[0].text}"/>
                <h:dataTable rendered="#{questionpool.actionType == 'item' &amp;&amp; questionpool.currentItems[1] != null}" id="questions" value="#{questionpool.currentItems}" var="curItem">
                  <h:column>
                    <h:outputText escape="false" value="#{curItem.text}"/>
                  </h:column>
                </h:dataTable>
              </div>
              <!--
<h:outputText styleClass="number" value="1"/>
<h:outputLabel rendered="#{questionpool.actionType == 'item'}" value="#{questionPoolMessages.mv_q_to}"/>
<h:outputLabel rendered="#{questionpool.actionType == 'pool'}" value="#{questionPoolMessages.mv_p_to}"/>
<br/><br/>
 -->
              <div class="longtext tier2">
                <jsp:directive.include file="/jsf/questionpool/movePoolTree.jsp"/>
                <h:inputHidden id="selectedRadioBtn" value="#{questionpool.destPool}"/>
                <!--
<br/>
<h:outputText styleClass="number" value="2"/>
<h:outputLabel value="#{questionPoolMessages.click_move}"/>
 -->
              </div>
              <p class="act">
                <h:commandButton type="submit" immediate="true" id="poolSubmit" value="#{questionPoolMessages.move}" action="#{questionpool.movePool}" rendered="#{questionpool.actionType == 'pool'}" styleClass="active" onclick="SPNR.disableControlsAndSpin(this, null);">
                  <f:param name="qpid" value="#{questionpool.currentPool.parentPoolId}"/>
                </h:commandButton>
                <h:commandButton type="submit" immediate="true" id="itemSubmit" value="#{questionPoolMessages.move}" action="#{questionpool.moveQuestion}" rendered="#{questionpool.actionType == 'item'}" styleClass="active" onclick="SPNR.disableControlsAndSpin(this, null);">
                </h:commandButton>
                <h:commandButton id="cancel" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" immediate="true" rendered="#{questionpool.actionType == 'pool'}" onclick="SPNR.disableControlsAndSpin(this, null);">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener"/>
                  <f:attribute name="returnToParentPool" value="true"/>
                </h:commandButton>
                <h:commandButton id="cancelItem" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" immediate="true" rendered="#{questionpool.actionType == 'item'}" onclick="SPNR.disableControlsAndSpin(this, null);">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener"/>
                  <f:attribute name="returnToParentPool" value="false"/>
                </h:commandButton>
              </p>
            </h:form>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
