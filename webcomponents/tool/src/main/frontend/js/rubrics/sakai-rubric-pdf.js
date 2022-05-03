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
    };
  }

  render() {

    return html`
      <span class="hidden-sm hidden-xs sr-only"><sr-lang key="export_label" /></span>
      <a role="button"
        title="${tr("export_title", [this.rubricTitle])}"
        href="#0"
        class="linkStyle pdf fa fa-file-pdf-o" @click=${this.pdfRubric}>
      </a>
    `;
  }

  pdfRubric(e) {

    e.stopPropagation();
    e.preventDefault();

    let url = `/api/sites/${this.siteId}/rubrics/${this.rubricId}/pdf`;
    if (this.toolId && this.entityId && this.evaluatedItemId) {
      url += `?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`;
    }
    fetch(url, { credentials: "include" })
    .then(r => {

      if (r.ok) {
        return r.blob();
      }

      throw new Error(`Network error while getting ${url}`);
    })
    .then(blob => this.saveByteArray(this.rubricTitle, blob))
    .catch(error => console.error(error));
  }

  saveByteArray(reportName, blob) {

    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    const fileName = reportName;
    link.download = fileName;
    link.click();
  }

}
const tagName = "sakai-rubric-pdf";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricPdf);
