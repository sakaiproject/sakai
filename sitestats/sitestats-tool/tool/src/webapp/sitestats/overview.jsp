<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sitestats" prefix="sst" %>
<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>

<f:loadBundle basename="org.sakaiproject.sitestats.tool.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<%--<sakai:flowState bean="#{OverviewBean}"/>--%>
	<h:outputText value="#{OverviewBean.init}"/>

	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>		
	<style type="text/css">
		@import url("sitestats/css/sitestats.css");
	</style>

	<h:form id="overviewForm" rendered="#{BaseBean.allowed}">
		
		<%/* MENU */%>
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="OverviewBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
	    </h:panelGroup>
	    
		
		<%/* TITLE */%>
		<t:htmlTag value="h3">
			<h:outputText value="#{msgs.menu_overview} (#{BaseBean.siteTitle})" rendered="#{BaseBean.adminView}"/>
			<h:outputText value="#{msgs.menu_overview}" rendered="#{!BaseBean.adminView}"/>
		</t:htmlTag>
	
	
		<t:div style="width:100%">
			 <h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="sst,sst">               
				
				<%/* CHART AREA */%>	
				<t:div style="text-align: left; white-space: nowrap; vertical-align:top;">   
	            	<t:htmlTag value="h4">
						<h:outputText value="#{msgs.overview_title_chart}"/>
					</t:htmlTag>
					<%/* CHART SELECTORS */%>					
					<t:htmlTag value="small">	
						<t:commandLink value="#{msgs.submenu_week}" actionListener="#{OverviewBean.selectWeekView}" rendered="#{OverviewBean.selectedView ne 'week'}" />
						<t:outputText value="#{msgs.submenu_week}" rendered="#{OverviewBean.selectedView eq 'week'}" />
						<t:outputText value="    |    "/>
						<t:commandLink value="#{msgs.submenu_month}" actionListener="#{OverviewBean.selectMonthView}" rendered="#{OverviewBean.selectedView ne 'month'}"/>
						<t:outputText value="#{msgs.submenu_month}" rendered="#{OverviewBean.selectedView eq 'month'}"/>
						<t:outputText value="    |    "/>
						<t:commandLink value="#{msgs.submenu_year}" actionListener="#{OverviewBean.selectYearView}" rendered="#{OverviewBean.selectedView ne 'year'}"/>
						<t:outputText value="#{msgs.submenu_year}" rendered="#{OverviewBean.selectedView eq 'year'}"/>
					</t:htmlTag>
					<%/* CHART */%>
					<sst:vbarchart 
						type="#{OverviewBean.selectedView}" 
						lastDate="#{OverviewBean.todayInMillis}"
						column1="#{OverviewBean.visits}" 
						column2="#{OverviewBean.activity}" 
						column3="#{OverviewBean.uniqueVisitors}"
					/>
				</t:div> 
	            
	            <%/* SUMMARY */%>	
				<t:div style="text-align: left; white-space: nowrap; vertical-align:top;">            
                	<t:htmlTag value="h4">
						<h:outputText value="#{msgs.overview_title_visits_sum}" style="text-align: left;"/>
					</t:htmlTag>
                    <h:panelGrid  styleClass="sectionContainerNav" style="width: 100%;" columns="2">
                            <t:div style="text-align: left; white-space: nowrap;">
                                    <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                            <h:outputText value="#{msgs.overview_total_visits}" style="font-weight: bold;"/>            
                                            <h:outputText value="#{msgs.overview_average}" style="font-weight: bold;"/>
                                    </h:panelGrid>
                            </t:div>
                            <t:div style="text-align: right; white-space: nowrap;">
                                    <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                            <h:outputText value="#{OverviewBean.totalVisits}"/>            
                                            <h:outputText value="#{OverviewBean.visitsAverage}"/>
                                    </h:panelGrid>
                            </t:div>
                       </h:panelGrid>
                       
                    <t:htmlTag value="h4">
						<h:outputText value="#{msgs.overview_title_unique_visits_sum}" style="text-align: left;"/>
					</t:htmlTag>
                    <h:panelGrid  styleClass="sectionContainerNav" style="width: 100%;" columns="2">
                            <t:div style="text-align: left; white-space: nowrap;">
                                    <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                            <h:outputText value="#{msgs.overview_total_unique_visits}" style="font-weight: bold;"/>            
                                            <h:outputText value="#{msgs.overview_average}" style="font-weight: bold;"/>           
                                            <h:outputText value="#{msgs.overview_unique_visits_total_users}" style="font-weight: bold;"/>
                                    </h:panelGrid>
                            </t:div>
                            <t:div style="text-align: right; white-space: nowrap;">
                                    <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                            <h:outputText value="#{OverviewBean.totalUniqueVisits}"/>            
                                            <h:outputText value="#{OverviewBean.uniqueVisitsAverage}"/>     
                                            <h:outputText value="#{OverviewBean.loggedTotalUsersRelation}"/>
                                    </h:panelGrid>
                            </t:div>
                       </h:panelGrid>
                       
                    <t:htmlTag value="h4">
						<h:outputText value="#{msgs.overview_title_activity_sum}" style="text-align: left;"/>
					</t:htmlTag>
                    <h:panelGrid  styleClass="sectionContainerNav" style="width: 100%;" columns="2">
                            <t:div style="text-align: left; white-space: nowrap;">
                                    <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                            <h:outputText value="#{msgs.overview_total_activity}" style="font-weight: bold;"/>            
                                            <h:outputText value="#{msgs.overview_average}" style="font-weight: bold;"/>       
                                    </h:panelGrid>
                            </t:div>
                            <t:div style="text-align: right; white-space: nowrap;">
                                    <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                            <h:outputText value="#{OverviewBean.totalActivity}"/>            
                                            <h:outputText value="#{OverviewBean.activityAverage}"/>
                                    </h:panelGrid>
                            </t:div>
                       </h:panelGrid>
				</t:div>
			</h:panelGrid>
		</t:div>	
	</h:form>
</sakai:view>
</f:view>
