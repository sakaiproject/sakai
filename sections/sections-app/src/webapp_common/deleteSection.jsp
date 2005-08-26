<f:view>
<h:form id="editSectionForm">

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{editSectionBean}"/>
    
    <h:panelGrid columns="1">
        <h:panelGroup>
            <h:graphicImage value="images/asterisk.gif"/>
            <h:outputFormat value="#{msgs.delete_section_confirmation}">
                <f:param value="#{editSectionBean.title}"/>
            </h:outputFormat>
        </h:panelGroup>
        
        <h:panelGroup>
            <h:commandButton action="#{editSectionBean.delete}" value="Delete"/>
            <h:commandButton action="editSection" value="Cancel"/>
        </h:panelGroup>
    </h:panelGrid>

    <h:panelGrid columns="2">
        <h:outputText value="#{msgs.edit_section_title}"/>
        <h:outputText value="#{editSectionBean.title}"/>
        
        <h:outputText value="#{msgs.edit_section_days}"/>
        <h:outputText value="#{editSectionBean.days}"/>

        <h:outputText value="#{msgs.edit_section_start_time}"/>
        <h:outputText value="#{editSectionBean.startTime}"/>

        <h:outputText value="#{msgs.edit_section_end_time}"/>
        <h:outputText value="#{editSectionBean.endTime}"/>

        <h:outputText value="#{msgs.edit_section_max_size}"/>
        <h:outputText value="#{editSectionBean.maxEnrollments}"/>

        <h:outputText value="#{msgs.edit_section_location}"/>
        <h:outputText value="#{editSectionBean.location}"/>
    </h:panelGrid>

    <h:commandButton action="#{editSectionBean.update}" value="Update" disabled="true" />
    
    <h:commandButton action="overview" value="Cancel" disabled="true" />
    
</h:form>
</f:view>
