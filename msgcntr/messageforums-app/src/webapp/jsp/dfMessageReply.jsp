<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>

  <sakai:view title="#{msgs.cdfm_reply_tool_bar_message}">
<!--jsp/dfMessageReply.jsp-->    
      <h:form id="dfCompose">
             		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
        <h:outputText styleClass="alertMessage" value="#{msgs.cdfm_reply_deleted}" rendered="#{ForumTool.errorSynch}" />
	

		  <h3><h:outputText value="#{msgs.cdfm_reply_tool_bar_message}"  /></h3>
      <h4><h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		    <h:outputText value=" - "/>
	      <h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>
		  </h4>	

    <p class="textPanel">
		  <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
    </p>


		<p class="instruction">
              <h:outputText value="#{msgs.cdfm_required}"/>
              <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
		</p>	  
          <h:panelGrid styleClass="jsfFormTable" columns="2" style="width: 100%;">
            <h:panelGroup>
	   			     <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
			  		   <h:outputLabel for="df_compose_title"><h:outputText value="#{msgs.cdfm_reply_title}" /></h:outputLabel>
 					  </h:panelGroup>
				   <h:inputText value="#{ForumTool.composeTitle}" style="width: 30em;" required="true" id="df_compose_title" />

          </h:panelGrid>
          
          <h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" />
          <h:message for="df_compose_title" styleClass="alertMessage" id="errorMessages" />
         
		  <h4>
		     <h:outputText value="#{msgs.cdfm_message}" /> 
		  </h4>
		    
		    <h:inputHidden id="msgHidden" value="#{ForumTool.selectedMessage.message.body}" />
		    <h:inputHidden id="titleHidden" value="#{ForumTool.selectedMessage.message.title}" />
		    <div>
		    	<input type="button" value="Insert Original Text" onClick="InsertHTML();" />
		   	</div>
		   	
            <sakai:rich_text_area value="#{ForumTool.composeBody}" rows="17" columns="70"/>
            <script language="javascript" type="text/javascript">
            var textareas = document.getElementsByTagName("textarea");
	        var rteId = textareas.item(0).id;
	        
//	        function FCKeditor_OnComplete( editorInstance )
//	        {
//	          // clears the FCK editor after initial loading
//	          editorInstance.SetHTML( "" );
//	        }
	        
	        // set the previous message variable
	        var messagetext = document.forms['dfCompose'].elements['dfCompose:msgHidden'].value;
	        var titletext = document.forms['dfCompose'].elements['dfCompose:titleHidden'].value;
	        
            function InsertHTML() 
            { 
              // These lines will write to the original textarea and makes the quoting work when FCK is not present
              var finalhtml = '<b><i><h:outputText value="#{msgs.cdfm_insert_original_text_comment}" /></i></b><br/><b><i><h:outputText value="#{msgs.cdfm_from}" /></i></b> <i><h:outputText value="#{ForumTool.selectedMessage.message.author}" /><h:outputText value=" #{msgs.cdfm_openb}" /><h:outputText value="#{ForumTool.selectedMessage.message.created}" ><f:convertDateTime pattern="#{msgs.date_format}" /></h:outputText><h:outputText value="#{msgs.cdfm_closeb}" /></i><br/><b><i><h:outputText value="#{msgs.cdfm_subject}" /></i></b> <i>' + titletext + '</i><br/><br/><i>' + messagetext + '</i><br/><br/>';
              document.forms['dfCompose'].elements[rteId].value = finalhtml;
              // Get the editor instance that we want to interact with.
              var oEditor = FCKeditorAPI.GetInstance(rteId);
              // Check the active editing mode.
              if ( oEditor.EditMode == FCK_EDITMODE_WYSIWYG )
              {
              // Insert the desired HTML.
              oEditor.InsertHtml( finalhtml );
              }
              else alert( 'You must be on WYSIWYG mode!' );
            }
            </script>
            
