<f:view>
<h:form id="addSectionsForm">

    <x:aliasBean alias="#{viewName}" value="addSections">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{addSectionsBean}"/>
    
    <h:messages globalOnly="true"/>

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
    
    <h:selectOneMenu
        id="category"
        immediate="true"
        value="#{addSectionsBean.category}"
        valueChangeListener="#{addSectionsBean.processChangeSections}"
        onchange="this.form.submit()">
        <f:selectItem itemLabel="#{msgs.add_sections_select_one}" itemValue=""/>
        <f:selectItems value="#{addSectionsBean.categoryItems}"/>
    </h:selectOneMenu>
    
    <!--x:dataTable
        id="sectionsTable"
        value="#{addSectionsBean.sections}"
        var="section"
        binding="#{addSectionsBean.sectionTable}"/-->

    <x:dataTable id="sectionTable" value="#{addSectionsBean.sections}" var="section">
        <h:column>
            <h:panelGrid columns="2">
                <h:outputText value="#{msgs.section_title}"/>
                <h:panelGroup>
                    <h:inputText id="titleInput" required="true" value="#{section.title}"/>
                    <h:message for="titleInput"/>
                </h:panelGroup>
                
                <h:outputText value="#{msgs.section_days}"/>
                <h:panelGroup>
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
                </h:panelGroup>
        
                <h:outputText value="#{msgs.section_start_time}"/>
                <h:panelGroup>
                    <h:inputText value="#{section.startTime}">
                        <f:convertDateTime type="time" pattern="h:mm"/>
                    </h:inputText>
                    <h:selectOneRadio value="#{section.startTimeAm}">
                        <f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
                        <f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
                    </h:selectOneRadio>
                </h:panelGroup>
        
                <h:outputText value="#{msgs.section_end_time}"/>
                <h:panelGroup>
                    <h:inputText value="#{section.endTime}">
                        <f:convertDateTime type="time" pattern="h:mm"/>
                    </h:inputText>
                    <h:selectOneRadio value="#{section.endTimeAm}">
                        <f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
                        <f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
                    </h:selectOneRadio>
                </h:panelGroup>
        
                <h:outputText value="#{msgs.section_max_size}"/>
                <h:panelGroup>
                    <h:inputText
                        id="maxEnrollmentInput"
                        value="#{section.maxEnrollments}">
                        <f:validateLongRange minimum="0" />
                    </h:inputText>
                    <h:message for="maxEnrollmentInput"/>
                </h:panelGroup>
        
                <h:outputText value="#{msgs.section_location}"/>
                <h:inputText value="#{section.location}"/>
            </h:panelGrid>
        </h:column>
    </x:dataTable>

    <h:commandButton
        action="#{addSectionsBean.addSections}"
        disabled="#{addSectionsBean.category == null}"
        value="#{msgs.add_sections_add}"/>
    
    <h:commandButton action="overview" value="#{msgs.add_sections_cancel}"/>
        
</h:form>
</f:view>
