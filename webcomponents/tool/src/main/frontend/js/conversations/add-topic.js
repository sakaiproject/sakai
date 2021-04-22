import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-editor.js";

export class AddTopic extends SakaiElement {

  static get properties() {

    return {
      aboutReference: { attribute: "about-reference", type: String },
      topic: { type: Object },
    };
  }

  constructor() {

    super();

    this.topic = {
      title: "",
      message: "",
      type: "QUESTION",
      visibility: "INSTRUCTORS",
    };

    this.new = true;

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set topic(value) {

    this._topic = value;
    this.new = false;
    this.requestUpdate();
  }

  get topic() { return this._topic; }
  
  save() {

    const url = `/api/topics${!this.new ? `/${this.topic.id}` : ""}`;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(this.topic),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while posting.");
      }
      return r.json();
    })
    .then(topic => {
      this.dispatchEvent(new CustomEvent("topic-saved", { detail: { topic: this.topic } }));
    })
    .catch (error => {
      console.error(error);
      //TODO: show error message to user here
    });
  }

  updateMessage(e) {
    this.topic.message = e.detail.content;
  }

  setType(e) {
    this.topic.type = e.target.dataset.type;
    this.requestUpdate();
  }

  selectTag(e) {

    const tag = e.target.dataset.tag;

    const existingIndex = this.topic.tags.indexOf(tag);
    if (existingIndex !== -1) {
      this.topic.tags.splice(existingIndex, 1);
    } else {
      this.topic.tags.push(tag);
    }

    this.requestUpdate();
  }

  shouldUpdate() {
    return this.i18n && (this.topic || this.aboutReference);
  }

  render() {

    return html`
      <div id="add-topic-wrapper">
        <h1>${this.new ? this.i18n["add_a_new_post"] : this.i18n["edit_post"]}</h1>

        <div class="add-topic-block">
          <div id="post-type-label" class="add-topic-label">${this.i18n["post_type"]}</div>
          <div @click=${this.setType}
              data-type="QUESTION"
              class="topic-type-toggle ${this.topic.type === "QUESTION" ? "active" : ""}">
            <div>${this.i18n["type_question"]}</div>
          </div>
          <div @click=${this.setType}
              data-type="DISCUSSION"
              class="topic-type-toggle ${this.topic.type === "DISCUSSION" ? "active" : ""}">
            <div>${this.i18n["type_discussion"]}</div>
          </div>
        </div>

        <div class="add-topic-block">
          <div id="summary-label" class="add-topic-label">${this.i18n["summary"]}</div>
          <input id="summary"
            @blur=${e => this.topic.title = e.target.value}
            .value="${this.topic.title}" />
        </div>
        <div class="add-topic-block">
          <div id="details-label" class="add-topic-label">${this.i18n["details"]}</div>
          <sakai-editor
              toolbar="basic"
              content="${this.topic.message}"
              @changed=${this.updateMessage}
              element-id="topic-details"
              id="topic-details-editor">
          </sakai-editor>
        </div>

        <div id="tag-post-block" class="add-topic-block">
          <div id="tag-post-label" class="add-topic-label">${this.i18n["tag_post"]}</div>
          <div>
          ${this.topic.availableTags.map(tag => html`
            <div data-tag="${tag}" @click=${this.selectTag}
                class="topic-tag ${this.topic.tags.includes(tag) ? "selected-tag" : ""}">${tag}</div>
          `)}
          </div>
        </div>

        <div id="post-to-block" class="add-topic-block">
          <div id="post-to-label" class="add-topic-label">${this.i18n["post_to"]}</div>
          <input type="radio" name="post-to" value="SITE"
            ?checked=${this.topic.visibility === "SITE"} />${this.i18n["everyone"]}
          <input type="radio" name="post-to" value="INSTRUCTORS"
            ?checked=${this.topic.visibility === "INSTRUCTORS"} />${this.i18n["instructors"]}
        </div>

        <div id="post-options-block" class="add-topic-block">
          <div id="post-options-label" class="add-topic-label">${this.i18n["post_options"]}</div>
          <input type="checkbox" id="pinned-checkbox"
            @click=${(e) => this.topic.pinned = e.target.checked}
            ?checked=${this.topic.pinned}>${this.i18n["pinned"]}
          </input>
          <input type="checkbox" id="anonymous-checkbox"
            @click=${(e) => this.topic.anonymous = e.target.checked}
            ?checked=${this.topic.anonymous}>${this.i18n["anonymous"]}
          </input>
          <input type="checkbox" id="anonymous-comments-checkbox"
            @click=${(e) => this.topic.anonymousComments = e.target.checked}
            ?checked=${this.topic.anonymousComments}>${this.i18n["anonymous_comments"]}
          </input>
        </div>

        <div>
          <button @click=${this.save} active>${this.i18n["save"]}</button>
          <button id="cancel-topic">${this.i18n["cancel"]}</button>
        </div>
      </div>
    `;
  }
}

if (!customElements.get("add-topic")) {
  customElements.define("add-topic", AddTopic);
}
