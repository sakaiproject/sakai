<f:view>
<h:form id="memberForm">

    <x:aliasBean alias="#{viewName}" value="editManagers">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{editManagersBean}"/>

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
        </h:panelGroup>
    
        <h:panelGroup>
            <f:verbatim>
                <p>
                    <input type="button" onclick="addAll();" value="&gt;&gt;&gt;" />
                </p>
                <p>
                    <input type="button" onclick="addUser();" value="&gt;"/>
                </p>
                <p>
                    <input type="button" onclick="removeUser();" value="&lt;"/>
                </p>
                <p>
                    <input type="button" onclick="removeAll();" value="&lt;&lt;&lt;"/>
                </p>
            </f:verbatim>
        </h:panelGroup>
        
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


</h:form>
</f:view>
