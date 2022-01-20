import {
  html,
  LitElement,
  unsafeCSS,
} from "../assets/lit-element/lit-element.js?version=__buildNumber__";
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
      href: { String },
      onclick: { type: String },
      class: { type: String },
      target: { type: String },
      icon: { type: String },
      title: { type: String },
      ariaLabel: { type: String },
      debug: { type: Boolean },
    };
  }

  constructor() {
    super();
    this.primary = false;
    this.type = "";
    // this.href = "";
    this.class = this.classList.value;
    this.target = "";
    this.icon = "";
    this.title = "";
    // This prevents duplicate styles from being added to the component
    this.classList = "";
    this.debug = false;
  }
  connectedCallback() {
    super.connectedCallback();
    this.debug
      ? console.log(`sui-button ${this.title} connectedCallback`)
      : null;
  }

  attributeChangedCallback(name, oldVal, newVal) {
    this.debug
      ? console.log(
          `sui-button ${this.title} attribute change: `,
          name,
          typeof newVal,
          newVal
        )
      : null;
    super.attributeChangedCallback(name, oldVal, newVal);
  }

  clicked(e) {
    this.debug ? console.log(`sui-button ${this.title} clicked`, e) : null;
    if (this.href) {
      if (this.target === "_blank") {
        window.open(this.href);
      }
      if (this.target === "") {
        window.parent.location = this.href;
      }
    }

    // Non-href buttons
    if (this.onclick) {
      this.debug
        ? console.log(`sui-button ${this.title} onclick`, this.onclick)
        : null;
      this.onclick;
    }
  }

  updated(changedProperties) {
    this.debug
      ? console.log(`sui-button ${this.title} start updated`, changedProperties)
      : null;
  }

  render() {
    return html`<button
      class="sui-btn btn ${this.class ? this.class : "btn-secondary"}"
      type="${this.type ? this.type : "button"}"
      aria-label="${this.ariaLabel ? this.ariaLabel : this.title}"
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
