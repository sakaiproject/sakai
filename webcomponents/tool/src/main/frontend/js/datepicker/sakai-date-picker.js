import { LitElement, html, css } from "../assets/lit-element/lit-element.js";
import "../assets/flatpickr/dist/flatpickr.min.js";
import moment from "../assets/moment/dist/moment.js";
import { loadProperties } from "../sakai-i18n.js";
import { flatpickerStyles } from "./flatpicker-styles.js";
import "../assets/flatpickr/dist/plugins/confirmDate/confirmDate.js";

/**
 * Renders an input which, when clicked, launches a Flatpickr instance.
 *
 * @example <caption>Usage:</caption>
 * <sakai-date-picker epoch-millis="345922925445" @/>
 * <sakai-date-picker hours-from-now="5" />
 *
 * The tag fires the event 'datetime-selected'. You'd handle that with (vanillajs):
 *
 * sakaiDatePicker.addEventListener("datetime-selected", (e) => console.log(e.detail.epochMillis));
 *
 * @extends LitElement
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
class SakaiDatePicker extends LitElement {

  constructor() {

    super();

    this.start = moment();
    this.i18n = {};
    loadProperties("date-picker-wc").then(r => this.i18n = r);
  }

  static get properties() {

    return {
      epochMillis: { attribute: "epoch-millis", type: Number },
    };
  }

  set epochMillis(newValue) {

    this._epochMillis = newValue;

    if (newValue) {
      this.start = this.getPreferredSakaiDatetime(newValue);
      this.flatpicker.set("defaultHour", this.start.hours());
      this.flatpicker.set("defaultMinute", this.start.minutes());
      this.flatpicker.setDate(this.start.toDate());
    }
  }

  get epochMillis() { return this._epochMillis; }

  disable() {

    this.flatpicker.destroy();
    let el = this.shadowRoot.getElementById("picker").disabled = true;
    this.shadowRoot.getElementById("picker").value = this.start.format("LLLL");
  }

  enable() {

    this.shadowRoot.getElementById("picker").disabled = false;
    this.attachPicker();
  }

  attachPicker() {

    const self = this;

    let config = {
      enableTime: true,
      appendTo: this.shadowRoot,
      time_24hr: true,
      allowInput: !this.disabled,
      defaultHour: this.start.hours(),
      defaultMinute: this.start.minutes(),
      plugins: [new confirmDatePlugin({confirmIcon: `<sakai-icon type"add"></sakai-icon>`, confirmText: "Ok"})],
      onReady() {

        this.showTimeInput = true;
        this.setDate(self.start.toDate());
      },
      onChange(selectedDates, dateStr, instance) {

        self.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochMillis: selectedDates[0].getTime() }, bubbles: true }));
        //instance.close();
      }
    };

    config.locale = window.top.portal && window.top.portal.locale ? window.top.portal.locale.split("-")[0] : "default";
    if (config.locale === "en") config.locale = "default";

    let el = this.shadowRoot.getElementById("picker");
    this.flatpicker = flatpickr(el, config);
  }

  firstUpdated(changedProperties) {
    this.attachPicker();
  }

  render() {

    return html`
      <input type="text" id="picker" size="30"
          placeholder="${this.i18n["input_placeholder"]}"
          aria-label="${this.i18n["input_placeholder"]}"></input>
    `;
  }

  getPreferredSakaiDatetime(epochMillis) {

    if (typeof portal !== "undefined" && portal.user && portal.user.offsetFromServerMillis) {
      let osTzOffset = new Date().getTimezoneOffset();
      return moment(epochMillis).add(portal.user.offsetFromServerMillis, 'ms').add(osTzOffset, 'm');
    } else {
      window.console && window.console.debug("No user timezone or server time set. Using agent's time and timezone for initial datetime");
      return moment(epochMillis);
    }
  }

  static get styles() {

    return css`
      ${flatpickerStyles}
    `;
  }
}

if (!customElements.get("sakai-date-picker")) {
  customElements.define("sakai-date-picker", SakaiDatePicker);
}
