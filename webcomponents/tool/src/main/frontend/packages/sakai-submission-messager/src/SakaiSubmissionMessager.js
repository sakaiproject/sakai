import { SakaiElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-group-picker";
import "@spectrum-web-components/progress-bar/sp-progress-bar.js";

export class SakaiSubmissionMessager extends SakaiElement {

  static properties = {

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
    recipientsRequested: { type: Boolean },
  };

  constructor() {

    super();

    this.groups = [];
    this.recipientsToCheck = [];
    this.recipientsRequested = false;
    this._i18n = {};
    this.reset();
    this.loadTranslations("submission-messager").then(t => this._i18n = t);
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`
      <div id="submission-messager-${this.assignmentId}" class="submission-messager">
        ${this.validationError ? html`
          <div class="alert alert-danger" role="alert">
            ${this.validationError}
          </div>
        ` : nothing}
        
        <div class="fs-5 fw-bold mb-2">${this.title}</div>
        
        <div class="mb-2">
          <label id="sm-subject-label-${this.assignmentId}" class="sr-only form-label" for="subject-${this.assignmentId}">${this._i18n.subject}</label>
          <input id="subject-${this.assignmentId}"
                 class="form-control"
                 type="text"
                 .value=${this.subject}
                 @change=${e => this.subject = e.target.value}
                 placeholder="${this._i18n.subject_placeholder}"/>
        </div>

        <div class="mb-2">
          <label id="sm-body-label-${this.assignmentId}" class="sr-only form-label" for="body-${this.assignmentId}">${this._i18n.message}</label>
          <textarea id="body-${this.assignmentId}"
                    class="form-control"
                    rows="4"
                    .value=${this.body}
                    placeholder="${this._i18n.message}"
                    @change=${e => this.body = e.target.value}>${this.body}</textarea>
        </div>

        <div class="mb-2">
          <label id="sm-action-selector-label-${this.assignmentId}" class="form-label" for="action-${this.assignmentId}">${this._i18n.select_action}</label>
          <select id="action-${this.assignmentId}"
                  class="form-select"
                  @change=${this.actionChanged}>
            <option value="1" ?selected=${this.action === "1"}>${this._i18n.ungraded_students}</option>
            <option value="2">${this._i18n.graded_students}</option>
            <option value="3">${this._i18n.all_students}</option>
          </select>
        </div>

        ${this.action === "2" ? html`
          <div class="row mb-2">
            <div class="col-6">
              <label class="form-label" for="min-score-${this.assignmentId}">${this._i18n.min_score_label}</label>
              <input id="min-score-${this.assignmentId}"
                     type="number"
                     class="form-control"
                     @input=${this.minScoreChanged} />
            </div>
            <div class="col-6">
              <label class="form-label" for="max-score-${this.assignmentId}">${this._i18n.max_score_label}</label>
              <input id="max-score-${this.assignmentId}"
                     type="number"
                     class="form-control"
                     @input=${this.maxScoreChanged} />
            </div>
          </div>
        ` : nothing}

        <div class="mb-2">
          <label id="sm-group-selector-label-${this.assignmentId}" class="form-label">${this._i18n.select_group}</label>
          <sakai-group-picker
            site-id="${portal.siteId}"
            group-ref="${ifDefined(this.groupId)}"
            aria-labelledby="sm-group-selector-label-${this.assignmentId}"
            class="d-block"
            @groups-selected=${this.groupSelected}>
          </sakai-group-picker>
        </div>

        <button type="button" class="btn btn-outline-primary mb-2" @click=${this.listRecipients}>
          ${this._i18n.show_recipients}
        </button>

        ${this.recipientsRequested ? html`
          ${this.recipientsToCheck.length > 0 ? html`
            <div class="card mb-2">
              <div class="card-header py-1 d-flex justify-content-between align-items-center">
                <span class="small">${this._i18n.recipients}</span>
                <span class="badge bg-secondary">${this.recipientsToCheck.length}</span>
              </div>
              <div class="card-body p-0" style="max-height: 100px; overflow-y: auto;">
                <div class="list-group list-group-flush small">
                  ${this.recipientsToCheck.map(r => html`
                    <div class="list-group-item py-1">${r.displayName}</div>
                  `)}
                </div>
              </div>
            </div>
          ` : html`
            <div class="alert alert-warning py-1 small mb-2">
              ${this._i18n.no_recipients || "No recipients found"}
            </div>
          `}
        ` : nothing}

        <div class="d-flex align-items-center gap-2">
          <button type="button"
                  class="btn btn-primary"
                  ?disabled=${this.sending}
                  @click=${this.sendMessage}>
            ${this._i18n.send}
          </button>
          ${this.sending ? html`
            <div class="flex-grow-1">
              <sp-progress-bar aria-label="Sending message" indeterminate></sp-progress-bar>
            </div>
          ` : nothing}
          ${this.success ? html`
            <div class="alert alert-success mb-0 py-2">${this._i18n.success}</div>
          ` : nothing}
          ${this.error ? html`
            <div class="alert alert-danger mb-0 py-2">${this._i18n.error}</div>
          ` : nothing}
        </div>
      </div>
    `;
  }

  actionChanged(e) {
    this.recipientsToCheck = [];
    this.recipientsRequested = false;
    this.action = e.target.value;
  }

  minScoreChanged(e) {
    this.recipientsToCheck = [];
    this.recipientsRequested = false;
    this.minScore = e.target.value;
  }

  maxScoreChanged(e) {
    this.recipientsToCheck = [];
    this.recipientsRequested = false;
    this.maxScore = e.target.value;
  }

  groupSelected(e) {
    this.recipientsToCheck = [];
    this.recipientsRequested = false;
    this.groupId = e.detail.value[0];
  }

  reset() {

    this.groupId = `/site/${portal.siteId}`;
    this.action = "1";
    this.subject = "";
    this.body = "";
    this.success = false;
    this.error = false;
    this.recipientsToCheck = [];
    this.minScore = "";
    this.maxScore = "";
    this.validationError = "";
    this.recipientsRequested = false;
  }

  getFormData() {

    const formData = new FormData();
    formData.set("action", this.action);
    formData.set("groupRef", this.groupId || "");
    formData.set("minScore", this.minScore || "");
    formData.set("maxScore", this.maxScore || "");
    formData.set("siteId", portal.siteId);
    formData.set("subject", this.subject);
    formData.set("body", this.body);
    formData.set("assignmentId", this.assignmentId);
    return formData;
  }

  listRecipients() {
    this.recipientsRequested = true;
    const formData = this.getFormData();

    fetch("/direct/gbng/listMessageRecipients.json", { method: "POST", cache: "no-cache", credentials: "same-origin", body: formData })
      .then(r => r.json())
      .then(data => {
        this.recipientsToCheck = data;
      });
  }

  sendMessage() {

    if (!this.subject || !this.body) {
      this.validationError = this._i18n.validation_error || "You need to supply a subject and body!";
      return;
    }

    const formData = this.getFormData();

    this.sending = true;

    fetch("/direct/gbng/messageStudents.json", { method: "POST", cache: "no-cache", credentials: "same-origin", body: formData })
      .then(r => {

        if (r.ok) {
          this.error = false;
          return r.json();
        }
        this.error = true;

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
