<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");	/*prepEventsForm();*/
%>
<f:loadBundle basename="org.sakaiproject.sitestats.tool.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<sakai:flowState bean="#{PrefsBean}"/>

	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>	
	
	<style type="text/css">
		@import url("sitestats/css/sitestats.css");
	</style>	
	
	<h:form rendered="#{BaseBean.allowed}">
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="PrefsBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
	    </h:panelGroup>	
	</h:form>
			    
	<h3>
		<h:outputText value="#{msgs.menu_prefs} (#{BaseBean.siteTitle})" rendered="#{BaseBean.adminView}"/>
		<h:outputText value="#{msgs.menu_prefs}" rendered="#{!BaseBean.adminView}"/>
	</h3>
		
	<f:subview id="msg">
		<h:message for="msg" infoClass="success" fatalClass="alertMessage" style="margin-top: 15px;" showDetail="true"/>
	</f:subview>		
	
	
	<h:form id="prefsForm" rendered="#{BaseBean.allowed}">
	<t:div style="width:100%">
			<h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="sst,sst">             
            	<t:div style="text-align: left; white-space: nowrap; vertical-align:top;">   
            		<f:verbatim><h4></f:verbatim><h:outputText value="#{msgs.prefs_overview_page}"/><f:verbatim></h4></f:verbatim>
                 	<h:outputText value="#{msgs.instructions_prefs_overview}" styleClass="instruction"/>
                 	<h:selectManyCheckbox 
							id="configuredOPEvents" 
							value="#{PrefsBean.configuredOPEvents}" 
							layout="pageDirection">
				        	<f:selectItems value="#{PrefsBean.availableOPEvents}"  />
				    </h:selectManyCheckbox>
                </t:div>
                              
				<t:div style="text-align: left; white-space: nowrap; vertical-align:top;">   
						<f:verbatim><h4></f:verbatim><h:outputText value="#{msgs.prefs_event_list}"/><f:verbatim></h4></f:verbatim>
						<h:outputText value="#{msgs.instructions_prefs_events}" styleClass="instruction" style="display:inline; white-space: normal;"/>
						<h:selectManyCheckbox 
							id="configuredEPEvents" 
							value="#{PrefsBean.configuredEPEvents}" 
							layout="pageDirection">
				        	<f:selectItems value="#{PrefsBean.availableEPEvents}"  />
				        </h:selectManyCheckbox>
				</t:div> 
		</h:panelGrid>
		
				        
        <t:div styleClass="act">
            <h:commandButton
                action="#{PrefsBean.update}"
                value="#{msgs.update}"
                styleClass="active" />        
            <h:commandButton
                action="#{PrefsBean.cancel}"
                value="#{msgs.cancel}"/>
        </t:div>
	</t:div>
	</h:form>
</sakai:view>
</f:view>
