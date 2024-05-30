import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import { getViewPreferences, updateViewPreferences } from "@sakai-ui/sakai-view-preferences";

export class SakaiGraderToggle extends SakaiElement {

  static properties = {

    checked: { type: Boolean },
    uncheckedByDefault: { attribute: "unchecked-by-default", type: Boolean },
    tool: { type: String },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this._i18n = {};
    this.loadTranslations("grader-toggle").then(i18n => this._i18n = i18n);
  }

  set tool(newValue) {

    this._tool = newValue;
    getViewPreferences(newValue).then(prefs => {

      if (!prefs && !this.uncheckedByDefault) {
        this.checked = true;
      } else {
        this.prefs = JSON.parse(prefs);
        this.checked = this.prefs?.usegrader;
      }
    });
  }

  get tool() { return this._tool; }

  shouldUpdate() { return this._i18n; }

  render() {

    return html`
      <label>
        <input type="checkbox" ?checked=${this.checked} @click=${this.toggleChecked} />
        ${this._i18n.use_grader}
      </label>
    `;
  }

  toggleChecked(e) {

    this.prefs = this.prefs || {};
    this.prefs.usegrader = e.target.checked;
    updateViewPreferences(this.tool, JSON.stringify(this.prefs));
  }
}
