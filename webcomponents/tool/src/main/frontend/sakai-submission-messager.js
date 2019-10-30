import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

class SakaiSubmissionMessager extends SakaiElement {

  constructor() {

    super();

    this.reset();
    this.loadTranslations("submission-messager").then(t => { this.i18n = t; this.requestUpdate(); });
  }

  static get properties() {

    return {
      assignmentId: { attribute: "assignment-id", type: String },
      title: String,
      action: Number,
      subject: String,
      body: String,
      error: String,
      success: String,
    };
  }

  shouldUpdate(changed) {
    return this.i18n;
  }

  render() {

    // sm = submissionmessager. So, sm-label = submissionmessager-label

    return html`
      <div id="submission-messager-${this.assignmentId}" class="submission-messager">
        <div class="sm-title">${this.title}</div>
        <div id="sm-group-selector-label-${this.assignmentId}" class="sm-label">${this.i18n["select_group"]}</div>
        <select aria-labelledby="sm-group-selector-label-${this.assignmentId}" class="group-select" @change=${e => this.action = e.target.value}>
          <option value="1">${this.i18n["ungraded_students"]}
          <option value="2">${this.i18n["graded_students"]}
          <option value="3">${this.i18n["all_students"]}
        </select>
        <div id="sm-subject-label-${this.assignmentId}" class="sm-label">${this.i18n["subject"]}</div>
        <input class="subject-input" aria-labelledby="sm-subject-label-${this.assignmentId}"
                type="text" .value=${this.subject} @change=${e => this.subject = e.target.value}
                placeholder="${this.i18n["subject_placeholder"]}"/>
        <div id="sm-body-label-${this.assignmentId}" class="sm-label">${this.i18n["message"]}</div>
        <textarea aria-labelledby="sm-body-label-${this.assignmentId}" .value=${this.body} id="message-editor-${this.assignmentId}" class="message-input" @change=${e => this.body = e.target.value}>${this.body}</textarea>
        <div class="send-button-wrapper">
          <button @click=${this.sendMessage}>${this.i18n["send"]}</button>
        </div>
        ${this.error ? html`<div class="sm-alert sm-error">${this.error}</div>` : ""}
        ${this.success ? html`<div class="sm-alert sm-success">${this.success}</div>` : ""}
      </div>
    `;
  }

  reset() {

    this.action = 1;
    this.subject = "";
    this.body = "";
    this.success = "";
    this.error = "";
  }

  sendMessage(e) {

    let formData = new FormData();
    formData.set("action", this.action);
    formData.set("siteId", portal.siteId);
    formData.set("subject", this.subject);
    formData.set("body", this.body);
    formData.set("assignmentId", this.assignmentId);

    fetch(`/direct/gbng/messageStudents.json`, {method: "POST", cache: "no-cache", credentials: "same-origin", body: formData})
      .then(r => {
        if (r.ok) {
          this.reset();
          this.requestUpdate();
          this.success = this.i18n["success"];
          window.setTimeout(() => this.dispatchEvent(new CustomEvent("submission-message-sent", { bubbles: true })), 1000);
        } else {
          this.error = this.i18n["error"];
        }
      });
  }
}

customElements.define("sakai-submission-messager", SakaiSubmissionMessager);
