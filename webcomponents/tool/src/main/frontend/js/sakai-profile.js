import { css, html, LitElement } from "./assets/lit-element/lit-element.js";
import { loadProperties } from "./sakai-i18n.js";
import "./sakai-icon.js";
import "./sakai-pronunciation-player.js";

/**
 * Renders a user's Sakai profile.
 *
 * Usage: <sakai-profile user-id="SOMEUSERID"></sakai-profile>
 */
class SakaiProfile extends LitElement {

  constructor() {

    super();

    loadProperties("profile").then(i18n => this.i18n = i18n);
  }

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      siteId: { attribute: "site-id", type: String },
      tool: { type: String },
      profile: { attribute: false, type: Object },
      i18n: { attribute: false, type: Object },
      playing: { attribute: false, type: Boolean },
    };
  }

  set userId(value) {

    this._userId = value;

    const url = `/api/users/${value}/profile`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Network error while getting user profile from ${url}`);
      })
      .then(profile => this.profile = profile)
      .catch(error => console.error(error));
  }

  get userId() { return this._userId; }

  playPronunciation() {

    this.shadowRoot.getElementById("pronunciation-player").play();
  }

  render() {

    return html`
      <div class="container">
        <div class="header">
          <div class="photo" style="background-image:url(/direct/profile/${this.userId}/image/thumb)">
          </div>
          <div>
            <div class="name">${this.profile.name}</div>
            ${this.profile.role ? html`
            <div class="role">${this.profile.role}</div>
            ` : ""}
            <div class="pronouns">${this.profile.pronouns}</div>
          </div>
        </div>
        <div class="body">
          ${this.profile.pronunciation || this.profile.pronunciationRecordingUrl ? html`
          <div class="label">${this.i18n.name_pronunciation}</div>
          <div class="field pronunciation">
            ${this.profile.pronunciation ? html`
            <div>${this.profile.pronunciation}</div>`
            : ""}
            ${this.profile.hasPronunciationRecording ? html`
            <sakai-pronunciation-player user-id="${this.userId}"></sakai-pronunciation-player>
            ` : ""}
          </div>
          ` : ""}
          ${this.profile.email ? html`
          <div class="label">${this.i18n.email}</div>
          <div class="field">${this.profile.email}</div>
          ` : ""}
          ${this.profile.studentNumber ? html`
          <div class="label">${this.i18n.student_number}</div>
          <div class="field">${this.profile.studentNumber}</div>
          ` : ""}
          ${this.profile.url ? html`
          <div class="label url"><a href="${this.profile.url}">${this.i18n.view_full_profile}</a></div>
          ` : ""}
        </div>
      </div>
    `;
  }

  static get styles() {

    return css`
      .container {
        padding: 14px;
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
        color: var(--sakai-profile-name-color, #262626);
        margin-bottom: var(--sakai-profile-name-margin-bottom, 8px);
      }
      .role, .pronouns {
        font-weight: var(--sakai-profile-header-weight, 400);
        font-size: var(--sakai-profile-header-size, 12px);
        color: var(--sakai-profile-header-color, #262626);
      }
      .label {
        font-weight: var(--sakai-profile-label-weight, 700);
        font-size: var(--sakai-profile-label-size, 12px);
        color: var(--sakai-profile-label-color, #666666);
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
    `;
  }
}

const tagName = "sakai-profile";
!customElements.get(tagName) && customElements.define(tagName, SakaiProfile);
