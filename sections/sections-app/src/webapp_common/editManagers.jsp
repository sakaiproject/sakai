<f:view>
<h:form id="memberForm">

    <sakai:flowState bean="#{editManagersBean}"/>

    <h:panelGroup rendered="#{ ! editManagersBean.externallyManaged}">
        <x:aliasBean alias="#{viewName}" value="editManagers">
            <%@include file="/inc/navMenu.jspf"%>
        </x:aliasBean>
    </h:panelGroup>

    <h:panelGrid columns="3">
    
        <h:panelGroup>
            <h:outputFormat value="#{msgs.edit_manager_available_label}">
                <f:param value="#{editManagersBean.courseTitle}"/>
            </h:outputFormat>
        
            <f:verbatim>
                <br/>
            </f:verbatim>
        
            <h:selectManyListbox id="availableUsers" size="20" style="width:200px;">
                <f:selectItems value="#{editManagersBean.availableUsers}"/>
            </h:selectManyListbox>
            
            <f:verbatim>
                <br/>
            </f:verbatim>
            
            <h:outputText value="#{msgs.edit_manager_ta_note}"/>
        </h:panelGroup>
    
        <%@include file="/inc/transferButtons.jspf"%>
        
        <h:panelGroup>
            <h:outputFormat value="#{msgs.edit_manager_selected_label}">
                <f:param value="#{editManagersBean.sectionTitle}"/>
            </h:outputFormat>
        
            <f:verbatim>
                <br/>
            </f:verbatim>
        
            <h:selectManyListbox id="selectedUsers" size="20" style="width:200px;">
                <f:selectItems value="#{editManagersBean.selectedUsers}"/>
            </h:selectManyListbox>
        </h:panelGroup>

    </h:panelGrid>
    
    <h:commandButton
        action="#{editManagersBean.update}"
        onclick="highlightUsers()"
        value="#{msgs.edit_manager_update}"/>

    <h:commandButton
        action="#{editManagersBean.cancel}"
        value="#{msgs.edit_manager_cancel}"/>

</h:form>
</f:view>
