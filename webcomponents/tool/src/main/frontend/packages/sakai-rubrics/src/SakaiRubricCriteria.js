import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { repeat } from "lit/directives/repeat.js";
import "../sakai-rubric-edit.js";
import "../sakai-item-delete.js";
import "../sakai-rubric-criterion-edit.js";
import "../sakai-rubric-criterion-rating-edit.js";
import { SharingChangeEvent } from "./SharingChangeEvent.js";
import { tr } from "./SakaiRubricsLanguage.js";
import "@sakai-ui/sakai-reorderer/sakai-reorderer.js";

export class SakaiRubricCriteria extends RubricsElement {

  static properties = {

    rubricId: { attribute: "rubric-id", type: String },
    siteId: { attribute: "site-id", type: String },
    criteria: { type: Array },
    weighted: { type: Boolean },
    totalWeight: { attribute: "total-weight", type: String },
    validWeight: { attribute: "valid-weight", type: Boolean },
    maxPoints: { attribute: "max-points", type: String },
    minPoints: { attribute: "min-points", type: String },
    isLocked: { attribute: "is-locked", type: Boolean },
    isDraft: { attribute: "is-draft", type: Boolean },
    _savingWeights: { state: true },
  };

  _criteriaReordered(e) {

    this.criteria = e.detail.reorderedIds.map(id => this.criteriaMap.get(parseInt(id)));

    // Focus the moved criterion's drag handle
    this.updateComplete.then(() => {

      this.querySelectorAll("sakai-reorderer").forEach(el => el.requestUpdate());
      this.querySelector(`[data-criterion-id="${e.detail.data.criterionId}"] .drag-handle`).focus();
    });

    // Reordering doesn't really care about the weight changes, but the event does get the criteria to update in the parent rubric object
    this.dispatchEvent(new CustomEvent("refresh-total-weight", { detail: { criteria: this.criteria } }));

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/sort`;
    fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(e.detail.reorderedIds),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while saving criteria sort");
      }
    })
    .catch (error => console.error(error));
  }

  _ratingsReordered(e) {

    e.stopPropagation();

    const criterionId = e.detail.data.criterionId;
    const criterion = this.criteria.find(c => c.id == criterionId);

    // Reorder the ratings based on the sort result
    criterion.ratings = e.detail.reorderedIds.map(id => criterion.ratings.find(r => r.id == id));
    this.requestUpdate();

    // Focus the moved rating's drag handle
    this.updateComplete.then(() => {
      this.querySelector(`[data-rating-id="${e.detail.data.ratingId}"] .drag-handle`).focus();
    });

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/${criterionId}/ratings/sort`;
    fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(e.detail.reorderedIds),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Network error while saving ratings sort at ${url}`);
      }

    })
    .catch (error => console.error(error));
  }

  letShareKnow() { this.dispatchEvent(new SharingChangeEvent()); }

  _handleSortedRatings(e) {

    e && e.stopPropagation();

    const criterionId = e.target.dataset.criterionId;
    const criterion = this.criteria.find(c => c.id == criterionId);

    const sortedIds = Array.from(this.querySelectorAll(`#cr-table-${criterionId} .rating-item`)).map(r => r.dataset.ratingId);

    // Reorder the ratings based on the sort result
    criterion.ratings = sortedIds.map(id => criterion.ratings.find(r => r.id == id));

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/${criterionId}/ratings/sort`;
    fetch(url, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(sortedIds),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Network error while saving ratings sort at ${url}`);
      }
    })
    .catch (error => console.error(error));
  }

  saveRating(e) {

    e.stopPropagation();

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/${e.detail.criterionId}/ratings/${e.detail.rating.id}`;
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      method: "POST",
      body: JSON.stringify(e.detail.rating)
    })
    .then(r => {

      if (r.ok) {
        const criterion = this.criteria.find(c => c.id == e.detail.criterionId);
        const rating = criterion.ratings.find(rat => rat.id == e.detail.rating.id);
        rating.title = e.detail.rating.title;
        rating.description = e.detail.rating.description;
        rating.points = e.detail.rating.points;
        rating.new = false;
        this.requestUpdate();
        this.updateComplete.then(() => this.querySelector(`#criterion-ratings-reorderer-${e.detail.criterionId}`).requestUpdate());
        this.letShareKnow();
        this.dispatchEvent(new CustomEvent("refresh-total-weight", { detail: { criteria: this.criteria } }));
      } else {
        throw new Error("Network error while saving rating");
      }
    })
    .catch(error => console.error(error));
  }

  deleteCriterion(e) {

    e.stopPropagation();
    const index = this.criteria.findIndex(c => c.id === e.detail.id);
    this.criteria.splice(index, 1);
    this.criteriaMap.delete(e.detail.id);
    this.requestUpdate();
    this.updateComplete.then(() => this.querySelector("sakai-reorderer").requestUpdate());
    this.letShareKnow();
    this.dispatchEvent(new CustomEvent("refresh-total-weight", { detail: { criteria: this.criteria } }));
  }

  emitWeightChanged(e) {

    let value = e.target.value;
    if (value !== "") {
      value = parseFloat(value.replace(",", "."));
      if (Number.isNaN(value)) value = 0;
    }
    const id = parseInt(e.target.getAttribute("data-criterion-id"));
    if (isNaN(id)) {
      return;
    }
    this.letShareKnow();
    this.dispatchEvent(new CustomEvent("weight-changed", { detail: { criterionId: id, value, criteria: this.criteria } }));
    this.requestUpdate();
  }

  addRating(e) {

    const criterionId = e.target.dataset.criterionId;
    const ratingPos = e.target.dataset.ratingPos;

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/${criterionId}/ratings/default?position=${ratingPos}`;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while adding rating");
    })
    .then(nr => this.addRatingResponse(criterionId, ratingPos, nr))
    .catch (error => console.error(error));
  }

  addRatingResponse(criterionId, ratingPos, newRating) {

    newRating.new = true;

    const criterion = this.criteriaMap.get(parseInt(criterionId));

    if (!criterion.ratings) criterion.ratings = [];

    criterion.ratings.splice(parseInt(ratingPos), 0, newRating);

    this.letShareKnow();
    this.requestUpdate();
  }

  deleteRating(e) {

    e.stopPropagation();

    const criterion = this.criteriaMap.get(parseInt(e.detail.criterionId));

    if (!criterion) {
      console.error(`No criterion found with id ${e.detail.criterionId}`);
      return;
    }

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/${e.detail.criterionId}/ratings/${e.detail.id}`;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Network error while deleting rating");
    })
    .then(c => {

      // The current weight may be lost because comes from DB and the UI can have a different value.
      c.weight = criterion.weight;

      Object.assign(criterion, c);
      this.requestUpdate();
      this.updateComplete.then(() => this.querySelector(`#criterion-ratings-reorderer-${e.detail.criterionId}`).requestUpdate());
      this.letShareKnow();
      this.dispatchEvent(new CustomEvent("refresh-total-weight", { detail: { criteria: this.criteria } }));
    })
    .catch (error => console.error(error));
  }

  // SAK-47640 - Get the maximum and minimum possible grade of the criterion,
  // multiplying the max-min rating points of the criterion by the criterion weight
  getCriterionMaxPoints(criterionId) {
    return this.getCriterionPoints(criterionId, Math.max);
  }

  getCriterionMinPoints(criterionId) {
    return this.getCriterionPoints(criterionId, Math.min);
  }

  getCriterionPoints(criterionId, minOrMax) {

    let totalPoints = 0;
    const criterion = this.criteria.find(c => c.id == criterionId);

    totalPoints += minOrMax(...criterion.ratings.map(rating => rating.points * (criterion.weight / 100)));

    return parseFloat(totalPoints).toLocaleString(this.locale);
  }

  cloneCriterion(e) {

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/${e.currentTarget.dataset.criterionId}/copy`;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Server error while cloning criterion");
    })
    .then(clone => this.createCriterionResponse(clone))
    .catch(error => console.error(error));
  }

  createCriterionResponse(nc) {

    this.criteria.push(nc);
    this.criteriaMap.set(nc.id, nc);

    // Add the criterion to the rubric
    this.requestUpdate();
    this.updateComplete.then(() => this.querySelector("sakai-reorderer").requestUpdate());
  }

  criterionEdited(e) {

    const criterion = this.criteriaMap.get(e.detail.id);
    criterion.title = e.detail.title;
    criterion.description = e.detail.description;
    criterion.new = false;
    this.requestUpdate();

    const del = this.querySelector(`sakai-item-delete[criterion-id="${e.detail.id}"]`);
    del.criterion = criterion;
    del.requestUpdate();
    const edit = this.querySelector(`sakai-rubric-criterion-edit[id="criterion-edit-${e.detail.id}"]`);
    edit.criterion = criterion;
    edit.requestUpdate();
  }

  saveWeights() {

    this._savingWeights = true;

    const parentRubric = this.closest("sakai-rubric");
    if (parentRubric) {
      parentRubric.saveCriterionWeights().finally(() => this._savingWeights = false);
    } else {
      console.error("Parent rubric not found");
    }
  }

  createCriterion(e, empty = false) {

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/criteria/default${empty ? "Empty" : ""}`;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while getting default criterion");
    })
    .then(nc => this.createCriterionResponse(nc))
    .catch(error => console.error(error));
  }

  openEditWithKeyboard(e) {

    if (e.keyCode == 32) {
      this.cloneCriterion(e);
    }
  }

  isRatingRemovable(criterion) {
    return criterion.ratings.length > 1;
  }

  debounce(fn, delay) {

    let timer = null;
    return function (...args) {
      clearTimeout(timer);
      timer = setTimeout(() => {
        fn.apply(this, args);
      }, delay);
    };
  }

  updated(changedProperties) {

    super.updated(changedProperties);

    if (changedProperties.has("criteria")) {
      this.criteriaMap = new Map(this.criteria.map(c => [ c.id, c ]));
    }
  }

  _renderAddRatingButton(c, pos = 0) {

    return html`
      <button data-criterion-id="${c.id}"
          aria-label="${this.tr("add_rating")} ${c.title}"
          title="${this.tr("add_rating")} ${c.title}"
          @click=${this.addRating}
          data-rating-pos="${pos}">
        <span class="fa fa-plus"></span>
      </button>
    `;
  }

  render() {

    return html`
      <sakai-reorderer drop-class="criterion-row" @reordered=${this._criteriaReordered}>
        <div data-rubric-id="${this.rubricId}" class="criterion style-scope sakai-rubric-criterion">
        ${repeat(this.criteria, c => c.id, c => html`
          ${this.isCriterionGroup(c) ? html`
            <div id="criterion_row_${c.id}" data-criterion-id="${c.id}" data-reorderable-id="${c.id}" class="criterion-row criterion-group">
              <div class="criterion-detail criterion-title">
                <h4 class="criterion-title d-flex align-items-center">
                  <div>
                    <span tabindex="0"
                        title="${this.tr("drag_order")}"
                        data-criterion-id="${c.id}"
                        aria-label="${this.tr("drag_to_reorder_label")}"
                        class="drag-handle reorder-icon si si-drag-handle fs-3">
                    </span>
                  </div>
                  <div class="ms-1">${c.title}</div>
                  <div>
                    <sakai-rubric-criterion-edit
                        id="criterion-edit-${c.id}"
                        @criterion-edited=${this.criterionEdited}
                        site-id="${this.siteId}"
                        rubric-id="${this.rubricId}"
                        .criterion=${c}
                        ?is-criterion-group="${true}">
                    </sakai-rubric-criterion-edit>
                  </div>
                </h4>
                <p>${unsafeHTML(c.description)}</p>
              </div>
              <div class="criterion-actions">
                ${!this.isLocked ? html`
                  <button type="button" data-criterion-id="${c.id}" title="${this.tr("copy")} ${c.title}" aria-label="${this.tr("copy")} ${c.title}" class="btn btn-sm clone" @click="${this.cloneCriterion}">
                    <span class="fa fa-copy" aria-hidden="true"></span>
                  </button>
                  <sakai-item-delete criterion-id="${c.id}" site-id="${this.siteId}" .criterion=${c} rubric-id="${this.rubricId}" @delete-item=${this.deleteCriterion}></sakai-item-delete>
                ` : nothing }
              </div>
            </div>
          ` : html`
            <div id="criterion_row_${c.id}" data-criterion-id="${c.id}" data-reorderable-id="${c.id}" class="criterion-row">
              <div class="criterion-detail">
                <h4 class="criterion-title d-flex align-items-center">
                  <div>
                    <span tabindex="0"
                        title="${this.tr("drag_order")}"
                        data-criterion-id="${c.id}"
                        aria-label="${this.tr("drag_to_reorder_label")}"
                        class="drag-handle reorder-icon si si-drag-handle fs-3">
                    </span>
                  </div>
                  <div class="ms-1">${c.title}</div>
                  <div>
                    <sakai-rubric-criterion-edit
                        id="criterion-edit-${c.id}"
                        @criterion-edited=${this.criterionEdited}
                        site-id="${this.siteId}"
                        rubric-id="${this.rubricId}"
                        .criterion=${c}>
                    </sakai-rubric-criterion-edit>
                  </div>
                </h4>
                <p>
                  ${unsafeHTML(c.description)}
                </p>
                ${this.weighted ? html`
                  <div class="weight-field">
                    ${!this.isLocked ? html`
                      <div class="field-item form-group input-group-sm ${this.validWeight ? "" : "weight-error"}">
                        <label
                          for="weight_input_${c.id}"
                          class="form-control-label"
                          title="${!this.validWeight ? tr("total_weight_wrong") : ""}"
                        >
                          <sr-lang key="weight">Weight</sr-lang>
                        </label>
                        <input
                          id="weight_input_${c.id}"
                          data-criterion-id="${c.id}"
                          type="text"
                          class="form-control"
                          placeholder="0.0"
                          @input="${this.debounce(this.emitWeightChanged, 500)}"
                          value="${c.weight === 0 ? "" : c.weight.toLocaleString(this.locale)}"
                          title="${!this.validWeight ? tr("total_weight_wrong") : ""}"
                        >
                        <span class="form-control-label"
                          title="${!this.validWeight ? tr("total_weight_wrong") : ""}"
                        >
                          <sr-lang key="percent_sign">%</sr-lang>
                        </span>
                      </div>
                    ` : nothing }
                    <div class="field-item">
                      <span>${this.tr("min_max_points", [ this.getCriterionMinPoints(c.id), this.getCriterionMaxPoints(c.id) ])}</span>
                    </div>
                  </div>
                ` : nothing }
                ${!this.isLocked ? html`
                  <div class="add-criterion-item">
                    ${this._renderAddRatingButton(c)}
                  </div>
                ` : nothing }
              </div>
              <div class="criterion-ratings">
                <sakai-reorderer id="criterion-ratings-reorderer-${c.id}" class="rating-reorderer" @reordered=${this._ratingsReordered} horizontal>
                  <div id="cr-table-${c.id}" class="cr-table" data-criterion-id="${c.id}">
                  ${repeat(c.ratings, r => r.id, (r, i) => html`
                    <div class="rating-item"
                        data-criterion-id="${c.id}"
                        data-rating-id="${r.id}"
                        data-reorderable-id="${r.id}"
                        id="rating_item_${r.id}">
                      <h5 class="criterion-item-title">
                        ${r.title}
                        <sakai-rubric-criterion-rating-edit
                          criterion-id="${c.id}"
                          @save-rating=${this.saveRating}
                          @delete-rating=${this.deleteRating}
                          minpoints="${c.pointrange ? c.pointrange.low : 0}"
                          maxpoints="${c.pointrange ? c.pointrange.high : 0}"
                          .rating=${r}
                          ?removable="${ this.isRatingRemovable(c) }"
                          ?is-locked="${this.isLocked}">
                        </sakai-rubric-criterion-rating-edit>
                      </h5>
                      <div class="div-description">
                        <p>
                        ${r.description}
                        </p>
                      </div>
                      <span class="points">
                        ${this.weighted && r.points > 0 ? html`
                          <b>
                            (${parseFloat((r.points * (c.weight / 100)).toFixed(2)).toLocaleString(this.locale)})
                          </b>
                        ` : nothing }
                        ${parseFloat(r.points).toLocaleString(this.locale)} <sr-lang key="points">Points</sr-lang>
                      </span>
                      ${!this.isLocked ? html`
                        <div class="add-criterion-item">
                          ${this._renderAddRatingButton(c, i + 1)}
                        </div>
                        <span tabindex="0"
                            data-criterion-id="${c.id}"
                            data-rating-id="${r.id}"
                            title="${this.tr("drag_order")}"
                            aria-label="${this.tr("drag_to_reorder_label")}"
                            aria-describedby="rubrics-reorder-info"
                            class="drag-handle reorder-icon sideways si si-drag-handle">
                        </span>
                      ` : nothing }
                    </div>
                  `)}
                  </div>
                </sakai-reorderer>
              </div>
              ${!this.isLocked ? html`
                <div class="criterion-actions">
                  <button type="button" 
                    data-criterion-id="${c.id}" 
                    title="${this.tr("copy")} ${c.title}" 
                    aria-label="${this.tr("copy")} ${c.title}" 
                    class="btn clone fa fa-copy"
                    @keyup="${this.openEditWithKeyboard}" 
                    @click="${this.cloneCriterion}">
                  </button>
                  <sakai-item-delete criterion-id="${c.id}" site-id="${this.siteId}" .criterion=${c} rubric-id="${this.rubricId}" @delete-item=${this.deleteCriterion}></sakai-item-delete>
                </div>
              ` : nothing }
            </div>
          `}
        `)}
        </div>
      </sakai-reorderer>
      ${!this.isLocked ? html`
        <div class="action-buttons">
          ${this.weighted ? html`
            <div class="card mb-3 p-2 w-25">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="fw-bold">${this.tr("total_weight")}</span>
                <span class="${!this.validWeight ? "text-danger" : ""}">${this.totalWeight}%</span>
              </div>
              <div class="d-flex justify-content-between align-items-center">
                <span class="fw-bold">${this.tr("total_grade")}</span>
                <span>${this.maxPoints}</span>
              </div>
            </div>
            <button class="btn-link save-weights" @click="${this.saveWeights}" .disabled=${this._savingWeights || (!this.validWeight && !this.isDraft)}>
              <span class="add fa fa-save" aria-hidden="true"></span>
              ${this._i18n.save_weights}
            </button>
          ` : nothing }
          <button class="btn-link add-criterion" @click="${this.createCriterion}">
            <span class="add fa fa-plus" aria-hidden="true"></span>
            ${this._i18n.add_criterion}
          </button>
          <button class="btn-link add-empty-criterion" @click="${event => this.createCriterion(event, true)}">
            <span class="add fa fa-plus" aria-hidden="true"></span>
            ${this._i18n.add_criterion_group}
          </button>
        </div>
        ${this.isDraft ? html`
        <div class="sak-banner-warn margin-bottom">${this._i18n.draft_info}</div>
        ` : nothing }
      ` : html`
        <div class="sak-banner-warn margin-bottom">${this._i18n.locked_warning}</div>
      `}
      <br>
    `;
  }
}
