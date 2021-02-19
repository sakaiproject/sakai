import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";

export class SakaiConversations extends SakaiElement {

  static get properties() {

    return {
    };
  }

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  shouldUpdate() {
    return this.i18n;
  }

  render() {
    return html`${this.i18n["title"]}`;
  }
}

if (!customElements.get("sakai-conversations")) {
  customElements.define("sakai-conversations", SakaiConversations);
}
