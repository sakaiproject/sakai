<f:view>
<div class="portletBody">
<h:form id="addSectionsForm">

	<sakai:flowState bean="#{addSectionsBean}"/>

	<x:aliasBean alias="#{viewName}" value="addSections">
		<%@include file="/inc/navMenu.jspf"%>
	</x:aliasBean>

		<h3><h:outputText value="#{msgs.nav_add_sections}"/></h3>

		<x:div styleClass="instructions">
			<h:outputText value="#{msgs.add_section_instructions}"/>
		</x:div>

		<%@include file="/inc/globalMessages.jspf"%>
	
		<h:outputText value="#{msgs.add_section_add}"/>
		<h:selectOneMenu
			id="numToAdd"
			immediate="true"
			value="#{addSectionsBean.numToAdd}"
			valueChangeListener="#{addSectionsBean.processChangeSections}"
			onchange="this.form.submit()">
			<f:selectItem itemValue="1"/>
			<f:selectItem itemValue="2"/>
			<f:selectItem itemValue="3"/>
			<f:selectItem itemValue="4"/>
			<f:selectItem itemValue="5"/>
			<f:selectItem itemValue="6"/>
			<f:selectItem itemValue="7"/>
			<f:selectItem itemValue="8"/>
			<f:selectItem itemValue="9"/>
			<f:selectItem itemValue="10"/>
		</h:selectOneMenu>
		<h:outputText value="#{msgs.add_section_sections_of}"/>
		<h:selectOneMenu
			id="category"
			immediate="true"
			value="#{addSectionsBean.category}"
			valueChangeListener="#{addSectionsBean.processChangeSections}"
			onchange="this.form.submit()">
			<f:selectItem itemLabel="#{msgs.add_sections_select_one}" itemValue=""/>
			<f:selectItems value="#{addSectionsBean.categoryItems}"/>
		</h:selectOneMenu>
		<h:outputText value="#{msgs.add_section_category}"/>
	
		<x:div rendered="#{not empty addSectionsBean.sections}" styleClass="verticalPadding">
			<%/* Add space if the table isn't rendered */%>
		</x:div>

		<x:dataTable
			id="sectionTable"
			value="#{addSectionsBean.sections}"
			var="section"
			rowClasses="#{addSectionsBean.rowStyleClasses}">
			<h:column>
				<h:panelGrid columns="2" rowClasses="sectionRow">

					<% // Title %>

					<h:outputLabel for="titleInput" value="#{msgs.section_title} #{msgs.section_required}" styleClass="formLabel"/>
					<h:panelGroup>
						<x:div>
							<h:message for="titleInput" styleClass="validationEmbedded"/>
						</x:div>
						<h:inputText
							id="titleInput"
							required="true"
							value="#{section.title}"/>
					</h:panelGroup>

					<% // Max Size %>

					<h:outputLabel for="maxEnrollmentInput" value="#{msgs.section_max_size}" styleClass="formLabel"/>
					<h:panelGroup>
						<x:div>
							<h:message for="maxEnrollmentInput" styleClass="validationEmbedded"/>
						</x:div>
						<h:inputText id="maxEnrollmentInput" value="#{section.maxEnrollments}"/>
					</h:panelGroup>

					<% // Logistics %>
					
					<h:outputLabel value="#{msgs.section_logistics}" styleClass="formLabel"/>
					<h:dataTable id="meetingsTable" value="#{section.meetings}" var="meeting">
					
						<% // One column per meeting.  Use a div to add a line break in the form controls %>
						<h:column>
							<h:panelGroup>
								<x:div>
									<h:selectBooleanCheckbox id="monday" value="#{meeting.monday}"/>
									<h:outputLabel for="monday" value="#{msgs.day_of_week_monday}"/>
						
									<h:selectBooleanCheckbox id="tuesday" value="#{meeting.tuesday}"/>
									<h:outputLabel for="tuesday" value="#{msgs.day_of_week_tuesday}"/>
						
									<h:selectBooleanCheckbox id="wednesday" value="#{meeting.wednesday}"/>
									<h:outputLabel for="wednesday" value="#{msgs.day_of_week_wednesday}"/>
						
									<h:selectBooleanCheckbox id="thursday" value="#{meeting.thursday}"/>
									<h:outputLabel for="thursday" value="#{msgs.day_of_week_thursday}"/>
						
									<h:selectBooleanCheckbox id="friday" value="#{meeting.friday}"/>
									<h:outputLabel for="friday" value="#{msgs.day_of_week_friday}"/>
						
									<h:selectBooleanCheckbox id="saturday" value="#{meeting.saturday}"/>
									<h:outputLabel for="saturday" value="#{msgs.day_of_week_saturday}"/>
						
									<h:selectBooleanCheckbox id="sunday" value="#{meeting.sunday}"/>
									<h:outputLabel for="sunday" value="#{msgs.day_of_week_sunday}"/>
								</x:div>
	
								<x:div>
									<h:outputFormat value="#{msgs.section_start_time}"/>
									<h:panelGroup>
										<x:div>
											<h:message for="startTime" styleClass="validationEmbedded"/>
										</x:div>
										<h:inputText id="startTime" value="#{meeting.startTimeString}" size="8"/>
										<x:selectOneRadio id="startTimeAm" layout="spread" value="#{meeting.startTimeAmString}">
											<f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
											<f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
										</x:selectOneRadio>
										<x:radio for="startTimeAm" index="0" />
										<x:radio for="startTimeAm" index="1" />
									</h:panelGroup>
				
									<h:outputLabel for="endTime" value="#{msgs.section_end_time}"/>
									<h:panelGroup>
										<x:div>
											<h:message for="endTime" styleClass="validationEmbedded"/>
										</x:div>
										<h:inputText  id="endTime" value="#{meeting.endTimeString}"  size="8"/>
										<x:selectOneRadio id="endTimeAm" layout="spread" value="#{meeting.endTimeAmString}">
											<f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
											<f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
										</x:selectOneRadio>
										<x:radio for="endTimeAm" index="0" />
										<x:radio for="endTimeAm" index="1" />
									</h:panelGroup>
		
									<h:outputLabel for="location" value="#{msgs.section_location}"/>
									<h:panelGroup>
										<h:inputText id="location" value="#{meeting.location}" maxlength="20" />
										<x:div>
											<h:outputText value=" #{msgs.section_location_truncation} "/>
										</x:div>
									</h:panelGroup>
								</x:div>
							</h:panelGroup>
						</h:column>
					</h:dataTable>

				</h:panelGrid>
			</h:column>
		</x:dataTable>
	
		<x:div rendered="#{empty addSectionsBean.sections}" styleClass="verticalPadding">
			<%/* Add space if the table isn't rendered */%>
		</x:div>

		<x:div styleClass="act">
			<h:commandButton
				action="#{addSectionsBean.addSections}"
				disabled="#{empty addSectionsBean.category}"
				value="#{msgs.add_sections_add}"
				styleClass="active" />
			
			<h:commandButton action="overview" immediate="true" value="#{msgs.add_sections_cancel}"/>
		</x:div>
</h:form>
</div>
</f:view>
