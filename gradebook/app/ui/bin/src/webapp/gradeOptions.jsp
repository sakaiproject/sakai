<script src="/library/js/spinner.js" type="text/javascript"></script>
<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
		<t:aliasBean alias="#{bean}" value="#{feedbackOptionsBean}">
			<%@ include file="/inc/appMenu.jspf"%>
		</t:aliasBean>

		<sakai:flowState bean="#{feedbackOptionsBean}" />

		<h2><h:outputText value="#{msgs.feedback_options_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.feedback_options_instruction}" escape="false"/></div>

		<div class="indnt1">

<!-- Grade Display -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_display}"/></h4>
		<h:panelGrid columns="2" columnClasses="prefixedCheckbox">
		</h:panelGrid>
		<h:panelGrid columns="2" columnClasses="prefixedCheckbox">
			<h:selectBooleanCheckbox id="displayCourseGrades" value="#{feedbackOptionsBean.localGradebook.courseGradeDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayCourseGrades" value="#{msgs.feedback_options_grade_display_course_grades}" />
		</h:panelGrid>
		<h:panelGrid columns="2" columnClasses="prefixedCheckbox" rendered="#{feedbackOptionsBean.showCoursePoints}">
			<h:selectBooleanCheckbox id="displayCoursePoints" value="#{feedbackOptionsBean.localGradebook.coursePointsDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayCoursePoints" value="#{msgs.feedback_options_grade_display_course_points}" />
		</h:panelGrid>

<!-- Grade Conversion -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_conversion}"/></h4>
		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="1"
			columnClasses="itemName"
			styleClass="itemSummary">

			

			<h:panelGroup>
			    <h:outputLabel value="#{msgs.feedback_options_grade_type}" for="selectGradeType" /><h:outputText value="&nbsp;&nbsp;" escape="false" />
				<h:selectOneMenu id="selectGradeType" value="#{feedbackOptionsBean.selectedGradeMappingId}">
					<f:selectItems value="#{feedbackOptionsBean.gradeMappingsSelectItems}" />
				</h:selectOneMenu>
				<f:verbatim> </f:verbatim>
				<h:commandButton actionListener="#{feedbackOptionsBean.changeGradeType}" value="#{msgs.feedback_options_change_grade_type}" onclick="SPNR.disableControlsAndSpin( this, null );" />
			</h:panelGroup>
		</h:panelGrid>

		<%@ include file="/inc/globalMessages.jspf"%>

		<h:panelGroup rendered="#{!feedbackOptionsBean.isExistingConflictScale}" styleClass="validation">
		  <h:outputText value="#{msgs.feedback_options_existing_conflict1}" rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
	  	<h:outputLink value="http://kb.iu.edu/data/aitz.html" rendered="#{!feedbackOptionsBean.isExistingConflictScale}" target="support_window1">
	  		<h:outputText value="#{msgs.feedback_options_existing_conflict2}" rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
		  </h:outputLink>
   		  <h:outputText value=" " rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
		  <h:outputText value="#{msgs.feedback_options_existing_conflict3}" rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
		</h:panelGroup>
		<h:panelGroup rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}" styleClass="validation">
		  <h:outputText value="#{msgs.feedback_options_cannot_change_percentage1}" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		  <h:outputLink value="http://kb.iu.edu/data/aitz.html" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}" target="support_window2">
		  	<h:outputText value="#{msgs.feedback_options_cannot_change_percentage2}" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		  </h:outputLink>
  		  <h:outputText value=" " rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		  <h:outputText value="#{msgs.feedback_options_cannot_change_percentage3}" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		</h:panelGroup>	

<!-- RESET TO DEFAULTS LINK -->
		<p>
		<h:commandLink actionListener="#{feedbackOptionsBean.resetMappingValues}">
			<h:outputText value="#{msgs.feedback_options_reset_mapping_values}" />
		</h:commandLink>
		</p>

<!-- GRADE MAPPING TABLE -->
		<t:dataTable cellpadding="0" cellspacing="0"
			id="mappingTable"
			value="#{feedbackOptionsBean.gradeRows}"
			var="gradeRow"
			styleClass="listHier narrowTable">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_grade_header}"/>
				</f:facet>
				<h:outputLabel value="#{gradeRow.grade}" for="mappingValue"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_percent_header}"/>
				</f:facet>
				<h:outputText value="#{gradeRow.mappingValue}"
					rendered="#{!gradeRow.gradeEditable}">
            <f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
        </h:outputText>
				<h:inputText id="mappingValue" value="#{gradeRow.mappingValue}"
					rendered="#{gradeRow.gradeEditable}"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
            <f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
        </h:inputText>
				<h:message for="mappingValue" styleClass="validationEmbedded" />
			</h:column>
		</t:dataTable>

		</div> <!-- END INDNT1 -->

		<p class="act">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.feedback_options_submit}"
				action="#{feedbackOptionsBean.save}"
				onclick="SPNR.disableControlsAndSpin( this, null );" >
				<f:param name="pageName" value="gradebookSetup" />
			</h:commandButton>
			<h:commandButton
				value="#{msgs.feedback_options_cancel}"
				action="#{feedbackOptionsBean.cancel}"
				immediate="true"
				onclick="SPNR.disableControlsAndSpin( this, null );" >
				<f:param name="pageName" value="gradebookSetup" />
			</h:commandButton>
		</p>

	</h:form>
  </div>
</f:view>
