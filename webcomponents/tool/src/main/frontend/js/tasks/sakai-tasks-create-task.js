import { css, html } from "../assets/lit-element/lit-element.js";
import { loadProperties } from "../sakai-i18n.js";
import { SakaiDialogContent } from "../sakai-dialog-content.js";
import "../datepicker/sakai-date-picker.js";
import "../sakai-icon.js";
import "../sakai-editor.js";

export class SakaiTasksCreateTask extends SakaiDialogContent {

  static get properties() {

    return {
      i18n: Object,
      task: {type: Object},
      description: String,
      error: { type: Boolean }
    };
  }

  constructor() {

    super();
    this.defaultTask = { taskId: "", description: "", priority: "3", notes: "", due: Date.now() };
    this.task = Object.assign({}, this.defaultTask);
    loadProperties("tasks").then(r => this.i18n = r);
  }

  title() {

    return html`
      ${this.task.taskId == "" ? this.i18n["create_new_task"] : this.i18n["edit_task"]}
    `;
  }

  save() {

    this.task.description = this.shadowRoot.getElementById("description").value;
    this.task.notes = this.getEditor().getData();

    fetch(`/api/tasks/${this.task.taskId}`, {
      credentials: "include",
      method: "PUT",
      cache: "no-cache",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(this.task),
    })
      .then(r => {

        if (r.ok) {
          this.error = false;
          return r.json();
        } else {
          this.error = true;
          throw new Error();
        }
      })
      .then(savedTask => {

        this.task = savedTask;
        this.dispatchEvent(new CustomEvent("task-created", {detail: { task: this.task }, bubbles: true }));
        this.close();
      })
      .catch(() => {});
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

    this.requestUpdate("task", old);
    this.updateComplete.then(() => {

      const datePicker = this.shadowRoot.getElementById("due");

      if (value.system) {
        datePicker.disable();
      } else {
        datePicker.enable();
        datePicker.epochMillis = value.due;
      }
      const descriptionEl = this.shadowRoot.getElementById("description");
      descriptionEl.disabled = value.system;
      descriptionEl.value = value.description;
      this.shadowRoot.getElementById("priority").value = value.priority;
      const editor = this.getEditor();
      if (editor) {
        editor.setData(value.notes);
        editor.isReadOnly = value.system;
      }
    });
  }

  get task() {
    return this._task;
  }

  shouldUpdate(changed) {
    return this.task && this.i18n && super.shouldUpdate(changed);
  }

  getEditorTag() {

    const el = this.shadowRoot.querySelector("slot[name='task-text'");

    if (el) {
      const slottedNodes = this.shadowRoot.querySelector("slot[name='task-text'").assignedNodes();
      return slottedNodes[0].querySelector("sakai-editor");
    }
  }

  getEditor() {
    return this.getEditorTag().editor;
  }

  reset() {
    this.task = Object.assign({}, this.defaultTask);
  }

  complete(e) {

    this.task.complete = e.target.checked;

    if (e.target.checked) {
      this.task.softDeleted = false;
    }

    console.log(this.task);
  }

  resetEditor() {
    this.getEditor().setData(this.task.notes || "");
  }

  connectedCallback() {

    super.connectedCallback();

    if (typeof CKEDITOR !== "undefined") {
      const tag = this.getEditorTag();
      if (tag) {
        return tag.attachEditor();
      }
    }
  }

  content() {

    return html`

      <div class="label">
        <label for="description">${this.i18n["description"]}</label>
      </div>
      <div class="input"><input type="text" id="description" size="50" maxlength="150" .value=${this.task.description}></div>
      <div id="due-and-priority-block">
        <div id="due-block">
          <div class="label">
            <label for="due">${this.i18n["due"]}</label>
          </div>
          <div class="input">
            <sakai-date-picker id="due" @datetime-selected=${(e) => { this.task.due = e.detail.epochMillis; this.dueUpdated = true; }} epoch-millis=${this.task.due}></sakai-date-picker>
          </div>
        </div>
        <div id="spacer"></div>
        <div id="priority-block">
          <div class="label">
            <label for="priority">${this.i18n["priority"]}</label>
          </div>
          <div class="input">
            <select id="priority" @change=${(e) => this.task.priority = e.target.value} .value=${this.task.priority}>
              <option value="5">${this.i18n["high"]}</option>
              <option value="4">${this.i18n["quite_high"]}</option>
              <option value="3">${this.i18n["medium"]}</option>
              <option value="2">${this.i18n["quite_low"]}</option>
              <option value="1">${this.i18n["low"]}</option>
            </select>
          </div>
        </div>
      </div>
      ${this.task.taskId != "" ? html`
        <div id="complete-block">
          <div>
            <label for="complete">${this.i18n["completed"]}</label>
            <input
              type="checkbox"
              id="complete"
              aria-label="${this.i18n["complete_tooltip"]}"
              title="${this.i18n["complete_tooltip"]}"
              @click=${this.complete}
              ?checked=${this.task.complete}>
          </div>
        </div>
      ` : ""}
      <div class="label">
        <label for="text">${this.i18n["text"]}</label>
      </div>
      <div class="input">
        <slot id="task-text" name="task-text"></slot>
      </div>
      ${this.error ? html`<div id="error">${this.i18n["save_failed"]}</div>` : ""}
    `;
  }

  buttons() {

    return html`
      <sakai-button @click=${this.save} primary>${this.task.taskId == "" ? this.i18n["add"] : this.i18n["save"]}</sakai-button>
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
    `];
  }
}

if (!customElements.get("sakai-tasks-create-task")) {
  customElements.define("sakai-tasks-create-task", SakaiTasksCreateTask);
}
