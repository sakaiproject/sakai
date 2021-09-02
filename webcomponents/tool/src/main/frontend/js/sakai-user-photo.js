import { SakaiElement } from "./sakai-element.js";
import { html } from "./assets/lit-html/lit-html.js";

class SakaiUserPhoto extends SakaiElement {

  constructor() {

    super();

    this.sizeClass = "large-thumbnail";

  }

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      sizeClass: { attribute: "size-class", type: String },
    };
  }

  shouldUpdate() {
    return this.userId && this.sizeClass;
  }

  render() {
    return html`
      <div class="sakai-user-photo ${this.sizeClass}"
            style="background-image:url(/direct/profile/${this.userId}/image/thumb)">
      </div>
    `;
  }
}

if (!customElements.get("sakai-user-photo")) {
  customElements.define("sakai-user-photo", SakaiUserPhoto);
}
