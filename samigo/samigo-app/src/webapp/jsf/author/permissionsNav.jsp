<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <h:panelGroup rendered="#{authorization.managePermissions}">
    <li role="menuitem">
      <h:panelGroup styleClass="menuitem">
        <h:commandLink id="permissionsLink" accesskey="#{generalMessages.a_permissions}" title="#{generalMessages.t_permissions}" action="permissions" immediate="true">
          <h:outputText value="#{generalMessages.permissions}"/>
        </h:commandLink>
      </h:panelGroup>
    </li>
  </h:panelGroup>
</jsp:root>
