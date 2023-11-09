import { css, html } from "../assets/lit-element/lit-element.js";
import { loadProperties } from "../sakai-i18n.js";
import { SakaiDialogContent } from "../sakai-dialog-content.js";
import "../sakai-date-picker.js";
import "../sakai-icon.js";
import "../sakai-editor.js";
import { GROUP, SITE, USER } from "./assignation-types.js";

/**
 * Handles the creation or updating of user or site tasks
 *
 * @property {string} [siteId]
 * @property {string} [userId]
 * @property {object} [task]
 * @fires {task-created} Fired when the task has been created. The detail is the new/updated task.
 */
export class SakaiTasksCreateTask extends SakaiDialogContent {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      userId: { attribute: "user-id", type: String },
      task: { type: Object },
      i18n: { attribute: false, type: Object },
      description: { attribute: false, type: String },
      error: { attribute: false, type: Boolean },
      deliverTasks: { attribute: "deliver-tasks", type: Boolean },
      assignationType: { attribute: false, type: String },
      selectedGroups: { attribute: false, type: Array },
      groups: { type: Array },
      mode: { attribute: false, type: String },
      siteIdBackup: { attribute: false, type: String },
    };
  }

  constructor() {

    super();

    this.deliverTasks = false;
    this.defaultTask = { taskId: "", description: "", priority: "3", notes: "", due: Date.now(), assignationType: "", selectedGroups: [], siteId: "", owner: "", taskAssignedTo: "", complete: false };
    this.task = { ...this.defaultTask };
    this.assignationType = USER;
    this.mode = "create";
    this.groups = [];
    loadProperties("tasks").then(r => this.i18n = r);
  }

  title() {

    return html`
      ${this.task.taskId == "" ? this.i18n.create_new_task : this.i18n.edit_task}
    `;
  }

  getTaskAssignedTo() {

    let result = this.task.taskAssignedTo;
    if (result != null) {
      result = result.replace("#GROUP#", this.i18n.task_assigned_to_group).replace("#SITE#", this.i18n.task_assigned_to_site).replace("#USER#", this.i18n.task_assigned_to_user);
    }
    return result;
  }

  _handleGroupsSelected(e) {
    this.task.selectedGroups = e.detail.groups;
  }

  addSelectedGroups() {

    if (this.selectedGroups != null) {
      this.task.selectedGroups = [ ...this.selectedGroups ].map(sg => sg.value);
    }
  }

  save() {

    this.task.description = this.shadowRoot.getElementById("description").value;
    this.task.notes = this.getEditor().getContent();
    this.task.assignationType = this.assignationType;
    this.task.siteId = this.siteId;
    this.task.userId = this.userId;
    this.task.owner = this.task.owner || this.userId;
    this.addSelectedGroups();
    const url = `/api/tasks${this.task.taskId ? `/${this.task.taskId}` : ""}`;
    fetch(url, {
      credentials: "include",
      method: this.task.taskId ? "PUT" : "POST",
      cache: "no-cache",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(this.task),
    })
    .then(r => {

      if (r.ok) {
        this.error = false;
        return r.json();
      }
      this.error = true;
      throw new Error(`Network error while saving task: ${r.status}`);
    })
    .then(savedTask => {

      this.task = savedTask;
      this.dispatchEvent(new CustomEvent("task-created", { detail: { task: this.task }, bubbles: true }));
      this.reset();
      this.close();
    })
    .catch(error => console.error(error));
  }

  cancel() {

    this.reset();
    this.close();
  }

  resetDate() {

    this.task.due = Date.now();
    const el = this.shadowRoot.getElementById("due");
    el && (el.epochMillis = this.task.due);
  }

  set task(value) {

    const old = this._task;
    this._task = value;

    this._backupTask = value;

    this.error = false;
    if (!this.siteId && value.siteId) {
      this.siteId = value.siteId;
    }

    this.requestUpdate("task", old);
  }

  get task() {
    return this._task;
  }

  shouldUpdate(changed) {
    return this.task && this.i18n && super.shouldUpdate(changed);
  }

  firstUpdated() {

    const datePicker = this.shadowRoot.getElementById("due");
    const disableFields = (this.task.owner !== this.userId && this.mode === "edit");
    if (this.task.system) {
      datePicker.disabled = true;
    } else {
      datePicker.disabled = false;
      datePicker.epochMillis = this.task.due;
    }
    const descriptionEl = this.shadowRoot.getElementById("description");
    descriptionEl.disabled = this.task.system;
    descriptionEl.value = this.task.description;
    this.shadowRoot.getElementById("priority").value = this.task.priority;
    const editor = this.getEditor();
    if (editor) {
      editor.updateComplete.then(() => {

        editor.setContent(this.task.notes);
        editor.isReadOnly = this.task.system;
      });
    }
    if (disableFields) {
      descriptionEl.disabled = true;
      datePicker.disabled = true;
    }
    const completeEl = this.shadowRoot.getElementById("complete");
    if (completeEl) {
      if (this.task.complete) {
        completeEl.checked = true;
      } else {
        completeEl.checked = false;
      }
    }
  }

  getEditor() {
    return this.shadowRoot.querySelector("sakai-editor");
  }

  reset() {

    this.getEditor().clear();
    const descriptionEl = this.shadowRoot.getElementById("description");
    const datePicker = this.shadowRoot.getElementById("due");
    const completeEl = this.shadowRoot.getElementById("complete");
    completeEl && (completeEl.checked = false);
    datePicker.disabled = false;
    descriptionEl.disabled = false;

    if (this._backupTask) {
      this.task = { ...this._backupTask };
    } else {
      this.task = { ...this.defaultTask };
    }

    this.assignationType = USER;
    this.mode = "create";
    this.siteId = this.siteIdBackup;
  }

  complete(e) {

    this.task.complete = e.target.checked;
    if (e.target.checked) {
      this.task.softDeleted = false;
    }
  }

  existGroups() {
    return Array.isArray(this.groups) && this.groups.length > 0;
  }

  content() {

    return html` 
      ${this.deliverTasks ? html`
      <div class="label" style="margin-bottom:15px;">
        <label>${this.getTaskAssignedTo()}</label>
      </div>
      ` : "" }
      <div class="label">
        <label for="description">${this.i18n.description}</label>
      </div>
      <div class="input">
        <input type="text" id="description" size="50" maxlength="150" .value=${this.task.description}>
      </div>
      <div id="due-and-priority-block">
        <div id="due-block">
          <div class="label">
            <label for="due">${this.i18n.due}</label>
          </div>
          <div class="input">
            <sakai-date-picker id="due"
                @datetime-selected=${e => { this.task.due = e.detail.epochMillis; this.dueUpdated = true; }}
                epoch-millis=${this.task.due}
                label="${this.i18n.due}">
            </sakai-date-picker>
          </div>
        </div>
        <div id="spacer"></div>
        <div id="priority-block">
          <div class="label">
            <label for="priority">${this.i18n.priority}</label>
          </div>
          <div class="input">
            <select id="priority" @change=${(e) => this.task.priority = e.target.value} .value=${this.task.priority}>
              <option value="5">${this.i18n.high}</option>
              <option value="4">${this.i18n.quite_high}</option>
              <option value="3">${this.i18n.medium}</option>
              <option value="2">${this.i18n.quite_low}</option>
              <option value="1">${this.i18n.low}</option>
            </select>
          </div>
        </div>
      </div>
      ${this.task.taskId != "" ? html`
        <div id="complete-block">
          <div>
            <label for="complete">${this.i18n.completed}</label>
            <input
              type="checkbox"
              id="complete"
              title="${this.i18n.complete_tooltip}"
              @click=${this.complete}
              ?checked=${this.task.complete}>
          </div>
        </div>
      ` : "" }
      <div class="label">
        <label for="text">${this.i18n.text}</label>
      </div>
      <div class="input">
        <sakai-editor element-id="task-text-editor" .content="${this.task.notes}" textarea></sakai-editor>
      </div>
      ${this.deliverTasks && this.task.taskId === "" ? html`
        <div class="label">
          <label for="description">${this.i18n.deliver_task}</label>
        </div>
        <div id="create-task-block">
          <div>
            <input type="radio"
                id="task-current-user"
                name="deliver-task"
                title="${this.i18n.deliver_my_dashboard}"
                value="user"
                @click=${() => this.assignationType = USER}
                ?checked=${this.assignationType === USER} >
            <label for="task-current-user">${this.i18n.deliver_my_dashboard}</label>
          </div>
          <div>
            <input type="radio"
                id="task-students"
                name="deliver-task"
                title="${this.i18n.deliver_site}"
                value="site"
                @click=${() => this.assignationType = SITE}
                ?checked=${this.assignationType === SITE}>
            <label for="task-students">${this.i18n.deliver_site}</label>
          </div>
          ${this.existGroups() ? html`
          <div class="d-inline">
            <input type="radio"
               id="task-groups"
               name="deliver-task"
               title="${this.i18n.deliver_group}"
               value="group"
               @click=${() => this.assignationType = GROUP}
               ?checked=${this.assignationType === GROUP}>
            <label for="task-groups">${this.i18n.deliver_group}</label>
          </div>
          ` : "" }
          ${this.existGroups() ? html`
          <div style="margin-left:20px; margin-top:5px;">
            <sakai-group-picker site-id="${this.siteId}" .groups=${this.groups} .selected-groups=${this.selectedGroups} @groups-selected=${this._handleGroupsSelected}></sakai-group-picker>
          </div>
          ` : "" }
        </div>
      ` : "" }
      ${this.error ? html`<div id="error">${this.i18n.save_failed}</div>` : "" }
    `;
  }

  buttons() {

    return html`
      <sakai-button @click=${this.save} primary>${this.task.taskId == "" ? this.i18n.add : this.i18n.save}</sakai-button>
    `;
  }

  static get styles() {

    return [ SakaiDialogContent.styles,
      css`
        #due-and-priority-block {
          display: flex;
          justify-content: space-between;
        }
          #due-block {
            flex: 1;
          }
          #spacer {
            flex: 2;
          }
          #priority-block {
            flex: 1;
          }
        #complete-block {
          margin-bottom: 10px;
        }
          #complete-block input {
            margin-left: 10px;
          }
      #error {
        font-weight: bold;
        color: var(--sakai-tasks-save-failed-color, red)
      }
      #group {
        width: 100%;
      }
      sakai-editor {
        width: 100%;
      }
      .global-overlays {
        z-index: 1200;
      }
    ` ];
  }
}

const tagName = "sakai-tasks-create-task";
!customElements.get(tagName) && customElements.define(tagName, SakaiTasksCreateTask);
