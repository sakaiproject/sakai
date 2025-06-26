class SubPageNavigation {

  constructor(data) {
    if (!data.hasOwnProperty('pages')) {
      console.warn('No page data for SubPageNavigation');
      return;
    }

    if (!data.hasOwnProperty('i18n')) {
      console.warn('No i18n data for SubPageNavigation');
      return;
    }

    this.data = data.pages;
    this.i18n = data.i18n;
    this.siteId = data.siteId;
    this.isInstructor = data.isInstructor;

    this.topLevelPageProps = {};
    data.topLevelPageProps.forEach((p) => this.topLevelPageProps[p.toolId] = p);

    this.setup();
  }

  setup() {
    Object.keys(this.data).forEach(pageId => {
      if (this.topLevelPageProps.hasOwnProperty(pageId)) {
        this.renderSubnavForPage(pageId, this.data[pageId], this.topLevelPageProps[pageId]);
      }
    });
  }

  renderSubnavForPage(pageId, subpages, props) {
    const element = document.querySelector(`#toolMenu a[href*="/tool/${pageId}"], #toolMenu [href*="/tool-reset/${pageId}"]`);
    let pageName = element.innerText?.trim() || this.topLevelPageProps[pageId].name;
    const siteListItem = element.parentElement;
    const mainLink = element.href?.replace(/\/tool\//, "/tool-reset/");

    const collapseId = `page-${pageId}-lessons-subpages`;
    const isExpanded = subpages[0].toolId === this.getCurrentPlacement();
    const template = `
            <div class="d-inline-flex align-items-stretch">
                <button class="btn btn-nav btn-subsite rounded-end text-start ${(isExpanded) ? "" : "collapsed"} border-0 ps-4"
                        data-bs-toggle="collapse"
                        data-bs-target="#${collapseId}"
                        aria-expanded="${(isExpanded) ? "true" : "false"}"
                        aria-controls="${collapseId}">
                    <i class="${(isExpanded) ? "bi-chevron-down" : "bi-chevron-right"}" aria-hidden="true"></i>
                    <span>${pageName}</span>
                </button>
            </div>
            <div id="${collapseId}" class="lessons-subpages-collapse ${(isExpanded) ? "show" : "collapse"}">
                <ul class="nav flex-column pe-2">
                    <li class="nav-item">
                        <a class="btn btn-nav rounded-end text-start ps-5" href="${mainLink}">
                            <i class="me-2 si si-sakai-lessonbuildertool" aria-hidden="true"></i>
                            <span>${this.i18n.main_link_name}</span>
                            ${(props.disabled === 'true') ? `<i class="bi-slash-circle ms-2"></i>` : ""}
                            ${(props.hidden === 'true' && props.disabled !== 'true') ? `<i class = "si si-hidden ms-2"></i>` : ""}
                        </a>
                    </li>
                    ${subpages.map((subpage) => `
                        <li class="nav-item">
                            <a class="btn btn-nav rounded-end text-start ps-5 ${((props.disabled === 'true' && props.disabledDueToPrerequisite === 'true') || (subpage.disabled === 'true' && subpage.disabledDueToPrerequisite === 'true')) ? `disabled` : ``}" href="${this.buildSubpageUrlFor(subpage)}">
                                <i class="me-2 bi bi-arrow-return-right" aria-hidden="true"></i>
                                <span>${subpage.name}</span>
                                ${(props.disabled === 'true' || subpage.disabled === 'true') ? `<i class="bi-slash-circle ms-2"></i>` : ``}
                                ${(subpage.hidden === 'true' && !(props.disabled === 'true' || subpage.disabled === 'true')) ? `<i class="si si-hidden ms-2"></i>` : ``}
                            </a>
                        </li>
                    `).join("")}
                </ul>
            </div>
        `;
    element.remove();
    siteListItem.insertAdjacentHTML("afterbegin", template);

    window.addEventListener("DOMContentLoaded", () => {

      const collapseEl = document.getElementById(collapseId);
      const chevron = document.querySelector(`[data-bs-target='#${collapseId}'] > i`);
      collapseEl.addEventListener("show.bs.collapse", e => {
        e.stopPropagation();
        chevron.classList.replace("bi-chevron-right", "bi-chevron-down");
      });

      collapseEl.addEventListener("hide.bs.collapse", e => {
        e.stopPropagation();
        chevron.classList.replace("bi-chevron-down", "bi-chevron-right");
      });
    });
  }

  buildSubpageUrlFor(subpage) {
    return `/portal/site/${subpage.siteId}`
        + `/tool/${subpage.toolId}`
        + `/ShowPage?sendingPage=${subpage.sendingPage}`
        + `&itemId=${subpage.itemId}`
        + "&path=clear_and_push"
        + `&title=${subpage.name}`
        + "&newTopLevel=false";
  }

  getCurrentPlacement() {
    const parts = (new URL(window.location.href)).pathname.split('/');
    return parts.length >= 6 ? parts[5] : '';
  }

  getSubPageElement() {
    const subPageNavToolIdInput = document.getElementById('lessonsSubnavToolId');
    const subPageNavPageIdInput = document.getElementById('lessonsSubnavPageId');
    const subPageNavItemIdInput = document.getElementById('lessonsSubnavItemId');
    let subPageElement = null;

    if (subPageNavToolIdInput && subPageNavPageIdInput && subPageNavItemIdInput) {
      subPageElement = document.querySelector(`#toolMenu a[href*="/tool/${subPageNavToolIdInput.value}/ShowPage?sendingPage=${subPageNavPageIdInput.value}&itemId=${subPageNavItemIdInput.value}&"]`);
    }

    // If the current page is not a subpage, then highlight the main page.
    if (!subPageElement && subPageNavToolIdInput) {
      subPageElement = document.querySelector(`#toolMenu a[href$="/tool-reset/${subPageNavToolIdInput.value}"]`);
    }

    return subPageElement;
  }

  setCurrentPage() {
    let subpageElement = this.getSubPageElement();

    if (!subpageElement) {
      // We're not on a ShowPage (e.g., Index of Pages, ShowItem, etc.), so highlight Main Page in the site nav.
      subpageElement = document.querySelector(`#toolMenu a[href$="/tool-reset/${this.getCurrentPlacement()}"]`);
    }
    subpageElement?.classList.add("selected-page");
  }
}

window.SubPageNavigation = SubPageNavigation;
