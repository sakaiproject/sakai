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

<f:view>
<f:loadBundle basename="org.sakaiproject.sitestats.tool.bundle.Messages" var="msgs"/>
<sakai:view title="#{msgs.tool_title}">
	<sakai:flowState bean="#{EventsBean}"/>

	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>	
	
	<style type="text/css">
		@import url("sitestats/css/sitestats.css");
	</style>	
	        
	        
	<%/* MENU */%>
	<h:panelGroup rendered="#{BaseBean.allowed}">
		<t:aliasBean  alias="#{viewName}" value="EventsBean">
			<f:subview id="menu">
				<%@include file="inc/navmenu.jsp"%>
			</f:subview>
        </t:aliasBean>
    </h:panelGroup>    
    
    
	<%/* TITLE */%>
	<h3>
		<h:outputText value="#{msgs.menu_events} (#{BaseBean.siteTitle})" rendered="#{BaseBean.adminView}"/>
		<h:outputText value="#{msgs.menu_events}" rendered="#{!BaseBean.adminView}"/>
	</h3>
	
	
	<h:form rendered="#{BaseBean.allowed}">			
		<t:div style="width:100%">		
		
			<%/* UI FILTERING CONTROLS */%>
			<t:aliasBean  alias="#{bean}" value="#{EventsBean}" >
				<f:subview id="filtering">
					<%@include file="inc/filtering.jsp"%>
				</f:subview>
		    </t:aliasBean>


			<%/* TABLE DATA */%>
			<t:dataTable
				value="#{EventsBean.events}"
				var="row"
				styleClass="listHier narrowTable"
				columnClasses="left,left,left,right"
				sortColumn="#{EventsBean.sortColumn}" 
	            sortAscending="#{EventsBean.sortAscending}"
	            first="#{EventsBean.firstItem}"
	            rows="#{EventsBean.pageSize}">
				<h:column>
					<f:facet name="header">	 
			            <t:commandSortHeader columnName="id" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_id}"/>		                
			            </t:commandSortHeader>               
			        </f:facet>
			        <h:outputText value="#{row.userId}" escape="false">
						<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.USER_ID_EID"/>
					</h:outputText>
				</h:column>
				<h:column>
					<f:facet name="header">	 
			            <t:commandSortHeader columnName="user" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_user}"/>		                
			            </t:commandSortHeader>               
			        </f:facet>
			        <h:outputText value="#{row.userId}" escape="false">
						<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.USER_ID_NAME"/>
					</h:outputText>
				</h:column>
				<h:column>
					<f:facet name="header">
			            <t:commandSortHeader columnName="event" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_event}"/>		                
			            </t:commandSortHeader>   	                
			        </f:facet>
			        <h:outputText value="#{row.ref}" escape="false">
						<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.EVENT_ID_NAME"/>
					</h:outputText>
				</h:column>
				<h:column>
					<f:facet name="header">
			            <t:commandSortHeader columnName="date" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_date}"/>		                
			            </t:commandSortHeader>   	  	                
			        </f:facet>		  
			        <t:outputText value="#{row.dateAsString}"/>    
				</h:column>
				<h:column >
					<f:facet name="header">
			            <t:commandSortHeader columnName="total" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_total}" styleClass="center"/>		                
			            </t:commandSortHeader>  		                
			        </f:facet>		  
			        <h:outputText value="#{row.count}" style="text-align: right;"/>    
				</h:column>
			</t:dataTable>
			<p class="instruction">
				<h:outputText value="#{msgs.no_data}" rendered="#{EventsBean.emptyList}" />
			</p>
			
		
			<%/* EXPORT BUTTONS */%>
			<p class="act">
				<t:commandButton id="exportXls" actionListener="#{EventsBean.exportEventsXls}" value="#{msgs.bt_export_excel}" rendered="#{!EventsBean.emptyList}" />
				<t:commandButton id="exportCsv" actionListener="#{EventsBean.exportEventsCsv}" value="#{msgs.bt_export_csv}" rendered="#{!EventsBean.emptyList}" />
			</p>
		</t:div>
	</h:form>
		
</sakai:view>
</f:view>
