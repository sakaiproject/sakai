<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="prefs_form">
		
<hr />
<h2>Private message- Sent</h2>
<hr />

        <sakai:button_bar>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>   
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
