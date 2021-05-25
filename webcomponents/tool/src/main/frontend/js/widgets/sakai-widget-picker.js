import { css, html } from "../assets/lit-element/lit-element.js";
import { SakaiDashboardWidget } from './sakai-dashboard-widget.js';
import { loadProperties } from "../sakai-i18n.js";
import { sakaiWidgets } from "./sakai-widgets.js";

export class SakaiWidgetPicker extends SakaiDashboardWidget {

  static get properties() {

    return {
      all: { type: Array },
      current: { type: Array },
      available: { type: Array },
      toolnames: { type: Object },
    };
  }

  constructor() {

    super();
    this.widgetId = "picker";
    this.all = sakaiWidgets.getIds();
    this.current = [];
    this.available = [];
    this.draggable = false;
    this.hasOptions = false;

    this.loadTranslations("widget-picker");
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
    return this.toolnames[id] || this.toolnames["unknown"];
  }

  widgetPicked(e) {
    this.dispatchEvent(new CustomEvent("widget-picked", { detail: { id: e.target.id}, bubbles: true }));
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
        <div id="topbar">${this.i18n["pick_instruction"]}</div>
        ${this.available.map(w => html`
          <div class="widget-option"><a href="javascript:;" id="${w}" @click=${this.widgetPicked}>${this.lookupWidgetName(w)}</a></div>
        `)}
      ` : html`
        <div class="widget-option">${this.i18n["all_displayed"]}</div>
      `}
    `;
  }

  static get styles() {

    return [
      ...super.styles,
      css`
        .widget-option {
          margin-left: 12px;
        }
        .widget-option a {
          text-decoration: none;
          color: black;
          font-size: 18px;
        }
      `,
    ];
  }
}

if (!customElements.get("sakai-widget-picker")) {
  customElements.define("sakai-widget-picker", SakaiWidgetPicker);
}
