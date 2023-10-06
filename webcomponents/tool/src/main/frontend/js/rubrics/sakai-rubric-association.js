import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";

class SakaiRubricAssociation extends RubricsElement {

  static get properties() {

    return {
      association: { type: Object },
      associationId: { attribute: "association-id", type: String },
      isAssociated: Boolean,
      entityId: { attribute: "entity-id", type: String },
      siteId: { attribute: "site-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      dontAssociateLabel: { attribute: "dont-associate-label", type: String },
      associateLabel: { attribute: "associate-label", type: String },
      dontAssociateValue: { attribute: "dont-associate-value", type: Number },
      associateValue: { attribute: "associate-value", type: Number },
      fineTunePoints: { attribute: "fine-tune-points", type: String },
      hideStudentPreview: { attribute: "hide-student-preview", type: String },
      readOnly: { attribute: "read-only", type: Boolean },
      selectedRubricId: { attribute: false, type: String },
      studentSelfReport: { attribute: "student-self-report", type: String },
      studentSelfReportMode0: { attribute: "student-self-report-mode-0", type: String },
      studentSelfReportMode1: { attribute: "student-self-report-mode-1", type: String },
      studentSelfReportMode2: { attribute: "student-self-report-mode-2", type: String },
      showSelfReportCheck: { attribute: "show-self-report-check", type: Boolean },
      rubrics: { attribute: false, type: Array },
    };
  }

  constructor() {

    super();

    this.selectedConfigOptions = {};

    this.isAssociated = false;
    this.showSelfReportCheck = false;
    this.i18nPromise = SakaiRubricsLanguage.loadTranslations();
    this.i18nPromise.then(r => this.i18n = r);
  }

  set siteId(value) {

    this._siteId = value;
    this.i18nPromise.then(r => this.initLightbox(r, value));
    this._getRubrics();
  }

  get siteId() { return this._siteId; }

  set entityId(value) {

    this._entityId = value;
    if (this.toolId) {
      this._getAssociation();
    }
  }

  get entityId() { return this._entityId; }


  set showSelfReportCheck(newValue) {
    this._showSelfReportCheck = newValue;
  }

  get showSelfReportCheck() {
    return this._showSelfReportCheck;
  }

  set association(value) {

    this._association = value;
    this.selectedRubricId = value.rubricId;
    this.selectedConfigOptions = value.parameters ? value.parameters : {};
    this.isAssociated = true;
    this._getRubrics();
  }

  get association() { return this._association; }

  _toggleFineTunePoints(e) {

    if (!e.target.checked) {
      if (!confirm(this.i18n.adjust_scores_warning)) {
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
        return r.json();
      }
      throw new Error("Network error while getting association");
    })
    .then(assoc => {

      this.association = assoc;
      if (this.association) {
        this.isAssociated = 1;
        this.selectedRubricId = this.association.rubricId;
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
        return r.json();
      }
      throw new Error("Network error while requesting rubrics");
    })
    .then(data => this._handleRubrics(data))
    .catch (error => console.error(error));
  }

  _handleRubrics(data) {

    this.rubrics = data.slice().filter( (rubric) => rubric.draft === false);

    if (this.rubrics.length && !this.isAssociated) {
      // Not associated yet, select the first rubric in the list.
      this.selectedRubricId = this.rubrics[0].id;
    }
  }

  _rubricSelected(e) {
    this.selectedRubricId = e.target.value;
  }

  _showRubric(e) {

    e.preventDefault();
    if (this.isAssociated) {
      this.showRubricLightbox(this.selectedRubricId);
    }
  }

  _associate(e) {
    this.isAssociated = e.target.value == 1;
  }

  shouldUpdate() {
    return this.i18n && this.rubrics && this.rubrics.length > 0;
  }

