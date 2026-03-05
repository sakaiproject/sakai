<!--jsp/discussionForum/area/dfAreaInclude.jsp-->
<script type="module" src="/webcomponents/bundles/rubric-association-requirements.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
<h:panelGrid columns="1" cellpadding="3" rendered="#{empty ForumTool.forums}">
    <h:panelGroup>
        <h:outputText styleClass="instruction noForumsMessage"  value="#{msgs.cdfm_forum_noforums} "  />
        <h:commandLink  id="create_forum" title="#{msgs.cdfm_new_forum}" value="#{msgs.cdfm_forum_inf_no_forum_create}" action="#{ForumTool.processActionNewForum}" rendered="#{ForumTool.newForum}" />
    </h:panelGroup>
</h:panelGrid>
<h:outputText styleClass="accessUserCheck" style="display:none" rendered="#{ForumTool.newForum}" value="x"/>
<script>
$(document).ready(function() {
    var topicLen = $('.topicBloc').length;
    var forumLen = $('.forumHeader').length;
    var draftForumLen = $('.draftForum').length
    var draftTopicLen = $('.draftTopic').length
    var accessCheck = $('.accessUserCheck').length
    var noForums = $('.noForumsMessage').length

    if (forumLen===1 && draftForumLen ===0 && topicLen===1 && draftTopicLen ===0){
        //probably the default forum adn topic, show an orienting message
        $('.defForums').show();
    }

    // either no topics or all topics are draft - show message in either case
    if((topicLen===0 || draftTopicLen===topicLen) && forumLen !==0){
        if ((topicLen===draftTopicLen) && topicLen!==0){
            $('.noTopicsDraft').show();
        }
        $('.noTopics').show();
        if(topicLen===0){
        $('.noTopicsatAll').show();
        }
    }
    //all forums are draft - show message
    if ((forumLen=== draftForumLen) && forumLen !==0){
        $('.noForumsDraft').show();
        $('.noTopics').hide();
    }
    //no forums because they are all draft or childless- show message to access users
    if (forumLen ===0 && accessCheck === 0 && noForums ===0){
        $('.noForumsAccess').show();
    }
});
</script>

            <h:outputText styleClass="showMoreText"  style="display:none" value="#{msgs.cdfm_show_more_full_description}"  />

    <p class="instruction noForumsAccess"  style="display:none;">
            <h:outputText styleClass="instruction"  value="#{msgs.cdfm_forum_inf_no_forum_access}"  />
    </p>
<f:subview id="maintainMessages" rendered="#{ForumTool.newForum}">
<f:verbatim>
    <p class="instruction defForums highlightPanel"  style="display:none;">
</f:verbatim>
<h:outputText value="#{msgs.cdfm_forum_inf_init_guide}" escape="false" />
<f:verbatim>
    </p>
</f:verbatim>
<f:verbatim>
    <p class="instruction noTopics  highlightPanel" style="display:none">
</f:verbatim>
<h:outputText styleClass="highlight" style="font-weight:bold" value="#{msgs.cdfm_forum_inf_note} " />
<h:outputText escape="false" value="#{msgs.cdfm_forum_inf_no_topics}" styleClass="noTopicsatAll" style="display:none"/>
<f:verbatim>
<span class="noTopicsDraft" style="display:none"><h:outputText value="#{msgs.cdfm_forum_inf_all_topics_draft}" /></span>
    </p>
</f:verbatim>
<f:verbatim>
    <p class="instruction noForumsDraft  highlightPanel" style="display:none"><h:outputText styleClass="highlight" style="font-weight:bold" value="#{msgs.cdfm_forum_inf_note} " />
</f:verbatim>
<!--
<h:outputText escape="false" value="#{msgs.cdfm_forum_inf_no_forums}"/>
-->
<f:verbatim>
<span class="noForumsDraft" style="display:none"><h:outputText value="#{msgs.cdfm_forum_inf_all_forums_draft}" /></span>
</p>
</f:verbatim>
</f:subview>

