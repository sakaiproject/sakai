import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { rubricsApiMixin } from "./SakaiRubricsApiMixin.js";
import { SakaiRubricModal } from "./SakaiRubricModal.js";

export class SakaiRubricStudentButton extends rubricsApiMixin(RubricsElement) {

  static properties = {

    _rubricId: { state: true },
    siteId: { attribute: "site-id", type: String },
    entityId: { attribute: "entity-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
    forcePreview: { attribute: "force-preview", type: Boolean },
    instructor: { type: Boolean },
  };

  constructor() {

    super();

    this.forcePreview = false;
  }

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

    return html`
      <a @click=${this.showRubric} href="javascript:;" title="${this._i18n.preview_rubric}">
        <span class="si si-sakai-rubrics"></span>
      </a>
    `;
  }

  _setRubricId() {

    this.apiGetAssociation()
      .then(association => {

        if (association && (this.instructor || !association.parameters.hideStudentPreview)) {
          this._rubricId = association.rubricId;
        }
      })
    .catch(error => console.error(error));
  }

  showRubric() {

    const config = this.forcePreview
      ? { mode: "preview", rubricId: this._rubricId }
      : {
        mode: "evaluation",
        toolId: this.toolId,
        entityId: this.entityId,
        evaluatedItemId: this.evaluatedItemId,
        evaluatedItemOwnerId: this.evaluatedItemOwnerId,
        instructor: this.instructor,
      };

    return SakaiRubricModal.openIn(this.ownerDocument, { ...config, siteId: this.siteId })
      .catch(error => console.error(error));
  }

  releaseEvaluation() {

    let url = `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
    return fetch(url, { credentials: "include" })
    .then(r => {

      if (r.status === 200) {
        return r.json();
      }

      if (r.status !== 204) {
        throw new Error(`Network error while getting evaluation at ${url}`);
      }

      return null;
    })
    .then(async evaluation => {
      if (evaluation) {
        const evaluationStatus = 2;
        url = `/api/sites/${this.siteId}/rubric-evaluations/${evaluation.id}`;
        await fetch(url, {
          body: JSON.stringify([
            { "op": "replace", "path": "/status", "value": evaluationStatus }
          ]),
          credentials: "include",
          headers: { "Content-Type": "application/json-patch+json" },
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
}
