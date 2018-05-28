<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.cdfm_discussion_topic_settings}" toolCssHref="/messageforums-tool/css/msgcntr.css">
	<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
	<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>      
	<script type="text/javascript" src="/messageforums-tool/js/jquery.charcounter.js"> </script>
	<sakai:script contextBase="/messageforums-tool" path="/js/permissions_header.js"/>
	<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
	<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
	<sakai:script contextBase="/messageforums-tool" path="/js/datetimepicker.js"/>
	<script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>
	<link href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" rel="stylesheet" type="text/css" />
	
	<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
	%>
	<script type="text/javascript">
		function resize(){
  			mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  		}
	</script> 
	<script type="text/javascript">
	function setDatesEnabled(radioButton){
		$(".calWidget").fadeToggle('slow');
	}

	function openDateCal(){
		NewCal('revise:openDate','MMDDYYYY',true,12, '<h:outputText value="#{ForumTool.defaultAvailabilityTime}"/>');
	}

	function closeDateCal(){
		NewCal('revise:closeDate','MMDDYYYY',true,12, '<h:outputText value="#{ForumTool.defaultAvailabilityTime}"/>');	
	}
	function setAutoCreatePanel(radioButton) {
		$(".createOneTopicPanel").slideToggle("fast");
		$(".createTopicsForGroupsPanel").slideToggle("fast");
	}

	function togglePostAnonymousOption(checked) {
		var revealIDsToRoles = $("#revise\\:revealIDsToRolesContainer");
		if (checked)
		{
			revealIDsToRoles.css("display", "");
		}
		else
		{
			revealIDsToRoles.css("display", "none");
		}
	}
	</script>

