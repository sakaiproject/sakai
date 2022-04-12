import { html, ifDefined } from "../assets/@lion/core/index.js";
import { SakaiElement } from "../sakai-element.js";
import "./sakai-topic-list.js";
import "./sakai-add-topic.js";
import "./sakai-conversations-tag-manager.js";
import "./sakai-conversations-guidelines.js";
import "./sakai-conversations-settings.js";
import "./sakai-topic.js";
import "./conversations-statistics.js";
import "../sakai-icon.js";
import "../sakai-permissions.js";
import "./options-menu.js";
import { STATE_PERMISSIONS,
  STATE_DISPLAYING_TOPIC,
  STATE_STATISTICS,
  STATE_NOTHING_SELECTED,
  STATE_ADDING_TOPIC,
  STATE_SETTINGS,
  QUESTION,
  SITE,
  STATE_MANAGING_TAGS } from "./sakai-conversations-constants.js";

export class SakaiConversations extends SakaiElement {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      topicId: { attribute: "topic-id", type: String },
      baseUrl: { attribute: "base-url", type: String },
      data: { type: Object },
      addingTopic: Boolean,
      currentTopic: { type: Object },
      topicBeingEdited: { type: Object },
      showingSettings: Boolean,
      state: { type: String },
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
        this.selectTopic(e.state.topicId);
      }
    };
    */

    this.loadTranslations("conversations").then(r => this.i18n = r);
  }

  set siteId(value) {

    this._siteId = value;

    this.blankTopic = {
      id: "",
      title: "",
      message: "",
      type: QUESTION,
      visibility: SITE,
      pinned: false,
      tags: [],
      groups: [],
      url: `/api/sites/${this.siteId}/topics`,
      aboutReference: `/site/${value}`,
    };

    const url = `/api/sites/${value}/conversations`;
    this.dataPromise = fetch(url)
      .then(r => r.json())
      .then(data => {

        this.data = data;
        if (this.topicId) {
          this.selectTopic(value);
        }
        this.wipTopicKey = `${this.data.userId}-wipTopic`;
        const wipTopic = sessionStorage.getItem(this.wipTopicKey);
        wipTopic && (this.wipTopic = JSON.parse(wipTopic));
      });
  }

  get siteId() { return this._siteId; }

  set topicId(value) {

    this._topicId = value;
    if (this.siteId && this.dataPromise) {
      this.dataPromise.then(() => this.selectTopic(value));
    }
  }

  get topicId() { return this._topicId; }

  set state(value) {

    this._state = value;

    switch (value) {
      case STATE_ADDING_TOPIC:
        this.addingTopic = true;
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

  addTopic(e) {

    e.preventDefault();
    //this.addingTopic = true;
    if (this.wipTopic && !this.wipTopic.id) {
      this.topicBeingEdited = this.wipTopic;
    } else {
      this.topicBeingEdited = this.blankTopic;
    }
    this.state = STATE_ADDING_TOPIC;
  }

  topicDeleted(e) {

    const index = this.data.topics.findIndex(t => t.id === e.detail.topic.id);
    this.data.topics.splice(index, 1);
    this.currentTopic = null;
    this.state = STATE_NOTHING_SELECTED;
  }

  saveWipTopic(e) {

    this.wipTopic = e.detail.topic;
    if (!this.wipTopic.id) {
      this.topicBeingEdited = this.wipTopic;
    }
    sessionStorage.setItem(this.wipTopicKey, JSON.stringify(e.detail.topic));
  }

  topicSaved(e) {

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
      this.data.topics.push(e.detail.topic);
    }

    this.currentTopic = e.detail.topic;
    this.state = STATE_DISPLAYING_TOPIC;
  }

  editTopic(e) {

    if (e?.detail?.topic) {
      this.topicBeingEdited = e.detail.topic;
    } else {
      this.topicBeingEdited = this.blankTopic;
    }
    this.state = STATE_ADDING_TOPIC;
  }

  topicUpdated(e) {
    this.updateTopic(e.detail.topic);
  }

  updateTopic(topic) {

    const index = this.data.topics.findIndex(t => t.id === topic.id);
    this.data.topics[index] = topic;
    this.currentTopic = this.data.topics[index];
    this.requestUpdate();
  }

  topicSelected(e) {

    const topicId = e.detail.topic.id;

    if (this.wipTopic?.id === topicId) {
      this.topicBeingEdited = this.wipTopic;
      this.wipTopic.beingEdited = true;
      this.selectTopic(topicId, true);
      this.currentTopic = e.detail.topic;
      //this.state = STATE_ADDING_TOPIC;
    } else {
      this.selectTopic(topicId);
      //const state = { state: STATE_DISPLAYING_TOPIC, topicId: topicId };
    }
    /*
    const url = `${this.baseUrl}/topics/${topicId}`;
    history.pushState(state, "", url);
    */
  }

  selectTopic(topicId, onlySelectInList) {

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

  cancelAddTopic() {

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

      const index1 = topic.tags.find(t => t.id == e.detail.tag.id);
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
  }

  resetState() {

    this.showingSettings = false;

    if (this.wasAddingTopic) {
      this.wasAddingTopic = false;
      this.state = STATE_ADDING_TOPIC;
    } else if (this.currentTopic) {
      this.state = STATE_DISPLAYING_TOPIC;
      this.selectTopic();
    } else {
      this.state = STATE_NOTHING_SELECTED;
    }
  }

  settingUpdated(e) {

    const setting = e.detail.setting;

    if (setting === "siteLocked") {
      // This is a far ranging setting. Refresh the tool entirely. That way, all the urls and
      // toggles can be recomputed on the server side.
      window.location.reload();
    }

    this.data.settings[e.detail.setting] = e.detail.on;

    this.requestUpdate();
  }

  guidelinesSaved(e) {

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

  editTags() {

    this.wasAddingTopic = true;
    this.state = STATE_MANAGING_TAGS;
  }

  shouldUpdate() {
    return this.i18n && this.data;
  }

  renderNoTopicsBlock() {

    return html`
      <div id="conv-no-topics-wrapper">
        <div id="conv-no-topics-message">
          <span>${this.i18n.no_topics_yet}</span><span>${this.data.canCreateTopic ? this.i18n.why_not_create_one : ""}</span>
        </div>
        ${this.data.canCreateTopic ? html`
        <div id="conv-add-topic">
          <a href="javascript:;" @click=${() => this.state = STATE_ADDING_TOPIC}>
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
        <a href="javascript:;" @click=${() => this.state = STATE_SETTINGS}>${this.i18n.general_settings}</a>
      </div>
      <div class=${ifDefined(this.state === STATE_PERMISSIONS ? "setting-active" : undefined)}>
        <a href="javascript:;" @click=${() => this.state = STATE_PERMISSIONS}>${this.i18n.permissions}</a>
      </div>
      ${this.data.canEditTags ? html`
      <div class=${ifDefined(this.state === STATE_MANAGING_TAGS ? "setting-active" : undefined)}>
        <a href="javascript:;" @click=${() => this.state = STATE_MANAGING_TAGS}>${this.i18n.manage_tags}</a>
      </div>
      ` : ""}
      ${this.data.canViewSiteStatistics ? html`
      <div class=${ifDefined(this.state === STATE_STATISTICS ? "setting-active" : undefined)}>
        <a href="javascript:;" @click=${() => this.state = STATE_STATISTICS}>${this.i18n.statistics}</a>
      </div>
      ` : ""}
    `;
  }

  renderTopbar(renderBackButton, mobile) {

    return html`
      <div class="conv-topbar">

        ${renderBackButton ? html`
          <div id="conv-back-button-block">
            <div>
              <a href="javascript:;" @click=${() => this.state = STATE_NOTHING_SELECTED}>
                <div><sakai-icon type="left"></sakai-icon></div>
              </a>
            </div>
          </div>
        ` : ""}
              
        <div class="conv-search">
          <input class="conv-search-field" aria-label="${this.i18n.search_field_tooltip}" value="${this.i18n.search_placeholder} (${this.i18n.disabled})" disabled></input>
        </div>

        <div class="conv-settings-and-create">
          ${this.data.canUpdatePermissions ? html`
          ${mobile ? html`

            <options-menu icon="menu" placement="bottom-left">
              <div slot="trigger">
                <a href="javascript:;">
                  <sakai-icon type="cog" size="small"></sakai-icon>
                </a>
              </div>
              <div slot="content" id="settings-menu" class="options-menu" role="dialog">
                ${this.renderSettingsMenu()}
              </div>
            </options-menu>
          ` : html`
          <div class="conv-settings-link">
            <a href="javascript:;" @click=${() => { this.showingSettings = true; this.state = STATE_SETTINGS; }}>
              <sakai-icon type="cog" size="small"></sakai-icon>
              <span id="conv-settings-label">${this.i18n.settings}</span>
            </a>
          </div>
          `}
          ` : ""}

          ${this.data.canCreateTopic ? html`
          <a href="javascript:;" @click=${this.addTopic}>
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
          tags="${JSON.stringify(this.data.tags)}"
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
      <sakai-topic topic="${JSON.stringify(this.currentTopic)}"
            @edit-topic=${this.editTopic}
            @topic-deleted=${this.topicDeleted}
            @topic-updated=${this.topicUpdated}
            @topic-unread-updated=${this.topicUnreadUpdated}
            ?is-instructor=${this.data.isInstructor}
            ?can-view-anonymous=${this.data.canViewAnonymous}>
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
        tags="${JSON.stringify(this.data.tags)}"
        groups="${JSON.stringify(this.data.groups)}"
        @topic-saved=${this.topicSaved}
        @save-wip-topic=${this.saveWipTopic}
        @topic-add-cancelled=${this.cancelAddTopic}
        @topic-dirty=${this.topicDirty}
        @edit-tags=${this.editTags}
        ?can-pin=${this.data.canPin}
        ?can-edit-tags=${this.data.canEditTags}
        ?can-anon=${this.data.settings.allowAnonPosting}
        topic=${ifDefined(this.topicBeingEdited ? JSON.stringify(this.topicBeingEdited) : undefined)}
      >
      </sakai-add-topic>
    `;
  }

  renderGeneralSettings() {

    return html`
      <sakai-conversations-settings
          settings="${JSON.stringify(this.data.settings)}"
          @setting-updated=${this.settingUpdated}
          @guidelines-saved=${this.guidelinesSaved}
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
              data="${JSON.stringify(this.data)}"
              @topic-updated=${this.topicUpdated}
              @topic-selected=${this.topicSelected}>
          </sakai-topic-list>
        </div>
    `;
  }

  render() {

    return html`

      ${this.data.showGuidelines ? html`
        <sakai-conversations-guidelines guidelines="${this.data.settings.guidelines}"></sakai-conversations-guidelines>
        <div class="act">
          <input type="button" class="active" @click=${this._agreeToGuidelines} value="${this.i18n.agree}">
        </div>
      `
      : html`
        <div id="conv-desktop">
          ${this.showingSettings && this.data.canUpdatePermissions ? html`
            <div>
            <div id="conv-back-button-block">
              <div>
                <div><sakai-icon type="left"></sakai-icon></div>
                <div><a href="javascript:;" @click=${this.resetState}>${this.i18n.back}</a></div>
              </div>
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
                  <div>${this.i18n.nothing_selected}</div>
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
