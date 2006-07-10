<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:view title="#{msgs.cdfm_reply_tool_bar_message}">
    
      <h:form id="dfCompose">
        <sakai:tool_bar_message value="#{msgs.cdfm_reply_tool_bar_message}" /> 
        <h:outputText styleClass="alertMessage" value="#{msgs.cdfm_reply_deleted}" rendered="#{ForumTool.errorSynch}" />
	
        <div class="breadCrumb">
          <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		      <h:outputText value=" - "/>
	        <h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>        
        </div>
          
        <sakai:instruction_message value="#{ForumTool.selectedTopic.topic.shortDescription}" />

        <div class="msgHeadings">
	      <h:outputText value="#{msgs.cdfm_your_message}"/>
        </div>

        <sakai:panel_titled>
          <h:panelGrid styleClass="jsfFormTable" columns="1" width="100%">
            <h:panelGroup styleClass="shorttext">
              <h:outputText value="#{msgs.cdfm_required}"/>
              <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
            </h:panelGroup>
            <h:panelGroup>
				     <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
					   <h:outputLabel for="df_compose_title"><h:outputText value="#{msgs.cdfm_reply_title}" /></h:outputLabel>
					   <f:verbatim><h:outputText value=" " /></f:verbatim>
					   <h:inputText value="#{ForumTool.composeTitle}" style="width: 30em;" required="true" id="df_compose_title" />
				   </h:panelGroup>

          </h:panelGrid>
          
          <h:message for="df_compose_title" styleClass="alertMessage" id="errorMessages" />

	        <sakai:panel_edit>
	          <sakai:doc_section>       
	            <h:outputLabel for=""><h:outputText value="#{msgs.cdfm_message}" /></h:outputLabel>  
	            <sakai:rich_text_area value="#{ForumTool.composeBody}" rows="17" columns="70"/>
	          </sakai:doc_section>    
	        </sakai:panel_edit>
	      </sakai:panel_titled>
<%--********************* Attachment *********************--%>	
	      <sakai:panel_titled>
	        <div class="msgHeadings">
	          <h:outputText value="#{msgs.cdfm_att}"/>
	        </div>
	        <sakai:doc_section>
	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" immediate="true"/>
	          </sakai:button_bar>
	        </sakai:doc_section>

	        <sakai:doc_section>	        
		        <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}"/>
	        </sakai:doc_section>
	        
		    <h:dataTable styleClass="listHier" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach" >
			  <h:column rendered="#{!empty ForumTool.attachments}">
			    <f:facet name="header">
				  <h:outputText value="#{msgs.cdfm_title}"/>
				</f:facet>
				<sakai:doc_section>
				  <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}" alt="" />
				  <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}" alt="" />
				  <h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
				  <h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
				  <h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}" alt="" />
				  <h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}" alt="" />
				  <h:outputText value="#{eachAttach.attachmentName}"/>
				</sakai:doc_section>
							
				<sakai:doc_section>
				  <h:commandLink action="#{ForumTool.processDeleteAttach}" 
								immediate="true"
								onfocus="document.forms[0].onsubmit();"
								title="#{msgs.cdfm_remove}">
				    <h:outputText value="#{msgs.cdfm_remove}"/>
<%--									<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
					<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>
				  </h:commandLink>
				</sakai:doc_section>
						
			  </h:column>
			  <h:column rendered="#{!empty ForumTool.attachments}">
			    <f:facet name="header">
				  <h:outputText value="#{msgs.cdfm_attsize}" />
				</f:facet>
				<h:outputText value="#{eachAttach.attachmentSize}"/>
			  </h:column>
			  <h:column rendered="#{!empty ForumTool.attachments}">
			    <f:facet name="header">
		  		  <h:outputText value="#{msgs.cdfm_atttype}" />
				</f:facet>
				<h:outputText value="#{eachAttach.attachmentType}"/>
			  </h:column>
			</h:dataTable>   
		  </sakai:panel_titled>   
        		
<%--********************* Label *********************
				<sakai:panel_titled>
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
        </sakai:panel_titled>
--%>

	        
	        <div class="msgHeadings">
	          <h:outputText value="#{msgs.cdfm_replyto}"/>
	        </div>
	        
	        <h:panelGrid summary="" columns="2" width="100%">
	          <h:panelGroup styleClass="msgDetailsCol">
	            <h:outputText value="#{msgs.cdfm_from}"/>
	          </h:panelGroup>
	          <h:panelGroup>
	            <h:outputText value="#{ForumTool.selectedMessage.message.author}" />  
              <h:outputText value="#{msgs.cdfm_openb}" />  
              <h:outputText value="#{ForumTool.selectedMessage.message.created}">
  	              <f:convertDateTime pattern="#{msgs.pvt_time_format}"/>
  	          	 </h:outputText>
  	          	 <h:outputText value="#{msgs.cdfm_closeb}" />
	          </h:panelGroup>
	          
	          <h:panelGroup styleClass="msgDetailsCol">
	            <h:outputText value="#{msgs.cdfm_subject}"/>
	          </h:panelGroup>
	          <h:panelGroup>
	            <h:outputText value="#{ForumTool.selectedMessage.message.title}" />
	          </h:panelGroup>
	          
	          <h:panelGroup styleClass="msgDetailsCol">
	            <h:outputText styleClass="msgReplyCol"  value="#{msgs.cdfm_att}"/>
	          </h:panelGroup>
	          <h:panelGroup>
	            <h:dataTable value="#{ForumTool.selectedMessage.message.attachments}" var="eachAttach" >
			          <h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
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
	          </h:panelGroup>
	          
	          <h:panelGroup styleClass="msgDetailsCol">
	            <h:outputText value="#{msgs.cdfm_message}" />
	          </h:panelGroup>
	          <h:panelGroup>
	            <%--              	<h:inputTextarea rows="5" cols="60"  value="#{ForumTool.selectedMessage.message.body}" disabled="true"/>	--%>
     			    <mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}" />
	          </h:panelGroup>
	       </h:panelGrid>
            
            
            <%--
            <tr>
              <td>
                <h:outputText value="Label" />
              </td>
              <td>
              	<h:outputText value="#{ForumTool.selectedMessage.message.label}" />  
              </td>
            </tr>
            --%>

      
      <sakai:button_bar>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgPost}" value="#{msgs.cdfm_button_bar_post_message}" accesskey="p" />
    <%--    <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" /> --%>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="c" />
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

  </sakai:view>
</f:view> 

