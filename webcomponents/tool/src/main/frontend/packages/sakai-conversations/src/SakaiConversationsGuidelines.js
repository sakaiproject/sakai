import { html } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { SakaiElement } from "@sakai-ui/sakai-element";

export class SakaiConversationsGuidelines extends SakaiElement {

  static properties = { guidelines: { type: String } };

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this._i18n = r);
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`
      <h1>${this._i18n.community_guidelines}</h1>
      <div class="sak-banner-info">${this._i18n.community_guidelines_instruction}</div>
      <div id="conv-guidelines">${unsafeHTML(this.guidelines || this._i18n.no_guidelines_yet)}</div>
    `;
  }
}
