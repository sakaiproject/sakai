<!-- 2a ATTACHMENTS -->
 <div class="longtext"><h:outputLabel value="#{msg.attachments}" rendered="#{partBean.hasAttachment}"/>
  <br/>
  <h:panelGroup rendered="#{partBean.hasAttachment}">
    <h:dataTable value="#{partBean.attachmentList}" var="attach">
      <h:column>
        <%@ include file="/jsf/shared/mimeicon.jsp" %>
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
        <h:outputText escape="false" value="#{attach.fileSize} kb" rendered="#{!attach.isLink}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>

</div>

