class SitesSidebar {

    constructor(element, config) {
        this._i18n = config?.i18n;
        this._element = element;
        this._lessonsSubpageData = config?.lessonsSubpageData;

        const sitesListItems = element.querySelectorAll(".site-list-item");
        //sitesListItems.forEach(sitesListItem => new LessonsSubPageNavigation(this._lessonsSubpageData))

        const pinButtonElements = element.querySelectorAll(".site-opt-pin");
        pinButtonElements.forEach((buttonEl) => new PinButton(buttonEl, { i18n: this._i18n?.pinButtons}));

        document.addEventListener("site-pin-change", this.handlePinChange)
    }

    async setView(mobile) {
        const mobileClasses = ["portal-nav-sidebar-mobile", "offcanvas", "offcanvas-start"];
        const desktopClasses = ["portal-nav-sidebar-desktop"];

        this._element.style.visibility = "hidden";
        if (mobile) {
            //Set mobile view
            mobileClasses.forEach((cssClass) => {
                this._element.classList.add(cssClass);
            });
            desktopClasses.forEach((cssClass) => {
                this._element.classList.remove(cssClass);
            });
            this._element.addEventListener("hidden.bs.offcanvas", () => {
                this._element.style.visibility = "visible";
            }, { once: true })
            this._element.addEventListener("show.bs.offcanvas", () => {
                this._element.style.visibility = "visible";
            }, { once: true })
        } else {
            //Set desktop view
            
            //Check if we can find an offcanvas instance and dispose it
            let offcanvas = bootstrap.Offcanvas.getInstance(this._element);
            if (offcanvas) {
                offcanvas.dispose();
            }

            mobileClasses.forEach((cssClass) => {
                this._element.classList.remove(cssClass);
            });
            desktopClasses.forEach((cssClass) => {
                this._element.classList.add(cssClass);
            });
            this._element.style.visibility = "visible";
        }
        this._element.classList.remove("d-none");
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
            const pinedRemote = favoritesValues.favoriteSiteIds.includes(siteId);

            if (pinned !== pinedRemote) {
                let payload = JSON.parse(JSON.stringify(favoritesValues));
                if (pinned) {
                    payload.favoriteSiteIds.push(siteId);
                } else {
                    payload.favoriteSiteIds.splice(payload.favoriteSiteIds.indexOf(siteId), 1);
                }

                /*
                const updateReq = await fetch(`/portal/favorites/update`, {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: JSON.stringify(payload)
                });

                if (updateReq.ok) {
                    console.log("OK")
                } else {
                    console.error(`Could not set pinned value ${await updateReq.text()}`);
                }
                */

                //Fetch did not work for some reason :(
                $PBJQ.ajax({
                        url: '/portal/favorites/update',
                        method: 'POST',
                        dataType: 'json',
                        data: {
                        userFavorites: JSON.stringify(payload),
                    }, error: function(res) {
                        console.error(`Error setting pinned status for site ${siteId}: ${res.responseText}`)
                    }
                });
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
        element.addEventListener("click", this.toggle.bind(this));
    }

    toggle() {
        this.pinned = !this.pinned
        this.title = this.pinned ? this._i18n.titleUnpin : this._i18n.titlePin;
        this.toggleIcon()
        this.emitPinChange();
    }

    toggleIcon() {
        const buttonClasses =  this._element.classList;
        const pinnedIcon = "bi-pin";
        const unPinnedIcon = "bi-pin-fill";
        buttonClasses.toggle(pinnedIcon);
        buttonClasses.toggle(unPinnedIcon);
    }

    //Dispatches event which will cause a fetch to cange pinned value
    emitPinChange() {
        const eventName = "site-pin-change";
        const eventPayload = {
            pinned: this.pinned,
            siteId: this._site
        };
        this._element.dispatchEvent(new CustomEvent(eventName, { "bubbles": true, "detail": eventPayload }));
    }
}
