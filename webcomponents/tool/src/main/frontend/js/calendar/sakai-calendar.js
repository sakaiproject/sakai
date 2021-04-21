import { html, css } from "../assets/lit-element/lit-element.js";
import {ifDefined} from '../assets/lit-html/directives/if-defined.js';
import  "./sakai-calendar-create-event.js";
import  "./sakai-calendar-display-event.js";
import { LionCalendar } from "../assets/@lion/calendar/src/LionCalendar.js";
import moment from "../assets/moment/dist/moment.js";
import '../sakai-icon.js';
import { loadProperties } from "../sakai-i18n.js";

export class SakaiCalendar extends LionCalendar {

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

    this.addEventListener("user-selected-date-changed", e => {

      const time = e.detail.selectedDate.getTime();
      this.daysEvents = this.events.filter(e => e.start > time && e.start < (time + 24*60*60*1000));
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

    fetch(url, { cache: "no-cache", credentials: "same-origin" }).then(res => res.json()).then(data => this.events = data);
  }

  update(changed) {

    super.update(changed);

    this.shadowRoot.querySelectorAll(".calendar__day-button").forEach(c => {

      c.classList.remove("has-events");
      c.classList.remove("deadline");

      const time = c.date.getTime();
      const e = this.events.find(e => e.start > time && e.start < (time + 24*60*60*1000));
      if (e) {
        c.classList.add("has-events");
        if (e.type === "deadline") {
          c.classList.add("deadline");
        }
      }
    });
  }

  render() {

    return html`

      <div id="container">
        <lion-dialog id="display-dialog">
          <sakai-calendar-display-event slot="content" selected="${ifDefined(this.selected ? JSON.stringify(this.selected): undefined)}"></sakai-calendar-display-event>
          <button slot="invoker" style="display: none">none</button>
        </lion-dialog>
        ${this.readOnly ? "" : html`
        <div id="add-block">
          <lion-dialog id="add-dialog">
            <sakai-calendar-create-event slot="content"></sakai-calendar-create-event>
            <a href="javascript:;" slot="invoker"><sakai-icon type="add" size="small"></a>
          </lion-dialog>
        </div>
        `}
        ${super.render()}
        ${this.daysEvents.length > 0 ? html`
        <div id="days-events">
          <div id="days-events-title">
            ${this.i18n["events_for"]} ${moment(this.selectedDate).format("LL")}
          </div>
          ${this.daysEvents.map(e => html`
            <div>
              <sakai-icon type="${e.tool}" size="small"></sakai-icon><a href="${e.url}"><span>${e.title}</span></a><span> (${e.siteTitle})</span>
            </div>
          `)}
        </div>
        ` : ""}
      </div>
    `;
  }

  static get styles() {

    return [super.styles, css`

      #calendar {
        font-family: var(--sakai-font-family);
      }

      #add-block {
        flex: 3;
        text-align: right;
        margin-bottom: 10px;
      }
        sakai-icon[type="add"] {
          color: var(--sakai-color-green);
        }
      #controls {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10px;
      }
        #view {
        }
        #nav {
          display: flex;
          align-items: center;
          justify-content: center;
        }
          #previous {
            margin-right: 20px;
          }
          #next {
            margin-left: 20px;
          }
      .sakai-event {
        font-size: 14px;
      }

      .deadline {
        background-color: var(--sakai-calendar-deadline-bg-color);
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

      .calendar__day-button[today] {
        background-color: var(--sakai-calendar-today-bg-color);
        color: var(--sakai-calendar-today-fg-color);
        text-decoration: none;
        font-weight: bold;
        border-radius: 50%;
      }
      .calendar__previous-month-button,
      .calendar__next-month-button,
      .calendar__day-button {
        background-color: var(--sakai-calendar-button-bg-color, #fff);
        color: var(--sakai-calendar-button-color, rgb(115, 115, 115));
      }

      .has-events {
        background-color: var(--sakai-calendar-has-events-bg-color);
        color: var(--sakai-calendar-has-events-fg-color);
        border-radius: 50%;
      }

      .calendar__day-button[previous-month],
      .calendar__day-button[next-month] {
        color: var(--sakai-calendar-button-disabled-color, rgb(115, 115, 115));
        background-color: var(--sakai-calendar-button-disabled-bg-color, #fff);

      }

      .calendar__day-button:hover {
        border-color: var(--sakai-color-green);
      }

      .calendar__day-button[disabled] {
        background-color: var(--sakai-calendar-button-disabled-bg-color, #fff);
        color: var(--sakai-calendar-button-diabled-color, #eee);
      }

      a {
        text-decoration: none;
        color: var(--link-color);
      }
    `];
  }
}

if (!customElements.get("sakai-calendar")) {
  customElements.define("sakai-calendar", SakaiCalendar);
}
