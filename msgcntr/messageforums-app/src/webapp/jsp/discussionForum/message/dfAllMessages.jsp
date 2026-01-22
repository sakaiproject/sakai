<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view title="Forums">
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
<link rel="stylesheet" href="/messageforums-tool/css/msgcntr.css" type="text/css" />
<link rel="stylesheet" href="/messageforums-tool/css/msgcntr_move_thread.css" type="text/css" />

<!-- messageforums-app/src/webapp/jsp/discussionForum/message-->

<script>includeLatestJQuery("msgcntr");</script>
<script src="/messageforums-tool/js/json2.js"></script>
<script src="/messageforums-tool/js/fluidframework-min.js"></script>
<script src="/messageforums-tool/js/Scroller.js"></script>
<script src="/messageforums-tool/js/forum.js"></script>
<script src="/messageforums-tool/js/frameAdjust.js"></script>
<script src="/messageforums-tool/js/forum_movethread.js"></script>

<script src="/messageforums-tool/js/sak-10625.js"></script>
<script type="module" src="/webcomponents/bundles/rubric-association-requirements.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>

<!--jsp/discussionForum/message/dfAllMessages.jsp-->
		<link rel="stylesheet" type="text/css" href="../../css/TableSorter.css" />
		<script>includeWebjarLibrary('jquery.tablesorter');</script>
		<script src="/messageforums-tool/js/forumTopicThreadsSorter.js"></script>
		<script>
 		jQuery(document).ready(function(){
 			//sort forum threads
 			$('#msgForum\\:messagesInHierDataTable').threadsSorter();
			//add handles to list for thread operat
			instrumentThreads('msgForum\\:messagesInHierDataTable');

			var menuLink = $('#forumsMainMenuLink');
			var menuLinkSpan = menuLink.closest('span');
			menuLinkSpan.addClass('current');
			menuLinkSpan.html(menuLink.text());

 		});

        function disableMoveLink() {
            var linkid = "msgForum:df_move_message_commandLink";
            var movelink = document.getElementById(linkid);
        	movelink.style.color="grey";
        }

        function enableMoveLink() {
            var linkid = "msgForum:df_move_message_commandLink";
            var movelink = document.getElementById(linkid);
        	movelink.style.color="";
        }
 
        // this is  called from messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/HierDataTableRender.java. 
        // checkbox is encoded there.  
        function enableDisableMoveThreadLink() {
            //function to check total number of CheckBoxes that are checked in a form
            //initialize total count to zero
            var totalChecked = 0;
            //get total number of CheckBoxes in form
            if (typeof document.forms['msgForum'].moveCheckbox.length === 'undefined') {
                // when there is just one checkbox moveCheckbox is not an array,  document.forms['msgForum'].moveCheckbox.length returns undefined
                if (document.forms['msgForum'].moveCheckbox.checked == true ) {
                    totalChecked += 1;
                }
            } else {
                // more than one checkbox is checked.
                var chkBoxCount = document.forms['msgForum'].moveCheckbox.length;
                //loop through each CheckBox
                for (var i = 0; i < chkBoxCount; i++) {
                    //check the state of each CheckBox
                    if (eval("document.forms['msgForum'].moveCheckbox[" + i + "].checked") == true)
                    {
                        //it's checked so increment the counter
                        totalChecked += 1;
                    }
                }
            }
            if (totalChecked >0) {
                // enable the move link
                enableMoveLink();
            }
            else {
                disableMoveLink();
            }
        
         }

 		</script>
		<h:outputText styleClass="showMoreText"  style="display:none" value="#{msgs.cdfm_show_more_full_description}"  />

	<%--//
		//plugin required below
		<script src="/messageforums-tool/js/pxToEm.js"></script>
		
		/*
		gsilver: get a value representing max indents
	 	from the server configuraiton service or the language bundle, parse 
		all the indented items, and if the item indent goes over the value, flatten to the value 
		*/
		<script>
		$(document).ready(function() {
			// pick value from element (that gets it from language bundle)
			maxThreadDepth =$('#maxthreaddepth').text()
			// double check that this is a number
			if (isNaN(maxThreadDepth)){
				maxThreadDepth=10
			}
			// for each message, if the message is indented more than the value above
			// void that and set the new indent to the value
			$("td.messageTitle").each(function (i) {
				paddingDepth= $(this).css('padding-left').split('px');
				if ( paddingDepth[0] > parseInt(maxThreadDepth.pxToEm ({scope:'body', reverse:true}))){
					$(this).css ('padding-left', maxThreadDepth + 'em');
				}
			});
		});

		</script>	
		// element into which the value gets insert and retrieved from
		<span class="highlight"  id="maxthreaddepth" class="skip"><h:outputText value="#{msgs.cdfm_maxthreaddepth}" /></span>