<div class="table-responsive">
<h:dataTable id="forums" value="#{ForumTool.forums}" rendered="#{!empty ForumTool.forums}" width="100%" var="forum" styleClass="table table-striped table-hover specialLink">
    <h:column>
        <h:panelGroup layout="block" rendered="#{! forum.nonePermission}">
        <h:panelGrid columns="1" styleClass="forumHeader"  border="0">
        <h:panelGroup>
                <h:panelGroup rendered="#{ForumTool.showBulkActions && forum.changeSettings}" styleClass="float-end" layout="block">
                    <h:selectBooleanCheckbox value="#{forum.selected}"
                        styleClass="bulk-select"
                        id="bulkSelectForum"
                        onclick="updateBulkButtons()" />
                    <h:outputLabel for="bulkSelectForum" styleClass="sr-only" value="#{msgs.cdfm_bulk_select_forum}" />
                </h:panelGroup>
                <%-- link to forum and decorations --%>
                <h:outputText styleClass="highlight title draftForum" id="draft" value="#{msgs.cdfm_draft}" rendered="#{forum.forum.draft == 'true'}"/>
                <h:outputText id="draft_space" value=" -  " rendered="#{forum.forum.draft == 'true'}" styleClass="title"/>
                <%-- availability marker --%>
                <h:panelGroup rendered="#{forum.availability == 'false'}" style="margin-right:.5em">
                    <span class="bi bi-calendar-x" aria-hidden="true"></span>
                    <span class="sr-only"><h:outputText value="#{msgs.forum_restricted_message}" escape="false" /></span>
                </h:panelGroup>
                <%-- locked marker --%>
                <h:panelGroup rendered="#{forum.locked == 'true'}" style="margin-right:.5em">
                    <span class="bi bi-lock-fill" aria-hidden="true"></span>
                    <span class="sr-only"><h:outputText value="#{msgs.cdfm_forum_locked}" escape="false" /></span>
                </h:panelGroup>
                <%-- Rubrics marker --%>
                <h:panelGroup rendered="#{forum.hasRubric == 'true'}">
                    <sakai-rubric-student-preview-button
                        site-id='<h:outputText value="#{ForumTool.siteId}" />'
                        display="icon"
                        tool-id="sakai.gradebookng"
                        entity-id="<h:outputText value="#{forum.gradeAssign}" />">
                    </sakai-rubric-student-preview-button>
                </h:panelGroup>
                <h:commandLink action="#{ForumTool.processActionDisplayForum}" title=" #{forum.forum.title}" rendered="#{ForumTool.showForumLinksInNav}"  styleClass="title">
                    <f:param value="#{forum.forum.id}" name="forumId"/>
                    <h:outputText value="#{forum.forum.title}"/>
                </h:commandLink>
                <h:outputText value="#{forum.forum.title}" rendered="#{!ForumTool.showForumLinksInNav}"  styleClass="title" />
                <%-- links to act on this forum --%>

                <h:outputText id="forum_moderated" value=" #{msgs.cdfm_forum_moderated_flag}" styleClass="textPanelFooter" rendered="#{forum.moderated == 'true'}" />
                <h:outputText value=" "  styleClass="actionLinks"/>
              <h:commandLink action="#{ForumTool.processActionNewTopic}" value="#{msgs.cdfm_new_topic}" rendered="#{forum.newTopic}" title="#{msgs.cdfm_new_topic}">
              <f:param value="#{forum.forum.id}" name="forumId"/>
          </h:commandLink>
            <h:outputText  value=" | " rendered="#{forum.changeSettings}"/><%-- gsilver: hiding the pipe when user does not have the ability to change the settings --%>
          <h:commandLink action="#{ForumTool.processActionForumSettings}"  value="#{msgs.cdfm_forum_settings}" rendered="#{forum.changeSettings}" title="#{msgs.cdfm_forum_settings}">
              <f:param value="#{forum.forum.id}" name="forumId"/>
          </h:commandLink>
                <h:outputText  value=" | " rendered="#{ForumTool.newForum || ForumTool.instructor || forum.changeSettings}" />

                <%-- link to display other options on this forum --%>
                <h:panelGroup rendered="#{ForumTool.newForum || ForumTool.instructor || forum.changeSettings}">
                    <button type="button" 
                            class="btn dropdown-toggle" 
                            data-bs-toggle="dropdown" 
                            aria-expanded="false">
                        <h:outputText value="#{msgs.cdfm__moremenulink}" />
                    </button>
                </h:panelGroup>

                <%-- list of options, revealed when link above is used, model new added options on existing ones--%>    
                <ul class="dropdown-menu">
                    <li>
                        <h:commandLink id="duplicate" action="#{ForumTool.processActionDuplicateForumMainConfirm}" value="#{msgs.cdfm_duplicate_forum}" rendered="#{ForumTool.newForum}" styleClass="dropdown-item">
                            <f:param value="#{forum.forum.id}" name="forumId"/>
                        </h:commandLink>
                    </li>

                    <li>
                        <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true" rendered="#{ForumTool.instructor}" styleClass="dropdown-item">
                            <f:param value="" name="topicId"/>
                            <f:param value="#{forum.forum.id}" name="forumId"/>
                            <h:outputText value="#{msgs.cdfm_button_bar_grade}" />
                        </h:commandLink>    
                    </li>

                    <li>
                        <h:commandLink id="delete" action="#{ForumTool.processActionDeleteForumMainConfirm}" value="#{msgs.cdfm_button_bar_delete_forum}" rendered="#{forum.changeSettings}" styleClass="dropdown-item">
                            <f:param value="#{forum.forum.id}" name="forumId"/>
                        </h:commandLink>
                    </li>
                </ul>

