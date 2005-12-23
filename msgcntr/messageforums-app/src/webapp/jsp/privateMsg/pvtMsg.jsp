<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="prefs_form">
  		<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="Message Forums" /> /
			<h:outputText value="#{PrivateMessagesTool.msgNavMode}" />
				
		<h2>Private message- <h:outputText value="#{PrivateMessagesTool.msgNavMode}" /> </h2>
		
		<%@include file="msgHeader.jsp"%>
<br><br><br>

	  <h:dataTable styleClass="listHier" id="pvtmsgs" width="100%" value="#{PrivateMessagesTool.decoratedPvtMsgs}" var="rcvdItems" >   
		  <h:column>
		    <f:facet name="header">

		     <h:commandButton alt="SelectAll" image="/sakai-messageforums-tool/images/checkbox.gif" action="#{PrivateMessagesTool.processSelectAllJobs}"/>
		    </f:facet>
		    <h:selectBooleanCheckbox value="#{rcvdItems.isSelected}"/>
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif"/>								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{!empty rcvdItems.msg.hasAttachments}"/>			 
			</h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Subject"/>
		    </f:facet>
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" immediate="true">
            <h:outputText value=" #{rcvdItems.msg.title}"/>
            <f:param value="#{rcvdItems.msg.id}" name="current_msg_detail"/>
          </h:commandLink>
		  </h:column>			
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Authored By"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.msg.createdBy}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Date"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.created}"/>
		  </h:column>
		  <%--
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Label"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.msg.label}"/>
		  </h:column>
		  --%>
		</h:dataTable>
		
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
