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
    // Treat instructor as a proper Boolean, parsing string values like "false"/"0" as false.
    instructor: { type: Boolean, converter: { fromAttribute: v => v !== null && v !== "false" && v !== "0" } },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
    rubricId: { attribute: "rubric-id", type: String },
    forcePreview: { attribute: "force-preview", type: Boolean },
    enablePdfExport: { attribute: "enable-pdf-export", type: Object },
    isPeerOrSelf: { attribute: "is-peer-or-self", type: Boolean },
    studentSelfReport: { attribute: "student-self-report", type: Boolean },

    _rubric: { state: true },
    _currentView: { state: true },
  };

  constructor() {

    super();

    this._currentView = GRADING_RUBRIC;
    this._loadId = 0;
    this._loadScheduled = false;

    this.options = {};
    this.instanceSalt = Math.floor(Math.random() * Date.now());
  }

  get dynamic () { return Number(this.options?.["rbcs-associate"]) === 2; }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ([ "site-id", "rubric-id", "preview", "tool-id", "entity-id", "evaluated-item-id", "evaluated-item-owner-id", "is-peer-or-self" ].includes(name)) {
      this._scheduleRubricLoad();
    }
  }

  _scheduleRubricLoad() {

    this._loadId += 1;
    if (this._loadScheduled) {
      return;
    }

    this._loadScheduled = true;
    queueMicrotask(() => {

      this._loadScheduled = false;
      const loadId = this._loadId;

      this._rubric = undefined;
      this.association = undefined;
      this.evaluation = undefined;
      this.options = {};

      if (this.siteId && this.rubricId && this.preview) {
        this._setRubric(loadId);
      } else if (this.siteId && this.toolId && this.entityId) {
        this._init(loadId);
      }
    });
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
    return Boolean(this.siteId && this._i18n);
  }

  render() {

    // Self-report results must remain visible even when the associated rubric is hidden from
    // the normal student preview. This does not grant access to instructor summary views.
    const rubricIsVisible = this.instructor
      || this.studentSelfReport
      || (!this.options.hideStudentPreview && !this.dynamic);
    if (!this._rubric || !rubricIsVisible) {
      return nothing;
    }

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

        ${this.instructor ? html`
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

  async _setRubric(loadId) {

    const siteId = this.siteId;
    const rubricId = this.rubricId;
    const url = `/api/sites/${siteId}/rubrics/${rubricId}`;

    try {
      const response = await fetch(url, { credentials: "include", headers: { "Content-Type": "application/json" } });

      if (!response.ok) {
        throw new Error(`Network error while getting rubric at ${url}`);
      }

      const rubric = await response.json();
      if (loadId === this._loadId) {
        this._rubric = rubric;
      }
    } catch (error) {
      if (loadId === this._loadId) {
        console.error(error);
      }
    }
  }

  async _init(loadId) {

    const toolId = this.toolId;
    const entityId = this.entityId;
    const evaluatedItemId = this.evaluatedItemId;
    const evaluatedItemOwnerId = this.evaluatedItemOwnerId;
    const isPeerOrSelf = this.isPeerOrSelf;

    try {
      const association = await this.apiGetAssociation();
      if (loadId !== this._loadId || !association) {
        return;
      }

      const options = {
        ...(association.parameters ?? {}),
        hideStudentPreview: association.parameters?.hideStudentPreview ?? false,
      };
      const rubricUrl = `/api/sites/${association.siteId}/rubrics/${association.rubricId}`;
      const rubricPromise = fetch(rubricUrl, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      }).then(response => {

        if (!response.ok) {
          throw new Error("Server error while getting rubric");
        }

        return response.json();
      });

      let evaluationPromise = Promise.resolve(null);
      if (evaluatedItemId) {
        let evalUrl = `/api/sites/${association.siteId}/rubric-evaluations/tools/${toolId}/items/${entityId}/evaluations/${evaluatedItemId}/owners/${evaluatedItemOwnerId}`;
        if (isPeerOrSelf) {
          evalUrl += "?isPeer=true";
        }

        evaluationPromise = fetch(evalUrl, {
          credentials: "include",
          headers: { "Content-Type": "application/json" },
        }).then(response => {

          if (response.status === 200) {
            return response.json();
          }

          if (response.status !== 204) {
            throw new Error(`Network error while getting evaluation at ${evalUrl}`);
          }

          return null;
        });
      }

      const [ rubric, evaluation ] = await Promise.all([ rubricPromise, evaluationPromise ]);
      if (loadId !== this._loadId) {
        return;
      }

      this.association = association;
      this.options = options;
      this.evaluation = evaluation ?? { criterionOutcomes: [] };
      this.preview = !evaluation;
      this._rubric = rubric;
    } catch (error) {
      if (loadId === this._loadId) {
        console.error(error);
      }
    }
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
