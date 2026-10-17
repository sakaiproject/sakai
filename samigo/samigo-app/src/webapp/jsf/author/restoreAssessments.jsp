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
          <h:outputText value="#{authorMessages.restore_assessments}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
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
                document.addEventListener('DOMContentLoaded', function() {
                    const tableElement = document.getElementById("restoreAssessmentsForm:deletedAssessmentsTable");
                    const notEmptyTableTd = tableElement?.querySelector("td:not(:empty)");
                    if (notEmptyTableTd) {
                        const table = new DataTable(tableElement, {
                            "paging": true,
                            "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, </jsp:text>
              <h:outputText value="'#{authorFrontDoorMessages.assessment_view_all}'"/>
              <jsp:text>]],
                            "pageLength": 20,
                            "order": [[0, "desc"]],
                            "columns": [
                                {"orderable": true, "searchable": true, "type": "natural-ci", "orderDataType": "dom-span"},
                                {"orderable": true, "searchable": true},
                                {"orderable": true, "searchable": true, "type": "natural-ci", "orderDataType": "dom-span"},
                                {"orderable": false, "searchable": false}
                            ],
                            "language": {
                                "search": </jsp:text>
              <h:outputText value="'#{dataTablesMessages.search}'"/>
              <jsp:text>,
                                "lengthMenu": </jsp:text>
              <h:outputText value="'#{authorFrontDoorMessages.datatables_lengthMenu}'"/>
              <jsp:text>,
                                "zeroRecords": </jsp:text>
              <h:outputText value="'#{authorFrontDoorMessages.datatables_zeroRecords}'"/>
              <jsp:text>,
                                "info": </jsp:text>
              <h:outputText value="'#{dataTablesMessages.info}'"/>
              <jsp:text>,
                                "infoEmpty": </jsp:text>
              <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'"/>
              <jsp:text>,
                                "infoFiltered": </jsp:text>
              <h:outputText value="'#{authorFrontDoorMessages.datatables_infoFiltered}'"/>
              <jsp:text>,
                                "emptyTable": </jsp:text>
              <h:outputText value="'#{dataTablesMessages.infoEmpty}'"/>
              <jsp:text>,
                                "paginate": {
                                    "next": </jsp:text>
              <h:outputText value="'#{dataTablesMessages.paginate_next}'"/>
              <jsp:text>,
                                    "previous": </jsp:text>
              <h:outputText value="'#{dataTablesMessages.paginate_previous}'"/>
              <jsp:text>
                                },
                                "aria": {
                                    "sortAscending": </jsp:text>
              <h:outputText value="'#{dataTablesMessages.aria_sortAscending}'"/>
              <jsp:text>,
                                    "sortDescending": </jsp:text>
              <h:outputText value="'#{dataTablesMessages.aria_sortDescending}'"/>
              <jsp:text><![CDATA[
                                }
                            },
                            "drawCallback": function(oSettings) {
                                document.querySelectorAll(".select-checkbox").forEach(checkbox => checkbox.checked = false);
                                updateRestoreButton();
                            },
                            // Match the entered phrase, including spaces, as before the migration.
                            "search": { "smart": false },
                            "stateLoadParams": function(settings, data) {
                                data.search.smart = false;
                            },
                            "stateSave": true,
                            "stateDuration": -1
                        });

                    }

                    tableElement?.addEventListener("change", event => {
                        if (!event.target.matches(".select-checkbox")) return;
                        updateRestoreButton();
                    });

                    function updateRestoreButton() {
                        const restoreButton = document.getElementById("restoreAssessmentsForm:restore-selected");
                        const hasSelectedAssessments = Boolean(document.querySelector(".select-checkbox:checked"));

                        if (!restoreButton) return;

                        if (hasSelectedAssessments) {
                            restoreButton.classList.remove("disabled");
                            restoreButton.classList.add("active");
                            restoreButton.disabled = false;
                        } else {
                            restoreButton.classList.remove("active");
                            restoreButton.classList.add("disabled");
                            restoreButton.disabled = true;
                        }
                    }
            });
            ]]></jsp:text>
            </script>
            <div class="samigo-container">
              <h:form id="restoreAssessmentsForm">
                <jsp:directive.include file="/jsf/author/restoreAssessmentsHeading.jsp"/>
                <div class="page-header">
                  <h1>
                    <h:outputText value="#{authorMessages.restore_assessments}"/>
                  </h1>
                </div>
                <h:outputText value="#{authorMessages.restore_assessments_empty}" rendered="#{restoreAssessmentsBean.deletedAssessmentList.isEmpty()}" escape="false" styleClass="sak-banner-info"/>
                <h:dataTable cellpadding="0" cellspacing="0" id="deletedAssessmentsTable" value="#{restoreAssessmentsBean.deletedAssessmentList}" var="deletedAssessment" styleClass="table table-bordered table-striped" rendered="#{!restoreAssessmentsBean.deletedAssessmentList.isEmpty()}">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorFrontDoorMessages.assessment_title}"/>
                    </f:facet>
                    <f:verbatim>
                      <jsp:text><![CDATA[<div>]]></jsp:text>
                    </f:verbatim>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " styleClass="highlight" rendered="#{deletedAssessment.draft}"/>
                    <h:outputText styleClass="spanValue" value="#{deletedAssessment.title}"/>
                    <f:verbatim>
                      <jsp:text><![CDATA[</div>]]></jsp:text>
                    </f:verbatim>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{eventLogMessages.id}"/>
                    </f:facet>
                    <h:outputText value="#{deletedAssessment.id}"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.restore_assessments_deleted_on}"/>
                    </f:facet>
                    <f:verbatim>
                      <jsp:text><![CDATA[<div>]]></jsp:text>
                    </f:verbatim>
                    <h:outputText value="#{deletedAssessment.lastModifiedDate}">
                      <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                    </h:outputText>
                    <h:outputText value="#{deletedAssessment.lastModifiedDate}" styleClass="d-none spanValue">
                      <f:convertDateTime pattern="yyyyMMddHHmmss"/>
                    </h:outputText>
                    <f:verbatim>
                      <jsp:text><![CDATA[</div>]]></jsp:text>
                    </f:verbatim>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText value="#{authorMessages.restore_assessments_select}"/>
                    </f:facet>
                    <h:selectBooleanCheckbox value="#{deletedAssessment.selected}" styleClass="select-checkbox"/>
                  </h:column>
                </h:dataTable>
                <jsp:element name="div">
                  <jsp:attribute name="class" trim="false">clearfix</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <p class="act">
                  <h:commandButton id="restore-selected" value="#{authorMessages.restore_assessments_restore}" type="submit" action="#{restoreAssessmentsBean.restoreAssessments}" styleClass="disabled"/>
                  <h:commandButton value="#{authorMessages.button_cancel}" type="submit" action="#{restoreAssessmentsBean.cancel}"/>
                </p>
              </h:form>
            </div>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
