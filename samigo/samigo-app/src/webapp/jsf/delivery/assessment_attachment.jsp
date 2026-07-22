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
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<!-- ASSESSMENT ATTACHMENTS -->
<h:panelGroup rendered="#{delivery.hasAttachment}">
  <p></p>
</h:panelGroup>
<h:outputLabel value="#{deliveryMessages.attachments}" rendered="#{delivery.hasAttachment}"/>

<h:panelGroup rendered="#{delivery.hasAttachment}">
  <t:aliasBean alias="#{itemAttachmentList}" value="#{delivery.attachmentList}">
    <%@ include file="/jsf/shared/itemAttachmentList.jsp" %>
  </t:aliasBean>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.hasAttachment}">
  <br />
</h:panelGroup>
