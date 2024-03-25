import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { reactionsMixin } from "./reactions-mixin.js";
import { DISCUSSION, QUESTION } from "./sakai-conversations-constants.js";
import "@sakai-ui/sakai-user-photo";
import "@sakai-ui/sakai-editor";
import "../sakai-comment.js";
import "../sakai-comment-editor.js";
import "@sakai-ui/sakai-icon";

export class SakaiPost extends reactionsMixin(SakaiElement) {

  static properties = {

    post: { type: Object },
    postType: { type: String },
    isInstructor: { attribute: "is-instructor", type: Boolean },
    canViewAnonymous: { attribute: "can-view-anonymous", type: Boolean },
    canViewDeleted: { attribute: "can-view-deleted", type: Boolean },
    siteId: { attribute: "site-id", type: String },

    _showingComments: { state: true },
    _expanded: { state: true },
    _editing: { state: true },
    _replying: { state: true },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this._showingComments = true;
    this._expanded = true;

    this.commentsBeingEdited = new Map();

    this.loadTranslations("conversations").then(r => this._i18n = r);
  }

  set post(value) {

    this.myReactions = value.myReactions || {};

    if (value.isThread && !value.keepExpanded) {
      this._collapseIfAllViewed(value);
    }

    if (value.keepExpanded || value.continued) {
      this._expanded = true;
    }
    const old = this._post;
    this._post = value;

    this.requestUpdate("post", old);
  }

  get post() { return this._post; }

  _collapseIfAllViewed(thread) {

    const transformAll = (posts = []) => {
      return posts.flatMap(r => [ r, ...transformAll(r.posts) ]);
    };

    const flattened = transformAll(thread.posts);

    this._expanded = flattened.filter(p => !p.viewed).length > 0;
  }

