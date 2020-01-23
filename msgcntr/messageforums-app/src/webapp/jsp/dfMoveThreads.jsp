<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <sakai:view title="#{msgs.cdfm_container_title}" toolCssHref="/messageforums-tool/css/msgcntr.css">
    <!--jsp/dfMoveThreads.jsp-->
        <h:form id="dfCompose">
			<f:verbatim><input type="hidden" id="currentMessageId" name="currentMessageId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedMessage.message.id}"/><f:verbatim>"/></f:verbatim>
			<f:verbatim><input type="hidden" id="currentTopicId" name="currentTopicId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedTopic.topic.id}"/><f:verbatim>"/></f:verbatim>
			<f:verbatim><input type="hidden" id="currentForumId" name="currentForumId" value="</f:verbatim><h:outputText value="#{ForumTool.selectedForum.forum.id}"/><f:verbatim>"/></f:verbatim>
            <script>includeLatestJQuery("msgcntr");</script>
            <script src="/messageforums-tool/js/sak-10625.js"></script>
            <script src="/messageforums-tool/js/forum.js"></script>
            <script>
                $(document).ready(function() {
                    $('#openLinkBlock').hide();
                    jQuery('.toggle').click(function(e) { 
                        $('#replytomessage').toggle('slow');
                        $('.toggleParent').toggle();                    
                         resizeFrame('grow')
                    });                        
                });            
            </script>
    
            <h3><h:outputText value="#{msgs.cdfm_tool_bar_message}" /></h3>
            <table class="topicBloc topicBlocLone specialLink">
                <tr>
                    <td>
                        <h:outputText value="#{ForumTool.selectedForum.forum.title} /  #{ForumTool.selectedTopic.topic.title}"  styleClass="title"/> 
                        <div class="textPanel">
                            <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
                        </div>
                        <div>
                            <h:commandLink immediate="true" 
                                           action="#{ForumTool.processDfComposeToggle}" 
                                           onmousedown="document.forms[0].onsubmit();"
                                           rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
                                           title="#{msgs.cdfm_read_full_description}"
                                           styleClass="show">
                                <h:graphicImage url="/images/collapse.gif" alt="" />
                                <h:outputText value="#{msgs.cdfm_read_full_description}" />
                                <f:param value="dfCompose" name="redirectToProcessAction"/>
                                <f:param value="true" name="composeExpand"/>
                            </h:commandLink>
                            <h:commandLink immediate="true" 
                                           action="#{ForumTool.processDfComposeToggle}" 
                                           onmousedown="document.forms[0].onsubmit();"
                                           rendered="#{ForumTool.selectedTopic.readFullDesciption}"
                                           title=" #{msgs.cdfm_hide_full_description}"
                                           styleClass="hide">
                                <h:graphicImage url="/images/expand.gif" alt="" />
                                <h:outputText value="#{msgs.cdfm_hide_full_description}" />
                                <f:param value="dfCompose" name="redirectToProcessAction"/>
                            </h:commandLink>                    
                             
                            <%-- //designNote: am assuming that the thinking is that once the user is here 
                                   there is no longer need for the long description context (or as much), so do not put it in
                                   the response by default - same goes for attachment list if any --%>
                            <mf:htmlShowArea value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
                                             rendered="#{ForumTool.selectedTopic.readFullDesciption}"
                                             id="topic_extended_description" 
                                             hideBorder="true" />
                        </div>
                    </td>
                </tr>
            </table>
            <div class="instruction">             
                <h:outputText value="#{msgs.cdfm_required}"/>
                <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
            </div>
            <h:message for="df_compose_title" warnStyle="WARN" styleClass="messageAlert"/>
            <h:panelGrid styleClass="jsfFormTable" columns="1" width="100%" border="0" cellpadding="0">
                <h:panelGroup>
                    <h:outputLabel for="df_compose_title" style="padding-bottom:.3em;display:block;clear:both;float:none">
                    <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
                    <h:outputText value="#{msgs.cdfm_title}"/>
                </h:outputLabel>
                    <h:inputText value="#{ForumTool.composeTitle}" style="width:30em;" maxlength="250" required="true" id="df_compose_title">
                    <f:validator validatorId="MessageTitle" />
                    <f:validateLength minimum="1" maximum="255"/>
                    </h:inputText>
               </h:panelGroup>
            </h:panelGrid>
    
            <p><h:message for="df_compose_body" styleClass="messageAlert" id="bodyErrorMessages" /></p>
            <h:outputText value="#{msgs.cdfm_message}" />
                
            <span style="margin-left:3em"><img src="/library/image/silk/table_add.png" />&nbsp;<h:outputText value="#{msgs.cdfm_message_count}" />:&nbsp;<span  id="counttotal"> </span></span>
    
            <sakai:inputRichText value="#{ForumTool.composeBody}" id="df_compose_body" rows="22" cols="132">
            </sakai:inputRichText>
            
            <%--********************* Attachment *********************--%>    
            <h4><h:outputText value="#{msgs.cdfm_att}" /></h4>
            <p>            
                <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}" styleClass="instruction" style="display:block"/>
            </p>    
                
            <h:dataTable styleClass="attachPanel" id="attmsg" width="100%" value="#{ForumTool.attachments}"  rendered="#{!empty ForumTool.attachments}" var="eachAttach"  columnClasses=",itemAction specialLink,," cellpadding="0" cellspacing="0" style="width:auto">
                <h:column rendered="#{!empty ForumTool.attachments}">
                    <f:facet name="header">
                        <h:outputText value="#{msgs.cdfm_title}"/>
                    </f:facet>
                    <sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>                                    
                    <h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt=""  />                            
                    <h:outputText value=" "/>
                    <h:outputText value="#{eachAttach.attachment.attachmentName}"/>
                </h:column>
                <h:column>
                    <h:commandLink action="#{ForumTool.processDeleteAttach}" 
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
                
            <p style="padding:0" class="act">
                <h:commandButton action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_redirect}"
                                 immediate="true" accesskey="a" style="font-size:96%"  rendered="#{empty ForumTool.attachments}"/>
                <h:commandButton action="#{ForumTool.processAddAttachmentRedirect}" value="#{msgs.cdfm_button_bar_add_attachment_more_redirect}"
                                 immediate="true" accesskey="a" style="font-size:96%"  rendered="#{!empty ForumTool.attachments}"/>
            </p>               
            <p style="padding:0" class="act">
                <h:commandButton action="#{ForumTool.processDfMsgPost}" value="#{msgs.cdfm_button_bar_post_message}" accesskey="s" styleClass="active"/>
                <h:commandButton action="#{ForumTool.processDfMsgCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="x" />
            </p>
        </h:form>
    </sakai:view>
</f:view> 
