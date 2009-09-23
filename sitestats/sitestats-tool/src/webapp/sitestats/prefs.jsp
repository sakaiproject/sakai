<%--

    $URL:$
    $Id:$

    Copyright (c) 2006-2009 The Sakai Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

                http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>

<%/* #####  TAGLIBS, BUNDLE, Etc  ##### */%>
<%@include file="inc/common.jsp"%>


<f:view>
<sakai:view title="#{msgs.tool_title}">

	<%/* #####  CSS  ##### */%>
	<style type="text/css">
		@import url("/sakai-sitestats-tool/sitestats/css/sitestats.css");
	</style>
	
	<%/* #####  JAVASCRIPT  ##### */%>
	<sakai:script path="/sitestats/script/common.js"/>
	<sakai:script path="/sitestats/script/prefs.js"/>
	<sakai:script path="/sitestats/script/tree2/tree.js"/>

	<%/* #####  FACES MESSAGES: Acess denied  ##### */%>
	<f:subview id="allowed" rendered="#{!ServiceBean.allowed}">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>
	

	<h:form id="prefsForm" rendered="#{ServiceBean.allowed}">
		
		<%/* #####  MENU  ##### */%>
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="PrefsBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
	    </h:panelGroup>
	    
		
		<%/* #####  TITLE  ##### */%>
		<t:htmlTag value="h2">
			<h:outputText value="#{msgs.menu_prefs} (#{ServiceBean.siteTitle})" rendered="#{ServiceBean.adminView}"/>
			<h:outputText value="#{msgs.menu_prefs}" rendered="#{!ServiceBean.adminView}"/>
		</t:htmlTag>


		<%/* #####  FACES MESSAGES: General ##### */%>
		<f:subview id="msg">		
	    	<h:message for="msg" infoClass="success" fatalClass="alertMessage" style="margin-top: 15px; width: auto" showDetail="true" />
		</f:subview>
		
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div style="width:100%">
			
			<h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="halfSize,halfSize">
		
				<t:div style="margin:0; padding:0;">
					<%/* #####  GENERAL  ##### */%>
					<t:div>
						<t:htmlTag value="h4" styleClass="summaryHeader">
							<h:outputText value="#{msgs.prefs_sep_general}"/>
						</t:htmlTag>		
						<t:htmlTag value="p" styleClass="instruction">
							<h:outputText value="#{msgs.instructions_prefs_general}"/>
						</t:htmlTag>
						
						<t:selectBooleanCheckbox id="general-listavailableonly-check" value="#{PrefsBean.prefsdata.listToolEventsOnlyAvailableInSite}"/>
						<t:outputLabel id="general-listavailableonly-label" for="general-listavailableonly-check" value="#{msgs.prefs_listToolEventsOnlyAvailableInSite}"/>
					</t:div>			
					
					<t:htmlTag value="br"/>	
			
					<%/* #####  CHART  ##### */%>
					<t:div rendered="#{ServiceBean.enableSiteVisits || ServiceBean.enableSiteActivity}">
						<t:htmlTag value="h4" styleClass="summaryHeader">
							<h:outputText value="#{msgs.prefs_sep_chart}"/>
						</t:htmlTag>		
						<t:htmlTag value="p" styleClass="instruction">
							<h:outputText value="#{msgs.instructions_prefs_sep_chart}"/>
						</t:htmlTag>
						
						<t:selectBooleanCheckbox id="chart-3dcharts-check" value="#{PrefsBean.prefsdata.chartIn3D}"/>
						<t:outputLabel id="chart-3dcharts-label" for="chart-3dcharts-check" value="#{msgs.prefs_3dCharts}"/>
						
						<t:htmlTag value="br"/>
						
						<t:selectBooleanCheckbox id="chart-itemLabelsVisible-check" value="#{PrefsBean.prefsdata.itemLabelsVisible}"/>
						<t:outputLabel id="chart-itemLabelsVisible-label" for="chart-itemLabelsVisible-check" value="#{msgs.prefs_itemLabelsVisible}"/>
						
						<t:htmlTag value="p"/>
						
						<t:outputLabel for="chart-transparency" value="#{msgs.prefs_chartTransparency}: "/>
						<t:selectOneMenu id="chart-transparency" value="#{PrefsBean.chartTransparency}" immediate="true">
			            	<f:selectItems value="#{PrefsBean.chartAlphaValues}" />
			            </t:selectOneMenu>						
					</t:div>
				</t:div>
				
			
				<%/* #####  ACTIVITY EVENTS  ##### */%>
				<t:div rendered="#{ServiceBean.enableSiteActivity}">
					<t:htmlTag value="h4" styleClass="summaryHeader">
						<h:outputText value="#{msgs.prefs_sep_activity_definition}"/>
					</t:htmlTag>
					
					<t:div styleClass="left">		
						<t:htmlTag value="p" styleClass="instruction">
							<h:outputText value="#{msgs.instructions_prefs_activity_tools}"/>
						</t:htmlTag>			
						<t:tree2 
							id="clientTree" 
							value="#{PrefsBean.treeData}"
							var="node" 
							varNodeToggler="t" 
							showRootNode="false" javascriptLocation="/sitestats/script/tree2" imageLocation="/sitestats/images/tree2">
					        <f:facet name="tool">
					            <h:panelGroup>
					                <f:facet name="expand">
					                </f:facet>
					                <f:facet name="collapse">
					                </f:facet>
									<t:selectBooleanCheckbox value="#{node.selected}" immediate="true"
										onclick="selectTool(this)"/>
					                <t:graphicImage value="/sitestats/images/silk/icons/application_side_boxes.png" border="0" styleClass="nodeIcon"/>
					                <h:outputText value=" #{node.description}" styleClass="nodeToolUnselected" rendered="#{!node.selected}"/>
					                <h:outputText value=" #{node.description}" styleClass="nodeToolSelected" rendered="#{node.selected && node.allChildsSelected}"/>
					                <h:outputText value=" #{node.description}" styleClass="nodeToolPartialSelected" rendered="#{node.selected && !node.allChildsSelected}"/>
					                <h:outputText value=" (#{node.childCount})" styleClass="childCount" rendered="#{!empty node.children}"/>
					            </h:panelGroup>
					        </f:facet>
					        <f:facet name="event">
					            <h:panelGroup>
					                <f:facet name="expand">
					                </f:facet>
					                <f:facet name="collapse">
					                </f:facet>
									<t:selectBooleanCheckbox value="#{node.selected}" immediate="true"
										onclick="selectEvent(this)"/>
					                <t:graphicImage value="/sitestats/images/silk/icons/bullet_feed.png" border="0" styleClass="nodeIcon"/>
					                <h:outputText value=" #{node.description}" styleClass="nodeEventUnselected" rendered="#{!node.selected}"/>
					                <h:outputText value=" #{node.description}" styleClass="nodeEventSelected" rendered="#{node.selected}"/>
					            </h:panelGroup>
					        </f:facet>
				        </t:tree2>
			        </t:div>
		        </t:div>

	        </h:panelGrid>
				
			
			
			<sakai:button_bar>
				<h:commandButton value="#{msgs.update}" actionListener="#{PrefsBean.processUpdate}" styleClass="active" immediate="false"/>
				<h:commandButton value="#{msgs.cancel}" actionListener="#{PrefsBean.processCancel}" immediate="true"/> 
			</sakai:button_bar>
			
		</t:div>
	    
	</h:form>
    
</sakai:view>
</f:view>