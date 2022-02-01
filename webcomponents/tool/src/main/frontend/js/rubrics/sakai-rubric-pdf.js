import { RubricsElement } from "./rubrics-element.js";
import { tr } from "./sakai-rubrics-language.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricPdf extends RubricsElement {
  static get properties() {
    return {
      rubricTitle: String,
      token: String,
      rubricId: String,
      toolId: String,
      entityId: String,
      evaluatedItemId: String
    };
  }

  render() {
    return html`<span class="hidden-sm hidden-xs sr-only"><sr-lang key="export_label" /></span>
            <a role="button" title="${tr("export_title", [this.rubricTitle])}" href="#0" tabindex="0" class="linkStyle pdf fa fa-file-pdf-o" @click="${this.exportPdfRubric}"></a>`;
  }

  exportPdfRubric(e) {
    e.stopPropagation();
    e.preventDefault();
    this.toolId ? this.pdfGradedRubric() : this.pdfRubric();
  }

  pdfRubric() {
    const options = {
      method: "GET",
      headers: {
        "Authorization": this.token
      }
    };
    fetch(`/rubrics-service/rest/getPdf?sourceId=${this.rubricId}`, options).then(data => data.json()).then((data) => {
      const sampleArr = this.base64ToArrayBuffer(data);
      this.saveByteArray(this.rubricTitle, sampleArr);
    }).catch(reason => {
      console.log(`Failed to get the pdf ${reason}`);
    });
  }

  pdfGradedRubric() {
    const options = {
      method: "GET",
      headers: {
        "Authorization": this.token
      }
    };
    fetch(`/rubrics-service/rest/getGradedPdf?sourceId=${this.rubricId}&toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`, options).then((data) => data.json()).then((data) => {
      const sampleArr = this.base64ToArrayBuffer(data);
      this.saveByteArray(this.rubricTitle, sampleArr);
    }).catch((reason) => {
      console.log(`Failed to get the pdf ${reason}`);
    });
  }

  base64ToArrayBuffer(base64) {
    const binaryString = window.atob(base64);
    const binaryLen = binaryString.length;
    const bytes = new Uint8Array(binaryLen);

    for (let i = 0; i < binaryLen; i++) {
      const ascii = binaryString.charCodeAt(i);
      bytes[i] = ascii;
    }

    return bytes;
  }

  saveByteArray(reportName, byte) {
    const blob = new Blob([byte], {
      type: "application/pdf"
    });
    const link = document.createElement("a");
    link.href = window.URL.createObjectURL(blob);
    const fileName = reportName;
    link.download = fileName;
    link.click();
  }

}
customElements.define("sakai-rubric-pdf", SakaiRubricPdf);
