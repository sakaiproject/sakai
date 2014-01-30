<!--
* $Id: assessment_attachment.jsp 6874 2006-03-22 17:01:47Z hquinn@stanford.edu $
<%--
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
--%>
-->
<!-- 2a ATTACHMENTS -->

 <h:panelGroup rendered="#{delivery.hasAttachment}">
 	<f:verbatim><p></p></f:verbatim>
 </h:panelGroup>
 <h:outputLabel value="#{deliveryMessages.attachments}" rendered="#{delivery.hasAttachment}"/>
  <f:verbatim><br/></f:verbatim>

  <h:panelGroup rendered="#{delivery.hasAttachment}">
    <h:dataTable value="#{delivery.attachmentList}" var="attach" border="0">
      <h:column>
        
      <h:outputText escape="false" value="
	    <embed src=\"#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&mimeType=#{attach.mimeType}&filename=#{attach.filename}\" volume=\"50\" height=\"350\" width=\"400\" autostart=\"false\"/>" rendered="#{attach.isInlineVideo && !delivery.isFromPrint}"/>
      <h:outputText value="#{attach.filename}" rendered="#{attach.isInlineVideo && delivery.isFromPrint}"/>
      
	  <h:outputText escape="false" value="
	    <embed src=\"#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&mimeType=#{attach.mimeType}&filename=#{attach.filename}\" height=\"350\" width=\"400\"/>" rendered="#{attach.isInlineFlash && !delivery.isFromPrint}"/>
      <h:outputText value="#{attach.filename}" rendered="#{attach.isInlineFlash && delivery.isFromPrint}"/>  
      
	  <h:outputText escape="false" value="
	    <img src=\"#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&mimeType=#{attach.mimeType}&filename=#{attach.filename}\" />" rendered="#{attach.isInlineImage}"/>

 	  <h:panelGrid rendered="#{!attach.isMedia && !delivery.isFromPrint}" border="0" columns="2">
      <h:column>
        <%@ include file="/jsf/shared/mimeicon.jsp" %>
        <f:verbatim>&nbsp;&nbsp;</f:verbatim>
        <h:outputLink value="#{attach.location}" target="new_window">
          <h:outputText value="#{attach.filename}" />
        </h:outputLink>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink}"/>
      </h:column>
      </h:panelGrid> 
      <h:outputText value="#{attach.filename}" rendered="#{!attach.isMedia && delivery.isFromPrint}"/>  
  
    </h:column>
    </h:dataTable>
  </h:panelGroup>
  
   <h:panelGroup rendered="#{delivery.hasAttachment}">
 	<f:verbatim><br /></f:verbatim>
   </h:panelGroup>



