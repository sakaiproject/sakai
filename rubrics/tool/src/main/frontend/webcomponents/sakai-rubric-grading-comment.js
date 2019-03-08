import {SakaiElement} from "/webcomponents/sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {tr} from "./sakai-rubrics-language.js";

export class SakaiRubricGradingComment extends SakaiElement {

  constructor() {

    super();

    this.randombit = Math.floor(Math.random() * 15001);
  }

  static get properties() {

    return {
      criterion: { type: Object },
      entityId: { attribute: "entity-id", type: String },
      evaluatedItemId: { attribute: "evaluated-item-id", type: String },
    };
  }

  set criterion(newValue) {

    var oldValue = this._criterion;
    this._criterion = newValue;
    this._criterion.comments = (newValue.comments && newValue.comments.indexOf("<p>null</p>") === 0) ? "" : newValue.comments;
    this.requestUpdate("criterion", oldValue);
  }

  get criterion() { return this._criterion; }

  firstUpdated(cp) {
    this.setupEditor();
  }

  render() {

    return html`
      <!-- edit icon -->
      <div class="comment-icon fa fa-2x fa-comments ${this.criterion.comments ? "active": ""}" @click="${this.toggleEditor}" title="${tr("criterion_comment")}"></div>

      <!-- popover -->
      <div id="criterion-editor-${this.criterion.id}" class="popover criterion-edit-popover left">
        <div class="arrow"></div>
        <div class="popover-title">
          <label class="criterion-title" for=""><sr-lang key="comment_for_criterion" values="${JSON.stringify([this.criterion.title])}">Comment for Criterion</sr-lang></label>
          <div class="buttons">
            <button class="btn btn-primary btn-xs done" @click="${this.hideTooltip}">
              <sr-lang key="done">Done</sr-lang>
            </button>
          </div>
        </div>
        <div class="popover-content form">
          <div class="form-group">
            <textarea aria-labelledby="${tr("criterion_comment")}" class="form-control" name="rbcs-${this.evaluatedItemId}-${this.entityId}-criterion-comment-${this.criterion.id}" id="criterion_${this.evaluatedItemId}_${this.entityId}_comment_${this.criterion.id}|${this.randombit}|">${this.criterion.comments}</textarea>
          </div>
        </div>
      </div>
    `;
  }

  toggleEditor(e) {

    e.stopPropagation();
    e.preventDefault();

    if (!this.classList.contains("show-tooltip")) {

      this.classList.add('show-tooltip');

      var popover = $(`#criterion-editor-${this.criterion.id}`);

      rubrics.css(popover[0], {
        'left': e.target.offsetLeft - 270 + "px",
        'top': (e.target.offsetTop + e.target.offsetHeight/2 - popover.height()/2) + "px",
      });
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
    $(`#criterion-editor-${this.criterion.id}`).hide();
    this.dispatchEvent(new CustomEvent('update-comment', {detail: {criterionId: this.criterion.id, comments: this.criterion.comments}, bubbles: true, composed: true}));
  }

  setupEditor() {

    try {
      var commentEditor = CKEDITOR.replace('criterion_' + this.evaluatedItemId + '_' + this.entityId + '_comment_' + this.criterion.id + '|' + this.randombit + '|', {
        toolbar : [
          // ['Bold', 'Italic', 'Underline', '-', 'Link', 'Unlink'],
          ['Bold', 'Italic', 'Underline'],
          ['NumberedList','BulletedList', 'Blockquote']
        ]
      });

      commentEditor.on('change', (e) => this.criterion.comments = commentEditor.getData() );
    } catch (error) {
      console.log(error);
    }
  }
}

customElements.define("sakai-rubric-grading-comment", SakaiRubricGradingComment);
