<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
  <sakai:view>
  
  	<h:form id="dfStatisticsForm" rendered="#{ForumTool.instructor}">
		<!-- discussionForum/statistics/dfStatisticsFullTextForOne.jsp -->
  	       	<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
			<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
			
			<script type="text/javascript">
	  			$(document).ready(function() {
					$(".messageBody").each(function(index){
						var msgBody = $(this).html();
						msgBody = msgBody.replace(/\n/g,',').replace(/\s/g,' ').replace(/  ,/g,',');
						var wordCountId = $(this).attr('id').substring(11, $(this).attr('id').length);
		  				fckeditor_word_count_fromMessage(msgBody,'wordCountSpan' + wordCountId);
					});
				});
			</script>
  	
  		<sakai:tool_bar>				
			<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyFullTextForOne}');" title="#{msgs.cdfm_print}">
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
			 <h:commandLink action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.selectedSiteUser}" rendered="#{empty mfStatisticsBean.selectedAllTopicsTopicId && empty mfStatisticsBean.selectedAllTopicsForumId}">
			 </h:commandLink>
			 <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByAllTopics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}" rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId || !empty mfStatisticsBean.selectedAllTopicsForumId}"/>
		      <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsForumId}">
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
  				    <f:param value="" name="topicId"/>
  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
  				    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsForumTitle}" />
	          	  </h:commandLink>
			  </h:panelGroup>
			  <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId}">
		      	  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsTopicId}" name="topicId"/>
  				    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
  				    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsTopicTitle}" />
	          	  </h:commandLink>
	          </h:panelGroup> 
			 <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			 <h:commandLink action="#{ForumTool.processActionShowFullTextForAll}" value="#{msgs.stat_authored}">
			 </h:commandLink>
			 <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			  <h:outputText value="#{mfStatisticsBean.selectedMsgSubject}" />
			  <f:verbatim></h3></div></f:verbatim>
  
  		<h:dataTable id="subjectBody" value="#{mfStatisticsBean.userSubjectMsgBody}" var="stat" styleClass="table table-hover table-striped table-bordered" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   			<h:column rendered="#{!stat.msgDeleted}">
   				<f:verbatim>
  					<span id="messageBody</f:verbatim><h:outputText value="#{stat.msgId}"/><f:verbatim>" style="display: none" class="messageBody">
  				</f:verbatim>
  					<h:outputText escape="false" value="#{stat.message}"/>
				<f:verbatim>  					
  					</span>  	
  					<span><img src="/library/image/silk/table_add.png" />&nbsp;</f:verbatim><h:outputText value="#{msgs.cdfm_message_count}" /><f:verbatim>:&nbsp;<span  id="wordCountSpan</f:verbatim><h:outputText value="#{stat.msgId}"/><f:verbatim>"> </span></span>			
	  			</f:verbatim>
				<h:panelGroup>
					<f:verbatim><div style="border-bottom:1px solid #ccc;padding-bottom:5px;height:100%;overflow:hidden"></f:verbatim>
						<f:verbatim><p style="width:80%;float:left;margin:0;padding:0;font-size:110%;color:#000;font-weight:bold"></f:verbatim>
							<h:outputText value="#{stat.forumTitle}" />
							 <f:verbatim><h:outputText value="/" /></f:verbatim>
							<h:outputText value="#{stat.topicTitle}" />
							 <f:verbatim><h:outputText value="/" /></f:verbatim>
							<h:outputText  value= "#{stat.forumSubject} " />
							<h:outputText value="#{stat.forumDate}">
								<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
							</h:outputText>
						<f:verbatim></p></f:verbatim>						
						<h:panelGroup style="display:block;float:right;width:15%;text-align:right;font-weight:bold">
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
  			</h:column>
  			
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
