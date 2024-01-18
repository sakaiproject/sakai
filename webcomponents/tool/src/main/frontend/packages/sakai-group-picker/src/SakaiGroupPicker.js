import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";

export class SakaiGroupPicker extends SakaiElement {

  static properties = {

    groups: { type: Array },
    siteId: { attribute: "site-id", type: String },
    groupRef: { attribute: "group-ref", type: String },
    multiple: { type: Boolean },
    selectedGroups: { attribute: "selected-groups", type: Array },
    _i18n: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("group-picker").then(t => this._i18n = t);
  }

  connectedCallback() {

    super.connectedCallback();

    if (this.siteId && !this.groups) {

      const url = `/direct/site/${this.siteId}/groups.json`;
      fetch(url, { credentials: "same-origin" })
        .then(r => {
          if (r.ok) {
            return r.json();
          }
          throw new Error(`Network error while retrieving groups from ${url}`);
        })
        .then(groups => {
          this.groups = groups.map(g => ({ reference: g.reference, title: g.title }));
        })
        .catch(error => console.error(error));
    }
  }

  groupChanged(e) {

    const groups = this.multiple ? Array.from(e.target.selectedOptions).map(o => o.value) : [ e.target.value ];
    this.dispatchEvent(new CustomEvent("groups-selected", { detail: { value: groups }, bubbles: true }));
  }

  shouldUpdate() {
    return this._i18n && this.groups;
  }

  render() {

    return html`
      <select aria-label="${this._i18n.group_selector_label}" @change=${this.groupChanged} ?multiple=${this.multiple} .value=${this.selectedGroups}>
        <option value="/site/${this.siteId}" .selected=${this.siteId && this.groupRef && this.siteId === this.groupRef}>
          ${this._i18n.site}
        </option>
        ${this.groups.map(g => html`
          <option value="${g.reference}" ?selected=${this.groupRef === g.reference || (this.selectedGroups && this.selectedGroups.includes(g.reference))}>${g.title}</option>
        `)}
      </select>
    `;
  }
}
