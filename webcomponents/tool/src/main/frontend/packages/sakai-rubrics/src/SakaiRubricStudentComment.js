import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";

export class SakaiRubricStudentComment extends RubricsElement {

  static properties = { criterion: { type: Object } };

  set criterion(value) {

    const oldValue = this._criterion;
    this._criterion = value;
    this._criterion.comments = value.comments && value.comments.indexOf("null") === 0 ? "" : value.comments;

    this.triggerId = `criterion-comment-${value.id}-trigger${Math.floor(Math.random() * 90 + 10)}`;

    this.requestUpdate("criterion", oldValue);
    this.updateComplete.then(() => {

      const triggerEl = this.querySelector("button");
      bootstrap.Popover.getInstance(triggerEl)?.hide();
      new bootstrap.Popover(triggerEl);
    });
  }

  get criterion() {
    return this._criterion;
  }

  handleClose() {
    bootstrap.Popover.getInstance(document.getElementById(this.triggerId))?.hide();
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`
      <button id="${ifDefined(this.triggerId)}"
          type="button"
          tabindex="0"
          data-bs-toggle="popover"
          data-bs-html="true"
          data-bs-content="${this.criterion.comments}"
          data-bs-title="${this.criterion.title}"
          aria-label="${this._i18n.criterion_comment_student}"
          class="btn btn-transparent">
        <i class="bi bi-chat${this.criterion.comments ? "-fill" : ""} ${this.criterion.comments ? "active" : ""}"></i>
      </button>
    `;
  }
}
