<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view> 
	<sakai:view title="#{msgs.podcast_home_title}" toolCssHref="css/podcaster.css">
	<script src="/library/js/headscripts.js"></script>
	<script src="scripts/popupscripts.js"></script>

<script>
    includeLatestJQuery('podMain.jsp');
</script>
<script>
    $(document).ready(function(){
        initializePopover("podMainForm\\:popover", "<h:outputText value="#{msgs.popup_text}" />");
        var menuLink = $('#podcastMainMenuLink');
        var menuLinkSpan = menuLink.closest('span');
        menuLinkSpan.addClass('current');
        menuLinkSpan.html(menuLink.text());
    });
</script>

<h:form id="podMainForm">
    <%@ include file="/podcasts/podcastMenu.jsp" %>

    <%-- if Resources tool not exist, if instructor, display error message
      		if student, display no podcasts exists --%>
    <h:panelGroup rendered="#{! podHomeBean.resourceToolExists || ! podHomeBean.canAccessFolder}" >
    	<%@ include file="podNoResource.jsp" %>
    </h:panelGroup>

	<h:panelGroup rendered="#{podHomeBean.resourceToolExists && podHomeBean.canAccessFolder}" >
		<h:outputText value="#{msgs.no_access}" styleClass="validation" rendered="#{! podHomeBean.hasAllGroups && ! podHomeBean.hasReadPerm }" />
      
		<h:panelGroup rendered="#{podHomeBean.hasReadPerm || podHomeBean.hasAllGroups}"> 
			<h:outputText styleClass="sak-banner-error" id="errorMessages" rendered="#{!empty facesContext.maximumSeverity}"/>
 	    </h:panelGroup>

		<h:panelGroup rendered="#{podHomeBean.hasReadPerm || podHomeBean.hasAllGroups}">
			<h:panelGroup style="display:block;" styleClass="pod-box">
				<h:panelGroup style="display:block;" styleClass="sak-banner-info">
						<h:outputText value="#{msgs.podcast_home_sub} " />
						<h:outputLink id="popover" onclick="return false;">
							<h:outputText value="#{msgs.podcatcher}." />
						</h:outputLink>
				</h:panelGroup>
				<h:panelGroup style="display:block;" styleClass="row">
					<h:panelGroup styleClass="col-lg-6 col-sm-4 col-xs-12">
						<h:inputText readonly="true" styleClass="form-control" id="rssLink" value="#{podHomeBean.URL}">
						</h:inputText>
					</h:panelGroup>
					<h:panelGroup style="display:block;" styleClass="col-lg-3 col-sm-4 col-xs-12">
						<h:commandButton onclick="copyToClipboard('podMainForm\\:rssLink'); return false;" styleClass="btn btn-default copyButton" value="#{msgs.copy_to_clipboard}">
						</h:commandButton>
					</h:panelGroup>
					<h:panelGroup style="display:block;" styleClass="col-lg-3 col-sm-4 col-xs-12">
						<h:commandButton action="podfeedRevise" styleClass="btn btn-default editButton" rendered="#{podHomeBean.canUpdateSite}" value="#{msgs.revise}">
						</h:commandButton>
					</h:panelGroup>
				</h:panelGroup>
			</h:panelGroup>
			<h:outputText styleClass="sak-banner-warn" value="#{msgs.no_podcasts}" rendered="#{!podHomeBean.actPodcastsExist}" />
		</h:panelGroup>
	</h:panelGroup>
 	 
	<!-- if there are podcasts, display their information here -->
	<h:dataTable value="#{podHomeBean.contents}" var="eachPodcast" rendered="#{podHomeBean.actPodcastsExist}" styleClass="table table-hover table-striped table-bordered" headerClass="hidden-xs" columnClasses="hidden-xs,,hidden-xs,,,,">
		<h:column>
			<f:facet name="header"><h:outputText value="#{msgs.date_prompt}"/></f:facet>
			<h:outputText value="#{eachPodcast.displayDate}" />
		</h:column>

		<h:column>
			<f:facet name="header"><h:outputText value="#{msgs.title_prompt}"/></f:facet>
			<h:outputText value="#{eachPodcast.title}" />
		</h:column>

		<h:column>
			<f:facet name="header"><h:outputText value="#{msgs.description_prompt}"/></f:facet>
			<h:outputText value="#{eachPodcast.description}" />
		</h:column>

		<h:column>
			<f:facet name="header"><h:outputText value="#{msgs.posted_by}"/></f:facet>
			<h:outputText value="#{eachPodcast.author} " />
		</h:column>

		<h:column>
			<f:facet name="header"><h:outputText value="#{msgs.at}"/></f:facet>
			<h:outputText value="#{eachPodcast.postedDatetime}" />
		</h:column>

		<h:column>
			<f:facet name="header"><h:outputText value="#{msgs.actions}"/></f:facet>
			<f:verbatim><i class="fa fa-download" aria-hidden="true"></i></f:verbatim>
			<h:outputLink value="#{eachPodcast.fileURL}" styleClass="active" target="#{eachPodcast.newWindow}">
				 <h:outputText value="#{msgs.download}" />
			</h:outputLink>
			<h:outputText value=" #{msgs.open_paren}#{eachPodcast.size} #{eachPodcast.type}#{msgs.close_paren}" />
			<f:verbatim></br></f:verbatim>
			<%--  go to Revise page --%>
			<h:panelGroup rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasReviseAnyPerm || (podHomeBean.hasReviseOwnPerm && eachPodcast.author == podHomeBean.userName)}">
				<f:verbatim><i class="fa fa-pencil-square-o" aria-hidden="true"></i></f:verbatim>
				<h:commandLink action="podcastRevise" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.revise}" styleClass="active" 
					rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasReviseAnyPerm || (podHomeBean.hasReviseOwnPerm && eachPodcast.author == podHomeBean.userName)}" >
				<f:param name="resourceId" value="#{eachPodcast.resourceId}" />
				</h:commandLink>
				<f:verbatim></br></f:verbatim>
			</h:panelGroup>
			<%--  go to Delete page --%> 
			<h:panelGroup rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasDelAnyPerm || (podHomeBean.hasDelOwnPerm && eachPodcast.author == podHomeBean.userName)}">
				<f:verbatim><i class="fa fa-trash" aria-hidden="true"></i></f:verbatim>
				<h:commandLink action="podcastDelete" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.delete}" styleClass="active" 
					rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasDelAnyPerm || (podHomeBean.hasDelOwnPerm && eachPodcast.author == podHomeBean.userName)}" >
				<f:param name="resourceId" value="#{eachPodcast.resourceId}" />
				</h:commandLink>
				<f:verbatim></br></f:verbatim>
			</h:panelGroup>
		</h:column>

		<h:column>
			<f:facet name="header"><h:outputText value="#{msgs.status}"/></f:facet>
			<h:outputText value="#{msgs.hidden}" rendered="#{eachPodcast.hidden}"/>
			<h:outputText value="#{msgs.published}" rendered="#{!eachPodcast.hidden}"/>
		</h:column>
	</h:dataTable>
</h:form>
   </sakai:view> 
  </f:view>
 
