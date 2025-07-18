import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { getTimezone } from "@sakai-ui/sakai-portal-utils";
import { Temporal } from "temporal-polyfill";

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
 * @extends SakaiElement
 * @property {number} [epochMillis] The milliseconds since the unix epoch to set this datetime to
 * @property {string} [isoDate] The ISO8601 string to set this datetime to
 * @property {boolean} [_disabled] Disable the date controls
 * @property {string} [label] The a11y label to use for the aria-label and title attributes
 * @fires datetime-selected This event has epochMillis and epochSeconds as the detail object
 */
export class SakaiDatePicker extends SakaiElement {

  static properties = {

    epochMillis: { attribute: "epoch-millis", type: Number },
    isoDate: { attribute: "iso-date", type: String },
    label: { type: String },
    addHiddenFields: { attribute: "add-hidden-fields", type: Boolean },
    hiddenPrefix: { attribute: "hidden-prefix", type: String },
    _disabled: { state: true },
  };

  constructor() {

    super();

    this.hiddenPrefix = "";
    this.addEventListener('paste', this._handlePaste.bind(this));
  }

  set epochMillis(value) {

    if (value) {
      this._epochMillis = value;
      const instant = Temporal.Instant.fromEpochMilliseconds(value);
      const zonedDateTime = instant.toZonedDateTimeISO(getTimezone());
      this.isoDate = zonedDateTime.toString().substring(0, 16);
    } else {
      this._epochMillis = null;
      this.isoDate = null;
    }

    const inputDate = this.querySelector("input[type='datetime-local']");
    inputDate && (inputDate.value = this.isoDate);
  }

  get epochMillis() { return this._epochMillis; }

  set isoDate(value) {

    this._isoDate = value;

    if (value) {
      // Pad it out
      const split1 = value.split("T");
      if (split1.length == 2) {
        const dateBits = split1[0].split("-").map(bit => bit.length == 1 ? `0${bit}` : bit);
        const timeBits = split1[1].split(":").map(bit => bit.length == 1 ? `0${bit}` : bit);
        this._isoDate = `${dateBits.join("-")}T${timeBits.join(":")}`;
      }
    }
  }

  get isoDate() { return this._isoDate; }

  disable() { this._disabled = true; }

  enable() { this._disabled = false; }

  reset() {
    this.querySelector("input").value = (new Date()).toISOString().substring(0, 16);
  }

  dateSelected(e) {
    this._dateSelectedImpl(e.target.value);
  }

  _dateSelectedImpl(value) {

    const temporalObj = Temporal.PlainDateTime.from(value);
    const zonedDateTime = temporalObj.toZonedDateTime(getTimezone());
    const epochMillis = zonedDateTime.toInstant().epochMilliseconds;

    if (this.addHiddenFields) {
      this.isoDate = zonedDateTime.toString().substring(0, 16);
      this.requestUpdate();
    } else {
      const epochSeconds = epochMillis / 1000;
      this.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochMillis, epochSeconds }, bubbles: true }));
    }
  }

  _getHiddenFieldValue(hiddenFieldName) {

    const d = new Date(this.isoDate);

    switch (hiddenFieldName) {
      case "year":
        return d.getFullYear();
      case "month":
        return d.getMonth() + 1;
      case "day":
        return d.getDate();
      case "hour":
        return d.getHours();
      case "min":
        return d.getMinutes();
      default:
        return "na";
    }
  }

  _handlePaste(event) {

     if (event.clipboardData && event.clipboardData.getData('text/plain')) {
         const iso = this._parsePastedDate(event.clipboardData.getData('text/plain'));
         if (iso != null) {
           this._dateSelectedImpl(iso);
         }
     }
  }

  _parsePastedDate(dateString) {

    if (dateString == null || typeof dateString !== 'string') {
      return null;
    }

    const parts = dateString.match(/(\d{2})\/(\d{2})\/(\d{4}) (\d{2}):(\d{2}) (AM|PM)/i);

    if (!parts && parts.length != 6) {
      return null; // Invalid date string format
    }

    const month = parts[1]; // Month is 0-indexed in Date object
    const day = parts[2];
    const year = parts[3];
    let h = parseInt(parts[4], 10);
    const minutes = parts[5];
    const ampm = parts[6].toLowerCase();

    // Adjust hours for PM
    if (ampm === 'pm' && h < 12) {
      h += 12;
    }
    // Adjust hours for 12 AM (midnight)
    if (ampm === 'am' && h === 12) {
      h = 0;
    }

    let hours = h.toString();
    if (h < 10) {
      hours = "0" + hours;
    }

    return year + "-" + month + "-" + day + "T" + hours + ":" + minutes;
  }

  render() {

    return html`
      <input type="datetime-local"
          @change=${this.dateSelected}
          .value=${this.isoDate}
          .disabled=${this._disabled}
          max="2099-12-31T23:59"
          aria-label="${this.label}"
          title="${this.label}">
      ${this.addHiddenFields && this.isoDate ? html`
        ${SakaiDatePicker.hiddenFieldNames.map(h => html`
          <input type="hidden" name="${this.hiddenPrefix}${h}" value="${this._getHiddenFieldValue(h)}">
        `)}
      ` : ""}
    `;
  }
}

SakaiDatePicker.hiddenFieldNames = [ "year", "month", "day", "hour", "min" ];
