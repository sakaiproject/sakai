<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id: gradeStudentResultAttachment.jsp 11254 2006-06-28 03:38:28Z daisyf@stanford.edu $

***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/

 -->
  <script>
    <jsp:text>&#13;
function addAttachments(event, field){&#13;
event.preventDefault();&#13;
var insertlinkid= field.id.replace("addAttachments", "hiddenlink");&#13;
var newindex = 0;&#13;<![CDATA[
for (i=0; i<document.links.length; i++) {]]>&#13;
  if(document.links[i].id == insertlinkid)&#13;
  {&#13;
    newindex = i;&#13;
    break;&#13;
  }&#13;
}&#13;
&#13;
document.links[newindex].click();&#13;
}&#13;
&#13;
function addRemoveAttachments(event, field){&#13;
event.preventDefault();&#13;
var insertlinkid= field.id.replace("addRemoveAttachments", "hiddenlink");&#13;
var newindex = 0;&#13;<![CDATA[
for (i=0; i<document.links.length; i++) {]]>&#13;
  if(document.links[i].id == insertlinkid)&#13;
  {&#13;
    newindex = i;&#13;
    break;&#13;
  }&#13;
}&#13;
&#13;
document.links[newindex].click();&#13;
}&#13;
&#13;
</jsp:text>
  </script>
  <!-- ASSESSMENT ATTACHMENTS  -->
  <h:panelGrid border="0">
    <f:verbatim>
      <jsp:text><![CDATA[<div class="longtext">]]></jsp:text>
    </f:verbatim>
    <h:outputLabel value="#{assessmentSettingsMessages.attachments}"/>
    <h:panelGroup rendered="#{question.hasItemGradingAttachment}">
      <h:dataTable value="#{question.itemGradingAttachmentList}" var="attach">
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
    <h:panelGroup rendered="#{!question.hasItemGradingAttachment}">
      <h:outputText escape="false" value="#{assessmentSettingsMessages.no_attachments}"/>
    </h:panelGroup>
    <h:commandButton tabindex="-1" value="#{assessmentSettingsMessages.add_attachments}" type="button" id="addAttachments" rendered="#{!question.hasItemGradingAttachment}" style="act" onclick="addAttachments(event, this);" onkeypress="addAttachments(event, this);"/>
    <h:commandButton tabindex="-1" value="#{assessmentSettingsMessages.add_remove_attachments}" type="button" id="addRemoveAttachments" rendered="#{question.hasItemGradingAttachment}" style="act" onclick="addRemoveAttachments(event, this);" onkeypress="addRemoveAttachments(event, this);"/>
    <h:commandLink tabindex="-1" id="hiddenlink" action="#{studentScores.addAttachmentsRedirect}">
      <f:param name="itemGradingId" value="#{question.itemGradingIdForFilePicker}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreAttachmentListener"/>
    </h:commandLink>
  </h:panelGrid>
  <f:verbatim>
    <jsp:text><![CDATA[</div>]]></jsp:text>
  </f:verbatim>
</jsp:root>
