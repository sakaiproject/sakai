import { html } from "@assets/lit-element/lit-element.js";
import { SakaiElement } from "../../sakai-element.js";
import "../sui-icon/sui-icon";

export class SakaiUIButton extends SakaiElement {

  static get properties() {
    return {
      type: { String },
      href: { String },
      buttonClass: { attribute: "button-class", type: String },
      target: { type: String },
      icon: { type: String },
      buttonTitle: { attribute: "button-title", type: String },
      buttonLabel: { attribtue: "button-label", type: String },
      debug: { type: Boolean },
    };
  }

  constructor() {

    super();
    this.debug = false;
    this.loadTranslations({bundle: "sui-button"}).then(r => this.i18n = r);
  }

  connectedCallback() {

    this.debug
    ? console.debug(`sui-button ${this.title} connectedCallback`)
    : null;
    super.connectedCallback();
  }

  attributeChangedCallback(name, oldVal, newVal) {

    this.debug
      ? console.debug(
          `sui-button ${this.title} attribute change: `,
          name,
          typeof newVal,
          newVal
        )
      : null;
    super.attributeChangedCallback(name, oldVal, newVal);
  }

  clicked(e) {
    this.debug ? console.debug(`sui-button ${this.title} clicked`, e) : null;
    if (this.href) {
      if (this.target === "_blank") {
        window.open(this.href);
      }
      if (!this.target) {
        window.parent.location = this.href;
      }
    }

  }

  updated(changedProperties) {

    this.debug
      ? console.debug(`sui-button ${this.title} start updated`, changedProperties)
      : null;
  }

  render() {

    return html`
      <button class="sui-btn btn ${this.buttonClass ? this.buttonClass : "btn-secondary"}"
          type="${this.type ? this.type : "button"}"
          aria-label="${this.buttonLabel ? this.buttonLabel : this.buttonTitle}"
          @click="${this.clicked}">
        ${this.icon ? html`
          <sui-icon class="sui-icon" type="${this.icon}"></sui-icon>
        ` : ""}
        ${this.buttonTitle ? this.buttonTitle : ""}
        ${this.target === "_blank" ? html`
          <span class="visually-hidden sr-only">${this.i18n.opens_in_new_window}</span>
        ` : ""}
      </button>
    `;
  }
}

const tagName = "sui-button";
!customElements.get(tagName) && customElements.define(tagName, SakaiUIButton);
