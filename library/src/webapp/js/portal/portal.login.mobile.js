class MobileLogin {

    constructor(element, config) {
        this._passConfig = config?.passwordField;
        this._element = element;
        this._element.addEventListener("click", this.showModal.bind(this));
    }

    async showModal() {
        //If modal dialog is not loaded yet, do it now 
        if (!this._loaded) {
            this.#createModalElement();
            await this.#loadLoginDialog();
            this.#adjustLoginDialog();
            this.passwordField = new PasswordField(this._modalEl.querySelector(".password-field"), this._passConfig);
            this._modal = new bootstrap.Modal(this._modalEl);
            this._loaded = true;
        }
        this._modal.show();
    }

    #createModalElement() {
        this._modalEl = document.createElement("div");
        this._modalEl.classList.add("modal", "mobile-login");
        this._modalEl.setAttribute("id", "mobileLoginModal");
        this._modalEl.setAttribute("tabindex", -1);
        document.body.appendChild(this._modalEl);
    }

    //Loads html from the xlogin endpoint and inserts the dialog into the modal
    async #loadLoginDialog() {
        const xloginQuery = await fetch('/portal/xlogin');
        if (xloginQuery.ok) {
            const xloginHtml = await xloginQuery.text();
            const parser = new DOMParser();
            const xloginDocument = parser.parseFromString(xloginHtml, 'text/html');
            const xloginDialog = xloginDocument.querySelector(".modal-dialog");
            this._modalEl.appendChild(xloginDialog);
        }
    }

    //Makes adjustments, to make the dialog viable to be displayed in a modal
    async #adjustLoginDialog() {
        //Remove d-none class from close button -> make it visible
        const closeModalButton = this._modalEl?.querySelector(".btn-close");
        closeModalButton?.classList.remove("d-none");

        //Add d-none class to the logo-container -> hide the logo
        const logoContainer = this._modalEl?.querySelector("#xlogin-logo");
        logoContainer?.classList.add("d-none");

        //Add d-none class to the logo-container -> hide the logo
        const cancelButton = this._modalEl?.querySelector("input[name='cancel']");
        cancelButton?.classList.add("d-none");
    }
}