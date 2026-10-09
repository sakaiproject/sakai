import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { css, html, nothing } from "lit";

/** Assessment/section countdown. SAVE and END are consumed by Samigo delivery. */
export class SakaiTimerBar extends SakaiShadowElement {

  static properties = {
    text: { type: String },
    timeLimit: { attribute: "time-limit", type: Number },
    timeElapsed: { attribute: "time-elapsed", type: Number },
    syncCall: { attribute: "sync-call", type: String },
    _remaining: { state: true },
    _showProgress: { state: true },
    _closedWarning: { state: true },
  };

  constructor() {
    super();
    this.timeLimit = 0;
    this.timeElapsed = 0;
    this._remaining = 0;
    this._showProgress = true;
    this._closedWarning = false;
    this.loadTranslations("timer-bar");
  }

  connectedCallback() {
    super.connectedCallback();
    if (this.hasUpdated) { this._start(); }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this._stop();
  }

  updated(changed) {
    if (changed.has("timeLimit") || changed.has("timeElapsed")) {
      this._remaining = Math.max(this.timeLimit - this.timeElapsed, 0);
      this._deadline = Date.now() + this._remaining * 1000;
      this._saved = false;
      this._ended = false;
      this._closedWarning = false;
    }
    if ([ "timeLimit", "timeElapsed", "syncCall" ].some(name => changed.has(name))) {
      this._start();
    }
  }

  _start() {
    this._stop();
    if (!this.isConnected || !this.timeLimit || this._ended) { return; }
    this._updateRemaining();
    if (this._ended) { return; }
    this._tick = setInterval(() => { this._updateRemaining(); }, 1000);
    if (this.syncCall) {
      this._sync = setInterval(() => { this._synchronize(); }, 60000);
    }
  }

  _stop() {
    clearInterval(this._tick);
    clearInterval(this._sync);
    this._syncRequest?.abort();
  }

  _updateRemaining() {
    this._remaining = Math.max(Math.ceil((this._deadline - Date.now()) / 1000), 0);
    this._checkRemaining();
  }

  _checkRemaining() {
    if (this._remaining <= 5 && !this._saved) {
      this._saved = true;
      this._postMessage("SAVE");
    }
    if (this._remaining === 0 && !this._ended) {
      this._ended = true;
      this._stop();
      this._postMessage("END");
    }
  }

  _postMessage(msg) {
    window.parent.postMessage({ id: this.id, msg }, window.location.origin);
  }

  async _synchronize() {
    if (this._syncRequest && !this._syncRequest.signal.aborted) { return; }
    const request = new AbortController();
    this._syncRequest = request;
    try {
      const response = await fetch(this.syncCall, { signal: request.signal });
      if (!response.ok) { throw new Error(`Unable to synchronize timer: ${response.status}`); }
      const data = await response.json();
      if (!request.signal.aborted && this.isConnected && String(data.id) === this.id
          && Number.isFinite(data.timeElapsed) && data.timeElapsed > 0) {
        this._remaining = Math.max(this.timeLimit - data.timeElapsed, 0);
        this._deadline = Date.now() + this._remaining * 1000;
        this._checkRemaining();
      }
    } catch (error) {
      if (!request.signal.aborted) { console.error("Unable to synchronize assessment timer", error); }
    } finally {
      if (this._syncRequest === request) { this._syncRequest = null; }
    }
  }

  render() {
    if (!this._i18n) { return nothing; }
    const progress = this.timeLimit ? 100 * this._remaining / this.timeLimit : 0;
    const color = progress >= 50 ? "full" : progress <= 25 ? "low" : "medium";
    const time = [ Math.floor(this._remaining / 3600), Math.floor(this._remaining % 3600 / 60), this._remaining % 60 ]
      .map(value => String(value).padStart(2, "0")).join(":");
    return html`
      <div class="timer-block">
        <div id="remaining" ?hidden=${!this._showProgress}>
          ${this.text ? html`<div class="timer-title">${this.text}</div>` : nothing}
          <div class="progress-wrapper">
            <span class="time-value">${time}</span>
            <div class="progress" aria-hidden="true"><div class="progress-bar ${color}" style="width: ${progress}%"></div></div>
          </div>
        </div>
        ${!this._closedWarning && !this._showProgress && progress < 10 ? html`
          <div class="warning" role="alert">
            ${this._i18n["msg.time_over_soon"]}
            <button type="button" @click=${() => { this._closedWarning = true; }}>${this._i18n["btn.close"]}</button>
          </div>` : nothing}
        <button type="button" class="show-hide" aria-controls="remaining" aria-expanded=${this._showProgress}
            @click=${() => { this._showProgress = !this._showProgress; }}>
          <span aria-hidden="true">${this._showProgress ? "▲" : "▼"}</span>
          ${this._i18n[this._showProgress ? "msg.hide" : "msg.show"]}
          <span aria-hidden="true">${this._showProgress ? "▲" : "▼"}</span>
        </button>
      </div>`;
  }

  static styles = css`
    :host { display: block; }
    .timer-block { background: var(--sakai-primary-color-1); border-radius: 10px; padding: 0 12px 5px; }
    .timer-title { padding-top: 12px; color: var(--sakai-text-color-inverted); font-weight: bold; text-align: center; }
    .progress-wrapper { display: flex; gap: 12px; padding-top: 12px; }
    .time-value { display: flex; justify-content: center; align-items: center; min-width: 100px;
      background: var(--sakai-background-color-4); border-radius: 4px; color: var(--sakai-text-color-1); font-weight: bold; }
    .progress { flex: 1; height: 3rem; background: var(--sakai-background-color-2); border-radius: 4px; overflow: hidden; }
    .progress-bar { height: 100%; transition: width .6s ease; }
    .full { background: var(--timer-bar-full-bg, green); }
    .medium { background: var(--timer-bar-medium-bg, orange); }
    .low { background: var(--timer-bar-low-bg, red); }
    .show-hide { display: block; margin: 5px auto 0; background: transparent; border: 0;
      color: var(--sakai-text-color-inverted); font: inherit; font-weight: bold; cursor: pointer; }
    .warning { margin-top: 12px; padding: 12px; background: var(--warnBanner-bgcolor); color: var(--warnBanner-color); }
    .warning button { float: right; background: transparent; border: 0; color: inherit; text-decoration: underline; cursor: pointer; }
    button:focus-visible { outline: 2px solid var(--focus-outline-color); outline-offset: 2px; }
    [hidden] { display: none; }
    @media (prefers-reduced-motion: reduce) { .progress-bar { transition: none; } }
  `;
}
