import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-icon.js";
import { getPostsForTopic } from "./utils.js";
import { QUESTION, DISCUSSION } from "./sakai-conversations-constants.js";

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

  topicSelected() {

    if (!this.topic.posts || this.topic.posts.length === 0) {

      getPostsForTopic(this.topic).then(posts => {

        this.topic.posts = posts;
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
        /*
        window.scroll({
          top: 0,
          left: 0,
          behavior: "smooth"
        });
        */
        this.requestUpdate();
      })
      .finally(() => {
        this.dispatchEvent(new CustomEvent("topic-selected", { detail: { topic: this.topic }, bubbles: true }));
      });
    } else {
      this.dispatchEvent(new CustomEvent("topic-selected", { detail: { topic: this.topic }, bubbles: true }));
    }
  }

  shouldUpdate() {
    return this.i18n && this.topic;
  }

  render() {

    return html`
      <a href="javascript:;" @click=${this.topicSelected} class="topic-summary-link">
        <div class="topic-summary
              ${this.topic.numberOfUnreadPosts > 0 && !this.topic.selected ? " unread" : ""}
              ${this.topic.selected ? "selected" : ""}"
        >
          <div class="type-icon">
            ${this.topic.type === QUESTION ? html`
              <sakai-icon type="questioncircle"
                  class="question-icon"
                  size="medium"
                  arial-label="${this.i18n.question_tooltip}"
                  title="${this.i18n.question_tooltip}">
              </sakai-icon>
            ` : ""}
            ${this.topic.type === DISCUSSION ? html`
            <div class="discussion-icon-wrapper">
              <sakai-icon type="forums"
                  class="discussion-icon"
                  size="small"
                  arial-label="${this.i18n.discussion_tooltip}"
                  title="${this.i18n.discussion_tooltip}">
              </sakai-icon>
            </div>
            ` : ""}
          </div>

          <div class="topic-summary-title-wrapper">
            <div class="topic-summary-title">${this.topic.draft ? html`<span class="draft">[${this.i18n.draft}]</span>` : ""} ${this.topic.title}</div>
          </div>

          <div class="topic-summary-pinned-indicator">
            ${this.topic.pinned && !this.topic.bookmarked ? html`
            <div>
              <sakai-icon type="pin"
                  size="small"
                  arial-label="${this.i18n.pinned_tooltip}"
                  title="${this.i18n.pinned_tooltip}">
              </sakai-icon>
            </div>
            ` : ""}
            ${this.topic.bookmarked ? html`
            <div>
              <sakai-icon type="favourite"
                  size="small"
                  class="bookmarked"
                  arial-label="${this.i18n.bookmarked_tooltip}"
                  title="${this.i18n.bookmarked_tooltip}">
              </sakai-icon>
            </div>
            ` : ""}
            ${this.topic.locked ? html`
            <div class="topic-status"
                role="image"
                title="${this.i18n.topic_locked_tooltip}"
                aria-label="${this.i18n.topic_locked_tooltip}">
              <sakai-icon type="lock" size="small"></sakai-icon></div>
            </div>
            ` : ""}
            ${this.topic.hidden ? html`
            <div class="topic-status"
                role="image"
                title="${this.i18n.topic_hidden_tooltip}"
                aria-label="${this.i18n.topic_hidden_tooltip}">
              <sakai-icon type="hidden" size="small"></sakai-icon></div>
            </div>
            ` : ""}
          </div>
          <div class="unread-icon">
          ${this.topic.numberOfUnreadPosts > 0 ? html`
            <sakai-icon type="circle"
                size="small"
                arial-label="${this.i18n.read_tooltip}"
                title="${this.i18n.read_tooltip}">
            </sakai-icon>
          ` : ""}
          </div>

          <div class="topic-tags">
          ${this.topic.tags.map(tag => html`
            <div class="tag"><div>${tag.label}</div></div>
          `)}
          </div>

          <div>
          ${this.topic.type === QUESTION ? html`
            ${this.topic.resolved ? html`
              <sakai-icon type="check_circle"
                  size="small"
                  class="answered-icon"
                  title="${this.i18n.answered_tooltip}">
              </sakai-icon>
            ` : html`
              <sakai-icon type="questioncircle"
                  size="small"
                  class="unanswered-icon"
                  title="${this.i18n.unanswered_tooltip}">
              </sakai-icon>
            `}
          ` : ""}
          </div>
          <div class="topic-summary-empty-cell">
          </div>
          <div class="topic-summary-creator-block">
            <div>
              <span>${this.topic.creatorDisplayName}</span>
              ${this.topic.isInstructor ? html`
              <span>(${this.i18n.instructor})</span>
            ` : ""}
            </div>
            <div class="conv-date-separator"><sakai-icon type="circle" size="smallest"></sakai-icon></div>
            <div>${this.topic.formattedCreatedDate}</div>
          </div>
          <div class="topic-summary-posts-indicator"
              title="${this.i18n.numberposts_tooltip}">
            <div>
              <sakai-icon type="comment" size="smallest"></sakai-icon>
            </div>
            <div class="post-numbers">
              <span class="post-number">${this.topic.numberOfPosts}</span>
              ${this.topic.numberOfUnreadPosts > 0 ? html`
              <span class="new-posts-number">(${this.topic.numberOfUnreadPosts} ${this.i18n.new})</span>
              ` : ""}
            </div>
          </div>
        </div>
      </a>
    `;
  }
}

const tagName = "sakai-topic-summary";
!customElements.get(tagName) && customElements.define(tagName, SakaiTopicSummary);
