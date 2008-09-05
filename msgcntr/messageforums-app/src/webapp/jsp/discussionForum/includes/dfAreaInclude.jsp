<!--jsp/discussionForum/area/dfArea.jsp-->
  <h:dataTable id="forums" value="#{ForumTool.forums}" width="100%" var="forum" cellpadding="0" cellspacing="0" summary="layout">
    <h:column rendered="#{! forum.nonePermission}">
    <f:verbatim><div class="hierItemBlockWrapper"></f:verbatim>
    <h:panelGrid columns="2" styleClass="hierItemBlock specialLink" columnClasses="bogus,itemAction" summary="layout" style="width:100%;">
  	    <h:panelGroup>
  	      <h:outputText styleClass="highlight" id="draft" value="#{msgs.cdfm_draft}" rendered="#{forum.forum.draft == 'true'}"/>
	      <h:outputText id="draft_space" value=" -  " rendered="#{forum.forum.draft == 'true'}"/>
		  
	
	      <h:graphicImage url="/images/lock.gif" alt="#{msgs.cdfm_forum_locked}"  rendered="#{forum.locked == 'true'}"/>
	      <h:outputText id="emptyspace" value="  " rendered="#{forum.locked == 'true'}"/>
	      <f:verbatim><h4></f:verbatim>
	        <h:commandLink action="#{ForumTool.processActionDisplayForum}"  value="#{forum.forum.title}" title=" #{forum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}">
		        <f:param value="#{forum.forum.id}" name="forumId"/>
	        </h:commandLink>
	        <h:outputText value="#{forum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
	      <f:verbatim></h4></f:verbatim>
	  	  </h:panelGroup>
	  	  <h:panelGroup styleClass="msgNav">
			  <h:commandLink action="#{ForumTool.processActionNewTopic}" value="#{msgs.cdfm_new_topic}" rendered="#{forum.newTopic}" title="#{msgs.cdfm_new_topic}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>
	      </h:commandLink>
		  <h:outputText  value=" | " rendered="#{forum.changeSettings}"/><%-- gsilver: hiding the pipe when user does not have the ability to change the settings --%>
	   	  <h:commandLink action="#{ForumTool.processActionForumSettings}"  value="#{msgs.cdfm_forum_settings}" rendered="#{forum.changeSettings}" title="#{msgs.cdfm_forum_settings}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>				
	      </h:commandLink>
  	    </h:panelGroup>


		<h:panelGroup>
		

<%-- the forum details --%>
	  
      	<f:verbatim><div></f:verbatim>
		  <f:verbatim><p class="textPanel"></f:verbatim>
		  <h:outputText value="#{forum.forum.shortDescription}" />
		  <f:verbatim></p></f:verbatim>
		  <f:verbatim><p class="textPanelFooter specialLink"></f:verbatim>

				<%--gsilver: would be good if the returned url from this would include a named internal anchor as the target so that the expando/collapso would go to the top of the viewport and avoid having to scroll and find --%>
			  <h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_read_full_description}" styleClass="show" 
			  		rendered="#{forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null}"
			  		onclick="toggleExtendedDescription($(this).next('.hide'), $('div.toggle:first', $(this).parents('div:first')), $(this));">  
			  		<h:outputText value="#{msgs.cdfm_read_full_description}" />
			  </h:outputLink>

			  <h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide_full_description}" style="display:none" styleClass="hide" 
			  		onclick="toggleExtendedDescription($(this).prev('.show'), $('div.toggle:first', $(this).parents('div:first')), $(this));">
			  		<h:outputText value="#{msgs.cdfm_hide_full_description}" />
			  </h:outputLink>

		    
		<f:verbatim></p></f:verbatim>

			 	<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
				<mf:htmlShowArea value="#{forum.forum.extendedDescription}"  
		                     hideBorder="true" />

				<f:verbatim></div></f:verbatim>
		  
		<f:verbatim></div></f:verbatim>
		
	  <h:dataTable  value="#{forum.attachList}" var="eachAttach" rendered="#{!empty forum.attachList}" columnClasses="attach,bogus" styleClass="listHier" summary="layout">
			<h:column>
			<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
			<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />				
		  </h:column>
		  <h:column>
