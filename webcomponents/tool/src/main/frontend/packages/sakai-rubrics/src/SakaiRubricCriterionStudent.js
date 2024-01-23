import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { repeat } from "lit/directives/repeat.js";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "../sakai-rubric-student-comment.js";

export class SakaiRubricCriterionStudent extends RubricsElement {

  static properties = {

    criteria: { type: Array },
    totalPoints: Number,
    association: { type: Object },
    outcomes: { type: Array },
    preview: Boolean,
    entityId: { attribute: "entity-id", type: String },
    weighted: Boolean,
  };

  set criteria(newValue) {

    const oldValue = this._criteria;

    this._criteria = newValue;
    this.criteria.forEach(c => {

      if (!c.selectedvalue) {
        c.selectedvalue = 0;
      }
      c.pointrange = this.getHighLow(c.ratings);
    });

    if (this.outcomes) {
      this.handleOutcomes();
    }

    this.requestUpdate("criteria", oldValue);
  }

  get criteria() { return this._criteria; }

  get dynamic () { return this.association?.parameters["rbcs-associate"] == 2 ?? false; }

  set outcomes(newValue) {

    const oldValue = this._outcomes;

    this._outcomes = newValue;

    if (this.criteria) {
      this.handleOutcomes();
    }

    this.requestUpdate("outcomes", oldValue);
  }

  get outcomes() { return this._outcomes; }

  handleClose() {
    this.querySelectorAll("sakai-rubric-student-comment").forEach(el => el.handleClose());
  }

  shouldUpdate() {
    return this.association && this.criteria && super.shouldUpdate();
  }

  render() {

    return html`
      <div class="criterion grading style-scope sakai-rubric-criterion-student" style="margin-bottom: 20px;">
        ${this.criteria.map(c => html`
          ${this.isCriterionGroup(c) ? html`
            <div id="criterion_row_${c.id}" class="criterion-row criterion-group">
              <div class="criterion-detail">
                <h4 class="criterion-title">${c.title}</h4>
                <p>${unsafeHTML(c.description)}</p>
              </div>
            </div>
          ` : html`
            <div id="criterion_row_${c.id}" class="criterion-row">
              <div class="criterion-detail">
                <h4 class="criterion-title">${c.title}</h4>
                <p>${unsafeHTML(c.description)}</p>
              </div>
              ${this.weighted ? html`
                <div class="criterion-weight">
                  <span>${this._i18n.weight}</span>
                  <span>${c.weight.toLocaleString(this.locale)}</span>
                  <span>${this._i18n.percent_sign}</span>
                </div>
              ` : nothing }
              <div class="criterion-ratings">
                <div class="cr-table">
                  <div class="cr-table-row">
                  ${repeat(c.ratings, r => r.id, r => html`
                    <div class="rating-item student ${r.selected ? "selected" : ""}" id="rating-item-${r.id}">
                      ${!this.dynamic ? html`
                        <h5 class="criterion-item-title">${r.title}</h5>
                      ` : nothing }
                      <p>${r.description}</p>
                      <span class="points">
                        ${this.weighted && r.points > 0 ? html`
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
              ${!this.preview ? html`
                ${!this.dynamic ? html`
                  <sakai-rubric-student-comment .criterion=${c}></sakai-rubric-student-comment>
                ` : nothing }
                <strong class="points-display ${this.getOverriddenClass(c.pointoverride, c.selectedvalue)}">
                  ${c.selectedRatingId ? c.selectedvalue.toLocaleString(this.locale) : "0"}
                </strong>
                ${this.isOverridden(c.pointoverride, c.selectedvalue) ?
                  html`<strong class="points-display">${c.pointoverride.toLocaleString(this.locale)}</strong>`
                  : nothing }
              ` : nothing }
              </div>
            </div>
          `}
        `)}
      </div>
      ${!this.preview && !this.dynamic ? html`
      <div class="rubric-totals" style="margin-bottom: 5px;">
        <div class="total-points"><span>${this._i18n.total}</span>: <strong>${this.totalPoints.toLocaleString(this.locale, { maximumFractionDigits:2 })}</strong></div>
      </div>
      ` : nothing }
    `;
  }

  handleOutcomes() {

    this.outcomes.forEach(outcome => {

      this.criteria.forEach(c => {

        if (outcome.criterionId === c.id) {

          let selectedRatingItem = null;
          c.ratings.forEach(r => {
            if (r.id == outcome.selectedRatingId) {
              r.selected = true;
              selectedRatingItem = r;
            } else {
              r.selected = false;
            }
          });

          c.selectedRatingId = outcome.selectedRatingId;
          if (outcome.pointsAdjusted) {
            c.pointoverride = outcome.points;
            c.selectedvalue = selectedRatingItem != null ? selectedRatingItem.points : 0; // Set selected value (points) to zero if no rating was selected
          } else {
            c.pointoverride = "";
            c.selectedvalue = outcome.points;
          }

          c.comments = outcome.comments;
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

    if (!this.association.parameters.fineTunePoints) {
      return false;
    }

    if ((pointoverride || pointoverride === 0) && (parseFloat(pointoverride) !== parseFloat(selected))) {
      return true;
    }
    return false;

  }

  updateTotalPoints() {

    this.totalPoints = this.criteria.reduce((a, c) => {

      if (c.pointoverride) {
        return a + parseFloat(c.pointoverride);
      } else if (c.selectedvalue) {
        return a + parseFloat(c.selectedvalue);
      }
      return a;

    }, 0);

    this.ready = true;
  }

  getOverriddenClass(ovrdvl, selected) {

    if (!this.association.parameters.fineTunePoints) {
      return "";
    }

    if ((ovrdvl || ovrdvl === 0) && (parseFloat(ovrdvl) !== parseFloat(selected))) {
      return "strike";
    }
    return "";
  }
}
