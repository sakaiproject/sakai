import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { unsafeHTML } from "/webcomponents/assets/lit-html/directives/unsafe-html.js";
import { tr } from "./sakai-rubrics-language.js";

export class SakaiRubricStudentComment extends RubricsElement {

  constructor() {

    super();

    this.randombit = Math.floor(Math.random() * 15001);
  }

  static get properties() {
    return { criterion: { type: Object } };
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
      <div tabindex="0" style="${this.criterion.comments ? "cursor: pointer;" : ""}" class="comment-icon fa fa-2x fa-comments ${this.criterion.comments ? "active" : ""}" @click=${this.toggleComment} @keypress=${this.toggleComment} title="${tr("criterion_comment_student")}"></div>

      <!-- popover -->
      <div id="criterion-comment-viewer-${this.criterion.id}-${this.randombit}" class="popover criterion-edit-popover left">
        <div class="arrow"></div>
        <div class="popover-title" style="display: flex;">
          <div style="flex: auto;">
            <span class="criterion-title">
              <sr-lang key="comment_for_criterion" values="${JSON.stringify([this.criterion.title])}" />
            </span>
          </div>
          <div class="buttons act" style="flex: 0;">
            <button class="active btn-xs done" @click="${this.toggleComment}"><sr-lang key="done" /></button>
          </div>
        </div>
        <div class="popover-content">
          <p class="renderComment">${unsafeHTML(this.criterion.comments)}</p>
        </div>
      </div>
    `;
  }

  toggleComment(e) {

    e.stopPropagation();
    e.preventDefault();

    if (!this.criterion.comments) {
      return;
    }

    var popover = $(`#criterion-comment-viewer-${this.criterion.id}-${this.randombit}`);

    if (!this.classList.contains("show-tooltip")) {

      this.classList.add('show-tooltip');
      popover[0].style.left = e.target.offsetLeft - 270 + "px";
      popover[0].style.top = e.target.offsetTop + e.target.offsetHeight / 2 - popover.height() / 2 + "px";
      popover.show();
    } else {
      this.classList.remove('show-tooltip');
      popover.hide();
    }
  }
}

customElements.define("sakai-rubric-student-comment", SakaiRubricStudentComment);
