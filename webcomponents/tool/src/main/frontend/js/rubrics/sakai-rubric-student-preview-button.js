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
      display: String,
      siteId: { attribute: "site-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      entityId: { attribute: "entity-id", type: String },
      rubricId: String,
    };
  }

  set siteId(value) {

    this._siteId = value;
    this.i18nPromise.then(r => this.initLightbox(r, value));
  }

  get siteId() { return this._siteId; }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.toolId && this.entityId) {
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
      : html`<span class="fa icon-sakai--sakai-rubrics" style="cursor: pointer;" title="${tr("preview_rubric")}" @click="${this.showRubric}" />`
      }
    `;
  }

  getRubricId() {

    const url = `/api/sites/${this.siteId}/rubric-associations/tools/${this.toolId}/items/${this.entityId}`;

    SakaiRubricsHelpers.get(url,
      { params: {toolId: this.toolId, itemId: this.entityId }})
    .then(association => {

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

const tagName = "sakai-rubric-student-preview-button";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricStudentPreviewButton);
