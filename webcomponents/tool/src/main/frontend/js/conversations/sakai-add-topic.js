import { html } from "../assets/lit-element/lit-element.js";
import { SakaiElement } from "../sakai-element.js";
import "../sakai-editor.js";
import "../sakai-icon.js";
import "../sakai-date-picker.js";
import { AVAILABILITY_DATED,
  AVAILABILITY_NOW,
  QUESTION,
  DISCUSSION,
  INSTRUCTORS,
  SITE, GROUP } from "./sakai-conversations-constants.js";

export class SakaiAddTopic extends SakaiElement {

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      aboutReference: { attribute: "about-reference", type: String },
      groups: { type: Array },
      tags: { attribute: "tags", type: Array },
      canPin: { attribute: "can-pin", type: Boolean },
      canAnonPost: { attribute: "can-anon", type: Boolean },
      disableDiscussions: { attribute: "disable-discussions", type: Boolean },
      canEditTags: { attribute: "can-edit-tags", type: Boolean },
      topic: { type: Object },
      titleError: { attribute: false, type: Boolean },
      errorDisplay: { attribute: false, type: String },
      showShowDatePicker: { attribute: false, type: Boolean },
      showHideDatePicker: { attribute: false, type: Boolean },
      showLockDatePicker: { attribute: false, type: Boolean },
      showDue: { attribute: false, type: Boolean },
      showAcceptUntil: { attribute: false, type: Boolean },
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

    if (this.topic.showDate || this.topic.lockDate || this.topic.hideDate) {
      this.topic.availability = AVAILABILITY_DATED;
    } else {
      this.topic.availability = AVAILABILITY_NOW;
    }
    this.showShowDatePicker = this.topic.showDate !== null;
    this.showLockDatePicker = this.topic.lockDate !== null;
    this.showHideDatePicker = this.topic.hideDate !== null;
    this.showDue = this.topic.dueDate !== null;
    this.showAcceptUntil = this.topic.lockDate !== null;

    const nowMillis = Date.now();
    this.topic.showDateMillis = this.topic.showDate ? this.topic.showDate * 1000 : nowMillis;
    this.topic.lockDateMillis = this.topic.lockDate ? this.topic.lockDate * 1000 : nowMillis;
    this.topic.hideDateMillis = this.topic.hideDate ? this.topic.hideDate * 1000 : nowMillis;
    this.topic.dueDateMillis = this.topic.dueDate ? this.topic.dueDate * 1000 : nowMillis;
    this.topic.acceptUntilDateMillis = this.topic.acceptUntilDate ? this.topic.acceptUntilDate * 1000 : nowMillis;

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
      this.errorDisplay = this.i18n.min_title_characters_info;
      setTimeout(() => this.errorDisplay = undefined, 4000);
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
    .catch (error => console.error(error));
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

    if (e.code && e.code === "Enter") {
      this.topic.type = e.target.dataset.type;
    } else {
      this.topic.type = e.target.dataset.type;
    }
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

  toggleShowDue(e) {

    this.showDue = e.target.checked;
    if (!this.showDue) {
      this.topic.dueDate = undefined;
    } else {
      this.topic.dueDate = Date.now() / 1000;
    }
  }

  toggleShowAcceptUntil(e) {

    this.showAcceptUntil = e.target.checked;
    if (!this.showAcceptUntil) {
      this.topic.acceptUntilDate = undefined;
    } else {
      this.topic.acceptUntilDate = Date.now();
    }
  }

  setVisibility(e) {

    this.topic.visibility = e.target.dataset.visibility;
    if (this.topic.visibility != GROUP) {
      this.topic.groups = [];
    }
    this.saveWip();
    this.requestUpdate();
  }

  setShowDate(e) {
    this.topic.showDate = e.detail.epochSeconds;
  }

  setLockDate(e) {
    this.topic.lockDate = e.detail.epochSeconds;
  }

  setHideDate(e) {
    this.topic.hideDate = e.detail.epochSeconds;
  }

  setDueDate(e) {
    this.topic.dueDate = e.detail.epochSeconds;
  }

  setAcceptUntilDate(e) {
    this.topic.acceptUntilDate = e.detail.epochSeconds;
  }

  setAvailableNow() {

    this.topic.availability = AVAILABILITY_NOW;
    this.topic.showDate = null;
    this.topic.showHideDatePicker = null;
    this.topic.showLockDatePicker = null;
    this.showShowDatePicker = false;
    this.showHideDatePicker = false;
    this.showLockDatePicker = false;
    this.requestUpdate();
  }

  setAvailableDated() {

    this.topic.availability = AVAILABILITY_DATED;
    this.requestUpdate();
  }

  toggleShowDatePicker(e) {

    this.showShowDatePicker = e.target.checked;
    if (!e.target.checked) {
      this.topic.showDate = undefined;
    }
  }

  toggleLockDatePicker(e) {

    this.showLockDatePicker = e.target.checked;
    if (!e.target.checked) {
      this.topic.lockDate = undefined;
    }
  }

  toggleHideDatePicker(e) {

    this.showHideDatePicker = e.target.checked;
    if (!e.target.checked) {
      this.topic.hideDate = undefined;
    }
  }

