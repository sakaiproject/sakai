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
  	
  		<h:panelGrid columns="1" summary="layout" width="100%" styleClass="navPanel  specialLink">
          <h:panelGroup>
          	 <f:verbatim><div class="breadCrumb"><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionStatistics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}"/>
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{mfStatisticsBean.selectedSiteUser}" />
			    <f:verbatim></h3></div></f:verbatim>
          </h:panelGroup>
        </h:panelGrid>
    	
    	<p class="textPanel">
		   <h:outputText value="#{msgs.stat_forum_authored}" />
		</p>
  		<h:dataTable styleClass="listHier lines nolines" id="members" value="#{mfStatisticsBean.userAuthoredStatistics}" var="stat" rendered="true"
   	 		columnClasses="specialLink,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header">
		   			<h:outputText value="#{msgs.stat_forum_title}" />
  				</f:facet>
			   	<h:outputText value="#{stat.forumTitle}" />
			</h:column>
  			<h:column>
  				<f:facet name="header">
				   	<h:outputText value="#{msgs.stat_forum_date}" />
  				</f:facet>
  				<h:outputText value="#{stat.forumDate}">
  					<f:convertDateTime pattern="#{msgs.date_format}" />
  				</h:outputText>
  			</h:column>
  			<h:column>
  				<f:facet name="header">
				   	<h:outputText value="#{msgs.stat_forum_subject}" />
  				</f:facet>
  				<h:outputText value="#{stat.forumSubject}" />
  			</h:column>
  		</h:dataTable>
  		
  		<br /><br /><br />
  		
  		<p class="textPanel">
		   <h:outputText value="#{msgs.stat_forum_read}" />
		</p>
  		<h:dataTable styleClass="listHier lines nolines" id="members2" value="#{mfStatisticsBean.userReadStatistics}" var="stat2" rendered="true"
   	 		columnClasses="specialLink,bogus,bogus,bogus,bogus" cellpadding="0" cellspacing="0">
  			<h:column>
  				<f:facet name="header">
		   			<h:outputText value="#{msgs.stat_forum_title}" />
  				</f:facet>
			   	<h:outputText value="#{stat2.forumTitle}" />
			</h:column>
  			<h:column>
  				<f:facet name="header">
				   	<h:outputText value="#{msgs.stat_forum_date}" />
  				</f:facet>
  				<h:outputText value="#{stat2.forumDate}">
  					<f:convertDateTime pattern="#{msgs.date_format}" />
  				</h:outputText>
  			</h:column>
  			<h:column>
  				<f:facet name="header">
				   	<h:outputText value="#{msgs.stat_forum_subject}" />
  				</f:facet>
  				<h:outputText value="#{stat2.forumSubject}" />
  			</h:column>
  		</h:dataTable>
  		
  	</h:form>
  </sakai:view>
 </f:view>