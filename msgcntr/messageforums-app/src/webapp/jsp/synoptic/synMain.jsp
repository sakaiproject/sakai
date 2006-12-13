<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
	<h:form>
	  <sakai:script contextBase="/sakai-messageforums-tool" path="/js/popupscripts.js"/>
	  <h:panelGroup rendered="#{mfSynopticBean.myWorkspace}" > 
       <sakai:tool_bar>
          <sakai:tool_bar_item action="#{mfSynopticBean.processGotoOptions}" value="#{msgs.syn_options}" />
       </sakai:tool_bar>

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
					<h:graphicImage id="waveImg" url="/images/silk/email_edit.png" 
						onmouseover="showPopupHere(this,'markAsRead'); ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_open.png');"
						onmouseout="hidePopup('markAsRead'); ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_edit.png');"
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

				<h:commandLink action="synMain"
					actionListener="#{mfSynopticBean.processReadAll}"
					styleClass="active">
						<h:graphicImage id="waveImgPrivate" url="/images/silk/email_edit.png"
							onmouseover="showPopupHere(this,'markAsRead'); ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_open.png');"
							onmouseout="hidePopup('markAsRead'); ImageRollOver(this, '/sakai-messageforums-tool/images/silk/email_edit.png');" />
						<f:param name="contextId" value="#{mfSynopticBean.siteInfo.siteId}" />
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
 
 