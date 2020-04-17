import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {unsafeHTML} from "/webcomponents/assets/lit-html/directives/unsafe-html.js";

/**
 * Loads a document from Sakai content hosting from the supplied ref attribute. Ref is a Sakai entity reference.
 *
 * Formats currently supported:
 * DOCX
 * ODP
 * ODT
 * PDF
 *
 * PDFs are opened with PDF.js, the same plugin used natively by Chrome and Firefox. ODP (slides) are displayed using
 * ViewerJS. DOCX and ODT are converted to html on the server and retrieved vi a Fetch call. ViewerJS and PDF.js loads
 * happen in an iframe. You can specify the height of that with the height attribute. Light dom is in use, so you can
 * style this from the usual Sakai SASS build.
 *
 * @example <caption>Usage:</caption>
 * <sakai-document-viewer height="400px" ref="/content/attachment/8c563fb1-6bf8-4e01-9e25-8881f4dc35e2/Assignments/77377d3d-6deb-4c78-b69c-2821c6d0602d/nndr 2015.odp"></sakai-document-viewer>
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
class SakaiDocumentViewer extends SakaiElement {

  constructor() {

    super();

    this.documentMarkup = "";
    this.height = "600px";

    this.loadTranslations("document-viewer").then(t => {

      this.i18n = t;
      this.documentFailureMessage = `<div>${this.i18n["failed_to_load_document"]}</div>`;
    });
  }

  static get properties() {

    return {
      ref: String,
      height: String,
      //INTERNAL
      documentMarkup: String,
      i18n: Object,
      nomargins: Boolean,
    };
  }

  set ref(newValue) {

    this._ref = newValue;
    this.loadDocumentMarkup(newValue);
  }

  get ref() { return this._ref; }

  render() {

    return html`
      <div class="document-link">${this.i18n["viewing"]}: <a href="/access${this.ref}" target="_blank" rel="noopener">${this.fileNameFromRef(this.ref)}</a></div>
      <div class="preview-outer">
        <div class="preview-middle">
          <div class="preview-inner ${this.nomargins ? "nomargins" : ""}" >
            ${unsafeHTML(this.documentMarkup)}
          </div>
        </div>
      </div>
    `;
  }

  fileNameFromRef(ref) { return ref.substring(ref.lastIndexOf("\/") + 1); }

  loadDocumentMarkup(documentRef) {

    this.nomargins = false;

    if (documentRef.endsWith("\.pdf") || documentRef.endsWith("\.PDF")) {
      this.nomargins = true;
      // Let PDFJS handle this. We can just literally use the viewer, like Firefox and Chrome do.
      this.documentMarkup = `<iframe src="/library/webjars/pdf-js/2.3.200/web/viewer.html?file=/access/${encodeURIComponent(documentRef)}" width="100%" height="${this.height}" />`;
    } else if (documentRef.endsWith("\.odp") || documentRef.endsWith("\.ODP")) {
      this.nomargins = true;
      this.documentMarkup = `<iframe src="/library/webjars/viewerjs/0.5.8/ViewerJS#/access${documentRef}" width="100%" height="${this.height}" />`;
    } else {
      let contentIndex = documentRef.indexOf("\/content\/");
      const ref = contentIndex >= 0 ? documentRef.substring(contentIndex + 8) : documentRef;

      fetch(`/direct/content/${portal.siteId}/htmlForRef.html?ref=${ref}`,
              {cache: "no-cache", credentials: "same-origin"})
        .then(r => {

          if (!r.ok) {
            this.documentMarkup = this.documentFailureMessage;
            throw new Error("Failed to load preview");
          } else {
            return r.text();
          }
        })
        .then(html => {

          if (html) {
            this.documentMarkup = html;
          } else {
            this.documentMarkup = this.documentFailureMessage;
          }
        })
        .catch (error => console.error(error));
    }
  }
}

customElements.define("sakai-document-viewer", SakaiDocumentViewer);
