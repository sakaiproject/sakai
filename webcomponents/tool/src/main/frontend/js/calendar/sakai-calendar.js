import { html } from "../assets/lit-element/lit-element.js";
import { LionCalendar } from "../assets/@lion/calendar/src/LionCalendar.js";
import '../sakai-icon.js';
import { loadProperties } from "../sakai-i18n.js";
import { calendarStyles } from "./calendar-styles.js";

export class SakaiCalendar extends LionCalendar {

  static get localizeNamespaces() {

    return [
      {
        'lion-calendar': /** @param {string} locale */ locale => {
          switch (locale) {
            case 'bg-BG':
              return import('../assets/@lion/calendar/translations/bg.js');
            case 'cs-CZ':
              return import('../assets/@lion/calendar/translations/cs.js');
            case 'de-AT':
            case 'de-DE':
              return import('../assets/@lion/calendar/translations/de.js');
            case 'en-AU':
            case 'en-GB':
            case 'en-PH':
            case 'en-US':
              return import('../assets/@lion/calendar/translations/en.js');
            case 'es-ES':
              return import('../assets/@lion/calendar/translations/es.js');
            case 'fr-FR':
            case 'fr-BE':
              return import('../assets/@lion/calendar/translations/fr.js');
            case 'hu-HU':
              return import('../assets/@lion/calendar/translations/hu.js');
            case 'it-IT':
              return import('../assets/@lion/calendar/translations/it.js');
            case 'nl-BE':
            case 'nl-NL':
              return import('../assets/@lion/calendar/translations/nl.js');
            case 'pl-PL':
              return import('../assets/@lion/calendar/translations/pl.js');
            case 'ro-RO':
              return import('../assets/@lion/calendar/translations/ro.js');
            case 'ru-RU':
              return import('../assets/@lion/calendar/translations/ru.js');
            case 'sk-SK':
              return import('../assets/@lion/calendar/translations/sk.js');
            case 'uk-UA':
              return import('../assets/@lion/calendar/translations/uk.js');
            case 'zh-CN':
              return import('../assets/@lion/calendar/translations/zh.js');
            default:
              return import('../assets/@lion/calendar/translations/en.js');
          }
        },
      },
      ...super.localizeNamespaces,
    ];
  }

  static get properties() {

    return {
      userId: { attribute: "user-id", type: String },
      siteId: { attribute: "site-id", type: String },
      _i18n: { attribute: false, type: Object },
      _selectedDate: { attribute: false, type: Date },
      _events: { attribute: false, type: Array },
      _days: { attribute: false, type: Number },
    };
  }

  constructor() {

    super();

    this._daysEvents = [];

    this.addEventListener("user-selected-date-changed", event => {

      const time = event.detail.selectedDate.getTime();
      this._daysEvents = this._events.filter(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
      this._selectedDate = event.detail.selectedDate;
    });

    // This is a hack. There's something about the way Lion's calendar connects and renders which
    // means we can't wait for this._i18n in shouldUpdate. So, we add placeholders in the _i18n
    // object. Nasty. If I can work it out, I'll come back and fix this.
    //TODO: Fix this properly
    this._i18n = { days_message: "", events_for: "", today: "" };

    loadProperties("calendar").then(r => this._i18n = r);
  }

  set userId(value) {

    const old = this._userId;

    this._userId = value;
    this._loadData();

    this.requestUpdate("userId", old);
  }

  get userId() { return this._userId; }

  set siteId(value) {

    const old = this._siteId;

    this._siteId = value;
    this._loadData();

    this.requestUpdate("siteId", old);
  }

  get siteId() { return this._siteId; }

  _loadData() {

    const url = this.siteId
      ? `/api/sites/${this.siteId}/calendar` : `/api/users/current/calendar`;

    fetch(url, { cache: "no-cache", credentials: "same-origin"})
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while retrieving calendar events from ${url}`);
    })
    .then(data => {

      this._events = data.events;
      this._days = data.days;
    })
    .catch (error => console.error(error));
  }

  update(changedProperties) {

    super.update(changedProperties);

    this.shadowRoot.querySelectorAll(".calendar__day-button").forEach(c => {

      c.classList.remove("has-events");
      c.classList.remove("deadline");

      const time = c.date.getTime();

      if (this._events) {
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
      <div class="sakai-calendar__navigation-wrapper">
        ${super.__renderNavigation()}
        <div class="sakai-calendar__navigation__today">
          <a href="javascript:;" @click=${() => { this._selectedDate = null; this.initCentralDate(); } }>${this._i18n.today}</a>
        </div>
      </div>
    `;
  }

  render() {

    return html`

      <div class="calendar-msg">${this._i18n.days_message.replace("{}", this._days)}</div>

      <div id="container">
        ${super.render()}
        ${this._selectedDate && this._daysEvents.length > 0 ? html`
        <div id="days-events">
          <div id="days-events-title">
            ${this._i18n.events_for} ${this._selectedDate.toLocaleDateString(undefined, { dateStyle: "medium"})}
          </div>
          ${this._daysEvents.map(e => html`
            <div>
              <a href="${e.url}">
                <sakai-icon type="${e.tool}" size="small"></sakai-icon>
                <span>${e.title}</span><span> (${e.siteTitle})</span>
              </a>
            </div>
          `)}
        </div>
        ` : ""}
      </div>
    `;
  }

  static get styles() {

    return [
      ...super.styles,
      calendarStyles,
    ];
  }
}

const tagName = "sakai-calendar";
!customElements.get(tagName) && customElements.define(tagName, SakaiCalendar);
