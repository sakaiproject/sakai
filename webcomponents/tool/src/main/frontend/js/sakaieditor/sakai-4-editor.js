import {SakaiElement} from "../sakai-element.js";
import {html} from "../assets/lit-element/lit-element.js";

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

  shouldUpdate(changed) {
    return this.elementId;
  }

  render() {

    console.log(this.content);

    return html `
      <div id="${this.editorId}" contenteditable=${isDefined(this.type === "inline" ? "true" : undefined)}></div>
    `;
  }

  firstUpdated(changedProperties) {

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
