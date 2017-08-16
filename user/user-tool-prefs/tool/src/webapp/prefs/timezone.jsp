<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%-- Core JSTL tag library --%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
    <sakai:stylesheet path="/css/prefs.css"/>
	<sakai:view_content>
		<h:form id="timezone_form">
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
		<script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
		<script type="text/javascript">
			$(document).ready(function(){
				setupPrefsGen();
			})  
		</script>

				<%-- Set current value for template --%> 
				<c:set var="cTemplate" value = "timezone" scope = "session" />
				<%@ include file="toolbar.jspf"%>
				
				<sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />
				<h3>
					<h:outputText value="#{msgs.prefs_timezone_title}" />
					<h:panelGroup rendered="#{UserPrefsTool.tzUpdated}"   style="margin:0 3em;font-weight:normal">
						<jsp:include page="prefUpdatedMsg.jsp"/>	
					</h:panelGroup>
				</h3>

				
				<p class="instruction">
				<h:outputFormat value="#{msgs.time_inst}">
					<f:param value="#{UserPrefsTool.serviceName}"/>
					<f:param value="#{UserPrefsTool.selectedTimeZone}"/>
				</h:outputFormat>
				</p>
					
    			 <h:selectOneListbox 
                      value="#{UserPrefsTool.selectedTimeZone}"
                      size="20"
                      styleClass="multiLine">
				    <f:selectItems value="#{UserPrefsTool.prefTimeZones}" />
				 </h:selectOneListbox>

			    <div class="act">
			    <h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionTzSave}"></h:commandButton>
				<h:commandButton accesskey="x" id="cancel" styleClass="formButton" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionTzCancel}"></h:commandButton>
				<h:commandButton type="button"  styleClass="dummy blocked" value="#{msgs.update_pref}" style="display:none"></h:commandButton>
				<h:commandButton type="button"  styleClass="dummy blocked" value="#{msgs.cancel_pref}" style="display:none"></h:commandButton>

			    </div>
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
