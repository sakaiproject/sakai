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
  	<!-- discussionForum/statistics/dfStatisticsAllAuthoredMsgForOneUser.jsp-->
  	       	<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
  	
  			<sakai:tool_bar>
  				<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyAllAuthoredMsg}');" title="#{msgs.cdfm_print}">
					<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
				</h:outputLink>		
  			</sakai:tool_bar>
  	
          	 <f:verbatim><div class="breadCrumb"><h3></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionStatistics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}"/>
			      <f:verbatim><h:outputText value="" /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.selectedSiteUser}"/>
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{msgs.stat_authored}" />
			  <f:verbatim></h3></div></f:verbatim>
			  <f:verbatim><div style="font-weight:bold;" styleClass="specialLink"></f:verbatim>					          
					<h:commandLink  action="#{mfStatisticsBean.toggleTopicTitleSort3}" title=" #{msgs.stat_sort_by_topic}">	
						<h:outputText value="#{msgs.stat_sort_by_topic}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.topicTitleSort3 && mfStatisticsBean.ascendingForUser3}" alt="#{msgs.stat_topic_title}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.topicTitleSort3 && !mfStatisticsBean.ascendingForUser3}" alt="#{msgs.stat_topic_title}"/>
					</h:commandLink>
	  				<f:verbatim><h:outputText value=" " /><h:outputText value=" | " /><h:outputText value=" " /></f:verbatim>
					<h:commandLink action="#{mfStatisticsBean.toggleDateSort3}" title=" #{msgs.stat_sort_by_date}">	
						<h:outputText value="#{msgs.stat_sort_by_date}" />
						<h:graphicImage value="/images/sortascending.gif" rendered="#{mfStatisticsBean.forumDateSort3 && mfStatisticsBean.ascendingForUser3}" alt="#{msgs.stat_forum_date}"/>
						<h:graphicImage value="/images/sortdescending.gif" rendered="#{mfStatisticsBean.forumDateSort3 && !mfStatisticsBean.ascendingForUser3}" alt="#{msgs.stat_forum_date}"/>
					</h:commandLink>
			  <f:verbatim></div></f:verbatim>					            
  			<h:dataTable id="staticAllMessages" value="#{mfStatisticsBean.userAuthoredStatistics2}" var="stat" styleClass="" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   				<h:column rendered="#{!stat.msgDeleted}">
				<h:panelGroup>
					<f:verbatim><h4 style="border-bottom:1px solid #ccc;padding-bottom:5px;height:100%;overflow:hidden"></f:verbatim>
					<f:verbatim><p style="width:80%;float:left;margin:0;padding:0;font-size:110%;color:#000"></f:verbatim>
						<h:outputText value="#{stat.forumTitle}" />
						<f:verbatim><h:outputText value=" / " /></f:verbatim>
						<h:outputText value="#{stat.topicTitle}" />
						<f:verbatim><h:outputText value=" / " /></f:verbatim>
						<h:outputText  value= "#{stat.forumSubject} " />
						<h:outputText value="#{stat.forumDate}" styleClass="textPanelFooter">
							<f:convertDateTime pattern="#{msgs.date_format_paren}" />
						</h:outputText>
						<f:verbatim></p></f:verbatim>
						<h:panelGroup style="display:block;float:right;width:15%;text-align:right">
							<h:commandLink action="#{ForumTool.processActionDisplayInThread}" title=" #{msgs.stat_display_in_thread}" >
								<f:param value="#{stat.forumId}" name="forumId"/>
		  				  		<f:param value="#{stat.topicId}" name="topicId"/>
		  				  		<f:param value="#{stat.msgId}" name="msgId"/>
								<h:outputText value=" #{msgs.stat_display_in_thread}" />
							</h:commandLink>
						</h:panelGroup>
					<f:verbatim></h4></f:verbatim>
				</h:panelGroup>
				<mf:htmlShowArea value="#{stat.message}" hideBorder="true" />

				<h:panelGroup rendered="#{!empty stat.decoAttachmentsList}" style="display:block;" styleClass="indnt1">
					<h:dataTable value="#{stat.decoAttachmentsList}" var="eachAttach" styleClass="attachListJSF" rendered="#{!empty stat.decoAttachmentsList}">
					<h:column rendered="#{!empty stat.decoAttachmentsList}">
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>		
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value=" #{eachAttach.attachment.attachmentName}" />
						</h:outputLink>								
					</h:column>
					</h:dataTable>
				</h:panelGroup>
					
  			</h:column>

  			<%-- deleted messages  - not implemented

  			<h:column rendered="#{stat.msgDeleted}">
   				<f:verbatim><div class="hierItemBlock"></f:verbatim>
				<f:verbatim><h4 class="textPanelHeader"></f:verbatim>
				<f:verbatim><div class="specialLink" style="width:65%;float:left;text-align:left"></f:verbatim>
				<h:panelGroup>
					<h:panelGroup styleClass="inactive">
						<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
						<f:verbatim></span></f:verbatim>
 					</h:panelGroup>
					<f:verbatim></div ></f:verbatim>
					<f:verbatim><div style="width:30%;float:right;text-align:right"	class="specialLink"></f:verbatim>
				</h:panelGroup>
				<h:panelGroup>
					<h:commandLink action="#{ForumTool.processActionDisplayInThread}" title=" #{msgs.stat_display_in_thread}" >
							<f:param value="#{stat.forumId}" name="forumId"/>
  				  			<f:param value="#{stat.topicId}" name="topicId"/>
  				  			<f:param value="#{stat.msgId}" name="msgId"/>
						<h:outputText value="#{msgs.stat_display_in_thread}" />
					</h:commandLink>
				</h:panelGroup>
			
				<f:verbatim></div ></f:verbatim>
				<f:verbatim><div style="clear:both;height:.1em;width:100%;"></div></f:verbatim>
				<f:verbatim></h4></f:verbatim>
				<mf:htmlShowArea value="" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
  			</h:column>			
			
			--%>
  		</h:dataTable>

  		<h:panelGroup styleClass="act" style="display:block">
  			<h:commandButton action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.buttonUserName}"  
			               title="#{mfStatisticsBean.buttonUserName}">			               			
			</h:commandButton>
		</h:panelGroup>
  	</h:form>
  </sakai:view>
 </f:view>
