<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<f:view locale="#{UserLocale.locale}">
	<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	   <jsp:setProperty name="msgs" property="baseName" value="signup"/>
	</jsp:useBean>
	
	<sakai:view_container title="Signup Tool">
		<style type="text/css">
			@import url("/sakai-signup-tool/css/signupStyle.css${Portal.CDNQuery}");
		</style>

		<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
		<script>
			//initialization of the page
			jQuery(document).ready(function() {
				const menuLink = $('#signupPermissionMenuLink');
				menuLink.addClass('current');
				menuLink.html(menuLink.find('a').text());
			});
		</script>

		<sakai:view_content>
			<h:form id="meeting">
				<%@ include file="/signup/menu/signupMenu.jsp" %>
				<h:outputText value="#{msgs.event_error_alerts} #{messageUIBean.errorMessage}" styleClass="alertMessage" escape="false" rendered="#{messageUIBean.error}"/>
				<div class="page-header">
					<sakai:view_title value="#{msgs.permission_page_title}"/>
				</div>
				<div class="sak-banner-info">
					<h:outputText value="#{SignupPermissionsUpdateBean.permissionsMessage}" />
				</div>
                <sakai-permissions
					tool="signup"
					reference="<h:outputText value="#{SignupPermissionsUpdateBean.reference}" />"
					on-refresh="<h:outputText value="#{SignupPermissionsUpdateBean.toolResetUrl}" />"
				>
				</sakai-permissions>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
	
</f:view>		
