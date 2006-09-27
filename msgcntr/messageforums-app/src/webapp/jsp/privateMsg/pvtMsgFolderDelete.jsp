<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs" />

	<sakai:view title="#{msgs.pvt_delcon}">
<!--jsp/privateMsg/pvtMsgFolderDelete.jsp-->
		<h:form id="pvtMsgFolderDelete">
			<sakai:tool_bar_message value="#{msgs.pvt_delcon}" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 
 
 			<table width="100%" class="listHier lines nolines" cellpadding="0" cellspacing="0">
 			  <tr>
 			    <th>
 			      <h:outputText value="#{msgs.pvt_folder_title}" />
 			    </th>
 			    <th>
 			      <h:outputText value="#{msgs.pvt_num_messages}" />
 			    </th>
 			  </tr>
 			  <tr>
 			    <td>
 			      <h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}" />
 			    </td>
 			    <td>
 			      <h:outputText value=" #{PrivateMessagesTool.totalMsgInFolder} #{msgs.pvt_lowercase_msgs} " />
 			    </td>
 			  </tr>
 			</table>
          
			<sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldDelete}" value="#{msgs.pvt_delete}" accesskey="s" styleClass="active"/>
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldAddCancel}" value="#{msgs.pvt_cancel}" accesskey="x" />
		  </sakai:button_bar>   
           
		 </h:form>

	</sakai:view>
</f:view>

