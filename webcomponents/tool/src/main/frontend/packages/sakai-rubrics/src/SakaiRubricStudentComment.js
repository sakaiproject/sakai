import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { unsafeHTML } from "lit/directives/unsafe-html.js";

export class SakaiRubricStudentComment extends RubricsElement {

  static properties = { criterion: { type: Object } };

  set criterion(value) {

    const oldValue = this._criterion;
    this._criterion = value;
    this._criterion.comments = value.comments && value.comments.indexOf("null") === 0 ? "" : value.comments;

    this.triggerId = `criterion-comment-${value.id}-trigger${Math.floor(Math.random() * 90 + 10)}`;

    this.requestUpdate("criterion", oldValue);
  }

  get criterion() {
    return this._criterion;
  }

  handleClose() {
    this.hideComment();
  }

  shouldUpdate() {
    return this._i18n;
  }

  hideComment() {
    bootstrap.Dropdown.getOrCreateInstance(this.querySelector(".dropdown-menu"))?.hide();
  }

  render() {

    return html`
      <div class="dropdown" id="${ifDefined(this.triggerId)}">
        <button class="btn btn-icon"
            type="button"
            data-bs-toggle="dropdown"
            data-bs-auto-close="false"
            aria-label="${this._i18n.criterion_comment_student}"
            aria-expanded="false">
          <i class="bi bi-chat${this.criterion.comments ? "-fill" : ""} ${this.criterion.comments ? "active" : ""}"></i>
        </button>

        <div class="rubric-comment-dropdown dropdown-menu">
          <div class="m-2 rubric-comment-body">
            <div class="rubric-criterion-comment-title">${this.tr("comment_for_criterion", [ this.criterion.title ])}</div>
            <div>${unsafeHTML(this.criterion.comments)}</div>
            <div class="buttons act float-end">
              <button type="button" class="active btn-xs" @click=${this.hideComment}>${this._i18n.done}</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }
}
