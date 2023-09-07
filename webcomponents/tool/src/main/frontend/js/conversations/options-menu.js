import { css, html, LitElement } from "../assets/lit-element/lit-element.js";

class OptionsMenu extends LitElement {

  static get properties() {

    return {
      placement: { type: String },
      _showing: { attribute: false, type: Boolean },
      _transform: { attribute: false, type: String },
    };
  }

  set placement(value) {

    this._placement = value;
    switch (value) {
      case "top":
        this._transform = "translate(-50%, -130%);";
        break;
      case "right":
        this._transform = "translate(20px, -50%);";
        break;
      case "bottom":
        this._transform = "translateX(-50%);";
        break;
      case "left":
        this._transform = "translate(-100%, -50%);";
        break;
      case "bottom-left":
        this._transform = "translateX(-100%);";
        break;
      default:
    }
  }

  get placement() { return this._placement; }

  _toggleShowing(e) {

    e.stopPropagation();
    this._showing = !this._showing;
  }

  render() {

    return html`
      <slot name="trigger" @click="${this._toggleShowing}"></slot>
      <slot name="content" class="content" style="display: ${this._showing ? "block" : "none"}; transform: ${this._transform}"></slot>
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
