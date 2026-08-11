import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html } from "lit";

export class SakaiRubricModal extends SakaiShadowElement {

  constructor() {

    super();

    this._i18nLoaded = this.loadTranslations("rubrics");
  }

  async open(config) {

    await this._i18nLoaded;
    await this.updateComplete;

    const rubric = this.ownerDocument.createElement("sakai-rubric-student");
    this.shadowRoot.querySelector(".modal-body").replaceChildren(rubric);
    rubric.loadRubric(config);
    this.shadowRoot.querySelector("dialog").showModal();
  }

  close() {

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
            <h2 id="rubric-modal-title" class="modal-title h5">${this._i18n.preview_rubric}</h2>
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
