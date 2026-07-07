sakai.announcements = sakai.announcements || {

  toggleBulkButtons: disable => {

    ["remove", "publish", "unpublish"].forEach(action => {
      document.getElementById(`announcement-${action}-button`).disabled = disable;
    });
  },
};

document.querySelectorAll(".announcement-select-checkbox").forEach(cb => {

  cb.addEventListener("click", e => {

    const anySelected = (document.querySelectorAll(".announcement-select-checkbox:checked").length > 0);
    sakai.announcements.toggleBulkButtons(!anySelected);
    document.getElementById("announcement-reset-button").disabled = !anySelected;
  });
});

document.getElementById("announcement-reset-button")?.addEventListener("click", () => {
  sakai.announcements.toggleBulkButtons(true);
});

// Read receipts modal — one combined, sortable, colour-coded table.
(() => {

  const modal = document.getElementById("anncReceiptsModal");
  if (!modal) return;

  const table = document.getElementById("anncReceiptsTable");
  const yesLabel = modal.dataset.yes || "Yes";
  const noLabel = modal.dataset.no || "No";

  // Highlight toggle (in the modal body, above the table). The tint CSS is gated on the
  // .annc-highlight-on class, so toggling it just adds/removes that class.
  const highlightToggle = document.getElementById("anncReceiptsHighlightToggle");
  let highlightOn = highlightToggle ? highlightToggle.checked : false;
  if (highlightToggle) {
    highlightToggle.addEventListener("change", () => {
      highlightOn = highlightToggle.checked;
      render();
    });
  }

  const escapeHtml = value => {
    const div = document.createElement("div");
    div.textContent = value == null ? "" : value;
    return div.innerHTML;
  };

  let data = [];
  let sortKey = "status"; // default order: red (unseen, no email), yellow (unseen, email), green (viewed)
  let sortAsc = true;

  // red = not viewed & email off, yellow = not viewed & email on, green = viewed.
  const rowClass = row => row.viewed ? "annc-row-viewed" : (row.emailEnabled ? "annc-row-emailed" : "annc-row-noemail");
  const statusRank = row => row.viewed ? 2 : (row.emailEnabled ? 1 : 0);

  const sortValue = (row, key) => {
    if (key === "firstViewedSort") return row.firstViewedSort || 0;
    if (key === "emailEnabled") return row.emailEnabled ? 1 : 0;
    if (key === "status") return statusRank(row);
    return (row.sortName || row.displayName || "").toLowerCase();
  };

  const render = () => {
    const tbody = table.querySelector("tbody");
    const rows = data.slice().sort((a, b) => {
      const av = sortValue(a, sortKey), bv = sortValue(b, sortKey);
      let cmp = av < bv ? -1 : (av > bv ? 1 : 0);
      if (cmp === 0) { // stable secondary sort by name
        const an = sortValue(a, "sortName"), bn = sortValue(b, "sortName");
        cmp = an < bn ? -1 : (an > bn ? 1 : 0);
      }
      return sortAsc ? cmp : -cmp;
    });
    tbody.innerHTML = "";
    rows.forEach(row => {
      const tr = document.createElement("tr");
      if (highlightOn) tr.classList.add(rowClass(row));
      const viewedCell = row.viewed ? escapeHtml(row.firstViewed) : "";
      tr.innerHTML =
        `<td>${escapeHtml(row.displayName)}</td>` +
        `<td>${viewedCell}</td>` +
        `<td>${row.emailEnabled ? escapeHtml(yesLabel) : escapeHtml(noLabel)}</td>`;
      tbody.appendChild(tr);
    });
    table.querySelectorAll("thead th[data-sort-key]").forEach(th => {
      const icon = th.querySelector(".annc-sort-icon");
      if (!icon) return;
      icon.className = "annc-sort-icon fa " +
        (th.dataset.sortKey === sortKey ? (sortAsc ? "fa-sort-up" : "fa-sort-down") : "fa-sort");
    });
  };

  table.querySelectorAll("thead th[data-sort-key]").forEach(th => {
    th.addEventListener("click", () => {
      const clicked = th.dataset.sortKey;
      if (sortKey === clicked) { sortAsc = !sortAsc; } else { sortKey = clicked; sortAsc = true; }
      render();
    });
  });

  modal.addEventListener("show.bs.modal", event => {

    const ref = event.relatedTarget?.dataset.anncRef;
    const errorEl = document.getElementById("anncReceiptsError");
    const noneEl = document.getElementById("anncReceiptsNone");

    // Reset (keep the default status sort).
    errorEl.classList.add("d-none");
    errorEl.textContent = "";
    noneEl.classList.add("d-none");
    data = [];
    sortKey = "status";
    sortAsc = true;
    render();
    document.getElementById("anncReceiptsViewedCount").textContent = "0";
    document.getElementById("anncReceiptsTotalCount").textContent = "0";

    // Reference is /announcement/msg/{site}/{channel}/{msg}
    const parts = (ref || "").split("/");
    if (parts.length < 6) {
      errorEl.textContent = "Could not resolve this announcement.";
      errorEl.classList.remove("d-none");
      return;
    }
    const [ , , , site, channel, msg ] = parts;
    const url = `/direct/announcement/readReceipts/${site}/${channel}/${msg}.json`;

    fetch(url, { credentials: "same-origin", headers: { "Accept": "application/json" } })
      .then(response => {
        if (!response.ok) throw new Error(`Request failed with status ${response.status}`);
        return response.json();
      })
      .then(payload => {
        // EntityBroker wraps the returned map in a {"data": {...}} envelope
        const d = payload && payload.data ? payload.data : (payload || {});
        const viewed = (d.viewed || []).map(r => ({ ...r, viewed: true }));
        const notViewed = (d.notViewed || []).map(r => ({ ...r, viewed: false, firstViewed: "", firstViewedSort: 0 }));
        data = viewed.concat(notViewed);
        render();
        document.getElementById("anncReceiptsViewedCount").textContent = viewed.length;
        document.getElementById("anncReceiptsTotalCount").textContent = data.length;
        if (data.length === 0) noneEl.classList.remove("d-none");
      })
      .catch(error => {
        errorEl.textContent = error.message;
        errorEl.classList.remove("d-none");
      });
  });
})();
