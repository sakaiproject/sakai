import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-user-photo.js";
import { findPost, getPostsForTopic } from "./utils.js";
import { reactionsMixin } from "./reactions-mixin.js";
import "../sakai-editor.js";
import "./sakai-post.js";
import { GROUP, INSTRUCTORS, QUESTION } from "./sakai-conversations-constants.js";
import "../sakai-icon.js";
import "./options-menu.js";

export class SakaiTopic extends reactionsMixin(SakaiElement) {

  static get properties() {

    return {
      topic: { type: Object },
      creatingPost: Boolean,
      isInstructor: { attribute: "is-instructor", type: Boolean },
      canViewAnonymous: { attribute: "can-view-anonymous", type: Boolean },
      replyEditorDisplayed: { type: Array },
      postEditorDisplayed: { type: Boolean },
    };
  }

  constructor() {

    super();

    this.replyEditorDisplayed = [];

    const options = {
      root: null,
      rootMargin: '0px',
      threshold: 1.0,
    };

    this.observer = new IntersectionObserver((entries, observer) => {

      const postIds = entries.filter(entry => entry.isIntersecting).map(entry => entry.target.dataset.postId);

      if (postIds) {
        const url = this.topic.links.find(l => l.rel === "markpostsviewed").href;
        fetch(url, {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(postIds),
        })
        .then(r => {

          if (r.ok) {
            // Posts marked. Now unobserve them. We don't want to keep triggering this fetch
            postIds.forEach(postId => observer.unobserve(entries.find(e => e.target.dataset.postId === postId).target));
            this.requestUpdate();
          } else {
            throw new Error("Network error while marking posts as viewed");
          }
        })
        .catch(error => console.error(error));
      }

    }, options);


    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set topic(value) {

    this._topic = value;
    this.postEditorDisplayed = false;
    this.myReactions = value.myReactions || {};
    if (value.posts) {
      value.posts.sort((p1, p2) => {
        if (p1.isInstructor && p2.isInstructor) return 0;
        if (p1.isInstructor && !p2.isInstructor) return -1;
        return 1;
      });
    }

    this.requestUpdate();

    if (value.posts) {

      this.updateComplete.then(() => {

        // We have to use a timeout for this to satisfy Safari. updateComplete does not fulfil after
        // the full render has happened on Safari, so the elements may well not be there.
        setTimeout(() => {

          value.posts.filter(p => !p.viewed).forEach(p => {
            this.observer.observe(document.getElementById(`post-${p.id}`));
          });
        }, 100);

      });
    }
  }

  get topic() { return this._topic; }

  shouldUpdate() {
    return this.i18n && this.topic;
  }

  toggleCreatePost() {
    this.creatingPost = !this.creatingPost;
  }

  toggleReplyToPost(e) {

    e.preventDefault();

    const postId = e.target.dataset.postId;
    this.replyEditorDisplayed[postId] = !this.replyEditorDisplayed[postId];
    this.requestUpdate();
  }

  savePostAsDraft() {
    this.postToTopic(true);
  }

  publishPost() {
    this.postToTopic(false);
  }

  postToTopic(draft) {

    this.creatingPost = false;

    const postData = {
      message: document.getElementById(`topic-${this.topic.id}-post-editor`).getContent(),
      topic: this.topic.id,
      siteId: this.topic.siteId,
      privatePost: this.privatePost,
      anonymous: this.anonymousPost,
      draft,
      replyable: true,
    };

    const url = this.topic.links.find(l => l.rel === "post").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(postData),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while posting.");
      }
      return r.json();
    })
    .then(post => {

      if (post.isInstructor) this.topic.resolved = true;

      this.topic.posts = this.topic.posts || [];
      this.topic.posts.push(post);
      this.topic.numberOfPosts += 1;
      this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
      this.postEditorDisplayed = false;
    })
    .catch (error => {
      console.error(error);
    });
  }

  editTopic(e) {

    e.preventDefault();
    this.dispatchEvent(new CustomEvent("edit-topic", { detail: { topic: this.topic }, bubbles: true }));
  }

  commentDeleted(e) {

    e.stopPropagation();

    // Recast this event with the topic id added. The data is managed by sakai-conversations.js,
    // and it needs the topic id to lookup the post with this comment.
    this.dispatchEvent(new CustomEvent("comment-deleted",
      { detail: { topicId: this.topic.id, ...e.detail }, bubbles: true }));
  }

  deleteTopic() {

    if (!confirm(this.i18n.confirm_topic_delete)) return;

    const url = this.topic.links.find(l => l.rel === "delete").href;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
    .then(r => {

      if (r.ok) {
        this.dispatchEvent(new CustomEvent("topic-deleted", { detail: { topic: this.topic }, bubbles: true }));
      } else {
        throw new Error(`Network error while deleting topic with id ${this.topic.id}`);
      }
    })
    .catch(error => console.error(error));
  }

  toggleBookmarked() {

    const url = this.topic.links.find(l => l.rel === "bookmark").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.topic.bookmarked),
    })
    .then(r => {

      if (r.ok) {
        this.topic.bookmarked = !this.topic.bookmarked;
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
      } else {
        throw new Error("Network error while bookmarking topic");
      }
    })
    .catch(error => console.error(error));
  }

  toggleLocked() {

    const url = this.topic.links.find(l => l.rel === "lock").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.topic.locked),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while locking/unlocking topic");

    })
    .then(topic => {

      topic.selected = true;
      getPostsForTopic(topic).then(posts => {

        topic.posts = posts;
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic }, bubbles: true }));
      });
    })
    .catch(error => console.error(error));
  }

  toggleHidden() {

    const url = this.topic.links.find(l => l.rel === "hide").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.topic.hidden),
    })
    .then(r => {

      if (r.ok) {
        this.topic.hidden = !this.topic.hidden;
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
      } else {
        throw new Error("Network error while hiding/showing topic");
      }
    })
    .catch(error => console.error(error));
  }

  togglePinned() {

    const url = this.topic.links.find(l => l.rel === "pin").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.topic.pinned),
    })
    .then(r => {

      if (r.ok) {
        this.topic.pinned = !this.topic.pinned;
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
      } else {
        throw new Error("Network error while pinning topic");
      }
    })
    .catch(error => console.error(error));
  }

  postReactions() {

    const url = this.topic.links.find(l => l.rel === "react").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(this.myReactions),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while posting topic.myReactions");

    })
    .then(reactionTotals => {

      this.topic.myReactions = this.myReactions;
      this.topic.reactionTotals = reactionTotals;
      this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
    })
    .catch(error => console.error(error));
  }

  postUpdated(e) {

    e.stopPropagation();

    const post = findPost(this.topic, { postId: e.detail.post.id });
    post && (Object.assign(post, e.detail.post));
    const verifiedCount = findPost(this.topic, { isInstructor: true });
    this.topic.resolved = verifiedCount > 0;

    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
  }

  postDeleted(e) {

    e.stopPropagation();

    const index = this.topic.posts.findIndex(p => p.id === e.detail.post.id);
    this.topic.posts.splice(index, 1);
    this.topic.numberOfPosts = this.topic.numberOfPosts <= 0 ? 0 : this.topic.numberOfPosts - 1;
    const verifiedCount = findPost(this.topic, { verified: true });
    this.topic.resolved = verifiedCount > 0;
    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
  }

  updated() {

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    }
  }

  renderPostEditor() {

    return html`
      <div class="conv-post-editor-wrapper">
        <div class="topic-add-post-prompt">${this.topic.type === QUESTION ? this.i18n.answer_this_question : this.i18n.post_to_this_topic}</div>
        <sakai-editor id="topic-${this.topic.id}-post-editor"></sakai-editor>
        <div class="conv-private-checkbox-block">
          <label>
            <input type="checkbox" @click=${e => this.privatePost = e.target.checked}>${this.i18n.private_reply}
          </label>
        </div>
        ${this.topic.allowAnonymousPosts ? html`
        <div class="conv-private-checkbox-block">
          <label>
            <input type="checkbox" @click=${e => this.anonymousPost = e.target.checked}>${this.i18n.post_anonymously}
          </label>
        </div>
        ` : ""}
        <div class="act">
          <input type="button" class="active" @click=${this.publishPost} value="${this.i18n.publish}">
          <input type="button" @click=${this.savePostAsDraft} value="${this.i18n.save_as_draft}">
          <input type="button" @click=${() => this.postEditorDisplayed = false} value="${this.i18n.cancel}">
        </div>
      </div>
    `;
  }

  render() {

    return html`
      <div class="topic">
        ${this.topic.draft ? html`
        <div class="sak-banner-warn">This topic is a draft.</div>
        ` : ""}
        ${this.topic.hidden ? html`
        <div class="sak-banner-warn">${this.i18n.topic_hidden}</div>
        ` : ""}
        ${this.topic.locked ? html`
        <div class="sak-banner-warn">${this.i18n.topic_locked}</div>
        ` : ""}
        ${this.topic.visibility === INSTRUCTORS ? html`
        <div class="sak-banner-warn">${this.i18n.topic_instructors_only_tooltip}</div>
        ` : ""}
        ${this.topic.visibility === GROUP ? html`
        <div class="sak-banner-warn">${this.i18n.topic_groups_only_tooltip}</div>
        ` : ""}
        <div class="topic-tags">
          ${this.topic.tags.map(tag => html`
            <div class="tag">${tag.label}</div>
          `)}
        </div>
        <div class="author-and-tools">
          <div class="author-block">
            <div><sakai-user-photo user-id="${this.topic.anonymous && !this.canViewAnonymous ? "blank" : this.topic.creator}"></sakai-user-photo></div>
            <div>
              <div class="author-details">
                <div class="topic-creator-name">${this.topic.creatorDisplayName}</div>
                <div class="topic-question-asked">${this.topic.type === QUESTION ? this.i18n.asked : this.i18n.posted}</div>
                <div class="topic-created-date">${this.topic.formattedCreatedDate}</div>
              </div>
            </div>
          </div>
          <div class="topic-options-menu">
          ${this.topic.canModerate || this.topic.canEdit || this.topic.canDelete || this.topic.canViewStatistics ? html`
            <options-menu icon="menu" placement="bottom-left">
              <div slot="trigger">
                <a href="javascript:;"
                    title="${this.i18n.topic_options_menu_tooltip}"
                    aria-haspopup="true"
                    aria-label="${this.i18n.topic_options_menu_tooltip}">
                  <sakai-icon type="menu" size="small"></sakai-icon>
                </a>
              </div>
              <div slot="content" class="options-menu" role="dialog">

                ${this.topic.canEdit ? html`
                <div>
                  <a href="javascript:;"
                      @click=${this.editTopic}
                      aria-label="${this.i18n.edit_topic_tooltip}"
                      title="${this.i18n.edit_topic_tooltip}">
                    ${this.i18n.edit}
                  </a>
                </div>
                ` : ""}

                ${this.topic.canDelete ? html`
                <div>
                  <a href="javascript:;"
                      @click=${this.deleteTopic}
                      aria-label="${this.i18n.delete_topic_tooltip}"
                      title="${this.i18n.delete_topic_tooltip}">
                    ${this.i18n.delete}
                    </a>
                </div>
                ` : ""}

                ${this.topic.canModerate ? html`
                <div>
                  <a href="javascript:;"
                      aria-label="${this.i18n[this.topic.hidden ? "show_topic_tooltip" : "hide_topic_tooltip"]}"
                      title="${this.i18n[this.topic.hidden ? "show_topic_tooltip" : "hide_topic_tooltip"]}"
                      @click=${this.toggleHidden}>
                    ${this.i18n[this.topic.hidden ? "show" : "hide"]}
                  </a>
                </div>
                ` : ""}
                ${this.topic.canModerate ? html`
                <div>
                  <a href="javascript:;"
                      aria-label="${this.i18n[this.topic.locked ? "unlock_topic_tooltip" : "lock_topic_tooltip"]}"
                      title="${this.i18n[this.topic.locked ? "unlock_topic_tooltip" : "lock_topic_tooltip"]}"
                      @click=${this.toggleLocked}>
                    ${this.i18n[this.topic.locked ? "unlock" : "lock"]}
                  </a>
                </div>
                ` : ""}
                ${this.topic.canViewStatistics ? html`
                <div>
                  <a href="javascript:;"
                      @click=${this.showStatistics}>
                    ${this.i18n.view_statistics}
                  </a>
                </div>
                ` : ""}
              </div>
            </options-menu>
            ` : ""}
          </div>
        </div>
        <div class="topic-title-and-status">
          <div class="topic-title-wrapper">
            <div class="topic-title">${this.topic.title}</div>
          </div>
          ${this.topic.type === QUESTION ? html`
          <div class="topic-status-icon-and-text">
            <div class="topic-status-icon">
              ${this.topic.resolved ? html`
                <sakai-icon type="check_circle"
                    class="answered-icon"
                    title="${this.i18n.answered_tooltip}">
                </sakai-icon>
              ` : html`
                <sakai-icon type="questioncircle"
                    class="unanswered-icon"
                    title="${this.i18n.unanswered_tooltip}">
                </sakai-icon>
              `}
            </div>
            <div class="topic-status-text">${this.i18n[this.topic.resolved ? "answered" : "unanswered"]}</div>
          </div>
          ` : ""}
        </div>
        <div class="topic-message">${unsafeHTML(this.topic.message)}</div>
        ${this.topic.draft ? "" : html`
        <div class="topic-message-bottom-bar">
          ${this.topic.canBookmark ? html`
          <div>
            <a href="javascript:;"
                @click=${this.toggleBookmarked}
                aria-label="${this.i18n[this.topic.bookmarked ? "unbookmark_tooltip" : "bookmark_tooltip"]}"
                title="${this.i18n[this.topic.bookmarked ? "unbookmark_tooltip" : "bookmark_tooltip"]}"
            >
              <div class="topic-option">
                <div><sakai-icon type="favourite" size="small"></sakai-icon></div>
                <div>
                  ${this.i18n[this.topic.bookmarked ? "unbookmark" : "bookmark"]}
                </div>
              </div>
            </a>
          </div>
          ` : ""}
          ${this.topic.canPin ? html`
          <div>
            <a href="javascript:;"
                @click=${this.togglePinned}
                aria-label="${this.topic.pinned ? this.i18n.unpin_tooltip : this.i18n.pin_tooltip}"
                title="${this.topic.pinned ? this.i18n.unpin_tooltip : this.i18n.pin_tooltip}">
              <div class="topic-option">
                <div><sakai-icon type="pin" size="small"></sakai-icon></div>
                <div>
                  ${this.i18n[this.topic.pinned ? "unpin" : "pin"]}
                </div>
              </div>
            </a>
          </div>
          ` : ""}
          ${this.topic.type === QUESTION ? html`
          <div>
            <div class="topic-option
                  ${this.topic.myReactions.GOOD_QUESTION ? "good-question-on" : ""}
                  ${this.topic.isMine && this.topic.reactionTotals.GOOD_QUESTION > 0 ? "reaction-on" : ""}"
            >
              ${this.topic.isMine && this.topic.reactionTotals.GOOD_QUESTION > 0 ? html`
                <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
                <div>
                ${this.i18n.goodquestion} ${this.isInstructor && this.topic.reactionTotals.GOOD_QUESTION ? ` - ${this.topic.reactionTotals.GOOD_QUESTION}` : ""}
                </div>
              ` : ""}
              ${!this.topic.isMine ? html`
              <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
              <div>
                <a href="javascript:;"
                    data-reaction="GOOD_QUESTION"
                    @click=${this.toggleReaction}
                    aria-label="${this.topic.myReactions.GOOD_QUESTION ? this.i18n.ungoodquestion_tooltip : this.i18n.goodquestion_tooltip}"
                    title="${this.topic.myReactions.GOOD_QUESTION ? this.i18n.ungoodquestion_tooltip : this.i18n.goodquestion_tooltip}">
                  ${this.i18n.goodquestion} ${this.isInstructor && this.topic.reactionTotals.GOOD_QUESTION ? ` - ${this.topic.reactionTotals.GOOD_QUESTION}` : ""}
                </a>
              </div>
              ` : ""}
            </div>
          <div>
          ` : ""}
          ${this.topic.canReact ? html`
          <div>
          <div class="topic-option">
            <options-menu placement="bottom">
              <div class="topic-option" slot="trigger">
                <div><sakai-icon type="smile" size="small"></sakai-icon></div>
                <div id="my-reactions-link-${this.topic.id}">
                  <a href="javascript:;"
                      @click=${() => this.showingMyReactions = !this.showingMyReactions}
                      aria-label="${this.i18n.reactions_tooltip}"
                      aria-haspopup="true"
                      title="${this.i18n.reactions_tooltip}">
                    ${this.i18n.add_a_reaction}
                  </a>
                </div>
              </div>
              <div slot="content">
                <div class="topic-reactions-popup" role="dialog">
                  ${this.renderMyReactions(this.topic.myReactions)}
                </div>
              </div>
            </options-menu>
          </div>
          </div>
          ` : ""}
          <div class="topic-option">
          </div>
        </div>
        ${this.renderReactionsBar(this.topic.reactionTotals)}
        ${this.topic.posts && this.topic.posts.length > 0 ? html `
          <div class="topic-posts-block">
            ${this.topic.type === QUESTION ? html`
            <div class="topic-posts-header">ANSWERS</div>
            ` : ""}
            ${this.topic.posts.map(r => html`
              ${r.canView ? html`
              <sakai-post
                  post="${JSON.stringify(r)}"
                  ?is-instructor="${this.isInstructor}"
                  ?can-view-anonymous="${this.canViewAnonymous}"
                  site-id="${this.topic.siteId}"
                  @post-updated=${this.postUpdated}
                  @post-deleted=${this.postDeleted}
                  @comment-deleted=${this.commentDeleted}></sakai-post>
              ` : ""}
            `)}
          </div>
          ${this.postEditorDisplayed ? this.renderPostEditor() : html`
            ${this.topic.canPost ? html`
            <div class="act">
              <input
                type="button"
                class="active"
                @click=${() => this.postEditorDisplayed = true}
                value="${this.topic.type === QUESTION ? this.i18n.add_an_answer : this.i18n.add_a_post}">
            </div>
            ` : ""}
          `}
        ` : html`
          ${this.postEditorDisplayed ? html`
          <div>
            ${this.renderPostEditor()}
          </div>
          ` : html`
          <div class="topic-no-replies-block">
            <div class="topic-no-replies-message">
              ${this.topic.type === QUESTION ? this.i18n.no_answers_yet : this.i18n.no_posts_yet}
            </div>
            ${this.topic.canPost ? html`
            <a href="javascript:;" @click=${() => this.postEditorDisplayed = true}>
              <div class="topic-reply-button-block">
                ${this.topic.type === QUESTION ? this.i18n.answer_this_question : this.i18n.post_to_this_topic}
              </div>
            </a>
            ` : ""}
          </div>
          `}
        `}
      `}
      </div>
    `;
  }
}

if (!customElements.get("sakai-topic")) {
  customElements.define("sakai-topic", SakaiTopic);
}
