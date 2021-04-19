import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";
import "./sakai-group-picker.js";

class SakaiSubmissionMessager extends SakaiElement {

  constructor() {

    super();

    this.groups = [];
    this.recipientsToCheck = [];
    this.i18n = {};
    this.group = `/site/${portal.siteId}`;
    this.reset();
    this.loadTranslations("submission-messager").then(t => this.i18n = t);
  }

  static get properties() {

    return {
      assignmentId: { attribute: "assignment-id", type: String },
      groups: { type: Array },
      title: String,
      action: String,
      subject: String,
      body: String,
      error: Boolean,
      success: Boolean,
      groupId: String,
      validationError: String,
      recipientsToCheck: Array,
      sending: Boolean,
      i18n: Object,
    };
  }

  shouldUpdate() {
    return this.i18n;
  }

  render() {

    // sm = submissionmessager. So, sm-label = submissionmessager-label

    return html`
      <div id="submission-messager-${this.assignmentId}" class="submission-messager">
        <div class="sak-banner-error" style="display: ${this.validationError ? "block" : "none"}">
          ${this.validationError}
        </div>
        <div class="sm-title">${this.title}</div>
        <div class="sm-block">
          <span id="sm-subject-label-${this.assignmentId}" class="sm-label">${this.i18n["subject"]}</span>
          <input class="subject-input" aria-labelledby="sm-subject-label-${this.assignmentId}"
                  type="text" .value=${this.subject} @change=${e => this.subject = e.target.value}
                  placeholder="${this.i18n["subject_placeholder"]}"/>
          <div id="sm-body-label-${this.assignmentId}" class="sm-label">${this.i18n["message"]}</div>
          <textarea aria-labelledby="sm-body-label-${this.assignmentId}" .value=${this.body} class="message-input" @change=${e => this.body = e.target.value}>${this.body}</textarea>
        </div>
        <div class="sm-block">
          <span id="sm-action-selector-label-${this.assignmentId}" class="sm-label">${this.i18n["select_action"]}</span>
          <select aria-labelledby="sm-action-selector-label-${this.assignmentId}" class="group-select" @change=${this.actionChanged}>
            <option value="1" ?selected=${this.action === "1"}>${this.i18n["ungraded_students"]}
            <option value="2">${this.i18n["graded_students"]}
            <option value="3">${this.i18n["all_students"]}
          </select>
        </div>
        <div class="sm-score-block" style="display: ${this.action === "2" ? "block" : "none"}">
          <div><label>${this.i18n["min_score_label"]}<input type="text" size="6" @input=${this.minScoreChanged} /></label></div>
          <div><label>${this.i18n["max_score_label"]}<input type="text" size="6" @input=${this.maxScoreChanged} /></label></div>
        </div>
        <div class="sm-block">
          <span id="sm-group-selector-label-${this.assignmentId}" class="sm-label">${this.i18n["select_group"]}</span>
          <sakai-group-picker
            site-id="${portal.siteId}"
            group-id="${this.groupId}"
            aria-labelledby="sm-group-selector-label-${this.assignmentId}"
            class="group-select"
            @group-selected=${this.groupSelected}>
          </sakai-group-picker>
        </div>
        <button @click=${this.listRecipients}>${this.i18n["show_recipients"]}</button>
        ${this.recipientsToCheck.length > 0 ? html`
          <div class="sm-recipients-label">${this.i18n["recipients"]}</div>
          <div class="sm-recipients">
            ${this.recipientsToCheck.map(r => html`<div>${r.displayName}</div>`)}
          </div>
        ` : ""}
        <div class="send-button-wrapper">
          <button @click=${this.sendMessage} style="display: inline-block;">${this.i18n["send"]}</button>
          ${this.sending ? html`<div id="sm-progressbar-${this.assignmentId}" class="sm-progressbar"></div>` : ""}
          ${this.success ? html`<span class="sm-alert sak-banner-success-inline">${this.i18n["success"]}</span>` : ""}
          ${this.error ? html`<span class="sm-alert sak-banner-error-inline">${this.i18n["error"]}</span>` : ""}
        </div>
      </div>
    `;
  }

  actionChanged(e) {

    this.recipientsToCheck = [];
    this.action = e.target.value;
  }

  minScoreChanged(e) {

    this.recipientsToCheck = [];
    this.minScore = e.target.value;
  }

  maxScoreChanged(e) {

    this.recipientsToCheck = [];
    this.maxScore = e.target.value;
  }

  groupSelected(e) {

    this.recipientsToCheck = [];
    this.group = e.detail.value;
  }

  reset() {

    this.groupId = "any";
    this.action = "1";
    this.subject = "";
    this.body = "";
    this.success = false;
    this.error = false;
    this.recipientsToCheck = [];
    this.minScore = "";
    this.maxScore = "";
    this.validationError = "";
  }

  getFormData() {

    const formData = new FormData();
    formData.set("action", this.action);
    formData.set("groupRef", this.group || "");
    formData.set("minScore", this.minScore || "");
    formData.set("maxScore", this.maxScore || "");
    formData.set("siteId", portal.siteId);
    formData.set("subject", this.subject);
    formData.set("body", this.body);
    formData.set("assignmentId", this.assignmentId);
    return formData;
  }

  listRecipients() {

    const formData = this.getFormData();

    fetch(`/direct/gbng/listMessageRecipients.json`, {method: "POST", cache: "no-cache", credentials: "same-origin", body: formData})
      .then(r => r.json())
      .then(data => {

          this.recipientsToCheck = data;
      });
  }

  sendMessage() {

    if (!this.subject || !this.body) {
      this.validationError = "You need to supply a subject and body!";
      return;
    }

    const formData = this.getFormData();

    this.sending = true;

    this.updateComplete.then(() => {
      $(`#sm-progressbar-${this.assignmentId}`).progressbar({value: false});
    });

    fetch(`/direct/gbng/messageStudents.json`, {method: "POST", cache: "no-cache", credentials: "same-origin", body: formData})
      .then(r => {

        if (r.ok) {
          this.error = false;
          return r.json();
        } else {
          this.error = true;
        }
      })
      .then(data => {

        if (data.result) {
          this.success = true;
          this.sending = false;
          window.setTimeout(() => {
            this.success = false;
            this.reset();
          }, 1500);
        }
      });
  }
}

customElements.define("sakai-submission-messager", SakaiSubmissionMessager);
