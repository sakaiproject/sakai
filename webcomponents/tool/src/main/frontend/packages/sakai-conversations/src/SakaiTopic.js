import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-user-photo";
import { findPost, markThreadViewed } from "./utils.js";
import { ReactionsMixin } from "./ReactionsMixin.js";
import { TopicMenuMixin } from "./TopicMenuMixin.js";
import "@sakai-ui/sakai-editor/sakai-editor.js";
import "../sakai-post.js";
import { GROUP, INSTRUCTORS, QUESTION, SORT_OLDEST, SORT_NEWEST, SORT_ASC_CREATOR, SORT_DESC_CREATOR, SORT_MOST_ACTIVE, SORT_LEAST_ACTIVE } from "./sakai-conversations-constants.js";
import "@sakai-ui/sakai-icon";

export class SakaiTopic extends TopicMenuMixin(ReactionsMixin(SakaiElement)) {

  static properties = {

    topic: { type: Object },
    postId: { attribute: "post-id", type: String },
    isInstructor: { attribute: "is-instructor", type: Boolean },
    canViewAnonymous: { attribute: "can-view-anonymous", type: Boolean },
    canViewDeleted: { attribute: "can-view-deleted", type: Boolean },
    reactionsAllowed: { attribute: "reactions-allowed", type: Boolean },

    _postEditorDisplayed: { state: true },
    _replying: { state: true },
  };

  constructor() {

    super();

    // Used by the intersection observer to track which posts we've already observed
    this._observedPosts = new Set();

    const options = {
      root: null,
      rootMargin: "0px",
      threshold: 1.0,
    };

    this.observer = new IntersectionObserver(entries => {

      const postIds = entries
        .filter(entry => entry.isIntersecting)
        .map(entry => entry.target.dataset.postId)
        .filter(postId => !this._observedPosts.has(postId)); // Only process posts we haven't marked yet

      postIds && this._markPostsViewed(postIds);
    }, options);

    this.loadTranslations("conversations");
  }

  set topic(value) {

    const oldValue = this._topic;

    this._topic = value;

    this._postEditorDisplayed = false;

    this.myReactions = value.myReactions || {};

    const update = () => {

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
      this._getPosts(this.topic, 0, null, this.postId).then(posts => {

        this.topic.posts = posts;

        // We've clicked on a topic and it has no posts. Ergo, it has been "viewed".
        if (!this.topic?.posts?.length) this.topic.viewed = true;

        update();
        this.dispatchEvent(new CustomEvent("topic-updated", { detail: { topic: this.topic, dontUpdateCurrent: true }, bubbles: true }));
      });
    } else {
      update();
    }
  }

  get topic() { return this._topic; }

