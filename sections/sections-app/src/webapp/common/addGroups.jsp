<f:view>
<div class="portletBody">
<h:form id="addGroupsForm">

    <sakai:flowState bean="#{addGroupsBean}"/>

    <x:aliasBean alias="#{viewName}" value="addGroups">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

        <h3><h:outputText value="#{msgs.nav_add_groups}"/></h3>

        <%@include file="/inc/globalMessages.jspf"%>
    
        <h:outputText value="#{msgs.add_groups_add}"/>
        <h:selectOneMenu
            id="numToAdd"
            immediate="true"
            value="#{addGroupsBean.numToAdd}"
            valueChangeListener="#{addGroupsBean.processChangeGroups}"
            onchange="this.form.submit()">
            <f:selectItem itemValue="1"/>
            <f:selectItem itemValue="2"/>
            <f:selectItem itemValue="3"/>
            <f:selectItem itemValue="4"/>
            <f:selectItem itemValue="5"/>
            <f:selectItem itemValue="6"/>
            <f:selectItem itemValue="7"/>
            <f:selectItem itemValue="8"/>
            <f:selectItem itemValue="9"/>
            <f:selectItem itemValue="10"/>
        </h:selectOneMenu>
        <h:outputText value="#{msgs.add_groups_groups}"/>

        <x:dataTable
            id="groupTable"
            value="#{addGroupsBean.groups}"
            var="group">
            <h:column>
                <h:panelGrid columns="2">
                    <h:outputLabel for="titleInput" value="#{msgs.add_groups_name} #{msgs.section_required}" styleClass="formLabel"/>
                    <h:panelGroup>
                        <x:div>
                            <h:message for="titleInput" styleClass="validationEmbedded"/>
                        </x:div>
                        <h:inputText
                            id="titleInput"
                            required="true"
                            value="#{group.title}"/>
                    </h:panelGroup>

                    <h:outputLabel for="description" value="#{msgs.add_groups_description}" styleClass="formLabel"/>
                    <h:inputText id="description" value="#{group.description}" maxlength="255" />
                </h:panelGrid>
            </h:column>
        </x:dataTable>
    
        <x:div rendered="#{empty addGroupsBean.groups}" styleClass="verticalPadding">
            <%/* Add space if the table isn't rendered */%>
        </x:div>

		<x:div styleClass="act">
	        <h:commandButton
	            action="#{addGroupsBean.addGroups}"
	            value="#{msgs.add_groups_add_action}"
	            styleClass="active" />
	        
	        <h:commandButton action="overview" immediate="true" value="#{msgs.add_sections_cancel}"/>
		</x:div>
</h:form>
</div>
</f:view>
