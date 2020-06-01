import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiItemDelete extends RubricsElement {

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
      rubricId: {attribute: "rubric-id", type: String},
      rubric: { type: Object },
      criterionId: {attribute: "criterion-id", type: String},
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
      <span @focus="${this.onFocus}" @focusout="${this.focusOut}" role="button" aria-haspopup="true" aria-expanded="${this.popoverOpen}" aria-controls="delete_${this.type}_${this.item.id}" tabindex="0" title="${tr("remove", [this.item.title])}" class="delete fa fa-times" @click="${this.deleteItem}"></span>
      <div id="delete_${this.type}_${this.item.id}" class="popover rubric-delete-popover left">
        <div class="arrow"></div>
        <div class="popover-title" tabindex="0">${tr("confirm_remove")} ${this.item.title}</div>
        <div class="popover-content">
          <div class="buttons text-right act">
            <button title="${tr("confirm_remove")}" class="active save" @click="${this.saveDelete}">
              <sr-lang key="remove_label" />
            </button>
            <button class="cancel" @click="${this.cancelDelete}">
              <sr-lang key="cancel">Cancel</sr-lang>
            </button>
          </div>
        </div>
      </div>
    `;
  }

  onFocus(e){
    var criterionRow= e.target.closest('.criterion-row');
    if(criterionRow!=undefined) criterionRow.classList.add("focused");
  }
  focusOut(e){
    var criterionRow= e.target.closest('.criterion-row');
    if(criterionRow!=undefined) criterionRow.classList.remove("focused");
  }

  closeOpen() {

    $('.show-tooltip .cancel').click();
  }

  deleteItem(e) {

    e.stopPropagation();

    if (!this.classList.contains("show-tooltip")) {
      this.closeOpen();
      this.popoverOpen = "true";

      this.classList.add("show-tooltip");

      var popover = $(`#delete_${this.type}_${this.item.id}`);

      var target = this.querySelector(".fa-times");

      popover[0].style.left = target.offsetLeft - 280 + "px";
      popover[0].style.top = (target.offsetTop - this.offsetHeight*2 - 10) + "px";

      $('.btn-danger').focus();

      popover.show();

    } else {
      this.popoverOpen = "false";
      this.hideToolTip();
      $(`#delete_${this.type}_${this.item.id}`).hide();
    }
  }

  hideToolTip() {
    this.classList.remove("show-tooltip");
  }

  cancelDelete(e) {

    e.stopPropagation();
    this.hideToolTip();
    $(`#delete_${this.type}_${this.item.id}`).hide();
  }

  saveDelete(e) {

    e.stopPropagation();
    var url = "/rubrics-service/rest/rubrics/";

    if ("criterion" === this.type) {
      url += `${this.rubricId}/criterions/${this.criterion.id}`;
    } else {
      url += this.rubric.id;

      // SAK-42944 removing the soft-deleted associations
      $.ajax({
        url: `/rubrics-service/rest/rubric-associations/search/by-rubric-id?rubricId=${this.rubric.id}`,
        headers: {"authorization": this.token},
        async: false
      }).done(data => {
        if (data._embedded['rubric-associations'].length) {
          data._embedded['rubric-associations'].forEach( assoc => {
            $.ajax({
              url: `/rubrics-service/rest/evaluations/search/by-association-id?toolItemRubricAssociationId=${assoc.id}`,
              headers: {"authorization": this.token},
              async: false
            }).done(data => {
              if (data._embedded['evaluations'].length) {
                data._embedded['evaluations'].forEach( rubeval => {
                  var evalUrl = "/rubrics-service/rest/evaluations/"+rubeval.id;
                  $.ajax({
                    url: evalUrl,
                    method: "DELETE",
                    headers: {"authorization": this.token},
                    contentType: "application/json"
                  }).fail((jqXHR, error, message) => {
                    console.error(error);
                    console.log(message);
                  });
                });
              }
            });
            var assocUrl = "/rubrics-service/rest/rubric-associations/"+assoc.id;
            $.ajax({
              url: assocUrl,
              method: "DELETE",
              headers: {"authorization": this.token},
              contentType: "application/json"
            }).fail((jqXHR, error, message) => {
              console.error(error);
              console.log(message);
            });
          });
        }
      });
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
    $(`#delete_${this.type}_${this.item.id}`).hide();
  }
}

customElements.define("sakai-item-delete", SakaiItemDelete);
