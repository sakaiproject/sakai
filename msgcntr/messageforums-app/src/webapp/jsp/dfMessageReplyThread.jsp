<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>

<sakai:view title="#{msgs.cdfm_reply_tool_bar_message}" toolCssHref="/messageforums-tool/css/msgcntr.css">
	<!--jsp/dfMessageReplyThread.jsp-->    
      <h:form id="dfCompose">
		<f:verbatim><input type="hidden" id="currentMessageId" name="currentMessageId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedMessage.message.id}"/><f:verbatim>"/></f:verbatim>
		<f:verbatim><input type="hidden" id="currentTopicId" name="currentTopicId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedTopic.topic.id}"/><f:verbatim>"/></f:verbatim>
		<f:verbatim><input type="hidden" id="currentForumId" name="currentForumId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedForum.forum.id}"/><f:verbatim>"/></f:verbatim>
             <script>includeLatestJQuery("msgcntr");</script>
       		<script src="/messageforums-tool/js/sak-10625.js"></script>
       		<script src="/messageforums-tool/js/forum.js"></script>
       		<script src="/messageforums-tool/js/messages.js"></script>
        <script>
            $(document).ready(function () {
                var menuLink = $('#forumsMainMenuLink');
                var menuLinkSpan = menuLink.closest('span');
                menuLinkSpan.addClass('current');
                menuLinkSpan.html(menuLink.text());

                $('#openLinkBlock').hide();
                jQuery('.toggle').click(function(e) {
                    $('#replytomessage').toggle('slow');
                    $('.toggleParent').toggle();
                    resizeFrame('grow');
                });
            });
        </script>
        <%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
        <h:outputText styleClass="alertMessage" value="#{msgs.cdfm_reply_deleted}" rendered="#{ForumTool.errorSynch}" />
		  <h3><h:outputText value="#{msgs.cdfm_reply_thread_tool_bar_message}"  /></h3>
	
		<table class="topicBloc topicBlocLone">
			<tr>
				<td>
					<span class ="title">
						<h:outputText value="#{ForumTool.selectedForum.forum.title}" />
						<h:outputText value=" / "/>
	      <h:outputText value="#{ForumTool.selectedTopic.topic.title}"/>
					</span>
    <p class="textPanel">
		  <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
    </p>
				</td>
			</tr>
		</table>	

		<%--********************* Reply To *********************--%>	     	
		<div class="singleMessageReply"> 
				<h:outputText value="#{msgs.cdfm_reply_message_pref}" styleClass="title highlight"/> <h:outputText value="#{ForumTool.selectedMessage.message.title}" styleClass="title"/>
				<h:outputText value="#{ForumTool.selectedMessage.anonAwareAuthor}" styleClass="textPanelFooter #{ForumTool.selectedMessage.useAnonymousId ? 'anonymousAuthor' : ''}"/>
				<h:outputText value=" #{msgs.cdfm_me}" styleClass="textPanelFooter" rendered="#{ForumTool.selectedMessage.currentUserAndAnonymous}" />
				<h:outputText value=" #{msgs.cdfm_openb}" styleClass="textPanelFooter"/>
				<h:outputText value="#{ForumTool.selectedMessage.message.created}" styleClass="textPanelFooter">
					<f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>  
				</h:outputText>
				<h:outputText value=" #{msgs.cdfm_closeb}" styleClass="textPanelFooter"/>

					<p style="padding:0;margin:.5em 0" id="openLinkBlock" class="toggleParent">
						<a href="#" id="showMessage" class="toggle show">
							<h:graphicImage url="/images/collapse.gif" alt="" />	
							<h:outputText value=" #{msgs.cdfm_read_full_rep_tomessage}" />
						</a>
					</p>
					<p style="padding:0;margin:.5em 0" id="hideLinkBlock" class="toggleParent">
						<a href="#" id="hideMessage" class="toggle show">
							<h:graphicImage url="/images/expand.gif" alt=""  />					
							<h:outputText value=" #{msgs.cdfm_hide_full_rep_tomessage}"/>
						</a>
					</p>


			<div  id="replytomessage">	
				<mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}" hideBorder="true" />
	
				<h:dataTable value="#{ForumTool.selectedMessage.message.attachments}" var="eachAttach"  rendered="#{!empty ForumTool.selectedMessage.message.attachments}" columnClasses="attach,bogus" styleClass="attachList"  
						style="font-size:.9em;width:auto;margin-left:1em" border="0">
					<h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
					<sakai:contentTypeMap fileType="#{eachAttach.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
    						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt=""  />
					</h:column>
					<%----%>
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
			</div>
		</div>	
		<t:htmlTag value="p" rendered="#{ForumTool.anonymousEnabled && ForumTool.selectedTopic.topic.postAnonymous}" >
			<h:outputText value="#{ForumTool.selectedTopic.topic.revealIDsToRoles ? msgs.cdfm_revealIDsToRoles_blurb : msgs.cdfm_anonymous_blurb}" />
		</t:htmlTag>
		<p class="instruction">
              <h:outputText value="#{msgs.cdfm_required}"/>
              <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
		</p>	  

		<h:panelGrid styleClass="jsfFormTable" columns="1" style="width: 100%;">
		
			<h:panelGroup style="padding-top:.5em">
				<h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" rendered="#{! empty facesContext.maximumSeverity}"/>
				<h:message for="df_compose_title" styleClass="messageAlert" id="errorMessages" />
				<h:outputLabel for="df_compose_title" style="display:block;float:none;clear:both;padding-bottom:.3em;padding-top:.3em;">
	   			     <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
					<h:outputText value="#{msgs.cdfm_reply_title}" />
				</h:outputLabel>
				<h:inputText value="#{ForumTool.composeTitle}" style="width: 30em;" maxlength="250" required="true" id="df_compose_title">
					 <f:validateLength minimum="1" maximum="255"/>
				</h:inputText>
 					  </h:panelGroup>
          </h:panelGrid>
          
	  <p><h:message for="df_compose_body" styleClass="messageAlert" id="bodyErrorMessage" /></p>
	  <div style="padding:.5em 0;white-space:nowrap">
			<h:outputText value="#{msgs.cdfm_message}" style="padding:.5em 0"/> 
		    <h:inputHidden id="msgHidden" value="#{ForumTool.selectedMessage.message.body}" />
		    <h:inputHidden id="titleHidden" value="#{ForumTool.selectedMessage.message.title}" />
		<h:outputText value="&nbsp;&nbsp;&nbsp; " escape="false" />
		<img src="/library/image/silk/paste_plain.png" />
		<a  href="javascript:void(0)"  onclick="InsertHTML('<b><i><h:outputText value="#{msgs.cdfm_insert_original_text_comment}"/></i></b><br/><b><i><h:outputText value="#{msgs.cdfm_from}" /></i></b> <i><h:outputText value="#{ForumTool.selectedTopic.topic.postAnonymous ? ForumTool.selectedMessage.anonId : ForumTool.selectedMessage.message.authorEscaped}" /><h:outputText value=" #{msgs.cdfm_openb}" /><h:outputText value="#{ForumTool.selectedMessage.message.created}" ><f:convertDateTime pattern="#{msgs.date_format_static}" locale="#{ForumTool.userLocale}" timeZone="#{ForumTool.userTimeZone}"/></h:outputText><h:outputText value="#{msgs.cdfm_closeb}" /></i><br/><b><i><h:outputText value="#{msgs.cdfm_subject}" /></i></b>');">
		<h:outputText value="#{msgs.cdfm_message_insert}" /></a>
			<span style="margin-left:3em"><img src="/library/image/silk/table_add.png" />&nbsp;<h:outputText value="#{msgs.cdfm_message_count}" />:&nbsp;<span  id="counttotal"> </span></span>		
			
		</div>
            <sakai:inputRichText textareaOnly="#{PrivateMessagesTool.mobileSession}" value="#{ForumTool.composeBody}" id="df_compose_body" rows="#{ForumTool.editorRows}" cols="132">
			</sakai:inputRichText>
			<%-- pre-morpheus would need this: script>
				CKEDITOR.on('instanceReady', function() {resizeFrame('grow')});
			</script --%>
		<script>
			var textareas = document.getElementsByTagName("textarea");
			var rteId = textareas.item(1).id;
			
