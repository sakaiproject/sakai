import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { rubricsApiMixin } from "./SakaiRubricsApiMixin.js";
import "../sakai-rubric-criterion-preview.js";
import "../sakai-rubric-criterion-student.js";
import "../sakai-rubric-pdf.js";
import { GRADING_RUBRIC, CRITERIA_SUMMARY, STUDENT_SUMMARY } from "./sakai-rubrics-constants.js";

export class SakaiRubricStudent extends rubricsApiMixin(RubricsElement) {

  static properties = {

    entityId: { attribute: "entity-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    siteId: { attribute: "site-id", type: String },
    preview: { type: Boolean },
    instructor: { type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
    rubricId: { attribute: "rubric-id", type: String },
    forcePreview: { attribute: "force-preview", type: Boolean },
    enablePdfExport: { attribute: "enable-pdf-export", type: Object },
    isPeerOrSelf: { attribute: "is-peer-or-self", type: Boolean },

    _rubric: { state: true },
    _currentView: { state: true },
  };

  constructor() {

    super();

    this.setRubricRequirements = [ "site-id", "rubric-id", "preview" ];

    this._currentView = GRADING_RUBRIC;

    this.options = {};
    this.instanceSalt = Math.floor(Math.random() * Date.now());
  }

  get dynamic () { return this.options && this.options["rbcs-associate"] === 2; }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ((name === "entity-id" && this.toolId) || (name === "tool-id" && this.entityId) || (name === "evaluated-item-id" && this.evaluatedItemId)) {
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

  _viewSelected(e) {
    this._currentView = e.target.value;

    switch (e.target.value) {
      case GRADING_RUBRIC:
        this.openGradePreviewTab();
        break;
      case STUDENT_SUMMARY:
        this.makeStudentSummary();
        break;
      case CRITERIA_SUMMARY:
        this.makeCriteriaSummary();
        break;
      default:
    }
  }

  handleClose() {

    const el = this.querySelector("sakai-rubric-criterion-student");
    el && el.handleClose();
  }

  shouldUpdate() {
    return this.siteId && this._i18nLoaded && this._rubric && (this.instructor || !this.options.hideStudentPreview || this.options["rbcs-associate"] != 2);
  }

  render() {

    console.debug("SakaiRubricStudent.render");
    const isInstructor = this.instructor && this.instructor === "true";

    return html`
      <div class="rubric-details grading student-view">
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

        ${isInstructor ? html`
          <select @change=${this._viewSelected} class="mb-3"
              aria-label="${this._i18n.rubric_view_selection_title}"
              title="${this._i18n.rubric_view_selection_title}" .value=${this._currentView}>
            <option value="grading-rubric">${this._i18n.grading_rubric}</option>
            <option value="${STUDENT_SUMMARY}">${this._i18n.student_summary}</option>
            <option value="${CRITERIA_SUMMARY}">${this._i18n.criteria_summary}</option>
          </select>
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

  displayGradingTab() {

    this.openGradePreviewTab();
    this._currentView = GRADING_RUBRIC;
  }

  openGradePreviewTab() {
    this.openRubricsTab(`rubric-grading-or-preview-${this.instanceSalt}`);
  }

  makeStudentSummary() {
    this.makeASummary("student", this.siteId);
  }

  makeCriteriaSummary() {
    this.makeASummary("criteria", this.siteId);
  }
}
