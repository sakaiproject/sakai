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
    <script src="/library/webjars/datatables/1.10.16/js/jquery.dataTables.min.js"></script>
    <samigo:script path="/js/naturalSort.js"/>
    <script type="text/JavaScript">
        $(document).ready(function() {
            jQuery.extend(jQuery.fn.dataTableExt.oSort, {
                "span-asc": function (a, b) {
                    return naturalSort($(a).find(".spanValue").text().toLowerCase(), $(b).find(".spanValue").text().toLowerCase(), false);
                },
                "span-desc": function (a, b) {
                    return naturalSort($(a).find(".spanValue").text().toLowerCase(), $(b).find(".spanValue").text().toLowerCase(), false) * -1;
                }
            });

            var viewAllText = <h:outputText value="'#{authorFrontDoorMessages.assessment_view_all}'" />;
            var searchText = <h:outputText value="'#{authorFrontDoorMessages.datatables_sSearch}'" />;
            var lengthMenuText = <h:outputText value="'#{authorFrontDoorMessages.datatables_lengthMenu}'" />;
            var zeroRecordsText = <h:outputText value="'#{authorFrontDoorMessages.datatables_zeroRecords}'" />;
            var infoText = <h:outputText value="'#{authorFrontDoorMessages.datatables_info}'" />;
            var infoEmptyText = <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />;
            var infoFilteredText = <h:outputText value="'#{authorFrontDoorMessages.datatables_infoFiltered}'" />;
            var emptyTableText = <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />;
            var nextText = <h:outputText value="'#{authorFrontDoorMessages.datatables_paginate_next}'" />;
            var previousText = <h:outputText value="'#{authorFrontDoorMessages.datatables_paginate_previous}'" />;
            var sortAscendingText = <h:outputText value="'#{authorFrontDoorMessages.datatables_aria_sortAscending}'" />;
            var sortDescendingText = <h:outputText value="'#{authorFrontDoorMessages.datatables_aria_sortDescending}'" />;

            var notEmptySelectTableTd = $("#selectIndexForm\\:selectTable td:not(:empty)").length;
            if (notEmptySelectTableTd > 0) {
              var table = $("#selectIndexForm\\:selectTable").DataTable({
                    "paging": true,
                    "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, viewAllText]],
                    "pageLength": 20,
                    "aaSorting": [[2, "desc"]],
                    "columns": [
                        {"bSortable": true, "bSearchable": true, "type": "span"},
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
                                <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                    <h:outputText value="#{selectIndexMessages.title}" />
                                <f:verbatim></a></f:verbatim>
                            </h:panelGroup>
                        </f:facet>

                        <h:commandLink title="#{selectIndexMessages.t_takeAssessment}" id="takeAssessment" action="beginAssessment">
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
                                <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
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
                                <f:verbatim><a href="#" onclick="return false;"></f:verbatim>
                                    <h:outputText value="#{selectIndexMessages.date_due}" />
                                <f:verbatim></a></f:verbatim>
                            </h:panelGroup>
                        </f:facet>
                        <h:outputText value="#{selectIndexMessages.na}" rendered="#{takeable.dueDate == null}" />
                        <h:outputText value="#{takeable.dueDateString}" style="color: red;" rendered="#{takeable.pastDue}" />
                        <h:outputText value="#{takeable.dueDateString}" rendered="#{!takeable.pastDue}" />
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

                <!-- REVIEW TABLE -->
                <div class="table-responsive table-sent-assessments">
                    <t:dataTable styleClass="table table-hover table-striped table-bordered table-assessments" id="reviewTable" value="#{select.reviewableAssessments}" var="reviewable" summary="#{selectIndexMessages.sum_submittedAssessment}">
                        <%-- TITLE --%>
                        <t:column>
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{selectIndexMessages.title} " styleClass="currentSort" />
                                    <h:panelGroup rendered="#{select.displayAllAssessments != '1'}">
                                        <h:outputText value="" styleClass="displayAllAssessments hidden" />
                                    </h:panelGroup>
                                </h:panelGroup>
                            </f:facet>
                            <h:outputText value="#{reviewable.assessmentTitle}" rendered="#{!reviewable.isRecordedAssessment}" styleClass="hidden" />
                            <h:outputText styleClass="highlight fa fa-fw fa-exclamation-circle" rendered="#{reviewable.isRecordedAssessment && reviewable.feedback == 'show' && !reviewable.isAssessmentRetractForEdit && reviewable.hasAssessmentBeenModified && select.warnUserOfModification}" title="#{selectIndexMessages.has_been_modified}" />
                            <h:outputText styleClass="highlight fa fa-fw fa-exclamation" rendered="#{reviewable.isRecordedAssessment && reviewable.isAssessmentRetractForEdit}" title="#{selectIndexMessages.currently_being_edited}" />
                            <h:outputText value="#{reviewable.assessmentTitle}" styleClass="currentSort"  rendered="#{reviewable.isRecordedAssessment}"  escape="false"/>
                        </t:column>

                        <!-- STATS creating separate column for stats -->
                        <t:column>
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{selectIndexMessages.stats}" styleClass="currentSort"  />
                                </h:panelGroup>
                            </f:facet>
                            <h:panelGroup>
                                <h:commandLink title="#{selectIndexMessages.t_histogram}" id="histogram"  action="#{delivery.getOutcome}" immediate="true"
                                    rendered="#{reviewable.feedback eq 'show' && reviewable.feedbackComponentOption == '2' && reviewable.statistics && !reviewable.isAssessmentRetractForEdit && reviewable.isRecordedAssessment}">
                                    <f:param name="publishedAssessmentId" value="#{reviewable.assessmentId}" />
                                    <f:param name="hasNav" value="false"/>
                                    <f:param name="allSubmissions" value="true" />
                                    <f:param name="actionString" value="reviewAssessment"/>
                                    <f:param name="isFromStudent" value="true"/>
                                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
                                    <h:outputText value="#{selectIndexMessages.stats} "/>
                                </h:commandLink>
                            </h:panelGroup>
                            <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.feedback eq 'na' ||  reviewable.feedbackComponentOption == '1' || reviewable.isAssessmentRetractForEdit || !reviewable.statistics) && reviewable.isRecordedAssessment}" />
                        </t:column>
                        <!-- created separate column for statistics  -->

                        <%-- Recorded SCORE --%>
                        <t:column>
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{selectIndexMessages.recorded_score}" styleClass="currentSort" />
                                </h:panelGroup>
                            </f:facet>

                            <h:outputText value="#{reviewable.roundedRawScore} " styleClass="currentSort" rendered="#{reviewable.showScore eq 'show' && reviewable.isRecordedAssessment && !reviewable.isAssessmentRetractForEdit}" />
                            <h:outputText value="" rendered="#{!reviewable.isRecordedAssessment && reviewable.showScore eq 'show' && !reviewable.isAssessmentRetractForEdit}"/>
                            <h:outputText value="#{selectIndexMessages.highest_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' && reviewable.isRecordedAssessment && reviewable.scoringOption eq '1' && (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) && !reviewable.isAssessmentRetractForEdit}"/>
                            <h:outputText value="#{selectIndexMessages.last_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' && reviewable.isRecordedAssessment && reviewable.scoringOption eq '2' && (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) && !reviewable.isAssessmentRetractForEdit}"/>
                            <h:outputText value="#{selectIndexMessages.average_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' && reviewable.isRecordedAssessment && reviewable.scoringOption eq '4' && (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) && !reviewable.isAssessmentRetractForEdit}"/>
                            <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.showScore eq 'na' || reviewable.isAssessmentRetractForEdit) && reviewable.isRecordedAssessment}" />
                        </t:column>

                        <%-- FEEDBACK DATE --%>
                        <t:column>
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{selectIndexMessages.feedback_date}" styleClass="currentSort"  />
                                </h:panelGroup>
                            </f:facet>

                            <h:outputText value="#{reviewable.feedbackDateString}" styleClass="currentSort" rendered="#{reviewable.feedbackComponentOption == '2'  && reviewable.feedbackDelivery eq '2' && !reviewable.isAssessmentRetractForEdit && reviewable.isRecordedAssessment}" />
                            <h:outputText value="#{selectIndexMessages.immediate}" styleClass="currentSort" rendered="#{reviewable.feedbackComponentOption == '2'  && (reviewable.feedbackDelivery eq '1' || reviewable.feedbackDelivery eq '4') && !reviewable.isAssessmentRetractForEdit && reviewable.isRecordedAssessment}" />
                            <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.feedbackComponentOption == '1' || reviewable.feedbackDelivery==null  || reviewable.feedbackDelivery eq '3' || reviewable.isAssessmentRetractForEdit) && reviewable.isRecordedAssessment}" />

                            <!-- mustansar -->
                            <h:commandLink title="#{selectIndexMessages.t_reviewAssessment}" action="#{delivery.getOutcome}" immediate="true"
                                rendered="#{reviewable.feedback == 'show' && reviewable.feedbackComponentOption == '2' && !reviewable.isAssessmentRetractForEdit && select.displayAllAssessments != '1' && !reviewable.isRecordedAssessment }">
                                <f:param name="publishedId" value="#{reviewable.assessmentId}" />
                                <f:param name="assessmentGradingId" value="#{reviewable.assessmentGradingId}" />
                                <f:param name="nofeedback" value="false"/>
                                <f:param name="actionString" value="reviewAssessment"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
                                <h:outputText styleClass="currentSort" value="#{commonMessages.feedback}" rendered="#{reviewable.isRecordedAssessment && select.displayAllAssessments != '1' }" escape="false"/>
                                <h:outputText value="#{commonMessages.feedback}" rendered="#{!reviewable.isRecordedAssessment }" escape="false"/>
                            </h:commandLink>
                            <!-- mustansar -->
                        </t:column>

                        <%-- SCORE --%>
                        <t:column rendered="#{select.displayAllAssessments != '1'}" headerstyleClass="hidden-xs" styleClass="hidden-xs">
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{selectIndexMessages.individual_score}" styleClass="currentSort" />
                                </h:panelGroup>
                            </f:facet>

                            <h:outputText value="#{reviewable.roundedRawScore} " rendered="#{(reviewable.showScore eq 'show' && !reviewable.isAssessmentRetractForEdit) && !reviewable.isRecordedAssessment}" />
                            <h:outputText value="#{selectIndexMessages.not_applicable}" rendered="#{(reviewable.showScore eq 'na' || reviewable.isAssessmentRetractForEdit) && !reviewable.isRecordedAssessment}" />
                        </t:column>

                        <%-- TIME --%>
                        <t:column rendered="#{select.displayAllAssessments != '1'}" headerstyleClass="hidden-xs" styleClass="hidden-xs">
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{selectIndexMessages.time} " styleClass="currentSort"  />
                                </h:panelGroup>
                            </f:facet>

                            <h:panelGroup>
                                <h:outputText id="timeElapse" value="#{reviewable.timeElapse}" styleClass="currentSort" rendered="#{reviewable.isRecordedAssessment}" />
                                <h:outputText value="#{reviewable.timeElapse}" rendered="#{!reviewable.isRecordedAssessment}" />
                            </h:panelGroup>
                        </t:column>

                        <%-- SUBMITTED --%>
                        <t:column rendered="#{select.displayAllAssessments != '1'}" headerstyleClass="hidden-xs" styleClass="hidden-xs">
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{selectIndexMessages.submitted} " styleClass="currentSort"  />
                                </h:panelGroup>
                            </f:facet>

                            <h:outputText value="#{reviewable.submissionDateString}" styleClass="currentSort" rendered="#{reviewable.isRecordedAssessment}" />
                            <h:outputText value="#{reviewable.submissionDateString}" rendered="#{!reviewable.isRecordedAssessment}" />
                        </t:column>
                    </t:dataTable>

                    <t:div styleClass="sam-asterisks-row" rendered="#{(select.hasAnyAssessmentBeenModified && select.warnUserOfModification) || select.hasAnyAssessmentRetractForEdit}">
                        <h:panelGroup rendered="#{select.hasAnyAssessmentBeenModified && select.warnUserOfModification}">
                            <f:verbatim><p></f:verbatim>
                                <h:outputText styleClass="highlight fa fa-fw fa-exclamation-circle" />
                                <h:outputText value="#{selectIndexMessages.has_been_modified}" styleClass="highlight"/>
                            <f:verbatim></p></f:verbatim>
                        </h:panelGroup>
                        <h:panelGroup rendered="#{select.hasAnyAssessmentRetractForEdit}">
                            <f:verbatim><p></f:verbatim>
                                <h:outputText styleClass="highlight fa fa-fw fa-exclamation" title="#{selectIndexMessages.currently_being_edited}" />
                                <h:outputText value="#{selectIndexMessages.currently_being_edited}" styleClass="highlight"/>
                            <f:verbatim></p></f:verbatim>
                        </h:panelGroup>
                    </t:div>
                </div>
            </t:div>
        </h:form>
    </div>
    <!-- end content -->
</body>
</html>
