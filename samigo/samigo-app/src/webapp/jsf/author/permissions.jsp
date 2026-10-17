<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text><![CDATA[



<!DOCTYPE html>
]]></jsp:text>
  <f:view>
    <html>
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{generalMessages.permissions}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <div class="samigo-container">
              <h:form id="permissionsForm">
                <jsp:directive.include file="/jsf/author/permissionsHeadings.jsp"/>
              </h:form>
              <div class="d-flex align-items-center mb-3">
                <h1 class="fs-3 mb-0">
                  <h:outputText value="#{generalMessages.permissions}"/>
                </h1>
              </div>
              <h:panelGroup rendered="#{template.showAssessmentTypes}">
                <jsp:element name="sakai-permissions">
                  <jsp:attribute name="id" trim="false">samigo-permissions</jsp:attribute>
                  <jsp:attribute name="tool" trim="false">assessment</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </h:panelGroup>
              <h:panelGroup rendered="#{!template.showAssessmentTypes}">
                <jsp:element name="sakai-permissions">
                  <jsp:attribute name="id" trim="false">samigo-permissions</jsp:attribute>
                  <jsp:attribute name="tool" trim="false">assessment</jsp:attribute>
                  <jsp:attribute name="exclude-permissions" trim="false">assessment.template.create,assessment.template.edit.own,assessment.template.delete.own</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </h:panelGroup>
            </div>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
