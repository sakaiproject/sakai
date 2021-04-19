import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricCriterionEdit extends RubricsElement {

  constructor() {

    super();

    this.token = "";
    this.criterion = {};
    this.criterionClone = {};
  }

  static get properties() {

    return {
      token: String,
      criterion: { type: Object, notify: true }
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
              <sr-lang key="criterion_title">Criterion Title</sr-lang>
            </label>
            <input id="criterion-title-field-${this.criterion.id}" type="text" class="form-control" value="${this.criterionClone.title}" maxlength="255">
          </div>
          <div class="form-group">
            <label for="criterion-description-field-${this.criterion.id}">
              <sr-lang key="criterion_description">Criterion Description</sr-lang>
            </label>
            <textarea id="criterion-description-field-${this.criterion.id}" class="form-control">${this.criterionClone.description}</textarea>
          </div>
        </div>
      </div>
    `;
  }

  onFocus(e){
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
    var titleinput = this.querySelector('[type="text"]');

    if (!this.classList.contains("show-tooltip")) {

      this.closeOpen();
      this.classList.add("show-tooltip");

      var popover = $(`#edit_criterion_${this.criterion.id}`);
      popover[0].style.top = e.target.offsetTop + 20 + "px";
      popover[0].style.left = (e.target.offsetLeft - popover.width()/2) + "px";
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

    var data = {
      title: document.getElementById(`criterion-title-field-${this.criterion.id}`).value,
      description: document.getElementById(`criterion-description-field-${this.criterion.id}`).value
    };

    $.ajax({
      url: `/rubrics-service/rest/criterions/${this.criterion.id}`,
      headers: {"authorization": this.token},
      method: "PATCH",
      contentType: "application/json",
      data: JSON.stringify(data)
    })
      .done(data => this.updateUi(data))
      .fail((jqXHR, error, message) => {console.log(error); console.log(message); });

    // hide the popover
    this.hideToolTip();
    this.dispatchEvent(new CustomEvent('hide-tooltip', {detail: this.criterion}));
    $(`#edit_criterion_${this.criterion.id}`).hide();
  }

  updateUi(data) {

    this.hideToolTip();
    this.dispatchEvent(new CustomEvent('criterion-edited', {detail: {id: data.id, title: data.title, description: data.description}}));
    this.dispatchEvent(new SharingChangeEvent());
  }

  openEditWithKeyboard(e) {
	
    if (e.keyCode == 32) {
      this.editCriterion(e)
    }
  }
}

customElements.define("sakai-rubric-criterion-edit", SakaiRubricCriterionEdit);
