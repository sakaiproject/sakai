<f:view>
  <div class="portletBody">
	<h:form id="gbForm">

		<t:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
			<%@include file="/inc/appMenu.jspf"%>
			
			<%@include file="/inc/breadcrumb.jspf" %>
		</t:aliasBean>

		<sakai:flowState bean="#{addAssignmentBean}" />

		<p class="instruction gbSection"><h:outputText value="#{msgs.flag_required}"/></p>

		<t:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
			<%@include file="/inc/assignmentEditing.jspf"%>
		</t:aliasBean>

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
