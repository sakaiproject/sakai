import { html, css, LitElement } from "./assets/lit-element/lit-element.js";
import { createPopper } from "./assets/@popperjs/core/lib/popper.js";
import './sakai-icon.js';

export class SakaiOptionsMenu extends LitElement {

  static get properties() {

    return {
      placement: String,
      invokerSize: { attribute: "invoker-size", type: String },
      invokerTooltip: { attribute: "invoker-tooltip", type: String },
    };
  }

  constructor() {

    super();
    this.placement = "right";
    this.invokerSize = "small";
    this.invokerTooltip = "Open Menu";
  }

  set placement(value) {

    this._placement = value;
    if (this.popper) {
      this.popper.setOptions({ placement: value });
    }
  }

  get placement() { return this._placement; }

  firstUpdated() {

    this.content = this.shadowRoot.querySelector('slot[name="content"]').assignedNodes()[0];
    this.content.addEventListener("keydown", (e) => this._handleEscape(e));

    this.invoker = this.shadowRoot.querySelector("#invoker");
    this.invoker.addEventListener("keydown", (e) => this._handleEscape(e));

    this.setupPopper();
  }

  _handleEscape(e) {

    if (this.showing && e.key === "Escape") {
      this.content.style.display = "none";
      this.showing = false;
      this.invoker.focus();
    }
  }

  _toggle() {

    if (this.showing) {
      this.content.style.visibility = "hidden";
    } else {
      this.content.style.visibility = "visible";
      //this.content.querySelector("a").focus();
    }
    this.showing = !this.showing;
  }

  render() {

    return html`
      <a id="invoker" href="javascript:;"
          @click=${this._toggle}
          aria-haspopup="true"
          title="${this.invokerTooltip}"
          aria-label="${this.invokerTooltip}">
        <sakai-icon type="menu" size="${this.invokerSize}" />
      </a>
      <slot name="content"></slot>
    `;
  }

  setupPopper() {

    this.popper = createPopper(this.invoker, this.content, {
        placement: this.placement,
        modifiers: [
          { name: 'offset', options: { offset: [0, 8] }},
        ]
      }
    );
  }

  refresh() {
    this.setupPopper();
  }

  static get styles() {

    return css`
      ::slotted(div) {
        visibility: hidden;
        background-color: var(--sakai-options-menu-background-color, yellow);
        border-width: var(--sakai-options-menu-border-width, 1px);
        border-style: var(--sakai-options-menu-border-style, solid);
        border-color: var(--sakai-options-menu-border-color, black);
        font-family: var(--sakai-font-family, roboto, arial, sans-serif);
        padding: 5px;
      }
      #invoker {
        color: var(--sakai-options-menu-invoker-color, black);
      }
    `;
  }
}

if (!customElements.get("sakai-options-menu")) {
  customElements.define("sakai-options-menu", SakaiOptionsMenu);
}
