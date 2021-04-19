import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

export class SakaiGroupPicker extends SakaiElement {

  constructor() {

    super();

    this.groups = [];
    this.debug = false;
    this.loadTranslations("group-picker").then(t => this.i18n = t );
  }

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      groupRef: { attribute: "group-id", type: String },
      groups: { type: Array },
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

        this.groups = groups.map(g => ({reference: g.reference, title: g.title}));
        if (this.debug) {
          console.debug(this.groups);
        }
      });
  }

  get siteId() { return this._siteId; }

  shouldUpdate() {
    return this.i18n;
  }

  render() {

    return html`
      <select aria-label="${this.i18n["group_selector_label"]}" @change=${this.groupChanged}>
        <option value="/site/${portal.siteId}" ?selected=${this.groupRef === "/site/${portal.siteId}"}>
          ${this.i18n["site"]}
        </option>
        ${this.groups.map(g => html`<option value="${g.reference}" ?selected=${this.groupRef === g.reference}>${g.title}</option>`)}
      </select>
    `;
  }

  groupChanged(e) {
    this.dispatchEvent(new CustomEvent("group-selected", { detail: { value: e.target.value }, bubbles: true }));
  }
}

if (!customElements.get("sakai-group-picker")) {
  customElements.define("sakai-group-picker", SakaiGroupPicker);
}
