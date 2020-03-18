import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {ifDefined} from '/webcomponents/assets/lit-html/directives/if-defined.js';
import {SakaiRubricGradingComment} from "./sakai-rubric-grading-comment.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricCriteriaGrading extends RubricsElement {

  constructor() {

    super();

    this.selectedRatings = [];
  }

  static get properties() {

    return {
      criteria: { type: Array},
      rubricAssociation: { attribute: "rubric-association", type: Object },
      urlEncodedStateDetails: { attribute: "state-details", type: String },
      entityId: { attribute: "entity-id", type: String},
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      totalPoints: { type: Number },
      evaluationDetails: { attribute: "evaluation-details", type: Array },
      stateDetailsJson: String,
      selectedRatings: { type: Array },
    };
  }

  set urlEncodedStateDetails(newValue) {

    this._urlEncodedStateDetails = newValue;
    this.stateDetails = JSON.parse(unescape(newValue));
    this.stateDetailsJson = newValue;
    if (this.criteria) this.setupCriteriaFromState();
  }

  get urlEncodedStateDetails() { return this._urlEncodedStateDetails; }

  set evaluationDetails(newVal) {

    this.selectedRatings = newVal.map(ed => ed.selectedRatingId);
    this._evaluationDetails = newVal;
    if (this.criteria) this.decorateCriteria();
  }

  get evaluationDetails() { return this._evaluationDetails; }

  set criteria(newVal) {

    var oldVal = this._criteria;
    this._criteria = newVal;
    this.criteria.forEach(c => {

      if (!c.selectedvalue) {
        c.selectedvalue = 0;
      }
      c.pointrange = this.getHighLow(c.ratings, "points");
    });

    if (this.evaluationDetails) this.decorateCriteria();

    if (this.stateDetails) {
      this.setupCriteriaFromState();
    }
  }

  get criteria() { return this._criteria; }

  render() {

    return html`
      <div class="criterion grading style-scope sakai-rubric-criteria-grading" style="margin-bottom: 10px;">
      ${this.criteria.map(c => html`
        <div id="criterion_row_${c.id}" class="criterion-row">
          <div class="criterion-detail">
            <h4 class="criterion-title">${c.title}</h4>
            <p>${c.description}</p>
          </div>
          <div class="criterion-ratings" style="margin-bottom: 15px !important;">
            <div class="cr-table">
              <div class="cr-table-row">
              ${c.ratings.map(r => html`
                <div class="rating-item ${this.selectedRatings.includes(r.id) ? "selected" : ""}" data-rating-id="${r.id}" id="rating-item-${r.id}" data-criterion-id="${c.id}" @click="${this.toggleRating}">
                  <h5 class="criterion-item-title">${r.title}</h5>
                  <p>${r.description}</p>
                  <span class="points" data-points="${r.points}">${r.points.toLocaleString(this.locale)} <sr-lang key="points">Points</sr-lang></span>
                </div>
              `)}
              </div>
            </div>
          </div>
          <div class="criterion-actions">
            <sakai-rubric-grading-comment id="comment-for-${c.id}" @comment-shown=${this.commentShown} @update-comment="${this.updateComment}" criterion="${JSON.stringify(c)}" evaluated-item-id="${this.evaluatedItemId}" entity-id="${this.entityId}"></sakai-rubric-grading-comment>
            <div>
              <strong id="points-display-${c.id}" class="points-display ${this.getOverriddenClass(c.pointoverride,c.selectedvalue)}">
                ${c.selectedvalue}
              </strong>
            </div>
            ${this.rubricAssociation.parameters.fineTunePoints ? 
              html`
                <input type="number" step="0.01" min="0" max="${ifDefined(c.pointrange ? c.pointrange.high : undefined)}"
                    title="${tr("point_override_details")}"
                    data-criterion-id="${c.id}"
                    name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-override-${c.id}"
                    class="fine-tune-points form-control hide-input-arrows"
                    @input=${this.fineTuneRating}
                    .value="${c.pointoverride}"
                />
              ` : ""
            }
            <input aria-labelledby="${tr("points")}" type="hidden" id="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" .value="${c.selectedvalue}">
            <input type="hidden" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterionrating-${c.id}" .value="${c.selectedRatingId}">
          </div>
        </div>
      `)}
      </div>
      <div class="rubric-totals" style="margin: 10px 0px 10px 0px;">
        <input type="hidden" aria-labelledby="${tr("total")}" id="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" name="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" .value="${this.totalPoints}">
        <div class="total-points">
          <sr-lang key="total">Total</sr-lang>: <strong id="sakai-rubrics-total-points">${this.totalPoints.toLocaleString(this.locale)}</strong>
        </div>
      </div>

      <input aria-labelledby="${tr("rubric")}" name="rbcs-${this.evaluatedItemId}-${this.entityId}-state-details" id="rbcs-${this.evaluatedItemId}-${this.entityId}-state-details" type="hidden" .value="${this.stateDetailsJson}">
    `;
  }

  updateComment(e) {

    this.criteria.forEach(c => {

      if (c.id === e.detail.criterionId) {
        c.comments = e.detail.value;
      }
    });

    // Dispatch an event for each rating. We have to do this to give tools like
    // Samigo a chance to build their form inputs properly.
    this._dispatchRatingChanged(this.criteria);

    this.updateStateDetails();
  }

  updateStateDetails() {

    const stateDetails =
      this.criteria.map(c => {
        return {
          cid: c.id,
          pval: c.selectedvalue || "",
          rid: c.selectedRatingId || "",
          povrd: c.pointoverride || "",
          comments: c.comments || ""
        };
      });

    // This will trigger a render to the input element
    this.stateDetailsJson = escape(JSON.stringify(stateDetails));

    var detail = { evaluatedItemId: this.evaluatedItemId, entityId: this.entityId, value: this.stateDetailsJson };
    this.dispatchEvent(new CustomEvent("update-state-details", { detail: detail, bubbles: true, composed: true }));
  }

  setupCriteriaFromState() {

    this.stateDetails.forEach(sd => {

      this.criteria.forEach(c => {

        if (sd.cid === c.id && (sd.povrd || sd.pval)) {
          c.selectedvalue = sd.pval;
          c.selectedRatingId = sd.rid;
          c.pointoverride = sd.povrd;
          c.comments = sd.comments;
        }
      });
    });

    // We have to do this to wait for any event handlers to be attached during the page render
    setTimeout(() => {
      this.criteria.filter(c => c.selectedRatingId).forEach(c => {

        let points = c.selectedvalue;
        let detail = { evaluatedItemId: this.evaluatedItemId, entityId: this.entityId, criterionId: c.id, value: parseFloat(points), ratingId: c.selectedRatingId };
        this.dispatchEvent(new CustomEvent("rubric-rating-changed", {detail: detail, bubbles: true, composed: true}));
      });
      this.updateStateDetails();
    }, 10);

    this.selectedRatings = this.criteria.filter(c => c.selectedRatingId).map(c => c.selectedRatingId);
    this.calculateTotalPointsFromCriteria();
  }

  calculateTotalPointsFromCriteria() {

    this.totalPoints = this.criteria.reduce((a, c) => {

      if (c.pointoverride) {
        return a + parseFloat(c.pointoverride);
      } else if (c.selectedvalue) {
        return a + parseFloat(c.selectedvalue);
      } else {
        return a;
      }
    }, 0);
  }

  decorateCriteria() {

    this.evaluationDetails.forEach(ed => {

      this.criteria.forEach(c => {

        if (ed.criterionId === c.id) {

          c.selectedRatingId = ed.selectedRatingId;
          if (ed.pointsAdjusted) {
            c.pointoverride = ed.points;
            let ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];
            if (ratingItem) {
              c.selectedvalue = ratingItem.points;
            }
          } else {
            c.pointoverride = "";
            c.selectedvalue = ed.points;
          }

          c.comments = ed.comments;
        }
      });
    });

    this.updateTotalPoints();
  }

  fineTuneRating(e) {

    var value = e.target.value;

    var criterion = this.criteria.find(c => c.id == e.target.dataset.criterionId);

    criterion.pointoverride = value;
    if (criterion.selectedvalue) {
      this.totalPoints = this.totalPoints - criterion.selectedvalue + parseFloat(criterion.pointoverride);
    } else {
      this.totalPoints = this.totalPoints + parseFloat(criterion.pointoverride);
    }
    
    this.dispatchEvent(new CustomEvent("rubric-ratings-changed", { bubbles: true, composed: true }));
    var detail = { evaluatedItemId: this.evaluatedItemId, entityId: this.entityId, criterionId: criterion.id, value: criterion.pointoverride };
    this.dispatchEvent(new CustomEvent("rubric-rating-tuned", { detail: detail, bubbles: true, composed: true }));

    // Dispatch an event for each rating. We have to do this to give tools like
    // Samigo a chance to build their form inputs properly.
    this._dispatchRatingChanged(this.criteria);

    this.updateTotalPoints();
  }

  _dispatchRatingChanged(criteria) {

    criteria.forEach(c => {

      let pointsElement = this.querySelector(`#criterion_row_${c.id} .criterion-ratings .selected .points`);
      if (pointsElement) {
        let points = pointsElement.dataset.points;
        let detail = { evaluatedItemId: this.evaluatedItemId, entityId: this.entityId, criterionId: c.id, value: parseFloat(points), ratingId: c.selectedRatingId };
        this.dispatchEvent(new CustomEvent("rubric-rating-changed", {detail: detail, bubbles: true, composed: true}));
      }
    });
  }

  getOverriddenClass(ovrdvl,selected) {

    if (!this.rubricAssociation.parameters.fineTunePoints) {
      return '';
    }

    if ((ovrdvl || ovrdvl === 0) && (parseFloat(ovrdvl) !== parseFloat(selected))) {
      return 'strike';
    } else {
      return '';
    }
  }

  toggleRating(e) {

    e.stopPropagation();

    var criterionId = e.currentTarget.dataset.criterionId;
    var ratingId = e.currentTarget.dataset.ratingId;

    // Look up the criterion and rating objects
    var criterion = this.criteria.filter(c => c.id == criterionId)[0];
    var rating = criterion.ratings.filter(r => r.id == ratingId)[0];

    var clickedRatingElement = this.querySelector(`#rating-item-${ratingId}`);
    if (clickedRatingElement.classList.contains("selected")) {
      clickedRatingElement.classList.remove("selected");
      criterion.selectedvalue = 0.0;
      criterion.selectedRatingId = "";
      criterion.pointoverride = "0";
    } else {
      var ratingElements = this.querySelectorAll(`#criterion_row_${criterion.id} .rating-item`);
      ratingElements.forEach(i => i.classList.remove('selected'));
      clickedRatingElement.classList.add("selected");
      criterion.selectedvalue = rating.points;
      criterion.selectedRatingId = rating.id;
      criterion.pointoverride = rating.points.toString();
    }

    var selector
      = `#rbcs-${this.evaluatedItemId.replace(/./g, "\\.")}-${this.entityId.replace(/./g, "\\.")}-criterion-override-${criterionId}`;
    var overrideInput = this.querySelector(selector);
    if (overrideInput) overrideInput.value = rating.points;

    // Whenever a rating is clicked, either to select or deselect, it cancels out any override so we
    // remove the strike out from the clicked points value
    this.querySelector(`#points-display-${criterionId}`).classList.remove("strike");

    // Dispatch an event for each rating. We have to do this to give tools like
    // Samigo a chance to build their form inputs properly.
    this._dispatchRatingChanged(this.criteria);

    this.dispatchEvent(new CustomEvent("rubric-ratings-changed", { bubbles: true, composed: true }));
    this.updateTotalPoints();
  }

  commentShown(e) {
    this.querySelectorAll(`sakai-rubric-grading-comment:not(#${e.target.id})`).forEach(c => c.hide());
  }

  updateTotalPoints() {

    this.calculateTotalPointsFromCriteria();
    var detail = { evaluatedItemId: this.evaluatedItemId, entityId: this.entityId, value: this.totalPoints.toLocaleString(this.locale) };
    this.dispatchEvent(new CustomEvent('total-points-updated', {detail: detail, bubbles: true, composed: true}));

    this.updateStateDetails();
  }
}

customElements.define("sakai-rubric-criteria-grading", SakaiRubricCriteriaGrading);
