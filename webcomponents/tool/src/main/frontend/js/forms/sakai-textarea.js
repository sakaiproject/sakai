import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
/**
 * Provides a textarea input field, supports the maxlength attribute displaying an errorMessage when the limit is reached by the user.
 * @example <caption>Usage:</caption>
 * <sakai-textarea input-id="user-bio-input" name="userBio" maxlength="20" cols="2" rows="45" error-message="You can't insert more than 20 chars"></sakai-text-input>
 *
 */

export class SakaiTextArea extends SakaiElement {

  static get properties() {

    return {
      inputId: { attribute: "input-id", type: String },
      name: { type: String },
      value: { type: String },
      errorMessage: { attribute: "error-message", type: String },
      maxLength: { type: Number },
      cols: { type: Number },
      rows: { type: Number },
      displayWarning: { attribute: false },
    };
  }

  keyup(e) {

    const inputValue = e.target.value;
    this.displayWarning = this.errorMessage && inputValue && inputValue.length >= this.maxLength;
  }

  render() {

    return html`
      <textarea @keyup=${this.keyup}
          id="${this.inputId}"
          name="${this.name}"
          rows="${this.rows}"
          cols="${this.cols}"
          maxlength="${this.maxLength}">
        ${this.value}
      </textarea>
      ${this.displayWarning ? html`
      <div class="sak-banner-warn">${this.errorMessage}</div>
      ` : html``}
    `;
  }
}

const tagName = "sakai-textarea";
!customElements.get(tagName) && customElements.define(tagName, SakaiTextArea);
