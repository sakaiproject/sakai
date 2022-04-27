import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";

class SakaiRubricAssociation extends RubricsElement {

  constructor() {

    super();

    this.selectedConfigOptions = {};

    this.isAssociated = false;

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
      isAssociated: Boolean,
      entityId: { attribute: "entity-id", type: String },
      siteId: { attribute: "site-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      selectedRubric: { type: String },
      dontAssociateLabel: { attribute: "dont-associate-label", type: String },
      associateLabel: { attribute: "associate-label", type: String },
      dontAssociateValue: { attribute: "dont-associate-value", type: Number },
      associateValue: { attribute: "associate-value", type: Number },
      fineTunePoints: { attribute: "fine-tune-points", type: String },
      hideStudentPreview: { attribute: "hide-student-preview", type: String },
      rubrics: { type: Array },
      readOnly: { attribute: "read-only", type: Boolean },
    };
  }

  set association(value) {

    this._association = value;
    this.selectedRubric = value.rubricId;
    this.selectedConfigOptions = value.parameters ? value.parameters : {};
    this.isAssociated = true;
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
                  type="radio"
                  .value="${this.dontAssociateValue}"
                  ?checked=${!this.isAssociated}
                  ?disabled=${this.readOnly}>${this.dontAssociateLabel}
            </label>
          </div>

          <div class="radio">
            <label>
              <input @click="${this.associate}" name="rbcs-associate" type="radio" .value="${this.associateValue}" ?checked=${this.isAssociated} ?disabled=${this.readOnly}>${this.associateLabel}
            </label>
          </div>
        `}
        <div class="rubrics-list">

          <div class="rubrics-selections">
            <select @change="${this.rubricSelected}" name="rbcs-rubricslist" aria-label="${tr("rubric_selector_label")}" class="form-control" ?disabled=${!this.isAssociated || this.readOnly}>
            ${this.rubrics.map(r => html`
              <option value="${r.id}" ?selected=${r.id == this.selectedRubric}>${r.title}</option>
            `)}
            </select>

            <button @click="${this.showRubric}" class="btn btn-link" ?disabled=${!this.isAssociated}>
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
                      ?disabled=${!this.isAssociated || this.readOnly}>${this.fineTunePoints}
                </label>
              </div>
              <div class="checkbox">
                <label>
                  <input name="rbcs-config-hideStudentPreview" type="checkbox" ?checked=${this.selectedConfigOptions.hideStudentPreview} value="1" ?disabled=${!this.isAssociated || this.readOnly}>${this.hideStudentPreview}
                </label>
              </div>
            </div>
        `}
        </div>
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
        this.isAssociated = 1;
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
      //this.selectedConfigOptions = this.association.parameters ? this.association.parameters : {};
      if (!this.isAssociated) {
        this.selectedRubric = this.rubrics[0].id;
      }
    }
  }

  rubricSelected(e) {
    this.selectedRubric = e.target.value;
  }

  showRubric(e) {

    e.preventDefault();
    if (this.isAssociated) {
      this.showRubricLightbox(this.selectedRubric);
    }
  }

  associate(e) {
    this.isAssociated = e.target.value == 1;
  }
}

customElements.define("sakai-rubric-association", SakaiRubricAssociation);
