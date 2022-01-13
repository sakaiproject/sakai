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
        padding: var(--sakai-button-padding);
        border: 1px solid var(--button-border-color);
        border-radius: var(--sakai-button-border-radius);
        background: var(--button-background);
        font-family: "Open Sans",sans-serif;
        font-size: 1em;
        font-weight: 400;
        line-height: 18px;
        color: var(--button-text-color);
        text-decoration: none;
        text-transform: none;
        cursor: pointer;
        -moz-appearance: none;
        -webkit-appearance: none;
        box-shadow: var(--button-shadow);
      }
      button:hover,
      button:focus {
        color: var(--button-hover-text-color);
        text-decoration: none;
        background: var(--button-hover-background);
        border-color: var(--button-hover-border-color);
        box-shadow: var(--button-hover-shadow);
      }
      button:focus {
        outline: none;
        box-shadow: 0px 0px 0px 3px var(--focus-outline-color);
      }
      button:active {
        outline: 0;
        color: var(--button-active-text-color);
        text-decoration: none;
        background: var(--button-active-background);
        border-color: var(--button-active-border-color);
        box-shadow: var(--button-active-shadow);
      }
      .primary {
        background-color: var(--button-primary-background, #0f4b6f);
        color: var(--primary-text-color, #FFFFFF);
        border: 1px solid var(--button-primary-border-color);
        background: var(--button-primary-background);
        font-weight: 600;
        color: var(--button-primary-text-color);
        text-decoration: none;
        text-transform: none;
        cursor: pointer;
        box-shadow: var(--button-primary-shadow);
      }
      .primary:hover,
      .primary:focus {
        color: var(--button-primary-hover-text-color);
        text-decoration: none;
        background: var(--button-primary-hover-background);
        border-color: var(--button-primary-hover-border-color);
        box-shadow: var(--button-primary-hover-shadow);
      }
      .primary:focus {
        outline: none;
        box-shadow: 0px 0px 0px 3px var(--focus-outline-color);
      }
      .primary:active {
        outline: 0;
        color: var(--button-primary-active-text-color);
        text-decoration: none;
        background: var(--button-primary-active-background);
        border-color: var(--button-primary-active-border-color);
        box-shadow: var(--button-primary-active-shadow);
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
