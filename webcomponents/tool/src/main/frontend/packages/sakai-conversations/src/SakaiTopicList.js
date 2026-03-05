import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "../sakai-topic-summary.js";
import { QUESTION, DISCUSSION } from "./sakai-conversations-constants.js";

export class SakaiTopicList extends SakaiElement {

  static properties = {

    aboutRef: { attribute: "about-ref", type: String },
    siteId: { attribute: "site-id", type: String },
    data: { type: Object },

    _filteredPinnedTopics: { state: true },
    _filteredDraftTopics: { state: true },
    _filteredUnpinnedTopics: { state: true },
    _expandDraft: { state: true },
    _expandTheRest: { state: true },
    _tagsInUse: { state: true },
    _selectedTag: { state: true },
    _hasBookmarked: { state: true },
    _hasAnsweredQuestions: { state: true },
    _hasQuestions: { state: true },
    _hasDiscussions: { state: true },
    _hasDiscussionsWithPosts: { state: true },
    _hasUnviewed: { state: true },
  };

  constructor() {

    super();

    this._expandDraft = true;
    this._expandTheRest = true;

    this.ANY = "any";
    this.BY_QUESTION = "by_question";
    this.BY_RESOLVED_QUESTION = "by_resolved_question";
    this.BY_DISCUSSION = "by_discussion";
    this.BY_DISCUSSION_WITH_POSTS = "by_discussion_with_posts";
    this.BY_BOOKMARKED = "by_bookmarked";
    this.BY_MODERATED = "by_moderated";
    this.BY_UNVIEWED = "by_unviewed";

    this._currentFilter = this.ANY;

    this.loadTranslations("conversations");
  }

