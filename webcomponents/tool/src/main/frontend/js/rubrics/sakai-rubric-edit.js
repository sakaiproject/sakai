import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { tr } from "./sakai-rubrics-language.js";

export class SakaiRubricEdit extends RubricsElement {

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

  static get properties() {

    return {
      rubric: { type: Object }
    };
  }

  render() {

    return html`
      <button class="btn btn-icon edit"
          id="edit-rubric-trigger-${this.rubric.id}"
          type="button"
          @click=${e => e.stopPropagation()}
          data-bs-toggle="popup"
          aria-haspopup="true"
          aria-expanded="false"
          aria-controls="edit-rubric-${this.rubric.id}"
          title="${tr("edit_rubric")}"
          aria-label="${tr("edit_rubric")} ${this.rubric.title}">
        <i class="si si-edit"></i>
      </button>

      <div id="edit-rubric-${this.rubric.id}" class="rubric-edit-popover d-none">
        <div>
          <div>
            <label class="label-rubrics" for="rubric-title-edit-${this.rubric.id}">
              <sr-lang key="rubric_title">Rubric Title</sr-lang>
            </label>
            <input title="${tr("rubric_title")}" id="rubric-title-edit-${this.rubric.id}" type="text" value="${this.rubricClone.title}" maxlength="255">
          </div>
        </div>
        <div class="mt-2">
          <div>
            <button class="btn btn-primary" type="button" data-rubric-id="${this.rubric.id}">
              <sr-lang key="save">Save</sr-lang>
            </button>
            <button class="btn btn-secondary" type="button" data-rubric-id="${this.rubric.id}">
              <sr-lang key="cancel">Cancel</sr-lang>
            </button>
          </div>
        </div>
      </div>
    `;
  }

  firstUpdated() {

    new bootstrap.Popover(this.querySelector("button"), {
      content: () => this.querySelector(`#edit-rubric-${this.rubric.id}`).innerHTML,
      html: true,
      placement: "bottom",
      sanitize: false,
    });

    this.querySelector("button").addEventListener("shown.bs.popover", () => {

      const save = document.querySelector(".popover.show .btn-primary");
      save.addEventListener("click", this.saveEdit);

      const titleInput = save.closest(".popover-body").querySelector("input");
      titleInput.setSelectionRange(0, titleInput.value.length);
      titleInput.focus();

      document.querySelector(".popover.show .btn-secondary")
        .addEventListener("click", this.cancelEdit);
    });
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

const tag = "sakai-rubric-edit";
!customElements.get(tag) && customElements.define(tag, SakaiRubricEdit);
