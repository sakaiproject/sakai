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
		<%="<script src=js/print.js></script>"%>
        <h:form id="roster_form">
            <t:aliasBean alias="#{viewBean}" value="#{pictures}">
				<h:panelGrid columns="2" styleClass="rosterPageHeader" columnClasses="rosterPageHeaderLeft,rosterPageHeaderRight">
					<sakai:view_title value="#{viewBean.pageTitle}" />
					<h:panelGroup>
						<h:commandButton onclick="javascript:window.print();return false;" value="#{msgs.print}" title="#{msgs.print}"/>
					</h:panelGroup>
				</h:panelGrid>
            </t:aliasBean>

            <%-- Initialize the filter --%>
			<h:outputText value="#{filter.init}" />
			
			<h:panelGrid columns="3" rendered="#{filter.displaySectionsFilter}">
			    <h:outputText value="#{msgs.section_filter_pre}" styleClass="filterLabel" />
			    <h:selectOneMenu id="section_filter" value="#{filter.sectionFilter}" onchange="this.form.submit()" immediate="true">
			        <f:selectItems value="#{filter.sectionSelectItems}" />
			    </h:selectOneMenu>
			    <h:outputText value="#{msgs.section_filter_post}" styleClass="filterLabel" />
			</h:panelGrid>
			
			<h:panelGrid id="search_group" columns="2" styleClass="searchFilter">
			    <h:panelGroup styleClass="instruction">
			        <t:div style="padding-left:10px;" rendered="#{filter.participantCount > 0}">
			            <h:outputFormat value="#{msgs.currently_displaying_participants}" rendered="#{filter.participantCount > 1}">
			                <f:param value="#{filter.participantCount}" />
			            </h:outputFormat>
			            <h:outputFormat value="#{msgs.currently_displaying_participant}"  rendered="#{filter.participantCount == 1}">
			                <f:param value="#{filter.participantCount}" />
			            </h:outputFormat>
			        </t:div>
			
			        <t:div style="padding-left:10px;" rendered="#{filter.participantCount > 1 }">
			            <h:outputText value="#{filter.roleCountMessage}" />
			        </t:div>
			    </h:panelGroup>
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

            <t:dataList
                    value="#{pictures.participants}"
                    var="participant"
                    rowIndexVar="partIndex"
                    rowCountVar="partCounter">
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
                            </h:outputFormat>
                            <h:outputFormat value="#{participant.user.sortName}" title="#{participant.user.displayName}" rendered="#{!filter.displayPhotoFirstNameLastName}">
                            </h:outputFormat>
                        </t:div>
                    </t:div>

                    <t:div>
                        <h:outputText value="#{participant.user.displayId}" title="#{msgs.show_profile}" rendered="#{pictures.renderProfileLinks}" style="text-decoration: underline;" />
                        <h:outputText value="#{participant.user.displayId}" rendered="#{ ! pictures.renderProfileLinks}" />
                    </t:div>
                    <t:div rendered="#{(((partIndex + 1) mod 7) == 0) && ((partIndex + 1) != partCounter)}" style="page-break-after: always;">
                    	<t:div styleClass="noprint">
	                    	<f:verbatim><p style="text-align: center;"></f:verbatim>
	                    	<h:outputText value="#{((partIndex + 1) / 7)}">
	                    		<f:convertNumber type="number" integerOnly="true"/>
	                    	</h:outputText>
	                    	<h:outputText value=" of " />
	                    	<h:outputText value="#{(partCounter mod 7 == 0) ? (partCounter / 7) : (partCounter / 7) + 1}">
	                    		<f:convertNumber type="number" integerOnly="true" maxFractionDigits="0"/>
	                    	</h:outputText>
	                    	<f:verbatim></p><hr/></f:verbatim>
	                    </t:div>
                    </t:div>
                    <t:div rendered="#{(partIndex + 1) == partCounter}" style="page-break-after: always;">
	                    <t:div styleClass="noprint">
	                    	<f:verbatim><p style="text-align: center;"></f:verbatim>
	                    	<h:outputText value="#{(partCounter mod 7 == 0) ? (partCounter / 7) : ((partCounter < 7) ? 1 : (partCounter / 7) + 1)}">
	                    		<f:convertNumber type="number" integerOnly="true" maxFractionDigits="0"/>
	                    	</h:outputText>
	                    	<h:outputText value=" of " />
	                    	<h:outputText value="#{(partCounter mod 7 == 0) ? (partCounter / 7) : ((partCounter < 7) ? 1 : (partCounter / 7) + 1)}">
	                    		<f:convertNumber type="number" integerOnly="true" maxFractionDigits="0"/>
	                    	</h:outputText>
	                    	<f:verbatim></p><hr/></f:verbatim>
	                    </t:div>
                    </t:div>
                </h:column>
            </t:dataList>
        </h:form>
    </sakai:view>
</f:view>
