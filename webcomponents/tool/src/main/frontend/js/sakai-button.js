import { css, html, LitElement } from "./assets/lit-element/lit-element.js";

export class SakaiButton extends LitElement {

  static get properties() {

    return {
      primary: { type: Boolean },
      type: { String },
      href: String,
    };
  }

  clicked() {

    if (this.href) {
      window.parent.location = this.href;
    }
  }

  focus() {
    this.shadowRoot.querySelector("button").focus();
  }

  render() {

    return html`
      <button
        class="${this.primary ? "primary" : ""} ${this.type ? this.type : ""}"
        @click=${this.clicked}
      >
        <slot>
      </button>
    `;
  }

  static get styles() {

    return css`
      button {
        text-align: center;
        border: solid;
        border-width: var(--sakai-button-border-width);
        border-color: var(--sakai-border-border-color);
        border-radius: var(--sakai-button-border-radius);
        padding: var(--sakai-button-padding);
      }
      .primary {
        background-color: var(--sakai-button-primary-bg-color, #0f4b6f);
        color: var(--sakai-primary-button-color, #FFFFFF);
      }
      .small {
        border-radius: var(--sakai-small-button-border-radius, 4px);
        padding: var(--sakai-small-button-padding, 2px);
      }
    `;
  }
}

if (!customElements.get("sakai-button")) {
  customElements.define("sakai-button", SakaiButton);
}
