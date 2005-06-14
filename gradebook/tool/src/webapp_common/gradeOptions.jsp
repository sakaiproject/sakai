<f:view>
  <h:form id="gbForm">

	<x:aliasBean alias="#{viewName}" value="feedbackOptions">
		<%@include file="/inc/appMenu.jspf"%>
	</x:aliasBean>

	<gbx:flowState bean="#{feedbackOptionsBean}" />

	<div class="portletBody">
		<h2><h:outputText value="#{msgs.feedback_options_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.feedback_options_instruction}" escape="false"/></div>

		<div class="indnt1">

<!-- Grade Display -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_display}"/></h4>
		<h:panelGrid columns="2" columnClasses="prefixedCheckbox">
			<h:selectBooleanCheckbox id="displayAssignmentGrades" value="#{feedbackOptionsBean.displayAssignmentGrades}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayAssignmentGrades" value="#{msgs.feedback_options_grade_display_assignment_grades}" />

			<h:selectBooleanCheckbox id="displayCourseGrades" value="#{feedbackOptionsBean.displayCourseGrades}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayCourseGrades" value="#{msgs.feedback_options_grade_display_course_grades}" />
		</h:panelGrid>

<!-- Grade Conversion -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_conversion}"/></h4>
		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">

			<h:outputText value="#{msgs.feedback_options_grade_type}" />

			<h:panelGroup>
				<h:selectOneMenu id="selectGradeType" value="#{feedbackOptionsBean.selectedGradeMappingId}">
					<f:selectItems value="#{feedbackOptionsBean.gradeMappingsSelectItems}" />
				</h:selectOneMenu>
				<f:verbatim> </f:verbatim>
				<h:commandButton actionListener="#{feedbackOptionsBean.changeGradeType}" value="#{msgs.feedback_options_change_grade_type}" />
			</h:panelGroup>
		</h:panelGrid>

		<%@include file="/inc/globalMessages.jspf"%>

<!-- RESET TO DEFAULTS LINK -->
		<p>
		<h:commandLink actionListener="#{feedbackOptionsBean.resetMappingValues}">
			<h:outputText value="#{msgs.feedback_options_reset_mapping_values}" />
		</h:commandLink>
		</p>

<!-- GRADE MAPPING TABLE -->
		<h:dataTable cellpadding="0" cellspacing="0"
			id="mappingTable"
			value="#{feedbackOptionsBean.gradeMappingRowsMap[feedbackOptionsBean.selectedGradeMappingId]}"
			var="row"
			styleClass="listHier narrowTable">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_grade_header}"/>
				</f:facet>
				<h:outputText value="#{row.grade}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_percent_header}"/>
				</f:facet>
				<h:outputText value="#{row.percentage}" rendered="#{row.readOnly}"/>
				<h:inputText id="mappingValue" value="#{row.percentage}" rendered="#{!row.readOnly}"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
				<h:message for="mappingValue" styleClass="validationEmbedded" />
			</h:column>
		</h:dataTable>

		</div> <!-- END INDNT1 -->

		<p class="act">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.feedback_options_submit}"
				action="#{feedbackOptionsBean.save}" />
			<h:commandButton
				value="#{msgs.feedback_options_cancel}"
				action="#{feedbackOptionsBean.cancel}"
				immediate="true" />
		</p>

	</div>
  </h:form>
</f:view>
