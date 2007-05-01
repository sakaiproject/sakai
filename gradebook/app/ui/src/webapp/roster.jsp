<f:view>
	<script type="text/javascript" src="/library/js/jquery-1.1.2.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/spreadsheetUI.js"></script>
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

		<gbx:spreadsheetUI 
			colLock="3"
			initialHeight="200px"
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
				<h:commandLink action="instructorView">
					<h:outputText value="#{row.sortName}"/>
					<f:param name="studentUid" value="#{row.studentUid}"/>
					<f:param name="returnToPage" value="roster" />
				</h:commandLink>
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
		</gbx:spreadsheetUI>

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
