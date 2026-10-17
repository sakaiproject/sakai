<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[



<!DOCTYPE html>
  ]]></jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="EventLog"/>
        </title>
        <script>
          <jsp:text>includeWebjarLibrary('datatables');</jsp:text>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/sortHelper.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:text>
          var deletedText = '</jsp:text>
          <h:outputText value="#{eventLogMessages.assessment_deleted}"/>
          <jsp:text>';
        </jsp:text>
        </script>
        <script>
          <jsp:text><![CDATA[
          document.addEventListener('DOMContentLoaded', () => {
            const dataTableConfig = JSON.parse(']]></jsp:text>
          <h:outputText value="#{eventLog.dataTableConfig.json}"/>
          <jsp:text>');
            dataTableConfig.search = { smart: false };
            setupDataTable("eventLogId:eventLogTable", dataTableConfig);
          });
        </jsp:text>
        </script>
        <jsp:directive.include file="/js/delivery.js"/>
        <jsp:element name="script">
          <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
          <jsp:attribute name="src" trim="false">/samigo-app/js/eventInfo.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
          <jsp:attribute name="src" trim="false">/samigo-app/js/dataTables.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression>;</jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <h:form id="eventLogId">
              <!-- HEADINGS  -->
              <jsp:directive.include file="/jsf/event/eventLogHeadings.jsp"/>
              <h:messages rendered="#{! empty facesContext.maximumSeverity}" infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
              <div class="page-header">
                <h1>
                  <h:outputText value="#{eventLogMessages.log}"/>
                </h1>
              </div>
              <h:panelGroup styleClass="d-flex justify-content-between align-items-center flex-wrap gap-1 mb-1" layout="block" rendered="#{not empty eventLog.eventLogDataList}">
                <h:panelGroup styleClass="d-flex flex-wrap flex-sm-nowrap align-items-center" layout="block">
                  <h:outputLabel styleClass="text-nowrap" value="#{eventLogMessages.filterBy}"/>
                  <h:outputText value="&amp;#160;" escape="false"/>
                  <h:selectOneMenu styleClass="form-select form-select-sm" value="#{eventLog.filteredAssessmentId}" id="assessmentTitle" required="true" onchange="document.forms[0].submit();">
                    <f:selectItems value="#{eventLog.assessments}"/>
                    <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener"/>
                  </h:selectOneMenu>
                </h:panelGroup>
                <h:outputLink styleClass="button" value="#{eventLog.exportUrl}">
                  <h:outputText value="#{eventLogMessages.export_csv}"/>
                </h:outputLink>
              </h:panelGroup>
              <div class="table">
                <h:dataTable style="display:none;" id="eventLogTable" styleClass="table table-striped table-bordered" value="#{eventLog.eventLogDataList}" rendered="#{not empty eventLog.eventLogDataList}" var="log">
                  <!-- Assessment Title...  -->
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.title}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.title}"/>
                      <h:outputText value="#{eventLogMessages.assessment_deleted}" rendered="#{eventLog.isDeleted(log.assessmentId)}"/>
                    </h:panelGroup>
                  </h:column>
                  <!-- Assessment ID...  -->
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.id}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.assessmentIdStr}"/>
                    </h:panelGroup>
                  </h:column>
                  <!-- UserID...  -->
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.user_id}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.userDisplay}"/>
                    </h:panelGroup>
                  </h:column>
                  <!-- Date Started...  -->
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.date_startd}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.startDate}">
                        <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                      </h:outputText>
                      <h:outputText value="#{log.startDate}" styleClass="hidden spanValue">
                        <f:convertDateTime pattern="yyyyMMddHHmmss"/>
                      </h:outputText>
                    </h:panelGroup>
                  </h:column>
                  <!-- Date Submitted...  -->
                  <h:column>
                    <f:facet name="header">
                      <h:outputText id="eventLogTable" value="#{eventLogMessages.date_submitted}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.endDate}">
                        <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                      </h:outputText>
                      <h:outputText value="#{log.endDate}" styleClass="hidden spanValue">
                        <f:convertDateTime pattern="yyyyMMddHHmmss"/>
                      </h:outputText>
                    </h:panelGroup>
                  </h:column>
                  <!-- Duration...  -->
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.duration}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.eclipseTime}"/>
                      <h:outputText value=" "/>
                      <h:outputText value="#{eventLogMessages.minutes}" rendered="#{log.eclipseTime &gt; 1}"/>
                      <h:outputText value="#{eventLogMessages.minute}" rendered="#{log.eclipseTime &lt; 2}"/>
                    </h:panelGroup>
                  </h:column>
                  <!-- Errors...  -->
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.errors}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.errorMsg}" rendered="#{!log.isNoErrors}">
                        <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.EventLogConverter"/>
                      </h:outputText>
                      <h:outputText value="#{log.errorMsg}" styleClass="text-muted" rendered="#{log.isNoErrors}">
                        <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.EventLogConverter"/>
                      </h:outputText>
                    </h:panelGroup>
                  </h:column>
                  <!-- IP Address  -->
                  <h:column rendered="#{eventLog.enabledIpAddress}">
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.ipAddress}"/>
                    </f:facet>
                    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
                      <h:outputText value="#{log.ipAddress}"/>
                    </h:panelGroup>
                  </h:column>
                </h:dataTable>
                <h:panelGroup styleClass="sak-banner-info" rendered="#{empty eventLog.eventLogDataList}" layout="block">
                  <h:outputText value="#{eventLogMessages.no_data}"/>
                </h:panelGroup>
              </div>
            </h:form>
            <!-- end content  -->
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