  render() {

    return html`
      <h4><sr-lang key="grading_rubric">Grading Rubric</sr-lang></h4>
      <div class="sak-banner-warn"><small>${this.i18n.rubric_points_warning}</small></div>
      <div class="sakai-rubric-association form">
        ${this.readOnly ? "" : html`
          <div class="form-check">
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

          <div class="form-check">
            <label>
              <input @click="${this._associate}" name="rbcs-associate" id="do-associate-radio" type="radio" class="me-1" .value="${this.associateValue}" ?checked=${this.isAssociated} ?disabled=${this.readOnly}>${this.associateLabel}
            </label>
          </div>
        `}
        <div class="rubrics-list">

          <div class="rubrics-selections">
            <select @change="${this._rubricSelected}" name="rbcs-rubricslist" aria-label="${tr("rubric_selector_label")}" class="form-control" ?disabled=${!this.isAssociated || this.readOnly}>
            ${this.rubrics.map(r => html`
              <option value="${r.id}" ?selected=${r.id === this.selectedRubricId}>
                ${r.title} ${r.maxPoints ? `(${r.maxPoints} ${this.i18n.points})` : ""}
              </option>
            `)}
            </select>

            <button @click="${this._showRubric}" class="btn btn-link" ?disabled=${!this.isAssociated}>
              <sr-lang key="preview_rubric">Preview Rubric</sr-lang>
            </button>
          </div>

          ${this.readOnly ? "" : html`
            <div class="rubric-options">
              <div class="form-check">
                <label>
                  <input
                      name="rbcs-config-fineTunePoints"
                      type="checkbox"
                      class="me-1"
                      @click=${this._toggleFineTunePoints}
                      ?checked=${this.selectedConfigOptions.fineTunePoints}
                      value="1"
                      ?disabled=${!this.isAssociated || this.readOnly}>${this.fineTunePoints}
                </label>
              </div>
              <div class="form-check">
                <label>
                  <input name="rbcs-config-hideStudentPreview" type="checkbox" class="me-1" ?checked=${this.selectedConfigOptions.hideStudentPreview} value="1" ?disabled=${!this.isAssociated || this.readOnly}>${this.hideStudentPreview}
                </label>
              </div>
              ${this.showSelfReportCheck ? html`
                <div class="form-check">
                  <label>
                    <input @change="${this.updateStudentSelfReportInput}" id="rbcs-config-studentSelfReport" name="rbcs-config-studentSelfReport" type="checkbox" ?checked=${this.selectedConfigOptions.studentSelfReport} value="1" ?disabled=${!this.isAssociated || this.readOnly}>${this.studentSelfReport}
                  </label>
                  <div id="rbcs-multiple-options-config-studentSelfReportMode-container" class="rubrics-list ${!this.selectedConfigOptions.studentSelfReport ? 'hidden' : ''}">
                    <div class="rubric-options">
                      <div class="form-check">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" class="me-1" value="0" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "0" || !this.selectedConfigOptions.studentSelfReportMode} ?disabled=${!this.isAssociated || this.readOnly}>${this.studentSelfReportMode0}
                        </label>
                      </div>
                      <div class="form-check">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" class="me-1" value="1" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "1"} ?disabled=${!this.isAssociated || this.readOnly}>${this.studentSelfReportMode1}
                        </label>
                      </div>
                      <div class="form-check">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" class="me-1" value="2" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "2"} ?disabled=${!this.isAssociated || this.readOnly}>${this.studentSelfReportMode2}
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
      </div>
    `;
  }

  updateStudentSelfReportInput(e) {
    if (this.isAssociated) {
      const showSelfReportMode = e.srcElement.checked;
      document.getElementById('rbcs-multiple-options-config-studentSelfReportMode-container').classList.toggle('hidden', !showSelfReportMode);
    }
  }

  firstUpdated() {
    setTimeout(() => { // in order to ensure proper loading order with the tool
      this.dispatchEvent(new CustomEvent('rubric-association-loaded', {
        bubbles: true,
        composed: true
      }));
    }, 100);
  }

}

customElements.define("sakai-rubric-association", SakaiRubricAssociation);
