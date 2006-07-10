<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view title="#{msgs.pvt_detmsgreply}">
    <h:form id="pvtMsgDetail">

  		  <div class="breadCrumb">
  			  <h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"/> /
  			  <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.cdfm_message_pvtarea}" title=" #{msgs.cdfm_message_pvtarea}"/> /
				<h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{PrivateMessagesTool.msgNavMode}" title=" #{PrivateMessagesTool.msgNavMode}"/> /
				<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />
			</div>
			
			<sakai:tool_bar_message value="#{msgs.pvt_detmsgreply}" /> 
			<h:messages styleClass="alertMessage" id="errorMessages" /> 

      	<sakai:panel_titled title="">
        <table class="msgDetails">
          <tr>
            <td class="msgDetailsCol">
            	 <h:outputText value="#{msgs.pvt_to}" rendered="#{PrivateMessagesTool.msgNavMode == 'Sent'}"/>
            </td>
            <td>
            	 <h:outputText value="#{PrivateMessagesTool.detailMsg.msg.recipientsAsText}" rendered="#{PrivateMessagesTool.msgNavMode == 'Sent'}"/>
            </td>  
            <td class="msgNav">         			
								<h:outputText value="#{msgs.pvt_prev_msg}"  rendered="#{!PrivateMessagesTool.detailMsg.hasPre}" />
								<h:commandLink action="#{PrivateMessagesTool.processDisplayPreviousMsg}" value="#{msgs.pvt_prev_msg}"  
								               rendered="#{PrivateMessagesTool.detailMsg.hasPre}" title=" #{msgs.pvt_prev_msg}">
								</h:commandLink>
								<h:outputText value=" " /><h:outputText value=" " />
								<h:outputText   value="#{msgs.pvt_next_msg}" rendered="#{!PrivateMessagesTool.detailMsg.hasNext}" />
								<h:commandLink action="#{PrivateMessagesTool.processDisplayNextMsg}" value="#{msgs.pvt_next_msg}" 
								               rendered="#{PrivateMessagesTool.detailMsg.hasNext}" title=" #{msgs.pvt_next_msg}">
								</h:commandLink>
			      </td>
          </tr>
          <tr>
            <td class="msgDetailsCol">
              <h:outputText value="#{msgs.pvt_subject}"/>
            </td>
            <td>
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />  
            </td>  
            <td><h:outputText value=" " /></td>
          </tr>          
          <tr>
            <td class="msgDetailsCol">
              <h:outputText value="#{msgs.pvt_authby}"/>
            </td>
            <td>
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.author}" />  
            	<h:outputText value="#{msgs.pvt_openb}" />  
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.created}" >
                	<f:convertDateTime pattern="#{msgs.date_format}" />  
                </h:outputText>
            	<h:outputText value=" #{msgs.pvt_closeb}" /> 
            </td>
            <td><h:outputText value=" " /></td>
          </tr>
          <tr>
            <td  class="msgDetailsCol">
              <h:outputText value="#{msgs.pvt_att}"/>
            </td>
            <td>
              <%-- Attachments --%>
              <h:dataTable value="#{PrivateMessagesTool.detailMsg.msg.attachments}" var="eachAttach" >
					  		<h:column rendered="#{!empty PrivateMessagesTool.detailMsg.msg.attachments}">
								  <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}" alt="" />
								  <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}" alt="" />
								  <h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}" alt="" />
								  <h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
								  <h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}" alt="" />
								  <h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}" alt="" />
								  
								  <h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
								  	<h:outputText value="#{eachAttach.attachmentName}"/>
									</h:outputLink>
								</h:column>
							</h:dataTable>   
              <%-- Attachments --%>
            </td>
            <td><h:outputText value=" " /></td>
          </tr>
          
          <tr>
            <td class="msgDetailsCol">
              <h:outputText value="#{msgs.pvt_label} "/>
            </td>
            <td>
            	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.label}" />  
            </td>
            <td><h:outputText value=" " /></td>
          </tr> 
                                        
        </table>    
        </sakai:panel_titled>
        
        
               
        <sakai:button_bar rendered="#{!PrivateMessagesTool.deleteConfirm}" >
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReply}" value="#{msgs.pvt_repmsg}" accesskey="r"/>
        
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgMove}" value="#{msgs.pvt_move}" accesskey="m" />
     
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirm}" value="#{msgs.pvt_delete}" accesskey="x" />
          
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDetailCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
        </sakai:button_bar>        
        <sakai:button_bar rendered="#{PrivateMessagesTool.deleteConfirm}" >  
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgDeleteConfirmYes}" value="#{msgs.pvt_delete}" accesskey="x" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
        </sakai:button_bar>

        <sakai:panel_titled title="">
        	  <mf:htmlShowArea value="#{PrivateMessagesTool.detailMsg.msg.body}" id="htmlMsgText" />
        </sakai:panel_titled> 
        
      </h:form>

  </sakai:view>
</f:view> 
                    