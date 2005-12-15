<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="dfCompose">
        <sakai:tool_bar_message value="Revise Discussion Forum Message" /> 

        <sakai:group_box>
					<sakai:panel_edit>
						<sakai:doc_section>
              <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		          <h:outputText value=" - "/>
						</sakai:doc_section>
						<sakai:doc_section>
		         	<h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>        
							<h:outputText value=""/>
						</sakai:doc_section>
					</sakai:panel_edit>
        </sakai:group_box>
          
        <sakai:group_box>  
          <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
          <h:outputText value=""/>
        </sakai:group_box>

        <sakai:group_box>
	        <table width="100%" align="center">
	          <tr>
	            <td align="center" style="font-weight:bold;background-color:#DDDFE4;color: #000;padding:.3em;margin:-.3em -2.2em;text-align:left;font-size: .9em;line-height:1.3em">
	              <h:outputText value="#{msgs.cdfm_your_message}"/>
	            </td>
	          </tr>
	        </table>
        </sakai:group_box>

        <sakai:group_box>
          <h:outputText value="#{msgs.cdfm_required}"/>
          <h:outputText value="*" style="color: red"/>
        </sakai:group_box>
				
				<sakai:group_box>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="7%">
                <h:outputText value="#{msgs.cdfm_title}" />
			          <h:outputText value="*" style="color: red"/>                
              </td>
              <td align="left" width="33%">
              	<h:inputText value="#{ForumTool.composeTitle}" style="width:200px;" required="true" id="df_compose_title"/>
              </td>
              <td align="left" width="60%">
              	<h:message for="df_compose_title" warnStyle="WARN" style="color: red;"/>
              </td>
            </tr>                                   
          </table>
        </sakai:group_box>

	      <sakai:group_box>
	        <sakai:panel_edit>
	          <sakai:doc_section>       
	            <h:outputText value="Message" />  
	            <sakai:rich_text_area value="#{ForumTool.composeBody}" rows="17" columns="70"/>
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
	          	<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" immediate="true"/>
	          </sakai:button_bar>
	        </sakai:doc_section>

	        <sakai:doc_section>	        
		        <h:outputText value="No Attachments Yet" rendered="#{empty ForumTool.attachments}"/>
	        </sakai:doc_section>
	        
					<h:dataTable styleClass="listHier" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach" >
					  <h:column rendered="#{!empty ForumTool.attachments}">
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
							
								<h:outputText value="#{eachAttach.attachmentName}"/>
							</sakai:doc_section>
							
							<sakai:doc_section>
								<h:commandLink action="#{ForumTool.processDeleteAttachRevise}" 
									immediate="true"
									onfocus="document.forms[0].onsubmit();">
									<h:outputText value="     Remove"/>
<%--									<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
									<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>
								</h:commandLink>
							</sakai:doc_section>
						
						</h:column>
					  <h:column rendered="#{!empty ForumTool.attachments}">
							<f:facet name="header">
								<h:outputText value="Size" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentSize}"/>
						</h:column>
					  <h:column rendered="#{!empty ForumTool.attachments}">
							<f:facet name="header">
		  			    <h:outputText value="Type" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachmentType}"/>
						</h:column>
						</h:dataTable>   
					</sakai:group_box>   
        		
<%--********************* Label *********************
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
--%>		        
      <sakai:button_bar>
        <sakai:button_bar_item action="#{ForumTool.processDfMsgRevisedPost}" value="Post Revised Message" />
        <sakai:button_bar_item action="#{ForumTool.processDfMsgSaveRevisedDraft}" value="#{msgs.cdfm_button_bar_save_draft}" />
        <sakai:button_bar_item action="#{ForumTool.processDfMsgCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true"/>
      </sakai:button_bar>
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

