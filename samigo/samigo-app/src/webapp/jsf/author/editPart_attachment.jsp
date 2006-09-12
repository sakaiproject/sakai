<!-- 2a ATTACHMENTS -->
 <div class="longtext"><h:outputLabel value="#{msg.attachments}" />
  <br/>
  <h:panelGroup rendered="#{sectionBean.hasAttachment}">
    <h:dataTable value="#{sectionBean.attachmentList}" var="attach">
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputLink value="#{attach.location}"
           target="new_window">
           <h:outputText escape="false" value="#{attach.filename}" />
        </h:outputLink>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputText escape="false" value="#{attach.fileSize} kb" rendered="#{!attach.isLink}"/>
      </h:column>
      <h:column>
        <h:commandLink title="#{msg.t_remove_attachment}" action="confirmRemovePartAttachment" immediate="true">
          <h:outputText value="   #{msg.remove_attachment}" />
          <f:param name="attachmentId" value="#{attach.attachmentId}"/>
          <f:param name="attachmentLocation" value="#{attach.location}"/>
          <f:param name="attachmentFilename" value="#{attach.filename}"/>
          <f:param name="attachmentType" value="2"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemoveAttachmentListener" />
        </h:commandLink>
      </h:column>
    </h:dataTable>
  </h:panelGroup>
  <h:panelGroup rendered="#{!sectionBean.hasAttachment}">
    <h:outputText escape="false" value="#{msg.no_attachments}" />
  </h:panelGroup>

  <sakai:button_bar>
    <sakai:button_bar_item action="#{sectionBean.addAttachmentsRedirect}"
           value="#{msg.add_attachments}"/>
  </sakai:button_bar>
</div>

