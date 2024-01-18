import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";

export class SakaiRubricPdf extends RubricsElement {

  static properties = {

    rubricTitle: { attribute: "rubric-title", type: String },
    rubricId: { attribute: "rubric-id", type: String },
    siteId: { attribute: "site-id", type: String },
    toolId: { attribute: "tool-id", type: String },
    entityId: { attribute: "entity-id", type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String },

    _url: { state: true },
  };

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.siteId && this.rubricId) {

      let url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/pdf`;
      if (this.toolId && this.entityId && this.evaluatedItemId) {
        url += `?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`;
      }
      this._url = url;
    }
  }

  shouldUpdate() {
    return this._url;
  }

  render() {

    return html`
      <a role="button"
        title="${this.tr("export_title", [ this.rubricTitle ])}"
        aria-label="${this.tr("export_title", [ this.rubricTitle ])}"
        href="${ifDefined(this._url)}"
        @click=${e => e.stopPropagation()}
        class="linkStyle pdf fa fa-file-pdf-o">
      </a>
    `;
  }
}
