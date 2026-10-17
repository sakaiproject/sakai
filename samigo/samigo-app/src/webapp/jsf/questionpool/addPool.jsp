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
          <h:outputText value="#{questionPoolMessages.add_title}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <div class="portletBody container-fluid">
            <script>
              <jsp:text><![CDATA[
function textCounter(field, maxlimit) {
	if (field.value.length > maxlimit) // if too long...trim it!
		field.value = field.value.substring(0, maxlimit);
}
]]></jsp:text>
            </script>
            <h:form id="questionpool">
              <h3 class="insColor insBak insBor">
                <h:outputText value="#{questionPoolMessages.add_p}"/>
              </h3>
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <p>
                <h:outputText value="#{questionPoolMessages.add_p_required}"/>
              </p>
              <div class="form-group row">
                <h:outputLabel for="namefield" value="#{questionPoolMessages.p_name}#{questionPoolMessages.star}" styleClass="form-label col-md-2"/>
                <div class="col-md-6">
                  <h:inputText id="namefield" maxlength="255" size="30" value="#{questionpool.currentPool.displayName}" styleClass="form-control"/>
                </div>
              </div>
              <div class="form-group row">
                <h:outputLabel for="ownerfield" value="#{questionPoolMessages.creator}" styleClass="form-label col-md-2"/>
                <div class="col-md-6">
                  <h:outputText id="ownerfield" value="#{questionpool.currentPool.owner}"/>
                </div>
              </div>
              <div class="form-group row">
                <h:outputLabel for="orgfield" value="#{questionPoolMessages.dept} " styleClass="form-label col-md-2"/>
                <div class="col-md-6">
                  <h:inputText id="orgfield" maxlength="255" size="30" value="#{questionpool.currentPool.organizationName}" styleClass="form-control"/>
                </div>
              </div>
              <div class="form-group row">
                <h:outputLabel for="descfield" value="#{questionPoolMessages.desc}" styleClass="form-label col-md-2"/>
                <div class="col-md-10">
                  <h:inputTextarea id="descfield" value="#{questionpool.currentPool.description}" cols="40" rows="6"/>
                </div>
              </div>
              <div class="form-group row">
                <h:outputLabel for="objfield" value="#{questionPoolMessages.obj} " styleClass="form-label col-md-2"/>
                <div class="col-md-6">
                  <h:inputText id="objfield" maxlength="255" size="30" value="#{questionpool.currentPool.objectives}" styleClass="form-control"/>
                </div>
              </div>
              <div class="form-group row">
                <h:outputLabel for="keyfield" value="#{questionPoolMessages.keywords} " styleClass="form-label col-md-2"/>
                <div class="col-md-6">
                  <h:inputText id="keyfield" maxlength="255" size="30" value="#{questionpool.currentPool.keywords}" styleClass="form-control"/>
                </div>
              </div>
              <h:panelGroup rendered="#{questionpool.showTags &amp;&amp; questionpool.canManageTags}" layout="block" styleClass="form-group row">
                <label for="tag-selector" class="col-sm-2 form-label">
                  <h:outputText value="#{questionPoolMessages.t_tags}"/>
                </label>
                <div class="col-sm-6">
                  <jsp:text><![CDATA[
            <sakai-tag-selector
                id="tag-selector"
                input-id="questionpool:questionPoolTags"
                class="b5 flex-grow-1"
                selected-ids="]]></jsp:text>
                  <h:outputText value="#{questionpool.currentPool.tags.tagIdsCsv}"/>
                  <jsp:text><![CDATA["
                add-new="true"
            ></sakai-tag-selector>
            ]]></jsp:text>
                  <h:inputHidden id="questionPoolTags" value="#{questionpool.currentPool.tags.tagIdsCsv}"/>
                  <h:outputText id="tagOptions" value="#{questionpool.tagOptionsJson}" style="display: none"/>
                  <script>
                    <jsp:text><![CDATA[
                customElements.whenDefined("sakai-tag-selector").then(() => {
                    const selector = document.getElementById("tag-selector");
                    selector.options = JSON.parse(document.getElementById("questionpool:tagOptions").textContent);
                    const selectedIds = (selector.selectedIds || "").split(",");
                    selector.selectedTags = selector.options.filter(tag => selectedIds.includes(tag.code));
                });
            ]]></jsp:text>
                  </script>
                </div>
              </h:panelGroup>
              <p class="act">
                <h:commandButton id="submit" action="#{questionpool.doit}" value="#{questionPoolMessages.save}" styleClass="active">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.PoolSaveListener"/>
                  <f:attribute name="addsource" value="editpool"/>
                </h:commandButton>
                <h:commandButton style="act" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" immediate="true">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener"/>
                  <f:attribute name="returnToParentPool" value="true"/>
                </h:commandButton>
              </p>
            </h:form>
          </div>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
