import "/webcomponents/bundles/sakai-pager.js";

document.querySelectorAll("sakai-pager[data-page-base]").forEach(pager => {
    const currentPage = Number(pager.getAttribute("current"));

    pager.addEventListener("page-selected", event => {
        const page = event.detail.page;
        if (page !== currentPage) {
            window.location.assign(new URL(`${page}/${pager.dataset.pageSize}`, pager.dataset.pageBase));
        }
    });
});
