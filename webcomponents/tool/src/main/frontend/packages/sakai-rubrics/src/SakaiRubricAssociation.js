import { RubricsElement } from "./RubricsElement.js";
import "../sakai-rubric-student.js";
import { html, nothing } from "lit";

export class SakaiRubricAssociation extends RubricsElement {

  static properties = {

    association: { type: Object },
    associationId: { attribute: "association-id", type: String },
    isAssociated: Number,
    entityId: { attribute: "entity-id", type: String },
    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    dontAssociateLabel: { attribute: "dont-associate-label", type: String },
    associateLabel: { attribute: "associate-label", type: String },
    associateLabelDyn: { attribute: "associate-label-dyn", type: String },
    fineTunePoints: { attribute: "fine-tune-points", type: String },
    hideStudentPreview: { attribute: "hide-student-preview", type: String },
    readOnly: { attribute: "read-only", type: Boolean },
    studentSelfReport: { attribute: "student-self-report", type: String },
    studentSelfReportMode0: { attribute: "student-self-report-mode-0", type: String },
    studentSelfReportMode1: { attribute: "student-self-report-mode-1", type: String },
    studentSelfReportMode2: { attribute: "student-self-report-mode-2", type: String },
    showSelfReportCheck: { attribute: "show-self-report-check", type: Boolean },
    showDynamic: { attribute: "show-dynamic", type: Boolean },

    _selectedRubricId: { state: true },
    _rubrics: { state: true },
  };

  constructor() {

    super();

    this.selectedConfigOptions = {};

    this.isAssociated = 0;
  }

  connectedCallback() {

    super.connectedCallback();

    if (this.siteId) {
      this._i18nLoaded.then(r => this.initLightbox(r, this.siteId));
      this._getRubrics();
    }

    if (!this.association && this.entityId && this.toolId) {
      this._getAssociation();
    }
  }

  set association(value) {

    this._association = value;
    this._selectedRubricId = value.rubricId;
    this.selectedConfigOptions = value.parameters ? value.parameters : {};
    this.isAssociated = 1;
    if (this.selectedConfigOptions["rbcs-associate"] == 2) {
      this.isAssociated = 2;
      this.selectedConfigOptions = {};
    }
    this._getRubrics();
  }

  get association() { return this._association; }

  _toggleFineTunePoints(e) {

    if (!e.target.checked) {
      if (!confirm(this._i18n.adjust_scores_warning)) {
        e.preventDefault();
      }
    }
  }

  _getAssociation() {

    let url = `/api/sites/${this.siteId}/rubric-associations/tools/${this.toolId}`;
    if (this.entityId) url += `/items/${this.entityId}`;

    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        if (r.status === 204) {
          return {};
        }
        return r.text().then(text => {
          if (!text) {
            this.style.display = "none";
            return {};
          }
          try {
            return JSON.parse(text);
          } catch (e) {
            console.error("Failed to parse response as JSON:", e);
            return {};
          }
        });
      }

      if (r.status === 404) {
        this.style.display = "none";
        return {};
      }