<%--				<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
					<h:outputText value="#{eachAttach.attachmentName}"  />
				</h:outputLink>--%>
				<h:outputLink value="#{eachAttach.url}" target="_blank">
					<h:outputText value="#{eachAttach.attachment.attachmentName}"  />
				</h:outputLink>			
			</h:column>	
	  </h:dataTable>
	  
	  </h:panelGroup>
  </h:panelGrid>

	  <%-- the topic list  --%>
	  
	  
	  	<%--gsilver: need a rendered atttrib for the folowing predicated on the existence of topics in this forum--%>
		 <h:dataTable id="topics" value="#{forum.topics}" var="topic" width="100%" styleClass="topicBloc"  cellspacing="0" cellpadding="0" summary="layout">
		   <h:column rendered="#{! topic.nonePermission}">
				<f:verbatim><div class="hierItemBlockChild"></f:verbatim>
		      <h:panelGrid columns="2" summary="layout" width="100%" styleClass="specialLink" cellpadding="0" cellspacing="0" columnClasses="bogus,itemAction">
		      	<h:panelGroup>
		      	<h:graphicImage url="/images/folder.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages == 0 }" />
					      <h:graphicImage url="/images/folder_unread.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages > 0 }" />
					  <f:verbatim>&nbsp;&nbsp;</f:verbatim>
						<h:outputText styleClass="highlight" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
				      <h:outputText id="draft_space" value="  - " rendered="#{topic.topic.draft == 'true'}"/>
				      <h:graphicImage url="/images/lock.gif" alt="#{msgs.cdfm_forum_locked}"  rendered="#{forum.locked == 'true' || topic.locked == 'true'}"/>
				      <h:outputText id="emptyspace" value="  " rendered="#{topic.locked == 'true'}"/>
						<f:verbatim><h5></f:verbatim>
				      <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{topic.topic.title}" title=" #{topic.topic.title}">
					      <f:param value="#{topic.topic.id}" name="topicId"/>
					      <f:param value="#{forum.forum.id}" name="forumId"/>
				      </h:commandLink>
					  <f:verbatim></h5></f:verbatim>
				     <h:outputText styleClass="textPanelFooter" id="topic_msg_count55" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" 
				                    rendered="#{topic.isRead && topic.totalNoMessages < 2}"/>
					   <h:outputText id="topic_msg_count56" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs} - #{topic.unreadNoMessages} #{msgs.cdfm_unread}" 
				                    rendered="#{topic.isRead && topic.totalNoMessages > 1}" styleClass="textPanelFooter" />
				     <h:outputText id="topic_moderated" value="#{msgs.cdfm_topic_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true' && topic.isRead}" />
    	        <h:outputText value=" #{msgs.cdfm_closeb}"styleClass="textPanelFooter" rendered="#{topic.isRead}"/>
    	        </h:panelGroup>
    	        <h:panelGroup styleClass="msgNav">
    	         <h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" rendered="#{topic.changeSettings}"
    	                        title=" #{msgs.cdfm_topic_settings}">
					     <f:param value="#{topic.topic.id}" name="topicId"/>
				       <f:param value="#{forum.forum.id}" name="forumId"/>
				     </h:commandLink>
    	        </h:panelGroup>
    	      </h:panelGrid>
				<h:panelGrid columns="1" width="100%"  cellpadding="0" cellspacing="0" summary="layout">
    	        <h:panelGroup styleClass="textPanel">
    	          <h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" />
    	        </h:panelGroup>
			    <h:panelGroup styleClass="textPanelFooter specialLink">
			    	<h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_read_full_description}" styleClass="show" 
			    		rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null}"
				  		onclick="toggleExtendedDescription($(this).next('.hide'), $('td div.toggle:first', $(this).parents('tr:first').next('tr')), $(this));">
				  		<h:outputText value="#{msgs.cdfm_read_full_description}" />
				    </h:outputLink>  
				  
				    <h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide_full_description}" style="display:none" styleClass="hide" 
				    	rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null}"
				  		onclick="toggleExtendedDescription($(this).prev('.show'), $('td div.toggle:first', $(this).parents('tr:first').next('tr')), $(this));">
				  		<h:outputText value="#{msgs.cdfm_hide_full_description}" />
				    </h:outputLink>

				 </h:panelGroup>
				<%--	gsilver: show the text, not the editor... --%>
				<h:panelGroup styleClass="textPanel">
					<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
					<mf:htmlShowArea  id="topic_fullDescription" hideBorder="true"	 value="#{topic.topic.extendedDescription}" />
		 			<%--  <sakai:inputRichText rows="5" cols="110" buttonSet="none"  readonly="true" showXPath="false" id="topic_extended_description" value="#{topic.topic.extendedDescription}" rendered="#{topic.readFullDesciption}"/> --%>
					<f:verbatim></div></f:verbatim>
				</h:panelGroup>
				<h:dataTable styleClass="listHier" value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" cellpadding="0" cellspacing="0" columnClasses="attach,bogus" summary="layout">
					  <h:column>
					  <%-- gsilver: need to tie in the attachment type to actual  MIME type mapping tables instead of the below (which is prevalent everywhere) or at the very least provide a mechanism for defaults. --%> 
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />						
						</h:column>
						<h:column>
<%--						<h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
							<h:outputText value="#{eachAttach.attachmentName}" />
						</h:outputLink>--%>
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
  	    </h:panelGrid>
    <f:verbatim></div></f:verbatim>

	 </h:column>
			
      </h:dataTable>			
	<f:verbatim></div><!--end single topic here --></f:verbatim>
	  </h:column>
  </h:dataTable>