import {SakaiElement} from "../sakai-element.js";
import {html} from "../assets/lit-element/lit-element.js";

class SakaiGraderFilePicker extends SakaiElement {

  constructor() {

    super();

    this.files = [];
    this.loadTranslations("file-picker").then(t => this.i18n = t);
  }

  static get properties() {

    return {
      buttonText: { attribute: "button-text", type: String },
      files: { type: Array }
    };
  }

  removeFile(e) {

    this.files.splice(this.files.findIndex(f => f.name === e.target.dataset.name), 1);
    this.requestUpdate();
  }

  render() {

    return html`
      <button title="${this.title}" @click=${this.pickFile}>${this.buttonText}</button>
      <div class="sakai-file-picker-list">
      ${this.files.length > 0 ? html`
        <div class="sakai-file-picker-list-title">${this.i18n["to_be_added"]}</div>
        ${this.files.map(f => html`
          <div class="file-row">
            <div class="file">${f.name}</div>
            <div class="file-remove">
              <a @click=${this.removeFile} data-name="${f.name}" href="javascript:;">${this.i18n["remove"]}</a>
            </div>
          </div>
        `)}
      ` : ""}
      </div>
    `;
  }

  pickFile() {

    var input = document.createElement("input");
    input.type = "file";
    input.multiple = true;

    input.oninput = e => {

      for (let i = 0; i < e.target.files.length; i++) {
        this.files.push(e.target.files[i]);
      }
      this.requestUpdate();
    };

    input.click();
  }

  reset() {

    this.files = [];
    this.requestUpdate();
  }
}

customElements.define("sakai-grader-file-picker", SakaiGraderFilePicker);
