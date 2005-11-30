<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Delete Confirmation ...">
	<sakai:view_content>
		<h:form id="pvtMsgDelete">

		<h2>Delete Confirmation ...</h2>
		
		<sakai:group_box>
			<h:panelGroup >
				<h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
				value="! Are you sure you want to permanently delete the following message(s)?" />
			</h:panelGroup>  
		</sakai:group_box>
				

	  <h:dataTable styleClass="listHier" id="pvtmsgdel" width="100%"  value="#{PrivateMessagesTool.selectedDeleteItems}" var="delItems">   
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Subject"/>
		    </f:facet>
		      <h:outputText value=" #{delItems.message.title}"/>              
		  </h:column>
		  <h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif"/>								
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{!empty delItems.message.attachments}"/>			 
			</h:column>
			
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Authored By"/>
		    </f:facet>		     		    
		     <h:outputText value="#{delItems.message.createdBy}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Date"/>
		    </f:facet>
		     <h:outputText value="#{delItems.message.created}"/>
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
