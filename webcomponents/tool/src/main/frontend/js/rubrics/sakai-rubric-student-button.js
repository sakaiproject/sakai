import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage, tr } from "./sakai-rubrics-language.js";
import { rubricsApiMixin } from "./sakai-rubrics-api-mixin.js";

class SakaiRubricStudentButton extends rubricsApiMixin(RubricsElement) {

  constructor() {

    super();

    this.hidden = true;
    this.instructor = false;
    this.forcePreview = false;
    this.i18nPromise = SakaiRubricsLanguage.loadTranslations();
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.toolId && this.entityId) {
      this.setupHidden();
    }
  }

  static get properties() {

    return {
      rubricId: { attribute: "rubric-id", type: Number },
      siteId: { attribute: "site-id", type: String },
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      hidden: { attribute: false, type: Boolean },
      instructor: Boolean,
      forcePreview: { attribute: "force-preview", type: Boolean },
      dontCheckAssociation: { attribute: "dont-check-association", type: Boolean },
    };
  }

  set siteId(value) {

    this._siteId = value;
    this.i18nPromise.then(r => this.initLightbox(r, value));
  }

  get siteId() { return this._siteId; }

  render() {

    return html`${this.hidden ? "" : html`
      <a @click=${this.showRubric} href="javascript:;" title="${tr("preview_rubric")}"><span class="si si-sakai-rubrics" /></a>
    `}`;
  }

  showRubric() {

    if (this.forcePreview) {
      this.showRubricLightbox(this.rubricId);
    } else {
      this.showRubricLightbox(this.rubricId, { "tool-id": this.toolId, "entity-id": this.entityId, "evaluated-item-id": this.evaluatedItemId });
    }
  }

  releaseEvaluation() {

    let url = `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
    return fetch(url, { credentials: "include" })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while getting evaluation");
    })
    .then(async data => {

      const evaluation = data._embedded.evaluations[0];
      if (evaluation) {
        evaluation.status = 2;
        url = `/api/sites/${this.siteId}/rubric-evaluations/${evaluation.id}`;
        await fetch(url, {
          body: JSON.stringify(evaluation),
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          method: "PATCH",
        })
        .then(r => {

          if (!r.ok) {
            throw new Error("Failed to release evaluation");
          }
        });
      }
    })
    .catch (error => console.error(error));
  }

  setupHidden() {

    if (this.dontCheckAssociation) {
      this.hidden = !this.instructor;
    } else {
      this.apiGetAssociation()
        .then(association => {
          this.hidden = association.parameters.hideStudentPreview && !this.instructor;
        })
        .catch(error => console.error(error));
    }
  }
}

const tagName = "sakai-rubric-student-button";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricStudentButton);
