import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";

export class SakaiGroupPicker extends SakaiElement {

  static properties = {

    groups: { type: Array },
    siteId: { attribute: "site-id", type: String },
    groupRef: { attribute: "group-ref", type: String },
    multiple: { type: Boolean },
    selectedGroups: { attribute: "selected-groups", type: Array },
    _boundSelect: { state: true },
  };

  constructor() {

    super();

    this.loadTranslations("group-picker");
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

  /** Enhance a tool-owned filter without changing its form values or change handlers. */
  bindSelect(select) {
    this._boundSelect = select;
    select.after(this);
  }

  _filterBoundOptions(event) {
    const query = event.target.value.trim().toLocaleLowerCase();
    for (const option of this._boundSelect.options) {
      option.hidden = !option.text.toLocaleLowerCase().includes(query);
    }
  }

  _setBoundSelection(selected) {
    for (const option of this._boundSelect.options) {
      if (!option.hidden && !option.disabled) {
        option.selected = selected;
      }
    }
    this._boundSelect.dispatchEvent(new Event("change", { bubbles: true }));
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
    return this._i18n && (this._boundSelect || this.groups);
  }

  render() {
    if (this._boundSelect) {
      return html`
        <div class="d-flex flex-wrap align-items-center gap-2">
          <input type="search" class="form-control w-auto" aria-label=${this._i18n.search_groups}
              placeholder=${this._i18n.search_groups} @input=${this._filterBoundOptions}
              @keydown=${event => { if (event.key === "Enter" && !event.isComposing) { event.preventDefault(); } }}>
          ${this._boundSelect}
          ${this._boundSelect.multiple ? html`
            <button type="button" class="btn btn-secondary" @click=${() => this._setBoundSelection(true)}>
              ${this._i18n.select_matching}
            </button>
            <button type="button" class="btn btn-secondary" @click=${() => this._setBoundSelection(false)}>
              ${this._i18n.clear_matching}
            </button>
          ` : ""}
        </div>
      `;
    }

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
