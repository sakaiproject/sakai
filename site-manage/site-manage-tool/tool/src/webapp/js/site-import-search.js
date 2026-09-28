document.addEventListener("DOMContentLoaded", () => {
    const search = document.getElementById("import-site-search");
    if (!search) return;

    const groups = Array.from(search.form.querySelectorAll("[data-import-group]"), element => ({
        element,
        sites: Array.from(element.querySelectorAll("[data-import-site]"), row => ({
            row,
            title: row.querySelector("[data-import-title]").textContent.toLowerCase(),
        })),
    }));
    const noMatches = document.getElementById("import-no-matches");

    search.addEventListener("input", () => {
        const query = search.value.trim().toLowerCase();
        let anyMatches = false;
        for (const { element, sites } of groups) {
            let groupMatches = false;
            for (const { row, title } of sites) {
                const matches = title.includes(query);
                row.hidden = !matches;
                groupMatches ||= matches;
            }
            element.hidden = !groupMatches;
            if (query && groupMatches) element.open = true;
            anyMatches ||= groupMatches;
        }
        noMatches.hidden = anyMatches;
    });
    search.addEventListener("keydown", event => {
        if (event.key === "Enter") event.preventDefault();
    });
});
