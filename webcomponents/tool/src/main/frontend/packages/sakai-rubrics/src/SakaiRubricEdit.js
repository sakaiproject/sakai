import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiRubricEdit extends RubricsElement {

  static properties = { rubric: { type: Object } };

  constructor() {

    super();

    this.rubricClone = {};
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "rubric") {
      this.rubricClone = JSON.parse(newValue);
      if (this.rubricClone.new) {
        this.updateComplete.then(() => this.querySelector(".edit").click() );
      }
    }
  }


  render() {

    return html`
      <button class="btn btn-icon edit"
          id="edit-rubric-trigger-${this.rubric.id}"
          type="button"
          data-bs-toggle="popup"
          aria-haspopup="true"
          aria-expanded="false"
          aria-controls="edit-rubric-${this.rubric.id}"
          title="${this.tr("edit_rubric")}"
          data-preserve-title="${this.tr("edit_rubric")}"
          aria-label="${this.tr("edit_rubric")} ${this.rubric.title}">
        <i class="si si-edit"></i>
      </button>

      <div id="edit-rubric-${this.rubric.id}" class="rubric-edit-popover d-none">
        <div>
          <div>
            <label class="label-rubrics" for="rubric_title_edit">
              ${this._i18n.rubric_title}
            </label>
            <input title="${this.tr("rubric_title")}" id="rubric-title-edit-${this.rubric.id}" type="text" value="${this.rubricClone.title}" maxlength="255">
          </div>
        </div>
        <div class="mt-2">
          <div>
            <button class="btn btn-primary" type="button" data-rubric-id="${this.rubric.id}">
              ${this._i18n.save}
            </button>
            <button class="btn btn-secondary" type="button" data-rubric-id="${this.rubric.id}">
              ${this._i18n.cancel}
            </button>
          </div>
        </div>
      </div>
    `;
  }

  firstUpdated() {
    const buttonTrigger = this.querySelector("button");

    new bootstrap.Popover(buttonTrigger, {
      content: () => this.querySelector(`#edit-rubric-${this.rubric.id}`).innerHTML,
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

      document.querySelector(".popover.show .btn-secondary")
        .addEventListener("click", this.cancelEdit);
    });

    // It seems that the bootstrap popover removes the title attribute from the trigger when it makes it the title of the popover.
    // Adding it back to the trigger.
    buttonTrigger.title = buttonTrigger.dataset.preserveTitle;
  }

  cancelEdit(e) {

    e.stopPropagation();
    const trigger = document.getElementById(`edit-rubric-trigger-${this.dataset.rubricId}`);
    bootstrap.Popover.getInstance(trigger).hide();
    trigger.focus();
  }

  saveEdit(e) {

    e.stopPropagation();
    const title = e.target.closest(".popover-body").querySelector("input").value;
    document.getElementById(`rubric-edit-${this.dataset.rubricId}`).dispatchEvent(new CustomEvent("update-rubric-title", { detail: title }));
    const trigger = document.getElementById(`edit-rubric-trigger-${this.dataset.rubricId}`);
    bootstrap.Popover.getInstance(trigger).hide();
    trigger.focus();
  }
}
