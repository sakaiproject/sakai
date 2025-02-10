import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiRubricEdit extends RubricsElement {

  static properties = { rubric: { type: Object } };

  set rubric(value) {

    const old = this._rubric;
    this._rubric = value;

    this.rubricClone = { ...value };
    if (this.rubricClone.new) {
      this.updateComplete.then(() => bootstrap.Modal.getOrCreateInstance(this.querySelector("div.modal")).show());
    }

    this.requestUpdate("rubric", old);
  }

  get rubric() { return this._rubric; }

  shouldUpdate() {
    return !!this.rubric && this._i18n;
  }

  firstUpdated() {

    const modal = this.querySelector("div.modal");

    modal.addEventListener("shown.bs.modal", () => {
      modal.querySelector("input[type='text']").select();
    });
  }

  render() {

    return html`
      <button class="btn btn-icon edit-button"
          type="button"
          data-bs-toggle="modal"
          data-bs-target="#edit-rubric-${this.rubric.id}"
          aria-controls="edit-rubric-${this.rubric.id}"
          aria-expanded="false"
          title="${this.tr("edit_rubric")} ${this.rubric.title}"
          aria-label="${this.tr("edit_rubric")} ${this.rubric.title}">
        <i class="si si-edit"></i>
      </button>

      <div class="modal modal-sm fade"
          id="edit-rubric-${this.rubric.id}"
          tabindex="-1"
          data-bs-backdrop="static"
          aria-labelledby="edit-rubric-${this.rubric.id}-label"
          aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fs-5" id="edit-rubric-${this.rubric.id}-label">${this.rubric.title}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this._i18n.close_dialog}"></button>
            </div>
            <div class="modal-body">
              <div>
                <label class="label-rubrics form-label" for="rubric_title_edit">
                  ${this._i18n.rubric_title}
                </label>
                <input title="${this.tr("rubric_title")}"
                    class="form-control"
                    type="text"
                    value="${this.rubricClone?.title}"
                    maxlength="255" autofocus>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-primary" type="button" @click=${this._saveEdit} data-rubric-id="${this.rubric.id}">
                ${this._i18n.save}
              </button>
              <button class="btn btn-secondary" id="rubric-cancel-${this.rubric.id}" type="button" data-bs-dismiss="modal" @click=${this._cancelEdit}>
                ${this._i18n.cancel}
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  _saveEdit(e) {

    e.stopPropagation();
    const title = this.querySelector("input[type='text']").value;
    this.dispatchEvent(new CustomEvent("update-rubric-title", { detail: title }));
    bootstrap.Modal.getInstance(this.querySelector("div.modal")).hide();
  }

  _cancelEdit() {

    //Reset input values, in case they were changed
    this.querySelector("input[type='text']").value = this.rubric.title;
  }
}