//--%>
	<h:form id="msgForum" rendered="#{!ForumTool.selectedTopic.topic.draft || ForumTool.selectedTopic.topic.createdBy == ForumTool.userId}">
        <%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
        <f:subview id="picker2">
            <%@ include file="moveThreadPicker.jsp" %>
        </f:subview>

 		<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
 			<h:panelGroup styleClass="itemNav">
			   <h:outputText styleClass="button formButtonDisabled" value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
				 <h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" 
				                title=" #{msgs.cdfm_previous_topic}">
					 <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
					 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				 </h:commandLink>
				 <h:outputText  styleClass="button formButtonDisabled" value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
				 <h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" 
				                title=" #{msgs.cdfm_next_topic}">
					<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
					<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				 </h:commandLink>
			 </h:panelGroup>
			<h:panelGrid columns="2" width="100%" styleClass="specialLink">
			    <h:panelGroup>
					<f:verbatim><div class="specialLink"><h1></f:verbatim>
			      <h:commandLink action="#{ForumTool.processActionHome}" title=" #{msgs.cdfm_message_forums}" rendered="#{ForumTool.messagesandForums}">
						<h:outputText value="#{msgs.cdfm_message_forums}"/>
					</h:commandLink>
			      <h:commandLink action="#{ForumTool.processActionHome}" title=" #{msgs.cdfm_discussions}" rendered="#{ForumTool.forumsTool}" >
							<h:outputText value="#{msgs.cdfm_discussions}"/>
						</h:commandLink>
      			  <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" title="#{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}">
						<h:outputText value="#{ForumTool.selectedForum.forum.title}"/>
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
					  <h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
					  <h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
					  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
						<%--//designNote: up arrow should go here - get decent image and put title into link. --%>
						<h:commandLink action="#{ForumTool.processActionDisplayForum}"  title="#{msgs.cdfm_up_level_title}" rendered="#{ForumTool.showForumLinksInNav}" style="margin-left:.3em">
							<span class="bi bi-arrow-90deg-up" aria-hidden="true"></span>
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						</h:commandLink>
					  <f:verbatim></h1></div></f:verbatim>
				 </h:panelGroup>
			</h:panelGrid>

		<h:panelGroup id="forumActions" layout="block">
			<h:commandLink styleClass="button" value="#{msgs.cdfm_container_title_thread}" action="#{ForumTool.processAddMessage}" id="df_componse_message_dfAllMessages" 
				rendered="#{ForumTool.selectedTopic.isNewResponse && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}"/>&nbsp;
			<h:commandLink styleClass="button" value="#{msgs.cdfm_flat_view}" action="#{ForumTool.processActionDisplayFlatView}"/>&nbsp;
			<h:commandLink styleClass="button" action="#{ForumTool.processActionTopicSettings}" id="topic_setting" rendered="#{ForumTool.selectedTopic.changeSettings}">
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
				<h:outputText value="#{msgs.cdfm_topic_settings}"/>
			</h:commandLink>&nbsp;
			<h:commandLink styleClass="button" action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
				value="#{msgs.cdfm_button_bar_delete_topic}" accesskey="d" rendered="#{!ForumTool.selectedTopic.markForDeletion && ForumTool.displayTopicDeleteOption}">
				<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
			</h:commandLink>&nbsp;
			<h:outputLink styleClass="button" id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyUrl}');">
				<span class="bi bi-printer" aria-hidden="true"></span>
				<span class="sr-only"><h:outputText value="#{msgs.print_friendly}" /></span>
			</h:outputLink>
 		</h:panelGroup>

			<h:panelGrid columns="1" width="100%"  styleClass="topicBloc topicBlocLone specialLink"  cellspacing="0" cellpadding="0">
				<h:panelGroup>
					<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{ForumTool.selectedTopic.topic.draft == 'true'}"/>
					<h:outputText id="draft_space" value="  - " rendered="#{ForumTool.selectedTopic.topic.draft == 'true'}" styleClass="title"/>
					<h:panelGroup rendered="#{ForumTool.selectedTopic.topic.availability == 'false'}">
						<span class="bi bi-calendar-x" aria-hidden="true" style="margin-right:.5em"></span>
						<h:outputText value="#{msgs.topic_restricted_message}" />
					</h:panelGroup>
					<h:panelGroup rendered="#{ForumTool.selectedForum.locked == 'true' || ForumTool.selectedTopic.locked == 'true'}">
						<span class="bi bi-lock-fill" aria-hidden="true" style="margin-right:.5em"></span>
					</h:panelGroup>
					<%-- Rubrics marker --%>
					<h:panelGroup rendered="#{ForumTool.selectedTopic.hasRubric == 'true'}" >
					  <sakai-rubric-student-preview-button
						  site-id='<h:outputText value="#{ForumTool.siteId}" />'
						  display="icon"
						  tool-id="sakai.gradebookng"
						  entity-id="<h:outputText value="#{ForumTool.selectedTopic.gradeAssign}" />">
					  </sakai-rubric-student-preview-button>
					</h:panelGroup>
					<h:outputText value="#{ForumTool.selectedTopic.topic.title}" styleClass="title"/>

			         <h:outputText id="topic_moderated" value=" #{msgs.cdfm_forum_moderated_flag}" styleClass="childrenNewZero" rendered="#{ForumTool.selectedTopic.moderated == 'true' }" />
					
					  <%--//designNote: for paralellism to other views, need to add read/unread count here as well as Moderated attribute--%>  
					  <%-- 
					  <h:outputText value=" #{msgs.cdfm_openb}" styleClass="textPanelFooter"/> 
					  <h:outputText value="123 messages - 5 unread" styleClass="textPanelFooter todo" />
					  <h:outputText id="topic_moderated" value="  #{msgs.cdfm_topic_moderated_flag}"  styleClass="textPanelFooter" rendered="#{ForumTool.selectedTopic.topic.moderated == 'true'}" />
					  <h:outputText value="#{msgs.cdfm_closeb}" styleClass="textPanelFooter"/>
					  --%>
					  <h:outputText value=" "  styleClass="actionLinks"/>

					<h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" rendered="#{!empty ForumTool.selectedTopic.topic.shortDescription}" styleClass="shortDescription" />
					
					<h:panelGroup rendered="#{!empty ForumTool.selectedTopic.attachList || ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null && ForumTool.selectedTopic.topic.extendedDescription != '<br/>'}">
						<p id="openLinkBlock" class="toggleParent openLinkBlock">
							<a href="#" id="showMessage" class="toggle show">
								<span class="bi bi-plus-square" aria-hidden="true"></span>
								<h:outputText value=" #{msgs.cdfm_read_full_description}" />
								<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty ForumTool.selectedTopic.attachList}"/>
								<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty ForumTool.selectedTopic.attachList}"/>
							</a>
						</p>
						<p id="hideLinkBlock" class="toggleParent hideLinkBlock display-none">
							<a href="#" id="hideMessage" class="toggle show">
								<span class="bi bi-dash-square" aria-hidden="true"></span>
								<h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
								<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty ForumTool.selectedTopic.attachList}" />
								<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty ForumTool.selectedTopic.attachList}"/>
							</a>
						</p>
					</h:panelGroup>

					<f:verbatim><div id="fullTopicDescription" class="textPanel fullTopicDescription"></f:verbatim>
						<div>
							<h:outputText escape="false" value="#{ForumTool.selectedTopic.topic.extendedDescription}" />
						</div>
						<div class="table">
						<h:dataTable styleClass="table table-hover table-striped table-bordered" value="#{ForumTool.selectedTopic.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.attachList}" cellpadding="3" cellspacing="0" columnClasses="attach,bogus">
					  <h:column>
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt=""  />						
						</h:column>
						<h:column>
						<h:outputLink value="#{eachAttach.url}" target="_blank">
							<h:outputText value="#{eachAttach.attachment.attachmentName}" />
						</h:outputLink>				  
					</h:column>
			  </h:dataTable>
			</div>
					<f:verbatim></div></f:verbatim>
				</h:panelGroup>
			</h:panelGrid>	
				<%--<%@ include file="dfViewSearchBar.jsp"%> --%>
				
				<h:panelGroup rendered="#{ForumTool.selectedTopic.isMovePostings}" >
				<f:verbatim><div class="post_move_links"></f:verbatim>
                    <%-- hidden link to call ForumTool.processMoveThread  --%>
                    <h:commandLink value="" action="#{ForumTool.processMoveThread}" id="hidden_move_message_commandLink" ></h:commandLink>
					<h:commandLink value="" action="$('.topic-picker').dialog('close');" id="hidden_close_move_thread" ></h:commandLink>

                    <%-- link for Move Thread(s)  --%>
					<f:verbatim>
                        <a class="button display-topic-picker" id="msgForum:df_move_message_commandLink" onclick="resizeFrameForDialog();" href="#" >
				    </f:verbatim>
                    <h:outputText value="#{msgs.move_thread}" />
					</a>
					</div>
   				</h:panelGroup>
			<%--//designNote: need a rendered attribute here that will toggle the display of the table (if messages) or a textblock (class="instruction") if there are no messages--%>
			<h:outputText styleClass="messageAlert" value="#{msgs.cdfm_postFirst_warning}" rendered="#{ForumTool.selectedTopic != null && ForumTool.needToPostFirst}"/>				
			<h:outputText value="#{msgs.cdfm_no_messages}" rendered="#{ForumTool.selectedTopic == null || (empty ForumTool.selectedTopic.messages && !ForumTool.needToPostFirst)}"  styleClass="sak-banner-info" style="display:block"/>
			<%--//gsilver: need a rendered attribute here that will toggle the display of the table (if messages) or a textblock (class="instruction") if there are no messages--%> 						
            <div id="checkbox">
			<mf:hierDataTable styleClass="specialLink allMessages" id="messagesInHierDataTable" rendered="#{!empty ForumTool.messages}"  value="#{ForumTool.messages}" var="message" expanded="#{ForumTool.expanded}"
					columnClasses="attach, attach,messageTitle,attach,bogus,bogus">		
			<h:column id="_checkbox">
				<f:facet name="header">
					<h:graphicImage value="/images/expand-collapse.gif" style="vertical-align:middle" alt="#{msgs.expandAll}" title="#{msgs.expandAll}" />
				</f:facet>
			</h:column>
			<h:column id="_toggle">
			</h:column>
			<h:column id="_msg_subject">
				<f:facet name="header">
				        <h:outputLink value="#" title="#{msgs.sort_thread}">
				          <h:outputText value="#{msgs.cdfm_thread}" /> 
					</h:outputLink>
				</f:facet>

				<%-- moved--%>
				<h:panelGroup rendered="#{message.moved}">
					<h:outputText styleClass="textPanelFooter" escape="false" value="| #{message.message.title} - " />
					<h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" styleClass="title">
						<h:outputText value="#{msgs.moved}" />
                                                  <f:param value="#{message.message.topic.id}" name="topicId"/>
                                                  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
                                          </h:commandLink>

					<h:outputText escape="false" styleClass="textPanelFooter"  value="#{message.message.topic.title}" />
				</h:panelGroup>
				<%-- moved--%>

				<%-- NOT moved--%>
				<h:panelGroup rendered="#{!message.moved}">
				<h:outputText escape="false" value="<a id=\"#{message.message.id}\" name=\"#{message.message.id}\"></a>" />
					<%-- display deleted message linked if any child messages (not deleted)
						displays the message "this message has been deleted" if the message has been, um deleted, leaves reply children in place --%>
					<h:panelGroup styleClass="inactive firstChild" rendered="#{message.deleted && message.depth == 0 && message.childCount > 0}">
					<h:commandLink action="#{ForumTool.processActionDisplayThread}" immediate="true" title="#{msgs.cdfm_msg_deleted_label}" >
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
       	    			<f:param value="#{message.message.id}" name="messageId"/>
   	    		    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
    			    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
					</h:commandLink>
				</h:panelGroup>
				
					<h:panelGroup rendered="#{!message.deleted}" styleClass="firstChild">
				
					<h:outputText styleClass="messageNew" value=" #{msgs.cdfm_newflag}" rendered="#{!message.read}"/>	

					<%-- message has been submitted and is pending approval by moderator --%>
						<h:outputText value="#{msgs.cdfm_msg_pending_label}" styleClass="messagePending" rendered="#{message.msgPending}" />
						<%-- message has been submitted and has bene denied  approval by moderator --%>
						<h:outputText value="#{msgs.cdfm_msg_denied_label}"  styleClass="messageDenied"  rendered="#{message.msgDenied}" />
					<%-- Rendered to view current thread only --%>
							<%--//designNote:  not sure what this controls - seems to affect all threads except the deleted, pending and denied--%>
					<h:commandLink styleClass="messagetitlelink" action="#{ForumTool.processActionDisplayThread}" immediate="true" title="#{message.message.title}"
						rendered="#{message.depth == 0}">
				   		<h:outputText value="#{message.message.title}" rendered="#{message.read && message.childUnread == 0 }" />
							
    	        		<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read || message.childUnread > 0}"/>
	       	    		<f:param value="#{message.message.id}" name="messageId"/>
    	    	    	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
   	    		    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	    	      	</h:commandLink>
				</h:panelGroup>

				<%-- Rendered to view current message only --%>
					<%-- shows the message "This message has been deleted" if the message has been deleted --%>
					<h:panelGroup styleClass="inactive firstChild" rendered="#{message.deleted && (message.depth != 0 || message.childCount == 0)}" >
					<span>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
					</span>
				</h:panelGroup>

					<%-- render the message that has not been deleted, what else? --%>
				<h:panelGroup rendered="#{!message.deleted}">	          	
					<h:commandLink styleClass="messagetitlelink" action="#{ForumTool.processActionDisplayMessage}" immediate="true" title=" #{message.message.title}"
						rendered="#{message.depth != 0}" >
					   	<h:outputText value="#{message.message.title}" rendered="#{message.read}" />
    	    	    	<h:outputText styleClass="unreadMsg" value="#{message.message.title}" rendered="#{!message.read}"/>
    	   	    		<f:param value="#{message.message.id}" name="messageId"/>
       		    		<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
        		    	<f:param value="#{ForumTool.selectedTopic.topic.baseForum.id}" name="forumId"/>
	          		</h:commandLink>
						<%-- //designNote: icon to mark as read, does it belong here? Is it the right icon? Is this functionality desired?--%>
						<%--
		          	<h:outputText value="  " />
				
						<h:graphicImage value="/images/trans.gif" rendered="#{!message.read}"
							alt="#{msgs.cdfm_mark_as_read}" title="#{msgs.cdfm_mark_as_read}"
							onclick="doAjax(#{message.message.id}, #{ForumTool.selectedTopic.topic.id}, this);" styleClass="markAsReadIcon"/>
						--%>	
				</h:panelGroup>
					<%--  thread metadata (count) --%>
					<%-- designNote: debug block --%>
					<%--
					<h:outputText value=" md: #{message.depth}" /> 
					<h:outputText value=" cc: #{message.childCount }" />
					<h:outputText value=" cu: #{message.childUnread }" />
					<h:outputText value= " mr: #{message.read}" />
					--%>
                    <%-- // display  ('unread ') if unread message is>= 1 --%>
                    <h:outputText styleClass="childrenNew childrenNewNumber" id="topic_msg_count55" value="#{(message.childUnread) + (message.read ? 0 : 1)}"
                                  rendered="#{message.depth == 0 && ((message.childUnread) + (message.read ? 0 : 1)) >= 1}"/>
                    <h:outputText styleClass="childrenNew" id="topic_msg_count56" value="#{msgs.cdfm_lowercase_unread_msg}"
                                  rendered="#{message.depth == 0 && ((message.childUnread) + (message.read ? 0 : 1)) >= 1}"/>
   
                    <%-- // display ('unread ') with different style sheet if unread message is 0 --%>  
                    <h:outputText styleClass="childrenNewZero" id="topic_msg_count57" value="  #{(message.childUnread) + (message.read ? 0 : 1)} #{msgs.cdfm_lowercase_unread_msg}" 
                                  rendered="#{message.depth == 0 && ((message.childUnread) + (message.read ? 0 : 1)) == 0}"/> 
                               
                     <%-- // display singular ('message') if total message is 1--%>                   
                     <h:outputText styleClass="textPanelFooter" id="topic_msg_count58" value="#{msgs.cdfm_of} #{message.childCount + 1} #{msgs.cdfm_lowercase_msg}"
                                   rendered="#{message.depth == 0 && message.childCount ==0}"  />
                                                                           
                     <%-- // display singular ('message') if total message is 0 or more than 1--%>                   
                     <h:outputText styleClass="textPanelFooter" id="topic_msg_count59" value="#{msgs.cdfm_of} #{message.childCount + 1} #{msgs.cdfm_lowercase_msgs}"
                                   rendered="#{message.depth == 0 && message.childCount >=1}"  />
				<%-- NOT moved--%>
				</h:panelGroup>
			</h:column>
				<%-- author column --%>
			<h:column rendered="#{ForumTool.selectedTopic.isMarkAsRead}">
				<f:facet name="header"><h:outputText value="#{msgs.cdfm_mark_as_read}" escape="false"/></f:facet>
				<h:outputLink rendered="#{!message.read}" value="javascript:void(0);" title="#{msgs.cdfm_mark_as_read}" styleClass="markAsReadIcon button"
							  onclick="doAjax(#{message.message.id}, #{ForumTool.selectedTopic.topic.id}, this);">
					<h:outputText value="#{msgs.cdfm_mark_as_read}"/>
				</h:outputLink>
			</h:column>
			<h:column>
				<f:facet name="header">
				   <h:outputLink value="#" title="#{msgs.sort_author}">
					<h:outputText value="#{msgs.cdfm_authoredby}" />
				   </h:outputLink>
				</f:facet>
				<h:panelGroup rendered="#{!message.deleted}" >
                    <h:outputText value="#{message.anonAwareAuthor}" rendered="#{!ForumTool.instructor || message.useAnonymousId}" styleClass="#{message.useAnonymousId ? 'anonymousAuthor' : ''}" />
                    <h:outputText value=" #{msgs.cdfm_me}" rendered="#{message.currentUserAndAnonymous}" />
                    <h:commandLink action="#{mfStatisticsBean.processActionStatisticsUser}" immediate="true" title=" #{message.anonAwareAuthor}" rendered="#{ForumTool.instructor && !message.useAnonymousId}" styleClass="#{message.useAnonymousId ? 'anonymousAuthor' : ''}">
                        <f:param value="#{message.authorEid}" name="siteUserId"/>
                        <h:outputText value="#{message.anonAwareAuthor}" />
                    </h:commandLink>
				</h:panelGroup>
			</h:column>
				<%-- date column --%>
			<h:column>
				<f:facet name="header">
				    <h:outputLink value="#" title="#{msgs.sort_date}">
					<h:outputText value="#{msgs.cdfm_date}" />
				    </h:outputLink>
				</f:facet>
				<h:panelGroup rendered="#{!message.deleted}" >
					<h:outputText value="#{message.message.created}" rendered="#{message.read}">
						<f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
					</h:outputText>
					<h:outputText styleClass="unreadMsg" value="#{message.message.created}" rendered="#{!message.read}">
						<f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
					</h:outputText>
				</h:panelGroup>
			</h:column> 
		</mf:hierDataTable>
