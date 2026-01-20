<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head><%= request.getAttribute("html.head") %>
<title><h:outputText value="#{selectIndexMessages.page_title}" /></title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">
    <!-- IF A SECURE DELIVERY MODULES ARE AVAILABLE, INJECT THEIR INITIAL HTML FRAGMENTS HERE -->
    <h:outputText  value="#{select.secureDeliveryHTMLFragments}" escape="false" />

    <!--JAVASCRIPT -->
    <script>includeWebjarLibrary('datatables');</script>
    <script src="/samigo-app/js/naturalSort.js"></script>
    <script>
        // Function to normalize search text
        window.normalizeSearchText = function(text) {
            return text
                .toLowerCase()
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "");
        };

        $(document).ready(function() {
            jQuery.extend(jQuery.fn.dataTableExt.oSort, {
                "span-asc": function (a, b) {
                    return naturalSort($(a).find(".spanValue").text().toLowerCase(), $(b).find(".spanValue").text().toLowerCase(), false);
                },
                "span-desc": function (a, b) {
                    return naturalSort($(a).find(".spanValue").text().toLowerCase(), $(b).find(".spanValue").text().toLowerCase(), false) * -1;
                },
                "numeric-asc": function (a, b) {
                    var numA = parseInt($(a).text()) || 0;
                    var numB = parseInt($(b).text()) || 0;
                    return ((numB < numA) ? 1 : ((numB > numA) ? -1 : 0));
                },
                "numeric-desc": function (a, b) {
                    var numA = parseInt($(a).text()) || 0;
                    var numB = parseInt($(b).text()) || 0;
                    return ((numA < numB) ? 1 : ((numA > numB) ? -1 : 0));
                }
            });

            var viewAllText = <h:outputText value="'#{authorFrontDoorMessages.assessment_view_all}'" />;
            var searchText = <h:outputText value="'#{dataTablesMessages.search}'" />;
            var lengthMenuText = <h:outputText value="'#{authorFrontDoorMessages.datatables_lengthMenu}'" />;
            var zeroRecordsText = <h:outputText value="'#{authorFrontDoorMessages.datatables_zeroRecords}'" />;
            var infoText = <h:outputText value="'#{dataTablesMessages.info}'" />;
            var infoEmptyText = <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />;
            var infoFilteredText = <h:outputText value="'#{authorFrontDoorMessages.datatables_infoFiltered}'" />;
            var emptyTableText = <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />;
            var nextText = <h:outputText value="'#{dataTablesMessages.paginate_next}'" />;
            var previousText = <h:outputText value="'#{dataTablesMessages.paginate_previous}'" />;
            var sortAscendingText = <h:outputText value="'#{dataTablesMessages.aria_sortAscending}'" />;
            var sortDescendingText = <h:outputText value="'#{dataTablesMessages.aria_sortDescending}'" />;

            var notEmptySelectTableTd = $("#selectIndexForm\\:selectTable td:not(:empty)").length;
            if (notEmptySelectTableTd > 0) {
              $.fn.dataTable.ext.classes.sLengthSelect = 'input-form-control';
              var table = $("#selectIndexForm\\:selectTable").DataTable({
                    "paging": true,
                    "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, viewAllText]],
                    "pageLength": 20,
                    "aaSorting": [[2, "asc"]],
                    "columns": [
                        {"bSortable": true, "bSearchable": true, "type": "span"},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": true, "type": "numeric"},
                    ],
                    "language": {
                        "search": searchText,
                        "lengthMenu": lengthMenuText,
                        "zeroRecords": zeroRecordsText,
                        "info": infoText,
                        "infoEmpty": infoEmptyText,
                        "infoFiltered": infoFilteredText,
                        "emptyTable": emptyTableText,
                        "paginate": {
                            "next": nextText,
                            "previous": previousText,
                        },
                        "aria": {
                            "sortAscending": sortAscendingText,
                            "sortDescending": sortDescendingText,
                        }
                    }
              });

              $(document).ready(function() {
                  const table = $('#selectIndexForm\\:selectTable').DataTable();
                  const searchInput = document.querySelector('#selectIndexForm\\:selectTable_filter input');
                  
                  if (table && searchInput && !searchInput.hasCustomSearch) {
                      searchInput.hasCustomSearch = true;

                      let lastSearchTerm = '';

                      $(searchInput).off();
                      searchInput.removeAttribute('data-dt-search');

                      const customSearchFunction = function(settings, searchData, index, rowData, counter) {
                          if (settings.nTable.id !== 'selectIndexForm:selectTable') {
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

                      searchInput.addEventListener('input', handleSearch);
                      searchInput.addEventListener('keyup', handleSearch);

                      if (searchInput.value) {
                          lastSearchTerm = searchInput.value;
                          table.draw();
                      }
                  }
              });
            }

            var notEmptyReviewTableTd = $("#selectIndexForm\\:reviewTable td:not(:empty)").length;
            if (notEmptyReviewTableTd > 0) {
              if ($("#selectIndexForm\\:reviewTable .displayAllAssessments").length > 0) {
                var table = $("#selectIndexForm\\:reviewTable").DataTable({
                    "paging": true,
                    "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, viewAllText]],
                    "pageLength": 20,
                    "aaSorting": [[6, "desc"]],
                    "paging": false,
                    "ordering": false,
                    "info": false,
                    "columns": [
                        {"bSortable": true, "bSearchable": true},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": true},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": true}
                    ],
                    "language": {
                        "search": searchText,
                        "lengthMenu": lengthMenuText,
                        "zeroRecords": zeroRecordsText,
                        "info": infoText,
                        "infoEmpty": infoEmptyText,
                        "infoFiltered": infoFilteredText,
                        "emptyTable": emptyTableText,
                        "paginate": {
                            "next": nextText,
                            "previous": previousText,
                        },
                        "aria": {
                            "sortAscending": sortAscendingText,
                            "sortDescending": sortDescendingText,
                        }
                    }
                });

                const searchInput = document.querySelector('#selectIndexForm\\:reviewTable_filter input');
                if (table && searchInput) {
                    if (searchInput.hasCustomSearch) {
                        return;
                    }
                    searchInput.hasCustomSearch = true;

                    let lastSearchTerm = '';

                    $(searchInput).off();
                    searchInput.removeAttribute('data-dt-search');

                    const customSearchFunction = function(settings, searchData, index, rowData, counter) {
                        if (settings.nTable.id !== 'selectIndexForm:reviewTable') {
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

                    searchInput.addEventListener('input', handleSearch);
                    searchInput.addEventListener('keyup', handleSearch);

                    if (searchInput.value) {
                        lastSearchTerm = searchInput.value;
                        table.draw();
                    }
                }

              } else {
                var table = $("#selectIndexForm\\:reviewTable").DataTable({
                    "paging": true,
                    "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, viewAllText]],
                    "pageLength": 20,
                    "paging": false,
                    "ordering": false,
                    "info": false,
                    "columns": [
                        {"bSortable": true, "bSearchable": true},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": true},
                    ],
                    "language": {
                        "search": searchText,
                        "lengthMenu": lengthMenuText,
                        "zeroRecords": zeroRecordsText,
                        "info": infoText,
                        "infoEmpty": infoEmptyText,
                        "infoFiltered": infoFilteredText,
                        "emptyTable": emptyTableText,
                        "paginate": {
                            "next": nextText,
                            "previous": previousText,
                        },
                        "aria": {
                            "sortAscending": sortAscendingText,
                            "sortDescending": sortDescendingText,
                        }
                    }
                });

                const searchInput2 = document.querySelector('#selectIndexForm\\:reviewTable_filter input');
                if (table && searchInput2) {
                    if (searchInput2.hasCustomSearch) {
                        return;
                    }
                    searchInput2.hasCustomSearch = true;

                    let lastSearchTerm2 = '';

                    $(searchInput2).off();
                    searchInput2.removeAttribute('data-dt-search');

                    const customSearchFunction2 = function(settings, searchData, index, rowData, counter) {
                        if (settings.nTable.id !== 'selectIndexForm:reviewTable') {
                            return true;
                        }

                        if (!lastSearchTerm2 || lastSearchTerm2.trim() === '') {
                            return true;
                        }

                        const normalizedSearch2 = window.normalizeSearchText(lastSearchTerm2);

                        return searchData.some(cellData => {
                            if (cellData && typeof cellData === 'string') {
                                const cleanCellData = cellData.replace(/<[^>]*>/g, '');
                                const normalizedCell = window.normalizeSearchText(cleanCellData);
                                return normalizedCell.includes(normalizedSearch2);
                            }
                            return false;
                        });
                    };

                    $.fn.dataTable.ext.search.push(customSearchFunction2);

                    const handleSearch2 = function() {
                        lastSearchTerm2 = this.value;
                        table.draw();
                    };

                    searchInput2.addEventListener('input', handleSearch2);
                    searchInput2.addEventListener('keyup', handleSearch2);

                    if (searchInput2.value) {
                        lastSearchTerm2 = searchInput2.value;
                        table.draw();
                    }
                }
              }
            }
        });
    </script>

    <!-- content... -->
    <div class="portletBody container-fluid">
        <div class="page-header">
            <h1>
                <h:outputText value="#{selectIndexMessages.page_heading}"/>
            </h1>
        </div>

        <h:form id="selectIndexForm">
            <!-- SELECT -->
            <div class="submission-container">
                <h2>
                    <h:outputText value="#{selectIndexMessages.take_assessment}" />
                </h2>

                <p class="info-text">
                    <h:outputText rendered="#{select.isThereAssessmentToTake eq 'true'}" value="#{selectIndexMessages.take_assessment_notes}" />
                    <h:outputText rendered="#{select.isThereAssessmentToTake eq 'false'}" value="#{selectIndexMessages.take_assessment_notAvailable}" />
                </p>

                <!-- SELECT TABLE -->
                <t:dataTable id="selectTable" rendered="#{select.isThereAssessmentToTake eq 'true'}" value="#{select.takeableAssessments}" var="takeable" styleClass="table table-hover table-striped table-bordered table-assessments" summary="#{selectIndexMessages.sum_availableAssessment}">
                    <t:column headerstyleClass="assessmentTitleHeader">
                        <f:facet name="header">
                            <h:panelGroup>
                                <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                    <h:outputText value="#{selectIndexMessages.title}" />
                                <f:verbatim></a></f:verbatim>
                            </h:panelGroup>
                        </f:facet>

                        <h:panelGroup rendered="#{not empty takeable.alternativeDeliveryUrl}">
                            <a href="<h:outputText value="#{takeable.alternativeDeliveryUrl}" escape="false" />" title="Proctored Assessment Link">
                              <span class="fa fa-user-circle-o" title="Proctored Assessment Link"></span>
                              <h:outputText value="#{takeable.assessmentTitle}" escape="false" />
                            </a>
                        </h:panelGroup>

                        <h:commandLink title="#{selectIndexMessages.t_takeAssessment}" id="takeAssessment" action="beginAssessment" rendered="#{empty takeable.alternativeDeliveryUrl}">
                            <f:param name="publishedId" value="#{takeable.assessmentId}" />
                            <f:param name="actionString" value="takeAssessment"/>
                            <f:actionListener
                               type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
                            <h:outputText value="#{takeable.assessmentTitle}" escape="false" styleClass="spanValue" />
                        </h:commandLink>
                        <h:outputText value="#{selectIndexMessages.assessment_updated_need_resubmit}" rendered="#{takeable.assessmentUpdatedNeedResubmit}" styleClass="validate" />
                        <h:outputText value="#{selectIndexMessages.assessment_updated}" rendered="#{takeable.assessmentUpdated}" styleClass="validate" />
                    </t:column>

                    <t:column headerstyleClass="assessmentTimeLimitHeader">
                        <f:facet name="header">
                            <h:panelGroup>
                                <f:verbatim><a href="#" tabindex="-1""></f:verbatim>
                                    <h:outputText value="#{selectIndexMessages.t_time_limit}" />
                                <f:verbatim></a></f:verbatim>
                            </h:panelGroup>
                        </f:facet>

                        <h:outputText value="#{takeable.timeLimit_hour} #{selectIndexMessages.hour} #{takeable.timeLimit_minute} #{selectIndexMessages.minutes}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour != 0 && takeable.timeLimit_minute != 0}"  escape="false"/>
                        <h:outputText value="#{takeable.timeLimit_hour} #{selectIndexMessages.hour}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour != 0 && takeable.timeLimit_minute == 0}"  escape="false"/>
                        <h:outputText value="#{takeable.timeLimit_minute} #{selectIndexMessages.minutes}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour == 0 && takeable.timeLimit_minute != 0}"  escape="false"/>
                        <h:outputText value="#{selectIndexMessages.na}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour == 0 && takeable.timeLimit_minute == 0}"  escape="false"/>
                    </t:column>

                    <t:column headerstyleClass="assessmentDueDateHeader">
                        <f:facet name="header">
                            <h:panelGroup>
                                <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                    <h:outputText value="#{selectIndexMessages.date_due}" />
                                <f:verbatim></a></f:verbatim>
                            </h:panelGroup>
                        </f:facet>
                        <h:outputText value="#{selectIndexMessages.na}" rendered="#{takeable.dueDate == null}" />
                        <h:outputText value="#{takeable.dueDate}" styleClass="text-danger" rendered="#{takeable.pastDue}">
                            <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                        </h:outputText>
                        <h:outputText value=" #{selectIndexMessages.late} " styleClass="text-danger" rendered="#{takeable.pastDue}" />
                        <h:outputText value="#{takeable.dueDate}" rendered="#{!takeable.pastDue}">
                            <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                        </h:outputText>
                        <h:outputText value="#{takeable.dueDate}" styleClass="d-none spanValue">
                            <f:convertDateTime pattern="yyyyMMddHHmmss" />
                        </h:outputText>
                    </t:column>
                </t:dataTable>
            </div>

            <div class="clearfix"></div>

            <!-- SUBMITTED ASSESMENTS -->
            <h2>
                <h:outputText value="#{selectIndexMessages.submitted_assessments}" />
            </h2>
            <div class="info-text">
                <h:outputText rendered="#{select.isThereAssessmentToReview eq 'true'}" value="#{selectIndexMessages.review_assessment_notes}" />
                <h:outputText rendered="#{select.isThereAssessmentToReview ne 'true'}" value="#{selectIndexMessages.review_assessment_notAvailable}" />
            </div>

            <t:div rendered="#{select.isThereAssessmentToReview eq 'true'}" styleClass="panel panel-default sam-submittedPanel">
                <t:div rendered="#{select.displayAllAssessments == 2}" styleClass="panel-heading sam-reviewHeaderTabs"> <%-- on the all submissions/score tab --%>
                    <span><h:outputText value="#{selectIndexMessages.review_assessment_all}" rendered="#{select.displayAllAssessments == 2}" /></span>
                    <h:commandLink
                        id="some"
                        value="#{selectIndexMessages.review_assessment_recorded}"
                        rendered="#{select.displayAllAssessments == 2}" styleClass="sam-leftSep">
                        <f:param name="selectSubmissions" value="1" />
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
                    </h:commandLink>
                </t:div>

                <t:div rendered="#{select.displayAllAssessments == 1}" styleClass="panel-heading sam-reviewHeaderTabs"> <%-- on the only recorded scores tab --%>
                    <h:commandLink
                        id="all"
                        value="#{selectIndexMessages.review_assessment_all}" rendered="#{select.displayAllAssessments == 1}">
                        <f:param name="selectSubmissions" value="2" />
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
                    </h:commandLink>
                    <span class="sam-leftSep"><h:outputText value="#{selectIndexMessages.review_assessment_recorded}" rendered="#{select.displayAllAssessments == 1}" /></span>
                </t:div>

                <%-- Include REVIEW TABLE --%>
			    <%@ include file="./selectIndex_review_table.jsp"%>

            </t:div>
        </h:form>
    </div>
    <!-- end content -->
</body>
</html>
