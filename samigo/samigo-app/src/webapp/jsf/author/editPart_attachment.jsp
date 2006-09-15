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
        <h:outputText escape="false" value="(#{attach.fileSize}kb)" rendered="#{!attach.isLink}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>
  <h:panelGroup rendered="#{!sectionBean.hasAttachment}">
    <h:outputText escape="false" value="#{msg.no_attachments}" />
  </h:panelGroup>

  <h:panelGroup rendered="#{!sectionBean.hasAttachment}">
    <sakai:button_bar>
     <sakai:button_bar_item action="#{sectionBean.addAttachmentsRedirect}"
           value="#{msg.add_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>

  <h:panelGroup rendered="#{sectionBean.hasAttachment}">
    <sakai:button_bar>
     <sakai:button_bar_item action="#{sectionBean.addAttachmentsRedirect}"
           value="#{msg.add_remove_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>

</div>

