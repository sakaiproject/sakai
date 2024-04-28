import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiRubricEvaluationRemover extends RubricsElement {

  static properties = {

    entityId: { attribute: "entity-id", type: String },
    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    onlyShowIfEvaluated: { attribute: "only-show-if-evaluated", type: Boolean },

    _hasEvaluation: { state: true },
  };

  constructor() {

    super();

    this._hasEvaluation = false;
  }

  attributeChangedCallback(name, oldVal, newVal) {

    super.attributeChangedCallback(name, oldVal, newVal);

    if (this.entityId && this.toolId && this.evaluatedItemId) {
      this.setHasEvaluation();
    }
  }

  render() {

    if (this.onlyShowIfEvaluated && !this._hasEvaluation) return;

    return html`
      <button class="btn btn-transparent text-decoration-underline"
          @click=${this._removeEvaluation}>
        ${this._i18n.remove_label}
      </button>
    `;
  }

  _getUrl() {
    return `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
  }

  setHasEvaluation() {

    const url = this._getUrl();
    fetch(url, { credentials: "include" }).then(r => this._hasEvaluation = r.ok);
  }

  _removeEvaluation() {

    if (!confirm(this._i18n.confirm_remove_evaluation)) return;

    const url = this._getUrl();
    fetch(url, { method: "DELETE", credentials: "include" })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Failed to delete evaluation at url: ${url}. Status: ${r.status}`);
      } else {
        this._hasEvaluation = false;
        this.dispatchEvent(new CustomEvent("evaluation-removed"));
      }
    })
    .catch (error => console.error(error));
  }
}
