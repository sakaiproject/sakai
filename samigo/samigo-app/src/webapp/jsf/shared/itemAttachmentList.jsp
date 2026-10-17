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
  <!-- ATTACHMENTS  -->
  <h:outputText value="#{printMessages.attachments} " escape="false" rendered="#{not empty itemAttachmentList &amp;&amp; delivery.fromPrint}"/>
  <div class="accordion my-3" id="attachmentsAccordion">
    <h:dataTable value="#{itemAttachmentList}" var="attach" border="0" styleClass="w-100 table table-borderless m-0 p-0" columnClasses="p-0" cellpadding="0" cellspacing="0">
      <h:column>
        <div class="accordion-item border rounded">
          <span class="accordion-header d-flex align-items-center px-3 py-2 m-0" style="border-radius: inherit;">
            <h:panelGroup rendered="#{!delivery.fromPrint}">
              <jsp:text><![CDATA[
            <a href="]]></jsp:text>
              <h:outputText value="#{attach.location}"/>
              <jsp:text>" 
              download="</jsp:text>
              <h:outputText value="#{attach.filename}"/>
              <jsp:text>" 
              aria-label="</jsp:text>
              <h:outputText value="#{commonMessages.action_download}"/>
              <jsp:text><![CDATA[" 
              class="btn btn-secondary me-3 bi bi-download"></a>
          ]]></jsp:text>
            </h:panelGroup>
            <div class="flex-grow-1">
              <h:panelGroup rendered="#{!delivery.fromPrint}">
                <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
              </h:panelGroup>
              <h:outputLink value="#{attach.location}" target="_blank" rel="noopener noreferrer" styleClass="text-decoration-none">
                <h:outputText value="#{attach.filename}"/>
              </h:outputLink>
              <h:panelGroup rendered="#{!attach.isLink &amp;&amp; !delivery.fromPrint}">
                <span class="text-muted small">
                  <jsp:text>(</jsp:text>
                  <h:outputText value="#{attach.fileSize}"/>
                  <jsp:text> </jsp:text>
                  <h:outputText value="#{generalMessages.kb}"/>
                  <jsp:text>)</jsp:text>
                </span>
              </h:panelGroup>
            </div>
            <h:panelGroup rendered="#{attach.isMedia &amp;&amp; !delivery.fromPrint}">
              <jsp:text><![CDATA[
            <button class="accordion-button collapsed w-auto p-2 ms-3 rounded gap-3" type="button" data-bs-toggle="collapse" 
                    data-bs-target="[id='collapse-]]></jsp:text>
              <h:outputText value="#{attach.encodedResourceId}"/>
              <jsp:text>']" 
                    aria-expanded="false" 
                    aria-controls="collapse-</jsp:text>
              <h:outputText value="#{attach.encodedResourceId}"/>
              <jsp:text><![CDATA[">
              ]]></jsp:text>
              <h:outputText value="#{commonMessages.action_preview}"/>
              <jsp:text><![CDATA[
            </button>
          ]]></jsp:text>
            </h:panelGroup>
          </span>
          <h:panelGroup rendered="#{attach.isMedia &amp;&amp; !delivery.fromPrint}">
            <jsp:text><![CDATA[
          
          <div id="collapse-]]></jsp:text>
            <h:outputText value="#{attach.encodedResourceId}"/>
            <jsp:text><![CDATA[" class="accordion-collapse collapse" data-bs-parent="#attachmentsAccordion">
            
            ]]></jsp:text>
            <div class="accordion-body border-top p-3">
              <h:panelGroup rendered="#{attach.isInlineVideo}">
                <jsp:text><![CDATA[
                <embed src="]]></jsp:text>
                <h:outputText value="#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&amp;amp;mimeType=#{attach.mimeType}&amp;amp;filename=#{attach.filename}"/>
                <jsp:text><![CDATA[" 
                       volume="50" 
                       autostart="false"
                       style="width: 100%; max-width: 400px; height: auto; max-height: 350px;" />
              ]]></jsp:text>
              </h:panelGroup>
              <h:panelGroup rendered="#{attach.isInlineImage}">
                <jsp:text><![CDATA[
                <img src="]]></jsp:text>
                <h:outputText value="#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&amp;amp;mimeType=#{attach.mimeType}&amp;amp;filename=#{attach.filename}"/>
                <jsp:text><![CDATA[" 
                     style="width: 100%; max-width: 400px; height: auto; max-height: 350px;" />
              ]]></jsp:text>
              </h:panelGroup>
            </div>
            <jsp:text><![CDATA[
            
          </div>

        ]]></jsp:text>
          </h:panelGroup>
          <h:panelGroup rendered="#{attach.isInlineImage &amp;&amp; delivery.fromPrint}">
            <jsp:text><![CDATA[
          <img src="]]></jsp:text>
            <h:outputText value="#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&amp;amp;mimeType=#{attach.mimeType}&amp;amp;filename=#{attach.filename}"/>
            <jsp:text><![CDATA[" 
                style="width: 100%; max-width: 400px; height: auto; max-height: 350px;" />
        ]]></jsp:text>
          </h:panelGroup>
        </div>
      </h:column>
    </h:dataTable>
  </div>
</jsp:root>
