import { html } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-editor";

export class SakaiCommentEditor extends SakaiElement {

  static properties = {

    comment: { type: Object },
    postId: { attribute: "post-id", type: String },
    siteId: { attribute: "site-id", type: String },
    topicId: { attribute: "topic-id", type: String },
    _editing: { state: true },
    _i18n: { state: true },
  };

  constructor() {

    super();

    loadProperties("conversations").then(r => this._i18n = r);
  }

  set comment(value) {

    this._comment = value;
    this._editing = true;
  }

  get comment() { return this._comment; }

  _commentOnPost() {

    this.comment = this.comment || { message: "" };

    const editor = this.querySelector("sakai-editor");

    this.comment.message = editor.getContent();

    const isNew = !this.comment.id;

    const postId = this.postId || this.comment.postId;

    this.comment.postId = postId;
    this.comment.topicId = this.topicId;

    const url = `/api/sites/${this.siteId}/topics/${this.topicId}/posts/${postId}/comments${ this.comment.id ? `/${this.comment.id}` : ""}`;
    fetch(url, {
      method: this.comment.id ? "PUT" : "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(this.comment),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while saving comment.");
      }
      return r.json();
    })
    .then(comment => {

      this.dispatchEvent(new CustomEvent(isNew ? "comment-created" : "comment-updated", { detail: { comment }, bubbles: true }));
      this._editing = false;
      this.comment.message = "";
    })
    .catch (error => {
      console.error(error);
      //TODO: show error message to user here
    });
  }

  _startEditing() {
    this._editing = true;
  }

  _cancelEditing() {

    this._editing = false;
    this.dispatchEvent(new CustomEvent("editing-cancelled", { bubbles: true }));
  }

  shouldUpdate() {
    return this._i18n && this.postId;
  }

  render() {

    return html`
      <div>
        ${this._editing ? html`
        <sakai-editor content=${ifDefined(this.comment ? this.comment.message : undefined)} set-focus></sakai-editor>
        <div class="act">
          <input type="button" class="active" @click=${this._commentOnPost} value="${this._i18n.publish}">
          <input type="button" @click=${this._cancelEditing} value="${this._i18n.cancel}">
        </div>
        ` : html`
        <input class="comment-editor-input"
            aria-label="${this._i18n.comment_editor_placeholder}"
            value="${this._i18n.add_a_comment}"
            @click=${this._startEditing}
            @keydown=${this._startEditing} />
        `}
      </div>
    `;
  }
}
