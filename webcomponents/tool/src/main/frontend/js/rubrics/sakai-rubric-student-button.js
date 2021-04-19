import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";
import { SakaiRubricsHelpers } from "./sakai-rubrics-helpers.js";

class SakaiRubricStudentButton extends RubricsElement {

  constructor() {

    super();

    this.hidden = true;
    this.instructor = false;
    this.forcePreview = false;
    this.i18nPromise = SakaiRubricsLanguage.loadTranslations();
  }

  static get properties() {

    return {
      token: String,
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      hidden: Boolean,
      instructor: Boolean,
      forcePreview: { attribute: "force-preview", type: Boolean },
      dontCheckAssociation: { attribute: "dont-check-association", type: Boolean },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.token && this.toolId && this.entityId) {
      this.setupHidden();
    }
  }

  set token(newValue) {

    this.i18nPromise.then(r => this.initLightbox(newValue, r));
    this._token = newValue;
  }

  get token() { return this._token; }

  render() {

    return html`${this.hidden ? "" : html`
      <a @click=${this.showRubric} href="javascript:;" title="${tr("preview_rubric")}"><span class="fa fa-table" /></a>
    `}`;
  }

  showRubric(e) {
    this.showRubricLightbox(undefined, { "tool-id": this.toolId, "entity-id": this.entityId, "evaluated-item-id": this.evaluatedItemId, "instructor": this.instructor, "force-preview": this.forcePreview }, e.target);
  }

  releaseEvaluation() {

    const token = `Bearer ${this.token}`;

    const url = `/rubrics-service/rest/evaluations/search/by-tool-and-assignment-and-submission?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`;
    return fetch(url, {
      credentials: "same-origin",
      headers: {
        "Authorization": token,
      },
    })
    .then(r => r.json())
    .then(async data => {

      const evaluation = data._embedded.evaluations[0];
      if (evaluation) {
        evaluation.status = 2;
        const url = `/rubrics-service/rest/evaluations/${evaluation.id}`;
        await fetch(url, {
          body: JSON.stringify(evaluation),
          credentials: "same-origin",
          headers: {
            "Authorization": token,
            "Content-Type": "application/json",
          },
          method: "PATCH",
        })
        .then(r => {

          if (!r.ok) {
            throw new Error("Failed to release evaluation");
          }
        });
      }
    }).catch(error => console.error(error));
  }

  setupHidden() {

    if (this.dontCheckAssociation) {
      this.hidden = true;
    } else {
      SakaiRubricsHelpers.get("/rubrics-service/rest/rubric-associations/search/by-tool-and-assignment"
        , "Bearer " + this.token, { params: { toolId: this.toolId, itemId: this.entityId } }).then(data => {

        const association = data._embedded["rubric-associations"][0];

        if (!association) {
          this.hidden = true;
        } else {
          this.hidden = association.parameters.hideStudentPreview && !this.instructor;
        }
      });
    }
  }
}

customElements.define("sakai-rubric-student-button", SakaiRubricStudentButton);
