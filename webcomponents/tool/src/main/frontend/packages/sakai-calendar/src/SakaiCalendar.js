import { html, nothing } from "lit";
import { LionCalendar } from "@lion/ui/calendar.js";
import "@sakai-ui/sakai-icon";
import { calendarStyles } from "./calendar-styles.js";
import { SakaiSitePicker } from "@sakai-ui/sakai-site-picker";
import "@sakai-ui/sakai-site-picker/sakai-site-picker.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiCalendar extends LionCalendar {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    defer: { type: Boolean },
    _daysEvents: { state: true },
    _events: { state: true },
    _i18n: { state: true },
    _sites: { state: true },
  };

  constructor() {

    super();

    this.addEventListener("user-selected-date-changed", event => {
      this._setDaysEvents(event.detail.selectedDate.getTime());
    });

    loadProperties("calendar-wc").then(r => this._i18n = r);
  }

  connectedCallback() {

    super.connectedCallback();

    if (!this.defer) this.loadData();
  }

  _setDaysEvents(time) {
    this._daysEvents = this._events.filter(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
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
    })
    .catch (error => console.error(error));
  }

  _filter() {

    if (!this._currentFilter) return;

    this._events = [ ... this._allEvents ];

    if (this._currentFilter === "sites" && this._selectedSite !== SakaiSitePicker.ALL) {
      this._events = [ ...this._events.filter(e => this._selectedSite === e.siteId) ];
    }
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
      this._setDaysEvents(new Date().setHours(0, 0, 0, 0));
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
  }

  // Override lion-calendar's function
  __renderNavigation() {

    return html`
      ${super.__renderNavigation()}
      <div class="sakai-calendar__navigation__today">
        <div>
          <a id="today-button" href="javascript:;" @click=${this._todayClicked}>${this._i18n?.today}</a>
        </div>
      </div>
    `;
  }

  render() {

    return html`
      ${!this.siteId && this._sites ? html`
      <div id="site-filter">
        <sakai-site-picker
            .sites=${this._sites}
            @sites-selected=${this._siteSelected}>
        </sakai-site-picker>
      </div>
      ` : nothing}

      <div class="calendar-msg">${this._i18n?.pinned_sites_message}</div>

      <div id="container">
        ${super.render()}
        ${this._daysEvents?.length > 0 ? html`
        <div id="days-events">
          <div id="days-events-title">
            ${this._i18n?.events_for} ${new Date(this._daysEvents[0].start).toLocaleDateString(undefined, { dateStyle: "medium" })}
          </div>
          ${this._daysEvents.map(e => html`
            <div>
              <sakai-icon type="${e.tool}" size="small"></sakai-icon>
              ${e.url ? html`
                <a href="${e.url}" role="link" aria-label="${e.title}">
                  <span>${e.title}</span><span> (${e.siteTitle})</span>
                </a>
              ` : html`
                <span>${e.title}</span><span> (${e.siteTitle})</span>
              `}
            </div>
          `)}
        </div>
        ` : nothing}
      </div>
    `;
  }

  static styles = [
    LionCalendar.styles,
    calendarStyles,
  ];
}
