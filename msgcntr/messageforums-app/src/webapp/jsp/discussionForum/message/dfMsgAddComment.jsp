<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf"%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader"
	scope="session">
	<jsp:setProperty name="msgs" property="baseName"
		value="org.sakaiproject.api.app.messagecenter.bundle.Messages" />
</jsp:useBean>
<f:view>

	<sakai:view title="#{msgs.cdfm_add_comment}">
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
			  
		  <div class="instruction">
  	    <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" />
		  </div>
		
		<f:verbatim><div class="hierItemBlock" style="padding-bottom:0"></f:verbatim>
		<f:verbatim><h4 class="textPanelHeader" style="margin-bottom:0"></f:verbatim>
			<h:panelGrid styleClass="itemSummary" columns="2" summary="layout">
				<h:outputText value="#{msgs.cdfm_subject}" />
				<h:outputText value="#{ForumTool.selectedMessage.message.title}" />

				<h:outputText value="#{msgs.cdfm_authoredby}" />
				<h:outputText	value="#{ForumTool.selectedMessage.message.author} #{msgs.cdfm_openb} #{ForumTool.selectedMessage.message.created} #{msgs.cdfm_closeb}" />

				<h:panelGroup styleClass="header"	rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
					<h:outputText value="#{msgs.cdfm_att}" />
				</h:panelGroup>
				<h:panelGroup	rendered="#{!empty ForumTool.selectedMessage.attachList}">
					<%-- Attachments --%>
					<h:dataTable value="#{ForumTool.selectedMessage.attachList}"	var="eachAttach">
						<h:column	rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />				

							<h:outputLink value="#{eachAttach.url}" target="_new_window">
								<h:outputText value="#{eachAttach.attachment.attachmentName}" />
							</h:outputLink>

						</h:column>
					</h:dataTable>
				</h:panelGroup>
			</h:panelGrid>
		  
			<f:verbatim></h4></f:verbatim>
    		<f:verbatim><div style="width:100%;height:150px;overflow:auto;"></f:verbatim>
				<h:outputText escape="false" value="#{ForumTool.selectedMessage.message.body}" />
			<f:verbatim></div></f:verbatim>
			
		<f:verbatim></div></f:verbatim>
		  
			<div style="padding-top: 1.0em;">
		  		<h:outputText value="#{msgs.cdfm_info_required_sign} " styleClass="reqStarInline"/><h:outputText value="#{msgs.cdfm_add_comment_label} " />	
		  </div>
		  
		  <h:inputTextarea value="#{ForumTool.moderatorComments}" rows="5" cols="50" />	  
		  
		  <sakai:button_bar> 
      	<sakai:button_bar_item action="#{ForumTool.processAddCommentToDeniedMsg}" value="#{msgs.cdfm_button_bar_add_comment}" accesskey="a"/>
      	<sakai:button_bar_item action="#{ForumTool.processCancelAddComment}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="c"/>
    	</sakai:button_bar>

		</h:form>
	</sakai:view>
</f:view>
