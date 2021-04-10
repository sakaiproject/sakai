import { html } from "../assets/lit-element/lit-element.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-user-photo.js";
import moment from "../assets/moment/dist/moment.js";
import "../sakai-editor.js";

export class SakaiTopic extends SakaiElement {

  static get properties() {

    return {
      topicId: { attribute: "topic-id", type: String },
      topic: { type: Object },
      creatingPost: Boolean,
      replyEditorDisplayed: { type: Array },
    };
  }

  constructor() {

    super();

    this.replyEditorDisplayed = [];

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  formatDate(millis) {

    const dateTime = new Date(millis);
    const date = dateTime.toLocaleDateString( 'en-gb', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      timeZone: 'utc'
    });
    const time = dateTime.toLocaleTimeString( 'en-gb', {
      hour: "numeric",
      minute: "numeric",
    });
    return `${date} (${time})`;
  }

  set topicId(value) {

    this._topicId = value;

    fetch(`/api/topics/${this.topicId}`)
      .then(r => r.json())
      .then(t => {

        t.lastActivityHuman = moment.duration(t.lastActivity - Date.now(), "milliseconds").humanize(true);
        t.formattedCreatedDate = this.formatDate(t.created);

        t.posts.forEach(p => this.setFormattedCreatedDate(p));

        this.topic = t;
      });
  }

  setFormattedCreatedDate(p) {

    p.formattedCreatedDate = this.formatDate(p.created);
    p.replies && p.replies.forEach(p => this.setFormattedCreatedDate(p));
  }

  decoratePost(p) {
      this.setFormattedCreatedDate(p);
  }

  get topicId() { return this._topicId; }

  shouldUpdate() {
    return this.i18n && this.topic;
  }
  
  firstUpdated() {
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

  postToTopic() {

    this.creatingPost = false;

    const post = {
      message: this.querySelector("sakai-editor").getContent(),
    };

    fetch(`/api/topics/${this.topic.id}`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(post),
    })
    .then(r => {

      console.log(r);

      if (!r.ok) {
        throw new Error("Network error while posting to topic.");
      }
      return r.json();
    })
    .then(post => {

      this.decoratePost(post);
      this.topic.posts.push(post);
      this.requestUpdate();
    })
    .catch (error => {
      //TODO: show error message to user here
    });
  }

  renderPost(p, isReply) {

    return html`
      <div class="post ${isReply ? "reply" : ""}">
        <div class="author-block">
          <div><sakai-user-photo user-id="${p.creator}" size-class="medium-thumbnail"></sakai-user-photo></div>
          <div>
            <div class="author-details">
              <span>${p.creatorDisplayName}</span>
              <div class="message-date">${p.formattedCreatedDate}</div>
            </div>
          </div>
        </div>
        <div class="message">${unsafeHTML(p.message)}</div>
        ${p.replyable ? html`
        <div class="reply-block">
          <a href="javascript:;" data-post-id="${p.id}" @click=${this.toggleReplyToPost}>Reply</a>
          <div class="post-reply-editor-block" style="display: ${this.replyEditorDisplayed[p.id] ? "block" : "none"}">
            <sakai-editor toolbar="basic" element-id="reply-to-post-${p.id}"></sakai-editor>
            <div class="post-buttons">
              <button data-post-id="${p.id}" @click=${this.replyToPost} active>Post</button>
              <button data-post-id="${p.id}" @click=${this.toggleReplyToPost}>Cancel</button>
            </div>
          </div>
        </div>
        ` : ""}
      </div>
      ${p.replies ? html`
      <div class="replies">
        ${p.replies.map(r => this.renderPost(r, true))}
      </div>
      ` : ""}
    `;
  }

  render() {

    return html`
      <div class="title-block">
        <span class="title">${this.topic.title}</span>
        <span class="author"> ${this.i18n["by"]} ${this.topic.creatorDisplayName}</span>
      </div>
      <div class="post starter-post">
        <div class="author-block">
          <div><sakai-user-photo user-id="${this.topic.creator}"></sakai-user-photo></div>
          <div>
            <div class="author-details">
              <span>${this.topic.creatorDisplayName}</span>
              <span>(${this.topic.creatorRole})</span>
              <span> - ${this.i18n["topic_author"]}</span>
              <div class="message-date">${this.topic.formattedCreatedDate}</div>
            </div>
          </div>
        </div>
        <div class="message">${unsafeHTML(this.topic.message)}</div>
      </div>
      <div class="post-to-topic">
        <div class="post-to-topic-link-block">
          <div><sakai-user-photo user-id="${parent.portal.userId}" size-class="small-thumbnail"></sakai-user-photo></div>
          <div class="post-to-topic-link"><a href="javascript:;" @click=${this.toggleCreatePost}>Post to Topic ...</a></div>
        </div>
        <div class="post-editor" style="display: ${this.creatingPost ? "block" : "none"}">
          <sakai-editor toolbar="basic" element-id="post-to-topic-${this.topic.id}"></sakai-editor>
          <div class="post-buttons">
            <button @click=${this.postToTopic} active>Post</button>
            <button @click=${this.toggleCreatePost}>Cancel</button>
          </div>
        </div>
      </div>
      <div class="posts">
        ${this.topic.posts.map(p => this.renderPost(p))}
      </div>
    `;
  }
}

if (!customElements.get("sakai-topic")) {
  customElements.define("sakai-topic", SakaiTopic);
}