<%--********************* Attachment *********************--%>	
	        <h4>
	          <h:outputText value="#{msgs.cdfm_att}"/>
	        </h4>
			<h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}" styleClass="instruction" />

	          <sakai:button_bar>
	          	<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" immediate="true"/>
	          </sakai:button_bar>
	        <%-- gsilver:moving rendered attr from column to table to avoid childless table if empty--%>
		    <h:dataTable styleClass="listHier lines nolines" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach"   rendered="#{!empty ForumTool.attachments}"
			columnClasses="attach,bogus,specialLink itemAction,bogus,bogus">
			  <h:column rendered="#{!empty ForumTool.attachments}">
			  	<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
				<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />				
				</h:column>
				  <h:column>
					  <f:facet name="header">
						<h:outputText value="#{msgs.cdfm_title}"/>
					</f:facet>
					  <h:outputText value="#{eachAttach.attachment.attachmentName}"/>			
				</h:column>
				<h:column>
				  <h:commandLink action="#{ForumTool.processDeleteAttach}" 
								immediate="true"
								onfocus="document.forms[0].onsubmit();"
								title="#{msgs.cdfm_remove}">
				    <h:outputText value="#{msgs.cdfm_remove}"/>
<%--									<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>--%>
					<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>
				  </h:commandLink>
			  </h:column>
			  <h:column rendered="#{!empty ForumTool.attachments}">
			    <f:facet name="header">
				  <h:outputText value="#{msgs.cdfm_attsize}" />
				</f:facet>
				<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
			  </h:column>
			  <h:column rendered="#{!empty ForumTool.attachments}">
			    <f:facet name="header">
		  		  <h:outputText value="#{msgs.cdfm_atttype}" />
				</f:facet>
				<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
			  </h:column>
			</h:dataTable>   
  
        		
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
<%--********************* Reply *********************--%>	     	

		<h4 class="textPanelHeader">
	  	<h:outputText value="#{msgs.cdfm_replyto}"/>  
	  </h4> 
	  
	<f:verbatim><div class="hierItemBlock"></f:verbatim>
	<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
	
	  <h:panelGrid columns="2" styleClass="itemSummary">
	    <h:outputText value="#{msgs.cdfm_from}" />
	    <h:panelGroup>
	      <h:outputText value="#{ForumTool.selectedMessage.message.author}" />
	      <h:outputText value=" #{msgs.cdfm_openb}" />
	      <h:outputText value="#{ForumTool.selectedMessage.message.created}" >
          <f:convertDateTime pattern="#{msgs.date_format}" />  
        </h:outputText>
        <h:outputText value=" #{msgs.cdfm_closeb}" />
	    </h:panelGroup>
	    
	    <h:outputText value="#{msgs.cdfm_subject}" />
	    <h:outputText value="#{ForumTool.selectedMessage.message.title}" />
	    
	    <h:outputText value="#{msgs.cdfm_att}" rendered="#{!empty ForumTool.selectedMessage.message.attachments}"/>
	    <h:panelGroup rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
	      <h:dataTable value="#{ForumTool.selectedMessage.message.attachments}" var="eachAttach"  rendered="#{!empty ForumTool.selectedMessage.message.attachments}" columnClasses="attach,bogus" styleClass="attachList">
					  <h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
						  	<sakai:contentTypeMap fileType="#{eachAttach.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />						  
							</h:column>
								<h:column>
<%--							<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
							  <h:outputText value="#{eachAttach.attachmentName}"/>
							  </h:outputLink>--%>
<%--							<h:outputLink value="#{ForumTool.attachmentUrl}" target="_blank">
							  <f:param name="attachmentId" value="#{eachAttach.attachment.attachmentId}"/>
							  <h:outputText value="#{eachAttach.attachment.attachmentName}"/>
							</h:outputLink>--%>
						  <h:outputText value="#{eachAttach.attachmentName}"/>							
					  </h:column>
					</h:dataTable>
	    </h:panelGroup> 
	    </h:panelGrid>
	    <f:verbatim></h4></f:verbatim>
	    <mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}" hideBorder="true" />
	       
	<f:verbatim></div></f:verbatim>
			
      <sakai:button_bar>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgPost}" value="#{msgs.cdfm_button_bar_post_message}" accesskey="s" styleClass="active" />
    <%--    <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" /> --%>
        <sakai:button_bar_item action="#{ForumTool.processDfReplyMsgCancel}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="x" />
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

