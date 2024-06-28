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


  set criterion(newValue) {

    const oldValue = this._criterion;
    this._criterion = newValue;
    this._criterion.comments = newValue.comments && newValue.comments.indexOf("null") === 0 ? "" : newValue.comments;
    this.requestUpdate("criterion", oldValue);
  }

  get criterion() { return this._criterion; }

  firstUpdated() {

    this.setupEditor();

    const trigger = this.querySelector("button.rubric-comment-trigger");
    const popover = this.querySelector("div.rubric-comment-popover");

    trigger.addEventListener("show.bs.popover", () => popover.classList.remove("d-none"));
    trigger.addEventListener("shown.bs.popover", () => this.resetEditor());

    new bootstrap.Popover(trigger, {
      content: popover,
      html: true,
    });
  }

  resetEditor() {

    console.debug("resetEditor");

    if (this.commentEditor) {
      this.commentEditor.destroy();
    }
    this.setupEditor(true);
  }

  render() {

    return html`
      <button type="button"
          class="btn icon-button rubric-comment-trigger"
          @click=${this.toggleEditor}>
        <i class="bi bi-chat${this.criterion.comments ? "-fill" : ""} ${this.criterion.comments ? "active" : ""}"></i>
      </button>

      <div class="rubric-comment-popover d-none">
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
          <button class="active btn-xs done" @click="${this.hideTooltip}">${this._i18n.done}</button>
        </div>
      </div>
    `;
  }

  hideTooltip() {
    bootstrap.Popover.getInstance(this.querySelector("button.rubric-comment-trigger"))?.hide();
  }

  setupEditor(resetContents = false) {

    const editorKey = `criterion-${this.criterion.id}-${this.evaluatedItemId}-comment-${this.randombit}`;
    const editorOptions = {
      startupFocus: true,
      versionCheck: false,
      removePlugins: "wordcount",
      height: 60,
    };
    let editorFunction;
    if (sakai && sakai.editor) {
      editorOptions.toolbarSet = "BasicText";
      editorFunction = sakai.editor.launch;
    } else {
      editorOptions.toolbar = [ [ "Bold", "Italic", "Underline" ] ] ;
      editorFunction = CKEDITOR.replace;
    }

    try {
      // Resetting the editor's contents is necessary when toggling among student submissions in Grader.
      if (resetContents) {
        // Reset the textarea's value before launching the ckeditor.
        // Otherwise, using the ckeditor's setData to assert the correct content is not always reliable (due to race conditions).
        document.getElementById(editorKey).value = (this.criterion.comments === undefined) ? null : this.criterion.comments;
      }
      this.commentEditor = editorFunction(editorKey, editorOptions);
      this.commentEditor.focus();

      this.commentEditor.on("blur", () => {

        // When we click away from the comment editor we need to save the comment, but only if the comment has been updated
        const updatedComments = this.commentEditor.getData();
        const nonEmptyComment = this.criterion.comments !== undefined || updatedComments.trim().length > 0;
        if (this.criterion.comments !== updatedComments && nonEmptyComment) {
          this.criterion.comments = updatedComments;
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

        this.hideTooltip();
      });
    } catch (error) {
      console.error(error);
    }
  }
}
