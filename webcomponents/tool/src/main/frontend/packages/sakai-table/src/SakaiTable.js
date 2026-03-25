import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-icon";
import "@sakai-ui/sakai-pager/sakai-pager.js";

/**
 * A reusable, accessible data table for Sakai.
 *
 * Modes:
 *   - Client-side: set `data` (Array) and `columns`. Sorting, search, and
 *     pagination are handled entirely in the browser.
 *   - Server-side: set `url` (String) and `columns`. The component fetches
 *     `url?page=N&pageSize=N&sortField=F&sortOrder=asc|desc&search=Q`.
 *     The endpoint must return `{ data: [], total: N }`.
 *
 * Attributes / Properties
 *   columns        {Array}   Required. [{ label, field, sortable?, hidden? }]
 *   data           {Array}   Client-side row objects.
 *   url            {String}  Server-side fetch URL.
 *   page-size      {Number}  Rows per page (default 20). 0 = no pagination.
 *   searchable     {Boolean} Show the search input.
 *   selectable     {Boolean} Show row checkboxes.
 *   loading        {Boolean} Show a loading spinner (auto-managed in server mode).
 *
 * Slots
 *   toolbar        Extra controls rendered in the toolbar (e.g. group dropdowns).
 *   actions        Per-row action cell content. The row object is passed as
 *                  `.rowData` on the slot host so consumers can read it.
 *
 * Events
 *   sakai-table-sort              { field, order }
 *   sakai-table-page              { page }
 *   sakai-table-search            { search }
 *   sakai-table-selection-changed { selected }  — array of selected row objects
 */
export class SakaiTable extends SakaiElement {

  static properties = {
    columns:    { type: Array },
    data:       { type: Array },
    url:        { type: String },
    pageSize:   { attribute: "page-size", type: Number },
    searchable: { type: Boolean },
    selectable: { type: Boolean },
    loading:    { type: Boolean },

    _displayData:  { state: true },
    _totalRows:    { state: true },
    _currentPage:  { state: true },
    _sortField:    { state: true },
    _sortOrder:    { state: true },
    _search:       { state: true },
    _selected:     { state: true },
    _allSelected:  { state: true },
  };

  constructor() {

    super();

    this.pageSize = 20;
    this.searchable = false;
    this.selectable = false;
    this.loading = false;
    this.columns = [];

    this._displayData = [];
    this._totalRows = 0;
    this._currentPage = 1;
    this._sortField = null;
    this._sortOrder = "asc";
    this._search = "";
    this._selected = new Set();
    this._allSelected = false;
  }

  connectedCallback() {

    super.connectedCallback();
    this.loadTranslations("sakai-table");
  }

  updated(changed) {

    if (changed.has("data") && this.data && !this.url) {
      this._totalRows = this.data.length;
      this._applyClientSide();
    }

    if (changed.has("url") && this.url) {
      this._fetchFromServer();
    }
  }

  // ─── Client-side helpers ──────────────────────────────────────────────────

  _applyClientSide() {

    let rows = [ ...(this.data || []) ];

    if (this._search) {
      const q = this._search.toLowerCase();
      rows = rows.filter(row =>
        this.columns.some(col => String(row[col.field] ?? "").toLowerCase().includes(q))
      );
    }

    if (this._sortField) {
      rows.sort((a, b) => {
        const va = a[this._sortField] ?? "";
        const vb = b[this._sortField] ?? "";
        const cmp = String(va).localeCompare(String(vb), undefined, { numeric: true, sensitivity: "base" });
        return this._sortOrder === "asc" ? cmp : -cmp;
      });
    }

    this._totalRows = rows.length;

    if (this.pageSize > 0) {
      const start = (this._currentPage - 1) * this.pageSize;
      this._displayData = rows.slice(start, start + this.pageSize);
    } else {
      this._displayData = rows;
    }

    this._selected = new Set();
    this._allSelected = false;
  }

  // ─── Server-side helpers ──────────────────────────────────────────────────

