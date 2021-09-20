import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
/**
 * Provides a text area input field, supports the maxlength attribute displaying an errorMessage when the limit is reached by the user.
 * @example <caption>Usage:</caption>
 * <sakai-text-area inputId="text-area-story" maxLength="20" cols="2" rows="45" errorMessage="You can't insert more than 20 chars"></sakai-text-input>
 *
 */

export class SakaiTextArea extends SakaiElement {
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
      cols: {
        type: Number
      },
      rows: {
        type: Number
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
      <textarea @keyup="${this.keyup}" id="${this.inputId}" name="${this.inputId}" rows="${this.rows}" cols="${this.cols}" maxlength="${this.maxLength}">${this.value}</textarea>
      ${this.displayWarning ? html`<div class="sak-banner-warn">${this.errorMessage}</div>` : html``}
    `;
  }

}

if (!customElements.get("sakai-text-area")) {
  customElements.define("sakai-text-area", SakaiTextArea);
}