<!--jsp/dfReviseTopicSettingsAttach.jsp-->
    <h:form id="revise">
      <sakai:tool_bar_message value="#{msgs.cdfm_discussion_topic_settings}" />
						<h3 class="specialLink">
				      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
				      		rendered="#{ForumTool.messagesandForums}" />
				      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
				      		rendered="#{ForumTool.forumsTool}" />
			  			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
						  <h:commandLink action="#{ForumTool.processActionDisplayForum}" title=" #{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}">
							  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
							  <h:outputText value="#{ForumTool.selectedForum.forum.title}"/>
						  </h:commandLink>
						  <h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
						  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
						  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
							<h:outputText value="#{msgs.cdfm_discussion_topic_settings}" />
			
						</h3>

 			<div class="instruction">
  			<h:outputText id="instruction"  value="#{msgs.cdfm_settings_instruction}"/>
			 	<h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
			</div>
			<h:messages errorClass="messageAlert" infoClass="success" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/> 

			<h:panelGrid styleClass="jsfFormTable" columns="1"  columnClasses="shorttext">
			<h:panelGroup>
		
				<h:outputLabel id="outputLabel" for="topic_title"   style="padding-bottom:.3em;display:block;clear:both;float:none">
					<h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" style="padding-right:3px"/>
					<h:outputText value="#{msgs.cdfm_topic_title}" />
				</h:outputLabel>	 
				<h:inputText size="50" id="topic_title"  maxlength="250" value="#{ForumTool.selectedTopic.topic.title}">
					<f:validateLength minimum="1" maximum="255"/>
				</h:inputText>
			</h:panelGroup>	
			</h:panelGrid>
			<%-- //designNote: rendered attr below should resolve to false only if there is no prior short description
			 		and if there is server property (TBD) saying not to use it  - below just checking for pre-existing short description--%>
			<h:panelGrid columns="1"  columnClasses="longtext" rendered="#{ForumTool.showTopicShortDescription}">
				<h:panelGroup>
					<h:outputLabel id="outputLabel1" for="topic_shortDescription"  value="#{msgs.cdfm_shortDescription}" />

							<h:outputText value="#{msgs.cdfm_shortDescriptionCharsRem}"  styleClass="charRemFormat" style="display:none"/>
							<%--							
							<h:outputText value="%1 chars remain"  styleClass="charRemFormat" style="display:none"/>
							--%>
							<h:outputText value="" styleClass="charsRemaining" style="padding-left:3em;font-size:.85em;"/>
							<h:outputText value=""  style="display:block"/>
					

					<h:inputTextarea rows="3" cols="45" id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}" styleClass="forum_shortDescriptionClass" style="float:none"/>
				</h:panelGroup>	
     	</h:panelGrid>

			<%--RTEditor area - if enabled--%>
		<h:panelGroup rendered="#{! ForumTool.disableLongDesc}">
				<h:outputText id="outputLabel2"   value="#{msgs.cdfm_fullDescription}" styleClass="labeled"/>
			<sakai:inputRichText textareaOnly="#{PrivateMessagesTool.mobileSession}" rows="#{ForumTool.editorRows}" cols="132" id="topic_description" value="#{ForumTool.selectedTopic.topic.extendedDescription}">
			</sakai:inputRichText>
		</h:panelGroup>
		
			<%--Attachment area  --%>
		<h4><h:outputText value="#{msgs.cdfm_att}"/></h4>
		
			<div style="padding-left:1em">
			<%--designNote: would be nice to make this an include, as well as a more comprehensive MIME type check  --%>
			<h:dataTable styleClass="attachPanel" id="attmsg" value="#{ForumTool.attachments}" var="eachAttach"  cellpadding="0" cellspacing="0" columnClasses="attach,bogus,specialLink,bogus,bogus" rendered="#{!empty ForumTool.attachments}">
				<h:column>
					<f:facet name="header">   <h:outputText escape="false"  value="&nbsp;"/>                                          
					</f:facet>
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
				</h:column>
				<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.cdfm_title}"/>
						</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
				</h:column>
				<h:column>
				<f:facet name="header"><h:outputText escape="false" value="&nbsp;"/>
				</f:facet>
							<h:commandLink action="#{ForumTool.processDeleteAttachSetting}" 
								immediate="true"
								onfocus="document.forms[0].onsubmit();"
								title="#{msgs.cdfm_remove}">
							<h:outputText value="#{msgs.cdfm_remove}"/>
								<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>
							</h:commandLink>
				  </h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
							<h:outputText value="#{msgs.cdfm_attsize}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
					</h:column>
					<h:column rendered="#{!empty ForumTool.attachments}">
						<f:facet name="header">
		  			  <h:outputText value="#{msgs.cdfm_atttype}" />
						</f:facet>
						<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
					</h:column>
					</h:dataTable>   
			
			<h:panelGroup styleClass="instruction" rendered="#{empty ForumTool.attachments}">	     
				<h:outputText value="#{msgs.cdfm_no_attachments}" />
			</h:panelGroup>

			<p class="act" style="padding:0 0 1em 0;">
				<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" 
					value="#{msgs.cdfm_button_bar_add_attachment_more_redirect}" 
					accesskey="a" 
					 rendered="#{!empty ForumTool.attachments}"
					 style="font-size:95%"/>
				<sakai:button_bar_item action="#{ForumTool.processAddAttachmentRedirect}" 
					value="#{msgs.cdfm_button_bar_add_attachment_redirect}" 
					accesskey="a" 
					 rendered="#{empty ForumTool.attachments}" 
					 style="font-size:95%"/>
			</p>
			</div>                                                                                  
			<%--general posting  topic settings --%>
			<h4><h:outputText  value="#{msgs.cdfm_topic_posting}"/></h4>

			<div class="indnt1">
				<p class="checkbox">
					<h:selectBooleanCheckbox
						title="topicLocked" value="#{ForumTool.selectedTopic.topicLocked}"
						id="topic_locked">
					</h:selectBooleanCheckbox> <h:outputLabel for="topic_locked" value="#{msgs.cdfm_lock_topic}" />
				</p>	
				<p class="checkbox">
					<h:selectBooleanCheckbox
						title="Moderated" value="#{ForumTool.selectedTopic.topicModerated}"
						id="topic_moderated">
					</h:selectBooleanCheckbox> <h:outputLabel for="topic_moderated" value="#{msgs.cdfm_moderate_topic}" />
				</p>
				<p class="checkbox">
					<h:selectBooleanCheckbox
						title="postFirst" value="#{ForumTool.selectedTopic.topicPostFirst}"
						id="topic_postFirst">
					</h:selectBooleanCheckbox> <h:outputLabel for="topic_postFirst" value="#{msgs.cdfm_postFirst}" />
				</p>	
				<t:htmlTag value="p" styleClass="checkbox" rendered="#{ForumTool.anonymousEnabled}">
					<h:selectBooleanCheckbox
						title="postAnonymous" value="#{ForumTool.selectedTopic.topicPostAnonymous}"
						id="topic_postAnonymous"
						onclick='togglePostAnonymousOption(this.checked);'
						disabled="#{!ForumTool.newTopicOrPostAnonymousRevisable}">
					</h:selectBooleanCheckbox> 
					<h:outputLabel for="topic_postAnonymous"> 
						<h:outputText value="#{msgs.cdfm_postAnonymous}"/>
						<h:outputText value="#{msgs.cdfm_noReviseAfter}" styleClass="messageInstruction" rendered="#{!ForumTool.postAnonymousRevisable && !ForumTool.existingTopic}"/>
						<h:outputText value="#{msgs.cdfm_noRevise}" styleClass="messageInstruction" rendered="#{!ForumTool.postAnonymousRevisable && ForumTool.existingTopic}"/>
					</h:outputLabel>
				</t:htmlTag>
				<t:htmlTag value="p" id="revealIDsToRolesContainer" style="display: #{ForumTool.selectedTopic.topicPostAnonymous ? '' : 'none'}" styleClass="checkbox indnt1" rendered="#{ForumTool.anonymousEnabled}">
					<h:selectBooleanCheckbox
						title="revealIDsToRoles" value="#{ForumTool.selectedTopic.topicRevealIDsToRoles}"
						id="topic_revealIDsToRoles"
						disabled="#{!ForumTool.newTopicOrRevealIDsToRolesRevisable}">
					</h:selectBooleanCheckbox> 
					<h:outputLabel for="topic_revealIDsToRoles">
						<h:outputText value="#{msgs.cdfm_revealIDsToRoles}"/>
						<h:outputText value="#{msgs.cdfm_noReviseAfter}" styleClass="messageInstruction" rendered="#{!ForumTool.revealIDsToRolesRevisable && !ForumTool.existingTopic}"/>
						<h:outputText value="#{msgs.cdfm_noRevise}" styleClass="messageInstruction" rendered="#{!ForumTool.revealIDsToRolesRevisable && ForumTool.existingTopic}"/>
					</h:outputLabel>
				</t:htmlTag>
			</div>	
			<h4><h:outputText  value="#{msgs.cdfm_forum_availability}" /></h4>
			<div class="indnt1">
			<h:panelGrid columns="1" columnClasses="longtext,checkbox" cellpadding="0" cellspacing="0">
              <h:panelGroup>
                 <h:selectOneRadio layout="pageDirection" onclick="this.blur()" onchange="setDatesEnabled(this);" disabled="#{not ForumTool.editMode}" id="availabilityRestricted"  value="#{ForumTool.selectedTopic.availabilityRestricted}">
                  <f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_forum_avail_show}"/>
                  <f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_forum_avail_date}"/>
               </h:selectOneRadio>
               </h:panelGroup>
               <h:panelGroup id="openDateSpan" styleClass="indnt2 openDateSpan  calWidget" style="display: #{ForumTool.selectedTopic.availabilityRestricted ? 'block' : 'none'}">

               	   <h:outputLabel value="#{msgs.openDate}: " for="openDate"/>

	               <h:inputText id="openDate" styleClass="openDate" value="#{ForumTool.selectedTopic.openDate}"/>


              	</h:panelGroup>
               <h:panelGroup id="closeDateSpan" styleClass="indnt2 openDateSpan  calWidget" style="display: #{ForumTool.selectedTopic.availabilityRestricted ? '' : 'none'}">

					
              		<h:outputLabel value="#{msgs.closeDate}: " for="closeDate"/>

	               <h:inputText id="closeDate" styleClass="closeDate" value="#{ForumTool.selectedTopic.closeDate}"/>

              	</h:panelGroup>
           </h:panelGrid>

			</div>


			

			<script type="text/javascript">
			      localDatePicker({
			      	input:'[id="revise:openDate"]', 
			      	allowEmptyDate: true, 
			      	ashidden: { iso8601: 'openDateISO8601' },
			      	getval:'[id="revise:openDate"]',
			      	useTime:1
			      });
			      
			      localDatePicker({
			      	input:'[id="revise:closeDate"]', 
			      	allowEmptyDate: true, 
			      	ashidden: { iso8601: 'closeDateISO8601' },
			      	getval: '[id="revise:closeDate"]',
			      	useTime:1
			      });
			</script>

		<%--
		   <h4><h:outputText  value="Confidential Responses"/></h4>
		   <h:selectBooleanCheckbox   title= "#{msgs.cdfm_topic_allow_anonymous_postings}"  value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_allow_anonymous_postings}" /> 
		   <br/>
		   <h:selectBooleanCheckbox   title= "#{msgs.cdfm_topic_author_identity}"  value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_author_identity}" />
     
       <h4><h:outputText  value="#{mags.cdfm_topic_post_before_reading}"/></h4>
    	   <p class="shorttext">
			<h:panelGrid columns="2">
				<h:panelGroup><h:outputLabel id="outputLabel4" for="topic_reading"  value="#{msgs.cdfm_topic_post_before_reading_desc}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectOneRadio layout="lineDirection"  id="topic_reading" value="#{ForumTool.selectedTopic.mustRespondBeforeReading}">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				</h:panelGroup>
			</h:panelGrid>
		</p>
		  --%>
      		  
      <h4><h:outputText value="#{msgs.cdfm_forum_mark_read}"/></h4>

			<table><tr><td>
				<p class="indnt1 checkbox"><h:selectBooleanCheckbox
				title="autoMarkThreadsRead"
				value="#{ForumTool.selectedTopic.topicAutoMarkThreadsRead}"
				id="autoMarkThreadsRead">
			</h:selectBooleanCheckbox> <h:outputLabel for="autoMarkThreadsRead"
				value="#{msgs.cdfm_auto_mark_threads_read}" /></p>
				</td></tr></table>
				<h4><h:outputText value="#{msgs.perm_choose_assignment_head}" rendered="#{ForumTool.gradebookExist}" /></h4>
				<h:panelGrid columns="2" rendered="#{ForumTool.gradebookExist && !ForumTool.selectedForum.markForDeletion}" style="margin-top:.5em;clear:both"  styleClass="itemSummary">
			    <h:panelGroup  style="white-space:nowrap;">
						<h:outputLabel for="topic_assignments"  value="#{msgs.perm_choose_assignment}"  ></h:outputLabel>
			  	</h:panelGroup>		
					  <h:panelGroup  styleClass="gradeSelector   itemAction actionItem"> 
						<h:selectOneMenu value="#{ForumTool.selectedTopic.gradeAssign}" id="topic_assignments" disabled="#{not ForumTool.editMode}">
			     	    <f:selectItems value="#{ForumTool.assignments}" />
			  	    </h:selectOneMenu>
									<h:outputText value="#{msgs.perm_choose_assignment_none_t}" styleClass="instrWOGrades" style="display:none;margin-left:0"/>
								<h:outputText value=" #{msgs.perm_choose_instruction_topic} " styleClass="instrWithGrades" style="margin-left:0;"/>
								<h:outputLink value="#" style="text-decoration:none"  styleClass="instrWithGrades"><h:outputText styleClass="displayMore" value="#{msgs.perm_choose_instruction_more_link}"/></h:outputLink>
					    </h:panelGroup>
								<h:panelGroup styleClass="displayMorePanel" style="display:none">
					    </h:panelGroup>
								<h:panelGroup styleClass="itemAction actionItem displayMorePanel" style="display:none">

								<h:outputText styleClass="displayMorePanel" value="#{msgs.perm_choose_instruction_topic_more}"/>
					    </h:panelGroup>
			  </h:panelGrid>

				<h:panelGroup rendered="#{ForumTool.selectedTopic.topic.id==null && !empty ForumTool.siteGroups}">
					<f:verbatim><h4></f:verbatim><h:outputText  value="#{msgs.cdfm_autocreate_topics_header}" /><f:verbatim></h4></f:verbatim>
				</h:panelGroup>
				<div class="indnt1">
					<h:panelGrid columns="1" columnClasses="longtext,checkbox" cellpadding="0" cellspacing="0" >
						<h:panelGroup rendered="#{ForumTool.selectedTopic.topic.id==null && !empty ForumTool.siteGroups}">
							<h:selectOneRadio layout="pageDirection" onclick="this.blur()" onchange="setAutoCreatePanel(this);" disabled="#{not ForumTool.editMode}" id="createTopicsForGroups" value="#{ForumTool.createTopicsForGroups}">
								<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_create_one_topic}"/>
								<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_autocreate_topics_for_groups}"/>
							</h:selectOneRadio>
						</h:panelGroup>
					</h:panelGrid>
				</div>
				<div id="createOneTopicPanel" class="createOneTopicPanel">
					<%@ include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
				</div>

				<div id="createTopicsForGroupsPanel" class="createTopicsForGroupsPanel" style="display:none" >
				<h:panelGroup rendered="#{ForumTool.selectedTopic.topic.id==null && !empty ForumTool.siteGroups}"> 
					<h:outputText value="#{msgs.cdfm_autocreate_topics_desc}" rendered="#{ForumTool.selectedTopic.topic.id==null && !empty ForumTool.siteGroups}" />
					<h:panelGroup styleClass="itemAction">
						<h:outputLink value="#" style="text-decoration:none"  styleClass="instrWithGrades">
							<h:outputText styleClass="displayMoreAutoCreate" value="#{msgs.perm_choose_instruction_more_link}"/>
						</h:outputLink>
					</h:panelGroup>
					<f:verbatim><br/></f:verbatim>
					<h:panelGroup styleClass="displayMoreAutoCreatePanel instruction" style="display:none">
						<h:outputText value="#{msgs.cdfm_autocreate_topics_desc_more}" />
						<h:outputText value="#{ForumTool.autoRolesNoneDesc}" />
						<h:outputText value="#{ForumTool.autoGroupsDesc}" />
					</h:panelGroup>
					<h:dataTable value="#{ForumTool.siteGroups}" var="siteGroup" cellpadding="0" cellspacing="0" styleClass="indnt1 jsfFormTable" 
								 rendered="#{ForumTool.selectedTopic.topic.id==null}">
						<h:column>
							<h:selectBooleanCheckbox value="#{siteGroup.createTopicForGroup}" />
							<h:outputText value="#{siteGroup.group.title}" />
						</h:column>
					</h:dataTable>
				</h:panelGroup>
				</div>
				