  async _fetchFromServer() {

    this.loading = true;

    const params = new URLSearchParams();
    if (this.pageSize > 0) {
      params.set("page", this._currentPage);
      params.set("pageSize", this.pageSize);
    }
    if (this._sortField) {
      params.set("sortField", this._sortField);
      params.set("sortOrder", this._sortOrder);
    }
    if (this._search) {
      params.set("search", this._search);
    }

    try {
      const response = await fetch(`${this.url}?${params}`);
      if (!response.ok) throw new Error(`sakai-table fetch failed: ${response.status}`);
      const json = await response.json();
      this._displayData = json.data ?? [];
      this._totalRows = json.total ?? this._displayData.length;
    } catch (e) {
      console.error(e);
      this._displayData = [];
    } finally {
      this.loading = false;
    }

    this._selected = new Set();
    this._allSelected = false;
  }

  // ─── User interactions ────────────────────────────────────────────────────

  _onSortHeader(field) {

    if (this._sortField === field) {
      this._sortOrder = this._sortOrder === "asc" ? "desc" : "asc";
    } else {
      this._sortField = field;
      this._sortOrder = "asc";
    }
    this._currentPage = 1;

    this.dispatchEvent(new CustomEvent("sakai-table-sort", {
      detail: { field: this._sortField, order: this._sortOrder },
      bubbles: true,
    }));

    this.url ? this._fetchFromServer() : this._applyClientSide();
  }

  _onSearch(e) {

    this._search = e.target.value;
    this._currentPage = 1;

    this.dispatchEvent(new CustomEvent("sakai-table-search", {
      detail: { search: this._search },
      bubbles: true,
    }));

    this.url ? this._fetchFromServer() : this._applyClientSide();
  }

  _onPageSelected(e) {

    this._currentPage = e.detail.page;

    this.dispatchEvent(new CustomEvent("sakai-table-page", {
      detail: { page: this._currentPage },
      bubbles: true,
    }));

    this.url ? this._fetchFromServer() : this._applyClientSide();
  }

  _onSelectAll(e) {

    this._allSelected = e.target.checked;

    if (this._allSelected) {
      this._selected = new Set(this._displayData);
    } else {
      this._selected = new Set();
    }

    this._fireSelectionChanged();
  }

  _onSelectRow(e, row) {

    const next = new Set(this._selected);
    if (e.target.checked) {
      next.add(row);
    } else {
      next.delete(row);
    }
    this._selected = next;
    this._allSelected = this._selected.size === this._displayData.length;
    this._fireSelectionChanged();
  }

  _fireSelectionChanged() {

    this.dispatchEvent(new CustomEvent("sakai-table-selection-changed", {
      detail: { selected: [ ...this._selected ] },
      bubbles: true,
    }));
  }

  // ─── Render helpers ───────────────────────────────────────────────────────

  _sortIcon(field) {

    if (this._sortField !== field) {
      return html`<sakai-icon type="sort" size="small"></sakai-icon>`;
    }
    return this._sortOrder === "asc"
      ? html`<sakai-icon type="sort-asc" size="small"></sakai-icon>`
      : html`<sakai-icon type="sort-desc" size="small"></sakai-icon>`;
  }

  _pageCount() {

    if (!this.pageSize || this.pageSize <= 0) return 0;
    return Math.ceil(this._totalRows / this.pageSize);
  }

  _hasActions() {

    return !!this.querySelector("[slot='actions']");
  }

  // ─── Template ─────────────────────────────────────────────────────────────

