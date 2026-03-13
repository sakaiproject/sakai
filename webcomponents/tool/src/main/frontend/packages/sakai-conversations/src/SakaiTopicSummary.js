import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { TopicMenuMixin }  from "./TopicMenuMixin.js";
import "@sakai-ui/sakai-icon";
import { QUESTION, DISCUSSION } from "./sakai-conversations-constants.js";

export class SakaiTopicSummary extends TopicMenuMixin(SakaiElement) {

  static properties = { topic: { type: Object } };

  constructor() {

    super();

    this.loadTranslations("conversations");
  }

  _topicSelected() {
    this.dispatchEvent(new CustomEvent("topic-selected", { detail: { topic: this.topic }, bubbles: true }));
  }

  shouldUpdate() {
    return this._i18n && this.topic;
  }

  render() {

    return html`
      <div class="topic-summary ${this.topic.numberOfUnreadPosts > 0 ? " unread" : ""}">
        <div class="type-icon">
          ${this.topic.type === QUESTION ? html`
            <sakai-icon type="questioncircle"
                class="question-icon"
                size="medium"
                aria-label="${this._i18n.question_tooltip}"
                title="${this._i18n.question_tooltip}">
            </sakai-icon>
          ` : nothing }
          ${this.topic.type === DISCUSSION ? html`
          <div class="discussion-icon-wrapper">
            <sakai-icon type="forums"
                class="discussion-icon"
                size="small"
                aria-label="${this._i18n.discussion_tooltip}"
                title="${this._i18n.discussion_tooltip}">
            </sakai-icon>
          </div>
          ` : nothing }
        </div>

        <div class="topic-summary-title-wrapper">
          <a href="javascript:;" @click=${this._topicSelected} class="topic-summary-link">
            <div class="topic-summary-title">${this.topic.draft ? html`<span class="draft">[${this._i18n.draft}]</span>` : nothing } ${this.topic.title}</div>
          </a>
        </div>

        <div class="topic-summary-pinned-indicator">
          ${this.topic.pinned && !this.topic.bookmarked ? html`
          <div>
            <sakai-icon type="pin"
                size="small"
                aria-label="${this._i18n.pinned_tooltip}"
                title="${this._i18n.pinned_tooltip}">
            </sakai-icon>
          </div>
          ` : nothing }
          ${this.topic.bookmarked ? html`
          <div>
            <i class="si si-bookmark-fill"
                aria-label="${this._i18n.bookmarked_tooltip}"
                title="${this._i18n.bookmarked_tooltip}">
            </i>
          </div>
          ` : nothing }
          ${this.topic.locked ? html`
          <div class="topic-status"
              role="img"
              title="${this._i18n.topic_locked_tooltip}"
              aria-label="${this._i18n.topic_locked_tooltip}">
            <sakai-icon type="lock" size="small"></sakai-icon></div>
          </div>
          ` : nothing }
          ${this.topic.hidden ? html`
          <div class="topic-status"
              role="img"
              title="${this._i18n.topic_hidden_tooltip}"
              aria-label="${this._i18n.topic_hidden_tooltip}">
            <sakai-icon type="hidden" size="small"></sakai-icon></div>
          </div>
          ` : nothing }
          ${this._renderMenu()}
        </div>

        ${this.topic.formattedDueDate ? html`
        <div></div>
        <div class="topic-summary-duedate"><span>${this._i18n.due}</span><span>${this.topic.formattedDueDate}</span></div>
        <div></div>
        ` : nothing }

        <div class="unread-icon">
        ${this.topic.numberOfUnreadPosts > 0 ? html`
          <sakai-icon type="circle"
              size="small"
              aria-label="${this._i18n.unread_tooltip}"
              title="${this._i18n.unread_tooltip}">
          </sakai-icon>
        ` : nothing }
        </div>

        <div class="topic-tags">
        ${this.topic.tags?.map(tag => html`
          <div class="tag"><div>${tag.label}</div></div>
        `)}
        </div>

        <div>
        ${this.topic.type === QUESTION ? html`
          ${this.topic.resolved ? html`
            <sakai-icon type="check_circle"
                size="small"
                class="answered-icon"
                title="${this._i18n.answered_tooltip}">
            </sakai-icon>
          ` : html`
            <sakai-icon type="questioncircle"
                size="small"
                class="unanswered-icon"
                title="${this._i18n.unanswered_tooltip}">
            </sakai-icon>
          `}
        ` : nothing }
        </div>
        <div class="topic-summary-empty-cell">
        </div>
        <div class="topic-summary-creator-block">
          <div>
            <span>${this.topic.creatorDisplayName}</span>
            ${this.topic.isInstructor ? html`
            <span>(${this._i18n.instructor})</span>
          ` : nothing }
          </div>
          <div class="conv-date-separator"><sakai-icon type="circle" size="smallest"></sakai-icon></div>
          <div>${this.topic.formattedCreatedDate}</div>
        </div>
        <div class="topic-summary-posts-indicator"
            title="${this._i18n.numberposts_tooltip}">
          <div>
            <sakai-icon type="comment" size="smallest"></sakai-icon>
          </div>
          <div class="post-numbers">
            <span class="post-number">${this.topic.numberOfPosts}</span>
            ${this.topic.numberOfUnreadPosts > 0 ? html`
            <span class="new-posts-number">(${this.topic.numberOfUnreadPosts} ${this._i18n.new})</span>
            ` : nothing }
          </div>
        </div>
      </div>
    `;
  }
}
