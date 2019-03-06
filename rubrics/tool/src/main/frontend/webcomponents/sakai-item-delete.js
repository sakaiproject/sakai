import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiItemDelete extends SakaiElement {

  constructor() {

    super();

    this._rubric;
    this._criterion;

    this.popoverOpen = "false";

    this.deleteItemConfig = {
      url: "/rubrics-service/rest/",
      method: "DELETE",
      contentType: "application/json"
    };
  }

  static get properties() {

    return {
      token: { type: String},
      rubricId: {type: String},
      rubric: { type: Object },
      criterion: { type: Object }
    };
  }

  set rubric(newValue) {

    this._rubric = newValue;
    this.item = newValue;
    this.type = "rubric";
  }

  get rubric() { return this._rubric; }

  set criterion(newValue) {

    this._criterion = newValue;
    this.item = newValue;
    this.type = "criterion";
  }

  get criterion() { return this._criterion; }

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
            <button title="${tr("confirm_remove")}" class="btn-primary save" @click="${this.saveDelete}">
              <sr-lang key="remove">Remove</sr-lang>
            </button>
            <button class="cancel" @click="${this.cancelDelete}">
              <sr-lang key="cancel">Cancel</sr-lang>
            </button>
          </div>
        </div>
      </div>
    `;
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
    var url = "/rubrics-service/rest/rubrics/";

    if ("criterion" === this.type) {
      url += `${this.rubricId}/criterions/${this.criterion.id}`;
    } else {
      url += this.rubric.id;
    }

    $.ajax({
      url: url,
      method: "DELETE",
      headers: {"authorization": this.token},
      contentType: "application/json"
    })
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

customElements.define("sakai-item-delete", SakaiItemDelete);
