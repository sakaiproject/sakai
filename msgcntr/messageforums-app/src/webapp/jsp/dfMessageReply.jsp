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
      <h:form id="dfCompose">
        <sakai:tool_bar_message value="Reply to Discussion Forum Message" /> 

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
              <td align="left" width="15%">
			          <h:outputText value="*" style="color: red"/>                
                <h:outputText value="Reply Title" />
              </td>
              <td align="left" width="30%">
              	<h:inputText value="#{ForumTool.composeTitle}" style="width:200px;" required="true" id="df_compose_title"/>
              </td>
              <td align="left" width="55%">
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
								<h:commandLink action="#{ForumTool.processDeleteAttach}" 
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
          			  <h:outputText value="#{ForumTool.selectedMessage.message.author}" />  
              	  <h:outputText value=" (" />  
              	  <h:outputText value="#{ForumTool.selectedMessage.message.created}">
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
          			<h:outputText value="#{ForumTool.selectedMessage.message.title}" />  
              </td>                           
            </tr>
            <%--
            <tr>
              <td align="left">
                <h:outputText value="Label" />
              </td>
              <td align="left">
              	<h:outputText value="#{ForumTool.selectedMessage.message.label}" />  
              </td>
            </tr>
            --%>
            <tr>
              <td align="left">
                <h:outputText style="font-weight:bold"  value="Attachments "/>
              </td>
              <td align="left">
                <h:dataTable value="#{ForumTool.selectedMessage.message.attachments}" var="eachAttach" >
			      		  <h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
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
              </td>
              <td></td>
            </tr>
            <tr>
              <td align="left">
                <h:outputText value="Message" />
              </td>
              <td align="left">
<%--              	<h:inputTextarea rows="5" cols="60"  value="#{ForumTool.selectedMessage.message.body}" disabled="true"/>	--%>
     						<mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}"/>
              </td>
            </tr>                                   
          </table>
	      </sakai:group_box>

      <sakai:button_bar>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgPost}" value="#{msgs.cdfm_button_bar_post_message}" />
    <%--    <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" /> --%>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true"/>
      </sakai:button_bar>
<script type="text/javascript">
setTimeout(function(){ 
  var _div = document.getElementsByTagName('div');
  for(i=0;i<_div.length; i++)
  {
    if(_div[i].className == 'htmlarea')
    {
      var children = _div[i].childNodes;
    	for (j=0; j<children.length; j++)
	    {
    	  if(children.item(j).tagName == 'IFRAME')
	      {
    	    children.item(j).contentWindow.focus();
	      }
      }
    }
  }
}, 800);
</script>
      
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

