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
    siteId: { attribute: "site-id", type: String },
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

  refresh() {
    if (this.blank) {
      this.url = "/api/users/blank/profile/image";
    } else {
      this.url = this.getImageUrl();
    }
  }

  close() {
    bootstrap.Popover.getInstance(this.querySelector("div"))?.hide();
  }

  willUpdate(changedProperties) {
    if (changedProperties.has("userId") || changedProperties.has("official") || changedProperties.has("blank") || changedProperties.has("siteId")) {
      if (this.blank) {
        this.url = "/api/users/blank/profile/image";
      } else {
        this.url = this.getImageUrl();
      }
    }
  }

  getImageUrl() {
    const uid = this.userId?.trim() || "blank";
    const sid = this.siteId?.trim() || getSiteId();
    const imageType = this.official ? "official" : "thumb";

    return `/api/users/${uid}/profile/image/${imageType}` + (sid ? `?siteId=${sid}&_=${Date.now()}` : `?_=${Date.now()}`);
  }

  firstUpdated() {

    if (this.profilePopup !== SakaiUserPhoto.ON || this.print) return;

    const el = this.querySelector("div");
    const sakaiProfile = this.querySelector("sakai-profile");

    const popover = new bootstrap.Popover(el, {
      content: sakaiProfile,
      html: true,
      trigger: "click",
    });

    const closeOnOutsideClick = e => {
      const popoverEl = document.querySelector(".popover");
      if (popoverEl && !popoverEl.contains(e.target) && !el.contains(e.target)) {
        popover.hide();
      }
    };

    el.addEventListener("show.bs.popover", () => {
      this.dispatchEvent(new CustomEvent("profile-shown", { detail: { userId: this.userId }, bubbles: true }));
      sakaiProfile.fetchProfileData();
      setTimeout(() => document.addEventListener("click", closeOnOutsideClick, { once: true }), 0);
    });
  }

  render() {

    if (this.print) {
      return html`
        <img src="${ifDefined(this.url)}" alt="${ifDefined(this.label ? this.label : undefined)}" />
      `;
    }

    const uid = this.userId?.trim() || "blank";
    return html`
      <div data-user-id="${uid}"
          class="sakai-user-photo ${this.classes ?? "large-thumbnail"}"
          data-bs-toggle="popover"
          data-bs-trigger="click"
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
        <sakai-profile user-id="${uid}"></sakai-profile>
      </div>
    `;
  }
}

SakaiUserPhoto.OFF = "off";
SakaiUserPhoto.ON = "on";
