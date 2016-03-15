<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<script src="js/dynamicSizeCheck.js" type="text/javascript"></script>
<script src="js/scoringAgent/integration.js" type="text/javascript"></script>
<script src="/library/js/spinner.js" type="text/javascript"></script>

<f:view>
  <div class="portletBody">
	<h:form id="gbForm" onsubmit="return blockDoubleSubmit();">
	

		<sakai:flowState bean="#{assignmentDetailsBean}" />

		<t:aliasBean alias="#{bean}" value="#{assignmentDetailsBean}">
			<%@ include file="/inc/appMenu.jspf"%>
		
			<%@ include file="/inc/breadcrumb.jspf" %>
		</t:aliasBean> 

		<h3><h:outputText value="#{msgs.assignment_details_page_title}"/></h3>
		<div class="indnt1">

		<p class="nav">
			<h:commandButton
				disabled="#{assignmentDetailsBean.first}"
				actionListener="#{assignmentDetailsBean.processAssignmentIdChange}"
				value="#{msgs.assignment_details_previous_assignment}"
				title="#{assignmentDetailsBean.previousAssignment.name}"
				accesskey="p"
				onclick="SPNR.disableControlsAndSpin( this, null );">
					<f:param name="assignmentId" value="#{assignmentDetailsBean.previousAssignment.id}"/>
			</h:commandButton>
			<h:commandButton
				action="#{assignmentDetailsBean.processCancel}"
				immediate="true"
				value="#{assignmentDetailsBean.returnString}"
				accesskey="l"
				onclick="SPNR.disableControlsAndSpin( this, null );"/>
			<h:commandButton
				disabled="#{assignmentDetailsBean.last}"
				actionListener="#{assignmentDetailsBean.processAssignmentIdChange}"
				value="#{msgs.assignment_details_next_assignment}"
				title="#{assignmentDetailsBean.nextAssignment.name}"
				accesskey="n"
				onclick="SPNR.disableControlsAndSpin( this, null );">
					<f:param name="assignmentId" value="#{assignmentDetailsBean.nextAssignment.id}"/>
			</h:commandButton>
		</p>

		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary"
 			summary="#{msgs.assignment_details_table_summary}"
			border="0">
				<h:outputText id="titleLabel" value="#{msgs.assignment_details_title}"/>
				<h:panelGroup>
					<h:outputText id="title" value="#{assignmentDetailsBean.assignment.name}"/>
					<h:outputText value=" (#{msgs.extra_credit})" rendered="#{assignmentDetailsBean.assignment.isExtraCredit}"/>
				</h:panelGroup>

				<h:outputText id="pointsLabel" value="#{msgs.assignment_details_points}"/>
				<h:outputText id="points" value="#{assignmentDetailsBean.assignment.pointsPossible}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
				</h:outputText>

				<h:outputText id="averageLabel" value="#{msgs.assignment_details_average}" rendered="#{overviewBean.userAbleToGradeAll}"/>
				<h:panelGroup rendered="#{overviewBean.userAbleToGradeAll}">
					<h:outputText id="average" value="#{assignmentDetailsBean.assignment}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_BASIC" />
					</h:outputText>
					<h:outputText id="averagePlaceholder" value="#{msgs.score_null_placeholder}" rendered="#{assignmentDetailsBean.assignment.formattedMean == null || assignmentDetailsBean.assignment.averageTotal == null}" />
				</h:panelGroup>
				
				<h:outputText id="categoryLabel" value="#{msgs.assignment_details_category}" rendered="#{assignmentDetailsBean.categoriesEnabled}" />
				<h:panelGroup rendered="#{assignmentDetailsBean.categoriesEnabled}" >
					<h:outputText id="category" value="#{assignmentDetailsBean.assignmentCategory}"  />
				</h:panelGroup>

				<h:outputText id="dueDateLabel" value="#{msgs.due_date}"/>
				<h:outputText id="dueDate" value="#{assignmentDetailsBean.assignment.dueDate}" rendered="#{assignmentDetailsBean.assignment.dueDate != null}" >
                     <gbx:convertDateTime/>
                </h:outputText>
				<h:outputText id="dueDatePlaceholder" value="#{msgs.score_null_placeholder}" rendered="#{assignmentDetailsBean.assignment.dueDate == null}" />

				<h:outputText id="optionsLabel" value="#{msgs.assignment_details_options}" rendered="#{assignmentDetailsBean.userAbleToEditAssessments}"/>
				<h:panelGrid cellpadding="0" cellspacing="0" columns="1" rendered="#{assignmentDetailsBean.userAbleToEditAssessments}">
					<h:outputText
						value="#{msgs.score_not_counted_tooltip}"
						rendered="#{assignmentDetailsBean.assignment.notCounted}"
					/>
					<h:commandLink
						action="#{assignmentDetailsBean.navigateToEdit}"
						accesskey="e"
						title="#{msgs.assignment_details_edit}">
						<h:outputFormat id="editAssignment" value="#{msgs.assignment_details_edit}" />
						<f:param name="assignmentId" value="#{assignmentDetailsBean.assignment.id}"/>
					</h:commandLink>
					<h:commandLink
						action="removeAssignment"
						rendered="#{!assignmentDetailsBean.assignment.externallyMaintained}"
						accesskey="r"
						title="#{msgs.assignment_details_remove}">
							<h:outputText id="removeAssignment" value="#{msgs.assignment_details_remove}"/>
						<f:param name="assignmentId" value="#{assignmentDetailsBean.assignment.id}"/>
					</h:commandLink>

					<h:outputLink
						value="#{assignmentDetailsBean.assignment.externalInstructorLink}"
						rendered="#{assignmentDetailsBean.assignment.externallyMaintained && not empty assignmentDetailsBean.assignment.externalInstructorLink}"
						accesskey="x"
						title="#{msgs.assignment_details_edit}">
							<h:outputFormat value="#{msgs.assignment_details_external_edit}">
								<f:param value="#{assignmentDetailsBean.assignment.externalAppName}"/>
							</h:outputFormat>
					</h:outputLink>

					<h:outputFormat value="#{msgs.assignment_details_external_link_unavailable}" rendered="#{assignmentDetailsBean.assignment.externallyMaintained && empty assignmentDetailsBean.assignment.externalInstructorLink}">
						<f:param value="#{assignmentDetailsBean.assignment.externalAppName}"/>
					</h:outputFormat>
				</h:panelGrid>
		</h:panelGrid>

		</div> <!-- END OF INDNT1 -->

		<h4><h:outputText value="#{msgs.assignment_details_grading_table}"/></h4>
		<div class="indnt1">

		<%@ include file="/inc/globalMessages.jspf"%>

		<t:aliasBean alias="#{bean}" value="#{assignmentDetailsBean}">
			<%@ include file="/inc/filterPaging.jspf"%>
		</t:aliasBean>
		
		<div id="buttonDiv1" class="act gbButtonBar">
			<h:commandButton
				id="saveButton1"
				styleClass="active"
				value="#{msgs.assignment_details_submit}"
				actionListener="#{assignmentDetailsBean.processUpdateScores}"
				disabled="#{assignmentDetailsBean.assignment.externallyMaintained || assignmentDetailsBean.allStudentsViewOnly}"
				rendered="#{!assignmentDetailsBean.emptyEnrollments}"
				accesskey="s"
				title="#{msgs.assignment_details_submit}"
				onclick="SPNR.disableControlsAndSpin( this, null );"/>
			<h:commandButton
				id="cancelButton1"
				value="#{msgs.assignment_details_cancel}"
				action="assignmentDetails"
				disabled="#{assignmentDetailsBean.assignment.externallyMaintained || assignmentDetailsBean.allStudentsViewOnly}"
				rendered="#{!assignmentDetailsBean.emptyEnrollments}"
				accesskey="c"
				immediate="true"
				title="#{msgs.assignment_details_cancel}" onclick="SPNR.disableControlsAndSpin( this, null );">
					<f:param name="breadcrumbPage" value="#{assignmentDetailsBean.breadcrumbPage}"/>
			</h:commandButton>
		</div>

		<t:dataTable cellpadding="0" cellspacing="0"
			id="gradingTable"
			value="#{assignmentDetailsBean.scoreRows}"
			var="scoreRow"
			rowIndexVar="scoreRowIndex"
			sortColumn="#{assignmentDetailsBean.sortColumn}"
			sortAscending="#{assignmentDetailsBean.sortAscending}"
			columnClasses="gbMessageAbove,gbMessageAbove,gbMessageAbove,gbMessageAbove,gbMessageAbove"
			headerClass="gbHeader"
			styleClass="listHier lines nolines gradingTable">
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentSortName" arrow="true" immediate="false" actionListener="#{assignmentDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_name}" styleClass="tier0"/>
		            </t:commandSortHeader>
				</f:facet>
				<t:div styleClass="gbTextOnRow">
				<h:commandLink action="#{assignmentDetailsBean.navigateToInstructorView}">
					<h:outputText value="#{scoreRow.enrollment.user.sortName}"/>
					<f:param name="studentUid" value="#{scoreRow.enrollment.user.userUid}"/>
					<f:param name="returnToPage" value="assignmentDetails" />
					<f:param name="assignmentId" value="#{assignmentDetailsBean.assignmentId}" />
				</h:commandLink>
				</t:div>
			</h:column>
			<h:column>
				<f:facet name="header">
		      <t:commandSortHeader columnName="studentDisplayId" arrow="true" immediate="false" actionListener="#{assignmentDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_id}" styleClass="tier0"/>
		      </t:commandSortHeader>
				</f:facet>
				<t:div styleClass="gbTextOnRow">
					<h:outputText value="#{scoreRow.enrollment.user.displayId}"/>
				</t:div>
			</h:column>

			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.assignment_details_log}" styleClass="tier0"/>
				</f:facet>
				<h:outputLink value="#"
					rendered="#{not empty scoreRow.eventRows}"
					onclick="javascript:dhtmlPopupToggle('#{scoreRowIndex}', event);return false;">
					<h:graphicImage value="images/log.png" alt="#{msgs.inst_view_log_alt}"/>
				</h:outputLink>
			</h:column>

			<%@include file="/inc/scoringAgent/assignmentDetails.jspf"%>

			<h:column>
				<f:facet name="header">
		      <t:commandSortHeader columnName="studentScore" arrow="true" immediate="false" actionListener="#{assignmentDetailsBean.sort}">
					  <h:outputText value="#{msgs.assignment_details_points}" rendered="#{assignmentDetailsBean.gradeEntryByPoints}"/>
					  <h:outputText value="#{msgs.assignment_details_percent}" rendered="#{assignmentDetailsBean.gradeEntryByPercent}"/>
					  <h:outputText value="#{msgs.assignment_details_letters}" rendered="#{assignmentDetailsBean.gradeEntryByLetter}" />
		      </t:commandSortHeader>
				</f:facet>

				<t:div>
					<h:panelGroup rendered="#{!scoreRow.droppedFromGrade && !assignmentDetailsBean.assignment.externallyMaintained && scoreRow.userCanGrade}">
						<h:inputText id="Score" value="#{scoreRow.score}" size="6" 
							 rendered="#{assignmentDetailsBean.gradeEntryByPoints || assignmentDetailsBean.gradeEntryByPercent}"
							 style="text-align:right;" onkeypress="return submitOnEnter(event, 'gbForm:saveButton1');">
							<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
							<f:validateDoubleRange minimum="0"/>
							<f:validator validatorId="org.sakaiproject.gradebook.jsf.validator.ASSIGNMENT_GRADE"/>
						</h:inputText>
            <h:outputText value="#{assignmentDetailsBean.localizedPercentInput}" rendered="#{assignmentDetailsBean.gradeEntryByPercent}"
              style="margin-left: 5px;" />
						<h:inputText id="LetterScore" value="#{scoreRow.letterScore}" size="6" 
							 rendered="#{assignmentDetailsBean.gradeEntryByLetter}"
							 style="text-align:right;" onkeypress="return submitOnEnter(event, 'gbForm:saveButton1');">
							<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.LETTER_GRADE_CONVERTER" />
						</h:inputText>
						
					</h:panelGroup>
					<h:panelGroup rendered="#{assignmentDetailsBean.assignment.externallyMaintained || !scoreRow.userCanGrade}">
						<h:outputText value="#{scoreRow.score}" rendered="#{assignmentDetailsBean.gradeEntryByPoints || assignmentDetailsBean.gradeEntryByPercent}">
							<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
						</h:outputText>
            <h:outputText value="#{assignmentDetailsBean.localizedPercentInput}" rendered="#{assignmentDetailsBean.gradeEntryByPercent && scoreRow.score != null}" />
						<h:outputText value="#{scoreRow.letterScore}" 
							 rendered="#{assignmentDetailsBean.gradeEntryByLetter && scoreRow.letterScore != null}">
							<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.LETTER_GRADE_CONVERTER" />
						</h:outputText>
						<h:outputText value="#{msgs.score_null_placeholder}" 
							 rendered="#{assignmentDetailsBean.gradeEntryByLetter && scoreRow.letterScore == null}" />
					</h:panelGroup>
					<h:panelGroup rendered="#{scoreRow.droppedFromGrade && !assignmentDetailsBean.assignment.externallyMaintained && scoreRow.userCanGrade}">
						<h:inputText id="Score2" value="#{scoreRow.score}" size="6" 
							 rendered="#{assignmentDetailsBean.gradeEntryByPoints || assignmentDetailsBean.gradeEntryByPercent}"
							 style="text-align:right;text-decoration:line-through" onkeypress="return submitOnEnter(event, 'gbForm:saveButton1');">
							<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
							<f:validateDoubleRange minimum="0"/>
							<f:validator validatorId="org.sakaiproject.gradebook.jsf.validator.ASSIGNMENT_GRADE"/>
						</h:inputText>
            			<h:outputText value="#{assignmentDetailsBean.localizedPercentInput}" rendered="#{assignmentDetailsBean.gradeEntryByPercent}"
              				style="margin-left: 5px;" />
						<h:inputText id="LetterScore2" value="#{scoreRow.letterScore}" size="6" 
							 rendered="#{assignmentDetailsBean.gradeEntryByLetter}"
							 style="text-align:right;" onkeypress="return submitOnEnter(event, 'gbForm:saveButton1');">
							<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.LETTER_GRADE_CONVERTER" />
						</h:inputText>
					</h:panelGroup>
				</t:div>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:panelGroup>
						<h:outputText value="#{msgs.assignment_details_comments_read} " styleClass="tier0" />
						<h:commandButton
							value="#{assignmentDetailsBean.commentsToggle}"
							actionListener="#{assignmentDetailsBean.toggleEditableComments}"
							disabled="#{assignmentDetailsBean.assignment.externallyMaintained || assignmentDetailsBean.allStudentsViewOnly}"
							rendered="#{!assignmentDetailsBean.allCommentsEditable}"/>
					</h:panelGroup>
				</f:facet>
				<h:message for="Score" styleClass="validationEmbedded gbMessageAdjustForContent"/>
				<h:message for="LetterScore" styleClass="validationEmbedded gbMessageAdjustForContent"/>
				<t:div styleClass="gbTextOnRow" rendered="#{!scoreRow.commentEditable}">
					<h:outputText value="#{scoreRow.commentText}"/>
				</t:div>
				<t:div rendered="#{scoreRow.commentEditable}">
					<h:inputTextarea id="Comment" value="#{scoreRow.commentText}"
						rows="4" cols="35">
					</h:inputTextarea>
				</t:div>
			</h:column>
		</t:dataTable>

		<t:aliasBean alias="#{bean}" value="#{assignmentDetailsBean}">
			<%@ include file="/inc/gradingEventLogs.jspf"%>
		</t:aliasBean>

		<p class="instruction">
			<h:outputText value="#{msgs.assignment_details_no_enrollments}" rendered="#{assignmentDetailsBean.emptyEnrollments}" />
		</p>
		
		<div id="buttonDiv2" class="act gbButtonBar">
			<h:commandButton
				id="saveButton2"
				styleClass="active"
				value="#{msgs.assignment_details_submit}"
				actionListener="#{assignmentDetailsBean.processUpdateScores}"
				disabled="#{assignmentDetailsBean.assignment.externallyMaintained || assignmentDetailsBean.allStudentsViewOnly}"
				rendered="#{!assignmentDetailsBean.emptyEnrollments}"
				title="#{msgs.assignment_details_submit}"
				onclick="SPNR.disableControlsAndSpin( this, null );"/>
			<h:commandButton
				id="cancelButton2"
				value="#{msgs.assignment_details_cancel}"
				action="assignmentDetails"
				immediate="true"
				disabled="#{assignmentDetailsBean.assignment.externallyMaintained || assignmentDetailsBean.allStudentsViewOnly}"
				rendered="#{!assignmentDetailsBean.emptyEnrollments}"
				title="#{msgs.assignment_details_cancel}"
				onclick="SPNR.disableControlsAndSpin( this, null );">
					<f:param name="breadcrumbPage" value="#{assignmentDetailsBean.breadcrumbPage}"/>
			</h:commandButton>
		</div>

		</div> <!-- END OF INDNT1 -->


<script>includeLatestJQuery('assignmentDetails.jsp');</script>
<script type="text/javascript">
$(document).ready(function(){
	org_vals = new Array($("table#gbForm\\:gradingTable :text").length);
	$("table#gbForm\\:gradingTable :text").each(function(i){
		org_vals[i] = this.value;
	});
	$(".shorttext .listNav input,select").click(check_change);
});
check_change = function(){
	changed = false;
	$("table#gbForm\\:gradingTable :text").each(function(i){
		if(org_vals[i] !== this.value){changed=true;}
	});
	if(changed){return confirm("<h:outputText value="#{msgs.assignment_details_page_confirm_unsaved}"/>");}
	return true;
};
</script>
		
	</h:form>
  </div>
</f:view>
