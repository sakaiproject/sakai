import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

class SakaiGroupPicker extends SakaiElement {

  constructor() {

    super();

    this.debug = false;
    this.loadTranslations("group-picker").then(t => this.i18n = t );
  }

  static get properties() {
    return {
      siteId: { attribute: "site-id", type: String },
      groups: { type: Object},
      formName: { attribute: "form-name", type: String },
      i18n: Object,
    }
  }

  /**
   * If site-id is set, this means the caller wants us to pull the groups map from the server.
   */
  set siteId(newValue) {

    this._siteId = newValue;
    fetch(`/direct/site/${newValue}/groups.json`, { credentials: "same-origin" })
      .then(r => r.json() )
      .then(groups => {

        this.groups = groups.map(g => ({id: g.id, title: g.title}));
        if (this.debug) {
          console.debug(this.groups);
        }
      });
  }

  get siteId() { return this._siteId; }

  shouldUpdate(changedProps) {
    return this.i18n && this.groups;
  }

  render() {

    return html`
      <select aria-label="${this.i18n["group_selector_label"]}">
        <option value="any">${this.i18n["any"]}</option>
        ${this.groups.map(g => html`<option value="${g.id}">${g.title}</option>`)}
      </select>
    `;
  }
}

export {SakaiGroupPicker};
customElements.define("sakai-group-picker", SakaiGroupPicker);
