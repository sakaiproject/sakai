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

  set criterion(newValue) {

    const oldValue = this._criterion;
    this._criterion = newValue;
    this.criterionClone = { ...newValue };
    this.requestUpdate("criterion", oldValue);
    if (this.criterionClone.isNew) {
      this.updateComplete.then(() => this.querySelector(".edit").click() );
    }
  }

  get criterion() { return this._criterion; }

  firstUpdated() {

    const buttonTrigger = this.querySelector("button");

    new bootstrap.Popover(buttonTrigger, {
      content: () => this.querySelector(`#edit-criterion-${this.criterion.id}`).innerHTML,
      customClass: "criterion-edit-popover",
      html: true,
      placement: "bottom",
      sanitize: false,
    });

    buttonTrigger.addEventListener("shown.bs.popover", () => {

      const save = document.querySelector(".popover.show .btn-primary");
      save.addEventListener("click", this.saveEdit);

      const titleInput = save.closest(".popover-body").querySelector("input");
      titleInput.setSelectionRange(0, titleInput.value.length);
      titleInput.focus();

      document.querySelector(".popover.show .btn-secondary").addEventListener("click", this.cancelEdit);
    });

    // It seems that the bootstrap popover removes the title attribute from the trigger when it makes it the title of the popover.
    // Adding it back to the trigger.
    buttonTrigger.title = buttonTrigger.dataset.preserveTitle;
  }

  shouldUpdate(changedProperties) {
    return super.shouldUpdate(changedProperties) && this.criterion;
  }

  render() {

    return html`
      <button class="btn btn-icon edit"
          id="edit-criterion-trigger-${this.criterion.id}"
          type="button"
          data-bs-toggle="popup"
          aria-haspopup="true"
          aria-expanded="false"
          aria-controls="edit-criterion-${this.criterion.id}"
          title="${this._i18n.edit_criterion} ${this.criterion.title}"
          data-preserve-title="${this._i18n.edit_criterion} ${this.criterion.title}"
          aria-label="${this._i18n.edit_criterion} ${this.criterion.title}">
        <i class="si si-edit"></i>
      </button>

      <div id="edit-criterion-${this.criterion.id}" class="criterion-edit-popover d-none">
        <div>
          <div>
            <label class="label-rubrics" for="criterion-title-edit-${this.criterion.id}">
              ${this._i18n.criterion_title}
            </label>
            <input title="${this._i18n.criterion_title}" id="criterion-title-edit-${this.criterion.id}" type="text" value="${this.criterionClone.title}" maxlength="255">
          </div>
        </div>
        <div class="form">
          <div class="form-group">
            <label class="label-rubrics" for="criterion-title-edit-${this.criterion.id}">
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

          <div class="mt-2">
            <div>
              <button class="btn btn-primary" type="button" data-site-id="${this.siteId}" data-rubric-id="${this.rubricId}" data-criterion-id="${this.criterion.id}">
                ${this._i18n.save}
              </button>
              <button class="btn btn-secondary" type="button" data-criterion-id="${this.criterion.id}">
                ${this._i18n.cancel}
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  cancelEdit(e) {

    e.stopPropagation();
    const trigger = document.getElementById(`edit-criterion-trigger-${this.dataset.criterionId}`);
    bootstrap.Popover.getInstance(trigger).hide();
    trigger.focus();
  }

  saveEdit(e) {

    e.stopPropagation();

    const title = e.target.closest(".popover-body").querySelector("input").value;
    const description = e.target.closest("div.form").querySelector("sakai-editor").getContent();

    const body = JSON.stringify([
      { "op": "replace", "path": "/title", "value": title },
      { "op": "replace", "path": "/description", "value": description },
    ]);

    const url = `/api/sites/${this.dataset.siteId}/rubrics/${this.dataset.rubricId}/criteria/${this.dataset.criterionId}`;
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

      throw new Error("Network error while updating criterion");
    })
    .then(criterion => {

      const originalEditor = document.getElementById(`criterion-edit-${this.dataset.criterionId}`);
      originalEditor.dispatchEvent(new CustomEvent("criterion-edited", { detail: criterion }));
      originalEditor.dispatchEvent(new SharingChangeEvent());
    })
    .catch (error => console.error(error));

    const trigger = document.getElementById(`edit-criterion-trigger-${this.dataset.criterionId}`);
    bootstrap.Popover.getInstance(trigger).hide();
    trigger.focus();
    document.getElementById(`edit-criterion-${this.dataset.criterionId}`).style.display = "none";
  }

  updateCriterionDescription(e) {
    this.criterionClone.description = e.detail.content;
  }
}
