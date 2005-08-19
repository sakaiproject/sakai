<f:view>
<h:form id="overviewForm">

    <sakai:flowState bean="#{overviewBean}"/>
    
    <x:dataTable cellpadding="0" cellspacing="0"
        id="sectionsTable"
        value="#{overviewBean.sections}"
        var="section"
        sortColumn="#{overviewBean.sortColumn}"
        sortAscending="#{overviewBean.sortAscending}"
        styleClass="listHier narrowTable">

        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="title" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_name}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.title}"/>

            <h:commandLink action="editSection" value="#{msgs.overview_link_edit}">
                <f:param name="sectionUuid" value="#{section.uuid}"/>
            </h:commandLink>
            
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="time" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_day_time}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.meetingTimes}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="managers" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_managers}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.instructorNames}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="location" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_location}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.location}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="max" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_max_size}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.maxEnrollments}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="available" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_available}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.spotsAvailable}"/>
        </h:column>
    </x:dataTable>

</h:form>
</f:view>
