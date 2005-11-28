<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="pvtMsgReply">

        <sakai:tool_bar_message value="Reply to Private Message" /> 
        <sakai:group_box>
          <h:outputText value="#{msgs.cdfm_required}"/>
          <h:outputText value="*" style="color: red"/>
        </sakai:group_box>
				
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="To "/>		
              </td>
              <td align="left">   
          				<h:outputText value="#{PrivateMessagesTool.detailMsg.message.createdBy}" /> 
              </td>                           
            </tr>
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="Select Additional Recipients "/>		
              </td>
              <td align="left">
      
          			<h:selectManyListbox id="list1" value="#{PrivateMessagesTool.selectedComposeToList}">
            			<f:selectItems value="#{PrivateMessagesTool.totalComposeToList}"/>
          			</h:selectManyListbox>      
          			
              </td>                           
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="Send" />
              </td>
              <td align="left">
              	<h:selectOneRadio value="#{PrivateMessagesTool.composeSendAs}">
  			    			<f:selectItem itemValue="pvtmsg" itemLabel="As Private Messages"/>
  			    			<f:selectItem itemValue="pvtmsg" itemLabel="To Recipients' Email Address(es)"/>
			    			</h:selectOneRadio>
              </td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="Subject" />
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.message.title}" />  
              </td>
            </tr>                                   
          </table>
        </sakai:group_box>

	      <sakai:group_box>
	        <sakai:panel_edit>
	          <sakai:doc_section>       
	            <h:outputText value="Message" />  
	            <sakai:rich_text_area value="#{PrivateMessagesTool.detailMsg.message.body}" rows="17" columns="70"/>
	          </sakai:doc_section>    
	        </sakai:panel_edit>
	      </sakai:group_box>

<%--********************* Attachment *********************	
	      <sakai:group_box>
	        <table width="100%" align="center">
	          <tr>
	            <td align="center" style="font-weight:bold;background-color:#DDDFE4;color: #000;padding:.3em;margin:-.3em -2.2em;text-align:left;font-size: .9em;line-height:1.3em">
	              <h:outputText value="Attachments"/>
	            </td>
	          </tr>
	        </table>
	      </sakai:group_box>       
--%>
<%--********************* Attachment *********************--%>	
	      <sakai:group_box>
	        <table width="100%" align="center">
	          <tr>
	            <td align="center" style="font-weight:bold;background-color:#DDDFE4;color: #000;padding:.3em;margin:-.3em -2.2em;text-align:left;font-size: .9em;line-height:1.3em">
	              <h:outputText value="Attachments"/>
	            </td>
	          </tr>
	        </table>
	        <sakai:doc_section>
	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{PrivateMessagesTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" />
	          </sakai:button_bar>
	        </sakai:doc_section>
	        
					<h:dataTable value="#{PrivateMessagesTool.attachments}" var="eachAttach" styleClass="chefFlatListViewTable">
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Title"/>
							</f:facet>
							<sakai:doc_section>
								<h:graphicImage url="/sakai-messageforums-tool/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
							
								<h:outputText value="#{eachAttach.attachmentName}"/>
							</sakai:doc_section>
							
							<%--
							<sakai:doc_section>
								<h:commandLink action="#{PrivateMessagesTool.processDeleteAttach}" 
									onfocus="document.forms[0].onsubmit();">
									<h:outputText value="     Remove"/>
									<f:param value="#{eachAttach.pvtMsgAttachId}" name="syllabus_current_attach"/>
								</h:commandLink>
							</sakai:doc_section>
						 --%>
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Size" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentSize}"/>
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
		  			    <h:outputText value="Type" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentType}"/>
						</h:column>
						<%--
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Created by" />
							</f:facet>
							<h:outputText value="#{eachAttach.createdBy}"/>
						</h:column>
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Last modified by" />
							</f:facet>
							<h:outputText value="#{eachAttach.lastModifiedBy}"/>
						</h:column>
						--%>
						</h:dataTable>   
					</sakai:group_box>  
					 
 <%--********************* Reply *********************--%>	
	      <sakai:group_box>
	        <table width="100%" align="center">
	          <tr>
	            <td align="center" style="font-weight:bold;background-color:#DDDFE4;color: #000;padding:.3em;margin:-.3em -2.2em;text-align:left;font-size: .9em;line-height:1.3em">
	              <h:outputText value="Replying To"/>
	            </td>
	          </tr>
	        </table>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="From "/>		
              </td>
              <td align="left">   
          			<h:outputText value="#{PrivateMessagesTool.userId}" />  
              	<h:outputText value="(" />  
              	<h:outputText value="#{PrivateMessagesTool.time}">
  	            	<f:convertDateTime pattern="MM/dd/yy 'at' HH:mm:ss"/>
  	          	</h:outputText>
              	<h:outputText value=")" />   
              </td>                           
            </tr>
            <tr>
              <td align="left" width="20%">
          			<h:outputText value="Subject "/>		
              </td>
              <td align="left">    
          			<h:outputText value="#{PrivateMessagesTool.detailMsg.message.title}" />  
              </td>                           
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="Label" />
              </td>
              <td align="left">
              	<h:outputText value="#{PrivateMessagesTool.detailMsg.message.label}" />  
              </td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="Message" />
              </td>
              <td align="left">
              	<h:inputTextarea value="#{PrivateMessagesTool.replyToBody}" />	
              </td>
            </tr>                                   
          </table>
	      </sakai:group_box>
	             		
<%--********************* Label *********************--%>		
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="Label"/>
              </td>
              <td align="left">
 							  <h:selectOneListbox size="1" id="viewlist">
            		  <f:selectItem itemLabel="Normal" itemValue="none"/>
          			</h:selectOneListbox>  
              </td>                                       
            </tr>                                
          </table>
        </sakai:group_box>
        
      <sakai:button_bar>
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplySend}" value="Send" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgReplySaveDraft}" value="Save Draft" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
      </sakai:button_bar>
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

