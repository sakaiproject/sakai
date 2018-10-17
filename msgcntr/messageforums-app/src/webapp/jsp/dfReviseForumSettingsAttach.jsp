<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.cdfm_discussion_forum_settings}" toolCssHref="/messageforums-tool/css/msgcntr.css">
	<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
	<script type="text/javascript" src="/messageforums-tool/js/jquery.charcounter.js"> </script>
	<script type="text/javascript" src="/messageforums-tool/js/sak-10625.js"></script>
	<script type="text/javascript" src="/messageforums-tool/js/forum.js"></script>
	<script type="text/javascript" src="/messageforums-tool/js/messages.js"></script>
	<script type="text/javascript" src="/messageforums-tool/js/permissions_header.js"></script>
	<script type="text/javascript" src="/messageforums-tool/js/datetimepicker.js"></script>
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

	function updateGradeAssignment(){
		var elems = document.getElementsByTagName('sakai-rubric-association');
		if( document.getElementById("revise:forum_assignments").value != null && document.getElementById("revise:forum_assignments").value != 'Default_0'){
			for (var i = 0; i<elems.length; i++) {
				elems[i].style.display = 'inline';
			}
		} else {
			for (var i = 0; i<elems.length; i++) {
				elems[i].style.display = 'none';
			}
		}
	}

	function setAutoCreatePanel(radioButton) {
		$(".createOneForumPanel").slideToggle("fast");
		$(".createForumsForGroupsPanel").slideToggle("fast", function() {
			if ($('#createForumsForGroupsPanel').is(':hidden')) {
			   document.getElementById("revise:saveandadd").disabled = false;
			}
			else {
			   document.getElementById("revise:saveandadd").disabled = true;
			}
		});		
	}
	</script>
<!-- RUBRICS JAVASCRIPT -->
<script>
  var imports = [
	'/rubrics-service/imports/sakai-rubric-association.html',
	'/rubrics-service/imports/sakai-rubric-grading.html'
  ];
  var Polymerdom = 'shady';
  var rbcstoken = "<h:outputText value="#{ForumTool.rbcsToken}"/>";
</script>
<%
	FacesContext fcontext = FacesContext.getCurrentInstance();
	Application appl = fcontext.getApplication();
	ValueBinding vbinding = appl.createValueBinding("#{ForumTool}");
	DiscussionForumTool forumTool = (DiscussionForumTool) vbinding.getValue(fcontext);
	String stateDetails = forumTool.getRbcsStateDetails();
	String entityId = forumTool.getSelectedForum().getForum().getUuid();
