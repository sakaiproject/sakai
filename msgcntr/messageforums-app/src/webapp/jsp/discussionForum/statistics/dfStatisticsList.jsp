<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>	
</jsp:useBean>
<f:view>
  <sakai:view >
  	<h:form id="dfStatisticsForm" rendered="#{ForumTool.instructor}">
<!-- discussionForum/statistics/dfStatisticsList.jsp-->
       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/msgcntr_statistics.css" />
  		<h:panelGrid columns="2" width="100%" styleClass="navPanel  specialLink">
          <h:panelGroup>
          	 <f:verbatim><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{msgs.stat_list}" />
			    <f:verbatim></h3></f:verbatim>
          </h:panelGroup>
          <h:panelGroup styleClass="itemNav specialLink">     
			<h:outputText value="#{msgs.cdfm_statistics} #{msgs.stat_byUser} " />
			<h:outputText value="#{msgs.cdfm_toolbar_separator} " />
			<h:commandLink action="#{mfStatisticsBean.processActionStatisticsByAllTopics}" value="#{msgs.cdfm_statistics} #{msgs.stat_byTopic}" title="#{msgs.cdfm_statistics} #{msgs.stat_byTopic}"/>				
		  </h:panelGroup>
        </h:panelGrid>
  	
      <div class="table-responsive">
  		<h:dataTable styleClass="table table-hover table-striped table-bordered lines nolines" id="members" value="#{mfStatisticsBean.allUserStatistics}" var="stat" rendered="true"
   	 		columnClasses="specialLink,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header">
					<h:commandLink action="#{mfStatisticsBean.toggleNameSort}" title="#{mfStatisticsBean.pureAnon ? msgs.stat_anon_user : msgs.stat_name}">
					   	<h:outputText value="#{mfStatisticsBean.pureAnon ? msgs.stat_anon_user : msgs.stat_name}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.nameSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_name}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.nameSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_name}"/>
					</h:commandLink>
  				</f:facet>
  				<h:commandLink action="#{mfStatisticsBean.processActionStatisticsUser}" immediate="true">
  				    <f:param value="#{stat.siteUserId}" name="siteUserId"/>
  				    <h:outputText rendered="#{!stat.useAnonId}" value="#{stat.siteUser}" />
  				    <h:outputText rendered="#{stat.useAnonId}" value="#{stat.siteAnonId}" styleClass="anonymousAuthor"/>
	          	</h:commandLink>
			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleAuthoredSort}" title="#{msgs.stat_authored}">
					   	<h:outputText value="#{msgs.stat_authored}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.authoredSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_authored}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.authoredSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_authored}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.authoredForumsAmt}" />
  			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleReadSort}" title="#{msgs.stat_read}">
					   	<h:outputText value="#{msgs.stat_read}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.readSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_read}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.readSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_read}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.readForumsAmt}" />
  			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.toggleUnreadSort}" title="#{msgs.stat_unread}">
					   	<h:outputText value="#{msgs.stat_unread}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.unreadSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_unread}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.unreadSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_unread}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.unreadForumsAmt}" />
  			</h:column>
  			<h:column>
  				<f:facet name="header">
  					<h:commandLink action="#{mfStatisticsBean.togglePercentReadSort}" title="#{msgs.stat_percent_read}">
					   	<h:outputText value="#{msgs.stat_percent_read}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.percentReadSort && mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_percent_read}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.percentReadSort && !mfStatisticsBean.ascending}" alt="#{msgs.stat_sort_percent_read}"/>
					</h:commandLink>
  				</f:facet>
  				<h:outputText value="#{stat.percentReadForumsAmt}">
  					<f:convertNumber type="percent" />
  				</h:outputText>
  			</h:column>
  		</h:dataTable>
      </div>
  	</h:form>
  </sakai:view>
</f:view>
