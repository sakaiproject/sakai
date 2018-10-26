<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<script src="/library/js/spinner.js" type="text/javascript"></script>
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
			<h:outputText value="#{msgs.course_grade_details_null_msg}" rendered="#{courseGradeDetailsBean.userAbleToGradeAll}"/>
			<h:commandLink value="#{msgs.calculate_course_grade_name}" rendered="#{courseGradeDetailsBean.userAbleToGradeAll}" action="calculateCourseGrades" /><h:outputText value="." rendered="#{courseGradeDetailsBean.userAbleToGradeAll}" />
			<h:outputText value="#{msgs.course_grade_details_null_msg_ta_view}" rendered="#{!courseGradeDetailsBean.userAbleToGradeAll}"/>
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
			styleClass="listHier lines nolines">
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
				<t:div styleClass="">
					<h:outputLabel for="Grade" value="#{msgs.course_grade_details_export_course_grades_options_gradeoverr_for} #{scoreRow.enrollment.user.sortName}" styleClass="skip"/>
					<h:inputText rendered="#{scoreRow.userCanGrade}"
						id="Grade"
						value="#{scoreRow.enteredGrade}"
						size="4"
						title="#{msgs.course_grade_details_export_course_grades_options_gradeoverr_for} #{scoreRow.enrollment.user.sortName}"
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
            <h:outputText rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}" escape="false" value="<span class=\"highlightPanel actionitem\" style=\"padding:10px 5px 10px 10px\">"/>
                <h:outputText styleClass="instruction" style="padding-right:10px;" value="#{courseGradeDetailsBean.exportCustomLabel}" rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}"/>
                <h:commandButton
    				value="#{msgs.course_grade_details_export_course_grades_institution_control}"
    				actionListener="#{courseGradeDetailsBean.exportCustomCsv}"
    				rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}"
    				/>
            <h:outputText rendered="#{!courseGradeDetailsBean.emptyEnrollments && courseGradeDetailsBean.enableCustomExport}" escape="false" value="</span>" />
            <h:outputText rendered="#{courseGradeDetailsBean.emptyEnrollments}" escape="false" value="<div style=\"display: none\">"/>
                &nbsp;<span class="highlightPanel actionitem" style="padding:10px 5px 10px 10px;margin-right:10xp;position:relative">
                    <h:outputLabel for="export_format" value="#{msgs.course_grade_details_export_label}" />
                    <h:selectOneMenu id="export_format" value="#{courseGradeDetailsBean.exportType}">
                        <f:selectItems value="#{courseGradeDetailsBean.exportFormats}" />
                    </h:selectOneMenu>
                    <h:commandButton
                        value="#{msgs.course_grade_details_export_course_grades_institution_control}"
                        actionListener="#{courseGradeDetailsBean.export}"
                        rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
                        />
                    <a href="#" id="exportPrefsLink"><h:outputText value="#{msgs.course_grade_details_export_course_grades_options}" /></a>
                    <div class="highlightPanel exportPrefsVals" style="display: none">
                    	<p>
                            <h:selectBooleanCheckbox id="sortname" value="#{courseGradeDetailsBean.includeSortname}" />
                            <h:outputLabel for="sortname" value="#{msgs.course_grade_details_export_course_grades_options_username}" />
                        </p>
                        <p>
                            <h:selectBooleanCheckbox id="userid" value="#{courseGradeDetailsBean.includeUsereid}"/>
                            <h:outputLabel for="userid" value="#{msgs.course_grade_details_export_course_grades_options_userid}" />
                        </p>
                        <p>
                            <h:selectBooleanCheckbox id="finalscore" value="#{courseGradeDetailsBean.includeFinalscore}" />
                            <h:outputLabel for="finalscore" value="#{msgs.course_grade_details_export_course_grades_options_finalscore}" />
                        </p>
                        <p>
                            <h:selectBooleanCheckbox id="calculatedgrade" value="#{courseGradeDetailsBean.includeCalculatedgrade}" />
                            <h:outputLabel for="calculatedgrade" value="#{msgs.course_grade_details_export_course_grades_options_calcgrade}" />
                        </p>
                        <p>
                            <h:selectBooleanCheckbox id="lastmodifieddate" value="#{courseGradeDetailsBean.includeLastmodifieddate}" />
                            <h:outputLabel for="lastmodifieddate" value="#{msgs.course_grade_details_export_course_grades_options_lastmod}" />
                        </p>
                        <p>
                            <h:selectBooleanCheckbox id="gradeoverride" value="#{courseGradeDetailsBean.includeGradeoverride}" />
                            <h:outputLabel for="gradeoverride" value="#{msgs.course_grade_details_export_course_grades_options_gradeoverr}" />
                        </p>
                        <p>
                            <h:selectBooleanCheckbox id="coursegrade" value="#{courseGradeDetailsBean.includeCoursegrade}" />
                            <h:outputLabel for="coursegrade" value="#{msgs.course_grade_details_export_course_grades_options_coursegrade}" />
                        </p>
                       
                        <a href="#" id="exportPrefsValsClose">X</a>
                    </div>
                </span>
            <h:outputText rendered="#{courseGradeDetailsBean.emptyEnrollments}" escape="false" value="</div>"/>
			<br /><br /><hr class="itemSeparator"/><br />
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.assignment_details_submit}"
				actionListener="#{courseGradeDetailsBean.processUpdateGrades}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				disabled="#{courseGradeDetailsBean.allStudentsViewOnly}"
				onclick="SPNR.disableControlsAndSpin( this, null );"
			/>
			<h:commandButton
				value="#{msgs.assignment_details_cancel}"
				action="overview"
				immediate="true"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				disabled="#{courseGradeDetailsBean.allStudentsViewOnly}"
				onclick="SPNR.disableControlsAndSpin( this, null );"
			/>

			<h:commandButton
				value="#{msgs.course_grade_details_calculate_course_grade}"
				action="calculateCourseGrades"
				rendered="#{courseGradeDetailsBean.userAbleToGradeAll}"
				style="margin-left: 5em;"
				onclick="SPNR.disableControlsAndSpin( this, null );"
			/>
		</div>
    <script>includeLatestJQuery('courseGradeDetails.jsp');</script>
    <script type="text/javascript">
        $(document).ready(function(){
            $('#exportPrefsLink').click(function(e){
                e.preventDefault();
                $(this).next('div.highlightPanel').css({
                 'top': -120,
                 'right':-100
                }).toggle();
            });
            $('#exportPrefsValsClose').click(function(e){
                e.preventDefault();
                $(this).parent('div.highlightPanel').hide();
            });
            
            org_vals = new Array($("table#gbForm\\:gradingTable :text").length);
            $("table#gbForm\\:gradingTable :text").each(function(i)
            {
                org_vals[i] = this.value;
            });
            $(".shorttext .listNav input,select").click(check_change);
        });
        
        check_change = function()
        {
            changed = false;
            $("table#gbForm\\:gradingTable :text").each(function(i)
            {
                if(org_vals[i] !== this.value){changed=true;}
            });
            if(changed)
            {
                return confirm("<h:outputText value="#{msgs.assignment_details_page_confirm_unsaved}"/>");
            }
            return true;
        };
    </script>

	  </h:form>
	</div>
</f:view>
