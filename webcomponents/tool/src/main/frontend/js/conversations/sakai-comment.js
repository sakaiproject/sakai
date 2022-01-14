import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-user-photo.js";
import "./sakai-comment-editor.js";
import "./options-menu.js";

export class SakaiComment extends SakaiElement {

  static get properties() {

    return {
      comment: { type: Object },
      topicId: { attribute: "topic-id", type: String },
      siteId: { attribute: "site-id", type: String },
      editing: { type: Boolean },
    };
  }

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  deleteComment() {

    if (!confirm(this.i18n.confirm_comment_delete)) {
      return;
    }

    const url = `/api/sites/${this.siteId}/topics/${this.topicId}/posts/${this.comment.post}/comments/${this.comment.id}`;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while deleting a comment.");
      } else {
        this.dispatchEvent(new CustomEvent("comment-deleted", { detail: { comment: this.comment }, bubbles: true} ));
      }
    })
    .catch(error => console.error(error));
  }

  commentUpdated(e) {

    this.comment = e.detail.comment;
    this.editing = false;
  }

  updated() {

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
    }
  }

  shouldUpdate() {
    return this.i18n && this.comment;
  }

  renderOptionsMenu() {

    return html`
      <options-menu placement="bottom-left">
        <div slot="trigger">
          <button
              class="comment-menu-button"
              title="${this.i18n.comment_options_menu_tooltip}"
              aria-haspopup="true"
              aria-label="${this.i18n.comment_options_menu_tooltip}">
            <sakai-icon type="menu" size="small"></sakai-icon>
          </button>
        </div>

        <div slot="content" class="options-menu" role="dialog">
          ${this.comment.canEdit ? html`
          <div>
            <a href="javascript:;"
                title="${this.i18n.edit_this_comment}"
                @click=${() => this.editing = true}
                arial-label="${this.i18n.edit_this_comment}">
              ${this.i18n.edit}
            </a>
          </div>
          ` : ""}
          ${this.comment.canDelete ? html`
          <div>
            <a href="javascript:;"
                @click=${this.deleteComment}
                title="${this.i18n.delete_this_comment}"
                arial-label="${this.i18n.delete_this_comment}">
              ${this.i18n.delete}
            </a>
          </div>
          ` : ""}
        </div>
      </options-menu>
    `;
  }

  render() {

    return html`
      <div class="post-comment">
        <div class="post-comment-topbar">
          <div class="photo">
            <sakai-user-photo user-id="${this.comment.creator}"
                size-class="medium-thumbnail">
            </sakai-user-photo>
          </div>
          <div class="author-details">
            <div class="post-creator-name">${this.comment.creatorDisplayName}</div>
            <div class="post-created-date">${this.comment.formattedCreatedDate}</div>
          </div>
          <div>
          </div>

          <div class="post-resolved-block">
          </div>
        </div>
        <div class="post-main">
          <div class="post-upvote-block">
          </div>
            ${this.editing ? html`
            <div class="post-edit-comment-block">
              <sakai-comment-editor
                  post-id="${this.comment.post}"
                  site-id="${this.siteId}"
                  topic-id="${this.topicId}"
                  comment="${JSON.stringify(this.comment)}"
                  @comment-updated=${this.commentUpdated}
                  @editing-cancelled=${() => this.editing = false}
                  show-buttons>
              </sakai-comment-editor>
            </div>
            <div></div>
            ` : html`
            <div class="post-message">${unsafeHTML(this.comment.message)}</div>
            <div class="post-reactions-block">
              ${!this.comment.locked && (this.comment.canEdit || this.comment.canDelete) ? html `
              ${this.renderOptionsMenu()}
              ` : ""}
            </div>
            `}
          </div>
        </div>
      </div>
    `;
  }
}

const tagName = "sakai-comment";
!customElements.get(tagName) && customElements.define(tagName, SakaiComment);
