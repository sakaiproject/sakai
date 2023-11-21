import { LitElement, css, html } from "lit";
import "@sakai-ui/sakai-button/sakai-button.js";
import "@lion/dialog/define";
import "@sakai-ui/sakai-image-editor/sakai-image-editor.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiCourseHeader extends LitElement {

  static properties = {

    site: { type: Object },
    editing: { type: Boolean },
    _i18n: { state: true },
  };

  constructor() {

    super();
    loadProperties("dashboard").then(r => this._i18n = r);
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
          ` : ""}
        </div>
      </div>
    `;
  }

  static styles = css`
    #container {
      display: flex;
      justify-content: flex-start;
      background-color: var(--sakai-tool-bg-color);
    }
      #image-block {
        flex: 0;
        margin-right: 26px;
      }
      #title-and-status-block {
        flex: 1;
      }
        #title-and-edit-block {
          margin-bottom: 10px;
          display: flex;
        }
          #title-block {
            flex: 2;
          }
          #title {
            font-size: var(--sakai-course-dashboard-title-font-size);
            display: inline-block;
          }
          #edit-block {
            flex: 2;
            text-align: right;
          }
            #edit {
              display: inline-block;
            }
            #cancel {
              display: inline-block;
            }
            #save {
              display: inline-block;
            }
        #programme {
          margin-bottom: 30px;
          font-size: var(--sakai-course-dashboard-programme-font-size);
          font-weight: var(--sakai-course-dashboard-programme-font-weight);
        }
    #course-image {
      max-width: 509px;
      max-height: 293px;
    }
  `;
}
