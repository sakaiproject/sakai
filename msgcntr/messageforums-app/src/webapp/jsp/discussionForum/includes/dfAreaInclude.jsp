<!--jsp/discussionForum/area/dfAreaInclude.jsp-->
<h:outputText styleClass="instruction"  value="#{msgs.cdfm_forum_noforums}"  rendered="#{empty ForumTool.forums}"/>
<h:dataTable id="forums" value="#{ForumTool.forums}" rendered="#{!empty ForumTool.forums}"  width="100%" var="forum" cellpadding="0" cellspacing="0" summary="layout" styleClass="specialLink">
    <h:column rendered="#{! forum.nonePermission}">
		<h:panelGrid columns="1" summary="layout" styleClass="forumHeader">
  	    <h:panelGroup>
				<%-- link to forum and decorations --%>
				<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{forum.forum.draft == 'true'}"/>
				<h:outputText id="draft_space" value=" -  " rendered="#{forum.forum.draft == 'true'}" styleClass="title"/>
				<%-- locked marker --%>
				<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{forum.locked == 'true'}" style="margin-right:.5em"/>
				<h:commandLink action="#{ForumTool.processActionDisplayForum}"  value="#{forum.forum.title}" title=" #{forum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}"  styleClass="title">
		        <f:param value="#{forum.forum.id}" name="forumId"/>
	        </h:commandLink>
				<h:outputText value="#{forum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"  styleClass="title" />
				<%-- links to act on this forum --%>
				
				<h:outputText id="forum_moderated" value=" #{msgs.cdfm_forum_moderated_flag}" styleClass="textPanelFooter" rendered="#{forum.moderated == 'true'}" />
				<h:outputText value=" "  styleClass="actionLinks"/>
			  <h:commandLink action="#{ForumTool.processActionNewTopic}" value="#{msgs.cdfm_new_topic}" rendered="#{forum.newTopic}" title="#{msgs.cdfm_new_topic}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>
	      </h:commandLink>
		  <h:outputText  value=" | " rendered="#{forum.changeSettings}"/><%-- gsilver: hiding the pipe when user does not have the ability to change the settings --%>
	   	  <h:commandLink action="#{ForumTool.processActionForumSettings}"  value="#{msgs.cdfm_forum_settings}" rendered="#{forum.changeSettings}" title="#{msgs.cdfm_forum_settings}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>				
	      </h:commandLink>

				<h:outputText  value=" | " rendered="#{forum.changeSettings}"/>

				<h:commandLink id="delete" action="#{ForumTool.processActionDeleteForumMainConfirm}" value="#{msgs.cdfm_button_bar_delete}" rendered="#{forum.changeSettings}"
						accesskey="d">
					<f:param value="#{forum.forum.id}" name="forumId"/>
				</h:commandLink>
		
				<%--//designNote: delete this forum link, a string now, with a fake rendered attribute - needs a real one --%>
				<%--
				<h:outputText  value=" | "   rendered="#{forum.changeSettings}"/>
				<h:outputText  value=" Delete "  rendered="#{forum.changeSettings}" styleClass="todo"/>
				--%>
