<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_foldersettings}">
	  <h:form id="pvtMsgFolderRevise">
		
    <sakai:tool_bar_message value="#{msgs.pvt_rename_folder_label}" />
    <h:messages styleClass="alertMessage" id="errorMessages" /> 
 
    <div class="instruction">
	    <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline"/>
		</div>

	 	<div>
	 		<h:outputText value="#{msgs.pvt_folder_title} #{msgs.pvt_colon}"/>
	 		<h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}" />
	 	</div>
	 	 
	 	<div class="msgHeadings">
	    <h:outputText value="#{msgs.pvt_folder_title}"/>
	  </div>
	 	
	 	<h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" />
	 	<h:outputLabel for="revised_title"><h:outputText value="#{msgs.pvt_folder_title} #{msgs.pvt_colon}"/></h:outputLabel>
	 	<h:inputText value="#{PrivateMessagesTool.selectedNewTopicTitle}" id="revised_title" />
	
		<sakai:button_bar>
	  	  <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldRevise}" value="#{msgs.pvt_saveset}" accesskey="s" />
	  	  <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingDelete}" value="#{msgs.pvt_delete}" accesskey="x" />
	    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldAddCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
	  </sakai:button_bar>   
           
	</h:form>

	</sakai:view>
</f:view>

