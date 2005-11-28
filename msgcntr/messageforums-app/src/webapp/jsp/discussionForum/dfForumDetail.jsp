<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
   <sakai:view>
      <h:form id="msgForum">
		<f:verbatim><div class="forumsRow"></f:verbatim>
		<h:commandLink action="#{ForumTool.processDisplayForum}"  value="#{ForumTool.selectedForum.forum.title}" /><f:verbatim></br></f:verbatim>
		<h:outputText id="forum_desc" value="#{ForumTool.selectedForum.forum.shortDescription}" />
		<f:verbatim></div></f:verbatim>
		 <h:dataTable id="topics" width="100%" value="#{ForumTool.selectedForum.topics}" var="topic">
			<h:column>
				<f:verbatim><div class="topicRows"></f:verbatim>
				<h:commandLink action="#{ForumTool.processDisplayTopic}" id="topic_title" value="#{topic.topic.title}">
					<f:param value="#{topic.topic.id}" name="topicId"/>
				</h:commandLink>
				<h:outputText id="topic_msg_count" value=" (#{topic.totalNoMessages} messages - #{topic.unreadNoMessages} unread)"/>
				<f:verbatim><br/></f:verbatim>
				<h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" />
				<f:verbatim></div></f:verbatim>
				<h:dataTable id="messages" value="#{topics.messages}" var="message">
					<h:column>
						<h:outputText id="message_title" value="#{message.message.title}"/>
						<f:verbatim><br/></f:verbatim>
						<h:outputText id="message_desc" value="#{message.message.shortDescription}" />
					</h:column>
				</h:dataTable>
			</h:column>
		</h:dataTable>
	 </h:form>
    </sakai:view>
</f:view>
