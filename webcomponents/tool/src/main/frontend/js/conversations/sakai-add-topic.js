import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-editor.js";
import "../sakai-icon.js";
import { QUESTION, DISCUSSION, INSTRUCTORS, SITE, GROUP } from "./sakai-conversations-constants.js";

export class SakaiAddTopic extends SakaiElement {

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      aboutReference: { attribute: "about-reference", type: String },
      groups: { type: Array },
      tags: { attribute: "tags", type: Array },
      canPin: { attribute: "can-pin", type: Boolean },
      canAnonPost: { attribute: "can-anon", type: Boolean },
      canEditTags: { attribute: "can-edit-tags", type: Boolean },
      topic: { type: Object },
      titleError: { attribute: false, type: Boolean },
    };
  }

  constructor() {

    super();

    this.new = true;

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set aboutReference(value) {

    this._aboutReference = value;
    this.site.aboutReference = value;
  }

  get aboutReference() { return this._aboutReference; }

  set topic(value) {

    this._topic = value;
    this.new = value.id === "";
    this.requestUpdate();
  }

  get topic() { return this._topic; }

  set tags(value) {

    this._tags = value;
    this.selectedTagId = this._tags.length ? this._tags[0].id : null;
  }

  get tags() { return this._tags; }

  saveAsDraft() {
    this.save(true);
  }

  saveWip() {
    this.dispatchEvent(new CustomEvent("save-wip-topic", { detail: { topic: this.topic }, bubbles: true }));
  }

  publish() {
    this.save(false);
  }

  save(draft) {

    if (this.topic.title.length < 4) {
      this.titleError = true;
      return;
    }

    this.topic.draft = draft;

    const lightTopic = {};
    Object.assign(lightTopic, this.topic);
    lightTopic.posts = [];

    fetch(this.topic.url, {
      method: this.new ? "POST" : "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(lightTopic),
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while creating topic.");
      } else {
        return r.json();
      }
    })
    .then(topic => {

      Object.assign(this.topic, topic);
      this.dispatchEvent(new CustomEvent("topic-saved", { detail: { topic: this.topic }, bubbles: true }));
    })
    .catch (error => {
      console.error(error);
    });
  }

  updateMessage(e) {

    this.topic.message = e.detail.content;
    this.dispatchEvent(new CustomEvent("topic-dirty", { bubbles: true }));
    this.saveWip();
  }

  updateSummary(e) {

    this.topic.title = e.target.value;
    this.dispatchEvent(new CustomEvent("topic-dirty", { bubbles: true }));
    this.saveWip();
  }

  cancel() {
    this.dispatchEvent(new CustomEvent("topic-add-cancelled", { bubbles: true }));
  }

  setType(e) {

    this.topic.type = e.target.dataset.type;
    this.requestUpdate();
  }

  selectTag() {

    const tagId = this.selectedTagId;

    const existingIndex = this.topic.tags.findIndex(t => t.id == tagId);
    if (existingIndex !== -1) {
      this.topic.tags.splice(existingIndex, 1);
    } else {
      const tag = this.tags.find(t => t.id == tagId);
      this.topic.tags.push(tag);
    }

    this.saveWip();

    this.requestUpdate();
  }

  removeTag(e) {

    const tagId = e.target.dataset.tagId;
    const existingIndex = this.topic.tags.findIndex(t => t.id == tagId);
    this.topic.tags.splice(existingIndex, 1);
    this.requestUpdate();
  }

  editAvailableTags() {

    this.saveWip();
    this.dispatchEvent(new CustomEvent("edit-tags", { bubbles: true }));
  }

  toggleGroup(e) {

    const groupRef = e.target.value;
    this.topic.groups = this.topic.groups || [];

    if (e.target.checked) {
      this.topic.groups.push(groupRef);
    } else {
      const index = this.topic.groups.findIndex(g => g === groupRef);
      if (index !== -1) {
        this.topic.groups.splice(index, 1);
      }
    }

    this.saveWip();
  }

  _setVisibility(e) {

    this.topic.visibility = e.target.dataset.visibility;
    if (this.topic.visibility != GROUP) {
      this.topic.groups = [];
    }
    this.saveWip();
    this.requestUpdate();
  }

  firstUpdated() {

    this.querySelector(".summary-input").focus();
    /*
    let url = `addTopic`;
    history.pushState({ state: STATE_ADDING_TOPIC }, "", url);
    */
  }

  shouldUpdate() {
    return this.i18n && this.tags && (this.topic || this.aboutReference);
  }

  render() {

    return html`
      ${this.topic.beingEdited ? html`
      <div class="sak-banner-info">${this.i18n.editing_topic}</div>
      ` : ""}
      <div class="add-topic-wrapper">
        <h1>${this.new ? this.i18n.add_a_new_topic : this.i18n.edit_topic}</h1>

        <!--div class="add-topic-block">
          <div id="post-type-label" class="add-topic-label">${this.i18n.topic_type}</div>
          <div id="topic-type-toggle-block">
            <div @click=${this.setType}
                data-type="${QUESTION}"
                class="topic-type-toggle ${this.topic.type === QUESTION ? "active" : ""}">
              <sakai-icon type="question" size="medium"></sakai-icon>
              <div>${this.i18n.type_question}</div>
            </div>
            <div @click=${this.setType}
                data-type="${DISCUSSION}"
                class="topic-type-toggle ${this.topic.type === DISCUSSION ? "active" : ""}">
              <sakai-icon type="forums" size="medium"></sakai-icon>
              <div>${this.i18n.type_discussion}</div>
            </div>
          </div>
        </div-->

        <div class="add-topic-block">
          <div id="summary-label" class="add-topic-label">${this.i18n.summary} *</div>
          <input id="summary"
            class="summary-input ${this.titleError ? "error" : ""}"
            @blur=${this.updateSummary}
            @focus=${() => this.titleError = false}
            .value="${this.topic.title}" />
          <div class="required"><span>* ${this.i18n.required}</span></div>
        </div>
        <div class="add-topic-block">
          <div id="details-label" class="add-topic-label">${this.i18n.details}</div>
          <sakai-editor
              toolbar="basic"
              content="${this.topic.message}"
              @changed=${this.updateMessage}
              id="topic-details-editor">
          </sakai-editor>
        </div>

        <div id="tag-post-block" class="add-topic-block">
          <div id="tag-post-label" class="add-topic-label">${this.i18n.tag_topic}</div>
          ${this.tags.length > 0 ? html`
          <select @change=${e => this.selectedTagId = e.target.value}>
            ${this.tags.map(tag => html`
            <option value="${tag.id}">${tag.label}</option>
            `)}
          </select>
          <input type="button" value="Add" @click=${this.selectTag}>
          ` : ""}
          ${this.canEditTags ? html`
          <span id="conv-edit-tags-link-wrapper">
            <a href="javascript:;" @click=${this.editAvailableTags}>Edit tags for this course</a>
          </span>
          ` : ""}
          <div id="tags">
          ${this.topic.tags.map(tag => html`
            <div class="tag">
              <div>${tag.label}</div>
              <a href="javascript:;" data-tag-id="${tag.id}" @click=${this.removeTag}>
                <div class="tag-remove-icon">
                  <sakai-icon type="close" size="small"></sakai-icon>
                </div>
              </a>
            </div>
          `)}
          </div>
        </div>

        <div id="post-to-block" class="add-topic-block">
          <div id="post-to-label" class="add-topic-label">${this.i18n.post_to}</div>
          <form>
          <div id="topic-visibility-wrapper">
            <div>
              <label>
              <input
                  type="radio"
                  name="post-to"
                  data-visibility="${SITE}"
                  @click=${this._setVisibility}
                  ?checked=${this.topic.visibility === SITE}>${this.i18n.everyone}
              </label>
            </div>
            <div>
              <label>
              <input
                  type="radio"
                  name="post-to"
                  data-visibility="${INSTRUCTORS}"
                  @click=${this._setVisibility}
                  ?checked=${this.topic.visibility === INSTRUCTORS}>${this.i18n.instructors}
              </label>
            </div>
            ${this.groups ? html`
              <div>
                <label>
                <input
                    type="radio"
                    name="post-to"
                    data-visibility="${GROUP}"
                    @click=${this._setVisibility}
                    ?checked=${this.topic.visibility === GROUP}>${this.i18n.groups}
                </label>
              </div>
              ${this.topic.visibility === GROUP ? html`
              <div id="add-topic-groups-block">
                ${this.groups.map(group => html`
                <div class="add-topic-group-block">
                  <input type="checkbox" @click=${this.toggleGroup} value="${group.reference}" ?checked=${this.topic.groups.includes(group.reference)}>${group.title}</input>
                </div>
                `)}
              </div>
              ` : ""}
            ` : ""}
          </div>
          </form>
        </div>

        <div id="post-options-block" class="add-topic-block">
          <div id="post-options-label" class="add-topic-label">${this.i18n.post_options}</div>
          <div id="topic-options-wrapper">
            ${this.canPin ? html`
            <div>
              <input type="checkbox" id="pinned-checkbox"
                @click=${e => { this.topic.pinned = e.target.checked; this.saveWip(); }}
                ?checked=${this.topic.pinned}>
              </input>
              <div class="topic-options-label-block">
                <div class="topic-option-label">${this.i18n.pinned}</div>
                <div class="topic-option-label-text">${this.i18n.pinned_text}</div>
              </div>
            </div>
            ` : ""}
            ${this.canAnonPost ? html`
            <div>
              <input type="checkbox"
                @click=${e => { this.topic.anonymous = e.target.checked; this.saveWip(); }}
                ?checked=${this.topic.anonymous}>
              </input>
              <div class="topic-options-label-block">
                <div class="topic-option-label">${this.i18n.anonymous}</div>
                <div class="topic-option-label-text">${this.i18n.anonymous_text}</div>
              </div>
            </div>
            <div>
              <input type="checkbox"
                @click=${e => { this.topic.allowAnonymousPosts = e.target.checked; this.saveWip(); }}
                ?checked=${this.topic.allowAnonymousPosts}>
              </input>
              <div class="topic-options-label-block">
                <div class="topic-option-label">${this.i18n.anonymous_posts}</div>
                <div class="topic-option-label-text">${this.i18n.anonymous_posts_text}</div>
              </div>
            </div>
            ` : ""}
          </div>
        </div>

        <div id="button-block" class="act">
          <input type="button" class="active" @click=${this.publish} value="${this.i18n.publish}">
          <input type="button" @click=${this.saveAsDraft} value="${this.i18n.save_as_draft}">
          <input type="button" @click=${this.cancel} value="${this.i18n.cancel}">
        </div>

      </div>
    `;
  }
}

const tagName = "sakai-add-topic";
!customElements.get(tagName) && customElements.define(tagName, SakaiAddTopic);
