<%-- Included on main page if Resources tool not included in site --%>

<%-- Instructors (maintain) get error message --%>
<h:panelGroup rendered="#{podHomeBean.canUpdateSite}" >
	<h:messages styleClass="sak-banner-error" id="errorMessagesNR" /> 
</h:panelGroup>

<%-- Students (access) get no podcasts exist --%>
<h:panelGroup rendered="#{! podHomeBean.canUpdateSite}" >
    <h:outputText styleClass="sak-banner-info" value="#{msgs.no_podcasts}"  />
</h:panelGroup>
