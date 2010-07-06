<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %> 
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
	<sakai:view_container title="#{msgs.not_found_roster_profile}">
		<sakai:view_content>
			<h:form id="roster_form">
				<sakai:view_title value="#{msgs.profile_user_profile}"/>
				<h:graphicImage id="image" alt="#{msgs.profile_no_picture_available}" url="#{msgs.img_unavail}" styleClass="rosterImage"/>
				<h4>
					<h:outputText value="#{msgs.profile_public_information}"/>
				</h4>
				<p class="instruction">
					<h:outputText value="#{msgs.not_found_message}"/>
				</p>
				<h4>
					<h:outputText value="#{msgs.profile_personal_information}"/>
				</h4>
				<p class="instruction">
					<jsp:include page="personalInfoUnavailable.jsp"/>
				</p>
				<p class="act">
					<h:commandButton styleClass="active" accesskey="x" id="submit" value="#{msgs.back}" immediate="true" action="overview"/>
				</p>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
