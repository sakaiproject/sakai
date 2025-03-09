import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-icon/sakai-icon.js";

/**
 * Renders a user's name pronunciation player.
 *
 * Usage: <sakai-pronunciation-player user-id="SOMEUSERID"></sakai-pronunciation-player>
 */
export class SakaiPronunciationPlayer extends SakaiShadowElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    _playing: { state: true },
    _src: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("pronunciation-player");
  }

  set userId(value) {

    this._userId = value;
    this._src = `/direct/profile/${value}/pronunciation?v=${Math.floor(Math.random() * 100)}`;
  }

  get userId() { return this._userId; }

  shouldUpdate() {
    return this._i18n && this._src;
  }

  render() {

    return html`
      <div>
        <button id="play-button"
            class="transparent"
            aria-label="${this._i18n.play_pronunciation_tooltip}"
            title="${this._i18n.play_pronunciation_tooltip}"
            @click=${() => this.shadowRoot.getElementById("player").play()}>
          <sakai-icon type="${this._playing ? "volume_up" : "play"}"
              size="${this._playing ? "small" : "smallest"}">
          </sakai-icon>
        </button>
        <audio id="player"
            src="${ifDefined(this._src)}"
            @playing=${() => this._playing = true}
            @ended=${() => this._playing = false}>
        </audio>
      </div>
    `;
  }
}
