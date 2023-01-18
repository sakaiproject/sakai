import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-user-photo.js";
import { findPost, markThreadViewed } from "./utils.js";
import { reactionsMixin } from "./reactions-mixin.js";
import "../sakai-editor.js";
import "./sakai-post.js";
import { GROUP, INSTRUCTORS, DISCUSSION, QUESTION, SORT_OLDEST, SORT_NEWEST, SORT_ASC_CREATOR, SORT_DESC_CREATOR, SORT_MOST_ACTIVE, SORT_LEAST_ACTIVE } from "./sakai-conversations-constants.js";
import "../sakai-icon.js";
import "./options-menu.js";

export class SakaiTopic extends reactionsMixin(SakaiElement) {

  static get properties() {

    return {
      topic: { type: Object },
      postId: { attribute: "post-id", type: String },
      creatingPost: Boolean,
      isInstructor: { attribute: "is-instructor", type: Boolean },
      canViewAnonymous: { attribute: "can-view-anonymous", type: Boolean },
      canViewDeleted: { attribute: "can-view-deleted", type: Boolean },
      replyEditorDisplayed: { type: Array },
      postEditorDisplayed: { type: Boolean },
      replying: { type: Boolean },
    };
  }

  constructor() {

    super();

    this.replyEditorDisplayed = [];

    this.sort = SORT_OLDEST;

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

    const sortAndUpdate = () => {

      if (this.topic.type === QUESTION) {
        this.topic.posts.sort((p1, p2) => {

          if (p1.isInstructor && p2.isInstructor) return 0;
          if (p1.isInstructor && !p2.isInstructor) return -1;
          return 1;
        });
      }

      this.page = 0;

      if (this.postId) {
        const post = findPost(this.topic, { postId: this.postId });
        if (post.parentThread) {
          const thread = findPost(this.topic, { postId: post.parentThread });
          thread.keepExpanded = true;
        }
      }

      this.requestUpdate();

      this.updateComplete.then(() => {

        if (this.postId) {
          const el = this.querySelector(`#post-${this.postId}`);
          el && (el.scrollIntoView());
        }

        // We have to use a timeout for this to satisfy Safari. updateComplete does not fulfil after
        // the full render has happened on Safari, so the elements may well not be there.
        setTimeout(() => { this.registerPosts(this.topic.posts); }, 100);
      });
    };

    if (!this.topic.posts && (!this.topic.mustPostBeforeViewing || this.topic.canPost)) {
      this.getPosts(this.topic, 0, SORT_OLDEST, this.postId).then(posts => {

        this.topic.posts = posts;
        sortAndUpdate();
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic, dontUpdateCurrent: true }, bubbles: true }));
      });
    } else {
      sortAndUpdate();
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
      privatePost: document.getElementById("conv-post-editor-private-checkbox").checked,
      anonymous: this.topic.allowAnonymousPosts && document.getElementById("conv-post-editor-anonymous-checkbox").checked,
      draft,
      replyable: true,
    };

    const url = this.topic.links.find(l => l.rel === "posts").href;
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
      if (this.topic.mustPostBeforeViewing && !this.topic.hasPosted) {
        // This user must have just posted to a post before viewing topic. We
        // need to load up the posts.
        this.getPosts(this.topic, 0, this.sort).then(posts => {

          this.topic.posts = posts;
          this.topic.hasPosted = true;
          this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
        });
      } else {
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
      }
      this.postEditorDisplayed = false;
      this.replying = false;


      this.updateComplete.then(() => {

        const el = this.querySelector(`#post-${post.id}`);
        el && (el.scrollIntoView());
      });
    })
    .catch (error => console.error(error));
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
      this.getPosts(topic).then(posts => {

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

    if (this.topic.continued) {
      // We need this as we may be viewing a continued slice of the threaded posts. In other words
      // we have two copies of the post in circulation, one in allPosts, one in posts.
      const currentPost = findPost(this.topic, { postId: e.detail.post.id, postsInView: true });
      currentPost && (Object.assign(currentPost, e.detail.post));
    }

    if (post.isThread) {
      post.keepExpanded = true;
    }

    if (post.parentThread) {
      const thread = findPost(this.topic, { postId: post.parentThread });
      thread.keepExpanded = true;
      if (e.detail.created && !e.detail.post.draft) {
        thread.numberOfThreadReplies += 1;
      }
    }

    if (this.topic.type === QUESTION) {
      const numberOfInstructorPosts = findPost(this.topic, { isInstructor: true });
      this.topic.resolved = numberOfInstructorPosts  > 0;
    }

    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
  }

  postDeleted(e) {

    e.stopPropagation();

    let index = this.topic.posts.findIndex(p => p.id === e.detail.post.id);
    if (index !== -1) {
      // This is a thread post, a direct reply to the topic.
      this.topic.posts.splice(index, 1);
    } else {
      // This is a reply to a post, so it has a parent
      const parentPost = findPost(this.topic, { postId: e.detail.post.parentPost });
      index = parentPost.posts.findIndex(p => p.id === e.detail.post.id);
      parentPost.posts.splice(index, 1);

      // Update the read status of the top level thread
      const thread = findPost(this.topic, { postId: e.detail.post.parentThread });
      thread && (markThreadViewed(thread));
      thread && (thread.numberOfThreadReplies -= 1);
    }

    this.topic.numberOfPosts = this.topic.numberOfPosts <= 0 ? 0 : this.topic.numberOfPosts - 1;

    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
  }

  continueThread(e) {

    e.detail.post.continued = true;
    const thread = findPost(this.topic, { postId: e.detail.post.parentThread });
    thread.keepExpanded = true;
    if (!this.topic.allPosts) {
      this.topic.allPosts = [...this.topic.posts];
    }
    this.topic.posts = [e.detail.post];
    this.topic.continued = true;
    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
    this.updateComplete.then(() => {

      const el = this.querySelector(`#post-${e.detail.post.id}`);
      el && (el.scrollIntoView());
    });
  }

  viewAllPosts() {

    this.topic.posts = [...this.topic.allPosts];
    this.topic.allPosts = undefined;
    this.topic.continued = false;
    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
  }

  registerPosts(posts) {

    posts.forEach(p => {

      if (!p.viewed) {
        this.observer.observe(this.querySelector(`#post-${p.id}`));
      }

      if (p.posts) {
        this.registerPosts(p.posts);
      }
    });
  }

  getMoreReplies() {

    this.page += 1;

    this.getPosts(this.topic, this.page, this.sort).then(posts => {

      this.topic.posts = this.topic.posts.concat(posts);
      this.requestUpdate();
      this.updateComplete.then(() => {
        setTimeout(() => { this.registerPosts(posts); }, 100);
      });

    });
  }

  postSortSelected(e) {

    this.sort = e.target.value;
    this.page = 0;

    this.getPosts(this.topic, this.page, this.sort).then(posts => {

      this.topic.posts = posts;
      this.requestUpdate();
    });
  }

  getPosts(topic, page = 0, sort = SORT_OLDEST, postId) {

    const url = `${topic.links.find(l => l.rel === "posts").href}?page=${page}&sort=${sort}${
       postId ? `&postId=${postId}` : ""}`;

    return fetch(url, { credentials: "include" })
    .then(r => {

      if (!r.ok) {
        throw new Error(`Network error while retrieving  posts for topic ${topic.id}`);
      } else {
        return r.json();
      }
    })
    .catch(error => console.error(error));
  }

  _unsetReplying() { this.replying = false; }

  _setReplying() { this.replying = true; }

  _toggleShowingMyReactions() { this.showingMyReactions = !this.showingMyReactions; }

  updated() {

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    }
  }

  renderPostEditor() {

    return html`
      <div class="conv-post-editor-wrapper">
        <div class="conv-post-editor-header">
          <span>${this.topic.type === QUESTION ? this.i18n.answer_this_question : this.i18n.reply_to}</span>
          <span>${this.topic.title}</span>
        </div>
        <sakai-editor id="topic-${this.topic.id}-post-editor" set-focus></sakai-editor>
        <div class="conv-private-checkbox-block">
          <label>
            <input id="conv-post-editor-private-checkbox" type="checkbox">${this.i18n.private_topic_reply}
          </label>
        </div>
        ${this.topic.allowAnonymousPosts ? html`
        <div class="conv-private-checkbox-block">
          <label>
            <input id="conv-post-editor-anonymous-checkbox" type="checkbox">${this.i18n.post_anonymously}
          </label>
        </div>
        ` : ""}
        <div class="act">
          <input type="button" class="active" @click=${this.publishPost} value="${this.i18n.publish}">
          <input type="button" @click=${this.savePostAsDraft} value="${this.i18n.save_as_draft}">
          <input type="button" @click="${this._unsetReplying}" value="${this.i18n.cancel}">
        </div>
      </div>
    `;
  }

  render() {

    return html`
      <div class="topic">
        ${this.topic.draft ? html`
        <div class="sak-banner-warn">${this.i18n.draft_warning}</div>
        ` : ""}
        ${this.topic.hidden ? html`
        <div class="sak-banner-warn">${this.i18n.topic_hidden}</div>
        ` : ""}
        ${this.topic.locked ? html`
        <div class="sak-banner-warn">${this.topic.canModerate ? this.i18n.moderator_topic_locked : this.i18n.topic_locked}</div>
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
        ${this.topic.formattedDueDate ? html`
        <div id="topic-duedate-block"><span>${this.i18n.duedate_label}</span><span>${this.topic.formattedDueDate}</span></div>
        ` : ""}
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
          </div>
          ` : ""}
          ${this.topic.canReact ? html`
          <div>
            <div class="topic-option">
              <options-menu placement="bottom">
                <div class="topic-option" slot="trigger">
                  <div><sakai-icon type="smile" size="small"></sakai-icon></div>
                  <div id="my-reactions-link-${this.topic.id}">
                    <a href="javascript:;"
                        @click="${this._toggleShowingMyReactions}"
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
          <div>
            <div class="topic-option">
            </div>
          </div>
        </div>
        ${this.renderReactionsBar(this.topic.reactionTotals)}
        <hr>
        ${!this.topic.continued && !this.topic.locked ? html`
        <div class="topic-reply-block ${!this.replying ? "padded" : ""}">
            ${this.topic.pastDueDate ? html`
            <div class="sak-banner-warn">${this.i18n.duedate_passed_info}</div>
            ` : ""}
            ${this.topic.mustPostBeforeViewing && !this.topic.canPost ? html`
            <div class="sak-banner-warn">${this.i18n.post_before_viewing_message}</div>
            ` : ""}
            ${this.replying ? html`
              ${this.renderPostEditor()}
            ` : html`
            <a href="javascript:;" @click="${this._setReplying}">
              <div class="placeholder">
                <div><sakai-user-photo user-id="${window.top.portal.user.id}"></sakai-user-photo></div>
                <div>${this.topic.type === QUESTION ? this.i18n.answer_this_question : this.i18n.reply_to}</div>
                <div>${this.topic.title}</div>
              </div>
            </a>
            `}
          </div>
        </div>
        ` : ""}
        ${this.topic.posts && this.topic.posts.length > 0 ? html `
          <div class="topic-posts-block">
            ${!this.topic.continued ? html`
            <div class="topic-posts-header">
              <div>${this.topic.type === QUESTION ? this.i18n.answers : this.i18n.responses}</div>
              ${this.topic.type === DISCUSSION ? html`
              <div>
                <select @change=${this.postSortSelected}>
                  <option value="${SORT_OLDEST}">oldest</option>
                  <option value="${SORT_NEWEST}">most recent</option>
                  <option value="${SORT_ASC_CREATOR}">ascending author</option>
                  <option value="${SORT_DESC_CREATOR}">descending author</option>
                  <option value="${SORT_MOST_ACTIVE}">most active</option>
                  <option value="${SORT_LEAST_ACTIVE}">least active</option>
                </select>
              </div>
              ` : ""}
            </div>
            ` : ""}

            ${this.topic.continued ? html`
            <div id="conv-back-button-block">
              <a href="javascript:;" @click=${this.viewAllPosts}>
                <div><sakai-icon type="left-arrow"></sakai-icon></div>
                <div>${this.i18n.back_to_all}</div>
              </a>
            </div>
            ` : ""}
            ${this.topic.posts.map(p => html`
              ${p.canView ? html`
              <sakai-post
                  post="${JSON.stringify(p)}"
                  postType="${this.topic.type}"
                  ?is-instructor="${this.isInstructor}"
                  ?can-view-anonymous="${this.canViewAnonymous}"
                  ?can-view-deleted="${this.canViewDeleted}"
                  site-id="${this.topic.siteId}"
                  @post-updated=${this.postUpdated}
                  @post-deleted=${this.postDeleted}
                  @continue-thread=${this.continueThread}
                  @comment-deleted=${this.commentDeleted}></sakai-post>
              ` : ""}
            `)}
          ${this.topic.numberOfThreads > this.topic.posts.length && !this.topic.continued ? html`
          <div class="topic-more-replies-block">
            <a href="javascript:;" @click=${this.getMoreReplies}>${this.topic.type === QUESTION ? this.i18n.more_answers : this.i18n.more_replies}</a>
          </div>
          ` : ""}
          </div>
        ` : html`
          ${this.postEditorDisplayed ? html`
          <div>
            ${this.renderPostEditor()}
          </div>
          ` : html`
          `}
        `}
      `}
      </div>
    `;
  }
}

const tagName = "sakai-topic";
!customElements.get(tagName) && customElements.define(tagName, SakaiTopic);
