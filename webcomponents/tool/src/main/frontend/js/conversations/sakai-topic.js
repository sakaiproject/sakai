import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-user-photo.js";
import moment from "../assets/moment/dist/moment.js";

export class SakaiTopic extends SakaiElement {

  static get properties() {

    return {
      topicId: { attribute: "topic-id", type: String },
      topic: { type: Object },
    };
  }

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  formatDate(millis) {

    const dateTime = new Date(millis);
    const date = dateTime.toLocaleDateString( 'en-gb', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      timeZone: 'utc'
    });
    const time = dateTime.toLocaleTimeString( 'en-gb', {
      hour: "numeric",
      minute: "numeric",
    });
    return `${date} (${time})`;
  }

  set topicId(value) {

    this._topicId = value;

    fetch(`/api/topics/${this.topicId}`)
      .then(r => r.json())
      .then(t => {

        t.lastActivityHuman = moment.duration(t.lastActivity - Date.now(), "milliseconds").humanize(true);
        t.formattedCreatedDate = this.formatDate(t.created);

        let setFormattedCreatedDate = (p) => {

          p.formattedCreatedDate = this.formatDate(p.created);
          p.replies && p.replies.forEach(p => setFormattedCreatedDate(p));
        };

        t.posts.forEach(p => setFormattedCreatedDate(p));

        this.topic = t;
      });
  }

  get topicId() { return this._topicId; }

  renderPost(p, isReply) {

    return html`
      <div class="post ${isReply ? "reply" : ""}">
        <div class="author-block">
          <div><sakai-user-photo user-id="${p.creator}"></sakai-user-photo></div>
          <div>
            <div class="author-details">
              <span>${p.creatorDisplayName}</span>
              <div class="message-date">${p.formattedCreatedDate}</div>
            </div>
          </div>
        </div>
        <div class="message">${unsafeHTML(p.message)}</div>
      </div>
      ${p.replies ? html`
      <div class="replies">
        ${p.replies.map(r => this.renderPost(r, true))}
      </div>
      ` : ""}
    `;
  }

  shouldUpdate() {
    return this.i18n && this.topic;
  }

  render() {

    return html`
      <div class="title-block">
        <span class="title">${this.topic.title}</span>
        <span class="author"> ${this.i18n["by"]} ${this.topic.creatorDisplayName}</span>
      </div>
      <div class="post starter-post">
        <div class="author-block">
          <div><sakai-user-photo user-id="${this.topic.creator}"></sakai-user-photo></div>
          <div>
            <div class="author-details">
              <span>${this.topic.creatorDisplayName}</span>
              <span>(${this.topic.creatorRole})</span>
              <span> - ${this.i18n["topic_author"]}</span>
              <div class="message-date">${this.topic.formattedCreatedDate}</div>
            </div>
          </div>
        </div>
        <div class="message">${unsafeHTML(this.topic.message)}</div>
      </div>
      <div class="posts">
        ${this.topic.posts.map(p => this.renderPost(p))}
      </div>
    `;
  }
}

if (!customElements.get("sakai-topic")) {
  customElements.define("sakai-topic", SakaiTopic);
}
