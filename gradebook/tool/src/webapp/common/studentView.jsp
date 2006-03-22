<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">
		<sakai:flowState bean="#{studentViewBean}" />

		<h2>
			<h:outputFormat value="#{msgs.student_view_page_title}"/>
			<h:outputFormat value="#{studentViewBean.userDisplayName}"/>
		</h2>

		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">
			<h:outputText value="#{msgs.student_view_cumulative_score}"/>
            <h:outputText value="#{msgs.student_view_not_released}" rendered="#{!studentViewBean.courseGradeReleased}"/>
            <h:outputFormat value="#{msgs.student_view_cumulative_score_details}" rendered="#{studentViewBean.courseGradeReleased}">
				<f:param value="#{studentViewBean.totalPointsEarned}" />
				<f:param value="#{studentViewBean.totalPointsScored}" />
				<f:param value="#{studentViewBean.percent}" />
			</h:outputFormat>

			<h:outputText value="#{msgs.student_view_course_grade}" />
			<h:panelGroup>
				<h:outputText value="#{msgs.student_view_not_released}" rendered="#{!studentViewBean.courseGradeReleased}"/>
				<h:outputText value="#{studentViewBean.courseGrade}" rendered="#{studentViewBean.courseGradeReleased}"/>
				<h:outputText value="#{msgs.student_view_not_counted_assignments}" rendered="#{studentViewBean.anyNotCounted && studentViewBean.courseGradeReleased}" escape="false"/>
			</h:panelGroup>
		</h:panelGrid>

		<h:panelGroup rendered="#{studentViewBean.assignmentsReleased}">
			<f:verbatim><fieldset>
			<legend></f:verbatim><h:outputText value="#{msgs.student_view_assignments}"/><f:verbatim></legend></f:verbatim>

			<x:dataTable cellpadding="0" cellspacing="0"
				id="studentViewTable"
				value="#{studentViewBean.assignmentGradeRows}"
				var="row"
				sortColumn="#{studentViewBean.sortColumn}"
				sortAscending="#{studentViewBean.sortAscending}"
				columnClasses="left,left,center,center,left,external"
				rowClasses="#{studentViewBean.rowStyles}"
				styleClass="listHier wideTable">
				<h:column>
					<f:facet name="header">
						<x:commandSortHeader columnName="name" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_title}"/>
						</x:commandSortHeader>
					</f:facet>

					<h:outputText value="#{row.assignment.name}" />
				</h:column>
				<h:column>
					<f:facet name="header">
						<x:commandSortHeader columnName="dueDate" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_due_date}"/>
						</x:commandSortHeader>
					</f:facet>

					<h:outputText value="#{row.assignment.dueDate}" rendered="#{row.assignment.dueDate != null}">
						<f:convertDateTime />
					</h:outputText>
					<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{row.assignment.dueDate == null}"/>
				</h:column>
				<h:column>
					<f:facet name="header">
						<x:commandSortHeader columnName="pointsEarned" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_score}"/>
						</x:commandSortHeader>
					</f:facet>

                    <h:outputText value="#{row.gradeRecord}" escape="false" rendered="#{studentViewBean.courseGradeReleased}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.ASSIGNMENT_POINTS"/>
					</h:outputText>
                    <h:outputText value="#{row.gradeRecord.pointsEarned}" escape="false" rendered="#{!studentViewBean.courseGradeReleased}">
                        <f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS"/>
                    </h:outputText>

                </h:column>
				<h:column>
					<f:facet name="header">
						<x:commandSortHeader columnName="pointsPossible" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_points}"/>
						</x:commandSortHeader>
					</f:facet>
					<h:outputText value="#{row.assignment}" escape="false" rendered="#{studentViewBean.courseGradeReleased}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.ASSIGNMENT_POINTS"/>
					</h:outputText>

                    <h:outputText value="#{row.gradeRecord.pointsEarned}" escape="false" rendered="#{!studentViewBean.courseGradeReleased}">
                        <f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS"/>
                    </h:outputText>

                </h:column>
				<h:column>
					<f:facet name="header">
						<x:commandSortHeader columnName="grade" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_grade}"/>
						</x:commandSortHeader>
					</f:facet>
					<h:outputText value="#{row.displayGrade}"/>
				</h:column>
				<h:column>
					<h:outputText value="#{row.assignment.externalAppName}" />
				</h:column>
			</x:dataTable>

			<f:verbatim></fieldset></f:verbatim>
		</h:panelGroup>

	  </h:form>
	</div>
</f:view>
