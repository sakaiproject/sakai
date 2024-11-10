import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { rubricsApiMixin } from "./SakaiRubricsApiMixin.js";
import "../sakai-rubric-criterion-preview.js";
import "../sakai-rubric-criterion-student.js";
import "../sakai-rubric-pdf.js";

export class SakaiRubricStudent extends rubricsApiMixin(RubricsElement) {

  static properties = {

    entityId: { attribute: "entity-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    siteId: { attribute: "site-id", type: String },
    preview: { type: Boolean },
    instructor: { type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    rubricId: { attribute: "rubric-id", type: String },
    forcePreview: { attribute: "force-preview", type: Boolean },
    enablePdfExport: { attribute: "enable-pdf-export", type: Object },

    _rubric: { state: true },
  };

  constructor() {

    super();

    this.setRubricRequirements = [ "site-id", "rubric-id", "preview" ];

    this.options = {};
    this.instanceSalt = Math.floor(Math.random() * Date.now());
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ((name === "entity-id" && this.toolId) || (name === "tool-id" && this.entityId)) {
      this._init();
    }

    if (this.setRubricRequirements.includes(name)) {
      this._setRubric();
    }

    // If rubric-id has been removed, undefine the rubric
    if (name === "rubric-id" && !newValue) {
      this._rubric = undefined;
    }
  }

  handleClose() {

    const el = this.querySelector("sakai-rubric-criterion-student");
    el && el.handleClose();
  }

  shouldUpdate() {
    return this.siteId && this._i18nLoaded && this._rubric && (this.instructor || !this.options.hideStudentPreview);
  }

  render() {

    console.debug("SakaiRubricStudent.render");
    const isInstructor = this.instructor && this.instructor === "true";

    return html`
      <div class="rubric-details student-view">
        <h3>
          <span>${this._rubric.title}</span>
          ${this.enablePdfExport ? html`
            <sakai-rubric-pdf
                site-id="${this.siteId}"
                rubric-title="${this._rubric.title}"
                rubric-id="${this._rubric.id}"
                tool-id="${this.toolId}"
                entity-id="${this.entityId}"
                evaluated-item-id="${this.evaluatedItemId}">
            </sakai-rubric-pdf>
          ` : nothing }
        </h3>

        ${isInstructor ? html`
        <div class="rubrics-tab-row">
          <a href="javascript:void(0);"
              id="rubric-grading-or-preview-${this.instanceSalt}-button"
              class="rubrics-tab-button rubrics-tab-selected"
              @keypress=${this.openGradePreviewTab}
              @click=${this.openGradePreviewTab}>
            ${this._i18n.grading_rubric}
          </a>
          <a href="javascript:void(0);"
              id="rubric-student-summary-${this.instanceSalt}-button"
              class="rubrics-tab-button"
              @keypress=${this.makeStudentSummary}
              @click=${this.makeStudentSummary}>
            ${this._i18n.student_summary}
          </a>
          <a href="javascript:void(0);"
              id="rubric-criteria-summary-${this.instanceSalt}-button"
              class="rubrics-tab-button"
              @keypress=${this.makeCriteriaSummary}
              @click=${this.makeCriteriaSummary}>
            ${this._i18n.criteria_summary}
          </a>
        </div>
        ` : nothing }

        <div id="rubric-grading-or-preview-${this.instanceSalt}" class="rubric-tab-content rubrics-visible">
          ${this.preview || this.forcePreview ? html`
          <sakai-rubric-criterion-preview .criteria=${this._rubric.criteria}
            ?weighted=${this._rubric.weighted}>
          </sakai-rubric-criterion-preview>
          ` : html`
          <sakai-rubric-criterion-student
            .criteria=${this._rubric.criteria}
            .association=${this.association}
            .outcomes=${this.evaluation.criterionOutcomes}
            ?preview=${this.preview}
            entity-id="${this.entityId}"
            ?weighted=${this._rubric.weighted}>
          </sakai-rubric-criterion-student>
          `}
        </div>
        <div id="rubric-student-summary-${this.instanceSalt}" class="rubric-tab-content"></div>
        <div id="rubric-criteria-summary-${this.instanceSalt}" class="rubric-tab-content"></div>
      </div>
    `;
  }

  _setRubric() {

    if (!this.siteId || !this.rubricId || !this.preview) return;

    const url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}`;
    fetch(url, { credentials: "include", headers: { "Content-Type": "application/json" } })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Network error while getting rubric at ${url}`);
    })
    .then(rubric => this._rubric = rubric)
    .catch (error => console.error(error));
  }

  _init() {

    console.debug("SakaiRubricStudent.init");

    // First, grab the tool association
    this.apiGetAssociation()
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

            if (this.evaluatedItemId) {
              const evalUrl = `/api/sites/${association.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
              fetch(evalUrl, {
                credentials: "include",
                headers: { "Content-Type": "application/json" },
              })
              .then(r => {

                if (r.status === 200) {
                  return r.json();
                }

                if (r.status !== 204) {
                  throw new Error(`Network error while getting evaluation at ${evalUrl}`);
                }

                return null;
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
                this._rubric = rubric;
              })
              .catch (error => console.error(error));
            } else {
              this.evaluation = { criterionOutcomes: [] };
              this.preview = true;
              this._rubric = rubric;
            }
          })
          .catch (error => console.error(error));

          if (this.options.hideStudentPreview == null) {
            this.options.hideStudentPreview = false;
          }
        }
      })
      .catch (error => console.error(error));
  }

  openGradePreviewTab(e) {

    e.stopPropagation();
    this.openRubricsTab(`rubric-grading-or-preview-${this.instanceSalt}`);
  }

  makeStudentSummary(e) {

    e.stopPropagation();
    this.makeASummary("student", this.siteId);
  }

  makeCriteriaSummary(e) {

    e.stopPropagation();
    this.makeASummary("criteria", this.siteId);
  }
}
