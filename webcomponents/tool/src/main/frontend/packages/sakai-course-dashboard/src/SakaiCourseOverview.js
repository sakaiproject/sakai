import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import "@sakai-ui/sakai-editor/sakai-editor.js";

export class SakaiCourseOverview extends SakaiElement {

  static properties = {

    overview: { type: String },
    editing: { type: Boolean },
  };

  shouldUpdate() {
    return (typeof this.overview) !== "undefined";
  }

  render() {

    return html`
      ${this.editing ? html`
        <sakai-editor content="${this.overview}"></sakai-editor>
      ` : html`
        <div id="sakai-course-overview-display">${unsafeHTML(this.overview)}</div>
      `}
    `;
  }
}
