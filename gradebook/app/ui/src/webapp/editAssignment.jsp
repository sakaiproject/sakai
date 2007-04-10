<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">

		<t:aliasBean alias="#{bean}" value="#{editAssignmentBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</t:aliasBean>

		<sakai:flowState bean="#{editAssignmentBean}" />

		<h2><h:outputText value="#{msgs.edit_assignment_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.edit_assignment_instruction}" escape="false"/></div>
		<p class="instruction"><h:outputText value="#{msgs.flag_required}"/></p>

		<div class="indnt1">
		<h4><h:outputText value="#{msgs.edit_assignment_header}"/></h4>

		<t:aliasBean alias="#{bean}" value="#{editAssignmentBean}">
			<%@include file="/inc/assignmentEditing.jspf"%>
		</t:aliasBean>

		</div>

		<p class="act calendarPadding">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.add_assignment_submit}"
				action="#{editAssignmentBean.updateAssignment}"/>
			<h:commandButton
				value="#{msgs.add_assignment_cancel}"
				action="#{editAssignmentBean.cancelToAssignmentDetails}"
				immediate="true"/>
		</p>
	  </h:form>
	</div>
</f:view>
