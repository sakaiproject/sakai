import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "../sakai-rubric-grading-comment.js";
import "../sakai-rubric-pdf.js";
import "../sakai-rubric-summary.js";
import { getUserId } from "@sakai-ui/sakai-portal-utils";
import { rubricsApiMixin } from "./SakaiRubricsApiMixin.js";
import { GRADING_RUBRIC, CRITERIA_SUMMARY, STUDENT_SUMMARY } from "./sakai-rubrics-constants.js";

export class SakaiRubricGrading extends rubricsApiMixin(RubricsElement) {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    entityId: { attribute: "entity-id", type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
    isPeerOrSelf: { attribute: "is-peer-or-self", type: Boolean },
    isPeerGroupGraded: { attribute: "is-peer-group-graded", type: Boolean },
    group: { type: Boolean },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },
    totalAsPercentage: { attribute: "total-as-percentage", type: Boolean },

    _evaluation: { state: true },
    _totalPoints: { state: true },
    _criteria: { state: true },
    _rubric: { state: true },
    _currentView: { state: true },
  };

  constructor() {

    super();

    this._rubric = { title: "" };
    this._criteria = [];
    this._totalPoints = -1;

    this._currentView = GRADING_RUBRIC;

    this.instanceSalt = Math.floor(Math.random() * Date.now());
  }

  set entityId(value) {

    this._entityId = value;
    this._getAssociation();
  }

  get entityId() { return this._entityId; }

  set evaluatedItemId(value) {

    this._evaluatedItemId = value;
    this._getAssociation();
  }

  get evaluatedItemId() { return this._evaluatedItemId; }

  set toolId(value) {

    this._toolId = value;
    this._getAssociation();
  }

  get toolId() { return this._toolId; }

  _viewSelected(e) {

    this._currentView = e.target.value;

    switch (e.target.value) {
      case GRADING_RUBRIC:
        this.openGradePreviewTab();
        break;
      case STUDENT_SUMMARY:
        this.makeStudentSummary();
        break;
      case CRITERIA_SUMMARY:
        this.makeCriteriaSummary();
        break;
      default:
    }
  }

  closeCommentEditors() {
    this.querySelectorAll("sakai-rubric-grading-comment").forEach(c => c.hideEditor());
  }

  shouldUpdate() {
    return this._i18n && this.association;
  }

  render() {

    return html`
      <div class="rubric-details grading">
        <h3>
          <span>${this._rubric.title}</span>
          ${this.enablePdfExport ? html`
            <sakai-rubric-pdf
                rubric-title="${this._rubric.title}"
                site-id="${this.siteId}"
                rubric-id="${this._rubric.id}"
                tool-id="${this.toolId}"
                entity-id="${this.entityId}"
                evaluated-item-id="${this.evaluatedItemId}">
            </sakai-rubric-pdf>
          ` : nothing }
        </h3>

        <select @change=${this._viewSelected}
            aria-label="${this._i18n.rubric_view_selection_title}"
            title="${this._i18n.rubric_view_selection_title}" .value=${this._currentView}>
          <option value="grading-rubric">${this._i18n.grading_rubric}</option>
          <option value="${STUDENT_SUMMARY}">${this._i18n.student_summary}</option>
          <option value="${CRITERIA_SUMMARY}">${this._i18n.criteria_summary}</option>
        </select>

        <div id="rubric-grading-or-preview-${this.instanceSalt}" class="rubric-tab-content rubrics-visible mt-1">
          ${this._evaluation && this._evaluation.status === "DRAFT" && !this.isPeerOrSelf ? html`
          <div class="sak-banner-warn">
            ${this.tr("draft_evaluation", [ this.tr(`draft_evaluation_${this.toolId}`) ])}
          </div>
          ` : html`
            <div class="mb-3"></div>
          `}
          <div class="criterion grading style-scope sakai-rubric-criteria-grading">
          ${this._criteria.map(c => html`
            <div id="criterion_row_${c.id}" class="criterion-row">
              ${this.isCriterionGroup(c) ? html`
                <div id="criterion_row_${c.id}" class="criterion-group">
                  <div class="criterion-detail">
                    <h4 class="criterion-title">${c.title}</h4>
                    <p>${unsafeHTML(c.description)}</p>
                  </div>
                </div>
              ` : html`
                <div class="criterion-detail" tabindex="0">
                  <h4 class="criterion-title">${c.title}</h4>
                  <p>${unsafeHTML(c.description)}</p>
                  ${this._rubric.weighted ? html`
                    <div class="criterion-weight">
                      <span>${this._i18n.weight}</span>
                      <span>${c.weight.toLocaleString(this.locale)}</span>
                      <span>${this._i18n.percent_sign}</span>
                    </div>
                  ` : nothing }
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
                          ${this._rubric.weighted && r.points > 0 ? html`
                            <b>
                              (${parseFloat((r.points * (c.weight / 100)).toFixed(2)).toLocaleString(this.locale)})
                            </b>
                          ` : nothing }
                          ${r.points.toLocaleString(this.locale)}
                          ${this._i18n.points}
                        </span>
                      </div>
                    `)}
                    </div>
                  </div>
                </div>
                <div class="criterion-actions">
                  <sakai-rubric-grading-comment id="comment-for-${c.id}"
                      @comment-shown=${this.commentShown}
                      @update-comment=${this.updateComment}
                      .criterion=${c}
                      evaluated-item-id="${this.evaluatedItemId}"
                      entity-id="${this.entityId}">
                  </sakai-rubric-grading-comment>
                  <div class="rubric-grading-points-value">
                    <strong id="points-display-${c.id}" class="points-display ${this.getOverriddenClass(c.pointoverride, c.selectedvalue)}">
                      ${c.selectedvalue?.toLocaleString(this.locale) || 0}
                    </strong>
                  </div>
                  ${this.association.parameters.fineTunePoints ? html`
                    <input
                        title="${this.tr("point_override_details")}"
                        aria-label="${this.tr("point_override_details")}"
                        data-criterion-id="${c.id}"
                        name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-override-${c.id}"
                        class="fine-tune-points form-control hide-input-arrows"
                        @input=${this.fineTuneRating}
                        .value="${c.pointoverride && typeof c.pointoverride === "number" ? c.pointoverride.toLocaleString(this.locale) : c.pointoverride}"
                    >
                  ` : nothing }
                  <input aria-labelledby="${this.tr("points")}" type="hidden" id="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" .value="${c.selectedvalue}">
                  <input type="hidden" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterionrating-${c.id}" .value="${c.selectedRatingId}">
                </div>
              </div>
              `}
            </div>
          `)}
          </div>
          <div class="rubric-totals">
            <input type="hidden"
                aria-labelledby="${this.tr("total")}"
                id="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints"
                name="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints"
                .value="${this._totalPoints.toString()}">
            <div class="total-points">
              <span>${this._i18n.total}</span>:
              <strong id="sakai-rubrics-total-points">
              ${this._totalPoints.toLocaleString(this.locale, { maximumFractionDigits: 2 })} ${this.totalAsPercentage ? this.tr("percent_sign") : ""}
              </strong>
            </div>
          </div>
        </div>
      </div>
      <div id="rubric-student-summary-${this.instanceSalt}" class="rubric-tab-content"></div>
      <div id="rubric-criteria-summary-${this.instanceSalt}" class="rubric-tab-content"></div>
    `;
  }

  openGradePreviewTab() {
    this.openRubricsTab(`rubric-grading-or-preview-${this.instanceSalt}`);
  }

  makeStudentSummary() {
    this.makeASummary("student", this.siteId);
  }

  makeCriteriaSummary() {
    this.makeASummary("criteria", this.siteId);
  }

  updateComment(e) {

    this._criteria.forEach(c => {

      if (c.id === e.detail.criterionId) {
        c.comments = e.detail.value;
      }
    });

    this.dispatchRatingChanged(this._criteria, 1);
  }

  release() {

    if (this._evaluation.criterionOutcomes.length) {
      // We only want to inform the enclosing tool about ratings changes
      // for an existing evaluation
      this.dispatchRatingChanged(this._criteria, 2);
    }
  }

  save() {
    this.dispatchRatingChanged(this._criteria, 1);
  }

  decorateCriteria() {

    this._evaluation.criterionOutcomes.forEach(ed => {

      this._criteria.forEach(c => {

        if (ed.criterionId === c.id) {

          c.selectedRatingId = ed.selectedRatingId;
          if (ed.pointsAdjusted) {
            c.pointoverride = ed.points;
            const ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];
            if (ratingItem) {
              c.selectedvalue = this.calculateCriterionScore(c, ratingItem);
              ratingItem.selected = true;
            }
          } else {
            const ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];
            if (ratingItem) {
              ratingItem.selected = true;
              // Apply weight if rubric is weighted
              const points = this.calculateCriterionScore(c, ratingItem);
              c.selectedvalue = points;
              c.pointoverride = points;
            } else {
              c.pointoverride = ed.points;
              c.selectedvalue = ed.points;
            }
          }

          c.comments = ed.comments;
        }
      });
    });

    this.updateTotalPoints(false);

    this.querySelectorAll("sakai-rubric-grading-comment").forEach(gc => gc.requestUpdate());
  }

  fineTuneRating(e) {

    const value = e.target.value;

    const parsed = value.replace(/,/g, ".");

    if (isNaN(parseFloat(parsed))) {
      return;
    }

    const criterion = this._criteria.find(c => c.id == e.target.dataset.criterionId);

    criterion.pointoverride = parsed;
    if (criterion.selectedvalue) {
      this._totalPoints = this._totalPoints - criterion.selectedvalue + criterion.pointoverride;
    } else {
      this._totalPoints = this._totalPoints + criterion.pointoverride;
    }

    const detail = {
      evaluatedItemId: this.evaluatedItemId,
      entityId: this.entityId,
      criterionId: criterion.id,
      value: criterion.pointoverride,
    };
    this.dispatchEvent(new CustomEvent("rubric-rating-tuned", { detail, bubbles: true, composed: true }));

    this.updateTotalPoints();
    this.dispatchRatingChanged(this._criteria, 1);
  }

  dispatchRatingChanged(criteria, status) {

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
      evaluatorId: this.isPeerGroupGraded ? this.evaluatedItemId : getUserId(),
      id: this._evaluation.id,
      evaluatedItemId: this.evaluatedItemId,
      evaluatedItemOwnerId: this.evaluatedItemOwnerId,
      evaluatedItemOwnerType: this.group ? "GROUP" : "USER",
      overallComment: "",
      criterionOutcomes: crit,
      associationId: this.association.id,
      peerOrSelf: this.isPeerOrSelf,
      status,
    };

    if (this._evaluation && this._evaluation.id) {
      evaluation.metadata = this._evaluation.metadata;
    }

    let url = `/api/sites/${this.siteId}/rubric-evaluations`;
    if (this._evaluation?.id) url += `/${this._evaluation.id}`;
    fetch(url, {
      body: JSON.stringify(evaluation),
      headers: { "Content-Type": "application/json" },
      method: this._evaluation?.id ? "PUT" : "POST",
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Server error while saving rubric evaluation");
    })
    .then(data => {

      this.dispatchEvent(new CustomEvent("rubric-ratings-changed", { bubbles: true }));
      this._evaluation = data;
      return Promise.resolve(this._evaluation);
    })
    .catch(error => console.error(error));
  }

  getOverriddenClass(ovrdvl, selected) {

    if (!this.association.parameters.fineTunePoints) {
      return "";
    }

    if ((ovrdvl || ovrdvl === 0) && parseFloat(ovrdvl) !== parseFloat(selected)) {
      return "strike";
    }
    return "";
  }

  clear() {

    this._evaluation = {};
    this._criteria.forEach(c => {

      c.ratings.forEach(r => r.selected = false);
      c.pointoverride = "";
      c.comments = "";
      c.selectedvalue = 0;
      this.querySelectorAll("sakai-rubric-grading-comment").forEach(gc => gc.requestUpdate());
    });
    this._totalPoints = 0;
    this.requestUpdate();
  }

  displayGradingTab() {

    this.openGradePreviewTab();
    this._currentView = GRADING_RUBRIC;
  }

  emptyCriterion(criterion) {

    criterion.selectedvalue = 0.0;
    criterion.selectedRatingId = 0;
    criterion.pointoverride = 0.0;
    criterion.ratings.forEach(r => r.selected = false);
  }

  toggleRating(e) {

    e.stopPropagation();

    const criterionId = parseInt(e.currentTarget.dataset.criterionId);
    const ratingId = parseInt(e.currentTarget.dataset.ratingId);

    // Look up the criterion and rating objects
    const criterion = this._criteria.filter(c => c.id == criterionId)[0];
    const rating = criterion.ratings.filter(r => r.id === ratingId)[0];

    if (rating.selected) {
      this.emptyCriterion(criterion);
      rating.selected = false;
      e.currentTarget.blur();
    } else {
      criterion.ratings.forEach(r => r.selected = false);
      const auxPoints = this._rubric.weighted ?
        (rating.points * (criterion.weight / 100)).toFixed(2) : rating.points;
      criterion.selectedvalue = auxPoints;
      criterion.selectedRatingId = rating.id;
      criterion.pointoverride = auxPoints;
      rating.selected = true;
    }

    // Whenever a rating is clicked, either to select or deselect, it cancels out any override so we
    // remove the strike out from the clicked points value
    this.querySelector(`#points-display-${criterionId}`).classList.remove("strike");

    this.requestUpdate();
    this.updateTotalPoints();

    this.dispatchRatingChanged(this._criteria, 1);
  }

  commentShown(e) {
    this.querySelectorAll(`sakai-rubric-grading-comment:not(#${e.target.id})`).forEach(c => c.hide());
  }

  updateTotalPoints(notify = true) {

    const points = this._criteria.reduce((a, c) => {

      if (c.pointoverride) {
        return a + parseFloat(c.pointoverride);
      } else if (c.selectedvalue) {
        return a + parseFloat(c.selectedvalue);
      }
      return a;

    }, 0);

    this._totalPoints = this.totalAsPercentage ? (points / this._maxPoints) * 100 : points;

    // Make sure total points is not negative
    if (parseFloat(this._totalPoints) < 0) this._totalPoints = 0;

    if (notify) {
      const detail = {
        evaluatedItemId: this.evaluatedItemId,
        entityId: this.entityId,
        value: this._totalPoints.toLocaleString(this.locale, { maximumFractionDigits: 2 }),
      };

      this.dispatchEvent(new CustomEvent("total-points-updated", { detail, bubbles: true, composed: true }));
    }
  }

  cancel() {

    if (this._evaluation.status !== "DRAFT") return;

    const url = `/api/sites/${this.siteId}/rubric-evaluations/${this._evaluation.id}/cancel`;

    fetch(url, { credentials: "include" })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Failed to cancel rubric evaluation");
    })
    .then(restored => {

      this._evaluation = restored;
      // Unset any ratings
      this._criteria.forEach(c => c.ratings.forEach(r => r.selected = false));
      // And set the original ones
      this.decorateCriteria();
    })
    .catch(error => console.error(error));
  }

  _getAssociation() {

    if (!this.toolId || !this.entityId || !this.evaluatedItemId) {
      return;
    }

    this.apiGetAssociation()
      .then(association => {

        this.association = association;
        this._rubricId = association.rubricId;
        this._getRubric(this._rubricId);
      })
      .catch (error => console.error(error));
  }

  _getRubric(rubricId) {

    this.apiGetRubric(rubricId)
      .then(rubric => {

        this._rubric = rubric;
        this._criteria = this._rubric.criteria;

        if (this.evaluatedItemId) {

          this.apiGetEvaluation()
            .then(evaluation => {

              this._evaluation = evaluation || { criterionOutcomes: [] };
              this._criteria.forEach(c => {

                c.pointoverride = "";

                if (!c.selectedvalue) {
                  c.selectedvalue = 0;
                }
                c.pointrange = this.getHighLow(c.ratings);
              });

              this.decorateCriteria();

              if (this.isPeerOrSelf) { // For self-review buttons locking
                this.dispatchEvent(new CustomEvent("rubrics-grading-loaded", { bubbles: true, composed: true }));
                this.updateComplete.then(() => this.dispatchEvent(new CustomEvent("rubric-ratings-changed", { bubbles: true, composed: true })));
              }
            })
            .catch(error => console.error(error));
        }

        this._maxPoints = this._criteria.reduce((total, criterion) => {
          if (criterion.ratings.length === 0) return total;

          const maxRatingPoints = Math.max(...criterion.ratings.map(r => {
            return this.calculateCriterionScore(criterion, r);
          }));

          return total + maxRatingPoints;
        }, 0);
      })
      .catch(error => console.error(error));
  }

  calculateCriterionScore(criterion, rating) {
    return this._rubric.weighted && criterion.weight ?
      rating.points * (criterion.weight / 100).toFixed(2) :
      rating.points;
  }
}
