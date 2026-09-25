export function initializeToolLinks(table, language) {
    if (!table) return;

    const link = (href, label, newWindow = false) => {
        const anchor = document.createElement("a");
        anchor.href = href;
        anchor.textContent = label ?? "";
        if (newWindow) {
            anchor.target = "_blank";
            anchor.rel = "noopener";
        }
        return anchor;
    };
    const linkedText = urlField => (value, type, row) => {
        if (type !== "display") return value;
        return row[urlField] ? link(row[urlField], value, true) : document.createTextNode(value ?? "");
    };
    const renderers = {
        launch: linkedText("launchUrl"),
        siteTitle: linkedText("siteUrl"),
        actions: (value, type, row) => {
            if (type !== "display") return "";
            const actions = document.createElement("span");
            for (const action of ["edit", "delete"]) {
                const url = new URL(table.dataset[action + "Url"], document.baseURI);
                url.searchParams.set("id", row.id);
                if (actions.childNodes.length) actions.append(" / ");
                actions.append(link(url, table.dataset[action + "Label"]));
            }
            return actions;
        },
    };

    const dataTable = new DataTable(table, {
        ajax: table.dataset.ajaxUrl,
        serverSide: true,
        processing: true,
        titleRow: 0,
        pageLength: 50,
        lengthMenu: [10, 50, 100, 200],
        order: [[0, "asc"]],
        orderMulti: false,
        stateSave: true,
        stateDuration: -1,
        layout: { topEnd: null },
        language,
        columns: Array.from(table.tHead.rows[0].cells, header => {
            const field = header.dataset.field;
            return {
                data: field === "actions" ? null : field,
                name: header.dataset.column ?? "",
                orderable: field !== "actions",
                searchable: field !== "actions",
                defaultContent: "",
                render: renderers[field] ?? DataTable.render.text(),
            };
        }),
        initComplete() {
            const dataTable = this.api();
            table.querySelectorAll("input[data-column]").forEach(input => {
                const column = dataTable.column(input.dataset.column + ":name");
                input.value = column.search();
                input.addEventListener("input", DataTable.util.debounce(() => {
                    column.search(input.value).draw();
                }, 300));
            });
        },
    });
    document.getElementById("installed-tools-link").addEventListener("click", () => dataTable.state.clear());
}
