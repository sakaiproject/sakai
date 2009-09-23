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

	<%/* #####  FACES MESSAGES  ##### */%>
	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>
	
	<style type="text/css">
		@import url("sitestats/css/sitestats.css");
	</style>	

	<h:form id="sitelistForm" rendered="#{SiteListBean.allowed}">
	    
		<%/* #####  MENU  ##### */%>
		<h:panelGroup rendered="#{ServiceBean.serverWideStatsEnabled}">
	        <t:aliasBean alias="#{viewName}" value="SiteListBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
        </h:panelGroup>

		
		<%/* #####  TITLE  ##### */%>
		<t:htmlTag value="h2">
			<h:outputText value="#{msgs.menu_sitelist}"/>
		</t:htmlTag>
		<sakai:instruction_message value="#{msgs.instructions_sitelist}" />
	    
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div style="width:100%">
		
			<%/* #####  UI FILTERING CONTROLS  ##### */%>
			<h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="halfSize,halfSize">               
				<t:div styleClass="left" >
					<h:outputLabel for="selectType" value="#{msgs.site_type}"/>
					<t:selectOneMenu 
		    	            id="selectType"
				           	value="#{SiteListBean.selectedSiteType}"
				           	immediate="true"
				           	onchange="submit();"
				           	valueChangeListener="#{SiteListBean.processActionSiteTypeChangeListener}" >
			    	    <f:selectItems value="#{SiteListBean.siteTypes}"/> 
			        </t:selectOneMenu>
				</t:div>
				<t:div styleClass="right">
					<h:inputText id="inputSearchBox" value="#{SiteListBean.searchKeyword}" onclick="this.value=''" valueChangeListener="#{SiteListBean.processActionSearchChangeListener}"
							onfocus="if(this.value == '#{msgs.search_int2}') this.value = '';"/>				   	
					<h:commandButton id="searchButton" action="#{SiteListBean.processActionSearch}" onkeypress="document.forms[0].submit;" value="#{msgs.search}" styleClass="active"/>
					<h:commandButton id="clearSearchButton" action="#{SiteListBean.processActionClearSearch}" onkeypress="document.forms[0].submit;" value="#{msgs.clear_search}"/>					
				</t:div>
			</h:panelGrid>
			
			<%/* #####  PAGER  ##### */%>
			<t:div styleClass="left">
				<sakai:pager
					id="pager" 
					totalItems="#{SiteListBean.totalItems}"
					firstItem="#{SiteListBean.firstItem}"
					pageSize="#{SiteListBean.pageSize}"
					accesskeys="true"
					immediate="true"
					textItem="#{msgs.sites}" />
			</t:div>
			
			<%/* TABLE */%>
			<%/* 
            first="#{SiteListBean.firstItem}"
            rows="#{SiteListBean.pageSize}" */%>
			<t:dataTable 
				value="#{SiteListBean.siteRows}"
				var="row"
				styleClass="listHier narrowTable"
	            sortColumn="#{SiteListBean.sortColumn}" 
	            sortAscending="#{SiteListBean.sortAscending}">
				<h:column id="title">
					<f:facet name="header">
			            <t:commandSortHeader columnName="title" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_title}"/>		                
			            </t:commandSortHeader>
			        </f:facet>		  
			        <t:commandLink action="#{ServiceBean.processActionSiteId}" value="#{row.title}">
						<f:param name="siteId" value="#{row.id}"/>
					</t:commandLink>      
				</h:column>
				<h:column id="type">
					<f:facet name="header">
			            <t:commandSortHeader columnName="type" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_type}"/>
			            </t:commandSortHeader>
			        </f:facet>        
					<h:outputText value="#{row.type}"/>
				</h:column>
				<h:column id="status">
					<f:facet name="header">
			            <t:commandSortHeader columnName="status" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_status}"/>
			            </t:commandSortHeader>
			        </f:facet>
					<h:outputText value="#{row.published? msgs.site_published : msgs.site_unpublished}"/>
				</h:column>
			</t:dataTable>
			
			<%/* #####  EMPTY TABLE  ##### */%>
			<p class="instruction">
				<h:outputText value="#{msgs.no_data}" rendered="#{SiteListBean.emptySiteList}" />
			</p>	
			
		</t:div>
	
	</h:form>
	
</sakai:view>
</f:view>
