import {SakaiElement} from "../sakai-element.js";
import {html} from "../assets/lit-html/lit-html.js";
import {getViewPreferences, updateViewPreferences} from "../sakai-view-preferences.js";

export class GraderToggle extends SakaiElement {

  constructor() {

    super();

    this.i18n = {};
    this.loadTranslations("grader-toggle").then(i18n => this.i18n = i18n);
  }

  static get properties() {

    return {
      checked: Boolean,
      i18n: Object,
      tool: String,
    };
  }

  set tool(newValue) {

    this._tool = newValue;
    getViewPreferences(newValue).then(prefs => {

      if (!prefs) {
        this.checked = true;
      } else {
        this.prefs = JSON.parse(prefs);
        this.checked = this.prefs.usegrader;
      }
    });
  }

  get tool() { return this._tool; }

  render() {

    return html`
      <label>
        <input type="checkbox" ?checked=${this.checked} @click=${this.toggleChecked} />
        ${this.i18n["use_grader"]}
      </label>
    `;
  }

  toggleChecked(e) {

    this.prefs = this.prefs || {};
    this.prefs.usegrader = e.target.checked;
    updateViewPreferences(this.tool, JSON.stringify(this.prefs));
  }
}

customElements.define("grader-toggle", GraderToggle);
