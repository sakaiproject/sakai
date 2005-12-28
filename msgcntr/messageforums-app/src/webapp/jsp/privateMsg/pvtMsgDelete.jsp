<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
	<sakai:view_container title="Delete Confirmation ...">
	<sakai:view_content>
		<h:form id="pvtMsgDelete">
		<sakai:tool_bar_message value="#{msgs.pvt_delcon}" />
		<h:messages styleClass="alertMessage" id="errorMessages" /> 

    <sakai:group_box>
	  <h:dataTable styleClass="listHier" id="pvtmsgdel" width="100%"  value="#{PrivateMessagesTool.selectedDeleteItems}" var="delItems">   
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_subject}"/>
		    </f:facet>
		      <h:outputText value=" #{delItems.msg.title}"/>              
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif"/>								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{delItems.msg.hasAttachments}"/>			 
			</h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_authby}"/>
		    </f:facet>		     		    
		     <h:outputText value="#{delItems.msg.createdBy}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="#{msgs.pvt_date}"/>
		    </f:facet>
		     <h:outputText value="#{delItems.msg.created}"/>
		  </h:column>
		</h:dataTable>
		</sakai:group_box>

    <sakai:button_bar>
      <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMultiDelete}" value="#{msgs.pvt_delmsgs}" />
      <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="#{msgs.pvt_cancel}" />
    </sakai:button_bar>
    
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
