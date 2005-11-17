<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<SCRIPT LANGUAGE="JavaScript">
<!-- Modified By:  Steve Robison, Jr. (stevejr@ce.net) -->

<!-- This script and many more are available free online at -->
<!-- The JavaScript Source!! http://javascript.internet.com -->

<!-- Begin
var checkflag = "false";
function check(field) {
if (checkflag == "false") {
for (i = 0; i < field.length; i++) {
field[i].checked = true;}
checkflag = "true";
return "Uncheck All"; }
else {
for (i = 0; i < field.length; i++) {
field[i].checked = false; }
checkflag = "false";
return "Check All"; }
}
//  End -->
</script>

<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="prefs_form">

		<h2>Private message- <h:outputText value="#{PrivateMessagesTool.msgNavMode}" /> </h2>
		<%@include file="msgHeader.jsp"%>


	  <h:dataTable value="#{PrivateMessagesTool.displayPvtMsgs}" var="rcvdItems" styleClass="chefFlatListViewTable">   
		  <h:column>
		    <f:facet name="header">

		     <h:commandButton alt="SelectAll" image="/sakai-messageforums-tool/images/checkbox.gif" action="#{PrivateMessagesTool.processSelectAllJobs}"/>
		    </f:facet>
		    <h:selectBooleanCheckbox value="#{rcvdItems.isSelected}"/>
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
