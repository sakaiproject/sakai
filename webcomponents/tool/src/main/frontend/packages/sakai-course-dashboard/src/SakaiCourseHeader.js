import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html, nothing } from "lit";
import "@sakai-ui/sakai-button/sakai-button.js";
import "@lion/ui/dialog.js";
import "@sakai-ui/sakai-image-editor/sakai-image-editor.js";

export class SakaiCourseHeader extends SakaiShadowElement {

  static properties = {

    site: { type: Object },
    editing: { type: Boolean },
  };

  constructor() {

    super();
    this.loadTranslations("dashboard");
  }

  set site(value) {

    const old = this._site;

    this._site = value;

    this.requestUpdate("site", old);
  }

  get site() { return this._site; }

  shouldUpdate() {
    return this.site;
  }

  imageEdited(e) {
    this.dispatchEvent(new CustomEvent("image-edited", { detail: e.detail, bubbles: true }));
  }

  render() {

    return html`
      <div id="container">
        <div id="image-block">
          <img id="course-image" src="${this.site.image}"></img>
          ${this.editing ? html`
            <lion-dialog>
              <sakai-image-editor slot="content" image-url="${this.site.image}" @image-edited=${this.imageEdited}></sakai-image-editor>
              <sakai-button slot="invoker">${this._i18n.change_this_image}</sakai-button>
            </lion-dialog>
          ` : nothing}
        </div>
      </div>
    `;
  }

  static styles = css`
    #container {
      background-color: var(--sakai-tool-bg-color);
    }
    #course-image {
      max-width: 100%;
    }
  `;
}
