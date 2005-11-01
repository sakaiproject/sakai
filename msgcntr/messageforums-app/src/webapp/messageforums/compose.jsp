<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form onsubmit="return false;">

        <sakai:tool_bar_message value="#{msgs.cdfm_tool_bar_message}" /> 
                
        <sakai:doc_section>
          <h:outputText value="#{MessageForumsTool.topicProxy.crumbTrail}" style="font-weight:bold"/>
        </sakai:doc_section>      
            
        <sakai:doc_section>
          <h:outputText value="#{MessageForumsTool.topicProxy.shortDescription}"/>
        </sakai:doc_section>

        <sakai:group_box>
          <table width="100%" align="center">
            <tr>
              <td align="center" style="font-weight:bold;background-color:#DDDFE4;color: #000;padding:.3em;margin:-.3em -2.2em;text-align:left;font-size: .9em;line-height:1.3em">
                <h:outputText value="#{msgs.cdfm_your_message}"/>
              </td>
            </tr>
          </table>
          <sakai:group_box>
            <h:outputText value="#{msgs.cdfm_required}"/>
            <h:outputText value="*" style="color: red"/>
          </sakai:group_box>
        </sakai:group_box>

        <sakai:group_box>
          <sakai:panel_edit>
            <sakai:doc_section>
              <h:outputText value="*" style="color: red"/>
              <h:outputText value="#{msgs.cdfm_title}" style="font-weight:bold"/>              
              <h:outputText value="    "/>
              <h:inputText value="#{MessageForumsTool.topicProxy.messageModel.title}" id="title"/>
              <h:outputText value="#{msgs.cdfm_empty_title_error}" style="color: red" rendered="#{MessageForumsTool.errorMessages.displayTitleErrorMessage}"/>
            </sakai:doc_section>    
          </sakai:panel_edit>
        </sakai:group_box>

        <sakai:group_box>
          <sakai:doc_section>                
            <h:outputText value="#{msgs.cdfm_message}" style="font-weight:bold"/>
          </sakai:doc_section>                
        </sakai:group_box>

        <sakai:group_box>
          <sakai:panel_edit>
            <sakai:doc_section>                
              <sakai:rich_text_area value="#{MessageForumsTool.topicProxy.messageModel.body}" rows="17" columns="70"/>
            </sakai:doc_section>    
          </sakai:panel_edit>
        </sakai:group_box>
		
        <sakai:group_box>
          <table width="100%" align="center">
            <tr>
              <td align="center" style="font-weight:bold;background-color:#DDDFE4;color: #000;padding:.3em;margin:-.3em -2.2em;text-align:left;font-size: .9em;line-height:1.3em">
                <h:outputText value="#{msgs.cdfm_attachments}"/>
              </td>
            </tr>
          </table>
        </sakai:group_box>       
		
        <sakai:group_box>
          <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty MessageForumsTool.topicProxy.messageModel.attachments}"/>          
          <h:dataTable 
              id="attachments-id" 
              border="0" cellspacing="10" cellpadding="2"
              value="#{MessageForumsTool.topicProxy.messageModel.attachments}" 
              var="attachment" 
              rendered="#{!empty MessageForumsTool.topicProxy.messageModel.attachments}"> 
            <h:column rendered="#{!empty MessageForumsTool.topicProxy.messageModel.attachments}">
              <f:facet name="header">
                <h:outputText value="Title"/>
              </f:facet>
              <sakai:doc_section>
                <h:graphicImage url="/messageforums/excel.gif" rendered="#{attachment.attachmentType == 'application/vnd.ms-excel'}"/>
                <h:graphicImage url="/messageforums/html.gif" rendered="#{attachment.attachmentType == 'text/html'}"/>
                <h:graphicImage url="/messageforums/pdf.gif" rendered="#{attachment.attachmentType == 'application/pdf'}"/>
                <h:graphicImage url="/messageforums/ppt.gif" rendered="#{attachment.attachmentType == 'application/vnd.ms-powerpoint'}"/>
                <h:graphicImage url="/messageforums/text.gif" rendered="#{attachment.attachmentType == 'text/plain'}"/>
                <h:graphicImage url="/messageforums/word.gif" rendered="#{attachment.attachmentType == 'application/msword'}"/>              
                <h:outputText value="#{attachment.attachmentName}"/>
              </sakai:doc_section>
            </h:column>
            <h:column rendered="#{!empty MessageForumsTool.topicProxy.messageModel.attachments}">
              <f:facet name="header">
                <h:outputText value="Size" />
              </f:facet>
              <h:outputText value="#{attachment.attachmentSize}"/>
            </h:column>
            <h:column rendered="#{!empty MessageForumsTool.topicProxy.messageModel.attachments}">
              <f:facet name="header">
                <h:outputText value="Type" />
              </f:facet>
              <h:outputText value="#{attachment.attachmentType}"/>
            </h:column>
            <h:column rendered="#{!empty MessageForumsTool.topicProxy.messageModel.attachments}">
              <f:facet name="header">
                <h:outputText value="Remove" />
              </f:facet>
              <h:commandLink action="#{MessageForumsTool.processCDFMDeleteAttach}" onfocus="document.forms[0].onsubmit();">
                <h:outputText value=" #{msgs.cdfm_remove}"/>
                <f:param value="#{attachment.attachmentId}" name="current_attachment"/>
              </h:commandLink>
            </h:column>
          </h:dataTable>
        </sakai:group_box>	
		
        <sakai:group_box>
          <sakai:doc_section>
            <sakai:button_bar>
              <sakai:button_bar_item action="#{MessageForumsTool.processCDFMAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" />
            </sakai:button_bar>
          </sakai:doc_section>
        </sakai:group_box>
        		
		<hr noshade="noshade"/>
		
        <sakai:group_box>
          <h:panelGroup>
          <h:outputText value="*" style="color: red"/>
          <h:outputText value="#{msgs.cdfm_labels} "/>
          <h:selectOneListbox size="1" id="list1" value="#{MessageForumsTool.topicProxy.messageModel.label}">
            <f:selectItem itemLabel="#{msgs.cdfm_none}" itemValue="none"/>
          </h:selectOneListbox>
          </h:panelGroup>
        </sakai:group_box>

        <sakai:button_bar>
          <sakai:button_bar_item action="#{MessageForumsTool.processCDFMPostMessage}" value="#{msgs.cdfm_button_bar_post_message}" />
          <sakai:button_bar_item action="#{MessageForumsTool.processCDFMSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" />
          <sakai:button_bar_item action="#{MessageForumsTool.processCDFMCancel}" value="#{msgs.cdfm_button_bar_cancel}" />
        </sakai:button_bar>
      </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 