  render() {

    if (!this._i18n) return nothing;

    const visibleCols = this.columns.filter(c => !c.hidden);
    const pageCount = this._pageCount();
    const hasActions = this._hasActions();

    return html`
      <div class="sakai-table-wrapper">

        <div class="sakai-table-toolbar d-flex flex-wrap align-items-center gap-2 mb-2">

          <slot name="toolbar"></slot>

          ${this.searchable ? html`
            <div class="sakai-table-search ms-auto">
              <label class="visually-hidden" for="sakai-table-search-${this._uid}">
                ${this._i18n.search}
              </label>
              <div class="input-group input-group-sm">
                <span class="input-group-text">
                  <sakai-icon type="search" size="small"></sakai-icon>
                </span>
                <input
                  id="sakai-table-search-${this._uid}"
                  type="search"
                  class="form-control"
                  .value=${this._search}
                  placeholder="${this._i18n.search}"
                  @input=${this._onSearch}
                  aria-label="${this._i18n.search}"
                />
              </div>
            </div>
          ` : nothing}
        </div>

        <div class="table-responsive">
          <table class="table table-hover table-striped table-bordered">
            <thead>
              <tr>
                ${this.selectable ? html`
                  <th scope="col" class="sakai-table-select-col">
                    <input
                      type="checkbox"
                      class="form-check-input"
                      .checked=${this._allSelected}
                      @change=${this._onSelectAll}
                      aria-label="${this._i18n.select_all}"
                    />
                  </th>
                ` : nothing}

                ${visibleCols.map(col => html`
                  <th
                    scope="col"
                    class=${col.sortable ? "sakai-table-sortable" : ""}
                    aria-sort=${this._sortField === col.field
                      ? (this._sortOrder === "asc" ? "ascending" : "descending")
                      : "none"}
                  >
                    ${col.sortable ? html`
                      <button
                        type="button"
                        class="btn btn-link p-0 text-start fw-semibold text-nowrap sakai-table-sort-btn"
                        @click=${() => this._onSortHeader(col.field)}
                        aria-label="${col.label} ${this._i18n.sort}"
                      >
                        ${col.label}
                        ${this._sortIcon(col.field)}
                      </button>
                    ` : html`${col.label}`}
                  </th>
                `)}

                ${hasActions ? html`
                  <th scope="col" class="sakai-table-actions-col">
                    ${this._i18n.actions}
                  </th>
                ` : nothing}
              </tr>
            </thead>

            <tbody>
              ${this.loading ? html`
                <tr>
                  <td colspan="${visibleCols.length + (this.selectable ? 1 : 0) + (hasActions ? 1 : 0)}"
                      class="text-center py-4">
                    <div class="spinner-border spinner-border-sm text-secondary" role="status">
                      <span class="visually-hidden">${this._i18n.loading}</span>
                    </div>
                  </td>
                </tr>
              ` : nothing}

              ${!this.loading && this._displayData.length === 0 ? html`
                <tr>
                  <td colspan="${visibleCols.length + (this.selectable ? 1 : 0) + (hasActions ? 1 : 0)}"
                      class="text-center py-4 text-muted">
                    ${this._i18n.no_data}
                  </td>
                </tr>
              ` : nothing}

              ${!this.loading ? this._displayData.map(row => html`
                <tr class=${this._selected.has(row) ? "table-active" : ""}>
                  ${this.selectable ? html`
                    <td class="sakai-table-select-col">
                      <input
                        type="checkbox"
                        class="form-check-input"
                        .checked=${this._selected.has(row)}
                        @change=${e => this._onSelectRow(e, row)}
                        aria-label="${this._i18n.select_row}"
                      />
                    </td>
                  ` : nothing}

                  ${visibleCols.map(col => html`
                    <td data-label="${col.label}">${row[col.field] ?? ""}</td>
                  `)}

                  ${hasActions ? html`
                    <td class="sakai-table-actions-col">
                      <slot name="actions" .rowData=${row}></slot>
                    </td>
                  ` : nothing}
                </tr>
              `) : nothing}
            </tbody>
          </table>
        </div>

        ${pageCount > 1 ? html`
          <div class="sakai-table-pager d-flex justify-content-center mt-2">
            <sakai-pager
              count="${pageCount}"
              current="${this._currentPage}"
              @page-selected=${this._onPageSelected}
            ></sakai-pager>
          </div>
        ` : nothing}

      </div>
    `;
  }

  // Unique ID per instance for label association
  _uid = Math.random().toString(36).slice(2, 9);
}
