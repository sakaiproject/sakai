import { html, css, LitElement } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { repeat } from "lit/directives/repeat.js";
import "../sakai-calendar-widget.js";
import "../sakai-tasks-widget.js";
import "../sakai-grades-widget.js";
import "../sakai-announcements-widget.js";
import "../sakai-forums-widget.js";
import "../sakai-widget-picker.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiWidgetPanel extends LitElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    widgetIds: { type: Array },
    layout: { type: Array },
    i18n: Object,
    state: String,
    editing: { type: Boolean },
    _widgets: { state: true },
    columns: { type: Number },
  };

  constructor() {

    super();
    this.columns = 2;
    this.state = "view";
    loadProperties("widgetpanel").then(r => this.i18n = r);
  }

  set widgetIds(value) {

    this._widgetIds = value;
    this._widgets = value.map(v => ({ id: v }));
  }

  get widgetIds() { return this._widgetIds; }

  set editing(value) {

    const old = this._editing;
    this._editing = value;
    if (!value) this.changeState("view");
    this.requestUpdate("editing", old);
  }

  get editing() { return this._editing; }

  set layout(value) {

    this._layout = value;

    if (this.widgetIds) {
      this._layout = this._layout.filter(l => this.widgetIds.includes(l));
    }
  }

  get layout() { return this._layout; }

  shouldUpdate() {
    return this.i18n;
  }

  fireChanged() {
    this.dispatchEvent(new CustomEvent("changed", { bubbles: true, detail: { layout: this.layout } }));
  }

  showWidgetPicker() {

    if (this.state === "add") {
      this.changeState("view");
    } else {
      this.changeState("add");
    }
  }

  stateSelected(e) {
    this.changeState(e.target.value);
  }

  changeState(state) {

    if (this.state === state) {
      return;
    }

    this.state = state;

    if (this.state === "add") {
      this._widgets.forEach(w => w.state = "view");

      // Add in the picker widget
      this.layout.unshift("picker");
      this.requestUpdate();
      this.updateComplete.then(() => {

        // Set up the picker with the current and all widgets
        const picker = this.shadowRoot.getElementById("picker");
        picker.setAttribute("all", JSON.stringify(this.widgetIds));
        picker.setAttribute("current", JSON.stringify(this.layout));
        picker.setAttribute("state", "remove");

        // After a widget's been picked
        picker.addEventListener("widget-picked", e => {

          const pickerIndex = this.layout.findIndex(w => w === "picker");
          this.layout.splice(pickerIndex, 1);
          this.state = "view";

          this.layout.unshift(e.detail.id);
          this.fireChanged();
          this.requestUpdate();
        });
      });
    } else {
      this._widgets.forEach(w => w.state = this.state);
      if (this.layout.indexOf("picker") !== -1) {
        // The picker's currently visible. Remove it.
        this.layout.shift();
      }
      this.requestUpdate();
    }
  }

  removeWidget(e) {

    const i = this.layout.findIndex(w => w === e.target.id);
    if (i !== -1) this.layout.splice(i, 1);
    this.fireChanged();
    this.requestUpdate();

    if (e.detail && e.detail.newState) {
      this.updateComplete.then(() => this.changeState(e.detail.newState));
    } else {
      this.updateComplete.then(() => this.changeState("remove"));
    }
  }

  moveWidget(e) {

    const currentIndex = this.layout.findIndex(w => w === e.detail.widgetId);
    const tmpWidgetId = this.layout[currentIndex];

    switch (e.detail.direction) {
      case "left":
        this.layout[currentIndex] = this.layout[currentIndex - 1];
        this.layout[currentIndex - 1] = tmpWidgetId;
        break;
      case "right":
        this.layout[currentIndex] = this.layout[currentIndex + 1];
        this.layout[currentIndex + 1] = tmpWidgetId;
        break;
      case "up":
        if (this.columns === 1) {
          this.layout[currentIndex] = this.layout[currentIndex - 1];
          this.layout[currentIndex - 1] = tmpWidgetId;
        } else {
          this.layout[currentIndex] = this.layout[currentIndex - this.columns];
          this.layout[currentIndex - this.columns] = tmpWidgetId;
        }
        break;
      case "down":
        if (this.columns === 1) {
          this.layout[currentIndex] = this.layout[currentIndex + 1];
          this.layout[currentIndex + 1] = tmpWidgetId;
        } else {
          this.layout[currentIndex] = this.layout[currentIndex + this.columns];
          this.layout[currentIndex + this.columns] = tmpWidgetId;
        }
        break;
      default:
    }
    this.requestUpdate();
    this.fireChanged();
  }

  getWidget(r) {

    const w = this._widgets.find(widget => widget.id === r);

    switch (r) {
      case "tasks":
        return html`
          <div class="${this.state === "add" ? "faded" : ""}">
            <sakai-tasks-widget
              id="${r}"
              site-id="${ifDefined(this.siteId ? this.siteId : "")}"
              user-id="${ifDefined(this.userId ? this.userId : "")}"
              class="widget"
              state="${w.state}"
              @remove=${this.removeWidget}
              @move=${this.moveWidget}
              ?editing=${this.editing}>
            </sakai-tasks-widget>
          </div>
        `;
      case "grades":
        return html`
          <div class="${this.state === "add" ? "faded" : ""}">
            <sakai-grades-widget
              id="${r}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              class="widget"
              state="${w.state}"
              @remove=${this.removeWidget}
              @move=${this.moveWidget}
              ?editing=${this.editing}>
            </sakai-grades-widget>
          </div>
        `;
      case "announcements":
        return html`
          <div class="${this.state === "add" ? "faded" : ""}">
            <sakai-announcements-widget
              id="${r}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              class="widget"
              state="${w.state}"
              @remove=${this.removeWidget}
              @move=${this.moveWidget}
              ?editing=${this.editing}>
            </sakai-announcements-widget>
          </div>
        `;
      case "calendar":
        return html`
          <div class="${this.state === "add" ? "faded" : ""}">
            <sakai-calendar-widget
              id="${r}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              class="widget"
              state="${w.state}"
              @remove=${this.removeWidget}
              @move=${this.moveWidget}
              ?editing=${this.editing}>
            </sakai-calendar-widget>
          </div>
        `;
      case "forums":
        return html`
          <div class="${this.state === "add" ? "faded" : ""}">
            <sakai-forums-widget
              id="${r}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              state="${w.state}"
              class="widget"
              @remove=${this.removeWidget}
              @move=${this.moveWidget}
              ?editing=${this.editing}>
            </sakai-forums-widget>
          </div>
        `;
      case "picker":
        return this.editing ? html`<div><sakai-widget-picker @remove=${this.removeWidget} id="picker" state="remove"></sakai-widget-picker></div>` : "";
      default:
        return "";
    }
  }

  render() {

    return html`
      ${this.editing ? html`
        <div id="add-button">
          <div>
            <a href="javascript:;"
                @click=${this.showWidgetPicker}
                title="${this.i18n.add_a_widget}"
                aria-label="${this.i18n.add_a_widget}">
              <sakai-icon type="add" size="small"></sakai-icon>
              <div id="add-text">${this.i18n.add_a_widget}</div>
            </a>
          </div>
        </div>
      ` : ""}

      <div id="grid">
        ${repeat(this.layout, w => w, w => html`
          ${this.getWidget(w)}
        `)}
      </div>
    `;
  }

  static styles = css`
    :host {
      display: block;
      width: var(--sakai-widget-panel-width);
      background-color: var(--sakai-tool-bg-color);
    }
    #add-button {
      text-align: right;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
    }
    #add-button sakai-icon {
      color: var(--sakai-widget-panel-add-button-color, green);
    }
    a {
      color: var(--link-color);
    }
    #add-text {
      display: inline-block;
      font-weight: bold;
      color: var(--sakai-widget-panel-add-text-color);
      font-size: var(--sakai-widget-panel-add-text-size, 14px);
      margin-left: 6px;
    }
    .faded {
      pointer-events: none;
      opacity: 0.4;
    }

    #grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(var(--sakai-widget-panel-min-widget-width, 350px), 1fr));
      grid-gap: var(--sakai-widget-panel-gutter-width, 1rem);
    }
  `;
}
