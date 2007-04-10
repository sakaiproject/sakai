<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">

			<t:aliasBean alias="#{bean}" value="#{gradebookSetupBean}">
				<%@include file="/inc/appMenu.jspf"%>
			</t:aliasBean>
	
			<sakai:flowState bean="#{gradebookSetupBean}" />
	
			<h2><h:outputText value="#{msgs.gb_setup_title}"/></h2>
			
			<%@include file="/inc/globalMessages.jspf"%>
	
			<h4><h:outputText value="#{msgs.grade_entry_heading}"/></h4>
			
			<div class="indnt1">
				<div class="instruction"><h:outputText value="#{msgs.grade_entry_info}" escape="false"/></div>
			
				<h:selectOneRadio value="#{gradebookSetupBean.gradeEntryMethod}" id="gradeEntryMethod" layout="pageDirection">
					<f:selectItem itemValue="points" itemLabel="#{msgs.entry_opt_points}" />
	        <f:selectItem itemValue="percent" itemLabel="#{msgs.entry_opt_percent}" /> 
				</h:selectOneRadio>
	
				<div class="gbSection">
					<h:selectBooleanCheckbox id="releaseItems" value="#{gradebookSetupBean.localGradebook.assignmentsDisplayed}"	/>
					<h:outputLabel for="releaseItems" value="#{msgs.display_released_items}" />
					<div class="indnt2">
						<h:outputText styleClass="instruction" value="#{msgs.display_released_items_info}" />
					</div>
				</div>
				
				<div class="gbSection">
					<h:selectBooleanCheckbox id="displayCumScore" value="#{gradebookSetupBean.localGradebook.courseGradeDisplayed}"/>
					<h:outputLabel for="displayCumScore" value="#{msgs.display_cum_score}" />
				</div>
			
			</div>
	 
		  <t:aliasBean alias="#{bean}" value="#{gradebookSetupBean}">
				<%@include file="/inc/categoryEdit.jspf"%>
			</t:aliasBean>
			
			<div class="act calendarPadding">
				<h:commandButton
					id="saveButton"
					styleClass="active"
					value="#{msgs.gb_setup_save}"
					action="#{gradebookSetupBean.processSaveGradebookSetup}"/>
				<h:commandButton
					value="#{msgs.gb_setup_cancel}"
					action="#{gradebookSetupBean.processCancelGradebookSetup}" immediate="true"/>
			</div>
			
	  </h:form>
	</div>
</f:view>
