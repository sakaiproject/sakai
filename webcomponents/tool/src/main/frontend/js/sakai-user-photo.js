import { SakaiElement } from "./sakai-element.js";
import { html } from "./assets/lit-html/lit-html.js";

/**
 * A simple wrapper for Sakai's user profile picture.
 *
 * Usage:
 *
 * Renders adrian's profile picture and pops up the profile panel when clicked.
 * <sakai-user-photo user-id="adrian">
 *
 * Renders adrian's profile picture without a popup
 * <sakai-user-photo user-id="adrian" no-popup>
 *
 * Renders adrian's profile picture with a popup and some custom styles from the supplied class.
 * <sakai-user-photo user-id="adrian" classes="custom">
 */
class SakaiUserPhoto extends SakaiElement {

  constructor() {

    super();

    this.classes = "large-thumbnail";
  }

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      classes: { type: String },
      noPopup: { attribute: "no-popup", type: Boolean },
      official: { type: Boolean },
      siteId: { attribute: "site-id", type: String },
    };
  }

  connectedCallback() {

    super.connectedCallback();

    this.generatedId = `sakai-user-photo-${this.userId}-${Math.floor(Math.random() * 100)}`;

    this.url = `/direct/profile/${this.userId}/image/${this.official ? "official" : "thumb"}`
                + (this.siteId && `?siteId=${this.siteId}`);

    if (!this.noPopup) {
      this.updateComplete.then(() => {
        profile.attachPopups($(`#${this.generatedId}`));
      });
    }
  }

  shouldUpdate() {
    return this.userId;
  }

  render() {

    return html`
      <div id="${this.generatedId}"
          data-user-id="${this.userId}"
          class="sakai-user-photo ${this.classes}"
          style="background-image:url(${this.url}) ${this.noPopup ? "" : ";cursor: pointer;"}">
    `;
  }
}

const tagName = "sakai-user-photo";
!customElements.get(tagName) && customElements.define(tagName, SakaiUserPhoto);
