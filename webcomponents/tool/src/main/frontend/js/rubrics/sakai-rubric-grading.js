import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import "./sakai-rubric-grading-comment.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";
import { getUserId } from "../sakai-portal-utils.js";

export class SakaiRubricGrading extends RubricsElement {

  constructor() {

    super();

    this.rubric = { title: "" };
    this.criteria = [];
    this.totalPoints = 0;

    SakaiRubricsLanguage.loadTranslations().then(result => this.i18nLoaded = result);
  }

  static get properties() {

    return {
      token: String,
      toolId: { attribute: "tool-id", type: String },
      entityId: { attribute: "entity-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
      group: { type: Boolean},

      // Non attribute
      evaluation: { type: Object },
      totalPoints: Number,
      translatedTotalPoints: { type: Number },
      criteria: { type: Array },
      rubric: { type: Object }
    };
  }

  set token(newValue) {

    if (!newValue.startsWith("Bearer")) {
      this._token = "Bearer " + newValue;
    } else {
      this._token = newValue;
    }

    this.getAssociation();
  }

  get token() {
    return this._token;
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

  render() {

    return html`
      <h3 style="margin-bottom: 10px;">${this.rubric.title}</h3>
      ${this.evaluation && this.evaluation.status === "DRAFT" ? html`
        <div class="sak-banner-warn">
          <sr-lang key="draft_evaluation">DRAFT</sr-lang>
        </div>
      ` : "" }
      <div class="criterion grading style-scope sakai-rubric-criteria-grading" style="margin-bottom: 10px;">
      ${this.criteria.map(c => html`
        <div id="criterion_row_${c.id}" class="criterion-row">
          <div class="criterion-detail" tabindex="0">
            <h4 class="criterion-title">${c.title}</h4>
            <p>${c.description}</p>
            ${this.rubric.weighted ?
              html`
                <div class="criterion-weight">
                  <span>
                    <sr-lang key="weight">Weight</sr-lang>
                  </span>
                  <span>${c.weight}</span>
                  <span>
                    <sr-lang key="percent_sign">%</sr-lang>
                  </span>
                </div>`
              : ""
            }
          </div>
          <div class="criterion-ratings" style="margin-bottom: 15px !important;">
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
                          (${(r.points * (c.weight / 100)).toFixed(2)})
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
            <sakai-rubric-grading-comment id="comment-for-${c.id}" @comment-shown=${this.commentShown} @update-comment="${this.updateComment}" criterion="${JSON.stringify(c)}" evaluated-item-id="${this.evaluatedItemId}" entity-id="${this.entityId}"></sakai-rubric-grading-comment>
            <div>
              <strong id="points-display-${c.id}" class="points-display ${this.getOverriddenClass(c.pointoverride, c.selectedvalue)}">
                ${c.selectedvalue}
              </strong>
            </div>
            ${this.association.parameters.fineTunePoints ? html`
                <input
                    title="${tr("point_override_details")}"
                    data-criterion-id="${c.id}"
                    name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-override-${c.id}"
                    class="fine-tune-points form-control hide-input-arrows"
                    @input=${this.fineTuneRating}
                    .value="${c.pointoverride}"
                />
              ` : ""}
            <input aria-labelledby="${tr("points")}" type="hidden" id="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" .value="${c.selectedvalue}">
            <input type="hidden" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterionrating-${c.id}" .value="${c.selectedRatingId}">
          </div>
        </div>
      `)}
      </div>
      <div class="rubric-totals" style="margin: 10px 0px 10px 0px;">
        <input type="hidden" aria-labelledby="${tr("total")}" id="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" name="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" .value="${this.totalPoints}">
        <div class="total-points">
          <sr-lang key="total">Total</sr-lang>: <strong id="sakai-rubrics-total-points">${this.totalPoints.toLocaleString(this.locale, {maximumFractionDigits: 2})}</strong>
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

    // If there are no criteria, this evaluation has been cancelled.
    if (this.criteria.length == 0) return;

    this.dispatchRatingChanged(this.criteria, 2).then(evaluation => {

      // We've saved the new returned evaluation. We now need to save the returned, backup copy.

      this.getReturnedEvaluation(evaluation.id).then(retEval => {

        retEval.overallComment = evaluation.overallComment;
        retEval.criterionOutcomes = evaluation.criterionOutcomes;
        retEval.criterionOutcomes.forEach(co => { delete co.id; delete co._links; });

        const url = `/rubrics-service/rest/returned-evaluations${retEval?.id ? `/${retEval.id}` : ""}`;
        fetch(url, {
          body: JSON.stringify(retEval),
          credentials: "same-origin",
          headers: {
            "Authorization": this.token,
            "Accept": "application/json",
            "Content-Type": "application/json"
          },
          method: retEval?.id ? "PUT" : "POST",
        })
        .then(r => {

          if (!r.ok) {
            throw new Error("Server error while saving returned evaluation");
          }
        })
        .catch(error => console.error(error));
      })
      .catch(error => console.error(error));
    });
  }

  save() {

    console.debug("save");

    this.dispatchRatingChanged(this.criteria, 1);
  }

  decorateCriteria() {

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

    this.updateTotalPoints({ notify: false });
  }

  fineTuneRating(e) {

    console.debug("fineTuneRating");

    const value = e.target.value;

    const parsed = parseFloat(value.replace(/,/g, "."));

    if (isNaN(parsed)) {
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
    this.dispatchEvent(new CustomEvent("rubric-rating-tuned", { detail: detail, bubbles: true, composed: true }));

    this.updateTotalPoints();
    this.dispatchRatingChanged(this.criteria, 1);
  }

  dispatchRatingChanged(criteria, status) {

    console.debug("dispatchRatingChanged");

    const crit = criteria.map(c => {

      return {
        criterionId: c.id,
        points: c.pointoverride || c.selectedvalue,
        comments: c.comments,
        pointsAdjusted: c.pointoverride !== c.selectedvalue,
        selectedRatingId: c.selectedRatingId
      };
    });

    const evaluation = {
      evaluatorId: getUserId(),
      evaluatedItemId: this.evaluatedItemId,
      evaluatedItemOwnerId: this.evaluatedItemOwnerId,
      evaluatedItemOwnerType: this.group ? "GROUP" : "USER",
      overallComment: "",
      criterionOutcomes: crit,
      toolItemRubricAssociation: this.association._links.self.href,
      status: status,
    };

    if (this.evaluation && this.evaluation.id) {
      evaluation.metadata = this.evaluation.metadata;
    }

    return this.saveEvaluation(evaluation, status);
  }

  saveEvaluation(evaluation) {

    console.debug("saveEvaluation");

    let url = "/rubrics-service/rest/evaluations";
    if (this.evaluation && this.evaluation.id) url += `/${this.evaluation.id}`;
    return fetch(url, {
      body: JSON.stringify(evaluation),
      credentials: "same-origin",
      headers: {
        "Authorization": this.token,
        "Accept": "application/json",
        "Content-Type": "application/json"
      },
      method: this.evaluation && this.evaluation.id ? "PATCH" : "POST"
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

  deleteEvaluation() {

    console.debug("deleteEvaluation");

    if (!this?.evaluation?.id) return;

    const url = `/rubrics-service/rest/evaluations/${this.evaluation.id}`;
    fetch(url, {
      credentials: "same-origin",
      headers: { "Authorization": this.token, },
      method: "DELETE"
    })
    .then(r => {

      if (r.ok) {
        this.updateTotalPoints({ notify: true, totalPoints: 0 });
        this.evaluation = { criterionOutcomes: [] };
        this.criteria.forEach(c => this.emptyCriterion(c));
        this.requestUpdate();
      } else {
        throw new Error("Server error while deleting evaluation");
      }
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
    } else {
      return '';
    }
  }

  emptyCriterion(criterion) {

    console.debug("emptyCriterion");

    criterion.selectedvalue = 0.0;
    criterion.selectedRatingId = 0;
    criterion.pointoverride = 0.0;
    criterion.ratings.forEach(r => r.selected = false);
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
      this.dispatchEvent(new CustomEvent('total-points-updated', { detail: detail, bubbles: true, composed: true }));
    }
  }

  cancel() {

    console.debug("cancel");

    if (this.evaluation.status !== "DRAFT") return;

    // Get the evaluation from session storage. This should be the last non draft evaluation that
    // the server originally sent before the user started setting ratings. Save it baack to the
    // server.
    this.getReturnedEvaluation(this.evaluation.id).then(retEval => {

      if (retEval?.id) {
        this.evaluation.criterionOutcomes = retEval.criterionOutcomes;
        this.evaluation.overallComment = retEval.overallComment;
        this.evaluation.status = 2;

        // Save cached evaluation and reset the criteria ready for rendering
        this.saveEvaluation(this.evaluation).then(() => {

          // Unset any ratings
          this.criteria.forEach(c => c.ratings.forEach(r => r.selected = false));
          // And set the original ones
          this.decorateCriteria();
          this.updateTotalPoints();
        });
      } else {
        this.deleteEvaluation();
      }
    }).catch(error => console.error(error));
  }

  getAssociation() {

    console.debug("getAssociation");

    if (!this.toolId || !this.entityId || !this.token || !this.evaluatedItemId) {
      return;
    }

    const url = `/rubrics-service/rest/rubric-associations/search/by-tool-and-assignment?toolId=${this.toolId}&itemId=${this.entityId}`;
    fetch(url, {
      credentials: "same-origin",
      headers: { "Authorization": this.token, "Accept": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Failed to retrieve association from ${url}. Status: ${r.status}`);
    })
    .then(data => {

      this.association = data._embedded['rubric-associations'][0];
      this.rubricId = data._embedded['rubric-associations'][0].rubricId;
      this.getRubric(this.rubricId);
    })
    .catch(error => console.error(error));
  }

  getRubric(rubricId) {

    console.debug("getRubric");

    const url = `/rubrics-service/rest/rubrics/${rubricId}?projection=inlineRubric`;
    fetch(url, {
      credentials: "same-origin",
      headers: { "Authorization": this.token, "Accept": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Failed to retrieve rubric from ${url}. Status: ${r.status}`);
    })
    .then(rubric => {

      const evaluationUrl = `/rubrics-service/rest/evaluations/search/by-tool-and-assignment-and-submission?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`;
      fetch(evaluationUrl, {
        credentials: "same-origin",
        headers: { "Authorization": this.token },
      })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Failed to retrieve evaluation from ${evaluationUrl}. Status: ${r.status}`);
      })
      .then(data => {

        this.evaluation = data._embedded.evaluations[0] || { criterionOutcomes: [] };

        this.rubric = rubric;

        this.criteria = this.rubric.criterions;
        this.criteria.forEach(c => {

          c.pointoverride = "";

          if (!c.selectedvalue) {
            c.selectedvalue = 0;
          }
          c.pointrange = this.getHighLow(c.ratings);
        });

        this.decorateCriteria();
        this.updateTotalPoints();
      })
      .catch(error => console.error(error));
    })
    .catch(error => console.error(error));
  }

  getReturnedEvaluation(originalEvaluationId) {

    console.debug("getReturnedEvaluation");

    const returnedUrl = `/rubrics-service/rest/returned-evaluations/search/by-original-evaluation-id?id=${originalEvaluationId}`;
    return fetch(returnedUrl, {
      credentials: "same-origin",
      headers: { "Authorization": this.token, "Accept": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      } else if (r.status === 404) {
        return Promise.resolve({ originalEvaluationId });
      }

      throw new Error("Server error while retrieving returned evaluation");
    });
  }
}

const tagName = "sakai-rubric-grading";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricGrading);
