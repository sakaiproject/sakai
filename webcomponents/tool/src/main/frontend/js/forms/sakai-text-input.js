import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
/**
 * Provides a text input field, supports the maxlength attribute displaying an errorMessage when the limit is reached by the user.
 * @example <caption>Usage:</caption>
 * <sakai-text-input value="" id="text-input-story" maxLength="5" errorMessage="You can't insert more than 5 chars"></sakai-text-input>
 *
 */

export class SakaiTextInput extends SakaiElement {
  static get properties() {
    return {
      inputId: {
        type: String
      },
      value: {
        type: String
      },
      errorMessage: {
        type: String
      },
      maxLength: {
        type: Number
      },
      styleClass: {
        type: String
      },
      displayWarning: {
        attribute: true
      }
    };
  }

  keyup(e) {
    const inputValue = e.target.value;
    this.displayWarning = this.errorMessage && inputValue && inputValue.length >= this.maxLength;
  }

  render() {
    return html`
      <input @keyup="${this.keyup}" type="text" id="${this.inputId}" name="${this.inputId}" value="${this.value}" maxlength="${this.maxLength}" class="${this.styleClass}">
      ${this.displayWarning ? html`<div class="sak-banner-warn">${this.errorMessage}</div>` : html``}
    `;
  }

}

if (!customElements.get("sakai-text-input")) {
  customElements.define("sakai-text-input", SakaiTextInput);
}
