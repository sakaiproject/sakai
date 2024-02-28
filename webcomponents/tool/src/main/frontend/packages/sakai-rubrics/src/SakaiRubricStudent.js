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
    instructor: { type: Boolean },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
    rubricId: { attribute: "rubric-id", type: String },
    forcePreview: { attribute: "force-preview", type: Boolean },
    enablePdfExport: { attribute: "enable-pdf-export", type: Object },
    isPeerOrSelf: { attribute: "is-peer-or-self", type: Boolean },

    _rubric: { state: true },
  };

  constructor() {

    super();

    this.setRubricRequirements = [ "site-id", "rubric-id", "preview" ];

    this.options = {};
  }

  get dynamic () { return this.options?.["rbcs-associate"] == 2 ?? false; }

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
    return this.siteId && this.i18nLoaded && this._rubric && (this.instructor || !this.options.hideStudentPreview || this.options["rbcs-associate"] != 2);
  }

  render() {

    console.debug("SakaiRubricStudent.render");

    return html`
      <hr class="itemSeparator" />

      <div class="rubric-details student-view">
        ${!this.dynamic ? html`
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
        ` : nothing }

        ${this.instructor === "true" ? html`
        <div class="rubrics-tab-row">
          <a href="javascript:void(0);"
              id="rubric-grading-or-preview-button"
              class="rubrics-tab-button rubrics-tab-selected"
              @keypress=${this.openGradePreviewTab}
              @click=${this.openGradePreviewTab}>
            ${this._i18n.grading_rubric}
          </a>
          <a href="javascript:void(0);"
              id="rubric-student-summary-button"
              class="rubrics-tab-button"
              @keypress=${this.makeStudentSummary}
              @click=${this.makeStudentSummary}>
            ${this._i18n.student_summary}
          </a>
          <a href="javascript:void(0);"
              id="rubric-criteria-summary-button"
              class="rubrics-tab-button"
              @keypress=${this.makeCriteriaSummary}
              @click=${this.makeCriteriaSummary}>
            ${this._i18n.criteria_summary}
          </a>
        </div>
        ` : nothing }

        <div id="rubric-grading-or-preview" class="rubric-tab-content rubrics-visible">
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
              // Now, get the evaluation
              let evalUrl = `/api/sites/${association.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}/owners/${this.evaluatedItemOwnerId}`;
              if (this.isPeerOrSelf) {
                //for permission filters
                evalUrl += "?isPeer=true";
              }
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
    this.openRubricsTab("rubric-grading-or-preview");
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
