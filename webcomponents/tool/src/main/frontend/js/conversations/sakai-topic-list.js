import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "./sakai-topic-summary.js";
import { QUESTION, DISCUSSION } from "./sakai-conversations-constants.js";

export class SakaiTopicList extends SakaiElement {

  static get properties() {

    return {
      aboutRef: { attribute: "about-ref", type: String },
      siteId: { attribute: "site-id", type: String },
      data: { type: Array },
      filteredPinnedTopics: { type: Array },
      filteredDraftTopics: { attribute: false, type: Array },
      filteredUnpinnedTopics: { attribute: false,  type: Array },
      expandDraft: { type: Boolean },
      expandTheRest: { type: Boolean },
      tagsInUse: { attribute: false, type: Array },
    };
  }

  constructor() {

    super();

    this.expandDraft = true;
    this.expandTheRest = true;

    this.NONE = "none";
    this.BY_QUESTION = "by_question";
    this.BY_RESOLVED_QUESTION = "by_resolved_question";
    this.BY_DISCUSSION = "by_discussion";
    this.BY_DISCUSSION_WITH_POSTS = "by_discussion_with_posts";
    this.BY_BOOKMARKED = "by_bookmarked";
    this.BY_MODERATED = "by_moderated";
    this.BY_UNVIEWED = "by_unviewed";

    this.currentFilter = this.NONE;

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set data(value) {

    this._data = value;

    this.initialFilter();

    this.filter(this.currentFilter);

    this.tagsInUse = [];

    value.topics.forEach(topic => {
      topic.tags.forEach(t => { if (!this.tagsInUse.find(e => e.id === t.id)) this.tagsInUse.push(t); });
    });

    this.requestUpdate();
  }

  get data() { return this._data; }

  set aboutRef(value) {

    this._aboutRef = value;

    const url = `/api/sites/${this.siteId}/topics?aboutRef=${this.aboutRef}`;
    fetch(url)
      .then(r => r.json())
      .then(data => this.data = data);
  }

  get aboutRef() { return this._aboutRef; }

  initialFilter() {

    this.pinnedTopics = this.data.topics.filter(t => t.pinned);
    this.filteredPinnedTopics = this.pinnedTopics;

    this.draftTopics = this.data.topics.filter(t => t.draft);
    this.filteredDraftTopics = this.draftTopics;

    this.unpinnedTopics = this.data.topics.filter(t => !t.pinned && !t.draft);
    this.filteredUnpinnedTopics = this.unpinnedTopics;
  }

  filter(filter) {

    switch (filter) {

      case this.BY_QUESTION:
        this.filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === QUESTION);
        this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === QUESTION);
        break;
      case this.BY_DISCUSSION:
        this.filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === DISCUSSION);
        this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === DISCUSSION);
        break;
      case this.BY_RESOLVED_QUESTION:
        this.filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === QUESTION && t.resolved);
        this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === QUESTION && t.resolved);
        break;
      case this.BY_DISCUSSION_WITH_POSTS:
        this.filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === DISCUSSION && t.numberOfPosts);
        this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === DISCUSSION && t.numberOfPosts);
        break;
      case this.BY_BOOKMARKED:
        this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.bookmarked);
        break;
      case this.BY_MODERATED:
        this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.hidden || t.locked);
        break;
      case this.BY_UNVIEWED:
        this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.numberOfUnreadPosts > 0);
        break;
      default:
        this.filteredPinnedTopics = this.pinnedTopics;
        this.filteredUnpinnedTopics = this.unpinnedTopics;
    }

    this.currentFilter = filter;
  }

  filterSelected(e) {
    this.filter(e.target.value);
  }

  tagSelected(e) {

    if (e.target.value === "none") {
      this.filteredPinnedTopics = this.pinnedTopics;
      this.filteredUnpinnedTopics = this.unpinnedTopics;
    } else {
      this.filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.tags.find(tag => tag.id == e.target.value));
    }
  }

  shouldUpdate() {
    return this.i18n && this.data;
  }

  render() {

    return html`
      <div class="topic-list">

        <div id="topic-list-filters">
          <div id="topic-list-filter-tags">
            <select @change=${this.tagSelected} aria-label="Filter by tag">
              <option value="none">${this.i18n.tag_any}</option>
            ${this.tagsInUse.map(tag => html`
              <option value="${tag.id}">${this.i18n.tag} ${tag.label}</option>
            `)}
            </select>
          </div>
          <div id="topic-list-filter-dunno">
            <select @change=${this.filterSelected} aria-label="Filter by various">
              <option value="none">${this.i18n.filter_none}</option>
              <!--option value="${this.BY_QUESTION}">${this.i18n.filter_questions}</option-->
              <option value="${this.BY_RESOLVED_QUESTION}">${this.i18n.filter_answered}</option>
              <!--option value="${this.BY_DISCUSSION}">${this.i18n.filter_discussions}</option-->
              <!--option value="${this.BY_DISCUSSION_WITH_POSTS}">${this.i18n.filter_discussions_with_posts}</option-->
              <option value="${this.BY_BOOKMARKED}"}>${this.i18n.filter_bookmarked}</option>
              <option value="${this.BY_MODERATED}">${this.i18n.filter_moderated}</option>
              <option value="${this.BY_UNVIEWED}">${this.i18n.filter_unviewed}</option>
            </select>
          </div>
        </div>

        <div id="topic-list-topics">

          ${!this.data?.topics?.length ? html`
          <div id="no-topics-yet-message"><div>${this.i18n.no_topics_yet}</div></div>
          ` : ""}

          ${this.filteredPinnedTopics.length > 0 ? html`
            <div class="topic-list-pinned-header">
              <div>${this.i18n.pinned}</div>
            </div>
            ${this.filteredPinnedTopics.map(t => html`
            <div class="topic-list-topic-wrapper">
              <sakai-topic-summary topic="${JSON.stringify(t)}"></sakai-topic-summary>
            </div>
            `)}
          ` : ""}

          ${this.filteredDraftTopics.length > 0 ? html`
          <a href="javascript:;" @click=${() => this.expandDraft = !this.expandDraft}>
            <div class="topic-list-pinned-header">
              <div class="topic-header-icon">
                <sakai-icon type="${this.expandDraft ? "chevron-down" : "chevron-up"}" size="small"></sakai-icon>
              </div>
              <div>${this.i18n.draft}</div>
            </div>
          </a>
            ${this.expandDraft ? html`
              ${this.filteredDraftTopics.map(t => html`
              <div class="topic-list-topic-wrapper">
                <sakai-topic-summary topic="${JSON.stringify(t)}"></sakai-topic-summary>
              </div>
              `)}
            ` : ""}
          ` : ""}


          ${this.filteredUnpinnedTopics.length > 0 ? html`
          <a href="javascript:;" @click=${() => this.expandTheRest = !this.expandTheRest}>
            <div class="topic-list-pinned-header">
              <div class="topic-header-icon">
                <sakai-icon type="${this.expandTheRest ? "chevron-down" : "chevron-up"}" size="small"></sakai-icon>
              </div>
              <div>${this.i18n.all_questions_and_discussions}</div>
            </div>
          </a>
            ${this.expandTheRest ? html`
              ${this.filteredUnpinnedTopics.map(t => html`
                <div class="topic-list-topic-wrapper">
                  <sakai-topic-summary topic="${JSON.stringify(t)}"></sakai-topic-summary>
                </div>
              `)}
            ` : ""}
          ` : ""}
        </div>
      </div>
    `;
  }
}

const tagName = "sakai-topic-list";
!customElements.get(tagName) && customElements.define(tagName, SakaiTopicList);
