<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <jsp:text><![CDATA[




<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
]]></jsp:text>
  <!--
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
  <!-- itemData is ItemData.java  -->
  <h:outputText escape="false" value="#{question.itemData.text}"/>
  <f:verbatim>
    <br/>
  </f:verbatim>
  <!-- ATTACHMENTS  -->
  <jsp:directive.include file="/jsf/author/preview_item/attachment.jsp"/>
  <f:verbatim>
    <br/>
  </f:verbatim>
  <t:dataTable id="table1" value="#{question.itemData.rowChoices}" var="row" rowIndexVar="rowIndex" columnClasses="matrixColumn" rowClasses="" frame="border" rules="rows" styleClass="matrixTable">
    <t:column headerstyle="#{question.itemData.relativeWidthStyle}" style="text-align:right;padding-left:0.3em">
      <f:facet name="header">
        <t:outputText value="                "/>
      </f:facet>
      <h:outputText value="#{row}"/>
    </t:column>
    <t:columns value="#{question.itemData.columnIndexList}" var="colIndex" styleClass="center" headerstyleClass="center matrixSurvey">
      <f:facet name="header">
        <t:outputText value="#{question.itemData.columnChoices[colIndex]}"/>
      </f:facet>
      <div style="text-align: center;">
        <h:panelGroup id="image2" styleClass="samigo-inline-radio fa fa-circle-o"/>
      </div>
    </t:columns>
  </t:dataTable>
  <f:verbatim>
    <br/>
  </f:verbatim>
  <h:panelGrid rendered="#{question.itemData.addCommentFlag}">
    <h:outputText value="#{question.itemData.commentField}"/>
  </h:panelGrid>
  <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow &amp;&amp; publishedSettings.feedbackAuthoring ne '2' }">
    <h:outputLabel value="#{commonMessages.feedback}: "/>
    <h:outputText value="#{question.itemData.generalItemFeedback}" escape="false"/>
  </h:panelGroup>
  <jsp:directive.include file="/jsf/author/preview_item/tags.jsp"/>
</jsp:root>
