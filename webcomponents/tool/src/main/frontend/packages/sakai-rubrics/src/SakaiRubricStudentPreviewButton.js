import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { rubricsApiMixin } from "./SakaiRubricsApiMixin.js";

export class SakaiRubricStudentPreviewButton extends rubricsApiMixin(RubricsElement) {

  static properties = {

    display: { type: String },
    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    entityId: { attribute: "entity-id", type: String },

    _rubricId: { state: true },
  };

  constructor() {

    super();

    this.display = "button";
  }

  set siteId(value) {

    this._siteId = value;
    this.i18nLoaded.then(r => this.initLightbox(r, value));
  }

  get siteId() { return this._siteId; }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.toolId && this.entityId) {
      this._setRubricId();
    }
  }

  shouldUpdate() {
    return this._rubricId;
  }

  render() {

    console.debug("SakaiRubricStudentPreviewButton.render");

    return html`
      ${this.display === "button" ? html`
        <h3>${this._i18n.grading_rubric}</h3>
        <button type="button" class="btn btn-link" aria-haspopup="true" @click=${this._showRubric}>${this._i18n.preview_rubric}</button>
      ` : html`
        <span class="si si-sakai-rubrics" style="cursor: pointer;" title="${this.tr("preview_rubric")}" @click="${this._showRubric}"></span>
      `}
    `;
  }

  _setRubricId() {

    this.apiGetAssociation()
      .then(association => {

        if (association && !association.parameters.hideStudentPreview && association.parameters["rbcs-associate"] != 2) {
          this._rubricId = association.rubricId;
        }
      })
      .catch(error => console.error(error));
  }

  _showRubric(e) {

    e.preventDefault();

    this.showRubricLightbox(this._rubricId);
    return false;
  }
}