<%-- the forum details --%>
                <h:outputText value="#{forum.forum.shortDescription}" styleClass="shortDescription"/>
                <f:subview id="longDesc" rendered="#{!empty forum.attachList || (forum.forum.extendedDescription != '' &&  forum.forum.extendedDescription != null && forum.forum.extendedDescription != '<br/>')}">

                <h:panelGroup>
                    <h:panelGroup layout="block" id="openLinkBlock" styleClass="toggleParent openLinkBlock">
                        <a href="#" id="showMessage" class="toggle show">
                            <span class="bi bi-plus-square" aria-hidden="true"></span>
                            <h:outputText value=" #{msgs.cdfm_read_full_description}" />
                            <h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty forum.attachList}"/>
                            <h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty forum.attachList}"/>
                        </a>
                    </h:panelGroup>
                    <h:panelGroup layout="block" id="hideLinkBlock" styleClass="toggleParent hideLinkBlock display-none">
                        <a href="#" id="hideMessage" class="toggle show">
                            <span class="bi bi-dash-square" aria-hidden="true"></span>
                            <h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
                            <h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty forum.attachList}" />
                            <h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty forum.attachList}"/>
                        </a>
                    </h:panelGroup>
                </h:panelGroup>

                    <h:panelGroup layout="block" id="fullTopicDescription" styleClass="textPanel fullTopicDescription display-none">
                        <h:outputText escape="false" value="#{forum.forum.extendedDescription}" />
                        <%-- attachs --%>
                        <h:dataTable  styleClass="attachListTable" value="#{forum.attachList}" var="eachAttach" rendered="#{!empty forum.attachList}" columnClasses="attach,bogus" border="0" cellpadding="3" cellspacing="0">
                            <h:column>
                                <sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
                                <h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />
                            </h:column>
                            <h:column>
                                <h:outputLink value="#{eachAttach.url}" target="_blank">
                                    <h:outputText value="#{eachAttach.attachment.attachmentName}" />
                                </h:outputLink>
                            </h:column> 
                        </h:dataTable>
                    </h:panelGroup>
                <f:verbatim></div></f:verbatim>
                </f:subview>
                <f:subview id="dates" rendered="#{ForumTool.showAvailabilityDates == 'true' && forum.availabilityRestricted == 'true'}">
                    <h:outputLink id="forum_extended_show2" value="javascript:void(0)" title="#{msgs.cdfm_read_dates}"  styleClass="show" style="#{'display:block'}"
                            onclick="toggleDates($(this).next('.hide'), $('div.toggle2:first', $(this).parents('table.forumHeader')), $(this));">
                        <span class="bi bi-plus-square" aria-hidden="true"></span>
                        <h:outputText value="#{msgs.cdfm_read_dates}" />
                        
                    </h:outputLink>
                    <h:outputLink id="forum_extended_hide2" value="javascript:void(0)" title="#{msgs.cdfm_hide_dates}" styleClass="hide" style="#{'display:none'}"
                            onclick="toggleDates($(this).prev('.show'), $('div.toggle2:first', $(this).parents('table.forumHeader')), $(this));">
                        <span class="bi bi-dash-square" aria-hidden="true"></span>
                        <h:outputText value="#{msgs.cdfm_hide_dates}" />
                    </h:outputLink>
                    <f:subview id="showDate">
                        <f:verbatim><div class="toggle2" style="display:none;"></f:verbatim>
                    </f:subview>
                        <h:panelGrid columns="1" rendered="#{forum.availabilityRestricted == 'true'}">
                            <h:outputText value="#{msgs.openDate}: #{forum.formattedOpenDate}" />
                            <h:outputText value="#{msgs.closeDate}: #{forum.formattedCloseDate}" />
                        </h:panelGrid>
                    <f:verbatim></div></f:verbatim>
                </f:subview>
      </h:panelGroup>
  </h:panelGrid>
      <%-- the topic list  --%>
        <%--//designNote: display a message if there is no topics for this forum , give a prompt to create a topic--%>
        <h:panelGrid columns="1" cellpadding="3" rendered="#{empty forum.topics}" style="margin:0 1em 2em 1em;">
            <h:panelGroup styleClass="instruction">
                <h:outputText escape="false" value="#{msgs.cdfm_forum_inf_no_topic_here} " />
                <h:commandLink action="#{ForumTool.processActionNewTopic}" value="#{msgs.cdfm_forum_inf_no_topic_create}" rendered="#{forum.newTopic}" title="#{msgs.cdfm_new_topic}">
              <f:param value="#{forum.forum.id}" name="forumId"/>
          </h:commandLink>
              
            </h:panelGroup>
        </h:panelGrid> 

        <h:dataTable id="topics" rendered="#{!empty forum.topics}" value="#{forum.topics}" var="topic"  width="100%"   cellspacing="0" cellpadding="0" border="0">
           <h:column>
                        <h:panelGroup layout="block" rendered="#{! topic.nonePermission}">
                    <h:panelGrid columns="1" width="100%" styleClass="specialLink topicBloc" cellpadding="0" cellspacing="0">
                <h:panelGroup>
                            <h:panelGroup rendered="#{ForumTool.showBulkActions && topic.changeSettings}" styleClass="float-end" layout="block">
                                <h:selectBooleanCheckbox value="#{topic.selected}"
                                    styleClass="bulk-select"
                                    id="bulkSelectTopic"
                                    onclick="updateBulkButtons()" />
                                <h:outputLabel for="bulkSelectTopic" styleClass="sr-only" value="#{msgs.cdfm_bulk_select_topic}" />
                            </h:panelGroup>
                            
                            <%-- Show warning icon for topics in draft forums instead of regular folder icon --%>
                            <h:panelGroup rendered="#{forum.forum.draft == 'true'}">
                                <span class="bi bi-exclamation-triangle topicIcon" style="margin-right:.5em; color: var(--bs-warning);" aria-hidden="true"></span>
                                <span class="sr-only"><h:outputText value="#{msgs.cdfm_forum_draft_topic_unavailable}" escape="false" /></span>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{forum.forum.draft != 'true'}">
                                <h:outputText styleClass="bi bi-folder topicIcon" style="margin-right:.5em" rendered="#{topic.unreadNoMessages == 0}" escape="false" />
                                <h:outputText styleClass="bi bi-folder-fill topicIcon" style="margin-right:.5em" rendered="#{topic.unreadNoMessages > 0}" escape="false" />
                            </h:panelGroup>
                            <h:outputText styleClass="highlight title draftTopic" id="draft" value="#{msgs.cdfm_draft}" rendered="#{topic.topic.draft == 'true'}"/>
                            <h:outputText id="draft_space" value="  - " rendered="#{topic.topic.draft == 'true'}" styleClass="title"/>
                            <h:panelGroup rendered="#{topic.availability == 'false'}" style="margin-right:.5em">
                                <span class="bi bi-calendar-x" aria-hidden="true"></span>
                                <span class="sr-only"><h:outputText value="#{msgs.topic_restricted_message}" escape="false" /></span>
                            </h:panelGroup>
                            <h:panelGroup rendered="#{forum.locked == 'true' || topic.locked == 'true'}" style="margin-right:.5em">
                                <span class="bi bi-lock-fill" aria-hidden="true"></span>
                                <span class="sr-only"><h:outputText value="#{msgs.cdfm_forum_locked}" escape="false" /></span>
                            </h:panelGroup>
                            <%-- Rubrics marker --%>
                            <h:panelGroup rendered="#{topic.hasRubric == 'true'}">
                                <sakai-rubric-student-preview-button
                                    site-id='<h:outputText value="#{ForumTool.siteId}" />'
                                    display="icon"
                                    tool-id="sakai.gradebookng"
                                    entity-id="<h:outputText value="#{topic.gradeAssign}" />">
                                </sakai-rubric-student-preview-button>
                            </h:panelGroup>
                            <h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" title=" #{topic.topic.title}" styleClass="#{forum.forum.draft == 'true' ? 'title draft-forum-topic' : 'title'}">
                                <f:param value="#{topic.topic.id}" name="topicId"/>
                                <f:param value="#{forum.forum.id}" name="forumId"/>
                                <h:outputText value="#{topic.topic.title}"/>
                            </h:commandLink>
                            
                            <%-- Indicator for topics in draft forums --%>
                            <h:panelGroup rendered="#{forum.forum.draft == 'true'}" styleClass="draft-forum-topic text-muted">
                                <span style="font-style: italic; font-size: 0.9em; margin-left: 0.5em;">
                                    <h:outputText value="#{msgs.cdfm_forum_draft_topic_unavailable}" />
                                </span>
                            </h:panelGroup>
             
               <%-- // display  singular ('unread message') if unread message is  1 --%> 
               <h:outputText styleClass="childrenNew" id="topic_msg_count55" value="  #{topic.unreadNoMessages} #{msgs.cdfm_lowercase_unread_msg}" 
                             rendered="#{topic.isRead && topic.unreadNoMessages >= 1 && forum.forum.draft != 'true'}"/>   
                       
               <%-- // display  plural ('unread messages') with different style sheet if unread message is 0 --%>  
               <h:outputText styleClass="childrenNewZero" id="topic_msg_count57" value="   #{topic.unreadNoMessages} #{msgs.cdfm_lowercase_unread_msg}" 
                             rendered="#{topic.isRead && topic.unreadNoMessages == 0 && forum.forum.draft != 'true'}"/> 
               
               <%-- // display singular ('message') if total message is 1--%>                   
               <h:outputText styleClass="textPanelFooter" id="topic_msg_count58" value="#{msgs.cdfm_of} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msg}"
                             rendered="#{topic.isRead && topic.totalNoMessages == 1 && forum.forum.draft != 'true'}"/>
                                     
               <%-- // display singular ('message') if total message is 0 or more than 1--%>                   
               <h:outputText styleClass="textPanelFooter" id="topic_msg_count59" value="#{msgs.cdfm_of} #{topic.totalNoMessages} #{msgs.cdfm_lowercase_msgs}"
                             rendered="#{topic.isRead && (topic.totalNoMessages > 1 || topic.totalNoMessages == 0) && forum.forum.draft != 'true'}"/>
                                
               <h:outputText id="topic_moderated" value=" #{msgs.cdfm_forum_moderated_flag}" styleClass="textPanelFooter" rendered="#{topic.moderated == 'true' && topic.isRead && forum.forum.draft != 'true'}" />

                            <%--//desNote: links to act on this topic --%>
                            <h:outputText value=" "  styleClass="actionLinks"/>
                 <h:commandLink action="#{ForumTool.processActionTopicSettings}" id="topic_setting" value="#{msgs.cdfm_topic_settings}" rendered="#{topic.changeSettings}"
                                title=" #{msgs.cdfm_topic_settings}">
                         <f:param value="#{topic.topic.id}" name="topicId"/>
                       <f:param value="#{forum.forum.id}" name="forumId"/>
                     </h:commandLink>
                            <h:outputText  value=" | " rendered="#{forum.newTopic || ForumTool.instructor || topic.changeSettings}"/>

                            <%-- link to display other options on this topic --%>
                            <h:panelGroup rendered="#{forum.newTopic || ForumTool.instructor || topic.changeSettings}">
                                <button type="button" 
                                        class="btn dropdown-toggle" 
                                        data-bs-toggle="dropdown" 
                                        aria-expanded="false">
                                    <h:outputText value="#{msgs.cdfm__moremenulink}" />
                                </button>
                            </h:panelGroup>   

                            <%-- list of options, revealed when link above is used, model new added options on existing ones--%>
                            <ul class="dropdown-menu">
                                <li>
                                    <h:commandLink action="#{ForumTool.processActionDuplicateTopicMainConfirm}" id="duplicate_confirm" value="#{msgs.cdfm_duplicate_topic}" rendered="#{forum.newTopic}"
                                        title=" #{msgs.cdfm_duplicate_topic}" styleClass="dropdown-item">
                                            <f:param value="#{topic.topic.id}" name="topicId"/>
                                            <f:param value="#{forum.forum.id}" name="forumId"/>
                                    </h:commandLink>
                                </li>
                                <li>                           
                                    <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true" rendered="#{ForumTool.instructor}" styleClass="dropdown-item">
                                        <f:param value="#{topic.topic.id}" name="topicId"/>
                                        <f:param value="#{forum.forum.id}" name="forumId"/>
                                        <h:outputText value="#{msgs.cdfm_button_bar_grade}" />
                                    </h:commandLink>
                                </li>
                                <li>
                                    <h:commandLink action="#{ForumTool.processActionDeleteTopicMainConfirm}" id="delete_confirm" value="#{msgs.cdfm_button_bar_delete_topic}" accesskey="d" rendered="#{topic.changeSettings}"
                                        title="#{msgs.cdfm_button_bar_delete_topic}" styleClass="dropdown-item">
                                        <f:param value="#{topic.topic.id}" name="topicId"/>
                                        <f:param value="#{forum.forum.id}" name="forumId"/>
                                    </h:commandLink>
                                </li>
                            </ul>

                            <%--the topic details --%>
                            <h:outputText id="topic_desc" value="#{topic.topic.shortDescription}" styleClass="#{forum.forum.draft == 'true' ? 'shortDescription draft-forum-topic' : 'shortDescription'}" />
                            <f:subview id="longDescTopic" rendered="#{!empty topic.attachList || (topic.topic.extendedDescription != '' &&  topic.topic.extendedDescription != null && topic.topic.extendedDescription != '<br/>')}">

                            <h:panelGroup>
                                <h:panelGroup layout="block" id="openLinkBlock" styleClass="toggleParent openLinkBlock">
                                    <a href="#" id="showMessage" class="toggle show">
                                        <span class="bi bi-plus-square" aria-hidden="true"></span>
                                        <h:outputText value=" #{msgs.cdfm_read_full_description}" />
                                        <h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList}"/>
                                        <h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
                                    </a>
                                </h:panelGroup>
                                <h:panelGroup layout="block" id="hideLinkBlock" styleClass="toggleParent hideLinkBlock display-none">
                                    <a href="#" id="hideMessage" class="toggle show">
                                        <span class="bi bi-dash-square" aria-hidden="true"></span>
                                        <h:outputText value=" #{msgs.cdfm_hide_full_description}"/>
                                        <h:outputText value=" #{msgs.cdfm_and}" rendered="#{!empty topic.attachList}" />
                                        <h:outputText value=" #{msgs.cdfm_attach}" rendered="#{!empty topic.attachList}"/>
                                    </a>
                                </h:panelGroup>
                            </h:panelGroup>

                            <h:panelGroup layout="block" id="fullTopicDescription" styleClass="textPanel fullTopicDescription display-none">
                                <h:outputText escape="false" value="#{topic.topic.extendedDescription}" />
                                <%-- attachs --%>
                                <%--//desNote:attach list --%>
                                <h:dataTable  styleClass="attachListTable" value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" cellpadding="3" cellspacing="0" columnClasses="attach,bogus" border="0">
                                    <h:column>
                                        <span class="bi bi-paperclip" aria-hidden="true"></span>
                                        <h:outputText value=" " />
                                            <h:outputLink value="#{eachAttach.url}" target="_blank">
                                                <h:outputText value="#{eachAttach.attachment.attachmentName}" />
                                            </h:outputLink>
                                        </h:column>
                                </h:dataTable>
                            </h:panelGroup>

                            <f:subview id="hideLongDescTopic">
                                <f:verbatim><div class="toggle" style="display:none;"></f:verbatim>
                            </f:subview>
                    <mf:htmlShowArea  id="topic_fullDescription" hideBorder="true"   value="#{topic.topic.extendedDescription}" />
                                <%--//desNote:attach list --%>
                                <h:dataTable  styleClass="attachListTable" value="#{topic.attachList}" var="eachAttach" rendered="#{!empty topic.attachList}" cellpadding="3" cellspacing="0" columnClasses="attach,bogus" style="font-size:.9em;width:auto;margin-left:1em" border="0">
                      <h:column>
                                        <span class="bi bi-paperclip" aria-hidden="true"></span>