<%-- the forum details --%>
				<h:outputText value="#{forum.forum.shortDescription}" styleClass="shortDescription"/>
	  
				<h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_view}"  styleClass="show"
						rendered="#{!empty forum.attachList || forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"
						onclick="resize();$(this).next('.hide').toggle(); $('div.toggle:first', $(this).parents('table.forumHeader')).slideToggle(resize);$(this).toggle();">
					<h:graphicImage url="/images/collapse.gif" /><h:outputText value="#{msgs.cdfm_view}" />
					<h:outputText value=" #{msgs.cdfm_full_description}"  rendered="#{forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_and}"  rendered="#{!empty forum.attachList && forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_attach}"  rendered="#{!empty forum.attachList}"/>
			  </h:outputLink>

				<%--//designNote: these link always show up even after you  have "zeroed out" a long description because it always saves a crlf --%>
				<h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide}" style="display:none;" styleClass="hide" 
						onclick="resize();$(this).prev('.show').toggle(); $('div.toggle:first', $(this).parents('table.forumHeader')).slideToggle(resize);$(this).toggle();">
					<h:graphicImage url="/images/expand.gif"/> <h:outputText value="#{msgs.cdfm_hide}" />
					<h:outputText value=" #{msgs.cdfm_full_description}"  rendered="#{forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_and}"  rendered="#{!empty forum.attachList && forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_attach}"  rendered="#{!empty forum.attachList}"/>
			  </h:outputLink>
				<f:verbatim><div class="toggle" style="display:none;padding-left:1em"></f:verbatim>
					<mf:htmlShowArea value="#{forum.forum.extendedDescription}"  hideBorder="true" />
					<%-- attachs --%>
					<h:dataTable  value="#{forum.attachList}" var="eachAttach" rendered="#{!empty forum.attachList}" columnClasses="attach,bogus"  summary="layout" style="font-size:.9em;width:auto;margin-left:1em" border="0" cellpadding="3" cellspacing="0">
			<h:column>
			<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
			<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />				
		  </h:column>
		  <h:column>
							<%--
								<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
					<h:outputText value="#{eachAttach.attachmentName}"  />
								</h:outputLink>
							--%>
				<h:outputLink value="#{eachAttach.url}" target="_blank">
					<h:outputText value="#{eachAttach.attachment.attachmentName}"  />
				</h:outputLink>			
			</h:column>	
	  </h:dataTable>
				<f:verbatim></div></f:verbatim>
	  </h:panelGroup>
  </h:panelGrid>
	  <%-- the topic list  --%>
		<%--//designNote: need a rendered atttrib for the folowing predicated on the existence of topics in this forum--%>
		<h:dataTable id="topics" rendered="#{!empty forum.topics}" value="#{forum.topics}" var="topic"  width="100%"   cellspacing="0" cellpadding="0" summary="layout">
		   <h:column rendered="#{! topic.nonePermission}">
					<h:panelGrid columns="1" summary="layout" width="100%" styleClass="specialLink topicBloc" cellpadding="0" cellspacing="0">
		      	<h:panelGroup>
							
							<h:graphicImage url="/images/folder.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages == 0 }" styleClass="topicIcon" style="margin-right:.5em"/>
							<h:graphicImage url="/images/folder_unread.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages > 0 }" styleClass="topicIcon" style="margin-right:.5em"/>
							<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
							<h:outputText id="draft_space" value="  - " rendered="#{topic.topic.draft == 'true'}" styleClass="title"/>
							<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{forum.locked == 'true' || topic.locked == 'true'}" style="margin-right:.5em"/>
							<h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{topic.topic.title}" title=" #{topic.topic.title}" styleClass="title">
					      <f:param value="#{topic.topic.id}" name="topicId"/>
					      <f:param value="#{forum.forum.id}" name="forumId"/>
				      </h:commandLink>
							<%-- // display singular ('message') if one message --%>
				     <h:outputText styleClass="textPanelFooter" id="topic_msg_count55" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" 
								rendered="#{topic.isRead && topic.totalNoMessages == 1}"/>
							<%-- // display plural ('messages') if 0 or more than 1 messages --%>
					   <h:outputText id="topic_msg_count56" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" 
								rendered="#{topic.isRead && (topic.totalNoMessages > 1 || topic.totalNoMessages == 0) }" styleClass="textPanelFooter" />
				     <h:outputText id="topic_moderated" value="#{msgs.cdfm_topic_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true' && topic.isRead}" />
    	        <h:outputText value=" #{msgs.cdfm_closeb}"styleClass="textPanelFooter" rendered="#{topic.isRead}"/>
							<%--//desNote: only show the new "new" message if there are no unread messages --%>
							<h:outputText styleClass="childrenNew" value=" #{msgs.cdfm_newflagparent}"  rendered="#{topic.unreadNoMessages > 0 }" />

							<%--//desNote: links to act on this topic --%>
							<h:outputText value=" "  styleClass="actionLinks"/>
    	         <h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" rendered="#{topic.changeSettings}"
    	                        title=" #{msgs.cdfm_topic_settings}">
					     <f:param value="#{topic.topic.id}" name="topicId"/>
				       <f:param value="#{forum.forum.id}" name="forumId"/>
				     </h:commandLink>
							
							<h:outputText  value=" | " rendered="#{topic.changeSettings}"/>
							
							<h:commandLink action="#{ForumTool.processActionDeleteTopicMainConfirm}" id="delete_confirm" value="#{msgs.cdfm_button_bar_delete}" accesskey="d" rendered="#{topic.changeSettings}"
							title=" #{msgs.cdfm_topic_settings}">
									<f:param value="#{topic.topic.id}" name="topicId"/>
									<f:param value="#{forum.forum.id}" name="forumId"/>
							</h:commandLink>
							
							
							<%-- delete this topic  link, a string now - needs a real rendered attribute --%>
							<%--
							<h:outputText  value=" | " rendered="#{topic.changeSettings}"/>
							<h:outputText  value=" Delete " rendered="#{topic.changeSettings}" styleClass="todo"/>
							--%>
							<%--the topic details --%>
							<h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" styleClass="shortDescription" />
							
							<h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_view}" styleClass="show"
									rendered="#{!empty topic.attachList || topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"
									onclick="resize();$(this).next('.hide').toggle(); $('td div.toggle', $(this).parents('tr:first').next('tr')).slideToggle(resize);$(this).toggle();">
									<h:graphicImage url="/images/collapse.gif"/><h:outputText value="#{msgs.cdfm_view}" />
									<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList && topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
				    </h:outputLink>  
				  
							<h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide}" style="display:none " styleClass="hide" 
									rendered="#{!empty topic.attachList || topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"
									onclick="resize();$(this).prev('.show').toggle(); $('td div.toggle', $(this).parents('tr:first').next('tr')).slideToggle(resize);$(this).toggle();">
									<h:graphicImage url="/images/expand.gif"/><h:outputText value="#{msgs.cdfm_hide}" />
									<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList && topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
				    </h:outputLink>

				 </h:panelGroup>
						<h:panelGroup>
							<f:verbatim><div class="toggle" style="display:none;padding-left:1em"></f:verbatim>
					<mf:htmlShowArea  id="topic_fullDescription" hideBorder="true"	 value="#{topic.topic.extendedDescription}" />
								<%--//desNote:attach list --%>
								<h:dataTable  value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" cellpadding="3" cellspacing="0" columnClasses="attach,bogus" summary="layout"  style="font-size:.9em;width:auto;margin-left:1em" border="0">
					  <h:column>
										<h:graphicImage url="/images/attachment.gif"/>
<%--						<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
							<h:outputText value="#{eachAttach.attachmentName}" />
						</h:outputLink>--%>
										<h:outputText value=" " />
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value="#{eachAttach.attachment.attachmentName}" />
						</h:outputLink>				  
					</h:column>
			  </h:dataTable>
			  <%-- gsilver: need a render attribute on the dataTable here to avoid putting an empty table in response -- since this looks like a stub that was never worked out to display the messages inside this construct - commenting the whole thing out.
			 <h:dataTable styleClass="indnt2" id="messages" value="#{topics.messages}" var="message">
			  <h:column>
					<h:outputText id="message_title" value="#{message.message.title}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText id="message_desc" value="#{message.message.shortDescription}" />
			  </h:column>
			</h:dataTable>
			--%>
    <f:verbatim></div></f:verbatim>
						</h:panelGroup>

					</h:panelGrid>
	 </h:column>
      </h:dataTable>			
	  </h:column>
  </h:dataTable>