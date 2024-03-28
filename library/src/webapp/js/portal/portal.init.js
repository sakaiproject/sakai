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

  if (portal?.user?.id && Notification?.permission !== "granted") {
    document.querySelectorAll(".portal-notifications-no-permissions-indicator").forEach(b => b.classList.remove("d-none"));
  }
});
