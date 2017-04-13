<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view title="#{msgs.cdfm_container_title}" toolCssHref="/messageforums-tool/css/msgcntr.css">
  <!--jsp/dfCompose.jsp-->
    <h:form id="dfCompose">
			<f:verbatim><input type="hidden" id="currentTopicId" name="currentTopicId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedTopic.topic.id}"/><f:verbatim>"/></f:verbatim>
			<f:verbatim><input type="hidden" id="currentForumId" name="currentForumId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedForum.forum.id}"/><f:verbatim>"/></f:verbatim>
           		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
			<script type="text/javascript">
				$(document).ready(function() {
					$('#openLinkBlock').hide();
					jQuery('.toggle').click(function(e) { 
						$('#fullTopicDescription').toggle('slow');
						$('.toggleParent').toggle();					
						 resizeFrame('grow')
				});						
				});			
			</script>



      <h3><h:outputText value="#{msgs.cdfm_tool_bar_message}" /></h3>
			<table class="topicBloc topicBlocLone specialLink">
				<tr>
					<td>
						<h:outputText value="#{ForumTool.selectedForum.forum.title} /  #{ForumTool.selectedTopic.topic.title}"  styleClass="title"/> 
	  <div class="textPanel">
	  <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
	  </div>
						<div>
		     <p style="padding:0;margin:.5em 0" id="openLinkBlock" class="toggleParent">
					<a href="#" id="showMessage" class="toggle show">
						<h:graphicImage url="/images/expand.gif" alt=""/>	
						<h:outputText value=" #{msgs.cdfm_hide_full_description}" />
					</a>
				</p>
				<p style="padding:0;margin:.5em 0" id="hideLinkBlock" class="toggleParent">
					<a href="#" id="hideMessage" class="toggle show">
						<h:graphicImage url="/images/collapse.gif" alt="" />					
						<h:outputText value=" #{msgs.cdfm_read_full_description}"/>
					</a>
				</p>
		     
		     
							<%-- //designNote: am assuming that the thinking is that once the user is here 
								there is no longer need for the long description context (or as much), so do not put it in
								the response by default - same goes for attachment list if any --%>
			<div id="fullTopicDescription" style="display: none">
			<mf:htmlShowArea value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
		                   id="topic_extended_description" 
									hideBorder="true" />
			</div>
						</div>
					</td>
				</tr>
			</table>
			<div>
				<h:outputText value="#{ForumTool.selectedTopic.topic.revealIDsToRoles ? msgs.cdfm_revealIDsToRoles_blurb :  msgs.cdfm_anonymous_blurb}" rendered="#{ForumTool.anonymousEnabled && ForumTool.selectedTopic.topic.postAnonymous}"/>
			</div>
			<div class="instruction">			 
				 <h:outputText value="#{msgs.cdfm_required}"/>
				 <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
			  </div>
			 <h:message for="df_compose_title" warnStyle="WARN" styleClass="messageAlert"/>
			 <h:panelGrid styleClass="jsfFormTable" columns="1" width="100%" border="0" cellpadding="0">
				<h:panelGroup>
					<h:outputLabel for="df_compose_title" style="padding-bottom:.3em;display:block;clear:both;float:none">
				     <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
					<h:outputText value="#{msgs.cdfm_title}"/>
				</h:outputLabel>
					   <h:inputText value="#{ForumTool.composeTitle}" style="width:30em;" maxlength="250" required="true" id="df_compose_title">
					     <f:validator validatorId="MessageTitle" />
						 <f:validateLength minimum="1" maximum="255"/>
					   </h:inputText>
				   </h:panelGroup>
          </h:panelGrid>


				<p><h:message for="df_compose_body" styleClass="messageAlert" id="bodyErrorMessages" /></p>
	            <h:outputText value="#{msgs.cdfm_message}" />
	            
			
			<span style="margin-left:3em"><img src="/library/image/silk/table_add.png" />&nbsp;<h:outputText value="#{msgs.cdfm_message_count}" />:&nbsp;<span  id="counttotal"> </span></span>
					

	            <sakai:inputRichText textareaOnly="#{PrivateMessagesTool.mobileSession}" value="#{ForumTool.composeBody}" id="df_compose_body" rows="#{ForumTool.editorRows}" cols="132">
				</sakai:inputRichText>
<%--********************* Attachment *********************--%>	
	      <h4>
	        <h:outputText value="#{msgs.cdfm_att}" />
	      </h4>
			<p>	        
				<h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}" styleClass="instruction" style="display:block"/>
			</p>	
	        
			<h:dataTable styleClass="attachPanel" id="attmsg" width="100%" value="#{ForumTool.attachments}"  rendered="#{!empty ForumTool.attachments}" var="eachAttach"  columnClasses=",itemAction specialLink,," cellpadding="0" cellspacing="0" style="width:auto">
					  <h:column rendered="#{!empty ForumTool.attachments}">
							<f:facet name="header">
								<h:outputText value="#{msgs.cdfm_title}"/>
							</f:facet>
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
					<h:outputText value=" "/>
								<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
							</h:column>
							<h:column>
								<h:commandLink action="#{ForumTool.processDeleteAttach}" 
									             immediate="true"
									             onfocus="document.forms[0].onsubmit();"
									             title="#{msgs.cdfm_remove}">
									<h:outputText value="#{msgs.cdfm_remove}"/>
<%--									<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
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
        		
			<p style="padding:0" class="act">
				<h:commandButton action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}" accesskey="a" 
					style="font-size:96%"  rendered="#{empty ForumTool.attachments}"/>
				<h:commandButton action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_more_redirect}" accesskey="a" 
					style="font-size:96%"  rendered="#{!empty ForumTool.attachments}"/>
			</p>

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
			<h:outputText value="#{msgs.cdfm_reply_message_note}" styleClass="highlight" rendered="#{ForumTool.selectedTopic.moderated == 'true' }" /><h:outputText value="#{msgs.cdfm_reply_message_mod_inst}" styleClass="instruction" rendered="#{ForumTool.selectedTopic.moderated == 'true' }" />
			<p style="padding:0" class="act">
				<h:commandButton id="post" action="#{ForumTool.processDfMsgPost}" value="#{msgs.cdfm_button_bar_post_message}" accesskey="s" styleClass="active blockMeOnClick"/>
				<%--  <sakai:button_bar_item action="#{ForumTool.processDfMsgSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" /> --%>
				<h:commandButton action="#{ForumTool.processDfMsgCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="x" />
                <h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
			</p>
			<%--
      <sakai:button_bar>
        <sakai:button_bar_item action="#{ForumTool.processDfMsgPost}" value="#{msgs.cdfm_button_bar_post_message}" accesskey="s" styleClass="active"/>
        <sakai:button_bar_item action="#{ForumTool.processDfMsgCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="x" />
      </sakai:button_bar>
			--%>
     

			
			
		</h:form>
  </sakai:view>
</f:view> 

