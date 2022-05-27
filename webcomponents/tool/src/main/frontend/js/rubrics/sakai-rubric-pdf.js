import { RubricsElement } from "./rubrics-element.js";
import { tr } from "./sakai-rubrics-language.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricPdf extends RubricsElement {

  static get properties() {

    return {
      rubricTitle: { attribute: "rubric-title", type: String },
      rubricId: { attribute: "rubric-id", type: String },
      siteId: { attribute: "site-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      entityId: { attribute: "entity-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
      url: { attribute: false, type: String },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.siteId && this.rubricId) {

      let url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/pdf`;
      if (this.toolId && this.entityId && this.evaluatedItemId) {
        url += `?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`;
      }
      this.url = url;
    }
  }

  render() {

    return html`
      <span class="hidden-sm hidden-xs sr-only"><sr-lang key="export_label" /></span>
      <a role="button"
        title="${tr("export_title", [this.rubricTitle])}"
        href="${this.url}"
        @click=${e => e.stopPropagation()}
        class="linkStyle pdf fa fa-file-pdf-o">
      </a>
    `;
  }
}
const tagName = "sakai-rubric-pdf";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricPdf);
