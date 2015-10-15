<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>


<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_foldersettings}">

<!--jsp/privateMsg/pvtMsgFolderSettings.jsp-->
		<h:form id="pvtMsgFolderSettings">
		       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
		  <sakai:tool_bar_message value="#{msgs.pvt_msgs_label} #{msgs.pvt_foldersettings}" /> 
	    <h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" /> 
		<table class="itemSummary">
			<tr>
				<th>
					 <h:outputText value="#{msgs.pvt_folder_title} #{msgs.pvt_colon}"/>
				</th>
				<td>
					<h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}" />
				</td>
			</tr>		
		</table>
		 
		 <%--
		 <p>
		    <h:commandButton rendered="#{PrivateMessagesTool.ismutable}" action="#{PrivateMessagesTool.processPvtMsgFolderSettingRevise}" value="Revise" />
		    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		    <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}" value="Add" />
		    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		    <h:commandButton rendered="#{PrivateMessagesTool.ismutable}" action="#{PrivateMessagesTool.processPvtMsgFolderSettingDelete}" value="Delete" />
		    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		    <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFolderSettingCancel}" value="Cancel" />
		 </p>
		 --%>    
						
			<sakai:button_bar>
		  	  <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingRevise}" value="#{msgs.pvt_rename_folder_button}" accesskey="r" />
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderInFolderAdd}" value="#{msgs.pvt_add}"/>
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingDelete}" value="#{msgs.pvt_delete}"/>
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReturnToMainOrHp}" value="#{msgs.pvt_cancel}" accesskey="x" />
		  </sakai:button_bar>   
          
		 </h:form>

	</sakai:view>
</f:view>

