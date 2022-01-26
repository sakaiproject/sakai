import { html } from "../assets/@lion/core/index.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-toggle.js";
import "../sakai-editor.js";

export class SakaiConversationsSettings extends SakaiElement {

  static get properties() {

    return {
      settings: { type: Object },
      siteId: { attribute: "site-id", type: String },
      editingGuidelines: { attribute: false, type: Boolean },
    };
  }

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  setSetting(e) {

    e.stopPropagation();

    const setting = e.target.dataset.setting;
    const on = e.detail.on;

    const url = `/api/sites/${this.siteId}/conversations/settings/${setting}`;
    return fetch(url, {
      credentials: "include",
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
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
        this.editingGuidelines = false;
      }
    })
    .catch(error => console.error(error));
  }

  render() {

    return html`
      <div class="add-topic-wrapper">
        <h1>${this.i18n.general_settings}</h1>
        <div id="settings-grid">
          <div id="setting-allow-reactions">${this.i18n.allow_reactions}</div>
          <div>
            <sakai-toggle @toggled=${this.setSetting}
                data-setting="allowReactions"
                on-text="${this.i18n.on}"
                off-text="${this.i18n.off}"
                labelled-by="setting-allow-reactions"
                ?on=${this.settings.allowReactions}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-upvoting">${this.i18n.allow_upvoting}</div>
          <div>
            <sakai-toggle @toggled=${this.setSetting}
                data-setting="allowUpvoting"
                on-text="${this.i18n.on}"
                off-text="${this.i18n.off}"
                labelled-by="setting-allow-upvoting"
                ?on=${this.settings.allowUpvoting}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-anon-posting">${this.i18n.allow_anon_posting}</div>
          <div>
            <sakai-toggle @toggled=${this.setSetting}
                data-setting="allowAnonPosting"
                on-text="${this.i18n.on}"
                off-text="${this.i18n.off}"
                labelled-by="setting-allow-anon-posting"
                ?on=${this.settings.allowAnonPosting}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-bookmarking">${this.i18n.allow_bookmarking}</div>
          <div>
            <sakai-toggle @toggled=${this.setSetting}
                data-setting="allowBookmarking"
                on-text="${this.i18n.on}"
                off-text="${this.i18n.off}"
                labelled-by="setting-allow-bookmarking"
                ?on=${this.settings.allowBookmarking}>
            </sakai-toggle>
          </div>
          <div id="setting-allow-pinning">${this.i18n.allow_pinning}</div>
          <div>
            <sakai-toggle @toggled=${this.setSetting}
                data-setting="allowPinning"
                on-text="${this.i18n.on}"
                off-text="${this.i18n.off}"
                labelled-by="setting-allow-pinning"
                ?on=${this.settings.allowPinning}>
            </sakai-toggle>
          </div>
          <div if="setting-lock-site">${this.i18n.lock_this_site}</div>
          <div>
            <sakai-toggle @toggled=${this.setSetting}
                data-setting="siteLocked"
                on-text="${this.i18n.on}"
                off-text="${this.i18n.off}"
                labelled-by="setting-lock-site"
                ?on=${this.settings.siteLocked}>
            </sakai-toggle></div>
          <div id="setting-require-guidelines">${this.i18n.enable_community_guidelines}</div>
          <div>
            <sakai-toggle @toggled=${this.setSetting}
                data-setting="requireGuidelinesAgreement"
                on-text="${this.i18n.on}"
                off-text="${this.i18n.off}"
                labelled-by="setting-require-guidelines"
                ?on=${this.settings.requireGuidelinesAgreement}>
            </sakai-toggle>
          </div>
        </div>
        ${this.settings.requireGuidelinesAgreement ? html`
        <div id="settings-guidelines-block">
          <div id="settings-guidelines-preview">
            <div>${this.i18n.community_guidelines_preview_heading}</div>
            <sakai-conversations-guidelines guidelines="${this.settings.guidelines}"></sakai-conversations-guidelines>
          </div>
          ${this.editingGuidelines ? html`
          <div id="settings-guidelines-editor-block">
            <sakai-editor id="settings-guidelines-editor" content="${this.settings.guidelines}"></sakai-editor>
            <div class="act">
              <input type="button" class="active" @click=${this._saveGuidelines} value="${this.i18n.save}">
              <input type="button" class="active" @click=${() => this.editingGuidelines = false} value="${this.i18n.cancel}">
            </div>
          </div>
          ` : html`
          <div class="act">
            <input type="button" class="active" @click=${() => this.editingGuidelines = true} value="${this.i18n.edit_guidelines}">
          </div>
          `}
        </div>
        ` : ""}
      </div>
    `;
  }
}

const tagName = "sakai-conversations-settings";
!customElements.get(tagName) && customElements.define(tagName, SakaiConversationsSettings);
