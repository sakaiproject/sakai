import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";
import "@sakai-ui/sakai-user-picker/sakai-user-picker.js";

export class SakaiGroupPicker extends SakaiElement {

  static properties = {

    groups: { type: Array },
    siteId: { attribute: "site-id", type: String },
    groupRef: { attribute: "group-ref", type: String },
    multiple: { type: Boolean },
    selectedGroups: { attribute: "selected-groups", type: Array },
  };

  constructor() {

    super();

    this._translations = this.loadTranslations("group-picker");
  }

  connectedCallback() {

    super.connectedCallback();

    if (this.hasAttribute("bind-select")) {
      const select = this.querySelector(":scope > select");
      if (select) { this.bindSelect(select); }
    }

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

  /** Enhance a tool-owned group field without changing its form values or change handlers. */
  bindSelect(select) {
    this._translations.then(() => {
      if (!this._i18n || !select.isConnected || this._boundSelect) { return; }
      this._boundSelect = select;
      const picker = document.createElement("sakai-user-picker");
      picker.placeholder = this._i18n.search_groups;
      picker.bulkActions = select.multiple && !select.disabled && !this.hasAttribute("no-bulk-actions");
      picker.style.maxInlineSize = "32rem";
      picker.style.setProperty("--sakai-user-picker-input-basis", "7rem");
      if (this.hasAttribute("bind-select")) {
        picker.style.setProperty("--sakai-user-picker-results-position", "absolute");
      }
      if (select.parentElement !== this) {
        select.after(this);
        this.append(select);
      }
      picker.bindSelect(select);
    });
  }

  groupChanged(e) {

    const groups = this.multiple ? Array.from(e.target.selectedOptions).map(o => o.value) : [ e.target.value ];
    this.dispatchEvent(new CustomEvent("groups-selected", { detail: { value: groups }, bubbles: true }));
  }

  firstUpdated() {

    if (this.groupRef) {
      // An initial group has been specified. We only want to do this on the first update.
      this.groupChanged({ target: { value: this.groupRef } });
    }
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
