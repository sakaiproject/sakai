<h:panelGroup rendered="#{authorization.managePermissions}">
    <li role="menuitem">
        <h:panelGroup styleClass="menuitem">
            <h:commandLink id="permissionsLink" accesskey="#{generalMessages.a_permissions}" title="#{generalMessages.t_permissions}" action="permissions" immediate="true">
                <h:outputText value="#{generalMessages.permissions}" />
            </h:commandLink>
        </h:panelGroup>
    </li>
</h:panelGroup>
