import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiRubricEdit extends RubricsElement {

  static properties = { rubric: { type: Object } };

  constructor() {

    super();

    this.rubricClone = {};
  }

  set rubric(value) {

    const old = this._rubric;
    this._rubric = value;

    this.rubricClone = { ...value };
    if (this.rubricClone.new) {
      this.updateComplete.then(() => bootstrap.Modal.getOrCreateInstance(this.querySelector(`#edit-rubric-${value.id}`)).show());
    }

    this.requestUpdate("rubric", old);
  }

  get rubric() { return this._rubric; }

  firstUpdated() {

    const modal = this.querySelector(`#edit-rubric-${this.rubric.id}`);

    modal.addEventListener("shown.bs.modal", () => {
      modal.querySelector("input[type='text'").select();
    });

    modal.addEventListener("hidden.bs.modal", () => {
      this.rubric.new = false;
    });
  }

  render() {

    return html`
      <button class="btn btn-icon"
          type="button"
          data-bs-toggle="modal"
          data-bs-target="#edit-rubric-${this.rubric.id}"
          aria-controls="edit-rubric-${this.rubric.id}"
          aria-expanded="false"
          title="${this.tr("edit_rubric")}"
          aria-label="${this.tr("edit_rubric")}">
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
                    id="rubric-title-edit-${this.rubric.id}"
                    type="text"
                    value="${this.rubricClone.title}"
                    maxlength="255" autofocus>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-primary" type="button" @click=${this._saveEdit} data-rubric-id="${this.rubric.id}">
                ${this._i18n.save}
              </button>
              <button class="btn btn-secondary" type="button" data-bs-dismiss="modal">
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
    const title = e.target.closest(".modal-content").querySelector("input").value;
    document.getElementById(`rubric-edit-${e.target.dataset.rubricId}`).dispatchEvent(new CustomEvent("update-rubric-title", { detail: title }));
    bootstrap.Modal.getInstance(this.querySelector(`#edit-rubric-${e.target.dataset.rubricId}`)).hide();
  }
}
