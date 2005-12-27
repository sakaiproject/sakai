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
		<sakai:tool_bar_message value="Delete Confirmation ..." />
		<h:messages styleClass="alertMessage" id="errorMessages" /> 

	  <h:dataTable styleClass="listHier" id="pvtmsgdel" width="100%"  value="#{PrivateMessagesTool.selectedDeleteItems}" var="delItems">   
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Subject"/>
		    </f:facet>
		      <h:outputText value=" #{delItems.msg.title}"/>              
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif"/>								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{!empty delItems.msg.hasAttachments}"/>			 
			</h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Authored By"/>
		    </f:facet>		     		    
		     <h:outputText value="#{delItems.msg.createdBy}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Date"/>
		    </f:facet>
		     <h:outputText value="#{delItems.msg.created}"/>
		  </h:column>
		</h:dataTable>

        <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMultiDelete}" value="Delete Message(s)" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>
        		
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
