<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Private Messages">
	<sakai:view_content>
		<h:form id="pvtMsgFolderAdd">
		

	<h2>Private Messages- Create Folder</h2>
  <sakai:group_box>
  	<h:outputText value="Required items marked with "/>
    <h:outputText value="*" style="color: red"/>
  </sakai:group_box> 
 	<sakai:group_box>
 		<h:outputText value="Folder :"/>
 		<h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}" />
 	</sakai:group_box> 
 	<sakai:group_box>
 	<h4><h:outputText value="Folder Title"/></h4>
 	<h:outputText value="*" style="color: red"/><h:outputText value="Folder Title:"/>
 	<h:inputText value="#{PrivateMessagesTool.addFolder}" />
 	</sakai:group_box> 

	<sakai:button_bar>
  	<sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldInFldCreate}" value="Add" />
    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldAddCancel}" value="Cancel" />
  </sakai:button_bar>   
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>

