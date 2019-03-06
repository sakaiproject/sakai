import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricDelete extends SakaiElement {

  constructor() {

    super();

    this.listeners = {
      'tap': 'hostEventCatch',
      'hideToolTip': 'hideToolTip',
    };

    this.token = "";
    this.rubric;
    this.criterion;
    this.popoverOpen = "false";
    this.rubricId = "";

    this.deleteItemConfig = {
      url: "/rubrics-service/rest/",
      method: "DELETE",
      contentType: "application/json"
    };
  }

  static get properties() {

    return {
      token: String,
      rubricId: String,
      rubric: { type: Object, notify: true },
      criterion: { type: Object, notify: true }
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "rubric") {
      this.item = this.rubric;
      this.type = "rubric";
    }

    if (name === "criterion") {
      this.item = this.criterion;
      this.type = "criterion";
    }

    if (name === "token") {
      this.deleteItemConfig.headers = {"authorization": newValue};
    }
  }

  updated(changedProperties) {

    if (changedProperties.has("criterion")) {
      this.deleteItemConfig.url = `/rubrics-service/rest/rubrics/${this.rubricId}/criterions/${this.criterion.id}`;
    }

    if (changedProperties.has("rubric")) {
      this.deleteItemConfig.url = `/rubrics-service/rest/rubrics/${this.rubric.id}`;
    }
  }

  render() {

    return html`

      ${this.criterion ?
        html`<span role="button" aria-haspopup="true" aria-expanded="${this.popoverOpen}" aria-controls="delete_criterion_${this.criterion.id}" tabindex="0" title="${tr("remove")} ${tr(this.type)} ${this.criterion.title}" class="delete fa fa-times" @click="${this.deleteItem}"></span>`
        : html`<span role="button" aria-haspopup="true" aria-expanded="${this.popoverOpen}" aria-controls="delete_rubric_${this.rubric.id}" tabindex="0" title="${tr("remove")} ${tr(this.type)} ${this.rubric.title}" class="delete fa fa-times" @click="${this.deleteItem}"></span>`
      }

      <div id="delete_rubric_${this.item.id}" class="popover rubric-delete-popover left">
        <div class="arrow"></div>
        <div class="popover-title" tabindex="0">
          <sr-lang key="confirm_remove">Are you sure you want to remove </sr-lang> ${this.item.title}?
        </div>
        <div class="popover-content">
          <div class="buttons text-right">
            <button title="${tr("confirm_remove")}" class="btn btn-danger btn-xs save" @click="${this.saveDelete}">
              <sr-lang key="remove">Remove</sr-lang>
            </button>
            <button class="btn btn-link btn-xs cancel" @click="${this.cancelDelete}">
              <sr-lang key="cancel">Cancel</sr-lang>
            </button>
          </div>
        </div>
      </div>
    `;
  }

  hostEventCatch(e) {

    e.stopPropagation();
    console.log('event stopped at host');
  }

  closeOpen() {

    $('.show-tooltip .cancel').click();
  }

  deleteItem(e) {

    e.stopPropagation();

    if (!this.classList.contains("show-tooltip")) {
      this.closeOpen();
      this.popoverOpen = "true";
      var triggerPosition = rubrics.altOffset(e.target);

      this.classList.add("show-tooltip");

      var popover = $(`#delete_rubric_${this.item.id}`);

      var target = this.querySelector(".fa-times");

      rubrics.css(popover[0], {
        'left': target.offsetLeft - 280 + "px",
        'top': (target.offsetTop - this.offsetHeight*2 - 10) + "px",
      });

      $('.btn-danger').focus();

      popover.show();

    } else {
      this.popoverOpen = "false";
      this.hideToolTip();
      $(`#delete_rubric_${this.item.id}`).hide();
    }
  }

  hideToolTip() {
    this.classList.remove("show-tooltip");
  }

  cancelDelete(e) {

    e.stopPropagation();
    this.hideToolTip();
    $(`#delete_rubric_${this.item.id}`).hide();
  }

  saveDelete(e) {

    e.stopPropagation();
    $.ajax(this.deleteItemConfig)
      .done(data => this.updateUi(data))
      .fail((jqXHR, error, message) => {
        console.log(error);
        console.log(message);
      });
  }

  updateUi(data) {

    this.dispatchEvent(new CustomEvent('delete-item', {detail: this.item, bubbles: true, composed: true}));
    this.hideToolTip();
    $(`#delete_rubric_${this.item.id}`).hide();
  }
}

customElements.define("sakai-rubric-delete", SakaiRubricDelete);
