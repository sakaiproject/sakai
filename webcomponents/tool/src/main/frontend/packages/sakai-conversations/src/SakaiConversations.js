import { html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiElement } from "@sakai-ui/sakai-element";
import { setupSearch } from "@sakai-ui/sakai-portal-utils";
import "../sakai-topic-list.js";
import "../sakai-conversations-guidelines.js";
import "@sakai-ui/sakai-icon";
import { STATE_PERMISSIONS,
  STATE_DISPLAYING_TOPIC,
  STATE_STATISTICS,
  STATE_NOTHING_SELECTED,
  STATE_ADDING_TOPIC,
  STATE_SETTINGS,
  STATE_MANAGING_TAGS } from "./sakai-conversations-constants.js";

export class SakaiConversations extends SakaiElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    topicId: { attribute: "topic-id", type: String },
    postId: { attribute: "post-id", type: String },
    baseUrl: { attribute: "base-url", type: String },

    _data: { state: true },
    _addingTopic: { state: true },
    _currentTopic: { state: true },
    _topicBeingEdited: { state: true },
    _showingSettings: { state: true },
    _state: { state: true },
    _loadingData: { state: true },
    _searchEnabled: { state: true },
  };

  constructor() {

    super();

    this._state = STATE_NOTHING_SELECTED;

    /*
    window.onpopstate = (e) => {

      //this._state = e.state.state;

      if (e.state.topicId) {
        this._selectTopic(e.state.topicId);
      }
    };
    */

    this.loadTranslations("conversations");
  }

  _tagUpdatedOrDeleted() { this._fetchConversationsData(); }

  _tagsCreated() {

    this._fetchConversationsData();
    this._resetState();
  }

  _fetchConversationsData() {

    this._loadingData = true;
    const url = `/api/sites/${this.siteId}/conversations`;
    this._dataPromise = fetch(url)
      .then(r => {
        if (r.ok) {
          return r.json();
        }
        throw new Error(`Network error while loading data from ${url}`);
      })
      .then(data => {
        this._data = data;
        this._searchEnabled = data.searchEnabled;
        if (this.topicId) {
          this._selectTopic(this.topicId);
        }
        this.wipTopicKey = `${this._data.userId}-wipTopic`;
        const wipTopicJson = sessionStorage.getItem(this.wipTopicKey);
        wipTopicJson && (this.wipTopic = JSON.parse(wipTopicJson));
        //this._resetState();
      })
      .catch(error => console.error(error))
      .finally(() => this._loadingData = false);
  }

  set siteId(value) {
    this._siteId = value;
    this._fetchConversationsData();
  }

  get siteId() { return this._siteId; }

  set topicId(value) {

    this._topicId = value;
    if (this.siteId && this._dataPromise) {
      this._dataPromise.then(() => this._selectTopic(value));
    }
  }

  get topicId() { return this._topicId; }

  set _state(value) {

    this.__state = value;

    switch (value) {
      case STATE_ADDING_TOPIC:
        this._addingTopic = true;
        this._showingSettings = false;
        /*
        let url = `${this.baseUrl}/addTopic`;
        history.pushState({ state: STATE_ADDING_TOPIC }, "", url);
        */
        break;
      case STATE_SETTINGS:
        this._addingTopic = false;
        this._showingSettings = true;
        /*
        url = `${this.baseUrl}/settings`;
        history.pushState({ state: STATE_SETTINGS }, "", url);
        */
        break;
      case STATE_MANAGING_TAGS:
        this._showingSettings = true;
        break;
      case STATE_DISPLAYING_TOPIC:
        this._showingSettings = false;
        break;
      default:
        this._addingTopic = false;
        /*
        url = `${this.baseURL}`;
        history.pushState({ state: STATE_NOTHING_SELECTED }, "", url);
        */
    }

    this.requestUpdate();
  }

  get _state() { return this.__state; }

  async _addTopic(e) {

    await import("../sakai-add-topic.js");

    e.preventDefault();
    if (this.wipTopic && !this.wipTopic.id) {
      this._topicBeingEdited = this.wipTopic;
    } else {
      this._topicBeingEdited = this._data.blankTopic;
    }
    this._state = STATE_ADDING_TOPIC;
  }

  _topicDeleted(e) {

    const index = this._data.topics.findIndex(t => t.id === e.detail.topic.id);
    this._data.topics.splice(index, 1);
    this._currentTopic = null;
    this._state = STATE_NOTHING_SELECTED;
  }

  _saveWipTopic(e) {

    this.wipTopic = e.detail.topic;
    if (!this.wipTopic.id) {
      this._topicBeingEdited = this.wipTopic;
    }
    sessionStorage.setItem(this.wipTopicKey, JSON.stringify(e.detail.topic));
  }

  async _topicSaved(e) {

    await import("../sakai-topic.js");

    this.wipTopic = null;
    this._topicBeingEdited = null;
    sessionStorage.removeItem(this.wipTopicKey);

    const currentIndex
      = this._data.topics.findIndex(t => t.id === e.detail.topic.id);

    e.detail.topic.selected = true;

    if (currentIndex !== -1) {
      this._data.topics.splice(currentIndex, 1, e.detail.topic);
      this._data.topics[currentIndex].selected = true;
    } else {
      this._data.topics.forEach(t => t.selected = false);
      this._data.topics.unshift(e.detail.topic);
    }

    this._currentTopic = e.detail.topic;
    this._currentTopic.beingEdited = false;
    this._state = STATE_DISPLAYING_TOPIC;
  }

  async _editTopic(e) {

    await import("../sakai-add-topic.js");

    if (e?.detail?.topic) {

      if (this.wipTopic && this.wipTopic.id === e.detail.topic.id) {
        this.wipTopic.beingEdited = true;
        this._topicBeingEdited = this.wipTopic;
      } else {
        this._topicBeingEdited = e.detail.topic;
      }
    } else {
      this._topicBeingEdited = this._data.blankTopic;
    }
    this._state = STATE_ADDING_TOPIC;
  }

  _postsViewed(e) {

    const topic = this._data.topics.find(t => t.id === e.detail.topicId);
    topic.numberOfUnreadPosts -= e.detail.postIds.length;
    if (topic.numberOfUnreadPosts === 0) topic.viewed = true;
    this.requestUpdate();
  }

  _topicUpdated(e) {

    const topic = e.detail.topic;

    const index = this._data.topics.findIndex(t => t.id === topic.id);
    this._data.topics[index] = topic;

    if (!e.detail.dontUpdateCurrent) {
      this._currentTopic = this._data.topics[index];
    }
  }

  _topicSelected(e) {

    const topicId = e.detail.topic.id;

    this.postId = undefined;

    this._selectTopic(topicId);

    /*
    const url = `${this.baseUrl}/topics/${topicId}`;
    history.pushState({}, "", url);
    */

    // Reset the current url to the base url, for now. Later we will want to actually push the
    // topic url so we can use the back button
    history.pushState({}, "", this.baseUrl);
  }

  async _selectTopic(topicId, onlySelectInList) {

    await import("../sakai-topic.js");

    if (topicId) {
      const topic = this._data.topics.find(t => t.id === topicId);
      topic && (topic.selected = true);
      this._data.topics.filter(t => t.id !== topic.id).forEach(t => t.selected = false);
      if (!onlySelectInList) {

        this._currentTopic = topic;
        this._state = STATE_DISPLAYING_TOPIC;
      }
    } else if (this._currentTopic) {
      this._state = STATE_DISPLAYING_TOPIC;
    }
  }

  _cancelAddTopic() {

    this.wipTopic = null;
    this._topicBeingEdited = null;
    sessionStorage.removeItem(this.wipTopicKey);

    if (this._currentTopic) {
      this._state = STATE_DISPLAYING_TOPIC;
    } else {
      this._state = STATE_NOTHING_SELECTED;
    }
  }

  _permissionsComplete() {

    if (this._currentTopic) {
      this._state = STATE_DISPLAYING_TOPIC;
    } else {
      this._state = STATE_NOTHING_SELECTED;
    }

    location.reload();
  }

  _resetState() {

    this._showingSettings = false;

    if (this.wasAddingTopic) {
      this.wasAddingTopic = false;
      this._state = STATE_ADDING_TOPIC;
    } else if (this._currentTopic) {
      this._state = STATE_DISPLAYING_TOPIC;
      this._selectTopic();
    } else {
      this._state = STATE_NOTHING_SELECTED;
    }
  }

  _settingUpdated(e) {

    const setting = e.detail.setting;

    if (setting === "siteLocked") {
      // This is a far ranging setting. Refresh the tool entirely. That way, all the urls and
      // toggles can be recomputed on the server side.
      window.location.reload();
    }

    this._data.settings[e.detail.setting] = e.detail.on;

    this.requestUpdate();
  }

  _guidelinesSaved(e) {

    this._data.settings.guidelines = e.detail.guidelines;
    this.requestUpdate();
  }

  _agreeToGuidelines() {

    const url = `/api/sites/${this.siteId}/conversations/agree`;
    fetch(url)
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while settings agreed status");
      } else {
        this._data.showGuidelines = false;
      }
    })
    .catch(error => console.error(error));

    this._data.showGuidelines = false;
    this.requestUpdate();
  }

  async _editTags() {

    await import("../sakai-conversations-tag-manager.js");

    this.wasAddingTopic = true;
    this._state = STATE_MANAGING_TAGS;
  }

  async _setStateSettings() {

    await import("../sakai-conversations-settings.js");

    this._state = STATE_SETTINGS;
    this._showingSettings = true;
  }

  async _setStatePermissions() {

    await import("@sakai-ui/sakai-permissions");
    this._state = STATE_PERMISSIONS;
  }

  async _setStateManagingTags() {

    await import("../sakai-conversations-tag-manager.js");
    this._state = STATE_MANAGING_TAGS;
  }

  async _setStateStatistics() {

    await import("../conversations-statistics.js");
    this._state = STATE_STATISTICS;
  }

  _setStateNothingSelected() {
    this._state = STATE_NOTHING_SELECTED;
  }

  _handleSearch() {
    setupSearch({ site: this.siteId, tool: "sakai.conversations" });
  }

  _renderSettingsMenu() {

    return html`
      <li class=${ifDefined(this._state === STATE_SETTINGS ? "setting-active" : undefined)}>
        <button class="btn btn-secondary dropdown-item" @click=${this._setStateSettings}>${this._i18n.general_settings}</button>
      </li>
      <li class=${ifDefined(this._state === STATE_PERMISSIONS ? "setting-active" : undefined)}>
        <button class="btn btn-secondary dropdown-item" @click=${this._setStatePermissions}>${this._i18n.permissions}</button>
      </li>
      ${this._data.canEditTags ? html`
      <li class=${ifDefined(this._state === STATE_MANAGING_TAGS ? "setting-active" : undefined)}>
        <button class="btn btn-secondary dropdown-item" @click=${this._setStateManagingTags}>${this._i18n.manage_tags}</button>
      </li>
      ` : nothing }
    `;
  }

  _renderTopbar() {

    return html`
      <div class="conv-topbar d-flex align-items-center">

        <div id="conv-back-button-block">
          <div>
            <button type="button"
                @click=${this._setStateNothingSelected}
                class="btn btn-secondary">
              Topics
            </button>
          </div>
        </div>

        <div class="conv-settings-and-create d-flex align-items-center">

          ${this._searchEnabled ? html`
          <div>
            <button type="button"
                @click=${this._handleSearch}
                class="sakai-conversations__search_button btn btn-secondary"
                data-bs-toggle="offcanvas"
                data-bs-target="#sakai-search-panel"
                aria-controls="sakai-search-panel">
              ${this._i18n.search}
            </button>
          </div>
          ` : nothing}

          ${this._data.canViewStatistics ? html`
            <div id="conv-stats-button" class="ms-1">
              <button type="button" class="btn btn-secondary" @click=${this._setStateStatistics}>
                ${this._i18n.statistics}
              </button>
            </div>
          ` : nothing }

          ${this._data.canUpdatePermissions || this._data.isInstructor ? html`
              <div>
                <div class="dropdown conv-settings-link">
                  <button type="button" class="btn btn-secondary" data-bs-toggle="dropdown" aria-expanded="false">
                    ${this._i18n.settings}
                  </button>
                  <ul class="dropdown-menu">
                  ${this._renderSettingsMenu()}
                  </ul>
                </div>
              </div>
          ` : nothing }

          ${this._data.canCreateTopic ? html`
          <button id="conv-add-topic" type="button" class="btn btn-primary ms-3" @click=${this._addTopic}>
            <span>${this._i18n.create_new}</span>
            <i class="si si-add"></i>
          </button>
          ` : nothing }
        </div>
      </div>
    `;
  }

  _renderPermissions() {

    return html`
      <div class="add-topic-wrapper">
        <h1 id="permissions-title">${this._i18n.permissions}</h1>
        <sakai-permissions tool="conversations" @permissions-complete=${this._permissionsComplete} fire-event></sakai-permissions>
      </div>
    `;
  }

  _renderTagManager() {

    return html`
      <sakai-conversations-tag-manager
          .tags="${this._data.tags}"
          site-id="${this.siteId}"
          ?editing-topic=${this.wasAddingTopic}
          @tags-created=${this._tagsCreated}
          @tag-updated=${this._tagUpdatedOrDeleted}
          @tag-deleted=${this._tagUpdatedOrDeleted}
          @continue=${this._resetState}
      >
      </sakai-conversations-tag-manager>
    `;
  }

  _renderCurrentTopic() {

    return html`
      <sakai-topic post-id="${ifDefined(this.postId)}"
            ?can-view-anonymous=${this._data.canViewAnonymous}
            ?is-instructor=${this._data.isInstructor}
            ?can-view-deleted=${this._data.canViewDeleted}
            ?reactions-allowed=${this._data.settings.allowReactions}
            topic="${JSON.stringify(this._currentTopic)}"
            @edit-topic=${this._editTopic}
            @posts-viewed=${this._postsViewed}
            @topic-deleted=${this._topicDeleted}
            @topic-updated=${this._topicUpdated}
            @topic-unread-updated=${this.topicUnreadUpdated}>
      </sakai-topic>
    `;
  }

  _renderStatistics() {

    return html`
      <conversations-statistics
          stats-url="${this._data.links.find(l => l.rel === "stats").href}"
      >
      </conversations-statistics>
    `;
  }

  _renderAddTopic() {

    return html`
      <sakai-add-topic
        user-id="${this._data.userId}"
        site-id="${this.siteId}"
        .tags="${this._data.tags}"
        .groups="${this._data.groups}"
        @topic-saved=${this._topicSaved}
        @save-wip-topic=${this._saveWipTopic}
        @topic-add-cancelled=${this._cancelAddTopic}
        @edit-tags=${this._editTags}
        ?can-create-discussion=${this._data.canCreateDiscussion}
        ?can-create-question=${this._data.canCreateQuestion}
        ?can-pin=${this._data.canPin}
        ?can-edit-tags=${this._data.canEditTags}
        ?can-anon=${this._data.settings.allowAnonPosting}
        ?can-grade=${this._data.canGrade}
        ?disable-discussions=${this._data.disableDiscussions}
        topic=${ifDefined(this._topicBeingEdited ? JSON.stringify(this._topicBeingEdited) : undefined)}>
      </sakai-add-topic>
    `;
  }

  _renderGeneralSettings() {

    return html`
      <sakai-conversations-settings
          settings="${JSON.stringify(this._data.settings)}"
          @setting-updated=${this._settingUpdated}
          @guidelines-saved=${this._guidelinesSaved}
          site-id="${this.siteId}">
      </sakai-conversations-settings>
    `;
  }

  _renderTopicList() {

    return html`
      <div id="conv-topic-list-wrapper">
        <sakai-topic-list
            id="conv-topic-list"
            site-id="${this._data.siteId}"
            .data="${this._data}"
            @edit-topic=${this._editTopic}
            @topic-deleted=${this._topicDeleted}
            @topic-updated=${this._topicUpdated}
            @topic-selected=${this._topicSelected}>
        </sakai-topic-list>
      </div>
    `;
  }

  shouldUpdate() { return this._i18n; }

  render() {

    if (this._loadingData) {
      return html`
        <div class="sak-banner-info">
          <div class="mb-3 fs-5 fw-bold">${this._i18n.loading_1}</div>
          <div>${this._i18n.loading_2}</div>
        </div>
      `;
    }

    if (this._data.showGuidelines) {

      return html`
        <sakai-conversations-guidelines
            guidelines="${this._data.settings.guidelines || this._i18n.community_guidelines_sample}">
        </sakai-conversations-guidelines>
        <div class="act">
          <input type="button" class="active" @click=${this._agreeToGuidelines} value="${this._i18n.agree}">
        </div>
      `;
    }

    return html`
      <div id="overlay"></div>

      <div id="conv-topbar-and-content">
        ${this._renderTopbar()}
        ${this._state === STATE_NOTHING_SELECTED ? this._renderTopicList() : nothing }
        ${this._state === STATE_ADDING_TOPIC ? this._renderAddTopic() : nothing }
        ${this._state === STATE_DISPLAYING_TOPIC ? this._renderCurrentTopic() : nothing }
        ${this._state === STATE_SETTINGS ? this._renderGeneralSettings() : nothing }
        ${this._state === STATE_PERMISSIONS ? this._renderPermissions() : nothing }
        ${this._state === STATE_MANAGING_TAGS ? this._renderTagManager() : nothing }
        ${this._state === STATE_STATISTICS ? this._renderStatistics() : nothing }
      </div>
    `;
  }
}