      throw new Error("Network error while getting association");
    })
    .then(assoc => {

      this.association = assoc;
      if (this.association?.rubricId) {
        this._selectedRubricId = this.association.rubricId;
      } else {
        this.isAssociated = 0;
      }
      this._getRubrics();
    })
    .catch (error => console.error(error));
  }

  _getRubrics() {

    const url = `/api/sites/${this.siteId}/rubrics?withshared=true`;
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        if (r.status === 204) {
          return {};
        }
        return r.json();
      }

      throw new Error("Network error while requesting rubrics");
    })
    .then(data => this._handleRubrics(data))
    .catch (error => console.error(error));
  }

  _handleRubrics(data) {

    this._rubrics = data.slice().filter(rubric => !rubric.draft);

    if (this._rubrics.length && this.isAssociated != 1 && !this.association?.rubricId) {
      // Not associated yet, select the first rubric in the list.
      this._selectedRubricId = this._rubrics[0].id;
    }
    if (this.association?.rubricId) {
      this._selectedRubricId = parseInt(this.association.rubricId, 10);
    }
  }

  _rubricSelected(e) {
    this._selectedRubricId = e.target.value;
  }

  _showRubric(e) {

    e.preventDefault();
    e.stopPropagation();

    if (this.isAssociated == 1) {
      this.showRubricLightbox(this._selectedRubricId);
    }
  }

  _associate(e) {
    this.isAssociated = e.target.value;
  }

  shouldUpdate() {
    return super.shouldUpdate() && this._rubrics && (this._rubrics.length > 0 || this.showDynamic);
  }

  render() {

    return html`
      <h4>${this._i18n.grading_rubric}</h4>
      <div class="sak-banner-warn"><small>${this._i18n.rubric_points_warning}</small></div>
      <div class="sakai-rubric-association form">
        ${this.readOnly ? nothing : html`
          <div class="radio">
            <label>
              <input
                  @click="${this._associate}"
                  name="rbcs-associate"
                  id="dont-associate-radio"
                  type="radio"
                  class="me-1"
                  .value="${this.dontAssociateValue}"
                  ?checked=${!this.isAssociated}
                  ?disabled=${this.readOnly}>${this.dontAssociateLabel}
            </label>
          </div>
        `}
        ${this._rubrics.length > 0 && !this.readOnly ? html`
          <div class="radio">
            <label>
              <input @click="${this._associate}" name="rbcs-associate" id="do-associate-radio" type="radio" class="me-1" value="1" ?checked=${this.isAssociated == 1} ?disabled=${this.readOnly}>${this.associateLabel}
            </label>
          </div>
        ` : ""}
        ${this._rubrics.length > 0 ? html`
        <div class="rubrics-list">

          <div class="rubrics-selections">
            <select @change="${this._rubricSelected}"
                name="rbcs-rubricslist"
                aria-label="${this._i18n.rubric_selector_label}"
                class="form-control"
                ?disabled=${this.isAssociated != 1 || this.readOnly}>
              ${this._rubrics.map(r => html`
              <option value="${r.id}" ?selected=${r.id == this._selectedRubricId ? "selected" : undefined}>
                ${r.title} ${r.maxPoints ? `(${r.maxPoints} ${this._i18n.points})` : ""}
              </option>
              `)}
            </select>

            <button type="button" @click="${this._showRubric}" class="btn btn-link" ?disabled=${this.isAssociated != 1}>
              ${this._i18n.preview_rubric}
            </button>
          </div>

          ${this.readOnly ? "" : html`
            <div class="rubric-options">
              <div class="checkbox">
                <label>
                  <input
                      name="rbcs-config-fineTunePoints"
                      type="checkbox"
                      class="me-1"
                      @click=${this._toggleFineTunePoints}
                      ?checked=${this.selectedConfigOptions.fineTunePoints}
                      value="1"
                      ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.fineTunePoints}
                </label>
              </div>
              <div class="checkbox">
                <label>
                  <input name="rbcs-config-hideStudentPreview" type="checkbox" class="me-1" ?checked=${this.selectedConfigOptions.hideStudentPreview} value="1" ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.hideStudentPreview}
                </label>
              </div>
              ${this.showSelfReportCheck ? html`
                <div class="checkbox">
                  <label>
                    <input @change="${this.updateStudentSelfReportInput}" id="rbcs-config-studentSelfReport" name="rbcs-config-studentSelfReport" type="checkbox" class="me-1" ?checked=${this.selectedConfigOptions.studentSelfReport} value="1" ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReport}
                  </label>
                  <div id="rbcs-multiple-options-config-studentSelfReportMode-container" class="rubrics-list ${!this.selectedConfigOptions.studentSelfReport ? "hidden" : ""}">
                    <div class="rubric-options">
                      <div class="form-check">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" class="me-1" value="0" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "0" || !this.selectedConfigOptions.studentSelfReportMode} ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReportMode0}
                        </label>
                      </div>
                      <div class="form-check">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" class="me-1" value="1" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "1"} ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReportMode1}
                        </label>
                      </div>
                      <div class="form-check">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" class="me-1" value="2" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "2"} ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReportMode2}
                        </label>
                      </div>
                    </div>
                  </div>
                </div>
                ` : ""
              }
            </div>
        `}
        </div>
        ` : ""}
        ${this.showDynamic ? html`
          <div class="radio">
            <label>
              <input @click="${this.associate}" id="do-associate-dynamic-radio" name="rbcs-associate" type="radio" class="me-1" value="2" ?checked=${this.isAssociated == 2} ?disabled=${this.readOnly}>${this.associateLabelDyn}
            </label>
          </div>
        ` : ""}
      </div>
    `;
    //<button @click="${this._showRubric}" class="btn btn-link" data-bs-toggle="modal" data-bs-target="#rubric-preview" aria-controls="rubric-preview" ?disabled=${!this.isAssociated}>
  }

  updateStudentSelfReportInput(e) {
    if (this.isAssociated == 1) {
      const showSelfReportMode = e.srcElement.checked;
      document.getElementById("rbcs-multiple-options-config-studentSelfReportMode-container").classList.toggle("hidden", !showSelfReportMode);
    }
  }

  firstUpdated() {
    setTimeout(() => { // in order to ensure proper loading order with the tool
      this.dispatchEvent(new CustomEvent("rubric-association-loaded", {
        bubbles: true,
        composed: true
      }));
    }, 100);
  }

}
