<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form >
      	<sakai:tool_bar_message value="Private Message Details" /> 
      	
        <sakai:group_box>
          <table width="100%" align="center" style="background-color:#DDDFE4;">
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Subject "/>
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.title}" />  
              </td>                           
            </tr>
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Authored By "/>
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.author}" />  
              	<h:outputText value="-" />  
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.created}" />  
              </td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Attachments "/>
              </td>
              <td align="left">
              </td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Label "/>
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.label}" />  
              </td>
            </tr>                                    
          </table>
        </sakai:group_box>

				<h:panelGroup rendered="#{PrivateMessagesTool.deleteConfirm}">
					<h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
					value="! Are you sure you want to delete this message? If yes, click Delete to delete the message." />
				</h:panelGroup>        
        
        <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReply}" value="Reply to Message" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMove}" value="Move" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirm}" value="Delete" />
        </sakai:button_bar>
        
        <sakai:button_bar rendered="#{PrivateMessagesTool.deleteConfirm}" >  
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirmYes}" value="Delete" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>

        <sakai:group_box>
          <sakai:panel_edit>
            <sakai:doc_section>            
              <h:inputTextarea value="#{PrivateMessagesTool.detailMsg.body}" style="width: 100%; align:left; height: 10%;" />
            </sakai:doc_section>    
          </sakai:panel_edit>
        </sakai:group_box>
                        

      </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

