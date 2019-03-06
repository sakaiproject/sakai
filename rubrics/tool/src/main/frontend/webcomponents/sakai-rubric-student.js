import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricCriterionStudent} from "./sakai-rubric-criterion-student.js";
import {SakaiRubricStudentComment} from "./sakai-rubric-student-comment.js";

class SakaiRubricStudent extends SakaiElement {

  constructor() {

    super();

    this.options = {};
  }

  static get properties() {

    return {
      token: { type: String},
      entityId: { type: String},
      toolId: { type: String},
      gradeFieldId: { type: String},
      stateDetails: { type: String},
      preview: { type: Boolean},
      instructor: { type: Boolean},
      evaluatedItemId: { type: String},
      rubric: { type: Object },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.token && this.toolId && this.entityId) {
      this.init();
    }
  }

  set token(newValue) {
    this._token = "Bearer " + newValue;
  }

  get token() { return this._token; }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubric") && (this.instructor || !this.options.hideStudentPreview);
  }

  render() {

    return html`
      <hr class="itemSeparator" />

      <h3>${this.rubric.title}</h3>

      <sakai-rubric-criterion-student
        criteria="${JSON.stringify(this.rubric.criterions)}"
        gradeFieldId="${this.gradeFieldId}"
        rubricAssociation="${JSON.stringify(this.association)}"
        stateDetails="${this.stateDetails}"
        evaluationDetails="${JSON.stringify(this.evaluation.criterionOutcomes)}"
        preview="${this.preview}"
        entityId="${this.entityId}"
        ></sakai-rubric-criterion-student>
    `;
  }

  init() {

    // First, grab the tool association
    $.ajax({
      url: `/rubrics-service/rest/rubric-associations/search/by-tool-item-ids?toolId=${this.toolId}&itemId=${this.entityId}`,
      headers: {"authorization": this.token}
    })
    .done(data => {

      if (data._embedded['rubric-associations'].length) {
        this.association = data._embedded['rubric-associations'][0];
        this.options = data._embedded['rubric-associations'][0].parameters;
        var rubricId = data._embedded['rubric-associations'][0].rubricId;

        // Now, get the rubric
        $.ajax({
          url: `/rubrics-service/rest/rubrics/${rubricId}?projection=inlineRubric`,
          headers: {"authorization": this.token},
          contentType: "application/json"
        })
        .done(rubric => {

          // Now, get the evaluation
          $.ajax({
            url: `/rubrics-service/rest/evaluations/search/by-tool-item-and-associated-item-and-evaluated-item-ids?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`,
            headers: {"authorization": this.token}
          })
          .done(data => {

            if (data._embedded.evaluations.length) {
              this.evaluation = data._embedded.evaluations[0];
            } else {
              this.evaluation = { criterionOutcomes: [] };
              this.preview = true;
            }

            // Set the rubric, thus triggering a render
            this.rubric = rubric;
          })
          .fail((jqXHR, textStatus, error) => { console.log(textStatus); console.log(error); });

        })
        .fail((jqXHR, textStatus, error) => { console.log(textStatus); console.log(error); });

        if (this.options.hideStudentPreview == null){
          this.options.hideStudentPreview = false;
        }
      }
    })
    .fail((jqXHR, textStatus, error) => { console.log(textStatus); console.log(error); });
  }
}

customElements.define("sakai-rubric-student", SakaiRubricStudent);
