<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="pvtMsgDetail">
  			<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
  			<h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.cdfm_message_pvtarea}" /> /
				<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{PrivateMessagesTool.msgNavMode}" /> /
				<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />
				<sakai:tool_bar_message value="#{msgs.pvt_detmsgreply}" /> 
				<h:messages styleClass="alertMessage" id="errorMessages" /> 
       
      	<br>
      	<sakai:group_box>
        <table width="100%" align="left" class="tablebgcolor" >
          <tr>
            <td align="left">
            	<h:outputText style="font-weight:bold"  value="#{msgs.pvt_to}" rendered="#{PrivateMessagesTool.msgNavMode == 'Sent'}"/>
            </td>
            <td align="left">
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.recipientsAsText}" rendered="#{PrivateMessagesTool.msgNavMode == 'Sent'}"/>
            </td>  
            <td nowrap align="right">         			
								<h:outputText   value="Previous Message"  rendered="#{!PrivateMessagesTool.detailMsg.hasPre}" />
								&nbsp;&nbsp;
								<h:commandLink action="#{PrivateMessagesTool.processDisplayPreviousMsg}" value="Previous Message"  rendered="#{PrivateMessagesTool.detailMsg.hasPre}" >
								</h:commandLink>
								&nbsp;&nbsp;
								<h:outputText   value="    Next Message      " rendered="#{!PrivateMessagesTool.detailMsg.hasNext}" />
								<h:commandLink action="#{PrivateMessagesTool.processDisplayNextMsg}" value="    Next Message   " rendered="#{PrivateMessagesTool.detailMsg.hasNext}" >
								</h:commandLink>
			      </td>
          </tr>
          <tr>
            <td align="left">
              <h:outputText style="font-weight:bold"  value="#{msgs.pvt_subject}"/>
            </td>
            <td align="left">
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />  
            </td>  
            <td align="left"></td>
          </tr>          
          <tr>
            <td align="left">
              <h:outputText style="font-weight:bold"  value="#{msgs.pvt_authby}"/>
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
              <h:outputText style="font-weight:bold"  value="#{msgs.pvt_att}"/>
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
          
          <tr>
            <td align="left">
              <h:outputText style="font-weight:bold"  value="#{msgs.pvt_label} "/>
            </td>
            <td align="left">
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.label}" />  
            </td>
            <td></td>
          </tr> 
                                        
        </table>    
        </sakai:group_box>
        
        <sakai:group_box>
        	<mf:htmlShowArea value="#{PrivateMessagesTool.detailMsg.msg.body}"/>
        </sakai:group_box> 
               
        <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReply}" value="#{msgs.pvt_repmsg}" />
          <%--
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMove}" value="#{msgs.pvt_move}" />
          --%>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirm}" value="#{msgs.pvt_delete}" />
        </sakai:button_bar>        
        <sakai:button_bar rendered="#{PrivateMessagesTool.deleteConfirm}" >  
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirmYes}" value="#{msgs.pvt_delete}" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="#{msgs.pvt_cancel}" />
        </sakai:button_bar>


      </h:form>
    </sakai:view_content>
  </sakai:view_container>
</f:view> 
                    