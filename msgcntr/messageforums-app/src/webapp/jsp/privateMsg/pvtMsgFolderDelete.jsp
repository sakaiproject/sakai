<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs" />
  <link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
	<sakai:view title="#{msgs.pvt_delcon}">

		<h:form id="pvtMsgFolderDelete">
			<sakai:tool_bar_message value="#{msgs.pvt_delcon}" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 
 
 			<table width="100%">
 			  <tr class="msgHeadings">
 			    <td>
 			      <h:outputText value="#{msgs.pvt_folder_title}" />
 			    </td>
 			    <td>
 			      <h:outputText value="#{msgs.pvt_num_messages}" />
 			    </td>
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
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldDelete}" value="#{msgs.pvt_delete}" accesskey="x" />
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldAddCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
		  </sakai:button_bar>   
           
		 </h:form>

	</sakai:view>
</f:view>

