<f:view>
<h:form id="studentViewForm">

    <sakai:flowState bean="#{studentViewBean}"/>
    
    <h:selectOneMenu value="#{studentViewBean.sectionFilter}" onchange="this.form.submit()">
        <f:selectItems value="#{studentViewBean.sectionCategoryItems}"/>
    </h:selectOneMenu>
    
    <x:dataTable cellpadding="0" cellspacing="0"
        id="studentViewSectionsTable"
        value="#{studentViewBean.sections}"
        var="section"
        sortColumn="#{studentViewBean.sortColumn}"
        sortAscending="#{studentViewBean.sortAscending}"
        styleClass="listHier narrowTable">

        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="time" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_time}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.meetingTimes}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="instructor" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_instructor}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.instructorNames}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="title" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_title}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.title}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="category" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_category}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.categoryForDisplay}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="location" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_location}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.location}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="max" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_max}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.maxEnrollments}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="available" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_available}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.spotsAvailable}"/>
        </h:column>
        <h:column rendered="#{!studentViewBean.externallyManaged}">
            <f:facet name="header">
                <x:commandSortHeader columnName="change" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.student_view_header_change}" />
                </x:commandSortHeader>
            </f:facet>
            <h:commandLink
                value="Join"
                actionListener="#{studentViewBean.processJoinSection}"
                rendered="#{section.joinable}">
                <f:param name="sectionUuid" value="#{section.uuid}"/>
            </h:commandLink>
            <h:commandLink
                value="Switch"
                actionListener="#{studentViewBean.processSwitchSection}"
                rendered="#{section.switchable}">
                <f:param name="sectionUuid" value="#{section.uuid}"/>
            </h:commandLink>
            <h:outputText
                value="Full"
                rendered="#{section.full}"/>
            <h:outputText
                value="Member"
                rendered="#{section.member}"/>
        </h:column>

    </x:dataTable>

</h:form>
</f:view>
