import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

/**
 * Renders an input which, when clicked, launches a Flatpickr instance. This tag relies
 * on both flatpickr.js and moment.js being in scope.
 *
 * @example <caption>Usage:</caption>
 * <sakai-date-picker epoch-millis="345922925445" @/>
 * <sakai-date-picker hours-from-now="5" />
 *
 * The tag fires the event 'datetime-selected'. You'd handle that with (vanillajs):
 *
 * sakaiDatePicker.addEventListener("datetime-selected", (e) => console.log(e.detail.epochMillis));
 *
 * @extends SakaiElement
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
class SakaiDatePicker extends SakaiElement {

  constructor() {

    super();

    this.hoursFromNow = 0;

    this.idSalt = Math.floor(Math.random() * Math.floor(1000));
    this.start = moment();

    this.i18n = {};
    this.loadTranslations("date-picker-wc").then(r => this.i18n = r);
  }

  static get properties() {

    return {
      hoursFromNow: { attribute: "hours-from-now", type: Number },
      epochMillis: { attribute: "epoch-millis", type: Number },
    };
  }

  set epochMillis(newValue) {

    this._epochMillis = newValue;

    if (newValue) {
      this.start = this.getPreferredSakaiDatetime(newValue);
    }
  }

  get epochMillis() {
    return this._epochMillis;
  }

  set hoursFromNow(newValue) {

    this._hoursFromNow = newValue;

    if (newValue) {
      this.start = moment(this.start).add(this.hoursFromNow, "hours");
    }
  }

  get hoursFromNow() {
    return this._hoursFromNow;
  }

  firstUpdated(changedProperties) {

    const self = this;

    let config = {
      enableTime: true,
      time_24hr: true,
      allowInput: true,
      defaultHour: this.start.hours(),
      defaultMinute: this.start.minutes(),
      position: "auto",
      confirmDate: {
        "enableTime": true,
        "plugins": [new confirmDatePlugin({})]
      },
      onReady() {
        this.showTimeInput = true;
        this.setDate(self.start.toDate());
      },
      onChange(selectedDates) {
        self.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochMillis: selectedDates[0].getTime() }, bubbles: true }));
      }
    };

    config.locale = window.top.portal && window.top.portal.locale ? window.top.portal.locale.split("-")[0] : "default";
    if (config.locale === "en") config.locale = "default";

    flatpickr(`#picker-${this.idSalt}`, config);
  }

  render() {

    return html`
      <input type="text" id="picker-${this.idSalt}" size="30"
          placeholder="${this.i18n["input_placeholder"]}"
          aria-label="${this.i18n["input_placeholder"]}" />
    `;
  }

  getPreferredSakaiDatetime(epochMillis) {

    if (portal.user && portal.user.offsetFromServerMillis) {
      let osTzOffset = new Date().getTimezoneOffset();
      return moment(epochMillis).add(portal.user.offsetFromServerMillis, 'ms').add(osTzOffset, 'm');
    } else {
      window.console && console.debug("No user timezone or server time set. Using agent's time and timezone for initial datetime");
      return moment();
    }
  }
}

customElements.define("sakai-date-picker", SakaiDatePicker);
