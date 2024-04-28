import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiItemDelete extends RubricsElement {

  static properties = {

    rubricId: { attribute: "rubric-id", type: String },
    siteId: { attribute: "site-id", type: String },
    rubric: { type: Object },
    criterionId: { attribute: "criterion-id", type: String },
    criterion: { type: Object },
  };

  constructor() {

    super();

    this._rubric;
    this._criterion;
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
      <button
          class="btn btn-sm"
          data-bs-toggle="modal"
          data-bs-target="#delete-${this.item.id}"
          aria-controls="delete-${this.item.id}"
          aria-expanded="false"
          title="${this.tr("remove", [ this.item.title ])}"
          aria-label="${this.tr("remove", [ this.item.title ])}"
          @click=${this._stopPropagation}>
        <span class="fa fa-times pe-none" style="pointer-events: none;"></span>
      </button>

      <div id="delete-${this.item.id}"
          tabindex="-1"
          class="modal"
          data-bs-backdrop="static"
          aria-labelledby="delete-modal-label-${this.item.id}"
          aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="delete-modal-label-${this.item.id}">
                ${this.tr("delete_item_title", [ this.type === "criterion" ? this._i18n.criterion : this._i18n.rubric ])}
              </h5>
              <button type="button"
                  class="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="${this.tr("close_dialog")}">
              </button>
            </div>
            <div class="modal-body">
              <div>${this.tr("confirm_remove", [ this.item.title ])}</div>
            </div>
            <div class="modal-footer">
              <div class="text-end">
                <button type="button"
                    class="btn btn-primary"
                    title="${this.tr("confirm_remove")}"
                    @click=${this.saveDelete}>
                  ${this._i18n.remove_label}
                </button>
                <button type="button"
                    class="btn btn-secondary btn-xs"
                    data-bs-dismiss="modal"
                    @click=${this.cancelDelete}>
                  <sr-lang key="cancel">Cancel</sr-lang>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  hideToolTip() {
    bootstrap.Modal.getOrCreateInstance(this.querySelector(".modal")).hide();
  }

  cancelDelete(e) {

    e.stopPropagation();
    this.hideToolTip();
  }

  saveDelete(e) {

    e.stopPropagation();
    let url = `/api/sites/${this.siteId}/rubrics/`;

    if (this.type === "criterion") {
      url += `${this.rubricId}/criterions/${this.criterion.id}`;
    } else {
      url += this.rubric.id;
    }

    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
    .then(r => {

      if (r.ok) {

        this.hideToolTip();
        this.dispatchEvent(new CustomEvent("delete-item", { detail: this.item, bubbles: true, composed: true }));
      } else {
        throw new Error("Network error while deleting rubric/criterion");
      }
    })
    .catch (error => console.error(error));
  }
}
