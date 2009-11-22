<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
  <sakai:view>
  	<h:form id="dfStatisticsForm">
		<!--discussionForum/statistics/dfStatisticsUser.jsp-->
  	       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
  	
  		<h:panelGrid columns="2" summary="layout" width="100%" styleClass="navPanel  specialLink">
          <h:panelGroup>
          	 <f:verbatim><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionStatistics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}"/>
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{mfStatisticsBean.selectedSiteUser}" />
			    <f:verbatim></div></f:verbatim>
          </h:panelGroup>
          <h:panelGroup styleClass="itemNav specialLink">	
				<h:commandButton action="#{mfStatisticsBean.processDisplayPreviousParticipant}" value="#{msgs.stat_forum_prev_participant}"  
			                rendered="#{!mfStatisticsBean.isFirstParticipant}" title=" #{msgs.stat_forum_prev_participant}">				
				</h:commandButton>
				<h:commandButton action="#{mfStatisticsBean.processDisplayPreviousParticipant}" value="#{msgs.stat_forum_prev_participant}"  
			                rendered="#{mfStatisticsBean.isFirstParticipant}" title=" #{msgs.stat_forum_prev_participant}" disabled = "true">				
				</h:commandButton>
				
				<h:commandButton action="#{mfStatisticsBean.processDisplayNextParticipant}" value="#{msgs.stat_forum_next_participant}" 
				  		                  rendered="#{!mfStatisticsBean.isLastParticipant}" title=" #{msgs.stat_forum_next_participant}">					
				</h:commandButton>
				<h:commandButton action="#{mfStatisticsBean.processDisplayNextParticipant}" value="#{msgs.stat_forum_next_participant}" 
				  		                  rendered="#{mfStatisticsBean.isLastParticipant}" title=" #{msgs.stat_forum_next_participant}" disabled="true">					
				</h:commandButton>
				
			
	
		</h:panelGroup>

        </h:panelGrid>

	  	<h:panelGrid columns="2" summary="layout" width="100%" style="margin:0">
   			<h:panelGroup>
    			<f:verbatim><h4 style="margin:0;padding:0"></f:verbatim>
		   		<h:outputText value="#{msgs.stat_forum_authored}" />
    			<f:verbatim></h4></f:verbatim>
			</h:panelGroup>
			<h:panelGroup styleClass="itemNav specialLink">
				<h:commandLink action="#{ForumTool.processActionShowFullTextForAll}" value="#{msgs.stat_show_all}" title=" #{msgs.stat_show_all}" rendered="#{!empty mfStatisticsBean.userAuthoredStatistics}"/>	
			</h:panelGroup>
		</h:panelGrid>
		<h:outputText rendered="#{empty mfStatisticsBean.userAuthoredStatistics}" value="#{msgs.stat_no_authored_message}" styleClass="instruction" style="display:block"/>

  		<h:dataTable styleClass="listHier lines nolines" id="members" value="#{mfStatisticsBean.userAuthoredStatistics}" var="stat" rendered="#{!empty mfStatisticsBean.userAuthoredStatistics}"
   	 		columnClasses="bogus,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header"> 				
  					<h:commandLink action="#{mfStatisticsBean.toggleForumTitleSort}" title="#{msgs.stat_forum_title}">
					   	<h:outputText value="#{msgs.stat_forum_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumTitleSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumTitleSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_title}"/>
					</h:commandLink>
   				</f:facet>
			   	<h:outputText value="#{stat.forumTitle}" />
			</h:column>
			<h:column>
  				<f:facet name="header"> 				
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicTitleSort}" title="#{msgs.stat_topic_title}">
					   	<h:outputText value="#{msgs.stat_topic_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.topicTitleSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_topic_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.topicTitleSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_topic_title}"/>
					</h:commandLink>
   				</f:facet>
			   	<h:outputText value="#{stat.topicTitle}" />
			</h:column>
  			<h:column>
  				<f:facet name="header">
				   <h:commandLink action="#{mfStatisticsBean.toggleDateSort}" title="#{msgs.stat_forum_date}">
					   	<h:outputText value="#{msgs.stat_forum_date}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumDateSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_date}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumDateSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_date}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.forumDate}">
  					<f:convertDateTime pattern="#{msgs.date_format}" />
  				</h:outputText>
  			</h:column>
  			<h:column>
  				<f:facet name="header">
				   <h:commandLink action="#{mfStatisticsBean.toggleSubjectSort}" title="#{msgs.stat_forum_subject}">
					   	<h:outputText value="#{msgs.stat_forum_subject}"  />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumSubjectSort && mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_subject}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumSubjectSort && !mfStatisticsBean.ascendingForUser}" alt="#{msgs.stat_forum_subject}"/>						
					</h:commandLink>
  				</f:facet>
  				<h:commandLink action="#{mfStatisticsBean.processActionDisplayMsgBody}" value="#{stat.forumSubject}">
  							<f:param value="#{stat.msgId}" name="msgId"/> 				  			
  				 </h:commandLink>
  				 </h:column>
  				 <h:column>
  			
  			
  			<h:commandLink action="#{ForumTool.processActionDisplayInThread}" value="#{msgs.stat_display_in_thread}" title=" #{msgs.stat_display_in_thread}">	
  				  		<f:param value="#{stat.topicId}" name="topicId"/>
  				  		<f:param value="#{stat.forumId}" name="forumId"/>
  				  		<f:param value="#{stat.msgId}" name="msgId"/>
  				  		
  			</h:commandLink>
  			</h:column>
  		</h:dataTable>
  		
  			<f:verbatim><h4></f:verbatim>
		   <h:outputText value="#{msgs.stat_forum_read}" />
  			<f:verbatim></h4></f:verbatim>
		<h:outputText rendered="#{empty mfStatisticsBean.userReadStatistics}" value="#{msgs.stat_no_read_message}" styleClass="instruction" style="display:block"/>
  		<h:dataTable styleClass="listHier lines nolines" id="members2" value="#{mfStatisticsBean.userReadStatistics}" var="stat2" rendered="#{!empty mfStatisticsBean.userReadStatistics}"
   	 	 columnClasses="bogus,bogus,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header">
		   			<h:commandLink action="#{mfStatisticsBean.toggleForumTitleSort2}" title="#{msgs.stat_forum_title}">
					   	<h:outputText value="#{msgs.stat_forum_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumTitleSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumTitleSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_title}"/>
					</h:commandLink>
  				</f:facet>
			   	<h:outputText value="#{stat2.forumTitle}" />
			</h:column>
			<h:column>
  				<f:facet name="header"> 				
  					<h:commandLink action="#{mfStatisticsBean.toggleTopicTitleSort2}" title="#{msgs.stat_topic_title}">
					   	<h:outputText value="#{msgs.stat_topic_title}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.topicTitleSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_topic_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.topicTitleSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_topic_title}"/>
					</h:commandLink>
   				</f:facet>
			   	<h:outputText value="#{stat2.topicTitle}" />
			</h:column>
			
  			<h:column>
  				<f:facet name="header">
				 <h:commandLink action="#{mfStatisticsBean.toggleDateSort2}" title="#{msgs.stat_forum_date}">
					   	<h:outputText value="#{msgs.stat_forum_date}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumDateSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_date}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumDateSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_date}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat2.forumDate}">
  					<f:convertDateTime pattern="#{msgs.date_format}" />
  				</h:outputText>
  			</h:column>
  			<h:column>
  				<f:facet name="header">
				   	<h:commandLink action="#{mfStatisticsBean.toggleSubjectSort2}" title="#{msgs.stat_forum_subject}">
					   	<h:outputText value="#{msgs.stat_forum_subject}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumSubjectSort2 && mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_subject}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumSubjectSort2 && !mfStatisticsBean.ascendingForUser2}" alt="#{msgs.stat_forum_subject}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat2.forumSubject}" />
  			</h:column>
  		</h:dataTable>
  		
  	</h:form>
  </sakai:view>
 </f:view>
