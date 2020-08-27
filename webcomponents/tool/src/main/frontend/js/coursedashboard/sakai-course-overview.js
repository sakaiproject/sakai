import { SakaiElement } from "../sakai-element.js";
import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import "../sakai-editor.js";

class SakaiCourseOverview extends SakaiElement {

  static get properties() {

    return {
      overview: { type: String },
      editing: { type: Boolean },
      editorShowing: Boolean,
    };
  }

  shouldUpdate(changed) {
    return this.overview;
  }

  render() {

    return html`
      ${this.editing && !this.editorShowing ? html`
        <sakai-editor content="${this.overview}" />
      ` : html`
        <div>${unsafeHTML(this.overview)}</div>
      `}
    `;
  }
}

if (!customElements.get("sakai-course-overview")) {
  customElements.define("sakai-course-overview", SakaiCourseOverview);
}
