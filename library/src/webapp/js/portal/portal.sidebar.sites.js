class SitesSidebar {

  constructor(element, config) {

    this._i18n = config?.i18n;
    this._element = element;
    this._subPageData = config?._subPageData;
    this._pinnedSiteList = document.getElementById("pinned-site-list");
    this._recentSiteList = document.getElementById("recent-site-list");
    this._currentSite = config?._currentSite;

    this._sitesAndToolsNav = document.querySelector("#portal-nav-sidebar");
    if (this._sitesAndToolsNav) {
        this._sitesAndToolsNav.addEventListener("mouseover", this.onHover.bind(this));
        this._sitesAndToolsNav.addEventListener("mouseout", this.onHoverOut.bind(this));
    }

    const sitesListItems = element.querySelectorAll(".site-list-item");

    const pinButtonElements = element.querySelectorAll(".site-opt-pin");
    pinButtonElements.forEach(buttonEl => new PinButton(buttonEl, { i18n: this._i18n?.pinButtons}));

    element.querySelectorAll(".site-description-button").forEach(buttonEl => new bootstrap.Popover(buttonEl));

    document.addEventListener("site-pin-change", this.handlePinChange.bind(this));

    element.querySelectorAll(".site-list-item-collapse").forEach(btn => {

      const chevron = element.querySelector(`[data-bs-target='#${btn.id}'] > i`);

      chevron.className = `bi-chevron-${btn.classList.contains("show") ? "down" : "right"}`;

      btn.addEventListener("show.bs.collapse", e => {

        e.stopPropagation();
        chevron.classList.replace("bi-chevron-right", "bi-chevron-down");
      });
      btn.addEventListener("hide.bs.collapse", e => {

        e.stopPropagation();
        chevron.classList.replace("bi-chevron-down", "bi-chevron-right");
      });
    });

    const update = (siteId, expanded) => {

      if (! portal.user.id) {
        return;
      }

      fetch(`/direct/userPrefs/updateKey/${portal.user.id}/sakai:portal:sitenav?currentExpanded=${expanded}&expandedSite=${siteId}`, { method: "PUT" })
      .then(r => {

        if (!r.ok) {
          console.error("Could not set current expanded state.");
        }
      });
    };

    const currentCollapseButton = element.querySelector("li.is-current-site .site-list-item-collapse");
    currentCollapseButton && currentCollapseButton.addEventListener("shown.bs.collapse", () => update(currentCollapseButton.dataset.siteId, "true"));
    currentCollapseButton && currentCollapseButton.addEventListener("hidden.bs.collapse", () => update(currentCollapseButton.dataset.siteId, "false"));

    this.setupSingleExpandedSite();

    // TODO: this needs to be implemented at some point. It would remove the annoying 
    // refresh message in the all sites sidebar
    /*
    document.body.addEventListener("site-pin-changed", e => {

      this.handlePinChange(e);
    });
    */
  }

  onHover() {
    const iconElement = document.querySelector("#sidebar-collapse-button span");
    if (iconElement) {
        const hoverIconClass = "portal-nav-sidebar-icon-hover";
        const expandedIconClass = "portal-nav-sidebar-icon";
        iconElement.classList.add(hoverIconClass);
        iconElement.classList.remove(expandedIconClass);
    }
  }

  onHoverOut() {
    const iconElement = document.querySelector("#sidebar-collapse-button span");
    if (iconElement) {
        const hoverIconClass = "portal-nav-sidebar-icon-hover";
        const expandedIconClass = "portal-nav-sidebar-icon";
        iconElement.classList.add(expandedIconClass);
        iconElement.classList.remove(hoverIconClass);
    }
  }

  setView(mobile) {

    const mobileClasses = ["portal-nav-sidebar-mobile", "offcanvas", "offcanvas-start"];
    const desktopClasses = ["portal-nav-sidebar-desktop"];

    this._element.style.visibility = "hidden";
    if (mobile) {
      //Set mobile view
      this._element.classList.add(...mobileClasses);
      this._element.classList.remove(...desktopClasses);
      this._element.addEventListener("hidden.bs.offcanvas", () => {
        this._element.style.visibility = "visible";
      }, { once: true })
      this._element.addEventListener("show.bs.offcanvas", () => {
        this._element.style.visibility = "visible";
      }, { once: true })
    } else {
      //Set desktop view
      
      //Check if we can find an offcanvas instance and dispose it
      bootstrap.Offcanvas.getInstance(this._element)?.dispose();

      this._element.classList.remove(...mobileClasses);
      this._element.classList.add(...desktopClasses);
      this._element.style.visibility = "visible";
    }
    this._element.classList.remove("d-none");
  }

  /**
   * Switches the attributes on the cloned pinned or unpinned site element
   *
   * @param {Element} clone - The cloned element that will be updated
   * @param {string} from - The site type to move from, either "pinned" or "recent"
   * @param {string} to - The site type to move to, either "pinned" or "recent"
   */
  _updatePinned(clone, from, to) {

    clone.id = clone.id.replace(from, to);
    clone.dataset.type = to;

    const pagesButton = clone.querySelector("button");
    pagesButton.dataset.bsTarget = pagesButton.dataset.bsTarget.replace(from, to);
    pagesButton.setAttribute("aria-controls", pagesButton.getAttribute("aria-controls").replace(from, to));

    const pagesCollapse = clone.querySelector("div.collapse");
    pagesCollapse.id = pagesCollapse.id.replace(from, to);

    if (to === "pinned") {
      this._pinnedSiteList.append(clone);
      this._pinnedSiteList.classList.remove("d-none");
    } else {
      this._recentSiteList.append(clone);
      this._recentSiteList.classList.remove("d-none");
    }

    new PinButton(clone.querySelector("button.site-opt-pin"), { i18n: this._i18n?.pinButtons });
  }

  async handlePinChange(event) {

    const pinButton = event.target;
    const pinned = event.detail.pinned;
    const siteId = event.detail.siteId;
    const timeStamp = new Date().valueOf();

    pinButton.setAttribute("disabled", "disabled");

    const favoritesReq = await fetch(`/portal/favorites/list?_${timeStamp}`);

    if (favoritesReq.ok) {
      const favoritesValues = await favoritesReq.json();
      const alreadyPinned = favoritesValues.favoriteSiteIds.includes(siteId);

      if (pinned !== alreadyPinned) {
        const payload = JSON.parse(JSON.stringify(favoritesValues));
        if (pinned) {
          payload.favoriteSiteIds.push(siteId);
        } else {
          payload.favoriteSiteIds.splice(payload.favoriteSiteIds.indexOf(siteId), 1);
        }

        const data = new URLSearchParams();
        data.append("userFavorites", JSON.stringify(payload));

        const url = "/portal/favorites/update";
        fetch(url, {
          credentials: "include",
          headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
          method: "POST",
          body: data,
        })
        .then(r => {

          if (!r.ok) {
            throw new Error(`Network error while updating pinned sites at url ${url}`);
          } else {
            const currentItem = pinButton.closest(".site-list-item");
            const clone = currentItem.cloneNode(true);

            if (pinned) {
              this._updatePinned(clone, "recent", "pinned");

              document.getElementById("sites-no-pinned-label").classList.add("d-none");

              currentItem.remove();

            } else {
              this._updatePinned(clone, "pinned", "recent");

              currentItem.remove();

              if (!this._pinnedSiteList.children.length) {
                this._pinnedSiteList.classList.add("d-none");
                document.getElementById("sites-no-pinned-label").classList.remove("d-none");
              }
            }

            const siteTitle = pinButton.dataset.siteTitle;
            document.body.dispatchEvent(new CustomEvent("site-pin-changed", { detail: { siteId, pinned, siteTitle }, bubbles: true }));

            if (!this._recentSiteList.children.length) {
              this._recentSiteList.parentElement.classList.add("d-none");
            } else {
              this._recentSiteList.parentElement.classList.remove("d-none");
            }
          }
        })
        .catch (error => console.error(error));
      }
    } else {
        console.error(`Failed to request favorites ${favoritesReq.text}`)
    }

    pinButton.removeAttribute("disabled");
  }

  /**
   * Control number of expanded sites
   */
  setupSingleExpandedSite() {
    const toolMenu = document.getElementById('toolMenu');
    if (!toolMenu) return;
    toolMenu.addEventListener('click', (event) => {
      const clickedButton = event.target.closest('button[data-bs-toggle="collapse"]');
      if (!clickedButton) return;
      const siteLi = clickedButton.closest('li.site-list-item');
      if (!siteLi || siteLi.classList.contains('is-current-site')) return;
      // Find all expanded sites except the current one
      const expandedSites = toolMenu.querySelectorAll('li.site-list-item .site-list-item-collapse.show');
      expandedSites.forEach((expandedSite) => {
        const parentLi = expandedSite.closest('li.site-list-item');
        if (parentLi && parentLi !== siteLi && !parentLi.classList.contains('is-current-site')) {
          const collapseButton = parentLi.querySelector('button[data-bs-toggle="collapse"]');
          if (collapseButton) {
            collapseButton.click();
          }
        }
      });
      // Ensure the newly expanded site remains visible
      setTimeout(() => {
        const listItems = Array.from(toolMenu.querySelectorAll('li.site-list-item'));
        const currentIndex = listItems.indexOf(siteLi.closest('li'));
        const previousLi = currentIndex > 0 ? listItems[currentIndex - 1] : undefined;
        if (previousLi) {
          let elementToScroll;
          if (previousLi.classList.contains('is-current-site')) {
            // If the previous <li> has the class .is-current-site, find the last tool title
            const lastToolLi = previousLi.querySelector('ul.site-page-list > li:last-child .btn-nav');
            if (lastToolLi) {
              elementToScroll = lastToolLi;
            }
          } else {
            // Otherwise, scroll to the .sidebar-site-title of the previous <li>
            elementToScroll = previousLi.querySelector('.sidebar-site-title');
          }
          if (elementToScroll) {
            elementToScroll.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }
        else {
          const firstSitesSection = toolMenu.querySelector('div.sites-section');
          if (firstSitesSection) {
            firstSitesSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }
      }, 250);
    });
  }
}

class PinButton {

  get title() {
    return this._element.title;
  }

  set title(newValue) {
    this._element.title = newValue;
  }

  get pinned() {
    return this._element.dataset.pinned === "true";
  }

  set pinned(newPinned) {
    this._element.dataset.pinned = newPinned;
  }

  constructor(element, config) {

    this._element = element;
    this._i18n = config?.i18n;
    this._site = element.dataset.pinSite;
    element.addEventListener("click", this.toggle.bind(this));
    this.title = element.dataset.pinned === "true" ? this._i18n.titleUnpin : this._i18n.titlePin;
  }

  toggle() {

    this.pinned = !this.pinned
    this.title = this.pinned ? this._i18n.titleUnpin : this._i18n.titlePin;
    this.toggleIcon()
    this.emitPinChange();
  }

  toggleIcon() {

    const buttonClasses =  this._element.classList;
    const pinnedIcon = "si-pin";
    const unPinnedIcon = "si-pin-fill";
    buttonClasses.toggle(pinnedIcon);
    buttonClasses.toggle(unPinnedIcon);
  }

  // Dispatches event which will cause a fetch to change pinned value
  emitPinChange() {

    const eventName = "site-pin-change";
    const eventPayload = { pinned: this.pinned, siteId: this._site };
    this._element.dispatchEvent(new CustomEvent(eventName, { detail: eventPayload, bubbles: true }));
  }
}
