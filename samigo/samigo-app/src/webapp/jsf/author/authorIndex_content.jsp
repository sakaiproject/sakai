<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
* $Id$
<%--
**********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
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
    <title><h:outputText value="#{authorFrontDoorMessages.auth_front_door}" /></title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">
    <div class="portletBody container-fluid">

    <script>includeWebjarLibrary('datatables');</script>
    <script>includeWebjarLibrary('bootstrap-multiselect');</script>
    <script src="/samigo-app/js/info.js"></script>
    <script src="/samigo-app/js/naturalSort.js"></script>
    <script type="text/javascript" src="/samigo-app/js/sortHelper.js"></script>
    <script>
        // Function to normalize search text
        window.normalizeSearchText = function(text) {
            return text
                .toLowerCase()
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "");
        };

        $(document).ready(function() {
            const pageLengthStorageKey = `samigo-pageLength-${portal.user.id}`;

            function getPageLength() {
                const pageLength = localStorage.getItem(pageLengthStorageKey);
                return pageLength === null ? 20 : parseInt(pageLength, 10);
            }

            function setPageLength(pageLength) {
                localStorage.setItem(pageLengthStorageKey, pageLength);
            }

            const notEmptyTableTd = $("#authorIndexForm\\:coreAssessments td:not(:empty)").length;
            const assessmentSortingColumn = <h:outputText value="'#{author.assessmentSortingColumn}'"/>;

            if (notEmptyTableTd > 0) {
                $.fn.dataTable.ext.classes.sLengthSelect = 'input-form-control';
                var table = $("#authorIndexForm\\:coreAssessments").DataTable({
                    "paging": true,
                    "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, <h:outputText value="`#{authorFrontDoorMessages.assessment_view_all}`" />]],
                    "pageLength": getPageLength(),
                    "aaSorting": [[parseInt(assessmentSortingColumn), "desc"]],
                    "columns": [
                        {"bSortable": true, "bSearchable": true, "type": "span"},
                        {"bSortable": false, "bSearchable": false},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": true, "type": "numeric"},
                        {"bSortable": true, "bSearchable": true, "type": "numeric"},
                        {"bSortable": true, "bSearchable": false},
                        {"bSortable": true, "bSearchable": true, "type": "numeric"},
                        {"bSortable": false, "bSearchable": false},
                    ],
                    "language": {
                        "search": <h:outputText value="`#{dataTablesMessages.search}`" />,
                        "lengthMenu": <h:outputText value="`#{authorFrontDoorMessages.datatables_lengthMenu}`" />,
                        "zeroRecords": <h:outputText value="`#{authorFrontDoorMessages.datatables_zeroRecords}`" />,
                        "info": <h:outputText value="`#{dataTablesMessages.info}`" />,
                        "infoEmpty": <h:outputText value="`#{authorFrontDoorMessages.datatables_infoEmpty}`" />,
                        "infoFiltered": <h:outputText value="`#{authorFrontDoorMessages.datatables_infoFiltered}`" />,
                        "emptyTable": <h:outputText value="`#{dataTablesMessages.infoEmpty}`" />,
                        "paginate": {
                            "next": <h:outputText value="`#{dataTablesMessages.paginate_next}`" />,
                            "previous": <h:outputText value="`#{dataTablesMessages.paginate_previous}`" />,
                        },
                        "aria": {
                            "sortAscending": <h:outputText value="`#{dataTablesMessages.aria_sortAscending}`" />,
                            "sortDescending": <h:outputText value="`#{dataTablesMessages.aria_sortDescending}`" />,
                        }
                    },
                    "fnDrawCallback": function(oSettings) {
                        $(".select-checkbox").prop("checked", false);
                        updateRemoveButton();
                    }
                });

                const searchInput = document.querySelector('#authorIndexForm\\:coreAssessments_filter input');
                if (table && searchInput) {
                    if (searchInput.hasCustomSearch) {
                        return;
                    }
                    searchInput.hasCustomSearch = true;

                    let lastSearchTerm = '';

                    $(searchInput).off();
                    searchInput.removeAttribute('data-dt-search');

                    const customSearchFunction = function(settings, searchData, index, rowData, counter) {
                        if (settings.nTable.id !== 'authorIndexForm:coreAssessments') {
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

                let spanClassName = "";
                let filterGroups = [];
                function filterBy() {
                    $.fn.dataTableExt.afnFiltering.push(
                        function (oSettings, aData, iDataIndex) {
                            let showBySpan = true;
                            let showByGroups = !<h:outputText value="#{author.groupFilterEnabled}" />;

                            if (spanClassName != "") {
                                showBySpan = (($(oSettings.aoData[iDataIndex].anCells).children("span." + spanClassName).length > 0) ? true : false);
                            }
                            if (filterGroups != null) {
                                for (var i=0; i<filterGroups.length; i++) {
                                    const filter = filterGroups[i];
                                    if (filter.startsWith("releaseto")) {
                                        showByGroups = (($(oSettings.aoData[iDataIndex].anCells).children("." + filterGroups[i]).length > 0) ? true : false);
                                    } else {
                                        showByGroups = (($(oSettings.aoData[iDataIndex].anCells[5]).find(".groupList > li > .hidden:contains('" + filter + "')").length > 0) ? true : false);  
                                    }
                                    if (showByGroups) {
                                        break;
                                    }
                                }
                            }

                            if (showBySpan && showByGroups) {
                                return true;
                            }
                            return false;
                        }
                    );
                    table.draw();
                    $.fn.dataTableExt.afnFiltering.pop();
                }

                table.on('order.dt', function () {
                    $.fn.dataTableExt.afnFiltering.push(
                        function (oSettings, aData, iDataIndex) {
                            if (spanClassName != "") {
                                const spanLength = $(oSettings.aoData[iDataIndex].anCells).children("." + spanClassName).length;
                                if (spanLength > 0) {
                                    return true;
                                }
                                return false;
                            }
                            return true;
                        }
                    );
                    updateRemoveButton();
                });

                table.on('length.dt', function (e, settings, len) {
                    setPageLength(len);
                });

                $("#authorIndexForm\\:filter-type").change(function() {
                    spanClassName = $(this).val();
                    filterBy();
                });

                $("#authorIndexForm\\:group-select").attr("multiple", true);
                $("#authorIndexForm\\:group-select").children("option").each(function() {
                    $(this).prop("selected", true);
                });
                filterGroups = $("#authorIndexForm\\:group-select").val();

                const divElem = document.createElement('div');
                let filterPlaceholder = <h:outputText value="`#{authorFrontDoorMessages.multiselect_filterPlaceholder}`" />;
                divElem.innerHTML = filterPlaceholder;
                filterPlaceholder = divElem.textContent;
                let selectAllText = <h:outputText value="`#{authorFrontDoorMessages.multiselect_selectAllText}`" />;
                divElem.innerHTML = selectAllText;
                selectAllText = divElem.textContent;
                let nonSelectedText = <h:outputText value="`#{authorFrontDoorMessages.multiselect_nonSelectedText}`" />;
                divElem.innerHTML = nonSelectedText;
                nonSelectedText = divElem.textContent;
                let allSelectedText = <h:outputText value="`#{authorFrontDoorMessages.multiselect_allSelectedText}`" />;
                divElem.innerHTML = allSelectedText;
                allSelectedText = divElem.textContent;
                let nSelectedText = <h:outputText value="`#{authorFrontDoorMessages.multiselect_nSelectedText}`" />;
                divElem.innerHTML = nSelectedText;
                nSelectedText = divElem.textContent;
                $("#authorIndexForm\\:group-select").multiselect({
                    enableFiltering: true,
                    enableCaseInsensitiveFiltering: true,
                    includeSelectAllOption: true,
                    filterPlaceholder: filterPlaceholder,
                    selectAllText: selectAllText,
                    nonSelectedText: nonSelectedText,
                    allSelectedText: allSelectedText,
                    nSelectedText: nSelectedText
                });

                $("#authorIndexForm\\:group-select").change(function() {
                    filterGroups = $(this).val();
                    filterBy();
                });
            }

            $("#authorIndexForm\\:coreAssessments").on("change", ".select-checkbox", function() {
                updateRemoveButton();
            });

            function updateRemoveButton() {
                var length = $(".select-checkbox:checked").length;
                if (length > 0) {
                    $("#authorIndexForm\\:remove-selected").removeClass("disabled");
                    $("#authorIndexForm\\:remove-selected").attr("tabindex", 0);
                    $("#authorIndexForm\\:publish-selected").removeClass("disabled");
                    $("#authorIndexForm\\:publish-selected").attr("tabindex", 0);
                } else {
                    $("#authorIndexForm\\:remove-selected").addClass("disabled");
                    $("#authorIndexForm\\:remove-selected").attr("tabindex", -1);
                    $("#authorIndexForm\\:publish-selected").addClass("disabled");
                    $("#authorIndexForm\\:publish-selected").attr("tabindex", -1);
                }
            }
        });

        function removeSelectedButtonAction() {
            if (!$("#authorIndexForm\\:remove-selected").hasClass("disabled")) {
                var message = <h:outputText value="`#{authorMessages.cert_rem_assmt}`" />;
                message += "\n\n";
                message += <h:outputText value="`#{authorMessages.cert_rem_assmt2}`" />;
                var elem = document.createElement('div');
                elem.innerHTML = message;
                if(!confirm(elem.textContent)) {
                    event.preventDefault();
                    return false;
                }
                return true;
            }
        }

        function publishSelectedButtonAction() {

            if (!document.getElementById("authorIndexForm:remove-selected").classList.contains("disabled")) {
                let message = <h:outputText value="`#{authorMessages.cert_pub_assmt}`" />;
                const elem = document.createElement('div');
                elem.innerHTML = message;
                if (!confirm(elem.textContent)) {
                    event.preventDefault();
                    return false;
                }
                return true;
            }
        }

    </script>

    <!-- content... -->
    <h:form id="authorIndexForm">
        <!-- HEADINGS -->
        <%@ include file="/jsf/author/assessmentHeadings.jsp" %>

        <h:panelGroup layout="block" styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}">
            <h:messages rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
        </h:panelGroup>

        <div class="samigo-container">
            <div class="page-header">
                <h1>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_list}"/>
                </h1>
            </div>

            <h:panelGroup rendered="#{author.allAssessments.size() > 0}">
                <div>
                    <f:verbatim><label class="form-label"></f:verbatim>
                        <h:outputText value="#{authorFrontDoorMessages.assessment_view} "/>
                        <h:selectOneMenu value="select" id="filter-type">
                            <f:selectItem itemValue="" itemLabel="#{authorFrontDoorMessages.assessment_view_all}" />
                            <f:selectItem itemValue="status_draft" itemLabel="#{authorFrontDoorMessages.assessment_pending}" />
                            <f:selectItem itemValue="status_published_2" itemLabel="#{authorFrontDoorMessages.assessment_status_active}" />
                            <f:selectItem itemValue="status_published_1" itemLabel="#{authorFrontDoorMessages.assessment_status_pending}" />
                            <f:selectItem itemValue="status_published_0" itemLabel="#{authorFrontDoorMessages.assessment_status_inactive}" />
                        </h:selectOneMenu>
                    <f:verbatim></label></f:verbatim>
                </div>
            </h:panelGroup>

            <h:panelGroup rendered="#{author.groupFilterEnabled and author.allAssessments.size() > 0}">
                <div>
                    <f:verbatim><label></f:verbatim>
                        <h:outputText value="#{authorFrontDoorMessages.filterbygroup} "/>
                        <h:selectOneMenu value="select" id="group-select">
                            <f:selectItem itemValue="releaseto_anon" itemLabel="#{authorFrontDoorMessages.anonymous_users}" />
                            <f:selectItem itemValue="releaseto_entire" itemLabel="#{authorFrontDoorMessages.entire_site}" />
                            <t:selectItems value="#{author.groups.entrySet()}" var="group" itemValue="#{group.key}" itemLabel="#{group.value}" />
                        </h:selectOneMenu>
                    <f:verbatim></label></f:verbatim>
                </div>
            </h:panelGroup>

            <!-- CORE ASSESSMENTS-->
            <h:panelGroup rendered="#{author.allAssessments.size() == 0}">
                <h:outputText value="#{authorFrontDoorMessages.datatables_zeroRecords}" styleClass="sak-banner-info" />
            </h:panelGroup>
            <t:dataTable cellpadding="0" cellspacing="0" rowClasses="list-row-even,list-row-odd" styleClass="table table-hover table-striped table-bordered table-assessments" id="coreAssessments" value="#{author.allAssessments}" var="assessment" rendered="#{author.allAssessments.size() > 0}" summary="#{authorFrontDoorMessages.sum_coreAssessment}">
                <%/* Title */%>
                <t:column headerstyleClass="titlePending" styleClass="titlePending">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_title}" />
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <strong>
                        <h:panelGroup rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}">
                            <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " styleClass="highlight" />
                        </h:panelGroup>

                        <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade' and assessment.status == 3}">
                            <h:outputText styleClass="highlight fa fa-fw fa-exclamation-circle" title="#{authorFrontDoorMessages.retracted_for_edit}" />
                        </h:panelGroup>

                        <h:outputText styleClass="spanValue" value="#{assessment.title}" />
                    </strong>

                </t:column>

                <%/* Action */%>
                <t:column headerstyleClass="titleActions" styleClass="titleActions">
                    <f:facet name="header">
                    </f:facet>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" styleClass="btn-group pull-right">
                        <f:verbatim><button class="btn btn-primary dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="</f:verbatim>
                            <h:outputText value="#{authorMessages.actions_for} " />
                            <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                            <h:outputText value="#{assessment.title}" />
                        <f:verbatim>" aria-label="</f:verbatim>
                            <h:outputText value="#{authorMessages.actions_for} " />
                            <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                            <h:outputText value="#{assessment.title}" />
                        <f:verbatim>"></f:verbatim>
                            <h:outputText value="#{authorMessages.select_action}" />
                            <span class="caret"></span>
                        <f:verbatim></button></f:verbatim>

                        <t:dataList layout="unorderedList" value="#{author.pendingSelectActionList1}" var="pendingSelectActionList" rendered="#{assessment.questionSize > 0 }" styleClass="dropdown-menu row">
                            <h:commandLink id="publishedHiddenlink" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item" action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" >
                                <f:param name="action" value="#{pendingSelectActionList.value}" />
                                <f:param name="assessmentId" value="#{assessment.assessmentBaseId}"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                            </h:commandLink>
                        </t:dataList>

                        <t:dataList layout="unorderedList" value="#{author.pendingSelectActionList2}" var="pendingSelectActionList" rendered="#{assessment.questionSize == 0 }" styleClass="dropdown-menu row">
                            <h:commandLink id="publishedHiddenlink" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item" action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" >
                                <f:param name="action" value="#{pendingSelectActionList.value}" />
                                <f:param name="assessmentId" value="#{assessment.assessmentBaseId}"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                            </h:commandLink>
                        </t:dataList>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}" styleClass="btn-group pull-right">
                        <h:panelGroup rendered="#{(author.isGradeable && assessment.hasAssessmentGradingData) && (author.isEditable && (!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                            <f:verbatim><button class="btn btn-primary btn-xs dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>" aria-label="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>"></f:verbatim>
                                <h:outputText value="#{authorMessages.select_action}" />
                                <span class="caret"></span>
                            <f:verbatim></button></f:verbatim>


                            <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" rowIndexVar="index" styleClass="dropdown-menu row">
                                <li>
                                    <h:commandLink action="#{author.getOutcome}" value="#{authorMessages.action_scores}" styleClass="hiddenBtn_scores dropdown-item" rendered="#{index == 0 && assessment.submittedCount > 0}">
                                        <f:param name="action" value="scores" />
                                        <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                    </h:commandLink>
                                </li>
                                <li>
                                    <h:commandLink action="#{author.getOutcome}" value="#{commonMessages.edit_action}" rendered="#{author.canEditPublishedAssessment(assessment) and index == 0}" styleClass="hiddenBtn_edit_published dropdown-item">
                                        <f:param name="action" value="edit_published" />
                                        <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                    </h:commandLink>
                                </li>
                                <li>
                                    <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item">
                                        <f:param name="action" value="#{pendingSelectActionList.value}" />
                                        <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                                        <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                    </h:commandLink>
                                </li>
                            </t:dataList>
                        </h:panelGroup>

                        <h:panelGroup rendered="#{(author.isGradeable && assessment.hasAssessmentGradingData) && (author.isEditable && !(!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                            <f:verbatim><button class="btn btn-primary btn-xs dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>" aria-label="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>"></f:verbatim>
                                <h:outputText value="#{authorMessages.select_action}" />
                                <span class="caret"></span>
                            <f:verbatim></button></f:verbatim>

                            <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" styleClass="dropdown-menu row" rowIndexVar="index">
                                <h:commandLink action="#{author.getOutcome}" value="#{authorMessages.action_scores}" rendered="#{index == 0 && assessment.submittedCount > 0}" styleClass="hiddenBtn_scores dropdown-item">
                                    <f:param name="action" value="scores" />
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>

                                <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item">
                                    <f:param name="action" value="#{pendingSelectActionList.value}" />
                                    <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>
                            </t:dataList>
                        </h:panelGroup>

                        <h:panelGroup rendered="#{!(author.isGradeable && assessment.hasAssessmentGradingData) && (author.isEditable && (!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                            <f:verbatim><button class="btn btn-primary btn-xs dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>" aria-label="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>"></f:verbatim>
                                <h:outputText value="#{authorMessages.select_action}" />
                                <span class="caret"></span>
                            <f:verbatim></button></f:verbatim>

                            <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" styleClass="dropdown-menu row" rowIndexVar="index">
                                <li>
                                    <h:commandLink action="#{author.getOutcome}" value="#{commonMessages.edit_action}" rendered="#{author.canEditPublishedAssessment(assessment) and index == 0}" styleClass="hiddenBtn_edit_published dropdown-item">
                                        <f:param name="action" value="edit_published" />
                                        <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                    </h:commandLink>
                                </li>
                                <li>
                                    <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item">
                                        <f:param name="action" value="#{pendingSelectActionList.value}" />
                                        <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                                        <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                    </h:commandLink>
                                </li>
                            </t:dataList>
                        </h:panelGroup>
                    </h:panelGroup>
                </t:column>

                <%/* Status */%>
                <t:column headerstyleClass="status" styleClass="status">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_status}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}">
                        <f:verbatim><span class="status_draft"></f:verbatim>
                            <h:outputText styleClass="d-none spanValue" value="-1" />
                            <h:outputText value="#{authorFrontDoorMessages.assessment_status_draft}" />
                        <f:verbatim></span></f:verbatim>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                        <f:verbatim><span class="status_published_</f:verbatim><h:outputText value="#{assessment.activeStatus}" /><f:verbatim>"></f:verbatim>
                        <h:outputText styleClass="d-none spanValue" value="#{assessment.activeStatus}" />
                        <h:outputText value="#{authorFrontDoorMessages.assessment_status_active}" rendered="#{assessment.activeStatus==2}"/>
                        <h:outputText value="#{authorFrontDoorMessages.assessment_status_pending}" rendered="#{assessment.activeStatus==1}"/>
                        <h:outputText value="#{authorFrontDoorMessages.assessment_status_inactive}" rendered="#{assessment.activeStatus==0}"/>
                        <f:verbatim></span></f:verbatim>
                    </h:panelGroup>
                </t:column>

                <%/* In Progress */%>
                <t:column headerstyleClass="inProgress d-none d-sm-table-cell" styleClass="inProgress d-none d-sm-table-cell">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_in_progress}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                        <h:outputText value="#{assessment.inProgressCount}"/>
                    </h:panelGroup>
                </t:column>

                <%/* Submitted */%>
                <t:column headerstyleClass="submitted d-none d-sm-table-cell" styleClass="submitted d-none d-sm-table-cell">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_submitted}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                        <h:panelGroup rendered="#{assessment.submittedCount==0 or !(authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
                            <h:outputText value="#{assessment.submittedCount}"/>
                        </h:panelGroup>

                        <h:panelGroup rendered="#{assessment.submittedCount>0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
                            <f:verbatim><span aria-label="</f:verbatim>
                                <h:outputText value="#{assessment.submittedCount} #{authorFrontDoorMessages.assessment_submissions}" />
                            <f:verbatim>" role="link" tabindex="0"></f:verbatim>
                                <h:commandLink title="#{authorFrontDoorMessages.assessment_submissions}" action="#{author.getOutcome}" immediate="true" id="authorIndexToScore1" tabindex="-1">
                                    <h:outputText value="#{assessment.submittedCount}" />
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}" />
                                    <f:param name="allSubmissionsT" value="3"/>
                                </h:commandLink>
                            <f:verbatim></span></f:verbatim>
                        </h:panelGroup>
                    </h:panelGroup>
                </t:column>

                <%/* Release To */%>
                <t:column headerstyleClass="releaseTo d-none d-sm-table-cell" styleClass="releaseTo d-none d-sm-table-cell">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_release }"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:outputText value="#{authorFrontDoorMessages.anonymous_users}" styleClass="releaseto_anon" rendered="#{assessment.releaseTo eq 'Anonymous Users'}" />
                    <h:outputText value="#{authorFrontDoorMessages.entire_site}" styleClass="releaseto_entire" rendered="#{assessment.releaseTo ne 'Anonymous Users' && assessment.releaseTo ne 'Selected Groups'}" />

                    <t:div rendered="#{assessment.releaseTo eq 'Selected Groups'}">
                        <t:div id="groupsHeader" onclick="#{assessment.groupCount gt 0 ? 'toggleGroups( this );' : ''}" styleClass="#{assessment.groupCount ge 1 ? 'collapsed' : 'sak-banner-error'}">
                            <h:outputText value="#{assessment.groupCount} " rendered ="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount gt 0}" />
                            <h:outputText value="#{authorFrontDoorMessages.selected_groups} " rendered="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount gt 1}"/>
                            <h:outputText value="#{authorFrontDoorMessages.selected_group} " rendered="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount eq 1}"/>
                            <h:outputText value="#{authorFrontDoorMessages.no_selected_groups_error}" rendered="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount eq 0}"/>
                        </t:div>
                        <t:div id="groupsPanel" style="display: none;">
                            <t:dataList layout="unorderedList" value="#{assessment.releaseToGroups.entrySet()}" var="group" styleClass="groupList">
                                <h:outputText value="#{group.value}" />
                                <h:outputText value="#{group.key}" styleClass="d-none" />
                            </t:dataList>
                        </t:div>
                    </t:div>
                </t:column>

                <%/* Release Date */%>
                <t:column headerstyleClass="releaseDate d-none d-sm-table-cell" styleClass="releaseDate d-none d-sm-table-cell">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_date}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <f:verbatim><div></f:verbatim>
                        <h:outputText value="#{assessment.startDate}" >
                            <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                        </h:outputText>

                        <h:outputText value="#{assessment.startDate}" styleClass="d-none spanValue">
                            <f:convertDateTime pattern="yyyyMMddHHmmss" />
                        </h:outputText>
                    <f:verbatim></div></f:verbatim>
                </t:column>

                <%/* Due Date */%>
                <t:column headerstyleClass="dueDate" styleClass="dueDate">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_due}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <f:verbatim><div></f:verbatim>
                        <h:outputText value="#{assessment.dueDate}">
                            <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                        </h:outputText>
                        <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                            <h:outputText value=" #{selectIndexMessages.late} " styleClass="text-danger" rendered="#{assessment.pastDue}" />
                        </h:panelGroup>

                        <h:outputText value="#{assessment.dueDate}" styleClass="d-none spanValue">
                            <f:convertDateTime pattern="yyyyMMddHHmmss" />
                        </h:outputText>
                    <f:verbatim></div></f:verbatim>
                </t:column>

                <%/* Last Modified */%>
                <t:column headerstyleClass="lastModified d-none d-sm-table-cell" styleClass="lastModified d-none d-sm-table-cell">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:outputText value="#{assessment.lastModifiedBy}" />
                </t:column>

                <%/* Modified Date */%>
                <t:column headerstyleClass="lastModifiedDate d-none d-sm-table-cell" styleClass="lastModifiedDate d-none d-sm-table-cell">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" tabindex="-1"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.header_last_modified_date}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <f:verbatim><div></f:verbatim>
                        <h:outputText value="#{assessment.lastModifiedDate}">
                            <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                        </h:outputText>

                        <h:outputText value="#{assessment.lastModifiedDate}" styleClass="d-none spanValue">
                            <f:convertDateTime pattern="yyyyMMddHHmmss" />
                        </h:outputText>
                    <f:verbatim></div></f:verbatim>
                </t:column>

                <%/* Remove */%>
                <t:column rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}">
                    <f:facet name="header">
                        <h:outputText value="#{authorFrontDoorMessages.header_select}" />
                    </f:facet>

                    <h:selectBooleanCheckbox value="#{assessment.selected}" styleClass="select-checkbox" title="#{authorFrontDoorMessages.assessment_select_to_remove}" />
                </t:column>
                <t:column rendered="#{!authorization.deleteAnyAssessment and !authorization.deleteOwnAssessment}" headerstyleClass="d-none" styleClass="d-none">
                </t:column>
            </t:dataTable>

            <div class="clearfix"></div>

            <h:panelGroup rendered="#{author.isAnyAssessmentRetractForEdit == true && author.allAssessments.size() > 0}">
                <f:verbatim><p></f:verbatim>
                    <h:outputText id="assessment-retracted" value="#{authorFrontDoorMessages.retracted_for_edit}" styleClass="sak-banner-red-warn" />
                <f:verbatim></p></f:verbatim>
            </h:panelGroup>
        </div>

        <div class="clearfix"></div>

        <h:commandButton type="button" id="remove-selected" value="#{authorFrontDoorMessages.assessment_remove_selected}" rendered="#{(authorization.deleteAnyAssessment or authorization.deleteOwnAssessment) and author.allAssessments.size() > 0}" styleClass="disabled" onclick="if (!removeSelectedButtonAction(this)) return false;" action="#{author.getOutcome}">
            <f:param name="action" value="remove_selected" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
        </h:commandButton>
        <h:commandButton type="button" id="publish-selected" value="#{authorFrontDoorMessages.assessment_publish_selected}" rendered="#{author.allAssessments.size() > 0}" styleClass="disabled ms-2" onclick="if (!publishSelectedButtonAction(this)) return false;" action="#{author.getOutcome}">
            <f:param name="action" value="publish_selected" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
        </h:commandButton>
    </h:form>
<!-- end content -->
</div>
</body>
</html>
