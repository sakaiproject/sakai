class SidebarCollapseButton {

  get collapsed() {
    return this._element.getAttribute("data-portal-sidebar-collapsed") === "true" ? true : false;
  }

  set collapsed(newValue) {

    this._element.setAttribute("data-portal-sidebar-collapsed", newValue);
    this.setCollapsed(newValue)
  }

  constructor(element, config) {

    this._i18n = config?.i18n;
    this._toggleClass = config?.toggleClass;
    this._portalContainer = config?.portalContainer;
    this._sitesSidebar = config?.sitesSidebar;
    this._element = element;
    this._element.addEventListener("click", this.toggle.bind(this));
  }

  toggle() {

    this.collapsed = !this.collapsed;
    this._portalContainer.classList.toggle(this._toggleClass, this.collapsed);
    this._sitesSidebar.classList.toggle(this._toggleClass, this.collapsed);
    this._element.title = this.collapsed ? this._i18n.titleCollapsed : this._i18n.titleExpanded;

    const iconElement = this._element.querySelector("span");
    if (iconElement) {
        const collapsedIconClass = "portal-nav-sidebar-icon-collapsed";
        const expandedIconClass = "portal-nav-sidebar-icon";
        iconElement.classList.toggle(collapsedIconClass, this.collapsed);
        iconElement.classList.toggle(expandedIconClass, !this.collapsed);
    }
  }

  async setCollapsed(collapsed) {

    if (!portal?.user?.id) {
      return; // Exit the function early if the user is not logged in.
    }

    collapsed = collapsed ? "true" : "false";
    const putReq = await fetch(`/direct/userPrefs/updateKey/${portal.user.id}/sakai:portal:sitenav?sidebarCollapsed=${collapsed}`, { method: "PUT" });
    if (!putReq.ok) {
      console.error(`Could not set collapsed state "${collapsed}" for sidebar.`);
    }
  }
}
