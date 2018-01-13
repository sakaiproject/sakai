<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<link href="dhtmlpopup/dhtmlCommentPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<script src="js/frameAdjust.js" type="text/javascript"></script>
<script src="js/scoringAgent/integration.js" type="text/javascript"></script>

<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">
		<sakai:flowState bean="#{studentViewBean}" />
		
		<h:panelGrid columns="2" width="99%" columnClasses="bogus,right">
			<h:panelGroup>
				<f:verbatim><h2></f:verbatim>
					<h:outputFormat value="#{msgs.student_view_page_title}">
						<f:param value="#{studentViewBean.userDisplayName}"/>
					</h:outputFormat>
				<f:verbatim></h2></f:verbatim>
			</h:panelGroup>
			<h:panelGroup>
				<h:commandLink action="instructorView" 
						rendered="#{studentViewBean.userAbleToGradeAll}">
					<h:outputFormat value="#{msgs.student_view_return_to_inst_view}">
						<f:param value="#{studentViewBean.userDisplayName}" />
					</h:outputFormat>
					<f:param name="studentUid" value="#{studentViewBean.studentUidToView}" />
					<f:param name="returnToPage" value="#{studentViewBean.instViewReturnToPage}" />
					<f:param name="assignmentId" value="#{studentViewBean.instViewAssignmentId}" />
				</h:commandLink>
			</h:panelGroup>
		</h:panelGrid>

		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">	
			<h:outputText value="#{msgs.course_grade_name}" />
			<h:panelGroup>
				<h:outputText id="letterGrade" value="#{studentViewBean.courseGradeLetter} " rendered="#{studentViewBean.courseGradeReleased}"/>
				<h:outputText id="cumScore" value="#{studentViewBean.courseGradeRecord}" rendered="#{studentViewBean.courseGradeReleased}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
				<h:outputText value="#{msgs.student_view_not_released}" rendered="#{!studentViewBean.courseGradeReleased}"/>
			</h:panelGroup>
			<h:panelGroup rendered="#{studentViewBean.showCoursePoints}">
					<h:outputText value="#{msgs.course_points_name}" />
					<h:outputText value="#{msgs.student_view_footnote_symbol2}" />
			</h:panelGroup>
			<h:panelGroup rendered="#{studentViewBean.showCoursePoints}">
					<h:outputText id="cumPoints"
						value="#{studentViewBean.pointsEarned}"
						rendered="#{studentViewBean.coursePointsReleased}">
						<f:converter
							converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
					</h:outputText>
					<h:outputText
						rendered="#{studentViewBean.coursePointsReleased && studentViewBean.totalPoints != -1}">
						<f:verbatim>/</f:verbatim>
					</h:outputText>
					<h:outputText id="totalPoints"
						value="#{studentViewBean.totalPoints}"
						rendered="#{studentViewBean.coursePointsReleased && studentViewBean.totalPoints != -1}">
						<f:converter
							converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
					</h:outputText>
					<h:outputText value="#{msgs.student_view_not_released}"
						rendered="#{!studentViewBean.coursePointsReleased}" />
				</h:panelGroup>

			</h:panelGrid>

      <h:panelGroup rendered="#{studentViewBean.assignmentsReleased}">
				<f:verbatim><h4></f:verbatim>
					<h:outputText value="#{msgs.student_view_assignments}"/>
				<f:verbatim></h4></f:verbatim>
			</h:panelGroup>
			
			<gbx:gradebookItemTable cellpadding="0" cellspacing="0"
				value="#{studentViewBean.gradebookItems}"
				var="row"
        sortColumn="#{studentViewBean.sortColumn}"
				sortAscending="#{studentViewBean.sortAscending}"
				columnClasses="attach,left,center,center,center,center,external"
				headerClasses="attach,left,center,center,center,center comments,bogus"
				rowClasses="#{studentViewBean.rowStyles}"
				styleClass="listHier wideTable lines"
				rendered="#{studentViewBean.assignmentsReleased}"
				expanded="true">
				
				<h:column id="_toggle" rendered="#{studentViewBean.categoriesEnabled}">
					<f:facet name="header">
						<h:outputText value="" />
					</f:facet>
				</h:column>
				
				<h:column id="_title">
					<f:facet name="header">
						<t:commandSortHeader columnName="name" propertyName="name" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_title}"/>
						</t:commandSortHeader>
					</f:facet>
					<h:outputText value="#{row.associatedAssignment.name}" rendered="#{row.assignment}"/>
					<h:outputText value="#{row.name}" styleClass="categoryHeading" rendered="#{row.isCategory}"/>
					<h:outputText value=" (#{msgs.extra_credit})" rendered="#{row.isCategory && row.isExtraCredit}"/>
					<h:outputText value=" (#{msgs.extra_credit})" rendered="#{!row.isCategory && row.associatedAssignment.isExtraCredit && (row.associatedAssignment.category == null || !row.associatedAssignment.category.isExtraCredit) }"/>
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
				
				<%@include file="/inc/scoringAgent/studentView.jspf"%>
				
				<h:column>
					<f:facet name="header">
						<t:commandSortHeader columnName="pointsEarned" propertyName="pointsEarned" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_grade}"/>
							<h:outputText value="#{msgs.student_view_footnote_symbol1}" />
						</t:commandSortHeader>
					</f:facet>
					
					<h:outputText value="#{row}" escape="false" rendered="#{row.isCategory}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER"/>
					</h:outputText>

          			<h:outputText value="#{row}" escape="false" rendered="#{row.assignment && !row.gradeRecord.droppedFromGrade}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.SCORE_CONVERTER"/>
					</h:outputText>
					<h:outputText value="#{row}" escape="false" style="text-decoration:line-through" rendered="#{row.assignment && row.gradeRecord.droppedFromGrade}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.SCORE_CONVERTER"/>
					</h:outputText>
        </h:column>
        
        <h:column rendered="#{studentViewBean.weightingEnabled}">
					<f:facet name="header">
			    	<t:commandSortHeader columnName="weight" propertyName="weight" immediate="true" arrow="true">
							<h:outputText value="#{msgs.student_view_weight}"/>
			      </t:commandSortHeader>
			    </f:facet>
	
					<h:outputText value="#{row.weight}" rendered="#{row.isCategory}">
						<f:convertNumber type="percent" maxFractionDigits="2" />
					</h:outputText>
				</h:column>
	    
		    <h:column>
	        <f:facet name="header">
	        	<h:outputText value="#{msgs.student_view_comment_header}"/>
	        </f:facet>
	        <h:outputText value="#{row.commentText}" rendered="#{row.assignment && row.commentText != null}" />
		    </h:column>
		    
		    <h:column rendered="#{studentViewBean.anyExternallyMaintained}">
		       <h:outputText value="#{msgs.overview_from} #{row.associatedAssignment.externalAppName}" rendered="#{row.assignment && row.associatedAssignment.externallyMaintained}" />
		    </h:column>
		  </gbx:gradebookItemTable>
		  
		  <h:panelGrid styleClass="instruction gbSection" cellpadding="0" cellspacing="0" columns="1">
				<h:outputText value="#{msgs.student_view_legend_title}" />
				<h:panelGroup>
					<h:outputText value="#{msgs.student_view_footnote_symbol1}" /> <h:outputText value="#{msgs.student_view_footnote_legend1}" />
				</h:panelGroup>
				<h:panelGroup rendered="#{studentViewBean.showCoursePoints}">
					<h:outputText value="#{msgs.student_view_footnote_symbol2}" /> <h:outputText value="#{msgs.student_view_footnote_legend2}" />
				</h:panelGroup>
			</h:panelGrid>

</h:form>
</div>
</f:view>
