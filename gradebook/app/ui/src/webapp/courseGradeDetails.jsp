<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">

		<t:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@ include file="/inc/appMenu.jspf"%>
		</t:aliasBean>

		<!-- Course Grade Summary -->
		<sakai:flowState bean="#{courseGradeDetailsBean}" />
		
		<h2><h:outputText value="#{msgs.course_grade_details_title}"/></h2>
		<p class="instruction">
			<h:outputText value="#{msgs.course_grade_details_null_msg}  " rendered="#{courseGradeDetailsBean.userAbleToGradeAll}"/>
			<h:outputText value="#{msgs.course_grade_details_null_msg_ta_view} " rendered="#{!courseGradeDetailsBean.userAbleToGradeAll}"/>
		</p>

		<h4><h:outputText value="#{msgs.course_grade_details_page_title}"/></h4>
		<div class="indnt1">
		<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">
			<h:outputText id="pointsLabel" value="#{msgs.course_grade_details_points}" rendered="#{!courseGradeDetailsBean.weightingEnabled}"/>
			<h:outputText id="points" value="#{courseGradeDetailsBean.totalPoints}" rendered="#{!courseGradeDetailsBean.weightingEnabled}">
				<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
			</h:outputText>
			
			<h:outputText id="courseGradeLabel" value="#{msgs.avg_course_grade_name}" rendered="#{courseGradeDetailsBean.userAbleToGradeAll}" />
			<h:panelGroup rendered="#{courseGradeDetailsBean.userAbleToGradeAll}">
				<h:outputText id="letterGrade" value="#{courseGradeDetailsBean.averageCourseGrade} " />
				<h:outputText id="cumScore" value="#{courseGradeDetailsBean.courseGrade}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:panelGroup>	

		</h:panelGrid>
		</div>

		<h4><h:outputText value="#{msgs.assignment_details_grading_table}"/></h4>
		<div class="indnt1">

		<%@ include file="/inc/globalMessages.jspf"%>
		
		<div class="instruction"><h:outputText value="#{msgs.course_grade_details_instruction}" escape="false"/></div>

		<t:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@ include file="/inc/filterPaging.jspf"%>
		</t:aliasBean>

		<!-- Grading Table -->
		<t:dataTable cellpadding="0" cellspacing="0"
			id="gradingTable"
			value="#{courseGradeDetailsBean.scoreRows}"
			var="scoreRow"
			rowIndexVar="scoreRowIndex"
			sortColumn="#{courseGradeDetailsBean.sortColumn}"
            sortAscending="#{courseGradeDetailsBean.sortAscending}"
            columnClasses="left,left,left,left,left"
			styleClass="listHier">
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentSortName" propertyName="studentSortName" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_name}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.user.sortName}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentDisplayId" propertyName="studentDisplayId" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_id}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.user.displayId}"/>
			</h:column>
			<h:column rendered="#{!courseGradeDetailsBean.weightingEnabled}">
				<f:facet name="header">
		            <t:commandSortHeader columnName="pointsEarned" propertyName="pointsEarned" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_points}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.courseGradeRecord.pointsEarned}" rendered="#{scoreRow.calculatedLetterGrade != null}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
				</h:outputText>
				
				<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{scoreRow.calculatedLetterGrade == null}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="autoCalc" propertyName="autoCalc" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.course_grade_details_calculated_grade}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:panelGroup rendered="#{scoreRow.calculatedLetterGrade !=  null}">
					<h:outputFormat value="#{msgs.course_grade_details_grade_display}" >
						<f:param value="#{scoreRow.calculatedLetterGrade}" />
						<f:param value="#{scoreRow.calculatedPercentGrade}" />
					</h:outputFormat>
				</h:panelGroup>

				<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{scoreRow.calculatedLetterGrade == null}"/>

			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.course_grade_details_log}" styleClass="tier0"/>
				</f:facet>
				<h:outputLink value="#"
					rendered="#{not empty scoreRow.eventRows}"
					onclick="javascript:dhtmlPopupToggle('#{scoreRowIndex}', event);return false;">
					<h:graphicImage value="images/log.png" alt="#{msgs.inst_view_log_alt}"/>
				</h:outputLink>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="override" propertyName="override" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.course_grade_details_grade}"/>
		            </t:commandSortHeader>
				</f:facet>
				<t:div styleClass="shorttext">
					<h:inputText rendered="#{scoreRow.userCanGrade}"
						id="Grade"
						value="#{scoreRow.enteredGrade}"
						size="4"
						onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.COURSE_GRADE" />
					</h:inputText>
					<h:outputText rendered="#{!scoreRow.userCanGrade && scoreRow.enteredGrade != null}" value="#{scoreRow.enteredGrade}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.COURSE_GRADE" />
					</h:outputText>
					<h:outputText rendered="#{!scoreRow.userCanGrade && scoreRow.enteredGrade == null}" value="#{msgs.score_null_placeholder}" />

				</t:div>
			</h:column>
			<h:column>
				<h:message for="Grade" styleClass="validationEmbedded" />
			</h:column>
		</t:dataTable>

		<t:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@ include file="/inc/gradingEventLogs.jspf"%>
		</t:aliasBean>

		<p class="instruction">
			<h:outputText value="#{msgs.course_grade_details_no_enrollments}" rendered="#{courseGradeDetailsBean.emptyEnrollments}" />
		</p>

		</div> <!-- END OF INDNT1 -->

		<div class="act">
			<h:commandButton
				value="#{msgs.course_grade_details_export_course_grades_pdf}"
				actionListener="#{courseGradeDetailsBean.exportPdf}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				/>

			<h:commandButton
				value="#{msgs.course_grade_details_export_course_grades}"
				actionListener="#{courseGradeDetailsBean.exportCsv}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				style="margin-left: 5em;"
				/>
			<h:commandButton
				value="#{msgs.course_grade_details_export_course_grades_excel}"
				actionListener="#{courseGradeDetailsBean.exportExcel}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				style="margin-left: 5em;"
				/>
			<h:outputText rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}" escape="false" value="<span class=\"highlightPanel\" style=\"padding:10px 5px 10px 10px\">"/>
			    <h:outputText styleClass="instruction" style="padding-right:10px;" value="#{courseGradeDetailsBean.exportCustomLabel}" rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}"/>
                <h:commandButton
    				value="#{msgs.course_grade_details_export_course_grades_institution_control}"
    				actionListener="#{courseGradeDetailsBean.exportCustomCsv}"
    				rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}"
    				/>
			<h:outputText rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}" escape="false" value="</span>" />
			<br /><br /><hr class="itemSeparator"/><br />
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.assignment_details_submit}"
				actionListener="#{courseGradeDetailsBean.processUpdateGrades}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				disabled="#{courseGradeDetailsBean.allStudentsViewOnly}"
				/>
			<h:commandButton
				value="#{msgs.assignment_details_cancel}"
				action="overview"
				immediate="true"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				disabled="#{courseGradeDetailsBean.allStudentsViewOnly}"
				/>

			<h:commandButton
				value="#{msgs.course_grade_details_calculate_course_grade}"
				action="calculateCourseGrades"
				rendered="#{courseGradeDetailsBean.userAbleToGradeAll}"
				style="margin-left: 5em;"
			/>
		</div>

	  </h:form>
	</div>
</f:view>
