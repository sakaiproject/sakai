import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";

export class SakaiRubricCriterionRatingEdit extends RubricsElement {

  static properties = {

    rating: { type: Object },
    criterionId: { attribute: "criterion-id", type: String },
    removable: { attribute: "removable", type: Boolean },
    isLocked: { attribute: "is-locked", type: Boolean },
    minpoints: Number,
    maxpoints:  Number,
  };

  set rating(value) {

    const old = this._rating;
    this._rating = value;
    this.requestUpdate("rating", old);

    // If this is a newly created rating, open the editor modal
    if (this._rating.new) {
      this.updateComplete.then(() => bootstrap.Modal.getOrCreateInstance(this.querySelector(`#edit-criterion-rating-${value.id}`)).show());
    }
  }

  get rating() { return this._rating; }

  firstUpdated() {

    const modal = this.querySelector(`#edit-criterion-rating-${this.rating.id}`);

    modal.addEventListener("shown.bs.modal", () => {

      this.closest(".rating-item")?.setAttribute("draggable", "false");
      this.closest(".criterion-row")?.setAttribute("draggable", "false");
      this.querySelector(`#rating-title-edit-${this.rating.id}`).select();
    });

    modal.addEventListener("hidden.bs.modal", () => {
      this.closest(".rating-item")?.setAttribute("draggable", "true");
      this.closest(".criterion-row")?.setAttribute("draggable", "true");
    });
  }

  render() {

    return html`
      <button class="btn btn-icon"
          type="button"
          data-bs-toggle="modal"
          data-bs-target="#edit-criterion-rating-${this.rating.id}"
          aria-controls="edit-criterion-rating-${this.rating.id}"
          aria-expanded="false"
          title="${this._i18n.edit_rating} ${this.rating.title}"
          aria-label="${this._i18n.edit_rating} ${this.rating.title}">
        <i class="si si-edit"></i>
      </button>

      <div class="modal modal-sm fade"
          id="edit-criterion-rating-${this.rating.id}"
          tabindex="-1"
          data-bs-backdrop="static"
          aria-labelledby="edit-criterion-rating-${this.rating.id}-label"
          aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fs-5" id="edit-criterion-rating-${this.rating.id}-label">${this.rating.title}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${this._i18n.close_dialog}"></button>
            </div>
            <div class="modal-body">
              <div class="d-flex">
                <div class="">
                  <label class="label-rubrics form-label" for="rating-title-edit-${this.rating.id}">${this._i18n.rating_title}</label>
                  <input type="text"
                      id="rating-title-edit-${this.rating.id}"
                      class="form-control"
                      .value=${this.rating.title}
                      maxlength="255">
                </div>
                <div class="ms-auto points ${this.isLocked ? "d-none" : ""}">
                  <label class="label-rubrics form-label" for="rating-points-${this.rating.id}">${this._i18n.points}</label>
                  <input type="number"
                      id="rating-points-${this.rating.id}"
                      class="form-control hide-input-arrows"
                      name="quantity"
                      .value=${this.rating.points}
                      min="${ifDefined(this.minpoints)}"
                      max="${ifDefined(this.maxpoints)}">
                </div>
              </div>
              <div class="form-group">
                <label class="label-rubrics" for="rating-description-${this.rating.id}">${this._i18n.rating_description}</label>
                <textarea name="" id="rating-description-${this.rating.id}" class="form-control" .value=${this.rating.description}></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-xs btn-primary" title="${this._i18n.save} ${this.rating.title}" @click=${this._saveEdit}>
                ${this._i18n.save}
              </button>
              ${!this.isLocked ? html`
                <button class="btn btn-secondary delete" title="${this.removeButtonTitle()}" ?disabled="${!this.removable}" @click=${this.deleteRating}>${this._i18n.remove_label}</button>
              ` : nothing }
              <button class="btn btn-secondary btn-xs cancel" title="${this._i18n.cancel}" data-bs-dismiss="modal" @click=${this.cancelEdit}>
                ${this._i18n.cancel}
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  resetFields() {

    document.getElementById(`rating-title-edit-${this.rating.id}`).value = this.rating.title;
    document.getElementById(`rating-points-${this.rating.id}`).value = this.rating.points;
    document.getElementById(`rating-description-${this.rating.id}`).value = this.rating.description;
  }

  cancelEdit(e) {

    e.stopPropagation();
    this.resetFields();
  }

  _saveEdit(e) {

    e.stopPropagation();

    this.rating.title = document.getElementById(`rating-title-edit-${this.rating.id}`).value;

    if (!this.isLocked) {

      const points = parseFloat(document.getElementById(`rating-points-${this.rating.id}`).value);
      // Check points value. Blank breaks things.
      if (isFinite(points)) {
        // Round user input to two digits.
        this.rating.points = points.toFixed(2);
      } // Else, previous saved score or default one will be used.
    }

    this.rating.description = document.getElementById(`rating-description-${this.rating.id}`).value;
    this.rating.criterionId = this.criterionId;

    this.resetFields();

    this.dispatchEvent(new CustomEvent("save-rating", { detail: { rating: this.rating, criterionId: this.criterionId } }));

    bootstrap.Modal.getInstance(this.querySelector(`#edit-criterion-rating-${this.rating.id}`)).hide();
  }

  deleteRating(e) {

    e.stopPropagation();

    this.rating.criterionId = this.criterionId;
    this.dispatchEvent(new CustomEvent("delete-rating", { detail: this.rating }));
    bootstrap.Modal.getInstance(this.querySelector(`#edit-criterion-rating-${this.rating.id}`)).hide();
  }

  removeButtonTitle() {

    return this.removable ? `${this._i18n.remove_label} ${this.rating.title}`
      : this._i18n.remove_rating_disabled;
  }
}
