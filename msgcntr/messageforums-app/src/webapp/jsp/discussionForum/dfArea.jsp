<mf:forumHideDivision title="Discussion Forums" id="_test_div">
  <mf:forum_bar_link id="create_forum" title="#{msgs.dfm_new_forum}" value=" #{msgs.dfm_new_forum} " action="#{ForumTool.processCreateNewForum}"/> &nbsp;
  <mf:forum_bar_link id="organize_forum" title="#{msgs.dfm_organize}" value=" #{msgs.dfm_organize} " action="#{ForumTool.processOrganize}"/> &nbsp;
  <mf:forum_bar_link id="forum_stats" title="#{msgs.dfm_statistic}" value=" #{msgs.dfm_statistic} " action="#{ForumTool.processStatistics}"/> &nbsp;
  <mf:forum_bar_link id="template_setting" title="#{msgs.dfm_template_setting}" value=" #{msgs.dfm_template_setting} " action="#{ForumTool.processTemplateSettings}"/> &nbsp;

  <h:dataTable id="forums" width="100%" value="#{ForumTool.forums}" var="forum">
    <h:column >
    	<f:verbatim><div class="forumsRow"></f:verbatim>
			<h:commandLink action="#{ForumTool.processDisplayForum}"  value="#{forum.forum.title}" >
				<f:param value="#{forum.forum.id}" name="forumId"/>
			</h:commandLink>
			<f:verbatim></br></f:verbatim>
			<h:outputText id="forum_desc" value="#{forum.forum.shortDescription}" />
			<f:verbatim></div></f:verbatim>
		 <h:dataTable id="topics" width="100%" value="#{forum.topics}" var="topic">
		    <h:column>
				<f:verbatim><div class="topicRows"></f:verbatim>
				<h:commandLink action="#{ForumTool.processDisplayTopic}" id="topic_title" value="#{topic.topic.title}"/>
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
	</h:column>
  </h:dataTable>
</mf:forumHideDivision>