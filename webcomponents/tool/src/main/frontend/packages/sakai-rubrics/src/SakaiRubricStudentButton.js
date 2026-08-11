import { RubricsElement } from "./RubricsElement.js";
import { html, nothing } from "lit";
import { rubricsApiMixin } from "./SakaiRubricsApiMixin.js";

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
    this._associationLoadId = 0;
    this._associationLoadScheduled = false;
  }

  set siteId(value) {

    this._siteId = value;
    this._i18nLoaded.then(r => this.initLightbox(r, value));
  }

  get siteId() { return this._siteId; }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if ([ "site-id", "tool-id", "entity-id", "instructor" ].includes(name)) {
      this._scheduleAssociationLoad();
    }
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    if (!this._rubricId) {
      return nothing;
    }

    return html`
      <a @click=${this.showRubric} href="javascript:;" title="${this._i18n.preview_rubric}">
        <span class="si si-sakai-rubrics"></span>
      </a>
    `;
  }

  _scheduleAssociationLoad() {

    this._associationLoadId += 1;
    if (this._associationLoadScheduled) {
      return;
    }

    this._associationLoadScheduled = true;
    queueMicrotask(() => {

      this._associationLoadScheduled = false;
      const loadId = this._associationLoadId;
      this._rubricId = undefined;

      if (this.siteId && this.toolId && this.entityId) {
        this._setRubricId(loadId);
      }
    });
  }

  async _setRubricId(loadId) {

    try {
      const association = await this.apiGetAssociation();
      if (loadId !== this._associationLoadId) {
        return;
      }

      const visibleToStudent = !association?.parameters?.hideStudentPreview;
      this._rubricId = association && (this.instructor || visibleToStudent)
        ? association.rubricId
        : undefined;
    } catch (error) {
      if (loadId === this._associationLoadId) {
        console.error(error);
      }
    }
  }

  showRubric() {

    if (this.forcePreview) {
      this.showRubricLightbox(this._rubricId);
    } else {
      this.showRubricLightbox(this._rubricId, { "tool-id": this.toolId, "entity-id": this.entityId, "evaluated-item-id": this.evaluatedItemId, "evaluated-item-owner-id": this.evaluatedItemOwnerId });
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
