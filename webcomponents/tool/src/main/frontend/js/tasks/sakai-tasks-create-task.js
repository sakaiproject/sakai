import { css, html } from "../assets/lit-element/lit-element.js";
import { loadProperties } from "../sakai-i18n.js";
import { SakaiDialogContent } from "../sakai-dialog-content.js";
import "../sakai-date-picker.js";
import "../sakai-icon.js";
import "../sakai-editor.js";
/* This was added before the introduction of the sakai-date-picker changes in SAK-46998, remove of the date picker changes work fine.*/
/*import moment from "../assets/moment/dist/moment.js";*/
export class SakaiTasksCreateTask extends SakaiDialogContent {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      userId: { attribute: "user-id", type: String },
      i18n: Object,
      task: {type: Object},
      description: String,
      error: { type: Boolean },
      deliverTasks: {type: Boolean},
      assignationType: {type: String},
      selectedGroups: {type: HTMLCollection},
      optionsGroup: {type: Array},
      mode: {type: String},
      siteIdBackup: {type: String}
    };
  }

  constructor() {
    super();
    this.deliverTasks = false;
    this.defaultTask = { taskId: "", description: "", priority: "3", notes: "", due: "", assignationType: "", selectedGroups: [], siteId: "", owner: "", taskAssignedTo: "", complete: null };
    this.task = { ...this.defaultTask};
    this.assignationType = "user";
    this.mode = "create";
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
      result = result.replace('#GROUP#', this.i18n.task_assigned_to_group).replace('#SITE#', this.i18n.task_assigned_to_site).replace('#USER#', this.i18n.task_assigned_to_user);
    }
    return result;
  }

  addSelectedGroups() {
    if (this.selectedGroups != null) {
      const arr = [];
      for (let x = 0; x < this.selectedGroups.length; x++) {
        arr.push(this.selectedGroups[x].value);
      }
      this.task.selectedGroups = arr;
    }
  }

  save() {
    this.task.description = this.shadowRoot.getElementById("description").value;
    this.task.notes = this.getEditor().getContent();
    this.task.assignationType = this.assignationType;
    this.task.siteId = this.siteId;
    this.task.userId = this.userId;
    if (this.task.owner === null || this.task.owner === '') {
      this.task.owner = this.userId;
    }
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
      throw new Error("Network error while saving task");
    })
    .then(savedTask => {

      this.task = savedTask;
      this.dispatchEvent(new CustomEvent("task-created", {detail: { task: this.task }, bubbles: true }));
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
    if (el) {
      el.epochMillis = this.task.due;
    }
  }

  set task(value) {

    const old = this._task;
    this._task = value;

    this.error = false;
    if (!this.siteId && value.siteId) {
      this.siteId = value.siteId;
    }
    this.requestUpdate("task", old);
    this.updateComplete.then(() => {

      const datePicker = this.shadowRoot.getElementById("due");
      const disableFields = (this.task.owner !== this.userId && this.mode === "edit");
      if (value.system) {
        datePicker.disabled = true;
      } else {
        datePicker.disabled = false;
        datePicker.epochMillis = value.due;
      }
      const descriptionEl = this.shadowRoot.getElementById("description");
      descriptionEl.disabled = value.system;
      descriptionEl.value = value.description;
      this.shadowRoot.getElementById("priority").value = value.priority;
      const editor = this.getEditor();
      if (editor) {
        editor.setContent(value.notes);
        editor.isReadOnly = value.system;
      }
      if (disableFields) {
        descriptionEl.disabled = true;
        datePicker.disabled = true;
      }
      const completeEl = this.shadowRoot.getElementById("complete");
      if (completeEl) {
        if (value.complete) {
          completeEl.checked = true;
        } else {
          completeEl.checked = false;
        }
      }
    });
  }

  get task() {
    return this._task;
  }

  shouldUpdate(changed) {
    return this.task && this.i18n && super.shouldUpdate(changed);
  }

  connectedCallback() {

    super.connectedCallback();
    this.deliverTasks = false;
    this.siteIdBackup = this.siteId;
    // Check user role - Only instructors can deliver tasks to students
    if (this.siteId && this.userId) {
      const url = `/api/sites/{siteId}/users/current/isSiteUpdater`;
      fetch(url)
      .then(r => {

        if (r.ok) {
          return r;
        }
        throw new Error(`Failed to get user role from ${url}`);
      })
      .then(data => {

        this.deliverTasks = data;
        // Retrieve group list from site
        if (this.deliverTasks && this.siteId) {
          fetch(`/api/tasks/site/groups/${this.siteId}`)
            .then((r) => {

              if (r.ok) {
                return r.json();
              }
              throw new Error(`Failed to get site group list from ${url}`);
            })
            .then(groups => this.optionsGroup = groups)
            .catch (error => console.error(error));
        }
      })
      .catch (error => console.error(error));
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
    datePicker.disabled = false;
    descriptionEl.disabled = false;
    if (completeEl) { completeEl.checked = false; }
    this.task = { ...this.defaultTask};
    this.assignationType = "user";
    this.mode = "create";
    this.siteId = this.siteIdBackup;
  }

  complete(e) {
    this.task.complete = e.target.checked;
    if (e.target.checked) {
      this.task.softDeleted = false;
    }
  }

  groupComboList() {
    return html`
        <select multiple="multiple" name="${this.i18n.groups}" id="group" style="width:100%;" @change=${(e) => this.selectedGroups = e.target.selectedOptions} .value=${this.selectedGroups} ?disabled=${this.assignationType !== 'group'}>
            ${this.optionsGroup.map((option) => html`
                <option value="${Object.keys(option)[0]}" ?selected=${this.selected === Object.keys(option)[0]}>${Object.values(option)[0]}</option>
            `)}
        </select>
    `;
  }

  existGroups() {
    let result = false;
    if (Array.isArray(this.optionsGroup) && this.optionsGroup.length > 0) { result = true; }
    return result;
  }

  content() {

    /* This was added before the introduction of the sakai-date-picker changes in SAK-46998, remove of the date picker changes work fine.*/
    /*
    let dueDate = null;
    if (this.task.due !== null || this.task.due !== '') {
      dueDate = moment(new Date(this.task.due)).format('YYYY-MM-DDTHH:mm:ss');
    }*/

    return html` 
      ${this.deliverTasks ? html`
      <div class="label" style="margin-bottom:15px;">
        <label>${this.getTaskAssignedTo()}</label>
      </div>
      ` : ""}
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
                @datetime-selected=${(e) => { this.task.due = e.detail.epochMillis; this.dueUpdated = true; }}
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
      ` : ""}
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
                 @click=${() => this.assignationType = 'user'}
                 ?checked=${this.assignationType === 'user'} >
             <label for="task-current-user">${this.i18n.deliver_my_dashboard}</label>
          </div>
          <div>
            <input type="radio"
                id="task-students"
                name="deliver-task"
                title="${this.i18n.deliver_site}"
                value="site"
                @click=${() => this.assignationType = 'site'}
                ?checked=${this.assignationType === 'site'}>
            <label for="task-students">${this.i18n.deliver_site}</label>
          </div>
          <div style="display:${this.existGroups() ? 'inline' : 'none'}">
            <input type="radio"
                id="task-groups"
                name="deliver-task"
                title="${this.i18n.deliver_group}"
                value="group"
                @click=${() => this.assignationType = 'group'}
                ?checked=${this.assignationType === 'group'}>
            <label for="task-groups">${this.i18n.deliver_group}</label>
          </div>
          <div style="display:${this.existGroups() ? 'block' : 'none'}; margin-left:20px; margin-top:5px;">
            ${this.groupComboList()}
          </div>
        </div>
      ` : ""}
      ${this.error ? html`<div id="error">${this.i18n.save_failed}</div>` : ""}
    `;
  }

  buttons() {

    return html`
      <sakai-button @click=${this.save} primary>${this.task.taskId == "" ? this.i18n.add : this.i18n.save}</sakai-button>
    `;
  }

  static get styles() {

    return [SakaiDialogContent.styles,
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
      sakai-editor {
        width: 100%;
      }
    `];
  }
}

const tagName = "sakai-tasks-create-task";
!customElements.get(tagName) && customElements.define(tagName, SakaiTasksCreateTask);
