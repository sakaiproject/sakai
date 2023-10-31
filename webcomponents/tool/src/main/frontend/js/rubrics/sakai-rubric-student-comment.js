import { RubricsElement } from "./rubrics-element.js";
import { html } from "/webcomponents/assets/lit-element/lit-element.js";
import { SakaiRubricsLanguage } from "./sakai-rubrics-language.js";

export class SakaiRubricStudentComment extends RubricsElement {

  static get properties() {

    return {
      criterion: { type: Object },
    };
  }

  constructor() {

    super();

    SakaiRubricsLanguage.loadTranslations().then(i18n => { console.log(i18n); this.i18n = i18n; this.requestUpdate(); });
  }

  set criterion(value) {

    const oldValue = this._criterion;
    this._criterion = value;
    this._criterion.comments = value.comments && value.comments.indexOf("null") === 0 ? "" : value.comments;
    this.triggerId = `criterion-comment-${value.id}-trigger`;

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
    return this.i18n;
  }

  render() {

    return html`
      <button id="${this.triggerId}"
          type="button"
          tabindex="0"
          data-bs-toggle="popover"
          data-bs-html="true"
          data-bs-content="${this.criterion.comments}"
          data-bs-title="${this.criterion.title}"
          aria-label="${this.i18n.criterion_comment_student}"
          class="btn btn-transparent">
        <i class="bi bi-chat${this.criterion.comments ? "-fill" : ""} ${this.criterion.comments ? "active" : ""}"></i>
      </button>
    `;
  }
}

const tagName = "sakai-rubric-student-comment";
!customElements.get(tagName) && customElements.define(tagName, SakaiRubricStudentComment);
