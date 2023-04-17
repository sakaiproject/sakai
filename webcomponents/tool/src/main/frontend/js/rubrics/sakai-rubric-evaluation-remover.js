import { RubricsElement } from "./rubrics-element.js";
import { html } from "../assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage } from "./sakai-rubrics-language.js";

class SakaiRubricEvaluationRemover extends RubricsElement {

  constructor() {

    super();

    this.hasEvaluation = false;

    SakaiRubricsLanguage.loadTranslations().then(r => this.i18n = r);
  }

  static get properties() {

    return {
      entityId: { attribute: "entity-id", type: String },
      siteId: { attribute: "site-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      hasEvaluation: { attribute: false, type: Boolean },
      onlyShowIfEvaluated: { attribute: "only-show-if-evaluated", type: Boolean },
    };
  }

  attributeChangedCallback(name, oldVal, newVal) {

    super.attributeChangedCallback(name, oldVal, newVal);

    if (this.entityId && this.toolId && this.evaluatedItemId) {
      this.setHasEvaluation();
    }
  }

  render() {

    if (this.onlyShowIfEvaluated && !this.hasEvaluation) {
      return;
    }

    return html`
      <button class="btn btn-transparent text-decoration-underline"
          @click=${this._removeEvaluation}>
        ${this.i18n.remove}
      </button>
    `;
  }

  _getUrl() {
    return `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
  }

  setHasEvaluation() {

    const url = this._getUrl();
    fetch(url, { credentials: "include" }).then(r => this.hasEvaluation = r.status !== 404);
  }

  _removeEvaluation() {

    if (!confirm(this.i18n.confirm_remove_evaluation)) return;

    const url = this._getUrl();
    fetch(url, { method: "DELETE", credentials: "include" })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Failed to delete evaluation at url: ${url}. Status: ${r.status}`);
      } else {
        this.hasEvaluation = false;
        this.dispatchEvent(new CustomEvent("evaluation-removed"));
      }
    })
    .catch (error => console.error(error));
  }
}

const tagName = "sakai-rubric-evaluation-remover";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricEvaluationRemover);
