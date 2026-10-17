import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-course-card/sakai-course-card.js";

const DENSITY_AUTOMATIC = "automatic";
const DENSITY_COMFORTABLE = "comfortable";
const DENSITY_COMPACT = "compact";
const DEFAULT_DENSITY = DENSITY_AUTOMATIC;
const DEFAULT_SORT = "title_a_to_z";
const DENSITY_OPTIONS = [ DENSITY_AUTOMATIC, DENSITY_COMFORTABLE, DENSITY_COMPACT ];

const LAYOUT_GAP = 12;
const MIN_COL_WIDTH_COMFORTABLE = 220;
const MIN_COL_WIDTH_COMPACT = 170;
const MAX_COLUMNS = 3;
const CARD_HEIGHT_COMFORTABLE = 150;
const CARD_HEIGHT_COMPACT = 112;

export class SakaiCourseList extends SakaiElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    sites: { type: Array },
    savedFilters: { attribute: false, type: Object },
    saveFilters: { attribute: "save-filters", type: Boolean },
    filtersOpen: { attribute: "filters-open", type: Boolean },

    _displayedSites: { state: true },
    _availableTerms: { state: true },
    _currentFilter: { state: true },
    _currentTermFilter: { state: true },
    _currentSort: { state: true },
    _density: { state: true },
    _alertsFirst: { state: true },
    _layoutColumns: { state: true },
    _resolvedDensity: { state: true },
  };

  constructor() {

    super();

    this.sites = [];
    this.terms = [];
    this._availableTerms = [];
    this._displayedSites = [];
    this._currentFilter = "pinned";
    this._currentTermFilter = "none";
    this._currentSort = DEFAULT_SORT;
    this._density = DEFAULT_DENSITY;
    this._alertsFirst = false;
    this.filtersOpen = false;
    this._layoutColumns = 1;
    this._resolvedDensity = "comfortable";
    this._layoutWidth = 0;
    this._layoutHeight = 0;

    this.loadTranslations({
      bundle: "courselist",
      requiredKeys: [ "no_pinned_sites_message", "no_matching_sites", "density_automatic" ],
    });
  }

  _closeFilters() {
    this.filtersOpen = false;
    this.dispatchEvent(new CustomEvent("filters-closed", { bubbles: true, composed: true }));
  }

  connectedCallback() {

    super.connectedCallback();

    this._loadData();
  }

  disconnectedCallback() {

    this._resizeObserver?.disconnect();
    this._resizeObserver = undefined;
    super.disconnectedCallback();
  }

  updated() {

    if (this._i18n && !this._resizeObserver) {
      this._observeLayout();
    }
  }

  _observeLayout() {

    const el = this.querySelector(".dashboard-widget-body");
    if (!el) {
      return;
    }

    this._resizeObserver = new ResizeObserver(entries => {
      const entry = entries[0];
      if (!entry) {
        return;
      }
      const { width, height } = entry.contentRect;
      if (Math.abs(width - this._layoutWidth) < 2 && Math.abs(height - this._layoutHeight) < 2) {
        return;
      }
      this._layoutWidth = width;
      this._layoutHeight = height;
      this._refreshLayout();
    });
    this._resizeObserver.observe(el);
  }

  _maxColumnsForWidth(width, minColWidth) {

    if (width <= 0) {
      return 1;
    }
    return Math.max(1, Math.min(MAX_COLUMNS, Math.floor((width + LAYOUT_GAP) / (minColWidth + LAYOUT_GAP))));
  }

  _rowsHeight(count, columns, cardHeight) {

    const cols = Math.max(1, columns);
    const rows = Math.ceil(count / cols);
    if (rows <= 0) {
      return 0;
    }
    return rows * cardHeight + Math.max(0, rows - 1) * LAYOUT_GAP;
  }

  _computeAutoLayout(width, height, count) {

    if (count <= 0 || count === 1) {
      return { columns: 1, density: "comfortable" };
    }

    const maxComfortable = Math.min(this._maxColumnsForWidth(width, MIN_COL_WIDTH_COMFORTABLE), count);
    const maxCompact = Math.min(this._maxColumnsForWidth(width, MIN_COL_WIDTH_COMPACT), count);

    if (height > 0) {
      for (let cols = 1; cols <= maxComfortable; cols++) {
        if (this._rowsHeight(count, cols, CARD_HEIGHT_COMFORTABLE) <= height) {
          return { columns: cols, density: "comfortable" };
        }
      }

      const comfortableAtMax = this._rowsHeight(count, maxComfortable, CARD_HEIGHT_COMFORTABLE);
      if (comfortableAtMax <= height * 1.35) {
        return { columns: maxComfortable, density: "comfortable" };
      }

      for (let cols = 1; cols <= maxCompact; cols++) {
        if (this._rowsHeight(count, cols, CARD_HEIGHT_COMPACT) <= height) {
          return { columns: cols, density: "compact" };
        }
      }

      return { columns: maxCompact, density: "compact" };
    }

    if (count <= 4) {
      return { columns: 1, density: "comfortable" };
    }
    return { columns: Math.min(2, maxComfortable), density: "comfortable" };
  }

  _refreshLayout() {

    const count = this._displayedSites?.length || 0;
    const layout = this._computeAutoLayout(this._layoutWidth, this._layoutHeight, count);
    this._layoutColumns = layout.columns;
    this._resolvedDensity = layout.density;
  }

  _resolveDensity(density) {

    return DENSITY_OPTIONS.includes(density) ? density : DEFAULT_DENSITY;
  }

  _cardDensity() {

    if (this._density === DENSITY_AUTOMATIC) {
      return this._resolvedDensity || DENSITY_COMFORTABLE;
    }
    return this._resolveDensity(this._density);
  }

  _loadData() {

    const userId = this.userId && this.userId !== "undefined" ? this.userId : globalThis.portal?.user?.id;
    if (!userId) {
      console.error("No user id available for SakaiCourseList");
      return;
    }

    const url = `/api/users/${userId}/sites?pinned=true`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get sites data from ${url}`);
      })
      .then(r => {

        this.sites = r.sites;

        this._displayedSites = r.sites;
        this.terms = r.terms;

        this.termCourses = new Map();

        this._availableTerms = [];

        for (let i = 0; i < this.sites.length; i++) {
          const site = this.sites[i];
          if (!site.course || !site.term) continue;
          if (!this.termCourses.has(site.term)) {
            this.termCourses.set(site.term, []);
          }
          this.termCourses.get(site.term).push(site);

          if (!this._availableTerms.find(t => t.name === site.term)) {
            const term = this.terms.find(t => t.name === site.term);
            if (term) this._availableTerms.push(term);
          }
        }

        this._applySavedFilters();
        this._filter();
      })
      .catch(error => console.error(error));
  }

  _applySavedFilters() {

    if (!this.savedFilters) {
      return;
    }

    const f = this.savedFilters;
    if (f.filter) {
      this._currentFilter = f.filter;
    }
    if (f.term) {
      this._currentTermFilter = f.term;
    }
    if (f.sort && f.sort !== "alerts_first") {
      this._currentSort = f.sort;
    } else if (f.sort === "alerts_first") {
      this._currentSort = DEFAULT_SORT;
      this._alertsFirst = true;
    }
    if (f.density) {
      this._density = this._resolveDensity(f.density);
    }
    if (typeof f.alertsFirst === "boolean") {
      this._alertsFirst = f.alertsFirst;
    }
  }

  _filter() {

    let filteredSites = [ ... this.sites ];

    if (this._currentFilter === "courses") {
      filteredSites = [ ...filteredSites.filter(s => s.course) ];
    }

    if (this._currentFilter === "projects") {
      filteredSites = [ ...filteredSites.filter(s => !s.course) ];
    }

    if (this._currentFilter === "active") {
      filteredSites = [ ...filteredSites.filter(s => s.tools.some(t => t.hasAlerts)) ];
    }

    if (this._currentTermFilter && this._currentTermFilter !== "none") {
      const selectedTerm = this._availableTerms.find(t => t.id === this._currentTermFilter)
        || (this.terms || []).find(t => t.id === this._currentTermFilter);
      if (selectedTerm) {
        filteredSites = [ ...filteredSites.filter(s => s.course && s.term && s.term === selectedTerm.name) ];
      } else {
        filteredSites = [];
      }
    }

    this._displayedSites = [ ...filteredSites ];
    this._applySort();
    this._refreshLayout();
  }

  _siteHasAlerts(site) {
    return Boolean(site.tools?.some(t => t.hasAlerts));
  }

  _compareByCurrentSort(a, b) {

    switch (this._currentSort) {
      case "title_z_to_a":
        return b.title.localeCompare(a.title);
      case "description_a_to_z": {
        const aKey = (a.shortDescription ?? "").trim();
        const bKey = (b.shortDescription ?? "").trim();
        if (!aKey && !bKey) return 0;
        if (!aKey) return 1;
        if (!bKey) return -1;
        return aKey.localeCompare(bKey, undefined, { sensitivity: "base" });
      }
      case "description_z_to_a": {
        const aKey = (a.shortDescription ?? "").trim();
        const bKey = (b.shortDescription ?? "").trim();
        if (!aKey && !bKey) return 0;
        if (!aKey) return 1;
        if (!bKey) return -1;
        return bKey.localeCompare(aKey, undefined, { sensitivity: "base" });
      }
      case "title_a_to_z":
      default:
        return a.title.localeCompare(b.title);
    }
  }

  _applySort() {

    const sites = [ ...this._displayedSites ];
    sites.sort((a, b) => {
      if (this._alertsFirst && this._currentFilter !== "active") {
        const aHas = this._siteHasAlerts(a);
        const bHas = this._siteHasAlerts(b);
        if (aHas !== bHas) {
          return aHas ? -1 : 1;
        }
      }
      return this._compareByCurrentSort(a, b);
    });
    this._displayedSites = sites;
  }

  _siteFilterChanged(e) {

    this._currentFilter = e.target.value;
    if (this._currentFilter === "active") {
      this._alertsFirst = false;
    }
    this._filter();
  }

  _siteSortChanged(e) {

    this._currentSort = e.target.value;
    this._applySort();
    this._refreshLayout();
  }

  _alertsFirstChanged(e) {
    this._alertsFirst = e.target.checked;
    this._applySort();
    this._refreshLayout();
  }

  _densityChanged(e) {
    this._density = this._resolveDensity(e.target.value);
    this._refreshLayout();
  }

  _termSelected(e) {

    this._currentTermFilter = e.target.value;
    this._filter();
  }

  _filterPayload() {

    return {
      filter: this._currentFilter,
      term: this._currentTermFilter,
      sort: this._currentSort,
      density: this._density,
      alertsFirst: this._alertsFirst,
    };
  }

  _resolveUserId() {

    return this.userId && this.userId !== "undefined" ? this.userId : globalThis.portal?.user?.id;
  }

  _saveFilters() {

    const userId = this._resolveUserId();
    if (!userId) {
      return;
    }

    const url = `/api/users/${userId}/dashboard/filters/courses`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(this._filterPayload()),
    }).catch(error => console.error(error));
  }

  _resetFilters() {

    const userId = this._resolveUserId();
    if (!userId) {
      return;
    }

    const url = `/api/users/${userId}/dashboard/filters/courses`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({}),
    }).catch(error => console.error(error));

    this._currentFilter = "pinned";
    this._currentTermFilter = "none";
    this._currentSort = DEFAULT_SORT;
    this._density = DEFAULT_DENSITY;
    this._alertsFirst = false;
    this._filter();
  }

  _filterToolbar() {

    return html`
      <div class="dashboard-filter-toolbar">
        <div class="dashboard-filter-toolbar-start">
          ${this.saveFilters ? html`
            <button type="button"
                class="btn btn-icon btn-sm"
                @click=${this._saveFilters}
                title="${this._i18n.save_filter}"
                aria-label="${this._i18n.save_filter}">
              <span class="si si-save fs-6" aria-hidden="true"></span>
            </button>
            <button type="button"
                class="btn btn-icon btn-sm"
                @click=${this._resetFilters}
                title="${this._i18n.reset_filter}"
                aria-label="${this._i18n.reset_filter}">
              <span class="si si-arrow-clockwise fs-6" aria-hidden="true"></span>
            </button>
          ` : nothing}
        </div>
        <button type="button"
            class="btn btn-icon btn-sm"
            @click=${this._closeFilters}
            title="${this._i18n.close_filters}"
            aria-label="${this._i18n.close_filters}">
          <span class="si si-close fs-6" aria-hidden="true"></span>
        </button>
      </div>
    `;
  }

  shouldUpdate() {
    return this._i18n;
  }

  _filtersPanel() {

    const showAlertsFirst = this._currentFilter !== "active";

    return html`
      ${this._filterToolbar()}
      <select class="w-100 mb-2" aria-label="${this._i18n.course_filter_label}" @change=${this._siteFilterChanged} ?disabled=${this.sites.length === 0}>
        <option value="pinned" ?selected=${this._currentFilter === "pinned"}>${this._i18n.all_pinned_sites}</option>
        <option value="projects" ?selected=${this._currentFilter === "projects"}>${this._i18n.pinned_projects}</option>
        <option value="courses" ?selected=${this._currentFilter === "courses"}>${this._i18n.pinned_courses}</option>
        <option value="active" ?selected=${this._currentFilter === "active"}>${this._i18n.pinned_activity}</option>
      </select>
      <select class="w-100 mb-2" id="course-list-term-filter"
          aria-label="${this._i18n.term_filter_label}" @change=${this._termSelected} ?disabled=${this._availableTerms.length === 0}>
        <option value="none" ?selected=${this._currentTermFilter === "none"}>${this._i18n.term_filter_none_option}</option>
        ${this._availableTerms.map(term => html`
          <option value="${term.id}" ?selected=${this._currentTermFilter === term.id}>${term.name}</option>
        `)}
      </select>
      <select class="w-100 mb-2" aria-label="${this._i18n.course_sort_label}" @change=${this._siteSortChanged} ?disabled=${this.sites.length === 0}>
        <option value="title_a_to_z" ?selected=${this._currentSort === "title_a_to_z"}>${this._i18n.title_a_to_z}</option>
        <option value="title_z_to_a" ?selected=${this._currentSort === "title_z_to_a"}>${this._i18n.title_z_to_a}</option>
        <option value="description_a_to_z" ?selected=${this._currentSort === "description_a_to_z"}>${this._i18n.description_a_to_z}</option>
        <option value="description_z_to_a" ?selected=${this._currentSort === "description_z_to_a"}>${this._i18n.description_z_to_a}</option>
      </select>
      ${showAlertsFirst ? html`
        <div class="form-check mb-2">
          <input class="form-check-input"
              type="checkbox"
              id="course-list-alerts-first"
              .checked=${this._alertsFirst}
              @change=${this._alertsFirstChanged}
              ?disabled=${this.sites.length === 0}>
          <label class="form-check-label" for="course-list-alerts-first">${this._i18n.alerts_first}</label>
        </div>
      ` : nothing}
      <select class="w-100 mb-2" aria-label="${this._i18n.density_label}" @change=${this._densityChanged} ?disabled=${this.sites.length === 0}>
        <option value="${DENSITY_AUTOMATIC}" ?selected=${this._density === DENSITY_AUTOMATIC}>${this._i18n.density_automatic}</option>
        <option value="${DENSITY_COMFORTABLE}" ?selected=${this._density === DENSITY_COMFORTABLE}>${this._i18n.density_comfortable}</option>
        <option value="${DENSITY_COMPACT}" ?selected=${this._density === DENSITY_COMPACT}>${this._i18n.density_compact}</option>
      </select>
    `;
  }

  render() {

    const filteredEmpty = this.sites.length > 0 && this._displayedSites.length === 0;
    const columns = Math.max(1, Math.min(MAX_COLUMNS, this._layoutColumns || 1));
    const cardDensity = this._cardDensity();

    return html`
      <div class="dashboard-widget-body">
        <div class="dashboard-widget-main courses-grid ${this.filtersOpen ? "is-blurred" : ""}"
            style="--courses-columns: ${columns}">
          ${this.sites.length === 0 ? html`
            <div class="sak-banner-info">${this._i18n.no_pinned_sites_message}</div>
          ` : nothing}
          ${filteredEmpty ? html`
            <div class="sak-banner-info">${this._i18n.no_matching_sites}</div>
          ` : nothing}
          ${this._displayedSites.map(card => html`
            <sakai-course-card density="${cardDensity}" .courseData=${card}></sakai-course-card>
          `)}
        </div>
        ${this.filtersOpen ? html`
          <div class="dashboard-filter-overlay" @click=${this._closeFilters}>
            <div class="dashboard-filter-panel" @click=${e => e.stopPropagation()}>
              ${this._filtersPanel()}
            </div>
          </div>
        ` : nothing}
      </div>
    `;
  }
}
