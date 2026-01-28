<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
        <title><h:outputText value="EventLog"/></title>
        <script>includeWebjarLibrary('datatables');</script>
        <script>includeWebjarLibrary('datatables-plugins');</script>
        <script>
          var deletedText = '<h:outputText value="#{eventLogMessages.assessment_deleted}" />';
        </script>
        <script>
          // Function to normalize search text
          window.normalizeSearchText = function(text) {
              return text
                  .toLowerCase()
                  .normalize("NFD")
                  .replace(/[\u0300-\u036f]/g, "");
          };

          $(document).ready(() => {
            const dataTableConfig = JSON.parse('<h:outputText value="#{eventLog.dataTableConfig.json}" />');
            const dataTable = setupDataTable("eventLogId:eventLogTable", dataTableConfig);

            $(document).ready(function() {
                const table = $('#eventLogId\\:eventLogTable').DataTable();
                const searchInput = document.querySelector('#eventLogId\\:eventLogTable_filter input');
                
                if (table && searchInput && !searchInput.hasCustomSearch) {
                    searchInput.hasCustomSearch = true;

                    let lastSearchTerm = '';

                    $(searchInput).off();
                    searchInput.removeAttribute('data-dt-search');

                    const customSearchFunction = function(settings, searchData, index, rowData, counter) {
                        if (settings.nTable.id !== 'eventLogId:eventLogTable') {
                            return true;
                        }

                        if (!lastSearchTerm || lastSearchTerm.trim() === '') {
                            return true;
                        }

                        const normalizedSearch = window.normalizeSearchText(lastSearchTerm);

                        return searchData.some(cellData => {
                            if (cellData && typeof cellData === 'string') {
                                const cleanCellData = cellData.replace(/<[^>]*>/g, '');
                                const normalizedCell = window.normalizeSearchText(cleanCellData);
                                return normalizedCell.includes(normalizedSearch);
                            }
                            return false;
                        });
                    };

                    $.fn.dataTable.ext.search.push(customSearchFunction);

                    const handleSearch = function() {
                        lastSearchTerm = this.value;
                        table.draw();
                    };

                    const handleKeyDown = function(event) {
                        if (event.key === 'Enter') {
                            event.preventDefault();
                        }
                    };

                    searchInput.addEventListener('input', handleSearch);
                    searchInput.addEventListener('keyup', handleSearch);
                    searchInput.addEventListener('keydown', handleKeyDown);

                    if (searchInput.value) {
                        lastSearchTerm = searchInput.value;
                        table.draw();
                    }
                }
            });
          });
        </script>
        <%@ include file="/js/delivery.js" %>
        <script type="text/javascript" src="/samigo-app/js/eventInfo.js"></script>
        <script type="text/javascript" src="/samigo-app/js/sortHelper.js"></script>
        <script type="text/javascript" src="/samigo-app/js/dataTables.js"></script>
      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>;">

