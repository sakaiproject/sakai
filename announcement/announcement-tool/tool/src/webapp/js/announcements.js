sakai.announcements = sakai.announcements || {

  toggleBulkButtons: disable => {

    ["remove", "publish", "unpublish"].forEach(action => {
      const button = document.getElementById(`announcement-${action}-button`);
      if (button) button.disabled = disable;
    });
  },

  refreshBulkButtons: () => {

    const checkboxes = [...document.querySelectorAll(".announcement-select-checkbox:enabled")];
    const selectedCount = checkboxes.filter(checkbox => checkbox.checked).length;
    const anySelected = selectedCount > 0;
    const selectAll = document.getElementById("announcement-select-all");
    if (selectAll) {
      selectAll.checked = anySelected && selectedCount === checkboxes.length;
      selectAll.indeterminate = anySelected && selectedCount < checkboxes.length;
      selectAll.disabled = checkboxes.length === 0;
    }
    sakai.announcements.toggleBulkButtons(!anySelected);
    const reset = document.getElementById("announcement-reset-button");
    if (reset) reset.disabled = !anySelected;
  },
};

// Delegate on document so the wiring survives the shared search macro replacing
// the table body with fresh rows (see searchFilterPanelMacro.js); binding to the
// checkboxes directly would be lost the moment tbody.innerHTML is swapped.
document.addEventListener("change", e => {
  if (e.target.matches("#announcement-select-all")) {
    document.querySelectorAll(".announcement-select-checkbox:enabled").forEach(checkbox => {
      checkbox.checked = e.target.checked;
    });
  }
  if (e.target.matches(".announcement-select-checkbox, #announcement-select-all")) {
    sakai.announcements.refreshBulkButtons();
  }
});

// A search/clear swaps in fresh (unchecked) rows, so re-sync the bulk buttons.
document.addEventListener("sfp:updated", () => sakai.announcements.refreshBulkButtons());

document.getElementById("announcement-reset-button")?.form.addEventListener("reset", () => {
  // The reset event fires before the browser restores the checkboxes.
  requestAnimationFrame(() => sakai.announcements.refreshBulkButtons());
});

sakai.announcements.refreshBulkButtons();
