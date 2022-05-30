import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import "./sakai-rubric-criterion-preview.js";
import "./sakai-rubric-criterion-student.js";
import "./sakai-rubric-pdf.js";
import { SakaiRubricsLanguage } from "./sakai-rubrics-language.js";

class SakaiRubricStudent extends RubricsElement {

  constructor() {

    super();

    this.preview = false;

    this.options = {};
    SakaiRubricsLanguage.loadTranslations().then(result => this.i18nLoaded = result);
  }

  static get properties() {

    return {
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      siteId: { attribute: "site-id", type: String },
      stateDetails: String,
      preview: Boolean,
      instructor: Boolean,
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      rubric: { type: Object },
      rubricId: { attribute: "rubric-id", type: String },
      forcePreview: { attribute: "force-preview", type: Boolean },
      enablePdfExport: { attribute: "enable-pdf-export", type: Object },
    };
  }

  set toolId(value) {

    this._toolId = value;

    if (this.toolId && this.entityId) {
      this.init();
    }
  }

  get toolId() { return this._toolId; }

  set entityId(value) {

    this._entityId = value;
    if (this.toolId && this.entityId) {
      this.init();
    }
  }

  get entityId() { return this._entityId; }

  set preview(newValue) {

    this._preview = newValue;
    if (this.rubricId) {
      this.setRubric();
    }
  }

  get preview() { return this._preview; }

  set rubricId(newValue) {

    this._rubricId = newValue;
    if (this._rubricId != null && this.preview) {
      this.setRubric();
    }
  }

  get rubricId() { return this._rubricId; }

  handleClose() {

    const el = this.querySelector("sakai-rubric-criterion-student");
    el && el.handleClose();
  }

  shouldUpdate() {
    return this.i18nLoaded && this.rubric && (this.instructor || !this.options.hideStudentPreview);
  }

  render() {

    return html`
      <hr class="itemSeparator" />

      <div class="rubric-details student-view">
        <h3>
          <span>${this.rubric.title}</span>
          ${this.enablePdfExport ? html`
            <sakai-rubric-pdf
                site-id="${this.siteId}"
                rubric-title="${this.rubric.title}"
                rubric-id="${this.rubric.id}"
                tool-id="${this.toolId}"
                entity-id="${this.entityId}"
                evaluated-item-id="${this.evaluatedItemId}"
            />
          ` : ""}
        </h3>

        ${this.preview || this.forcePreview ? html`
          <sakai-rubric-criterion-preview
            .criteria="${this.rubric.criteria}"
            .weighted=${this.rubric.weighted}
          ></sakai-rubric-criterion-preview>
          ` : html`
          <sakai-rubric-criterion-student
            .criteria="${this.rubric.criteria}"
            rubric-association="${JSON.stringify(this.association)}"
            evaluation-details="${JSON.stringify(this.evaluation.criterionOutcomes)}"
            ?preview="${this.preview}"
            entity-id="${this.entityId}"
            .weighted=${this.rubric.weighted}
          ></sakai-rubric-criterion-student>
        `}
      </div>
    `;
  }

  setRubric() {

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}`;
    fetch(url, { credentials: "include", headers: { "Content-Type": "application/json" } })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while getting rubric");
    })
    .then(rubric => this.rubric = rubric)
    .catch (error => console.error(error));
  }

  init() {

    // First, grab the tool association
    const url = `/api/sites/${this.siteId}/rubric-associations/tools/${this.toolId}/items/${this.entityId}`;

    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while getting association");
    })
    .then(association => {

      if (association) {
        this.association = association;
        this.options = association.parameters;
        const rubricId = association.rubricId;

        // Now, get the rubric
        const rubricUrl = `/api/sites/${association.siteId}/rubrics/${rubricId}`;
        fetch(rubricUrl, {
          credentials: "include",
          headers: { "Content-Type": "application/json" },
        })
        .then(r => {

          if (r.ok) {
            return r.json();
          }
          throw new Error("Server error while getting rubric");
        })
        .then(rubric => {

          // Now, get the evaluation
          const evalUrl = `/api/sites/${association.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
          fetch(evalUrl, {
            credentials: "include",
            headers: { "Content-Type": "application/json" },
          })
          .then(r => {

            if (r.ok) {
              return r.json();
            }

            if (r.status !== 404) {
              throw new Error("Server error while getting evaluation");
            }
          })
          .then(evaluation => {

            if (evaluation) {
              this.evaluation = evaluation;
              this.preview = false;
            } else {
              this.evaluation = { criterionOutcomes: [] };
              this.preview = true;
            }

            // Set the rubric, thus triggering a render
            this.rubric = rubric;
          })
          .catch (error => console.error(error));
        })
        .catch (error => console.error(error));

        if (this.options.hideStudentPreview == null) {
          this.options.hideStudentPreview = false;
        }
      }
    })
    .catch (error => console.error(error));
  }
}

const tagName = "sakai-rubric-student";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricStudent);
