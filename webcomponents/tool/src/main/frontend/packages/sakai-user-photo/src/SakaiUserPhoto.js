import { SakaiElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { getSiteId } from "@sakai-ui/sakai-portal-utils";
import "@sakai-ui/sakai-profile/sakai-profile.js";

/**
 * A simple wrapper for Sakai's user profile picture.
 *
 * Usage:
 * <sakai-user-photo user-id="adrian">
 *
 * @element sakai-user-photo
 * @property {string} user-id - A Sakai user id
 * @property {string} [classes] - Extra classes to style the content
 * @property {string} [profile-popup] By default, profile popups are off. Set this to "on" if you want them
 * @property {boolean} [official] Set this if you want the official Sakai photo
 * @property {string} [site-id] Set this to trigger permissions checks on the photo
 * @property {string} [label] This will be used as the title and aria-label for the div
 * @property {boolean} [print] Set this to trigger the render of a print friendly img tag
 */
export class SakaiUserPhoto extends SakaiElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    classes: { type: String },
    profilePopup: { attribute: "profile-popup", type: String },
    official: { type: Boolean },
    blank: { type: Boolean },
    label: { type: String },
    print: { type: Boolean },
    online: { type: Boolean },
    url: { state: true },
  };

  constructor() {

    super();

    this.classes = "large-thumbnail";
    this.profilePopup = SakaiUserPhoto.OFF;
  }

  connectedCallback() {

    super.connectedCallback();

    if (this.blank) {
      this.url = "/direct/profile/blank/image";
    } else {
      this.url = `/direct/profile/${this.userId}/image/${this.official ? "official" : "thumb"}`
                  + (getSiteId() ? `?siteId=${getSiteId()}` : "");
    }
  }

  close() {
    bootstrap.Popover.getInstance(this.querySelector("div"))?.hide();
  }

  shouldUpdate() { return this.url; }

  firstUpdated() {

    if (this.profilePopup !== SakaiUserPhoto.ON || this.print) return;

    const el = this.querySelector("div");
    const sakaiProfile = this.querySelector("sakai-profile");

    new bootstrap.Popover(el, {
      content: sakaiProfile,
      html: true,
      trigger: "focus",
    });

    el.addEventListener("show.bs.popover", () => {

      this.dispatchEvent(new CustomEvent("profile-shown", { detail: { userId: this.userId }, bubbles: true }));
      sakaiProfile.fetchProfileData(); // Trigger the JSON load for this user
    });
  }

  render() {

    if (this.print) {
      return html`
        <img src="${ifDefined(this.url)}" alt="${ifDefined(this.label ? this.label : undefined)}" />
      `;
    }

    return html`
      <div data-user-id="${this.userId}"
          class="sakai-user-photo ${this.classes}"
          data-bs-toggle="popover"
          data-bs-trigger="focus"
          aria-label="${ifDefined(this.label)}"
          title="${ifDefined(this.label)}"
          tabindex="0"
          role="button"
          style="background-image:url(${this.url}) ${this.profilePopup === SakaiUserPhoto.OFF ? "" : ";cursor: pointer;"}">
        ${this.online ? html`
        <span></span>
        ` : nothing}
      </div>
      <div class="d-none">
        <sakai-profile user-id="${this.userId}"></sakai-profile>
      </div>
    `;
  }
}

SakaiUserPhoto.OFF = "off";
SakaiUserPhoto.ON = "on";
