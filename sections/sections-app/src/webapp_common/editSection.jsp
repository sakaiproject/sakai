<f:view>
<h:form id="editSectionForm">

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{editSectionBean}"/>
    
    <h:messages globalOnly="true"/>
    
    <h:panelGrid columns="2">
        <h:outputText value="#{msgs.section_title}"/>
        <h:panelGroup>
            <h:inputText id="titleInput" required="true" value="#{editSectionBean.title}"/>
            <h:message for="titleInput"/>
        </h:panelGroup>
        
        <h:outputText value="#{msgs.section_days}"/>
        <h:panelGroup>
            <h:selectBooleanCheckbox id="monday" value="#{editSectionBean.monday}"/>
            <h:outputLabel for="monday" value="#{msgs.day_of_week_monday}"/>

            <h:selectBooleanCheckbox id="tuesday" value="#{editSectionBean.tuesday}"/>
            <h:outputLabel for="tuesday" value="#{msgs.day_of_week_tuesday}"/>

            <h:selectBooleanCheckbox id="wednesday" value="#{editSectionBean.wednesday}"/>
            <h:outputLabel for="wednesday" value="#{msgs.day_of_week_wednesday}"/>

            <h:selectBooleanCheckbox id="thursday" value="#{editSectionBean.thursday}"/>
            <h:outputLabel for="thursday" value="#{msgs.day_of_week_thursday}"/>

            <h:selectBooleanCheckbox id="friday" value="#{editSectionBean.friday}"/>
            <h:outputLabel for="friday" value="#{msgs.day_of_week_friday}"/>

            <h:selectBooleanCheckbox id="saturday" value="#{editSectionBean.saturday}"/>
            <h:outputLabel for="saturday" value="#{msgs.day_of_week_saturday}"/>

            <h:selectBooleanCheckbox id="sunday" value="#{editSectionBean.sunday}"/>
            <h:outputLabel for="sunday" value="#{msgs.day_of_week_sunday}"/>
        </h:panelGroup>

        <h:outputText value="#{msgs.section_start_time}"/>
        <h:panelGroup>
            <h:inputText value="#{editSectionBean.startTime}"/>
            <h:selectOneRadio value="#{editSectionBean.startTimeAm}">
                <f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
                <f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
            </h:selectOneRadio>
        </h:panelGroup>

        <h:outputText value="#{msgs.section_end_time}"/>
        <h:panelGroup>
            <h:inputText value="#{editSectionBean.endTime}"/>
            <h:selectOneRadio value="#{editSectionBean.endTimeAm}">
                <f:selectItem itemValue="true" itemLabel="#{msgs.time_of_day_am_cap}"/>
                <f:selectItem itemValue="false" itemLabel="#{msgs.time_of_day_pm_cap}"/>
            </h:selectOneRadio>
        </h:panelGroup>

        <h:outputText value="#{msgs.section_max_size}"/>
        <h:panelGroup>
            <h:inputText
                id="maxEnrollmentInput"
                required="true"
                value="#{editSectionBean.maxEnrollments}">
                <f:validateLongRange minimum="0" />
            </h:inputText>
            <h:message for="maxEnrollmentInput"/>
        </h:panelGroup>

        <h:outputText value="#{msgs.section_location}"/>
        <h:inputText value="#{editSectionBean.location}"/>
    </h:panelGrid>

    <h:commandButton action="#{editSectionBean.update}" value="Update"/>
    
    <h:commandButton action="deleteSection" value="Delete">
        <f:param name="sectionUuid" value="#{editSectionBean.sectionUuid}"/>
    </h:commandButton>
    
    <h:commandButton action="overview" value="Cancel" immediate="true" />
    
</h:form>
</f:view>
