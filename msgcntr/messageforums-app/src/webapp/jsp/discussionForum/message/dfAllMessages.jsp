<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
<sakai:view>
	<h:form id="DF-1">
		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
		<div class="header-section">
			<div class="left-header-section">
				<h3><h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
					<h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" >
						<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					</h:commandLink> /
					<h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
				</h3>
				 <sakai:instruction_message value="#{ForumTool.selectedTopic.topic.shortDescription}" />
				<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}"
					id="topic_extended_show" value="#{msgs.cdfm_read_full_description}">
					<f:param value="#{topic.topic.id}" name="topicId"/>
					<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
				</h:commandLink>
				<%--<h:inputTextarea rows="5" cols="100" id="topic_extended_description" disabled="true" value="#{ForumTool.selectedTopic.topic.extendedDescription}" rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>--%>
				 <sakai:inputRichText rows="5" cols="110" buttonSet="none"  readonly="true" showXPath="false" id="topic_extended_description" value="#{ForumTool.selectedTopic.topic.extendedDescription}" rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>
				<f:verbatim><br/></f:verbatim>
				<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" id="topic_extended_hide"
					 value="#{msgs.cdfm_hide_full_description}" rendered="#{ForumTool.selectedTopic.readFullDesciption}">
					<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
					<f:param value="processActionDisplayTopic" name="redirectToProcessAction"/>
				</h:commandLink>
			</div>
			<div class="right-header-section">
				<h:outputText   value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
				<h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" >
					<f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				</h:commandLink>
				<h:outputText  id="blankSpace1" value="  " />				
				<h:outputText   value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
				<h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" >
					<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				</h:commandLink>
			</div>
		</div>
		<%@include file="dfViewSearchBar.jsp"%>		
   	<h:dataTable styleClass="listHier" id="messages" value="#{ForumTool.selectedTopic.messages}" var="message" rendered="#{!ForumTool.threaded}"
   	 columnClasses="checkboxCol,attachmentCol,subjectCol,authorCol,dateCol">
			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:commandLink action="#{ForumTool.processCheckAll}" value="#{msgs.cdfm_checkall}" />
				</f:facet>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{!message.read}"/>
			</h:column>
			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif"/>
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{message.hasAttachment && message.read && !ForumTool.displayUnreadOnly}"/>
				<h:graphicImage value="/images/attachment.gif" rendered="#{message.hasAttachment && !message.read}"/>
			</h:column>
			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_subject}" />
				</f:facet>
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true">
				   	<h:outputText value="#{message.message.title}" rendered="#{message.read && !ForumTool.displayUnreadOnly}" />
    	        	<h:outputText style="font-weight:bold;" value="#{message.message.title}" rendered="#{!message.read}"/>
        	    	<f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	          	</h:commandLink>
			</h:column>

			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				</f:facet>
				 	<h:outputText value="#{message.message.createdBy}" rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
    	        	<h:outputText style="font-weight:bold;" value="#{message.message.createdBy}" rendered="#{!message.read}"/>

			</h:column>

			<h:column rendered="#{!ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_date}" />
				</f:facet>
				 	<h:outputText value="#{message.message.created}" rendered="#{message.read && !ForumTool.displayUnreadOnly}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
				 	</h:outputText>
    	        	<h:outputText style="font-weight:bold;" value="#{message.message.created}" rendered="#{!message.read}">
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
					<h:commandLink action="#{ForumTool.processCheckAll}" value="#{msgs.cdfm_checkall}" />
				</f:facet>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
				<h:selectBooleanCheckbox value="#{message.selected}"  rendered="#{!message.read}"/>
			</h:column>
			<h:column rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:graphicImage value="/images/attachment.gif"/>
				</f:facet>
				<h:graphicImage value="/images/attachment.gif" rendered="#{message.hasAttachment}"/>
			</h:column>
			<h:column id="_msg_subject" rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_subject}" />
				</f:facet>
				<h:commandLink action="#{ForumTool.processActionDisplayMessage}" immediate="true">
				   	<h:outputText value="#{message.message.title}" rendered="#{message.read && !ForumTool.displayUnreadOnly}" />
    	        	<h:outputText style="font-weight:bold;" value="#{message.message.title}" rendered="#{!message.read}"/>
        	    	<f:param value="#{message.message.id}" name="messageId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	          	</h:commandLink>
			</h:column>

			<h:column rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				</f:facet>
				 	<h:outputText value="#{message.message.createdBy}" rendered="#{message.read && !ForumTool.displayUnreadOnly}"/>
    	        	<h:outputText style="font-weight:bold;" value="#{message.message.createdBy}" rendered="#{!message.read}"/>

			</h:column>

			<h:column rendered="#{ForumTool.threaded}">
				<f:facet name="header">
					<h:outputText value="#{msgs.cdfm_date}" />
				</f:facet>
				 	<h:outputText value="#{message.message.created}" rendered="#{message.read && !ForumTool.displayUnreadOnly}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
					</h:outputText>
    	        	<h:outputText style="font-weight:bold;" value="#{message.message.created}" rendered="#{!message.read}">
					 	<f:convertDateTime pattern="#{msgs.date_format}" />
				    </h:outputText>    	        	

			</h:column>
		</mf:hierDataTable>
	</h:form>
</sakai:view>
</f:view>
