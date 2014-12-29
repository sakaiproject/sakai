<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.ui.*"%>

<%
	/** if MyWorkspace, display wait page. **/
	FacesContext context = FacesContext.getCurrentInstance();
	Application app = context.getApplication();
	ValueBinding binding = app.createValueBinding("#{mfSynopticBean}");
	MessageForumSynopticBean mfsb = (MessageForumSynopticBean) binding.getValue(context);

	// If user navigates back to tool and in myWorkspace, display wait page first.
	if (! "1".equals(request.getParameter("time")) && mfsb.isMyWorkspace()) {
	   	PrintWriter writer = response.getWriter();
   		writer.println("<script language='JavaScript'>var url = window.location.href;");
  		writer.println("var lastSlash = url.lastIndexOf('/');");
   		writer.println("url = url.substring(0, lastSlash+1) + 'wait?url=' + url.substring(lastSlash+1);");
     	writer.println("window.location.href = url;");
   		writer.println("</script>");
 		return;
	}
	else {
%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
		<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
			<h:form styleClass="specialLink">
	  <sakai:script contextBase="/messageforums-tool" path="/js/popupscripts.js"/>

	  <h:panelGroup rendered="#{mfSynopticBean.myWorkspace}" > 
		<h:outputText value="#{msgs.syn_no_sites}" rendered="#{! mfSynopticBean.sitesToView}" />

		<%-- ===== Display when in MyWorkspace ===== --%>
		<f:subview id="myWorkspaceUnread" >
	 	<h:dataTable value="#{mfSynopticBean.contents}" var="eachSite" styleClass="listHier lines nolines"
	 			rendered = "#{mfSynopticBean.sitesToView}" >
 
       		<h:column>
				<f:facet name="header">
			 		<h:outputText  value="#{msgs.syn_site_heading}"/>
				</f:facet>
			
				<h:outputText value="#{eachSite.siteName}" />
	   		</h:column>

			<h:column>
				<f:facet name="header">
 					<h:outputText  value="#{msgs.syn_private_heading}"/>
 				</f:facet>

				<h:panelGroup rendered="#{eachSite.messagesandForums || eachSite.messages}" >			
									<%-- === To create a link to Messages (& Forums) home page === 
									--%>
											<h:outputText value="#{eachSite.unreadPrivateAmt}" rendered="#{eachSite.unreadPrivateAmt > 0}" style="width:3.5em;display:block;float:left"/>
											<h:panelGroup style="display:block;float:left">
											<h:graphicImage url="/../library/image/silk/email_go.png" rendered="#{eachSite.unreadPrivateAmt > 0}" />	
											<h:outputText value="#{msgs.syn_tool_link_begin}#{eachSite.privateMessagesUrl}';\">
												Read</a>" 
													escape="false" title="#{msgs.syn_goto_messages}" rendered="#{eachSite.unreadPrivateAmt > 0}"
													/>
											<%--
					<h:outputText value="  " rendered="#{eachSite.unreadPrivateAmt > 0}" />
											--%>									
		 			<h:commandLink action="synMain"
						actionListener="#{mfSynopticBean.processReadAll}"
						rendered="#{eachSite.unreadPrivateAmt > 0}"
														title="#{msgs.syn_mark_as_read}">

													<h:graphicImage url="/images/trans.gif" alt="#{msgs.syn_mark_as_read}"
															rendered="#{eachSite.unreadPrivateAmt > 0}"  styleClass="markAsReadIcon"/>
															<h:outputText  value="#{msgs.syn_mark_as_read}"/>
													<%--		
							<h:graphicImage url="/../library/image/silk/email_edit.png" title="#{msgs.syn_mark_as_read}"
								onmouseover="ImageRollOver(this, '/..//library/image/silk/email_open.png');"
								onmouseout="ImageRollOver(this, '/../library/image/silk/email_edit.png');"
								rendered="#{eachSite.unreadPrivateAmt > 0}" />
													--%>	
														
														
							<f:param name="contextId" value="#{eachSite.siteId}" />
					</h:commandLink>
											</h:panelGroup>	
					<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.unreadPrivateAmt == 0}" />			
				</h:panelGroup>
			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText  value="#{msgs.syn_discussion_heading}"/>
 				</f:facet>
 				
				<h:panelGroup rendered="#{eachSite.messagesandForums || eachSite.forums}" > 				
									
										<h:outputText value="#{eachSite.unreadForumsAmt}" rendered="#{eachSite.unreadForumsAmt > 0}" style="width:3.5em;display:block;float:left"/>								
										<h:panelGroup style="display:block;float:left">
					<%-- === To create a link to (Messages &) Forums home page === --%>
										<h:graphicImage url="/../library/image/silk/comments.png" alt="#{msgs.syn_goto_forums}"  rendered="#{eachSite.unreadForumsAmt > 0}"/>
										<h:outputText value="#{msgs.syn_tool_link_begin}#{eachSite.mcPageURL}';\">Read</a>" 
							escape="false" title="#{msgs.syn_goto_forums}" rendered="#{eachSite.unreadForumsAmt > 0}"/>
										<%--
					<h:graphicImage url="#{PrivateMessagesTool.serverUrl}/library/image/silk/email.png" rendered="#{eachSite.unreadForumsAmt > 0}" />
										--%>
									</h:panelGroup>

					<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.unreadForumsAmt == 0}" />
									
				</h:panelGroup>
			</h:column>
	 	</h:dataTable>
	 	</f:subview>
	</h:panelGroup>

	<%-- ====== Dispaly for homepage of a site ===== --%>
	
	<%-- *** Display this if Message & Forums or either piece is not part of site *** --%>
	<h:outputText value="#{msgs.syn_no_mc}" rendered="#{(! mfSynopticBean.myWorkspace) && (! mfSynopticBean.anyMFToolInSite)}" />

	<%-- *** Display this if the user accessing the site isn't logged in *** --%>
	<h:outputText value="#{msgs.syn_anon}" rendered="#{(not mfSynopticBean.loggedIn)}" />

	<%-- *** Display this if Message Center is part of site *** --%>
	<h:panelGrid columns="2" styleClass="listHier lines nolines"
		rendered="#{(! mfSynopticBean.myWorkspace) && mfSynopticBean.anyMFToolInSite && mfSynopticBean.loggedIn}" >
		
		<h:panelGroup rendered="#{mfSynopticBean.messageForumsPageInSite || mfSynopticBean.messagesPageInSite}" >
			<h:outputText value="#{msgs.syn_tool_link_begin}#{mfSynopticBean.siteInfo.privateMessagesUrl}';\">#{msgs.syn_private_heading}</a>"
							escape="false" title="#{msgs.syn_goto_messages}" />
		</h:panelGroup>
		
		<h:panelGroup rendered="#{mfSynopticBean.messageForumsPageInSite || mfSynopticBean.messagesPageInSite}" >
			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{mfSynopticBean.siteInfo.unreadPrivateAmt == 0}" />

			<h:panelGroup rendered="#{mfSynopticBean.siteInfo.unreadPrivateAmt > 0}" >
							<h:outputText value="#{mfSynopticBean.siteInfo.unreadPrivateAmt}" rendered="#{mfSynopticBean.siteInfo.unreadPrivateAmt > 0}" style="width:3.5em;display:block;float:left"/>
							<h:panelGroup style="display:block;float:left">
								<h:graphicImage url="/../library/image/silk/email_go.png" alt="#{msgs.syn_mark_as_read}"
										rendered="#{mfSynopticBean.siteInfo.unreadPrivateAmt > 0}"/>
				<h:outputText 
										value="#{msgs.syn_tool_link_begin}#{mfSynopticBean.siteInfo.privateMessagesUrl}';\">Read</a>"
					escape="false" title="#{msgs.syn_goto_messages}" rendered="#{mfSynopticBean.siteInfo.unreadPrivateAmt > 0}" />
				<h:outputText value="  " rendered="true" />
										<h:outputText escape="false" value="&nbsp;&nbsp;" />
	 			<h:commandLink action="synMain" actionListener="#{mfSynopticBean.processReadAll}" styleClass="active" >
									<h:graphicImage url="/images/trans.gif" alt="#{msgs.syn_mark_as_read}"  styleClass="markAsReadIcon"/>
										<h:outputText value="#{msgs.syn_mark_as_read} " />
					<f:param name="contextId" value="#{eachSite.siteId}" />
				</h:commandLink>
			</h:panelGroup>
		</h:panelGroup>
					</h:panelGroup>
		
		<h:panelGroup rendered="#{mfSynopticBean.messageForumsPageInSite || mfSynopticBean.forumsPageInSite}">
			<h:outputText 
				value="#{msgs.syn_tool_link_begin}#{mfSynopticBean.siteInfo.mcPageURL}';\">#{msgs.syn_discussion_heading}</a>"
				escape="false" title="#{msgs.syn_goto_forums}" />
		</h:panelGroup>
		<h:panelGroup rendered="#{mfSynopticBean.messageForumsPageInSite || mfSynopticBean.forumsPageInSite}">
			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{mfSynopticBean.siteInfo.unreadForumsAmt == 0}" />
						<h:panelGroup>
							<h:outputText value="#{mfSynopticBean.siteInfo.unreadForumsAmt}" rendered="#{mfSynopticBean.siteInfo.unreadForumsAmt > 0}"   style="width:3.5em;display:block;float:left"/>
							<h:panelGroup   style="display:block;float:left">
								<h:graphicImage url="/../library/image/silk/comments.png" alt="#{msgs.syn_goto_forums}"  rendered="#{mfSynopticBean.siteInfo.unreadForumsAmt > 0}"/>
								<h:outputText value="#{msgs.syn_tool_link_begin}#{mfSynopticBean.siteInfo.mcPageURL}';\">Read</a>" 
							escape="false" title="#{msgs.syn_goto_forums}" rendered="#{mfSynopticBean.siteInfo.unreadForumsAmt > 0}"/>
									<%--
									<h:graphicImage url="/messageforums-tool/images/12-em-check.png" rendered="#{mfSynopticBean.siteInfo.unreadForumsAmt > 0}" />
									--%>
		</h:panelGroup>
						</h:panelGroup>
					</h:panelGroup>
	</h:panelGrid>

    <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="markAsRead" class="markasread_popup" 
        style="position:absolute; top: -1000px; left: -1000px;" >
  	  <h:outputText value="#{msgs.syn_mark_as_read}" />
    </div>

    </h:form> 
  </sakai:view>
 </f:view>

 <% } %>