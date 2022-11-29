var portal = portal || {};
portal.search = portal.search || {};

portal.search.setup = options => {

  const search = document.querySelector("#sakai-search-panel sakai-search");
  options.tool ? search.setAttribute("tool", options.tool) : search.removeAttribute("tool");
  const label = document.getElementById("sakai-search-panel-label");
  label.innerHTML = options.tool ? `${portal.search.searchTranslation} ${portal.toolTitles[options.tool]}` : portal.search.defaultTitle;
};

document.addEventListener("DOMContentLoaded", () => {

  portal.search.searchTranslation = document.getElementById("sakai-search-translation")?.innerHTML;
  portal.search.defaultTitle = document.getElementById("sakai-search-default-title")?.innerHTML;

  const searchPanel = document.getElementById("sakai-search-panel");

  searchPanel?.addEventListener("shown.bs.offcanvas", e => {
    e.target.querySelector("sakai-search input")?.focus();
  });

  searchPanel?.addEventListener("hidden.bs.offcanvas", e => {
    e.target.querySelector("sakai-search")?.clear();
  });

  document.querySelectorAll(".portal-search-button").forEach(b => {
    b.addEventListener("click", () => portal.search.setup({}));
  });
});
