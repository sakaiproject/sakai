import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html } from "lit";

export class SakaiRubricModal extends SakaiShadowElement {

  static properties = {
    _mode: { state: true },
  };

  static openIn(ownerDocument, config) {

    let modal = ownerDocument.body.querySelector(":scope > sakai-rubric-modal");
    if (!modal) {
      modal = ownerDocument.createElement("sakai-rubric-modal");
      ownerDocument.body.prepend(modal);
    }

    return modal.open(config)
      .catch(error => {
        modal.remove();
        throw error;
      });
  }

  constructor() {

    super();

    this._i18nLoaded = this.loadTranslations("rubrics");
    this._openGeneration = 0;
  }

  async open(config) {

    const openGeneration = ++this._openGeneration;
    this._mode = config.mode;
    await this._i18nLoaded;
    await this.updateComplete;
    if (openGeneration !== this._openGeneration || !this.isConnected) {
      return this;
    }

    this._removeRubric();
    const rubric = this.ownerDocument.createElement("sakai-rubric-student");
    this.shadowRoot.querySelector(".modal-body").replaceChildren(rubric);
    rubric.loadRubric(config);

    const dialog = this.shadowRoot.querySelector("dialog");
    if (!dialog.open) {
      dialog.showModal();
    }

    return this;
  }

  close() {

    this._openGeneration += 1;
    const dialog = this.shadowRoot?.querySelector("dialog");
    if (dialog?.open) {
      dialog.close();
    } else {
      this._removeRubric();
      this.remove();
    }
  }

  disconnectedCallback() {

    this._removeRubric();
    super.disconnectedCallback();
  }

  _closed() {

    this._removeRubric();
    this.remove();
  }

  _removeRubric() {

    const rubric = this.shadowRoot?.querySelector("sakai-rubric-student");
    rubric?.handleClose();
    rubric?.remove();
  }

  shouldUpdate() {
    return Boolean(this._i18n);
  }

  render() {

    return html`
      <dialog class="modal-lg" closedby="any" aria-labelledby="rubric-modal-title" @close=${this._closed}>
        <div class="modal-content">
          <div class="modal-header">
            <h2 id="rubric-modal-title" class="modal-title h5">
              ${this._mode === "evaluation" ? this._i18n.grading_rubric : this._i18n.preview_rubric}
            </h2>
            <button type="button" class="btn-close" aria-label=${this._i18n.close_dialog} @click=${this.close}></button>
          </div>
          <div class="modal-body"></div>
        </div>
      </dialog>
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
      dialog {
        /* Bootstrap defines these on .modal for its nested modal classes. */
        --bs-modal-padding: 1rem;
        --bs-modal-bg: var(--sakai-background-color-1, var(--bs-body-bg));
        --bs-modal-border-color: var(--sakai-border-color, var(--bs-border-color-translucent));
        --bs-modal-border-width: var(--bs-border-width, 1px);
        --bs-modal-border-radius: var(--bs-border-radius-lg, 0.5rem);
        --bs-modal-inner-border-radius: calc(var(--bs-modal-border-radius) - var(--bs-modal-border-width));
        --bs-modal-header-padding-x: 1rem;
        --bs-modal-header-padding-y: 1rem;
        --bs-modal-header-padding: var(--bs-modal-header-padding-y) var(--bs-modal-header-padding-x);
        --bs-modal-header-border-color: var(--sakai-border-color, var(--bs-border-color));
        --bs-modal-header-border-width: var(--bs-border-width, 1px);
        --bs-modal-title-line-height: 1.5;
        box-sizing: border-box;
        width: calc(100% - 1rem);
        max-width: var(--bs-modal-width, 800px);
        padding: 0;
        border: 0;
        color: inherit;
        background: transparent;
      }

      dialog::backdrop {
        background-color: rgba(0, 0, 0, 0.5);
      }

    `,
  ];
}
