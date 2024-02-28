import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiRubricStudentButton extends RubricsElement {

  static properties = {

    rubricId: { attribute: "rubric-id", type: Number },
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

  set siteId(value) {

    this._siteId = value;
    this.i18nLoaded.then(r => this.initLightbox(r, value));
  }

  get siteId() { return this._siteId; }

  render() {

    return html`
      <a @click=${this.showRubric} href="javascript:;" title="${this._i18n.preview_rubric}">
        <span class="si si-sakai-rubrics"></span>
      </a>
    `;
  }

  showRubric() {

    if (this.forcePreview) {
      this.showRubricLightbox(this.rubricId);
    } else {
      this.showRubricLightbox(this.rubricId, { "tool-id": this.toolId, "entity-id": this.entityId, "evaluated-item-id": this.evaluatedItemId, "evaluated-item-owner-id": this.evaluatedItemOwnerId });
    }
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
}
