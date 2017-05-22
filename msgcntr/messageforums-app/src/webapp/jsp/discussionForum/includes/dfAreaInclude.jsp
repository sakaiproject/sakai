<!--jsp/discussionForum/area/dfAreaInclude.jsp-->
<h:panelGrid columns="1" cellpadding="3" rendered="#{empty ForumTool.forums}">
	<h:panelGroup>
		<h:outputText styleClass="instruction noForumsMessage"  value="#{msgs.cdfm_forum_noforums} "  />
		<h:commandLink  id="create_forum" title="#{msgs.cdfm_new_forum}" value="#{msgs.cdfm_forum_inf_no_forum_create}" action="#{ForumTool.processActionNewForum}" rendered="#{ForumTool.newForum}" />
	</h:panelGroup>
</h:panelGrid>
<h:outputText styleClass="accessUserCheck" style="display:none" rendered="#{ForumTool.newForum}" value="x"/>
<script type="text/javascript">
$(document).ready(function() {
	var topicLen = $('.topicBloc').length;
	var forumLen = $('.forumHeader').length;
	var draftForumLen = $('.draftForum').length
	var draftTopicLen = $('.draftTopic').length
	var accessCheck = $('.accessUserCheck').length
	var noForums = $('.noForumsMessage').length

	if (forumLen===1 && draftForumLen ===0 && topicLen===1 && draftTopicLen ===0){
		//probably the default forum adn topic, show an orienting message
		$('.defForums').show();
	}

	// either no topics or all topics are draft - show message in either case
	if((topicLen===0 || draftTopicLen===topicLen) && forumLen !==0){
		if ((topicLen===draftTopicLen) && topicLen!==0){
			$('.noTopicsDraft').show();
		}
		$('.noTopics').show();
		if(topicLen===0){
		$('.noTopicsatAll').show();
		}
	}
	//all forums are draft - show message
	if ((forumLen=== draftForumLen) && forumLen !==0){
		$('.noForumsDraft').show();
		$('.noTopics').hide();
	}
	//no forums because they are all draft or childless- show message to access users
	if (forumLen ===0 && accessCheck === 0 && noForums ===0){
		$('.noForumsAccess').show();
	}
	setupdfAIncMenus();
});
</script>
<h:outputText escape="false" value="<script type='text/javascript'>$(document).ready(function() {setupLongDesc()});</script>"  rendered="#{!ForumTool.showShortDescription}"/>

			<h:outputText styleClass="showMoreText"  style="display:none" value="#{msgs.cdfm_show_more_full_description}"  />

	<p class="instruction noForumsAccess"  style="display:none;">
			<h:outputText styleClass="instruction"  value="#{msgs.cdfm_forum_inf_no_forum_access}"  />
	</p>
<f:subview id="maintainMessages" rendered="#{ForumTool.newForum}">
<f:verbatim>
	<p class="instruction defForums highlightPanel"  style="display:none;">
</f:verbatim>
<h:outputText value="#{msgs.cdfm_forum_inf_init_guide}" escape="false" />
<f:verbatim>
	</p>
</f:verbatim>
<f:verbatim>
	<p class="instruction noTopics  highlightPanel" style="display:none">
</f:verbatim>
<h:outputText styleClass="highlight" style="font-weight:bold" value="#{msgs.cdfm_forum_inf_note} " />
<h:outputText escape="false" value="#{msgs.cdfm_forum_inf_no_topics}" styleClass="noTopicsatAll" style="display:none"/>
<f:verbatim>
<span class="noTopicsDraft" style="display:none"><h:outputText value="#{msgs.cdfm_forum_inf_all_topics_draft}" /></span>
	</p>
</f:verbatim>
<f:verbatim>
	<p class="instruction noForumsDraft  highlightPanel" style="display:none"><h:outputText styleClass="highlight" style="font-weight:bold" value="#{msgs.cdfm_forum_inf_note} " />
</f:verbatim>
<!--
<h:outputText escape="false" value="#{msgs.cdfm_forum_inf_no_forums}"/>
-->
<f:verbatim>
<span class="noForumsDraft" style="display:none"><h:outputText value="#{msgs.cdfm_forum_inf_all_forums_draft}" /></span>
</p>
</f:verbatim>
</f:subview>

