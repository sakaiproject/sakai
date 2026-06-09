// Common datatables JS
const pageLengthStorageKey = `samigo-pageLength-${portal.user.id}`;

function getPageLength() {
    const pageLength = localStorage.getItem(pageLengthStorageKey);
    return pageLength === null ? 20 : parseInt(pageLength, 10);
}

function setPageLength(pageLength) {
    localStorage.setItem(pageLengthStorageKey, pageLength);
}

function onInit(event, settings, pageLength) {
    settings.table.style.display = "";
}

function onPageLengthChange(event, settings, pageLength) {
    setPageLength(pageLength);
}

function setupDataTable(tableId, dataTableConfig) {

    const table = document.getElementById(tableId);

    if (!table) return;

    const pageLength = getPageLength();
    const callerHandler = dataTableConfig?.on?.["length.dt"];

    const eventHandlers = {
        ...(dataTableConfig?.on || {}),
        "length.dt": function() {
            callerHandler?.apply(this, arguments);
            onPageLengthChange.apply(this, arguments);
        },
    };

    const dataTable = sakaiDataTables.init(table, {
        ...dataTableConfig,
        pageLength,
        initComplete(settings) {
            onInit(null, settings, pageLength);
            dataTableConfig?.initComplete?.apply(this, arguments);
        },
    });

    Object.entries(eventHandlers).forEach(([eventName, handler]) => dataTable.on(eventName, handler));

    return dataTable;
}
