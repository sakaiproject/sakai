import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-icon.js";

export class SakaiTopicSummary extends SakaiElement {

  static get properties() {

    return {
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

  showTopic() {
    this.dispatchEvent(new CustomEvent("show-topic", { detail: { topicId: this.topic.id } }));
  }

  shouldUpdate() {
    return this.i18n && this.topic;
  }

  render() {

    return html`
      <div class="topic-summary">
        <div class="topic-summary-details">
          <div class="type-and-read-icons">
            <div>
            ${this.topic.type === "QUESTION" ? html`
            <sakai-icon type="question" size="medium"></sakai-icon>
            ` : ""}
            ${this.topic.type === "DISCUSSION" ? html`
            <sakai-icon type="forums" size="medium"></sakai-icon>
            ` : ""}
            </div>
            ${this.topic.viewed ? "" : html`
            <div class="read-icon">
              <sakai-icon type="circle" size="small"></sakai-icon>
            </div>
            `}
          </div>
          <div class="topic-summary-details-block">
            <div class="topic-title">
              <a href="javascript:;" @click=${this.showTopic}>${this.topic.title}</a>
            </div>
            <div class="topic-summary-creator-block">
              <span>${this.topic.creatorDisplayName}</span>
              <span style="white-space: nowrap;">${this.formatDate(this.topic.created)}</span>
            </div>
            <div class="topic-summary-tags-block">
            ${this.topic.tags.map(tag => html`
              <div class="topic-summary-tag">${tag}</div>
            `)}
            </div>
          </div>
        </div>
        <div class="topic-summary-indicators">
          <div class="topic-summary-answered-indicator">
            ${this.topic.resolved ? html`
            <sakai-icon type="check_circle" size="small"></sakai-icon>
            ` : html`
            <sakai-icon type="circle" size="small"></sakai-icon>
            `}
          </div>
          <div class="topic-summary-posts-indicator">
            <sakai-icon type="forums" size="small"></sakai-icon>
            <span class="post-number">${this.topic.numberOfPosts}</span>
          </div>
        </div>
      </div>
    `;
  }
}

if (!customElements.get("sakai-topic-summary")) {
  customElements.define("sakai-topic-summary", SakaiTopicSummary);
}
