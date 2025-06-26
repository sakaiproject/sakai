import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { html } from "lit";

export class SakaiSitePicker extends SakaiShadowElement {

  static properties = {

    sites: { type: Array },
    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    multiple: { type: Boolean },
    selectedSites: { attribute: "selected-sites", type: Array },
  };

  constructor() {

    super();

    this.loadTranslations("site-picker").then(t => this._i18n = t);
  }

  connectedCallback() {

    super.connectedCallback();

    if (this.userId && !this.sites) {

      const url = `/api/users/${this.userId}/sites?pinned=true`;
      fetch(url, { credentials: "same-origin" })
        .then(r => {

          if (r.ok) {
            return r.json();
          }
          throw new Error(`Network error while retrieving sites from ${url}`);
        })
        .then(data => {
          this.sites = this.whitelist ? data.sites.filter(s => this.whitelist.includes(s.siteId)) : data.sites;
        })
        .catch(error => console.error(error));
    }
  }

  _siteChanged(e) {

    const sites = this.multiple ? Array.from(e.target.selectedOptions).map(o => o.value) : e.target.value;
    this.dispatchEvent(new CustomEvent("sites-selected", { detail: { value: sites }, bubbles: true }));
  }

  shouldUpdate() {
    return this._i18n && this.sites;
  }

  firstUpdated() {

    if (this.multiple) {
      this.renderRoot.querySelector(`option[value='${SakaiSitePicker.ALL}']`).selected = false;
    }
  }

  render() {

    return html`
      <select part="select" aria-label="${this._i18n.site_selector_label}" @change=${this._siteChanged} ?multiple=${this.multiple}>
        <option value="${SakaiSitePicker.ALL}" ?selected=${!this.selectedSites}>${this._i18n.all_pinned_sites}</option>
        ${this.sites.map(s => html`
          <option value="${s.siteId}" ?selected=${this.siteId === s.siteId || (this.selectedSites?.includes(s.siteId))}>${s.title}</option>
        `)}
      </select>
    `;
  }
}

SakaiSitePicker.ALL = "all";
