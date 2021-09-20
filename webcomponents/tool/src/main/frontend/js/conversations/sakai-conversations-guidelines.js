import { html, unsafeHTML } from "../assets/@lion/core/index.js";
import { SakaiElement } from "../sakai-element.js";

export class SakaiConversationsGuidelines extends SakaiElement {

  static get properties() {

    return {
      guidelines: { type: String },
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

    return html`
      <h1>${this.i18n.community_guidelines}</h1>
      <div class="sak-banner-info">${this.i18n.community_guidelines_instruction}</div>
      <div id="conv-guidelines">${unsafeHTML(this.guidelines || this.i18n.no_guidelines_yet)}</div>
    `;
  }
}

const tagName = "sakai-conversations-guidelines";
!customElements.get(tagName) && customElements.define(tagName, SakaiConversationsGuidelines);
