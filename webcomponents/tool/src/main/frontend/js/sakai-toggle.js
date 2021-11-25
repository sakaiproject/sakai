import { css, html, LitElement } from "./assets/lit-element/lit-element.js";

export class SakaiToggle extends LitElement {

  static get properties() {

    return {
      onText: { attribute: "on-text", type: String },
      offText: { attribute: "off-text", type: String },
      labelledBy: { attribute: "labelled-by", type: String },
      on: { type: Boolean },
    };
  }

  toggle(e) {

    e.stopPropagation();

    this.on = !this.on;

    this.dispatchEvent(new CustomEvent("toggled", { detail: { on: this.on }, bubbles: true }));
  }

  keyup(e) {
    (e.keyCode === 13 || e.keyCode === 32) && this.toggle(e);
  }

  get checked() {
    return this.on;
  }

  render() {

    return html`
      <div id="toggle"
          role="checkbox"
          aria-checked="${this.on ? "true" : "false"}"
          aria-labelledby="${this.labelledBy}"
          @click=${this.toggle}
          @keyup=${this.keyup} tabindex="0">
        <div>${this.offText}</div>
        <div>${this.onText}</div>
      </div>
    `;
  }

  static get styles() {

    return [
      css`
        #toggle {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin: 0 0 0 auto;     // push to far right
          padding: var(--sakai-standard-space, 8px);
          background: var(--sakai-background-color-1);
          font-size: 12px;
          line-height: 22px;
          border: 1px solid var(--sakai-toggle-border-color, black);
          border-radius: 4px;
        }

        #toggle div {
          padding: 0 8px;
          pointer-events: none;
          -moz-user-select: none;
          -ms-user-select: none;
          -webkit-user-select: none;
          user-select: none;
        }

        #toggle[aria-checked="false"] :first-child,
        #toggle[aria-checked="true"] :last-child {
          background: var(--sakai-primary-color-1, lightgreen);
          color: var(--sakai-text-color-inverted);
        }

        #toggle[aria-checked="false"] :last-child,
        #toggle[aria-checked="true"] :first-child {
          color: var(--sakai-text-color-1);
        }
      `
    ];
  }
}

const tagName = "sakai-toggle";
!customElements.get(tagName) && customElements.define(tagName, SakaiToggle);
