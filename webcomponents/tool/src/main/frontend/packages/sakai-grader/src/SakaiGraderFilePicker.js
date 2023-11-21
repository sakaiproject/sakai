import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";

export class SakaiGraderFilePicker extends SakaiElement {

  static properties = {

    buttonText: { attribute: "button-text", type: String },
    _files: { state: true },
  };

  constructor() {

    super();

    this._files = [];
    this.loadTranslations("file-picker").then(t => this.i18n = t);
  }

  getFiles() { return this._files; }

  hasFiles() { return this._files.length > 0; }

  removeFile(e) {

    this._files.splice(this._files.findIndex(f => f.name === e.target.dataset.name), 1);
    this.requestUpdate();
  }

  render() {

    return html`
      <button class="btn btn-link" title="${this.title}" @click=${this.pickFile}>${this.buttonText}</button>
      <div class="sakai-file-picker-list">
      ${this._files.length > 0 ? html`
        <div class="sakai-file-picker-list-title">${this.i18n.to_be_added}</div>
        ${this._files.map(f => html`
          <div class="file-row">
            <div class="file">${f.name}</div>
            <div class="file-remove">
              <a @click=${this.removeFile} data-name="${f.name}" href="javascript:;">${this.i18n.remove}</a>
            </div>
          </div>
        `)}
      ` : ""}
      </div>
    `;
  }

  pickFile() {

    const input = document.createElement("input");
    input.type = "file";
    input.multiple = true;

    input.oninput = e => {

      for (let i = 0; i < e.target.files.length; i++) {
        this._files.push(e.target.files[i]);
      }
      this.requestUpdate();
    };

    input.click();
  }

  reset() {

    this._files = [];
    this.requestUpdate();
  }
}