  set data(value) {

    const oldValue = this._data;

    this._data = value;

    this._initialFilter();

    this._filter();

    this._tagsInUse = [];

    value.topics.forEach(topic => {
      (topic.tags || []).forEach(tag => {
        if (tag?.id && !this._tagsInUse.some(e => e?.id === tag.id)) {
          this._tagsInUse.push(tag);
        }
      });
    });

    this._hasBookmarked = value.topics.some(t => t.bookmarked);
    this._hasQuestions = value.topics.some(t => t.type === QUESTION);
    this._hasAnsweredQuestions = value.topics.some(t => t.type === QUESTION && t.resolved);
    this._hasDiscussions = value.topics.some(t => t.type === DISCUSSION);
    this._hasDiscussionsWithPosts = value.topics.some(t => t.type === DISCUSSION && t.numberOfPosts);
    this._hasUnviewed = value.topics.some(t => !t.viewed);
    this._hasModerated = value.topics.some(t => t.hidden || t.locked);

    this.requestUpdate("data", oldValue);
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

  _initialFilter() {

    this.pinnedTopics = this.data.topics.filter(t => t.pinned);
    this._filteredPinnedTopics = this.pinnedTopics;

    this.draftTopics = this.data.topics.filter(t => t.draft);
    this._filteredDraftTopics = this.draftTopics;

    this.unpinnedTopics = this.data.topics.filter(t => !t.pinned && !t.draft);
    this._filteredUnpinnedTopics = this.unpinnedTopics;
  }

  _filter() {

    this._filteredPinnedTopics = this.pinnedTopics;
    this._filteredUnpinnedTopics = this.unpinnedTopics;

    switch (this._currentFilter) {

      case undefined:
        break;
      case this.BY_QUESTION:
        this._filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === QUESTION);
        this._filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === QUESTION);
        break;
      case this.BY_DISCUSSION:
        this._filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === DISCUSSION);
        this._filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === DISCUSSION);
        break;
      case this.BY_RESOLVED_QUESTION:
        this._filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === QUESTION && t.resolved);
        this._filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === QUESTION && t.resolved);
        break;
      case this.BY_DISCUSSION_WITH_POSTS:
        this._filteredPinnedTopics = this.pinnedTopics.filter(t => t.type === DISCUSSION && t.numberOfPosts);
        this._filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.type === DISCUSSION && t.numberOfPosts);
        break;
      case this.BY_BOOKMARKED:
        this._filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.bookmarked);
        break;
      case this.BY_MODERATED:
        this._filteredUnpinnedTopics = this.unpinnedTopics.filter(t => t.hidden || t.locked);
        break;
      case this.BY_UNVIEWED:
        this._filteredUnpinnedTopics = this.unpinnedTopics.filter(t => !t.viewed);
        break;
      default:
        this._filteredPinnedTopics = this.pinnedTopics;
        this._filteredUnpinnedTopics = this.unpinnedTopics;
    }

    if (this._selectedTag) {
      this._filteredUnpinnedTopics = this._filteredUnpinnedTopics.filter(t => t.tags.find(tag => tag.id == this._selectedTag));
    }
  }

  _filterSelected(e) {

    this._currentFilter = e.target.value;
    this._filter();
  }

  _tagSelected(e) {

    if (e.target.value === this.ANY) {
      this._selectedTag = undefined;
    } else {
      this._selectedTag = e.target.value;
    }
    this._filter();
  }

  _toggleExpandDraft() { this._expandDraft = !this._expandDraft; }

  _toggleExpandTheRest() { this._expandTheRest = !this._expandTheRest; }

  shouldUpdate() {
    return this._i18n && this.data;
  }

  render() {

    return html`
      <div class="topic-list">

        <div id="topic-list-filters">
          <div>
            <select @change=${this._tagSelected} aria-label="${this._i18n.filter_by_tag_tooltip}" ?disabled=${!this._tagsInUse?.length}>
              ${!this._tagsInUse?.length ? html`
                <option value="none">No tags in use</option>
                ` : html`
                <option value="${this.ANY}">${this._i18n.tag_any}</option>
                ${this._tagsInUse.map(tag => html`
                  <option value="${tag.id}">${this._i18n.tag} ${tag.label}</option>
                `)}
              `}
            </select>
          </div>
          <div>
            <select @change=${this._filterSelected} aria-label="${this._i18n.filter_by_various_tooltip}">
              <option value="${this.ANY}">${this._i18n.filter_any}</option>
              ${this._hasQuestions ? html`
              <option value="${this.BY_QUESTION}">${this._i18n.filter_questions}</option>
              ` : nothing }
              ${this._hasDiscussions ? html`
              <option value="${this.BY_DISCUSSION}">${this._i18n.filter_discussions}</option>
              ` : nothing }
              ${this._hasAnsweredQuestions ? html`
              <option value="${this.BY_RESOLVED_QUESTION}">${this._i18n.filter_answered}</option>
              ` : nothing }
              ${this._hasDiscussionsWithPosts ? html`
              <option value="${this.BY_DISCUSSION_WITH_POSTS}">${this._i18n.filter_discussions_with_posts}</option>
              ` : nothing }
              ${this._hasBookmarked ? html`
              <option value="${this.BY_BOOKMARKED}">${this._i18n.filter_bookmarked}</option>
              ` : nothing }
              ${this._hasModerated ? html`
              <option value="${this.BY_MODERATED}">${this._i18n.filter_moderated}</option>
              ` : nothing }
              ${this._hasUnviewed ? html`
              <option value="${this.BY_UNVIEWED}">${this._i18n.filter_unviewed}</option>
              ` : nothing }
            </select>
          </div>
        </div>

        <div id="topic-list-topics">

          ${!this.data?.topics?.length ? html`
          <div id="no-topics-yet-message"><div>${this._i18n.no_topics_yet}</div></div>
          ` : nothing }

          ${this._filteredPinnedTopics.length > 0 ? html`
            <div class="topic-list-pinned-header">
              <div>${this._i18n.pinned}</div>
            </div>
            ${this._filteredPinnedTopics.map(t => html`
            <div class="topic-list-topic-wrapper">
              <sakai-topic-summary .topic=${t}></sakai-topic-summary>
            </div>
            `)}
          ` : nothing }

          ${this._filteredDraftTopics.length > 0 ? html`
          <a href="javascript:;" @click="${this._toggleExpandDraft}">
            <div class="topic-list-pinned-header">
              <div class="topic-header-icon">
                <sakai-icon type="${this._expandDraft ? "chevron-down" : "chevron-up"}" size="small"></sakai-icon>
              </div>
              <div>${this._i18n.draft}</div>
            </div>
          </a>
            ${this._expandDraft ? html`
              ${this._filteredDraftTopics.map(t => html`
              <div class="topic-list-topic-wrapper">
                <sakai-topic-summary topic="${JSON.stringify(t)}"></sakai-topic-summary>
              </div>
              `)}
            ` : nothing }
          ` : nothing }


          ${this._filteredUnpinnedTopics.length > 0 ? html`
          <a href="javascript:;" @click="${this._toggleExpandTheRest}">
            <div class="topic-list-pinned-header">
              <div class="topic-header-icon">
                <sakai-icon type="${this._expandTheRest ? "chevron-down" : "chevron-up"}" size="small"></sakai-icon>
              </div>
              <div>${this._i18n.all_topics}</div>
            </div>
          </a>
            ${this._expandTheRest ? html`
              ${this._filteredUnpinnedTopics.map(t => html`
                <div class="topic-list-topic-wrapper">
                  <sakai-topic-summary topic="${JSON.stringify(t)}"></sakai-topic-summary>
                </div>
              `)}
            ` : nothing }
          ` : nothing }
        </div>
      </div>
    `;
  }
}
