import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiRubricGradingButton extends RubricsElement {

  static properties = {

    entityId: { attribute: "entity-id", type: String },
    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    onlyShowIfEvaluated: { attribute: "only-show-if-evaluated", type: Boolean },

    _hasEvaluation: { state: true },
  };

  attributeChangedCallback(name, oldVal, newVal) {

    super.attributeChangedCallback(name, oldVal, newVal);

    if (this.entityId && this.toolId && this.evaluatedItemId) {
      this.setHasEvaluation();
    }
  }

  render() {

    if (this.onlyShowIfEvaluated && !this._hasEvaluation) {
      return;
    }

    return html`
      <button class="btn btn-transparent">
        <span class="si si-sakai-rubrics ${this._hasEvaluation ? "has-evaluation" : ""}"></span>
      </button>
    `;
  }

  setHasEvaluation() {

    const url = `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
    fetch(url).then(r => this._hasEvaluation = r.status === 200);
  }
}
