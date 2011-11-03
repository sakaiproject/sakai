<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="msgForum">
      	<%@ include file="privateMsg/pvtArea.jsp" %>
        <%@ include file="discussionForum/dfArea.jsp" %>
        <sakai:group_box>
          <h:dataTable 
              id="messages-id" 
              columnClasses="list-column-center, list-column-right, list-column-center, list-column-right" 
              headerClass="list-header" rowClasses="list-row" styleClass="list-background" 
              value="#{MessageForumsTool.debugMessages}" 
              var="" 
              rendered="#{!empty MessageForumsTool.debugMessages}"> 
            <h:column rendered="#{!empty MessageForumsTool.debugMessages}">
              <f:facet name="header">
                <h:outputText value="Title"/>
              </f:facet>
            </h:column>
            <h:column rendered="#{!empty MessageForumsTool.debugMessages}">
              <f:facet name="header">
                <h:outputText value="Size" />
              </f:facet>
              <h:outputText value="#{attachment.attachmentSize}"/>
            </h:column>
            <h:column rendered="#{!empty MessageForumsTool.debugMessages}">
              <f:facet name="header">
                <h:outputText value="Type" />
              </f:facet>
              <h:outputText value="#{attachment.attachmentType}"/>
            </h:column>
            <h:column rendered="#{!empty MessageForumsTool.debugMessages}">
              <f:facet name="header">
                <h:outputText value="Remove" />
              </f:facet>
              <h:commandLink action="#{MessageForumsTool.processCDFMDeleteAttach}" onfocus="document.forms[0].onsubmit();">
                <h:outputText value=" #{msgs.cdfm_remove}"/>
                <f:param value="#{attachment.attachmentId}" name="current_attachment"/>
              </h:commandLink>
            </h:column>
          </h:dataTable>
        </sakai:group_box>	        
        
      </h:form>
    </sakai:view_content>
  </sakai:view_container>
</f:view> 
