<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<script src="js/dynamicSizeCheck.js" type="text/javascript"></script>
<script src="/library/js/jquery.js" type="text/javascript"></script>
<script src="js/scoringAgent/integration.js" type="text/javascript"></script>
<script src="/library/js/spinner.js" type="text/javascript"></script>

<f:view>
	<div class="portletBody">
	  <h:form id="gbForm" onsubmit="return blockDoubleSubmit();">

		<sakai:flowState bean="#{instructorViewBean}" />
		
		<t:aliasBean alias="#{bean}" value="#{instructorViewBean}">
			<%@ include file="/inc/appMenu.jspf" %>
		</t:aliasBean>

		<t:aliasBean alias="#{bean}" value="#{instructorViewBean}">
			<%@ include file="/inc/breadcrumbInstructor.jspf" %>
		</t:aliasBean>

		<h:panelGrid columns="2" width="99%" columnClasses="bogus,right">
			<h:panelGroup>
				<f:verbatim><h3></f:verbatim>
					<h:outputText value="#{msgs.inst_view_student_summary}" />
				<f:verbatim></h3></f:verbatim>
			</h:panelGroup>
			<h:commandLink action="studentView" style="text-align: right;"
					rendered="#{instructorViewBean.userAbleToGradeAll}">
				<h:outputFormat value="#{msgs.inst_view_students_grades}">
					<f:param value="#{instructorViewBean.userDisplayName}" />
				</h:outputFormat>
				<f:param name="studentUidToView" value="#{instructorViewBean.studentUid}" />
				<f:param name="instViewReturnToPage" value="#{instructorViewBean.returnToPage}" />
				<f:param name="instViewAssignmentId" value="#{instructorViewBean.assignmentId}" />
			</h:commandLink>
			<h:outputText value="" rendered="#{!instructorViewBean.userAbleToGradeAll}" />
		</h:panelGrid>
		
		
		<div class="nav indnt3 gbSection">
			<h:commandButton
				disabled="#{instructorViewBean.first}"
				actionListener="#{instructorViewBean.processStudentUidChange}"
				value="#{msgs.inst_view_prev}"
				title="#{instructorViewBean.previousStudent.user.displayName}"
				accesskey="p">
					<f:param name="studentUid" value="#{instructorViewBean.previousStudent.user.userUid}"/>
				</h:commandButton>
			
			<h:commandButton
				action="#{instructorViewBean.processCancel}"
				value="#{instructorViewBean.returnToPageButtonName}"
				accesskey="l"/>
			
			<h:commandButton
				disabled="#{instructorViewBean.last}"
				actionListener="#{instructorViewBean.processStudentUidChange}"
				value="#{msgs.inst_view_next}"
				title="#{instructorViewBean.nextStudent.user.displayName}"
				accesskey="n">
					<f:param name="studentUid" value="#{instructorViewBean.nextStudent.user.userUid}"/>
			</h:commandButton>
		</div>
		
		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary gbSection">	
			<h:outputText value="#{msgs.inst_view_name}" />
			<h:outputText value="#{instructorViewBean.userDisplayName}" />
			
			<h:outputText value="#{msgs.inst_view_email}" />
			<h:outputText value="#{instructorViewBean.studentEmailAddress}" />
			
			<h:outputText value="#{msgs.inst_view_id}" />
			<h:outputText value="#{instructorViewBean.currentStudent.user.displayId}" />
			
			<h:outputText value="#{msgs.inst_view_sections}"/>
			<h:outputText value="#{instructorViewBean.studentSections}"/>
		</h:panelGrid>
		
		<hr/>
		
		<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
			columnClasses="itemName"
			styleClass="itemSummary gbSection"
			rendered="#{rosterBean.userAbleToGradeAll}">
			<h:outputText value="#{msgs.course_grade_name}" />
			<h:panelGroup>
				<h:outputText id="letterGrade" value="#{instructorViewBean.courseGradeLetter} " />
				<h:outputText id="cumScore" value="#{instructorViewBean.courseGradeRecord}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:panelGroup>	
		</h:panelGrid>
		
		<%@ include file="/inc/globalMessages.jspf"%>
		
		<div>
			<div class="act" style="display: inline;float: left;">
			<h4 >
				<h:outputText value="#{msgs.inst_view_grading_table}" />
			</h4>
			</div>
			<div id="buttonDiv2" class="act gbButtonBar">
					<h:commandButton
						id="saveButton2"
						styleClass="active"
						value="#{msgs.inst_view_save}"
						action="#{instructorViewBean.processUpdateScores}"
						accesskey="s"
						title="#{msgs.inst_view_save}"
						disabled="#{instructorViewBean.allItemsViewOnly}"
						onclick="SPNR.disableControlsAndSpin( this, null );"/>
					<h:commandButton
						id="clearButton2"
						value="#{msgs.inst_view_clear}"
						action=""
						immediate="true"
						accesskey="c"
						title="#{msgs.inst_view_clear}"
						disabled="#{instructorViewBean.allItemsViewOnly}"
						onclick="SPNR.disableControlsAndSpin( this, null );"/>
				</div>
			</div>

		
		<gbx:gradebookItemTable cellpadding="0" cellspacing="0"
				value="#{instructorViewBean.gradebookItems}"
				var="row"
        sortColumn="#{instructorViewBean.sortColumn}"
				sortAscending="#{instructorViewBean.sortAscending}"
				columnClasses="attach,left,center,center,center,center,center,center,external"
				rowClasses="#{instructorViewBean.rowStyles}"
				headerClasses="attach,left,center,center,center,center,center,center comments,bogus"
				styleClass="listHier lines nolines gradingTable"
				expanded="true"
				rowIndexVar="rowIndex">
				
				<h:column id="_toggle" rendered="#{instructorViewBean.categoriesEnabled}">
					<f:facet name="header">
						<h:outputText value="" />
					</f:facet>
				</h:column>
				
				<h:column id="_title">
					<f:facet name="header">
						<t:commandSortHeader columnName="name" propertyName="name" immediate="true" arrow="true">
							<h:outputText value="#{msgs.inst_view_title}"/>
							<h:outputText value="#{msgs.inst_view_footnote_symbol1}" />
						</t:commandSortHeader>
					</f:facet>
					
					<h:panelGroup rendered="#{row.assignment}">
						<h:commandLink action="#{instructorViewBean.navigateToAssignmentDetails}" rendered="#{row.associatedAssignment.released}">
							<h:outputText value="#{row.associatedAssignment.name}" />
							<f:param name="assignmentId" value="#{row.associatedAssignment.id}"/>
						</h:commandLink>
						<h:panelGroup rendered="#{!row.associatedAssignment.released}" styleClass="inactive" >
							<h:commandLink action="#{instructorViewBean.navigateToAssignmentDetails}" >
								<h:outputText value="#{row.associatedAssignment.name}"/>
								<f:param name="assignmentId" value="#{row.associatedAssignment.id}"/>
							</h:commandLink>
						</h:panelGroup>
						<h:outputText value=" (#{msgs.extra_credit})" rendered="#{row.associatedAssignment.isExtraCredit && (row.associatedAssignment.category == null || !row.associatedAssignment.category.isExtraCredit) }"/>
					</h:panelGroup>
					
					<h:outputText value="#{row.name}" styleClass="categoryHeading" rendered="#{row.isCategory}"/>
					<h:outputText value=" (#{msgs.extra_credit})" rendered="#{row.isCategory && row.isExtraCredit}"/>
					<h:outputText value=" (" styleClass="categoryHeading" rendered="#{row.isCategory && (row.dropHighest != 0 || row.dropLowest != 0 || row.keepHighest != 0)}" />
		            <h:outputFormat value="#{msgs.cat_drop_highest_display}" styleClass="categoryHeading" rendered="#{row.isCategory && row.dropHighest != 0}" >
		                <f:param value="#{row.dropHighest}"/>
		            </h:outputFormat>
		            <h:outputText value="; " styleClass="categoryHeading" rendered="#{row.isCategory && (row.dropHighest != 0 && row.dropLowest != 0)}" />
		            <h:outputFormat value="#{msgs.cat_drop_lowest_display}" styleClass="categoryHeading" rendered="#{row.isCategory && row.dropLowest != 0}" >
		                <f:param value="#{row.dropLowest}"/>
		            </h:outputFormat>
		            
		            <h:outputFormat value="#{msgs.cat_keep_highest_display}" styleClass="categoryHeading" rendered="#{row.isCategory && row.keepHighest != 0}" >
		                <f:param value="#{row.keepHighest}"/>
		            </h:outputFormat>
		            <h:outputText value=")" styleClass="categoryHeading" rendered="#{row.isCategory && (row.dropHighest != 0 || row.dropLowest != 0 || row.keepHighest != 0)}" />
					
				</h:column>
				
				<h:column>
					<f:facet name="header">
						<t:commandSortHeader columnName="dueDate" propertyName="dueDate" immediate="true" arrow="true">
							<h:outputText value="#{msgs.due_date}"/>
						</t:commandSortHeader>
					</f:facet>

					<h:outputText value="#{row.associatedAssignment.dueDate}" rendered="#{row.assignment && row.associatedAssignment.dueDate != null}">
						<gbx:convertDateTime />
					</h:outputText>
					<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{row.assignment && row.associatedAssignment.dueDate == null}"/>
				</h:column>
				    
        <h:column rendered="#{instructorViewBean.weightingEnabled}">
					<f:facet name="header">
			    	<t:commandSortHeader columnName="weight" propertyName="weight" immediate="true" arrow="true">
							<h:outputText value="#{msgs.inst_view_weight}"/>
			      </t:commandSortHeader>
			    </f:facet>
	
					<h:outputText value="#{row.weight}" rendered="#{row.isCategory}">
						<f:convertNumber type="percent" maxFractionDigits="2" />
					</h:outputText>
				</h:column>
				
				<h:column>
					<f:facet name="header">
						<h:outputText value="#{msgs.inst_view_log}" styleClass="tier0"/>
					</f:facet>
					<h:outputLink value="#"
					rendered="#{row.assignment && not empty row.eventRows}"
					onclick="javascript:dhtmlPopupToggle('#{rowIndex}', event);return false;">
					<h:graphicImage value="images/log.png" alt="#{msgs.inst_view_log_alt}"/>
				</h:outputLink>
				</h:column>
				
				 <%@include file="/inc/scoringAgent/instructorView.jspf"%>

				<h:column>
					<f:facet name="header">
						<t:commandSortHeader columnName="pointsEarned" propertyName="pointsEarned" immediate="true" arrow="true">
							<h:outputText value="#{msgs.inst_view_grade}" rendered="#{instructorViewBean.gradeEntryByPoints || instructorViewBean.gradeEntryByPercent}" />
							<h:outputText value="#{msgs.inst_view_grade_letter}" rendered="#{instructorViewBean.gradeEntryByLetter}" />
							<h:outputText value="#{msgs.inst_view_footnote_symbol2}" />
						</t:commandSortHeader>
					</f:facet>

						<h:panelGroup rendered="#{row.assignment}" style="white-space:nowrap;">
							<h:outputText value="#{msgs.inst_view_not_counted_open}" rendered="#{!row.associatedAssignment.counted}" />
							
							<h:panelGroup rendered="#{!row.associatedAssignment.externallyMaintained && row.userCanGrade}">
								<h:inputText id="Score" value="#{row.score}" size="4" 
									 rendered="#{(instructorViewBean.gradeEntryByPoints || instructorViewBean.gradeEntryByPercent)  && !row.gradeRecord.droppedFromGrade}"
									 style="text-align:right;" onkeypress="return submitOnEnter(event, 'gbForm:saveButton1');">
									<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
									<f:validateDoubleRange minimum="0"/>
									<f:validator validatorId="org.sakaiproject.gradebook.jsf.validator.ASSIGNMENT_GRADE"/>
								</h:inputText>
								<h:inputText id="Score2" value="#{row.score}" size="4" 
									 rendered="#{(instructorViewBean.gradeEntryByPoints || instructorViewBean.gradeEntryByPercent)  && row.gradeRecord.droppedFromGrade}"
									 style="text-align:right;text-decoration:line-through" onkeypress="return submitOnEnter(event, 'gbForm:saveButton1');">
									<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
									<f:validateDoubleRange minimum="0"/>
									<f:validator validatorId="org.sakaiproject.gradebook.jsf.validator.ASSIGNMENT_GRADE"/>
								</h:inputText>
                                
                <h:outputText value="#{instructorViewBean.localizedPercentInput}" rendered="#{instructorViewBean.gradeEntryByPercent}"
                  style="margin-left: 5px;" />
								
								<h:inputText id="LetterScore" value="#{row.letterScore}" size="4" 
									 rendered="#{instructorViewBean.gradeEntryByLetter}"
									 style="text-align:right;" onkeypress="return submitOnEnter(event, 'gbForm:saveButton1');">
									<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.LETTER_GRADE_CONVERTER" />
								</h:inputText>
							</h:panelGroup>
							
              <h:panelGroup rendered="#{row.associatedAssignment.externallyMaintained || !row.userCanGrade}">
    							<h:outputText value="#{row.score}" rendered="#{!row.gradeRecord.droppedFromGrade}">
    								<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
    							</h:outputText>
    							<h:outputText value="#{row.score}" style="text-decoration:line-through" rendered="#{row.gradeRecord.droppedFromGrade}">
    								<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
    							</h:outputText>
                  <h:outputText value="#{instructorViewBean.localizedPercentInput}" rendered="#{instructorViewBean.gradeEntryByPercent && row.score != null}" />
              </h:panelGroup>
							
							<h:outputText value="#{msgs.inst_view_not_counted_close}" rendered="#{!row.associatedAssignment.counted}" />
						</h:panelGroup>
						
						<h:outputText value="#{row}" escape="false" rendered="#{row.isCategory}">
							<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER"/>
						</h:outputText>
        </h:column>
        
        <h:column>
        	<f:facet name="header" >
        		<t:commandSortHeader columnName="itemValue" propertyName="itemValue" immediate="true" arrow="true">
							<h:outputText value="#{msgs.inst_view_item_value}"/>
						</t:commandSortHeader>
        	</f:facet>
        	
        	<h:outputText value="#{row.associatedAssignment.pointsPossible}" rendered="#{row.assignment}">
        		<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS"/>
        	</h:outputText>
        </h:column>
	    
		    <h:column>
	        <f:facet name="header">
	        	<h:outputText value="#{msgs.inst_view_comments}"/>
	        </f:facet>
	        <h:message for="Score" styleClass="validationEmbedded gbMessageAdjustForContent"/>
	        <h:message for="LetterScore" styleClass="validationEmbedded gbMessageAdjustForContent"/>
	        <h:outputText value="#{row.commentText}" rendered="#{row.assignment && row.commentText != null}" />
		    </h:column>
		    
		    <h:column rendered="#{instructorViewBean.anyExternallyMaintained}">
					<f:facet name="header">
	        	<t:commandSortHeader columnName="gradeEditor" propertyName="gradeEditor" immediate="true" arrow="true">
	          	<h:outputText value="#{msgs.inst_view_grade_editor}" />
	          </t:commandSortHeader>
	        </f:facet>
					<h:outputText value="#{msgs.overview_from} #{row.associatedAssignment.externalAppName}" rendered="#{row.assignment && row.associatedAssignment.externallyMaintained}"/>
				</h:column>
		    
		  </gbx:gradebookItemTable>
		  
		  <t:aliasBean alias="#{bean}" value="#{instructorViewBean}">
			 <%@ include file="/inc/gradingEventLogsInstView.jspf"%>
		  </t:aliasBean>
		
		
			<div id="buttonDiv1" class="act gbButtonBar">
				<h:commandButton
					id="saveButton1"
					styleClass="active"
					value="#{msgs.inst_view_save}"
					action="#{instructorViewBean.processUpdateScores}"
					accesskey="s"
					title="#{msgs.inst_view_save}"
					disabled="#{instructorViewBean.allItemsViewOnly}"
					onclick="SPNR.disableControlsAndSpin( this, null );"/>
				<h:commandButton
					id="clearButton1"
					value="#{msgs.inst_view_clear}"
					action=""
					immediate="true"
					accesskey="c"
					title="#{msgs.inst_view_clear}"
					disabled="#{instructorViewBean.allItemsViewOnly}"
					onclick="SPNR.disableControlsAndSpin( this, null );"/>
			</div>
				
			<h:panelGrid styleClass="instruction gbSection" cellpadding="0" cellspacing="0" columns="1">
				<h:outputText value="#{msgs.inst_view_legend_title}" />
				<h:panelGroup>
					<h:outputText value="#{msgs.inst_view_footnote_symbol1}" />
					<h:outputText value="#{msgs.inst_view_footnote_legend1}" />
				</h:panelGroup>
				<h:panelGroup>
					<h:outputText value="#{msgs.inst_view_footnote_symbol2}" />
					<h:outputText value="#{msgs.inst_view_footnote_legend2}" />
				</h:panelGroup>
			</h:panelGrid>

	  </h:form>
	</div>
</f:view>