<%--                        <h:outputLink value="#{eachAttach.attachmentUrl}" target="_blank">
                            <h:outputText value="#{eachAttach.attachmentName}" />
                        </h:outputLink>--%>
                                        <h:outputText value=" " />
                        <h:outputLink value="#{eachAttach.url}" target="_blank">
                            <h:outputText value="#{eachAttach.attachment.attachmentName}" />
                        </h:outputLink>               
                    </h:column>
              </h:dataTable>
              <%-- gsilver: need a render attribute on the dataTable here to avoid putting an empty table in response -- since this looks like a stub that was never worked out to display the messages inside this construct - commenting the whole thing out.
             <h:dataTable styleClass="indnt2" id="messages" value="#{topics.messages}" var="message">
              <h:column>
                    <h:outputText id="message_title" value="#{message.message.title}"/>
                    <f:verbatim><br /></f:verbatim>
                    <h:outputText id="message_desc" value="#{message.message.shortDescription}" />
              </h:column>
            </h:dataTable>
            --%>
    <f:verbatim></div></f:verbatim>
    </f:subview>
            <f:subview id="topic_dates" rendered="#{ForumTool.showAvailabilityDates == 'true' && topic.availabilityRestricted == 'true'}">
                <h:outputLink id="forum_extended_show2" value="javascript:void(0)" title="#{msgs.cdfm_read_dates}"  styleClass="show" style="#{'display:block'}"
                        onclick="toggleDates($(this).next('.hide'), $('div.toggle2:first', $(this).parents('tr:first')), $(this));">
                    <span class="bi bi-plus-square" aria-hidden="true"></span> <h:outputText value="#{msgs.cdfm_read_dates}" />
                </h:outputLink>
                <h:outputLink id="forum_extended_hide2" value="javascript:void(0)" title="#{msgs.cdfm_hide_dates}" styleClass="hide" style="#{'display:none'}"
                        onclick="toggleDates($(this).prev('.show'), $('div.toggle2:first', $(this).parents('tr:first')), $(this));">
                    <span class="bi bi-dash-square" aria-hidden="true"></span> <h:outputText value="#{msgs.cdfm_hide_dates}" />
                </h:outputLink>
                <f:subview id="showTopicDate">
                    <f:verbatim><div class="toggle2" style="display:none;"></f:verbatim>
                </f:subview>
                    <h:panelGrid columns="1" rendered="#{topic.availabilityRestricted == 'true'}">
                        <h:outputText value="#{msgs.openDate}: #{topic.formattedOpenDate}" />
                        <h:outputText value="#{msgs.closeDate}: #{topic.formattedCloseDate}" />
                    </h:panelGrid>
                <f:verbatim></div></f:verbatim>
            </f:subview>
                        </h:panelGroup>

                    </h:panelGrid>
            </h:panelGroup>
     </h:column>
      </h:dataTable>            
        </h:panelGroup>
      </h:column>
  </h:dataTable>
</div>
<h:panelGroup rendered="#{ForumTool.showBulkActions}" layout="block" styleClass="navPanel nav_table">
    <h:commandButton value="#{msgs.cdfm_bulk_remove}"
        action="#{ForumTool.processActionBulkRemove}"
        styleClass="btn btn-primary bulk-action me-2" />
    <h:commandButton value="#{msgs.cdfm_bulk_publish}"
        action="#{ForumTool.processActionBulkPublish}"
        styleClass="btn btn-primary bulk-action me-2" />
    <h:commandButton value="#{msgs.cdfm_bulk_unpublish}"
        action="#{ForumTool.processActionBulkUnpublish}"
        styleClass="btn btn-primary bulk-action" />
</h:panelGroup>
<script>
function updateBulkButtons() {
    var anyChecked = document.querySelectorAll('.bulk-select:checked').length > 0;
    var bulkButtons = document.querySelectorAll('.bulk-action');
    for (var i = 0; i < bulkButtons.length; i++) {
        bulkButtons[i].disabled = !anyChecked;
    }
}
document.addEventListener('DOMContentLoaded', function() {
    updateBulkButtons();
});
</script>