<h:dataTable id="forums" value="#{ForumTool.forums}" rendered="#{!empty ForumTool.forums}"  width="100%" var="forum" cellpadding="0" cellspacing="0" styleClass="specialLink" border="0">
    <h:column rendered="#{! forum.nonePermission}">
		<h:panelGrid columns="1" styleClass="forumHeader"  border="0">
  	    <h:panelGroup>
				<%-- link to forum and decorations --%>
				<h:outputText styleClass="highlight title draftForum" id="draft" value="#{msgs.cdfm_draft}" rendered="#{forum.forum.draft == 'true'}"/>
				<h:outputText id="draft_space" value=" -  " rendered="#{forum.forum.draft == 'true'}" styleClass="title"/>
				<%-- availability marker --%>
				<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.forum_restricted_message}" alt="#{msgs.forum_restricted_message}" rendered="#{forum.availability == 'false'}" style="margin-right:.5em"/>
				<%-- locked marker --%>
				<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{forum.locked == 'true'}" style="margin-right:.5em"/>
				<h:commandLink action="#{ForumTool.processActionDisplayForum}" title=" #{forum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}"  styleClass="title">
			        <f:param value="#{forum.forum.id}" name="forumId"/>
			        <h:outputText value="#{forum.forum.title}"/>
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
				<h:outputText  value=" | " rendered="#{ForumTool.newForum || ForumTool.instructor || forum.changeSettings}" />

					<%-- link to display other options on this forum --%>
					<f:verbatim><a href="#" class="moreMenuLink"></f:verbatim>
						<h:outputText  styleClass="moreMenuLinkSpan" value="#{msgs.cdfm__moremenulink}" rendered="#{ForumTool.newForum || ForumTool.instructor || forum.changeSettings}" />	
					<f:verbatim></a></f:verbatim>	

				<%-- list of options, revealed when link above is used, model new added options on existing ones--%>	
				<f:verbatim><ul style="display:none" class="moreMenu"></f:verbatim>
					
					<f:verbatim><li></f:verbatim>
						<h:commandLink id="duplicate" action="#{ForumTool.processActionDuplicateForumMainConfirm}" value="#{msgs.cdfm_duplicate_forum}" rendered="#{ForumTool.newForum}" >
							<f:param value="#{forum.forum.id}" name="forumId"/>
						</h:commandLink>
					<f:verbatim></li></f:verbatim>	

				<f:verbatim><li></f:verbatim>
					<h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true" rendered="#{ForumTool.instructor}">
  					<f:param value="" name="topicId"/>
  					<f:param value="#{forum.forum.id}" name="forumId"/>
  					<h:outputText value="#{msgs.cdfm_button_bar_grade}" />
					</h:commandLink>  	
				<f:verbatim></li></f:verbatim>	

				<f:verbatim><li></f:verbatim>                
					<h:commandLink id="delete" action="#{ForumTool.processActionDeleteForumMainConfirm}" value="#{msgs.cdfm_button_bar_delete_forum}" rendered="#{forum.changeSettings}">
						<f:param value="#{forum.forum.id}" name="forumId"/>
					</h:commandLink>
				<f:verbatim></li></f:verbatim>	
			<f:verbatim></ul></f:verbatim>	

		
