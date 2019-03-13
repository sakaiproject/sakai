import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricEdit extends SakaiElement {

  constructor() {

    super();

    this.popoverOpen = "false";
    this.rubricClone = {};
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("rubric" === name) {
      this.rubricClone = JSON.parse(newValue);
      if (this.rubricClone.new) {
        this.updateComplete.then(() => this.querySelector(".edit").click() );
      }
    }
  }

  static get properties() {

    return {
      token: String,
      rubric: { type: Object }
    };
  }

  render() {

    return html`
      <span class="edit fa fa-edit" role="button" aria-haspopup="true" aria-expanded="${this.popoverOpen}" aria-controls="edit_rubric_${this.rubric.id}" tabindex="0" @click="${this.editRubric}" title="${tr("edit_rubric")} ${this.rubric.title}" ></span>

      <div id="edit_rubric_${this.rubric.id}" @click="${this.eatEvent}" class="popover rubric-edit-popover bottom">
        <div class="arrow"></div>
        <div class="popover-title">
          <div class="buttons">
            <button class="btn btn-primary btn-xs save" @click="${this.saveEdit}">
              <sr-lang key="save">Save</sr-lang>
            </button>
            <button class="btn btn-link btn-xs cancel" @click="${this.cancelEdit}">
              <sr-lang key="cancel">Cancel</sr-lang>
            </button>
          </div>
        </div>
        <div class="popover-content form">
          <div class="form-group">
            <label for="rubric_title_edit">
              <sr-lang key="rubric_title">Rubric Title</sr-lang>
            </label>
            <input title="${tr("rubric_title")}" name="rubric_title_edit" type="text" class="form-control" value="${this.rubricClone.title}" maxlength="255">
          </div>
        </div>
      </div>
    `;
  }

  eatEvent(e) {
    e.stopPropagation();
  }

  editRubric(e) {

    e.stopPropagation();
    this.dispatchEvent(new CustomEvent("show-tooltip", {detail: this.rubric}));

    if (!this.classList.contains("show-tooltip")) {
      this.closeOpen();
      this.popoverOpen = "true";
      var target = this.querySelector(".fa-edit");

      this.classList.add("show-tooltip");

      var popover = $(`#edit_rubric_${this.rubric.id}`);

      rubrics.css(popover[0], {
        'top': target.offsetTop + 20 + "px",
        'left': (target.offsetLeft - 125) + "px",
      });

      popover.show();
      var input =  popover.find("input[type='text']")[0];
      input.setSelectionRange(0, input.value.length);
      input.focus();
    } else {
      this.popoverOpen = "false";
      this.hideToolTip();
      $(`#edit_rubric_${this.rubric.id}`).hide();
    }
  }

  closeOpen() {
    $('.show-tooltip .cancel').click();
  }

  hideToolTip() {

    this.classList.remove("show-tooltip");
    this.dispatchEvent(new CustomEvent("hide-tooltip", {detail: this.rubric}));
  }

  cancelEdit(e) {

    e.stopPropagation();
    this.rubricClone.title = this.rubric.title;
    this.hideToolTip();
    var popover = $(`#edit_rubric_${this.rubric.id}`);
    popover.find("input[type='text']")[0].value = this.rubric.title;
    popover.hide();
  }

  saveEdit(e) {

    e.stopPropagation();
    var title = this.querySelector("[name='rubric_title_edit']").value;
    this.dispatchEvent(new CustomEvent("update-rubric-title", { detail: title }));
    $(`#edit_rubric_${this.rubric.id}`).hide();
    this.hideToolTip();
  }
}

customElements.define("sakai-rubric-edit", SakaiRubricEdit);
