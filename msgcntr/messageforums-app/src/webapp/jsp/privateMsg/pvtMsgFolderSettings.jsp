<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>


<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_foldersettings}">

<!--jsp/privateMsg/pvtMsgFolderSettings.jsp-->
		<h:form id="pvtMsgFolderSettings">
		       		<script>includeLatestJQuery("msgcntr");</script>
			<script src="/messageforums-tool/js/sak-10625.js"></script>
			<script src="/messageforums-tool/js/messages.js"></script>
            <script>
                $(document).ready(function() {
                    var menuLink = $('#messagesMainMenuLink');
                    var menuLinkSpan = menuLink.closest('span');
                    menuLinkSpan.addClass('current');
                    menuLinkSpan.html(menuLink.text());
                });
            </script>
            <%@ include file="/jsp/privateMsg/pvtMenu.jsp" %>
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
		  	  <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFolderSettingRevise}" value="#{msgs.pvt_rename_folder_button}" accesskey="r" />
		    <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFolderInFolderAdd}" value="#{msgs.pvt_add}"/>
		    <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFolderSettingDelete}" value="#{msgs.pvt_delete}"/>
		    <h:commandButton action="#{PrivateMessagesTool.processPvtMsgReturnToMainOrHp}" value="#{msgs.pvt_cancel}" accesskey="x" />
		  </sakai:button_bar>   
          
		 </h:form>

	</sakai:view>
</f:view>

