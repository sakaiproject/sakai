<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>

	<sakai:view title="#{msgs.pvt_delcon}">
<!--jsp/privateMsg/pvtMsgFolderDelete.jsp-->
		<h:form id="pvtMsgFolderDelete">
		       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
					<script type="text/javascript" src="/messageforums-tool/js/sak-10625.js"></script>
					<script type="text/javascript" src="/messageforums-tool/js/messages.js"></script>
			<sakai:tool_bar_message value="#{msgs.pvt_delcon}" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/> 
 
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
        <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFldDelete}" value="#{msgs.pvt_delete}" accesskey="s" styleClass="active"/>
		    <h:commandButton action="#{PrivateMessagesTool.processPvtMsgReturnToFolderView}" value="#{msgs.pvt_cancel}" accesskey="x" />
		  </sakai:button_bar>   
           
		 </h:form>

	</sakai:view>
</f:view>

