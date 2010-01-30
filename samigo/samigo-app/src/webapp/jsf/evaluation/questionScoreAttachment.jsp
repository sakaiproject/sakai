<!-- $Id: questionScoreAttachment.jsp 11254 2006-06-28 03:38:28Z daisyf@stanford.edu $
<%--
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
--%>
-->
<!-- ASSESSMENT ATTACHMENTS -->
 <f:verbatim><div class="longtext"></f:verbatim><h:outputLabel value="#{assessmentSettingsMessages.attachments}" />
  <f:verbatim><br/></f:verbatim>
  <h:panelGroup rendered="#{description.hasItemGradingAttachment}">
    <h:dataTable value="#{description.itemGradingAttachmentList}" var="attach">
      <h:column>
        <%@ include file="/jsf/shared/mimeicon.jsp" %>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputLink value="#{attach.location}" target="new_window">
          <h:outputText escape="false" value="#{attach.filename}" />
        </h:outputLink>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>
  <h:panelGroup rendered="#{!description.hasItemGradingAttachment}">
    <h:outputText escape="false" value="#{assessmentSettingsMessages.no_attachments}" />
  </h:panelGroup>

  <h:panelGroup rendered="#{!description.hasItemGradingAttachment}">
    <sakai:button_bar>
      <sakai:button_bar_item action="#{description.addAttachmentsRedirect}"
             value="#{assessmentSettingsMessages.add_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>

  <h:panelGroup rendered="#{description.hasItemGradingAttachment}">
    <sakai:button_bar>
      <sakai:button_bar_item action="#{description.addAttachmentsRedirect}"
             value="#{assessmentSettingsMessages.add_remove_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>
<f:verbatim></div></f:verbatim>

