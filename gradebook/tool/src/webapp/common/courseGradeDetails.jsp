<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">

		<x:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</x:aliasBean>

		<!-- Course Grade Summary -->
		<sakai:flowState bean="#{courseGradeDetailsBean}" />

		<h2><h:outputText value="#{courseGradeDetailsBean.courseGrade.name}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.course_grade_details_instruction}" escape="false"/></div>

		<h4><h:outputText value="#{msgs.course_grade_details_page_title}"/></h4>
		<div class="indnt1">
		<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">
			<h:outputText id="pointsLabel" value="#{msgs.course_grade_details_points}"/>
			<h:outputText id="points" value="#{courseGradeDetailsBean.courseGrade.pointsForDisplay}">
				<f:convertNumber maxFractionDigits="2"/>
			</h:outputText>

			<h:outputText id="averageLabel" value="#{msgs.course_grade_details_average}" rendered="#{overviewBean.userAbleToGradeAll}"/>
			<h:outputText id="average" value="#{courseGradeDetailsBean.courseGrade.formattedMean}" rendered="#{overviewBean.userAbleToGradeAll}">
				<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.PERCENTAGE"/>
			</h:outputText>
		</h:panelGrid>
		</div>

		<h4><h:outputText value="#{msgs.assignment_details_grading_table}"/></h4>
		<div class="indnt1">

		<%@include file="/inc/globalMessages.jspf"%>

		<x:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@include file="/inc/filterPaging.jspf"%>
		</x:aliasBean>

		<!-- Grading Table -->
		<x:dataTable cellpadding="0" cellspacing="0"
			id="gradingTable"
			value="#{courseGradeDetailsBean.scoreRows}"
			var="scoreRow"
			rowIndexVar="scoreRowIndex"
			sortColumn="#{courseGradeDetailsBean.sortColumn}"
            sortAscending="#{courseGradeDetailsBean.sortAscending}"
            columnClasses="left,left,right,left,right"
			styleClass="listHier">
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentSortName" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_name}"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.user.sortName}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentDisplayId" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_id}"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.user.displayId}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="pointsEarned" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_points}"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.courseGradeRecord.pointsEarned}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
				</h:outputText>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="autoCalc" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.course_grade_details_calculated_grade}"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.calculatedLetterGrade}"/>
				<h:outputText value=" ("/>
				<h:outputText value="#{scoreRow.calculatedPercentGrade}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.PERCENTAGE" />
				</h:outputText>
				<h:outputText value=")"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.course_grade_details_log}" styleClass="tier0"/>
				</f:facet>
				<h:outputLink value="#"
					rendered="#{not empty scoreRow.eventRows}"
					onclick="javascript:dhtmlPopupToggle('#{scoreRowIndex}', event);return false;">
					<h:graphicImage value="images/log.png" alt="Show log"/>
				</h:outputLink>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="override" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.course_grade_details_grade}"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:inputText
					id="Grade"
					value="#{scoreRow.courseGradeRecord.enteredGrade}"
					size="4"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.COURSE_GRADE" />
				</h:inputText>
			</h:column>
			<h:column>
				<h:message for="Grade" styleClass="validationEmbedded" />
			</h:column>
		</x:dataTable>

		<x:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@include file="/inc/gradingEventLogs.jspf"%>
		</x:aliasBean>

		<p class="instruction">
			<h:outputText value="#{msgs.course_grade_details_no_enrollments}" rendered="#{courseGradeDetailsBean.emptyEnrollments}" />
		</p>

		</div> <!-- END OF INDNT1 -->

		<p class="act">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.assignment_details_submit}"
				actionListener="#{courseGradeDetailsBean.processUpdateGrades}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				/>
			<h:commandButton
				value="#{msgs.course_grade_details_export_excel}"
				actionListener="#{exportBean.exportCourseGradeExcel}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				/>
			<h:commandButton
				value="#{msgs.course_grade_details_export_csv}"
				actionListener="#{exportBean.exportCourseGradeCsv}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				/>
			<h:commandButton
				value="#{msgs.assignment_details_cancel}"
				action="overview"
				immediate="true"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				/>
		</p>
	  </h:form>
	</div>
</f:view>
