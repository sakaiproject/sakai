import { SakaiElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import "@sakai-ui/sakai-image-editor/image-editor-launcher.js";

export class SakaiCourseHeader extends SakaiElement {

  static properties = {

    site: { type: Object },
    editing: { type: Boolean },
  };

  constructor() {

    super();

    this.loadTranslations("dashboard");
  }

  _imageEdited(e) {

    this.dispatchEvent(new CustomEvent("image-edited", { detail: e.detail, bubbles: true }));
    this.querySelector("image-editor-launcher").close();
  }

  shouldUpdate() {
    return this.site;
  }

  render() {

    return html`
      <div id="container">
        <div id="image-block">
          <img id="course-image" src="${this.site.image}"></img>
          ${this.editing ? html`
          <image-editor-launcher image-url="${this.site.image}" @image-edited=${this._imageEdited}></image-editor-launcher>
          ` : nothing}
        </div>
      </div>
    `;
  }
}
