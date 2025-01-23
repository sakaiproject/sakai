import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";

export class SakaiLTIPopup extends SakaiElement {

  static properties = {

    preLaunchText: { attribute: "pre-launch-text", type: String },
    postLaunchText: { attribute: "post-launch-text", type: String },
    launchUrl: { attribute: "launch-url", type: String },
    auto: { attribute: "auto-launch", type: Boolean },
    popped: { state: true },
  };

  connectedCallback() {

    super.connectedCallback();

    this.loadTranslations("lti").then(t => {

      this._i18n = t;
      this.preLaunchText = this.preLaunchText ?? this._i18n.pre_launch_text;
      this.postLaunchText = this.postLaunchText ?? this._i18n.post_launch_text;
    });

    this.auto && this.launchPopup();
  }

  launchPopup() {

    window.open(this.launchUrl, "_blank");
    this.popped = true;
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`
      <div class="${this.auto ? "d-none" : "d-block"}">
        <button class="btn btn-primary ${this.popped ? "d-none" : "d-block"}"
            @click=${this.launchPopup}>
          ${this.preLaunchText}
        </button>
        <button class="btn btn-primary ${this.popped ? "d-block" : "d-none"}"
            disabled>
          ${this.postLaunchText}
        </button>
      </div>
    `;
  }
}
