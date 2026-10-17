<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!--
* $Id: assessment_attachment.jsp 6874 2006-03-22 17:01:47Z hquinn@stanford.edu $

***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
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
***********************************************************************************/

 -->
  <!-- 2a ATTACHMENTS  -->
  <h:panelGroup rendered="#{delivery.hasAttachment}">
    <f:verbatim>
      <jsp:element name="p">
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </f:verbatim>
  </h:panelGroup>
  <h:outputLabel value="#{deliveryMessages.attachments}" rendered="#{delivery.hasAttachment}"/>
  <h:panelGroup rendered="#{delivery.hasAttachment}">
    <h:dataTable value="#{delivery.attachmentList}" var="attach" border="0">
      <h:column>
        <h:outputText escape="false" value="&#10;&#9;    &lt;embed src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; volume=&quot;50&quot; height=&quot;350&quot; width=&quot;400&quot; autostart=&quot;false&quot;/&gt;" rendered="#{attach.isInlineVideo &amp;&amp; !delivery.fromPrint}"/>
        <h:outputText value="#{attach.filename}" rendered="#{attach.isInlineVideo &amp;&amp; delivery.fromPrint}"/>
        <h:outputText escape="false" value="&#10;&#9;    &lt;img src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; /&gt;" rendered="#{attach.isInlineImage}"/>
        <h:panelGrid rendered="#{!attach.isMedia &amp;&amp; !delivery.fromPrint}" border="0" columns="2">
          <h:column>
            <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
            <f:verbatim>
              <jsp:text><![CDATA[&nbsp;&nbsp;]]></jsp:text>
            </f:verbatim>
            <h:outputLink value="#{attach.location}" target="new_window">
              <h:outputText value="#{attach.filename}"/>
            </h:outputLink>
          </h:column>
          <h:column>
            <f:verbatim>
              <jsp:text><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></jsp:text>
            </f:verbatim>
            <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink}"/>
          </h:column>
        </h:panelGrid>
        <h:outputText value="#{attach.filename}" rendered="#{!attach.isMedia &amp;&amp; delivery.fromPrint}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.hasAttachment}">
    <f:verbatim>
      <br/>
    </f:verbatim>
  </h:panelGroup>
</jsp:root>
