import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";

class SakaiRubricAssociation extends RubricsElement {

  constructor() {

    super();

    this.selectedConfigOptions = {};

    this.isAssociated = 0;
    this.showSelfReportCheck = false;
    this.showDynamic = false;
    this.i18nPromise = SakaiRubricsLanguage.loadTranslations();
    this.i18nPromise.then(r => this.i18n = r);
  }

  set siteId(value) {

    this._siteId = value;
    this.i18nPromise.then(r => this.initLightbox(r, value));
    this.getRubrics();
  }

  get siteId() { return this._siteId; }

  set entityId(value) {

    this._entityId = value;
    if (this.toolId) {
      this.getAssociation();
    }
  }

  get entityId() { return this._entityId; }

  static get properties() {

    return {
      association: { type: Object },
      associationId: { attribute: "association-id", type: String },
      isAssociated: Number,
      entityId: { attribute: "entity-id", type: String },
      siteId: { attribute: "site-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      selectedRubric: { type: String },
      dontAssociateLabel: { attribute: "dont-associate-label", type: String },
      associateLabel: { attribute: "associate-label", type: String },
      associateLabelDyn: { attribute: "associate-label-dyn", type: String },
      fineTunePoints: { attribute: "fine-tune-points", type: String },
      hideStudentPreview: { attribute: "hide-student-preview", type: String },
      studentSelfReport: { attribute: "student-self-report", type: String },
      studentSelfReportMode0: { attribute: "student-self-report-mode-0", type: String },
      studentSelfReportMode1: { attribute: "student-self-report-mode-1", type: String },
      studentSelfReportMode2: { attribute: "student-self-report-mode-2", type: String },
      showSelfReportCheck: { attribute: "show-self-report-check", type: Boolean },
      rubrics: { type: Array },
      readOnly: { attribute: "read-only", type: Boolean },
      showDynamic: { attribute: "show-dynamic", type: Boolean },
    };
  }

  set showSelfReportCheck(newValue) {
    this._showSelfReportCheck = newValue;
  }

  get showSelfReportCheck() {
    return this._showSelfReportCheck;
  }

  set association(value) {

    this._association = value;
    this.selectedRubric = value.rubricId;
    this.selectedConfigOptions = value.parameters ? value.parameters : {};
    this.isAssociated = 1;
    if (this.selectedConfigOptions['rbcs-associate'] == 2) {
      this.isAssociated = 2;
      this.selectedConfigOptions = {};
    }
    this.getRubrics();
  }

  get association() { return this._association; }

  toggleFineTunePoints(e) {

    if (!e.target.checked) {
      if (!confirm(this.i18n.adjust_scores_warning)) {
        e.preventDefault();
      }
    }
  }

  shouldUpdate() {
    return this.i18n && this.rubrics && this.rubrics.length > 0;
  }

  render() {

    return html`
      <h4><sr-lang key="grading_rubric">Grading Rubric</sr-lang></h4>
      <div class="sakai-rubric-association form">
        ${this.readOnly ? "" : html`
          <div class="radio">
            <label>
              <input
                  @click="${this.associate}"
                  name="rbcs-associate"
                  id="dont-associate-radio"
                  type="radio"
                  value="0"
                  ?checked=${this.isAssociated == 0}
                  ?disabled=${this.readOnly}>${this.dontAssociateLabel}
            </label>
          </div>

          <div class="radio">
            <label>
              <input @click="${this.associate}" id="do-associate-radio" name="rbcs-associate" type="radio" value="1" ?checked=${this.isAssociated == 1} ?disabled=${this.readOnly}>${this.associateLabel}
            </label>
          </div>
        `}
        <div class="rubrics-list">

          <div class="rubrics-selections">
            <select @change="${this.rubricSelected}" name="rbcs-rubricslist" aria-label="${tr("rubric_selector_label")}" class="form-control" ?disabled=${this.isAssociated != 1 || this.readOnly}>
            ${this.rubrics.map(r => html`
              <option value="${r.id}" ?selected=${r.id == this.selectedRubric}>${r.title}</option>
            `)}
            </select>

            <button @click="${this.showRubric}" class="btn btn-link" ?disabled=${this.isAssociated != 1}>
              <sr-lang key="preview_rubric">Preview Rubric</sr-lang>
            </button>
          </div>

          ${this.readOnly ? "" : html`
            <div class="rubric-options">
              <div class="checkbox">
                <label>
                  <input
                      name="rbcs-config-fineTunePoints"
                      type="checkbox"
                      @click=${this.toggleFineTunePoints}
                      ?checked=${this.selectedConfigOptions.fineTunePoints}
                      value="1"
                      ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.fineTunePoints}
                </label>
              </div>
              <div class="checkbox">
                <label>
                  <input name="rbcs-config-hideStudentPreview" type="checkbox" ?checked=${this.selectedConfigOptions.hideStudentPreview} value="1" ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.hideStudentPreview}
                </label>
              </div>
              ${this.showSelfReportCheck ? html`
                <div class="checkbox">
                  <label>
                    <input @change="${this.updateStudentSelfReportInput}" id="rbcs-config-studentSelfReport" name="rbcs-config-studentSelfReport" type="checkbox" ?checked=${this.selectedConfigOptions.studentSelfReport} value="1" ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReport}
                  </label>
                  <div id="rbcs-multiple-options-config-studentSelfReportMode-container" class="rubrics-list ${!this.selectedConfigOptions.studentSelfReport ? 'hidden' : ''}">
                    <div class="rubric-options">
                      <div class="radio">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" value="0" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "0" || !this.selectedConfigOptions.studentSelfReportMode} ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReportMode0}
                        </label>
                      </div>
                      <div class="radio">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" value="1" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "1"} ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReportMode1}
                        </label>
                      </div>
                      <div class="radio">
                        <label>
                          <input name="rbcs-multiple-options-config-studentSelfReportMode" type="radio" value="2" ?checked=${this.selectedConfigOptions.studentSelfReportMode == "2"} ?disabled=${this.isAssociated != 1 || this.readOnly}>${this.studentSelfReportMode2}
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
        ${this.showDynamic ? html`
          <div class="radio">
            <label>
              <input @click="${this.associate}" id="do-associate-dynamic-radio" name="rbcs-associate" type="radio" value="2" ?checked=${this.isAssociated == 2} ?disabled=${this.readOnly}>${this.associateLabelDyn}
            </label>
          </div>
        ` : ""}
      </div>
    `;
  }

  getAssociation() {

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
        this.selectedRubric = this.association.rubricId;
      } else {
        this.isAssociated = 0;
      }
      this.getRubrics();
    })
    .catch (error => console.error(error));
  }

  getRubrics() {

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
    .then(data => this.handleRubrics(data))
    .catch (error => console.error(error));
  }

  handleRubrics(data) {

    this.rubrics = data;

    if (this.rubrics.length) {
      if (this.isAssociated != 1) {
        this.selectedRubric = this.rubrics[0].id;
      }
    }
  }

  rubricSelected(e) {
    this.selectedRubric = e.target.value;
  }

  showRubric(e) {

    e.preventDefault();
    if (this.isAssociated == 1) {
      this.showRubricLightbox(this.selectedRubric);
    }
  }

  associate(e) {
    this.isAssociated = e.target.value;
  }

  updateStudentSelfReportInput(e) {
    if (this.isAssociated == 1) {
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
