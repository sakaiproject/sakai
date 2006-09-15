<!-- JAVASCRIPT -->
<script language="javascript" type="text/JavaScript">

function getMimeIcon(String mimeType){
  return "http://sakai-l.stanford.edu:8080/samigo/images/pdf.gif"
}
</script>

<!-- 2a ATTACHMENTS -->
 <div class="longtext"><h:outputLabel value="#{msg.attachments}" />
  <br/>
  <h:panelGroup rendered="#{itemauthor.hasAttachment}">
    <h:dataTable value="#{itemauthor.attachmentList}" var="attach">
      <h:column>
        <h:graphicImage value="javascript:getMimeIcon(\"#{attach.mimeType}\");" />
      </h:column>
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
  <h:panelGroup rendered="#{!itemauthor.hasAttachment}">
    <h:outputText escape="false" value="#{msg.no_attachments}" />
  </h:panelGroup>

  <h:panelGroup rendered="#{!itemauthor.hasAttachment}">
    <sakai:button_bar>
     <sakai:button_bar_item action="#{itemauthor.addAttachmentsRedirect}"
           value="#{msg.add_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>

  <h:panelGroup rendered="#{itemauthor.hasAttachment}">
    <sakai:button_bar>
     <sakai:button_bar_item action="#{itemauthor.addAttachmentsRedirect}"
           value="#{msg.add_remove_attachments}"/>
    </sakai:button_bar>
  </h:panelGroup>

</div>

