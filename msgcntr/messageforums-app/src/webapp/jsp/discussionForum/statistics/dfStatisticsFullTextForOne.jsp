<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
  <sakai:view>
  
	<h:form id="dfStatisticsForm" rendered="#{ForumTool.instructor || mfStatisticsBean.isAuthor}">
		<!-- discussionForum/statistics/dfStatisticsFullTextForOne.jsp -->
  	       	<script>includeLatestJQuery("msgcntr");</script>
       		<script src="/messageforums-tool/js/sak-10625.js"></script>
			<script src="/messageforums-tool/js/forum.js"></script>
			
			<script>
				$(document).ready(function() {
					$(".messageBody").each(function(index){
						let msgBody = $(this).html();
						msgBody = msgBody.replace(/\n/g,',').replace(/\s/g,' ').replace(/  ,/g,',');
						const wordCountId = $(this).attr('id').substring(11, $(this).attr('id').length);
						$("#wordCountSpan" + wordCountId).html(getWordCount(msgBody));
					});

                    var menuLink = $('#forumsStatisticsMenuLink');
                    var menuLinkSpan = menuLink.closest('span');
                    menuLinkSpan.addClass('current');
                    menuLinkSpan.html(menuLink.text());

				});
			</script>
            <%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>

          	 <f:verbatim><div class="breadCrumb"><h3></f:verbatim>
			 <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			 <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussions}" title=" #{msgs.cdfm_discussions}"
			      		rendered="#{ForumTool.forumsTool}" />
			 <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
			 <h:commandLink action="#{ForumTool.processActionStatistics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}"/>
			 <h:outputText value="" /><h:outputText value=" / " /><h:outputText value=" " />
			 <h:commandLink action="#{mfStatisticsBean.processActionBackToUser}" rendered="#{empty mfStatisticsBean.selectedAllTopicsTopicId && empty mfStatisticsBean.selectedAllTopicsForumId}">
				 <h:outputText value="#{mfStatisticsBean.selectedSiteUser}" />
			 </h:commandLink>
			 <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByAllTopics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}" rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId || !empty mfStatisticsBean.selectedAllTopicsForumId}"/>
		      <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsForumId}">
			      <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
			      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
  				    <f:param value="" name="topicId"/>
  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
  				    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsForumTitle}" />
	          	  </h:commandLink>
			  </h:panelGroup>
			  <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId}">
		      	  <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
			      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsTopicId}" name="topicId"/>
  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
  				    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsTopicTitle}" />
	          	  </h:commandLink>
	          </h:panelGroup> 
			 <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
			 <h:commandLink action="#{ForumTool.processActionShowFullTextForAll}" value="#{msgs.stat_authored}">
			 </h:commandLink>
			 <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
			  <h:outputText value="#{mfStatisticsBean.selectedMsgSubject}" />
			  <f:verbatim></h3></div></f:verbatim>

		<h:panelGroup id="forumsActions">
			<h:outputLink styleClass="button" id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyFullTextForOne}');" title="#{msgs.cdfm_print}">
				<span class="bi bi-printer" aria-hidden="true"></span>
			</h:outputLink>
		</h:panelGroup>

  		<h:dataTable id="subjectBody" value="#{mfStatisticsBean.userSubjectMsgBody}" var="stat" styleClass="table table-hover table-striped table-bordered" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   			<h:column>
				<h:panelGroup rendered="#{!stat.msgDeleted}" layout="block">
   				<f:verbatim>
  					<span id="messageBody</f:verbatim><h:outputText value="#{stat.msgId}"/><f:verbatim>" style="display: none" class="messageBody">
  				</f:verbatim>
  					<h:outputText escape="false" value="#{stat.message}"/>
  					</span>  	
					<span>
						<span class="bi bi-plus-square-fill" aria-hidden="true"></span>
						<h:outputText value="#{msgs.cdfm_message_count}: " escape="false" />
						<span id="wordCountSpan<h:outputText value="#{stat.msgId}"/>"> </span>
					</span>
				<h:panelGroup>
					<f:verbatim><div style="border-bottom:1px solid #ccc;padding-bottom:5px;height:100%;overflow:hidden"></f:verbatim>
						<f:verbatim><p style="width:80%;float:left;margin:0;padding:0;font-size:110%;color:#000;font-weight:bold"></f:verbatim>
							<h:outputText value="#{stat.forumTitle}" />
							 <h:outputText value="/" />>
							<h:outputText value="#{stat.topicTitle}" />
							 <h:outputText value="/" />
							<h:outputText  value= "#{stat.forumSubject} " />
							<h:outputText value="#{stat.forumDate}">
								<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
							</h:outputText>
						<f:verbatim></p></f:verbatim>						
						<h:panelGroup rendered="#{ForumTool.instructor}" style="display:block;float:right;width:15%;text-align:right;font-weight:bold">
							<h:commandLink action="#{ForumTool.processActionDisplayInThread}" title=" #{msgs.stat_display_in_thread}" >
								<f:param value="#{stat.forumId}" name="forumId"/>
		  				  		<f:param value="#{stat.topicId}" name="topicId"/>
		  				  		<f:param value="#{stat.msgId}" name="msgId"/>
								<h:outputText value="#{msgs.stat_display_in_thread}" />
							</h:commandLink>
						</h:panelGroup>
					<f:verbatim></div></f:verbatim>						
				</h:panelGroup>
			
			
				<mf:htmlShowArea value="#{stat.message}" hideBorder="true" />
				<h:panelGroup rendered="#{!empty stat.decoAttachmentsList}" style="display:block" styleClass="indnt1">
					<h:dataTable value="#{stat.decoAttachmentsList}" var="eachAttach" styleClass="attachListJSF" rendered="#{!empty stat.decoAttachmentsList}">
						<h:column rendered="#{!empty stat.decoAttachmentsList}">
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>		
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
							<h:outputLink value="#{eachAttach.url}" target="_blank">
								<h:outputText value="#{eachAttach.attachment.attachmentName}" />
							</h:outputLink>								
						</h:column>
					</h:dataTable>
				</h:panelGroup>
				</h:panelGroup>

  				<h:panelGroup rendered="#{stat.msgDeleted}" layout="block"> 
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
				</h:panelGroup>
  			</h:column>
  			
  		</h:dataTable>

  		<br /><br />
  		<h:panelGroup>
  			<h:commandButton action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.buttonUserName}"  
			               title="#{mfStatisticsBean.buttonUserName}">			               			
			</h:commandButton>
		</h:panelGroup>
  	</h:form>
  </sakai:view>
 </f:view>
