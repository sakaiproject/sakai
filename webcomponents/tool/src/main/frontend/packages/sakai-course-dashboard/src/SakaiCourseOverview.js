import { SakaiElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-editor/sakai-editor.js";

export class SakaiCourseOverview extends SakaiElement {

  static properties = {

    overview: { type: String },
    editing: { type: Boolean },
  };

  _hasOverviewContent() {
    if (!this.overview) {
      return false;
    }
    const text = String(this.overview).replace(/<[^>]*>/g, "").replace(/&nbsp;/gi, " ").trim();
    return text.length > 0;
  }

  shouldUpdate() {
    return (typeof this.overview) !== "undefined";
  }

  render() {

    if (this.editing) {
      return html`<sakai-editor content="${this.overview}"></sakai-editor>`;
    }

    if (!this._hasOverviewContent()) {
      return nothing;
    }

    return html`
      <div id="sakai-course-overview-display">${unsafeHTML(this.overview)}</div>
    `;
  }
}
