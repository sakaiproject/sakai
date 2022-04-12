import { LitElement, html } from "./assets/lit-element/lit-element.js";
import { getOffsetFromServerMillis } from "./sakai-portal-utils.js";

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
 * @author Adrian Fish <adrian.r.fish@gmail.com>
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

    this._epochMillis = value;
    this.isoDate = (new Date(value + parseInt(getOffsetFromServerMillis()))).toISOString().substring(0, 19);
  }

  get epochMillis() { return this._epochMillis; }

  disable() {

    this.disabled = true;
  }

  enable() {

    this.disabled = false;
  }

  dateSelected(e) {

    const epochMillis = ((new Date(e.target.value)).getTime());
    this.dispatchEvent(new CustomEvent("datetime-selected", { detail: { epochMillis }, bubbles: true }));
  }

  render() {

    return html`
      <input type="datetime-local"
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
