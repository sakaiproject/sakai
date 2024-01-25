import { css, html } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";
import { SakaiPageableElement } from "@sakai-ui/sakai-pageable-element";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import { sakaiFormatDistance } from "@sakai-ui/sakai-date-fns";
import * as constants from "./sakai-tasks-constants.js";

export class SakaiTasks extends SakaiPageableElement {

  static properties = {

    _currentFilter: { state: true },
    _canAddTask: { state: true },
    _canUpdateSite: { state: true },
    _groups: { state: true },
  };

  constructor() {

    super();

    this.defaultTask = { taskId: "", description: "", priority: "3", notes: "", due: Date.now(), assignationType: "", selectedGroups: [], siteId: "", owner: "", taskAssignedTo: "", complete: false };

    this.showPager = true;
    this._canUpdateSite = false;
    this._currentFilter = constants.CURRENT;
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

  decorateTask(t) {

    t.visible = true;
    if (t.due) {
      t.dueHuman = sakaiFormatDistance(new Date(t.due), new Date());
    } else {
      t.dueHuman = "No due date";
    }
    return t;
  }

  async loadAllData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/tasks` : "/api/users/me/tasks";
    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get tasks from ${url}`);

      })
      .then(async response => {

        this.data = response.tasks;
        this._canAddTask = response.canAddTask;

        if (this._canAddTask) {
          await Promise.allSettled([
            import("../sakai-tasks-create-task.js"),
            import("@lion/dialog/define"),
          ]);
        }

        this._canUpdateSite = response.canUpdateSite;
        this._groups = response.groups;
        this.filter(constants.CURRENT);
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

  // Override the method to apply custom filtering, used within the repage() method of SakaiPageableElement
  getFilteredDataBeforeRepaging() {
    return this.data.filter(t => t.visible);
  }

  filter(f) {

    this._currentFilter = f;

    switch (f) {
      case constants.PRIORITY_5:
        this.data.forEach(t => t.visible = !!(!t.softDeleted && !t.complete && t.priority === 5));
        break;
      case constants.PRIORITY_4:
        this.data.forEach(t => t.visible = !!(!t.softDeleted && t.priority === 4));
        break;
      case constants.PRIORITY_3:
        this.data.forEach(t => t.visible = !!(!t.softDeleted && !t.complete && t.priority === 3));
        break;
      case constants.PRIORITY_2:
        this.data.forEach(t => t.visible = !!(!t.softDeleted && !t.complete && t.priority === 2));
        break;
      case constants.PRIORITY_1:
        this.data.forEach(t => t.visible = !!(!t.softDeleted && !t.complete && t.priority === 1));
        break;
      case constants.OVERDUE:
        this.data.forEach(t => t.visible = !t.complete && t.due && (t.due < Date.now()));
        break;
      case constants.TRASH:
        this.data.forEach(t => t.visible = t.softDeleted);
        break;
      case constants.COMPLETE:
        this.data.forEach(t => t.visible = t.complete);
        break;
      default:
        this.data.forEach(t => t.visible = !t.softDeleted && !t.complete);
        break;
    }
    this.repage();
  }

  filterChanged(e) {

    this.currentPage = 1;
    const sakaiPager = this.shadowRoot.querySelector("#pager sakai-pager");
    sakaiPager && (sakaiPager.current = this.currentPage);
    this.filter(e.target.value);
  }

  editTask(e) {

    e.stopPropagation();

    const editDialog = this.shadowRoot.getElementById("add-edit-dialog");
    editDialog.__toggle();
    const taskId = e.currentTarget.dataset.taskId;
    if (taskId) {
      editDialog._overlayContentNode.task = { ...this.data.find(t => t.taskId == taskId) };
      editDialog._overlayContentNode.mode = "edit";
    } else {
      editDialog._overlayContentNode.mode = "create";
    }
  }

  deleteTask(e) {

    if (!confirm(`${this.i18n.alert_want_to_delete}`)) {
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
            this.filter(constants.CURRENT);
          } else {
            this.requestUpdate();
            this.repage();
          }
        } else {
          throw new Error(`Failed to delete task at ${url}`);
        }
      })
      .catch(error => console.error(error));
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
          this.repage();
        } else {
          throw new Error(`Failed to soft delete task at ${url}`);
        }
      })
      .catch(error => console.error(error));
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
            this.filter(constants.TRASH);
          } else {
            this.filter(constants.CURRENT);
          }
        } else {
          throw new Error(`Failed to soft delete task at ${url}`);
        }
      })
      .catch(error => console.error(error));
  }

  taskCreated(e) {

    const existingIndex = this.data.findIndex(t => t.taskId == e.detail.task.taskId);

    if (existingIndex === -1) {
      this.data.push(this.decorateTask(e.detail.task));
    } else {
      this.data.splice(existingIndex, 1, this.decorateTask(e.detail.task));
    }

    this.filter(constants.CURRENT);
    this._currentFilter = constants.CURRENT;
    this.repage();
  }

  shouldUpdate() {
    return this.i18n && this.dataPage;
  }

  content() {

    console.debug("SakaiTasks.content()");

    return html`

      ${this._canAddTask ? html`
      <div id="add-block">
        <lion-dialog id="add-edit-dialog">

          <sakai-tasks-create-task class="dialog-content"
            id="create-task"
            slot="content"
            site-id="${this.siteId}"
            user-id="${this.userId}"
            @task-created=${this.taskCreated}
            @soft-deleted=${this.softDeleteTask}
            .groups=${this._groups}
            ?deliver-tasks=${this._canUpdateSite}>
          </sakai-tasks-create-task>

          <div slot="invoker">
            <a href="javascript:;" @click=${this.editTask} title="${this.i18n.add_task}" aria-label="${this.i18n.add_task}">
              <button type="button" @click=${this._addTask} class="add-task-button"><sakai-icon type="add" size="small"></sakai-icon>${this.i18n.add_task}</button>
            </a>
          </div>

        </lion-dialog>
      </div>
      ` : ""}
      <div id="controls">
        <div id="filter">
          <select @change=${this.filterChanged} .value=${this._currentFilter}>
            <option value="current">${this.i18n.filter_current}</option>
            <option value="${constants.PRIORITY_5}">${this.i18n.filter_priority_5}</option>
            <option value="${constants.PRIORITY_4}">${this.i18n.filter_priority_4}</option>
            <option value="${constants.PRIORITY_3}">${this.i18n.filter_priority_3}</option>
            <option value="${constants.PRIORITY_2}">${this.i18n.filter_priority_2}</option>
            <option value="${constants.PRIORITY_1}">${this.i18n.filter_priority_1}</option>
            <option value="${constants.OVERDUE}">${this.i18n.filter_overdue}</option>
            <option value="${constants.TRASH}">${this.i18n.trash}</option>
            <option value="${constants.COMPLETE}">${this.i18n.completed}</option>
          </select>
        </div>
        <div id="sort">
          <select @change=${this.sortChanged}>
            <option value="none">${this.i18n.sort_none}</option>
            <option value="due_latest_first">${this.i18n.sort_due_latest_first}</option>
            <option value="due_earliest_first">${this.i18n.sort_due_earliest_first}</option>
            <option value="priority_lowest_first">${this.i18n.sort_priority_lowest_first}</option>
            <option value="priority_highest_first">${this.i18n.sort_priority_highest_first}</option>
          </select>
        </div>
      </div>
      ${this.dataPage.filter(t => t.visible).length > 0 ? html`
        <div id="tasks">
          <div class="priority-block header">${this.i18n.priority}</div>
          <div class="task-block task-block-header header">${this.i18n.task}</div>
          <div class="link-block header">${this.i18n.options}</div>
        ${this.dataPage.filter(t => t.visible).map((t, i) => html`
          <div class="priority-block priority_${t.priority} cell ${i % 2 === 0 ? "even" : "odd"}">
            <div tabindex="0" title="${this.i18n[`priority_${t.priority}_tooltip`]}" aria-label="${this.i18n[`priority_${t.priority}_tooltip`]}">
              <sakai-icon size="small" type="priority"></sakai-icon>
            </div>
          </div>
          <div class="task-block cell ${i % 2 === 0 ? "even" : "odd"}">
            <div class="site-title">${t.siteTitle}</div>
            <div class="description">${t.description}</div>
            <div class="due-date"><span class="due">${this.i18n.due} </span>${t.dueHuman}</div>
            ${t.notes ? html`
              <div class="task-text-toggle">
                <a href="javascript:;"
                    @click=${() => { t.textVisible = !t.textVisible; this.requestUpdate(); }}
                    title="${t.textVisible ? this.i18n.show_less : this.i18n.show_more}"
                    arial-label="${t.textVisible ? this.i18n.show_less : this.i18n.show_more}">
                  ${t.textVisible ? this.i18n.less : this.i18n.more}
                </a>
              </div>
              <div class="task-text" style="${t.textVisible ? "" : "display: none"}">${unsafeHTML(t.notes)}</div>
            ` : ""}
          </div>
          <div class="link-block cell ${i % 2 === 0 ? "even" : "odd"}">
            ${!t.system ? html`
            <div class="edit">
              <a href="javascript:;"
                  data-task-id="${t.taskId}"
                  @click=${this.editTask}
                  title="${this.i18n.edit}"
                  aria-label="${this.i18n.edit}">
                <sakai-icon type="edit" size="small"></sakai-icon>
              </a>
            </div>
              ${t.softDeleted ? html`
                <div class="delete">
                  <a href="javascript:;"
                      data-task-id="${t.userTaskId}"
                      @click=${this.deleteTask}
                      title="${this.i18n.hard_delete}"
                      aria-label="${this.i18n.hard_delete}">
                    <sakai-icon type="delete" size="small"></sakai-icon>
                  </a>
                </div>
                <div class="restore">
                  <a href="javascript:;"
                      data-task-id="${t.taskId}"
                      @click=${this.restoreTask}
                      title="${this.i18n.restore}"
                      aria-label="${this.i18n.restore}">
                    <sakai-icon type="restore" size="small"></sakai-icon>
                  </a>
                </div>
              ` : html`
                <div class="delete">
                  <a href="javascript:;"
                      data-task-id="${t.taskId}"
                      @click=${this.softDeleteTask}
                      title="${this.i18n.soft_delete}"
                      aria-label="${this.i18n.soft_delete}">
                    <sakai-icon type="delete" size="small"></sakai-icon>
                  </a>
                </div>
              `}
            ` : ""}
            <div>
            ${t.url ? html`
              <div>
                <a href="${t.url}"
                    title="${this.i18n.task_url}"
                    aria-label="${this.i18n.task_url}">
                  <sakai-icon type="right" size="small"></sakai-icon>
                </a>
              </div>
            ` : "" }
            </div>
          </div>
        `)}
        </div>
      ` : html`<div>${this.i18n.no_tasks}</div>`
      }
    `;
  }

  static styles = [
    SakaiPageableElement.styles,
    css`
    a {
      color: var(--link-color);
    }
    .global-overlays {
      z-index: 1200;
    }
      #add-block {
        text-align: right;
        margin-top: 8px;
        margin-bottom: 10px;
      }
        sakai-icon[type="add"] {
          padding: 3px 3px 2px 0;
          vertical-align: middle;
          color: var(--button-primary-text-color);
        }
        .add-task-button {
          margin-left: 1px;
          background-color: var(--button-primary-background);
          color: var(--button-primary-text-color);
          border: 0px solid var(--button-primary-border-color);
          border-radius: 2px;
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

          .demo-box-placements lion-tooltip {
            margin: 20px;
          }
    `,
  ];
}
