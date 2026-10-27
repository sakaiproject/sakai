import { css, html, nothing } from "lit";
import { LionCalendar } from "@lion/ui/calendar.js";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-icon";
import { calendarStyles } from "./calendar-styles.js";
import { SakaiSitePicker } from "@sakai-ui/sakai-site-picker";
import "@sakai-ui/sakai-site-picker/sakai-site-picker.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiCalendar extends LionCalendar {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    defer: { type: Boolean },
    dashboard: { type: Boolean },
    tileW: { attribute: "tile-w", type: Number },
    tileH: { attribute: "tile-h", type: Number },
    filtersOpen: { attribute: "filters-open", type: Boolean },
    _daysEvents: { state: true },
    _events: { state: true },
    _i18n: { state: true },
    _sites: { state: true },
    _calendarExpanded: { state: true },
    _selectedDayStart: { state: true },
  };

  constructor() {

    super();

    this.filtersOpen = false;
    this.dashboard = false;
    this.tileW = 1;
    this.tileH = 1;
    this._calendarExpanded = false;
    this._selectedDayStart = new Date().setHours(0, 0, 0, 0);

    this.addEventListener("user-selected-date-changed", event => {
      const start = event.detail.selectedDate.setHours(0, 0, 0, 0);
      this._selectedDayStart = start;
      this._setDaysEvents(start);
    });

    loadProperties({
      bundle: "calendar-wc",
      requiredKeys: [ "expand_calendar", "collapse_calendar", "no_events_for_day" ],
    }).then(r => this._i18n = r);
  }

  connectedCallback() {

    super.connectedCallback();

    if (!this.defer) this.loadData();
  }

  _closeFilters() {
    this.filtersOpen = false;
    this.dispatchEvent(new CustomEvent("filters-closed", { bubbles: true, composed: true }));
  }

  _setDaysEvents(time) {
    this._daysEvents = (this._events || []).filter(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
  }

  _tileW() {
    return Math.max(1, this.tileW || 1);
  }

  _tileH() {
    return Math.max(1, this.tileH || 1);
  }

  _isTall() {
    return this.dashboard && this._tileH() >= 2;
  }

  _isWide() {
    return this.dashboard && this._tileW() >= 2 && this._tileH() < 2;
  }

  _showExpandToggle() {
    return this.dashboard && this._i18n && !this._isTall() && !this._isWide();
  }

  _showFullMonth() {
    return !this.dashboard || this._isTall() || this._isWide() || this._calendarExpanded;
  }

  _toggleCalendarExpanded() {
    this._calendarExpanded = !this._calendarExpanded;
  }

  loadData() {

    const url = this.siteId
      ? `/api/sites/${this.siteId}/calendar` : "/api/users/current/calendar";

    fetch(url, { cache: "no-cache", credentials: "same-origin" })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while retrieving calendar events from ${url}`);
    })
    .then(data => {

      this._allEvents = data.events;
      this._events = data.events;
      !this.siteId && (this._sites = data.sites);

      this.renderRoot.querySelector(".calendar__day-button[today]")?.click();
      this._setDaysEvents(this._selectedDayStart);
    })
    .catch (error => console.error(error));
  }

  _filter() {

    if (!this._currentFilter) return;

    this._events = [ ... this._allEvents ];

    if (this._currentFilter === "sites" && this._selectedSite !== SakaiSitePicker.ALL) {
      this._events = [ ...this._events.filter(e => this._selectedSite === e.siteId) ];
    }
    this._setDaysEvents(this._selectedDayStart);
  }

  _siteSelected(e) {

    this._selectedSite = e.detail.value;
    this._currentFilter = "sites";
    this._filter();
  }

  _todayClicked() {

    this.selectedDate = null;
    this.initCentralDate();

    this.updateComplete.then(() => {

      this.renderRoot.querySelector(".calendar__day-button[today]")?.click();
      const start = new Date().setHours(0, 0, 0, 0);
      this._selectedDayStart = start;
      this._setDaysEvents(start);
    });

  }

  _filtersPanel() {

    return html`
      <div class="dashboard-filter-toolbar">
        <div class="dashboard-filter-toolbar-start"></div>
        <button type="button"
            class="btn btn-icon btn-sm"
            @click=${this._closeFilters}
            title="${this._i18n?.close_filters}"
            aria-label="${this._i18n?.close_filters}">
          <span class="si si-close fs-6" aria-hidden="true"></span>
        </button>
      </div>
      ${!this.siteId ? html`
        <div id="site-filter">
          <sakai-site-picker
              .sites=${this._sites}
              @sites-selected=${this._siteSelected}>
          </sakai-site-picker>
        </div>
      ` : nothing}
    `;
  }

  _statusMessage() {
    return !this.siteId && this._i18n?.pinned_sites_message ? this._i18n.pinned_sites_message : null;
  }

  _todayControl() {

    return html`
      <a id="today-button" href="javascript:;" @click=${this._todayClicked}>${this._i18n?.today}</a>
    `;
  }

  _chrome(status) {

    const statusEl = status ? html`<div id="widget-status">${status}</div>` : nothing;
    const nav = html`
      <div class="calendar__navigation-row">
        ${this.__renderNavigation()}
        ${this._todayControl()}
      </div>
    `;

    return html`
      <div class="calendar__chrome ${this.dashboard ? "dashboard" : ""}">
        ${this.dashboard ? html`${statusEl}${nav}` : html`${nav}${statusEl}`}
      </div>
    `;
  }

  _selectedDayLabel() {

    const start = this._selectedDayStart || new Date().setHours(0, 0, 0, 0);
    return new Date(start).toLocaleDateString(undefined, { dateStyle: "medium" });
  }

  _daysEventsBlock() {

    if (!this.dashboard && !this._daysEvents?.length) {
      return nothing;
    }

    return html`
      <div id="days-events" class="${this.dashboard ? "dashboard-events" : ""}">
        ${this._calendarExpandToggle()}
        <div class="dashboard-events-body">
          <div id="days-events-title">
            ${this._i18n?.events_for} ${this._selectedDayLabel()}
          </div>
          ${this._daysEvents?.length ? this._daysEvents.map(e => html`
            <div class="dashboard-event-row">
              <sakai-icon type="${e.tool}" size="small"></sakai-icon>
              ${e.url ? html`
                <a href="${e.url}" role="link" aria-label="${e.title}">
                  <span>${e.title}</span>${e.siteTitle ? html`<span> (${e.siteTitle})</span>` : nothing}
                </a>
              ` : html`
                <span>${e.title}</span>${e.siteTitle ? html`<span> (${e.siteTitle})</span>` : nothing}
              `}
            </div>
          `) : (this.dashboard ? html`
            <div class="dashboard-events-empty">${this._i18n?.no_events_for_day}</div>
          ` : nothing)}
        </div>
      </div>
    `;
  }

  _calendarExpandToggle() {

    if (!this._showExpandToggle()) {
      return nothing;
    }

    const expanded = this._calendarExpanded;
    const label = expanded ? this._i18n.collapse_calendar : this._i18n.expand_calendar;

    return html`
      <button type="button"
          class="calendar__expand-toggle"
          @click=${this._toggleCalendarExpanded}
          aria-expanded="${expanded ? "true" : "false"}"
          title="${label}"
          aria-label="${label}">
        <span class="si ${expanded ? "si-up" : "si-down"}" aria-hidden="true"></span>
        <span>${label}</span>
      </button>
    `;
  }

  _applyWeekVisibility() {

    if (!this.dashboard) {
      return;
    }

    const rows = [ ...this.renderRoot.querySelectorAll(".calendar__grid tbody tr") ];
    if (!rows.length) {
      return;
    }

    const full = this._showFullMonth();
    if (full) {
      rows.forEach(tr => {
        tr.hidden = false;
        tr.classList.remove("calendar__week--active");
      });
      return;
    }

    const selected = this.renderRoot.querySelector(".calendar__day-button[selected]");
    const today = this.renderRoot.querySelector(".calendar__day-button[today]");
    let activeRow = selected?.closest("tr") || today?.closest("tr");
    if (!activeRow) {
      activeRow = rows.find(tr => tr.querySelector(".calendar__day-button[current-month]")) || rows[0];
    }

    rows.forEach(tr => {
      const on = tr === activeRow;
      tr.hidden = !on;
      tr.classList.toggle("calendar__week--active", on);
    });
  }

  update(changedProperties) {

    super.update(changedProperties);

    this.renderRoot.querySelectorAll(".calendar__day-button,.calendar__day-button[today]").forEach(c => {

      c.classList.remove("has-events");
      c.classList.remove("deadline");

      if (c.date && this._events) {
        const time = c.date.getTime();

        const matchingEvent = this._events.find(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
        if (matchingEvent) {
          c.classList.add("has-events");
          if (matchingEvent.type === "deadline") {
            c.classList.add("deadline");
          }
        }
      }
    });

    this._applyWeekVisibility();
  }

  render() {

    const status = this._statusMessage();
    const chrome = this._chrome(status);
    const tall = this._isTall();
    const wide = this._isWide();
    const expanded = this._showFullMonth();
    const layoutClass = [
      this.dashboard ? "dashboard" : "standalone",
      tall ? "is-tall" : "",
      wide ? "is-wide" : "",
      expanded && this.dashboard && !tall && !wide ? "is-calendar-expanded" : "",
    ].filter(Boolean).join(" ");
    const grid = html`
      <div class="calendar-block">
        <div class="calendar" role="application">
          ${this.__renderData()}
        </div>
      </div>
    `;

    return html`
      <div class="dashboard-widget-body ${this.dashboard ? "is-dashboard" : ""}">
        <div class="dashboard-widget-main ${this.filtersOpen ? "is-blurred" : ""}">
          <div id="container" class="${layoutClass}">
            ${this.dashboard ? html`
              ${grid}
              ${this._daysEventsBlock()}
              ${chrome}
            ` : html`
              ${chrome}
              ${grid}
              ${this._daysEventsBlock()}
            `}
          </div>
        </div>
        ${this.filtersOpen ? html`
          <div class="dashboard-filter-overlay" @click=${this._closeFilters}>
            <div class="dashboard-filter-panel" @click=${e => e.stopPropagation()}>
              ${this._filtersPanel()}
            </div>
          </div>
        ` : nothing}
      </div>
    `;
  }

  static styles = [
    ...SakaiShadowElement.styles,
    LionCalendar.styles,
    calendarStyles,
    css`
      :host {
        display: block;
        height: 100%;
        min-height: 0;
      }

      .dashboard-widget-body {
        position: relative;
        height: 100%;
        min-height: 0;
      }

      .dashboard-widget-body.is-dashboard {
        display: flex;
        flex-direction: column;
      }

      .dashboard-widget-main {
        min-height: 0;
        flex: 1 1 auto;
        display: flex;
        flex-direction: column;
      }

      .dashboard-widget-main.is-blurred {
        filter: blur(4px);
        pointer-events: none;
        user-select: none;
      }

      #container {
        display: flex;
        flex-direction: column;
        min-height: 0;
        flex: 1 1 auto;
        height: 100%;
      }

      #container.dashboard {
        display: grid;
        grid-template-rows: auto minmax(0, 1fr) auto;
        grid-template-areas:
          "cal"
          "events"
          "chrome";
        gap: 0;
      }

      #container.dashboard.is-calendar-expanded {
        grid-template-rows: minmax(0, 1fr) auto auto;
      }

      #container.dashboard.is-tall {
        grid-template-rows: auto minmax(0, 1fr) auto;
      }

      #container.dashboard.is-wide {
        grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
        grid-template-rows: minmax(0, 1fr) auto;
        grid-template-areas:
          "cal events"
          "chrome chrome";
      }

      #container.dashboard .calendar-block {
        grid-area: cal;
        min-width: 0;
        min-height: 0;
        display: flex;
        flex-direction: column;
      }

      #container.dashboard #days-events {
        grid-area: events;
        min-height: 0;
        overflow: hidden;
      }

      #container.dashboard .calendar__chrome {
        grid-area: chrome;
      }

      #container.dashboard:not(.is-calendar-expanded):not(.is-tall):not(.is-wide) .calendar-block {
        overflow: hidden;
      }

      #container.dashboard.is-calendar-expanded .calendar-block,
      #container.dashboard.is-tall .calendar-block,
      #container.dashboard.is-wide .calendar-block {
        overflow: auto;
      }

      #container.dashboard.is-tall .calendar-block {
        overflow: visible;
        height: auto;
      }

      #container.dashboard.is-calendar-expanded #days-events {
        max-height: 9rem;
      }

      #container.dashboard.is-wide #days-events {
        border-top: 0;
        border-left: 1px solid var(--sakai-border-color, #ddd);
      }

      #container.dashboard .calendar {
        flex: 0 0 auto;
        min-height: 0;
      }

      .dashboard-filter-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: color-mix(in srgb, var(--sakai-background-color-1, #fff) 35%, transparent);
        z-index: 2;
        padding: 0.75rem;
      }

      .dashboard-filter-panel {
        width: min(100%, 22rem);
        background: var(--sakai-background-color-1, #fff);
        border: 1px solid var(--sakai-border-color, #ddd);
        border-radius: var(--bs-border-radius, 0.375rem);
        box-shadow: 0 0.5rem 1.25rem rgba(0, 0, 0, 0.12);
        padding: 0.75rem;
      }

      .dashboard-filter-toolbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 0.25rem;
        margin-bottom: 0.75rem;
      }

      .dashboard-filter-toolbar-start {
        display: flex;
        align-items: center;
        gap: 0.25rem;
      }

      .dashboard-filter-toolbar .btn-icon {
        --bs-btn-color: var(--sakai-text-color-2);
      }

      #site-filter sakai-site-picker::part(select) {
        width: 100%;
      }
    `,
  ];
}
