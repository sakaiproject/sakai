import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import "../sakai-image-editor.js";

export class ImageEditorLauncher extends SakaiElement {

  static properties = { imageUrl: { attribute: "image-url", type: String } };

  constructor() {

    super();

    this.loadTranslations("image-editor");
  }

  close() {
    bootstrap.Modal.getInstance(this.querySelector("#image-editor-modal")).hide();
  }

  shouldUpdate() {
    return this.imageUrl;
  }

  render() {

    return html`
      <div class="modal fade" id="image-editor-modal" tabindex="-1" aria-labelledby="image-editor-modal-label" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h1 class="modal-title fs-5" id="image-editor-modal-label">${this._i18n.edit_image}</h1>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this._i18n.close}"></button>
            </div>
            <div class="modal-body">
              <sakai-image-editor image-url="${this.imageUrl}"></sakai-image-editor>
            </div>
          </div>
        </div>
      </div>

      <div class="text-center mt-4">
        <button type="button"
            class="btn btn-secondary"
            data-bs-toggle="modal"
            data-bs-target="#image-editor-modal">
          ${this._i18n.edit_image}
        </button>
      </div>
    `;
  }
}
