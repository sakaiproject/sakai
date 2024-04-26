import { css, html, LitElement } from "lit";
import { loadProperties } from "@sakai-ui/sakai-i18n";
import "@sakai-ui/sakai-icon";

export class SakaiFileList extends LitElement {

  static properties = {

    files: { type: Array },
    _i18n: { state: true },
  };

  constructor() {

    super();

    loadProperties("file-list").then(r => this._i18n = r);
  }

  shouldUpdate() {
    return this._i18n && this.files;
  }

  render() {

    return html`
      <div id="container">
      ${this.files.map(f => html`
        <div class="file">
          <div><sakai-icon type="${SakaiFileList.iconMapping.get(f.mimetype)}"></sakai-icon></div>
          <div><a href="${f.url}">${f.name}</a></div>
          <div>${f.size}</div>
        </div>
      `)}
      </div>
    `;
  }

  static styles = css`
    #container {
      padding: 14px;
    }
    .file {
      display: flex;
      align-items: center;
    }
      .file div {
        margin-left: 10px;
      }
  `;
}

SakaiFileList.iconMapping = new Map();
SakaiFileList.iconMapping.set("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "word");
