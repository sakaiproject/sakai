import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SharingChangeEvent } from "./SharingChangeEvent.js";
import "@sakai-ui/sakai-editor/sakai-editor.js";

export class SakaiRubricCriterionEdit extends RubricsElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    rubricId: { attribute: "rubric-id", type: String },
    criterion: { type: Object },
    isCriterionGroup: { attribute: "is-criterion-group", type: Boolean },
    textarea: { type: Boolean },
  };

  constructor() {

    super();

    this.criterion = {};
    this.criterionClone = {};
  }

  set criterion(value) {

    const old = this._criterion;
    this._criterion = value;
    this.criterionClone = { ...value };
    this.requestUpdate("criterion", old);
    if (this.criterionClone.new) {
      this.updateComplete.then(() => bootstrap.Modal.getOrCreateInstance(this.querySelector(`#edit-criterion-${value.id}`)).show());
    }
  }

  get criterion() { return this._criterion; }

  firstUpdated() {

    const modal = this.querySelector(`#edit-criterion-${this.criterion.id}`);

    modal.addEventListener("shown.bs.modal", () => {

      this.closest("[draggable='true']")?.setAttribute("draggable", "false");
      this.querySelector(`#criterion-title-edit-${this.criterion.id}`).select();
    });

    modal.addEventListener("hidden.bs.modal", () => {
      this.closest("[draggable='false']")?.setAttribute("draggable", "true");
    });
  }

  shouldUpdate(changedProperties) {
    return super.shouldUpdate(changedProperties) && this.criterion;
  }

  render() {

    return html`
      <button class="btn btn-icon edit-criterion-button"
          type="button"
          data-bs-toggle="modal"
          data-bs-target="#edit-criterion-${this.criterion.id}"
          aria-controls="edit-criterion-${this.criterion.id}"
          aria-expanded="false"
          title="${this._i18n.edit_criterion}"
          aria-label="${this._i18n.edit_criterion}">
        <i class="si si-edit"></i>
      </button>

      <div class="modal fade"
          id="edit-criterion-${this.criterion.id}"
          tabindex="-1"
          data-bs-backdrop="static"
          aria-labelledby="edit-criterion-${this.criterion.id}-label"
          aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fs-5" id="edit-criterion-${this.criterion.id}-label">${this.criterion.title}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this._i18n.close_dialog}"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="label-rubrics form-label" for="criterion-title-edit-${this.criterion.id}">
                  ${this._i18n.criterion_title}
                </label>
                <input title="${this._i18n.criterion_title}"
                    class="form-control"
                    id="criterion-title-edit-${this.criterion.id}"
                    type="text"
                    value="${this.criterionClone.title}"
                    maxlength="255">
              </div>
              <label class="label-rubrics form-label" for="criterion-title-edit-${this.criterion.id}">
                ${this.isCriterionGroup ? html`
                  ${this._i18n.criterion_group_description}
                ` : html`
                  ${this._i18n.criterion_description}
                `}
              </label>
              <sakai-editor id="criterion-title-edit-${this.criterion.id}"
                toolbar="BasicText"
                content="${ifDefined(this.criterionClone.description)}"
                @changed=${this.updateCriterionDescription}
                ?textarea=${this.textarea}>
              </sakai-editor>
            </div>
            <div class="modal-footer">
              <div>
                <button class="btn btn-primary" type="button" @click=${this._saveEdit}>
                  ${this._i18n.save}
                </button>
                <button class="btn btn-secondary" type="button" data-bs-dismiss="modal" @click=${this._cancelEdit}>
                  ${this._i18n.cancel}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  _saveEdit(e) {

    e.stopPropagation();

    const title = e.target.closest(".modal-content").querySelector("input").value;
    const description = e.target.closest(".modal-content").querySelector("sakai-editor").getContent();

    const body = JSON.stringify([
      { "op": "replace", "path": "/title", "value": title },
      { "op": "replace", "path": "/description", "value": description },
    ]);

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/${this.criterion.id}`;
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json-patch+json" },
      method: "PATCH",
      body
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while updating criterion at url ${url}`);
    })
    .then(criterion => {

      const originalEditor = document.getElementById(`criterion-edit-${this.criterion.id}`);
      originalEditor.dispatchEvent(new CustomEvent("criterion-edited", { detail: criterion }));
      originalEditor.dispatchEvent(new SharingChangeEvent());
    })
    .catch (error => console.error(error));

    bootstrap.Modal.getInstance(this.querySelector(`#edit-criterion-${this.criterion.id}`)).hide();
  }

  _cancelEdit() {

    this.criterion.new = false;
    this.criterionClone.new = false;
  }

  updateCriterionDescription(e) { this.criterionClone.description = e.detail.content; }
}
