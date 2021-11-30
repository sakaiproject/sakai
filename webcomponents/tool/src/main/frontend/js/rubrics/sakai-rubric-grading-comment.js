import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { tr } from "./sakai-rubrics-language.js";

export class SakaiRubricGradingComment extends RubricsElement {

  constructor() {

    super();

    this.randombit = Math.floor(Math.random() * 15001);
  }

  static get properties() {

    return {
      criterion: { type: Object },
      entityId: { attribute: "entity-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String }
    };
  }

  set criterion(newValue) {

    var oldValue = this._criterion;
    this._criterion = newValue;
    this._criterion.comments = newValue.comments && newValue.comments.indexOf("null") === 0 ? "" : newValue.comments;
    this.requestUpdate("criterion", oldValue);
  }

  get criterion() {
    return this._criterion;
  }

  render() {

    return html`
      <!-- edit icon -->
      <div tabindex="0" style="cursor: pointer;" class="comment-icon fa fa-2x fa-comments ${this.criterion.comments ? "active" : ""}" @click=${this.toggleEditor} @keypress=${this.toggleEditor} title="${tr("criterion_comment")}"></div>

      <!-- popover -->
      <div id="criterion-editor-${this.criterion.id}-${this.randombit}" class="popover criterion-edit-popover left">
        <div class="arrow"></div>
        <div class="popover-title" style="display: flex;">
          <div style="flex: auto;">
            <span class="criterion-title">
              <sr-lang key="comment_for_criterion" values="${JSON.stringify([this.criterion.title])}" />
            </span>
          </div>
          <div class="buttons act" style="flex: 0">
            <button class="active btn-xs done" @click="${this.hideTooltip}"><sr-lang key="done" /></button>
          </div>
        </div>
        <div class="popover-content form">
          <div class="form-group">
            <textarea
              aria-label="${tr("criterion_comment")}"
              class="form-control"
              name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-comment-${this.criterion.id}"
              id="criterion-${this.criterion.id}-${this.evaluatedItemId}-comment-${this.randombit}">
              ${this.criterion.comments}
            </textarea>
          </div>
        </div>
      </div>
    `;
  }

  hide() {
    this.hideTooltip();
  }

  toggleEditor(e) {

    e.stopPropagation();
    e.preventDefault();

    if (!this.classList.contains("show-tooltip")) {

      this.dispatchEvent(new CustomEvent('comment-shown'));

      this.classList.add('show-tooltip');

      var popover = $(`#criterion-editor-${this.criterion.id}-${this.randombit}`);

      popover[0].style.left = e.target.offsetLeft - 270 + "px";
      popover[0].style.top = e.target.offsetTop + e.target.offsetHeight / 2 + 20 - popover.height() / 2 + "px";

      Object.keys(CKEDITOR.instances)
        .filter(n => n.includes("criterion-")).forEach(n => CKEDITOR.instances[n].destroy(true));

      this.setupEditor();

      popover.show();
    } else {
      this.hideTooltip();
    }
  }

  hideTooltip(e) {

    if (e) {
      e.stopPropagation();
      e.preventDefault();
    }

    // hide the edit popover
    this.classList.remove("show-tooltip");
    $(`#criterion-editor-${this.criterion.id}-${this.randombit}`).hide();
    if (!this.criterion.comments) {
      this.criterion.comments = "";
    }

    this.requestUpdate();
  }

  setupEditor() {

    const editorKey = `criterion-${this.criterion.id}-${this.evaluatedItemId}-comment-${this.randombit}`;

    try {
      var commentEditor = CKEDITOR.replace(editorKey, {
        startupFocus: true,
        toolbar: [['Bold', 'Italic', 'Underline'], ['NumberedList', 'BulletedList', 'Blockquote']],
        height: 40
      });

      commentEditor.on('blur', () => {

        // When we click away from the comment editor we need to save the comment, but only if the comment has been updated
        const updatedComments = commentEditor.getData();

        if (this.criterion.comments !== updatedComments) {
          this.criterion.comments = updatedComments;
          const updateEvent = new CustomEvent('update-comment', {
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
      console.log(error);
    }
  }
}

const tagName = "sakai-rubric-grading-comment";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricGradingComment);
