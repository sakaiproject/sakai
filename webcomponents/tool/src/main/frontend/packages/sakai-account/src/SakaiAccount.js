import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-picture-changer/sakai-picture-changer.js";
import "@sakai-ui/sakai-audio-recorder/sakai-audio-recorder.js";
import "@sakai-ui/sakai-pronunciation-player/sakai-pronunciation-player.js";
import isEmail from "validator/es/lib/isEmail.js";
const SOCIAL_HOSTS = {
  facebook: ["facebook.com"],
  instagram: ["instagram.com"],
  linkedin: ["linkedin.com"],
};

export class SakaiAccount extends SakaiElement {

  static properties = {
    userId: { attribute: "user-id", type: String },

    _profile: { state: true },
    _editingBasicInfo: { state: true },
    _editingPronunciationInfo: { state: true },
    _editingContactInfo: { state: true },
    _editingSocialInfo: { state: true },
    _editingPicture: { state: true },
    _pronunciationRecordingPreviewUrl: { state: true },
    _displayBasicInfoUpdatedBanner: { state: true },
    _displayBasicInfoErrorBanner: { state: true },
    _displayPronunciationInfoUpdatedBanner: { state: true },
    _displayPronunciationInfoErrorBanner: { state: true },
    _displayContactInfoUpdatedBanner: { state: true },
    _displayContactInfoErrorBanner: { state: true },
    _displaySocialInfoUpdatedBanner: { state: true },
    _displaySocialInfoErrorBanner: { state: true },
    _emailInvalid: { state: true },
    _mobileInvalid: { state: true },
    _facebookUrlInvalid: { state: true },
    _linkedinUrlInvalid: { state: true },
    _instagramUrlInvalid: { state: true },
    _currentError: { state: true },
  };

  constructor() {

    super();

    this._updateProfileOptions = {
      method: "PATCH",
      headers: { "Content-Type": "application/json-patch+json" },
    };

    this.loadTranslations("account");
  }

  connectedCallback() {

    super.connectedCallback();
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (name === "user-id") {
      this._loadUser();
    }
  }

