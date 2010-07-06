<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%--TODO: Refactor Profile Code in order to use profile custom components--%>
<%@ taglib uri="http://sakaiproject.org/jsf/roster" prefix="roster" %> 
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
	<sakai:view_container title="#{msgs.profile_site_roster}">
		<sakai:view_content>
			<h:form id="roster_form">
				<sakai:view_title value="#{msgs.profile_user_profile}"/>
				<%-- TODO: should the image sizes be predetermine for this view? --%>
				<h:graphicImage value="#{msgs.img_unavail}"  rendered="#{profileBean.showCustomPhotoUnavailableForSelectedProfile}" title="#{msgs.profile_no_picture_available}" styleClass="rosterImage"/>
				<h:graphicImage value="#{profileBean.participant.profile.pictureUrl}"  rendered="#{profileBean.showURLPhotoForSelectedProfile}" title="#{msgs.profile_picture_alt} #{profileBean.participant.profile.firstName} #{profileBean.participant.profile.lastName}" styleClass="rosterImage"/>
				<h:graphicImage value="ParticipantImageServlet.prf?photo=#{profileBean.participant.user.id}"  rendered="#{profileBean.showCustomIdPhotoForSelectedProfile}" title="#{msgs.profile_picture_alt} #{profileBean.participant.profile.firstName} #{profileBean.participant.profile.lastName}" styleClass="rosterImage"/>
				<h4>
					<h:outputText value="#{msgs.profile_public_information}"/>
				</h4>
				<sakai:panel_edit>
					<h:outputLabel for="firstName" style ="shorttext" value="#{msgs.profile_first_name}"/>
					<h:outputText id="firstName" value="#{profileBean.participant.profile.firstName}"/> 
					<h:outputLabel for="lastName"  value="#{msgs.profile_last_name}"/>
					<h:outputText id="lastName" value="#{profileBean.participant.profile.lastName}"/>
					<h:outputLabel for="nick" value="#{msgs.profile_nick_name}"/>				
					<h:outputText id="nick" value="#{profileBean.participant.profile.nickName}"/>
					<h:outputLabel for="position" value="#{msgs.profile_position}"/> 				
					<h:outputText id="position" value="#{profileBean.participant.profile.position}"/> 
					<h:outputLabel for="dept"  value="#{msgs.profile_department}"/>
					<h:outputText id="dept" value="#{profileBean.participant.profile.department}"/> 
					<h:outputLabel for="school" value="#{msgs.profile_school}"/>
					<h:outputText id="school" value="#{profileBean.participant.profile.school}"/>
					<h:outputLabel for="room"  value="#{msgs.profile_room}"/>
					<h:outputText id="room"  value="#{profileBean.participant.profile.room}"/> 
				</sakai:panel_edit>
				<h4>
					<h:outputText value="#{msgs.profile_personal_information}"/>
				</h4>
				<sakai:panel_edit>
					<h:outputText  value="#{msgs.profile_email}"/>
		            <h:outputLink value="mailto:#{profileBean.participant.profile.email}"><h:outputText value="#{profileBean.participant.profile.email}"/></h:outputLink>
					<h:outputText value="#{msgs.profile_homepage}"/>
					<%--need to test for empty value here - and omit the outputLink if null --%>				
					<h:outputLink target="_blank" value="#{profileBean.participant.profile.homepage}">
						<h:outputText value="#{profileBean.participant.profile.homepage}"/>
					</h:outputLink>
					<h:outputText  value="#{msgs.profile_work_phone}"/>
					<h:outputText value="#{profileBean.participant.profile.workPhone}"/>
					<h:outputText value="#{msgs.profile_home_phone}"/>
					<h:outputText value="#{profileBean.participant.profile.homePhone}"/>
					<h:outputText value="#{msgs.profile_other_information}"/>
					<roster:roster_display_HTML value="#{profileBean.participant.profile.otherInformation}"/>
				</sakai:panel_edit>
				<p class="act">
				 	<h:commandButton styleClass="active" accesskey="x" id="submit" value="#{msgs.back}" immediate="true" action="#{prefs.getReturnPage}" />
				</p>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
