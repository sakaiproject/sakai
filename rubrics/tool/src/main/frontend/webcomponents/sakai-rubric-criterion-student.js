import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricStudentComment} from "./sakai-rubric-student-comment.js";

export class SakaiRubricCriterionStudent extends SakaiElement {

  static get properties() {

    return {
      criteria: { type: Array},
      totalPoints: { type: Number },
      rubricAssociation: { attribute: "rubric-association", type: Object },
      stateDetails: { attribute: "state-details", type: String },
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

    this.requestUpdate('criteria', oldVal);
    this.updateComplete.then(() => this.handleEvaluationDetails());
  }

  get criteria() { return this._criteria; }

  render() {

    this.preview = false;

    return html`
      <div class="criterion grading style-scope sakai-rubric-criterion-student">
        ${this.criteria.map(c => html`
          <div id="criterion_row_${c.id}" class="criterion-row">
            <div class="criterion-detail">
              <h4 class="criterion-title">${c.title}</h4>
              <p>${c.description}</p>
            </div>
            <div class="criterion-ratings">
              <div class="cr-table">
                <div class="cr-table-row">
                ${c.ratings.map(r => html`
                  <div class="rating-item student" id="rating_item_${r.id}">
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
                ${!c.selectedRadingId ? html`0` : ``}
                &nbsp;
              </strong>
              ${this.showOverridden(c.pointoverride,c.selectedvalue) ?
                html`<strong class="points-display">${c.pointoverride}</strong>`
                : html``}
            ` : html``}
            </div>
          </div>
        `)}
      </div>
      ${!this.preview ? html`
      <div class="rubric-totals">
        <div class="total-points">Total: <strong>${this.totalPoints}</strong></div>
      </div>
      ` : html``}
      <input name="rbcs-${this.entityId}-state-details" id="rbcs-${this.entityId}-state-details" type="hidden" value="${this.stateDetails}">
    `;
  }

  updateStateDetails() {

    this.stateDetails =
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
    //this.stateDetailsJson = escape(JSON.stringify(this.stateDetails));
  }

  handleStateDetails() {

    this.stateDetails.forEach(sd => {

      this.criteria.forEach(c => {

        if (sd.cid === c.id && (sd.povrd || sd.pval)) {
          c.selectedvalue = sd.pval;
          c.selectedRatingId = sd.rid;
          c.pointoverride = sd.povrd;
          c.comments = sd.comments;
        }
      });

      if (sd.rid) {
        document.getElementById(`rating-item-${sd.rid}`).classList.addClass("selected");
      }
    });

    this.updateTotalPoints();
  }

  handleEvaluationDetails() {

    this.evaluationDetails.forEach(ed => {

      this.criteria.forEach(c => {

        if (ed.criterionId === c.id) {

          var ratingItem = c.ratings.filter(r => r.id == ed.selectedRatingId)[0];

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
      document.getElementById(`rating_item_${ed.selectedRatingId}`).classList.add("selected");
    });

    this.updateTotalPoints();
  }

  isOverridden(ovrdvl,selected) {

    if (!this.rubricAssociation.parameters.fineTunePoints) {
      return '';
    }

    if ((ovrdvl || ovrdvl === 0) && (parseInt(ovrdvl) !== parseInt(selected))) {
      return 'strike';
    } else {
      return '';
    }
  }

  showOverridden(ovrdvl,selected) {
    return (this.isOverridden(ovrdvl,selected) === 'strike');
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

    this.dispatchEvent(new CustomEvent("state-details"));
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

  /*
  rubricReady() {

    this.options = this.rubricAssociation.parameters;
    this.criterions = this.rubric.criterions;
    setTimeout(function () {

      if (this.stateDetails) {
        this.handleStateDetails();
      } else if (this.evaluationDetails) {
        this.handleEvaluationDetails();
      } else {
        this.dispatchEvent(new CustomEvent("state-details"));
      }
    }.bind(this))
  }
  */
}

customElements.define("sakai-rubric-criterion-student", SakaiRubricCriterionStudent);
