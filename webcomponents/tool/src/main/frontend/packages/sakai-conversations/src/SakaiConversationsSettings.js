import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-toggle/sakai-toggle.js";
import "@sakai-ui/sakai-editor";
import "../sakai-conversations-guidelines.js";

export class SakaiConversationsSettings extends SakaiElement {

  static properties = {

    settings: { type: Object },
    siteId: { attribute: "site-id", type: String },
    _editingGuidelines: { state: true },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this._i18n = r);
  }

  _setSetting(e) {

    e.stopPropagation();

    const setting = e.target.dataset.setting;
    const on = e.detail.on;

    const url = `/api/sites/${this.siteId}/conversations/settings/${setting}`;
    return fetch(url, {
      credentials: "include",
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: `${on}`,
    })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Network error while toggling setting ${setting}`);
      } else {
        this.dispatchEvent(new CustomEvent("setting-updated", { detail: { setting, on }, bubbles: true }));
      }
    })
    .catch(error => {

      console.error(error);
      e.target.on = !on;
    });
  }

  _saveGuidelines() {

    const guidelines = this.querySelector("#settings-guidelines-editor")?.getContent();

    const url = `/api/sites/${this.siteId}/conversations/settings/guidelines`;
    fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: `${guidelines}`,
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while saving guidelines");
      } else {
        this.dispatchEvent(new CustomEvent("guidelines-saved", { detail: { guidelines }, bubbles: true }));
        this._editingGuidelines = false;
      }
    })
    .catch(error => console.error(error));
  }

  _startEditingGuidelines() { this._editingGuidelines = true; }

  _stopEditingGuidelines() { this._editingGuidelines = false; }

  shouldUpdate() {
    return this._i18n && this.settings;
  }

  render() {

    return html`
      <div class="add-topic-wrapper">
        <h1>${this._i18n.general_settings}</h1>
        <div id="settings-grid">
          <div id="setting-allow-reactions">${this._i18n.allow_reactions}</div>
          <div>
            <sakai-toggle @toggled=${this._setSetting}
                data-setting="allowReactions"
                text-on="${this._i18n.on}"
                text-off="${this._i18n.off}"
                labelled-by="setting-allow-reactions"
                ?on=${this.settings.allowReactions}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-upvoting">${this._i18n.allow_upvoting}</div>
          <div>
            <sakai-toggle @toggled=${this._setSetting}
                data-setting="allowUpvoting"
                text-on="${this._i18n.on}"
                text-off="${this._i18n.off}"
                labelled-by="setting-allow-upvoting"
                ?on=${this.settings.allowUpvoting}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-anon-posting">${this._i18n.allow_anon_posting}</div>
          <div>
            <sakai-toggle @toggled=${this._setSetting}
                data-setting="allowAnonPosting"
                text-on="${this._i18n.on}"
                text-off="${this._i18n.off}"
                labelled-by="setting-allow-anon-posting"
                ?on=${this.settings.allowAnonPosting}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-bookmarking">${this._i18n.allow_bookmarking}</div>
          <div>
            <sakai-toggle @toggled=${this._setSetting}
                data-setting="allowBookmarking"
                text-on="${this._i18n.on}"
                text-off="${this._i18n.off}"
                labelled-by="setting-allow-bookmarking"
                ?on=${this.settings.allowBookmarking}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-pinning">${this._i18n.allow_pinning}</div>
          <div>
            <sakai-toggle @toggled=${this._setSetting}
                data-setting="allowPinning"
                text-on="${this._i18n.on}"
                text-off="${this._i18n.off}"
                labelled-by="setting-allow-pinning"
                ?on=${this.settings.allowPinning}>
            </sakai-toggle>
          </div>
          <div id="setting-lock-site">${this._i18n.lock_this_site}</div>
          <div>
            <sakai-toggle @toggled=${this._setSetting}
                data-setting="siteLocked"
                text-on="${this._i18n.on}"
                text-off="${this._i18n.off}"
                labelled-by="setting-lock-site"
                ?on=${this.settings.siteLocked}>
            </sakai-toggle></div>
          <div id="setting-require-guidelines">${this._i18n.enable_community_guidelines}</div>
          <div>
            <sakai-toggle @toggled=${this._setSetting}
                data-setting="requireGuidelinesAgreement"
                text-on="${this._i18n.on}"
                text-off="${this._i18n.off}"
                labelled-by="setting-require-guidelines"
                ?on=${this.settings.requireGuidelinesAgreement}>
            </sakai-toggle>
          </div>
        </div>
        ${this.settings.requireGuidelinesAgreement ? html`
        <div id="settings-guidelines-block">
          <div id="settings-guidelines-preview">
            <div>${this._i18n.community_guidelines_preview_heading}</div>
            <sakai-conversations-guidelines guidelines="${this.settings.guidelines}"></sakai-conversations-guidelines>
          </div>
          ${this._editingGuidelines ? html`
          <div id="settings-guidelines-editor-block">
            <sakai-editor id="settings-guidelines-editor" content="${this.settings.guidelines}"></sakai-editor>
            <div class="act">
              <input type="button" class="active" @click=${this._saveGuidelines} value="${this._i18n.save}">
              <input type="button" class="active" @click="${this._stopEditingGuidelines}" value="${this._i18n.cancel}">
            </div>
          </div>
          ` : html`
          <div class="act">
            <input type="button" class="active" @click="${this._startEditingGuidelines}" value="${this._i18n.edit_guidelines}">
          </div>
          `}
        </div>
        ` : nothing }
      </div>
    `;
  }
}
