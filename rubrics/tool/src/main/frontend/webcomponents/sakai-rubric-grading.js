import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricCriteriaGrading} from "./sakai-rubric-criteria-grading.js";
import {SakaiRubricGradingComment} from "./sakai-rubric-grading-comment.js";
import {SakaiRubricsLanguage} from "./sakai-rubrics-language.js";
import {loadProperties, tr} from "/webcomponents/sakai-i18n.js";

export class SakaiRubricGrading extends SakaiElement {

  constructor() {

    super();

    loadProperties({namespace: "rubrics"});
  }

  set token(newValue) {
    this._token = "Bearer " + newValue;
  }

  get token() { return this._token; }

  static get properties() {

    return {
      token: String,
      entityId: {type: String},
      toolId: {type: String},
      gradeFieldId: String,
      stateDetails: String,
      evaluatedItemId: String,
      rubric: {type: Object},
      evaluation: {type: Object},
    };
  }

  attributeChangedCallback(name, oldVal, newVal) {

    super.attributeChangedCallback(name, oldVal, newVal);

    if (this.entityId && this.toolId && this.token) {
      this.getAssociation();
    }
  }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubric");
  }

  render() {

    return html`
      <h3>${this.rubric.title}</h3>

      <sakai-rubric-criteria-grading
        criteria="${JSON.stringify(this.rubric.criterions)}"
        gradeFieldId="${this.gradeFieldId}"
        rubricAssociation="${JSON.stringify(this.association)}"
        stateDetails="${this.stateDetails}"
        entityId="${this.entityId}"
        evaluatedItemId="${this.evaluatedItemId}"
        evaluationDetails="${JSON.stringify(this.evaluation.criterionOutcomes)}"
        ></sakai-rubric-criteria-grading>
    `;
  }

  getAssociation() {

    $.ajax({
      url: `/rubrics-service/rest/rubric-associations/search/by-tool-item-ids?toolId=${this.toolId}&itemId=${this.entityId}`,
      headers: {"authorization": this.token}
    })
    .done(data => {

      this.association = data._embedded['rubric-associations'][0];
      var rubricId = data._embedded['rubric-associations'][0].rubricId;
      this.getRubric(rubricId);
    })
    .fail((jqXHR, textStatus, errorThrown) => { console.log(textStatus); console.log(errorThrown); });
  }

  getRubric(rubricId) {

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${rubricId}?projection=inlineRubric`,
      headers: {"authorization": this.token},
    })
    .done(rubric => {

      $.ajax({
        url: `/rubrics-service/rest/evaluations/search/by-tool-item-and-associated-item-and-evaluated-item-ids?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`,
        headers: {"authorization": this.token}
      })
      .done(data => {

        this.evaluation = data._embedded.evaluations[0] || {criterionOutcomes: []};
        this.rubric = rubric;
      })
      .fail((jqXHR, textStatus, errorThrown) => { console.log(textStatus); console.log(errorThrown); });
    })
    .fail((jqXHR, textStatus, errorThrown) => { console.log(textStatus); console.log(errorThrown); });
  }
}

customElements.define("sakai-rubric-grading", SakaiRubricGrading);