//	        function FCKeditor_OnComplete( editorInstance )
//	        {
//	          // clears the FCK editor after initial loading
//	          editorInstance.SetHTML( "" );
//	        }
	        
	        // set the previous message variable
	        var messagetext = document.forms['dfCompose'].elements['dfCompose:msgHidden'].value;
	        var titletext = document.forms['dfCompose'].elements['dfCompose:titleHidden'].value;
            </script>
            
<%--********************* Attachment *********************--%>	
	        <h4>
	          <h:outputText value="#{msgs.cdfm_att}"/>
	        </h4>
		<div style="padding-left:1em">
		<p>
			<h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}" styleClass="instruction" />
		</p>	

		<%-- //designNote: moving rendered attr from column to table to avoid childless table if empty--%>
		<h:dataTable styleClass="attachPanel" id="attmsg"  value="#{ForumTool.attachments}" var="eachAttach"   rendered="#{!empty ForumTool.attachments}"
			columnClasses="attach,bogus,specialLink itemAction,bogus,bogus" style="width:auto">
			<h:column>
				  	<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt=""  />				 
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
			<h:column>
			    <f:facet name="header">
				  <h:outputText value="#{msgs.cdfm_attsize}" />
				</f:facet>
				<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
			  </h:column>
			<h:column>
			    <f:facet name="header">
		  		  <h:outputText value="#{msgs.cdfm_atttype}" />
				</f:facet>
				<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
			  </h:column>
			</h:dataTable>   
		<p style="padding:0" class="act">
			<h:commandButton action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" 
				rendered="#{empty ForumTool.attachments}" style="font-size:95%"/>
			<h:commandButton action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_more_redirect}" 
				rendered="#{!empty ForumTool.attachments}" style="font-size:95%"/>
		</p>
		</div>
  
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

						<h:outputText value="#{msgs.cdfm_reply_message_note} " styleClass="highlight" rendered="#{ForumTool.selectedTopic.moderated == 'true' }" /><h:outputText value="#{msgs.cdfm_reply_message_mod_inst}" styleClass="instruction" rendered="#{ForumTool.selectedTopic.moderated == 'true' }" />
	  
		<div style="padding:0" class="act">
        <h:commandButton id="post" action="#{ForumTool.processDfReplyMsgPost}" value="#{msgs.cdfm_button_bar_post_message}" accesskey="s" styleClass="blockMeOnClick"/>
        <h:commandButton action="#{ForumTool.processDfReplyThreadCancel}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="x" />
         <h:outputText styleClass="sak-banner-info" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
		</div>

<script>
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

