<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id: editPart_attachment.jsp 11254 2006-06-28 03:38:28Z daisyf@stanford.edu $

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
**********************************************************************************/

 -->
  <!-- 2a ATTACHMENTS  -->
  <div class="attachmentEditor">
    <fieldset>
      <legend>
        <h:outputText value="#{authorMessages.attachments}"/>
      </legend>
      <h:panelGroup rendered="#{sectionBean.hasAttachment}">
        <h:dataTable value="#{sectionBean.attachmentList}" var="attach">
          <h:column>
            <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
          </h:column>
          <h:column>
            <f:verbatim>
              <jsp:text><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></jsp:text>
            </f:verbatim>
            <h:outputLink value="#{attach.location}" target="new_window">
              <h:outputText value="#{attach.filename}"/>
            </h:outputLink>
          </h:column>
          <h:column>
            <f:verbatim>
              <jsp:text><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></jsp:text>
            </f:verbatim>
            <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
          </h:column>
        </h:dataTable>
      </h:panelGroup>
      <h:panelGroup rendered="#{!sectionBean.hasAttachment}">
        <h:outputText escape="false" value="#{authorMessages.no_attachments}"/>
      </h:panelGroup>
      <h:panelGroup rendered="#{!sectionBean.hasAttachment}">
        <sakai:button_bar>
          <h:commandButton action="#{sectionBean.addAttachmentsRedirect}" value="#{authorMessages.add_attachments}"/>
        </sakai:button_bar>
      </h:panelGroup>
      <h:panelGroup rendered="#{sectionBean.hasAttachment}">
        <sakai:button_bar>
          <h:commandButton action="#{sectionBean.addAttachmentsRedirect}" value="#{authorMessages.add_remove_attachments}"/>
        </sakai:button_bar>
      </h:panelGroup>
    </fieldset>
  </div>
</jsp:root>
