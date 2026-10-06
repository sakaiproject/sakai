import { css, html, nothing } from "lit";
import { repeat } from "lit/directives/repeat.js";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";

export class SakaiSiteStatsResourceSearch extends SakaiShadowElement {

  static properties = {
    endpoint: { type: String },
    initialSelection: { type: Array, attribute: "initial-selection" },
    disabled: { type: Boolean, reflect: true },
    _query: { state: true },
    _items: { state: true },
    _selected: { state: true },
    _page: { state: true },
    _hasNext: { state: true },
    _selectedPage: { state: true },
    _state: { state: true },
    _announcement: { state: true },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host { display: block; color: var(--sakai-text-color-1); font-size: 1rem; }
      :host([hidden]) { display: none; }
      fieldset { border: 0; padding: 0; margin: 0; min-width: 0; }
      label { display: block; margin-block-end: 0.5rem; }
      input.form-control { width: 100%; box-sizing: border-box; font: inherit;
        background-color: var(--sakai-background-color-1); color: var(--sakai-text-color-1);
        border-color: var(--sakai-border-color); }
      input.form-control:focus { background-color: var(--sakai-background-color-1);
        color: var(--sakai-text-color-1); border-color: var(--focus-outline-color); box-shadow: none; }
      input.form-control:disabled { background-color: var(--sakai-background-color-4);
        color: var(--sakai-text-color-disabled); border-color: var(--sakai-border-color); }
      input.form-control:focus-visible { outline-color: var(--focus-outline-color); }
      .resource-list { list-style: none; padding: 0; margin: 0.5rem 0 1rem; }
      .resource-list li { display: flex; align-items: center; gap: 0.75rem;
        padding-block: 0.5rem; border-block-end: 1px solid var(--sakai-border-color); }
      .resource-info { flex: 1; min-width: 0; overflow-wrap: anywhere; }
      .resource-name, .resource-location { display: block; }
      .resource-location { color: var(--sakai-text-color-2); }
      button { flex-shrink: 0; min-height: 2.5rem; font: inherit; }
      button:focus-visible, input:focus-visible { outline: 2px solid var(--sakai-active-color-1);
        outline-offset: 2px; }
      .pages { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-block-end: 1rem; }
      h3 { font-size: 1rem; font-weight: 600; margin-block: 1rem 0.5rem; }
      .status { margin-block: 0.5rem; }
    `,
  ];

  constructor() {

    super();
    this.loadTranslations({ bundle: "sitestats", cache: false });
    this.initialSelection = [];
    this.disabled = false;
    this._query = "";
    this._items = [];
    this._selected = [];
    this._page = 0;
    this._hasNext = false;
    this._selectedPage = 0;
    this._state = "guidance";
    this._announcement = "";
    this._requestVersion = 0;
    this._requestedPage = 0;
  }

  updated(changed) {

    if (changed.has("initialSelection")) {
      this._selected = (this.initialSelection || []).map(resource => ({ ...resource }));
      this._selectedPage = 0;
    }
    if (changed.has("disabled")) {
      this._cancelSearch();
      if (this.disabled) {
        this._items = [];
        this._state = "guidance";
      } else if (this._query.trim().length >= 2) {
        this._search(0);
      }
    }
  }

  disconnectedCallback() {

    this._cancelSearch();
    this._state = "guidance";
    super.disconnectedCallback();
  }

  _cancelSearch() {

    clearTimeout(this._searchTimer);
    this._abortController?.abort();
    this._requestVersion++;
  }

  _queryChanged(event) {

    this._cancelSearch();
    this._query = event.target.value;
    this._items = [];
    this._page = 0;
    this._hasNext = false;
    this._announcement = "";
    if (this.disabled || [ ...this._query.trim() ].length < 2) {
      this._state = "guidance";
      return;
    }
    this._state = "searching";
    this._searchTimer = setTimeout(() => this._search(0), 300);
  }

  async _search(page) {

    if (this.disabled || !this.endpoint || [ ...this._query.trim() ].length < 2) return;
    this._cancelSearch();
    const version = this._requestVersion;
    const controller = new AbortController();
    this._abortController = controller;
    this._requestedPage = page;
    const pagingControl = this.renderRoot.activeElement?.closest(".result-pages button");
    this._state = "searching";
    this._announcement = "";
    const endpoint = new URL(this.endpoint, document.baseURI);
    endpoint.searchParams.set("q", this._query.trim());
    endpoint.searchParams.set("page", String(page));
    try {
      const response = await fetch(endpoint, {
        credentials: "same-origin",
        headers: { Accept: "application/json" },
        signal: controller.signal,
      });
      if (!response.ok) throw new Error("Resource search failed");
      const result = await response.json();
      if (version !== this._requestVersion || !this.isConnected) return;
      this._items = result.items.slice(0, 20);
      this._page = result.page;
      this._hasNext = result.hasNext;
      this._state = this._items.length ? "results" : "empty";
      if (pagingControl) {
        await this.updateComplete;
        if (version === this._requestVersion && this.isConnected
            && (!this.renderRoot.activeElement || this.renderRoot.activeElement === pagingControl)
            && (document.activeElement === this || document.activeElement === document.body)) {
          const control = pagingControl.isConnected && !pagingControl.matches(":disabled") ? pagingControl
            : this.renderRoot.querySelector(".result-pages button:not(:disabled)")
              || this.renderRoot.querySelector("#resource-query");
          control?.focus();
        }
      }
    } catch (error) {
      if (version === this._requestVersion && error.name !== "AbortError") {
        this._items = [];
        this._hasNext = false;
        this._state = "error";
      }
    }
  }

  _selectionChanged() {

    this.dispatchEvent(new CustomEvent("resource-selection-changed", {
      bubbles: true,
      composed: true,
      detail: { ids: this._selected.map(resource => resource.id) },
    }));
  }

  async _add(resource) {

    if (this.disabled || this._selected.some(selected => selected.id === resource.id)) return;
    this._selected = [ ...this._selected, { ...resource } ];
    this._announcement = this.tr("resource_search_added", [ resource.label ]);
    this._selectionChanged();
    await this.updateComplete;
    this.renderRoot.querySelector("#resource-query")?.focus();
  }

  async _remove(resource, index) {

    if (this.disabled) return;
    this._selected = this._selected.filter(selected => selected.id !== resource.id);
    this._selectedPage = Math.min(this._selectedPage, Math.max(0, Math.ceil(this._selected.length / 50) - 1));
    this._announcement = this.tr("resource_search_removed", [ resource.label ]);
    this._selectionChanged();
    await this.updateComplete;
    const buttons = this.renderRoot.querySelectorAll("#selected-resources button");
    (buttons[Math.min(index, buttons.length - 1)] || this.renderRoot.querySelector("#resource-query"))?.focus();
  }

  _status() {

    if (this._announcement) return this._announcement;
    if (this._state === "results") {
      return this.tr("resource_search_results", [ this._items.length ]);
    }
    return this._i18n[`resource_search_${this._state}`];
  }

  render() {

    if (!this._i18n) return nothing;
    const selectedIds = new Set(this._selected.map(resource => resource.id));
    const selected = this._selected.slice(this._selectedPage * 50, (this._selectedPage + 1) * 50);
    return html`
      <fieldset ?disabled=${this.disabled}>
        <label for="resource-query">${this._i18n.resource_search_label}</label>
        <input id="resource-query" class="form-control" type="search" maxlength="256"
               aria-describedby="resource-search-status" autocomplete="off" .value=${this._query}
               @input=${this._queryChanged}
               @keydown=${event => { if (event.key === "Enter" && !event.isComposing) event.preventDefault(); }}>
        <p id="resource-search-status" class="status" role="status" aria-live="polite">${this._status()}</p>
        ${this._state === "error" ? html`
          <button class="btn btn-secondary" type="button" @click=${() => this._search(this._requestedPage)}>
            ${this._i18n.resource_search_retry}
          </button>` : nothing}
        <ul id="resource-results" class="resource-list" aria-busy=${String(this._state === "searching")}>
          ${repeat(this._items, resource => resource.id, resource => html`
            <li>
              <span class="resource-info">
                <span class="resource-name">${resource.label}</span>
                <span class="resource-location">${resource.location}</span>
              </span>
              <button class="btn btn-secondary" type="button" ?disabled=${selectedIds.has(resource.id)}
                      aria-label=${this.tr("resource_search_add_named", [ resource.label, resource.location ])}
                      @click=${() => this._add(resource)}>
                ${selectedIds.has(resource.id) ? this._i18n.resource_search_selected : this._i18n.resource_search_add}
              </button>
            </li>`)}
        </ul>
        ${this._page > 0 || this._hasNext ? html`
          <div class="pages result-pages">
            <button class="btn btn-secondary" type="button" ?disabled=${this._page === 0 || this._state === "searching"}
                    @click=${() => this._search(this._page - 1)}>${this._i18n.resource_search_previous}</button>
            <button class="btn btn-secondary" type="button" ?disabled=${!this._hasNext || this._state === "searching"}
                    @click=${() => this._search(this._page + 1)}>${this._i18n.resource_search_more}</button>
          </div>` : nothing}
        <h3>${this.tr("resource_search_selection_count", [ this._selected.length ])}</h3>
        ${this._selected.length ? nothing : html`<p>${this._i18n.resource_search_none_selected}</p>`}
        <ul id="selected-resources" class="resource-list">
          ${repeat(selected, resource => resource.id, (resource, index) => html`
            <li>
              <span class="resource-info">
                <span class="resource-name">${resource.label}</span>
                ${resource.location ? html`<span class="resource-location">${resource.location}</span>` : nothing}
                ${resource.legacyCollection ? html`<span>${this._i18n.resource_search_legacy_collection}</span>` : nothing}
              </span>
              <button class="btn btn-secondary" type="button"
                      aria-label=${this.tr("resource_search_remove_named", [ resource.label ])}
                      @click=${() => this._remove(resource, index)}>${this._i18n.resource_search_remove}</button>
            </li>`)}
        </ul>
        ${this._selected.length > 50 ? html`
          <div class="pages">
            <button class="btn btn-secondary" type="button" ?disabled=${this._selectedPage === 0}
                    @click=${() => this._selectedPage--}>${this._i18n.resource_search_previous_selected}</button>
            <button class="btn btn-secondary" type="button" ?disabled=${(this._selectedPage + 1) * 50 >= this._selected.length}
                    @click=${() => this._selectedPage++}>${this._i18n.resource_search_more_selected}</button>
          </div>` : nothing}
      </fieldset>
    `;
  }
}
