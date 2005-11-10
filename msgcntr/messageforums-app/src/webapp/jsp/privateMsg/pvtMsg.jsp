<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<style>
.bord-r-t {
	border-top: 1px solid #C8C8C8;
	border-right: 1px solid #C8C8C8;
	border-bottom: 1px #C8C8C8;
	border-left: 1px #C8C8C8;
}
.greyback1 {
	background-color: #e6e6e6;
	border-top: 1px #C8C8C8;
	border-right: 1px #C8C8C8;
	border-bottom: 1px solid #C8C8C8;
	border-left: 1px solid #C8C8C8;
	padding: 2px 8px 2px 2px;
}
.rosterGroupByRoleColumns_1{
  width: 8%;
  align:left;
  
 }

.rosterGroupByRoleColumns_2{
  width: 5%;
  text-align:align:left;
  valign-text:vlaign:middle; 

}
</style>
<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="prefs_form">

		<h2>Private message- <h:outputText value="#{PrivateMessagesTool.msgNavMode}" /> </h2>
		<%@include file="msgHeader.jsp"%>


	  <h:dataTable value="#{PrivateMessagesTool.dispPvtMsgs}" var="rcvdItems">   
		  <h:column>
		    <f:facet name="header">
		     <h:outputText value="Check All"/>
		    </f:facet>
		    <h:selectBooleanCheckbox > </h:selectBooleanCheckbox> 
  
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Subject"/>
		    </f:facet>
		      <h:commandLink action="#{PrivateMessagesTool.processPvtMsgDetail}" immediate="true">
            <h:outputText value=" #{rcvdItems.title}"/>
            <f:param value="#{rcvdItems.uuid}" name="current_msg_detail"/>
          </h:commandLink>
              
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Authored By"/>
		    </f:facet>		     		    
		     <h:outputText value="#{rcvdItems.createdBy}"/>
		  </h:column>
		  <h:column>
		    <f:facet name="header">
		       <h:outputText value="Date"/>
		    </f:facet>
		     <h:outputText value="#{rcvdItems.created}"/>
		  </h:column>
		</h:dataTable>
		
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
