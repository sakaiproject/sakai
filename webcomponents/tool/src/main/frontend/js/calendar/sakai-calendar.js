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
      i18n: { attribute: false, type: Object },
      siteId: { attribute: "site-id", type: String },
      userId: { attribute: "user-id", type: String },
      selectedDate: { attribute: false, type: Date },
      events: { attribute: false, type: Array },
      days: { attribute: false, type: Number },
    };
  }

  constructor() {

    super();
    loadProperties("calendar").then(r => this.i18n = r);
    this.daysEvents = [];

    this.addEventListener("user-selected-date-changed", event => {

      const time = event.detail.selectedDate.getTime();
      this.daysEvents = this.events.filter(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
      this.selectedDate = event.detail.selectedDate;
    });
  }

  set siteId(value) {

    this._siteId = value;
    this.loadData();
  }

  get siteId() { return this._siteId; }

  set userId(value) {

    this._userId = value;
    this.loadData();
  }

  get userId() { return this._userId; }

  shouldUpdate(changed) {
    return super.shouldUpdate(changed);
  }

  loadData() {

    const url = this.siteId
      ? `/api/sites/${this.siteId}/calendar` : `/api/users/current/calendar`;

    fetch(url, {
      cache: "no-cache",
      credentials: "same-origin"
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }

      throw new Error(`Network error while retrieving calendar events from ${url}`);
    })
    .then(data => {

      this.events = data.events;
      this.days = data.days;
    })
    .catch (error => console.error(error));
  }

  update(changed) {

    super.update(changed);

    this.shadowRoot.querySelectorAll(".calendar__day-button").forEach(c => {

      c.classList.remove("has-events");
      c.classList.remove("deadline");

      const time = c.date.getTime();

      if (this.events) {
        const matchingEvent = this.events.find(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
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
          <a href="javascript:;" @click=${() => { this.selectedDate = null; this.initCentralDate(); } }>${this.i18n.today}</a>
        </div>
      </div>
    `;
  }

  render() {

    return html`

      <div>${this.i18n.days_message.replace("{}", this.days)}</div>

      <div id="container">
        ${super.render()}
        ${this.selectedDate && this.daysEvents.length > 0 ? html`
        <div id="days-events">
          <div id="days-events-title">
            ${this.i18n.events_for} ${this.selectedDate.toLocaleDateString(undefined, { dateStyle: "medium"})}
          </div>
          ${this.daysEvents.map(e => html`
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
