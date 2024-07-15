import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import ClassicEditor from "./ClassicEditor.js";

export class SakaiEditor extends SakaiElement {

  static properties = {

    elementId: { attribute: "element-id", type: String },
    content: { type: String },
    active: { type: Boolean },
    delay: { type: Boolean },
    label: { type: String },
    textarea: { type: Boolean },
    toolbar: { attribute: "toolbar", type: String },
    setFocus: { attribute: "set-focus", type: Boolean },
    ck5: { type: Boolean },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this.toolbar = "full";
    this.content = "";
    this.loadTranslations("editor-wc");
  }

  connectedCallback() {

    super.connectedCallback();

    !this.elementId && (this.elementId = `editable_${Math.floor((1 + Math.random()) * 0x1000000).toString(16).substring(1)}`);

    // If neither CKEDITOR nor sakai.editor are defined, default to textarea
    if (typeof CKEDITOR === "undefined" && !sakai?.editor) {
      this.textarea = true;
    }
  }

  getContent() {

    if (this.textarea) {
      return this.querySelector("textarea").value;
    }

    return this.editor.getData();
  }

  setContent(text) {

    this.content = text;

    this.ears = "wax";

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

    if (typeof CKEDITOR !== "undefined") CKEDITOR.instances[this.elementId]?.destroy();

    if (this.ck5) {
      const toolbar = SakaiEditor.toolbars[`${this.toolbar + (this.ck5 && "_ck5")}`];

      if (this.toolbar === "full") {
        toolbar.at(-1).label = this._i18n.more_editing_options;
      }
      ClassicEditor.create(this.querySelector(`#${this.elementId}`), {
        toolbar,
        alignment: { options: [ "left", "right" ] },
        table: { contentToolbar: [ "tableRow", "tableColumn", "mergeTableCells", "tableCellProperties" ] },
        image: {
          insert: {
            // This is the default configuration, you do not need to provide
            // this configuration key if the list content and order reflects your needs.
            integrations: [ "upload", "assetManager", "url" ],
          }
        },
      }).then(editor => {

        editor.model.document.on("change:data", () => this._dispatchChangedEvent(editor.getData()));

        if (this.setFocus) {
          editor.focus();
        }
      })
      .catch(error => console.error(error));
    } else {
      CKEDITOR.instances[this.elementId]?.destroy();

      if (sakai?.editor?.launch) {
        const options = {
          autosave: {
            delay: 10000000,
            messageType: "no",
          }
        };
        this.toolbar && (options.toolbar = this.toolbar);
        this.editor = sakai.editor.launch(this.elementId, options);
      } else {
        this.editor = CKEDITOR.replace(this.elementId, { toolbar: SakaiEditor.toolbars.basic });
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
  }

  _fireChanged(e) {
    this._dispatchChangedEvent(e.target.value);
  }

  _dispatchChangedEvent(content) {
    this.dispatchEvent(new CustomEvent("changed", { detail: { content }, bubbles: true }));
  }

  shouldUpdate() {
    return this.elementId;
  }

  firstUpdated() {

    if (this.textarea && this.setFocus) {
      this.querySelector("textarea").focus();
    } else if (!this.delay && !this.textarea) {
      this.attachEditor();
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

    return html `
      <textarea style="width: 100%"
          id="${ifDefined(this.elementId)}"
          @input=${this._fireChanged}
          aria-label="${ifDefined(this.label)}"
          tabindex="0"
          .value=${this.content}>
      </textarea>
    `;
  }
}

SakaiEditor.toolbars = {
  "basic": [ "source", "-", "Bold", "Italic", "Underline", "-", "Link", "-", "NumberedList", "BulletedList", "Blockquote" ],
  "basic_ck5": [ "sourceEditing", "heading", "fontSize", "bold", "italic", "underline", "link", "numberedList", "bulletedList", "blockQuote" ],
  "full": [
    [ "A11ychecker", "Format", "Bold", "Italic", "TextColor", "fontBackgroundColor" ],
    [ "JustifyLeft", "JustifyCenter", "JustifyRight", "JustifyBlock" ],
    [ "NumberedList", "BulletedList", "Outdent", "Indent", "Link", "Unlink", "Image", "Table", "Templates", "Source" ],
    //if sakaiDropdownToolbar is true, everything defined after the / will be displayed only after toggle
    "/",
    // Uncomment the next line and comment the following to enable the default spell checker.
    // Note that it uses spellchecker.net, displays ads and sends content to remote servers without additional setup.
    //[ "Cut","Copy","Paste","PasteText","-","Print", "SpellChecker", "Scayt" ],
    [ "Cut", "Copy", "Paste", "PasteText", "Undo", "Redo", "Find", "Replace", "SelectAll", "RemoveFormat" ],
    [ "underline", "Strike", "Subscript", "Superscript" ],
    [ "BidiLtr", "BidiRtl" ],
    [ "Blockquote", "HorizontalRule", "Anchor", "Html5video", "AudioRecorder", "Smiley", "SpecialChar", "CreateDiv", "CodeSnippet" ],
    [ (sakai.editor.contentItemUrl ? "ContentItem" : undefined), (sakai.editor.enableResourceSearch ? "ResourceSearch" : undefined) ],
    [ "atd-ckeditor" ],
    "/",
    [ "Styles", "Font", "FontSize", "Print", "SakaiPreview" ],
    [ "Maximize", "ShowBlocks" ],
    [ "FMathEditor" ],
    [ "About" ]
  ],
  "full_ck5": [ "sourceEditing", "heading", "fontSize", "bold", "italic", "underline", "link", "numberedList", "bulletedList", "blockQuote",
    {
      label: "More items",
      shouldNotGroupWhenFull: true,
      items: [
        "A11checker", "fontColor", "fontBackgroundColor", "alignment",
        "outdent", "indent", "insertImage", "insertTable", "templates",
        "undo", "redo", "findAndReplace", "selectAll", "removeFormat",
        "-", "strikeThrough", "subscript", "superscript",
        "-", "horizontalLine", "htmlEmbed", "specialCharacters", "codeBlock",
        //[(sakai.editor.contentItemUrl ? "ContentItem" : undefined),(sakai.editor.enableResourceSearch ? "ResourceSearch" : undefined)],
        "-", "atd-ckeditor",
        "-", "style", "fontFamily", "SakaiPreview",
        "-", "showBlocks", "FMathEditor",
      ],
    }
  ],
};
