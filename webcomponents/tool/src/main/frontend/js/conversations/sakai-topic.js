import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-user-photo.js";
import moment from "../assets/moment/dist/moment.js";

export class SakaiTopic extends SakaiElement {

  static get properties() {

    return {
      topicId: { attribute: "topic-id", type: String },
      topic: { type: Object },
    };
  }

  constructor() {

    super();

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set topicId(value) {

    this._topicId = value;

    fetch(`/api/topics/${this.topicId}`)
      .then(r => r.json())
      .then(t => {

        t.lastActivityHuman = moment.duration(t.lastActivity - Date.now(), "milliseconds").humanize(true);
        this.topic = t;
      });
  }

  get topicId() { return this._topicId; }

  shouldUpdate() {
    return this.i18n && this.topic;
  }

  render() {

    return html`
      <div class="-title-block">
        <span class="title">${this.topic.title}</span>
        <span class="author"> ${this.i18n["by"]} ${this.topic.creatorDisplayName}</span>
      </div>
      <div class="starter-message">
        <div class="author-block">
          <sakai-user-photo user-id="${this.topic.creator}"></sakai-user-photo>
          <div class="author-details">
            <span>${this.topic.creatorDisplayName}</span>
            <span>(${this.topic.creatorRole})</span>
            <span> - ${this.i18n["topic_author"]}</span>
          </div>
      </div>
    `;
  }
}

if (!customElements.get("sakai-topic")) {
  customElements.define("sakai-topic", SakaiTopic);
}
