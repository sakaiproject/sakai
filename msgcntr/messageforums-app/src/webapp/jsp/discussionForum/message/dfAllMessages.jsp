<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
	<h:form id="DF-1">
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
		
			  <h:panelGrid columns="2" summary="" width="100%">
			    <h:panelGroup styleClass="breadCrumb">
      			  <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_topic_settings}"/>
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" title=" #{msgs.cdfm_topic_settings}">
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
					  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
				 </h:panelGroup>
				 <h:panelGroup styleClass="msgNav">
				   <h:outputText   value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" 
					                title=" #{msgs.cdfm_topic_settings}">
						 <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
						 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
					 <f:verbatim><h:outputText  id="blankSpace1" value="  " /></f:verbatim>				
					 <h:outputText   value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
					 <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" 
					                title=" #{msgs.cdfm_topic_settings}">
						<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					 </h:commandLink>
				 </h:panelGroup>
			  </h:panelGrid>
				 
				<sakai:instruction_message value="#{ForumTool.selectedTopic.topic.shortDescription}" />

        <h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}"
					id="topic_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
					<f:param value="#{topic.topic.id}" name="topicId"/>
					<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
				</h:commandLink>
				<%--<h:inputTextarea rows="5" cols="100" id="topic_extended_description" disabled="true" value="#{ForumTool.selectedTopic.topic.extendedDescription}" rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>--%>
				 <sakai:inputRichText rows="5" cols="110" buttonSet="none"  readonly="true" showXPath="false" id="topic_extended_description" value="#{ForumTool.selectedTopic.topic.extendedDescription}" rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>
				<f:verbatim><br /></f:verbatim>
				<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" id="topic_extended_hide"
					 value="#{msgs.cdfm_hide_full_description}" rendered="#{ForumTool.selectedTopic.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
					<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
					<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
				</h:commandLink>

		<%@include file="dfViewSearchBar.jsp"%>
				
   	<h:dataTable styleClass="listHier" id="messages" value="#{ForumTool.selectedTopic.messages}" var="message" rendered="#{!ForumTool.threaded}"
   	 columnClasses="checkboxCol,attachmentCol,subjectCol,authorCol,dateCol">
			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:commandLink action="#{ForumTool.processCheckAll}" value="#{msgs.cdfm_checkall}" title="#{msgs.cdfm_checkall}" />
				</f:facet>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{!message.read}"/>
			</h:column>
			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}"/>
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" rendered="#{message.hasAttachment && message.read && !ForumTool.displayUnreadOnly}"/>
				<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" rendered="#{message.hasAttachment && !message.read}"/>
			</h:column>
			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_subject}" />
				</f:facet>
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{message.message.title}">
				   	<h:outputText value="#{message.message.title}" rendered="#{message.read && !ForumTool.displayUnreadOnly}" />
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}"/>
        	    	<f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	          	</h:commandLink>
			</h:column>

			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				</f:facet>
				 	<h:outputText value="#{message.message.author}" rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.author}" rendered="#{!message.read}"/>

			</h:column>

			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_date}" />
				</f:facet>
				 	<h:outputText value="#{message.message.created}" rendered="#{message.read && !ForumTool.displayUnreadOnly}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
				 	</h:outputText>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
					</h:outputText>
			</h:column>
		<%--TODO:// Implement me:
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_label}" />
				</f:facet>
				<h:outputText value="#{message.message.label}" rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
    	        <h:outputText style="font-weight:bold;" value="#{message.message.label}" rendered="#{!message.read}"/>

			</h:column>  	--%>
		</h:dataTable>
		
		<mf:hierDataTable styleClass="listHier" id="messagesInHierDataTable" value="#{ForumTool.selectedTopic.messages}" var="message" rendered="#{ForumTool.threaded}" expanded="#{ForumTool.expanded}"
		                  columnClasses="checkboxCol,attachmentCol,subjectCol,authorCol,dateCol">
			<h:column rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:commandLink action="#{ForumTool.processCheckAll}" value="#{msgs.cdfm_checkall}" title="#{msgs.cdfm_checkall}"/>
				</f:facet>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{!message.read}"/>
			</h:column>
			<h:column rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif" alt="#{msgs.msg_has_attach}" />
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{message.hasAttachment}" alt="#{msgs.msg_has_attach}"/>
			</h:column>
			<h:column id="_msg_subject" rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_subject}" />
				</f:facet>
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{message.message.title}">
				   	<h:outputText value="#{message.message.title}" rendered="#{message.read && !ForumTool.displayUnreadOnly}" />
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}"/>
        	    	<f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	          	</h:commandLink>
			</h:column>

			<h:column rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				</f:facet>
				 	<h:outputText value="#{message.message.author}" rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.author}" rendered="#{!message.read}"/>

			</h:column>

			<h:column rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_date}" />
				</f:facet>
				 	<h:outputText value="#{message.message.created}" rendered="#{message.read && !ForumTool.displayUnreadOnly}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
					</h:outputText>
    	        	<h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
				    </h:outputText>    	        	

			</h:column>
		</mf:hierDataTable>
	</h:form>
</sakai:view>
</f:view>
