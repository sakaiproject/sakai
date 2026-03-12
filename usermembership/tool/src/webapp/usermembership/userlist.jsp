<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>

<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addHeader("Cache-Control", "no-store");
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.umem.tool.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<script src="/library/js/spinner.js"></script>
	<script src="/sakai-usermembership-tool/usermembership/js/usermembership.js"></script>
	<%/*<sakai:flowState bean="#{UserListBean}"/>*/%>
	<h:outputText value="#{UserListBean.initValues}"/>
	
	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>	
	
	<style type="text/css">
		@import url("usermembership/css/usermembership.css");
	</style>

	<h:form id="userlistForm" rendered="#{UserListBean.allowed}">
		<t:div styleClass="page-header">
			<h1><h:outputText value="#{msgs.title_userlist}"/></h1>
		</t:div>
		<sakai:instruction_message value="#{msgs.instructions_userlist}" />

		<t:div styleClass="sakai-table-toolBar">
			<t:div styleClass="sakai-table-filterContainer">
				<t:div styleClass="sakai-table-viewFilter">
					<h:outputLabel for="selectType" value="#{msgs.combo_user_type}"/>
					<t:selectOneMenu id="selectType" styleClass="form-control" immediate="true" value="#{UserListBean.selectedUserType}" title="#{msgs.combo_user_type_title}">
						<f:selectItems value="#{UserListBean.userTypes}"/> 
					</t:selectOneMenu>
				</t:div>
				<t:div styleClass="sakai-table-viewFilter">
					<h:outputLabel for="selectAuthority" value="#{msgs.combo_user_ext}"/>
					<t:selectOneMenu id="selectAuthority" styleClass="form-control" immediate="true" value="#{UserListBean.selectedAuthority}" title="#{msgs.combo_user_ext_title}">
						<f:selectItems value="#{UserListBean.userAuthorities}"/> 
					</t:selectOneMenu>
				</t:div>
				<t:div styleClass="sakai-table-searchFilter">
					<h:outputLabel for="inputSearchBox" value="#{msgs.bar_input_search_title}"/>
					<t:div styleClass="sakai-table-searchFilterControls">
						<t:div styleClass="sakai-table-searchFilter-inputWrapper position-relative d-flex align-items-center flex-grow-1">
							<h:inputText id="inputSearchBox" value="#{UserListBean.searchKeyword}" styleClass="sakai-table-searchFilter-searchField form-control w-100"
								valueChangeListener="#{UserListBean.processActionSearchChangeListener}"
								size="20" title="#{msgs.bar_input_search_title}"
								onfocus="if(this.value == '#{msgs.bar_input_search_inst}') this.value = '';"
							/>
						</t:div>
						<t:div styleClass="act">
							<h:commandButton id="searchButton" action="#{UserListBean.processActionSearch}"
								onkeypress="document.forms[0].submit;" value="#{msgs.bar_search}"
								styleClass="active" onclick="SPNR.disableControlsAndSpin( this, null );" />
							<h:commandButton id="clearSearchButton" action="#{UserListBean.processActionClearSearch}"
								rendered="#{UserListBean.renderClearSearch}"
								onkeypress="document.forms[0].submit;" value="#{msgs.bar_clear_search}"
								onclick="SPNR.disableControlsAndSpin( this, null );" />
						</t:div>
					</t:div>
				</t:div>
			</t:div>
			<t:div styleClass="sakai-table-pagerContainer">
				<sakai:pager id="pager" totalItems="#{UserListBean.totalItems}" firstItem="#{UserListBean.firstItem}"
					pageSize="#{UserListBean.pageSize}" accesskeys="true" immediate="true"
					rendered="#{UserListBean.renderPager}" textItem="#{msgs.users}" />
			</t:div>
		</t:div>
	 	<t:dataTable 
			value="#{UserListBean.userRows}"
			var="row"
			styleClass="table table-hover table-striped table-bordered"
			columnClasses="d-table-cell, d-none d-sm-table-cell, d-table-cell, d-table-cell, d-table-cell, d-none d-sm-table-cell, d-none d-sm-table-cell, d-none d-sm-table-cell"
			sortColumn="#{UserListBean.userSortColumn}" 
			sortAscending="#{UserListBean.userSortAscending}"
			first="#{UserListBean.firstItem}"
			rows="#{UserListBean.rowsNumber}"
			rendered="#{UserListBean.renderTable}">
			<t:column id="userID">
				<f:facet name="header">
		            <t:commandSortHeader columnName="id" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.user_id}"/>
		            </t:commandSortHeader>
		        </f:facet>
		        <h:commandLink action="#{SiteListBean.processActionUserId}" value="#{row.userDisplayId}">
					<f:param name="userId" value="#{row.userID}"/>
				</h:commandLink>
			</t:column>
			<t:column id="internalUserId" headerstyleClass="d-none d-sm-table-cell">
				<f:facet name="header">
		            <t:commandSortHeader columnName="internalUserId" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.internal_user_id}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.userID}"/>
			</t:column>
			<h:column id="userName">
				<f:facet name="header">
		            <t:commandSortHeader columnName="name" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.user_name}"/>
		            </t:commandSortHeader>
		        </f:facet>        
				<h:outputText value="#{row.userName eq ''? UserListBean.noName : row.userName}"/>
			</h:column>
			<h:column id="userEmail">
				<f:facet name="header">
		            <t:commandSortHeader columnName="email" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.user_email}"/>
		            </t:commandSortHeader>
		        </f:facet>
		        <h:outputLink value="mailto:#{row.userEmail}"><h:outputText value="#{row.userEmail}"/></h:outputLink>
			</h:column>
			<h:column id="userType">
				<f:facet name="header">
		            <t:commandSortHeader columnName="type" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.user_type}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.userType}"/>
			</h:column>
			<t:column id="authority" headerstyleClass="d-none d-sm-table-cell">
				<f:facet name="header">
		            <t:commandSortHeader columnName="authority" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.user_authority}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.authority}"/>
			</t:column>
			<t:column id="createdOn" headerstyleClass="d-none d-sm-table-cell">
				<f:facet name="header">
		            <t:commandSortHeader columnName="createdOn" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.user_created_on}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.createdOn}">
					<f:convertDateTime dateStyle="medium" timeZone="#{UserListBean.userTimeZone}"/>
				</h:outputText>
			</t:column>
			<t:column id="modifiedOn" headerstyleClass="d-none d-sm-table-cell">
				<f:facet name="header">
		            <t:commandSortHeader columnName="modifiedOn" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.user_modified_on}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.modifiedOn}">
					<f:convertDateTime dateStyle="medium" timeZone="#{UserListBean.userTimeZone}"/>
				</h:outputText>
			</t:column>
		</t:dataTable>
	
		<h:outputText value="#{msgs.no_enrollments}" rendered="#{UserListBean.emptyUserList}" styleClass="instruction" />

		<t:div styleClass="act" rendered="#{!UserListBean.emptyUserList && UserListBean.renderTable}">
			<h:commandButton id="exportCsv" actionListener="#{UserListBean.exportAsCsv}" value="#{msgs.export_csv_button}"/>
			<h:commandButton id="exportXls" actionListener="#{UserListBean.exportAsXls}" value="#{msgs.export_excel_button}"/>
		</t:div>

	</h:form>
	<script>sakaiUserMembership.bindInputSearchChange();</script>
</sakai:view>
</f:view>
