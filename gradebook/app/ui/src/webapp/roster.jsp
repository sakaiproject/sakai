<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">
		<x:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</x:aliasBean>

		<sakai:flowState bean="#{rosterBean}" />

		<h2><h:outputText value="#{msgs.roster_page_title}"/></h2>

		<x:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@include file="/inc/filterPaging.jspf"%>
		</x:aliasBean>

		<x:dataTable cellpadding="0" cellspacing="0"
			id="rosterTable"
			styleClass="listHier"
			value="#{rosterBean.studentRows}"
			binding="#{rosterBean.rosterDataTable}"
			sortColumn="#{rosterBean.sortColumn}"
            sortAscending="#{rosterBean.sortAscending}"
			var="row">
			<h:column id="studentNameData">
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentSortName" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_name}"/>
		            </x:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.sortName}"/>
			</h:column>
			<h:column id="studentIdData">
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentDisplayId" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_id}"/>
		            </x:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.displayId}"/>
			</h:column>
			<%/* Assignment columns will be dynamically appended, starting here. */%>
		</x:dataTable>

		<p class="instruction">
			<h:outputText value="#{msgs.roster_no_enrollments}" rendered="#{rosterBean.emptyEnrollments}" />
		</p>

		<p class="act">
			<h:commandButton
				id="exportExcel"
				value="#{msgs.roster_export_excel}"
				actionListener="#{exportBean.exportRosterExcel}"
				rendered="#{!rosterBean.emptyEnrollments}"
				/>
			<h:commandButton
				id="exportCsv"
				value="#{msgs.roster_export_csv}"
				actionListener="#{exportBean.exportRosterCsv}"
				rendered="#{!rosterBean.emptyEnrollments}"
				/>
		</p>
	  </h:form>
	</div>
</f:view>
