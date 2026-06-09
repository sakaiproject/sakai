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
    <script>
        sakaiDataTables.onReady(function() {
            const viewAllText = <h:outputText value="'#{authorFrontDoorMessages.assessment_view_all}'" />;
            const language = {
                search: <h:outputText value="'#{dataTablesMessages.search}'" />,
                lengthMenu: <h:outputText value="'#{authorFrontDoorMessages.datatables_lengthMenu}'" />,
                zeroRecords: <h:outputText value="'#{authorFrontDoorMessages.datatables_zeroRecords}'" />,
                info: <h:outputText value="'#{dataTablesMessages.info}'" />,
                infoEmpty: <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />,
                infoFiltered: <h:outputText value="'#{authorFrontDoorMessages.datatables_infoFiltered}'" />,
                emptyTable: <h:outputText value="'#{authorFrontDoorMessages.datatables_infoEmpty}'" />,
                paginate: {
                    next: <h:outputText value="'#{dataTablesMessages.paginate_next}'" />,
                    previous: <h:outputText value="'#{dataTablesMessages.paginate_previous}'" />,
                },
                aria: {
                    sortAscending: <h:outputText value="'#{dataTablesMessages.aria_sortAscending}'" />,
                    sortDescending: <h:outputText value="'#{dataTablesMessages.aria_sortDescending}'" />,
                }
            };
            const lengthMenu = [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, viewAllText]];
            const commonOptions = {
                paging: true,
                lengthMenu,
                pageLength: 20,
                language,
                stateSave: true,
                stateDuration: -1
            };

            const selectTable = sakaiDataTables.initIfNotEmpty('selectIndexForm:selectTable', {
                ...commonOptions,
                order: [[2, "asc"]],
                columns: [
                    { orderable: true, searchable: true, type: "span" },
                    { orderable: true, searchable: false },
                    { orderable: true, searchable: true, type: "num" },
                ],
            });

            if (selectTable) {
                sakaiDataTables.attachSearch(selectTable, {
                    input: "#selectIndexForm\\:selectTable_filter input",
                    tableId: "selectIndexForm:selectTable",
                });
            }

            const displayAllAssessments = document.querySelector("#selectIndexForm\\:reviewTable .displayAllAssessments");
            const reviewTable = sakaiDataTables.initIfNotEmpty('selectIndexForm:reviewTable', {
                ...commonOptions,
                order: displayAllAssessments ? [[6, "desc"]] : [],
                paging: false,
                ordering: false,
                info: false,
                columns: displayAllAssessments ? [
                    { orderable: true, searchable: true },
                    { orderable: true, searchable: false },
                    { orderable: true, searchable: false },
                    { orderable: true, searchable: true },
                    { orderable: true, searchable: false },
                    { orderable: true, searchable: false },
                    { orderable: true, searchable: true }
                ] : [
                    { orderable: true, searchable: true },
                    { orderable: true, searchable: false },
                    { orderable: true, searchable: false },
                    { orderable: true, searchable: true }
                ],
            });

            if (reviewTable) {
                sakaiDataTables.attachSearch(reviewTable, {
                    input: "#selectIndexForm\\:reviewTable_filter input",
                    tableId: "selectIndexForm:reviewTable",
                });
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