<div class="portletBody container-fluid">
  <h:form id="eventLogId">
  <!-- HEADINGS -->
  <%@ include file="/jsf/event/eventLogHeadings.jsp" %>
  
  <h:messages rendered="#{! empty facesContext.maximumSeverity}" infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

  <div class="page-header">
    <h1>
      <h:outputText value="#{eventLogMessages.log}"/>
    </h1>
  </div>

  <h:panelGroup styleClass="d-flex justify-content-between align-items-center flex-wrap gap-1 mb-1" layout="block" rendered="#{not empty eventLog.eventLogDataList}">
    <h:panelGroup styleClass="d-flex flex-wrap flex-sm-nowrap align-items-center" layout="block">
     <h:outputLabel styleClass="text-nowrap" value="#{eventLogMessages.filterBy}" />
     <h:outputText value="&#160;" escape="false" />
     <h:selectOneMenu styleClass="form-select form-select-sm" value="#{eventLog.filteredAssessmentId}" id="assessmentTitle"
         required="true" onchange="document.forms[0].submit();">
        <f:selectItems value="#{eventLog.assessments}"/>
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
     </h:selectOneMenu>
    </h:panelGroup>
    <h:outputLink styleClass="button" value="#{eventLog.exportUrl}">
      <h:outputText value="#{eventLogMessages.export_csv}"/>
    </h:outputLink>
  </h:panelGroup>

 <div class="table">
 <h:dataTable style="display:none;" id="eventLogTable" styleClass="table table-striped table-bordered" value="#{eventLog.eventLogDataList}"
         rendered="#{not empty eventLog.eventLogDataList}" var="log">
  <!-- Assessment Title... -->
    <h:column>
    <f:facet name="header">
        <h:outputText value="#{eventLogMessages.title}"/>
    </f:facet>

   <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
    <h:outputText value="#{log.title}"/>
    <h:outputText value="#{eventLogMessages.assessment_deleted}" rendered="#{eventLog.isDeleted(log.assessmentId)}"/>
   </h:panelGroup>
  </h:column>

  <!-- Assessment ID... -->
  <h:column>
    <f:facet name="header">
        <h:outputText value="#{eventLogMessages.id}"/>
    </f:facet>
    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
      <h:outputText value="#{log.assessmentIdStr}" />
    </h:panelGroup>
  </h:column>

	 <!-- UserID... -->
	 <h:column >
	  <f:facet name="header">
        <h:outputText value="#{eventLogMessages.user_id}"/>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.userDisplay}"/>
     </h:panelGroup>
	</h:column>
	 <!-- Date Started... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText value="#{eventLogMessages.date_startd}"/>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.startDate}">
	    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
	  </h:outputText>
	  <h:outputText value="#{log.startDate}" styleClass="hidden spanValue">
	    <f:convertDateTime pattern="yyyyMMddHHmmss" />
	  </h:outputText>
    </h:panelGroup>
	</h:column>
	<!-- Date Submitted... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText id="eventLogTable" value="#{eventLogMessages.date_submitted}"/>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.endDate}">
	    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
	  </h:outputText>
	  <h:outputText value="#{log.endDate}" styleClass="hidden spanValue">
	    <f:convertDateTime pattern="yyyyMMddHHmmss" />
	  </h:outputText>
     </h:panelGroup>
	</h:column>

	<!-- Duration... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText value="#{eventLogMessages.duration}"/>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.eclipseTime}"/>
	  <h:outputText value=" "/>
	  <h:outputText value="#{eventLogMessages.minutes}" rendered="#{log.eclipseTime > 1}"/>
	   <h:outputText value="#{eventLogMessages.minute}" rendered="#{log.eclipseTime < 2}"/>
     </h:panelGroup>
	</h:column>

	<!-- Errors... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText value="#{eventLogMessages.errors}"/>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	   <h:outputText value="#{log.errorMsg}" rendered="#{!log.isNoErrors}">
	     <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.EventLogConverter" />
	   </h:outputText>
	   <h:outputText value="#{log.errorMsg}" styleClass="text-muted" rendered="#{log.isNoErrors}">
	     <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.EventLogConverter" />
	   </h:outputText>
	 </h:panelGroup>
	</h:column>

	<!-- IP Address -->
	<h:column rendered="#{eventLog.enabledIpAddress}">
	  <f:facet name="header">
        <h:outputText value="#{eventLogMessages.ipAddress}"/>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	    <h:outputText value="#{log.ipAddress}" />
     </h:panelGroup>
	</h:column>

	</h:dataTable>

	<h:panelGroup styleClass="sak-banner-info" rendered="#{empty eventLog.eventLogDataList}" layout="block">
		<h:outputText value="#{eventLogMessages.no_data}"/>
	</h:panelGroup>
</div>
</h:form>
<!-- end content -->
</div>
</body>
</html>
  </f:view>

