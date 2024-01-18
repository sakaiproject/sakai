import { css, html } from "lit";
import { SakaiDashboardWidget } from "./SakaiDashboardWidget.js";
import { sakaiWidgets } from "./SakaiWidgets.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiWidgetPicker extends SakaiDashboardWidget {

  static properties = {

    all: { type: Array },
    current: { type: Array },
    available: { type: Array },
    toolnames: { type: Object },
  };

  constructor() {

    super();

    this.widgetId = "widget-picker";
    this.all = sakaiWidgets.getIds();
    this.current = [];
    this.available = [];
    this.draggable = false;
    this.hasOptions = false;

    this.loadTranslations(this.widgetId);
    loadProperties("toolnames").then(r => this.toolnames = r);
  }

  set all(value) {

    this._all = value;

    if (this.current) {
      this.available = value.filter(v => !this.current.includes(v));
    }
  }

  get all() { return this._all; }

  set current(value) {

    this._current = value;
    if (this.all) {
      this.available = this.all.filter(v => !value.includes(v));
    }
  }

  get current() { return this._current; }

  lookupWidgetName(id) {
    return this.toolnames[id] || this.toolnames.unknown;
  }

  widgetPicked(e) {
    this.dispatchEvent(new CustomEvent("widget-picked", { detail: { id: e.target.id }, bubbles: true }));
  }

  remove() {
    this.dispatchEvent(new CustomEvent("remove", { detail: { newState: "view" }, bubbles: true }));
  }

  shouldUpdate(changed) {
    return super.shouldUpdate(changed) && this.toolnames;
  }

  content() {

    return html`
      ${this.available.length ? html`
        <div id="topbar">${this._i18n.pick_instruction}</div>
        ${this.available.map(w => html`
          <div class="widget-option"><a href="javascript:;" id="${w}" @click=${this.widgetPicked}>${this.lookupWidgetName(w)}</a></div>
        `)}
      ` : html`
        <div class="widget-option">${this._i18n.all_displayed}</div>
      `}
    `;
  }

  static styles = [
    SakaiDashboardWidget.styles,
    css`
      .widget-option {
        margin-left: 12px;
      }
      .widget-option a {
        text-decoration: none;
        font-size: 18px;
      }
    `,
  ];
}
