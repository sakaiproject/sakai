var portal = portal || {};
portal.search = portal.search || {};

portal.search.setup = options => {

  const search = document.querySelector("#sakai-search-panel sakai-search");
  options.tool ? search.setAttribute("tool", options.tool) : search.removeAttribute("tool");
  options.site ? search.setAttribute("site-id", options.site) : search.removeAttribute("site-id");
  const label = document.getElementById("sakai-search-panel-label");
  label.innerHTML = options.tool ? `${portal.search.searchTranslation} ${portal.toolTitles[options.tool]}` : portal.search.defaultTitle;
};

document.addEventListener("DOMContentLoaded", () => {

  portal.search.searchTranslation = document.getElementById("sakai-search-translation")?.innerHTML;
  portal.search.defaultTitle = document.getElementById("sakai-search-default-title")?.innerHTML;

  document.getElementById("sakai-search-panel")?.addEventListener("shown.bs.offcanvas", e => {
    e.target.querySelector("sakai-search input")?.focus();
  });

  document.querySelectorAll(".portal-search-button").forEach(b => {
    b.addEventListener("click", () => portal.search.setup({}));
  });

  document.getElementById("sakai-calendar-panel").addEventListener("show.bs.offcanvas", e => {
    e.target.querySelector("sakai-calendar")?.loadData();
  });

  const notificationsPanel = document.getElementById("sakai-notifications-panel");
  notificationsPanel?.addEventListener("show.bs.offcanvas", e => {
    e.target.querySelector("sakai-notifications")?.loadNotifications();
  });

  notificationsPanel?.addEventListener("hidden.bs.offcanvas", e => {
    e.target.querySelector("sakai-notifications")?._clearTestNotifications();
  });
});

portal.displayProfile = e => {

  document.querySelector("sakai-account").setAttribute("user-id", e.target.dataset.userId);
  bootstrap.Modal.getOrCreateInstance(document.getElementById("account-details-modal")).show();
};
