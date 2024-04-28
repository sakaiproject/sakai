import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { unsafeHTML } from "lit/directives/unsafe-html.js";

export class SakaiEditor extends SakaiElement {

  static properties = {

    elementId: { attribute: "element-id", type: String },
    content: { type: String },
    active: { type: Boolean },
    delay: { type: Boolean },
    textarea: { type: Boolean },
    toolbar: { attribute: "toolbar", type: String },
    setFocus: { attribute: "set-focus", type: Boolean },
  };

  constructor() {

    super();

    this.toolbar = "Full";
    this.content = "";
    this.elementId = `editable_${Math.floor((1 + Math.random()) * 0x1000000).toString(16).substring(1)}`;
  }

  getContent() {

    if (this.textarea) {
      return this.querySelector("textarea").value;
    }
    return this.editor.getData();
  }

  setContent(text) {

    this.content = text;

    if (this.textarea) {
      this.querySelector("textarea").value = this.content;
    } else {
      this.editor.setData(this.content);
    }
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

    CKEDITOR.instances[this.elementId]?.destroy();

    if (sakai?.editor?.launch) {
      const options = {
        autosave: {
          delay: 10000000,
          messageType: "no"
        }
      };
      this.toolbar && (options.toolbar = this.toolbar);
      this.editor = sakai.editor.launch(this.elementId, options);
    } else {
      this.editor = CKEDITOR.replace(this.elementId, { toolbar: SakaiEditor.toolbars.get("basic") });
    }

    this.editor.on("change", e => this._dispatchChangedEvent(e.editor.getData()));
    this.editor.on("instanceReady", e => {

      // TODO: This is a hack. When loading this component in a bootstrap popover, we end up with a
      // double render, so two instances of either the textarea or ckeditor
      const editors = this.querySelectorAll(".cke");
      (editors.length > 1) && editors[0].remove();
      if (this.setFocus) {
        e.editor.focus();
      }
    });
  }

  _fireChanged(e) {
    this._dispatchChangedEvent(e.target.value);
  }

  _dispatchChangedEvent(content) {
    this.dispatchEvent(new CustomEvent("changed", { detail: { content }, bubbles: true }));
  }

  firstUpdated() {

    if (!this.delay && !this.textarea) {
      this.attachEditor();
      return;
    }

    if (this.textarea && this.setFocus) {
      this.querySelector("textarea").focus();
    }
  }

  updated() {

    if (this.textarea) {
      // TODO: This is a hack. When loading this component in a bootstrap popover, we end up with a
      // double render, so two instances of either the textarea or ckeditor
      const areas = this.querySelectorAll("textarea");
      (areas.length > 1) && areas[0].remove();
    }
  }

  render() {

    if (this.textarea) {
      return html `
        <textarea style="width: 100%" id="${this.elementId}" @input=${this._fireChanged} aria-label="Sakai editor textarea" tabindex="0">${this.content}</textarea>
      `;
    }

    return html `
      <div id="${this.elementId}" tabindex="0" contenteditable=${ifDefined(this.type === "inline" && this.active ? "true" : undefined)}>${unsafeHTML(this.content)}</div>
    `;
  }
}

SakaiEditor.toolbars = new Map();
SakaiEditor.toolbars.set("basic", [ { name: "document", items : [ "Source", "-", "Bold", "Italic", "Underline", "-", "Link", "Unlink", "-", "NumberedList", "BulletedList", "Blockquote" ] } ]);
