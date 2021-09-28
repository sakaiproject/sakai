import { css, html, LitElement } from "../assets/lit-element/lit-element.js";
import "../sakai-icon.js";

class OptionsMenu extends LitElement {

  static get properties() {

    return {
      placement: { type: String },
      showing: { type: Boolean },
      transform: { attribute: false, type: String },
    };
  }

  set placement(value) {

    this._placement = value;
    switch (value) {
      case "top":
        this.transform = "translate(-50%, -130%);";
        break;
      case "right":
        this.transform = "translate(20px, -50%);";
        break;
      case "bottom":
        this.transform = "translateX(-50%);";
        break;
      case "left":
        this.transform = "translate(-100%, -50%);";
        break;
      case "bottom-left":
        this.transform = "translateX(-100%);";
        break;
      default:
    }
  }

  get placement() { return this._placement; }

  render() {

    return html`
      <slot name="trigger" @click=${e => { this.showing = !this.showing; e.stopPropagation(); }}></slot>
      <slot name="content" class="content" style="display: ${this.showing ? "block" : "none"}; transform: ${this.transform}"></slot>
    `;
  }

  static get styles() {

    return [
      css`
        .content {
          position: absolute;
          transform: translateX(-50%);
        }
      `,
    ];
  }
}

const tagName = "options-menu";
if (!customElements.get(tagName)) {
  customElements.define(tagName, OptionsMenu);
}
