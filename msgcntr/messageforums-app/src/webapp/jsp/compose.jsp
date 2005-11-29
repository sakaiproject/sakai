<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="compose">

        <sakai:tool_bar_message value="Compose a Private Message" /> 
        <sakai:group_box>
          <h:outputText value="#{msgs.cdfm_required}"/>
          <h:outputText value="*" style="color: red"/>
        </sakai:group_box>
				
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="*" style="color: red"/>
          			<h:outputText value="To "/>		
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
  			    			<f:selectItem itemValue="pvtmsg" itemLabel="As Private Messages"/><br />
  			    			<f:selectItem itemValue="pvtmsg" itemLabel="To Recipients' Email Address(es)"/><br />
			    			</h:selectOneRadio>
              </td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="Subject" />
              </td>
              <td align="left">
              	<h:inputText value="#{PrivateMessagesTool.composeSubject}" />
              </td>
            </tr>                                   
          </table>
        </sakai:group_box>

	      <sakai:group_box>
	        <sakai:panel_edit>
	          <sakai:doc_section>       
	            <h:outputText value="Message" />  
	            <sakai:rich_text_area value="#{PrivateMessagesTool.composeBody}" rows="17" columns="70"/>
	          </sakai:doc_section>    
	        </sakai:panel_edit>
	      </sakai:group_box>
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
	        
					<h:dataTable styleClass="listHier" id="attmsg" width="100%" value="#{PrivateMessagesTool.attachments}" var="eachAttach" >
					  <h:column rendered="#{!empty PrivateMessagesTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Title"/>
							</f:facet>
							<sakai:doc_section>
								<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
								<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
								<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
								<h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
								<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
								<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
							
								<h:outputText value="#{eachAttach.attachmentName}"/>
							</sakai:doc_section>
							
							
							<sakai:doc_section>
								<h:commandLink action="#{PrivateMessagesTool.processDeleteAttach}" 
									onfocus="document.forms[0].onsubmit();">
									<h:outputText value="     Remove"/>
									<f:param value="#{eachAttach.pvtMsgAttachId}" name="pvtmsg_current_attach"/>
								</h:commandLink>
							</sakai:doc_section>
						
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
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSend}" value="Send" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgSaveDraft}" value="Save Draft" />
        <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
      </sakai:button_bar>
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

