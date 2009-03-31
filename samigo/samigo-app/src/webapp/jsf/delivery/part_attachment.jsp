<!--
* $Id: part_attachment.jsp 6874 2006-03-22 17:01:47Z hquinn@stanford.edu $
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
 <div class="tier1"><h:outputLabel value="#{deliveryMessages.attachments}" rendered="#{part.hasAttachment}"/>
  <br/>
  <h:panelGroup rendered="#{part.hasAttachment}">
    <h:dataTable value="#{part.attachmentList}" var="attach">
      <h:column rendered="#{!attach.isMedia}">
        <%@ include file="/jsf/shared/mimeicon.jsp" %>
      </h:column>
	  <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputText escape="false" value="
	      <embed src=\"#{delivery.protocol}/samigo/servlet/ShowAttachmentMedia?actionMode=preview&resourceId=#{attach.encodedResourceId}&mimeType=#{attach.mimeType}&filename=#{attach.filename}\" volume=\"50\" height=\"350\" width=\"400\" autostart=\"false\"/>" rendered="#{attach.isInlineVideo}"/>
        <h:outputText escape="false" value="
	      <embed src=\"#{delivery.protocol}/samigo/servlet/ShowAttachmentMedia?actionMode=preview&resourceId=#{attach.encodedResourceId}&mimeType=#{attach.mimeType}&filename=#{attach.filename}\" height=\"350\" width=\"400\"/>" rendered="#{attach.isInlineFlash}"/>
	    <h:outputText escape="false" value="
	      <img src=\"#{delivery.protocol}/samigo/servlet/ShowAttachmentMedia?actionMode=preview&resourceId=#{attach.encodedResourceId}&mimeType=#{attach.mimeType}&filename=#{attach.filename}\" />" rendered="#{attach.isInlineImage}"/>
        <h:outputLink value="#{attach.location}" target="new_window" rendered="#{!attach.isMedia}">
          <h:outputText escape="false" value="#{attach.filename}" />
        </h:outputLink>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputText escape="false" value="#{attach.fileSize} kb" rendered="#{!attach.isLink && !attach.isMedia}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>

</div>

