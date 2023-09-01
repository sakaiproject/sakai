import { html } from "../assets/lit-element/lit-element.js";
import { ifDefined } from "../assets/lit-html/directives/if-defined.js";
import { SakaiElement } from "../sakai-element.js";
import { setupSearch } from "../sakai-portal-utils.js";
import "./sakai-topic-list.js";
import "./sakai-add-topic.js";
import "./sakai-conversations-tag-manager.js";
import "./sakai-conversations-guidelines.js";
import "./sakai-conversations-settings.js";
import "./sakai-topic.js";
import "./conversations-statistics.js";
import "../sakai-icon.js";
import "../sakai-permissions.js";
import "../sakai-search.js";
import "./options-menu.js";
import { STATE_PERMISSIONS,
  STATE_DISPLAYING_TOPIC,
  STATE_STATISTICS,
  STATE_NOTHING_SELECTED,
  STATE_ADDING_TOPIC,
  STATE_SETTINGS,
  STATE_MANAGING_TAGS } from "./sakai-conversations-constants.js";

export class SakaiConversations extends SakaiElement {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      topicId: { attribute: "topic-id", type: String },
      postId: { attribute: "post-id", type: String },
      baseUrl: { attribute: "base-url", type: String },
      data: { type: Object },
      addingTopic: Boolean,
      currentTopic: { type: Object },
      topicBeingEdited: { type: Object },
      showingSettings: Boolean,
      state: { type: String },
      loadingData: { attribute: false, type: Boolean },
    };
  }

  constructor() {

    super();

    this.state = STATE_NOTHING_SELECTED;
    this.addEventListener("click", () => this.querySelectorAll("options-menu").forEach(o => o.showing = false));

    /*
    window.onpopstate = (e) => {

      //this.state = e.state.state;

      if (e.state.topicId) {
        this._selectTopic(e.state.topicId);
      }
    };
    */

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set siteId(value) {

    this._siteId = value;

    this.loadingData = true;

    const url = `/api/sites/${value}/conversations`;
    this.dataPromise = fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Network error while loading data from ${url}`);
      })
      .then(data => {

        this.data = data;

        if (this.topicId) {
          this._selectTopic(this.topicId);
        }

        // If this topic has some sessions stored changes, load them up into wipTopic.
        this.wipTopicKey = `${this.data.userId}-wipTopic`;
        const wipTopicJson = sessionStorage.getItem(this.wipTopicKey);
        wipTopicJson && (this.wipTopic = JSON.parse(wipTopicJson));
      })
      .catch (error => console.error(error))
      .finally (() => this.loadingData = false)
  }

  get siteId() { return this._siteId; }

  set topicId(value) {

    this._topicId = value;
    if (this.siteId && this.dataPromise) {
      this.dataPromise.then(() => this._selectTopic(value));
    }
  }

  get topicId() { return this._topicId; }

  set state(value) {

    this._state = value;

    switch (value) {
      case STATE_ADDING_TOPIC:
        this.addingTopic = true;
        this.showingSettings = false;
        /*
        let url = `${this.baseUrl}/addTopic`;
        history.pushState({ state: STATE_ADDING_TOPIC }, "", url);
        */
        break;
      case STATE_SETTINGS:
        this.addingTopic = false;
        this.showingSettings = true;
        /*
        url = `${this.baseUrl}/settings`;
        history.pushState({ state: STATE_SETTINGS }, "", url);
        */
        break;
      case STATE_MANAGING_TAGS:
        this.showingSettings = true;
        break;
      case STATE_DISPLAYING_TOPIC:
        this.showingSettings = false;
        break;
      default:
        this.addingTopic = false;
        /*
        url = `${this.baseURL}`;
        history.pushState({ state: STATE_NOTHING_SELECTED }, "", url);
        */
    }

    this.requestUpdate();
  }

  get state() { return this._state; }

  _addTopic(e) {

    e.preventDefault();
    if (this.wipTopic && !this.wipTopic.id) {
      this.topicBeingEdited = this.wipTopic;
    } else {
      this.topicBeingEdited = this.data.blankTopic;
    }
    this.state = STATE_ADDING_TOPIC;
  }

  _topicDeleted(e) {

    const index = this.data.topics.findIndex(t => t.id === e.detail.topic.id);
    this.data.topics.splice(index, 1);
    this.currentTopic = null;
    this.state = STATE_NOTHING_SELECTED;
  }

  _saveWipTopic(e) {

    this.wipTopic = e.detail.topic;
    if (!this.wipTopic.id) {
      this.topicBeingEdited = this.wipTopic;
    }
    sessionStorage.setItem(this.wipTopicKey, JSON.stringify(e.detail.topic));
  }

  _topicSaved(e) {

    this.wipTopic = null;
    this.topicBeingEdited = null;
    sessionStorage.removeItem(this.wipTopicKey);

    const currentIndex
      = this.data.topics.findIndex(t => t.id === e.detail.topic.id);

    e.detail.topic.selected = true;

    if (currentIndex !== -1) {
      this.data.topics.splice(currentIndex, 1, e.detail.topic);
      this.data.topics[currentIndex].selected = true;
    } else {
      this.data.topics.forEach(t => t.selected = false);
      this.data.topics.unshift(e.detail.topic);
    }

    window.scrollTo(0, 0);

    this.currentTopic = e.detail.topic;
    this.currentTopic.beingEdited = false;
    this.state = STATE_DISPLAYING_TOPIC;
  }

  editTopic(e) {

    if (e?.detail?.topic) {
      if (this.wipTopic && this.wipTopic.id === e.detail.topic.id) {
        this.wipTopic.beingEdited = true;
        this.topicBeingEdited = this.wipTopic;
      } else {
        this.topicBeingEdited = e.detail.topic;
      }
    } else {
      this.topicBeingEdited = this.data.blankTopic;
    }
    this.state = STATE_ADDING_TOPIC;
  }

  _postsViewed(e) {

    const topic = this.data.topics.find(t => t.id === e.detail.topicId);
    topic.numberOfUnreadPosts -= e.detail.postIds.length;
    if (topic.numberOfUnreadPosts === 0) topic.viewed = true;
    this.requestUpdate();
  }

  _topicUpdated(e) {

    const topic = e.detail.topic;

    const index = this.data.topics.findIndex(t => t.id === topic.id);
    this.data.topics[index] = topic;

    if (!e.detail.dontUpdateCurrent) {
      this.currentTopic = this.data.topics[index];
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

  _selectTopic(topicId, onlySelectInList) {

    if (topicId) {
      const topic = this.data.topics.find(t => t.id === topicId);
      topic && (topic.selected = true);
      this.data.topics.filter(t => t.id !== topic.id).forEach(t => t.selected = false);
      if (!onlySelectInList) {
        this.currentTopic = topic;
        this.state = STATE_DISPLAYING_TOPIC;
      }
    } else if (this.currentTopic) {
      this.state = STATE_DISPLAYING_TOPIC;
    }
  }

  _cancelAddTopic() {

    this.wipTopic = null;
    this.topicBeingEdited = null;
    sessionStorage.removeItem(this.wipTopicKey);

    if (this.currentTopic) {
      this.state = STATE_DISPLAYING_TOPIC;
    } else {
      this.state = STATE_NOTHING_SELECTED;
    }
  }

  tagsCreated(e) {

    this.data.tags = this.data.tags.concat(e.detail.tags);
    this.requestUpdate();
  }

  tagUpdated(e) {

    const index = this.data.tags.findIndex(t => t.id == e.detail.tag.id);
    this.data.tags.splice(index, 1, e.detail.tag);
    this.data.topics.forEach(topic => {

      const index1 = topic.tags.findIndex(t => t.id == e.detail.tag.id);
      topic.tags.splice(index1, 1, e.detail.tag);
    });

    this.requestUpdate();
  }

  tagDeleted(e) {

    const index = this.data.tags.findIndex(t => t.id == e.detail.id);
    this.data.tags.splice(index, 1);
    this.data.topics.forEach(topic => {

      const index1 = topic.tags.findIndex(t => t.id == e.detail.id);
      topic.tags.splice(index1, 1);
    });

    this.requestUpdate();
  }

  permissionsComplete() {

    if (this.currentTopic) {
      this.state = STATE_DISPLAYING_TOPIC;
    } else {
      this.state = STATE_NOTHING_SELECTED;
    }

    location.reload();
  }

  resetState() {

    this.showingSettings = false;

    if (this.wasAddingTopic) {
      this.wasAddingTopic = false;
      this.state = STATE_ADDING_TOPIC;
    } else if (this.currentTopic) {
      this.state = STATE_DISPLAYING_TOPIC;
      this._selectTopic();
    } else {
      this.state = STATE_NOTHING_SELECTED;
    }
  }

  _settingUpdated(e) {

    const setting = e.detail.setting;

    if (setting === "siteLocked") {
      // This is a far ranging setting. Refresh the tool entirely. That way, all the urls and
      // toggles can be recomputed on the server side.
      window.location.reload();
    }

    this.data.settings[e.detail.setting] = e.detail.on;

    this.requestUpdate();
  }

  _guidelinesSaved(e) {

    this.data.settings.guidelines = e.detail.guidelines;
    this.requestUpdate();
  }

  _agreeToGuidelines() {

    const url = `/api/sites/${this.siteId}/conversations/agree`;
    fetch(url, {
      credentials: "include",
    })
    .then(r => {

      if (!r.ok) {
        throw new Error("Network error while settings agreed status");
      } else {
        this.data.showGuidelines = false;
      }
    })
    .catch(error => console.error(error));

    this.data.showGuidelines = false;
    this.requestUpdate();
  }

  _editTags() {

    this.wasAddingTopic = true;
    this.state = STATE_MANAGING_TAGS;
  }

  _dimBackground() { document.getElementById("overlay").style.display = "block"; }

  _undimBackground() { document.getElementById("overlay").style.display = "none"; }

  _setStateAddingTopic() { this.state = STATE_ADDING_TOPIC; }

  _setStateSettings() {

    this.state = STATE_SETTINGS;
    this.showingSettings = true;
  }

  _setStatePermissions() { this.state = STATE_PERMISSIONS; }

  _setStateManagingTags() { this.state = STATE_MANAGING_TAGS; }

  _setStateStatistics() { this.state = STATE_STATISTICS; }

  _setStateNothingSelected() { this.state = STATE_NOTHING_SELECTED; }

  shouldUpdate() {
    return this.i18n;
  }

  _handleSearch() {
    setupSearch({ site: this.siteId, tool: "sakai.conversations" });
  }

  renderNoTopicsBlock() {

    return html`
      <div id="conv-no-topics-wrapper">
        <div id="conv-no-topics-message">
          <span>${this.i18n.no_topics_yet}</span><span>${this.data.canCreateTopic ? this.i18n.why_not_create_one : ""}</span>
        </div>
        ${this.data.canCreateTopic ? html`
        <div id="conv-add-topic">
          <a href="javascript:;" @click="${this._setStateAddingTopic}">
            ${this.i18n.create_new}
          </a>
        </div>
        ` : ""}
      </div>
    `;
  }

  renderSettingsMenu() {

    return html`
      <div class=${ifDefined(this.state === STATE_SETTINGS ? "setting-active" : undefined)}>
        <button class="btn btn-transparent" @click="${this._setStateSettings}">${this.i18n.general_settings}</button>
      </div>
      <div class=${ifDefined(this.state === STATE_PERMISSIONS ? "setting-active" : undefined)}>
        <button class="btn btn-transparent" @click="${this._setStatePermissions}">${this.i18n.permissions}</button>
      </div>
      ${this.data.canEditTags ? html`
      <div class=${ifDefined(this.state === STATE_MANAGING_TAGS ? "setting-active" : undefined)}>
        <button class="btn btn-transparent" @click="${this._setStateManagingTags}">${this.i18n.manage_tags}</button>
      </div>
      ` : ""}
      ${this.data.canViewSiteStatistics ? html`
      <div class=${ifDefined(this.state === STATE_STATISTICS ? "setting-active" : undefined)}>
        <button class="btn btn-transparent" @click="${this._setStateStatistics}">${this.i18n.statistics}</button>
      </div>
      ` : ""}
    `;
  }

  renderTopbar(renderBackButton, mobile) {

    return html`
      <div class="conv-topbar d-flex align-items-center">

        ${renderBackButton ? html`
        <div id="conv-back-button-block">
          <div>
            <a href="javascript:;" @click="${this._setStateNothingSelected}">
              <div><sakai-icon type="left"></sakai-icon></div>
            </a>
          </div>
        </div>
        ` : ""}

        <div class="conv-settings-and-create d-flex align-items-center">
          ${this.data.canUpdatePermissions || this.data.isInstructor ? html`
          <div>
            <button type="button"
                @click=${this._handleSearch}
                class="btn btn-link icon-button"
                data-bs-toggle="offcanvas"
                data-bs-target="#sakai-search-panel"
                aria-controls="sakai-search-panel">
              <i class="si si-sakai-search"></i>
              <span>Search</span>
            </button>
          </div>
          ${mobile ? html`
            <options-menu icon="menu" placement="bottom-left">
              <div slot="trigger">
                <button type="button" class="btn icon-button">
                  <i class="si si-settings"></i>
                  <span>${this.i18n.settings}</span>
                </button>
              </div>
              <div slot="content" id="settings-menu" class="options-menu" role="dialog">
                ${this.renderSettingsMenu()}
              </div>
            </options-menu>
          ` : html`
          <div class="conv-settings-link">
            <button type="button" class="btn icon-button text-nowrap" @click="${this._setStateSettings}">
              <i class="si si-settings"></i>
              <span>${this.i18n.settings}</span>
            </button>
          </div>
          `}
          ` : ""}

          ${this.data.canCreateTopic ? html`
          <a href="javascript:;" @click=${this._addTopic}>
            <div class="conv-add-topic">
                <span class="add-topic-text">
                ${this.i18n.create_new}
                </span>
                <sakai-icon class="add-topic-icon" type="add" size="medium"></sakai-icon>
            </div>
          </a>
          ` : ""}
        </div>
      </div>
    `;
  }

  renderPermissions() {

    return html`
      <div class="add-topic-wrapper">
        <h1 id="permissions-title">${this.i18n.permissions}</h1>
        <sakai-permissions tool="conversations" @permissions-complete=${this.permissionsComplete} fire-event></sakai-permissions>
      </div>
    `;
  }

  renderTagManager() {

    return html`
      <sakai-conversations-tag-manager
          .tags="${this.data.tags}"
          site-id="${this.siteId}"
          @tags-created=${this.tagsCreated}
          @tag-updated=${this.tagUpdated}
          @tag-deleted=${this.tagDeleted}
      >
      </sakai-conversations-tag-manager>
    `;
  }

  renderCurrentTopic() {

    return html`
      <sakai-topic post-id="${ifDefined(this.postId)}"
            ?can-view-anonymous=${this.data.canViewAnonymous}
            ?is-instructor=${this.data.isInstructor}
            ?can-view-deleted=${this.data.canViewDeleted}
            topic="${JSON.stringify(this.currentTopic)}"
            @edit-topic=${this.editTopic}
            @posts-viewed=${this._postsViewed}
            @topic-deleted=${this._topicDeleted}
            @topic-updated=${this._topicUpdated}
            @topic-unread-updated=${this.topicUnreadUpdated}>
      </sakai-topic>
    `;
  }

  renderStatistics() {

    return html`
      <conversations-statistics
          stats-url="${this.data.links.find(l => l.rel === "stats").href}"
      >
      </conversations-statistics>
    `;
  }

  renderAddTopic() {

    return html`
      <sakai-add-topic
        user-id="${this.data.userId}"
        site-id="${this.siteId}"
        .tags="${this.data.tags}"
        .groups="${this.data.groups}"
        @topic-saved=${this._topicSaved}
        @save-wip-topic=${this._saveWipTopic}
        @topic-add-cancelled=${this._cancelAddTopic}
        @edit-tags=${this._editTags}
        ?can-create-discussion=${this.data.canCreateDiscussion}
        ?can-create-question=${this.data.canCreateQuestion}
        ?can-pin=${this.data.canPin}
        ?can-edit-tags=${this.data.canEditTags}
        ?can-anon=${this.data.settings.allowAnonPosting}
        ?disable-discussions=${this.data.disableDiscussions}
        topic=${ifDefined(this.topicBeingEdited ? JSON.stringify(this.topicBeingEdited) : undefined)}
      >
      </sakai-add-topic>
    `;
  }

  renderGeneralSettings() {

    return html`
      <sakai-conversations-settings
          settings="${JSON.stringify(this.data.settings)}"
          @setting-updated=${this._settingUpdated}
          @guidelines-saved=${this._guidelinesSaved}
          site-id="${this.siteId}">
      </sakai-conversations-settings>
    `;
  }

  renderTopicList() {

    return html`
      <div id="conv-topic-list-wrapper">
        <sakai-topic-list
            id="conv-topic-list"
            site-id="${this.data.siteId}"
            .data="${this.data}"
            @topic-selected=${this._topicSelected}>
        </sakai-topic-list>
      </div>
    `;
  }

  render() {

    if (this.loadingData) {
      return html`
        <div class="sak-banner-info">
          <div class="mb-3 fs-5 fw-bold">${this.i18n.loading_1}</div>
          <div>${this.i18n.loading_2}</div>
        </div>
      `;
    }

    return html`

      ${this.data.showGuidelines ? html`
        <sakai-conversations-guidelines guidelines="${this.data.settings.guidelines}"></sakai-conversations-guidelines>
        <div class="act">
          <input type="button" class="active" @click=${this._agreeToGuidelines} value="${this.i18n.agree}">
        </div>
      `
      : html`
        <div id="overlay"></div>
        <div id="conv-desktop">
          ${this.showingSettings && (this.data.canUpdatePermissions || this.data.isInstructor) ? html`
          <div>
            <div id="conv-back-button-block">
              <a href="javascript:;" @click=${this.resetState}>
                <div><sakai-icon type="left-arrow"></sakai-icon></div>
                <div>${this.i18n.back}</div>
              </a>
            </div>
            <div id="conv-settings">
              ${this.renderSettingsMenu()}
            </div>
          </div>
          ` : this.renderTopicList()}

          <div id="conv-topbar-and-content">

            ${this.renderTopbar()}

            <div id="conv-content">
              ${this.state === STATE_PERMISSIONS ? this.renderPermissions() : ""}
              ${this.state === STATE_MANAGING_TAGS ? this.renderTagManager() : ""}
              ${this.state === STATE_STATISTICS ? this.renderStatistics() : ""}
              ${this.state === STATE_SETTINGS ? this.renderGeneralSettings() : ""}
              ${this.state === STATE_ADDING_TOPIC ? this.renderAddTopic() : ""}
              ${this.state === STATE_DISPLAYING_TOPIC ? this.renderCurrentTopic() : ""}
              ${this.state === STATE_NOTHING_SELECTED ? html`
                <div id="conv-nothing-selected">
                  ${this.data.canCreateDiscussion || this.data.canCreateQuestion ? html`
                    <div>${this.i18n.nothing_selected}</div>
                  ` : html`
                    <div>${this.i18n.nothing_selected_no_create}</div>
                  `}
                </div>
              ` : ""}
            </div>

          </div>

        </div>
      `}

      <div id="conv-mobile">
        ${this.renderTopbar(this.state !== STATE_NOTHING_SELECTED, true)}
        ${this.state === STATE_NOTHING_SELECTED ? this.renderTopicList() : ""}
        ${this.state === STATE_DISPLAYING_TOPIC ? this.renderCurrentTopic() : ""}
        ${this.state === STATE_ADDING_TOPIC ? this.renderAddTopic() : ""}
        ${this.state === STATE_SETTINGS ? this.renderGeneralSettings() : ""}
        ${this.state === STATE_PERMISSIONS ? this.renderPermissions() : ""}
        ${this.state === STATE_STATISTICS ? this.renderStatistics() : ""}
        ${this.state === STATE_MANAGING_TAGS ? this.renderTagManager() : ""}
      </div>
    `;
  }
}

const tagName = "sakai-conversations";
!customElements.get(tagName) && customElements.define(tagName, SakaiConversations);
