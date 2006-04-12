<f:view>
<div class="portletBody">
<h:form id="editSectionForm">

    <sakai:flowState bean="#{editSectionBean}"/>

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>
    
        <h3><h:outputText value="#{msgs.edit_section_page_header}"/></h3>
        
        <%@include file="/inc/globalMessages.jspf"%>
    
        <h:panelGrid columns="2">
            <h:outputLabel for="titleInput" value="#{msgs.section_title} #{msgs.section_required}" styleClass="formLabel"/>
            <h:panelGroup>
                <x:div>
                    <h:message for="titleInput" styleClass="validationEmbedded"/>
                </x:div>
                <h:inputText
                    id="titleInput"
                    required="true"
                    value="#{editSectionBean.title}"/>
            </h:panelGroup>
            
            <h:outputLabel for="monday" value="#{msgs.section_days}" styleClass="formLabel"/>
            <h:panelGrid columns="14">
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
            </h:panelGrid>
    
            <h:outputLabel for="startTime" value="#{msgs.section_start_time}" styleClass="formLabel"/>
            <h:panelGroup>
                <x:div>
                    <h:message for="startTime" styleClass="validationEmbedded"/>
                </x:div>
                <h:inputText id="startTime" value="#{editSectionBean.startTime}"/>
                <h:outputText value=" #{msgs.section_start_time_example} "/>
                <x:selectOneRadio id="startTimeAm" layout="spread" value="#{editSectionBean.startTimeAm}">
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
                    value="#{editSectionBean.endTime}"/>
                <h:outputText value=" #{msgs.section_end_time_example} "/>
                <x:selectOneRadio id="endTimeAm" layout="spread" value="#{editSectionBean.endTimeAm}">
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
                <h:inputText
                    id="maxEnrollmentInput"
                    value="#{editSectionBean.maxEnrollments}"/>
            </h:panelGroup>
    
            <h:outputLabel value="#{msgs.section_location}" for="location" styleClass="formLabel"/>
            <h:panelGroup>
                <h:inputText id="location" value="#{editSectionBean.location}" maxlength="20" />
                <h:outputText value="#{msgs.section_location_truncation}"/>
            </h:panelGroup>
        </h:panelGrid>

        <x:div rendered="#{empty addSectionsBean.sections}" styleClass="verticalPadding">
            <%/* Add space if the table isn't rendered */%>
        </x:div>
    
    <x:div styleClass="act">
        <h:commandButton
        	action="#{editSectionBean.update}"
        	value="#{msgs.section_update}"
        	styleClass="active" />
        <h:commandButton action="overview" value="#{msgs.section_cancel}" immediate="true" />
    </x:div>
</h:form>
</div>
</f:view>
