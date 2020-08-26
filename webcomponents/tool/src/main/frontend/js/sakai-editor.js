import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";
import { ifDefined } from "./assets/lit-html/directives/if-defined.js";
import { unsafeHTML } from "./assets/lit-html/directives/unsafe-html.js";

class SakaiEditor extends SakaiElement {

  static get properties() {

    return {
      elementId: { attribute: "element-id", type: String },
      type: String,
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
    this.type = "classic";
    this.elementId = "editable";
  }

  shouldUpdate(changed) {
    return (this.content || this.elementId) && typeof CKEDITOR !== "undefined";
  }

  set active(value) {

    let old = this._active;
    this._active = value;
    if (value) {
      this.attachEditor();
    } else {
      this.editor.destroy()
    }
  }

  get active() { return this._active; }

  attachEditor() {

    const element = document.getElementById(`${this.elementId}`);

    if (element) {
      if (this.type === "inline") {
        this.editor = CKEDITOR.inline(element, {title: false});
      } else {
        if (this.toolbar) {
          let toolbar = SakaiEditor.toolbars.get(this.toolbar);
          this.editor = CKEDITOR.replace(element, {toolbar: toolbar});
        } else {
          this.editor = CKEDITOR.replace(element);
        }
      }
      this.editor.on("change", (e) => {
        this.dispatchEvent(new CustomEvent("changed", { detail: { overview: e.editor.getData() }, bubbles: true }));
      });
    }
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
SakaiEditor.toolbars.set("basic", [{ name: 'document', items: ['Bold', 'Italic', 'Underline', 'NumberedList', 'BulletedList', 'Link', 'Source', '-', 'RemoveFormat'] }]);
