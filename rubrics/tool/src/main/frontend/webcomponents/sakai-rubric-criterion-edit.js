import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SharingChangeEvent} from "./sharing-change-event.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricCriterionEdit extends RubricsElement {

  constructor() {

    super();

    this.listeners = { "tap": "hostEventCatch" };
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

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ("criterion" === name) {
      this.criterionClone = JSON.parse(newValue);
      if (this.criterionClone.new) {
        this.updateComplete.then(() => this.querySelector(".edit").click() );
      }
    }
  }

  render() {

    return html`
      <span tabindex="0" role="button" class="edit fa fa-edit" @focus="${this.onFocus}" @click="${this.editCriterion}" title="${tr("edit_criterion")} ${this.criterion.title}"></span>

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
            <label>
              <sr-lang key="criterion_title">Criterion Title</sr-lang>
            </label>
            <input id="criterion-title-field-${this.criterion.id}" type="text" class="form-control" value="${this.criterionClone.title}" maxlength="255">
          </div>
          <div class="form-group">
            <label>
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

  hostEventCatch(e) {

    // catch and stop taps from bubbling outside the component
    e.stopPropagation();
    console.log('event stopped at host');
  }

  closeOpen() {
    $('.show-tooltip .cancel').click();
  }

  editCriterion(e) {

    e.stopPropagation();

    this.dispatchEvent(new CustomEvent('show-tooltip', {detail: this.criterion}));

    // title input box reference
    var titleinput = this.querySelector('[type="text"]');

    if (!this.classList.contains("show-tooltip")) {

      this.closeOpen();
      this.classList.add("show-tooltip");

      var popover = $(`#edit_criterion_${this.criterion.id}`);
      this.rubricsUtils.css(popover[0], {
        'top': e.target.offsetTop + 20 + "px",
        'left': (e.target.offsetLeft - popover.width()/2) + "px",
      });

      // and highlight the title
      popover.show();
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
}

customElements.define("sakai-rubric-criterion-edit", SakaiRubricCriterionEdit);