%>
<script src="/rubrics-service/js/sakai-rubrics.js"></script>
<link rel="stylesheet" href="/rubrics-service/css/sakai-rubrics-associate.css">
<!-- END RUBRICS JAVASCRIPT -->

  <!-- Y:\msgcntr\messageforums-app\src\webapp\jsp\dfReviseForumSettingsAttach.jsp -->
    <h:form id="revise">
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
				$('.displayMore').click(function(e){
						e.preventDefault();
						$('.displayMorePanel').fadeIn('slow')
				})
				var charRemFormat = $('.charRemFormat').text();
				$(".forum_shortDescriptionClass").charCounter(255, {
					container: ".charsRemaining",
					format: charRemFormat
				 });
				 updateGradeAssignment();
			 });				 
        </script>
        <h1><h:outputText value="#{msgs.cdfm_discussion_forum_settings}" /></h1>
		<div class="instruction">
		  <h:outputText id="instruction"  value="#{msgs.cdfm_settings_instruction}"/>
		  <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
		</div>
			<h:messages styleClass="messageAlert" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" /> 
     
			<h:panelGrid columns="1" styleClass="jsfFormTable" columnClasses="shorttext">
				<h:panelGroup>	
					<%-- //designNote: does this text input need a maxlength attribute ? --%>
					<h:outputLabel id="outputLabel" for="forum_title" styleClass="block" style="padding-bottom:.3em;display:block;clear:both;float:none">
					<h:outputText id="req_star"  value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>	
						<h:outputText  value="#{msgs.cdfm_forum_title}" />
					</h:outputLabel>	
					<h:inputText size="50" id="forum_title"  maxlength="250" value="#{ForumTool.selectedForum.forum.title}">
						<f:validateLength minimum="1" maximum="255"/>
					</h:inputText>
				</h:panelGroup>	
			</h:panelGrid>
			<%-- //designNote: rendered attr below should resolve to false only if there is no prior short description
			 		and if there is server property (TBD) saying not to use it  - below just checking for pre-existing short description--%>
			<h:panelGrid columns="1"  columnClasses="longtext" rendered="#{ForumTool.showForumShortDescription}">
				<h:panelGroup >
					<h:outputText value="" />
					<%-- //designNote: this label should alert that textarea has a 255 max chars limit --%>
					<h:outputLabel id="outputLabel1" for="forum_shortDescription"  value="#{msgs.cdfm_shortDescription}"/>	
							<h:outputText value="#{msgs.cdfm_shortDescriptionCharsRem}"  styleClass="charRemFormat" style="display:none"/>
							<%--
						
							<h:outputText value="%1 chars remain"  styleClass="charRemFormat" style="display:none"/>
						--%>
							<h:outputText value="" styleClass="charsRemaining" style="padding-left:3em;font-size:.85em;"/>
							<h:outputText value=""  style="display:block"/>
					
					<h:inputTextarea rows="3" cols="45" id="forum_shortDescription"  value="#{ForumTool.selectedForum.forum.shortDescription}" styleClass="forum_shortDescriptionClass" style="float:none"/>
					<h:outputText value="" />
				</h:panelGroup>	
      	</h:panelGrid>
      		
			<%--RTEditor area - if enabled--%>
			<h:panelGroup rendered="#{! ForumTool.disableLongDesc}">
				<h:outputText id="outputLabel2" value="#{msgs.cdfm_fullDescription}" styleClass="labeled"/>
			<sakai:inputRichText textareaOnly="#{PrivateMessagesTool.mobileSession}" rows="#{ForumTool.editorRows}" cols="132" id="df_compose_description" value="#{ForumTool.selectedForum.forum.extendedDescription}">
			</sakai:inputRichText>
	      	</h:panelGroup>
	      	
			
			<%--Attachment area  --%>
	      <h2>
		        <h:outputText value="#{msgs.cdfm_att}"/>
	      </h2>
			<div>
				<%--designNote: would be nice to make this an include, as well as a more comprehensive MIME type check  --%> 
			<h:dataTable styleClass="attachPanel" id="attmsg"  value="#{ForumTool.attachments}" var="eachAttach"  cellpadding="0" cellspacing="0" columnClasses="attach,bogus,specialLink,bogus,bogus" rendered="#{!empty ForumTool.attachments}">
				<h:column>
					<f:facet name="header">   <h:outputText value=" "/>                                          
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
					<f:facet name="header">
						<h:outputText value=" "/>
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
					<%--//designNote: do we really need this info if the lookup has worked? I Suppose till the MIME type check is more comprehensive, yes --%>
						<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
					</h:column>
					</h:dataTable>   

			<h:panelGroup rendered="#{empty ForumTool.attachments}" styleClass="instruction">
				<h:outputText value="#{msgs.cdfm_no_attachments}" />
			</h:panelGroup>
			<p class="act" style="padding:0 0 1em 0;">
				<h:commandButton  action="#{ForumTool.processAddAttachmentRedirect}"
					value="#{msgs.cdfm_button_bar_add_attachment_more_redirect}"  
					style="font-size:96%"
					rendered="#{!empty ForumTool.attachments}"/>
				<h:commandButton  action="#{ForumTool.processAddAttachmentRedirect}"
					value="#{msgs.cdfm_button_bar_add_attachment_redirect}"  
					style="font-size:96%"
					rendered="#{empty ForumTool.attachments}"
					/>
			</p>	
			</div>		
			<%--general posting  forum settings --%>
			<h2>
				<h:outputText value="#{msgs.cdfm_forum_posting}" />
			</h2>
			
				<p class="checkbox">
					<h:selectBooleanCheckbox
						title="ForumLocked" value="#{ForumTool.selectedForum.forumLocked}"
						id="forum_locked">
					</h:selectBooleanCheckbox> <h:outputLabel for="forum_locked" value="#{msgs.cdfm_lock_forum}" />
				</p>
				<p class="checkbox">
					<h:selectBooleanCheckbox
						title="Moderated" value="#{ForumTool.selectedForum.forumModerated}"
						id="moderated">
					</h:selectBooleanCheckbox> <h:outputLabel for="moderated" value="#{msgs.cdfm_moderate_forum}" />
				</p>
				<p class="checkbox">
					<h:selectBooleanCheckbox
						title="postFirst" value="#{ForumTool.selectedForum.forumPostFirst}"
						id="postFirst">
					</h:selectBooleanCheckbox> <h:outputLabel for="postFirst" value="#{msgs.cdfm_postFirst}" />
				</p>

			<h2><h:outputText  value="#{msgs.cdfm_forum_availability}" /></h2>
			
			<div class="indnt1">
			<%-- <h:panelGrid columns="1" columnClasses="longtext,checkbox" cellpadding="0" cellspacing="0"> --%>
              <h:panelGroup styleClass="checkbox">
                 <h:selectOneRadio layout="pageDirection" onclick="this.blur()" onchange="setDatesEnabled(this);" disabled="#{not ForumTool.editMode}" id="availabilityRestricted"  value="#{ForumTool.selectedForum.availabilityRestricted}">
                  <f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_forum_avail_show}"/>
                  <f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_forum_avail_date}"/>
               </h:selectOneRadio>
               </h:panelGroup>
               <h:panelGroup id="openDateSpan" styleClass="indnt2 openDateSpan calWidget" style="display: #{ForumTool.selectedForum.availabilityRestricted ? 'block' : 'none'}">
               	   <h:outputLabel value="#{msgs.openDate}: " for="openDate"/>

	               <h:inputText id="openDate" styleClass="openDate" value="#{ForumTool.selectedForum.openDate}"/>

              	</h:panelGroup>
               <h:panelGroup id="closeDateSpan" styleClass="indnt2 closeDateSpan calWidget" style="display: #{ForumTool.selectedForum.availabilityRestricted ? '' : 'none'}">
              		<h:outputLabel value="#{msgs.closeDate}: " for="closeDate" />
	               <h:inputText id="closeDate" styleClass="closeDate" value="#{ForumTool.selectedForum.closeDate}"/>

              	</h:panelGroup>
           <%-- </h:panelGrid> --%>
 		</div>

 		<script type="text/javascript">
 		      localDatePicker({
 		      	input:'.openDate', 
 		      	allowEmptyDate:true, 
 		      	ashidden: { iso8601: 'openDateISO8601' },
 		      	getval:'.openDate',
 		      	useTime:1 
 		      });

 		      localDatePicker({
 		      	input:'.closeDate', 
 		      	allowEmptyDate:true, 
 		      	ashidden: { iso8601: 'closeDateISO8601' },
 		      	getval:'.closeDate',
 		      	useTime:1 
 		      });
 		</script>

		<h2><h:outputText value="#{msgs.cdfm_forum_mark_read}"/></h2>
			
			<p class="checkbox">
				<h:selectBooleanCheckbox
					title="autoMarkThreadsRead"
					value="#{ForumTool.selectedForum.forumAutoMarkThreadsRead}"
					id="autoMarkThreadsRead">
				</h:selectBooleanCheckbox>
				<h:outputLabel for="autoMarkThreadsRead"	value="#{msgs.cdfm_auto_mark_threads_read}" />
			</p>

	      <%--designNote: gradebook assignment - need to finesse this - make aware that functionality exists, but flag that there are no gb assignmetns to select --%>
				<%--designNote:  How is this a "permission" item? --%>  
				<h2><h:outputText value="#{msgs.perm_choose_assignment_head}" rendered="#{ForumTool.gradebookExist}" /></h2>

				<div class="row form-group" id="forum_grading">
					<h:outputLabel for="forum_assignments" value="#{msgs.perm_choose_assignment}" styleClass="col-md-2 col-sm-2"></h:outputLabel>  
					<div class="col-md-10 col-sm-10">
						<div class="row">
				  		<h:panelGroup  styleClass="gradeSelector  itemAction actionItem"> 
							<h:selectOneMenu id="forum_assignments" onchange="updateGradeAssignment()" value="#{ForumTool.selectedForum.gradeAssign}" disabled="#{not ForumTool.editMode}">
			   	    			<f:selectItems value="#{ForumTool.assignments}" />
			      			</h:selectOneMenu>
							<h:outputText value="#{msgs.perm_choose_assignment_none_f}" styleClass="instrWOGrades" style="display:none;margin-left:0"/>
							<h:outputText value=" #{msgs.perm_choose_instruction_forum} " styleClass="instrWithGrades" style="margin-left:0;"/>
							<h:outputLink value="#" style="text-decoration:none" styleClass="instrWithGrades"><h:outputText styleClass="displayMore" value="#{msgs.perm_choose_instruction_more_link}"/></h:outputLink>
			    		</h:panelGroup>
			    		</div>
			    		<div class="row">
							<h:panelGroup styleClass="displayMorePanel" style="display:none" ></h:panelGroup>
							<h:panelGroup styleClass="itemAction actionItem displayMorePanel" style="display:none" >
								<h:outputText styleClass="displayMorePanel" value="#{msgs.perm_choose_instruction_forum_more}"/>
			    			</h:panelGroup>
			    		</div>
					</div>
				</div>
			
		<sakai-rubric-association styleClass="checkbox" style="margin-left:10px;display:none;"

			dont-associate-label='<h:outputText value="#{msgs.forum_dont_associate_label}" />'
			dont-associate-value="0"
			associate-label='<h:outputText value="#{msgs.forum_associate_label}" />'
			associate-value="1"

			tool-id="sakai.forums"
			<% if(entityId != null && !"".equals(entityId)){ %>
				entity-id=<%= entityId %>
			<%}%>
			<% if(stateDetails != null && !"".equals(stateDetails)){ %>
				state-details=<%= stateDetails %>
			<%}%>

			config-fine-tune-points='<h:outputText value="#{msgs.option_pointsoverride}" />'
			config-hide-student-preview='<h:outputText value="#{msgs.option_studentpreview}" />'

		></sakai-rubric-association>

			<h:panelGroup rendered="#{ForumTool.selectedForum.forum.id==null && !empty ForumTool.siteGroups}">
					<f:verbatim><h4></f:verbatim><h:outputText  value="#{msgs.cdfm_autocreate_forums_header}" /><f:verbatim></h4></f:verbatim>
				</h:panelGroup>
				<div class="indnt1">
					<h:panelGrid columns="1" columnClasses="longtext,checkbox" cellpadding="0" cellspacing="0" >
						<h:panelGroup rendered="#{ForumTool.selectedForum.forum.id==null && !empty ForumTool.siteGroups}">
							<h:selectOneRadio layout="pageDirection" onclick="this.blur()" onchange="setAutoCreatePanel(this);" disabled="#{not ForumTool.editMode}" id="createForumsForGroups" value="#{ForumTool.selectedForum.restrictPermissionsForGroups}">
								<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_create_one_forum}"/>
								<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_autocreate_forums_for_groups}"/>
							</h:selectOneRadio>
						</h:panelGroup>
					</h:panelGrid>
				</div>
				<div id="createOneForumPanel" class="createOneForumPanel">
					<%@ include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
				</div>

				<div id="createForumsForGroupsPanel" class="createForumsForGroupsPanel" style="display:none" >
				<h:panelGroup rendered="#{ForumTool.selectedForum.forum.id==null && !empty ForumTool.siteGroups}"> 
					<h:outputText value="#{msgs.cdfm_autocreate_forums_desc}" rendered="#{ForumTool.selectedForum.forum.id==null && !empty ForumTool.siteGroups}" />
					<h:panelGroup styleClass="itemAction">
						<h:outputLink value="#" style="text-decoration:none"  styleClass="instrWithGrades">
							<h:outputText styleClass="displayMoreAutoCreate" value="#{msgs.perm_choose_instruction_more_link}"/>
						</h:outputLink>
					</h:panelGroup>
					<f:verbatim><br/></f:verbatim>
					<h:panelGroup styleClass="displayMoreAutoCreatePanel instruction" style="display:none">
						<h:outputText value="#{ForumTool.autoRolesNoneDesc}" />
						<h:outputText value="#{ForumTool.autoGroupsDesc}" />
					</h:panelGroup>
					<h:dataTable value="#{ForumTool.siteGroups}" var="siteGroup" cellpadding="0" cellspacing="0" styleClass="indnt1 jsfFormTable" 
								 rendered="#{ForumTool.selectedForum.forum.id==null}">
						<h:column>
							<h:selectBooleanCheckbox value="#{siteGroup.createForumForGroup}" />
							<h:outputText value="#{siteGroup.group.title}" />
						</h:column>
					</h:dataTable>
				</h:panelGroup>
				</div>
				
        
      <div class="act">
          <h:commandButton action="#{ForumTool.processActionSaveForumSettings}" actionListener="#{ForumTool.keepStateDetails}" value="#{msgs.cdfm_button_bar_save_setting}"
          								 rendered="#{!ForumTool.selectedForum.markForDeletion}" accesskey="s" styleClass="blockMeOnClick"> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>         
          </h:commandButton>
				<h:commandButton id="saveandadd" action="#{ForumTool.processActionSaveForumAndAddTopic}" actionListener="#{ForumTool.keepStateDetails}" value="#{msgs.cdfm_button_bar_save_setting_add_topic}" accesskey="t"
          								 rendered = "#{!ForumTool.selectedForum.markForDeletion}" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>  
				<h:commandButton action="#{ForumTool.processActionSaveForumAsDraft}" actionListener="#{ForumTool.keepStateDetails}" value="#{msgs.cdfm_button_bar_save_draft}" accesskey="v"
          								 rendered = "#{!ForumTool.selectedForum.markForDeletion}" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
				<%-- // designNote: these next 2 actions  should be available in the list view instead of here --%>
          <h:commandButton id="delete_confirm" action="#{ForumTool.processActionDeleteForumConfirm}" 
                           value="#{msgs.cdfm_button_bar_delete_forum}" rendered="#{!ForumTool.selectedForum.markForDeletion && ForumTool.displayForumDeleteOption}"
                           accesskey="d" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          
          <h:commandButton id="delete" action="#{ForumTool.processActionDeleteForum}" 
                           value="#{msgs.cdfm_button_bar_delete_forum}" rendered="#{ForumTool.selectedForum.markForDeletion}"
                           accesskey="d"  styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandButton>
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" accesskey="x" />
          <h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
