<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<!DOCTYPE html>
<f:view>
    <html>
        <head><%= request.getAttribute("html.head") %>
            <title><h:outputText value="#{authorMessages.restore_assessments}" /></title>
        </head>
        <body onload="<%= request.getAttribute("html.body.onload") %>">
          <div class="portletBody container-fluid">
            <script>includeWebjarLibrary('datatables');</script>
            <script>
                sakaiDataTables.onReady(function() {
                    const tableElement = document.getElementById("restoreAssessmentsForm:deletedAssessmentsTable");
                    const notEmptyTableTd = tableElement?.querySelector("td:not(:empty)");
                    if (notEmptyTableTd) {
                        const table = sakaiDataTables.init(tableElement, {
                            "paging": true,
                            "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, <h:outputText value="'#{authorFrontDoorMessages.assessment_view_all}'" />]],
                            "pageLength": 20,
                            "order": [[0, "desc"]],
                            "columns": [
                                {"orderable": true, "searchable": true, "type": "span"},
                                {"orderable": true, "searchable": true},
                                {"orderable": true, "searchable": true, "type": "num"},
                                {"orderable": false, "searchable": false}
                            ],
                            "language": {
                                "search": <h:outputText value="'#{dataTablesMessages.search}'" />,
                                "lengthMenu": <h:outputText value="'#{authorFrontDoorMessages.datatables_lengthMenu}'" />,
                                "zeroRecords": <h:outputText value="'#{authorFrontDoorMessages.datatables_zeroRecords}'" />,
                                "info": <h:outputText value="'#{dataTablesMessages.info}'" />,
                                "infoEmpty": <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />,
                                "infoFiltered": <h:outputText value="'#{authorFrontDoorMessages.datatables_infoFiltered}'" />,
                                "emptyTable": <h:outputText value="'#{dataTablesMessages.infoEmpty}'" />,
                                "paginate": {
                                    "next": <h:outputText value="'#{dataTablesMessages.paginate_next}'" />,
                                    "previous": <h:outputText value="'#{dataTablesMessages.paginate_previous}'" />
                                },
                                "aria": {
                                    "sortAscending": <h:outputText value="'#{dataTablesMessages.aria_sortAscending}'" />,
                                    "sortDescending": <h:outputText value="'#{dataTablesMessages.aria_sortDescending}'" />
                                }
                            },
                            "drawCallback": function(oSettings) {
                                document.querySelectorAll(".select-checkbox").forEach(checkbox => checkbox.checked = false);
                                updateRestoreButton();
                            },
                            "stateSave": true,
                            "stateDuration": -1
                        });

                        sakaiDataTables.attachSearch(table, {
                            input: "#restoreAssessmentsForm\\:deletedAssessmentsTable_filter input",
                            tableId: "restoreAssessmentsForm:deletedAssessmentsTable",
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
            </script>

            <div class="samigo-container">
                <h:form id="restoreAssessmentsForm">
                    <%@ include file="/jsf/author/restoreAssessmentsHeading.jsp" %>
                    <div class="page-header">
                        <h1>
                            <h:outputText value="#{authorMessages.restore_assessments}" />
                        </h1>
                    </div>
                    <h:outputText value="#{authorMessages.restore_assessments_empty}" rendered ="#{restoreAssessmentsBean.deletedAssessmentList.isEmpty()}" escape="false" styleClass="sak-banner-info"/>
                    <h:dataTable cellpadding="0" cellspacing="0" id="deletedAssessmentsTable" value="#{restoreAssessmentsBean.deletedAssessmentList}"
                        var="deletedAssessment" styleClass="table table-bordered table-striped" rendered ="#{!restoreAssessmentsBean.deletedAssessmentList.isEmpty()}">

                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="#{authorFrontDoorMessages.assessment_title}" />
                            </f:facet>
                            <f:verbatim><div></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " styleClass="highlight" rendered="#{deletedAssessment.draft}" />
                                <h:outputText styleClass="spanValue" value="#{deletedAssessment.title}" />
                            <f:verbatim></div></f:verbatim>
                        </h:column>
                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="#{eventLogMessages.id}" />
                            </f:facet>
                            <h:outputText value="#{deletedAssessment.id}" />
                        </h:column>

                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="#{authorMessages.restore_assessments_deleted_on}" />
                            </f:facet>
                            <f:verbatim><div></f:verbatim>
                                <h:outputText value="#{deletedAssessment.lastModifiedDate}">
                                    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                                </h:outputText>
                                <h:outputText value="#{deletedAssessment.lastModifiedDate}" styleClass="d-none spanValue">
                                    <f:convertDateTime pattern="yyyyMMddHHmmss" />
                                </h:outputText>
                            <f:verbatim></div></f:verbatim>
                        </h:column>

                        <h:column>
                            <f:facet name="header">
                                <h:outputText value="#{authorMessages.restore_assessments_select}" />
                            </f:facet>
                            <h:selectBooleanCheckbox value="#{deletedAssessment.selected}" styleClass="select-checkbox" />
                        </h:column>
                    </h:dataTable>
                    <div class="clearfix"></div>
                    <p class="act">
                        <h:commandButton id="restore-selected" value="#{authorMessages.restore_assessments_restore}"
                            type="submit" action="#{restoreAssessmentsBean.restoreAssessments}" styleClass="disabled" />
                        <h:commandButton value="#{authorMessages.button_cancel}" type="submit" action="#{restoreAssessmentsBean.cancel}"/>
                    </p>
                </h:form>
            </div>
          </div>
        </body>
    </html>
</f:view>
