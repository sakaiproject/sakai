import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";
import { ifDefined } from "./assets/lit-html/directives/if-defined.js";
import { unsafeHTML } from "./assets/lit-html/directives/unsafe-html.js";

class SakaiEditor extends SakaiElement {

  static get properties() {

    return {
      elementId: { attribute: "element-id", type: String },
      debug: { type: Boolean },
      content: String,
      active: { type: Boolean },
      delay: { type: Boolean },
      toolbar: String,
    };
  }

  constructor() {

    super();

    if (this.debug) console.debug("Sakai Editor constructor");
    this.content = "";
    this.elementId = "editable";
  }

  shouldUpdate() {
    return (this.content || this.elementId) && typeof CKEDITOR !== "undefined";
  }

  set active(value) {

    this._active = value;
    if (value) {
      this.attachEditor();
    } else {
      this.editor.destroy()
    }
  }

  get active() { return this._active; }

  attachEditor() {

    if (CKEDITOR.instances[this.elementId]) {
      CKEDITOR.instances[this.elementId].destroy();
    }

    if (sakai?.editor?.launch) {
      this.editor = sakai.editor.launch(this.elementId);
    } else {
      this.editor = CKEDITOR.replace(this.elementId, {toolbar: SakaiEditor.toolbars.get("basic")});
    }
    this.editor.on("change", (e) => {
      this.dispatchEvent(new CustomEvent("changed", { detail: { overview: e.editor.getData() }, bubbles: true }));
    });
  }

  firstUpdated(changed) {

    super.firstUpdated(changed);

    if (!this.delay) {
      this.attachEditor();
    }
  }

  render() {

    return html `
      <div id="${this.elementId}" tabindex="0" contenteditable=${ifDefined(this.type === "inline" && this.active ? "true" : undefined)}>${unsafeHTML(this.content)}</div>
    `;
  }
}

if (!customElements.get("sakai-editor")) {
  customElements.define("sakai-editor", SakaiEditor);
}

SakaiEditor.toolbars = new Map();
SakaiEditor.toolbars.set("basic", [{ name: 'document', items : ['Source', '-', 'Bold', 'Italic', 'Underline', '-', 'Link', 'Unlink', '-', 'NumberedList','BulletedList', 'Blockquote']}]);
