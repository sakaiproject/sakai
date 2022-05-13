import { RubricsElement } from "./rubrics-element.js";
import { html } from "../assets/lit-element/lit-element.js";
import "./sakai-rubric-grading-comment.js";
import "./sakai-rubric-pdf.js";
import { unsafeHTML } from "/webcomponents/assets/lit-html/directives/unsafe-html.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";
import { getUserId } from "../sakai-portal-utils.js";

export class SakaiRubricGrading extends RubricsElement {

  constructor() {

    super();

    this.rubric = { title: "" };
    this.criteria = [];
    this.totalPoints = 0;

    SakaiRubricsLanguage.loadTranslations().then(r => this.i18nLoaded = r);
  }

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      entityId: { attribute: "entity-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
      group: { type: Boolean},
      enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },

      // Non attribute
      evaluation: { attribute: false, type: Object },
      totalPoints: { attribute: false, type: Number },
      translatedTotalPoints: { attribute: false, type: Number },
      criteria: { attribute: false, type: Array },
      rubric: { attribute: false, type: Object },
    };
  }

  set entityId(value) {

    this._entityId = value;
    this.getAssociation();
  }

  get entityId() { return this._entityId; }

  set evaluatedItemId(value) {

    this._evaluatedItemId = value;
    this.getAssociation();
  }

  get evaluatedItemId() { return this._evaluatedItemId; }

  set toolId(value) {

    this._toolId = value;
    this.getAssociation();
  }

  get toolId() { return this._toolId; }

  shouldUpdate() {
    return this.i18nLoaded;
  }

  render() {

    return html`
      <div class="rubric-details grading">
        <h3>
          <span>${this.rubric.title}</span>
          ${this.enablePdfExport ? html`
            <sakai-rubric-pdf
                rubric-title="${this.rubric.title}"
                site-id="${this.siteId}"
                rubric-id="${this.rubric.id}"
                tool-id="${this.toolId}"
                entity-id="${this.entityId}"
                evaluated-item-id="${this.evaluatedItemId}"
            />
          ` : ""}
        </h3>
        ${this.evaluation && this.evaluation.status === "DRAFT" ? html`
          <div class="sak-banner-warn">
            ${tr('draft_evaluation', [tr(`draft_evaluation_${this.toolId}`)])}
          </div>
        ` : "" }
        <div class="criterion grading style-scope sakai-rubric-criteria-grading">
        ${this.criteria.map(c => html`
          <div id="criterion_row_${c.id}" class="criterion-row">
            ${this.isCriterionGroup(c) ? html`
              <div id="criterion_row_${c.id}" class="criterion-row criterion-group">
                <div class="criterion-detail">
                  <h4 class="criterion-title">${c.title}</h4>
                  <p>${unsafeHTML(c.description)}</p>
                </div>
              </div>
            ` : html`
              <div class="criterion-detail" tabindex="0">
                <h4 class="criterion-title">${c.title}</h4>
                <p>${unsafeHTML(c.description)}</p>
                ${this.rubric.weighted ?
                  html`
                    <div class="criterion-weight">
                      <span>
                        <sr-lang key="weight">Weight</sr-lang>
                      </span>
                      <span>${c.weight.toLocaleString(this.locale)}</span>
                      <span>
                        <sr-lang key="percent_sign">%</sr-lang>
                      </span>
                    </div>`
                  : ""
                }
              </div>
              <div class="criterion-ratings">
                <div class="cr-table">
                  <div class="cr-table-row">
                  ${c.ratings.map(r => html`
                    <div class="rating-item ${r.selected ? "selected" : ""}"
                          tabindex="0"
                          data-rating-id="${r.id}"
                          id="rating-item-${r.id}"
                          data-criterion-id="${c.id}"
                          @keypress=${this.toggleRating}
                          @click=${this.toggleRating}>
                      <h5 class="criterion-item-title">${r.title}</h5>
                      <p>${r.description}</p>
                      <span class="points" data-points="${r.points}">
                        ${this.rubric.weighted && r.points > 0 ?
                          html`
                            <b>
                              (${parseFloat((r.points * (c.weight / 100)).toFixed(2)).toLocaleString(this.locale)})
                            </b>`
                          : ""
                        }
                        ${r.points.toLocaleString(this.locale)}
                        <sr-lang key="points">Points</sr-lang>
                      </span>
                    </div>
                  `)}
                  </div>
                </div>
              </div>
              <div class="criterion-actions">
                <sakai-rubric-grading-comment id="comment-for-${c.id}"
                    @comment-shown=${this.commentShown}
                    @update-comment="${this.updateComment}"
                    criterion="${JSON.stringify(c)}"
                    evaluated-item-id="${this.evaluatedItemId}"
                    entity-id="${this.entityId}">
                </sakai-rubric-grading-comment>
                <div class="rubric-grading-points-value">
                  <strong id="points-display-${c.id}" class="points-display ${this.getOverriddenClass(c.pointoverride, c.selectedvalue)}">
                    ${c.selectedvalue.toLocaleString(this.locale)}
                  </strong>
                </div>
                ${this.association.parameters.fineTunePoints ? html`
                    <input
                        title="${tr("point_override_details")}"
                        data-criterion-id="${c.id}"
                        name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-override-${c.id}"
                        class="fine-tune-points form-control hide-input-arrows"
                        @input=${this.fineTuneRating}
                        .value="${c.pointoverride.toLocaleString(this.locale)}"
                    />
                  ` : ""}
                <input aria-labelledby="${tr("points")}" type="hidden" id="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" .value="${c.selectedvalue}">
                <input type="hidden" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterionrating-${c.id}" .value="${c.selectedRatingId}">
              </div>
            </div>
          `}
        `)}
        </div>
        <div class="rubric-totals">
          <input type="hidden" aria-labelledby="${tr("total")}" id="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" name="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" .value="${this.totalPoints}">
          <div class="total-points">
            <sr-lang key="total">Total</sr-lang>: <strong id="sakai-rubrics-total-points">${this.totalPoints.toLocaleString(this.locale, {maximumFractionDigits: 2})}</strong>
          </div>
        </div>
      </div>
    `;
  }

  updateComment(e) {

    console.debug("updateComment");

    this.criteria.forEach(c => {

      if (c.id === e.detail.criterionId) {
        c.comments = e.detail.value;
      }
    });

    this.dispatchRatingChanged(this.criteria, 1);
  }

  release() {

    console.debug("release");

    if (this.evaluation.criterionOutcomes.length) {
      // We only want to inform the enclosing tool about ratings changes
      // for an existing evaluation
      this.dispatchRatingChanged(this.criteria, 2);
    }
  }

  save() {

    console.debug("save");

    this.dispatchRatingChanged(this.criteria, 1);
  }

  decorateCriteria(options = { notify: false }) {

    console.debug("decorateCriteria");

    this.evaluation.criterionOutcomes.forEach(ed => {

      this.criteria.forEach(c => {

        if (ed.criterionId === c.id) {

          c.selectedRatingId = ed.selectedRatingId;
          if (ed.pointsAdjusted) {
            c.pointoverride = ed.points;
            const ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];
            if (ratingItem) {
              c.selectedvalue = ratingItem.points;
              ratingItem.selected = true;
            }
          } else {
            const ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];
            if (ratingItem) {
              ratingItem.selected = true;
            }
            c.pointoverride = ed.points;
            c.selectedvalue = ed.points;
          }

          c.comments = ed.comments;
        }
      });
    });

    this.updateTotalPoints(options);
  }

  fineTuneRating(e) {

    console.debug("fineTuneRating");

    const value = e.target.value;

    const parsed = value.replace(/,/g, ".");

    if (isNaN(parseFloat(parsed))) {
      return;
    }

    const criterion = this.criteria.find(c => c.id == e.target.dataset.criterionId);

    criterion.pointoverride = parsed;
    if (criterion.selectedvalue) {
      this.totalPoints = this.totalPoints - criterion.selectedvalue + criterion.pointoverride;
    } else {
      this.totalPoints = this.totalPoints + criterion.pointoverride;
    }

    this.dispatchEvent(new CustomEvent("rubric-ratings-changed", { bubbles: true, composed: true }));
    const detail = {
      evaluatedItemId: this.evaluatedItemId,
      entityId: this.entityId,
      criterionId: criterion.id,
      value: criterion.pointoverride,
    };
    this.dispatchEvent(new CustomEvent("rubric-rating-tuned", { detail, bubbles: true, composed: true }));

    this.updateTotalPoints();
    this.dispatchRatingChanged(this.criteria, 1);
  }

  dispatchRatingChanged(criteria, status) {

    console.debug("dispatchRatingChanged");

    const crit = criteria.map(c => {

      return {
        criterionId: c.id,
        points: c.pointoverride ? parseFloat(c.pointoverride) : c.selectedvalue,
        comments: c.comments,
        pointsAdjusted: c.pointoverride !== c.selectedvalue,
        selectedRatingId: c.selectedRatingId
      };
    });

    const evaluation = {
      evaluatorId: getUserId(),
      id: this.evaluation.id,
      evaluatedItemId: this.evaluatedItemId,
      evaluatedItemOwnerId: this.evaluatedItemOwnerId,
      evaluatedItemOwnerType: this.group ? "GROUP" : "USER",
      overallComment: "",
      criterionOutcomes: crit,
      associationId: this.association.id,
      status,
    };

    if (this.evaluation && this.evaluation.id) {
      evaluation.metadata = this.evaluation.metadata;
    }

    let url = `/api/sites/${this.siteId}/rubric-evaluations`;
    if (this.evaluation?.id) url += `/${this.evaluation.id}`;
    fetch(url, {
      body: JSON.stringify(evaluation),
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      method: this.evaluation?.id ? "PUT" : "POST",
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Server error while saving rubric evaluation");
    })
    .then(data => {

      this.evaluation = data;
      return Promise.resolve(this.evaluation);
    })
    .catch(error => console.error(error));
  }

  getOverriddenClass(ovrdvl, selected) {

    console.debug("getOverriddenClass");

    if (!this.association.parameters.fineTunePoints) {
      return '';
    }

    if ((ovrdvl || ovrdvl === 0) && parseFloat(ovrdvl) !== parseFloat(selected)) {
      return 'strike';
    }
    return '';
  }

  emptyCriterion(criterion) {

    console.debug("emptyCriterion");

    criterion.selectedvalue = 0.0;
    criterion.selectedRatingId = 0;
    criterion.pointoverride = 0.0;
    criterion.ratings.forEach(r => r.selected = false);
    criterion.comments = undefined;
  }

  toggleRating(e) {

    console.debug("toggleRating");

    e.stopPropagation();

    const criterionId = parseInt(e.currentTarget.dataset.criterionId);
    const ratingId = parseInt(e.currentTarget.dataset.ratingId);

    // Look up the criterion and rating objects
    const criterion = this.criteria.filter(c => c.id == criterionId)[0];
    const rating = criterion.ratings.filter(r => r.id === ratingId)[0];

    criterion.ratings.forEach(r => r.selected = false);

    if (rating.selected) {
      this.emptyCriterion(criterion);
      rating.selected = false;
    } else {
      const auxPoints = this.rubric.weighted ?
        (rating.points * (criterion.weight / 100)).toFixed(2) : rating.points;
      criterion.selectedvalue = auxPoints;
      criterion.selectedRatingId = rating.id;
      criterion.pointoverride = auxPoints;
      rating.selected = true;
    }

    // Whenever a rating is clicked, either to select or deselect, it cancels out any override so we
    // remove the strike out from the clicked points value
    this.querySelector(`#points-display-${criterionId}`).classList.remove("strike");

    this.dispatchEvent(new CustomEvent("rubric-ratings-changed", { bubbles: true, composed: true }));
    this.requestUpdate();
    this.updateTotalPoints();

    this.dispatchRatingChanged(this.criteria, 1);
  }

  commentShown(e) {

    console.debug("commentShown");

    this.querySelectorAll(`sakai-rubric-grading-comment:not(#${e.target.id})`).forEach(c => c.hide());
  }

  updateTotalPoints(options = { notify: true }) {

    console.debug("updateTotalPoints");

    if (typeof options.totalPoints !== "undefined") {
      this.totalPoints = options.totalPoints;
    } else {
      this.totalPoints = this.criteria.reduce((a, c) => {

        if (c.pointoverride) {
          return a + parseFloat(c.pointoverride);
        } else if (c.selectedvalue) {
          return a + parseFloat(c.selectedvalue);
        }
        return a;

      }, 0);
    }

    // Make sure total points is not negative
    if (parseFloat(this.totalPoints) < 0) this.totalPoints = 0;

    if (options.notify) {
      const detail = {
        evaluatedItemId: this.evaluatedItemId,
        entityId: this.entityId,
        value: this.totalPoints.toLocaleString(this.locale, { maximumFractionDigits: 2 }),
      };

      this.dispatchEvent(new CustomEvent('total-points-updated', { detail, bubbles: true, composed: true }));
    }
  }

  cancel() {

    console.debug("cancel");

    if (this.evaluation.status !== "DRAFT") return;

    const url = `/api/sites/${this.siteId}/rubric-evaluations/${this.evaluation.id}/cancel`;

    fetch(url, { credentials: "include" })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Failed to cancel rubric evaluation");
    })
    .then(restored => {

      this.evaluation = restored;
      // Unset any ratings
      this.criteria.forEach(c => c.ratings.forEach(r => r.selected = false));
      // And set the original ones
      this.decorateCriteria();
    })
    .catch(error => console.error(error));
  }

  getAssociation() {

    console.debug("getAssociation");

    if (!this.toolId || !this.entityId || !this.evaluatedItemId) {
      return;
    }

    const url = `/api/sites/${this.siteId}/rubric-associations/tools/${this.toolId}/items/${this.entityId}`;
    fetch(url, { credentials: "include" })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Network error while getting association");
    })
    .then(association => {

      this.association = association;
      this.rubricId = association.rubricId;
      this.getRubric(this.rubricId);
    })
    .catch (error => console.error(error));
  }

  getRubric(rubricId) {

    console.debug("getRubric");

    const rubricUrl = `/api/sites/${this.siteId}/rubrics/${rubricId}`;
    fetch(rubricUrl, { credentials: "include" })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Network error while getting rubric");
    })
    .then(rubric => {

      const url = `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
      fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        if (r.status !== 404) {
          throw new Error("Network error while getting evaluation");
        }
      })
      .then(evaluation => {

        this.evaluation = evaluation || { criterionOutcomes: [] };

        this.rubric = rubric;

        this.criteria = this.rubric.criteria;
        this.criteria.forEach(c => {

          c.pointoverride = "";

          if (!c.selectedvalue) {
            c.selectedvalue = 0;
          }
          c.pointrange = this.getHighLow(c.ratings);
        });

        this.decorateCriteria();
      })
      .catch(error => console.error(error));
    })
    .catch(error => console.error(error));
  }
}

const tagName = "sakai-rubric-grading";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricGrading);
