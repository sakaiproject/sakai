import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricCriterionGrading extends SakaiElement {

  static get properties() {

    return {
      criteria: { type: Array},
      gradeFieldId: { type: Number},
      rubricAssociation: { type: Object },
      stateDetailsJson: { type: String},
      entityId: { type: String},
      evaluatedItemId: { type: String },
      totalPoints: { type: Number },
      evaluationDetails: { type: Array },
      selectedRatings: { type: Array },
    };
  }

  set evaluationDetails(newVal) {

    var oldVal = this._evaluationDetails;
    this.selectedRatings = newVal.map(ed => ed.selectedRatingId);
    this._evaluationDetails = newVal;
    if (this.criteria) this.decorateCriteria();
    this.requestUpdate('evaluationDetails', oldVal);
  }

  get evaluationDetails() { return this._evaluationDetails; }

  set criteria(newVal) {

    var oldVal = this._criteria;
    this._criteria = newVal;
    this.criteria.forEach(c => {

      if (!c.selectedvalue) {
        c.selectedvalue = 0;
      }
      c.pointrange = rubrics.getHighLow(c.ratings, "points");
    });

    if (this.evaluationDetails) this.decorateCriteria();
    this.requestUpdate('criteria', oldVal);
  }

  get criteria() { return this._criteria; }

  render() {

    return html`
      <div class="criterion grading style-scope sakai-rubric-criterion-grading">
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
                <div class="rating-item ${this.selectedRatings.includes(r.id) ? "selected" : ""}" data-rating-id="${r.id}" id="rating-item-${r.id}" data-criterion-id="${c.id}" @click="${this.toggleRating}">
                  <h5 class="criterion-item-title">${r.title}</h5>
                  <p>${r.description}</p>
                  <span class="points">${r.points} <sr-lang key="points">Points</sr-lang></span>
                </div>
              `)}
              </div>
            </div>
          </div>
          <div class="criterion-actions">
            <sakai-rubric-grading-comment @update-comment="${this.updateComment}" criterion="${JSON.stringify(c)}" evaluatedItemId="${this.evaluatedItemId}" entityId="${this.entityId}"></sakai-rubric-grading-comment>
            <div>
              <strong class="points-display ${this.getOverriddenClass(c.pointoverride,c.selectedvalue)}">
                ${c.selectedvalue}
              </strong>
            </div>
            ${this.rubricAssociation.parameters.fineTunePoints ?
              html`<input type="number" min="0" max="${c.pointrange.high}"
                      @keypress="${this.allowOnlyNumbersAndTab(event)}"
                      @input="${this.restrictValuesRange(event,this)}"
                      title="${rubrics.i18n['point_override_details']}"
                      class="fine-tune-points form-control hide-input-arrows"
                      id="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-override-${c.id}"
                      name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-override-${c.id}"
                      @input="${this.finetuneRating}" value="${c.pointoverride}">`
              : html``
            }
            <input aria-labelledby="${rubrics.i18n['points']}" type="hidden" id="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-${c.id}" value="${c.selectedvalue}">
          </div>
        </div>
      `)}
      </div>
      <div class="rubric-totals">
        <input type="hidden" aria-labelledby="${rubrics.i18n['total']}" id="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" name="rbcs-${this.evaluatedItemId}-${this.entityId}-totalpoints" value="${this.totalPoints}">
        <div class="total-points">
          <sr-lang key="total">Total</sr-lang>: <strong id="sakai-rubrics-total-points">${this.totalPoints}</strong>
        </div>
      </div>

      <input aria-labelledby="${rubrics.i18n['rubric']}" name="rbcs-${this.evaluatedItemId}-${this.entityId}-state-details" id="rbcs-${this.evaluatedItemId}-${this.entityId}-state-details" type="hidden" value="${this.stateDetailsJson}">
    `;
  }

  allowOnlyNumbersAndTab(e) {

    if (!(( e.charCode >= 48 && e.charCode <= 57 ) || e.charCode === 9 )){
      e.preventDefault();
      return false;
    }
  }

  restrictValuesRange(e, element) {

    var max = parseInt(element.getAttribute("max"));
    var value = parseInt(element.value);

    if ( value > max){
      element.value = max;
      e.preventDefault();
      return false;
    }
    return true;
  }

  updateComment(e) {

    this.criteria.forEach(c => {

      if (c.id === e.detail.criterionId) {
        c.comments = e.detail.comments;
      }
    });
    this.updateStateDetails();
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
    this.stateDetailsJson = escape(JSON.stringify(this.stateDetails));
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
    });

    this.updateTotalPoints();
  }

  decorateCriteria() {

    if (this.evaluationDetails && this.criteria) {

      this.evaluationDetails.forEach(ed => {

        this.criteria.forEach(c => {

          if (ed.criterionId === c.id) {

            c.selectedRatingId = ed;
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
    }
  }

  finetuneRating(e) {

    var cindex = this.$.criterion.indexForElement(e.target);
    var citem = this.$.criterion.itemForElement(e.target);

    this.criteria[cindex].pointoverride = e.target.value;
    if (citem.selectedvalue) {
      this.totalPoints = this.totalPoints - citem.selectedvalue + parseInt(citem.pointoverride);
    } else {
      this.totalPoints = this.totalPoints + parseInt(citem.pointoverride);
    }

    this.rubricsEvent({
      event: 'rubric-ratings-changed'
    });

    this.updateTotalPoints();
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

  toggleRating(e) {

    e.stopPropagation();

    var criterionId = e.currentTarget.dataset.criterionId;
    var ratingId = e.currentTarget.dataset.ratingId;

    // Look up the criterion and rating objects
    var criterion = this.criteria.filter(c => c.id == criterionId)[0];
    var rating = criterion.ratings.filter(r => r.id == ratingId)[0];

    criterion.selectedvalue = rating.points;
    criterion.selectedRatingId = rating.id;
    //criterion.pointoverride = rating.points.toString();

    var criterionRow = document.getElementById(`criterion_row_${criterion.id}`);
    var ratingElements = criterionRow.querySelectorAll('.rating-item');
    var clickedRatingElement = document.getElementById(`rating-item-${ratingId}`);
    if (clickedRatingElement.classList.contains("selected")) {
      clickedRatingElement.classList.remove("selected");
      criterion.selectedvalue = 0;
      criterion.selectedRatingId = "";
    } else {
      ratingElements.forEach(i => i.classList.remove('selected'));
      clickedRatingElement.classList.add("selected");
      criterion.selectedvalue = rating.points;
      criterion.selectedRatingId = rating.id;
    }

    this.dispatchEvent(new CustomEvent("rubric-ratings-changed"));
    this.updateTotalPoints();
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

    setTimeout(function() {

      var field = document.getElementById(this.gradeFieldId);
      if (this.gradeFieldId && field) {
        field.value = this.totalPoints;
      }
    }.bind(this));

    this.dispatchEvent(new CustomEvent('total-points-updated', {detail: this.totalPoints, bubbles: true, composed: true}));

    this.updateStateDetails();
  }
}

customElements.define("sakai-rubric-criterion-grading", SakaiRubricCriterionGrading);
