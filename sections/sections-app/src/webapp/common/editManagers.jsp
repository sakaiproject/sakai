<f:view>
<div class="portletBody">
<h:form id="memberForm">

    <sakai:flowState bean="#{editManagersBean}"/>

    <h:panelGroup rendered="#{ ! editManagersBean.externallyManaged}">
        <x:aliasBean alias="#{viewName}" value="editManagers">
            <%@include file="/inc/navMenu.jspf"%>
        </x:aliasBean>
    </h:panelGroup>

        <h3><h:outputText value="#{msgs.edit_manager_page_header}"/></h3>
        <h4><h:outputText value="#{editManagersBean.sectionDescription}"/></h4>

        <%@include file="/inc/globalMessages.jspf"%>

        <h:panelGrid id="transferTable" columns="3" columnClasses="available,transferButtons,selected">
        
            <h:panelGroup>
                <x:div>
                    <h:outputText value="#{msgs.edit_manager_available_label}"/>
                </x:div>
                        
                <x:div>
                    <h:selectManyListbox id="availableUsers" size="20" style="width:250px;">
                        <f:selectItems value="#{editManagersBean.availableUsers}"/>
                    </h:selectManyListbox>
                </x:div>
            </h:panelGroup>
        
            <%@include file="/inc/transferButtons.jspf"%>
            
            <h:panelGroup>
                <h:outputFormat value="#{msgs.edit_manager_selected_label}" style="white-space:nowrap;">
                    <f:param value="#{editManagersBean.abbreviatedSectionTitle}"/>
                </h:outputFormat>

                <x:div>
                    <h:selectManyListbox id="selectedUsers" size="20" style="width:250px;">
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
        
        <x:div styleClass="act">
            <h:commandButton
                action="#{editManagersBean.update}"
                onclick="highlightUsers()"
                value="#{msgs.edit_manager_update}"
                immediate="true"
                styleClass="active" />
        
            <h:commandButton
                action="#{editManagersBean.cancel}"
                immediate="true"
                value="#{msgs.edit_manager_cancel}"/>
        </x:div>
</h:form>
</div>
</f:view>
