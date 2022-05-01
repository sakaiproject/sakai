import { html } from "@assets/lit-element/lit-element.js";
import { SakaiElement } from "../../sakai-element.js";
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
      buttonTitle: { attribute: "button-title", type: String },
      label: { type: String },
    };
  }

  constructor() {

    super();

    this.buttonClass = this.classList.value;
    // This prevents duplicate styles from being added to the component
    this.classList = "";

    this.loadTranslations("sui-button").then(r => this.i18n = r);
  }

  clicked(e) {

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
      this.onclick;
    }
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
