import { css } from "./assets/lit-element/lit-element.js";
import { LionTooltip } from "./assets/@lion/tooltip/src/LionTooltip.js";

export class SakaiTooltip extends LionTooltip {

  constructor() {
    super();
  }

  static get styles() {

    return [super.styles, css`
      ::slotted(div[slot="content"]) {
        display: block;
        font-size: 16px;
        color: black;
        background-color: var(--sakai-modal-content-bg, #F4F4F4);
        border: 1px solid black;
        border-radius: 4px;
        padding: 8px;
      }
    `];
  }
}

if (!customElements.get("sakai-tooltip")) {
  customElements.define("sakai-tooltip", SakaiTooltip);
}

