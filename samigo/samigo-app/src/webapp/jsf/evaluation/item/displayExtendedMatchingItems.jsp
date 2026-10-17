<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id: displayExtendedMatchingItems.jsp 70946 2010-01-06 14:47:07Z gopal.ramasammycook@gmail.com $
include file for displaying Extended Matching Items questions
 -->
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
  <h:outputText value="#{question.text}" escape="false"/>
  <f:verbatim>
    <jsp:text><![CDATA[</h5><h3>]]></jsp:text>
  </f:verbatim>
  <h:outputText value="#{question.themeText}" escape="false"/>
  <f:verbatim>
    <jsp:text><![CDATA[</h3>]]></jsp:text>
    <br/>
  </f:verbatim>
  <h:dataTable value="#{question.emiAnswerOptions}" var="option" styleClass="simpleBorder" cellspacing="0">
    <h:column>
      <h:panelGroup rendered="#{option.text != null &amp;&amp; option.text ne ''}">
        <h:outputText escape="false" value="#{option.label}. #{option.text}"/>
      </h:panelGroup>
    </h:column>
  </h:dataTable>
  <h:outputText value="#{question.emiAnswerOptionsRichText}" escape="false" rendered="#{question.isAnswerOptionsRich}"/>
  <!-- ATTACHMENTS BELOW - EMI RICH ANSWER OPTIONS -->
  <h:dataTable value="#{question.itemAttachmentList}" var="attach" rendered="#{question.isAnswerOptionsRich}">
    <h:column rendered="#{!attach.isMedia}">
      <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
    </h:column>
    <h:column>
      <f:verbatim>
        <jsp:text><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></jsp:text>
      </f:verbatim>
      <h:outputText escape="false" value="&#10;&#9;    &lt;embed src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?actionMode=preview&amp;resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; volume=&quot;50&quot; height=&quot;350&quot; width=&quot;400&quot; autostart=&quot;false&quot;/&gt;" rendered="#{attach.isInlineVideo}"/>
      <h:outputText escape="false" value="&#10;&#9;    &lt;img src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?actionMode=preview&amp;resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; /&gt;" rendered="#{attach.isInlineImage}"/>
      <h:outputLink value="#{attach.location}" target="new_window" rendered="#{!attach.isMedia}">
        <h:outputText escape="false" value="#{attach.filename}"/>
      </h:outputLink>
    </h:column>
    <h:column>
      <f:verbatim>
        <jsp:text><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></jsp:text>
      </f:verbatim>
      <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink &amp;&amp; !attach.isMedia}"/>
    </h:column>
  </h:dataTable>
  <!-- ATTACHMENTS ABOVE - EMI RICH ANSWER OPTIONS -->
  <f:verbatim>
    <br/>
    <br/>
  </f:verbatim>
  <h:outputText escape="false" value="#{question.leadInText}"/>
  <f:verbatim>
    <br/>
    <br/>
  </f:verbatim>
  <h:dataTable value="#{question.emiQuestionAnswerCombinations}" var="option" styleClass="simpleBorder" cellspacing="0">
    <h:column>
      <h:panelGroup rendered="#{option.text != null &amp;&amp; option.text ne ''}">
        <h:outputText escape="false" value="#{option.sequence}. #{option.text}"/>
        <h:outputLabel value="#{authorMessages.correct_options}:"/>
        <h:outputText escape="false" value="#{option.emiCorrectOptionLabels}"/>
        <h:outputText escape="false" value="(#{option.requiredOptionsCount} #{authorMessages.answers_required})" rendered="#{option.requiredOptionsCount&gt;0}"/>
      </h:panelGroup>
    </h:column>
  </h:dataTable>
  <jsp:directive.include file="/jsf/evaluation/item/displayTags.jsp"/>
</jsp:root>
