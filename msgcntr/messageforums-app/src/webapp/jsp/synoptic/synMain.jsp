<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
	<h:form>
	 <h:panelGroup rendered="#{mfSynopticBean.myWorkspace}" > 
       <sakai:tool_bar>
          <sakai:tool_bar_item action="synOptions" value="#{msgs.syn_options}" />
       </sakai:tool_bar>
     </h:panelGroup>

	 <h:dataTable value="#{mfSynopticBean.contents}" var="eachSite" >
       	<h:column>
			<h:outputText value="#{eachSite.siteName}" />
	   	</h:column>
		<h:column>
			<%-- === To create a link to Message Center home page === --%>
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{eachSite.privateMessagesURL}';\">#{eachSite.unreadPrivate}" />
			<f:verbatim></a></f:verbatim>
		</h:column>
		<h:column>
			<h:commandLink action="synMain"
				actionListener="#{mfSynopticBean.processReadAll}"
				rendered="#{eachSite.unreadPrivateAmt > 0}"
				styleClass="active">
				<h:outputText value="#{msgs.syn_mark_as_read}" />
				<f:param name="contextId" value="#{eachSite.siteId}" />
			</h:commandLink>
		</h:column>
		<h:column>
			<!-- === To create a link to Message Center home page === -->
			<f:verbatim><a href="#" onclick="javascript:window.parent.location.href='</f:verbatim>
			<h:outputText escape="false" value="#{eachSite.privateMessagesURL}';\">#{eachSite.unreadForums}" />
			<f:verbatim></a></f:verbatim>
		</h:column>
	 </h:dataTable>
    </h:form>
  </sakai:view>
 </f:view>
 