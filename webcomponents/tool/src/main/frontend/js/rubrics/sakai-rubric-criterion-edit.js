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
    if (this.criterionClone.new) {
      this.updateComplete.then(() => this.querySelector(".edit").click() );
    }
  }

  get criterion() { return this._criterion; }

  render() {

    return html`
      <a tabindex="0" role="button" class="linkStyle edit fa fa-edit" @focus="${this.onFocus}" @keyup="${this.openEditWithKeyboard}" @click="${this.editCriterion}" title="${tr("edit_criterion")} ${this.criterion.title}" href="#"></a>

      <div id="edit_criterion_${this.criterion.id}" class="popover criterion-edit-popover bottom">
        <div class="arrow"></div>
        <div class="popover-title">
          <div class="buttons act">
            <button class="active save" @click="${this.saveEdit}">
              <sr-lang key="save">Save</sr-lang>
            </button>
            <button class="btn btn-link btn-xs cancel" @click="${this.cancelEdit}">
              <sr-lang key="cancel">Cancel</sr-lang>
            </button>
          </div>
        </div>
        <div class="popover-content form">
          <div class="form-group">
            <label for="criterion-title-field-${this.criterion.id}">
              ${this.isCriterionGroup ? html`
                <sr-lang key="criterion_group_title">Criterion Group Title</sr-lang>
              ` : html`
                <sr-lang key="criterion_title">Criterion Title</sr-lang>
              `}
            </label>
            <input id="criterion-title-field-${this.criterion.id}" type="text" class="form-control" value="${this.criterionClone.title}" maxlength="255">
          </div>
          <div class="form-group">
            <label for="criterion-description-field-${this.criterion.id}">
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
        </div>
      </div>
    `;
  }

  onFocus(e) {

    e.target.closest('.criterion-row').classList.add("focused");
  }

  closeOpen() {
    $('.show-tooltip .cancel').click();
  }

  editCriterion(e) {
    e.preventDefault();
    e.stopPropagation();

    this.dispatchEvent(new CustomEvent('show-tooltip', {detail: this.criterion}));

    // title input box reference
    const titleinput = this.querySelector('[type="text"]');

    if (!this.classList.contains("show-tooltip")) {

      this.closeOpen();
      this.classList.add("show-tooltip");

      const popover = $(`#edit_criterion_${this.criterion.id}`);
      popover[0].style.top = `${e.target.offsetTop + 20  }px`;
      popover[0].style.left = `${e.target.offsetLeft - popover.width() / 2  }px`;
      popover.show();

      // and highlight the title
      titleinput.setSelectionRange(0, titleinput.value.length);
      titleinput.focus();

    } else {
      // if the tooltip is showing
      // hide the tooltip
      this.hideToolTip();
      $(`#edit_criterion_${this.criterion.id}`).hide();
    }
  }

  hideToolTip() {

    // hide the edit popover
    this.classList.remove("show-tooltip");

    // fire hide-tooltip event to allow parent components to take action.
    this.dispatchEvent(new CustomEvent('hide-tooltip', {details: {criterion: this.criterion}}));
  }

  cancelEdit(e) {

    e.stopPropagation();

    // revert changed data
    this.criterionClone.title = this.criterion.title;
    this.criterionClone.description = this.criterion.description;

    // hide popover
    this.hideToolTip();
    this.dispatchEvent(new CustomEvent('hide-tooltip', {details: this.criterion}));
    $(`#edit_criterion_${this.criterion.id}`).hide();
  }

  saveEdit(e) {

    e.stopPropagation();

    const title = document.getElementById(`criterion-title-field-${this.criterion.id}`).value;
    const description = this.criterionClone.description;

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
        this.hideToolTip();
        this.dispatchEvent(new CustomEvent('criterion-edited', { detail: { id: this.criterion.id, title, description } }));
        this.dispatchEvent(new SharingChangeEvent());
      }

      throw new Error("Network error while updating criterion");
    })
    .catch (error => console.error(error));

    // hide the popover
    this.hideToolTip();
    this.dispatchEvent(new CustomEvent('hide-tooltip', {detail: this.criterion}));
    $(`#edit_criterion_${this.criterion.id}`).hide();
  }

  openEditWithKeyboard(e) {

    if (e.keyCode == 32) {
      this.editCriterion(e);
    }
  }

  updateCriterionDescription(e) {
    this.criterionClone.description = e.detail.content;
  }
}

customElements.define("sakai-rubric-criterion-edit", SakaiRubricCriterionEdit);
