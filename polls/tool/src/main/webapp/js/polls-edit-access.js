document.addEventListener("DOMContentLoaded", () => {
  const typeOfAccess = document.getElementById("type-of-access");
  const groupsWrapper = document.getElementById("poll-groups-wrapper");

  if (!typeOfAccess || !groupsWrapper) {
    return;
  }

  const updateGroupsVisibility = () => {
    const isGroupAccess = typeOfAccess.value === "GROUP";
    groupsWrapper.classList.toggle("d-none", !isGroupAccess);
    const groupSelect = groupsWrapper.querySelector("select");
    if (groupSelect) {
      groupSelect.disabled = !isGroupAccess;
    }
  };

  typeOfAccess.addEventListener("change", updateGroupsVisibility);
  updateGroupsVisibility();
});
