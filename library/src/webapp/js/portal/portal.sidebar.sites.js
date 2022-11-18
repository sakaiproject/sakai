class SitesSidebar {

  constructor(element, config) {

    this._i18n = config?.i18n;
    this._element = element;
    this._lessonsSubpageData = config?.lessonsSubpageData;

    const sitesListItems = element.querySelectorAll(".site-list-item");
    //sitesListItems.forEach(sitesListItem => new LessonsSubPageNavigation(this._lessonsSubpageData))

    const pinButtonElements = element.querySelectorAll(".site-opt-pin");
    pinButtonElements.forEach((buttonEl) => new PinButton(buttonEl, { i18n: this._i18n?.pinButtons}));

    document.addEventListener("site-pin-change", this.#handlePinChange);

    element.querySelectorAll(".site-list-item-collapse").forEach(btn => {

      const chevron = element.querySelector(`[data-bs-target='#${btn.id}'] > i`);
      btn.addEventListener("show.bs.collapse", e => {

        e.stopPropagation();
        chevron.classList.replace("bi-chevron-right", "bi-chevron-down");
      });
      btn.addEventListener("hide.bs.collapse", e => {

        e.stopPropagation();
        chevron.classList.replace("bi-chevron-down", "bi-chevron-right");
      });
    });
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

  async #handlePinChange(event) {

    const pinButton = event.target;
    const pinned = event.detail.pinned;
    const siteId = event.detail.siteId;
    const timeStamp = new Date().valueOf();

    pinButton.setAttribute("disabled", "disabled");

    const favoritesReq = await fetch(`/portal/favorites/list?_${timeStamp}`);

    if (favoritesReq.ok) {
      const favoritesValues = await favoritesReq.json();
      const pinedRemote = favoritesValues.favoriteSiteIds.includes(siteId);

      if (pinned !== pinedRemote) {
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
          }
        })
        .catch (error => console.error(error));
      } else {
        //Nothing to do
      }
    } else {
        console.error(`Failed to request favorites ${favoritesReq.text}`)
    }

    pinButton.removeAttribute("disabled");
  }
}

class PinButton {

  get title() {
    return this._element.getAttribute("title");
  }

  set title(newValue) {
    this._element.setAttribute("title", newValue);
    this._tooltip.setContent({'.tooltip-inner': newValue});
  }

  get pinned() {
    return this._element.getAttribute("data-pinned") == "true" ? true : false;
  }

  set pinned(newPinned) {
    this._element.setAttribute("data-pinned", newPinned);
  }

  constructor(element, config) {

    this._element = element;
    this._i18n = config?.i18n;
    this._site = element.getAttribute("data-pin-site");
    this._tooltip = bootstrap.Tooltip.getOrCreateInstance(this._element);
    element.addEventListener("click", this.#toggle.bind(this));
  }

  #toggle() {

    this.pinned = !this.pinned
    this.title = this.pinned ? this._i18n.titleUnpin : this._i18n.titlePin;
    this.#toggleIcon()
    this.#emitPinChange();
  }

  #toggleIcon() {

    const buttonClasses =  this._element.classList;
    const pinnedIcon = "bi-pin";
    const unPinnedIcon = "bi-pin-fill";
    buttonClasses.toggle(pinnedIcon);
    buttonClasses.toggle(unPinnedIcon);
  }

  //Dispatches event which will cause a fetch to cange pinned value
  #emitPinChange() {

    const eventName = "site-pin-change";
    const eventPayload = { pinned: this.pinned, siteId: this._site };
    this._element.dispatchEvent(new CustomEvent(eventName, { detail: eventPayload, bubbles: true }));
  }
}
