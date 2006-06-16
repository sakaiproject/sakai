<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="pvtMsgReply">
  			<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
  			<h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.cdfm_message_pvtarea}" /> /
				<h:outputText value="Reply to Private Message" />
        	  <sakai:tool_bar_message value="Reply to Private Message" /> 
 			  <div class="instruction">
  			    <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" style="color: red"/>
			  </div>
			  <h:outputLink rendered="#{PrivateMessagesTool.renderPrivacyAlert}" value="#{PrivateMessagesTool.privacyAlertUrl}" target="_blank" >
			  	 <sakai:instruction_message value="#{PrivateMessagesTool.privacyAlert}"/>
			  </h:outputLink>
			  <h:messages styleClass="alertMessage" id="errorMessages" /> 
			  
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="#{msgs.pvt_to}"/>		
              </td>
              <td align="left">   
          				<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.author}" /> 
              </td>                           
            </tr>
            <tr>
              <td align="left" width="20%">
              	<h:outputText value="Select Additional Recipients "/>		
              </td>
              <td align="left">
      
          			<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}" size="5" style="width:200px;">
            			<f:selectItems value="#{PrivateMessagesTool.totalComposeToList}"/>
          			</h:selectManyListbox>      
          			
              </td>                           
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="#{msgs.pvt_send}" />
              </td>
              <td align="left">
              	<h:selectOneRadio value="#{PrivateMessagesTool.composeSendAsPvtMsg}" layout="pageDirection">
  			    			<f:selectItem itemValue="yes" itemLabel="As Private Messages"/>
  			    			<f:selectItem itemValue="no" itemLabel="To Recipients' Email Address(es)"/>
			    			</h:selectOneRadio>
              </td>
            </tr>
            <tr>
              <td align="left">
              	<h:outputText value="#{msgs.pvt_star}" style="color: red"/>
                <h:outputText value="#{msgs.pvt_subject}" />
              </td>
              <td align="left">
              	<%--
              	<h:outputText value="Re: #{PrivateMessagesTool.detailMsg.msg.title}" />  
              	--%>
              	<h:inputText value="#{PrivateMessagesTool.replyToSubject}" style="width:300px;"/>	
              </td>
            </tr>                                   
          </table>
        </sakai:group_box>

	      <sakai:group_box>
	        <sakai:panel_edit>
	          <sakai:doc_section>       
	            <h:outputText value="Message" />  
	            <sakai:rich_text_area rows="17" columns="70"  value="#{PrivateMessagesTool.replyToBody}" />	
	          </sakai:doc_section>    
	        </sakai:panel_edit>
	      </sakai:group_box>


<%--********************* Attachment *********************--%>	
	      <sakai:group_box>
					<h4><h:outputText value="#{msgs.pvt_att}"/></h4>
	          <%-- Existing Attachments 
              <h:dataTable value="#{PrivateMessagesTool.detailMsg.msg.attachments}" var="existAttach" >
					  		<h:column rendered="#{!empty PrivateMessagesTool.detailMsg.message.attachments}">
								<h:graphicImage url="/images/excel.gif" rendered="#{existAttach.attachmentType == 'application/vnd.ms-excel'}"/>
								<h:graphicImage url="/images/html.gif" rendered="#{existAttach.attachmentType == 'text/html'}"/>
								<h:graphicImage url="/images/pdf.gif" rendered="#{existAttach.attachmentType == 'application/pdf'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{existAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								<h:graphicImage url="/images/text.gif" rendered="#{existAttach.attachmentType == 'text/plain'}"/>
								<h:graphicImage url="/images/word.gif" rendered="#{existAttach.attachmentType == 'application/msword'}"/>
							
								<h:outputText value="#{existAttach.attachmentName}"/>
						
								</h:column>
							</h:dataTable>  
					--%>	
	        <sakai:doc_section>	        
	        	<h:outputText value="#{msgs.pvt_noatt}" rendered="#{empty PrivateMessagesTool.allAttachments}"/>
	        </sakai:doc_section>
	        
	        <sakai:doc_section>
	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{PrivateMessagesTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" />
	          </sakai:button_bar>
	        </sakai:doc_section>
	        
	        
					<h:dataTable styleClass="listHier" id="attmsgrep" width="100%" rendered="#{!empty PrivateMessagesTool.allAttachments}" value="#{PrivateMessagesTool.allAttachments}" var="eachAttach" >
					  <h:column >
							<f:facet name="header">
								<h:outputText value="Title"/>
							</f:facet>
							<sakai:doc_section>
								<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
								<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
								<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
								<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
								<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
							 
							  <h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
									<h:outputText value="#{eachAttach.attachmentName}"/>
								</h:outputLink>

							</sakai:doc_section>
							

							<sakai:doc_section>
								<h:commandLink action="#{PrivateMessagesTool.processDeleteReplyAttach}" 
									immediate="true"
									onfocus="document.forms[0].onsubmit();">
									<h:outputText value="     Remove"/>
									<f:param value="#{eachAttach.attachmentId}" name="remsg_current_attach"/>
								</h:commandLink>
							</sakai:doc_section>
							
						</h:column>
					  <h:column >
							<f:facet name="header">
								<h:outputText value="Size" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentSize}"/>
						</h:column>
					  <h:column >
							<f:facet name="header">
		  			    <h:outputText value="Type" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentType}"/>
						</h:column>
						</h:dataTable>   
					</sakai:group_box>  
					 
 <%--********************* Reply *********************--%>	
	      <sakai:group_box>
	      	<h4><h:outputText value="#{msgs.pvt_replyto}"/></h4>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="#{msgs.pvt_from} "/>		
              </td>
              <td align="left">   
          			<h:outputText value="#{PrivateMessagesTool.userId}" />  
              	<h:outputText value=" #{msgs.pvt_openb}" />  
              	<h:outputText value="#{PrivateMessagesTool.time}">
  	            	<f:convertDateTime pattern="MM/dd/yy 'at' HH:mm:ss"/>
  	          	</h:outputText>
              	<h:outputText value="#{msgs.pvt_closeb}" />   
              </td>                           
            </tr>
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="#{msgs.pvt_subject}"/>		
              </td>
              <td align="left">   
          			<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.title}" />  
              </td>                           
            </tr>
            <%--
            <tr>
              <td align="left">
                <h:outputText value="#{msgs.pvt_label}" />
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.msg.label}" />  
              </td>
            </tr>
            --%>
            <tr>
              <td valign="top">
              	<h:outputText value="#{msgs.pvt_message}" />
              </td>
              <td align="left">
              	<mf:htmlShowArea value="#{PrivateMessagesTool.detailMsg.msg.body}"/>					
              </td>
            </tr>                                   
          </table>
	      </sakai:group_box>
	             		
<hr/>
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="#{msgs.pvt_label}"/>
              </td>
              <td align="left">
 							  <h:selectOneListbox size="1" id="viewlist" value="#{PrivateMessagesTool.selectedLabel}">
            		  <f:selectItem itemLabel="Normal" itemValue="Normal"/>
            		  <f:selectItem itemLabel="Low" itemValue="Low Priority"/>
            		  <f:selectItem itemLabel="High Priority" itemValue="High Priority"/>
          			</h:selectOneListbox>  
              </td>                                       
            </tr>                                
          </table>
        </sakai:group_box>
		        
      <sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplySend}" value="Send" />
        <%--<sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplySaveDraft}" value="Save Draft" />--%>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgComposeCancel}" value="Cancel" />
      </sakai:button_bar>
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

