import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import moment from "../assets/moment/dist/moment.js";

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

        this.topics = topics.map(t => {

          t.lastActivityHuman = moment.duration(t.lastActivity - Date.now(), "milliseconds").humanize(true);
          return t;
        });
      });
  }

  get siteId() { return this._siteId; }

  shouldUpdate() {
    return this.i18n && this.topics;
  }

  render() {

    return html`
      <div id="conv-desktop">
        <div class="header">Topic</div>
        <div class="header">Type</div>
        <div class="header">Posters</div>
        <div class="header">Unread</div>
        <div class="header">Posts</div>
        <div class="header">Last Activity</div>
        ${this.topics.map(t => html`
          <div class="topic-title-wrapper">
            ${t.type === "QUESTION" ? html`
            <span class="question-prefix">${this.i18n["question"]}</span>
            ` : ""}
            <span class="topic-title">${t.title}</span>
          </div>
          <div>${this.i18n[t.type]}</div>
          <div class="topic-poster-images-wrapper">
            <div class="topic-poster-image-wrapper"><img src="${t.metadata.creatorImage}" class="topic-poster-image" /></div>
            ${t.posters.map(p => html`
            <div class="topic-poster-image-wrapper"><img src="${p.posterImage}" class="topic-poster-image" /></div>
            `)}
          </div>
          <div>${t.numberOfPosts - t.viewed}</div>
          <div>${t.numberOfPosts}</div>
          <div>${t.lastActivityHuman}</div>
        `)}
      </div>
      <div id="conv-mobile">
        ${this.topics.map(t => html`
        <div class="topic">
          <div class="topic-title-wrapper">
            ${t.type === "QUESTION" ? html`
            <span class="question-prefix">${this.i18n["question"]}</span>
            ` : ""}
            <span class="topic-title">${t.title}</span>
          </div>
          <div class="topic-data">
            <div class="header">Type:</div><div>${this.i18n[t.type]}</div>
            <div class="header">Posters</div>
            <div class="topic-poster-images-wrapper">
              <div class="topic-poster-image-wrapper"><img src="${t.metadata.creatorImage}" class="topic-poster-image" /></div>
              ${t.posters.map(p => html`
              <div class="topic-poster-image-wrapper"><img src="${p.posterImage}" class="topic-poster-image" /></div>
              `)}
            </div>
            <div class="header">Unread</div><div>${t.numberOfPosts - t.viewed}</div>
            <div class="header">Posts</div><div>${t.numberOfPosts}</div>
            <div class="header">Last Activity</div><div>${t.lastActivityHuman}</div>
          </div>
        </div>
        `)}
      </div>
    `;
  }
}

if (!customElements.get("sakai-conversations")) {
  customElements.define("sakai-conversations", SakaiConversations);
}
