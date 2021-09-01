import { html, css } from "../assets/lit-element/lit-element.js";
//import "../sakai-editor.js";
import { SakaiDialogContent } from "../sakai-dialog-content.js";
import "../datepicker/sakai-date-picker.js";
import { loadProperties } from "../sakai-i18n.js";
import moment from "../assets/moment/dist/moment.js";
import "../sakai-button.js";

class SakaiCalendarCreateEvent extends SakaiDialogContent {

  static get styles() {

    return [SakaiDialogContent.styles,
      css`

        .frequency-options {
          margin-top: 10px;
          margin-left: 20px;
        }
    `];
  }

  constructor() {

    super();

    loadProperties("calendar").then(r => this.i18n = r);

    this.start = moment();
    this.start = moment(this.start).add(30 - (this.start.minute() % 30), "minutes");
    this.end = moment(this.start).add(1, "hour");
  }

  static get properties() {

    return {
      i18n: Object,
      frequency: String,
    };
  }

  shouldUpdate(changed) {
    return this.i18n && super.shouldUpdate(changed);
  }

  add() {

    console.log(this.shadowRoot.getElementById("event-title").value);
    console.log(this.start);
    console.log(this.end);
  }

  title() {
    return this.i18n["java.new.title"];
  }

  content() {

    return html`
      <p class="sak-banner-info">
        ${this.i18n["new.a"]} <span class="reqStarInline"> * </span>
      </p>
      <div class="label">
        <label for="event-title">${this.i18n["new.title"]}</label>
        <span class="reqStar">*</span>
      </div>
      <div class="input"><input type="text" id="event-title" size="50" maxlength="150" /></div>
      <div class="label"><label for="start">${this.i18n["viewl.st"]}</label></div>
      <div class="input">
        <sakai-date-picker id="start" @datetime-selected=${(e) => this.start = e.detail.epochMillis}>
      </div>
      <div class="label"><label for="duration">${this.i18n["new.duration"]}:</label></div>
      <div class="input">
        <select>
        ${Array(24).fill().map((_, i) => html`<option value=${i}>${i}</option>`)}
        </select>
        <span>Hours</span>
        <select @change=${(e) => this.hoursDuration = e.target.value}>
        ${Array(12).fill().map((_, i) => html`<option value=${i*5}>${i*5}</option>`)}
        </select>
        <span>Minutes</span>
      </div>
      <div class="label"><label for="end">End</label></div>
      <div class="input">
        <sakai-date-picker id="end" @datetime-selected=${(e) => this.start = e.detail.epochMillis}>
      </div>
      <div class="label"><label for="frequency">${this.i18n["new.freq"]}:</label></div>
      <div class="input">
        <select @change=${(e) => this.frequency = e.target.value}>
          <option value="once">${this.i18n["set.once"]}</option>
          <option value="daily">${this.i18n["set.daily"]}</option>
          <option value="weekly">${this.i18n["set.weekly"]}</option>
          <option value="monthly">${this.i18n["set.monthly"]}</option>
          <option value="yearly">${this.i18n["set.yearly"]}</option>
        </select>
        ${this.frequency === "daily" ? html`
        <div class="frequency-options" id="daily-options">
          <span>${this.i18n["new.every"]} </span>
          <select>
            ${Array(30).fill().map((_, i) => html`<option value=${i+1}>${i+1}</option>`)}
          </select>
          <span> ${this.i18n["set.days"]}</span>
        </div>
        ` : ""}
        ${this.frequency === "weekly" ? html`
        <div class="frequency-options" id="weekly-options">
          <span>${this.i18n["new.every"]} </span>
          <select>
            ${Array(51).fill().map((_, i) => html`<option value=${i+1}>${i+1}</option>`)}
          </select>
          <span> ${this.i18n["set.weeks"]}</span>
        </div>
        ` : ""}
        ${this.frequency === "monthly" ? html`
        <div class="frequency-options" id="monthly-options">
          <span>${this.i18n["new.every"]} </span>
          <select>
            ${Array(11).fill().map((_, i) => html`<option value=${i+1}>${i+1}</option>`)}
          </select>
          <span> ${this.i18n["set.months"]}</span>
        </div>
        ` : ""}
        ${this.frequency === "yearly" ? html`
        <div class="frequency-options" id="yearly-options">
          <span>${this.i18n["new.every"]} </span>
          <select>
            ${Array(30).fill().map((_, i) => html`<option value=${i+1}>${i+1}</option>`)}
          </select>
          <span> ${this.i18n["set.years"]}</span>
        </div>
        ` : ""}
      </div>
    `;
        //<sakai-editor editor-id="editor-classic"></sakai-editor>
  }

  buttons() {

    return html`
      <sakai-buttonv primary>Add Event</sakai-button>
    `;
  }
}

if (!customElements.get("sakai-calendar-create-event", SakaiCalendarCreateEvent)) {
  customElements.define("sakai-calendar-create-event", SakaiCalendarCreateEvent);
}
