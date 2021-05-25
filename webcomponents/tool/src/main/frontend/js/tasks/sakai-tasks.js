import { css, html } from "../assets/lit-element/lit-element.js";
import {unsafeHTML} from '../assets/lit-html/directives/unsafe-html.js';
import { SakaiPageableElement } from '../sakai-pageable-element.js';
import '../sakai-icon.js';
import moment from "../assets/moment/dist/moment.js";
import "../assets/@lion/dialog/lion-dialog.js";
import "./sakai-tasks-create-task.js";
import "../sakai-editor.js";

export class SakaiTasks extends SakaiPageableElement {

  static get properties() {

    return {
      taskBeingEdited: { type: Object},
      currentFilter: String,
    };
  }

  constructor() {

    super();

    this.showPager = true;
    this.currentFilter = "current";
    this.loadTranslations("tasks").then(r => this.i18n = r);
  }

  set data(value) {

    const old = this._data;
    this._data = value;

    // Show highest priority tasks first
    this._data.sort((t1, t2) => t2.priority - t1.priority);

    this._data.forEach(t => this.decorateTask(t));

    this.requestUpdate("data", old);
  }

  get data() { return this._data; }

  set siteId(value) {
    this._siteId = value;
  }

  decorateTask(t) {

    t.visible = true;
    if (t.due) {
      t.dueHuman = moment.duration(t.due - Date.now(), "milliseconds").humanize(true);
    } else {
      t.dueHuman = "No due date";
    }
    return t;
  }

