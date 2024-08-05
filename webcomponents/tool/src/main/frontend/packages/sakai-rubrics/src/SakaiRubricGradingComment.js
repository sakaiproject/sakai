import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";

export class SakaiRubricGradingComment extends RubricsElement {

  static properties = {

    criterion: { type: Object },
    entityId: { attribute: "entity-id", type: String },
    evaluatedItemId: { attribute: "evaluated-item-id", type: String }
  };

  constructor() {

    super();

    this.randombit = Math.floor(Math.random() * 15001);
  }

  hideEditor() {
    bootstrap.Dropdown.getOrCreateInstance(this.querySelector(".dropdown-menu"))?.hide();
  }

  _setupEditor() {

    const editorOptions = {
      startupFocus: true,
      versionCheck: false,
      removePlugins: "wordcount",
      height: 60,
    };

    if (sakai && sakai.editor) {
      editorOptions.toolbarSet = "BasicText";
    } else {
      editorOptions.toolbar = [ [ "Bold", "Italic", "Underline" ] ];
    }

    const editorKey = `criterion-${this.criterion.id}-${this.evaluatedItemId}-comment-${this.randombit}`;
    const editorFunction = sakai && sakai.editor ? sakai.editor.launch : CKEDITOR.replace;
    this._commentEditor = editorFunction(editorKey, editorOptions);

    this._commentEditor.focus();

    this._commentEditor.on("blur", () => {

      // When we click away from the comment editor we need to save the comment, but only if the comment has been updated
      if (this._commentEditor.checkDirty()) {
        this.criterion.comments = this._commentEditor.getData();
        const updateEvent = new CustomEvent("update-comment", {
          detail: {
            evaluatedItemId: this.evaluatedItemId,
            entityId: this.entityId,
            criterionId: this.criterion.id,
            value: this.criterion.comments
          },
          bubbles: true, composed: true });
        this.dispatchEvent(updateEvent);
        this.requestUpdate();
      }

      this.hideEditor();
    });
  }

  _updateEditor() {

    this._commentEditor?.setData(this.criterion.comments, () => {
      this._commentEditor.updateElement();
      this._commentEditor.resetDirty();
    });
  }

  firstUpdated() { this._setupEditor(); }

  updated() { this._updateEditor(); }

  render() {

    return html`
      <div class="dropdown">
        <button class="btn btn-icon"
            type="button"
            data-bs-toggle="dropdown"
            data-bs-auto-close="false"
            aria-label="${this._i18n.criterion_comment}"
            aria-expanded="false">
          <i class="bi bi-chat${this.criterion.comments ? "-fill" : ""} ${this.criterion.comments ? "active" : ""}"></i>
        </button>

        <div class="rubric-comment-dropdown dropdown-menu">
          <div class="m-2 rubric-comment-body">
            <div class="fw-bold rubric-criterion-comment-title">${this.tr("comment_for_criterion", [ this.criterion.title ])}</div>
            <div>
              <textarea
                aria-label="${this._i18n.criterion_comment}"
                class="form-control"
                name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-comment-${this.criterion.id}"
                .value=${this.criterion.comments === undefined ? null : this.criterion.comments}
                id="criterion-${this.criterion.id}-${this.evaluatedItemId}-comment-${this.randombit}">
              </textarea>
            </div>
            <div class="buttons act float-end">
              <button type="button" class="active btn-xs" @click=${this.hideEditor}>${this._i18n.done}</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }
}
