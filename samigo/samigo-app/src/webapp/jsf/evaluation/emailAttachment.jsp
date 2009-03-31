<!-- $Id: attachment.jsp 11254 2006-06-28 03:38:28Z daisyf@stanford.edu $
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
**********************************************************************************/
--%>
-->
<!-- 2a ATTACHMENTS -->
 <div class="longtext"><h:outputLabel value="#{evaluationMessages.attachments}" />
  <h:panelGroup rendered="#{email.hasAttachment && param.fromEmailLinkClick != 'true'}">
    <h:dataTable value="#{email.attachmentList}" var="attach">
      <h:column>
        <%@ include file="/jsf/shared/mimeicon.jsp" %>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputLink value="#{attach.filename}" target="new_window" rendered="#{attach.isLink}">
          <h:outputText escape="false" value="#{attach.filename}" />
        </h:outputLink>
        <h:outputLink value="#{attach.location}" target="new_window" rendered="#{!attach.isLink}">
          <h:outputText escape="false" value="#{attach.filename}" />
        </h:outputLink>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputText escape="false" value="(#{attach.fileSize}kb)" rendered="#{!attach.isLink}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>
  <h:panelGroup rendered="#{!email.hasAttachment || param.fromEmailLinkClick == 'true'}">
    <h:outputText escape="false" value="#{evaluationMessages.no_attachments_yet}" />
  </h:panelGroup>

  <h:panelGroup rendered="#{!email.hasAttachment || param.fromEmailLinkClick == 'true'}">
     <h:commandButton action="#{email.addAttachmentsRedirect}"
           value="#{evaluationMessages.add_attachments}" immediate="true"/>
  </h:panelGroup>

  <h:panelGroup rendered="#{email.hasAttachment && param.fromEmailLinkClick != 'true'}">
     <h:commandButton action="#{email.addAttachmentsRedirect}"
           value="#{evaluationMessages.add_remove_attachments}" immediate="true"/>
  </h:panelGroup>
</div>

