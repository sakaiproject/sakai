import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";

export class SakaiConversations extends SakaiElement {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      topics: { type: Array },
    };
  }

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set siteId(value) {

    this._siteId = value;

    console.log("here");

    fetch(`/api/sites/${this.siteId}/conversations`)
      .then(r => r.json())
      .then(topics => {

        console.log(topics);
        this.topics = topics;
      });
  }

  get siteId() { return this._siteId; }

  shouldUpdate() {
    return this.i18n && this.topics;
  }

  render() {

    return html`
      <h2>${this.i18n["title"]}</h2>
      ${this.topics.map(t => html`
        <div class="topic">
          ${t.type === "QUESTION" ? html`
          <span>${this.i18n["question"]}</span>
          ` : ""}
          <div class="topic-title">${t.title}</div>
          <div class="topic-messagesauthor-image-wrapper">
            <img src="${t.metadata.authorImage}" class="topic-author-image" />
          </div>
        </div>
      `)}
    `;
  }
}

if (!customElements.get("sakai-conversations")) {
  customElements.define("sakai-conversations", SakaiConversations);
}
