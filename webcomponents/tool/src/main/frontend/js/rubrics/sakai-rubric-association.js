import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";
import { rubricsApiMixin } from "./sakai-rubrics-api-mixin.js";

class SakaiRubricAssociation extends rubricsApiMixin(RubricsElement) {

  constructor() {

    super();

    this.selectedConfigOptions = {};

    this.isAssociated = false;

    this.i18nPromise = SakaiRubricsLanguage.loadTranslations();
    this.i18nPromise.then(r => this.i18n = r);
    this.maxPoints = [];
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
      maxPoints: { type: Array },
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

    const optionItems = [];
    const rubrics = Array.from(this.rubrics);
    const maxPoints = Array.from(this.maxPoints);
    for (let i = 0; i < this.rubrics.length; i++) {
      const r = rubrics.pop();
      const m = maxPoints.pop();
      if (! m) {
        optionItems.push(html`<option value="${r.id}" ?selected=${r.id === this.selectedRubric}>${r.title}</option>`);
      } else {
        optionItems.push(html`<option value="${r.id}" ?selected=${r.id === this.selectedRubric}>${r.title} (${m} <sr-lang key="points">Points</sr-lang>)</option>`);
      }
    }

    return html`
      <h4><sr-lang key="grading_rubric">Grading Rubric</sr-lang></h4>
      <div class="sak-banner-warn"><small><sr-lang key="rubric_points_warning">A rubric's point value should match the maximum point value of the activity or question to grade.</sr-lang></small></div>
      <div class="sakai-rubric-association form">
        ${this.readOnly ? "" : html`
          <div class="radio">
            <label class="label-rubrics">
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
            <label class="label-rubrics">
              <input @click="${this.associate}" name="rbcs-associate" type="radio" .value="${this.associateValue}" ?checked=${this.isAssociated} ?disabled=${this.readOnly}>${this.associateLabel}
            </label>
          </div>
        `}
        <div class="rubrics-list">

          <div class="rubrics-selections">
            <select @change="${this.rubricSelected}" name="rbcs-rubricslist" aria-label="${tr("rubric_selector_label")}" class="form-control" ?disabled=${!this.isAssociated || this.readOnly}>
            ${optionItems}
            </select>

            <button @click="${this.showRubric}" class="btn btn-link" ?disabled=${!this.isAssociated}>
              <sr-lang key="preview_rubric">Preview Rubric</sr-lang>
            </button>
          </div>

          ${this.readOnly ? "" : html`
            <div class="rubric-options">
              <div class="checkbox">
                <label class="label-rubrics">
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
                <label class="label-rubrics">
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

      if (r.status === 404) {
        this.style.display = "none";
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

  async calcMaxPoints(rubric, i) {
    let result;

    try {
      result = await this.apiGetRubric(rubric.id);
      let maxPoints = 0;
      result.criteria.filter((c) => !this.isCriterionGroup(c)).forEach((c) => {
        const pointrange = this.getHighLow(c.ratings);

        if (rubric.weighted && pointrange.high > 0) {
          maxPoints += (pointrange.high * (c.weight / 100));
        }
        else {
          maxPoints += pointrange.high;
        }
      });

      this.maxPoints[i] = (maxPoints - Math.floor(maxPoints)) === 0 ? maxPoints.toFixed(0) : maxPoints.toFixed(2);

    } catch (error) {
      console.error(error);
    }
  }

  handleRubrics(data) {

    this.rubrics = data.slice().filter( (rubric) => rubric.draft === false);

    if (this.rubrics.length) {
      //this.selectedConfigOptions = this.association.parameters ? this.association.parameters : {};
      if (!this.isAssociated) {
        this.selectedRubric = this.rubrics[0].id;
      }
      const rubrics = Array.from(this.rubrics);
      for (let i = 0; i < this.rubrics.length; i++) {
        this.calcMaxPoints(rubrics.pop(), i);
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
