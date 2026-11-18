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

    const isRubric = this.type === "rubric";
    const action = isRubric ? "move_to_trash" : "remove";
    const confirmation = isRubric ? "confirm_move_to_trash" : "confirm_remove";

    return html`
      <button
          class="btn btn-sm"
          data-bs-toggle="modal"
          data-bs-target="#delete-${this.type}-${this.item.id}"
          aria-controls="delete-${this.type}-${this.item.id}"
          aria-expanded="false"
          title="${this.tr(action, [ this.item.title ])}"
          aria-label="${this.tr(action, [ this.item.title ])}"
          @click=${this._openDelete}>
        <span class="fa fa-times pe-none" style="pointer-events: none;"></span>
      </button>

      <div id="delete-${this.type}-${this.item.id}"
          tabindex="-1"
          class="modal"
          data-bs-backdrop="static"
          aria-labelledby="delete-modal-label-${this.type}-${this.item.id}"
          aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="delete-modal-label-${this.type}-${this.item.id}">
                ${this.tr("delete_item_title", [ this.type === "criterion" ? this._i18n.criterion : this._i18n.rubric ])}
              </h5>
              <button type="button"
                  class="btn-close"
                  data-bs-dismiss="modal"
                  aria-label="${this.tr("close_dialog")}">
              </button>
            </div>
            <div class="modal-body">
              ${this.renderDeleteError()}
              <div>${this.tr(confirmation, [ this.item.title ])}</div>
            </div>
            <div class="modal-footer">
              <div class="text-end">
                <button type="button"
                    class="btn btn-primary"
                    title="${this.tr(confirmation, [ this.item.title ])}"
                    ?disabled=${this._deleting}
                    @click=${this.saveDelete}>
                  ${isRubric ? this._i18n.move_to_trash_label : this._i18n.remove_label}
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

  _openDelete(e) {
    e.stopPropagation();
    this._saveError = null;
    this._deleteBlockedBy = null;
  }

  hideToolTip() {
    bootstrap.Modal.getOrCreateInstance(this.renderRoot.querySelector(".modal")).hide();
  }

  cancelDelete(e) {

    e.stopPropagation();
    this.hideToolTip();
  }

  saveDelete(e) {

    e.stopPropagation();
    if (this._deleting) { return; }

    this._deleting = true;
    this._saveError = null;
    this._deleteBlockedBy = null;
    const item = this.item;
    let url = `/api/sites/${this.siteId}/rubrics/`;

    if (this.type === "criterion") {
      url += `${this.rubricId}/criterions/${this.criterion.id}`;
    } else {
      url += this.rubric.id;
    }

    this.enqueueMutation(() => fetch(url, {
      method: "DELETE",
      headers: { "If-Match": `"${this.type === "criterion" ? this.closest("sakai-rubric").rubric.version : this.rubric.version}"` },
      credentials: "include",
    })
    .then(async r => {

      if (r.ok) {
        if (this.type === "criterion") { await this.readSaveResponse(r); }

        if (this.item.id === item.id) { this.hideToolTip(); }
        this.dispatchEvent(new CustomEvent("delete-item", { detail: item, bubbles: true, composed: true }));
      } else {
        await this.readDeleteError(r);
      }
    }))
    .catch(error => this.reportSaveError(error))
    .finally(() => { this._deleting = false; });
  }
}
