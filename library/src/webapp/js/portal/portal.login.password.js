class PasswordField {

    constructor(element, config) {
        this._element = element;
        this._icons = config.icons;
        this._passInput = this._element.querySelector("#pw");
        this._showPassCheck = this._element.querySelector("input[type='checkbox']");
        this._showPassCheck.addEventListener("click", this.#handleShowPassClick.bind(this));
        this._showPassIcon = this._element.querySelector(`[for="${this._showPassCheck.id}"] .bi`);
        window.addEventListener("keydown", this.#handleCapslock.bind(this));
    }

    async #handleShowPassClick() {
        this._showPassword = this._showPassCheck.checked;
        if (this._showPassCheck.checked) {
            this._showPassIcon.classList.replace(this._icons.hidden, this._icons.shown);
            this._passInput.setAttribute("type", "text") ;
        } else {
            this._showPassIcon.classList.replace(this._icons.shown, this._icons.hidden);
            this._passInput.setAttribute("type", "password") ;
        }
    }
    async #handleCapslock(event) {
        const prevcapsLockActive = this._capsLockActive;
        const capsMod = event.getModifierState("CapsLock");
        this._capsLockActive = event.key !== "CapsLock" ? capsMod : !capsMod; 

        if (this._capsLockActive === prevcapsLockActive) { return }

        const capsLockIndicatorId = "capsLockIndicator";
        if (this._capsLockActive) {
            const indicatorTemplate = `
                <span aria-hidden="true" class="input-group-text" id="${capsLockIndicatorId}">
                    <i class="bi ${this._icons.capsLock}"></i>
                </span>
            `
            this._element.insertAdjacentHTML("afterbegin", indicatorTemplate);
        } else {
            this._element.querySelector("#" + capsLockIndicatorId).remove();
        }
    }

}
