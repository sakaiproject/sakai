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
    $(event.currentTarget).show();
}

function onPageLengthChange(event, settings, pageLength) {
    setPageLength(pageLength);
}

function setupDataTable(tableId, dataTableConfig) {

    const table = document.getElementById(tableId);

    if (!table) return;

    const pageLength = getPageLength();

    const dataTable = $(table);
    dataTable.on("init.dt", onInit);
    dataTable.on("length.dt", onPageLengthChange);

    dataTable.DataTable({
        ...dataTableConfig,
        pageLength,
    });

    return dataTable;
}
