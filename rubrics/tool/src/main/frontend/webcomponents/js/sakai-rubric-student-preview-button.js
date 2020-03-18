import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricsLanguage, tr} from "./sakai-rubrics-language.js";
import {SakaiRubricsHelpers} from "./sakai-rubrics-helpers.js";

export class SakaiRubricStudentPreviewButton extends RubricsElement {

  constructor() {

    super();

    this.display = "button";

    this.i18nPromise = SakaiRubricsLanguage.loadTranslations();
  }

  static get properties() {

    return {
      token: String,
      display: String,
      toolId: { attribute: "tool-id", type: String },
      entityId: { attribute: "entity-id", type: String },
      rubricId: String,
    };
  }

  set token(newValue) {

    this.i18nPromise.then(r => this.initLightbox(newValue, r));
    this._token = "Bearer " + newValue;
  }

  get token() { return this._token; }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.token && this.toolId && this.entityId) {
      this.getRubricId();
    }
  }

  shouldUpdate(changedProperties) {
    return changedProperties.has("rubricId");
  }

  render() {

    return html`
      ${this.display === "button" ?
      html`<h3><sr-lang key="grading_rubric" /></h3>
      <button aria-haspopup="dialog" @click="${this.showRubric}"><sr-lang key="preview_rubric" /></button>`
      : html`<span class="fa fa-table" style="cursor: pointer;" title="${tr("preview_rubric")}" @click="${this.showRubric}" />`
      }
    `;
  }

  getRubricId() {

    SakaiRubricsHelpers.get("/rubrics-service/rest/rubric-associations/search/by-tool-item-ids", this.token, { params: {toolId: this.toolId, itemId: this.entityId }})
    .then(data => {

      const association = data._embedded["rubric-associations"][0];
      if (association && !association.parameters.hideStudentPreview) {
        this.rubricId = association.rubricId;
      }
    });
  }

  showRubric(e) {

    e.preventDefault();

    this.showRubricLightbox(this.rubricId);
    return false;
  }
}

try {
  customElements.define("sakai-rubric-student-preview-button", SakaiRubricStudentPreviewButton);
} catch (error) {
  // Can happen when using the same component in a page then a frame.
}
