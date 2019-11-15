<%--
***********************************************************************************
*
* Copyright (c) 2019 Apereo Foundation

* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*             http://opensource.org/licenses/ecl2

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
--%>
<!-- 2a ATTACHMENTS -->
<h:panelGroup layout="block" id="attachments">
  <h:outputLabel value="#{authorMessages.attachments}" />
  <h:panelGroup id="attachmentTable" layout="block" rendered="#{itemauthor.hasAttachment}">
    <h:dataTable styleClass="table table-bordered table-striped attachment-table" value="#{itemauthor.attachmentList}" var="attach">
      <h:column>
        <%@ include file="/jsf/shared/mimeicon.jsp" %>
        <h:outputText value=" " />
        <h:outputLink value="#{attach.location}" target="new_window">
           <h:outputText value="#{attach.filename}" />
        </h:outputLink>
      </h:column>
      <h:column>
        <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>
  <h:panelGroup layout="block" rendered="#{!itemauthor.hasAttachment}">
    <h:outputText escape="false" value="#{authorMessages.no_attachments}" />
  </h:panelGroup>

  <h:panelGroup layout="block" rendered="#{!itemauthor.hasAttachment}">
    <sakai:button_bar>
     <h:commandButton action="#{itemauthor.addAttachmentsRedirect}" value="#{authorMessages.add_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>

  <h:panelGroup layout="block" rendered="#{itemauthor.hasAttachment}">
    <sakai:button_bar>
     <h:commandButton action="#{itemauthor.addAttachmentsRedirect}" value="#{authorMessages.add_remove_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>
</h:panelGroup>
