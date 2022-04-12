import { html, css } from "../assets/lit-element/lit-element.js";
import {ifDefined} from '../assets/lit-html/directives/if-defined.js';
import  "./sakai-calendar-display-event.js";
import { LionCalendar } from "../assets/@lion/calendar/src/LionCalendar.js";
import moment from "../assets/moment/dist/moment.js";
import '../sakai-icon.js';
import { loadProperties } from "../sakai-i18n.js";
import "../assets/@lion/dialog/lion-dialog.js";

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
      current: String,
      selected: Object,
      readOnly: { attribute: "read-only", type: Boolean },
      compact: Boolean,
      i18n: Object,
      siteId: { attribute: "site-id", type: String },
      userId: { attribute: "user-id", type: String },
      selectedDate: Number,
      events: { type: Array },
    };
  }

  constructor() {

    super();
    loadProperties("calendar").then(r => this.i18n = r);
    this.daysEvents = [];

    this.events = [];

    this.readOnly = true;

    this.addEventListener("user-selected-date-changed", event => {

      const time = event.detail.selectedDate.getTime();
      this.daysEvents = this.events.filter(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
      this.selectedDate = time;
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
    return this.events && super.shouldUpdate(changed);
  }

  loadData() {

    const url = this.siteId
      ? `/api/sites/${this.siteId}/calendar` : `/api/users/${this.userId}/calendar`;

    fetch(url, {
      cache: "no-cache",
      credentials: "same-origin"
    })
      .then(res => res.json())
      .then(data => this.events = data);
  }

  update(changed) {

    super.update(changed);

    this.shadowRoot.querySelectorAll(".calendar__day-button").forEach(c => {

      c.classList.remove("has-events");
      c.classList.remove("deadline");

      const time = c.date.getTime();
      const matchingEvent = this.events.find(e => e.start > time && e.start < (time + 24 * 60 * 60 * 1000));
      if (matchingEvent) {
        c.classList.add("has-events");
        if (matchingEvent.type === "deadline") {
          c.classList.add("deadline");
        }
      }
    });
  }

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

      <div id="container">
        <lion-dialog id="display-dialog">
          <sakai-calendar-display-event slot="content" selected="${ifDefined(this.selected ? JSON.stringify(this.selected) : undefined)}"></sakai-calendar-display-event>
          <button slot="invoker" style="display: none">none</button>
        </lion-dialog>
        ${super.render()}
        ${this.selectedDate && this.daysEvents.length > 0 ? html`
        <div id="days-events">
          <div id="days-events-title">
            ${this.i18n.events_for} ${moment(this.selectedDate).format("LL")}
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
      css`
        .sakai-calendar__navigation-wrapper {
          display: grid;
          grid-template-columns: 1fr min-content;
          align-items: center;
        }

        .calendar__navigation {
          display: inline-block;
        }

        .calendar__navigation-heading {
          font-size: 22px;
        }

        .calendar__navigation__year, .calendar__navigation__month {
          display: inline-flex;
        }

        .calendar__next-button, .calendar__previous-button {
          min-width: 25px;
          min-height: 25px;
          font-size: 20px;
          background: var(--sakai-background-color);
          color: var(--sakai-text-color);
        }

        .sakai-calendar__navigation__today {
          display: inline-block;
          margin-right: 14px;
        }

        .sakai-calendar__navigation__today > a {
          font-weight: bold;
          text-decoration: none;
          color: var(--sakai-text-color);
        }

        #add-block {
          flex: 3;
          text-align: right;
          margin-bottom: 10px;
        }
          sakai-icon[type="add"] {
            color: var(--sakai-color-green);
          }

        .sakai-event {
          font-size: 14px;
        }

        .deadline {
          background-color: var(--sakai-calendar-deadline-background-color);
        }

        #days-events {
          margin-top: 10px;
        }
        #days-events sakai-icon {
          margin-right: 10px;
        }
        #days-events-title {
          font-weight: bold;
          margin-bottom: 10px;
        }
        #days-events a {
          color: var(--sakai-text-color);
          text-decoration: none;
        }

        .calendar__day-button[today] {
          background-color: var(--sakai-calendar-today-background-color);
          color: var(--sakai-calendar-today-color);
          font-weight: bold;
          border-radius: 50%;
        }
        .calendar__previous-month-button,
        .calendar__next-month-button,
        .calendar__day-button {
          background-color: var(--sakai-background-color);
          font-weight: bold;
          color: var(--sakai-calendar-button-color);
        }

        .has-events {
          background-color: var(--sakai-calendar-has-events-background-color);
          color: var(--sakai-calendar-has-events-color);
          border-radius: 50%;
        }

        .calendar__day-button[previous-month],
        .calendar__day-button[next-month] {
          color: var(--sakai-calendar-button-color);
        }

        .calendar__day-button:hover {
          border-color: var(--sakai-border-color);
        }

        a {
          text-decoration: none;
          color: var(--link-color);
        }

        .calendar__day-button[disabled] {
          background-color: var(--sakai-calendar-button-disabled-background-color, #fff);
          color: var(--sakai-text-color-disabled, #eee);
        }
      `,
    ];
  }
}

if (!customElements.get("sakai-calendar")) {
  customElements.define("sakai-calendar", SakaiCalendar);
}