  async loadAllData() {

    const url = "/api/tasks";
    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        } else {
          throw new Error(`Failed to get tasks from ${url}`);
        }
      })
      .then(data => {

        this.data = data;
        this.filter("current");
      })
      .catch (error => console.error(error));
  }

  sortChanged(e) {

    switch (e.target.value) {
      case "due_latest_first":
        this.data.sort((t1, t2) => t2.due - t1.due);
        break;
      case "due_earliest_first":
        this.data.sort((t1, t2) => t1.due - t2.due);
        break;
      case "priority_lowest_first":
        this.data.sort((t1, t2) => t1.priority - t2.priority);
        break;
      case "priority_highest_first":
        this.data.sort((t1, t2) => t2.priority - t1.priority);
        break;
      default:
        break;
    }
    this.repage();
  }

  filter(f) {

    this.currentFilter = f;

    switch (f) {
      case "priority_5":
        this.data.forEach(t => t.visible = !t.softDeleted && !t.complete && t.priority === 5 ? true : false);
        break;
      case "priority_4":
        this.data.forEach(t => t.visible = !t.softDeleted && t.priority === 4 ? true : false);
        break;
      case "priority_3":
        this.data.forEach(t => t.visible = !t.softDeleted && !t.complete && t.priority === 3 ? true : false);
        break;
      case "priority_2":
        this.data.forEach(t => t.visible = !t.softDeleted && !t.complete && t.priority === 2 ? true : false);
        break;
      case "priority_1":
        this.data.forEach(t => t.visible = !t.softDeleted && !t.complete && t.priority === 1 ? true : false);
        break;
      case "overdue":
        this.data.forEach(t => t.visible =  !t.complete && t.due && (t.due < Date.now() ? true : false));
        break;
      case "trash":
        this.data.forEach(t => t.visible = t.softDeleted);
        break;
      case "complete":
        this.data.forEach(t => t.visible = t.complete);
        break;
      default:
        this.data.forEach(t => t.visible = !t.softDeleted && !t.complete);
        break;
    }
    this.repage();
  }

  filterChanged(e) {
    this.filter(e.target.value);
  }

  add() {
    this.shadowRoot.getElementById("add-edit-dialog")._overlayContentNode.reset();
  }

  editTask(e) {

    const task = this.data.find(t => t.taskId == e.currentTarget.dataset.taskId);
    this.shadowRoot.getElementById("add-edit-dialog").__toggle();
    this.shadowRoot.getElementById("add-edit-dialog")._overlayContentNode.task = task;
  }

  deleteTask(e) {

    if (!confirm("Are you sure you want to delete this task?")) {
      return false;
    }

    const taskId = e.currentTarget.dataset.taskId;

    const url = `/api/tasks/${taskId}`;
    fetch(url, {
      credentials: "include",
      method: "DELETE",
    })
      .then(r => {

        if (r.ok) {
          this.data.splice(this.data.findIndex(t => t.userTaskId == taskId), 1);
          if (this.data.filter(t => t.softDeleted).length == 0) {
            this.filter("current");
          } else {
            this.requestUpdate();
          }
        } else {
          throw new Error(`Failed to delete task at ${url}`);
        }
      })
      .catch(error => {
        console.error(error);
      });
  }

  softDeleteTask(e) {

    const task = this.data.find(t => t.taskId == e.currentTarget.dataset.taskId);

    task.softDeleted = true;
    task.visible = false;
    const url = `/api/tasks/${task.taskId}`;
    fetch(url, {
      credentials: "include",
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(task),
    })
      .then(r => {

        if (r.ok) {
          this.requestUpdate();
        } else {
          throw new Error(`Failed to soft delete task at ${url}`);
        }
      })
      .catch(error => {
        console.error(error);
      });
  }

  restoreTask(e) {

    const task = this.data.find(t => t.taskId == e.currentTarget.dataset.taskId);

    task.softDeleted = false;
    task.visible = true;
    const url = `/api/tasks/${task.taskId}`;
    fetch(url, {
      credentials: "include",
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(task),
    })
      .then(r => {

        if (r.ok) {
          if (this.data.filter(t => t.softDeleted).length > 0) {
            this.filter("trash");
          } else {
            this.filter("current");
          }
        } else {
          throw new Error(`Failed to soft delete task at ${url}`);
        }
      })
      .catch(error => {
        console.error(error);
      });
  }

  taskCreated(e) {

    const existingIndex = this.data.findIndex(t => t.taskId == e.detail.task.taskId);

    if (existingIndex === -1) {
      this.data.push(this.decorateTask(e.detail.task));
    } else {
      this.data.splice(existingIndex, 1, this.decorateTask(e.detail.task));
    }

    this.filter("current");
    this.repage();
  }

  shouldUpdate() {
    return this.i18n && this.dataPage;
  }

  content() {

    return html`

      <div id="add-block">
        <lion-dialog id="add-edit-dialog">

          <sakai-tasks-create-task class="dialog-content"
            id="create-task"
            slot="content"
            @task-created=${this.taskCreated}
            @soft-deleted=${this.softDeleteTask}>

            <div slot="task-text">
              <sakai-editor element-id="task-text-editor" toolbar="basic" delay></sakai-editor>
            </div>

          </sakai-tasks-create-task>

          <div slot="invoker"><a @click=${this.add} href="javascript:;" title="${this.i18n["add_task"]}" aria-label="${this.i18n["add_task"]}"><sakai-icon type="add" size="small"></a></div>

        </lion-dialog>
      </div>
      <div id="controls">
        <div id="filter">
          <select @change=${this.filterChanged} .value=${this.currentFilter}>
            <option value="current">${this.i18n["filter_current"]}</option>
            <option value="priority_5">${this.i18n["filter_priority_5"]}</option>
            <option value="priority_4">${this.i18n["filter_priority_4"]}</option>
            <option value="priority_3">${this.i18n["filter_priority_3"]}</option>
            <option value="priority_2">${this.i18n["filter_priority_2"]}</option>
            <option value="priority_1">${this.i18n["filter_priority_1"]}</option>
            <option value="overdue">${this.i18n["filter_overdue"]}</option>
            <option value="trash">${this.i18n["trash"]}</option>
            <option value="complete">${this.i18n["completed"]}</option>
          </select>
        </div>
        <div id="sort">
          <select @change=${this.sortChanged}>
            <option value="none">${this.i18n["sort_none"]}</option>
            <option value="due_latest_first">${this.i18n["sort_due_latest_first"]}</option>
            <option value="due_earliest_first">${this.i18n["sort_due_earliest_first"]}</option>
            <option value="priority_lowest_first">${this.i18n["sort_priority_lowest_first"]}</option>
            <option value="priority_highest_first">${this.i18n["sort_priority_highest_first"]}</option>
          </select>
        </div>
      </div>
      ${this.dataPage.filter(t => t.visible).length > 0 ? html`
        <div id="tasks">
          <div class="priority-block header">${this.i18n["priority"]}</div>
          <div class="task-block task-block-header header">${this.i18n["task"]}</div>
          <div class="link-block header">${this.i18n["options"]}</div>
        ${this.dataPage.filter(t => t.visible).map((t, i) => html`
          <div class="priority-block priority_${t.priority} cell ${i % 2 === 0 ? "even" : "odd"}">
            <div tabindex="0" title="${this.i18n[`priority_${t.priority}_tooltip`]}" aria-label="${this.i18n[`priority_${t.priority}_tooltip`]}">
              <sakai-icon size="small" type="priority">
            </div>
          </div>
          <div class="task-block cell ${i % 2 === 0 ? "even" : "odd"}">
            <div class="site-title">${t.siteTitle}</div>
            <div class="description">${t.description}</div>
            <div class="due-date"><span class="due">${this.i18n["due"]} </span>${t.dueHuman}</div>
            ${t.notes ? html`
              <div class="task-text-toggle">
                <a href="javascript:;"
                    @click=${() => { t.textVisible = !t.textVisible; this.requestUpdate(); }}
                    title="${t.textVisible ? this.i18n["show_less"] : this.i18n["show_more"]}"
                    arial-label="${t.textVisible ? this.i18n["show_less"] : this.i18n["show_more"]}">
                  ${t.textVisible ? this.i18n["less"] : this.i18n["more"]}
                </a>
              </div>
              <div class="task-text" style="${t.textVisible ? "" : "display: none"}">${unsafeHTML(t.notes)}</div>
            ` : ""}
          </div>
          <div class="link-block cell ${i % 2 === 0 ? "even" : "odd"}">
            <div class="edit">
              <a href="javascript:;"
                  data-task-id="${t.taskId}"
                  @click=${this.editTask}
                  title="${this.i18n["edit"]}"
                  aria-label="${this.i18n["edit"]}">
                <sakai-icon type="edit" size="small"></sakai-icon>
              </a>
            </div>
            ${t.softDeleted ? html`
              <div class="delete">
                <a href="javascript:;"
                    data-task-id="${t.userTaskId}"
                    @click=${this.deleteTask}
                    title="${this.i18n["hard_delete"]}"
                    aria-label="${this.i18n["hard_delete"]}">
                  <sakai-icon type="delete" size="small"></sakai-icon>
                </a>
              </div>
              <div class="restore">
                <a href="javascript:;"
                    data-task-id="${t.taskId}"
                    @click=${this.restoreTask}
                    title="${this.i18n["restore"]}"
                    aria-label="${this.i18n["restore"]}">
                  <sakai-icon type="restore" size="small"></sakai-icon>
                </a>
              </div>
            ` : html`
              <div class="delete">
                <a href="javascript:;"
                    data-task-id="${t.taskId}"
                    @click=${this.softDeleteTask}
                    title="${this.i18n["soft_delete"]}"
                    aria-label="${this.i18n["soft_delete"]}">
                  <sakai-icon type="delete" size="small"></sakai-icon>
                </a>
              </div>
            `}
            <div>
              ${t.url ? html`
                <div>
                  <a href="${t.url}"
                      title="${this.i18n["task_url"]}"
                      aria-label="${this.i18n["task_url"]}">
                    <sakai-icon type="right" size="small"></sakai-icon>
                  </a>
                </div>
              ` : "" }
            </div>
          </div>
        `)}
        </div>
      ` : html`<div>${this.i18n["no_tasks"]}</div>`
      }
    `;
  }

  static get styles() {

    return [
      ...super.styles,
      css`
        #add-block {
          text-align: right;
          margin-top: 8px;
          margin-bottom: 10px;
        }
          sakai-icon[type="add"] {
            color: green;
          }

        #controls {
          display: flex;
          margin-bottom: 10px;
        }
          #filter {
            flex: 1;
          }
          #sort {
            flex: 2;
            text-align: right;
          }

        #tasks {
          display: grid;
          grid-template-columns: 0fr 4fr 0fr;
          grid-auto-rows: minmax(10px, auto);
        }
          #tasks > div:nth-child(-n+3) {
            padding-bottom: 14px;
          }
          .header {
            font-weight: bold;
            padding: 0 5px 0 5px;
          }
          .cell {
            padding: 8px;
            font-size: var(--sakai-grades-title-font-size, 12px);
          }
          .even {
            background-color: var(--sakai-table-even-color, #f4f4f4);
          }

          .priority-block {
            flex: 1;
            display: flex;
            align-items: center;
          }
            .priority_5 {
              color: red;
            }
            .priority_4 {
              color: brown;
            }
            .priority_3 {
              color: orange;
            }
            .priority_2 {
              color: yellow;
            }
            .priority_1 {
              color: green;
            }

          .task-block {
            flex: 3 3 0px;
          }
            .site-title {
              font-size: var(--sakai-task-site-title-font-size, 12px);
              margin-bottom: 5px;
            }
            .description {
              font-size: var(--sakai-task-site-title-font-size, 14px);
              margin-bottom: 5px;
            }
            .due-date {
              font-size: var(--sakai-task-site-title-font-size, 12px);
            }
            .due {
              font-weight: var(--sakai-task-due-font-weight, bold);
            }

          .link-block {
            display: flex;
            align-items: center;
            justify-content: flex-end;
          }
            .link-block div {
              margin-right: 8px;
            }

          .task-text {
            margin-left: 20px;
          }

          .task-text-toggle {
            margin-top: 10px;
            margin-bottom: 10px;
          }
            .edit {
              margin-right: 8px;
            }

            .demo-box-placements {
              display: flex;
              flex-direction: column;
              margin: 40px 0 0 200px;
            }

            .demo-box-placements lion-tooltip {
              margin: 20px;
            }
      `,
    ];
  }
}

if (!customElements.get("sakai-tasks")) {
  customElements.define("sakai-tasks", SakaiTasks);
}
