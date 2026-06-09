(function (checklistProgress) {

    checklistProgress.init = function () {
        document.querySelectorAll(".checklistProgressTable").forEach(table => {
            sakaiDataTables.init(table, {
                order: [[0, "asc"]],
                paging: false,
                info: false,
                searching: false
            });
        });

        document.querySelectorAll(".headerNum").forEach(header => {
            header.addEventListener("click", function () {
                const itemTitle = this.querySelector("span.itemText")?.textContent || "";
                const descRowTip = document.getElementById("descRowTip");
                if (descRowTip) {
                    descRowTip.textContent = itemTitle;
                    descRowTip.style.display = "";
                }
                document.querySelectorAll(".headerNum").forEach(item => item.classList.remove("selectedChItem"));
                this.classList.add("selectedChItem");
            });
        });
    };
}(window.checklistProgress = window.checklistProgress || {}));
