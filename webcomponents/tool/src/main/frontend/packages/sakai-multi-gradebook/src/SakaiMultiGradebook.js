import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import "@sakai-ui/sakai-tag-selector/sakai-tag-selector.js";

/** Searchable gradebook selection with at most one item/category per gradebook. */
export class SakaiMultiGradebook extends SakaiShadowElement {

  static properties = {
    siteId: { attribute: "site-id", type: String },
    selectedTemp: { attribute: "selected-temp", type: String },
    isCategory: { attribute: "is-category", converter: value => value !== null && value !== "false" },
    userId: { attribute: "user-id", type: String },
    groupId: { attribute: "group-id", type: String },
    appName: { attribute: "app-name", type: String },
    inputId: { attribute: "input-id", type: String },
    _items: { state: true },
    _selected: { state: true },
    _loading: { state: true },
    _error: { state: true },
  };

  constructor() {
    super();
    this.selectedTemp = "";
    this.isCategory = false;
    this._items = [];
    this._selected = [];
    this._loading = true;
    this.loadTranslations("gb-selector");
  }

  updated(changed) {
    if ([ "siteId", "isCategory", "userId", "groupId", "appName" ].some(name => changed.has(name))) {
      this._loadItems();
    } else if (changed.has("selectedTemp")) {
      this._restoreSelection();
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this._request?.abort();
  }

  connectedCallback() {
    super.connectedCallback();
    if (this.hasUpdated && this._loading) { this._loadItems(); }
  }

  async _loadItems() {
    this._request?.abort();
    if (!this.siteId) { return; }
    const request = new AbortController();
    this._request = request;
    this._loading = true;
    this._error = false;
    const parts = [ "api", "sites", this.siteId, this.isCategory ? "categories" : "items" ];
    if (!this.isCategory) { parts.push(this.appName || "undefined"); }
    if (this.userId) { parts.push(this.userId); }
    if (this.groupId?.trim()) { parts.push(this.groupId); }
    try {
      const response = await fetch(`/${parts.map(encodeURIComponent).join("/")}`, { signal: request.signal });
      if (!response.ok) { throw new Error(`Unable to load gradebook items: ${response.status}`); }
      const groups = await response.json();
      if (request.signal.aborted) { return; }
      this._items = (groups ?? []).flatMap((group, index) => group.items.map(item => ({
        ...item, name: `${group.name} - ${item.name}`, gradebook: index,
      })));
      this._restoreSelection();
    } catch (error) {
      if (!request.signal.aborted) {
        this._error = true;
        console.error("Unable to load gradebook selection", error);
      }
    } finally {
      if (!request.signal.aborted) { this._loading = false; }
    }
  }

  _restoreSelection() {
    const ids = this.selectedTemp.split(",");
    this._selected = ids.map(id => this._items.find(item => String(item.id) === id)).filter(Boolean);
  }

  _selectionChanged(event) {
    event.stopPropagation();
    const groups = new Map();
    for (const tag of event.detail.value) {
      const item = this._items.find(candidate => String(candidate.id) === tag.code);
      if (item) { groups.set(item.gradebook, item); }
    }
    this._selected = [ ...groups.values() ];
    const value = this._selected.map(item => {
      const selected = { ...item };
      delete selected.gradebook;
      return selected;
    });
    const input = this.ownerDocument.getElementById(this.inputId);
    if (input) { input.value = value.map(item => item.id).join(","); }
    this.dispatchEvent(new CustomEvent("change", { detail: { value }, bubbles: true, composed: true }));
  }

  render() {
    if (!this._i18n) { return nothing; }
    if (this._error) {
      return html`<p role="alert">${this._i18n.load_error}
        <button type="button" @click=${this._loadItems}>${this._i18n.retry}</button></p>`;
    }
    if (this._loading) { return html`<span role="status">${this._i18n.loading}</span>`; }
    if (!this._items.length) { return this.isCategory ? nothing : html`<span>${this._i18n.no_items}</span>`; }
    return html`<sakai-tag-selector
      .options=${this._items.map(item => ({ code: String(item.id), name: item.name }))}
      .selectedTags=${this._selected.map(item => ({ code: String(item.id), name: item.name }))}
      @tags-changed=${this._selectionChanged}></sakai-tag-selector>`;
  }
}
