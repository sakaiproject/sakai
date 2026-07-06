sakai.announcements = sakai.announcements || {

  toggleBulkButtons: disable => {

    ["remove", "publish", "unpublish"].forEach(action => {
      document.getElementById(`announcement-${action}-button`).disabled = disable;
    });
  },

  refreshBulkButtons: () => {

    const anySelected = (document.querySelectorAll(".announcement-select-checkbox:checked").length > 0);
    sakai.announcements.toggleBulkButtons(!anySelected);
    const reset = document.getElementById("announcement-reset-button");
    if (reset) reset.disabled = !anySelected;
  },
};

// Delegate on document so the wiring survives the shared search macro replacing
// the table body with fresh rows (see searchFilterPanelMacro.js); binding to the
// checkboxes directly would be lost the moment tbody.innerHTML is swapped.
document.addEventListener("click", e => {
  if (e.target.matches(".announcement-select-checkbox")) {
    sakai.announcements.refreshBulkButtons();
  }
});

// A search/clear swaps in fresh (unchecked) rows, so re-sync the bulk buttons.
document.addEventListener("sfp:updated", () => sakai.announcements.refreshBulkButtons());

document.getElementById("announcement-reset-button")?.addEventListener("click", () => {
  sakai.announcements.toggleBulkButtons(true);
});
