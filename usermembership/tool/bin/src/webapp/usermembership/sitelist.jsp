<%@ page import="org.sakaiproject.umem.tool.ui.SiteListBean"%>
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
	<script>includeLatestJQuery('sitelist.jsp');</script>
	<script type="text/javascript" src="/library/js/spinner.js"></script>
	<script type="text/javascript" src="/sakai-usermembership-tool/usermembership/js/usermembership.js"></script>
	<link href="/sakai-usermembership-tool/usermembership/css/usermembership.css" rel="stylesheet" type="text/css" media="all"></link>

	<script>
		USR_MEMBRSHP.frameID = "<%= SiteListBean.getFrameID() %>";
	</script>

	<%/*<sakai:flowState bean="#{SiteListBean}"/>*/%>
	<h:outputText value="#{SiteListBean.initValues}"/>
	
	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>

	<h:form id="sitelistform" rendered="#{SiteListBean.allowed}">
		<h3><h:outputText value="#{msgs.title_sitelist} (#{SiteListBean.userDisplayId})"/></h3>
		<sakai:instruction_message value="#{msgs.instructions_sitelist}" />

		<t:div id="headerContainer1" rendered="#{SiteListBean.renderTable && !SiteListBean.emptySiteList}">
			<t:div styleClass="headerWrapper" onclick="USR_MEMBRSHP.toggleActions( this );">
				<h:outputText id="actionHeader1" styleClass="collapsed" value="#{msgs.actions_header}" />
			</t:div>
		</t:div>
		<t:div id="actionContainer1" rendered="#{SiteListBean.renderTable && !SiteListBean.emptySiteList}">
			<t:div styleClass="column1">
				<h:commandButton type="button" title="#{msgs.select_all}" value="#{msgs.select_all}" onclick="USR_MEMBRSHP.applyStateToCheckboxes( true );" />
				<h:commandButton type="button" title="#{msgs.deselect_all}" value="#{msgs.deselect_all}" onclick="USR_MEMBRSHP.applyStateToCheckboxes( false );" />
				<h:commandButton type="button" title="#{msgs.invert_selection}" value="#{msgs.invert_selection}" onclick="USR_MEMBRSHP.invertSelection();" />
			</t:div>
			<t:div styleClass="column2">
				<h:commandButton id="setToInactive1" actionListener="#{SiteListBean.setToInactive}" value="#{msgs.set_to_inactive_button}" disabled="true"
								 onclick="SPNR.disableControlsAndSpin( this, null );" />
				<h:commandButton id="setToActive1" actionListener="#{SiteListBean.setToActive}" value="#{msgs.set_to_active_button}" disabled="true" 
								 onclick="SPNR.disableControlsAndSpin( this, null );" />
			</t:div>
			<t:div styleClass="column3">
				<h:commandButton id="exportCsv1" actionListener="#{SiteListBean.exportAsCsv}" value="#{msgs.export_selected_to_csv}" disabled="true" />
				<h:commandButton id="exportXls1" actionListener="#{SiteListBean.exportAsXls}" value="#{msgs.export_selected_to_excel}" disabled="true" />
			</t:div>
		</t:div>

		<t:dataTable
			value="#{SiteListBean.userSitesRows}"
			var="row1"
			styleClass="table table-hover table-striped table-bordered"
			columnClasses="visible,visible,visible,hidden-xs,hidden-xs,hidden-xs,hidden-xs,visible"
			sortColumn="#{SiteListBean.sitesSortColumn}"
            sortAscending="#{SiteListBean.sitesSortAscending}"
            rendered="#{SiteListBean.renderTable}" >
			<h:column id="statusToggle">
				<h:selectBooleanCheckbox value="#{row1.selected}" styleClass="chkStatus" onclick="this.value = this.checked; USR_MEMBRSHP.checkEnableButtons();" />
			</h:column>
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
			<t:column id="siteType" headerstyleClass="hidden-xs">
				<f:facet name="header">
		            <t:commandSortHeader columnName="siteType" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.site_type}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.siteType}"/>
			</t:column>
			<t:column id="siteTerm" headerstyleClass="hidden-xs">
				<f:facet name="header">
		            <t:commandSortHeader columnName="siteTerm" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.site_term}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.siteTerm}"/>
			</t:column>
			<t:column id="roleID" headerstyleClass="hidden-xs">
				<f:facet name="header">
		            <t:commandSortHeader columnName="roleId" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.role_name}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.roleName}"/>
			</t:column>
			<t:column id="pubView" headerstyleClass="hidden-xs">
				<f:facet name="header">
		            <t:commandSortHeader columnName="published" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.status}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.pubView}"/>
			</t:column>
			<h:column id="userStatus">
				<f:facet name="header">
		            <t:commandSortHeader columnName="userStatus" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.site_user_status}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row1.userStatus}"/>
			</h:column>
		</t:dataTable>

		<h:panelGroup rendered="#{SiteListBean.emptySiteList}">
			<p class="instruction" style="margin-top: 40px;">
				<h:outputText value="#{msgs.no_sitelist}" />
			</p>
		</h:panelGroup>

		<t:div id="headerContainer2" rendered="#{SiteListBean.renderTable && !SiteListBean.emptySiteList}" onclick="USR_MEMBRSHP.toggleActions( this );">
			<t:div styleClass="headerWrapper">
				<h:outputText id="actionHeader2" styleClass="collapsed" value="#{msgs.actions_header}" />
			</t:div>
		</t:div>
		<t:div id="actionContainer2" rendered="#{SiteListBean.renderTable && !SiteListBean.emptySiteList}">
			<t:div styleClass="column1">
				<h:commandButton type="button" title="#{msgs.select_all}" value="#{msgs.select_all}" onclick="USR_MEMBRSHP.applyStateToCheckboxes( true );" />
				<h:commandButton type="button" title="#{msgs.deselect_all}" value="#{msgs.deselect_all}" onclick="USR_MEMBRSHP.applyStateToCheckboxes( false );" />
				<h:commandButton type="button" title="#{msgs.invert_selection}" value="#{msgs.invert_selection}" onclick="USR_MEMBRSHP.invertSelection();" />
			</t:div>
			<t:div styleClass="column2">
				<h:commandButton id="setToInactive2" actionListener="#{SiteListBean.setToInactive}" value="#{msgs.set_to_inactive_button}" disabled="true"
								 onclick="SPNR.disableControlsAndSpin( this, null );" />
				<h:commandButton id="setToActive2" actionListener="#{SiteListBean.setToActive}" value="#{msgs.set_to_active_button}" disabled="true" 
								 onclick="SPNR.disableControlsAndSpin( this, null );" />
			</t:div>
			<t:div styleClass="column3">
				<h:commandButton id="exportCsv2" actionListener="#{SiteListBean.exportAsCsv}" value="#{msgs.export_selected_to_csv}" disabled="true" />
				<h:commandButton id="exportXls2" actionListener="#{SiteListBean.exportAsXls}" value="#{msgs.export_selected_to_excel}" disabled="true" />
			</t:div>
		</t:div>

		<t:div styleClass="act">
			<h:commandButton id="userlist" action="#{SiteListBean.processActionBack}" value="#{msgs.back_button}" styleClass="active"
											 onclick="SPNR.disableControlsAndSpin( this, null );" />
		</t:div>
	</h:form>
</sakai:view>
</f:view>
