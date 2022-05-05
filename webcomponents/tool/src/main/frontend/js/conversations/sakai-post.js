import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { SakaiElement } from "../sakai-element.js";
import { reactionsMixin } from "./reactions-mixin.js";
import { DISCUSSION, QUESTION } from "./sakai-conversations-constants.js";
import "../sakai-user-photo.js";
import "../sakai-editor.js";
import "./sakai-comment.js";
import "./sakai-comment-editor.js";
import "../sakai-options-menu.js";
import "../sakai-icon.js";
import "./options-menu.js";

export class SakaiPost extends reactionsMixin(SakaiElement) {

  static get properties() {

    return {
      post: { type: Object },
      postType: { type: String },
      isInstructor: { attribute: "is-instructor", type: Boolean },
      canViewAnonymous: { attribute: "can-view-anonymous", type: Boolean },
      canViewDeleted: { attribute: "can-view-deleted", type: Boolean },
      siteId: { attribute: "site-id", type: String },
      showingComments: Boolean,
      expanded: Boolean,
      editing: { type: Boolean },
      replying: { type: Boolean },
    };
  }

  constructor() {

    super();

    this.showingComments = true;
    this.expanded = true;

    this.commentsBeingEdited = new Map();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set post(value) {

    this.myReactions = value.myReactions || {};

    if (value.isThread && !value.keepExpanded) {
      this._collapseIfAllViewed(value);
    }

    if (value.keepExpanded || value.continued) {
      this.expanded = true;
    }
    const old = this._post;
    this._post = value;

    this.requestUpdate("post", old);
  }

  get post() { return this._post; }

  shouldUpdate() {
    return this.post;
  }

  _collapseIfAllViewed(thread) {

    const transformAll = (posts = []) => {
      return posts.flatMap(r => [ r, ...transformAll(r.posts) ]);
    };

    const flattened = transformAll(thread.posts);

    this.expanded = flattened.filter(p => !p.viewed).length > 0;
  }

  postReactions() {

    const url = this.post.links.find(l => l.rel === "react").href;
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
      throw new Error("Network error while posting reactions");

    })
    .then(reactionTotals => {

      this.post.myReactions = this.myReactions;
      this.post.reactionTotals = reactionTotals;
      this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    })
    .catch(error => console.error(error));
  }

  toggleReplyToPost(e) {

    e.preventDefault();

    const postId = e.target.dataset.postId;
    this.replyEditorDisplayed[postId] = !this.replyEditorDisplayed[postId];
    this.requestUpdate();
  }

  toggleLocked() {

    const url = this.post.links.find(l => l.rel === "lock").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.post.locked),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Network error while locking/unlocking post");
    })
    .then(post => {
      this.dispatchEvent(new CustomEvent("post-updated", { detail: { post }, bubbles: true }));
    })
    .catch(error => console.error(error));
  }

  toggleHidden() {

    const url = this.post.links.find(l => l.rel === "hide").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(!this.post.hidden),
    })
    .then(r => {

      if (r.ok) {
        this.post.hidden = !this.post.hidden;
        this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
      }

      throw new Error("Network error while hiding/showing post");
    })
    .catch(error => console.error(error));
  }

  replyToPostAsDraft() {
    this.replyToPost(true);
  }

  replyToPostAsPublished() {
    this.replyToPost(false);
  }

  replyToPost(draft = false) {

    const reply = {
      message: document.getElementById(`post-${this.post.id}-reply-editor`).getContent(),
      parentPost: this.post.id,
      parentThread: this.post.parentThread || this.post.id,
      draft,
    };

    const url = this.post.links.find(l => l.rel === "reply").href;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(reply),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while replying to a post.");
      }
      return r.json();
    })
    .then(post => {

      if (!this.post.posts) this.post.posts = [];
      this.post.posts.push(post);
      if (this.post.isThread) this.post.numberOfThreadReplies += 1;
      this.replying = false;
      this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post, created: true }, bubbles: true }));
    })
    .catch (error => console.error(error));
  }

  savePostAsDraft() {
    this.savePost(true);
  }

  publishPost() {
    this.savePost(false);
  }

  savePost(draft) {

    const currentMessage = this.post.message;

    const editor = document.getElementById(`post-${this.post.id}-editor`);
    this.post.message = editor.getContent();
    this.post.draft = draft;

    const url = this.post.links.find(l => l.rel === "self").href;
    fetch(url, {
      credentials: "include",
      method: "PUT",
      body: JSON.stringify(this.post),
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json()
      }

      throw new Error(`Network error while saving post: ${r.status}`);
    })
    .then(post => {

      this.post = post;
      this.editing = false;
      this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    })
    .catch (error => {

      console.error(error);
      this.post.message = currentMessage;
    });
  }

  toggleUpvotePost() {

    if (!this.post.canUpvote) return;

    const url = `/api/sites/${this.siteId}/topics/${this.post.topic}/posts/${this.post.id}/${this.post.upvoted ? "unupvote" : "upvote"}`;
    fetch(url, {
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while upvoting a post.");
      } else {
        if (this.post.upvoted) {
          this.post.upvotes -= 1;
          this.post.upvoted = false;
        } else {
          this.post.upvotes += 1;
          this.post.upvoted = true;
        }
        this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
        this.requestUpdate();
      }
    })
    .catch (error => console.error(error));
  }

  commentCreated(e) {

    const comment = e.detail.comment;

    this.post.comments = this.post.comments || [];
    this.post.comments.unshift(comment);
    this.post.numberOfComments = this.post.comments.length;
    this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    this.showingComments = true;
  }

  commentUpdated(e) {

    const comment = e.detail.comment;

    this.post.comments.splice(this.post.comments.findIndex(c => c.id === comment.id), 1, comment);
    this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    this.commentsBeingEdited.set(comment.id, false);
    this.requestUpdate();
  }

  deletePost() {

    if (!this.post.canDelete) return;

    if (!confirm(this.i18n.confirm_post_delete)) {
      return;
    }

    const url = this.post.links.find(l => l.rel === "delete").href;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while deleting a post.");
      } else {
        // The post has been hard deleted
        if (this.post.depth > 5) {
          // TODO: his is a hack and a lit-element anti-pattern. lit is somehow losing track of post
          // elements that have been added to continued threads. Lit should be removing this in the
          // render.
          document.getElementById(`discussion-post-block-${this.post.id}`).remove();
        }
        this.dispatchEvent(new CustomEvent("post-deleted", { detail: { post: this.post }, bubbles: true }));
      }
    })
    .catch(error => console.error(error));
  }

  updated() {

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    }
  }

  editComment(e) {

    const commentId = e.target.dataset.commentId;
    this.commentsBeingEdited.set(commentId, true);
    this.requestUpdate();
  }

  commentDeleted(e) {

    const commentId = e.detail.comment.id;
    const index = this.post.comments.findIndex(c => c.id === commentId);
    this.post.comments.splice(index, 1);
    this.post.numberOfComments -= 1;
    this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    this.requestUpdate();
  }

  _renderAuthorDetails() {

    return html`

      <div class="author-details">
        <div class="post-creator-name">${this.post.creatorDisplayName}</div>
        ${this.post.isInstructor ? html`
        <div class="post-creator-instructor">${this.i18n.instructor}</div>
        ` : ""}
        <div class="post-created-date">${this.post.formattedCreatedDate}</div>
        ${this.post.draft ? html`
        <div class="draft">${this.i18n.draft}</div>
        ` : ""}
        ${this.post.locked ? html`
        <div class="topic-status"
            role="image"
            title="${this.i18n.post_locked_tooltip}"
            aria-label="${this.i18n.post_locked_tooltip}">
          <sakai-icon type="lock" size="small"></sakai-icon></div>
        </div>
        ` : ""}
        ${this.post.privatePost ? html`
        <div class="topic-status"
            role="image"
            title="${this.i18n.post_private_tooltip}"
            aria-label="${this.i18n.post_private_tooltip}">
          <sakai-icon type="secret" size="small"></sakai-icon></div>
        </div>
        ` : ""}
      </div>
    `;
  }

  _continueThreadFromHere() {
    this.dispatchEvent(new CustomEvent("continue-thread", { detail: { post: this.post }, bubbles: true }));
  }

  getGoodAnswerTooltip() {

    switch (this.post.type) {
      case QUESTION:
        return this.post.myReactions.GOOD_ANSWER ? "ungoodanswer_tooltip" : "goodanswer_tooltip";
      case DISCUSSION:
        return this.post.myReactions.GOOD_ANSWER ? "ungoodpost_tooltip" : "goodpost_tooltip";
      default:
        return "";
    }
  }

  _renderMessageRow() {

    return html`
      <div class="post-message-block">

        ${this.post.hidden ? html`
          <div class="sak-banner-info">${this.post.canModerate ? this.i18n.moderator_hidden_message : this.i18n.hidden_message}</div>
          ${this.post.canModerate ? html`
            <div class="post-message">${unsafeHTML(this.post.message)}</div>
          ` : ""}
        ` : html`
          <div class="post-message">${unsafeHTML(this.post.message)}</div>
        `}
      </div>

      <div>
        ${this.post.canEdit || this.post.canDelete || this.post.canModerate ? html`

          <options-menu placement="bottom-left">
            <a slot="trigger"
                id="options-menu-link-${this.post.id}"
                aria-label="${this.i18n.post_options_menu_tooltip}"
                title="${this.i18n.post_options_menu_tooltip}"
                aria-haspopup="true"
                href="javascript:;">
              <div><sakai-icon type="menu" size="small"></sakai-icon></div>
            </a>
            <div slot="content" class="options-menu" role="dialog">
              ${this.post.canEdit ? html`
              <div class="edit-post">
                <a href="javascript:;"
                    @click=${() => this.editing = true}
                    title="${this.i18n.edit_this_post}"
                    arial-label="${this.i18n.edit_this_post}">
                  ${this.i18n.edit}
                </a>
              </div>
              ` : ""}
              ${this.post.canDelete ? html`
              <div class="delete-post">
                <a href="javascript:;"
                    @click=${this.deletePost}
                    title="${this.i18n.delete_this_post}"
                    arial-label="${this.i18n.delete_this_post}">
                  ${this.i18n.delete}
                </a>
              </div>
              ` : ""}
              ${this.post.canModerate ? html`
              <div>
                <a href="javascript:;"
                    @click=${this.toggleHidden}>
                  ${this.i18n[this.post.hidden ? "show" : "hide"]}
                </a>
              </div>
              <div>
                <a href="javascript:;"
                    @click=${this.toggleLocked}>
                  ${this.i18n[this.post.locked ? "unlock" : "lock"]}
                </a>
              </div>
              ` : ""}

            </div>
          </options-menu>
        ` : ""}
      </div>
    `;
  }

  _renderEditor() {

    return html`
      <div class="post-editor-block">
        <sakai-editor id="post-${this.post.id}-editor" content="${this.post.message}" set-focus></sakai-editor>
        <div class="conv-private-checkbox-block">
          <label>
            <input type="checkbox" .checked=${this.post.privatePost} @click=${e => this.post.privatePost = e.target.checked}>
            <span>${this.i18n.private_reply}</span>
          </label>
        </div>
        <div class="act">
          <input type="button" class="active" @click=${this.publishPost} value="${this.i18n.publish}">
          <input type="button" @click=${this.savePostAsDraft} value="${this.i18n.save_as_draft}">
          <input type="button" @click=${() => this.editing = false} value="${this.i18n.cancel}">
        </div>
      </div>
    `;
  }

  _renderReplyEditor() {

    return html`
      <div class="post-reply-editor-block">
        <sakai-editor id="post-${this.post.id}-reply-editor" set-focus></sakai-editor>
        <div class="conv-private-checkbox-block">
          <label>
            <input type="checkbox" @click=${e => this.post.privatePost = e.target.checked}>
            <span>${this.i18n.private_reply}</span>
          </label>
        </div>
        <div class="act">
          <input type="button" class="active" @click=${this.replyToPostAsPublished} value="${this.i18n.publish}">
          <input type="button" @click=${this.replyToPostAsDraft} value="${this.i18n.save_as_draft}">
          <input type="button" @click=${() => this.replying = false} value="${this.i18n.cancel}">
        </div>
      </div>
    `;
  }

  _renderDiscussionPost() {

    return html`

      <div id="discussion-post-block-${this.post.id}" class="discussion-post-block ${this.post.hidden ? "soft-deleted" : ""}">

        <div class="discussion-post-left-column">
          <div class="photo">
            <sakai-user-photo
                user-id="${this.post.anonymous && !this.canViewAnonymous ? "blank" : this.post.creator}"
                size-class="medium-thumbnail"
            >
            </sakai-user-photo>
          </div>
          ${this.post?.posts?.length ? html`
          <div class="discussion-post-vbar">
          </div>
          ` : ""}
        </div>

        <div class="discussion-post-right-column">

          <div class="discussion-post-content-wrapper ${!this.post.viewed ? "new" : ""}">
            <div id="post-${this.post.id}" class="discussion-post-content" data-post-id="${this.post.id}">
              ${this._renderAuthorDetails()}
              <div>
              ${this.post.late ? html`
              <div class="discussion-post-late">late</div>
              ` : ""}
              ${!this.post.viewed ? html`
              <div class="discussion-post-new">${this.i18n.new}</div>
              ` : ""}
              </div>
              ${this.editing ? html`
                ${this._renderEditor()}
              ` : html`
              ${this._renderMessageRow()}
              `}
            </div>
            ${this.editing ? "" : html`
            <div class="discussion-post-bottom-bar">
              <div class="post-actions-block">
              ${!this.post.hidden || this.post.canModerate ? html`
                ${this._renderReactionsBlock()}
                ${this.renderReactionsBar(this.post.reactionTotals)}
              ` : ""}
              </div>
              <div class="discussion-post-reply-options">
              ${this.post.canReply ? html`
                <div>
                  <a href="javascript:;"
                      @click=${() => this.replying = !this.replying}
                      aria-label="${this.i18n.reply_tooltip}"
                      title="${this.i18n.reply_tooltip}">
                    ${this.i18n.reply}
                  </a>
                </div>
                ` : ""}
                ${this.post.isThread && this.post.numberOfThreadReplies ? html`
                <div class="discussion-post-toggle-replies">
                  <a href="javascript:;"
                      @click=${() => this.expanded = !this.expanded}
                      aria-label="${this.expanded ? this.i18n.hide_replies_tooltip : this.i18n.show_replies_tooltip}"
                      title="${this.expanded ? this.i18n.hide_replies_tooltip : this.i18n.show_replies_tooltip}">
                    <div class="post-replies-toggle-block">
                      <div class="post-replies-toggle-icon">
                        <sakai-icon
                            type="${this.expanded ? "chevron-down" : "chevron-up"}"
                            size="small">
                        </sakai-icon>
                      </div>
                      <div>${this.post.numberOfThreadReplies} ${this.post.numberOfThreadReplies == 1 ? this.i18n.reply : this.i18n.replies}</div>
                    </div>
                  </a>
                </div>
                ` : ""}
              </div>
            </div>
            `}
          </div>

          ${this.replying ? html`
          <div class="discussion-post-reply-block">
            <div class="photo">
              <sakai-user-photo
                  user-id="${window.top.portal.user.id}"
                  classes="medium-thumbnail"
              >
              </sakai-user-photo>
            </div>
            ${this._renderReplyEditor()}
          </div>
          ` : ""}
          ${this.post.depth % 5 === 0 && !this.post.continued ? html`
            <a href="javascript:;"
                class="discussion-post-continue-block"
                @click=${this._continueThreadFromHere}>
              <div>Continue this thread</div>
              <div><sakai-icon type="right-arrow" size="small"></sakai-icon></div>
            </a>
          ` : html`

            ${this.post.posts && this.expanded ? html`
              ${this.post.posts.map(p => html`
                <sakai-post
                    post="${JSON.stringify(p)}"
                    postType="${this.postType}"
                    ?is-instructor="${this.isInstructor}"
                    ?can-view-anonymous="${this.canViewAnonymous}"
                    ?can-view-deleted="${this.canViewDeleted}"
                    site-id="${this.siteId}"
                    @comment-deleted=${this.commentDeleted}>
                </sakai-post>
              `)}
            ` : ""}
          `}
        </div>

      </div>
    `;
  }

  _renderReactionsBlock() {

    return html`
        <div class="post-option
            ${this.post.myReactions.GOOD_ANSWER ? "reaction-on" : ""}
            ${this.post.isMine && this.post.reactionTotals.GOOD_ANSWER > 0 ? "reaction-on" : ""}"
        >
          ${this.post.isMine && this.post.reactionTotals.GOOD_ANSWER > 0 ? html`
          <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
          <div>
            ${this.post.type == QUESTION ? this.i18n.goodanswer : this.i18n.goodpost}
            ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : ""}
          </div>
          ` : ""}
          ${!this.post.isMine && this.post.canReact ? html`
          <div>
          ${!this.post.locked && this.post.canReact ? html`
            <a href="javascript:;"
                class="post-option"
                data-reaction="GOOD_ANSWER"
                @click=${this.toggleReaction}
                aria-label="${this.getGoodAnswerTooltip()}"
                title="${this.getGoodAnswerTooltip()}">
              <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
              <div>
                ${this.post.type == QUESTION ? this.i18n.goodanswer : this.i18n.goodpost}
                ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : ""}
              </div>
            </a>
          ` : html`
            ${this.post.type == QUESTION ? this.i18n.goodanswer : this.i18n.goodpost}
            ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : ""}
          `}
          </div>
          ` : ""}
        </div>

        ${this.post.canReact ? html`
        <div class="post-option single">
          <options-menu placement="bottom">
            <div slot="trigger" class="reactions-link">
              <a href="javascript:;"
                  @click=${() => this.showingMyReactions = !this.showingMyReactions}
                  aria-label="${this.i18n.reactions_tooltip}"
                  title="${this.i18n.reactions_tooltip}">
                <sakai-icon type="smile" size="small"></sakai-icon>
              </a>
            </div>
            <div slot="content" class="topic-reactions-popup" role="dialog">
              ${this.renderMyReactions(this.post.myReactions)}
            </div>
          </options-menu>
        </div>
        ` : ""}
    `;
  }

  _renderQAPost() {

    return html`
      <div id="post-${this.post.id}"
          data-post-id="${this.post.id}"
          class="post ${this.post.isInstructor ? "instructor" : ""}
          ${this.post.hidden ? "soft-deleted" : ""}
          ${(!this.post.comments || !this.post.comments.length) && !this.post.canComment ? "post-without-comment-block" : ""}">

        <div class="post-topbar">
          <div class="photo">
            <sakai-user-photo
                user-id="${this.post.anonymous && !this.canViewAnonymous ? "blank" : this.post.creator}"
                classes="medium-thumbnail">
            </sakai-user-photo>
          </div>
          ${this._renderAuthorDetails()}
          ${this.post.late ? html`
          <div class="discussion-post-late">late</div>
          ` : ""}
          ${this.post.isInstructor ? html`
          <div class="conv-instructors-answer">${this.i18n.instructors_answer}</div>
          ` : ""}
        </div>

        <div class="post-main">
          <div>
          </div>
          ${this._renderMessageRow()}
        </div>

        ${this.editing ? html`
          ${this._renderEditor()}
        ` : html`
        <div class="post-reactions-comment-toggle-block">
          <div class="post-upvote-block">
          ${this.post.canUpvote ? html`
            <a href="javascript:;"
                @click=${this.toggleUpvotePost}
                aria-label="${this.i18n.upvote_tooltip}"
                title="${this.i18n.upvote_tooltip}">
              <div class="post-upvote-container ${!this.post.upvotes ? "no-votes" : ""}">
                <div>
                  <sakai-icon type="up" size="smallest"></sakai-icon>
                </div>
                <div>${this.post.upvotes || 0}</div>
              </div>
            </a>
          ` : ""}
          </div>
          <div class="post-actions-block">
            ${this._renderReactionsBlock()}
            ${this.renderReactionsBar(this.post.reactionTotals)}
          </div>
          <div>
          ${this.post.numberOfComments > 0 ? html`
          <a href="javascript:;"
              aria-label="${this.showingComments ? this.i18n.hide_comments_tooltip : this.i18n.show_comments_tooltip}"
              title="${this.showingComments ? this.i18n.hide_comments_tooltip : this.i18n.show_comments_tooltip}"
              @click=${() => this.showingComments = !this.showingComments}>
            <div class="post-comment-toggle-block">
              <div class="post-comment-toggle-icon">
                <sakai-icon
                    type="${this.showingComments ? "chevron-down" : "chevron-up"}"
                    size="small">
                </sakai-icon>
              </div>
              <div>${this.post.numberOfComments} ${this.post.numberOfComments == 1 ? this.i18n.comment : this.i18n.comments}</div>
            </div>
          </a>
          ` : ""}
          </div>
        </div>
        `}
      </div>

      <div class="post-comments-block">
        ${this.showingComments && this.post.comments && this.post.numberOfComments > 0 ? html`
        <div class="post-comments">
        ${this.post.comments.map(c => html`
          <sakai-comment
              comment="${JSON.stringify(c)}"
              topic-id="${this.post.topic}"
              site-id="${this.siteId}"
              @comment-updated=${this.commentUpdated}
              @comment-deleted=${this.commentDeleted}>
          </sakai-comment>
        `)}
        </div>
        ` : ""}
        ${this.post.canComment ? html`
        <div class="post-add-comment-block">
          <div>
            <sakai-user-photo user-id="${window.top.portal.user.id}" classes="medium-thumbnail">
            </sakai-user-photo>
          </div>
          <div><sakai-comment-editor post-id="${this.post.id}" site-id="${this.siteId}" topic-id="${this.post.topic}" @comment-created=${this.commentCreated}></sakai-comment-editor></div>
        </div>
        ` : ""}
      </div>
    `;
  }

  render() {
    return this.postType === QUESTION ? this._renderQAPost() : this._renderDiscussionPost();
  }
}

const tagName = "sakai-post";
!customElements.get(tagName) && customElements.define(tagName, SakaiPost);
