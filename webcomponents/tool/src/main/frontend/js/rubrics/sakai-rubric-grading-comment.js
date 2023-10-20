import { RubricsElement } from "./rubrics-element.js";
import { html } from "../assets/lit-element/lit-element.js";
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

    const oldValue = this._criterion;
    this._criterion = newValue;
    this._criterion.comments = newValue.comments && newValue.comments.indexOf("null") === 0 ? "" : newValue.comments;
    this.requestUpdate("criterion", oldValue);
  }

  get criterion() {
    return this._criterion;
  }

  _toggleModal() {

    const el = document.getElementById(`criterion-comment-${this.criterion.id}`);
    bootstrap.Modal.getOrCreateInstance(el).toggle();
  }

  setupEditor() {

    const editorKey = `criterion-${this.criterion.id}-${this.evaluatedItemId}-comment-${this.randombit}`;

    this.editor = sakai.editor.launch(editorKey, {
      autosave: {
        delay: 10000000,
        messageType: "no"
      },
      startupFocus: true,
      toolbarSet: "Basic",
    });

    this.editor.on('blur', () => {

      // When we click away from the comment editor we need to save the comment, but only if the comment has been updated
      const updatedComments = this.editor.getData();

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
    });
  }

  firstUpdated() {

    // We need to move the comment modal onto the body, or it ends up displaying under the mask and
    // can't be interacted with. Bit of a hack ...
    const el = document.getElementById(`criterion-comment-${this.criterion.id}`);
    el.remove();
    document.body.append(el);

    this.setupEditor();
    el.addEventListener("shown.bs.modal", () => this.editor.focus());
  }

  render() {

    return html`
      <button type="button"
          class="btn btn-link"
          aria-label="${tr("criterion_comment")}"
          title="${tr("criterion_comment")}"
          @click=${this._toggleModal}>
        <i class="comment-icon bi bi-chat-text${this.criterion.comments ? "-fill" : ""} ${this.criterion.comments ? "active" : ""}"></i>
      </button>

      <div id="criterion-comment-${this.criterion.id}"
          class="modal fade"
          tabindex="-1"
          aria-hidden="true"
          aria-labelledby="criterion-comment-${this.criterion.id}-label">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 id="criterion-comment-${this.criterion.id}-label" class="modal-title">${tr("comment_for_criterion", [ this.criterion.title ])}</h5>
              <button type="button"
                  class="btn-close"
                  data-bs-dismiss="modal"
                  title="${tr("close_comment_modal_tooltip")}"
                  aria-label="${tr("close_comment_modal_tooltip")}">
              </button>
            </div>
            <div class="modal-body">
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
            <div class="modal-footer">
              <button class="btn btn-primary"
                  data-bs-dismiss="modal"
                  aria-label="${tr("close_comment_modal_tooltip")}"
                  title="${tr("close_comment_modal_tooltip")}">
                ${tr("done")}
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }
}

const tagName = "sakai-rubric-grading-comment";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricGradingComment);
