// TODO Review commented lines
// TODO Review need for shadown DOM
import { html } from "@assets/lit-element/lit-element.js";
import { SakaiElement } from "../../sakai-element.js";
// import styles from "./sui-button.scss";
import "../sui-icon/sui-icon";

export class SakaiUIButton extends SakaiElement {

  static get properties() {

    return {
      type: { String },
      href: { String },
      onclick: { type: String },
      buttonClass: { attribute: "button-class", type: String },
      target: { type: String },
      icon: { type: String },
      title: { type: String },
      label: { type: String },
      debug: { type: Boolean },
    };
  }

  constructor() {

    super();

    this.buttonClass = this.classList.value;
    // This prevents duplicate styles from being added to the component
    this.classList = "";
    this.debug = false;

    this.loadTranslations("sui-button").then(r => this.i18n = r);
  }

  connectedCallback() {

    super.connectedCallback();
    this.debug
      ? console.debug(`sui-button ${this.title} connectedCallback`)
      : null;
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

    // Non-href buttons
    if (this.onclick) {
      this.debug
        ? console.debug(`sui-button ${this.title} onclick`, this.onclick)
        : null;
      this.onclick;
    }
  }

  updated(changedProperties) {

    this.debug
      ? console.debug(`sui-button ${this.title} start updated`, changedProperties)
      : null;
  }

  render() {

    return html`
      <button class="sui-btn btn ${this.class ? this.class : "btn-secondary"}"
          type="${this.type ? this.type : "button"}"
          aria-label="${this.label ? this.label : this.title}"
          @click="${this.clicked}">
        ${this.icon ? html`
          <sui-icon class="sui-icon" type="${this.icon}"></sui-icon>
        ` : ""}
        ${this.title}
        ${this.target === "_blank" ? html`
          <span class="visually-hidden sr-only">${this.i18n.opens_in_new_window}</span>
        ` : ""}
      </button>
    `;
  }
}

const tagName = "sui-button";
!customElements.get(tagName) && customElements.define(tagName, SakaiUIButton);
