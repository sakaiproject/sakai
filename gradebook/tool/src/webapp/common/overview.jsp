<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
	  <x:aliasBean alias="#{bean}" value="#{overviewBean}">
		<%@include file="/inc/appMenu.jspf"%>
	  </x:aliasBean>

	  <sakai:flowState bean="#{overviewBean}" />

		<h2><h:outputText value="#{msgs.appmenu_overview}"/></h2>

		<div class="instruction">
			<h:outputText value="#{msgs.overview_instruction}" escape="false"/>
			<h:panelGroup rendered="#{overviewBean.userAbleToEditAssessments}">
				<f:verbatim><p></f:verbatim>
				<h:outputText value="#{overviewBean.gradeOptionSummary} "/>
				<h:commandLink action="feedbackOptions" immediate="true">
					<h:outputText value="#{msgs.overview_grade_option_change}"/>
				</h:commandLink>
				<f:verbatim></p></f:verbatim>
			</h:panelGroup>
		</div>

		<%@include file="/inc/globalMessages.jspf"%>

		<h4><h:outputText value="#{msgs.overview_assignments_title}"/></h4>
		<x:dataTable cellpadding="0" cellspacing="0"
			id="assignmentsTable"
			value="#{overviewBean.gradableObjects}"
			var="gradableObject"
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
				<h:commandLink action="assignmentDetails" rendered="#{!gradableObject.courseGrade}">
					<h:outputText value="#{gradableObject.name}" />
					<f:param name="assignmentId" value="#{gradableObject.id}"/>
				</h:commandLink>

				<!-- Course grade link -->
				<h:commandLink action="courseGradeDetails" rendered="#{gradableObject.courseGrade}"  styleClass="courseGrade">
					<h:outputText value="#{gradableObject.name}" />
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="dueDate" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_due_date}"/>
		            </x:commandSortHeader>
		        </f:facet>

				<h:outputText value="#{gradableObject.dueDate}" rendered="#{! gradableObject.courseGrade && gradableObject.dueDate != null}"/>
				<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{! gradableObject.courseGrade && gradableObject.dueDate == null}"/>
			</h:column>
			<h:column rendered="#{overviewBean.userAbleToGradeAll}">
				<f:facet name="header">
		            <x:commandSortHeader columnName="mean" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_average}"/>
		            </x:commandSortHeader>
		        </f:facet>

				<h:outputText value="#{gradableObject.formattedMean}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.PERCENTAGE"/>
				</h:outputText>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="pointsPossible" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_points}"/>
		            </x:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{gradableObject}" escape="false">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.ASSIGNMENT_POINTS"/>
				</h:outputText>
			</h:column>
			<h:column>
				<h:outputText value="from #{gradableObject.externalAppName}" rendered="#{! gradableObject.courseGrade && ! empty gradableObject.externalAppName}"/>
			</h:column>
		</x:dataTable>

	  </h:form>
	</div>
</f:view>
