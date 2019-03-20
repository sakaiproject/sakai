import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricCriterionPreview} from "./sakai-rubric-criterion-preview.js";
import {SakaiRubricCriterionStudent} from "./sakai-rubric-criterion-student.js";
import {SakaiRubricStudentComment} from "./sakai-rubric-student-comment.js";
import {SakaiRubricsLanguage} from "./sakai-rubrics-language.js";

class SakaiRubricStudent extends SakaiElement {

  constructor() {

    super();

    this.preview = false;

    this.options = {};
    SakaiRubricsLanguage.loadTranslations().then(result => this.i18nLoaded = result);
  }

  static get properties() {

    return {
      token: { type: String},
      entityId: { attribute: "entity-id", type: String},
      toolId: { attribute: "tool-id", type: String},
      stateDetails: { type: String},
      preview: { type: Boolean},
      instructor: { type: Boolean},
      evaluatedItemId: { attribute: "evaluated-item-id", type: String},
      rubric: { type: Object },
      rubricId: { attribute: "rubric-id", type: String },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.token && this.toolId && this.entityId) {
      this.init();
    } else if (this.token && this.preview && this.rubricId) {
      this.setRubric();
    }
  }

  set token(newValue) {
    this._token = "Bearer " + newValue;
  }

  get token() { return this._token; }

  shouldUpdate(changedProperties) {
    return this.i18nLoaded && changedProperties.has("rubric") && (this.instructor || !this.options.hideStudentPreview);
  }

  render() {

    return html`
      <hr class="itemSeparator" />

      <h3>${this.rubric.title}</h3>

      ${this.preview ?
        html`<sakai-rubric-criterion-preview
          criteria="${JSON.stringify(this.rubric.criterions)}"
          ></sakai-rubric-criterion-preview>
        `
        :
        html`<sakai-rubric-criterion-student
          criteria="${JSON.stringify(this.rubric.criterions)}"
          rubric-association="${JSON.stringify(this.association)}"
          evaluation-details="${JSON.stringify(this.evaluation.criterionOutcomes)}"
          ?preview="${this.preview}"
          entity-id="${this.entityId}"
          ></sakai-rubric-criterion-student>
        `
      }
    `;
  }

  setRubric() {

    $.ajax({
      url: `/rubrics-service/rest/rubrics/${this.rubricId}?projection=inlineRubric`,
      headers: {"authorization": this.token},
      contentType: "application/json"
    })
    .done(data => this.rubric = data )
    .fail((jqXHR, textStatus, errorThrown) => { console.log(textStatus); console.log(errorThrown); });
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
              this.preview = false;
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
