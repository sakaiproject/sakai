import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-user-photo";
import "../sakai-comment-editor.js";

export class SakaiComment extends SakaiElement {

  static properties = {

    comment: { type: Object },
    topicId: { attribute: "topic-id", type: String },
    siteId: { attribute: "site-id", type: String },
    _editing: { attribute: false, type: Boolean },
    _i18n: { attribute: false, type: Boolean },
  };

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this._i18n = r);
  }

  _deleteComment() {

    if (!confirm(this._i18n.confirm_comment_delete)) {
      return;
    }

    const url = `/api/sites/${this.siteId}/topics/${this.topicId}/posts/${this.comment.postId}/comments/${this.comment.id}`;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while deleting a comment.");
      } else {
        this.dispatchEvent(new CustomEvent("comment-deleted", { detail: { comment: this.comment }, bubbles: true } ));
      }
    })
    .catch(error => console.error(error));
  }

  _commentUpdated(e) {

    this.comment = e.detail.comment;
    this._editing = false;
  }

  _startEditing() { this._editing = true; }

  _stopEditing() { this._editing = false; }

  updated() {

    if (typeof MathJax !== "undefined") {
      MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
    }
  }

  shouldUpdate() {
    return this._i18n && this.comment;
  }

  _renderOptionsMenu() {

    return html`
      <div class="dropdown">
        <button class="btn btn-transparent"
            id="comment-options-${this.comment.id}"
            type="button"
            title="${this._i18n.comment_options_menu_tooltip}"
            data-bs-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
            aria-label="${this._i18n.comment_options_menu_tooltip}">
          <sakai-icon type="menu" size="small"></sakai-icon>
        </button>
        <ul class="dropdown-menu conv-dropdown-menu" aria-labelledby="comment-options-${this.comment.id}">
          ${this.comment.canEdit ? html`
          <li>
            <button class="btn btn-icon dropdown-item"
                type="button"
                title="${this._i18n.edit_this_comment}"
                @click=${this._startEditing}
                aria-label="${this._i18n.edit_this_comment}">
              ${this._i18n.edit}
            </button>
          </li>
          ` : ""}
          ${this.comment.canDelete ? html`
          <li>
            <button class="dropdown-item"
                type="button"
                @click=${this._deleteComment}
                title="${this._i18n.delete_this_comment}"
                aria-label="${this._i18n.delete_this_comment}">
              ${this._i18n.delete}
            </button>
          </li>
          ` : nothing }
        </ul>
      </div>
    `;
  }

  render() {

    return html`
      <div class="post-comment">
        <div class="post-comment-topbar">
          <div class="photo">
            <sakai-user-photo
                user-id="${this.comment.creator}"
                classes="medium-thumbnail"
                profile-popup="on">
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
            ${this._editing ? html`
            <div class="post-edit-comment-block">
              <sakai-comment-editor
                  post-id="${this.comment.postId}"
                  site-id="${this.siteId}"
                  topic-id="${this.topicId}"
                  comment="${JSON.stringify(this.comment)}"
                  @comment-updated=${this._commentUpdated}
                  @editing-cancelled=${this._stopEditing}
                  show-buttons>
              </sakai-comment-editor>
            </div>
            <div></div>
            ` : html`
            <div class="post-message">${unsafeHTML(this.comment.message)}</div>
            <div class="post-reactions-block">
              ${!this.comment.locked && (this.comment.canEdit || this.comment.canDelete) ? html `
              ${this._renderOptionsMenu()}
              ` : ""}
            </div>
            `}
          </div>
        </div>
      </div>
    `;
  }
}