<script type="text/javascript">
setPanelId('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
$(function () {
	if (<h:outputText value="#{ForumTool.selectedTopic.topic.id==null && !empty ForumTool.siteGroups && ForumTool.createTopicsForGroups}" />) {
		$("#createOneTopicPanel").hide();
		$("#createTopicsForGroupsPanel").show();
	}
});
</script>
      <div class="act">
          <h:commandButton action="#{ForumTool.processActionSaveTopicSettings}" value="#{msgs.cdfm_button_bar_save_setting}" accesskey="s"
          								 rendered="#{!ForumTool.selectedTopic.markForDeletion}" styleClass="blockMeOnClick"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>    
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>         
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAsDraft}" value="#{msgs.cdfm_button_bar_save_draft}" accesskey="v"
          								 rendered="#{!ForumTool.selectedTopic.markForDeletion}" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionSaveTopicAndAddTopic}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}" accesskey="t"
          								 rendered="#{!ForumTool.selectedTopic.markForDeletion}"  styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/> 
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
                           value="#{msgs.cdfm_button_bar_delete_topic}" accesskey="d" rendered="#{!ForumTool.selectedTopic.markForDeletion && ForumTool.displayTopicDeleteOption}" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopic}" id="delete" accesskey="d"
                           value="#{msgs.cdfm_button_bar_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId" />
          </h:commandButton>
          <h:commandButton  action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="c" />
          <h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
       </div>
       
	 </h:form>
			  <script type="text/javascript">
            $(document).ready(function(){
							$('.displayMore').click(function(e){
									e.preventDefault();
									$('.displayMorePanel').fadeIn('slow')
							})
                            $('.displayMoreAutoCreate').click(function(e){
									e.preventDefault();
									$('.displayMoreAutoCreatePanel').fadeIn('slow')
							})
							if ($('.gradeSelector').find('option').length ===1){
								$('.gradeSelector').find('select').hide();
								$('.gradeSelector').find('.instrWithGrades').hide();
								$('.gradeSelector').find('.instrWOGrades').show();
							}
							
		
				var charRemFormat = $('.charRemFormat').text();
				$(".forum_shortDescriptionClass").charCounter(255, {
					container: ".charsRemaining",
					format: charRemFormat
				 });
			 });				 
        </script>
		
    </sakai:view>
</f:view>
