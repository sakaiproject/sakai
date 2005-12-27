<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />


<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="pvtMsgFolderSettings">
		

<h2>Private message- Folder Settings</h2>
  <h4><h:outputText value="Folder Title"/></h4>
 

 <h:outputText value="Folder Title:"/>
 <h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}" />
 
 <br><br>
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
 	<h:panelGroup rendered="#{PrivateMessagesTool.ismutable}">
	  <h:outputText style="alertMessage" value="! You cann't Revise or Delete this folder." />
	</h:panelGroup>   
				
	<sakai:button_bar>
  	<sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingRevise}" value="Revise" />
    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}" value="Add" />
    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingDelete}" value="Delete" />
    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFolderSettingCancel}" value="Cancel" />
  </sakai:button_bar>   
          
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>

