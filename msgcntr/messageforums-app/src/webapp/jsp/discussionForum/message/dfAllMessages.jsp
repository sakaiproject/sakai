<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
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

<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
<script type="text/javascript" src="/messageforums-tool/js/json2.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/fluidframework-min.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/Scroller.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/forum.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/frameAdjust.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/forum_movethread.js"></script>

<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>

<!--jsp/discussionForum/message/dfAllMessages.jsp-->
		<link rel="stylesheet" type="text/css" href="../../css/TableSorter.css" />
 		<script type="text/javascript" src="/library/js/jquery/tablesorter/2.0.3/jquery.tablesorter-2.0.3.min.js"></script>
 		<sakai:script contextBase="/messageforums-tool" path="/js/forumTopicThreadsSorter.js"/>
 		<script type="text/javascript">
 		jQuery(document).ready(function(){
 			//sort forum threads
 			$('#msgForum\\:messagesInHierDataTable').threadsSorter();
			//add handles to list for thread operat
			instrumentThreads('msgForum\\:messagesInHierDataTable');
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
		<sakai:script contextBase="/messageforums-tool" path="/js/pxToEm.js"/>
		
		/*
		gsilver: get a value representing max indents
	 	from the server configuraiton service or the language bundle, parse 
		all the indented items, and if the item indent goes over the value, flatten to the value 
		*/
		<script type="text/javascript">
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
        <f:subview id="picker2">
            <%@ include file="moveThreadPicker.jsp" %>
        </f:subview>

		<sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}">
   			<h:commandLink action="#{ForumTool.processAddMessage}" id="df_componse_message_dfAllMessages"
		            rendered="#{ForumTool.selectedTopic.isNewResponse && !ForumTool.selectedTopic.locked && !ForumTool.selectedForum.locked == 'true'}">
					<h:outputText value="#{msgs.cdfm_container_title_thread}"/>
				</h:commandLink>

			<h:commandLink action="#{ForumTool.processActionDisplayFlatView}">
					<h:outputText value="#{msgs.cdfm_flat_view}"/>
				</h:commandLink>

      			<h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting"
      				rendered="#{ForumTool.selectedTopic.changeSettings}">
					<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
					<h:outputText value="#{msgs.cdfm_topic_settings}"/>
				</h:commandLink>
				
				<h:outputLink id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyUrl}');">
					<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
				</h:outputLink>
 		</sakai:tool_bar>
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
			      <h:commandLink action="#{ForumTool.processActionHome}" title=" #{msgs.cdfm_discussion_forums}" rendered="#{ForumTool.forumsTool}" >
							<h:outputText value="#{msgs.cdfm_discussion_forums}"/>
						</h:commandLink>
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:commandLink action="#{ForumTool.processActionDisplayForum}" title="#{ForumTool.selectedForum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}">
						<h:outputText value="#{ForumTool.selectedForum.forum.title}"/>
						  <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
					  </h:commandLink>
					  <h:outputText value="#{ForumTool.selectedForum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"/>
					  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
					  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
						<%--//designNote: up arrow should go here - get decent image and put title into link. --%>
						<h:commandLink action="#{ForumTool.processActionDisplayForum}"  title="#{msgs.cdfm_up_level_title}" rendered="#{ForumTool.showForumLinksInNav}" style="margin-left:.3em">
							<h:graphicImage url="/images/silk/arrow_turn_up.gif" style="vertical-align:top;padding:0;margin-top:-2px" alt="" />	
							<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
						</h:commandLink>
					  <f:verbatim></h1></div></f:verbatim>
				 </h:panelGroup>
			</h:panelGrid>
		
			<h:panelGrid columns="1" width="100%"  styleClass="topicBloc topicBlocLone specialLink"  cellspacing="0" cellpadding="0">
				<h:panelGroup>
					<h:outputText styleClass="highlight title" id="draft" value="#{msgs.cdfm_draft}" rendered="#{ForumTool.selectedTopic.topic.draft == 'true'}"/>
					<h:outputText id="draft_space" value="  - " rendered="#{ForumTool.selectedTopic.topic.draft == 'true'}" styleClass="title"/>
					<h:graphicImage url="/images/silk/date_delete.png" title="#{msgs.topic_restricted_message}" alt="#{msgs.topic_restricted_message}" rendered="#{ForumTool.selectedTopic.topic.availability == 'false'}" style="margin-right:.5em"/>
					<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedForum.forum.locked == 'true' || ForumTool.selectedTopic.topic.locked == 'true'}" style="margin-right:.5em"/>
					  
					
					
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
					


					<h:outputText   value="#{ForumTool.selectedTopic.topic.shortDescription}" rendered="#{ForumTool.selectedTopic.topic.shortDescription} != ''}"  styleClass="shortDescription" />
					
					
					
						<h:outputLink id="forum_extended_show" value="#" title="#{msgs.cdfm_view}" styleClass="show"
								rendered="#{!empty ForumTool.selectedTopic.attachList || ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null && ForumTool.selectedTopic.topic.extendedDescription != '<br/>'}"
							onclick="resize();$(this).next('.hide').toggle(); $('div.toggle').slideToggle(resize);$(this).toggle();$(this).toggleClass('opened');">
								<h:graphicImage url="/images/collapse.gif" alt="" /><h:outputText value="#{msgs.cdfm_view}" />
								<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null && ForumTool.selectedTopic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty ForumTool.selectedTopic.attachList && ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null && ForumTool.selectedTopic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty ForumTool.selectedTopic.attachList}"/>
					  </h:outputLink>
				   
			
					<h:outputLink id="forum_extended_hide" value="#" title="#{msgs.cdfm_hide}" style="display:none " styleClass="hide" 
								rendered="#{!empty ForumTool.selectedTopic.attachList || ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null && ForumTool.selectedTopic.topic.extendedDescription != '<br/>'}"
							onclick="resize();$(this).prev('.show').toggle(); $('div.toggle').slideToggle(resize);$(this).toggle();">
								<h:graphicImage url="/images/expand.gif" alt="" /><h:outputText value="#{msgs.cdfm_hide}" />
								<h:outputText value=" #{msgs.cdfm_full_description}" rendered="#{ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null && ForumTool.selectedTopic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty ForumTool.selectedTopic.attachList && ForumTool.selectedTopic.topic.extendedDescription != '' && ForumTool.selectedTopic.topic.extendedDescription != null && ForumTool.selectedTopic.topic.extendedDescription != '<br/>'}"/>
								<h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty ForumTool.selectedTopic.attachList}"/>
					  </h:outputLink>
					
					<f:verbatim><div class="toggle" style="display:none"></f:verbatim>
						<mf:htmlShowArea  id="forum_fullDescription" hideBorder="true"	 value="#{ForumTool.selectedTopic.topic.extendedDescription}"/> 
						<div class="table-responsive">
						<h:dataTable styleClass="table table-hover table-striped table-bordered" value="#{ForumTool.selectedTopic.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.attachList}" cellpadding="3" cellspacing="0" columnClasses="attach,bogus" style="font-size:.9em;width:auto;margin-left:1em">
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
					<f:verbatim></a></f:verbatim>
					<f:verbatim></div></f:verbatim>
   				</h:panelGroup>
			<%--//designNote: need a rendered attribute here that will toggle the display of the table (if messages) or a textblock (class="instruction") if there are no messages--%>
			<h:outputText styleClass="messageAlert" value="#{msgs.cdfm_postFirst_warning}" rendered="#{ForumTool.selectedTopic != null && ForumTool.needToPostFirst}"/>				
			<h:outputText value="#{msgs.cdfm_no_messages}" rendered="#{ForumTool.selectedTopic == null || (empty ForumTool.selectedTopic.messages && !ForumTool.needToPostFirst)}"  styleClass="instruction" style="display:block"/>
			<%--//gsilver: need a rendered attribute here that will toggle the display of the table (if messages) or a textblock (class="instruction") if there are no messages--%> 						
            <div id="checkbox">
			<mf:hierDataTable styleClass="specialLink allMessages" id="messagesInHierDataTable" rendered="#{!empty ForumTool.messages}"  value="#{ForumTool.messages}" var="message" expanded="#{ForumTool.expanded}"
					columnClasses="attach, attach,messageTitle,attach,bogus,bogus">		
			<h:column id="_checkbox">
				<f:facet name="header">
				</f:facet>
			</h:column>
			<h:column id="_toggle">
				<f:facet name="header">
					<h:commandLink action="#{ForumTool.processActionToggleExpanded}" immediate="true" title="#{msgs.cdfm_collapse_expand_all}">
							<h:graphicImage value="/images/collapse-expand.gif" style="vertical-align:middle" rendered="#{ForumTool.expanded == 'true'}" alt=""  />
							<h:graphicImage value="/images/expand-collapse.gif" style="vertical-align:middle" rendered="#{ForumTool.expanded != 'true'}" alt=""  />
					</h:commandLink>
				</f:facet>
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
					<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
					<f:verbatim></span></f:verbatim>
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
                    <h:outputText styleClass="childrenNew" id="topic_msg_count55" value="  #{(message.childUnread) + (message.read ? 0 : 1)} #{msgs.cdfm_lowercase_unread_msg}" 
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
			<h:column>
				<f:facet name="header">
						<h:outputText value="&nbsp;" escape="false"/>
					</f:facet>
               	
					<h:graphicImage value="/images/trans.gif" rendered="#{message.read}" style="margin-left:.5em" alt="" />
					<h:graphicImage value="/images/trans.gif" rendered="#{!message.read}"
						alt="#{msgs.cdfm_mark_as_read}" title="#{msgs.cdfm_mark_as_read}"
						onclick="doAjax(#{message.message.id}, #{ForumTool.selectedTopic.topic.id}, this);" styleClass="markAsReadIcon"/>
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
			<script type="text/javascript">
			function resize(){
  				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  			}
			</script> 
<h:outputText escape="false" value="<script type='text/javascript'>$(document).ready(function() {setupLongDesc()});</script>"  rendered="#{!ForumTool.showShortDescription}"/>
	</h:form>

	<h:outputText value="#{msgs.cdfm_insufficient_privileges_view_topic}" rendered="#{ForumTool.selectedTopic.topic.draft && ForumTool.selectedTopic.topic.createdBy != ForumTool.userId}" />
		
</sakai:view>
</f:view>
