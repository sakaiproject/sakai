import {SakaiElement} from "./sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import "/webcomponents/fa-icon.js";

class SakaiMaximiseButton extends SakaiElement {

  constructor() {

    super();

    this.loadTranslations("maximise-button").then(t => this.i18n = t);
  }

  static get properties() {

    return {
      fullScreen: { attribute: "full-screen", type: Boolean },
      i18n: Object,
    };
  }

  shouldUpdate(changed) {
    return this.i18n;
  }

  render() {

    return html`
      ${this.fullScreen ?
      html`
          <a class="Mrphs-toolTitleNav__link" href="javascript;" title="${this.i18n["normal_view"]}" @click=${this.minimise}>
            <fa-icon i-class="fas compress-arrows-alt" path-prefix="/webcomponents/assets" />
          </a>`
      : html`
          <a class="Mrphs-toolTitleNav__link" href="javascript;" title="${this.i18n["fullscreen_view"]}" @click=${this.maximise}>
            <fa-icon i-class="fas expand-arrows-alt" path-prefix="/webcomponents/assets" />
          </a>`
      }
    `;
  }

  maximise(e) {

    e.preventDefault();

    portal.maximiseTool();

    this.dispatchEvent(new CustomEvent("maximise-tool", {bubbles: true, composed: true}));
    this.fullScreen = true;
  }

  minimise(e) {

    if (e) {
      e.preventDefault();
    }

    portal.minimiseTool();

    this.dispatchEvent(new CustomEvent("minimise-tool", {bubbles: true, composed: true}));
    this.fullScreen = false;
  }

  setMinimised() {
    this.fullScreen = false;
  }

  setMaximised() {
    this.fullScreen = true;
  }
}

customElements.define("sakai-maximise-button", SakaiMaximiseButton);
