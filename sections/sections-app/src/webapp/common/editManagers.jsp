<f:view>
<div class="portletBody">
<h:form id="memberForm">

    <sakai:flowState bean="#{editManagersBean}"/>

    <h:panelGroup rendered="#{ ! editManagersBean.externallyManaged}">
        <x:aliasBean alias="#{viewName}" value="editManagers">
            <%@include file="/inc/navMenu.jspf"%>
        </x:aliasBean>
    </h:panelGroup>

        <h2><h:outputText value="#{msgs.edit_manager_page_header}"/></h2>
        <h4><h:outputText value="#{editManagersBean.sectionDescription}"/></h4>

        <%@include file="/inc/globalMessages.jspf"%>

        <h:panelGrid columns="3" columnClasses="available,transferButtons,selected">
        
            <h:panelGroup>
                <h:outputText value="#{msgs.edit_manager_available_label}"/>
                        
                <x:div>
                    <h:selectManyListbox id="availableUsers" size="20" style="width:200px;">
                        <f:selectItems value="#{editManagersBean.availableUsers}"/>
                    </h:selectManyListbox>
                </x:div>
            </h:panelGroup>
        
            <%@include file="/inc/transferButtons.jspf"%>
            
            <h:panelGroup>
                <h:outputFormat value="#{msgs.edit_manager_selected_label}">
                    <f:param value="#{editManagersBean.sectionTitle}"/>
                </h:outputFormat>

                <x:div>
                    <h:selectManyListbox id="selectedUsers" size="20" style="width:200px;">
                        <f:selectItems value="#{editManagersBean.selectedUsers}"/>
                    </h:selectManyListbox>
                </x:div>
            </h:panelGroup>

            <x:div style="width:200px;">
                <h:outputText value="#{msgs.edit_manager_ta_note}"/>
            </x:div>
    
            <x:div>
                <h:outputText value=" "/>
            </x:div>

            <x:div>
                <h:outputText value=" "/>
            </x:div>

        </h:panelGrid>
        
        <h:commandButton
            action="#{editManagersBean.update}"
            onclick="highlightUsers()"
            value="#{msgs.edit_manager_update}"/>
    
        <h:commandButton
            action="#{editManagersBean.cancel}"
            value="#{msgs.edit_manager_cancel}"/>
</h:form>
</div>
</f:view>
