<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <jsp:text><![CDATA[



<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <!--
* $Id$

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

 -->
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <jsp:expression> request.getAttribute("html.head") </jsp:expression>
      <title>
        <h:outputText value="#{authorFrontDoorMessages.auth_front_door}"/>
      </title>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
      <jsp:body>
        <div class="portletBody container-fluid">
          <script>
            <jsp:text>includeWebjarLibrary('datatables');</jsp:text>
          </script>
          <script>
            <jsp:text>includeWebjarLibrary('bootstrap-multiselect');</jsp:text>
          </script>
          <jsp:element name="script">
            <jsp:attribute name="src" trim="false">/samigo-app/js/info.js</jsp:attribute>
            <jsp:body>
            </jsp:body>
          </jsp:element>
          <jsp:element name="script">
            <jsp:attribute name="src" trim="false">/samigo-app/js/sortHelper.js</jsp:attribute>
            <jsp:body>
            </jsp:body>
          </jsp:element>
          <script>
            <jsp:text>
        document.addEventListener('DOMContentLoaded', function() {
            const pageLengthStorageKey = "samigo-pageLength-" + portal.user.id;

            function getPageLength() {
                const pageLength = localStorage.getItem(pageLengthStorageKey);
                return pageLength === null ? 20 : parseInt(pageLength, 10);
            }

            function setPageLength(pageLength) {
                localStorage.setItem(pageLengthStorageKey, pageLength);
            }

            const tableElement = document.getElementById("authorIndexForm:coreAssessments");
            const notEmptyTableTd = tableElement?.querySelector("td:not(:empty)");
            const assessmentSortingColumn = </jsp:text>
            <h:outputText value="'#{author.assessmentSortingColumn}'"/>
            <jsp:text>;

            if (notEmptyTableTd) {
                const table = new DataTable(tableElement, {
                    "paging": true,
                    "lengthMenu": [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.assessment_view_all}`"/>
            <jsp:text>]],
                    "pageLength": getPageLength(),
                    "order": [[parseInt(assessmentSortingColumn), "desc"]],
                    "columns": [
                        {"orderable": true, "searchable": true, "type": "assessment-title", "orderDataType": "dom-assessment-title"},
                        {"orderable": false, "searchable": false},
                        {"orderable": true, "searchable": false},
                        {"orderable": true, "searchable": false},
                        {"orderable": true, "searchable": false},
                        {"orderable": true, "searchable": false},
                        {"orderable": true, "searchable": true, "type": "natural-ci", "orderDataType": "dom-span"},
                        {"orderable": true, "searchable": true, "type": "natural-ci", "orderDataType": "dom-span"},
                        {"orderable": true, "searchable": false},
                        {"orderable": true, "searchable": true, "type": "natural-ci", "orderDataType": "dom-span"},
                        {"orderable": false, "searchable": false},
                    ],
                    "language": {
                        "search": </jsp:text>
            <h:outputText value="`#{dataTablesMessages.search}`"/>
            <jsp:text>,
                        "lengthMenu": </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.datatables_lengthMenu}`"/>
            <jsp:text>,
                        "zeroRecords": </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.datatables_zeroRecords}`"/>
            <jsp:text>,
                        "info": </jsp:text>
            <h:outputText value="`#{dataTablesMessages.info}`"/>
            <jsp:text>,
                        "infoEmpty": </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.datatables_infoEmpty}`"/>
            <jsp:text>,
                        "infoFiltered": </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.datatables_infoFiltered}`"/>
            <jsp:text>,
                        "emptyTable": </jsp:text>
            <h:outputText value="`#{dataTablesMessages.infoEmpty}`"/>
            <jsp:text>,
                        "paginate": {
                            "next": </jsp:text>
            <h:outputText value="`#{dataTablesMessages.paginate_next}`"/>
            <jsp:text>,
                            "previous": </jsp:text>
            <h:outputText value="`#{dataTablesMessages.paginate_previous}`"/>
            <jsp:text>,
                        },
                        "aria": {
                            "sortAscending": </jsp:text>
            <h:outputText value="`#{dataTablesMessages.aria_sortAscending}`"/>
            <jsp:text>,
                            "sortDescending": </jsp:text>
            <h:outputText value="`#{dataTablesMessages.aria_sortDescending}`"/>
            <jsp:text><![CDATA[,
                        }
                    },
                    "drawCallback": function(oSettings) {
                        document.querySelectorAll(".select-checkbox").forEach(checkbox => checkbox.checked = false);
                        updateRemoveButton();
                    },
                    // Match the entered phrase, including spaces, as before the migration.
                    "search": { "smart": false },
                    "stateLoadParams": function(settings, data) {
                        data.search.smart = false;
                    },
                    "stateSave": true,
                    "stateDuration": -1
                });

                let spanClassName = "";
                let filterGroups = [];
                const assessmentFilter = function (searchData, rowData, rowIndex) {
                    const row = table.row(rowIndex).node();
                    const cells = row ? Array.from(row.cells) : [];
                    let showBySpan = true;
                    let showByGroups = !]]></jsp:text>
            <h:outputText value="#{author.groupFilterEnabled}"/>
            <jsp:text><![CDATA[;

                    if (spanClassName != "") {
                        showBySpan = cells.some(cell => cell.querySelector("span." + CSS.escape(spanClassName)));
                    }
                    if (filterGroups != null) {
                        for (let i = 0; i < filterGroups.length; i++) {
                            const filter = filterGroups[i];
                            if (filter.startsWith("releaseto")) {
                                showByGroups = cells.some(cell => cell.querySelector("." + CSS.escape(filter)));
                            } else {
                                showByGroups = Array.from(cells[5]?.querySelectorAll(".groupList > li > .d-none") || [])
                                    .some(item => item.textContent.includes(filter));
                            }
                            if (showByGroups) {
                                break;
                            }
                        }
                    }

                    return showBySpan && showByGroups;
                };

                table.search.fixed("assessment-filter", assessmentFilter);

                function filterBy() {
                    table.draw();
                }

                table.on('order.dt', function () {
                    updateRemoveButton();
                });

                table.on('length.dt', function (e, settings, len) {
                    setPageLength(len);
                });

                document.getElementById("authorIndexForm:filter-type")?.addEventListener("change", event => {
                    spanClassName = event.target.value;
                    filterBy();
                });

                const groupSelect = document.getElementById("authorIndexForm:group-select");
                groupSelect?.setAttribute("multiple", "multiple");
                Array.from(groupSelect?.options || []).forEach(option => option.selected = true);
                filterGroups = groupSelect ? Array.from(groupSelect.selectedOptions).map(option => option.value) : [];

                const divElem = document.createElement('div');
                let filterPlaceholder = ]]></jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.multiselect_filterPlaceholder}`"/>
            <jsp:text>;
                divElem.innerHTML = filterPlaceholder;
                filterPlaceholder = divElem.textContent;
                let selectAllText = </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.multiselect_selectAllText}`"/>
            <jsp:text>;
                divElem.innerHTML = selectAllText;
                selectAllText = divElem.textContent;
                let nonSelectedText = </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.multiselect_nonSelectedText}`"/>
            <jsp:text>;
                divElem.innerHTML = nonSelectedText;
                nonSelectedText = divElem.textContent;
                let allSelectedText = </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.multiselect_allSelectedText}`"/>
            <jsp:text>;
                divElem.innerHTML = allSelectedText;
                allSelectedText = divElem.textContent;
                let nSelectedText = </jsp:text>
            <h:outputText value="`#{authorFrontDoorMessages.multiselect_nSelectedText}`"/>
            <jsp:text><![CDATA[;
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

                groupSelect?.addEventListener("change", event => {
                    filterGroups = Array.from(event.target.selectedOptions).map(option => option.value);
                    filterBy();
                });
            }

            tableElement?.addEventListener("change", event => {
                if (!event.target.matches(".select-checkbox")) return;
                updateRemoveButton();
            });

            function updateRemoveButton() {
                const removeSelected = document.getElementById("authorIndexForm:remove-selected");
                const publishSelected = document.getElementById("authorIndexForm:publish-selected");
                const hasSelectedAssessments = Boolean(document.querySelector(".select-checkbox:checked"));

                if (hasSelectedAssessments) {
                    removeSelected?.classList.remove("disabled");
                    removeSelected?.setAttribute("tabindex", 0);
                    publishSelected?.classList.remove("disabled");
                    publishSelected?.setAttribute("tabindex", 0);
                } else {
                    removeSelected?.classList.add("disabled");
                    removeSelected?.setAttribute("tabindex", -1);
                    publishSelected?.classList.add("disabled");
                    publishSelected?.setAttribute("tabindex", -1);
                }
            }
        });

        function removeSelectedButtonAction() {
            if (!document.getElementById("authorIndexForm:remove-selected").classList.contains("disabled")) {
                let message = ]]></jsp:text>
            <h:outputText value="`#{authorMessages.cert_rem_assmt}`"/>
            <jsp:text>;
                message += "\n\n";
                message += </jsp:text>
            <h:outputText value="`#{authorMessages.cert_rem_assmt2}`"/>
            <jsp:text>;
                const elem = document.createElement('div');
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
                let message = </jsp:text>
            <h:outputText value="`#{authorMessages.cert_pub_assmt}`"/>
            <jsp:text>;
                const elem = document.createElement('div');
                elem.innerHTML = message;
                if (!confirm(elem.textContent)) {
                    event.preventDefault();
                    return false;
                }
                return true;
            }
        }

    </jsp:text>
          </script>
          <!-- content...  -->
          <h:form id="authorIndexForm">
            <!-- HEADINGS  -->
            <jsp:directive.include file="/jsf/author/assessmentHeadings.jsp"/>
            <h:panelGroup layout="block" styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}">
              <h:messages rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
            </h:panelGroup>
            <div class="samigo-container">
              <div class="page-header">
                <h1>
                  <h:outputText value="#{authorFrontDoorMessages.assessment_list}"/>
                </h1>
              </div>
              <h:panelGroup rendered="#{author.allAssessments.size() &gt; 0}">
                <div>
                  <f:verbatim>
                    <jsp:text><![CDATA[<label class="form-label">]]></jsp:text>
                  </f:verbatim>
                  <h:outputText value="#{authorFrontDoorMessages.assessment_view} "/>
                  <h:selectOneMenu value="select" id="filter-type">
                    <f:selectItem itemValue="" itemLabel="#{authorFrontDoorMessages.assessment_view_all}"/>
                    <f:selectItem itemValue="status_draft" itemLabel="#{authorFrontDoorMessages.assessment_pending}"/>
                    <f:selectItem itemValue="status_published_2" itemLabel="#{authorFrontDoorMessages.assessment_status_active}"/>
                    <f:selectItem itemValue="status_published_1" itemLabel="#{authorFrontDoorMessages.assessment_status_pending}"/>
                    <f:selectItem itemValue="status_published_0" itemLabel="#{authorFrontDoorMessages.assessment_status_inactive}"/>
                  </h:selectOneMenu>
                  <f:verbatim>
                    <jsp:text><![CDATA[</label>]]></jsp:text>
                  </f:verbatim>
                </div>
              </h:panelGroup>
              <h:panelGroup rendered="#{author.groupFilterEnabled and author.allAssessments.size() &gt; 0}">
                <div>
                  <f:verbatim>
                    <jsp:text><![CDATA[<label>]]></jsp:text>
                  </f:verbatim>
                  <h:outputText value="#{authorFrontDoorMessages.filterbygroup} "/>
                  <h:selectOneMenu value="select" id="group-select">
                    <f:selectItem itemValue="releaseto_anon" itemLabel="#{authorFrontDoorMessages.anonymous_users}"/>
                    <f:selectItem itemValue="releaseto_entire" itemLabel="#{authorFrontDoorMessages.entire_site}"/>
                    <t:selectItems value="#{author.groups.entrySet()}" var="group" itemValue="#{group.key}" itemLabel="#{group.value}"/>
                  </h:selectOneMenu>
                  <f:verbatim>
                    <jsp:text><![CDATA[</label>]]></jsp:text>
                  </f:verbatim>
                </div>
              </h:panelGroup>
              <!-- CORE ASSESSMENTS -->
              <h:panelGroup rendered="#{author.allAssessments.size() == 0}">
                <h:outputText value="#{authorFrontDoorMessages.datatables_zeroRecords}" styleClass="sak-banner-info"/>
              </h:panelGroup>
              <t:dataTable cellpadding="0" cellspacing="0" rowClasses="list-row-even,list-row-odd" styleClass="table table-hover table-striped table-bordered table-assessments" id="coreAssessments" value="#{author.allAssessments}" var="assessment" rendered="#{author.allAssessments.size() &gt; 0}" summary="#{authorFrontDoorMessages.sum_coreAssessment}">
                <jsp:scriptlet>/* Title */</jsp:scriptlet>
                <t:column headerstyleClass="titlePending" styleClass="titlePending">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_title}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <strong>
                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}">
                      <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " styleClass="highlight"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade' and assessment.status == 3}">
                      <h:outputText styleClass="highlight fa fa-fw fa-exclamation-circle" title="#{authorFrontDoorMessages.retracted_for_edit}"/>
                    </h:panelGroup>
                    <h:outputText styleClass="spanValue" value="#{assessment.title}"/>
                  </strong>
                </t:column>
                <jsp:scriptlet>/* Action */</jsp:scriptlet>
                <t:column headerstyleClass="titleActions" styleClass="titleActions">
                  <f:facet name="header">
                  </f:facet>
                  <h:panelGroup rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}" styleClass="btn-group pull-right">
                    <f:verbatim>
                      <jsp:text><![CDATA[<button class="btn btn-primary dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="]]></jsp:text>
                    </f:verbatim>
                    <h:outputText value="#{authorMessages.actions_for} "/>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                    <h:outputText value="#{assessment.title}"/>
                    <f:verbatim>
                      <jsp:text>" aria-label="</jsp:text>
                    </f:verbatim>
                    <h:outputText value="#{authorMessages.actions_for} "/>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                    <h:outputText value="#{assessment.title}"/>
                    <f:verbatim>
                      <jsp:text><![CDATA[">]]></jsp:text>
                    </f:verbatim>
                    <h:outputText value="#{authorMessages.select_action}"/>
                    <jsp:element name="span">
                      <jsp:attribute name="class" trim="false">caret</jsp:attribute>
                      <jsp:body>
                      </jsp:body>
                    </jsp:element>
                    <f:verbatim>
                      <jsp:text><![CDATA[</button>]]></jsp:text>
                    </f:verbatim>
                    <t:dataList layout="unorderedList" value="#{author.pendingSelectActionList1}" var="pendingSelectActionList" rendered="#{assessment.questionSize &gt; 0 }" styleClass="dropdown-menu row">
                      <h:commandLink id="publishedHiddenlink" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item" action="#{author.getOutcome}" value="#{pendingSelectActionList.label}">
                        <f:param name="action" value="#{pendingSelectActionList.value}"/>
                        <f:param name="assessmentId" value="#{assessment.assessmentBaseId}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                      </h:commandLink>
                    </t:dataList>
                    <t:dataList layout="unorderedList" value="#{author.pendingSelectActionList2}" var="pendingSelectActionList" rendered="#{assessment.questionSize == 0 }" styleClass="dropdown-menu row">
                      <h:commandLink id="publishedHiddenlink" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item" action="#{author.getOutcome}" value="#{pendingSelectActionList.label}">
                        <f:param name="action" value="#{pendingSelectActionList.value}"/>
                        <f:param name="assessmentId" value="#{assessment.assessmentBaseId}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                      </h:commandLink>
                    </t:dataList>
                  </h:panelGroup>
                  <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}" styleClass="btn-group pull-right">
                    <h:panelGroup rendered="#{(author.isGradeable &amp;&amp; assessment.hasAssessmentGradingData) &amp;&amp; (author.isEditable &amp;&amp; (!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                      <f:verbatim>
                        <jsp:text><![CDATA[<button class="btn btn-primary btn-xs dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.actions_for} "/>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                      <h:outputText value="#{assessment.title}"/>
                      <f:verbatim>
                        <jsp:text>" aria-label="</jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.actions_for} "/>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                      <h:outputText value="#{assessment.title}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.select_action}"/>
                      <jsp:element name="span">
                        <jsp:attribute name="class" trim="false">caret</jsp:attribute>
                        <jsp:body>
                        </jsp:body>
                      </jsp:element>
                      <f:verbatim>
                        <jsp:text><![CDATA[</button>]]></jsp:text>
                      </f:verbatim>
                      <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" rowIndexVar="index" styleClass="dropdown-menu row">
                        <li>
                          <h:commandLink action="#{author.getOutcome}" value="#{authorMessages.action_scores}" styleClass="hiddenBtn_scores dropdown-item" rendered="#{index == 0 &amp;&amp; assessment.submittedCount &gt; 0}">
                            <f:param name="action" value="scores"/>
                            <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                          </h:commandLink>
                        </li>
                        <li>
                          <h:commandLink action="#{author.getOutcome}" value="#{commonMessages.edit_action}" rendered="#{author.canEditPublishedAssessment(assessment) and index == 0}" styleClass="hiddenBtn_edit_published dropdown-item">
                            <f:param name="action" value="edit_published"/>
                            <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                          </h:commandLink>
                        </li>
                        <li>
                          <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item">
                            <f:param name="action" value="#{pendingSelectActionList.value}"/>
                            <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                            <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                          </h:commandLink>
                        </li>
                      </t:dataList>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{(author.isGradeable &amp;&amp; assessment.hasAssessmentGradingData) &amp;&amp; (author.isEditable &amp;&amp; !(!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                      <f:verbatim>
                        <jsp:text><![CDATA[<button class="btn btn-primary btn-xs dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.actions_for} "/>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                      <h:outputText value="#{assessment.title}"/>
                      <f:verbatim>
                        <jsp:text>" aria-label="</jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.actions_for} "/>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                      <h:outputText value="#{assessment.title}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.select_action}"/>
                      <jsp:element name="span">
                        <jsp:attribute name="class" trim="false">caret</jsp:attribute>
                        <jsp:body>
                        </jsp:body>
                      </jsp:element>
                      <f:verbatim>
                        <jsp:text><![CDATA[</button>]]></jsp:text>
                      </f:verbatim>
                      <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" styleClass="dropdown-menu row" rowIndexVar="index">
                        <h:commandLink action="#{author.getOutcome}" value="#{authorMessages.action_scores}" rendered="#{index == 0 &amp;&amp; assessment.submittedCount &gt; 0}" styleClass="hiddenBtn_scores dropdown-item">
                          <f:param name="action" value="scores"/>
                          <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                        </h:commandLink>
                        <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item">
                          <f:param name="action" value="#{pendingSelectActionList.value}"/>
                          <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                          <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                        </h:commandLink>
                      </t:dataList>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{!(author.isGradeable &amp;&amp; assessment.hasAssessmentGradingData) &amp;&amp; (author.isEditable &amp;&amp; (!author.editPubAssessmentRestricted || !assessment.hasAssessmentGradingData))}">
                      <f:verbatim>
                        <jsp:text><![CDATA[<button class="btn btn-primary btn-xs dropdown-toggle" aria-expanded="false" data-bs-toggle="dropdown" title="]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.actions_for} "/>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                      <h:outputText value="#{assessment.title}"/>
                      <f:verbatim>
                        <jsp:text>" aria-label="</jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.actions_for} "/>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_draft} - " rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}"/>
                      <h:outputText value="#{assessment.title}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorMessages.select_action}"/>
                      <jsp:element name="span">
                        <jsp:attribute name="class" trim="false">caret</jsp:attribute>
                        <jsp:body>
                        </jsp:body>
                      </jsp:element>
                      <f:verbatim>
                        <jsp:text><![CDATA[</button>]]></jsp:text>
                      </f:verbatim>
                      <t:dataList layout="unorderedList" value="#{author.publishedSelectActionList}" var="pendingSelectActionList" styleClass="dropdown-menu row" rowIndexVar="index">
                        <li>
                          <h:commandLink action="#{author.getOutcome}" value="#{commonMessages.edit_action}" rendered="#{author.canEditPublishedAssessment(assessment) and index == 0}" styleClass="hiddenBtn_edit_published dropdown-item">
                            <f:param name="action" value="edit_published"/>
                            <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                          </h:commandLink>
                        </li>
                        <li>
                          <h:commandLink action="#{author.getOutcome}" value="#{pendingSelectActionList.label}" styleClass="hiddenBtn_#{pendingSelectActionList.value} dropdown-item">
                            <f:param name="action" value="#{pendingSelectActionList.value}"/>
                            <f:param name="assessmentId" value="#{assessment.publishedAssessmentId}"/>
                            <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
                          </h:commandLink>
                        </li>
                      </t:dataList>
                    </h:panelGroup>
                  </h:panelGroup>
                </t:column>
                <jsp:scriptlet>/* Status */</jsp:scriptlet>
                <t:column headerstyleClass="status" styleClass="status">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_status}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup rendered="#{assessment['class'].simpleName == 'AssessmentFacade'}">
                    <f:verbatim>
                      <jsp:text><![CDATA[<span class="status_draft">]]></jsp:text>
                    </f:verbatim>
                    <h:outputText styleClass="d-none spanValue" value="-1"/>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_status_draft}"/>
                    <f:verbatim>
                      <jsp:text><![CDATA[</span>]]></jsp:text>
                    </f:verbatim>
                  </h:panelGroup>
                  <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                    <f:verbatim>
                      <jsp:text><![CDATA[<span class="status_published_]]></jsp:text>
                    </f:verbatim>
                    <h:outputText value="#{assessment.activeStatus}"/>
                    <f:verbatim>
                      <jsp:text><![CDATA[">]]></jsp:text>
                    </f:verbatim>
                    <h:outputText styleClass="d-none spanValue" value="#{assessment.activeStatus}"/>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_status_active}" rendered="#{assessment.activeStatus==2}"/>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_status_pending}" rendered="#{assessment.activeStatus==1}"/>
                    <h:outputText value="#{authorFrontDoorMessages.assessment_status_inactive}" rendered="#{assessment.activeStatus==0}"/>
                    <f:verbatim>
                      <jsp:text><![CDATA[</span>]]></jsp:text>
                    </f:verbatim>
                  </h:panelGroup>
                </t:column>
                <jsp:scriptlet>/* In Progress */</jsp:scriptlet>
                <t:column headerstyleClass="inProgress d-none d-sm-table-cell" styleClass="inProgress d-none d-sm-table-cell">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_in_progress}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                    <h:outputText value="#{assessment.inProgressCount}"/>
                  </h:panelGroup>
                </t:column>
                <jsp:scriptlet>/* Submitted */</jsp:scriptlet>
                <t:column headerstyleClass="submitted d-none d-sm-table-cell" styleClass="submitted d-none d-sm-table-cell">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_submitted}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                    <h:panelGroup rendered="#{assessment.submittedCount==0 or !(authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
                      <h:outputText value="#{assessment.submittedCount}"/>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{assessment.submittedCount&gt;0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
                      <f:verbatim>
                        <jsp:text><![CDATA[<span aria-label="]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{assessment.submittedCount} #{authorFrontDoorMessages.assessment_submissions}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[" role="link" tabindex="0">]]></jsp:text>
                      </f:verbatim>
                      <h:commandLink title="#{authorFrontDoorMessages.assessment_submissions}" action="#{author.getOutcome}" immediate="true" id="authorIndexToScore1" tabindex="-1">
                        <h:outputText value="#{assessment.submittedCount}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener"/>
                        <f:param name="publishedId" value="#{assessment.publishedAssessmentId}"/>
                        <f:param name="allSubmissionsT" value="3"/>
                      </h:commandLink>
                      <f:verbatim>
                        <jsp:text><![CDATA[</span>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </h:panelGroup>
                </t:column>
                <jsp:scriptlet>/* Release To */</jsp:scriptlet>
                <t:column headerstyleClass="releaseTo d-none d-sm-table-cell" styleClass="releaseTo d-none d-sm-table-cell">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_release }"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <h:outputText value="#{authorFrontDoorMessages.anonymous_users}" styleClass="releaseto_anon" rendered="#{assessment.releaseTo eq 'Anonymous Users'}"/>
                  <h:outputText value="#{authorFrontDoorMessages.entire_site}" styleClass="releaseto_entire" rendered="#{assessment.releaseTo ne 'Anonymous Users' &amp;&amp; assessment.releaseTo ne 'Selected Groups'}"/>
                  <t:div rendered="#{assessment.releaseTo eq 'Selected Groups'}">
                    <t:div id="groupsHeader" onclick="#{assessment.groupCount gt 0 ? 'toggleGroups( this );' : ''}" styleClass="#{assessment.groupCount ge 1 ? 'collapsed' : 'sak-banner-error'}">
                      <h:outputText value="#{assessment.groupCount} " rendered="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount gt 0}"/>
                      <h:outputText value="#{authorFrontDoorMessages.selected_groups} " rendered="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount gt 1}"/>
                      <h:outputText value="#{authorFrontDoorMessages.selected_group} " rendered="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount eq 1}"/>
                      <h:outputText value="#{authorFrontDoorMessages.no_selected_groups_error}" rendered="#{assessment.releaseTo eq 'Selected Groups' and assessment.groupCount eq 0}"/>
                    </t:div>
                    <t:div id="groupsPanel" style="display: none;">
                      <t:dataList layout="unorderedList" value="#{assessment.releaseToGroups.entrySet()}" var="group" styleClass="groupList">
                        <h:outputText value="#{group.value}"/>
                        <h:outputText value="#{group.key}" styleClass="d-none"/>
                      </t:dataList>
                    </t:div>
                  </t:div>
                </t:column>
                <jsp:scriptlet>/* Release Date */</jsp:scriptlet>
                <t:column headerstyleClass="releaseDate d-none d-sm-table-cell" styleClass="releaseDate d-none d-sm-table-cell">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_date}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <f:verbatim>
                    <jsp:text><![CDATA[<div>]]></jsp:text>
                  </f:verbatim>
                  <h:outputText value="#{assessment.startDate}">
                    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                  </h:outputText>
                  <h:outputText value="#{assessment.startDate}" styleClass="d-none spanValue">
                    <f:convertDateTime pattern="yyyyMMddHHmmss"/>
                  </h:outputText>
                  <f:verbatim>
                    <jsp:text><![CDATA[</div>]]></jsp:text>
                  </f:verbatim>
                </t:column>
                <jsp:scriptlet>/* Due Date */</jsp:scriptlet>
                <t:column headerstyleClass="dueDate" styleClass="dueDate">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.assessment_due}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <f:verbatim>
                    <jsp:text><![CDATA[<div>]]></jsp:text>
                  </f:verbatim>
                  <h:outputText value="#{assessment.dueDate}">
                    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                  </h:outputText>
                  <h:panelGroup rendered="#{assessment['class'].simpleName == 'PublishedAssessmentFacade'}">
                    <h:outputText value=" #{selectIndexMessages.late} " styleClass="text-danger" rendered="#{assessment.pastDue}"/>
                  </h:panelGroup>
                  <h:outputText value="#{assessment.dueDate}" styleClass="d-none spanValue">
                    <f:convertDateTime pattern="yyyyMMddHHmmss"/>
                  </h:outputText>
                  <f:verbatim>
                    <jsp:text><![CDATA[</div>]]></jsp:text>
                  </f:verbatim>
                </t:column>
                <jsp:scriptlet>/* Last Modified */</jsp:scriptlet>
                <t:column headerstyleClass="lastModified d-none d-sm-table-cell" styleClass="lastModified d-none d-sm-table-cell">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <h:outputText value="#{assessment.lastModifiedBy}"/>
                </t:column>
                <jsp:scriptlet>/* Modified Date */</jsp:scriptlet>
                <t:column headerstyleClass="lastModifiedDate d-none d-sm-table-cell" styleClass="lastModifiedDate d-none d-sm-table-cell">
                  <f:facet name="header">
                    <h:panelGroup>
                      <f:verbatim>
                        <jsp:text><![CDATA[<a href="#" tabindex="-1">]]></jsp:text>
                      </f:verbatim>
                      <h:outputText value="#{authorFrontDoorMessages.header_last_modified_date}"/>
                      <f:verbatim>
                        <jsp:text><![CDATA[</a>]]></jsp:text>
                      </f:verbatim>
                    </h:panelGroup>
                  </f:facet>
                  <f:verbatim>
                    <jsp:text><![CDATA[<div>]]></jsp:text>
                  </f:verbatim>
                  <h:outputText value="#{assessment.lastModifiedDate}">
                    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                  </h:outputText>
                  <h:outputText value="#{assessment.lastModifiedDate}" styleClass="d-none spanValue">
                    <f:convertDateTime pattern="yyyyMMddHHmmss"/>
                  </h:outputText>
                  <f:verbatim>
                    <jsp:text><![CDATA[</div>]]></jsp:text>
                  </f:verbatim>
                </t:column>
                <jsp:scriptlet>/* Remove */</jsp:scriptlet>
                <t:column rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}">
                  <f:facet name="header">
                    <h:outputText value="#{authorFrontDoorMessages.header_select}"/>
                  </f:facet>
                  <h:selectBooleanCheckbox value="#{assessment.selected}" styleClass="select-checkbox" title="#{authorFrontDoorMessages.assessment_select_to_remove}"/>
                </t:column>
                <t:column rendered="#{!authorization.deleteAnyAssessment and !authorization.deleteOwnAssessment}" headerstyleClass="d-none" styleClass="d-none">
                </t:column>
              </t:dataTable>
              <jsp:element name="div">
                <jsp:attribute name="class" trim="false">clearfix</jsp:attribute>
                <jsp:body>
                </jsp:body>
              </jsp:element>
              <h:panelGroup rendered="#{author.isAnyAssessmentRetractForEdit == true &amp;&amp; author.allAssessments.size() &gt; 0}">
                <f:verbatim>
                  <jsp:text><![CDATA[<p>]]></jsp:text>
                </f:verbatim>
                <h:outputText id="assessment-retracted" value="#{authorFrontDoorMessages.retracted_for_edit}" styleClass="sak-banner-red-warn"/>
                <f:verbatim>
                  <jsp:text><![CDATA[</p>]]></jsp:text>
                </f:verbatim>
              </h:panelGroup>
            </div>
            <jsp:element name="div">
              <jsp:attribute name="class" trim="false">clearfix</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <h:commandButton type="button" id="remove-selected" value="#{authorFrontDoorMessages.assessment_remove_selected}" rendered="#{(authorization.deleteAnyAssessment or authorization.deleteOwnAssessment) and author.allAssessments.size() &gt; 0}" styleClass="disabled" onclick="if (!removeSelectedButtonAction(this)) return false;" action="#{author.getOutcome}">
              <f:param name="action" value="remove_selected"/>
              <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
              <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener"/>
            </h:commandButton>
            <h:commandButton type="button" id="publish-selected" value="#{authorFrontDoorMessages.assessment_publish_selected}" rendered="#{author.allAssessments.size() &gt; 0}" styleClass="disabled ms-2" onclick="if (!publishSelectedButtonAction(this)) return false;" action="#{author.getOutcome}">
              <f:param name="action" value="publish_selected"/>
              <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener"/>
              <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener"/>
            </h:commandButton>
          </h:form>
          <!-- end content  -->
        </div>
      </jsp:body>
    </jsp:element>
  </html>
</jsp:root>