</div>

<f:verbatim><br/><br/></f:verbatim>
<h:panelGrid columns="1" width="100%" styleClass="specialLink">
	 <h:panelGroup styleClass="itemNav">
	   <h:outputText styleClass="button formButtonDisabled"  value="#{msgs.cdfm_previous_topic}"  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
		 <h:commandLink styleClass="button" action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}"  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" 
		                title=" #{msgs.cdfm_previous_topic}">
			 <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
			 <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		 </h:commandLink>	
		 <h:outputText  styleClass="button formButtonDisabled" value="#{msgs.cdfm_next_topic}" rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
		 <h:commandLink  styleClass="button" action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}" rendered="#{ForumTool.selectedTopic.hasNextTopic}" 
		                title=" #{msgs.cdfm_next_topic}">
			<f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
			<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
		 </h:commandLink>
	 </h:panelGroup>
</h:panelGrid>
		
<input type="hidden" id="selectedTopicid" name="selectedTopicid" class="selectedTopicid" value="0" />
<input type="hidden" id="moveReminder" name="moveReminder" class="moveReminder" value="false" />

		<h:inputHidden id="mainOrForumOrTopic" value="dfAllMessages" />
		<%
  String thisId = request.getParameter("panel");
  if (thisId == null) 
  {
    thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  }
%>
			<script>
			function resize(){
  				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  			}
			</script> 
	</h:form>

	<h:outputText value="#{msgs.cdfm_insufficient_privileges_view_topic}" rendered="#{ForumTool.selectedTopic.topic.draft && ForumTool.selectedTopic.topic.createdBy != ForumTool.userId}" />
		
</sakai:view>
</f:view>
