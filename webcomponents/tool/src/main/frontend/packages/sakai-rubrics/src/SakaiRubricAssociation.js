import { RubricsElement } from "./RubricsElement.js";
import "../sakai-rubric-student.js";
import { html, nothing } from "lit";

export class SakaiRubricAssociation extends RubricsElement {

  static properties = {

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

    _selectedRubricId: { state: true },
    _rubrics: { state: true },
  };

  constructor() {

    super();

    this.selectedConfigOptions = {};

    this.isAssociated = false;
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
    this.isAssociated = true;
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
        return r.json();
      }

      if (r.status === 404) {
        this.style.display = "none";
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

    if (this._rubrics.length && !this.isAssociated) {
      // Not associated yet, select the first rubric in the list.
      this._selectedRubricId = this._rubrics[0].id;
    }
  }

  _rubricSelected(e) {
    this._selectedRubricId = e.target.value;
  }

  _showRubric(e) {

    e.preventDefault();
    e.stopPropagation();

    if (this.isAssociated) {
      this.showRubricLightbox(this._selectedRubricId);
    }
  }

  _associate(e) {
    this.isAssociated = e.target.value == 1;
  }

  shouldUpdate() {
    return super.shouldUpdate() && this._rubrics && this._rubrics.length > 0;
  }

  render() {

    console.debug("SakaiRubricAssociation.render()");

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
                  type="radio"
                  .value="${this.dontAssociateValue}"
                  ?checked=${!this.isAssociated}
                  ?disabled=${this.readOnly}>${this.dontAssociateLabel}
            </label>
          </div>

          <div class="radio">
            <label>
              <input @click="${this._associate}" name="rbcs-associate" type="radio" class="me-1" .value="${this.associateValue}" ?checked=${this.isAssociated} ?disabled=${this.readOnly}>${this.associateLabel}
            </label>
          </div>
        `}
        <div class="rubrics-list">

          <div class="rubrics-selections">
            <select @change="${this._rubricSelected}"
                name="rbcs-rubricslist"
                aria-label="${this._i18n.rubric_selector_label}"
                class="form-control"
                ?disabled=${!this.isAssociated || this.readOnly}>
              ${this._rubrics.map(r => html`
              <option value="${r.id}" ?selected=${r.id === this._selectedRubricId}>
                ${r.title} ${r.maxPoints ? `(${r.maxPoints} ${this._i18n.points})` : ""}
              </option>
              `)}
            </select>

            <button type="button" @click="${this._showRubric}" class="btn btn-link" ?disabled=${!this.isAssociated}>
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
                      @click=${this._toggleFineTunePoints}
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
    //<button @click="${this._showRubric}" class="btn btn-link" data-bs-toggle="modal" data-bs-target="#rubric-preview" aria-controls="rubric-preview" ?disabled=${!this.isAssociated}>
  }
}
