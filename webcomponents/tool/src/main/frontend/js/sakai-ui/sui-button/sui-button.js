import {
  html,
  LitElement,
  unsafeCSS,
} from "../assets/lit-element/lit-element.js?version=__buildNumber__";
import { Button } from "../assets/bootstrap/dist/js/bootstrap.esm.min.js?version=__buildNumber__";
import styles from "./sui-button.scss";
import "../sui-icon/sui-icon";
export class SakaiUIButton extends LitElement {
  createRenderRoot() {
    // Render to the real dom, not the shadow. We can now pull
    // in Sakai's css and js. This makes any this, and any subclasses,
    // custom elements, not full blown web components
    return this;
  }
  static get properties() {
    return {
      primary: { type: Boolean },
      type: { String },
      href: String,
      isToggle: { type: Boolean },
      class: { type: String },
      target: { type: String },
      icon: { type: String },
      title: { type: String },
    };
  }

  constructor() {
    super();
    this.primary = false;
    this.type = "";
    this.href = "";
    this.isToggle = false;
    this.class = this.classList.value;
    this.target = "";
    this.icon = "";
    this.title = "";

    // This prevents duplicate styles from being added to the component
    this.classList = "";
  }

  clicked() {
    if (this.href) {
      if (this.target === "_blank") {
        window.open(this.href);
      }
      if (this.target === "") {
        window.parent.location = this.href;
      }
    }

    // Non-href buttons
  }

  toggled() {
    const btsButton = new Button(this);
    btsButton.toggle();
  }

  render() {
    return html`<button
      class="sui-btn btn ${this.class ? this.class : "btn-secondary"}"
      type="${this.type ? this.type : "button"}"
      @click="${this.clicked}"
    >
      ${this.icon
        ? html`<sui-icon class="sui-icon" type="${this.icon}"></sui-icon>`
        : ""}${this.title}${this.target === "_blank"
        ? html`<span class="visually-hidden sr-only">Opens in new window</span>`
        : ""}
    </button>`;
  }

  static get styles() {
    return [unsafeCSS(styles)];
  }
}

if (!customElements.get("sui-button")) {
  customElements.define("sui-button", SakaiUIButton);
}
