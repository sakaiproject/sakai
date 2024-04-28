import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-user-photo";
import { findPost, markThreadViewed } from "./utils.js";
import { reactionsMixin } from "./reactions-mixin.js";
import "@sakai-ui/sakai-editor/sakai-editor.js";
import "../sakai-post.js";
import { GROUP, INSTRUCTORS, DISCUSSION, QUESTION, SORT_OLDEST, SORT_NEWEST, SORT_ASC_CREATOR, SORT_DESC_CREATOR, SORT_MOST_ACTIVE, SORT_LEAST_ACTIVE } from "./sakai-conversations-constants.js";
import "@sakai-ui/sakai-icon";

export class SakaiTopic extends reactionsMixin(SakaiElement) {

  static properties = {

    topic: { type: Object },
    postId: { attribute: "post-id", type: String },
    isInstructor: { attribute: "is-instructor", type: Boolean },
    canViewAnonymous: { attribute: "can-view-anonymous", type: Boolean },
    canViewDeleted: { attribute: "can-view-deleted", type: Boolean },

    _postEditorDisplayed: { state: true },
    _replying: { state: true },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this.sort = SORT_OLDEST;

    const options = {
      root: null,
      rootMargin: "0px",
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
            postIds.forEach(postId => {

              observer.unobserve(entries.find(e => e.target.dataset.postId === postId).target);
              findPost(this.topic, { postId }).viewed = true;
              this.requestUpdate();
            });
            this.dispatchEvent(new CustomEvent("posts-viewed", { detail: { postIds, topicId: this.topic.id } }));
          } else {
            throw new Error("Network error while marking posts as viewed");
          }
        })
        .catch(error => console.error(error));
      }

    }, options);


    this.loadTranslations("conversations").then(r => this._i18n = r);
  }

  set topic(value) {

    const oldValue = this._topic;

    this._topic = value;

    this._postEditorDisplayed = false;

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

      this.requestUpdate("topic", oldValue);

      this.updateComplete.then(() => {

        if (this.postId) {
          const el = this.querySelector(`#post-${this.postId}`);
          el && (el.scrollIntoView());
        }

        // We have to use a timeout for this to satisfy Safari. updateComplete does not fulfil after
        // the full render has happened on Safari, so the elements may well not be there.
        setTimeout(() => { this._registerPosts(this.topic.posts); }, 100);
      });
    };

    if (!this.topic.posts && (!this.topic.mustPostBeforeViewing || this.topic.canPost)) {
      this._getPosts(this.topic, 0, SORT_OLDEST, this.postId).then(posts => {

        this.topic.posts = posts;

        // We've clicked on a topic and it has no posts. Ergo, it has been "viewed".
        if (!this.topic.posts.length) this.topic.viewed = true;

        sortAndUpdate();
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic, dontUpdateCurrent: true }, bubbles: true }));
      });
    } else {
      sortAndUpdate();
    }
  }

  get topic() { return this._topic; }

  _savePostAsDraft() { this._postToTopic(true); }

  _publishPost() { this._postToTopic(false); }

  _postToTopic(draft) {

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
        this._getPosts(this.topic, 0, this.sort).then(posts => {

          this.topic.posts = posts;
          this.topic.hasPosted = true;
          this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
        });
      } else {
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
      }
      this._postEditorDisplayed = false;
      this._replying = false;

      this.updateComplete.then(() => {

        const el = this.querySelector(`#post-${post.id}`);
        el && (el.scrollIntoView());
      });
    })
    .catch (error => console.error(error));
  }

  _editTopic(e) {

    e.preventDefault();
    this.dispatchEvent(new CustomEvent("edit-topic", { detail: { topic: this.topic }, bubbles: true }));
  }

  _commentDeleted(e) {

    e.stopPropagation();

    // Recast this event with the topic id added. The data is managed by sakai-conversations.js,
    // and it needs the topic id to lookup the post with this comment.
    this.dispatchEvent(new CustomEvent("comment-deleted",
      { detail: { topicId: this.topic.id, ...e.detail }, bubbles: true }));
  }

  _deleteTopic() {

    if (!confirm(this._i18n.confirm_topic_delete)) return;

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

  _toggleBookmarked() {

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

  _toggleLocked() {

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
      this._getPosts(topic).then(posts => {

        topic.posts = posts;
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic }, bubbles: true }));
      });
    })
    .catch(error => console.error(error));
  }

  _toggleHidden() {

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

  _togglePinned() {

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

  _postReactions() {

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

  _postUpdated(e) {

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

    if (e.detail.created) this.topic.numberOfPosts += 1;

    if (post.parentThread) {
      const thread = findPost(this.topic, { postId: post.parentThread });
      thread.keepExpanded = true;
      if (e.detail.created && !e.detail.post.draft) {
        thread.numberOfThreadReplies += 1;
      }
    }

    if (this.topic.type === QUESTION) {
      const numberOfInstructorPosts = findPost(this.topic, { isInstructor: true });
      this.topic.resolved = numberOfInstructorPosts > 0;
    }

    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
  }

  _postDeleted(e) {

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

  _continueThread(e) {

    e.detail.post.continued = true;
    const thread = findPost(this.topic, { postId: e.detail.post.parentThread });
    thread.keepExpanded = true;
    if (!this.topic.allPosts) {
      this.topic.allPosts = [ ...this.topic.posts ];
    }
    this.topic.posts = [ e.detail.post ];
    this.topic.continued = true;
    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
    this.updateComplete.then(() => {

      const el = this.querySelector(`#post-${e.detail.post.id}`);
      el && (el.scrollIntoView());
    });
  }

  _viewAllPosts() {

    this.topic.posts = [ ...this.topic.allPosts ];
    this.topic.allPosts = undefined;
    this.topic.continued = false;
    this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic }, bubbles: true }));
  }

  _registerPosts(posts) {

    if (posts) {
      posts.forEach(p => {

        if (!p.viewed) {
          this.observer.observe(this.querySelector(`#post-${p.id}`));
        }

        if (p.posts) {
          this._registerPosts(p.posts);
        }
      });
    }
  }

  _getMoreReplies() {

    this.page += 1;

    this._getPosts(this.topic, this.page, this.sort).then(posts => {

      this.topic.posts = this.topic.posts.concat(posts);
      this.requestUpdate();
      this.updateComplete.then(() => {
        setTimeout(() => { this._registerPosts(posts); }, 100);
      });

    });
  }

  _postSortSelected(e) {

    this.sort = e.target.value;
    this.page = 0;

    this._getPosts(this.topic, this.page, this.sort).then(posts => {

      this.topic.posts = posts;
      this.requestUpdate();
    });
  }

  _getPosts(topic, page = 0, sort = SORT_OLDEST, postId) {

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

  _unsetReplying() { this._replying = false; }

  _setReplying() { this._replying = true; }

  _toggleShowingMyReactions() { this.showingMyReactions = !this.showingMyReactions; }

  _renderPostEditor() {

    return html`
      <div class="conv-post-editor-wrapper">
        <div class="conv-post-editor-header">
          <span>${this.topic.type === QUESTION ? this._i18n.answer_this_question : this._i18n.reply_to}</span>
          <span>${this.topic.title}</span>
        </div>
        <sakai-editor id="topic-${this.topic.id}-post-editor" set-focus></sakai-editor>
        <div class="conv-private-checkbox-block">
          <label>
            <input id="conv-post-editor-private-checkbox" type="checkbox">${this._i18n.private_topic_reply}
          </label>
        </div>
        ${this.topic.allowAnonymousPosts ? html`
        <div class="conv-private-checkbox-block">
          <label>
            <input id="conv-post-editor-anonymous-checkbox" type="checkbox">${this._i18n.post_anonymously}
          </label>
        </div>
        ` : nothing }
        <div class="act">
          <input type="button" class="active" @click=${this._publishPost} value="${this._i18n.publish}">
          <input type="button" @click=${this._savePostAsDraft} value="${this._i18n.save_as_draft}">
          <input type="button" @click="${this._unsetReplying}" value="${this._i18n.cancel}">
        </div>
      </div>
    `;
  }

  shouldUpdate() {
    return this._i18n && this.topic;
  }

  updated() {

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
    }
  }

  render() {

    return html`
      <div class="topic">
        ${this.topic.draft ? html`
        <div class="sak-banner-warn">${this._i18n.draft_warning}</div>
        ` : nothing }
        ${this.topic.hidden ? html`
        <div class="sak-banner-warn">${this._i18n.topic_hidden}</div>
        ` : nothing }
        ${this.topic.locked ? html`
        <div class="sak-banner-warn">${this.topic.canModerate ? this._i18n.moderator_topic_locked : this._i18n.topic_locked}</div>
        ` : nothing }
        ${this.topic.visibility === INSTRUCTORS ? html`
        <div class="sak-banner-warn">${this._i18n.topic_instructors_only_tooltip}</div>
        ` : nothing }
        ${this.topic.visibility === GROUP ? html`
        <div class="sak-banner-warn">${this._i18n.topic_groups_only_tooltip}</div>
        ` : nothing }
        <div class="topic-tags">
          ${this.topic.tags.map(tag => html`
            <div class="tag">${tag.label}</div>
          `)}
        </div>
        <div class="author-and-tools">
          <div class="author-block">
            <div><sakai-user-photo user-id="${this.topic.anonymous && !this.canViewAnonymous ? "blank" : this.topic.creator}" profile-popup="on"></sakai-user-photo></div>
            <div>
              <div class="author-details">
                <div class="topic-creator-name">${this.topic.creatorDisplayName}</div>
                <div class="topic-question-asked">${this.topic.type === QUESTION ? this._i18n.asked : this._i18n.posted}</div>
                <div class="topic-created-date">${this.topic.formattedCreatedDate}</div>
              </div>
            </div>
          </div>
          <div class="topic-options-menu">
          ${this.topic.canModerate || this.topic.canEdit || this.topic.canDelete || this.topic.canViewStatistics ? html`
            <div class="dropdown">
              <button class="btn btn-transparent"
                  id="topic-options-toggle-${this.topic.id}"
                  type="button"
                  title="${this._i18n.topic_options_menu_tooltip}"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                  aria-haspopup="true"
                  aria-label="${this._i18n.topic_options_menu_tooltip}">
                <sakai-icon type="menu" size="small"></sakai-icon>
              </button>
              <ul class="dropdown-menu conv-dropdown-menu"
                  aria-labelledby="topic-options-toggle-${this.topic.id}">

                ${this.topic.canEdit ? html`
                <li>
                  <button type="button"
                      class="dropdown-item"
                      @click=${this._editTopic}
                      aria-label="${this._i18n.edit_topic_tooltip}"
                      title="${this._i18n.edit_topic_tooltip}">
                    ${this._i18n.edit}
                  </button>
                </li>
                ` : nothing }

                ${this.topic.canDelete ? html`
                <li>
                  <button type="button"
                      class="dropdown-item"
                      @click=${this._deleteTopic}
                      aria-label="${this._i18n.delete_topic_tooltip}"
                      title="${this._i18n.delete_topic_tooltip}">
                    ${this._i18n.delete}
                  </button>
                </li>
                ` : nothing }

                ${this.topic.canModerate ? html`
                <li>
                  <button type="button"
                      class="dropdown-item"
                      aria-label="${this._i18n[this.topic.hidden ? "show_topic_tooltip" : "hide_topic_tooltip"]}"
                      title="${this._i18n[this.topic.hidden ? "show_topic_tooltip" : "hide_topic_tooltip"]}"
                      @click=${this._toggleHidden}>
                    ${this._i18n[this.topic.hidden ? "show" : "hide"]}
                  </button>
                </li>
                <li>
                  <button type="button"
                      class="dropdown-item"
                      href="javascript:;"
                      aria-label="${this._i18n[this.topic.locked ? "unlock_topic_tooltip" : "lock_topic_tooltip"]}"
                      title="${this._i18n[this.topic.locked ? "unlock_topic_tooltip" : "lock_topic_tooltip"]}"
                      @click=${this._toggleLocked}>
                    ${this._i18n[this.topic.locked ? "unlock" : "lock"]}
                  </button>
                </li>
                ` : nothing }
                ${this.topic.canViewStatistics ? html`
                <li>
                  <button type="button"
                      class="dropdown-item"
                      href="javascript:;"
                      @click=${this.showStatistics}>
                    ${this._i18n.view_statistics}
                  </button>
                </li>
                ` : nothing }
              </ul>
            </div>
            ` : nothing }
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
                    title="${this._i18n.answered_tooltip}">
                </sakai-icon>
              ` : html`
                <sakai-icon type="questioncircle"
                    class="unanswered-icon"
                    title="${this._i18n.unanswered_tooltip}">
                </sakai-icon>
              `}
            </div>
            <div class="topic-status-text">${this._i18n[this.topic.resolved ? "answered" : "unanswered"]}</div>
          </div>
          ` : nothing }
        </div>
        ${this.topic.formattedDueDate ? html`
        <div id="topic-duedate-block"><span>${this._i18n.duedate_label}</span><span>${this.topic.formattedDueDate}</span></div>
        ` : nothing }
        <div class="topic-message">${unsafeHTML(this.topic.message)}</div>
        ${this.topic.draft ? "" : html`
        <div class="topic-message-bottom-bar">
          ${this.topic.canBookmark ? html`
          <div>
            <a href="javascript:;"
                @click=${this._toggleBookmarked}
                aria-label="${this._i18n[this.topic.bookmarked ? "unbookmark_tooltip" : "bookmark_tooltip"]}"
                title="${this._i18n[this.topic.bookmarked ? "unbookmark_tooltip" : "bookmark_tooltip"]}"
            >
              <div class="topic-option">
                <div><sakai-icon type="favourite" size="small"></sakai-icon></div>
                <div>
                  ${this._i18n[this.topic.bookmarked ? "unbookmark" : "bookmark"]}
                </div>
              </div>
            </a>
          </div>
          ` : nothing }
          ${this.topic.canPin ? html`
          <div>
            <a href="javascript:;"
                @click=${this._togglePinned}
                aria-label="${this.topic.pinned ? this._i18n.unpin_tooltip : this._i18n.pin_tooltip}"
                title="${this.topic.pinned ? this._i18n.unpin_tooltip : this._i18n.pin_tooltip}">
              <div class="topic-option">
                <div><sakai-icon type="pin" size="small"></sakai-icon></div>
                <div>
                  ${this._i18n[this.topic.pinned ? "unpin" : "pin"]}
                </div>
              </div>
            </a>
          </div>
          ` : nothing }
          ${this.topic.type === QUESTION ? html`
          <div>
            <div class="topic-option
                ${this.topic.myReactions.GOOD_QUESTION ? "good-question-on" : ""}
                ${this.topic.isMine && this.topic.reactionTotals.GOOD_QUESTION > 0 ? "reaction-on" : ""}"
            >
              ${this.topic.isMine && this.topic.reactionTotals.GOOD_QUESTION > 0 ? html`
                <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
                <div>
                ${this._i18n.goodquestion} ${this.isInstructor && this.topic.reactionTotals.GOOD_QUESTION ? ` - ${this.topic.reactionTotals.GOOD_QUESTION}` : ""}
                </div>
              ` : nothing }
              ${!this.topic.isMine ? html`
              <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
              <div>
                <a href="javascript:;"
                    data-reaction="GOOD_QUESTION"
                    @click=${this.toggleReaction}
                    aria-label="${this.topic.myReactions.GOOD_QUESTION ? this._i18n.ungoodquestion_tooltip : this._i18n.goodquestion_tooltip}"
                    title="${this.topic.myReactions.GOOD_QUESTION ? this._i18n.ungoodquestion_tooltip : this._i18n.goodquestion_tooltip}">
                  ${this._i18n.goodquestion} ${this.isInstructor && this.topic.reactionTotals.GOOD_QUESTION ? ` - ${this.topic.reactionTotals.GOOD_QUESTION}` : ""}
                </a>
              </div>
              ` : nothing }
            </div>
          </div>
          ` : nothing }
          ${this.topic.canReact ? html`
          <div class="reactions-block">
            <div class="topic-option">
              <div class="dropdown">
                <button class="btn btn-transparent"
                    id="topic-reactions-id-${this.topic.id}"
                    type="button"
                    aria-expanded="false"
                    data-bs-toggle="dropdown">
                  <sakai-icon type="smile" size="small"></sakai-icon>
                </button>
                <ul class="dropdown-menu conv-dropdown-menu" aria-labelledby="topic-reactions-${this.topic.id}">
                  ${this.renderMyReactions(this.topic.myReactions)}
                </ul>
              </div>
            </div>
            ${this.renderReactionsBar(this.topic.reactionTotals)}
          </div>
          ` : nothing }
          <div>
            <div class="topic-option">
            </div>
          </div>
        </div>
        <hr>
        ${!this.topic.continued && !this.topic.locked ? html`
        <div class="topic-reply-block ${!this._replying ? "padded" : ""}">
            ${this.topic.pastDueDate ? html`
            <div class="sak-banner-warn">${this._i18n.duedate_passed_info}</div>
            ` : nothing }
            ${this.topic.mustPostBeforeViewing && !this.topic.canPost ? html`
            <div class="sak-banner-warn">${this._i18n.post_before_viewing_message}</div>
            ` : nothing }
            ${this._replying ? html`
              ${this._renderPostEditor()}
            ` : html`
            <a href="javascript:;" @click=${this._setReplying}>
              <div class="editor-placeholder">
                <div><sakai-user-photo user-id="${window.top.portal.user.id}"></sakai-user-photo></div>
                <div>${this.topic.type === QUESTION ? this._i18n.answer_this_question : this._i18n.reply_to}</div>
                <div>${this.topic.title}</div>
              </div>
            </a>
            `}
          </div>
        </div>
        ` : nothing }
        ${this.topic?.posts?.length ? html `
          <div class="topic-posts-block">
            ${!this.topic.continued ? html`
            <div class="topic-posts-header">
              <div>${this.topic.type === QUESTION ? this._i18n.answers : this._i18n.responses}</div>
              ${this.topic.type === DISCUSSION ? html`
              <div>
                <select @change=${this._postSortSelected}>
                  <option value="${SORT_OLDEST}">oldest</option>
                  <option value="${SORT_NEWEST}">most recent</option>
                  <option value="${SORT_ASC_CREATOR}">ascending author</option>
                  <option value="${SORT_DESC_CREATOR}">descending author</option>
                  <option value="${SORT_MOST_ACTIVE}">most active</option>
                  <option value="${SORT_LEAST_ACTIVE}">least active</option>
                </select>
              </div>
              ` : nothing }
            </div>
            ` : nothing }

            ${this.topic.continued ? html`
            <div id="conv-back-button-block">
              <a href="javascript:;" @click=${this._viewAllPosts}>
                <div><sakai-icon type="left-arrow"></sakai-icon></div>
                <div>${this._i18n.back_to_all}</div>
              </a>
            </div>
            ` : nothing }
            ${this.topic.posts.map(p => html`
              ${p.canView ? html`
              <sakai-post
                  post="${JSON.stringify(p)}"
                  postType="${this.topic.type}"
                  ?is-instructor="${this.isInstructor}"
                  ?can-view-anonymous="${this.canViewAnonymous}"
                  ?can-view-deleted="${this.canViewDeleted}"
                  site-id="${this.topic.siteId}"
                  @post-updated=${this._postUpdated}
                  @post-deleted=${this._postDeleted}
                  @continue-thread=${this._continueThread}
                  @comment-deleted=${this._commentDeleted}></sakai-post>
              ` : nothing }
            `)}
          ${this.topic.numberOfThreads > this.topic.posts.length && !this.topic.continued ? html`
          <div class="topic-more-replies-block">
            <a href="javascript:;" @click=${this._getMoreReplies}>${this.topic.type === QUESTION ? this._i18n.more_answers : this._i18n.more_replies}</a>
          </div>
          ` : nothing }
          </div>
        ` : html`
          ${this._postEditorDisplayed ? html`
          <div>
            ${this._renderPostEditor()}
          </div>
          ` : html`
          `}
        `}
      `}
      </div>
    `;
  }
}
