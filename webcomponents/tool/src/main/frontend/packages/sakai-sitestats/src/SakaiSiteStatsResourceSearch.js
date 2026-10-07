import { css, html, nothing } from "lit";
import { repeat } from "lit/directives/repeat.js";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";

const SEARCH_LIMIT = 20;
const SELECTED_PAGE_SIZE = 50;

export class SakaiSiteStatsResourceSearch extends SakaiShadowElement {

  static formAssociated = true;

  static properties = {
    endpoint: { type: String },
    initialSelection: { type: Array, attribute: "initial-selection" },
    _disabled: { state: true },
    _query: { state: true },
    _items: { state: true },
    _selected: { state: true },
    _truncated: { state: true },
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
    this._internals = this.attachInternals();
    this._disabled = false;
    this._query = "";
    this._items = [];
    this._resources = null;
    this._selected = [];
    this._truncated = false;
    this._selectedPage = 0;
    this._state = "guidance";
    this._announcement = "";
  }

  willUpdate(changed) {

    if (changed.has("endpoint")) {
      this._cancelLoad();
      this._resources = null;
      this._search();
    }
    if (changed.has("initialSelection")) {
      this._selected = (this.initialSelection || []).map(resource => ({ ...resource }));
      this._selectedPage = 0;
      this._syncFormValue();
    }
  }

  get value() {

    return this._selected.map(resource => resource.id).join("\n");
  }

  get _canSearch() {

    return [ ...this._query.trim() ].length >= 2;
  }

  formDisabledCallback(disabled) {

    this._disabled = disabled;
    this._search();
  }

  formResetCallback() {

    this._selected = (this.initialSelection || []).map(resource => ({ ...resource }));
    this._selectedPage = 0;
    this._syncFormValue();
  }

  formStateRestoreCallback(state) {

    this._selected = JSON.parse(state);
    this._selectedPage = 0;
    this._syncFormValue();
  }

  disconnectedCallback() {

    this._cancelLoad();
    this._state = "guidance";
    super.disconnectedCallback();
  }

  _cancelLoad() {

    this._abortController?.abort();
    this._abortController = null;
  }

  _queryChanged(event) {

    this._query = event.target.value;
    this._search();
  }

  _search() {

    this._items = [];
    this._truncated = false;
    this._announcement = "";
    if (this._disabled || !this._canSearch) {
      this._state = "guidance";
      return;
    }
    if (this._resources === null) {
      this._state = "searching";
      this._loadResources();
      return;
    }
    const query = this._query.trim().toLowerCase();
    const items = [];
    for (const resource of this._resources) {
      if (!resource.label.toLowerCase().includes(query) && !resource.location.toLowerCase().includes(query)) continue;
      if (items.length === SEARCH_LIMIT) {
        this._truncated = true;
        break;
      }
      items.push(resource);
    }
    this._items = items;
    this._state = items.length ? "results" : "empty";
  }

  async _loadResources() {

    if (!this.endpoint || this._abortController) return;
    const controller = new AbortController();
    this._abortController = controller;
    this._state = "searching";
    try {
      const response = await fetch(new URL(this.endpoint, document.baseURI), {
        credentials: "same-origin",
        headers: { Accept: "application/json" },
        signal: controller.signal,
      });
      if (!response.ok) throw new Error("Resource lookup failed");
      const resources = await response.json();
      if (controller.signal.aborted || !this.isConnected) return;
      this._resources = resources;
      this._search();
    } catch (error) {
      if (!controller.signal.aborted && this.isConnected && error.name !== "AbortError") {
        this._state = this._disabled || !this._canSearch ? "guidance" : "error";
      }
    } finally {
      if (this._abortController === controller) this._abortController = null;
    }
  }

  _syncFormValue() {

    this._internals.setFormValue(this.value, JSON.stringify(this._selected));
  }

  async _add(resource) {

    if (this._selected.some(selected => selected.id === resource.id)) return;
    this._selected = [ ...this._selected, { ...resource } ];
    this._announcement = this.tr("resource_search_added", [ resource.label ]);
    this._syncFormValue();
    await this.updateComplete;
    this.renderRoot.querySelector("#resource-query")?.focus();
  }

  async _remove(resource, index) {

    this._selected = this._selected.filter(selected => selected.id !== resource.id);
    this._selectedPage = Math.min(this._selectedPage, Math.max(0, Math.ceil(this._selected.length / SELECTED_PAGE_SIZE) - 1));
    this._announcement = this.tr("resource_search_removed", [ resource.label ]);
    this._syncFormValue();
    await this.updateComplete;
    const buttons = this.renderRoot.querySelectorAll("#selected-resources button");
    (buttons[Math.min(index, buttons.length - 1)] || this.renderRoot.querySelector("#resource-query"))?.focus();
  }

  _status() {

    const truncated = this._state === "results" && this._truncated;
    if (this._announcement) {
      return this._announcement + (truncated ? ` ${this._i18n.resource_search_truncated}` : "");
    }
    if (this._state === "results") {
      return truncated ? this._i18n.resource_search_truncated
        : this.tr("resource_search_results", [ this._items.length ]);
    }
    return this._i18n[`resource_search_${this._state}`];
  }

  render() {

    if (!this._i18n) return nothing;
    const selectedIds = new Set(this._selected.map(resource => resource.id));
    const selected = this._selected.slice(this._selectedPage * SELECTED_PAGE_SIZE, (this._selectedPage + 1) * SELECTED_PAGE_SIZE);
    return html`
      <fieldset ?disabled=${this._disabled}>
        <label for="resource-query">${this._i18n.resource_search_label}</label>
        <input id="resource-query" class="form-control" type="search" maxlength="256"
               aria-describedby="resource-search-status" autocomplete="off" .value=${this._query}
               @input=${this._queryChanged}
               @keydown=${event => { if (event.key === "Enter" && !event.isComposing) event.preventDefault(); }}>
        <p id="resource-search-status" class="status" role="status" aria-live="polite">${this._status()}</p>
        ${this._state === "error" ? html`
          <button class="btn btn-secondary" type="button" @click=${() => this._loadResources()}>
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
        ${this._selected.length > SELECTED_PAGE_SIZE ? html`
          <div class="pages">
            <button class="btn btn-secondary" type="button" ?disabled=${this._selectedPage === 0}
                    @click=${() => this._selectedPage--}>${this._i18n.resource_search_previous_selected}</button>
            <button class="btn btn-secondary" type="button" ?disabled=${(this._selectedPage + 1) * SELECTED_PAGE_SIZE >= this._selected.length}
                    @click=${() => this._selectedPage++}>${this._i18n.resource_search_more_selected}</button>
          </div>` : nothing}
      </fieldset>
    `;
  }
}
