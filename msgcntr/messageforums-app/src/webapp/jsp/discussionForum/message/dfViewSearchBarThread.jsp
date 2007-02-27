

<h:panelGrid columns="3" styleClass="navPanel" width="100%" summary="layout">
  <h:panelGroup styleClass="specialLink"  style="float:none;text-align:center">  
		 <h:commandLink action="#{ForumTool.processActionMarkAllAsRead}" id="markAllAsread"  value="#{msgs.cdfm_mark_all_as_read}"
				rendered="#{ForumTool.selectedTopic.isMarkAsRead}" title="#{msgs.cdfm_mark_all_as_read}"> 
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		 </h:commandLink>
  </h:panelGroup>

  	<h:panelGroup styleClass="itemNav" style="float:none;text-align:center;">
  		<h:commandButton action="#{ForumTool.processDfMsgReplyThread}" value="#{msgs.cdfm_reply_thread}" 
			rendered="#{ForumTool.selectedTopic.isNewResponse}" accesskey="n" />
	</h:panelGroup>
		
   
    <h:panelGroup styleClass="specialLink" style="text-align: right;float:right">
    	<h:outputText value="View By: " />
    	<h:selectOneMenu id="select_label" onchange="this.form.submit();"  valueChangeListener="#{ForumTool.processValueChangedForMessageOrganize}" value="#{ForumTool.selectedMessageView}">
			<f:selectItem itemValue="thread" itemLabel="#{msgs.msg_organize_thread}" />
   			<f:selectItem itemValue="date" itemLabel="#{msgs.msg_organize_date_asc}" />
   			<f:selectItem itemValue="date_desc" itemLabel="#{msgs.msg_organize_date_desc}" />
			<f:selectItem itemValue="unread" itemLabel="#{msgs.msg_organize_unread}" />
	  </h:selectOneMenu>
	</h:panelGroup>
</h:panelGrid>
  

<h:messages styleClass="alertMessage" id="errorMessages"  />
