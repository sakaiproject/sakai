<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
	<h:form>
	 <h:panelGroup rendered="#{mfSynopticBean.myWorkspace}" > 
       <sakai:tool_bar>
          <sakai:tool_bar_item action="#{mfSynopticBean.processGotoOptions}" value="#{msgs.syn_options}" />
       </sakai:tool_bar>

	<h:outputText value="syn_no_sites" rendered="#{! mfSynopticBean.sitesToView}" />

	 <%-- ===== Display when in MyWorkspace ===== --%>
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
 			<%-- === To create a link to Message Center home page === --%>
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{eachSite.mcPageURL}';\">" />

			<h:outputText value="#{eachSite.unreadPrivateAmt}  " rendered="#{eachSite.unreadPrivateAmt > 0}" />
			<f:verbatim></a></f:verbatim>

			<h:graphicImage id="waveImg" url="/images/silk/email.png" rendered="#{eachSite.unreadPrivateAmt > 0}" />

 			<h:commandLink action="synMain"
				actionListener="#{mfSynopticBean.processReadAll}"
				rendered="#{eachSite.unreadPrivateAmt > 0}"
				styleClass="active">
				<h:outputText value="  (#{msgs.syn_mark_as_read})  " rendered="#{eachSite.unreadPrivateAmt > 0}" />
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
			
			<h:outputText value="#{eachSite.unreadForumsAmt}  " rendered="#{eachSite.unreadForumsAmt > 0}" />
			<f:verbatim></a></f:verbatim>

			<h:graphicImage url="/images/silk/email.png" rendered="#{eachSite.unreadForumsAmt > 0}" />

			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.unreadForumsAmt == 0}" />
		</h:column>
	 </h:dataTable>

   </h:panelGroup>

	<%-- ====== Dispaly for homepage of a site ===== --%>
	
	<%-- *** Display this if Message Center is not part of site *** --%>
	<h:outputText value="#{msgs.syn_no_mc}" rendered="#{(! mfSynopticBean.myWorkspace) && ! mfSynopticBean.messageForumsPageInSite}" />

	<%-- *** Display this if Message Center is part of site *** --%>	
	 <h:dataTable value="#{mfSynopticBean.contents}" var="eachSite" styleClass="listHier lines nolines"
	 		rendered = "#{! mfSynopticBean.myWorkspace}" >
 		<h:column>
			<h:outputText  value="#{eachSite.heading}" />
		</h:column>
		<h:column>
			<!-- === To create a link to Message Center home page === -->
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{eachSite.mcPageURL}';\">" />

			<h:outputText value="#{eachSite.unreadMessages}  " rendered="#{eachSite.unreadMessages > 0}" />
			<f:verbatim></a></f:verbatim>

			<h:graphicImage id="waveImg" url="/images/silk/email.png" rendered="#{eachSite.unreadMessages > 0}" />

 			<h:commandLink action="synMain"
				actionListener="#{mfSynopticBean.processReadAll}"
				rendered="#{eachSite.unreadPrivateAmt > 0}"
				styleClass="active">
				<h:outputText value="  (#{msgs.syn_mark_as_read})  " rendered="#{eachSite.unreadPrivateAmt > 0}" />
				<f:param name="contextId" value="#{eachSite.siteId}" />
			</h:commandLink>

			<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.unreadMessages == 0}" />
		</h:column>
	 </h:dataTable>
    </h:form>
  </sakai:view>
 </f:view>
 