  _loadUser() {

    const url = `/api/users/${this.userId}/profile`;
    fetch(url)
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Failed to get user from ${url}`);
    })
    .then(profile => this._profile = profile);
  }

  _openPictureChanger() { this._editingPicture = true; }

  _closePictureChanger() { this._editingPicture = false; }

  _profilePhotoUpdated() {

    this._editingPicture = false;

    this.updateComplete.then(() => {
      this.querySelector("sakai-user-photo").refresh();
    });
  }

  _editBasicInfo() { this._editingBasicInfo = true; }

  _editPronunciationInfo() { this._editingPronunciationInfo = true; }

  _editContactInfo() { this._editingContactInfo = true; }

  _editSocialInfo() { this._editingSocialInfo = true; }

  _patchProfile(patch) {

    const options = { ...this._updateProfileOptions, body: JSON.stringify(patch) };

    const url = `/api/users/${this.userId}/profile`;
    return fetch(url, options)
      .then(r => {

        if (r.ok) {
          this._savingProfile = false;
          return r.json();
        }

        throw new Error(`Network error while patching to ${url}`);
      })
      .then(profile => this._profile = profile);
  }

  _saveBasicInfo() {

    const patch = [
      { op: "replace", path: "/firstName", value: this.renderRoot.querySelector("#first-name-input").value },
      { op: "replace", path: "/lastName", value: this.renderRoot.querySelector("#last-name-input").value },
      { op: "replace", path: "/nickname", value: this.renderRoot.querySelector("#nickname-input").value },
      { op: "replace", path: "/pronouns", value: this.renderRoot.querySelector("#pronouns-input").value },
    ];

    this._patchProfile(patch).then(() => {

      this._editingBasicInfo = false;
      this._displayBasicInfoUpdatedBanner = true;
      setTimeout(() => this._displayBasicInfoUpdatedBanner = false, 3000);
    })
    .catch (err => {

      console.error(err);
      this._displayBasicInfoErrorBanner = true;
    });
  }

  _resetBasicInfo() {

    this._editingBasicInfo = false;
    this._displayBasicInfoErrorBanner = false;
  }

  async _savePronunciationInfo() {

    const base64 = await this.querySelector("sakai-audio-recorder").getBase64();

    const patch = [
      { op: "replace", path: "/phoneticPronunciation", value: this.renderRoot.querySelector("#phonetic-pronunciation-input").value },
    ];

    if (base64) {
      patch.push({ op: "replace", path: "/audioBase64", value: base64 });
    }

    this._patchProfile(patch).then(() => {

      this._editingPronunciationInfo = false;
      this._displayPronunciationInfoUpdatedBanner = true;
      setTimeout(() => this._displayPronunciationInfoUpdatedBanner = false, 3000);
    })
    .catch(err => {

      console.error(err);
      this._displayBasicPronunciationInfoErrorBanner = true;
    });
  }

  _resetPronunciationInfo() {

    this._editingPronunciationInfo = false;
    this._displayPronunciationInfoErrorBanner = false;
  }

  _saveContactInfo() {

    const email = this.renderRoot.querySelector("#email-input").value;
    if (!isEmail(email)) {
      this._currentError = this._i18n.invalid_email;
      this._displayContactInfoErrorBanner = true;
      this._emailInvalid = true;
      email.scrollIntoView();
      return;
    }

    const mobile = this.renderRoot.querySelector("#mobile-input").value;
    const digitsOnly = mobile ? mobile.replace(/\D/g, "") : "";
    if (mobile && mobile.trim() && (digitsOnly.length < 7 || !/^[\d\s\-().+]+$/.test(mobile))) {
      this._currentError = this._i18n.invalid_mobile;
      this._displayContactInfoErrorBanner = true;
      this._mobileInvalid = true;
      mobile.scrollIntoView();
      return;
    }

    const patch = [
      { op: "replace", path: "/email", value: email },
      { op: "replace", path: "/mobile", value: mobile },
    ];

    this._patchProfile(patch).then(() => {

      this._editingContactInfo = false;
      this._clearContactError();
      this._displayContactInfoUpdatedBanner = true;
      setTimeout(() => this._displayContactInfoUpdatedBanner = false, 3000);
    })
    .catch(err => {

      console.error(err);
      this._displayContactInfoErrorBanner = true;
    });
  }

  _resetContactInfo() {

    this._editingContactInfo = false;
    this._clearContactError();
  }

  _clearContactError() {

    this._displayContactInfoErrorBanner = false;
    this._emailInvalid = false;
    this._mobileInvalid = false;
  }

  _isValidSocialUrl(provider, value) {

    if (!value) {
      return false;
    }

    const trimmed = value.trim();
    let url;
    try {
      const normalized = /^(https?:)?\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
      url = new URL(normalized);
    } catch (e) {
      return false;
    }

    const host = url.hostname.toLowerCase();
    const allowedHosts = SOCIAL_HOSTS[provider] || [];
    return allowedHosts.some(allowedHost => host === allowedHost || host.endsWith(`.${allowedHost}`));
  }

  _saveSocialInfo() {

    const facebookUrl = this.renderRoot.querySelector("#facebook-input");
    const instagramUrl = this.renderRoot.querySelector("#instagram-input");
    const linkedinUrl = this.renderRoot.querySelector("#linkedin-input");

    if (facebookUrl.value && !this._isValidSocialUrl("facebook", facebookUrl.value)) {
      this._currentError = this._i18n.invalid_facebook;
      this._displaySocialInfoErrorBanner = true;
      this._facebookUrlInvalid = true;
      facebookUrl.scrollIntoView();
      return;
    }

    if (instagramUrl.value && !this._isValidSocialUrl("instagram", instagramUrl.value)) {
      this._currentError = this._i18n.invalid_instagram;
      this._displaySocialInfoErrorBanner = true;
      this._instagramUrlInvalid = true;
      instagramUrl.scrollIntoView();
      return;
    }

    if (linkedinUrl.value && !this._isValidSocialUrl("linkedin", linkedinUrl.value)) {
      this._currentError = this._i18n.invalid_linkedin;
      this._displaySocialInfoErrorBanner = true;
      this._linkedinUrlInvalid = true;
      linkedinUrl.scrollIntoView();
      return;
    }

    const patch = [
      { op: "replace", path: "/facebookUrl", value: facebookUrl.value },
      { op: "replace", path: "/instagramUrl", value: instagramUrl.value },
      { op: "replace", path: "/linkedinUrl", value: linkedinUrl.value },
    ];

    this._patchProfile(patch).then(() => {

      this._editingSocialInfo = false;
      this._clearSocialError();
      this._displaySocialInfoUpdatedBanner = true;
      setTimeout(() => this._displaySocialInfoUpdatedBanner = false, 3000);
    })
    .catch(err => {

      console.error(err);
      this._displaySocialInfoErrorBanner = true;
    });
  }

  _resetSocialInfo() {

    this._editingSocialInfo = false;
    this._clearSocialError();
  }

  _clearSocialError() {

    this._currentError = null;
    this._displaySocialInfoErrorBanner = false;
    this._facebookUrlInvalid = false;
    this._linkedinUrlInvalid = false;
    this._instagramUrlInvalid = false;
  }

  _recordingComplete(e) {
    this._pronunciationRecordingPreviewUrl = e.detail.blobUrl;
  }

  _clearNameRecording() {

    const url = `/api/users/${this.userId}/profile/pronunciation`;
    fetch(url, { method: "DELETE" })
    .then(r => {

      if (r.ok) {
        this._profile.nameRecordingUrl = null;
        this._editingPronunciationInfo = false;
      } else {
        throw new Error(`Network error while removing pronunciation recording at ${url}`);
      }
    })
    .catch(e => console.error(e));
  }

  shouldUpdate() {
    return this._i18n;
  }

  _renderBasicInfoDisplayBlock() {

    return html`
      <div class="mainSection editable flex-fill ms-3">
        <div class="mainSectionHeading">${this._i18n.basic_heading}</div>

        ${this._profile.canEdit ? html`
        <button id="basic-info-edit-button"
            type="button"
            class="btn btn-secondary float-end mt-1"
            @click=${this._editBasicInfo}>
          ${this._i18n.edit}
        </button>
        ` : nothing}

        <div class="mainSectionContent">
          ${this._profile.firstName || this._profile.lastName || this._profile.nickname || this._profile.pronouns ? html`
          <table class="profileContent">
            ${this._profile.firstName ? html`
            <tr>
              <td class="label required">${this._i18n.first_name_label}</td>
              <td class="content">${this._profile.firstName}</td>
            </tr>
            ` : nothing}
            ${this._profile.lastName ? html`
            <tr>
              <td class="label required">${this._i18n.last_name_label}</td>
              <td class="content">${this._profile.lastName}</td>
            </tr>
            ` : nothing}
            ${this._profile.pronouns ? html`
            <tr>
              <td class="label">${this._i18n.pronouns_label}</td>
              <td class="content">${this._profile.pronouns}</td>
            </tr>
            ` : nothing}
            ${this._profile.nickname ? html`
            <tr>
              <td class="label">${this._i18n.nickname_label}</td>
              <td class="content">${this._profile.nickname}</td>
            </tr>
            ` : nothing}
          </table>
          ` : html`
            <div class="profile_instruction">${this._i18n.nothing_filled_out}</div>
          `}
        </div>
        ${this._displayBasicInfoUpdatedBanner ? html`
        <div class="sak-banner-info">${this._i18n.basic_info_updated}</div>
        ` : nothing}
      </div>
    `;
  }

  _renderBasicInfoEditBlock() {

    return html`
      <div>${this._profile.displayName}</div>
      <div class="mainSection editable flex-fill ms-3">
        <div class="mainSectionHeading">${this._i18n.basic_heading}</div>
        <div class="mainSectionContent">
          <table class="profileContent">
            <tbody>
              <tr>
                <td class="label">${this._i18n.first_name_label}</td>
                <td class="content">
                  <input id="first-name-input"
                      aria-label="${this._i18n.first_name_label}"
                      type="text"
                      class="formInputField"
                      .value=${this._profile.firstName || ""}
                      required
                      ?disabled=${!this._profile.canEditNameAndEmail}>
                </td>
              </tr>
              <tr>
                <td class="label">${this._i18n.last_name_label}</td>
                <td class="content">
                  <input id="last-name-input"
                      aria-label="${this._i18n.last_name_label}"
                      type="text"
                      class="formInputField"
                      .value=${this._profile.lastName || ""}
                      required
                      ?disabled=${!this._profile.canEditNameAndEmail}>
                </td>
              </tr>
              <tr>
                <td class="label">${this._i18n.pronouns_label}</td>
                <td class="content">
                  <input id="pronouns-input"
                      aria-label="${this._i18n.pronouns_label}"
                      type="text"
                      class="formInputField"
                      .value=${this._profile.pronouns || ""}>
                </td>
              </tr>
              <tr>
                <td class="label">${this._i18n.nickname_label}</td>
                <td class="content">
                  <input id="nickname-input"
                      aria-label="${this._i18n.nickname_label}"
                      type="text"
                      class="formInputField"
                      .value=${this._profile.nickname || ""}>
                </td>
              </tr>
            </tbody>
          </table>
          <div>
            <button id="basic-info-save-button" type="button" class="btn btn-primary" @click=${this._saveBasicInfo}>${this._i18n.save}</button>
            <button id="basic-info-cancel-button" type="button" class="btn btn-secondary" @click=${this._resetBasicInfo}>${this._i18n.cancel}</button>
          </div>
          ${this._displayBasicInfoErrorBanner ? html`
          <div class="sak-banner-error">${this._i18n.basic_info_error}</div>
          ` : nothing}
        </div>
      </div>
    `;
  }

  _renderPronunciationInfoDisplayBlock() {

    return html`
      <div class="mainSection editable">
        <div class="mainSectionHeading">${this._i18n.pronunciation_heading}</div>

        ${this._profile.canEdit ? html`
        <button id="pronunciation-info-edit-button"
            type="button"
            class="btn btn-secondary float-end mt-1"
            @click=${this._editPronunciationInfo}>
          ${this._i18n.edit}
        </button>
        ` : nothing}

        <div class="mainSectionContent">
          ${this._profile.phoneticPronunciation || this._profile.nameRecordingUrl ? html`

            <table class="profileContent">
              ${this._profile.phoneticPronunciation ? html`
              <tr>
                <td class="label">${this._i18n.phonetic_name_label}</td>
                <td class="content">${this._profile.phoneticPronunciation}</td>
              </tr>
              ` : nothing}
              ${this._profile.nameRecordingUrl ? html`
              <tr>
                <td class="label">${this._i18n.name_recording_label}</td>
                <td class="content">
                  <sakai-pronunciation-player user-id="${this.userId}"></sakai-pronunciation-player>
                </td>
              </tr>
              ` : nothing}
            </table>
          ` : html`
            <div class="profile_instruction">${this._i18n.nothing_filled_out}</div>
          `}
        </div>
        ${this._displayPronunciationInfoUpdatedBanner ? html`
        <div class="sak-banner-info">${this._i18n.pronunciation_info_updated}</div>
        ` : nothing}
      </div>
    `;
  }

  _renderPronunciationInfoEditBlock() {

    return html`
      <div class="mainSection">
        <div class="mainSectionHeading">${this._i18n.pronunciation_heading}</div>

        <div class="mainSectionContentForm">
          <div class="mainSectionContentDescription mt-1">${this._i18n.name_pronunciation_description}</div>
          <table class="profileContent">

            <!-- phoneticPronunciation -->
            <tr>
              <td class="label">${this._i18n.phonetic_name_label}</td>
              <td class="content">
                <input type="text" id="phonetic-pronunciation-input"
                    aria-label="${this._i18n.phonetic_name_label}"
                    class="formInputField"
                    .value=${this._profile.phoneticPronunciation || ""}>
              </td>
            </tr>
            <tr>
              <td class="label notBold align-top ps-3">${this._i18n.phonetic_examples_label}</td>
              <td class="content">
                <div>
                  <span>${this._i18n.phonetic_example_1_pronun}</span>
                  <span class="contentExample">${this._i18n.phonetic_example_1_pronun_name}</span>
                </div>
                <div>
                  <span>${this._i18n.phonetic_example_2_pronun}</span>
                  <span class="contentExample">${this._i18n.phonetic_example_2_pronun_name}</span>
                </div>
              </td>
            </tr>

            <tr>
              <td class="label align-top">${this._i18n.name_recording_label}</td>
              <td class="content">
                <sakai-audio-recorder current-recording-url="${this._profile.nameRecordingUrl}" @recording-complete=${this._recordingComplete}></sakai-audio-recorder>
                <input type="hidden" id="audioBase64" aria-hidden="true" />
                ${this._profile.nameRecordingUrl ? html`
                  <button type="button" class="btn btn-secondary" @click=${this._clearNameRecording}>
                    ${this._i18n.clear_name_recording}
                  </button>
                ` : nothing}
              </td>
            </tr>
          </table>

          <span id="namePronunciationDuration" class="d-none"></span>

          <div>
            <button type="button" id="pronunciation-info-save-button" class="btn btn-primary" @click=${this._savePronunciationInfo}>${this._i18n.save}</button>
            <button type="button" id="pronunciation-info-cancel-button" class="btn btn-secondary" @click=${this._resetPronunciationInfo}>${this._i18n.cancel}</button>
          </div>
          ${this._displayPronunciationInfoErrorBanner ? html`
          <div class="sak-banner-error">${this._i18n.pronunciation_info_error}</div>
          ` : nothing}
        </div>
      </div>
    `;
  }

  _renderContactInfoDisplayBlock() {

    return html`
      <div class="mainSection">
        <div class="mainSectionHeading">${this._i18n.contact_heading}</div>

        ${this._profile.canEdit ? html`
        <button id="contact-info-edit-button"
            type="button"
            class="btn btn-secondary float-end mt-1"
            @click=${this._editContactInfo}>
          ${this._i18n.edit}
        </button>
        ` : nothing}

        <div class="mainSectionContent">
        ${this._profile.email || this._profile.mobile ? html`
          <table class="profileContent">
            <tbody>
              ${this._profile.email ? html`
              <tr>
                <td class="label required">${this._i18n.email_label}</td>
                <td class="content">${this._profile.email}</td>
              </tr>
              ` : nothing}
              ${this._profile.mobile ? html`
              <tr>
                <td class="label">${this._i18n.mobile_label}</td>
                <td class="content">${this._profile.mobile}</td>
              </tr>
              ` : nothing}
            </tbody>
          </table>
        ` : html`
            <div class="profile_instruction">${this._i18n.nothing_filled_out}</div>
        `}
        </div>
        ${this._displayContactInfoUpdatedBanner ? html`
        <div class="sak-banner-info">${this._i18n.contact_info_updated}</div>
        ` : nothing}
      </div>
    `;
  }

  _renderContactInfoEditBlock() {

    return html`
      <div class="mainSection">
        <div class="mainSectionHeading">${this._i18n.contact_heading}</div>

        <div class="mainSectionContentForm">
            
          <table class="profileContent">
            
            <tr>
              <td class="label required">${this._i18n.email_label}</td>
              <td class="content">
                <input type="text"
                    id="email-input"
                    class="formInputField ${this._emailInvalid ? "bg-danger" : ""}"
                    aria-label="${this._i18n.email_label}"
                    .value=${this._profile.email || ""}
                    required />
                <span class="feedbackLabel"></span>
              </td>
            </tr>
            
            <tr>
              <td class="label">${this._i18n.mobile_label}</td>
              <td class="content">
                <input type="text"
                    id="mobile-input"
                    class="formInputField ${this._mobileInvalid ? "bg-danger" : ""}"
                    aria-label="${this._i18n.mobile_label}"
                    .value=${this._profile.mobile || ""} />
                <span class="feedbackLabel"></span>
              </td>
            </tr>
            
          </table>
          
          <div class="profileFormButtons">
            <button type="button" id="contact-info-save-button" class="btn btn-primary" @click=${this._saveContactInfo}>${this._i18n.save}</button>
            <button type="button" id="contact-info-cancel-button" class="btn btn-secondary" @click=${this._resetContactInfo}>${this._i18n.cancel}</button>
          </div>
          ${this._displayContactInfoErrorBanner ? html`
          <div class="sak-banner-error">${this._currentError}</div>
          ` : nothing}
        </div>
      </div>
    `;
  }

  _renderSocialInfoDisplayBlock() {

    return html`
      <div class="mainSection">
        <div class="mainSectionHeading">${this._i18n.social_heading}</div>
        
        ${this._profile.canEdit ? html`
        <button id="social-info-edit-button"
            type="button"
            class="btn btn-secondary float-end mt-1"
            @click=${this._editSocialInfo}>
          ${this._i18n.edit}
        </button>
        ` : nothing}

        <div class="mainSectionContent">
        ${this._profile.facebookUrl || this._profile.linkedinUrl || this._profile.instagramUrl ? html`
          <table class="profileContent">
            <tbody>
              ${this._profile.facebookUrl ? html`
              <tr>
                <td class="label">${this._i18n.facebook_label}</td>
                <td class="content">${this._profile.facebookUrl}</td>
              </tr>
              ` : nothing}
              ${this._profile.linkedinUrl ? html`
              <tr>
                <td class="label">${this._i18n.linkedin_label}</td>
                <td class="content">${this._profile.linkedinUrl}</td>
              </tr>
              ` : nothing}
              ${this._profile.instagramUrl ? html`
              <tr>
                <td class="label">${this._i18n.instagram_label}</td>
                <td class="content">${this._profile.instagramUrl}</td>
              </tr>
              ` : nothing}
            </tbody>
          </table>
        ` : html`
            <div class="profile_instruction">${this._i18n.nothing_filled_out}</div>
        `}
        </div>
        ${this._displaySocialInfoUpdatedBanner ? html`
        <div class="sak-banner-info">${this._i18n.social_info_updated}</div>
        ` : nothing}
      </div>
    `;
  }

  _renderSocialInfoEditBlock() {

    return html`
      <div class="mainSection">
        <div class="mainSectionHeading">${this._i18n.social_heading}</div>

        <div class="mainSectionContentForm">

          <table class="profileContent">

            <!-- facebook -->
            <tr>
              <td class="label">${this._i18n.facebook_label}</td>
              <td class="content">
                <input type="text"
                    id="facebook-input"
                    class="formInputField ${this._facebookUrlInvalid ? "bg-danger" : ""}"
                    aria-label="${this._i18n.facebook_label}"
                    .value=${this._profile.facebookUrl || ""} />
                <span id="facebookToolTip"></span>
                <span class="feedbackLabel"></span>
              </td>
            </tr>

            <!-- linkedin -->
            <tr>
              <td class="label">${this._i18n.linkedin_label}</td>
              <td class="content">
                <input type="text"
                    id="linkedin-input"
                    class="formInputField ${this._linkedinUrlInvalid ? "bg-danger" : ""}"
                    aria-label="${this._i18n.linkedin_label}"
                    .value=${this._profile.linkedinUrl || ""} />
                <span id="linkedinToolTip"></span>
                <span class="feedbackLabel"></span>
              </td>
            </tr>

            <!-- instagram -->
            <tr>
              <td class="label">${this._i18n.instagram_label}</td>
              <td class="content">
                <input type="text"
                    id="instagram-input"
                    class="formInputField ${this._instagramUrlInvalid ? "bg-danger" : ""}"
                    aria-label="${this._i18n.instagram_label}"
                    .value=${this._profile.instagramUrl || ""} />
                <span id="instagramToolTip"></span>
                <span class="feedbackLabel"></span>
              </td>
            </tr>
          </table>

          <div class="profileFormButtons">
            <button type="button" id="social-info-save-button" class="btn btn-primary" @click=${this._saveSocialInfo}>${this._i18n.save}</button>
            <button type="button" id="social-info-cancel-button" class="btn btn-secondary" @click=${this._resetSocialInfo}>${this._i18n.cancel}</button>
          </div>
          ${this._displaySocialInfoErrorBanner ? html`
          <div class="sak-banner-error">${this._currentError}</div>
          ` : nothing}
        </div>
      </div>
    `;
  }

  render() {

    if (!this._profile) {
      return html`<div class="sak-banner-info">${this._i18n.loading_profile}</div>`;
    }

    return html`

      ${this._editingPicture ? html`
        <sakai-picture-changer user-id="${this.userId}"
            @cancel=${this._closePictureChanger}
            @updated=${this._profilePhotoUpdated}>
        </sakai-picture-changer>
      ` : html`
        <div class="d-flex mb-3">
          <div class="d-flex flex-column align-items-center">
            <div>
              <sakai-user-photo user-id="${this.userId}" classes="medium"></sakai-user-photo>
            </div>
            <div>

            ${this._profile.canUpdatePicture ? html`
            <button type="button"
                class="btn btn-link edit-image-button"
                @click=${this._openPictureChanger}>
              <i class="si si-edit"></i>
              <span>${this._i18n.change_profile_picture}</span>
            </button>
            ` : nothing}

            </div>
          </div>
          ${this._editingBasicInfo ? this._renderBasicInfoEditBlock() : this._renderBasicInfoDisplayBlock()}
        </div>
        ${this._editingPronunciationInfo ? this._renderPronunciationInfoEditBlock() : this._renderPronunciationInfoDisplayBlock()}
        ${this._editingContactInfo ? this._renderContactInfoEditBlock() : this._renderContactInfoDisplayBlock()}
        ${this._editingSocialInfo ? this._renderSocialInfoEditBlock() : this._renderSocialInfoDisplayBlock()}
      `}
    `;
  }
}
