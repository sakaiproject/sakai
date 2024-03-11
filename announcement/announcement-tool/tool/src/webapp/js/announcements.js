sakai.announcements = sakai.announcements || {

  toggleBulkButtons: disable => {

    document.getElementById("announcement-remove-button").disabled = disable;
    document.getElementById("announcement-publish-button").disabled = disable;
    document.getElementById("announcement-unpublish-button").disabled = disable;
  },
};

document.querySelectorAll(".announcement-select-checkbox").forEach(cb => {

  cb.addEventListener("click", e => {

    const anySelected = (document.querySelectorAll(".announcement-select-checkbox:checked").length > 0);
    sakai.announcements.toggleBulkButtons(!anySelected);
    document.getElementById("announcement-reset-button").disabled = !anySelected;
  });
});

document.getElementById("announcement-reset-button").addEventListener("click", () => {
  sakai.announcements.toggleBulkButtons(true);
});
