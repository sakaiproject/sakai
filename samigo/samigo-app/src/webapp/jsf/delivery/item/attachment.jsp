<!-- ATTACHMENTS -->
  <h:dataTable value="#{question.itemData.itemAttachmentList}" var="attach">
    <h:column>
      <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
<%--
      <h:outputText escape="false" value="#{attach} : " />
      <h:outputText escape="false" value="#{attach.attachmentId} : " />
      <h:outputText escape="false" value="#{attach.resourceId} : " />
--%>
      <h:outputLink value="#{attach.location}" target="new_window">
        <h:outputText escape="false" value="#{attach.filename}" />
      </h:outputLink>
    </h:column>
  </h:dataTable>
  
