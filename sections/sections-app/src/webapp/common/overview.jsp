<f:view>
<div class="portletBody">
<h:form id="overviewForm">

    <sakai:flowState bean="#{overviewBean}"/>

    <t:aliasBean alias="#{viewName}" value="overview">
        <%@ include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

	<h3>
	    <h:outputFormat value="#{msgs.overview_page_header}">
	        <f:param value="#{overviewBean.siteRole}"/>
	    </h:outputFormat>
	</h3>
	<div class="instructions">
		<h:outputText value="#{overviewBean.instructions}"/>
	</div>

    <%@ include file="/inc/globalMessages.jspf"%>

    <t:aliasBean alias="#{filterBean}" value="#{overviewBean}">
        <%@ include file="/inc/sectionFilter.jspf"%>
    </t:aliasBean>

    <sec:rowGroupTable cellpadding="0" cellspacing="0"
        id="sectionsTable"
        value="#{overviewBean.sections}"
        var="section"
        sortColumn="#{preferencesBean.overviewSortColumn}"
        sortAscending="#{preferencesBean.overviewSortAscending}"
        styleClass="listHier sectionTable"
        columnClasses="leftIndent,left,left,left,left,right,right,center"
        rowClasses="groupRow"
        >
    
        <h:column>
            <f:facet name="header">
                <t:commandSortHeader columnName="title" immediate="false" arrow="true">
                <h:outputText value="#{msgs.overview_table_header_name}" />
                </t:commandSortHeader>
            </f:facet>
            <t:div>
                <h:outputText value="#{section.title}"/>
            </t:div>
            <t:div styleClass="itemAction" rendered="#{ ! section.readOnly }">
                <h:panelGroup rendered="#{ ! overviewBean.externallyManaged && overviewBean.sectionManagementEnabled}">
                    <h:commandLink action="editSection" value="#{msgs.overview_link_edit}">
                        <f:param name="sectionUuid" value="#{section.uuid}"/>
						<h:outputText value=" (#{section.title})" styleClass="skip"/>
                    </h:commandLink>
                    <h:outputFormat value=" #{msgs.overview_link_sep_char} "/>
                </h:panelGroup>
    
                <h:commandLink
                    action="editManagers"
                    value="#{msgs.overview_link_managers}"
                    rendered="#{overviewBean.sectionTaManagementEnabled}">
                        <f:param name="sectionUuid" value="#{section.uuid}"/>
						<h:outputText value=" (#{section.title})" styleClass="skip"/>
                </h:commandLink>
    
                <h:panelGroup rendered="#{ ! overviewBean.externallyManaged}">
                    <h:outputFormat
                        value=" #{msgs.overview_link_sep_char} "
                        rendered="#{overviewBean.sectionTaManagementEnabled}"/>
        
                    <h:commandLink
                        action="editStudents"
                        value="#{msgs.overview_link_students}"
                        rendered="#{overviewBean.sectionEnrollmentMangementEnabled}">
                            <f:param name="sectionUuid" value="#{section.uuid}"/>
						<h:outputText value=" (#{section.title})" styleClass="skip"/>			
                    </h:commandLink>
                </h:panelGroup>
            </t:div>
        </h:column>
        <h:column>
            <f:facet name="header">
                <t:commandSortHeader columnName="managers" immediate="false" arrow="true">
                <h:outputText value="#{msgs.overview_table_header_managers}" />
                </t:commandSortHeader>
            </f:facet>
            <t:dataList id="instructorName"
                var="instructorName"
                value="#{section.instructorNames}"
                layout="simple">
                    <t:div>
                        <h:outputText value="#{instructorName}" />
                    </t:div>
            </t:dataList>
        </h:column>
        <h:column>
            <f:facet name="header">
                <t:commandSortHeader columnName="meetingDays" immediate="false" arrow="true">
                	<h:outputText value="#{msgs.overview_table_header_day}" />
                </t:commandSortHeader>
            </f:facet>
            <t:dataList id="meetingDayList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
	            <t:div>
	            	<h:outputText value="#{meeting.abbreviatedDays}"/>
	            </t:div>
            </t:dataList>
        </h:column>
        <h:column>
            <f:facet name="header">
                <t:commandSortHeader columnName="meetingTimes" immediate="false" arrow="true">
                	<h:outputText value="#{msgs.overview_table_header_time}" />
                </t:commandSortHeader>
            </f:facet>
            <t:dataList id="meetingTimeList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
	            <t:div>
		            <h:outputText value="#{meeting.times}"/>
	            </t:div>
            </t:dataList>
        </h:column>
        <h:column>
            <f:facet name="header">
                <t:commandSortHeader columnName="location" immediate="false" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_location}" />
                </t:commandSortHeader>
            </f:facet>
            <t:dataList id="meetingLocationList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
	            <t:div>
	            	<h:outputText value="#{meeting.location}"/>
	            </t:div>
            </t:dataList>
        </h:column>
        <h:column>
            <f:facet name="header">
                <t:commandSortHeader columnName="totalEnrollments" immediate="false" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_current_size}" />
                </t:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.totalEnrollments}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <t:commandSortHeader columnName="available" immediate="false" arrow="true">
                    <h:outputText value="#{msgs.overview_table_header_available}" />
                </t:commandSortHeader>
            </f:facet>
            <h:outputText value="#{section.spotsAvailable}"/>
        </h:column>
        <h:column rendered="#{overviewBean.deleteRendered}">
            <f:facet name="header">
                <h:outputText value="#{msgs.overview_table_header_remove}" />
            </f:facet>
            <h:selectBooleanCheckbox id="remove" value="#{section.flaggedForRemoval}" rendered="#{ ! section.readOnly }"/>
        </h:column>
    </sec:rowGroupTable>

    <t:div styleClass="verticalPadding" rendered="#{empty overviewBean.sections}">
        <h:outputText value="#{msgs.no_sections_available}"/>
        <h:outputText value="#{msgs.no_sections_instructions}" rendered="#{overviewBean.sectionManagementEnabled}"/>
    </t:div>

    <t:div rendered="#{overviewBean.deleteRendered}" styleClass="verticalPadding">
        <%/* Add space before the buttons */%>
    </t:div>

    <t:div styleClass="act">
        <h:commandButton
            action="#{overviewBean.confirmDelete}"
            value="#{msgs.overview_delete}"
            rendered="#{overviewBean.deleteRendered}"
            styleClass="active"/>
    
        <h:commandButton
            action="overview"
            value="#{msgs.cancel}"
            rendered="#{overviewBean.deleteRendered}"/>
    </t:div>

</h:form>
</div>
</f:view>
