<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="pvtMsgDetail">
  			<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="Message Forums" /> / 
				<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{PrivateMessagesTool.msgNavMode}" /> /
				<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />
				<sakai:instruction_message value="Reply to Private Message" /> 
      	<br>
        <table width="100%" align="left" class="tablebgcolor" >
          <tr>
            <td align="left">
              <h:outputText style="font-weight:bold"  value="Subject "/>
            </td>
            <td align="left">
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />  
            </td>  
            <td align="left">
            <%--
  						<h:commandLink action="#{PrivateMessagesTool.processDisplayPreviousMsg}" value="Previous Message   "  rendered="#{PrivateMessagesTool.detailMsg.hasPreviousMsg}" >
	      				<f:param value="#{PrivateMessagesTool.detailMsg.previousMsgId}" name="previousMsgId"/>
							</h:commandLink>
							<h:commandLink action="#{PrivateMessagesTool.processDisplayNextMsg}" value="Next Message   " rendered="#{PrivateMessagesTool.detailMsg.hasNextMsg}" >
								<f:param value="#{PrivateMessagesTool.detailMsg.nextMsgId}" name="nextTopicId"/>
							</h:commandLink>
							<h:outputText   value="Previous Message   "  rendered="#{!PrivateMessagesTool.detailMsg.hasPreviousMsg}" />
							<h:outputText   value="Next Message   " rendered="#{!PrivateMessagesTool.detailMsg.hasNextMsg}" />		
						--%>
			      </td>
          </tr>
          <tr>
            <td align="left">
              <h:outputText style="font-weight:bold"  value="Authored By "/>
            </td>
            <td align="left">
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.createdBy}" />  
            	<h:outputText value=" (" />  
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.created}" />  
            	<h:outputText value=" )" /> 
            </td>
            <td></td>
          </tr>
          <tr>
            <td align="left">
              <h:outputText style="font-weight:bold"  value="Attachments "/>
            </td>
            <td align="left">
              <%-- Attachments --%>
              <h:dataTable value="#{PrivateMessagesTool.detailMsg.msg.attachments}" var="eachAttach" >
					  		<h:column rendered="#{!empty PrivateMessagesTool.detailMsg.msg.attachments}">
								  <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
								  <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
								  <h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
								  <h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								  <h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
								  <h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
								  
								  <h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
								  	<h:outputText value="#{eachAttach.attachmentName}"/>
									</h:outputLink>
							
								</h:column>
							</h:dataTable>   
              <%-- Attachments --%>
            </td>
            <td></td>
          </tr>
          <%--
          <tr>
            <td align="left">
              <h:outputText style="font-weight:bold"  value="Label "/>
            </td>
            <td align="left">
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.message.label}" />  
            </td>
            <td></td>
          </tr> 
          --%>                                   
        </table>    
        
        <br/><br/>
        <sakai:group_box>
          <sakai:panel_edit>
            <sakai:doc_section>  
            </sakai:doc_section>    
          </sakai:panel_edit>
        </sakai:group_box>

				<h:panelGroup rendered="#{PrivateMessagesTool.deleteConfirm}">
					<h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
					  value="! Are you sure you want to delete this message? If yes, click Delete to delete the message." />
				</h:panelGroup>        
        
        <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReply}" value="Reply to Message" />
          <%--
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMove}" value="Move" />
          --%>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirm}" value="Delete" />
        </sakai:button_bar>
        
        <sakai:button_bar rendered="#{PrivateMessagesTool.deleteConfirm}" >  
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirmYes}" value="Delete" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>

        <sakai:group_box>
          <sakai:panel_edit>
            <sakai:doc_section>            
              <h:inputTextarea value="#{PrivateMessagesTool.detailMsg.msg.body}" cols="100" rows="5" />
            </sakai:doc_section>    
          </sakai:panel_edit>
        </sakai:group_box>

      </h:form>
    </sakai:view_content>
  </sakai:view_container>
</f:view> 
                    