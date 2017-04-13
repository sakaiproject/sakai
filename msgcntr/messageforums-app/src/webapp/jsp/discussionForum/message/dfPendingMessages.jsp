<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
		<h:form id="msgForum" styleClass="specialLink">
			
	       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>


			<h3>
				<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      		rendered="#{ForumTool.messagesandForums}" />
			  <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussion_forums}" title=" #{msgs.cdfm_discussion_forums}"
			      		rendered="#{ForumTool.forumsTool}" />
      	<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				<h:outputText value="#{msgs.cdfm_msg_pending_queue_title}" />
			</h3>
			<script type="text/javascript">
				$(document).ready(function() {
				$('.table table-hover table-striped table-bordered tr th a').fadeTo("fast",0.50)	
			    //deactivate remove link if nothing checked
			    $(':checkbox').click(function(){
			        var makeActive = false
			        $('tr').removeClass('selectedSelected');
			        $('td :checkbox').each(function(){
			            if (this.checked) {
			                makeActive = true
			                $(this).parents("tr").addClass('selectedSelected');
			            }
			        });
			        if (makeActive) {
						$('.table table-hover table-striped table-bordered tr th a').fadeTo("fast",1)
			        }
			        else {
						$('.table table-hover table-striped table-bordered tr th a').fadeTo("fast",0.50)			        }

			    });




				});
			</script>
			
		<div class="instruction">
				<p>	
			<h:outputText value="#{msgs.cdfm_deny_with_comments_msg}" rendered="#{ForumTool.numPendingMessages > 0}" />
	  	<h:outputText value="#{msgs.cdfm_no_pending_msgs}" rendered="#{ForumTool.numPendingMessages < 1}" />
				</p>	
	  </div>
	  
	  <h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" rendered="#{! empty facesContext.maximumSeverity}"/>
	  <div class="table-responsive">
		<h:dataTable id="pendingMsgs" value="#{ForumTool.pendingMessages}" width="100%" var="message" 
				columnClasses="bogus,nopadd" styleClass="table table-hover table-striped table-bordered specialLink" rendered="#{ForumTool.numPendingMessages >0 }" cellpadding="0" cellspacing="0">
			<h:column>
				<f:facet name="header">
					<h:selectBooleanCheckbox title="#{msgs.cdfm_checkall}" id="mainCheckbox" onclick="javascript:selectDeselectCheckboxes(this.id, document.forms[0]);"/>
				</f:facet>
					<h:selectBooleanCheckbox value="#{message.selected}" id="childCheckbox" onclick="javascript:resetMainCheckbox('msgForum:pendingMsgs:mainCheckbox');"/>
			</h:column>
			<h:column>
				<f:facet name="header">
						<h:panelGroup   styleClass="messageActions">
						<h:commandLink id="denyMsgs" title="#{msgs.cdfm_button_bar_deny}" action="#{ForumTool.markCheckedAsDenied}">
		  				<h:graphicImage value="/../../library/image/silk/cross.png" alt="#{msgs.cdfm_button_bar_deny}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_deny} " />
		  			</h:commandLink>
						<h:commandLink id="approveMsgs" title="#{msgs.cdfm_button_bar_approve}" action="#{ForumTool.markCheckedAsApproved}" style="padding-left: 1.0em; padding-right: 1.0em;">
		  				<h:graphicImage value="/../../library/image/silk/tick.png" alt="#{msgs.cdfm_button_bar_approve}" />
		  				<h:outputText value=" #{msgs.cdfm_button_bar_approve}" />
		  		</h:commandLink>
					</h:panelGroup>
				</f:facet>
<%--					<f:verbatim><div class="title"></f:verbatim>
				
						<h:commandLink action="#{ForumTool.processActionDisplayForum}"  value="#{message.message.topic.openForum.title}" title=" #{message.message.topic.openForum.title}" styleClass="title">
		        <f:param value="#{message.message.topic.openForum.id}" name="forumId"/>
	      </h:commandLink>
						<h:outputText value=" / " styleClass="title"/>
						<h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{message.message.topic.title}" title=" #{message.message.topic.title}" styleClass="title">
					      <f:param value="#{message.message.topic.id}" name="topicId"/>
					      <f:param value="#{message.message.topic.openForum.id}" name="forumId"/>
				      </h:commandLink>
--%>						
						<f:verbatim></div><div class="pendingApproval" style="padding-left:.5em;width:98%;"></f:verbatim> 
					<h:commandLink action="#{ForumTool.processActionDisplayMessage}" title="#{message.message.title}" styleClass="title">
					<h:outputText value="#{message.message.title}" />
					<f:param value="#{message.message.id}" name="messageId"/>
		      <f:param value="#{message.message.topic.id}" name="topicId"/>
		      <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
				</h:commandLink>
					<h:outputText value=" #{msgs.cdfm_dash} " styleClass="textPanelFooter"/>
					<h:outputText value="#{message.anonAwareAuthor}" styleClass="textPanelFooter #{message.useAnonymousId ? 'anonymousAuthor' : ''}"/>
					<h:outputText value=" #{msgs.cdfm_me}" styleClass="textPanelFooter" rendered="#{message.currentUserAndAnonymous}" />
					<h:outputText value=" #{msgs.cdfm_openb}" styleClass="textPanelFooter"/>
					<h:outputText value="#{message.message.created}" styleClass="textPanelFooter">
			  	<f:convertDateTime pattern="#{msgs.date_format}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
			  </h:outputText>
				<h:outputText value="#{msgs.cdfm_closeb}" styleClass="textPanelFooter" />
					<%--designNote: need to i18N --%>
					<h:outputText value=" - #{msgs.cdfm_in} "/>
					<h:commandLink action="#{ForumTool.processActionDisplayForum}"  value="#{message.message.topic.openForum.title}" title=" #{message.message.topic.openForum.title}">
						<f:param value="#{message.message.topic.openForum.id}" name="forumId"/>
					</h:commandLink>
					<h:outputText value=" / " />
					<h:commandLink action="#{ForumTool.processActionDisplayTopic}" id="topic_title" value="#{message.message.topic.title}" title=" #{message.message.topic.title}">
						<f:param value="#{message.message.topic.id}" name="topicId"/>
						<f:param value="#{message.message.topic.openForum.id}" name="forumId"/>
					</h:commandLink>
					<%--designNote:  sort of useless to check for message.message.body as it always seems to exist *if only with a line break*--%>
					<h:outputText value="#{message.message.body}" escape="false"  rendered= "#{message.message.body}" styleClass="textPanel" style="display:block"/>
					<%--designNote: how about attachments? --%>
					<f:verbatim></div></f:verbatim>
			</h:column>
		</h:dataTable>
	</div>
	</h:form>
</sakai:view>
</f:view>
