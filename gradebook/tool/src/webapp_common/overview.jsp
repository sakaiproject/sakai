<f:view>
	<h:form id="gbForm">
	  <x:aliasBean alias="#{viewName}" value="overview">
		<%@include file="/inc/appMenu.jspf"%>
	  </x:aliasBean>

	  <gbx:flowState bean="#{overviewBean}" />

	  <div class="portletBody">
		<h2><h:outputText value="#{msgs.appmenu_overview}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.overview_instruction}" escape="false"/></div>

		<%@include file="/inc/globalMessages.jspf"%>

		<h4><h:outputText value="#{msgs.overview_assignments_title}"/></h4>
		<x:dataTable cellpadding="0" cellspacing="0"
			id="assignmentsTable"
			value="#{overviewBean.gradableObjectRows}"
			var="gradableObjectRow"
			sortColumn="#{overviewBean.assignmentSortColumn}"
            sortAscending="#{overviewBean.assignmentSortAscending}"
            columnClasses="left,left,rightpadded,rightpadded,external"
            rowClasses="#{overviewBean.rowStyles}"
			styleClass="listHier narrowTable">
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="name" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.overview_assignments_header_name}" />
		            </x:commandSortHeader>
		        </f:facet>

				<!-- Assignment / Assessment link -->
				<h:commandLink action="assignmentDetails" rendered="#{!gradableObjectRow.courseGrade}">
					<h:outputText value="#{gradableObjectRow.name}" />
					<f:param name="assignmentId" value="#{gradableObjectRow.id}"/>
				</h:commandLink>

				<!-- Course grade link -->
				<h:commandLink action="courseGradeDetails" rendered="#{gradableObjectRow.courseGrade}"  styleClass="courseGrade">
					<h:outputText value="#{gradableObjectRow.name}" />
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="dueDate" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_due_date}"/>
		            </x:commandSortHeader>
		        </f:facet>

				<h:outputText value="#{gradableObjectRow.dueDate}" rendered="#{gradableObjectRow.dueDate != null}"/>
				<h:outputText value="#{msgs.overview_null_placeholder}" rendered="#{gradableObjectRow.dueDate == null && ! gradableObjectRow.courseGrade}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="mean" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_average}"/>
		            </x:commandSortHeader>
		        </f:facet>

				<h:outputText value="#{gradableObjectRow.mean}" rendered="#{gradableObjectRow.mean != null}">
					<f:convertNumber type="percentage" integerOnly="true" />
				</h:outputText>
				<h:outputText value="#{msgs.overview_null_placeholder}" rendered="#{gradableObjectRow.mean == null}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="pointsPossible" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_points}"/>
		            </x:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{gradableObjectRow.points}" rendered="#{gradableObjectRow.points != null}">
					<f:convertNumber />
				</h:outputText>
				<h:outputText value="#{msgs.overview_null_placeholder}" rendered="#{gradableObjectRow.points == null}"/>
			</h:column>
			<h:column>
				<h:outputText value="from #{gradableObjectRow.externalAppName}" rendered="#{!empty gradableObjectRow.externalAppName}"/>
			</h:column>
		</x:dataTable>

	  </div>
	</h:form>
</f:view>