  _resetTitle() { this.titleError = false; }

  _setSelectedTagId(e) { this.selectedTagId = e.target.value; }

  _setPinned(e) {

    this.topic.pinned = e.target.checked;
    this.saveWip();
  }

  _setAnonymous(e) {

    this.topic.anonymous = e.target.checked;
    this.saveWip();
  }

  _setAllowAnonymousPosts(e) {

    this.topic.allowAnonymousPosts = e.target.checked;
    this.saveWip();
  }

  _setMustPostBeforeViewing(e) {

    this.topic.mustPostBeforeViewing = e.target.checked;
    this.saveWip();
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

        ${this.disableDiscussions ? "" : html`
        <div class="add-topic-block">
          <div id="post-type-label" class="add-topic-label">${this.i18n.topic_type}</div>
          <div id="topic-type-toggle-block">
            <div @click=${this.setType}
                @keydown=${this.setType}
                tabindex="0"
                data-type="${QUESTION}"
                class="topic-type-toggle ${this.topic.type === QUESTION ? "active" : ""}">
              <sakai-icon type="question" size="medium"></sakai-icon>
              <div>${this.i18n.type_question}</div>
            </div>
            <div @click=${this.setType}
                @keydown=${this.setType}
                tabindex="0"
                data-type="${DISCUSSION}"
                class="topic-type-toggle ${this.topic.type === DISCUSSION ? "active" : ""}">
              <sakai-icon type="forums" size="medium"></sakai-icon>
              <div>${this.i18n.type_discussion}</div>
            </div>
          </div>
        </div>
        `}

        <div class="add-topic-block">
          <div id="summary-label" class="add-topic-label">${this.i18n.summary} *</div>
          <input id="summary"
            class="summary-input ${this.titleError ? "error" : ""}"
            @change=${this.updateSummary}
            @focus="${this._resetTitle}"
            .value="${this.topic.title}" />
          <div class="required">
            <span>* ${this.i18n.required}</span>
            <span>(${this.i18n.min_title_characters_info})</span>
          </div>
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
          <select @change="${this._setSelectedTagId}" aria-labelledby="tag-post-label">
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
                  @click=${this.setVisibility}
                  ?checked=${this.topic.visibility === SITE}>${this.i18n.everyone}
              </label>
            </div>
            <div>
              <label>
                <input
                    type="radio"
                    name="post-to"
                    data-visibility="${INSTRUCTORS}"
                    @click=${this.setVisibility}
                    ?checked=${this.topic.visibility === INSTRUCTORS}>${this.i18n.instructors}
              </label>
            </div>
            ${this.groups && this.groups.length > 0 ? html`
              <div>
                <label>
                  <input
                      type="radio"
                      name="post-to"
                      data-visibility="${GROUP}"
                      @click=${this.setVisibility}
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

        ${this.topic.canModerate ? html`
        <div id="conversations-availablility-block" class="add-topic-block">
          <form>
            <div id="availability-label" class="add-topic-label">${this.i18n.availability}</div>
            <div class="availability-wrapper">
              <div>
                <input
                    type="radio"
                    id="add-topic-now"
                    aria-labelledby="availability-now-label"
                    name="availabilitytype"
                    @click=${this.setAvailableNow}
                    ?checked=${this.topic.availability === AVAILABILITY_NOW}>
              </div>
              <div id="availability-now-label">${this.i18n.make_available_now}</div>
              <div>${this.i18n.make_available_now_explanation}</div>
            </div>
            <div class="availability-wrapper">
              <div>
                <input
                    type="radio"
                    aria-labelledby="availability-dated-label"
                    name="availabilitytype"
                    @click=${this.setAvailableDated}
                    ?checked=${this.topic.availability === AVAILABILITY_DATED} />
              </div>
              <div id="availability-dated-label">${this.i18n.make_available_dated}</div>
              <div>${this.i18n.make_available_dated_explanation}</div>
            </div>
            ${this.topic.availability === AVAILABILITY_DATED ? html`
            <div id="add-topic-availability-block">
              <div class="add-topic-date-checkbox">
                <div>
                  <input type="checkbox"
                      aria-labelledby="add-topic-show-label"
                      @click=${this.toggleShowDatePicker}
                      ?checked=${this.topic.showDate}>
                </div>
                <div>
                  <div id="add-topic-show-label">${this.i18n.show}</div>
                  ${this.showShowDatePicker ? html`
                  <div>
                    <span>${this.i18n.date}</span>
                    <sakai-date-picker
                        @datetime-selected=${this.setShowDate}
                        epoch-millis="${this.topic.showDateMillis}"
                        label="${this.i18n.showdate_picker_tooltip}">
                    </sakai-date-picker>
                  </div>
                  ` : ""}
                </div>
              </div>
              <div class="add-topic-date-checkbox">
                <div>
                  <input type="checkbox"
                      aria-labelledby="add-topic-lock-label"
                      @click=${this.toggleLockDatePicker}
                      ?checked=${this.topic.lockDate}>
                </div>
                <div>
                  <div id="add-topic-lock-label">${this.i18n.lock}</div>
                  ${this.showLockDatePicker ? html`
                  <div>
                    <span>${this.i18n.date}</span>
                    <sakai-date-picker
                        @datetime-selected=${this.setLockDate}
                        epoch-millis="${this.topic.lockDateMillis}"
                        label="${this.i18n.lockdate_picker_tooltip}">
                    </sakai-date-picker>
                  </div>
                  ` : ""}
                </div>
              </div>
              <div class="add-topic-date-checkbox">
                <div>
                  <input type="checkbox"
                      aria-labelledby="add-topic-hide-label"
                      @click=${this.toggleHideDatePicker}
                      ?checked=${this.topic.hideDate}>
                </div>
                <div>
                  <div id="add-topic-hide-label">${this.i18n.hide}</div>
                  ${this.showHideDatePicker ? html`
                  <div>
                    <span>${this.i18n.date}</span>
                    <sakai-date-picker
                        @datetime-selected=${this.setHideDate}
                        epoch-millis="${this.topic.hideDateMillis}"
                        label="${this.i18n.hidedate_picker_tooltip}">
                    </sakai-date-picker>
                  </div>
                  ` : ""}
                </div>
              </div>
            </div>
          ` : ""}
          </form>
        </div>
        ` : ""}

        ${this.topic.canModerate ? html`
        <div id="conversations-grading-and-duedate-block" class="add-topic-block">
          <div class="add-topic-label">${this.i18n.grading_and_duedate}</div>
          <div class="add-topic-date-checkbox">
            <div>
              <input type="checkbox"
                  @click=${this.toggleShowDue}
                  ?checked=${this.topic.dueDate}>
            </div>
            <div>
              <div>
                <span>${this.i18n.duedate}</span>
                <span>${this.i18n.duedate_explanation}</span>
              </div>
              ${this.showDue ? html`
              <div>
                <span>${this.i18n.date}</span>
                <sakai-date-picker
                    @datetime-selected=${this.setDueDate}
                    epoch-millis="${this.topic.dueDateMillis}"
                    label="${this.i18n.duedate_picker_tooltip}">
                </sakai-date-picker>
                <div class="add-topic-date-checkbox">
                  <div>
                    <input type="checkbox"
                        @click=${this.toggleShowAcceptUntil}
                        ?checked=${this.topic.lockDate}>
                  </div>
                  <div>
                    <div>
                      <span>${this.i18n.acceptuntildate}</span>
                      <span>${this.i18n.acceptuntildate_explanation}</span>
                    </div>
                    ${this.showAcceptUntil ? html`
                    <div>
                      <span>${this.i18n.date}</span>
                      <sakai-date-picker
                          @datetime-selected=${this.setLockDate}
                          epoch-millis="${this.topic.lockDateMillis}"
                          label="${this.i18n.acceptuntildate_picker_tooltip}">
                      </sakai-date-picker>
                    </div>
                    ` : ""}
                  </div>
                </div>
              </div>
              ` : ""}
            </div>
          </div>
        </div>
        ` : ""}

        <div id="post-options-block" class="add-topic-block">
          <div id="post-options-label" class="add-topic-label">${this.i18n.post_options}</div>
          ${this.canPin ? html`
          <div>
            <input type="checkbox" id="pinned-checkbox"
              @click="${this._setPinned}"
              ?checked=${this.topic.pinned}>
            </input>
            <span class="topic-option-label">${this.i18n.pinned}</span>
            <span class="topic-option-label-text">${this.i18n.pinned_text}</span>
          </div>
          ` : ""}
          ${this.canAnonPost ? html`
          <div>
            <input type="checkbox"
              @click=${this._setAnonymous}
              ?checked=${this.topic.anonymous}>
            </input>
            <span class="topic-option-label">${this.i18n.anonymous}</span>
            <span class="topic-option-label-text">${this.i18n.anonymous_text}</span>
          </div>
          <div>
            <input type="checkbox"
              @click="${this._setAllowAnonymousPosts}"
              ?checked=${this.topic.allowAnonymousPosts}>
            </input>
            <span class="topic-option-label">${this.i18n.anonymous_posts}</span>
            <span class="topic-option-label-text">${this.i18n.anonymous_posts_text}</span>
          </div>
          ` : ""}
          <div>
            <input type="checkbox"
              @click="${this._setMustPostBeforeViewing}"
              ?checked=${this.topic.mustPostBeforeViewing}>
            </input>
            <span class="topic-option-label">${this.i18n.post_before_viewing_label}</span>
          </div>
        </div>

        <div id="button-block" class="act">
          <input type="button" class="active" @click=${this.publish} value="${this.i18n.publish}">
          <input type="button" @click=${this.saveAsDraft} value="${this.i18n.save_as_draft}">
          <input type="button" @click=${this.cancel} value="${this.i18n.cancel}">
        </div>
        ${this.errorDisplay ? html`
          <div id="conv-add-topic-error" class="sak-banner-error">${this.errorDisplay}</div>
        ` : ""}

      </div>
    `;
  }
}

const tagName = "sakai-add-topic";
!customElements.get(tagName) && customElements.define(tagName, SakaiAddTopic);
