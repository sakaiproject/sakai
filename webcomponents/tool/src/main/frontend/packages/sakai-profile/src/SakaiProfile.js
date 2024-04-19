import { css, html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { confirmConnection, ignoreConnection, removeConnection, requestConnection } from "./sakai-connection-utils.js";
import { getUserId } from "@sakai-ui/sakai-portal-utils";
import "@sakai-ui/sakai-pronunciation-player/sakai-pronunciation-player.js";

/**
 * Renders a user's Sakai profile.
 *
 * Usage: <sakai-profile user-id="SOMEUSERID"></sakai-profile>
 */
export class SakaiProfile extends SakaiShadowElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    siteId: { attribute: "site-id", type: String },
    tool: { type: String },

    _profile: { state: true },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("profile").then(i18n => this._i18n = i18n);
  }

  fetchProfileData() {

    const url = `/api/users/${this.userId}/profile`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok && r.status !== 204) return r.json();

        if (r.status === 204) {
          this._profile = {};
        } else {
          throw new Error(`Network error while getting user profile from ${url}`);
        }
      })
      .then(profile => this._profile = profile)
      .catch(error => console.error(error));
  }

  playPronunciation() {
    this.shadowRoot.getElementById("pronunciation-player").play();
  }

  _requestConnection() {

    requestConnection(this.userId)
      .then(() => {

        this._profile.connectionStatus = 1;
        this.requestUpdate();
      })
      .catch(error => console.error(error));
  }

  _confirmConnection() {

    confirmConnection(this.userId)
      .then(() => {

        this._profile.connectionStatus = 3;
        this.requestUpdate();
      })
      .catch(error => console.error(error));
  }

  _removeConnection() {

    removeConnection(this.userId)
      .then(() => {

        this._profile.connectionStatus = 0;
        this.requestUpdate();
      })
      .catch(error => console.error(error));
  }

  _ignoreConnection() {

    ignoreConnection(this.userId)
      .then(() => {

        this._profile.connectionStatus = 0;
        this.requestUpdate();
      })
      .catch(error => console.error(error));
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    if (!this._profile) return html`${this._i18n.loading}`;

    return html`
      <div class="container">
        <div class="header">
          <div class="photo" style="background-image:url(/direct/profile/${this.userId}/image/thumb)">
          </div>
          <div>
            <div class="name">${this._profile.name}</div>
            ${this._profile.role ? html`
            <div class="role">${this._profile.role}</div>
            ` : nothing}
            <div class="pronouns">${this._profile.pronouns}</div>
          </div>
        </div>
        <div class="body">
          ${this._profile.pronunciation || this._profile.pronunciationRecordingUrl ? html`
          <div class="label">${this._i18n.name_pronunciation}</div>
          <div class="field pronunciation">
            ${this._profile.pronunciation ? html`
            <div>${this._profile.pronunciation}</div>
            ` : nothing}
            ${this._profile.hasPronunciationRecording ? html`
            <sakai-pronunciation-player user-id="${this.userId}"></sakai-pronunciation-player>
            ` : nothing}
          </div>
          ` : ""}
          ${this._profile.email ? html`
          <div class="label">${this._i18n.email}</div>
          <div class="field">${this._profile.email}</div>
          ` : nothing}
          ${this._profile.studentNumber ? html`
          <div class="label">${this._i18n.student_number}</div>
          <div class="field">${this._profile.studentNumber}</div>
          ` : nothing}
          ${this._profile.url ? html`
          <div class="label url"><a href="${this._profile.url}">${this._i18n.view_full_profile}</a></div>
          ` : nothing}

          ${getUserId() !== this.userId ? html`

            <div class="label">${this._i18n.connection}</div>

            ${this._profile.connectionStatus === 0 ? html`
              <div>
                <button type="button" class="btn btn-secondary" @click=${this._requestConnection}>${this._i18n.connect}</button>
              </div>
            ` : nothing}
            ${this._profile.connectionStatus === 1 ? html`
              <div>${this._i18n.connection_requested}</div>
            ` : nothing}
            ${this._profile.connectionStatus === 2 ? html`
              <div>
                <button type="button" class="btn btn-secondary" @click=${this._confirmConnection}>${this._i18n.accept_connection}</button>
              </div>
              <div>
                <button type="button" class="btn btn-secondary" @click=${this._ignoreConnection}>${this._i18n.ignore_connection}</button>
              </div>
            ` : nothing}
            ${this._profile.connectionStatus === 3 ? html`
              <div>
                <button type="button" class="btn btn-secondary" @click=${this._removeConnection}>${this._i18n.remove_connection}</button>
              </div>
            ` : nothing}
          ` : nothing}
        </div>
      </div>
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
      .container {
        padding: 14px;
        color: var(--sakai-text-color-1);
      }
      .header {
        display: flex;
        margin-bottom: var(--sakai-profile-header-margin-bottom, 10px);
        padding-bottom: var(--sakai-profile-header-padding-bottom, 10px);
        border-bottom: var(--sakai-profile-border-bottom, 1px solid #E0E0E0);
      }
      .header > div:nth-child(2) > div {
        margin-top: 5px;
      }
      .photo {
        min-width: var(--sakai-profile-photo-size, 64px);
        max-width: var(--sakai-profile-photo-size, 64px);
        height: var(--sakai-profile-photo-size, 64px);
        background-position: 50%;
        background-size: auto 50%;
        border-radius: 50%;
        margin-right: var(--sakai-profile-photo-margin-right, 10px);
        background-size: var(--sakai-profile-photo-size, 64px) var(--sakai-profile-photo-size, 64px);
      }
      .name {
        font-weight: var(--sakai-profile-name-weight, 700);
        font-size: var(--sakai-profile-name-size, 16px);
        margin-bottom: var(--sakai-profile-name-margin-bottom, 8px);
      }
      .role, .pronouns {
        font-weight: var(--sakai-profile-header-weight, 400);
        font-size: var(--sakai-profile-header-size, 12px);
      }
      .label {
        font-weight: var(--sakai-profile-label-weight, 700);
        font-size: var(--sakai-profile-label-size, 12px);
        margin-bottom: var(--sakai-profile-label-margin-bottom, 4px);
      }
      .url {
        margin-top: var(--sakai-profile-url-margin-top, 12px);
      }
      .field {
        margin-bottom: var(--sakai-profile-field-margin-bottom, 8px);
      }

      .pronunciation {
        display: flex;
        align-items: center;
      }

      .pronunciation > div {
        margin-right: 10px;
      }

      button {
        margin-bottom: 5px;
      }
    `
  ];
}
