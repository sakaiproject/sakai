import { SakaiElement } from "./sakai-element.js";
import { html } from "./assets/lit-html/lit-html.js";

/**
 * A simple wrapper for Sakai's user profile picture.
 *
 * Usage:
 * <sakai-user-photo user-id="adrian">
 *
 * @element sakai-user-photo
 * @property {string} user-id - A Sakai user id
 * @property {string} [classes] - Extra classes to style the content
 * @property {boolean} [no-popup] Set this if you don't want the profile popup
 * @property {boolean} [official] Set this if you want the official Sakai photo
 * @property {string} [site-id] Set this to trigger permissions checks on the photo
 * @property {boolean} [print] Set this to trigger the render of a print friendly img tag
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
      print: { type: Boolean },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.userId) {
      this.generatedId = `sakai-user-photo-${this.userId}-${Math.floor(Math.random() * 100)}`;

      this.url = `/direct/profile/${this.userId}/image/${this.official ? "official" : "thumb"}`
                  + (this.siteId && `?siteId=${this.siteId}`);

      if (!this.noPopup) {
        this.updateComplete.then(() => {
          profile.attachPopups($(`#${this.generatedId}`));
        });
      }
    }
  }

  shouldUpdate() {
    return this.userId;
  }

  render() {

    if (this.print) {
      return html`
        <img src="${this.url}" alt="Print view only" />
      `;
    }

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
