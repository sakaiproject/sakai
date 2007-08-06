<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view>
	<h:form id="msgForum">
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>

		<div class="breadCrumb specialLink">
			<h3>
				<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			  <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
      	<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				<h:outputText value="#{msgs.cdfm_msg_pending_queue_title}" />
			</h3>
		</div>

		<div class="instruction">
			<h:outputText value="#{msgs.cdfm_deny_with_comments_msg}" rendered="#{ForumTool.numPendingMessages > 0}" />
	  	<h:outputText value="#{msgs.cdfm_no_pending_msgs}" rendered="#{ForumTool.numPendingMessages < 1}" />
	  </div>
	  
	  <h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" />
	  
		<h:dataTable id="pendingMsgs" value="#{ForumTool.pendingMessages}" width="100%" var="message" 
									columnClasses="attach,bogus" styleClass="listHier" rendered="#{ForumTool.numPendingMessages >0 }">
			<h:column>
				<f:facet name="header">
					<h:selectBooleanCheckbox title="#{msgs.cdfm_checkall}" id="mainCheckbox" onclick="javascript:selectDeselectCheckboxes(this.id, document.forms[0]);"/>
				</f:facet>
					<h:selectBooleanCheckbox value="#{message.selected}" id="childCheckbox" onclick="javascript:resetMainCheckbox('msgForum:pendingMsgs:mainCheckbox');"/>
			</h:column>
			
			<h:column>
				<f:facet name="header">
					<h:panelGroup style="text-align: right; float:right;">
						<h:commandLink id="denyMsgs" title="#{msgs.cdfm_button_bar_deny}" action="#{ForumTool.markCheckedAsDenied}">
		  				<h:graphicImage value="/../../library/image/silk/cross.png" alt="#{msgs.cdfm_button_bar_deny}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_deny} " />
		  			</h:commandLink>
						<h:commandLink id="approveMsgs" title="#{msgs.cdfm_button_bar_approve}" action="#{ForumTool.markCheckedAsApproved}" style="padding-left: 1.0em; padding-right: 1.0em;">
		  				<h:graphicImage value="/../../library/image/silk/tick.png" alt="#{msgs.cdfm_button_bar_approve}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_approve}" />
		  		</h:commandLink>
					</h:panelGroup>
				</f:facet>
				
				<f:verbatim><div class="hierItemBlock"><h4 class="specialLink textPanelHeader"><div class="unreadMsg"></f:verbatim>
				<h:commandLink action="#{ForumTool.processActionDisplayForum}"  value="#{message.message.topic.openForum.title}" title=" #{message.message.topic.openForum.title}">
		        <f:param value="#{message.message.topic.openForum.id}" name="forumId"/>
	      </h:commandLink>
					<h:outputText value=" / " />
			  <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{message.message.topic.title}" title=" #{message.message.topic.title}">
					      <f:param value="#{message.message.topic.id}" name="topicId"/>
					      <f:param value="#{message.message.topic.openForum.id}" name="forumId"/>
				      </h:commandLink>
				<f:verbatim></div><div style="padding-top: 1.0em"></f:verbatim>
				
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" title="#{message.message.title}">
					<h:outputText value="#{message.message.title}" />
					<f:param value="#{message.message.id}" name="messageId"/>
		      <f:param value="#{message.message.topic.id}" name="topicId"/>
		      <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				</h:commandLink>
				<h:outputText value=" #{msgs.cdfm_dash} " />
				<h:outputText value="#{message.message.author}" />
				<h:outputText value=" #{msgs.cdfm_openb}" />
				<h:outputText value="#{message.message.created}">
			  	<f:convertDateTime pattern="#{msgs.date_format}" />
			  </h:outputText>
				<h:outputText value="#{msgs.cdfm_closeb}" />
				<f:verbatim></div></h4><div></f:verbatim>
					<h:outputText value="#{message.message.body}" escape="false" />
				<f:verbatim></div></div></f:verbatim>
				
			</h:column>
			
		</h:dataTable>
			  
			  
		
	</h:form>
</sakai:view>
</f:view>
