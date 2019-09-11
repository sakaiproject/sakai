import {SakaiElement} from "./sakai-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import "/webcomponents/fa-icon.js";

class SakaiMaximiseButton extends SakaiElement {

  constructor() {

    super();

    this.loadTranslations({bundle: "maximise-button"}).then(t => { this.i18n = t; this.requestUpdate(); });
  }

  static get properties() {

    return {
      fullScreen: { attribute: "full-screen", type: Boolean },
    };
  }

  shouldUpdate(changed) {
    return this.i18n;
  }

  render() {

    return html`
      <div style="display: inline-block;">
      ${this.fullScreen ?
      html`
          <a href="javascript;" title="${this.i18n["normal_view"]}" @click=${this.minimise}>
            <fa-icon size="1.3em" class="fas compress-arrows-alt" path-prefix="/webcomponents/assets" />
          </a>`
      : html`
          <a href="javascript;" title="${this.i18n["fullscreen_view"]}" @click=${this.maximise}>
            <fa-icon size="1.3em" class="fas expand-arrows-alt" path-prefix="/webcomponents/assets" />
          </a>`
      }
      </div>
    `;
  }

  maximise(e) {

    e.preventDefault();

    this.dispatchEvent(new CustomEvent("maximise-tool", {bubbles: true, composed: true}));
    this.fullScreen = true;
  }

  minimise(e) {

    e.preventDefault();

    this.dispatchEvent(new CustomEvent("minimise-tool", {bubbles: true, composed: true}));
    this.fullScreen = false;
  }

  setMinimised() {
    this.fullScreen = false;
  }
}

customElements.define("sakai-maximise-button", SakaiMaximiseButton);
