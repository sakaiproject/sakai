import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricsLanguage} from "./sakai-rubrics-language.js";
import {tr} from "./sakai-rubrics-language.js";

class SakaiRubricAssociation extends RubricsElement {

  constructor() {

    super();

    this.configurationOptions = [];
    this.selectedConfigOptions = {};

    this.isAssociated = false;

    this.i18nPromise = SakaiRubricsLanguage.loadTranslations();
    this.i18nPromise.then(r => this.i18n = r);
  }

  set token(newValue) {

    this.i18nPromise.then(r => this.initLightbox(newValue, r));
    this._token = "Bearer " + newValue;
  }

  get token() { return this._token; }

  static get properties() {

    return {
      token: String,
      isAssociated: Boolean,
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      stateDetails: { attribute: "state-details", type: String },
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

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.token && this.toolId) {
      this.getAssociation();
    }
  }

  shouldUpdate(changedProperties) {
    return this.i18n && this.rubrics && this.rubrics.length > 0;
  }

  render() {

    return html`
      <h4><sr-lang key="grading_rubric">Grading Rubric</sr-lang></h4>
      <div class="sakai-rubric-association form">
        ${this.readOnly ? "" : html`
          <div class="radio">
            <label>
              <input @click="${this.associate}" name="rbcs-associate" type="radio" .value="${this.dontAssociateValue}" ?checked=${!this.isAssociated} ?disabled=${this.readOnly}>${this.dontAssociateLabel}
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
              <option value="${r.id}" ?selected=${r.id === this.selectedRubric}>${r.title}</option>
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
                  <input @change="${this.updateStateDetails}" name="rbcs-config-fineTunePoints" type="checkbox" ?checked=${this.selectedConfigOptions["fineTunePoints"]} value="1" ?disabled=${!this.isAssociated || this.readOnly}>${this.fineTunePoints}
                </label>
              </div>
              <div class="checkbox">
                <label>
                  <input @change="${this.updateStateDetails}" name="rbcs-config-hideStudentPreview" type="checkbox" ?checked=${this.selectedConfigOptions["hideStudentPreview"]} value="1" ?disabled=${!this.isAssociated || this.readOnly}>${this.hideStudentPreview}
                </label>
              </div>
            </div>
        `}
        </div>
      </div>
      <input name="rbcs-state-details" type="hidden" value="${this.stateDetails}" />
    `;
  }

  getAssociation() {

    let url = `/rubrics-service/rest/rubric-associations/search/by-tool-item-ids?toolId=${this.toolId}`;
    if (this.entityId) url += `&itemId=${this.entityId}`;

    $.ajax({
      url: url,
      headers: {"authorization": this.token},
      contentType: "application/json"
    })
    .done(data => {

      var associations = data._embedded['rubric-associations'];
      this.association = associations.length ? associations[0] : false;
      if (this.association) {
        this.isAssociated = 1;
        this.selectedRubric = this.association.rubricId;
      } else {
        this.isAssociated = 0;
      }
      this.getRubrics();
    })
    .fail((jqXHR, textStatus, message) => { console.log(textStatus); console.log(message); });
  }

  getRubrics(data) {

    $.ajax({
      url: "/rubrics-service/rest/rubrics",
      headers: {"authorization": this.token},
      data: data || {}
    })
    .done(data => this.handleRubrics(data))
    .fail((jqXHR, textStatus, message) => { console.log(textStatus); console.log(message); });
  }

  handleRubrics(data) {

    this.rubrics = data._embedded.rubrics;

    if (data.page.size <= this.rubrics.length) {
      this.getRubrics({"size": this.rubrics.length+25});
      return;
    }

    if (this.rubrics.length) {
      this.selectedConfigOptions = this.association.parameters ? this.association.parameters : {};
      if (!this.isAssociated) {
        this.selectedRubric = this.rubrics[0].id;
      }
    }
  }

  rubricSelected(e) {

    this.selectedRubric = e.target.value;
    this.updateStateDetails();
  }

  updateStateDetails() {

    if (this.isAssociated == 1) {
      this.stateDetails = escape(JSON.stringify({ rubric: this.selectedRubric }));
    } else {
      this.stateDetails = "";
    }
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
