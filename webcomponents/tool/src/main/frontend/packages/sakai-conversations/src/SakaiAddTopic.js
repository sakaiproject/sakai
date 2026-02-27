import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-editor/sakai-editor.js";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import "@sakai-ui/sakai-date-picker/sakai-date-picker.js";
import "@sakai-ui/sakai-grader/sakai-grading-item-association.js";
import { AVAILABILITY_DATED,
          AVAILABILITY_NOW,
          QUESTION,
          DISCUSSION,
          INSTRUCTORS,
          SITE, GROUP } from "./sakai-conversations-constants.js";

export class SakaiAddTopic extends SakaiElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    siteId: { attribute: "site-id", type: String },
    aboutReference: { attribute: "about-reference", type: String },
    groups: { type: Array },
    tags: { attribute: "tags", type: Array },
    canPin: { attribute: "can-pin", type: Boolean },
    canAnonPost: { attribute: "can-anon", type: Boolean },
    canGrade: { attribute: "can-grade", type: Boolean },
    canCreateDiscussion: { attribute: "can-create-discussion", type: Boolean },
    canCreateQuestion: { attribute: "can-create-question", type: Boolean },
    disableDiscussions: { attribute: "disable-discussions", type: Boolean },
    canEditTags: { attribute: "can-edit-tags", type: Boolean },
    topic: { type: Object },

    _showShowDatePicker: { state: true },
    _showHideDatePicker: { state: true },
    _showLockDatePicker: { state: true },
    _showDue: { state: true },
    _showAcceptUntil: { state: true },
    _lockDateInvalid: { state: true },
    _dueDateInPast: { state: true },
    _showDateAfterDueDate: { state: true },
    _hideDateBeforeDueDate: { state: true },
  };

  constructor() {

    super();

    this.new = true;

    this.loadTranslations("conversations");
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
    this._showShowDatePicker = !!this.topic.showDate;
    this._showLockDatePicker = !!this.topic.lockDate;
    this._showHideDatePicker = !!this.topic.hideDate;
    this._showDue = !!this.topic.dueDate;
    this._showAcceptUntil = !!this.topic.lockDate;

    const nowMillis = Date.now();
    this.topic.showDateMillis = this.topic.showDate ? this.topic.showDate * 1000 : nowMillis;
    this.topic.lockDateMillis = this.topic.lockDate ? this.topic.lockDate * 1000 : nowMillis;
    this.topic.hideDateMillis = this.topic.hideDate ? this.topic.hideDate * 1000 : nowMillis;
    this.topic.dueDateMillis = this.topic.dueDate ? this.topic.dueDate * 1000 : nowMillis;

    this.new = !value.id;
    this.requestUpdate();
  }

  get topic() { return this._topic; }

  set tags(value) {

    this._tags = value;
    this.selectedTagId = this._tags.length ? this._tags[0].id : null;
  }

  get tags() { return this._tags; }

  _saveAsDraft() { this._save(true); }

  _saveWip() {
    this.dispatchEvent(new CustomEvent("save-wip-topic", { detail: { topic: this.topic }, bubbles: true }));
  }

  _publish() { this._save(false); }

  _save(draft) {

    if (this.topic.title.length < 4) {
      const summaryInput = this.querySelector("#summary");
      summaryInput.classList.add("form-control", "is-invalid");
      summaryInput.focus();
      return;
    }

    if (this._computeLockDateInvalid()) {
      this._lockDateInvalid = true;
      this.updateComplete.then(() => {
        document.querySelector(".portal-main-container")?.scrollTo({ top: 0, behavior: "smooth" });
      });
      return;
    }

    this.topic.draft = draft;

    const lightTopic = { ...this.topic, posts: [] };

    const itemAssociation = this.querySelector("sakai-grading-item-association");
    const useGrading = !!itemAssociation?.useGrading;
    lightTopic.graded = useGrading;
    if (useGrading) {
      lightTopic.createGradingItem = !!itemAssociation.createGradingItem;
      lightTopic.gradingCategory = itemAssociation.category == null ? -1 : Number(itemAssociation.category);
      lightTopic.gradingItemId = itemAssociation.gradingItemId == null ? -1 : Number(itemAssociation.gradingItemId);
      const points = Number(itemAssociation.points);

      if (!(Number.isFinite(points) && points > 0)) {
        console.warn("Grading points must be a positive number");
        itemAssociation.focusPoints();
        return;
      }

      lightTopic.gradingPoints = points;
    } else {
      lightTopic.gradingPoints = undefined;
      // Clear any stale association when grading is off
      lightTopic.createGradingItem = false;
      lightTopic.gradingCategory = -1;
      lightTopic.gradingItemId = -1;
    }

    fetch(this.topic.url, {
      method: this.new ? "POST" : "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(lightTopic),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error("Network error while creating topic.");
    })
    .then(topic => {

      Object.assign(this.topic, topic);
      this.dispatchEvent(new CustomEvent("topic-saved", { detail: { topic: this.topic }, bubbles: true }));
    })
    .catch (error => console.error(error));
  }

  _updateMessage(e) {

    this.topic.message = e.detail.content;
    this.dispatchEvent(new CustomEvent("topic-dirty", { bubbles: true }));
    this._saveWip();
  }

  _updateSummary(e) {

    this.topic.title = e.target.value;
    this.dispatchEvent(new CustomEvent("topic-dirty", { bubbles: true }));
    this._saveWip();
  }

  _cancel() {
    this.dispatchEvent(new CustomEvent("topic-add-cancelled", { bubbles: true }));
  }

  _setType(e) {

    this.topic.type = e.target.dataset.type;
    this._saveWip();
  }

  _selectTag() {

    const tagId = this.selectedTagId;

    this.topic.tags ??= [];

    const existingIndex = this.topic.tags.findIndex(t => t?.id == tagId);
    if (existingIndex !== -1) {
      this.topic.tags.splice(existingIndex, 1);
    } else {
      const tag = this.tags.find(t => t?.id == tagId);
      tag && this.topic.tags.push(tag);
    }

    this._saveWip();

    this.requestUpdate();
  }

  _removeTag(e) {

    const tagId = e.target.dataset.tagId;
    const existingIndex = this.topic.tags.findIndex(t => t.id == tagId);
    this.topic.tags.splice(existingIndex, 1);
    this.requestUpdate();
  }

  _editAvailableTags() {

    this._saveWip();
    this.dispatchEvent(new CustomEvent("edit-tags", { bubbles: true }));
  }

  _toggleGroup(e) {

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

    this._saveWip();
  }

  _toggleShowDue(e) {

    this._showDue = e.target.checked;
    if (this._showDue) {
      const nowSeconds = Math.floor(Date.now() / 1000);
      const fallbackSeconds = this.topic.dueDate ?? nowSeconds;
      this.topic.dueDate = fallbackSeconds;
      this.topic.dueDateMillis = fallbackSeconds * 1000;
      this._dueDateInPast = fallbackSeconds < nowSeconds;
      this._lockDateInvalid = this._computeLockDateInvalid();
      this._validateShowDate();
      this._validateHideDate();
    } else {
      this.topic.dueDate = undefined;
      this.topic.dueDateMillis = undefined;
      this._showAcceptUntil = false;
      this.topic.lockDate = undefined;
      this.topic.lockDateMillis = undefined;
      this._dueDateInPast = false;
      this._showDateAfterDueDate = false;
      this._hideDateBeforeDueDate = false;
      this._lockDateInvalid = false;
    }
    this._saveWip();
  }

  _toggleShowAcceptUntil(e) {

    this._showAcceptUntil = e.target.checked;

    if (this._showAcceptUntil) {
      const nowSeconds = Math.floor(Date.now() / 1000);
      const fallbackSeconds = this.topic.lockDate
        ?? (this.topic.dueDate != null ? Math.max(this.topic.dueDate, nowSeconds) : nowSeconds);
      this.topic.lockDate = fallbackSeconds;
      this.topic.lockDateMillis = fallbackSeconds * 1000;
      this._lockDateInvalid = this._computeLockDateInvalid();
    } else {
      this.topic.lockDate = undefined;
      this.topic.lockDateMillis = undefined;
      this._lockDateInvalid = false;
    }

    this._saveWip();
  }

  _setVisibility(e) {

    this.topic.visibility = e.target.dataset.visibility;
    if (this.topic.visibility != GROUP) {
      this.topic.groups = [];
    }
    this._saveWip();
    this.requestUpdate();
  }

  _setShowDate(e) {

    this.topic.showDate = e.detail.epochSeconds;
    this._validateShowDate();
    this._saveWip();
  }

  _validateShowDate() {

    this._showDateAfterDueDate = this.topic.showDate && this.topic.dueDate
                                    && this.topic.dueDate <= this.topic.showDate;
  }

  _validateHideDate() {

    this._hideDateBeforeDueDate = this.topic.hideDate && this.topic.dueDate
                                    && this.topic.dueDate > this.topic.hideDate;
  }

  _computeLockDateInvalid() {

    const { dueDate, lockDate } = this.topic;

    if (dueDate == null || lockDate == null || dueDate === "" || lockDate === "") {
      return false;
    }

    const due = Number(dueDate);
    const lock = Number(lockDate);
    return Number.isFinite(due) && Number.isFinite(lock) && lock < due;
  }

  _setLockDate(e) {

    this.topic.lockDate = e.detail.epochSeconds;
    this._lockDateInvalid = this._computeLockDateInvalid();
    this._saveWip();
  }

  _setHideDate(e) {

    this.topic.hideDate = e.detail.epochSeconds;
    this._validateHideDate();
    this._saveWip();
  }

  _setDueDate(e) {

    this.topic.dueDate = e.detail.epochSeconds;
    this._dueDateInPast = e.detail.epochMillis < Date.now();
    this._validateShowDate();
    this._validateHideDate();
    this._saveWip();
  }

  _setAvailableNow() {

    this.topic.availability = AVAILABILITY_NOW;
    this.topic.showDate = null;
    this.topic._showHideDatePicker = null;
    this.topic._showLockDatePicker = null;
    this._showShowDatePicker = false;
    this._showHideDatePicker = false;
    this._showLockDatePicker = false;
    this.requestUpdate();
  }

  _setAvailableDated() {

    this.topic.availability = AVAILABILITY_DATED;
    this.requestUpdate();
  }

  _toggleShowDatePicker(e) {

    this._showShowDatePicker = e.target.checked;
    if (!e.target.checked) {
      this.topic.showDate = undefined;
    }
  }

  _toggleLockDatePicker(e) {

    this._showLockDatePicker = e.target.checked;
    if (!e.target.checked) {
      this.topic.lockDate = undefined;
    }
  }

  _toggleHideDatePicker(e) {

    this._showHideDatePicker = e.target.checked;
    if (!e.target.checked) {
      this.topic.hideDate = undefined;
    }
  }

  _resetTitle() { this.titleError = false; }

  _setSelectedTagId(e) { this.selectedTagId = e.target.value; }

  _setPinned(e) {

    this.topic.pinned = e.target.checked;
    this._saveWip();
  }

  _setAnonymous(e) {

    this.topic.anonymous = e.target.checked;
    this._saveWip();
  }

  _setAllowAnonymousPosts(e) {

    this.topic.allowAnonymousPosts = e.target.checked;
    this._saveWip();
  }

  _setMustPostBeforeViewing(e) {

    this.topic.mustPostBeforeViewing = e.target.checked;
    this._saveWip();
  }

  _renderTitle() {

    return html`
      ${this.canCreateQuestion && !this.canCreateDiscussion ? html`
        <h1>${this.new ? this._i18n.add_a_new_question : this._i18n.edit_question}</h1>
      ` : nothing}
      ${this.canCreateDiscussion && !this.canCreateQuestion ? html`
        <h1>${this.new ? this._i18n.add_a_new_discussion : this._i18n.edit_discussion}</h1>
      ` : nothing}
      ${this.canCreateDiscussion && this.canCreateQuestion ? html`
        <h1>${this.new ? this._i18n.add_a_new_topic : this._i18n.edit_topic}</h1>
      ` : nothing}
    `;
  }

  firstUpdated() {

    this.querySelector("#summary").focus();
    /*
    let url = `addTopic`;
    history.pushState({ state: STATE_ADDING_TOPIC }, "", url);
    */
  }

  shouldUpdate() {
    return this._i18n && this.tags && (this.topic || this.aboutReference);
  }

  render() {

    return html`
      ${this.topic.beingEdited ? html`
      <div class="sak-banner-info">${this._i18n.editing_topic}</div>
      ` : nothing}
      ${this._lockDateInvalid ? html`
      <div class="sak-banner-error">${this._i18n.invalid_lock_date}</div>
      ` : nothing}
      <div class="add-topic-wrapper">
        ${this._renderTitle()}

        ${this.disableDiscussions ? nothing : html`
        <div class="add-topic-block">
          ${this.canCreateQuestion && this.canCreateDiscussion ? html`
          <div id="post-type-label" class="add-topic-label">${this._i18n.topic_type}</div>
          <div id="topic-type-toggle-block">
            <div @click=${this._setType}
                @keydown=${this._setType}
                tabindex="0"
                data-type="${QUESTION}"
                class="topic-type-toggle ${this.topic.type === QUESTION ? "active" : ""}">
              <div>
                <sakai-icon type="question" size="medium"></sakai-icon>
                <div>${this._i18n.type_question}</div>
              </div>
              <div class="topic-type-description">${this._i18n.question_type_description}</div>
            </div>
            <div @click=${this._setType}
                @keydown=${this._setType}
                tabindex="0"
                data-type="${DISCUSSION}"
                class="topic-type-toggle ${this.topic.type === DISCUSSION ? "active" : ""}">
              <div>
                <sakai-icon type="forums" size="medium"></sakai-icon>
                <div>${this._i18n.type_discussion}</div>
              </div>
              <div class="topic-type-description">${this._i18n.discussion_type_description}</div>
            </div>
          </div>
          ` : nothing}
        </div>
        `}

        <div class="add-topic-block">
          <div id="summary-label" class="add-topic-label required">${this._i18n.summary}</div>
          <input id="summary"
            @change=${this._updateSummary}
            aria-labelledby="summary-label"
            .value="${this.topic.title}"
            required />
          <div class="required-info">
            <span>* ${this._i18n.required}</span>
            <span>(${this._i18n.min_title_characters_info})</span>
          </div>
        </div>
        <div class="add-topic-block">
          <div id="details-label" class="add-topic-label">${this._i18n.details}</div>
          <sakai-editor
              content="${this.topic.message}"
              label="${this._i18n.details}"
              @changed=${this._updateMessage}
              id="topic-details-editor">
          </sakai-editor>
        </div>

        ${this.tags.length || this.canEditTags ? html`
        <div id="tag-post-block" class="add-topic-block">
          <div id="tag-post-label" class="add-topic-label">${this._i18n.tag_topic}</div>
          ${this.tags.length > 0 ? html`
          <select @change="${this._setSelectedTagId}" aria-labelledby="tag-post-label">
            ${this.tags.map(tag => html`
            <option value="${tag.id}">${tag.label}</option>
            `)}
          </select>
          <input type="button" value="${this._i18n.add}" @click=${this._selectTag}>
          ` : nothing}
          ${this.canEditTags ? html`
          <span id="conv-edit-tags-link-wrapper">
            <button type="button"
                class="btn btn-link"
                @click=${this._editAvailableTags}>
              ${this._i18n.edit_tags}
            </button>
          </span>
          ` : nothing}
          <div id="tags">
          ${this.topic.tags?.map(tag => html`
            <div class="tag">
              <div>${tag.label}</div>
              <a href="javascript:;" data-tag-id="${tag.id}" @click=${this._removeTag} aria-label="${this._i18n.remove} ${tag.label}">
                <div class="tag-remove-icon">
                  <sakai-icon type="close" size="small"></sakai-icon>
                </div>
              </a>
            </div>
          `)}
          </div>
        </div>
        ` : nothing}

        <div id="post-to-block" class="add-topic-block">
          <div id="post-to-label" class="add-topic-label">${this._i18n.post_to}</div>
          <form>
          <div id="topic-visibility-wrapper">
            <div>
              <label>
              <input
                  type="radio"
                  name="post-to"
                  data-visibility="${SITE}"
                  @click=${this._setVisibility}
                  ?checked=${this.topic.visibility === SITE}>${this._i18n.everyone}
              </label>
            </div>
            <div>
              <label>
                <input
                    type="radio"
                    name="post-to"
                    data-visibility="${INSTRUCTORS}"
                    @click=${this._setVisibility}
                    ?checked=${this.topic.visibility === INSTRUCTORS}>${this._i18n.instructors}
              </label>
            </div>
            ${this.groups?.length > 0 ? html`
              <div>
                <label>
                  <input
                      type="radio"
                      name="post-to"
                      data-visibility="${GROUP}"
                      @click=${this._setVisibility}
                      ?checked=${this.topic.visibility === GROUP}>${this._i18n.groups}
                </label>
              </div>
              ${this.topic.visibility === GROUP ? html`
              <div id="add-topic-groups-block">
                ${this.groups.map(group => html`
                <div class="add-topic-group-block">
                  <label>
                    <input type="checkbox"
                        @click=${this._toggleGroup}
                        value="${group.reference}"
                        ?checked=${this.topic.groups.includes(group.reference)}>${group.title}
                  </label>
                </div>
                `)}
              </div>
              ` : nothing}
            ` : nothing}
          </div>
          </form>
        </div>

        ${this.topic.canModerate ? html`
        <div id="conversations-availability-block" class="add-topic-block">
          <form>
            <div id="availability-label" class="add-topic-label">${this._i18n.availability}</div>
            <div class="availability-wrapper">
              <div>
                <input
                    type="radio"
                    id="add-topic-now"
                    aria-labelledby="availability-now-label"
                    name="availabilitytype"
                    @click=${this._setAvailableNow}
                    ?checked=${this.topic.availability === AVAILABILITY_NOW} />
              </div>
              <div id="availability-now-label">${this._i18n.make_available_now}</div>
              <div>${this._i18n.make_available_now_explanation}</div>
            </div>
            <div class="availability-wrapper">
              <div>
                <input
                    type="radio"
                    aria-labelledby="availability-dated-label"
                    name="availabilitytype"
                    @click=${this._setAvailableDated}
                    ?checked=${this.topic.availability === AVAILABILITY_DATED} />
              </div>
              <div id="availability-dated-label">${this._i18n.make_available_dated}</div>
              <div>${this._i18n.make_available_dated_explanation}</div>
            </div>
            ${this.topic.availability === AVAILABILITY_DATED ? html`
            <div id="add-topic-availability-block">
              <div class="add-topic-date-checkbox">
                <div>
                  <input type="checkbox"
                      aria-labelledby="add-topic-show-label"
                      @click=${this._toggleShowDatePicker}
                      ?checked=${this.topic.showDate}>
                </div>
                <div>
                  <div id="add-topic-show-label">${this._i18n.show}</div>
                  ${this._showShowDatePicker ? html`
                  <div>
                    <span>${this._i18n.date}</span>
                    <sakai-date-picker
                        @datetime-selected=${this._setShowDate}
                        epoch-millis="${this.topic.showDateMillis}"
                        label="${this._i18n.showdate_picker_tooltip}">
                    </sakai-date-picker>
                  </div>
                  ` : nothing}
                </div>
              </div>
              <div class="add-topic-date-checkbox">
                <div>
                  <input type="checkbox"
                      aria-labelledby="add-topic-lock-label"
                      @click=${this._toggleLockDatePicker}
                      ?checked=${this.topic.lockDate}>
                </div>
                <div>
                  <div id="add-topic-lock-label">${this._i18n.lock}</div>
                  ${this._showLockDatePicker ? html`
                  <div>
                    <span>${this._i18n.date}</span>
                    <sakai-date-picker
                        @datetime-selected=${this._setLockDate}
                        epoch-millis="${this.topic.lockDateMillis}"
                        label="${this._i18n.lockdate_picker_tooltip}">
                    </sakai-date-picker>
                  </div>
                  ` : nothing}
                </div>
              </div>
              <div class="add-topic-date-checkbox">
                <div>
                  <input type="checkbox"
                      aria-labelledby="add-topic-hide-label"
                      @click=${this._toggleHideDatePicker}
                      ?checked=${this.topic.hideDate}>
                </div>
                <div>
                  <div id="add-topic-hide-label">${this._i18n.hide}</div>
                  ${this._showHideDatePicker ? html`
                  <div>
                    <span>${this._i18n.date}</span>
                    <sakai-date-picker
                        @datetime-selected=${this._setHideDate}
                        epoch-millis="${this.topic.hideDateMillis}"
                        label="${this._i18n.hidedate_picker_tooltip}">
                    </sakai-date-picker>
                  </div>
                  ` : nothing}
                </div>
              </div>
            </div>
          ` : nothing}
          </form>
        </div>
        ` : nothing}

        ${this.canGrade ? html`
        <div class="add-topic-label mb-2">${this._i18n.grading}</div>
        <sakai-grading-item-association
            site-id="${this.siteId}"
            gradable-type="Topic"
            .gradingItemId=${this.topic.gradingItemId}
            gradable-ref="${this.topic.reference}"
            .useGrading=${this.topic.graded ?? (Number(this.topic.gradingItemId) > 0)}>
        </sakai-grading-item-association>
        ` : nothing}

        ${this.topic.canModerate ? html`
        <div id="conversations-grading-and-duedate-block" class="add-topic-block">
          <div class="add-topic-label">${this._i18n.grading_and_duedate}</div>
          ${this._dueDateInPast ? html`
            <div class="sak-banner-warn">${this._i18n.duedate_in_past_warning}</div>
          ` : nothing}
          ${this._showDateAfterDueDate ? html`
            <div class="sak-banner-warn">${this._i18n.showdate_after_duedate_warning}</div>
          ` : nothing}
          ${this._hideDateBeforeDueDate ? html`
            <div class="sak-banner-warn">${this._i18n.hidedate_before_duedate_warning}</div>
          ` : nothing}
          <div class="add-topic-date-checkbox">
            <div>
              <input type="checkbox"
                  @click=${this._toggleShowDue}
                  aria-labelledby="add-topic-duedate-label"
                  ?checked=${this.topic.dueDate}>
            </div>
            <div>
              <div>
                <span id="add-topic-duedate-label">${this._i18n.duedate}</span>
                <span>${this._i18n.duedate_explanation}</span>
              </div>
              ${this._showDue ? html`
              <div>
                <span>${this._i18n.date}</span>
                <sakai-date-picker
                    @datetime-selected=${this._setDueDate}
                    epoch-millis="${this.topic.dueDateMillis}"
                    label="${this._i18n.duedate_picker_tooltip}">
                </sakai-date-picker>
                <div class="add-topic-date-checkbox">
                  <div>
                    <input type="checkbox"
                        @click=${this._toggleShowAcceptUntil}
                        aria-labelledby="add-topic-lockdate-label"
                        ?checked=${this.topic.lockDate}>
                  </div>
                  <div>
                    <div>
                      <span id="add-topic-lockdate-label">${this._i18n.acceptuntildate}</span>
                      <span>${this._i18n.acceptuntildate_explanation}</span>
                    </div>
                    ${this._showAcceptUntil ? html`
                    <div>
                      <span>${this._i18n.date}</span>
                      <sakai-date-picker
                          @datetime-selected=${this._setLockDate}
                          epoch-millis="${this.topic.lockDateMillis}"
                          label="${this._i18n.acceptuntildate_picker_tooltip}">
                      </sakai-date-picker>
                    </div>
                    ` : nothing}
                  </div>
                </div>
              </div>
              ` : nothing}
            </div>
          </div>
        </div>
        ` : nothing}

        <div id="post-options-block" class="add-topic-block">
          <div id="post-options-label" class="add-topic-label">${this._i18n.post_options}</div>
          ${this.canPin ? html`
          <div>
            <input type="checkbox"
              id="pinned-checkbox"
              aria-labelledby="pinned-checkbox-label"
              @click="${this._setPinned}"
              ?checked=${this.topic.pinned}>
            </input>
            <span id="pinned-checkbox-label" class="topic-option-label">${this._i18n.pinned}</span>
            <span class="topic-option-label-text">${this._i18n.pinned_text}</span>
          </div>
          ` : nothing}
          ${this.canAnonPost ? html`
          <div>
            <input type="checkbox"
              id="anon-checkbox"
              aria-labelledby="anon-checkbox-label"
              @click=${this._setAnonymous}
              ?checked=${this.topic.anonymous}>
            </input>
            <span id="anon-checkbox-label" class="topic-option-label">${this._i18n.anonymous}</span>
            <span class="topic-option-label-text">${this._i18n.anonymous_text}</span>
          </div>
          <div>
            <input type="checkbox"
              aria-labelledby="allow-anon-checkbox-label"
              @click="${this._setAllowAnonymousPosts}"
              ?checked=${this.topic.allowAnonymousPosts}>
            </input>
            <span id="allow-anon-checkbox-label" class="topic-option-label">${this._i18n.anonymous_posts}</span>
            <span class="topic-option-label-text">${this._i18n.anonymous_posts_text}</span>
          </div>
          ` : nothing}
          <div>
            <input type="checkbox"
              aria-labelledby="post-before-viewing-label"
              @click="${this._setMustPostBeforeViewing}"
              ?checked=${this.topic.mustPostBeforeViewing}>
            </input>
            <span id="post-before-viewing-label" class="topic-option-label">${this._i18n.post_before_viewing_label}</span>
          </div>
        </div>

        <div id="button-block" class="act">
          <input type="button" class="active" @click=${this._publish} value="${this._i18n.publish}">
          <input type="button" @click=${this._saveAsDraft} value="${this._i18n.save_as_draft}">
          <input type="button" @click=${this._cancel} value="${this._i18n.cancel}">
        </div>

      </div>
    `;
  }
}
