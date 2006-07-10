<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />


<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_foldersettings}">

		<h:form id="pvtMsgFolderSettings">
		  <sakai:tool_bar_message value="#{msgs.pvt_msgs_label} #{msgs.pvt_foldersettings}" /> 
	    <h:messages styleClass="alertMessage" id="errorMessages" /> 

      <div class="msgHeadings">
        <h:outputText value="#{msgs.pvt_folder_title}"/>
      </div>

		 <h:outputText value="#{msgs.pvt_folder_title} #{msgs.pvt_colon}"/>
		 <h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}" />
		 
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
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderInFolderAdd}" value="#{msgs.pvt_add}" accesskey="a" />
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingDelete}" value="#{msgs.pvt_delete}" accesskey="x" />
		    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
		  </sakai:button_bar>   
          
		 </h:form>

	</sakai:view>
</f:view>

