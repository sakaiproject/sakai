DataTable.ext.order["dom-span"] = function (settings, columnIndex) {
    return this.api()
        .column(columnIndex, { order: "index" })
        .nodes()
        .map(cell => cell.querySelector(".spanValue")?.textContent.trim() || cell.textContent.trim());
};

// Keep published assessments ahead of their drafts in either title direction.
DataTable.ext.order["dom-assessment-title"] = function (settings, columnIndex) {
    return this.api()
        .column(columnIndex, { order: "index" })
        .nodes()
        .map(cell => ({
            title: cell.querySelector(".spanValue")?.textContent.trim() || cell.textContent.trim(),
            draft: cell.parentElement.querySelector(".status_draft") ? 1 : 0
        }));
};

DataTable.ext.type.order["assessment-title-asc"] = function (a, b) {
    return DataTable.ext.type.order["natural-ci-asc"](a.title, b.title) || a.draft - b.draft;
};

DataTable.ext.type.order["assessment-title-desc"] = function (a, b) {
    return DataTable.ext.type.order["natural-ci-desc"](a.title, b.title) || a.draft - b.draft;
};

// Custom ordering types still need HTML stripping and accent normalization for search.
DataTable.ext.type.search["natural-ci"] = DataTable.ext.type.search.html;
DataTable.ext.type.search["assessment-title"] = DataTable.ext.type.search.html;
