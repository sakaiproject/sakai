import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {repeat} from "/webcomponents/assets/lit-html/directives/repeat.js";
import {SakaiRubricStudentComment} from "./sakai-rubric-student-comment.js";

export class SakaiRubricCriterionStudent extends SakaiElement {

  static get properties() {

    return {
      criteria: { type: Array},
      totalPoints: { type: Number },
      rubricAssociation: { attribute: "rubric-association", type: Object },
      evaluationDetails: { attribute: "evaluation-details", type: Array },
      preview: { type: Boolean },
      entityId: { attribute: "entity-id", type: String },
    };
  }

  set criteria(newVal) {

    var oldVal = this._criteria;
    this._criteria = newVal;
    this.criteria.forEach(c => {

      if (!c.selectedvalue) {
        c.selectedvalue = 0;
      }
      c.pointrange = rubrics.getHighLow(c.ratings, "points");
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
  
  shouldUpdate(changedProperties) {
    return (this.preview && this.criteria) || this.ready;
  }

  render() {

    return html`
      <div class="criterion grading style-scope sakai-rubric-criterion-student" style="margin-bottom: 20px;">
        ${this.criteria.map(c => html`
          <div id="criterion_row_${c.id}" class="criterion-row">
            <div class="criterion-detail">
              <h4 class="criterion-title">${c.title}</h4>
              <p>${c.description}</p>
            </div>
            <div class="criterion-ratings">
              <div class="cr-table">
                <div class="cr-table-row">
                ${repeat(c.ratings, r => r.id, r => html`
                  <div class="rating-item student ${r.selected ? "selected" : ""}" id="rating-item-${r.id}">
                    <h5 class="criterion-item-title">${r.title}</h5>
                    <p>${r.description}</p>
                    <span class="points">${r.points} Points</span>
                  </div>
                `)}
                </div>
              </div>
            </div>
            <div class="criterion-actions">
            ${!this.preview ? html`
              <sakai-rubric-student-comment criterion="${JSON.stringify(c)}"></sakai-rubric-student-comment>
              <strong class="points-display ${this.getOverriddenClass(c.pointoverride,c.selectedvalue)}">
                &nbsp;
                ${c.selectedvalue}
                ${!c.selectedRadingId ? "0" : ""}
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
        <div class="total-points">Total: <strong>${this.totalPoints}</strong></div>
      </div>
      ` : html``}
    `;
  }

  handleEvaluationDetails() {

    this.evaluationDetails.forEach(ed => {

      this.criteria.forEach(c => {

        if (ed.criterionId === c.id) {

          var ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];
          ratingItem.selected = true;

          c.selectedRadingId = ed.selectedRatingId;
          if (ed.pointsAdjusted) {
            c.pointoverride = ed.points;
            c.selectedvalue = ratingItem.points;
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

  isOverridden(ovrdvl, selected) {

    if (!this.rubricAssociation.parameters.fineTunePoints) {
      return false;
    }

    if ((ovrdvl || ovrdvl === 0) && (parseInt(ovrdvl) !== parseInt(selected))) {
      return true;
    } else {
      return false;
    }
  }

  updateTotalPoints() {

    this.totalPoints = this.criteria.reduce((a, c) => {

      if (c.pointoverride) {
        return a + parseInt(c.pointoverride);
      } else if (c.selectedvalue) {
        return a + parseInt(c.selectedvalue);
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

    if ((ovrdvl || ovrdvl === 0) && (parseInt(ovrdvl) !== parseInt(selected))) {
      return 'strike';
    } else {
      return '';
    }
  }
}

customElements.define("sakai-rubric-criterion-student", SakaiRubricCriterionStudent);
