import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

class SakaiDatePicker extends SakaiElement {

  static get properties() {

    return {
      initialValue: { attribute: "initial-value", type : String },
      parseFormat: { attribute: "parse-format", type: String },
    };

  }

  constructor() {

    super();

    this.parseFormat = "YYYY-MM-DDTHH:mm:ssZ";
    this.inputId = `date-picker-${Math.floor(Math.random() * Math.floor(1000))}`;
  }

  firstUpdated(changedProperties) {

    localDatePicker({
      input: `#${this.inputId}`,
      useTime: 1,
      val: this.initialValue,
      parseFormat: this.parseFormat,
      onDateTimeSelected: (v) => this.fireEvent(v),
    });
  }

  render() {

    return html`
      <input id="${this.inputId}" type="text" />
      <div>${this.dateTime}</div>
    `;
  }

  fireEvent(v) {
    this.dispatchEvent(new CustomEvent("datetime-selected", {detail: { epochMillis : v }, bubbles: true }));
  }
}

customElements.define("sakai-date-picker", SakaiDatePicker);
