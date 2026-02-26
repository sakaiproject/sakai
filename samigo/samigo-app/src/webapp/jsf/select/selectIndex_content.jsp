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
    <style>
        /* While waiting for first page: hide filters, length, headers and table body so processing overlay looks clean */
        .dt-waiting .dataTables_length,
        .dt-waiting .dataTables_filter,
        .dt-waiting thead,
        .dt-waiting tfoot,
        .dt-waiting tbody,
        .dt-waiting .dataTables_info,
        .dt-waiting .dataTables_paginate {
            display: none !important;
        }
        /* also ensure any stray rows are not visible */
        .dt-waiting tbody tr {
            display: none !important;
        }
        /* Ensure DataTables processing indicator is positioned inside the table wrapper
           and does not overlay arbitrary parts of the page */
        .dataTables_wrapper {
            position: relative;
        }
        .dataTables_processing {
            position: absolute !important;
            top: 8px !important;
            right: 8px !important;
            left: auto !important;
            width: auto !important;
            z-index: 1000 !important;
            background: rgba(255,255,255,0.95) !important;
            border: 1px solid rgba(0,0,0,0.08) !important;
            padding: 6px 10px !important;
            border-radius: 4px !important;
            box-shadow: 0 2px 6px rgba(0,0,0,0.08) !important;
            display: none; /* default hidden; shown by JS when needed */
        }
    </style>
    <script>
        // Normalize search text for accent-insensitive, case-insensitive matching
        // (used by custom DataTables search functions)
        window.normalizeSearchText = function(text) {
            return text
                .toLowerCase()
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "");
        };

        // Parse/format various server date formats into a local readable string
        // Accepts yyyyMMddHHmmss, epoch seconds/millis, or ISO strings
        window.formatServerDate = function(value) {
            if (!value) return '';
            // If server sent yyyymmddHHMMSS (14 digits) e.g. 20260223123000
            if (typeof value === 'string' && /^\d{14}$/.test(value)) {
                var y = parseInt(value.substr(0,4), 10);
                var m = parseInt(value.substr(4,2), 10) - 1;
                var d = parseInt(value.substr(6,2), 10);
                var hh = parseInt(value.substr(8,2), 10);
                var mm = parseInt(value.substr(10,2), 10);
                var ss = parseInt(value.substr(12,2), 10);
                var dt = new Date(y, m, d, hh, mm, ss);
                return dt.toLocaleString();
            }
            // If numeric (epoch seconds or millis)
            if (!isNaN(value)) {
                var n = Number(value);
                var dt = (n > 1e12) ? new Date(n) : new Date(n * 1000);
                return dt.toLocaleString();
            }
            // Try ISO parse
            var parsed = Date.parse(value);
            if (!isNaN(parsed)) return new Date(parsed).toLocaleString();
            return String(value);
        };

        $(document).ready(function() {
            // Add custom sorters: natural string sort and numeric fallbacks
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
            var assessmentUpdatedNeedResubmitText = <h:outputText value="'#{selectIndexMessages.assessment_updated_need_resubmit}'" />;
            var assessmentUpdatedText = <h:outputText value="'#{selectIndexMessages.assessment_updated}'" />;

            // Initialize client-side/server-side DataTable for takeable assessments
            var hasSelectTable = $("#selectIndexForm\\:selectTable").length > 0;
            if (hasSelectTable) {
                // mark table as 'waiting' while the first page is fetched/rendered
                try {
                    $("#selectIndexForm\\:selectTable").addClass('dt-waiting');
                } catch(e) {}

                try {
                    var wrapEarly = $("#selectIndexForm\\:selectTable").closest('.dataTables_wrapper'); 
                    if (wrapEarly.length) wrapEarly.addClass('dt-waiting');
                } catch(e) {}

                $.fn.dataTable.ext.classes.sLengthSelect = 'input-form-control';
                var table = $("#selectIndexForm\\:selectTable").DataTable({
                    serverSide: true,
                    processing: true,
                    paging: true,
                    lengthMenu: [[5, 10, 20, 50, 100, 200, -1], [5, 10, 20, 50, 100, 200, viewAllText]],
                    pageLength: 20,
                    aaSorting: [[2, "asc"]],
                    columns: [
                        {orderable: true, searchable: true, type: "span"},
                        {orderable: true, searchable: false},
                        {orderable: true, searchable: true, type: "numeric"}
                    ],
                    // Server-side ajax bridge: translate DataTables request -> backend API
                    ajax: function(dtRequest, callback, settings) {
                        const page = Math.floor(dtRequest.start / dtRequest.length) + 1;
                        const search = dtRequest.search && dtRequest.search.value ? dtRequest.search.value : '';
                        fetchTakeablePage({page, size: dtRequest.length, search, order: dtRequest.order})
                            .then(resp => {
                                const payload = resp && Array.isArray(resp.content) ? resp.content : [];
                                const rows = payload.map(item => {
                                    var messagesHtml = '';
                                    if (item.assessmentUpdatedNeedResubmit) {
                                        messagesHtml += '<span class="validate">' + assessmentUpdatedNeedResubmitText + '</span>';
                                    }
                                    if (item.assessmentUpdated) {
                                        messagesHtml += '<span class="validate">' + assessmentUpdatedText + '</span>';
                                    }

                                    var titleInner = '<span class="spanValue">' + item.assessmentTitle + '</span>';
                                    const titleHtml = item.alternativeDeliveryUrl
                                        ? '<a href="' + item.alternativeDeliveryUrl + '" title="Proctored Assessment Link">' + '<span class="fa fa-user-circle-o" title="Proctored Assessment Link"></span> ' + titleInner + '</a>' + messagesHtml
                                        : '<a href="#" onclick="submitBeginAssessment(\'' + encodeURIComponent(item.assessmentId) + '\'); return false;">' + titleInner + '</a>' + messagesHtml;

                                    let timeStr = '';
                                    if (item.timeLimit_hour || item.timeLimit_minute) {
                                        timeStr = '<b>';
                                        timeStr += item.timeLimit_hour ? item.timeLimit_hour + " hr " : "";
                                        timeStr += item.timeLimit_minute ? item.timeLimit_minute + " min" : "";
                                        timeStr += '</b>';
                                    } else {
                                        timeStr = '<span>n/a</span>';
                                    }

                                    const dueRaw = item.dueDate || item.due || item.dueDateString || '';
                                    const dueHtml = dueRaw ? "<b>" + window.formatServerDate(dueRaw) + "</b>" : '<span>n/a</span>';

                                    return [titleHtml, timeStr, dueHtml];
                                });

                                const recordsTotal = resp && resp.totalElements ? resp.totalElements : payload.length;
                                const recordsFiltered = recordsTotal;

                                // Update availability UI based on whether we received any rows
                                try {
                                    const hasRows = rows && rows.length > 0;
                                    const infoP = document.querySelector('.submission-container > .info-text');
                                    const takeNotesSpan = document.getElementById('takeNotesText');
                                    const takeNotAvailableSpan = document.getElementById('takeNotAvailableText');
                                    const tableEl = document.getElementById('selectIndexForm:selectTable');
                                    const wrapper = tableEl ? tableEl.closest('.dataTables_wrapper') : null;
                                    if (infoP) {
                                        if (hasRows) {
                                            if (takeNotesSpan) infoP.innerHTML = takeNotesSpan.innerHTML;
                                        } else {
                                            if (takeNotAvailableSpan) infoP.innerHTML = takeNotAvailableSpan.innerHTML;
                                        }
                                    }
                                    if (wrapper) {
                                        if (hasRows) {
                                            wrapper.style.display = '';
                                        } else {
                                            // If the user is performing a search, keep the table visible
                                            // so DataTables can display its zero-records message. Only
                                            // hide the table on the initial (no-search) empty load.
                                            var isSearchActive = dtRequest && dtRequest.search && dtRequest.search.value;
                                            if (isSearchActive) {
                                                wrapper.style.display = '';
                                            } else {
                                                wrapper.style.display = 'none';
                                            }
                                        }
                                    }
                                } catch (e) { console.warn('Error updating availability UI', e); }

                                callback({
                                    draw: dtRequest.draw,
                                    recordsTotal: recordsTotal,
                                    recordsFiltered: recordsFiltered,
                                    data: rows
                                });

                                // On first successful load, reveal headers/controls hidden earlier
                                try {
                                    var wrapper = $("#selectIndexForm\\:selectTable").closest('.dataTables_wrapper');
                                    if (wrapper.length && wrapper.hasClass('dt-waiting')) {
                                        wrapper.removeClass('dt-waiting');
                                        try { table.columns.adjust(); } catch(e) {}
                                    }
                                    try { $("#selectIndexForm\\:selectTable").removeClass('dt-waiting'); } catch(e) {}
                                    try { $("#selectIndexForm\\:selectTable").css('visibility','visible'); } catch(e) {}
                                } catch (e) { /* ignore */ }
                            })
                            .catch(err => {
                                callback({draw: dtRequest.draw, recordsTotal:0, recordsFiltered:0, data: []});
                            });
                    },
                    language: {
                        search: searchText,
                        lengthMenu: lengthMenuText,
                        zeroRecords: zeroRecordsText,
                        info: infoText,
                        infoEmpty: infoEmptyText,
                        infoFiltered: infoFilteredText,
                        emptyTable: emptyTableText,
                        paginate: {
                            next: nextText,
                            previous: previousText,
                        },
                        aria: {
                            sortAscending: sortAscendingText,
                            sortDescending: sortDescendingText,
                        }
                    }
                });

                // Mark wrapper/table as waiting until first AJAX page arrives
                try {
                    var wrapper = $("#selectIndexForm\\:selectTable").closest('.dataTables_wrapper');
                    if (wrapper.length) wrapper.addClass('dt-waiting');
                    $("#selectIndexForm\\:selectTable").addClass('dt-waiting');
                } catch (e) { /* ignore if wrapper not yet present */ }

                // Delay showing the DataTables processing indicator for quick requests
                // This avoids a flash for fast responses; indicator appears after 2s
                (function(){
                    var processingTimer = null;
                    try {
                        var tableEl = $("#selectIndexForm\\:selectTable");
                        var procEl = tableEl.closest('.dataTables_wrapper').find('.dataTables_processing');
                        // ensure the default processing indicator is hidden immediately
                        try { procEl.hide(); } catch(e) {}

                        // Listen to DataTables processing events for this table
                        tableEl.on('processing.dt', function(e, settings, processing) {
                            if (processing) {
                                // hide immediately to avoid a flash, then show only after delay
                                try { procEl.hide(); } catch(e) {}
                                if (processingTimer) { clearTimeout(processingTimer); }
                                processingTimer = setTimeout(function(){ try { procEl.show(); } catch(e) {} }, 2000);
                            } else {
                                if (processingTimer) { clearTimeout(processingTimer); processingTimer = null; }
                                try { procEl.hide(); } catch(e) {}
                            }
                        });
                    } catch(e) { /* ignore if elements not present yet */ }
                })();

                // Custom hook, accent-insensitive search into DataTables
                var selectTableApi = $('#selectIndexForm\\:selectTable').DataTable();
                const searchInput = document.querySelector('#selectIndexForm\\:selectTable_filter input');

                if (selectTableApi && searchInput && !searchInput.hasCustomSearch) {
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

                    // Debounced search: wait for user to stop typing
                    let searchDebounceTimer = null;
                    const searchDebounceMs = 600;
                    const handleSearch = function(e) {
                        lastSearchTerm = this.value;
                        // If user pressed Enter, skip debounce and search immediately
                        if (e && e.key === 'Enter') {
                            if (searchDebounceTimer) { clearTimeout(searchDebounceTimer); searchDebounceTimer = null; }
                            try { selectTableApi.search(lastSearchTerm).draw(); } catch (err) { selectTableApi.draw(); }
                            return;
                        }
                        if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
                        searchDebounceTimer = setTimeout(() => {
                            try { selectTableApi.search(lastSearchTerm).draw(); } catch (err) { selectTableApi.draw(); }
                        }, searchDebounceMs);
                    };

                    searchInput.addEventListener('input', handleSearch);
                    searchInput.addEventListener('keydown', function(e) { if (e.key === 'Enter') handleSearch.call(this, e); });

                    if (searchInput.value) {
                        lastSearchTerm = searchInput.value;
                        try { selectTableApi.search(lastSearchTerm).draw(); } catch (e) { selectTableApi.draw(); }
                    }
                }
            }

            // Initialize reviewTable (submitted assessments) if present
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

        // API bridge for fetching paged takeable assessments. Returns backend JSON.
        // Accepts DataTables pagination/search/order and maps to API query params.
        function fetchTakeablePage({page = 1, size = 5, search = '', order = null} = {}) {
            const siteId = window.portal.siteId;
            // determine sort and ascending from front-end `order` if provided
            const sortDefault = 'title';
            let sort = sortDefault;
            let ascending = true;
            try {
                if (order) {
                    // support either an array (DataTables `order`) or a single object
                    const ord = Array.isArray(order) && order.length ? order[0] : order;
                    if (ord && typeof ord === 'object') {
                        const colIndex = Number(ord.column);
                        const dir = (ord.dir || ord.order || '').toLowerCase();
                        ascending = (dir !== 'desc');
                        // map column indexes to backend sort fields
                        const colMap = {
                            0: 'title',
                            1: 'timeLimit',
                            2: 'due'
                        };
                        sort = colMap.hasOwnProperty(colIndex) ? colMap[colIndex] : sortDefault;
                    }
                }
            } catch (e) {
                console.warn('Error parsing order param, falling back to defaults', e);
            }

            const url = "/api/samigo/select/takeable?siteId=" + siteId
                        + "&ascending=" + (ascending ? 'true' : 'false')
                        + "&sort=" + encodeURIComponent(sort)
                        + "&page=" + page
                        + "&size=" + size
                        + "&search=" + encodeURIComponent(search);
            return fetch(url, { method: 'GET', credentials: 'same-origin' })
                .then(resp => {
                    if (!resp.ok) return [];
                    return resp.json();
                })
                .catch(() => {
                    return [];
                });
        }
    </script>

    <!-- content... -->
    <div class="portletBody container-fluid">
        <div class="page-header">
            <h1>
                <h:outputText value="#{selectIndexMessages.page_heading}"/>
            </h1>
        </div>

        <h:form id="selectIndexForm">
            <input type="hidden" id="selectIndexForm:publishedIdForBegin" name="publishedId" value="" />
            <input type="hidden" id="selectIndexForm:actionStringForBegin" name="actionString" value="takeAssessment" />
            <h:commandLink id="beginAssessmentHidden" action="beginAssessment" style="display:none">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
            </h:commandLink>

            <script>
                function submitBeginAssessment(publishedId) {
                    try {
                        var pid = decodeURIComponent(publishedId);
                        var publishedEl = document.getElementById('selectIndexForm:publishedIdForBegin');
                        if (publishedEl) publishedEl.value = pid;
                        var actionEl = document.getElementById('selectIndexForm:actionStringForBegin');
                        if (actionEl) actionEl.value = 'takeAssessment';
                        var link = document.getElementById('selectIndexForm:beginAssessmentHidden');
                        if (link) {
                            if (typeof link.click === 'function') link.click();
                            else if (link.onclick) link.onclick();
                            else document.getElementById('selectIndexForm').submit();
                        } else {
                            document.getElementById('selectIndexForm').submit();
                        }
                    } catch (e) {
                        console.warn('submitBeginAssessment error', e);
                        try { document.getElementById('selectIndexForm').submit(); } catch (err) {}
                    }
                }
            </script>
            <!-- SELECT -->
            <div class="submission-container">
                <h2>
                    <h:outputText value="#{selectIndexMessages.take_assessment}" />
                </h2>

                <p class="info-text">
                    <span id="takeAvailabilityMessage"></span>
                    <noscript>
                        <h:outputText value="#{selectIndexMessages.take_assessment_notAvailable}" />
                    </noscript>
                </p>

                <!-- SELECT TABLE: replaced server-side JSF table with client-side skeleton to avoid double-rendering -->
                <h:panelGroup>
                    <table id="selectIndexForm:selectTable" class="table table-hover table-striped table-bordered table-assessments" data-summary="" style="visibility:hidden;">
                        <thead>
                            <tr>
                                <th class="assessmentTitleHeader"><div class="tablesorter-header-inner"><h:outputText value="#{selectIndexMessages.title}" /></div></th>
                                <th class="assessmentTimeLimitHeader"><h:outputText value="#{selectIndexMessages.t_time_limit}" /></th>
                                <th class="assessmentDueDateHeader"><h:outputText value="#{selectIndexMessages.date_due}" /></th>
                            </tr>
                        </thead>
                        <tfoot class="d-none"><tr><td colspan="3"><h:outputText value="#{selectIndexMessages.sum_availableAssessment}" /></td></tr></tfoot>
                        <tbody></tbody>
                    </table>
                </h:panelGroup>
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
                    <!-- Hidden localized messages for client-side toggle (moved inside form) -->
                    <span id="takeNotesText" style="display:none"><h:outputText value="#{selectIndexMessages.take_assessment_notes}" /></span>
                    <span id="takeNotAvailableText" style="display:none"><h:outputText value="#{selectIndexMessages.take_assessment_notAvailable}" /></span>
        </h:form>
    </div>
    <!-- end content -->
</body>
</html>
