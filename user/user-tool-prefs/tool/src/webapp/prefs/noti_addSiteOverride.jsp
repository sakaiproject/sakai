<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
	<sakai:view_content>
<f:verbatim>
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
<script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
</f:verbatim>
	
		<h:form id="site_list_form">
		
			<h3><h:outputFormat value="#{msgs.noti_prefs_title}">
				<f:param value="#{UserPrefsTool.currentDecoratedNotificationPreference.userNotificationPreferencesRegistration.sectionTitle}"></f:param>
			</h:outputFormat>
			
			</h3>
			<p class="instruction"><h:outputText value="#{msgs.noti_prefs_instr}"/></p>

			<h:dataTable value="#{UserPrefsTool.currentDecoratedNotificationPreference.siteList}" var="decoType" border="0">
      			<h:column>
      				<h:panelGroup>
      				<f:verbatim><h4 style="cursor:pointer" onclick="toggle_visibility('</f:verbatim><h:outputText value="#{decoType.condensedTypeText}"/><f:verbatim>')"></f:verbatim>
      				<f:verbatim>
      					<img id="toggle</f:verbatim><h:outputText value="#{decoType.condensedTypeText}" /><f:verbatim>" src="</f:verbatim><h:outputText value="#{decoType.iconUrl}" /><f:verbatim>" 
      						style="padding-top:4px;width:13px"
      						title="</f:verbatim><h:outputText value="#{decoType.showHideText}"/><f:verbatim>" />
      					</f:verbatim>      				
      			    <h:outputText value="#{decoType.typeText}"/>
					<f:verbatim></h4></f:verbatim>
					<f:verbatim>
					<div id="</f:verbatim><h:outputText value="#{decoType.condensedTypeText}" /><f:verbatim>" style="</f:verbatim><h:outputText value="#{decoType.style}" /><f:verbatim>">
					</f:verbatim>
					<t:dataTable value="#{decoType.sites}" var="decoSite">
						<t:column>
							<t:selectBooleanCheckbox value="#{decoSite.selected}" id="siteCheckbox" immediate="true" 
							valueChangeListener="#{decoSite.checkBoxChanged}" />
						</t:column>
						<t:column>
							<t:outputText value="#{decoSite.site.title}"/>
						</t:column>
					</t:dataTable>
	      			<f:verbatim>
	      			</div>
	      			</f:verbatim>
	      			</h:panelGroup>
      			</h:column>
   			</h:dataTable>	

			<p class="act">
				<h:commandButton accesskey="s" id="submit" immediate="true" styleClass="active" value="#{msgs.noti_prefs_add}" action="#{UserPrefsTool.currentDecoratedNotificationPreference.processActionSiteOverrideSave}"></h:commandButton>
				<h:commandButton accesskey="x" id="cancel" immediate="true" value="#{msgs.noti_prefs_cancel}" action="#{UserPrefsTool.currentDecoratedNotificationPreference.processActionSiteOverrideCancel}"></h:commandButton>
			</p>
			
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
