

<h:panelGrid columns="3" styleClass="navPanel" width="100%" summary="layout">
  <h:panelGroup styleClass="specialLink"  style="float:none;text-align:center">  
		 <h:commandLink action="#{ForumTool.processActionMarkAllAsRead}" id="markAllAsread"  value="#{msgs.cdfm_mark_all_as_read}"
				rendered="#{ForumTool.selectedTopic.isMarkAsRead}" title="#{msgs.cdfm_mark_all_as_read}"> 
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		 </h:commandLink>
  </h:panelGroup>

  	<h:panelGroup styleClass="itemNav" style="float:none;text-align:center;">
  		<h:commandButton action="#{ForumTool.processDfMsgReplyMsgFromEntire}" value="#{msgs.cdfm_reply_thread}" 
			rendered="#{ForumTool.selectedTopic.isNewResponse}" accesskey="n" >
	        <f:param value="#{ForumTool.selectedThreadHead.message.id}" name="messageId"/>
 			<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
	    </h:commandButton>
	</h:panelGroup>
		
   
    <h:panelGroup styleClass="specialLink" style="text-align: right;float:right">
    	<h:outputText value="View By:" />
    	<h:selectOneMenu id="select_label" onchange="this.form.submit();"  valueChangeListener="#{ForumTool.processValueChangedForMessageOrganize}" value="#{ForumTool.selectedMessageView}">
   			<f:selectItem itemValue="date" itemLabel="#{msgs.msg_organize_date}" />
			<f:selectItem itemValue="thread" itemLabel="#{msgs.msg_organize_thread}" />
	  </h:selectOneMenu>
	</h:panelGroup>
</h:panelGrid>
  

<h:messages styleClass="alertMessage" id="errorMessages"  />
