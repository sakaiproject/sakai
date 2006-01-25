<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Private Messages">
	<sakai:view_content>
		<h:form id="pvtMsgFolderDelete">
			<sakai:tool_bar_message value="Private Messages- Delete Confirmation" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 
 
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="Folder Title"/>
              </td>
              <td align="left" >
                <h:outputText value="Number of Messages"/>
              </td>                      
            </tr> 
            <tr>
            </tr>
            <tr>
              <td align="left" width="20%">
                <h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}"/>
              </td>
              <td align="left" >
              	<h:outputText value="("/>
              	<h:outputText value=")"/>
              </td>                      
            </tr>                                              
          </table>
        </sakai:group_box>
     

	<sakai:button_bar>
  	<sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldDelete}" value="Delete" />
    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldAddCancel}" value="Cancel" />
  </sakai:button_bar>   
           
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>

