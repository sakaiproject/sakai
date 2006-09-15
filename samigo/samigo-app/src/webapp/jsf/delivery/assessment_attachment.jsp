<!-- 2a ATTACHMENTS -->
 <div class="longtext"><h:outputLabel value="#{msg.attachments}" rendered="#{delivery.hasAttachment}"/>
  <br/>
  <h:panelGroup rendered="#{delivery.hasAttachment}">
    <h:dataTable value="#{delivery.attachmentList}" var="attach">
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
        <h:outputText escape="false" value="#{attach.fileSize} kb" rendered="#{!attach.isLink}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>

</div>

