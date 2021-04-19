import {SakaiElement} from "../sakai-element.js";
import {html} from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";

class Sakai4Editor extends SakaiElement {

  static get properties() {

    return {
      elementId: { attribute: "element-id", type: String },
      type: String,
      debug: { type: Boolean },
      content: String,
    };
  }

  constructor() {

    super();

    console.log("balls");
    if (this.debug) console.debug("Sakai Editor constructor");
    this.type = "classic";
    this.elementId = "editable";
  }

  shouldUpdate() {
    return this.elementId;
  }

  render() {

    return html `
      <div id="${this.editorId}" contenteditable=${ifDefined(this.type === "inline" ? "true" : undefined)}></div>
    `;
  }

  firstUpdated() {

    const element = document.getElementById(`${this.elementId}`);

    if (element) {
      if (this.type === "inline") {
        CKEDITOR.inline(element);
      } else {
        CKEDITOR.replace(element);
      }
    }
  }
}

if (!customElements.get("sakai-4-editor")) {
  customElements.define("sakai-4-editor", Sakai4Editor);
}
