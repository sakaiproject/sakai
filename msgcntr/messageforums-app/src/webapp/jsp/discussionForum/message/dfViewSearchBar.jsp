
<f:verbatim><br /></f:verbatim>
<!--jsp/discussionForum/message/dfViewSearchBar.jsp-->
<%--
<h:panelGrid columns="6" styleClass="navPanel" width="500px" summary="layout">
--%>
  <%--
  <h:panelGroup styleClass="viewNav">
    <h:outputLabel value="#{msgs.msg_view}" for="select_label" />
		<h:selectOneMenu id="select_label" onchange="this.form.submit();"  valueChangeListener="#{ForumTool.processValueChangeForMessageView}" value="#{ForumTool.selectedMessageView}">
			<f:selectItem itemValue="dfAllMessages" itemLabel="#{msgs.msg_view_all}" />
			<f:selectItem itemValue="dfExpandAllView" itemLabel="#{msgs.msg_expand_all}" />
			<f:selectItem itemValue="dfThreadedView" itemLabel="#{msgs.msg_view_threaded}" />
			<f:selectItem itemValue="expand" itemLabel="#{msgs.msg_view_expanded}" />
			<f:selectItem itemValue="collapse" itemLabel="#{msgs.msg_view_collapsed}" />
			<f:selectItem itemValue="dfUnreadView" itemLabel="#{msgs.msg_view_unread}" />
			<f:selectItem itemValue="label" itemLabel="#{msgs.msg_view_bylabel}" />
	  </h:selectOneMenu>
	</h:panelGroup>
   --%>
   
   <%--
	<h:panelGroup styleClass="msgNav">
   		<h:outputText value="#{msgs.msg_view}" />
    </h:panelGroup>
    <h:panelGroup>
   		<h:selectOneRadio id="select_label" onchange="this.form.submit();" layout="pageDirection"
   			valueChangeListener="#{ForumTool.processValueChangeForMessageView}" value="#{ForumTool.selectedMessageView}">
   			<f:selectItem itemValue="dfAllMessages" itemLabel="#{msgs.msg_view_all}" />
   			<f:selectItem itemValue="dfUnreadView" itemLabel="#{msgs.msg_view_unread}" />
   		</h:selectOneRadio>
    </h:panelGroup>
    <h:panelGroup styleClass="msgNav"> 
   		<h:outputText value="#{msgs.msg_show}" />
    </h:panelGroup>
    <h:panelGroup>
		<h:selectOneRadio id="show_label" onchange="this.form.submit();" layout="pageDirection"
			valueChangeListener="#{ForumTool.processValueChangedForMessageShow}" value="#{ForumTool.selectedMessageShow}">
			<f:selectItem itemValue="dfSubjectOnly" itemLabel="#{msgs.msg_show_subject}" />
			<f:selectItem itemValue="dfEntireMsg" itemLabel="#{msgs.msg_show_entire}" />
		</h:selectOneRadio>
    </h:panelGroup>
    <h:panelGroup styleClass="msgNav">
   		<h:outputText value="#{msgs.msg_organize}" />
    </h:panelGroup>
    <h:panelGroup>
    	<h:selectOneRadio id="organize_label" onchange="this.form.submit();" layout="pageDirection"
    		valueChangeListener="#{ForumTool.processValueChangedForMessageOrganize}" value="#{ForumTool.selectedMessageOrganize}">
    			<f:selectItem itemValue="thread" itemLabel="#{msgs.msg_organize_thread}" />
    			<f:selectItem itemValue="date" itemLabel="#{msgs.msg_organize_date}" />
    	</h:selectOneRadio>
    </h:panelGroup>
</h:panelGrid>
--%>

  <%--<h:panelGroup>
		<div>
			  <h:inputText  value="#{ForumTool.searchText}" />&nbsp;&nbsp;&nbsp;&nbsp;
		    <h:commandButton  value="Search" action="#{ForumTool.processActionSearch}" onkeypress="document.forms[0].submit;"/>
		</div>

    <h:outputText value=" " />
  </h:panelGroup>
  <h:panelGroup>
    <f:verbatim><br /></f:verbatim>
  </h:panelGroup>
  --%>
<h:panelGrid columns="2" styleClass="navPanel" width="100%">
  <%--
  <h:panelGroup styleClass="specialLink"  style="float:none;text-align:center">  
		  <h:commandLink action="#{ForumTool.processActionMarkCheckedAsNotRead}" id="markAsNotRead"  value="#{msgs.cdfm_mark_check_as_not_read}"
				rendered="#{ForumTool.selectedTopic.isMarkAsNotRead && !ForumTool.expandedView}" title="#{msgs.cdfm_mark_check_as_not_read}">
  --%>
				<%--<h:commandLink action="#{ForumTool.processActionMarkCheckedAsUnread}" id="markAsunread"  value="#{msgs.cdfm_mark_check_as_unread}">--%>
  <%--
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		 </h:commandLink>
		 <h:commandLink action="#{ForumTool.processActionMarkAllAsNotRead}" id="markAllAsNotRead"  value="#{msgs.cdfm_mark_all_as_not_read}"
				rendered="#{ForumTool.selectedTopic.isMarkAsNotRead && ForumTool.expandedView}" title="#{msgs.cdfm_mark_all_as_not_read}"> 
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		 </h:commandLink>
  </h:panelGroup>
	--%>

  	<h:panelGroup styleClass="itemNav" style="float:none;text-align:center;">
  		  <h:commandButton action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages" value="#{msgs.cdfm_container_title}"
		  rendered="#{ForumTool.selectedTopic.isNewResponse}" accesskey="n"/>
		</h:panelGroup>
          <%-- 	<h:commandButton action="#{ForumTool.processAddMessage}" id="df_compose_message_dfAllMessages" value="#{msgs.cdfm_container_title}"/> --%>
		  <h:panelGroup styleClass="specialLink" style="text-align: right;float:right">
			  <h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" 
				rendered="#{ForumTool.selectedTopic.changeSettings}" title=" #{msgs.cdfm_topic_settings}">
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
			</h:commandLink>
		</h:panelGroup>
  </h:panelGrid>
  

<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
