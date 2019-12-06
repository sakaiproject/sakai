import {SakaiElement} from "../sakai-element.js";
import {html} from "../assets/lit-element/lit-element.js";
import "../fa-icon.js";

class SakaiGraderFilePicker extends SakaiElement {

  constructor() {

    super();

    this.files = [];
  }

  static get properties() {

    return {
      buttonText: { attribute: "button-text", type: String },
      files: Array,
    };
  }

  render() {

    return html`
      <button title="${this.title}" @click=${this.pickFile}>${this.buttonText}</button>
      <div class="sakai-file-picker-list">
      ${this.files.map(f => html`<div style="overflow-wrap: break-word;">${f.name}</div>`)}
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
