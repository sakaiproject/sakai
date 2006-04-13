<%/* Temporary workaround for lack of a date picker. */%>
<link href="calendar/theme.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="calendar/popcalendar.js"></script>
<f:view>
  <div class="portletBody">
	<h:form id="gbForm">

		<x:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</x:aliasBean>

		<sakai:flowState bean="#{addAssignmentBean}" />

		<h2><h:outputText value="#{msgs.add_assignment_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.add_assignment_instruction}" escape="false"/></div>
		<p class="instruction"><h:outputText value="#{msgs.flag_required}"/></p>

		<h4><h:outputText value="#{msgs.add_assignment_header}"/></h4>

		<x:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
			<%@include file="/inc/assignmentEditing.jspf"%>
		</x:aliasBean>

		<p class="act calendarPadding">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.add_assignment_submit}"
				action="#{addAssignmentBean.saveNewAssignment}"/>
			<h:commandButton
				value="#{msgs.add_assignment_cancel}"
				action="overview" immediate="true"/>
		</p>
	</h:form>
  </div>
</f:view>
