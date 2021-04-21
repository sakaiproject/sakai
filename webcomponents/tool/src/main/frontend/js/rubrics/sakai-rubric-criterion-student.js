import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import "./sakai-rubric-student-comment.js";

export class SakaiRubricCriterionStudent extends RubricsElement {

  static get properties() {

    return {
      criteria: { type: Array},
      totalPoints: Number,
      rubricAssociation: { attribute: "rubric-association", type: Object },
      evaluationDetails: { attribute: "evaluation-details", type: Array },
      preview: Boolean,
      entityId: { attribute: "entity-id", type: String },
      weighted: Boolean
    };
  }

  set criteria(newVal) {

    this._criteria = newVal;
    this.criteria.forEach(c => {

      if (!c.selectedvalue) {
        c.selectedvalue = 0;
      }
      c.pointrange = this.getHighLow(c.ratings);
    });

    if (this.evaluationDetails) {
      this.handleEvaluationDetails();
    }
  }

  get criteria() { return this._criteria; }

  set evaluationDetails(newValue) {

    this._evaluationDetails = newValue;

    if (this.criteria) {
      this.handleEvaluationDetails();
    }
  }

  get evaluationDetails() { return this._evaluationDetails; }

  render() {

    return html`
      <div class="criterion grading style-scope sakai-rubric-criterion-student" style="margin-bottom: 20px;">
        ${this.criteria.map(c => html`
          <div id="criterion_row_${c.id}" class="criterion-row">
            <div class="criterion-detail">
              <h4 class="criterion-title">${c.title}</h4>
              <p>${c.description}</p>
              ${this.weighted ? html`
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
            <div class="criterion-ratings">
              <div class="cr-table">
                <div class="cr-table-row">
                ${repeat(c.ratings, r => r.id, r => html`
                  <div class="rating-item student ${r.selected ? "selected" : ""}" id="rating-item-${r.id}">
                    <h5 class="criterion-item-title">${r.title}</h5>
                    <p>${r.description}</p>
                    <span class="points">
                      ${this.weighted && r.points > 0 ? html`
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
            ${!this.preview ? html`
              <sakai-rubric-student-comment criterion="${JSON.stringify(c)}"></sakai-rubric-student-comment>
              <strong class="points-display ${this.getOverriddenClass(c.pointoverride,c.selectedvalue)}">
                ${c.selectedvalue}
                ${!c.selectedRatingId ? "0" : ""}
                &nbsp;
              </strong>
              ${this.isOverridden(c.pointoverride,c.selectedvalue) ?
                html`<strong class="points-display">${c.pointoverride}</strong>`
                : html``}
            ` : html``}
            </div>
          </div>
        `)}
      </div>
      ${!this.preview ? html`
      <div class="rubric-totals" style="margin-bottom: 5px;">
        <div class="total-points"><sr-lang key="total">Total</sr-lang>: <strong>${this.totalPoints.toLocaleString(this.locale, {maximumFractionDigits:2})}</strong></div>
      </div>
      ` : html``}
    `;
  }

  handleEvaluationDetails() {

    this.evaluationDetails.forEach(ed => {

      this.criteria.forEach(c => {

        if (ed.criterionId === c.id) {

          let selectedRatingItem = null;
          c.ratings.forEach(r => {
            if (r.id == ed.selectedRatingId) {
              r.selected = true;
              selectedRatingItem = r;
            } else {
              r.selected = false;
            }
          });

          c.selectedRatingId = ed.selectedRatingId;
          if (ed.pointsAdjusted) {
            c.pointoverride = ed.points;
            c.selectedvalue = selectedRatingItem != null ? selectedRatingItem.points : 0; // Set selected value (points) to zero if no rating was selected
          } else {
            c.pointoverride = "";
            c.selectedvalue = ed.points;
          }

          c.comments = ed.comments;
          if (c.comments === "undefined") {
            // This can happen if undefined gets passed to the server when the evaluation is saved.
            c.comments = "";
          }
        }
      });
    });

    this.updateTotalPoints();
  }

  isOverridden(pointoverride, selected) {

    if (!this.rubricAssociation.parameters.fineTunePoints) {
      return false;
    }

    if ((pointoverride || pointoverride === 0) && (parseFloat(pointoverride) !== parseFloat(selected))) {
      return true;
    } else {
      return false;
    }
  }

  updateTotalPoints() {

    this.totalPoints = this.criteria.reduce((a, c) => {

      if (c.pointoverride) {
        return a + parseFloat(c.pointoverride);
      } else if (c.selectedvalue) {
        return a + parseFloat(c.selectedvalue);
      } else {
        return a;
      }
    }, 0);

    this.ready = true;
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
}

customElements.define("sakai-rubric-criterion-student", SakaiRubricCriterionStudent);
