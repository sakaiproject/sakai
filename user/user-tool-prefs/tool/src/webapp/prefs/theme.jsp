<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Core JSTL tag library --%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
	<sakai:stylesheet path="/css/prefs.css"/>
	<sakai:view_content>
		<h:form id="theme_form" rendered="#{UserPrefsTool.prefShowThemePreferences==true}">

			<h:outputText value="#{Portal.latestJQuery}" escape="false"/>

			<script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
			<script type="text/javascript" src="/library/js/spinner.js"></script>
			<script type="text/javascript">
				$(document).ready(function(){
					fixImplicitLabeling();
				})
			</script>

			<c:set var="cTemplate" value = "theme" scope="session"/>

			<%@ include file="toolbar.jspf"%>

			<div class="page-header">
				<h1><h:outputText value="#{msgs.theme_prefs_title}"/></h1>
			</div>

			<t:div rendered="#{UserPrefsTool.themeUpdated}">
				<jsp:include page="prefUpdatedMsg.jsp"/>
			</t:div>

			<p class="instruction"><h:outputFormat value="#{msgs.theme_prefs_instructions}"><f:param value="#{UserPrefsTool.serviceName}"/></h:outputFormat></p>

			<t:selectOneRadio id="themeOptions" value="#{UserPrefsTool.selectedTheme}" layout="spread">
				<f:selectItem itemValue="sakaiUserTheme-light" itemLabel="#{msgs.theme_prefs_lightTheme}"/>
				<f:selectItem itemValue="sakaiUserTheme-dark" itemLabel="#{msgs.theme_prefs_darkTheme}"/>
			</t:selectOneRadio>

			<ul class="prefs-themeSelection">
				<li><t:radio for="themeOptions" index="0" /></li>
				<li><t:radio for="themeOptions" index="1" /></li>
			</ul>

			<div class="submit-buttons act">
				<h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionThemeSave}" onclick="SPNR.disableControlsAndSpin( this, null );" />
				<h:commandButton accesskey="x" id="cancel" styleClass="formButton" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionThemeFrmEdit}" onclick="SPNR.disableControlsAndSpin( this, null );" />
			</div>
		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
