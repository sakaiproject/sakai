import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";
import { ifDefined } from "./assets/lit-html/directives/if-defined.js";
import { unsafeHTML } from "./assets/lit-html/directives/unsafe-html.js";

class SakaiEditor extends SakaiElement {

  static get properties() {

    return {
      elementId: { attribute: "element-id", type: String },
      debug: { type: Boolean },
      content: {attribute: "content", type: String},
      active: { type: Boolean },
      delay: { type: Boolean },
      textarea: { type: Boolean },
      toolbar: String,
      setFocus: { attribute: "set-focus", type: Boolean },
    };
  }

  constructor() {

    super();

    if (this.debug) console.debug("Sakai Editor constructor");
    this.content = "";
    this.elementId = `editable_${Math.floor(Math.random() * 20) + 1}`;
  }

  getContent() {

    if (this.textarea) {
      return this.querySelector(`#${this.elementId}`).value;
    }
    return this.editor.getData();
  }

  setContent(text) {
    this.content = text;
    if (this.textarea) {
      return this.querySelector(`#${this.elementId}`).value = this.content;
    }
    return this.editor.setData(this.content);
  }

  clear() {

    if (this.textarea) {
      this.querySelector(`#${this.elementId}`).value = "";
    } else {
      this.editor.setData("");
    }
  }

  shouldUpdate() {
    return (this.content || this.elementId);
  }

  set active(value) {

    this._active = value;
    if (!this.textarea) {
      if (value) {
        this.attachEditor();
      } else {
        this.editor.destroy();
      }
    }
  }

  get active() { return this._active; }

  attachEditor() {

    if (CKEDITOR.instances[this.elementId]) {
      CKEDITOR.instances[this.elementId].destroy();
    }

    if (sakai?.editor?.launch) {
      this.editor = sakai.editor.launch(this.elementId, { autosave: { delay: 10000000, messageType: "no" } });
    } else {
      this.editor = CKEDITOR.replace(this.elementId, {toolbar: SakaiEditor.toolbars.get("basic")});
    }

    this.editor.on("change", (e) => {
      this.dispatchEvent(new CustomEvent("changed", { detail: { content: e.editor.getData() }, bubbles: true }));
    });

    if (this.setFocus) {
      this.editor.on("instanceReady", e => {
        e.editor.focus();
      });
    }
  }

  firstUpdated(changed) {

    super.firstUpdated(changed);

    if (!this.delay && !this.textarea) {
      this.attachEditor();
    }
  }

  render() {

    if (this.textarea) {
      return html `
        <textarea style="width: 100%" id="${this.elementId}" aria-label="Sakai editor textarea" tabindex="0">${unsafeHTML(this.content)}</textarea>
      `;
    }

    return html `
      <div id="${this.elementId}" tabindex="0" contenteditable=${ifDefined(this.type === "inline" && this.active ? "true" : undefined)}>${unsafeHTML(this.content)}</div>
    `;
  }
}

const tagName = "sakai-editor";
!customElements.get(tagName) && customElements.define(tagName, SakaiEditor);

SakaiEditor.toolbars = new Map();
SakaiEditor.toolbars.set("basic", [{ name: 'document', items : ['Source', '-', 'Bold', 'Italic', 'Underline', '-', 'Link', 'Unlink', '-', 'NumberedList', 'BulletedList', 'Blockquote']}]);
