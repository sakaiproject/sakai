<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%
response.setContentType("text/html; charset=UTF-8");
%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.roster.bundle.Messages"/>
</jsp:useBean>
<f:view>
    <sakai:view title="#{msgs.facet_roster_pictures}" toolCssHref="/sakai-roster-tool/css/roster.css">
		<%="<script src=js/roster.js></script>"%>
		<script language="JavaScript">
			// open print preview in another browser window so can size approx what actual
			// print out will look like
			function printFriendly(url) {
			window.open(url,'mywindow','width=960,height=1100,scrollbars=1'); 		
			}
		</script>
        <h:form id="roster_form">
            <t:aliasBean alias="#{viewBean}" value="#{pictures}">
                <%@include file="inc/nav.jspf" %>
            </t:aliasBean>

            <%@include file="inc/filter.jspf" %>

            <h:panelGrid columns="2" styleClass="rosterPicturesFilter" columnClasses="rosterPageHeaderLeft,rosterPageHeaderRight">
                <t:selectOneRadio  value="#{prefs.displayProfilePhotos}" onclick="this.form.submit()" immediate="true" rendered="#{pictures.renderPicturesOptions}">
                    <f:selectItems value="#{pictures.photoSelectItems}" />
                </t:selectOneRadio>
				
				<%-- Render something to keep the panel grid happy with two rendered components --%>
				<h:outputText value="" rendered="#{ ! pictures.renderPicturesOptions}"/>

                <h:commandButton value="#{msgs.roster_show_names}" actionListener="#{pictures.showNames}" rendered="#{ ! prefs.displayNames}" />
                <h:commandButton value="#{msgs.roster_hide_names}" actionListener="#{pictures.hideNames}" rendered="#{prefs.displayNames}"/>
            </h:panelGrid>


             <t:div styleClass="instruction">

      			<%-- No filtering --%>
                <h:outputText value="#{msgs.no_participants}" rendered="#{empty filter.participants && filter.searchFilterString eq filter.defaultSearchText && empty filter.sectionFilterTitle}" />

      			<%-- Filtering on section, but not user --%>
                <h:outputFormat value="#{msgs.no_participants_msg}" rendered="#{empty filter.participants && filter.searchFilterString eq filter.defaultSearchText && not empty filter.sectionFilterTitle}" >
                     <f:param value="#{filter.sectionFilterTitle}"/>
                </h:outputFormat>

      			<%-- Filtering on user, but not section --%>
                <h:outputFormat value="#{msgs.no_participants_msg}" rendered="#{empty filter.participants &&  filter.searchFilterString != filter.defaultSearchText && empty filter.sectionFilterTitle}" >
                    <f:param value="#{filter.searchFilterString}"/>
                </h:outputFormat>

      			<%-- Filtering on section and user --%>
                <h:outputFormat value="#{msgs.no_participants_in_section}" rendered="#{empty filter.participants &&  filter.searchFilterString != filter.defaultSearchText && not empty filter.sectionFilterTitle}" >
                    <f:param value="#{filter.searchFilterString}"/>
                    <f:param value="#{filter.sectionFilterTitle}"/>
                </h:outputFormat>

            </t:div>

            <t:dataTable
                    newspaperColumns="5"
                    newspaperOrientation="horizontal"
                    value="#{pictures.participants}"
                    var="participant"
                    styleClass="rosterPictures">
                <h:column>
                    <t:div>
                        <h:graphicImage
                                id="profileImage"
                                value="#{participant.profile.pictureUrl}"
                                title="#{msgs.profile_picture_alt} #{participant.user.displayName}"
                                styleClass="rosterImage"
                                rendered="#{
                                (
                                ! pictures.officialPhotosAvailableToCurrentUser &&
                                participant.profilePhotoPublic &&
                                ! empty participant.profile.pictureUrl &&
                                ! participant.officialPhotoPublicAndPreferred
                                ) ||
                                (
                                pictures.officialPhotosAvailableToCurrentUser &&
                                prefs.displayProfilePhotos &&
                                ! participant.officialPhotoPreferred &&
                                ! empty participant.profile.pictureUrl
                                )
                                }"
							/>

                        <h:graphicImage
                                id="rosterImage"
                                value="ParticipantImageServlet.prf?photo=#{participant.user.id}"
                                title="#{msgs.profile_picture_alt} #{participant.user.displayName}"
                                styleClass="rosterImage"
                                rendered="#{
                                (pictures.officialPhotosAvailableToCurrentUser && ! prefs.displayProfilePhotos) ||
                                (pictures.officialPhotosAvailableToCurrentUser && prefs.displayProfilePhotos && participant.officialPhotoPreferred) ||
                                ( ! pictures.officialPhotosAvailableToCurrentUser && participant.officialPhotoPublicAndPreferred)
                                }"
                                />
							
                        <h:graphicImage
                                id="profileImageNotAvailable"
                                value="#{msgs.img_unavail}"
                                title="#{msgs.profile_no_picture_available}"
                                styleClass="rosterImage"
                                rendered="#{
                                (
                                pictures.officialPhotosAvailableToCurrentUser &&
                                prefs.displayProfilePhotos &&
                                ! participant.officialPhotoPreferred &&
                                empty participant.profile.pictureUrl
                                ) ||
                                (
                                ! pictures.officialPhotosAvailableToCurrentUser &&
                                participant.profilePhotoPublic &&
                                ! participant.officialPhotoPublicAndPreferred &&
                                empty participant.profile.pictureUrl
                                ) ||
                                (
								! pictures.officialPhotosAvailableToCurrentUser &&
                                ! participant.profilePhotoPublic
                                )
                                }"
                                />

                    </t:div>
                    <t:div rendered="#{prefs.displayNames}">
                        <t:div>
                            <h:outputFormat value="#{participant.user.displayName}" title="#{participant.user.displayName}" rendered="#{filter.displayPhotoFirstNameLastName}">
                                <f:converter converterId="textTruncateConverter"/>
                            </h:outputFormat>
                            <h:outputFormat value="#{participant.user.sortName}" title="#{participant.user.displayName}" rendered="#{!filter.displayPhotoFirstNameLastName}">
                                <f:converter converterId="textTruncateConverter"/>
                            </h:outputFormat>
                        </t:div>
                    </t:div>

                    <t:div>
                        <h:commandLink action="#{profileBean.displayProfile}" value="#{participant.user.displayId}" title="#{msgs.show_profile}" rendered="#{pictures.renderProfileLinks}">
                            <f:param name="participantId" value="#{participant.user.id}" />
                            <f:param name="returnPage" value="pictures" />
                        </h:commandLink>
                        <h:outputText value="#{participant.user.displayId}" rendered="#{ ! pictures.renderProfileLinks}" />
                    </t:div>
                </h:column>
            </t:dataTable>
        </h:form>
    </sakai:view>
</f:view>
