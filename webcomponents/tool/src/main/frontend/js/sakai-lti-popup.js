import { SakaiElement } from "./sakai-element.js";
import { html } from "./assets/lit-html/lit-html.js";

class SakaiLTIPopup extends SakaiElement {
  constructor() {
    super();
    const randomId = Math.floor(Math.random() * 1000000);
    this.randomId = randomId;
    this.preLaunchText = null;
    this.postLaunchText = null;
    this.loadTranslations("lti").then(t => {
      this.i18n = t;
      if ( this.preLaunchText == null ) this.preLaunchText = this.i18n.pre_launch_text;
      if ( this.postLaunchText == null ) this.postLaunchText = this.i18n.post_launch_text;
    } );
  }

  static get properties() {
    return {
      preLaunchText: {
        attribute: "pre-launch-text",
        type: String
      },
      postLaunchText: {
        attribute: "post-launch-text",
        type: String
      },
      launchUrl: {
        attribute: "launch-url",
        type: String
      }
    };
  }

  launchPopup() {
    window.open(this.launchUrl, '_blank');
    document.getElementById('sakai-lti-popup-' + this.randomId).style.display = 'none';
    document.getElementById('sakai-lti-popup-hidden-' + this.randomId).style.display = 'block';
    return false;
  }

  shouldUpdate() {
    return this.preLaunchText && this.postLaunchText && this.launchUrl;
  }

  render() {
    return html`
<div>
<button id="sakai-lti-popup-${this.randomId}" class="btn btn-primary" @click="${this.launchPopup}" >
${this.preLaunchText}
</button>
<button id="sakai-lti-popup-hidden-${this.randomId}"
    class="btn btn-primary" role="button"
    disabled style="display:none;"
>
${this.postLaunchText}
</button>
</div>
    `;
  }

}

if (!customElements.get("sakai-lti-popup")) {
  customElements.define("sakai-lti-popup", SakaiLTIPopup);
}

