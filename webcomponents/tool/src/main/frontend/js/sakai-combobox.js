import { LionCombobox } from "./assets/@lion/combobox/src/LionCombobox.js";
import { LionOption } from "./assets/@lion/listbox/src/LionOption.js";
import { css } from "./assets/lit-element/lit-element.js";

class SakaiOption extends LionOption {

  static get styles() {

    return [
      ...super.styles,
    ];
  }

  constructor() {
    super();
  }
}

if (!customElements.get("sakai-option")) {
  customElements.define("sakai-option", SakaiOption);
}

class SakaiCombobox extends LionCombobox {

  static get styles() {

    return [
      ...super.styles,
      css`
        .input-group__container {
          border: 1px solid black;
          width: 496px;
          border-radius: 4px;
          background: #F1F2F3;
        }

        :host([opened]) .input-group__container {
          border-radius: 4px 4px 0 0;
        }

        ::slotted([slot='listbox']) {
          border: 1px solid black;
          border-radius: 0 0 4px 4px;
          background: #F1F2F3;
        }

        /** Undo Popper */
        #overlay-content-node-wrapper {
          position: static !important;
          width: 496px !important;
          transform: none !important;

          /* height: 300px;
          overflow: scroll; */
        }
      `,
    ];
  }

  constructor() {

    super();

    this.addEventListener("keyup", e => {

      if (e.keyCode === 13) {
        this.dispatchEvent(new CustomEvent("option-selected", { detail: { option: e.target.value }, bubbles: true }));
        this.clear();
      }
    });

    this.showAllOnEmpty = true;
  }
}

if (!customElements.get("sakai-combobox")) {
  customElements.define("sakai-combobox", SakaiCombobox);
}
