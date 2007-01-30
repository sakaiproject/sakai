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
  <sakai:view>
	<h:form>
	  <sakai:script contextBase="/sakai-messageforums-tool" path="/js/popupscripts.js"/>

	  <h:panelGroup rendered="#{mfSynopticBean.myWorkspace}" > 

	<h:outputText value="#{msgs.syn_no_sites}" rendered="#{! mfSynopticBean.sitesToView}" />

	 <%-- ===== Display when in MyWorkspace ===== --%>
	 <h:dataTable value="#{mfSynopticBean.contents}" var="eachSite" styleClass="listHier lines nolines"
	 		rendered = "#{mfSynopticBean.sitesToView}" >
 
       	<h:column>
			<f:facet name="header">
			 <h:outputText  value="#{msgs.syn_site_heading}"/>
			</f:facet>
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{eachSite.mcPageURL}';\">" />
			
			<h:outputText value="#{eachSite.siteName}" title="#{msgs.syn_goto_mc}" />
			<f:verbatim></a></f:verbatim>
	   	</h:column>

		<h:column>
			<f:facet name="header">
 				<h:outputText  value="#{msgs.syn_private_heading}"/>
 			</f:facet>
 			<%-- === To create a link to Message Center home page === --%>
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{eachSite.privateMessagesUrl}';\">" />
			<h:outputText value="#{eachSite.unreadPrivateAmt}" title="#{msgs.syn_goto_messages}" rendered="#{eachSite.unreadPrivateAmt > 0}" />
			<f:verbatim></a></f:verbatim>
			<h:outputText value="  " rendered="#{eachSite.unreadPrivateAmt > 0}" />

 			<h:commandLink action="synMain"
				actionListener="#{mfSynopticBean.processReadAll}"
				rendered="#{eachSite.unreadPrivateAmt > 0}"
				styleClass="active">
					<h:graphicImage id="waveImg" url="/images/silk/email_edit.png" title="#{msgs.syn_mark_as_read}"
						onmouseover="ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_open.png');"
						onmouseout="ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_edit.png');"
						rendered="#{eachSite.unreadPrivateAmt > 0}" />
					<f:param name="contextId" value="#{eachSite.siteId}" />
			</h:commandLink>

			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.unreadPrivateAmt == 0}" />			
		</h:column>

		<h:column>
			<f:facet name="header">
				<h:outputText  value="#{msgs.syn_discussion_heading}"/>
 			</f:facet>
			<!-- === To create a link to Message Center home page === -->
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{eachSite.mcPageURL}';\">" />
			
			<h:outputText value="#{eachSite.unreadForumsAmt}" title="#{msgs.syn_goto_forums}" rendered="#{eachSite.unreadForumsAmt > 0}" />
			<f:verbatim></a></f:verbatim>
			<h:outputText value="  " rendered="#{eachSite.unreadForumsAmt > 0}" />

			<h:graphicImage url="/images/silk/email.png" rendered="#{eachSite.unreadForumsAmt > 0}" />

			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.unreadForumsAmt == 0}" />
		</h:column>
	 </h:dataTable>

   </h:panelGroup>

	<%-- ====== Dispaly for homepage of a site ===== --%>
	
	<%-- *** Display this if Message Center is not part of site *** --%>
	<h:outputText value="#{msgs.syn_no_mc}" rendered="#{(! mfSynopticBean.myWorkspace) && ! mfSynopticBean.messageForumsPageInSite}" />

	<%-- *** Display this if Message Center is part of site *** --%>
	<h:panelGrid columns="2" styleClass="listHier lines nolines"
		rendered="#{(! mfSynopticBean.myWorkspace) && mfSynopticBean.messageForumsPageInSite}" >
		<h:panelGroup>
			<h:panelGroup rendered="#{mfSynopticBean.pmEnabled}" >
				<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
				<h:outputText escape="false" value="#{mfSynopticBean.siteInfo.privateMessagesUrl}';\">" />
			</h:panelGroup>
			
			<h:outputText  value="#{msgs.syn_private_heading}" title="#{msgs.syn_goto_messages}" />
			<h:panelGroup rendered="#{mfSynopticBean.pmEnabled}" >
				<f:verbatim></a></f:verbatim>
			</h:panelGroup>
		</h:panelGroup>
		
		<h:panelGroup>
			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{mfSynopticBean.siteInfo.unreadPrivateAmt == 0}" />

			<h:panelGroup rendered="#{mfSynopticBean.siteInfo.unreadPrivateAmt > 0}" >
				<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
				<h:outputText escape="false" value="#{mfSynopticBean.siteInfo.privateMessagesUrl}';\">"  />

				<h:outputText value="#{mfSynopticBean.siteInfo.unreadPrivateAmt}" title="#{msgs.syn_goto_messages}" />
				<f:verbatim></a></f:verbatim>
				<h:outputText value="  " rendered="true" />

	 			<h:commandLink action="synMain" actionListener="#{mfSynopticBean.processReadAll}" styleClass="active">
					<h:graphicImage id="waveImg" url="/images/silk/email_edit.png" title="#{msgs.syn_mark_as_read}" 
						onmouseover="ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_open.png');"
						onmouseout="ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_edit.png');" />
					<f:param name="contextId" value="#{eachSite.siteId}" />
			</h:commandLink>
			</h:panelGroup>
		</h:panelGroup>
		
		<h:panelGroup>
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{mfSynopticBean.siteInfo.mcPageURL}';\">" />

			<h:outputText  value="#{msgs.syn_discussion_heading}" title="#{msgs.syn_goto_forums}" />
			<f:verbatim></a></f:verbatim>
		</h:panelGroup>

		<h:panelGroup>
			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{mfSynopticBean.siteInfo.unreadForumsAmt == 0}" />
			
			<h:panelGroup rendered="#{mfSynopticBean.siteInfo.unreadForumsAmt > 0}" >
				<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
				<h:outputText escape="false" value="#{mfSynopticBean.siteInfo.mcPageURL}';\">" />

				<h:outputText value="#{mfSynopticBean.siteInfo.unreadForumsAmt}" title="#{msgs.syn_goto_forums}"  />
				<f:verbatim></a></f:verbatim>
				<h:outputText value="  " rendered="true" />

				<h:graphicImage id="waveImgForum" url="/images/silk/email.png" />
			</h:panelGroup>
		</h:panelGroup>
	</h:panelGrid>

    </h:form> 

    <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="markAsRead" class="markasread_popup" 
        style="position:absolute; top: -1000px; left: -1000px;" >
  	  <h:outputText value="#{msgs.syn_mark_as_read}" />
    </div>

  </sakai:view>
 </f:view>

 <% } %>