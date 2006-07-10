<mf:forumHideDivision title="#{msgs.cdfm_discussion_forums}" id="_test_div">
  <mf:forum_bar_link id="create_forum" title="#{msgs.cdfm_new_forum}" value=" #{msgs.cdfm_new_forum}" action="#{ForumTool.processActionNewForum}" rendered="#{ForumTool.newForum}"/>    
   &nbsp;
<%--  <mf:forum_bar_link id="organize_forum" title="#{msgs.cdfm_organize}" value=" #{msgs.cdfm_organize} " action="#{ForumTool.processActionOrganize}"/> &nbsp;
  <mf:forum_bar_link id="forum_stats" title="#{msgs.cdfm_statistic}" value=" #{msgs.cdfm_statistic} " action="#{ForumTool.processActionStatistics}"/> &nbsp;--%>
  <h:outputText id="draft_space2" value="   " />
  <mf:forum_bar_link id="template_setting" title="#{msgs.cdfm_template_setting}" value="#{msgs.cdfm_template_setting} " action="#{ForumTool.processActionTemplateSettings}" rendered="#{ForumTool.instructor}"/>    
     &nbsp;


  <h:dataTable id="forums" value="#{ForumTool.forums}" width="100%" var="forum">
    <h:column>
    
    <h:panelGrid columns="2" styleClass="msgHeadings" summary="">
  	    <h:panelGroup>
  	      <h:outputText styleClass="msgDraft" id="draft" value="#{msgs.cdfm_draft}" rendered="#{forum.forum.draft == 'true'}"/>
	      <h:outputText id="draft_space" value="  " rendered="#{forum.forum.draft == 'true'}"/>
	
	      <h:graphicImage url="/images/lock.gif" alt="#{msgs.cdfm_forum_locked}"  rendered="#{forum.locked == 'true'}"/>
	      <h:outputText id="emptyspace" value="  " rendered="#{forum.locked == 'true'}"/>
	      <h4>
	        <h:commandLink action="#{ForumTool.processActionDisplayForum}"  value="#{forum.forum.title}" title=" #{forum.forum.title}">
		        <f:param value="#{forum.forum.id}" name="forumId"/>
	        </h:commandLink>
	      </h4>
	  	  </h:panelGroup>
	  	  <h:panelGroup styleClass="msgNav">
			  <h:commandLink action="#{ForumTool.processActionNewTopic}" value="#{msgs.cdfm_new_topic}" rendered="#{forum.newTopic}" title="#{msgs.cdfm_new_topic}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>
	      </h:commandLink>
	      <f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
	   	  <h:commandLink action="#{ForumTool.processActionForumSettings}"  value="#{msgs.cdfm_forum_settings}" rendered="#{forum.changeSettings}" title="#{msgs.cdfm_forum_settings}">
		      <f:param value="#{forum.forum.id}" name="forumId"/>				
	      </h:commandLink>
  	    </h:panelGroup>
  	  </h:panelGrid>
  	  <h:panelGrid columns="1" summary="">  
  	    	<h:panelGroup>
			  <h:outputText id="forum_desc" value="#{forum.forum.shortDescription}" />
  	    </h:panelGroup>
  	    
  	    	<h:panelGroup>
			  <h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayForumExtendedDescription}" rendered="#{forum.hasExtendedDesciption}"
				               id="forum_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
				  <f:param value="#{forum.forum.id}" name="forumId_displayExtended"/>
				  <f:param value="displayHome" name="redirectToProcessAction"/>
		    </h:commandLink>
	 	    <sakai:inputRichText rows="5" cols="110" buttonSet="none" readonly="true" showXPath="false" id="forum_extended_description" value="#{forum.forum.extendedDescription}" rendered="#{forum.readFullDesciption}"/>
		    <f:verbatim><br /></f:verbatim>
		    <h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayForumExtendedDescription}" id="forum_extended_hide"
				               value="#{msgs.cdfm_hide_full_description}" rendered="#{forum.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
				  <f:param value="#{forum.forum.id}" name="forumId_hideExtended"/>
				  <f:param value="displayHome" name="redirectToProcessAction"/>
		    </h:commandLink>
  	    </h:panelGroup>
  	    
  	  </h:panelGrid>

	  <h:dataTable  value="#{forum.forum.attachments}" var="eachAttach" rendered="#{!empty forum.forum.attachments}">
			<h:column>
				<f:facet name="header">
					<h:outputText id="attachment" value="#{msgs.cdfm_attachments}" />
				</f:facet>
				<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}" alt="" />
				<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}" alt="" />
				<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}" alt="" />
				<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
				<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}" alt="" />
				<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}" alt="" />
				<h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
					<h:outputText value="#{eachAttach.attachmentName}"  style="text-decoration:underline;"/>
				</h:outputLink>
		  </h:column>
	  </h:dataTable>

		 <h:dataTable id="topics" value="#{forum.topics}" var="topic" width="100%">
		   <h:column>
		      <h:panelGrid columns="2" summary="" width="100%">
    	        <h:panelGroup styleClass="indnt2">
    	          <h:outputText styleClass="msgDraft" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
				      <h:outputText id="draft_space" value="  " rendered="#{topic.topic.draft == 'true'}"/>

				      <h:graphicImage url="/images/lock.gif" alt="#{msgs.cdfm_forum_locked}"  rendered="#{forum.locked == 'true' || topic.locked == 'true'}"/>
				      <h:outputText id="emptyspace" value="  " rendered="#{topic.locked == 'true'}"/>

				      <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{topic.topic.title}" title=" #{topic.topic.title}">
					      <f:param value="#{topic.topic.id}" name="topicId"/>
					      <f:param value="#{forum.forum.id}" name="forumId"/>
				      </h:commandLink>
				      <h:outputText id="topic_msg_count55" value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg} - #{topic.unreadNoMessages} #{msgs.cdfm_unread} #{msgs.cdfm_closeb}" 
				                    rendered="#{topic.isRead}"/>
    	        </h:panelGroup>
    	        <h:panelGroup styleClass="msgNav">
    	         <h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" rendered="#{topic.changeSettings}"
    	                        title=" #{msgs.cdfm_topic_settings}">
					     <f:param value="#{topic.topic.id}" name="topicId"/>
				       <f:param value="#{forum.forum.id}" name="forumId"/>
				     </h:commandLink>
    	        </h:panelGroup>
    	      </h:panelGrid>

          <h:panelGrid columns="1" summary="" width="100%">
    	        <h:panelGroup styleClass="indnt2">
    	          <h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" />
    	        </h:panelGroup>
    	        
				    <h:panelGroup styleClass="indnt2">
					    <h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" rendered="#{topic.hasExtendedDesciption}"
							                id="topic_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
							   <f:param value="#{topic.topic.id}" name="topicId_displayExtended"/>
							   <f:param value="displayHome" name="redirectToProcessAction"/>
						   </h:commandLink>
		 				   <sakai:inputRichText styleClass="indnt2" rows="5" cols="110" buttonSet="none"  readonly="true" showXPath="false" id="topic_extended_description" value="#{topic.topic.extendedDescription}" rendered="#{topic.readFullDesciption}"/>
						   <f:verbatim><br /></f:verbatim>
						   <h:commandLink styleClass="indnt2" immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" id="topic_extended_hide"
							                value="#{msgs.cdfm_hide_full_description}" rendered="#{topic.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
							   <f:param value="#{topic.topic.id}" name="topicId_hideExtended"/>
							   <f:param value="displayHome" name="redirectToProcessAction"/>
						   </h:commandLink>
	    	      </h:panelGroup>
	    	      
					    <h:dataTable styleClass="indnt2" value="#{topic.topic.attachments}" var="eachAttach" rendered="#{!empty topic.topic.attachments}">
							  <h:column>
								<f:facet name="header">
									<h:outputText id="attachment" value="#{msgs.cdfm_attachments}" />
								</f:facet>
								<h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}" alt="" />
								<h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}" alt="" />
								<h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}" alt="" />
								<h:graphicImage url="/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
								<h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}" alt="" />
								<h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}" alt="" />
								<h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
									<h:outputText value="#{eachAttach.attachmentName}" />
								</h:outputLink>
						  </h:column>
					  </h:dataTable>
  	    
	    	      
					    <h:dataTable styleClass="indnt2" id="messages" value="#{topics.messages}" var="message">
				        <h:column>
						      <h:outputText id="message_title" value="#{message.message.title}"/>
						      <f:verbatim><br /></f:verbatim>
						      <h:outputText id="message_desc" value="#{message.message.shortDescription}" />
				        </h:column>
				      </h:dataTable>
				      <f:verbatim><hr /></f:verbatim>
	    	  

	    	    </h:panelGrid>
		    </h:column>
      </h:dataTable>			
	  </h:column>
  </h:dataTable>
</mf:forumHideDivision>