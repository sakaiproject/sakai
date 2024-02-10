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

    new bootstrap.Popover(trigger, {
      content: popover,
      html: true,
    });
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
            .value=${this.criterion.comments}
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

  setupEditor() {

    const editorKey = `criterion-${this.criterion.id}-${this.evaluatedItemId}-comment-${this.randombit}`;

    try {
      /*
      const commentEditor = CKEDITOR.replace(editorKey, {
        startupFocus: true,
        toolbar: [ [ "Bold", "Italic", "Underline" ], [ "NumberedList", "BulletedList", "Blockquote" ] ],
        height: 40
      });
      */
      const commentEditor = sakai.editor.launch(editorKey, {
        startupFocus: true,
        versionCheck: false,
        toolbarSet: "BasicText",
        removePlugins: "wordcount",
        height: 60,
      });

      commentEditor.focus();

      commentEditor.on("blur", () => {

        // When we click away from the comment editor we need to save the comment, but only if the comment has been updated
        const updatedComments = commentEditor.getData();

        if (this.criterion.comments !== updatedComments) {
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
        }

        this.hideTooltip();
      });
    } catch (error) {
      console.error(error);
    }
  }
}
