import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { ifDefined } from "/webcomponents/assets/lit-html/directives/if-defined.js";
import { SakaiRubricGradingComment } from "./sakai-rubric-grading-comment.js";
import { SakaiRubricsLanguage } from "./sakai-rubrics-language.js";
import { tr } from "./sakai-rubrics-language.js";

export class SakaiRubricGrading extends RubricsElement {

  constructor() {

    super();

    this.selectedRatings = [];
    this.existingEvaluation = false;

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

      // Non attribute
      evaluation: { type: Object },
      totalPoints: Number,
      translatedTotalPoints: { type: Number },
      selectedRatings: { type: Array },
      criteria: { type: Array },
      rubric: { type: Object }
    };
  }

  set token(newValue) {

    if (!newValue.startsWith("Bearer")) {
      this._token = "Bearer " + newValue;
    } else this._token = newValue;
  }

  get token() {
    return this._token;
  }

  attributeChangedCallback(name, oldVal, newVal) {

    super.attributeChangedCallback(name, oldVal, newVal);

    if (this.entityId && this.toolId && this.token) {
      this.getAssociation();
    }
  }

  render() {

    return html`
      <h3 style="margin-bottom: 10px;">${this.rubric.title}</h3>
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
                <div class="rating-item ${this.selectedRatings.includes(r.id) ? "selected" : ""}" tabindex="0" data-rating-id="${r.id}" id="rating-item-${r.id}" data-criterion-id="${c.id}" @keypress=${this.toggleRating} @click=${this.toggleRating}>
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
              <strong id="points-display-${c.id}" class="points-display ${this.getOverriddenClass(c.pointoverride, c.selectedvalue)}">
                ${c.selectedvalue}
              </strong>
            </div>
            ${this.association.parameters.fineTunePoints ? html`
                <input type="number" step="0.01" min="0" max="${ifDefined(c.pointrange ? c.pointrange.high : undefined)}"
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

    this.evaluation.criterionOutcomes.forEach(ed => {

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
            c.pointoverride = 0;
            c.selectedvalue = ed.points;
          }

          c.comments = ed.comments;
        }
      });
    });

    this.updateTotalPoints(false);
  }

  fineTuneRating(e) {

    var value = e.target.value;

    var criterion = this.criteria.find(c => c.id == e.target.dataset.criterionId);

    criterion.pointoverride = parseFloat(value);
    if (criterion.selectedvalue) {
      this.totalPoints = this.totalPoints - criterion.selectedvalue + criterion.pointoverride;
    } else {
      this.totalPoints = this.totalPoints + criterion.pointoverride;
    }

    this.dispatchEvent(new CustomEvent("rubric-ratings-changed", { bubbles: true, composed: true }));
    var detail = { evaluatedItemId: this.evaluatedItemId, entityId: this.entityId, criterionId: criterion.id, value: criterion.pointoverride };
    this.dispatchEvent(new CustomEvent("rubric-rating-tuned", { detail: detail, bubbles: true, composed: true }));

    this._dispatchRatingChanged(this.criteria);

    this.updateTotalPoints();
  }

  _dispatchRatingChanged(criteria) {

    let crit = criteria.map(c => {

      return {
        criterionId: c.id,
        points: c.pointoverride || c.selectedvalue,
        comments: c.comments,
        pointsAdjusted: c.pointoverride !== c.selectedvalue,
        selectedRatingId: c.selectedRatingId
      };
    });

    let evaluation = {
      evaluatorId: window.top.portal.user.id,
      evaluatedItemId: this.evaluatedItemId,
      evaluatedItemOwnerId: this.evaluatedItemOwnerId,
      overallComment: "",
      criterionOutcomes: crit,
      toolItemRubricAssociation: this.association._links.self.href
    };

    if (this.evaluation && this.evaluation.id) {
      evaluation.metadata = this.evaluation.metadata;
    }

    let url = "/rubrics-service/rest/evaluations";
    if (this.evaluation && this.evaluation.id) url += `/${this.evaluation.id}`;
    fetch(url, {
      body: JSON.stringify(evaluation),
      credentials: "same-origin",
      headers: {
        "Authorization": this.token,
        "Accept": "application/json",
        "Content-Type": "application/json"
      },
      method: this.evaluation && this.evaluation.id ? "PATCH" : "POST"
    }).then(r => r.json()).then(r => this.evaluation = r);
  }

  getOverriddenClass(ovrdvl, selected) {

    if (!this.association.parameters.fineTunePoints) {
      return '';
    }

    if ((ovrdvl || ovrdvl === 0) && parseFloat(ovrdvl) !== parseFloat(selected)) {
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
      criterion.selectedRatingId = 0;
      criterion.pointoverride = 0.0;
    } else {
      var ratingElements = this.querySelectorAll(`#criterion_row_${criterion.id} .rating-item`);
      ratingElements.forEach(i => i.classList.remove('selected'));
      clickedRatingElement.classList.add("selected");
      criterion.selectedvalue = rating.points;
      criterion.selectedRatingId = rating.id;
      criterion.pointoverride = rating.points;
    }

    var selector = `#rbcs-${this.evaluatedItemId.replace(/./g, "\\.")}-${this.entityId.replace(/./g, "\\.")}-criterion-override-${criterionId}`;
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

  updateTotalPoints(notify = true) {

    this.calculateTotalPointsFromCriteria();
    if (notify) {
      var detail = { evaluatedItemId: this.evaluatedItemId, entityId: this.entityId, value: this.totalPoints.toLocaleString(this.locale) };
      this.dispatchEvent(new CustomEvent('total-points-updated', { detail: detail, bubbles: true, composed: true }));
    }
  }

  getAssociation() {

    $.ajax({
      url: `/rubrics-service/rest/rubric-associations/search/by-tool-item-ids?toolId=${this.toolId}&itemId=${this.entityId}`,
      headers: { "authorization": this.token }
    }).done(data => {

      this.association = data._embedded['rubric-associations'][0];
      var rubricId = data._embedded['rubric-associations'][0].rubricId;
      this.getRubric(rubricId);
    }).fail((jqXHR, textStatus, errorThrown) => {
      console.log(textStatus);console.log(errorThrown);
    });
  }

  getRubric(rubricId) {

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${rubricId}?projection=inlineRubric`,
      headers: { "authorization": this.token }
    }).done(rubric => {

      $.ajax({
        url: `/rubrics-service/rest/evaluations/search/by-tool-item-and-associated-item-and-evaluated-item-ids?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`,
        headers: { "authorization": this.token }
      }).done(data => {

        this.evaluation = data._embedded.evaluations[0] || { criterionOutcomes: [] };
        this.selectedRatings = this.evaluation.criterionOutcomes.map(ed => ed.selectedRatingId);
        if (this.criteria) this.decorateCriteria();
        this.existingEvaluation = true;

        this.rubric = rubric;

        this.criteria = this.rubric.criterions;
        this.criteria.forEach(c => {

          if (!c.selectedvalue) {
            c.selectedvalue = 0;
          }
          c.pointrange = this.getHighLow(c.ratings, "points");
        });

        this.decorateCriteria();
      }).fail((jqXHR, textStatus, errorThrown) => {
        console.log(textStatus);console.log(errorThrown);
      });
    }).fail((jqXHR, textStatus, errorThrown) => {
      console.log(textStatus);console.log(errorThrown);
    });
  }
}

customElements.define("sakai-rubric-grading", SakaiRubricGrading);
