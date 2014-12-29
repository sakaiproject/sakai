<f:view>
<div class="portletBody">
<h:form id="memberForm">

    <sakai:flowState bean="#{editManagersBean}"/>

    <h:panelGroup>
        <t:aliasBean alias="#{viewName}" value="editManagers">
            <%@ include file="/inc/navMenu.jspf"%>
        </t:aliasBean>
    </h:panelGroup>

        <h3><h:outputText value="#{msgs.edit_manager_page_header}"/></h3>
        <h4><h:outputText value="#{editManagersBean.sectionDescription}"/></h4>

        <%@ include file="/inc/globalMessages.jspf"%>

        <h:panelGrid id="transferTable" columns="3" columnClasses="available,transferButtons,selected">
        
            <h:panelGroup>
                <t:div>
                    <h:outputText value="#{msgs.edit_manager_available_label}"/>
                </t:div>
                        
                <t:div>
                    <h:selectManyListbox id="availableUsers" size="20" style="width:250px;">
                        <f:selectItems value="#{editManagersBean.availableUsers}"/>
                    </h:selectManyListbox>
                </t:div>
            </h:panelGroup>
        
            <%@ include file="/inc/transferButtons.jspf"%>
            
            <h:panelGroup>
                <h:outputFormat value="#{msgs.edit_manager_selected_label}" style="white-space:nowrap;">
                    <f:param value="#{editManagersBean.abbreviatedSectionTitle}"/>
                </h:outputFormat>

                <t:div>
                    <h:selectManyListbox id="selectedUsers" size="20" style="width:250px;">
                        <f:selectItems value="#{editManagersBean.selectedUsers}"/>
                    </h:selectManyListbox>
                </t:div>
            </h:panelGroup>

            <t:div style="width:200px;">
                <h:outputText value="#{msgs.edit_manager_ta_note}"/>
            </t:div>
    
            <t:div>
                <h:outputText value=" "/>
            </t:div>

            <t:div>
                <h:outputText value=" "/>
            </t:div>

        </h:panelGrid>
        
        <t:div styleClass="act">
            <h:commandButton
                action="#{editManagersBean.update}"
                onclick="highlightUsers()"
                value="#{msgs.edit_manager_update}"
                immediate="true"
                styleClass="active" />
        
            <h:commandButton
                action="#{editManagersBean.cancel}"
                immediate="true"
                value="#{msgs.cancel}"/>
        </t:div>
</h:form>
</div>
</f:view>