  _postReactions() {

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

  _toggleLocked() {

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

  _toggleHidden() {

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
        throw new Error(`Network error while hiding/showing post at ${url}: ${r.status}`);
      }
    })
    .catch(error => console.error(error));
  }

  _replyToPostAsDraft() { this._replyToPost(true); }

  _replyToPostAsPublished() { this._replyToPost(false); }

  _replyToPost(draft = false) {

    const reply = {
      message: document.getElementById(`post-${this.post.id}-reply-editor`).getContent(),
      parentPost: this.post.id,
      // If the post being replied to is private, the reply must be private.
      privatePost: this._privateReply || this.post.privatePost,
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
      this._replying = false;
      this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post, created: true }, bubbles: true }));
    })
    .catch (error => console.error(error));
  }

  _savePostAsDraft() { this._savePost(true); }

  _publishPost() { this._savePost(false); }

  _savePost(draft) {

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
        return r.json();
      }

      throw new Error(`Network error while saving post: ${r.status}`);
    })
    .then(post => {

      this.post = post;
      this._editing = false;
      this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    })
    .catch (error => {

      console.error(error);
      this.post.message = currentMessage;
    });
  }

  _toggleUpvotePost() {

    if (!this.post.canUpvote) return;

    const url = `/api/sites/${this.siteId}/topics/${this.post.topic}/posts/${this.post.id}/${this.post.upvoted ? "unupvote" : "upvote"}`;
    fetch(url, {
      credentials: "include",
    })
    .then(r => {

      if (r.ok) {
        if (this.post.upvoted) {
          this.post.upvotes -= 1;
          this.post.upvoted = false;
        } else {
          this.post.upvotes += 1;
          this.post.upvoted = true;
        }
        this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
        this.requestUpdate();
      } else {
        throw new Error("Network error while upvoting a post.");
      }
    })
    .catch (error => console.error(error));
  }

  _commentCreated(e) {

    const comment = e.detail.comment;

    this.post.comments = this.post.comments || [];
    this.post.comments.unshift(comment);
    this.post.numberOfComments = this.post.comments.length;
    this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    this._showingComments = true;
  }

  _commentUpdated(e) {

    const comment = e.detail.comment;

    this.post.comments.splice(this.post.comments.findIndex(c => c.id === comment.id), 1, comment);
    this.dispatchEvent(new CustomEvent("post-updated", { detail: { post: this.post }, bubbles: true }));
    this.commentsBeingEdited.set(comment.id, false);
    this.requestUpdate();
  }

  _deletePost() {

    if (!this.post.canDelete) return;

    if (!confirm(this._i18n.confirm_post_delete)) {
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

  _commentDeleted(e) {

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
        <div class="post-creator-instructor">${this._i18n.instructor}</div>
        ` : nothing }
        <div class="post-created-date">${this.post.formattedCreatedDate}</div>
        ${this.post.draft ? html`
        <div class="draft">${this._i18n.draft}</div>
        ` : nothing }
        ${this.post.locked ? html`
        <div class="topic-status"
            role="img"
            title="${this._i18n.post_locked_tooltip}"
            aria-label="${this._i18n.post_locked_tooltip}">
          <sakai-icon type="lock" size="small"></sakai-icon></div>
        </div>
        ` : nothing }
        ${this.post.privatePost ? html`
        <div class="topic-status"
            role="img"
            title="${this._i18n.post_private_tooltip}"
            aria-label="${this._i18n.post_private_tooltip}">
          <sakai-icon type="secret" size="small"></sakai-icon></div>
        </div>
        ` : nothing }
      </div>
    `;
  }

  _continueThreadFromHere() {
    this.dispatchEvent(new CustomEvent("continue-thread", { detail: { post: this.post }, bubbles: true }));
  }

  _getGoodAnswerTooltip() {

    switch (this.post.type) {
      case QUESTION:
        return this.post.myReactions.GOOD_ANSWER ? "ungoodanswer_tooltip" : "goodanswer_tooltip";
      case DISCUSSION:
        return this.post.myReactions.GOOD_ANSWER ? "ungoodpost_tooltip" : "goodpost_tooltip";
      default:
        return "";
    }
  }

  _setEditing() { this._editing = true; }

  _unsetEditing() { this._editing = false; }

  _setPrivatePost(e) { this.post.privatePost = e.target.checked; }

  _setPrivateReply(e) { this._privateReply = e.target.checked; }

  _unsetReplying() { this._replying = false; }

  _toggleReplying() { this._replying = !this._replying; }

  _toggleExpanded() { this._expanded = !this._expanded; }

  _toggleShowingMyReactions() { this.showingMyReactions = !this.showingMyReactions; }

  _toggleShowingComments() { this._showingComments = !this._showingComments; }

  _renderMessageRow() {

    return html`
      <div class="post-message-block">

        ${this.post.hidden ? html`
          <div class="sak-banner-info">${this.post.canModerate ? this._i18n.moderator_hidden_message : this._i18n.hidden_message}</div>
          ${this.post.canModerate ? html`
            <div class="post-message">${unsafeHTML(this.post.message)}</div>
          ` : nothing }
        ` : html`
          <div class="post-message">${unsafeHTML(this.post.message)}</div>
        `}
      </div>

      <div>
        ${this.post.canEdit || this.post.canDelete || this.post.canModerate ? html`
          <div class="dropdown">
            <button class="btn btn-transparent"
                id="post-menu-${this.post.id}"
                type="button"
                data-bs-toggle="dropdown"
                title="${this._i18n.post_options_menu_tooltip}"
                aria-label="${this._i18n.post_options_menu_tooltip}"
                aria-haspopup="true"
                aria-expanded="false">
              <div><sakai-icon type="menu" size="small"></sakai-icon></div>
            </button>
            <ul class="dropdown-menu conv-dropdown-menu" aria-labelledby="post-menu-${this.post.id}">
              ${this.post.canEdit ? html`
              <li>
                <button class="btn dropdown-item"
                    type="button"
                    @click=${this._setEditing}
                    title="${this._i18n.edit_this_post}"
                    aria-label="${this._i18n.edit_this_post}">
                  ${this._i18n.edit}
                </button>
              </li>
              ` : nothing }
              ${this.post.canDelete ? html`
              <li>
                <button class="dropdown-item"
                    type="button"
                    @click=${this._deletePost}
                    title="${this._i18n.delete_this_post}"
                    aria-label="${this._i18n.delete_this_post}">
                  ${this._i18n.delete}
                </button>
              </li>
              ` : nothing }
              ${this.post.canModerate ? html`
              <li>
                <button class="dropdown-item"
                    type="button"
                    @click=${this._toggleHidden}>
                  ${this._i18n[this.post.hidden ? "show" : "hide"]}
                </button>
              </li>
              <li>
                <button class="dropdown-item"
                    type="button"
                    @click=${this._toggleLocked}>
                  ${this._i18n[this.post.locked ? "unlock" : "lock"]}
                </button>
              </li>
              ` : nothing }
            </ul>
          </div>
        ` : nothing }
      </div>
    `;
  }

  _renderEditor() {

    return html`
      <div class="post-editor-block">
        <sakai-editor id="post-${this.post.id}-editor" content="${this.post.message}" set-focus></sakai-editor>
        <div class="conv-private-checkbox-block">
          <label>
            <input type="checkbox" .checked=${this.post.privatePost} @click=${this._setPrivatePost} ?disabled=${this.post.parentIsPrivate}>
            <span>${this._i18n.private_reply}</span>
          </label>
        </div>
        <div class="act">
          <input type="button" class="active" @click=${this._publishPost} value="${this._i18n.publish}">
          <input type="button" @click=${this._savePostAsDraft} value="${this._i18n.save_as_draft}">
          <input type="button" @click=${this._unsetEditing} value="${this._i18n.cancel}">
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
            <input type="checkbox" @click=${this._setPrivateReply} .checked=${this.post.privatePost} ?disabled=${this.post.privatePost}>
            <span>${this._i18n.private_reply}</span>
          </label>
        </div>
        <div class="act">
          <input type="button" class="active" @click=${this._replyToPostAsPublished} value="${this._i18n.publish}">
          <input type="button" @click=${this._replyToPostAsDraft} value="${this._i18n.save_as_draft}">
          <input type="button" @click=${this._unsetReplying} value="${this._i18n.cancel}">
        </div>
      </div>
    `;
  }

  _renderDiscussionPost() {

    return html`

      <div id="discussion-post-block-${this.post.id}" class="discussion-post-block">

        <div class="discussion-post-left-column">
          <div class="photo">
            <sakai-user-photo
                user-id="${this.post.anonymous && !this.canViewAnonymous ? "blank" : this.post.creator}"
                size-class="medium-thumbnail"
                profile-popup="on"
            >
            </sakai-user-photo>
          </div>
          ${this.post?.posts?.length ? html`
          <div class="discussion-post-vbar">
          </div>
          ` : nothing }
        </div>

        <div class="discussion-post-right-column">

          <div class="discussion-post-content-wrapper ${!this.post.viewed ? "new" : nothing }">
            <div id="post-${this.post.id}" class="discussion-post-content" data-post-id="${this.post.id}">
              ${this._renderAuthorDetails()}
              <div>
              ${this.post.late && (this.isInstructor || this.post.isMine) ? html`
              <div class="discussion-post-late">${this._i18n.late}</div>
              ` : nothing }
              ${!this.post.viewed ? html`
              <div class="discussion-post-new">${this._i18n.new}</div>
              ` : nothing }
              </div>
              ${this._editing ? html`
                ${this._renderEditor()}
              ` : html`
              ${this._renderMessageRow()}
              `}
            </div>
            ${this._editing ? "" : html`
            <div class="discussion-post-bottom-bar">
              <div class="post-actions-block">
                ${this.post.canViewUpvotes ? html`
                <div class="post-upvote-block">
                  ${this.post.canUpvote ? html`
                  <a href="javascript:;"
                      @click=${this._toggleUpvotePost}
                      aria-label="${this.post.upvoted ? this._i18n.downvote_tooltip : this._i18n.upvote_tooltip}"
                      title="${this.post.upvoted ? this._i18n.downvote_tooltip : this._i18n.upvote_tooltip}"
                      ?disabled=${!this.post.canUpvote}>
                    ${this._renderUpvote()}
                  </a>
                  ` : this._renderUpvote()
                  }
                </div>
                ` : nothing }
              ${!this.post.hidden || this.post.canModerate ? html`
                ${this._renderReactionsBlock()}
                ${this.renderReactionsBar(this.post.reactionTotals)}
              ` : nothing }
              </div>
              <div class="discussion-post-reply-options">
                ${this.post.canReply ? html`
                <div>
                  <a href="javascript:;"
                      @click=${this._toggleReplying}
                      aria-label="${this._i18n.reply_tooltip}"
                      title="${this._i18n.reply_tooltip}">
                    ${this._i18n.reply}
                  </a>
                </div>
                ` : nothing }
                ${this.post.isThread && this.post.posts.length ? html`
                <div class="discussion-post-toggle-replies">
                  <a href="javascript:;"
                      @click=${this._toggleExpanded}
                      aria-label="${this._expanded ? this._i18n.hide_replies_tooltip : this._i18n.show_replies_tooltip}"
                      title="${this._expanded ? this._i18n.hide_replies_tooltip : this._i18n.show_replies_tooltip}">
                    <div class="post-replies-toggle-block">
                      <div class="post-replies-toggle-icon">
                        <sakai-icon
                            type="${this._expanded ? "chevron-down" : "chevron-up"}"
                            size="small">
                        </sakai-icon>
                      </div>
                      <div>${this.post.posts.length} ${this.post.posts.length == 1 ? this._i18n.reply : this._i18n.replies}</div>
                    </div>
                  </a>
                </div>
                ` : nothing }
              </div>
            </div>
            `}
          </div>

          ${this._replying ? html`
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
          ` : nothing }
          ${this.post.depth % 5 === 0 && !this.post.continued ? html`
            <a href="javascript:;"
                class="discussion-post-continue-block"
                @click=${this._continueThreadFromHere}>
              <div>Continue this thread</div>
              <div><sakai-icon type="right-arrow" size="small"></sakai-icon></div>
            </a>
          ` : html`

            ${this.post.posts && this._expanded ? html`
              ${this.post.posts.map(p => html`
                <sakai-post
                    post="${JSON.stringify(p)}"
                    postType="${this.postType}"
                    ?is-instructor="${this.isInstructor}"
                    ?can-view-anonymous="${this.canViewAnonymous}"
                    ?can-view-deleted="${this.canViewDeleted}"
                    site-id="${this.siteId}"
                    @comment-deleted=${this._commentDeleted}>
                </sakai-post>
              `)}
            ` : nothing }
          `}
        </div>

      </div>
    `;
  }

  _renderReactionsBlock() {

    return html`
      <div class="reactions-block post-option
          ${this.post.myReactions.GOOD_ANSWER ? "good-answer-on" : nothing }
          ${this.post.isMine && this.post.reactionTotals.GOOD_ANSWER > 0 ? "good-answer-on" : nothing }"
      >
        ${this.post.isMine && this.post.reactionTotals.GOOD_ANSWER > 0 ? html`
        <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
        <div>
          ${this.post.type == QUESTION ? this._i18n.goodanswer : this._i18n.goodpost}
          ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : nothing }
        </div>
        ` : nothing }
        ${!this.post.isMine && this.post.canReact ? html`
        <div>
        ${!this.post.locked && this.post.canReact ? html`
          <a href="javascript:;"
              class="post-option"
              data-reaction="GOOD_ANSWER"
              @click=${this.toggleReaction}
              aria-label="${this._getGoodAnswerTooltip()}"
              title="${this._getGoodAnswerTooltip()}">
            <div><sakai-icon type="thumbs-up" size="small"></sakai-icon></div>
            <div>
              ${this.post.type == QUESTION ? this._i18n.goodanswer : this._i18n.goodpost}
              ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : nothing }
            </div>
          </a>
        ` : html`
          ${this.post.type == QUESTION ? this._i18n.goodanswer : this._i18n.goodpost}
          ${this.isInstructor && this.post.reactionTotals.GOOD_ANSWER ? ` - ${this.post.reactionTotals.GOOD_ANSWER}` : nothing }
        `}
        </div>
        ` : nothing }
      </div>

      ${this.post.canReact ? html`
      <div class="post-option single">
        <div class="dropdown">
          <button class="btn btn-transparent"
              id="post-reactions-${this.post.id}"
              type="button"
              @click=${this._toggleShowingMyReactions}
              data-bs-toggle="dropdown"
              aria-label="${this._i18n.reactions_tooltip}"
              aria-expanded="false"
              title="${this._i18n.reactions_tooltip}">
            <sakai-icon type="smile" size="small"></sakai-icon>
          </button>
          <ul class="dropdown-menu conv-dropdown-menu"
              aria-labelledby="post-reactions-${this.post.id}">
            ${this.renderMyReactions(this.post.myReactions)}
          </ul>
        </div>
      </div>
      ` : nothing }
    `;
  }

  _renderUpvote() {

    return html`
      <div class="post-upvote-container ${!this.post.upvotes ? "no-votes" : nothing }"
          title="${ifDefined(this.post.canUpvote ? "" : this.post.upvotes ? this.tr("upvote_count_tooltip", [ this.post.upvotes ]) : this._i18n.no_upvotes_tooltip)}">
        <div>
          <sakai-icon type="${this.post.upvoted ? "down" : "up"}" size="smallest"></sakai-icon>
        </div>
        <div>${this.post.upvotes || 0}</div>
      </div>
    `;
  }

  _renderQAPost() {

    return html`
      <div id="post-${this.post.id}"
          data-post-id="${this.post.id}"
          class="post ${this.post.isInstructor ? "instructor" : nothing }
          ${(!this.post.comments || !this.post.comments.length) && !this.post.canComment ? "post-without-comment-block" : nothing }">

        <div class="post-topbar">
          <div class="photo">
            <sakai-user-photo
                user-id="${this.post.anonymous && !this.canViewAnonymous ? "blank" : this.post.creator}"
                classes="medium-thumbnail"
                profile-popup="on">
            </sakai-user-photo>
          </div>
          ${this._renderAuthorDetails()}
          ${this.post.late && (this.isInstructor || this.post.isMine) ? html`
          <div class="discussion-post-late">late</div>
          ` : nothing }
          ${this.post.isInstructor ? html`
          <div class="conv-instructors-answer">${this._i18n.instructors_answer}</div>
          ` : nothing }
        </div>

        <div class="post-main">
          <div>
          </div>
          ${this._renderMessageRow()}
        </div>

        ${this._editing ? html`
          ${this._renderEditor()}
        ` : html`
        <div class="post-reactions-comment-toggle-block">
          ${this.post.canViewUpvotes ? html`
            <div class="post-upvote-block">
              ${this.post.canUpvote ? html`
              <a href="javascript:;"
                  @click=${this._toggleUpvotePost}
                  aria-label="${this.post.upvoted ? this._i18n.downvote_tooltip : this._i18n.upvote_tooltip}"
                  title="${this.post.upvoted ? this._i18n.downvote_tooltip : this._i18n.upvote_tooltip}"
                  ?disabled=${!this.post.canUpvote}>
                ${this._renderUpvote()}
              </a>
              ` : this._renderUpvote()
              }
            </div>
          ` : nothing }
          <div class="post-actions-block">
            ${this._renderReactionsBlock()}
            ${this.renderReactionsBar(this.post.reactionTotals)}
          </div>
          <div class="ms-auto">
          ${this.post.numberOfComments > 0 ? html`
          <a href="javascript:;"
              aria-label="${this._showingComments ? this._i18n.hide_comments_tooltip : this._i18n.show_comments_tooltip}"
              title="${this._showingComments ? this._i18n.hide_comments_tooltip : this._i18n.show_comments_tooltip}"
              @click=${this._toggleShowingComments}>
            <div class="post-comment-toggle-block">
              <div class="post-comment-toggle-icon">
                <sakai-icon
                    type="${this._showingComments ? "chevron-down" : "chevron-up"}"
                    size="small">
                </sakai-icon>
              </div>
              <div>${this.post.numberOfComments} ${this.post.numberOfComments == 1 ? this._i18n.comment : this._i18n.comments}</div>
            </div>
          </a>
          ` : nothing }
          </div>
        </div>
        `}
      </div>

      <div class="post-comments-block">
        ${this._showingComments && this.post.comments && this.post.numberOfComments > 0 ? html`
        <div class="post-comments">
        ${this.post.comments.map(c => html`
          <sakai-comment
              comment="${JSON.stringify(c)}"
              topic-id="${this.post.topic}"
              site-id="${this.siteId}"
              @comment-updated=${this._commentUpdated}
              @comment-deleted=${this.commentDeleted}>
          </sakai-comment>
        `)}
        </div>
        ` : nothing }
        ${this.post.canComment ? html`
        <div class="post-add-comment-block">
          <div>
            <sakai-user-photo user-id="${window.top.portal.user.id}" classes="medium-thumbnail">
            </sakai-user-photo>
          </div>
          <div>
            <sakai-comment-editor
                post-id="${this.post.id}"
                site-id="${this.siteId}"
                topic-id="${this.post.topic}"
                @comment-created=${this._commentCreated}>
            </sakai-comment-editor>
          </div>
        </div>
        ` : nothing }
      </div>
    `;
  }

  shouldUpdate() {
    return this._i18n && this.post;
  }

  updated() {

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
    }
  }

  render() {
    return this.postType === QUESTION ? this._renderQAPost() : this._renderDiscussionPost();
  }
}
