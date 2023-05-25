import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {tr} from "./sakai-rubrics-language.js";
import "../sakai-editor.js";

export class SakaiRubricCriterionEdit extends RubricsElement {

  constructor() {

    super();

    this.criterion = {};
    this.criterionClone = {};
  }

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      rubricId: { attribute: "rubric-id", type: String },
      criterion: { type: Object, notify: true },
      isCriterionGroup: {attribute: "is-criterion-group", type: Boolean},
    };
  }

  set criterion(newValue) {

    const oldValue = this._criterion;
    this._criterion = newValue;
    this.criterionClone = JSON.parse(JSON.stringify(newValue));
    this.requestUpdate("criterion", oldValue);
    if (this.criterionClone.isNew) {
      this.updateComplete.then(() => this.querySelector(".edit").click() );
    }
  }

  get criterion() { return this._criterion; }

  firstUpdated() {

    new bootstrap.Popover(this.querySelector("button"), {
      content: () => this.querySelector(`#edit-criterion-${this.criterion.id}`).innerHTML,
      customClass: "criterion-edit-popover",
      html: true,
      placement: "bottom",
      sanitize: false,
    });

    this.querySelector("button").addEventListener("shown.bs.popover", () => {

      const save = document.querySelector(".popover.show .btn-primary");
      save.addEventListener("click", this.saveEdit);
      save.closest(".popover-body").querySelector("input").focus();

      document.querySelector(".popover.show .btn-secondary")
        .addEventListener("click", this.cancelEdit);
    });
  }

  render() {

    return html`
      <button class="btn btn-icon edit"
          id="edit-criterion-trigger-${this.criterion.id}"
          type="button
          data-bs-toggle="popup"
          aria-haspopup="true"
          aria-expanded="false"
          aria-controls="edit-criterion-${this.criterion.id}"
          title="${tr("edit_criterion")} ${this.criterion.title}"
          aria-label="${tr("edit_criterion")} ${this.criterion.title}">
        <i class="si si-edit"></i>
      </button>

      <div id="edit-criterion-${this.criterion.id}" class="criterion-edit-popover d-none">
        <div>
          <div>
            <label class="label-rubrics" for="criterion-title-edit-${this.criterion.id}">
              <sr-lang key="criterion_title">Criterion Title</sr-lang>
            </label>
            <input title="${tr("criterion_title")}" id="criterion-title-edit-${this.criterion.id}" type="text" value="${this.criterionClone.title}" maxlength="255">
          </div>
        </div>
        <div class="form">
          <div class="form-group">
            <label class="label-rubrics" for="criterion-description-field-${this.criterion.id}">
              ${this.isCriterionGroup ? html`
                <sr-lang key="criterion_group_description">Criterion Group Description</sr-lang>
              ` : html`
                <sr-lang key="criterion_description">Criterion Description</sr-lang>
              `}
            </label>
            <sakai-editor
              toolbar="BasicText"
              content="${this.criterionClone.description ? `${this.criterionClone.description}` : ``}"
              @changed="${this.updateCriterionDescription}"
              id="criterion-description-field-${this.criterion.id}">
            </sakai-editor>
          </div>

          <div class="mt-2">
            <div>
              <button class="btn btn-primary" type="button" data-site-id="${this.siteId}" data-rubric-id="${this.rubricId}" data-criterion-id="${this.criterion.id}">
                <sr-lang key="save">Save</sr-lang>
              </button>
              <button class="btn btn-secondary" type="button" data-criterion-id="${this.criterion.id}">
                <sr-lang key="cancel">Cancel</sr-lang>
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

const tagName = "sakai-rubric-criterion-edit";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricCriterionEdit);