  _markPostsViewed(postIds) {

    // Filter out postIds that have already been marked as viewed
    const newPostIds = postIds.filter(id => !this._observedPosts.has(id));

    // If there are no new posts to mark as viewed, return early
    if (newPostIds.length === 0) {
      return Promise.resolve();
    }

    // Add these posts to our tracked set before making the request
    newPostIds.forEach(id => this._observedPosts.add(id));

    const url = this.topic.links.find(l => l.rel === "markpostsviewed").href;
    return fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newPostIds),
    })
    .then(r => {

      if (r.ok) {
        // Posts marked. Now unobserve them. We don't want to keep triggering this fetch
        newPostIds.forEach(postId => {

          const post = findPost(this.topic, { postId });
          if (post) {
            post.viewed = true;
            post.keepExpanded = true;
          }

          // Only try to unobserve if we have entries
          if (this.observer?.unobserve) {
            const element = document.querySelector(`#post-${postId}`);
            element && this.observer.unobserve(element);
          }
        });

        this.dispatchEvent(new CustomEvent("posts-viewed", { detail: { postIds: newPostIds, topicId: this.topic.id } }));
        this.requestUpdate();
      } else {
        throw new Error(`Network error while marking posts as viewed at url ${url}`);
      }
    })
    .catch (error => {
      console.error(error);
      throw error;
    });
  }

  _savePostAsDraft() { this._postToTopic(true); }

  _publishPost() { this._postToTopic(false); }

  _postToTopic(draft) {

    const postData = {
      message: this.querySelector(".topic-reply-block sakai-editor").getContent(),
      topic: this.topic.id,
      siteId: this.topic.siteId,
      privatePost: this.querySelector("#conv-post-editor-private-checkbox").checked,
      anonymous: this.topic.allowAnonymousPosts && this.querySelector("#conv-post-editor-anonymous-checkbox").checked,
      draft,
      replyable: true,
    };

    const url = this.topic.links.find(l => l.rel === "posts").href;
    fetch(url, {
      method: "POST",
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

  _commentDeleted(e) {

    e.stopPropagation();

    // Recast this event with the topic id added. The data is managed by sakai-conversations.js,
    // and it needs the topic id to lookup the post with this comment.
    this.dispatchEvent(new CustomEvent("comment-deleted",
      { detail: { topicId: this.topic.id, ...e.detail }, bubbles: true }));
  }

  _toggleBookmarked() {

    const url = this.topic.links.find(l => l.rel === "bookmark").href;
    fetch(url, {
      method: "POST",
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

  _togglePinned() {

    const url = this.topic.links.find(l => l.rel === "pin").href;
    fetch(url, {
      method: "POST",
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

    posts?.forEach(p => {

      if (!p.viewed && !this._observedPosts.has(p.id)) {
        const postElement = this.querySelector(`#post-${p.id}`);
        postElement && this.observer.observe(postElement);
      }

      // Child posts? Recurse.
      p.posts && this._registerPosts(p.posts);
    });
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

  _getPosts(topic, page = 0, sort, postId) {

    const baseUrl = topic.links.find(l => l.rel === "posts").href;
    const url = `${baseUrl}?page=${page}${sort ? `&sort=${sort}` : ""}${postId ? `&postId=${postId}` : ""}`;

    return fetch(url)
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while retrieving  posts from ${url}`);
    })
    .catch(error => console.error(error));
  }

  _unsetReplying() { this._replying = false; }

  _setReplying() { this._replying = true; }

  _toggleShowingMyReactions() { this.showingMyReactions = !this.showingMyReactions; }

  _renderPostEditor() {

    return html`
      <div>
        <div class="conv-post-editor-header">
          <span>${this.topic.type === QUESTION ? this._i18n.answer_this_question : this._i18n.reply_to}</span>
          <span>${this.topic.title}</span>
        </div>
        <sakai-editor set-focus></sakai-editor>
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
          ${this.topic.graded && !this.topic.gradingItemId ? html`
          <div class="sak-banner-error conv-graded-no-grading-item-id-warning">
            ${this._i18n.graded_no_item_id}
          </div>
          ` : nothing}
        ` : nothing}
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
          ${this.topic.tags?.map(tag => tag ? html`
            <div class="tag">${tag.label}</div>
          ` : nothing)}
        </div>
        <div class="author-and-tools">
          <div class="author-block">
            <div>
              <sakai-user-photo
                  class="largest-thumbnail"
                  user-id="${this.topic.anonymous && !this.canViewAnonymous ? "blank" : this.topic.creator}"
                  profile-popup="${this.topic.anonymous && !this.canViewAnonymous ? "off" : "on"}">
              </sakai-user-photo>
            </div>
            <div>
              <div class="d-flex align-items-center flex-wrap">
                <div class="conversations-topic__creator-name fs-5 fw-bold text-nowrap">${this.topic.anonymous && !this.canViewAnonymous ? this._i18n.anonymous : this.topic.creatorDisplayName}</div>
                <div class="topic-question-asked">${this.topic.type === QUESTION ? this._i18n.asked : this._i18n.posted}</div>
                <div class="conversations-topic__created-date ms-1 small text-nowrap">${this.topic.formattedCreatedDate}</div>
              </div>
            </div>
          </div>
          ${this._renderMenu()}
        </div>
        <div class="topic-title-and-status">
          <div class="conversations-topic__title fs-1 me-1 fw-light">${this.topic.title}</div>
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
        ${this.topic.gradingItemId ? html`
          <div>${this.tr("graded", [ this.topic.gradingPoints ])}</div>
        ` : nothing}
        ${this.topic.formattedDueDate ? html`
        <div id="topic-duedate-block"><span>${this._i18n.duedate_label}</span><span>${this.topic.formattedDueDate}</span></div>
        ` : nothing }
        <div class="topic-message fs-5">${unsafeHTML(this.topic.message)}</div>
        ${this.topic.draft ? nothing : html`
        <div class="topic-message-bottom-bar mb-1">
          ${this.topic.canBookmark ? html`
          <div>
            <a href="javascript:;"
                @click=${this._toggleBookmarked}
                aria-label="${this._i18n[this.topic.bookmarked ? "unbookmark_tooltip" : "bookmark_tooltip"]}"
                title="${this._i18n[this.topic.bookmarked ? "unbookmark_tooltip" : "bookmark_tooltip"]}"
            >
              <div class="topic-option">
                <div><i class="si si-bookmark${this.topic.bookmarked ? "-fill" : ""}"></i></div>
                <div>${this.topic.bookmarked ? this._i18n.unbookmark : this._i18n.bookmark}</div>
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
                <div><i class="si si-pin${this.topic.pinned ? "-fill" : ""}"></i></div>
                <div>${this.topic.pinned ? this._i18n.unpin : this._i18n.pin}</div>
              </div>
            </a>
          </div>
          ` : nothing }
        </div>
        ${this.reactionsAllowed ? html`
          ${this.renderReactionsBar(this.topic)}
        ` : nothing}
        <div class="conversations-actions-block d-flex mb-1">
          ${this._renderReactionsBlock(this.topic)}
          ${this._renderUpvoteBlock(this.topic)}
        </div>
        <div>
          <div class="topic-option">
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
          <div class="topic-posts-block ${this.topic.type === QUESTION ? "ms-4" : ""}">
            ${!this.topic.continued ? html`
            <div class="topic-posts-header">
              <div>${this.topic.type === QUESTION ? this._i18n.answers : this._i18n.responses}</div>
              <div>
                <select @change=${this._postSortSelected} aria-label="${this._i18n.sort_by_label}">
                  <option value="${SORT_NEWEST}">${this._i18n.most_recent}</option>
                  <option value="${SORT_OLDEST}">${this._i18n.oldest}</option>
                  <option value="${SORT_ASC_CREATOR}">${this._i18n.ascending_by_author}</option>
                  <option value="${SORT_DESC_CREATOR}">${this._i18n.descending_by_author}</option>
                  <option value="${SORT_MOST_ACTIVE}">${this._i18n.most_active}</option>
                  <option value="${SORT_LEAST_ACTIVE}">${this._i18n.least_active}</option>
                </select>
              </div>
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
                  .post=${p}
                  post-type="${this.topic.type}"
                  ?is-instructor=${this.isInstructor}
                  ?can-view-anonymous=${this.canViewAnonymous}
                  ?can-view-deleted=${this.canViewDeleted}
                  ?reactions-allowed=${this.reactionsAllowed}
                  grading-item-id=${ifDefined(this.topic.gradingItemId)}
                  max-grade-points=${ifDefined(this.topic.gradingPoints)}
                  site-id="${this.topic.siteId}"
                  topic-reference="${this.topic.reference}"
                  @post-updated=${this._postUpdated}
                  @post-deleted=${this._postDeleted}
                  @continue-thread=${this._continueThread}
                  @comment-deleted=${this._commentDeleted}>
              </sakai-post>
              ${this.topic.type === QUESTION ? html`<hr class="border-bottom border-3">` : nothing}
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