<%-- the forum details --%>
				<h:outputText value="#{forum.forum.shortDescription}" styleClass="shortDescription"/>
	  			<f:subview id="longDesc" rendered="#{!empty forum.attachList || (forum.forum.extendedDescription != '' &&  forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>')}">
				<h:outputLink id="forum_extended_show" value="javascript:void(0)" title="#{msgs.cdfm_view}"  styleClass="show" style="#{ForumTool.alwaysShowFullDesc  ? 'display:none' : 'display:block'}"
						onclick="toggleExtendedDescription($(this).next('.hide'), $('div.toggle:first', $(this).parents('table.forumHeader')), $(this));">
					<h:graphicImage url="/images/collapse.gif" alt="" /><h:outputText value="#{msgs.cdfm_view}" />
					<h:outputText value=" #{msgs.cdfm_full_description}"  rendered="#{forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_and}"  rendered="#{!empty forum.attachList && forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_attach}"  rendered="#{!empty forum.attachList}"/>
			  </h:outputLink>

				<%--//designNote: these link always show up even after you  have "zeroed out" a long description because it always saves a crlf --%>
				<h:outputLink id="forum_extended_hide" value="javascript:void(0)" title="#{msgs.cdfm_hide}" styleClass="hide" style="#{ForumTool.alwaysShowFullDesc ? 'display:block' : 'display:none'}"
						onclick="toggleExtendedDescription($(this).prev('.show'), $('div.toggle:first', $(this).parents('table.forumHeader')), $(this));">
					<h:graphicImage url="/images/expand.gif" alt=""/> <h:outputText value="#{msgs.cdfm_hide}" />
					<h:outputText value=" #{msgs.cdfm_full_description}"  rendered="#{forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_and}"  rendered="#{!empty forum.attachList && forum.forum.extendedDescription != '' && forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>'}"/>
					<h:outputText value=" #{msgs.cdfm_attach}"  rendered="#{!empty forum.attachList}"/>
			  </h:outputLink>
			  	<f:subview id="hideLongDesc" rendered="#{!ForumTool.alwaysShowFullDesc}">
					<f:verbatim><div class="toggle" style="display:none;"></f:verbatim>
				</f:subview>
				<f:subview id="showLongDesc" rendered="#{ForumTool.alwaysShowFullDesc}">
					<f:verbatim><div class="toggle"></f:verbatim>
				</f:subview>
					<mf:htmlShowArea value="#{forum.forum.extendedDescription}"  hideBorder="true" />
					<%-- attachs --%>
					<h:dataTable  styleClass="attachListTable" value="#{forum.attachList}" var="eachAttach" rendered="#{!empty forum.attachList}" columnClasses="attach,bogus" style="font-size:.9em;width:auto;margin-left:1em" border="0" cellpadding="3" cellspacing="0">
			<h:column>
			<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
			<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />				
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
				</f:subview>
				<f:subview id="dates" rendered="#{ForumTool.showAvailabilityDates == 'true' && forum.availabilityRestricted == 'true'}">
					<h:outputLink id="forum_extended_show2" value="javascript:void(0)" title="#{msgs.cdfm_read_dates}"  styleClass="show" style="#{'display:block'}"
							onclick="toggleDates($(this).next('.hide'), $('div.toggle2:first', $(this).parents('table.forumHeader')), $(this));">
						<h:graphicImage url="/images/collapse.gif" /><h:outputText value="#{msgs.cdfm_read_dates}" />
						
					</h:outputLink>
					<h:outputLink id="forum_extended_hide2" value="javascript:void(0)" title="#{msgs.cdfm_hide_dates}" styleClass="hide" style="#{'display:none'}"
							onclick="toggleDates($(this).prev('.show'), $('div.toggle2:first', $(this).parents('table.forumHeader')), $(this));">
						<h:graphicImage url="/images/expand.gif"/> <h:outputText value="#{msgs.cdfm_hide_dates}" />
					</h:outputLink>
					<f:subview id="showDate">
						<f:verbatim><div class="toggle2" style="display:none;"></f:verbatim>
					</f:subview>
						<h:panelGrid columns="1" rendered="#{forum.availabilityRestricted == 'true'}">
							<h:outputText value="#{msgs.openDate}: #{forum.formattedOpenDate}" />
							<h:outputText value="#{msgs.closeDate}: #{forum.formattedCloseDate}" />
						</h:panelGrid>
					<f:verbatim></div></f:verbatim>
				</f:subview>
	  </h:panelGroup>
  </h:panelGrid>
	  <%-- the topic list  --%>
		<%--//designNote: display a message if there is no topics for this forum , give a prompt to create a topic--%>
		<h:panelGrid columns="1" cellpadding="3" rendered="#{empty forum.topics}" style="margin:0 1em 2em 1em;">
			<h:panelGroup styleClass="instruction">
				<h:outputText escape="false" value="#{msgs.cdfm_forum_inf_no_topic_here} " />
				<h:commandLink action="#{ForumTool.processActionNewTopic}" value="#{msgs.cdfm_forum_inf_no_topic_create}" rendered="#{forum.newTopic}" title="#{msgs.cdfm_new_topic}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>
	      </h:commandLink>
			  
			</h:panelGroup>
		</h:panelGrid> 

		<h:dataTable id="topics" rendered="#{!empty forum.topics}" value="#{forum.topics}" var="topic"  width="100%"   cellspacing="0" cellpadding="0" border="0">
		   <h:column rendered="#{! topic.nonePermission}">
					<h:panelGrid columns="1" width="100%" styleClass="specialLink topicBloc" cellpadding="0" cellspacing="0">
		      	<h:panelGroup>
							
							<h:graphicImage url="/images/folder.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages == 0 }" styleClass="topicIcon" style="margin-right:.5em"/>
							<h:graphicImage url="/images/folder_unread.gif" alt="Topic Folder" rendered="#{topic.unreadNoMessages > 0 }" styleClass="topicIcon" style="margin-right:.5em"/>
							<h:outputText styleClass="highlight title draftTopic" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
							<h:outputText id="draft_space" value="  - " rendered="#{topic.topic.draft == 'true'}" styleClass="title"/>
							<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.topic_restricted_message}" alt="#{msgs.topic_restricted_message}" rendered="#{topic.availability == 'false'}" style="margin-right:.5em"/>
							<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{forum.locked == 'true' || topic.locked == 'true'}" style="margin-right:.5em"/>
							<h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" title=" #{topic.topic.title}" styleClass="title">
								<f:param value="#{topic.topic.id}" name="topicId"/>
								<f:param value="#{forum.forum.id}" name="forumId"/>
								<h:outputText value="#{topic.topic.title}"/>
							</h:commandLink>
             
               <%-- // display  singular ('unread message') if unread message is  1 --%> 
               <h:outputText styleClass="childrenNew" id="topic_msg_count55" value="  #{topic.unreadNoMessages} #{msgs.cdfm_lowercase_unread_msg}" 
                             rendered="#{topic.isRead && topic.unreadNoMessages >= 1}"/>   
                       
               <%-- // display  plural ('unread messages') with different style sheet if unread message is 0 --%>  
               <h:outputText styleClass="childrenNewZero" id="topic_msg_count57" value="   #{topic.unreadNoMessages} #{msgs.cdfm_lowercase_unread_msg}" 
                             rendered="#{topic.isRead && topic.unreadNoMessages == 0}"/> 
               
               <%-- // display singular ('message') if total message is 1--%>                   
               <h:outputText styleClass="textPanelFooter" id="topic_msg_count58" value="#{msgs.cdfm_of} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg}"
                             rendered="#{topic.isRead && topic.totalNoMessages == 1}"/>
                                     
               <%-- // display singular ('message') if total message is 0 or more than 1--%>                   
               <h:outputText styleClass="textPanelFooter" id="topic_msg_count59" value="#{msgs.cdfm_of} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs}"
                             rendered="#{topic.isRead && (topic.totalNoMessages > 1 || topic.totalNoMessages == 0)}"/>
                                
               <h:outputText id="topic_moderated" value=" #{msgs.cdfm_forum_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true' && topic.isRead}" />

							<%--//desNote: links to act on this topic --%>
							<h:outputText value=" "  styleClass="actionLinks"/>
    	         <h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" rendered="#{topic.changeSettings}"
    	                        title=" #{msgs.cdfm_topic_settings}">
					     <f:param value="#{topic.topic.id}" name="topicId"/>
				       <f:param value="#{forum.forum.id}" name="forumId"/>
				     </h:commandLink>
							<h:outputText  value=" | " rendered="#{forum.newTopic || ForumTool.instructor || topic.changeSettings}"/>

							<%-- link to display other options on this topic --%>
							<f:verbatim><a href="#" class="moreMenuLink"></f:verbatim>
								<h:outputText  styleClass="moreMenuLinkSpan" value="#{msgs.cdfm__moremenulink}" rendered="#{forum.newTopic || ForumTool.instructor || topic.changeSettings}" />	
							<f:verbatim></a></f:verbatim>	

							<%-- list of options, revealed when link above is used, model new added options on existing ones--%>	
							<f:verbatim><ul style="display:none" class="moreMenu"></f:verbatim>
								<f:verbatim><li></f:verbatim>

									<h:commandLink action="#{ForumTool.processActionDuplicateTopicMainConfirm}" id="duplicate_confirm" value="#{msgs.cdfm_duplicate_topic}" rendered="#{forum.newTopic}"
									title=" #{msgs.cdfm_duplicate_topic}">
											<f:param value="#{topic.topic.id}" name="topicId"/>
											<f:param value="#{forum.forum.id}" name="forumId"/>
									</h:commandLink>
								<f:verbatim></li></f:verbatim>
								<f:verbatim><li></f:verbatim>							
							<h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true" rendered="#{ForumTool.instructor}">
			  				    <f:param value="#{topic.topic.id}" name="topicId"/>
			  				    <f:param value="#{forum.forum.id}" name="forumId"/>
			  				    <h:outputText value="#{msgs.cdfm_button_bar_grade}" />
				          	</h:commandLink>  	
								<f:verbatim></li></f:verbatim>
								                            
								<f:verbatim><li></f:verbatim>							
							<h:commandLink action="#{ForumTool.processActionDeleteTopicMainConfirm}" id="delete_confirm" value="#{msgs.cdfm_button_bar_delete_topic}" accesskey="d" rendered="#{topic.changeSettings}"
							title="#{msgs.cdfm_button_bar_delete_topic}">
									<f:param value="#{topic.topic.id}" name="topicId"/>
									<f:param value="#{forum.forum.id}" name="forumId"/>
							</h:commandLink>
								<f:verbatim></li></f:verbatim>
						<f:verbatim></ul></f:verbatim>														
							<%--the topic details --%>
							<h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" styleClass="shortDescription" />
							<f:subview id="longDescTopic" rendered="#{!empty topic.attachList || (topic.topic.extendedDescription != '' &&  topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>')}">
							<h:outputLink id="forum_extended_show" value="javascript:void(0)" title="#{msgs.cdfm_view}" styleClass="show" style="#{ForumTool.alwaysShowFullDesc  ? 'display:none' : 'display:block'}"
									onclick="toggleExtendedDescription($(this).next('.hide'), $('td div.toggle:first', $(this).parents('tr:first')), $(this));">
									<h:graphicImage url="/images/collapse.gif" alt=""/><h:outputText value="#{msgs.cdfm_view}" />
									<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList && topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
				    </h:outputLink>  
				  
							<h:outputLink id="forum_extended_hide" value="javascript:void(0)" title="#{msgs.cdfm_hide}" styleClass="hide" style="#{ForumTool.alwaysShowFullDesc ? 'display:block' : 'display:none'}"
									onclick="toggleExtendedDescription($(this).prev('.show'), $('td div.toggle:first', $(this).parents('tr:first')), $(this));">
									<h:graphicImage url="/images/expand.gif" alt=""/><h:outputText value="#{msgs.cdfm_hide}" />
									<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList && topic.topic.extendedDescription != '' && topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>'}"/>
									<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
				    </h:outputLink>

							<f:subview id="hideLongDescTopic" rendered="#{!ForumTool.alwaysShowFullDesc}">
								<f:verbatim><div class="toggle" style="display:none;"></f:verbatim>
							</f:subview>
							<f:subview id="showLongDescTopic" rendered="#{ForumTool.alwaysShowFullDesc}">
								<f:verbatim><div class="toggle"></f:verbatim>
							</f:subview>
					<mf:htmlShowArea  id="topic_fullDescription" hideBorder="true"	 value="#{topic.topic.extendedDescription}" />
								<%--//desNote:attach list --%>
								<h:dataTable  styleClass="attachListTable" value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" cellpadding="3" cellspacing="0" columnClasses="attach,bogus" style="font-size:.9em;width:auto;margin-left:1em" border="0">
					  <h:column>
										<h:graphicImage url="/images/attachment.gif" alt=""/>
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
    </f:subview>
			<f:subview id="topic_dates" rendered="#{ForumTool.showAvailabilityDates == 'true' && topic.availabilityRestricted == 'true'}">
				<h:outputLink id="forum_extended_show2" value="javascript:void(0)" title="#{msgs.cdfm_read_dates}"  styleClass="show" style="#{'display:block'}"
						onclick="toggleDates($(this).next('.hide'), $('div.toggle2:first', $(this).parents('tr:first')), $(this));">
					<h:graphicImage url="/images/collapse.gif" /><h:outputText value="#{msgs.cdfm_read_dates}" />
					
				</h:outputLink>
				<h:outputLink id="forum_extended_hide2" value="javascript:void(0)" title="#{msgs.cdfm_hide_dates}" styleClass="hide" style="#{'display:none'}"
						onclick="toggleDates($(this).prev('.show'), $('div.toggle2:first', $(this).parents('tr:first')), $(this));">
					<h:graphicImage url="/images/expand.gif"/> <h:outputText value="#{msgs.cdfm_hide_dates}" />
				</h:outputLink>
				<f:subview id="showTopicDate">
					<f:verbatim><div class="toggle2" style="display:none;"></f:verbatim>
				</f:subview>
					<h:panelGrid columns="1" rendered="#{topic.availabilityRestricted == 'true'}">
						<h:outputText value="#{msgs.openDate}: #{topic.formattedOpenDate}" />
						<h:outputText value="#{msgs.closeDate}: #{topic.formattedCloseDate}" />
					</h:panelGrid>
				<f:verbatim></div></f:verbatim>
			</f:subview>
						</h:panelGroup>
					
					</h:panelGrid>
	 </h:column>
      </h:dataTable>			
	  </h:column>
  </h:dataTable>
