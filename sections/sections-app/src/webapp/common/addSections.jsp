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
                    
                    <h:outputLabel for="monday" value="#{msgs.section_days}" styleClass="formLabel"/>
                    <h:panelGrid columns="14">
                        <h:selectBooleanCheckbox id="monday" value="#{section.monday}"/>
                        <h:outputLabel for="monday" value="#{msgs.day_of_week_monday}"/>
            
                        <h:selectBooleanCheckbox id="tuesday" value="#{section.tuesday}"/>
                        <h:outputLabel for="tuesday" value="#{msgs.day_of_week_tuesday}"/>
            
                        <h:selectBooleanCheckbox id="wednesday" value="#{section.wednesday}"/>
                        <h:outputLabel for="wednesday" value="#{msgs.day_of_week_wednesday}"/>
            
                        <h:selectBooleanCheckbox id="thursday" value="#{section.thursday}"/>
                        <h:outputLabel for="thursday" value="#{msgs.day_of_week_thursday}"/>
            
                        <h:selectBooleanCheckbox id="friday" value="#{section.friday}"/>
                        <h:outputLabel for="friday" value="#{msgs.day_of_week_friday}"/>
            
                        <h:selectBooleanCheckbox id="saturday" value="#{section.saturday}"/>
                        <h:outputLabel for="saturday" value="#{msgs.day_of_week_saturday}"/>
            
                        <h:selectBooleanCheckbox id="sunday" value="#{section.sunday}"/>
                        <h:outputLabel for="sunday" value="#{msgs.day_of_week_sunday}"/>
                    </h:panelGrid>
            
                    <h:outputLabel for="startTime" value="#{msgs.section_start_time}" styleClass="formLabel"/>
                    <h:panelGroup>
                        <x:div>
                            <h:message for="startTime" styleClass="validationEmbedded"/>
                        </x:div>
                        <h:inputText id="startTime" value="#{section.startTime}"/>
                        <h:outputText value=" #{msgs.section_start_time_example} "/>
                        <x:selectOneRadio id="startTimeAm" layout="spread" value="#{section.startTimeAm}">
                            <f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
                            <f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
                        </x:selectOneRadio>
                        <x:radio for="startTimeAm" index="0" />
                        <x:radio for="startTimeAm" index="1" />
                    </h:panelGroup>

                    <h:outputLabel for="endTime" value="#{msgs.section_end_time}" styleClass="formLabel"/>
                    <h:panelGroup>
                        <x:div>
                            <h:message for="endTime" styleClass="validationEmbedded"/>
                        </x:div>
                        <h:inputText
                            id="endTime"
                            value="#{section.endTime}"/>
                        <h:outputText value=" #{msgs.section_end_time_example} "/>
                        <x:selectOneRadio id="endTimeAm" layout="spread" value="#{section.endTimeAm}">
                            <f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
                            <f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
                        </x:selectOneRadio>
                        <x:radio for="endTimeAm" index="0" />
                        <x:radio for="endTimeAm" index="1" />
                    </h:panelGroup>
            
                    <h:outputLabel for="maxEnrollmentInput" value="#{msgs.section_max_size}" styleClass="formLabel"/>
                    <h:panelGroup>
                        <x:div>
                            <h:message for="maxEnrollmentInput" styleClass="validationEmbedded"/>
                        </x:div>
                        <h:inputText id="maxEnrollmentInput" value="#{section.maxEnrollments}"/>
                    </h:panelGroup>

                    <h:outputLabel for="location" value="#{msgs.section_location}" styleClass="formLabel"/>
                    <h:panelGroup>
                        <h:inputText id="location" value="#{section.location}" maxlength="20" />
                        <x:div>
                            <h:outputText value=" #{msgs.section_location_truncation} "/>
                        </x:div>
                    </h:panelGroup>
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
