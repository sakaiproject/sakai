import { css, html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-icon";
import { SakaiPageableElement } from "@sakai-ui/sakai-pageable-element";
import { SakaiSitePicker } from "@sakai-ui/sakai-site-picker";
import "@sakai-ui/sakai-site-picker/sakai-site-picker.js";
import {
  TITLE_A_TO_Z,
  TITLE_Z_TO_A,
  SITE_A_TO_Z,
  SITE_Z_TO_A,
  EARLIEST_FIRST,
  LATEST_FIRST,
  INSTRUCTOR_ORDER,
} from "./sakai-announcements-constants.js";

export class SakaiAnnouncements extends SakaiPageableElement {

  static properties = {
    savedFilters: { attribute: false, type: Object },
    saveFilters: { attribute: "save-filters", type: Boolean },
    filtersOpen: { attribute: "filters-open", type: Boolean },
    _currentSort: { state: true },
    _pickerSiteId: { state: true },
  };

  constructor() {

    super();

    this.showPager = true;
    this._currentSort = LATEST_FIRST;
    this.filtersOpen = false;
    this.loadTranslations({ bundle: "announcements", requiredKeys: [ "no_announcements" ] });
  }

  _closeFilters() {
    this.filtersOpen = false;
    this.dispatchEvent(new CustomEvent("filters-closed", { bubbles: true, composed: true }));
  }

  async loadAllData() {

    const url = this.siteId ? `/api/sites/${this.siteId}/announcements`
      : "/api/users/me/announcements";

    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get announcements from ${url}`);

      })
      .then(data => {

        this.data = data.announcements;
        this.data.forEach(a => a.visible = true);
        !this.siteId && (this._sites = data.sites);
        this._applySavedFilters();
      })
      .catch (error => console.error(error));
  }

  _siteSortsAllowed() {
    return !this.siteId || this.siteId === "home";
  }

  _availableSorts() {

    const sorts = [ LATEST_FIRST, EARLIEST_FIRST, TITLE_A_TO_Z, TITLE_Z_TO_A ];
    if (this._siteSortsAllowed()) {
      sorts.push(SITE_A_TO_Z, SITE_Z_TO_A);
    }
    sorts.push(INSTRUCTOR_ORDER);
    return sorts;
  }

  _resolveSort(sort) {

    return this._availableSorts().includes(sort) ? sort : LATEST_FIRST;
  }

  _applySavedFilters() {

    const sort = this._resolveSort(this.savedFilters?.sort);
    this._currentSort = sort;
    this._sortChanged({ target: { value: sort } });

    if (!this.siteId && this.savedFilters?.siteId && this.savedFilters.siteId !== SakaiSitePicker.ALL) {
      this._pickerSiteId = this.savedFilters.siteId;
      this.data.forEach(a => a.visible = a.siteId === this.savedFilters.siteId);
      this.repage();
    }
  }

  _sitesSelected(e) {

    this._pickerSiteId = e.detail.value === SakaiSitePicker.ALL ? undefined : e.detail.value;

    if (e.detail.value === SakaiSitePicker.ALL) {
      this.data.forEach(a => a.visible = true);
    } else {
      this.data.forEach(a => a.visible = a.siteId === e.detail.value);
    }
    this.requestUpdate();
  }

  _sortChanged(e) {

    this._currentSort = this._resolveSort(e.target.value);

    switch (this._currentSort) {
      case TITLE_A_TO_Z:
        this.data.sort((a1, a2) => a1.subject.localeCompare(a2.subject));
        break;
      case TITLE_Z_TO_A:
        this.data.sort((a1, a2) => a2.subject.localeCompare(a1.subject));
        break;
      case SITE_A_TO_Z:
        this.data.sort((a1, a2) => a1.siteTitle.localeCompare(a2.siteTitle));
        break;
      case SITE_Z_TO_A:
        this.data.sort((a1, a2) => a2.siteTitle.localeCompare(a1.siteTitle));
        break;
      case EARLIEST_FIRST:
        this.data.sort((a1, a2) => {

          if (a1.date < a2.date) return -1;
          if (a1.date < a2.date) return 1;
          return 0;
        });
        break;
      case LATEST_FIRST:
        this.data.sort((a1, a2) => {

          if (a1.date < a2.date) return 1;
          if (a1.date > a2.date) return -1;
          return 0;
        });
        break;
      case INSTRUCTOR_ORDER:
        this.data.sort((a1, a2) => {

          if (a1.order < a2.order) return 1;
          if (a1.order > a2.order) return -1;
          return 0;
        });
        break;
      default:
        console.warn(`Invalid sort option: ${this._currentSort}`);
    }

    this.repage();
  }

  _resolveUserId() {

    return this.userId && this.userId !== "undefined" ? this.userId : globalThis.portal?.user?.id;
  }

  _filterPayload() {

    return {
      sort: this._currentSort,
      siteId: this._pickerSiteId || SakaiSitePicker.ALL,
    };
  }

  _saveFilters() {

    const userId = this._resolveUserId();
    if (!userId) {
      return;
    }

    const url = `/api/users/${userId}/dashboard/filters/announcements`;
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

    const url = `/api/users/${userId}/dashboard/filters/announcements`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({}),
    }).catch(error => console.error(error));

    this._pickerSiteId = undefined;
    this._currentSort = LATEST_FIRST;
    this.data.forEach(a => a.visible = true);
    this._sortChanged({ target: { value: LATEST_FIRST } });
  }

  _filtersPanel() {

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
      ${!this.siteId ? html`
        <div id="site-filter" class="mb-2">
          <sakai-site-picker
              .sites=${this._sites}
              site-id="${ifDefined(this._pickerSiteId)}"
              @sites-selected=${this._sitesSelected}>
          </sakai-site-picker>
        </div>
      ` : nothing}
      <div id="sorting">
        <select class="w-100 mb-2"
            aria-label="${this._i18n.announcement_sort_label}"
            @change=${this._sortChanged}>
          <option value="${LATEST_FIRST}" ?selected=${this._currentSort === LATEST_FIRST}>${this._i18n.latest_first}</option>
          <option value="${EARLIEST_FIRST}" ?selected=${this._currentSort === EARLIEST_FIRST}>${this._i18n.earliest_first}</option>
          <option value="${TITLE_A_TO_Z}" ?selected=${this._currentSort === TITLE_A_TO_Z}>${this._i18n.title_a_to_z}</option>
          <option value="${TITLE_Z_TO_A}" ?selected=${this._currentSort === TITLE_Z_TO_A}>${this._i18n.title_z_to_a}</option>
          ${this._siteSortsAllowed() ? html`
          <option value="${SITE_A_TO_Z}" ?selected=${this._currentSort === SITE_A_TO_Z}>${this._i18n.site_a_to_z}</option>
          <option value="${SITE_Z_TO_A}" ?selected=${this._currentSort === SITE_Z_TO_A}>${this._i18n.site_z_to_a}</option>
          ` : nothing}
          <option value="${INSTRUCTOR_ORDER}" ?selected=${this._currentSort === INSTRUCTOR_ORDER}>${this._i18n.instructor_order}</option>
        </select>
      </div>
    `;
  }

  shouldUpdate(changedProperties) {
    return this._i18n && super.shouldUpdate(changedProperties);
  }

  statusMessage() {
    const visible = (this.dataPage || []).filter(a => a.visible);
    return visible.length > 0 ? this._i18n.viewing : null;
  }

  content() {

    const visible = (this.dataPage || []).filter(a => a.visible);

    return html`
      <div class="dashboard-widget-body">
        <div class="dashboard-widget-main ${this.filtersOpen ? "is-blurred" : ""}">
          ${visible.length === 0 ? html`
            <div class="sak-banner-info">${this._i18n.no_announcements}</div>
          ` : html`
          <div class="announcements ${!this.siteId || this.siteId === "home" ? "home" : "course"}">
            <div class="header">
              <a href="javascript:;"
                  title="${this._i18n.sort_by_title_tooltip}"
                  aria-label="${this._i18n.sort_by_title_tooltip}"
                  @click=${this.sortByTitle}>
                ${this._i18n.title}
              </a>
            </div>
            ${!this.siteId || this.siteId === "home" ? html`
              <div class="header">
                <a href="javascript:;"
                    title="${this._i18n.sort_by_site_tooltip}"
                    aria-label="${this._i18n.sort_by_site_tooltip}"
                    @click=${this.sortBySite}>
                  ${this._i18n.site}
                </a>
              </div>
            ` : nothing}
            <div class="header">${this._i18n.view}</div>
          ${visible.map((a, i) => html`
            <div class="title cell ${i % 2 === 0 ? "even" : "odd"}">
              ${a.highlighted ? html`
              <sakai-icon type="favourite" size="small"></sakai-icon>
              ` : nothing}
              <span class="${ifDefined(a.highlighted ? "highlighted" : undefined)}">${a.subject}</span>
            </div>
            ${!this.siteId || this.siteId === "home" ? html`
              <div class="site cell ${i % 2 === 0 ? "even" : "odd"}">${a.siteTitle}</div>
            ` : nothing}
            <div class="url cell ${i % 2 === 0 ? "even" : "odd"}">
              <a href="${a.url}"
                  title="${this._i18n.url_tooltip}"
                  aria-label="${this._i18n.url_tooltip}">
                <sakai-icon type="right" size="small"></sakai-icon>
              </a>
            </div>
          `)}
          </div>
          `}
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

  static styles = [
    SakaiPageableElement.styles,
    css`
      a {
        color: var(--link-color);
      }
      a:hover { 
        color: var(--link-hover-color);
      }
      a:active {
        color: var(--link-active-color);
      }
      a:visited {
        color: var(--link-visited-color);
      }

      .dashboard-widget-body {
        position: relative;
        min-height: 8rem;
      }
      .dashboard-widget-main.is-blurred {
        filter: blur(4px);
        pointer-events: none;
        user-select: none;
      }
      .dashboard-filter-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: color-mix(in srgb, var(--sakai-background-color-1, #fff) 35%, transparent);
        z-index: 2;
        padding: 0.75rem;
      }
      .dashboard-filter-panel {
        width: min(100%, 22rem);
        background: var(--sakai-background-color-1, #fff);
        border: 1px solid var(--sakai-border-color, #ddd);
        border-radius: var(--bs-border-radius, 0.375rem);
        box-shadow: 0 0.5rem 1.25rem rgba(0, 0, 0, 0.12);
        padding: 0.75rem;
      }
      .dashboard-filter-toolbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 0.25rem;
        margin-bottom: 0.75rem;
      }
      .dashboard-filter-toolbar-start {
        display: flex;
        align-items: center;
        gap: 0.25rem;
      }
      .dashboard-filter-toolbar .btn-icon {
        --bs-btn-color: var(--sakai-text-color-2);
      }
      #site-filter sakai-site-picker::part(select) {
        width: 100%;
      }
      .announcements {
        display:grid;
        grid-auto-rows: minmax(10px, auto);
      }

      .home {
        grid-template-columns: 4fr 1fr 0fr;
      }

      .course {
        grid-template-columns: 4fr 0fr;
      }
        .announcements > div:nth-child(-n+3) {
          padding-bottom: 14px;
        }
        .header {
          font-weight: bold;
          padding: 0 5px 0 5px;
        }
          .header a {
            text-decoration: none;
            color: var(--sakai-text-color-1, #000);
          }
        .title {
          flex: 2;
        }
        .cell {
          display: flex;
          align-items: center;
          padding: 8px;
          font-size: var(--sakai-grades-title-font-size);
        }
        .even {
          background-color: var(--sakai-table-even-color);
        }
        .site {
          flex: 1;
        }
        .url {
          flex: 1;
        }
    `,
  ];
}
