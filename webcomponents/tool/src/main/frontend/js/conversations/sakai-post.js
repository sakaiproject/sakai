import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-user-photo.js";
import { reactionsMixin } from "./reactions-mixin.js";
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
      isInstructor: { attribute: "is-instructor", type: Boolean },
      canViewAnonymous: { attribute: "can-view-anonymous", type: Boolean },
      siteId: { attribute: "site-id", type: String },
      showingComments: Boolean,
      editing: { type: Boolean },
    };
  }

  constructor() {

    super();

    this.showingComments = true;

    this.commentsBeingEdited = new Map();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set post(value) {

    this._post = value;
    this.myReactions = value.myReactions || {};
    this.requestUpdate();
  }

  get post() { return this._post; }

  shouldUpdate() {
    return this.i18n && this.post;
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
      } else {
        throw new Error("Network error while hiding/showing post");
      }
    })
    .catch(error => console.error(error));
  }

  replyToPost() {

    const reply = {
      message: document.getElementById(`reply-to-${this.post.id}-editor`).getContent(),
      parentPost: this.post.id,
    };

    fetch("/api/posts", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
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
      this.replyEditorDisplayed[this.post.id] = !this.replyEditorDisplayed[this.post.id];
      this.requestUpdate();
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
      headers: {
        "Content-Type": "application/json",
      },
    })
    .then(r => {

      if (!r.ok) {
        this.post.message = currentMessage;
        throw new Error("Network error while saving post");
      }

      this.editing = false;

      this.post.isInstructor = true;

      this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    })
    .catch(error => console.error(error));
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
    .catch (error => {
      console.error(error);
      //TODO: show error message to user here
    });
  }

  commentCreated(e) {

    const comment = e.detail.comment;

    this.post.comments = this.post.comments || [];
    this.post.comments.push(comment);
    this.post.numberOfComments += 1;
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

  hardDeletePost() {

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
        this.dispatchEvent(new CustomEvent("post-deleted", { detail: { post: this.post }, bubbles: true }));
        this.requestUpdate();
      }
    });
  }

  softDeletePost() {

    if (!this.post.canDelete) return;

    if (!confirm(this.i18n.confirm_post_delete)) {
      return;
    }

    const url = `/api/sites/${this.siteId}/topics/${this.post.topic}/posts/${this.post.id}`;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while deleting a post.");
      } else {
        this.post.softDeleted = true;
        this.dispatchEvent(new CustomEvent("post-deleted", { detail: { post: this.post }, bubbles: true }));
        this.requestUpdate();
      }
    });

    /*
    const url = `/api/posts/${this.post.id}/softdelete`;
    fetch(url, {
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while (soft) deleting a post.");
      } else {
        this.post.softDeleted = true;
        this.dispatchEvent(new CustomEvent("post-softdeleted", { detail: { post: this.post }, bubbles: true }));
        this.requestUpdate();
      }
    })
    .catch (error => console.error(error));
    */
  }

  restorePost() {

    if (!this.post.canDelete) return;

    const url = `/api/sites/${this.siteId}/topics/${this.post.topic}/posts/${this.post.id}/restore`;
    fetch(url, {
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while restoring a post.");
      } else {
        this.post.softDeleted = false;
        this.dispatchEvent(new CustomEvent("post-restored", { detail: { post: this.post }, bubbles: true }));
        this.requestUpdate();
      }
    })
    .catch (error => console.error(error));
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

  render(isReply = false) {

    return html`
      <div id="post-${this.post.id}"
          data-post-id="${this.post.id}"
          class="post ${isReply ? "reply" : ""}
          ${this.post.isInstructor ? "instructor" : ""}
          ${this.post.softDeleted ? "soft-deleted" : ""}
          ${(!this.post.comments || !this.post.comments.length) && !this.post.canComment ? "post-without-comment-block" : ""}">
        ${this.post.softDeleted ? html`
        <div class="post-soft-deleted">This post has been deleted. Click restore to restore it.</div>
        ` : ""}

        <div class="post-topbar">
          <div class="photo">
            <sakai-user-photo user-id="${this.post.anonymous && !this.canViewAnonymous ? "blank" : this.post.creator}" size-class="medium-thumbnail">
            </sakai-user-photo>
          </div>
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
            ${this.post.hidden ? html`
            <div class="topic-status"
                role="image"
                title="${this.i18n.post_hidden_tooltip}"
                aria-label="${this.i18n.post_hidden_tooltip}">
              <sakai-icon type="hidden" size="small"></sakai-icon></div>
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
          ${this.post.isInstructor ? html`
          <div class="conv-instructors-answer">${this.i18n.instructors_answer}</div>
          ` : ""}
        </div>

        <div class="post-main">
          <div>
          </div>
          ${this.editing ? "" : html`
          <div class="post-message-block">
            <div class="post-message">${unsafeHTML(this.post.message)}</div>
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
                  ${this.post.canDelete && !this.post.softDeleted ? html`
                  <div class="delete-post">
                    <a href="javascript:;"
                        @click=${this.hardDeletePost}
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
                  ` : ""}
                  ${this.post.canModerate ? html`
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
          `}
        </div>

        ${this.editing ? html`
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

            <div class="topic-option post-option
                  ${this.post.myReactions.GOOD_ANSWER ? "good-question-on" : ""}
                  ${this.post.isMine && this.post.reactionTotals.GOOD_ANSWER > 0 ? "reaction-on" : ""}"
            >
              ${this.post.isMine && this.post.reactionTotals.GOOD_ANSWER > 0 ? html`
              <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
              <div>
                ${this.i18n.goodanswer} ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : ""}
              </div>
              ` : ""}
              ${!this.post.isMine ? html`
              <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
              <div>
              ${!this.post.locked ? html`
              <a href="javascript:;"
                  data-reaction="GOOD_ANSWER"
                  @click=${this.toggleReaction}
                  aria-label="${this.i18n[this.post.myReactions.GOOD_ANSWER ? "ungoodanswer_tooltip" : "goodanswer_tooltip"]}"
                  title="${this.i18n[this.post.myReactions.GOOD_ANSWER ? "ungoodanswer_tooltip" : "goodanswer_tooltip"]}">
                ${this.i18n.goodanswer} ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : ""}
              </a>
              ` : html`
                ${this.i18n.goodanswer} ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : ""}
              `}
              </div>
              ` : ""}
            </div>

            ${this.post.canReact ? html`
            <div class="topic-option">
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
        <div class="post-bottom-bar">
          <div></div>
          <div>
          ${this.renderReactionsBar(this.post.reactionTotals)}
          </div>
          <div></div>
        </div>
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
          <div><sakai-user-photo user-id="${window.top.portal.user.id}" size-class="medium-thumbnail"></sakai-user-photo></div>
          <div><sakai-comment-editor post-id="${this.post.id}" site-id="${this.siteId}" topic-id="${this.post.topic}" @comment-created=${this.commentCreated}></sakai-comment-editor></div>
        </div>
        ` : ""}
      </div>
    `;
  }
}

const tagName = "sakai-post";
if (!customElements.get(tagName)) {
  customElements.define(tagName, SakaiPost);
}
