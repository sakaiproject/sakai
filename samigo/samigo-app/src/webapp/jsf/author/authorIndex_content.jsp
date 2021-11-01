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
    <script>
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

            var notEmptyTableTd = $("#authorIndexForm\\:coreAssessments td:not(:empty)").length;
            var assessmentSortingColumn = <h:outputText value="'#{author.assessmentSortingColumn}'"/>;

            if (notEmptyTableTd > 0) {
                var table = $("#authorIndexForm\\:coreAssessments").DataTable({
                    "paging": true,
                    "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, <h:outputText value="'#{authorFrontDoorMessages.assessment_view_all}'" />]],
                    "pageLength": 20,
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
                        updateRemoveButton();
                    }
                });

                var spanClassName = "";
                var filterGroups = [];
                function filterBy() {
                    $.fn.dataTableExt.afnFiltering.push(
                        function (oSettings, aData, iDataIndex) {
                            var showBySpan = true;
                            var showByGroups = !<h:outputText value="#{author.groupFilterEnabled}" />;

                            if (spanClassName != "") {
                                showBySpan = (($(oSettings.aoData[iDataIndex].anCells).children("span." + spanClassName).length > 0) ? true : false);
                            }
                            if (filterGroups != null) {
                                for (var i=0; i<filterGroups.length; i++) {
                                    var filter = filterGroups[i];
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
                                var spanLength = $(oSettings.aoData[iDataIndex].anCells).children("." + spanClassName).length;
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

                $("#authorIndexForm\\:filter-type").change(function() {
                    spanClassName = $(this).val();
                    filterBy();
                });

                $("#authorIndexForm\\:group-select").attr("multiple", true);
                $("#authorIndexForm\\:group-select").children("option").each(function() {
                    $(this).prop("selected", true);
                });
                filterGroups = $("#authorIndexForm\\:group-select").val();

                var divElem = document.createElement('div');
                var filterPlaceholder = <h:outputText value="'#{authorFrontDoorMessages.multiselect_filterPlaceholder}'" />;
                divElem.innerHTML = filterPlaceholder;
                filterPlaceholder = divElem.textContent;
                var selectAllText = <h:outputText value="'#{authorFrontDoorMessages.multiselect_selectAllText}'" />;
                divElem.innerHTML = selectAllText;
                selectAllText = divElem.textContent;
                var nonSelectedText = <h:outputText value="'#{authorFrontDoorMessages.multiselect_nonSelectedText}'" />;
                divElem.innerHTML = nonSelectedText;
                nonSelectedText = divElem.textContent;
                var allSelectedText = <h:outputText value="'#{authorFrontDoorMessages.multiselect_allSelectedText}'" />;
                divElem.innerHTML = allSelectedText;
                allSelectedText = divElem.textContent;
                var nSelectedText = <h:outputText value="'#{authorFrontDoorMessages.multiselect_nSelectedText}'" />;
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
                } else {
                    $("#authorIndexForm\\:remove-selected").addClass("disabled");
                }
            }
        });

        function removeSelectedButtonAction() {
            if (!$("#authorIndexForm\\:remove-selected").hasClass("disabled")) {
                var message = <h:outputText value="'#{authorMessages.cert_rem_assmt}'" />;
                message += "\n\n";
                message += <h:outputText value="'#{authorMessages.cert_rem_assmt2}'" />;
                var elem = document.createElement('div');
                elem.innerHTML = message;
                if(!confirm(elem.textContent)) {
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
                    <f:verbatim><label></f:verbatim>
                        <h:outputText value="#{authorFrontDoorMessages.assessment_view} "/>
                        <h:selectOneMenu value="select" id="filter-type">
                            <f:selectItem itemValue="" itemLabel="#{authorFrontDoorMessages.assessment_view_all}" />
                            <f:selectItem itemValue="status_draft" itemLabel="#{authorFrontDoorMessages.assessment_pending}" />
                            <f:selectItem itemValue="status_published" itemLabel="#{authorFrontDoorMessages.assessment_pub}" />
                            <f:selectItem itemValue="status_true" itemLabel="#{authorFrontDoorMessages.assessment_status_active}" />
                            <f:selectItem itemValue="status_false" itemLabel="#{authorFrontDoorMessages.assessment_status_inactive}" />
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
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_title}" />
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <strong id="assessmentTitle2">
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
                        <f:verbatim><button class="btn btn-xs dropdown-toggle" aria-expanded="false" data-toggle="dropdown" title="</f:verbatim>
                            <h:outputText value="#{authorMessages.actions_for} " />
                            <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                            <h:outputText value="#{assessment.title}" />
                        <f:verbatim>"></f:verbatim>
                            <h:outputText value="#{authorMessages.select_action}" />
                            <f:verbatim><span class="sr-only"></f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim></span></f:verbatim>
                            <span class="caret"></span>
                        <f:verbatim></button></f:verbatim>

                        <t:dataList layout="unorderedList" value="#{author.pendingSelectActionList1}" var="pendingSelectActionList" rendered="#{assessment.questionSize > 0 }" styleClass="dropdown-menu row">
                            <h:commandLink id="publishedHiddenlink" styleClass="hiddenBtn_#{pendingSelectActionList.value}" action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" >
                                <f:param name="action" value="#{pendingSelectActionList.value}" />
                                <f:param name="assessmentId" value="#{assessment.assessmentBaseId}"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                            </h:commandLink>
                        </t:dataList>

                        <t:dataList layout="unorderedList" value="#{author.pendingSelectActionList2}" var="pendingSelectActionList" rendered="#{assessment.questionSize == 0 }" styleClass="dropdown-menu row">
                            <h:commandLink id="publishedHiddenlink" styleClass="hiddenBtn_#{pendingSelectActionList.value}" action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" >
                                <f:param name="action" value="#{pendingSelectActionList.value}" />
                                <f:param name="assessmentId" value="#{assessment.assessmentBaseId}"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                            </h:commandLink>
                        </t:dataList>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}" styleClass="btn-group pull-right">
                        <h:panelGroup rendered="#{(author.isGradeable && assessment.hasAssessmentGradingData) && (author.isEditable && (!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                            <f:verbatim><button class="btn btn-xs dropdown-toggle" aria-expanded="false" data-toggle="dropdown" title="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>"></f:verbatim>
                                <h:outputText value="#{authorMessages.select_action}" />
                                <f:verbatim><span class="sr-only"></f:verbatim>
                                    <h:outputText value="#{authorMessages.actions_for} " />
                                    <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                    <h:outputText value="#{assessment.title}" />
                                <f:verbatim></span></f:verbatim>
                                <span class="caret"></span>
                            <f:verbatim></button></f:verbatim>


                            <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" rowIndexVar="index" styleClass="dropdown-menu row">
                                <h:commandLink action="#{author.getOutcome}" value="#{authorMessages.action_scores}" styleClass="hiddenBtn_scores" rendered="#{index == 0 && assessment.submittedCount > 0}">
                                    <f:param name="action" value="scores" />
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>

                                <h:commandLink action="#{author.getOutcome}" value="#{commonMessages.edit_action}" rendered="#{author.canEditPublishedAssessment(assessment) and index == 0}" styleClass="hiddenBtn_edit_published">
                                    <f:param name="action" value="edit_published" />
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>

                                <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value}">
                                    <f:param name="action" value="#{pendingSelectActionList.value}" />
                                    <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>
                            </t:dataList>
                        </h:panelGroup>

                        <h:panelGroup rendered="#{(author.isGradeable && assessment.hasAssessmentGradingData) && (author.isEditable && !(!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                            <f:verbatim><button class="btn btn-xs dropdown-toggle" aria-expanded="false" data-toggle="dropdown" title="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>"></f:verbatim>
                                <h:outputText value="#{authorMessages.select_action}" />
                                <f:verbatim><span class="sr-only"></f:verbatim>
                                    <h:outputText value="#{authorMessages.actions_for} " />
                                    <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                    <h:outputText value="#{assessment.title}" />
                                <f:verbatim></span></f:verbatim>
                                <span class="caret"></span>
                            <f:verbatim></button></f:verbatim>

                            <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" styleClass="dropdown-menu row" rowIndexVar="index">
                                <h:commandLink action="#{author.getOutcome}" value="#{authorMessages.action_scores}" rendered="#{index == 0 && assessment.submittedCount > 0}" styleClass="hiddenBtn_scores">
                                    <f:param name="action" value="scores" />
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>

                                <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value}">
                                    <f:param name="action" value="#{pendingSelectActionList.value}" />
                                    <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>
                            </t:dataList>
                        </h:panelGroup>

                        <h:panelGroup rendered="#{!(author.isGradeable && assessment.hasAssessmentGradingData) && (author.isEditable && (!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                            <f:verbatim><button class="btn btn-xs dropdown-toggle" aria-expanded="false" data-toggle="dropdown" title="</f:verbatim>
                                <h:outputText value="#{authorMessages.actions_for} " />
                                <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                <h:outputText value="#{assessment.title}" />
                            <f:verbatim>"></f:verbatim>
                                <h:outputText value="#{authorMessages.select_action}" />
                                <f:verbatim><span class="sr-only"></f:verbatim>
                                    <h:outputText value="#{authorMessages.actions_for} " />
                                    <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" />
                                    <h:outputText value="#{assessment.title}" />
                                <f:verbatim></span></f:verbatim>
                                <span class="caret"></span>
                            <f:verbatim></button></f:verbatim>

                            <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" styleClass="dropdown-menu row" rowIndexVar="index">
                                <h:commandLink action="#{author.getOutcome}" value="#{commonMessages.edit_action}" rendered="#{author.canEditPublishedAssessment(assessment) and index == 0}" styleClass="hiddenBtn_edit_published">
                                    <f:param name="action" value="edit_published" />
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>

                                <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value}">
                                    <f:param name="action" value="#{pendingSelectActionList.value}" />
                                    <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
                                </h:commandLink>
                            </t:dataList>
                        </h:panelGroup>
                    </h:panelGroup>
                </t:column>

                <%/* Status */%>
                <t:column headerstyleClass="status" styleClass="status">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_status}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}">
                        <f:verbatim><span class="status_draft"></f:verbatim>
                            <h:outputText value="#{authorFrontDoorMessages.assessment_draft}" />
                        <f:verbatim></span></f:verbatim>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                        <f:verbatim><span class="status_published status_</f:verbatim><h:outputText value="#{assessment.activeStatus}" /><f:verbatim>"></f:verbatim>
                        <h:outputText value="#{authorFrontDoorMessages.assessment_status_active}" rendered="#{assessment.activeStatus==true}"/>
                        <h:outputText value="#{authorFrontDoorMessages.assessment_status_inactive}" rendered="#{assessment.activeStatus==false}"/>
                        <f:verbatim></span></f:verbatim>
                    </h:panelGroup>
                </t:column>

                <%/* In Progress */%>
                <t:column headerstyleClass="inProgress hidden-xs hidden-sm" styleClass="inProgress hidden-xs hidden-sm">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_in_progress}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                        <h:outputText value="#{assessment.inProgressCount}"/>
                    </h:panelGroup>
                </t:column>

                <%/* Submitted */%>
                <t:column headerstyleClass="submitted hidden-xs hidden-sm" styleClass="submitted hidden-xs hidden-sm">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_submitted}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                        <h:panelGroup rendered="#{assessment.submittedCount==0 or !(authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
                            <h:outputText value="#{assessment.submittedCount}"/>
                        </h:panelGroup>

                        <h:panelGroup rendered="#{assessment.submittedCount>0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
                            <h:commandLink title="#{authorFrontDoorMessages.t_score}" action="#{author.getOutcome}" immediate="true" id="authorIndexToScore1" >
                                <h:outputText value="#{assessment.submittedCount}" />
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
                                <f:param name="publishedId" value="#{assessment.publishedAssessmentId}" />
                                <f:param name="allSubmissionsT" value="3"/>
                            </h:commandLink>
                        </h:panelGroup>
                    </h:panelGroup>
                </t:column>

                <%/* Release To */%>
                <t:column headerstyleClass="releaseTo hidden-xs hidden-sm" styleClass="releaseTo hidden-xs hidden-sm">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
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
                                <h:outputText value="#{group.key}" styleClass="hidden" />
                            </t:dataList>
                        </t:div>
                    </t:div>
                </t:column>

                <%/* Release Date */%>
                <t:column headerstyleClass="releaseDate hidden-xs hidden-sm" styleClass="releaseDate hidden-xs hidden-sm">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_date}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:outputText value="#{assessment.startDate}" >
                        <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                    </h:outputText>

                    <h:outputText value="#{assessment.startDate}" styleClass="hidden spanValue">
                        <f:convertDateTime pattern="yyyyMMddHHmmss" />
                    </h:outputText>
                </t:column>

                <%/* Due Date */%>
                <t:column headerstyleClass="dueDate" styleClass="dueDate">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.assessment_due}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:outputText value="#{assessment.dueDate}">
                        <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                    </h:outputText>
                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                        <h:outputText value=" #{selectIndexMessages.late} " styleClass="text-danger" rendered="#{assessment.pastDue}" />
                    </h:panelGroup>

                    <h:outputText value="#{assessment.dueDate}" styleClass="hidden spanValue">
                        <f:convertDateTime pattern="yyyyMMddHHmmss" />
                    </h:outputText>
                </t:column>

                <%/* Last Modified */%>
                <t:column headerstyleClass="lastModified hidden-xs hidden-sm" styleClass="lastModified hidden-xs hidden-sm">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:outputText value="#{assessment.lastModifiedBy}" />
                </t:column>

                <%/* Modified Date */%>
                <t:column headerstyleClass="lastModifiedDate hidden-xs hidden-sm" styleClass="lastModifiedDate hidden-xs hidden-sm">
                    <f:facet name="header">
                        <h:panelGroup>
                            <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                <h:outputText value="#{authorFrontDoorMessages.header_last_modified_date}"/>
                            <f:verbatim></a></f:verbatim>
                        </h:panelGroup>
                    </f:facet>

                    <h:outputText value="#{assessment.lastModifiedDate}">
                        <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
                    </h:outputText>

                    <h:outputText value="#{assessment.lastModifiedDate}" styleClass="hidden spanValue">
                        <f:convertDateTime pattern="yyyyMMddHHmmss" />
                    </h:outputText>
                </t:column>

                <%/* Remove */%>
                <t:column rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}">
                    <f:facet name="header">
                        <h:outputText value="#{authorFrontDoorMessages.header_remove}" />
                    </f:facet>

                    <h:selectBooleanCheckbox value="#{assessment.selected}" styleClass="select-checkbox" />
                </t:column>
                <t:column rendered="#{!authorization.deleteAnyAssessment and !authorization.deleteOwnAssessment}" headerstyleClass="hidden" styleClass="hidden">
                </t:column>
            </t:dataTable>

            <div class="clearfix"></div>

            <h:panelGroup rendered="#{author.isAnyAssessmentRetractForEdit == true && author.allAssessments.size() > 0}">
                <f:verbatim><p></f:verbatim>
                    <h:outputText styleClass="highlight fa fa-fw fa-exclamation-circle" />
                    <h:outputText id="assessment-retracted" value="#{authorFrontDoorMessages.retracted_for_edit}" styleClass="highlight" />
                <f:verbatim></p></f:verbatim>
            </h:panelGroup>
        </div>

        <div class="clearfix"></div>

        <h:commandLink type="submit" id="remove-selected" value="#{authorFrontDoorMessages.assessment_remove_selected}" rendered="#{(authorization.deleteAnyAssessment or authorization.deleteOwnAssessment) and author.allAssessments.size() > 0}" styleClass="disabled" onclick="if (!removeSelectedButtonAction(this)) return false;" action="#{author.getOutcome}">
            <f:param name="action" value="remove_selected" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
        </h:commandLink>
    </h:form>
<!-- end content -->
</div>
</body>
</html>
