<f:view>
	<h:form id="gbForm">

	  <x:aliasBean alias="#{viewName}" value="removeAssignment">
		<%@include file="/inc/appMenu.jspf"%>
	  </x:aliasBean>

	  <gbx:flowState bean="#{removeAssignmentBean}" />
	  <div class="portletBody">

		<h2><h:outputText value="#{msgs.remove_assignment_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.remove_assignment_instruction}" escape="false"/></div>

		<p>
			<h:outputFormat value="#{msgs.remove_assignment_confirmation_question}"
				escape="false">
				<f:param value="#{removeAssignmentBean.assignmentName}"/>
			</h:outputFormat>
		</p>

		<%@include file="/inc/globalMessages.jspf"%>

		<div class="indnt1">
			<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
				columnClasses="prefixedCheckbox">
				<h:selectBooleanCheckbox id="removeConfirmed" value="#{removeAssignmentBean.removeConfirmed}" />
				<h:outputLabel for="removeConfirmed" value="#{msgs.remove_assignment_confirmation_label}" />
			</h:panelGrid>
		</div>

		<p class="act">
			<h:commandButton
				styleClass="active"
				value="#{msgs.remove_assignment_submit}"
				action="#{removeAssignmentBean.removeAssignment}"/>
			<h:commandButton
				value="#{msgs.remove_assignment_cancel}"
				action="#{removeAssignmentBean.cancel}"
				immediate="true"/>
		</p>
	  </div>
	</h:form>
</f:view>
