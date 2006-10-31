<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

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
	<sakai:flowState bean="#{SiteListBean}"/>

	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>	
	
	<style type="text/css">
		@import url("sitestats/css/sitestats.css");
	</style>	

	<h:form id="sitelistForm" rendered="#{SiteListBean.allowed}">
		<h3><h:outputText value="#{msgs.menu_sitelist}" /></h3>
		<sakai:instruction_message value="#{msgs.instructions_sitelist}" />

		<f:subview id="allowed">
			<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
		</f:subview>
	
		<t:div style="width:100%">
			<%/* UI FILTERING CONTROLS */%>
			<h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="sst,sst">               
				<t:div style="text-align: left; white-space: nowrap; vertical-align:top;">
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
				<t:div style="text-align: right; white-space: nowrap; vertical-align:top;">
					<h:inputText id="inputSearchBox" value="#{SiteListBean.searchKeyword}" onclick="this.value=''" valueChangeListener="#{SiteListBean.processActionSearchChangeListener}"
							onfocus="if(this.value == '#{msgs.search_int2}') this.value = '';"/>				   	
					<h:commandButton id="searchButton" action="#{SiteListBean.processActionSearch}" onkeypress="document.forms[0].submit;" value="#{msgs.search}" styleClass="active"/>
					<h:commandButton id="clearSearchButton" action="#{SiteListBean.processActionClearSearch}" onkeypress="document.forms[0].submit;" value="#{msgs.clear_search}"/>					
				</t:div>
			</h:panelGrid>
			
			<%/* PAGER */%>
			<t:div style="text-align: right; white-space: nowrap;">
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
			        <t:commandLink action="#{BaseBean.processActionSiteId}" value="#{row.title}">
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
					
			<%/* EMPTY TABLE */%>
			<p class="instruction">
				<h:outputText value="#{msgs.no_data}" rendered="#{SiteListBean.emptySiteList}" />
			</p>	
			
		</t:div>
	
	</h:form>
	
</sakai:view>
</f:view>
