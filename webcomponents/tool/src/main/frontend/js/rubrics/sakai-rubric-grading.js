import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import "./sakai-rubric-grading-comment.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";

export class SakaiRubricGrading extends RubricsElement {

  constructor() {

    super();

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

    this.criteria.forEach(c => {

      if (c.id === e.detail.criterionId) {
        c.comments = e.detail.value;
      }
    });

    this._dispatchRatingChanged(this.criteria, 1);
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

  release() {
    this._dispatchRatingChanged(this.criteria, 2);
  }

  save() {
    this._dispatchRatingChanged(this.criteria, 1);
  }

  decorateCriteria() {

    this.evaluation.criterionOutcomes.forEach(ed => {

      this.criteria.forEach(c => {

        if (ed.criterionId === c.id) {

          c.selectedRatingId = ed.selectedRatingId;
          if (ed.pointsAdjusted) {
            c.pointoverride = ed.points;
            const ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];
            if (ratingItem) {
              c.selectedvalue = ratingItem.points;
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

    this.updateTotalPoints(false);
  }

  fineTuneRating(e) {

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
    this._dispatchRatingChanged(this.criteria, 1);
  }

  _dispatchRatingChanged(criteria, status) {

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
      evaluatorId: window.top.portal.user.id,
      evaluatedItemId: this.evaluatedItemId,
      evaluatedItemOwnerId: this.evaluatedItemOwnerId,
      overallComment: "",
      criterionOutcomes: crit,
      toolItemRubricAssociation: this.association._links.self.href,
      status: status
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

    const criterionId = parseInt(e.currentTarget.dataset.criterionId);
    const ratingId = parseInt(e.currentTarget.dataset.ratingId);

    // Look up the criterion and rating objects
    const criterion = this.criteria.filter(c => c.id == criterionId)[0];
    const rating = criterion.ratings.filter(r => r.id === ratingId)[0];

    criterion.ratings.forEach(r => r.selected = false);

    if (rating.selected) {
      criterion.selectedvalue = 0.0;
      criterion.selectedRatingId = 0;
      criterion.pointoverride = 0.0;
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

    this._dispatchRatingChanged(this.criteria, 1);
  }

  commentShown(e) {
    this.querySelectorAll(`sakai-rubric-grading-comment:not(#${e.target.id})`).forEach(c => c.hide());
  }

  updateTotalPoints(notify = true) {

    this.calculateTotalPointsFromCriteria();

    // Make sure total points is not negative
    if (parseFloat(this.totalPoints) < 0) this.totalPoints = 0;

    if (notify) {
      const detail = {
        evaluatedItemId: this.evaluatedItemId,
        entityId: this.entityId,
        value: this.totalPoints.toLocaleString(this.locale, { maximumFractionDigits: 2 }),
      };
      this.dispatchEvent(new CustomEvent('total-points-updated', { detail: detail, bubbles: true, composed: true }));
    }
  }

  getAssociation() {

    if (!this.toolId || !this.entityId || !this.token || !this.evaluatedItemId) {
      return;
    }

    $.ajax({
      url: `/rubrics-service/rest/rubric-associations/search/by-tool-and-assignment?toolId=${this.toolId}&itemId=${this.entityId}`,
      headers: { "authorization": this.token }
    }).done(data => {

      this.association = data._embedded['rubric-associations'][0];
      this.rubricId = data._embedded['rubric-associations'][0].rubricId;
      this.getRubric(this.rubricId);
    }).fail((jqXHR, textStatus, errorThrown) => {

      console.info(textStatus);
      console.error(errorThrown);
    });
  }

  getRubric(rubricId) {

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${rubricId}?projection=inlineRubric`,
      headers: { "authorization": this.token }
    }).done(rubric => {

      $.ajax({
        url: `/rubrics-service/rest/evaluations/search/by-tool-and-assignment-and-submission?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`,
        headers: { "authorization": this.token }
      }).done(data => {

        this.evaluation = data._embedded.evaluations[0] || { criterionOutcomes: [] };
        this.existingEvaluation = true;

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
      }).fail((jqXHR, textStatus, errorThrown) => {

        console.info(textStatus);
        console.error(errorThrown);
      });
    }).fail((jqXHR, textStatus, errorThrown) => {

      console.info(textStatus);
      console.error(errorThrown);
    });
  }
}

if (!customElements.get("sakai-rubric-grading")) {
  customElements.define("sakai-rubric-grading", SakaiRubricGrading);
}
