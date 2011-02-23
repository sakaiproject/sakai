<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<%
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.umem.tool.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<%/*<sakai:flowState bean="#{SiteListBean}"/>*/%>
	<h:outputText value="#{SiteListBean.initValues}"/>
	
	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>

	<h:form id="sitelistform" rendered="#{SiteListBean.allowed}">
		<h3><h:outputText value="#{msgs.title_sitelist} (#{SiteListBean.userDisplayId})"/></h3>
		<sakai:instruction_message value="#{msgs.instructions_sitelist}" />

		<t:dataTable
			value="#{SiteListBean.userSitesRows}"
			var="row1"
			styleClass="listHier narrowTable"
			sortColumn="#{SiteListBean.sitesSortColumn}"
            sortAscending="#{SiteListBean.sitesSortAscending}"
            rendered="#{SiteListBean.renderTable}" >
			<h:column id="siteName">
				<f:facet name="header">
		            <t:commandSortHeader columnName="siteName" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.site_name}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputLink target="_top" value="#{row1.siteURL}">
					<h:outputText value="#{row1.siteTitle}"/>
				</h:outputLink>
			</h:column>
			<h:column id="groups">
				<f:facet name="header">
		            <h:outputText value="#{msgs.groups}"/>
		        </f:facet>
				<h:outputText value="#{row1.groups}"/>
			</h:column>
			<h:column id="siteType">
				<f:facet name="header">
		            <t:commandSortHeader columnName="siteType" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.site_type}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.siteType}"/>
			</h:column>
			<h:column id="siteTerm">
				<f:facet name="header">
		            <t:commandSortHeader columnName="siteTerm" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.site_term}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.siteTerm}"/>
			</h:column>
			<h:column id="roleID">
				<f:facet name="header">
		            <t:commandSortHeader columnName="roleId" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.role_name}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.roleName}"/>
			</h:column>
			<h:column id="pubView">
				<f:facet name="header">
		            <t:commandSortHeader columnName="published" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.status}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.pubView}"/>
			</h:column>
			<h:column id="userStatus">
				<f:facet name="header">
		            <t:commandSortHeader columnName="userStatus" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.site_user_status}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.userStatus}"/>
			</h:column>
		</t:dataTable>

		<p class="instruction" style="margin-top: 40px;">
			<h:outputText value="#{msgs.no_sitelist}" rendered="#{SiteListBean.emptySiteList}" />
		</p>
		
		<br>
		<t:div styleClass="act" rendered="#{SiteListBean.allowed}">
			<h:commandButton id="userlist" action="#{SiteListBean.processActionBack}" value="#{msgs.back_button}" styleClass="active"/>
			<h:commandButton id="exportCsv" actionListener="#{SiteListBean.exportAsCsv}" value="#{msgs.export_csv_button}" rendered="#{!SiteListBean.emptySiteList}" />
			<h:commandButton id="exportXls" actionListener="#{SiteListBean.exportAsXls}" value="#{msgs.export_excel_button}" rendered="#{!SiteListBean.emptySiteList}" />
		</t:div>
	  	

	</h:form>

</sakai:view>
</f:view>
