<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">
		<t:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</t:aliasBean>

		<sakai:flowState bean="#{rosterBean}" />

		<h2><h:outputText value="#{msgs.roster_page_title}"/></h2>

		<t:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@include file="/inc/filterPaging.jspf"%>
		</t:aliasBean>

		<t:dataTable cellpadding="0" cellspacing="0"
			id="rosterTable"
			styleClass="listHier"
			value="#{rosterBean.studentRows}"
			binding="#{rosterBean.rosterDataTable}"
			sortColumn="#{rosterBean.sortColumn}"
            sortAscending="#{rosterBean.sortAscending}"
			var="row">
			<h:column id="studentNameData">
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentSortName" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_name}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.sortName}"/>
			</h:column>
			<h:column id="studentIdData">
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentDisplayId" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_id}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.displayId}"/>
			</h:column>
			<%/* Assignment columns will be dynamically appended, starting here. */%>
		</t:dataTable>

		<p class="instruction">
			<h:outputText value="#{msgs.roster_no_enrollments}" rendered="#{rosterBean.emptyEnrollments}" />
		</p>

		<p class="act">
			<h:commandButton
				id="exportExcel"
				value="#{msgs.roster_export_excel}"
				actionListener="#{rosterBean.exportExcel}"
				rendered="#{!rosterBean.emptyEnrollments}"
				/>
			<h:commandButton
				id="exportCsv"
				value="#{msgs.roster_export_csv}"
				actionListener="#{rosterBean.exportCsv}"
				rendered="#{!rosterBean.emptyEnrollments}"
				/>
		</p>
	  </h:form>
	</div>
</f:view>
