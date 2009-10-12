<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf"%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages" />
</jsp:useBean>
<f:view>

<sakai:view title="#{msgs.cdfm_add_comment}" toolCssHref="/sakai-messageforums-tool/css/msgcntr.css">
	       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
		<h:form id="dfMsgAddComment">

			<h3><h:outputText value="#{msgs.cdfm_add_comment}" /></h3>
			<h4>
				<h:outputText value="#{ForumTool.selectedForum.forum.title}" />
				<h:outputText value=" #{msgs.cdfm_dash} " /> 
				<h:outputText	value="#{ForumTool.selectedTopic.topic.title}" />
			</h4>
			
			<h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" />
			  
		<div class="singleMessage">
			<h:outputText value="#{ForumTool.selectedMessage.message.title}"  styleClass="title"/>
			<h:outputText	value="#{ForumTool.selectedMessage.message.author} #{msgs.cdfm_openb} #{ForumTool.selectedMessage.message.created} #{msgs.cdfm_closeb}" styleClass="textPanelFooter"/>
					<%-- Attachments --%>
			<h:dataTable value="#{ForumTool.selectedMessage.attachList}"	var="eachAttach"  cellpadding="3" cellspacing="0" columnClasses="attach,bogus" summary="layout"  style="font-size:.9em;width:auto;margin-left:1em" border="0"  rendered="#{!empty ForumTool.selectedMessage.attachList}">
						<h:column	rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />				
							<h:outputLink value="#{eachAttach.url}" target="_new_window">
								<h:outputText value="#{eachAttach.attachment.attachmentName}" />
							</h:outputLink>
						</h:column>
					</h:dataTable>
			<f:verbatim><div style="width:100%;"></f:verbatim>
				<h:outputText escape="false" value="#{ForumTool.selectedMessage.message.body}" />
			<f:verbatim></div></f:verbatim>
		  </div>
		<div class="instruction">
			<h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" />
		</div>
		  
		<div class="longtext">
			<label for="dfMsgAddComment:commentsBox" class="block"><h:outputText value="#{msgs.cdfm_info_required_sign} " styleClass="reqStarInline"/><h:outputText value="#{msgs.cdfm_add_comment_label} " /></label>	
			<h:inputTextarea value="#{ForumTool.moderatorComments}" rows="5" cols="50"  id="commentsBox"/>
		</div>	
		  <sakai:button_bar> 
		<sakai:button_bar_item action="#{ForumTool.processAddCommentToDeniedMsg}" value="#{msgs.cdfm_button_bar_add_comment}" accesskey="s" styleClass="active"/>
			<sakai:button_bar_item action="#{ForumTool.processCancelAddComment}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="x"/>
    	</sakai:button_bar>

		</h:form>
	</sakai:view>
</f:view>
