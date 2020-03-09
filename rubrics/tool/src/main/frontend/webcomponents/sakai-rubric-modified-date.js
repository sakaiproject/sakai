import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";

export class SakaiRubricModifiedDate extends RubricsElement {

  constructor() {

    super();

    this.modified = "";
  }

  static get properties() {
    return { modified: String };
  }

  render() {

    if (moment(this.modified).isValid()) {
      let formatted = moment(this.modified).locale(this.locale).format('LLLL');
      return html`${formatted}`;
    } else {
      return html`${this.modified}`;
    }
  }
}

customElements.define("sakai-rubric-modified-date", SakaiRubricModifiedDate);
