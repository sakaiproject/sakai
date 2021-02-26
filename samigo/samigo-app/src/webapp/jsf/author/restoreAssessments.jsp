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
                $(document).ready(function() {
                    var notEmptyTableTd = $("#restoreAssessmentsForm\\:deletedAssessmentsTable td:not(:empty)").length;
                    if (notEmptyTableTd > 0) {
                        var table = $("#restoreAssessmentsForm\\:deletedAssessmentsTable").DataTable({
                            "paging": true,
                            "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, <h:outputText value="'#{authorFrontDoorMessages.assessment_view_all}'" />]],
                            "pageLength": 20,
                            "aaSorting": [[0, "desc"]],
                            "columns": [
                                {"bSortable": true, "bSearchable": true, "type": "span"},
                                {"bSortable": true, "bSearchable": true},
                                {"bSortable": true, "bSearchable": true},
                                {"bSortable": false, "bSearchable": false},
                            ],
                            "language": {
                                "search": <h:outputText value="'#{authorFrontDoorMessages.datatables_sSearch}'" />,
                                "lengthMenu": <h:outputText value="'#{authorFrontDoorMessages.datatables_lengthMenu}'" />,
                                "zeroRecords": <h:outputText value="'#{authorFrontDoorMessages.datatables_zeroRecords}'" />,
                                "info": <h:outputText value="'#{authorFrontDoorMessages.datatables_info}'" />,
                                "infoEmpty": <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />,
                                "infoFiltered": <h:outputText value="'#{authorFrontDoorMessages.datatables_infoFiltered}'" />,
                                "emptyTable": <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />,
                                "paginate": {
                                    "next": <h:outputText value="'#{authorFrontDoorMessages.datatables_paginate_next}'" />,
                                    "previous": <h:outputText value="'#{authorFrontDoorMessages.datatables_paginate_previous}'" />,
                                },
                                "aria": {
                                    "sortAscending": <h:outputText value="'#{authorFrontDoorMessages.datatables_aria_sortAscending}'" />,
                                    "sortDescending": <h:outputText value="'#{authorFrontDoorMessages.datatables_aria_sortDescending}'" />,
                                }
                            },
                            "fnDrawCallback": function(oSettings) {
                                $(".select-checkbox").prop("checked", false);
                                updateRestoreButton();
                            }
                        });
                    }

                    $("#restoreAssessmentsForm\\:deletedAssessmentsTable").on("change", ".select-checkbox", function() {
                        updateRestoreButton();
                    });

                    function updateRestoreButton() {
                        var length = $(".select-checkbox:checked").length;
                        var restoreButton = $("#restoreAssessmentsForm\\:restore-selected");
                        if (length > 0) {
                            restoreButton.removeClass("disabled");
                            restoreButton.addClass("active");
                            restoreButton.prop('disabled', false);
                        } else {
                            restoreButton.removeClass("active");
                            restoreButton.addClass("disabled");
                            restoreButton.prop('disabled', true);
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
                            <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " styleClass="highlight" rendered="#{deletedAssessment.draft}" />
                            <h:outputText value="#{deletedAssessment.title}" />
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
                            <h:outputText value="#{deletedAssessment.lastModifiedDate}">
                                <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                            </h:outputText>
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
