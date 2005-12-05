<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
<sakai:view>
	<h:form id="DF-1">
		<div class="header-section">
			<div class="left-header-section">
				<h3><h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
					<h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedTopic.topic.baseForum.title}" >
						<f:param value="#{ForumTool.selectedTopic.topic.baseForum.uuid}" name="forumId"/>
					</h:commandLink> /
					<h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
				</h3>
				 <sakai:instruction_message value="#{ForumTool.selectedTopic.topic.shortDescription}" />
				 <br/>
				<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
					id="topic_extended_show" value="#{msgs.cdfm_read_full_description}">
					<f:param value="#{topic.topic.uuid}" name="topicId"/>
					<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
				</h:commandLink>
				<h:inputTextarea rows="5" cols="100" id="topic_extended_description" disabled="true" value="#{ForumTool.selectedTopic.topic.extendedDescription}" rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>
				<f:verbatim><br/></f:verbatim>
				<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" id="topic_extended_hide" 
					 value="#{msgs.cdfm_hide_full_description}" rendered="#{ForumTool.selectedTopic.readFullDesciption}">
					<f:param value="#{ForumTool.selectedTopic.topic.uuid}" name="topicId"/>
					<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
				</h:commandLink>		
			</div>
			<div class="right-header-section">
				<h:outputText   value="#{msgs.cdfm_previous_topic}   "  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
				<h:outputText   value="#{msgs.cdfm_next_topic}   " rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
				<h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}   "  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" >
					<f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
				</h:commandLink>
				<h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}   " rendered="#{ForumTool.selectedTopic.hasNextTopic}" >
					<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>	
				</h:commandLink>
			</div>
		</div>

      	<h:dataTable styleClass="listHier" id="messages" value="#{ForumTool.selectedTopic.messages}" var="message">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_checkall}" />
				</f:facet>
				<h:selectBooleanCheckbox value="#{message.selected}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif"/>
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{message.hasAttachment}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_subject}" />
				</f:facet>
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true">
    	        	<h:outputText value=" #{message.message.title}"/>
        	    	<f:param value="#{message.message.uuid}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.uuid}" name="topicId"/>
	          	</h:commandLink>
			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				</f:facet>
				<h:outputText value="#{message.message.createdBy}"/>
			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_date}" />
				</f:facet>
				<h:outputText value="#{message.message.created}"/>
			</h:column>
		<%--TODO:// Implement me:
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_label}" />
				</f:facet>
				<h:outputText value="#{message.message.label}"/>
			</h:column>  	--%>
		</h:dataTable>
	</h:form>
</sakai:view>
</f:view>
