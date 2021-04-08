import { SakaiElement } from "./sakai-element.js";
import { html } from "./assets/lit-html/lit-html.js";

class SakaiUserPhoto extends SakaiElement {

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
    };
  }

  shouldUpdate() {
    return this.userId;
  }

  render() {
    return html`
      <div class="sakai-user-photo"
            style="background-image:url(/direct/profile/${this.userId}/image/thumb)">
      </div>
    `;
  }
}

if (!customElements.get("sakai-user-photo")) {
  customElements.define("sakai-user-photo", SakaiUserPhoto);
}
