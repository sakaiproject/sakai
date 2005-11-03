<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="prefs_form">
		

		<h2>Private message- Received</h2>
		
		<hr />
		<h:outputText value="Mark Checked as Read "/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<h:outputText value="  Printer Friendly Format"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<h:commandLink value="Display Options" action="#{PrivateMessagesTool.processPvtMsgDispOtions}"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<h:commandLink value="Folder Settings" action="#{PrivateMessagesTool.processPvtMsgFldrSettings}"/>  
		
		<sakai:button_bar>
      <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCompose}" value="Compose Message" />
    </sakai:button_bar>  


	  <h:dataTable columnClasses="columns_1,columns_2,columns_3,columns_4,columns_5" 
		   styleClass="listHier" value="#{PrivateMessagesTool.receivedItems}" var="rcvdItems">   
		  <h:column>
		    <f:facet name="header">
		    </f:facet>
		    <h:selectBooleanCheckbox > </h:selectBooleanCheckbox> 
  
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		      Subject
		    </f:facet>
		     <h:outputText value="Re:Homework Question"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		      Authored By
		    </f:facet>
		     Pamela Yu
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		      Date
		    </f:facet>
		     Sept 1, 2005
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		      Label
		    </f:facet>
		     Normal
		  </h:column>
		</h:dataTable>

           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>

