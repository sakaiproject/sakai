import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";

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
export class SakaiDocumentViewer extends SakaiElement {

  static properties = {

    preview: { type: Object },
    content: { type: Object },
    height: { type: String },

    _documentMarkup: { state: true },
    _noMargins: { state: true },
  };

  constructor() {

    super();

    this._documentMarkup = "";
    this.height = "600px";

    this.loadTranslations("document-viewer").then(t => {

      this._i18n = t;
      this.documentFailureMessage = `<div>${this._i18n.failed_to_load_document}</div>`;
      this.documentTooBigMessage = `<div>${this._i18n.document_too_big}</div>`;
      this.documentNotSupportedMessage = `<div>${this._i18n.document_not_supported}</div>`;
    });
  }

  set preview(newValue) {

    this._preview = newValue;
    this.loadDocumentMarkup(newValue);
  }

  get preview() { return this._preview; }

  shouldUpdate() {
    return this._i18n;
  }

  renderWithoutBorders() {

    return html`
      <div class="preview-inner ${this._noMargins ? "nomargins" : ""}">
        ${unsafeHTML(this._documentMarkup)}
      </div>
    `;
  }

  renderWithBorders() {

    return html`
      <div class="preview-outer">
        <div class="preview-middle">
          ${this.renderWithoutBorders()}
        </div>
      </div>
    `;
  }

  render() {

    return html`
      <div class="document-link">${this._i18n.viewing}: <a href="/access${this.content.ref}" target="_blank" rel="noopener">${this.content.name}</a></div>
      ${this.withBorders ? this.renderWithBorders() : this.renderWithoutBorders()}
    `;
  }

  loadDocumentMarkup(preview) {

    let ref = preview.ref;
    const type = preview.type;

    this._noMargins = false;

    this.withBorders = false;

    if (type === "application/pdf") {
      this._noMargins = true;
      // Let PDFJS handle this. We can just literally use the viewer, like Firefox and Chrome do.
      this._documentMarkup = `<iframe src="/library/webjars/pdf-js/5.3.31/web/viewer.html?file=/access/${encodeURIComponent(ref)}" width="100%" height="${this.height}" />`;
    } else if (type === "application/vnd.oasis.opendocument.presentation"
                || type === "application/vnd.oasis.opendocument.text") {
      this._noMargins = true;
      this._documentMarkup = `<iframe src="/library/webjars/viewerjs/0.5.9#/access${ref}" width="100%" height="${this.height}" />`;
    } else if (type.includes("image/")) {
      this._documentMarkup = `<img src="/access/${ref}" />`;
    } else if (type.includes("video/")) {
      this._documentMarkup = `<video controls playsinline><source src='/access/${ref}' type='${type}'></video>`;
    } else {
      this.withBorders = true;
      const contentIndex = ref.indexOf("/content/");
      ref = contentIndex >= 0 ? ref.substring(contentIndex + 8) : ref;

      fetch(`/direct/content/${portal.siteId}/htmlForRef.json?ref=${ref}`,
        { cache: "no-cache", contentcredentials: "same-origin" })
        .then(r => {

          if (r.ok) {
            return r.json();
          }

          this._documentMarkup = this.documentFailureMessage;
          throw new Error("Failed to load preview");
        })
        .then(data => {

          switch (data.status) {
            case "CONVERSION_OK":
              this._documentMarkup = data.content;
              break;
            case "CONVERSION_TOO_BIG":
              this._documentMarkup = this.documentTooBigMessage;
              break;
            case "CONVERSION_NOT_SUPPORTED":
              this._documentMarkup = this.documentNotSupportedMessage;
              break;
            default:
              this._documentMarkup = this.documentFailureMessage;
          }
        })
        .catch (error => console.error(error));
    }
  }
}
