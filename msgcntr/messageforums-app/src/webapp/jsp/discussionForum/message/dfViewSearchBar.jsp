
<f:verbatim><br /></f:verbatim>
<h:panelGrid columns="3" styleClass="jsfFormTable" width="100%" summary="">
  <h:panelGroup>
    <h:outputLabel value="#{msgs.msg_view}" for="select_label" />
    <f:verbatim><h:outputText value=" " /></f:verbatim>
		<h:selectOneMenu id="select_label" onchange="this.form.submit();"  valueChangeListener="#{ForumTool.processValueChangeForMessageView}" value="#{ForumTool.selectedMessageView}">
			<f:selectItem itemValue="dfAllMessages" itemLabel="#{msgs.msg_view_all}" />
			<f:selectItem itemValue="dfThreadedView" itemLabel="#{msgs.msg_view_threaded}" />
			<f:selectItem itemValue="expand" itemLabel="#{msgs.msg_view_expanded}" />
			<f:selectItem itemValue="collapse" itemLabel="#{msgs.msg_view_collapsed}" />
			<f:selectItem itemValue="dfUnreadView" itemLabel="#{msgs.msg_view_unread}" />
			<%--<f:selectItem itemValue="label" itemLabel="#{msgs.msg_view_bylabel}" />--%>
	  </h:selectOneMenu>
	</h:panelGroup>
  <h:panelGroup>
		<%--<div>
			  <h:inputText  value="#{ForumTool.searchText}" />&nbsp;&nbsp;&nbsp;&nbsp;
		    <h:commandButton  value="Search" action="#{ForumTool.processActionSearch}" onkeypress="document.forms[0].submit;"/>
		</div>--%>

    <h:outputText value=" " />
  </h:panelGroup>
  <h:panelGroup>
    <f:verbatim><br /></f:verbatim>
  </h:panelGroup>
  
  <h:panelGroup>
    <h:commandLink action="#{ForumTool.processActionMarkCheckedAsRead}" id="markAsread"  value="#{msgs.cdfm_mark_check_as_read}"
 	                 rendered="#{ForumTool.selectedTopic.isMarkAsRead}" title="#{msgs.cdfm_mark_check_as_read}"> 

   		<%--<h:commandLink action="#{ForumTool.processActionMarkCheckedAsUnread}" id="markAsunread"  value="#{msgs.cdfm_mark_check_as_unread}">--%>
	    <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	  </h:commandLink>
  </h:panelGroup>
  <h:panelGroup>
    <h:commandButton action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages" value="#{msgs.cdfm_container_title}"
                   	  rendered="#{ForumTool.selectedTopic.isNewResponse}" accesskey="n"/>
          <%-- 	<h:commandButton action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages" value="#{msgs.cdfm_container_title}"/> --%>
  </h:panelGroup>
  <h:panelGroup styleClass="msgNav">
    	<h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" 
    	               rendered="#{ForumTool.instructor}" title=" #{msgs.cdfm_topic_settings}">
	    <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	    <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	  </h:commandLink>
  </h:panelGroup>
</h:panelGrid>

<h:messages styleClass="alertMessage" id="errorMessages"  />
