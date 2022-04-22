import { SakaiElement } from "./sakai-element.js";
import { html } from "./assets/lit-html/lit-html.js";

/**
 * A simple wrapper for Sakai's profile picture.
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
 * <sakai-user-photo user-id="adrian" style-class="custom">
 */
class SakaiUserPhoto extends SakaiElement {

  constructor() {

    super();

    this.sizeClass = "large-thumbnail";
  }

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      sizeClass: { attribute: "size-class", type: String },
      noPopup: { attribute: "no-popup", type: Boolean },
      official: { type: Boolean },
    };
  }

  set userId(value) {

    const old = this._userId;
    this._userId = value;
    this.generatedId = `sakai-user-photo-${this.userId}-${Math.floor(Math.random() * 100)}`;
    this.requestUpdate("userId", old);

    if (!this.noPopup) {
      this.updateComplete.then(() => {
        profile.attachPopups($(`#${this.generatedId}`));
      });
    }
  }

  get userId() { return this._userId; }

  shouldUpdate() {
    return this.userId && this.sizeClass;
  }

  render() {

    return html`
      <div id="${this.generatedId}"
          data-user-id="${this.userId}"
          class="sakai-user-photo ${this.sizeClass}"
          style="background-image:url(/direct/profile/${this.userId}/image/${this.official ? "official" : "thumb"})">
    `;
  }
}

const tagName = "sakai-user-photo";
!customElements.get(tagName) && customElements.define(tagName, SakaiUserPhoto);
