DataTable.ext.order["dom-span"] = function (settings, columnIndex) {
    return this.api()
        .column(columnIndex, { order: "index" })
        .nodes()
        .map(cell => cell.querySelector(".spanValue")?.textContent.trim() || cell.textContent.trim());
};
