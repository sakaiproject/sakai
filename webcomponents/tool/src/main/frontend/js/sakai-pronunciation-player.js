import { html, LitElement } from "./assets/lit-element/lit-element.js";
import { loadProperties } from "./sakai-i18n.js";
import "./sakai-icon.js";

/**
 * Renders a user's name pronunciation player.
 *
 * Usage: <sakai-pronunciation-player user-id="SOMEUSERID"></sakai-pronunciation-player>
 */
class SakaiPronunciationPlayer extends LitElement {

  constructor() {

    super();

    loadProperties("pronunciation-player").then(i18n => this.i18n = i18n);
  }

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      i18n: { attribute: false, type: Object },
      playing: { attribute: false, type: Boolean },
      src: { attribute: false, type: String },
    };
  }

  set userId(value) {

    this._userId = value;
    this.src = `/direct/profile/${value}/pronunciation?v=${Math.floor(Math.random() * 100)}`;
  }

  get userId() { return this._userId; }

  render() {

    return html`
      <div>
        <button id="play-button"
            class="transparent"
            aria-label="${this.i18n.play_pronunciation_tooltip}"
            title="${this.i18n.play_pronunciation_tooltip}"
            @click=${() => this.shadowRoot.getElementById("player").play()}>
          <sakai-icon type="${this.playing ? "volume_up" : "play"}"
              size="${this.playing ? "small" : "smallest"}">
          </sakai-icon>
        </button>
        <audio id="player"
            src="${this.src}"
            @playing=${() => this.playing = true}
            @ended=${() => this.playing = false}>
        </audio>
      </div>
    `;
  }
}

const tagName = "sakai-pronunciation-player";
!customElements.get(tagName) && customElements.define(tagName, SakaiPronunciationPlayer);
