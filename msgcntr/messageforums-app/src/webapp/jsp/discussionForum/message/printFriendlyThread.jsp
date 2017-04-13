<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
	<h:form id="msgForum">

<!--jsp/discussionForum/message/printFriendlyThread.jsp-->
		
			<div class="navIntraTool">
				<a id="printIcon" href="javascript:" onClick="javascript:window.print();">
					<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					<h:outputText value="#{msgs.send_to_printer}" />
				</a>
				<a value="" href="" onClick="window.close();" >
					<h:outputText value="#{msgs.close_window}" />
				</a>
			</div>
			<div class="printBlock">
				<h2>
			      <h:outputText value="#{msgs.cdfm_discussion_forums}" />
					<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				  <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
					<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				  	  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
					<h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " />
				  	  <h:outputText value="#{ForumTool.selectedThreadHead.message.title}" />
				</h2>	
		
		<%--rjlowe: Expanded View to show the message bodies, but not threaded --%>
		<div class="table-responsive">
		<h:dataTable id="expandedMessages" value="#{ForumTool.PFSelectedThread}" var="message" rendered="#{!ForumTool.threaded}"
				styleClass="table table-hover table-striped table-bordered printTable" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column>
				<h:panelGroup styleClass="heading">
					<h:graphicImage value="#{ForumTool.serverUrl}/direct/profile/#{ForumTool.selectedMessage.message.authorId}/image/thumb" 
					alt="#{ForumTool.selectedMessage.message.author}" styleClass="authorImage" rendered="#{ForumTool.showProfileInfo && !message.useAnonymousId}"/>
						<h:outputText value="#{message.message.title}" styleClass="title" />		          	
			          	<h:outputText value=" - #{message.anonAwareAuthor}"/>
			          	<h:outputText value=" #{msgs.cdfm_me}" rendered="#{message.currentUserAndAnonymous}" />
                        <h:outputText value=" #{message.message.created}">
  				   	         <f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
            			</h:outputText>
				</h:panelGroup>
						<mf:htmlShowArea value="#{message.message.body}" hideBorder="false" />		
			</h:column>
		</h:dataTable>
		</div>
		
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
		<div class="table-responsive">
		<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.PFSelectedThread}" var="message" rendered="#{ForumTool.threaded}"
						noarrows="true" styleClass="table table-hover table-striped table-bordered printTable" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column id="_msg_subject">
						<h:panelGroup styleClass="heading">
						<h:graphicImage value="#{ForumTool.serverUrl}/direct/profile/#{message.message.authorId}/image/thumb" 
						    alt="#{message.message.author}" styleClass="authorImage" rendered="#{ForumTool.showProfileInfo && !message.useAnonymousId}"/>
						<h:outputText value="#{message.message.title}" styleClass="title"/>		          	
			          	<h:outputText value=" - #{message.anonAwareAuthor}"/>
			          	<h:outputText value=" #{msgs.cdfm_me}" rendered="#{message.currentUserAndAnonymous}" />
                        <h:outputText value=" #{message.message.created}">
  				   	         <f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
            			</h:outputText>
						</h:panelGroup>
						<mf:htmlShowArea value="#{message.message.body}" hideBorder="false" />
			</h:column>
		</mf:hierDataTable>
		</div>
			</div>	
	</h:form>
</sakai:view>
</f:view>
