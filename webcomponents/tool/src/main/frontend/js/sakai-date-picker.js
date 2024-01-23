import { LitElement, html } from "./assets/lit-element/lit-element.js";
import { getTimezone } from "./sakai-portal-utils.js";
import { Temporal } from "./assets/@js-temporal/polyfill/dist/index.esm.js";

/**
 * Renders an input which, when clicked, launches a date picker.
 *
 * @example <caption>Usage:</caption>
 * <sakai-date-picker epoch-millis="345922925445" @/>
 *
 * The tag fires the event 'datetime-selected'. You'd handle that with (vanillajs):
 *
 * sakaiDatePicker.addEventListener("datetime-selected", e => console.log(e.detail.epochMillis));
 *
 * @extends LitElement
 * @property {number} [epochMillis] The milliseconds since the unix epoch to set this datetime to
 * @property {string} [isoDate] The ISO8601 string to set this datetime to
 * @property {boolean} [disabled] Disable the date controls
 * @property {string} [label] The a11y label to use for the aria-label and title attributes
 * @fires datetime-selected This event has epochMillis and epochSeconds as the detail object
 */
class SakaiDatePicker extends LitElement {

  static get properties() {

    return {
      epochMillis: { attribute: "epoch-millis", type: Number },
      isoDate: { attribute: "iso-date", type: String },
      disabled: { attribute: false, type: Boolean },
      label: { type: String },
    };
  }

  set epochMillis(value) {

    if (value) {
      this._epochMillis = value;
      const instant = Temporal.Instant.fromEpochMilliseconds(value);
      const zonedDateTime = instant.toZonedDateTimeISO({ timeZone: getTimezone() });
      this.isoDate = zonedDateTime.toString().substring(0, 16);
    } else {
      this._epochMillis = null;
      this.isoDate = null;
    }

    const inputDate = this.shadowRoot.getElementById("date-picker-input");
    inputDate && (inputDate.value = this.isoDate);
  }

  get epochMillis() { return this._epochMillis; }

  disable() {

    this.disabled = true;
  }

  enable() {

    this.disabled = false;
  }

  dateSelected(e) {

    const temporalObj = Temporal.PlainDateTime.from(e.target.value);
    const zonedDateTime = temporalObj.toZonedDateTime({ timeZone: getTimezone() });
    const epochMillis = zonedDateTime.toInstant().epochMilliseconds;
    const epochSeconds = epochMillis / 1000;
    this.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochMillis, epochSeconds }, bubbles: true }));
  }

  render() {

    return html`
      <input type="datetime-local"
          id="date-picker-input"
          @change=${this.dateSelected}
          value="${this.isoDate}"
          .disabled=${this.disabled}
          aria-label="${this.label}"
          title="${this.label}">
    `;
  }
}

const tagName = "sakai-date-picker";
!customElements.get(tagName) && customElements.define(tagName, SakaiDatePicker);
