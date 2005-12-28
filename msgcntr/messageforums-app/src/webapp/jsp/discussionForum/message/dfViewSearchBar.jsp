<h4>
	<div style="width:100%">
		<div   style="float:left;width:49%;" >View &nbsp;&nbsp;
			<h:selectOneMenu  onchange="this.form.submit();"  valueChangeListener="#{ForumTool.processValueChangeForMessageView}" value="#{ForumTool.selectedMessageView}">
				<f:selectItem itemValue="dfAllMessages" itemLabel="#{msgs.msg_view_all}" />
				<f:selectItem itemValue="dfThreadedView" itemLabel="#{msgs.msg_view_threaded}" />
				<f:selectItem itemValue="expand" itemLabel="#{msgs.msg_view_expanded}" />
				<f:selectItem itemValue="collapse" itemLabel="#{msgs.msg_view_collapsed}" />
				<f:selectItem itemValue="dfUnreadView" itemLabel="#{msgs.msg_view_unread}" />
				<%--<f:selectItem itemValue="label" itemLabel="#{msgs.msg_view_bylabel}" />--%>
			</h:selectOneMenu>
		</div>
		<div  style="float:right;text-align:right;width:49%;">
			<%--<h:inputText  value="#{ForumTool.searchText}" />&nbsp;&nbsp;&nbsp;&nbsp;
		    <h:commandButton  value="Search" action="#{ForumTool.processActionSearch}" onkeypress="document.forms[0].submit;"/>--%>
		</div>
	</div>
</h4>
<div style="width:100%;">
	<div   style="float:left;width:62%;" >
	 <div   style="float:left;width:79%;" >
	 	<h:commandLink action="#{ForumTool.processActionMarkCheckedAsRead}" id="markAsread"  value="#{msgs.cdfm_mark_check_as_read}"
	 	  rendered="#{ForumTool.selectedTopic.isMarkAsRead}"> 
		<%--<h:commandLink action="#{ForumTool.processActionMarkCheckedAsUnread}" id="markAsunread"  value="#{msgs.cdfm_mark_check_as_unread}">--%>
			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
			<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		</h:commandLink>
	</div>
	<div   style="float:right;width:19%;" >
	 	<h:commandButton action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages" value="#{msgs.cdfm_container_title}"
	 	  rendered="#{ForumTool.selectedTopic.isNewResponse}"/>
	<%-- 	<h:commandButton action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages" value="#{msgs.cdfm_container_title}"/> --%>
	</div>
	</div>
	<div  style="float:right;text-align:right;width:30%;">
	   	<h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" styleClass="rightAlign" value="#{msgs.cdfm_topic_settings}" rendered="#{ForumTool.instructor}">
			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
			<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		</h:commandLink>
	</div>
</div>
<div style="width:100%;">
	<h:messages styleClass="alertMessage" id="errorMessages"  />
</div>