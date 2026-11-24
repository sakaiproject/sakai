import { css, html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import "@sakai-ui/sakai-icon";
import { SakaiPageableElement } from "@sakai-ui/sakai-pageable-element";
import { SakaiSitePicker } from "@sakai-ui/sakai-site-picker";
import "@sakai-ui/sakai-site-picker/sakai-site-picker.js";

const MESSAGES_MOST_TO_LEAST = "messages_most_to_least";
const MESSAGES_LEAST_TO_MOST = "messages_least_to_most";
const FORUMS_MOST_TO_LEAST = "forums_most_to_least";
const FORUMS_LEAST_TO_MOST = "forums_least_to_most";
const SITE_A_TO_Z = "site_a_to_z";
const SITE_Z_TO_A = "site_z_to_a";
const SORT_NONE = "none";

export class SakaiForums extends SakaiPageableElement {

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
    this.filtersOpen = false;
    this._currentSort = SORT_NONE;
    this.loadTranslations({ bundle: "forums", requiredKeys: [ "no_discussions" ] });
  }

  _closeFilters() {
    this.filtersOpen = false;
    this.dispatchEvent(new CustomEvent("filters-closed", { bubbles: true, composed: true }));
  }

  async loadAllData() {

    this.messagesClass = !this.siteId ? "three-col" : "two-col";

    const url = this.siteId ? `/api/sites/${this.siteId}/forums/summary` : "/api/users/current/forums/summary";
    return fetch(url)
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get forums data from ${url}`);

      })
      .then(data => {

        !this.siteId && (this.sites = data.sites);

        this._allData = data.forums;
        this.data = data.forums;
        this._applySavedFilters();

      })
      .catch (error => console.error(error));
  }

  _availableSorts() {

    const sorts = [
      SORT_NONE,
      MESSAGES_MOST_TO_LEAST,
      MESSAGES_LEAST_TO_MOST,
      FORUMS_MOST_TO_LEAST,
      FORUMS_LEAST_TO_MOST,
    ];
    if (!this.siteId) {
      sorts.push(SITE_A_TO_Z, SITE_Z_TO_A);
    }
    return sorts;
  }

  _resolveSort(sort) {

    return this._availableSorts().includes(sort) ? sort : SORT_NONE;
  }

  _applySavedFilters() {

    const sort = this._resolveSort(this.savedFilters?.sort);
    this._applySort(sort);

    if (!this.siteId && this.savedFilters?.siteId && this.savedFilters.siteId !== SakaiSitePicker.ALL) {
      this._pickerSiteId = this.savedFilters.siteId;
      this._selectedSites = this.savedFilters.siteId;
      this._currentFilter = "sites";
      this._filter();
    }
  }

  _applySort(sort) {

    this._currentSort = this._resolveSort(sort);

    switch (this._currentSort) {
      case MESSAGES_MOST_TO_LEAST:
        this.data.sort((a1, a2) => a2.messageCount - a1.messageCount);
        this.sortMessages = "10";
        break;
      case MESSAGES_LEAST_TO_MOST:
        this.data.sort((a1, a2) => a1.messageCount - a2.messageCount);
        this.sortMessages = "01";
        break;
      case FORUMS_MOST_TO_LEAST:
        this.data.sort((a1, a2) => a2.forumCount - a1.forumCount);
        this.sortForums = "10";
        break;
      case FORUMS_LEAST_TO_MOST:
        this.data.sort((a1, a2) => a1.forumCount - a2.forumCount);
        this.sortForums = "01";
        break;
      case SITE_A_TO_Z:
        this.data.sort((a1, a2) => a1.siteTitle.localeCompare(a2.siteTitle));
        this.sortSite = "ZA";
        break;
      case SITE_Z_TO_A:
        this.data.sort((a1, a2) => a2.siteTitle.localeCompare(a1.siteTitle));
        this.sortSite = "AZ";
        break;
      default:
        break;
    }

    this.repage();
  }

  _sortChanged(e) {

    this._applySort(e.target.value);
  }

  sortByMessages() {

    if (this.sortMessages === "01") {
      this._applySort(MESSAGES_MOST_TO_LEAST);
    } else {
      this._applySort(MESSAGES_LEAST_TO_MOST);
    }
  }

  sortByForums() {

    if (this.sortForums === "01") {
      this._applySort(FORUMS_MOST_TO_LEAST);
    } else {
      this._applySort(FORUMS_LEAST_TO_MOST);
    }
  }

  sortBySite() {

    if (this.sortSite === "AZ") {
      this._applySort(SITE_A_TO_Z);
    } else {
      this._applySort(SITE_Z_TO_A);
    }
  }

  _filter() {

    this.data = [ ... this._allData ];

    if (this._currentFilter === "sites" && this._selectedSites !== SakaiSitePicker.ALL) {
      const selected = this._selectedSites;
      const ids = Array.isArray(selected) ? selected : [ selected ];
      this.data = [ ...this.data.filter(s => ids.includes(s.siteId)) ];
    }

    if (this._currentSort && this._currentSort !== SORT_NONE) {
      this._applySort(this._currentSort);
    } else {
      this.repage();
    }
  }

  _sitesSelected(e) {

    this._selectedSites = e.detail.value;
    this._pickerSiteId = e.detail.value === SakaiSitePicker.ALL ? undefined : e.detail.value;
    this._currentFilter = "sites";
    this._filter();
  }

  _resolveUserId() {

    return this.userId && this.userId !== "undefined" ? this.userId : globalThis.portal?.user?.id;
  }

  _filterPayload() {

    if (this.siteId) {
      const payload = { sort: this._currentSort };
      if (this.savedFilters?.siteId) {
        payload.siteId = this.savedFilters.siteId;
      }
      return payload;
    }

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

    const url = `/api/users/${userId}/dashboard/filters/forums`;
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

    const url = `/api/users/${userId}/dashboard/filters/forums`;
    fetch(url, {
      method: "PUT",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({}),
    }).catch(error => console.error(error));

    this._pickerSiteId = undefined;
    this._selectedSites = SakaiSitePicker.ALL;
    this._currentFilter = undefined;
    this._currentSort = SORT_NONE;
    this.data = [ ...this._allData ];
    this.repage();
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
              .sites=${this.sites}
              site-id="${ifDefined(this._pickerSiteId)}"
              @sites-selected=${this._sitesSelected}>
          </sakai-site-picker>
        </div>
      ` : nothing}
      <select class="w-100 mb-2"
          .value=${this._currentSort}
          @change=${this._sortChanged}
          aria-label="${this._i18n.sort_label}">
        <option value="${SORT_NONE}">${this._i18n.sort_none}</option>
        <option value="${MESSAGES_MOST_TO_LEAST}">${this._i18n.sort_messages_most_to_least}</option>
        <option value="${MESSAGES_LEAST_TO_MOST}">${this._i18n.sort_messages_least_to_most}</option>
        <option value="${FORUMS_MOST_TO_LEAST}">${this._i18n.sort_forums_most_to_least}</option>
        <option value="${FORUMS_LEAST_TO_MOST}">${this._i18n.sort_forums_least_to_most}</option>
        ${!this.siteId ? html`
          <option value="${SITE_A_TO_Z}">${this._i18n.sort_site_a_to_z}</option>
          <option value="${SITE_Z_TO_A}">${this._i18n.sort_site_z_to_a}</option>
        ` : nothing}
      </select>
    `;
  }

  shouldUpdate(changedProperties) {
    return this._i18n && super.shouldUpdate(changedProperties);
  }

  content() {

    return html`
      <div class="dashboard-widget-body">
        <div class="dashboard-widget-main ${this.filtersOpen ? "is-blurred" : ""}">
          ${!this.dataPage?.length ? html`
            <div class="sak-banner-info">${this._i18n.no_discussions}</div>
          ` : html`
            <div class="messages ${this.messagesClass}">
              <div class="header">
                <a href="javascript:;"
                    @click=${this.sortByMessages}
                    title="${this._i18n.sort_by_messages_tooltip}"
                    aria-label="${this._i18n.sort_by_messages_tooltip}">
                  ${this._i18n.syn_private_heading}
                </a>
              </div>
              <div class="header">
                <a href="javascript:;"
                    @click=${this.sortByForums}
                    title="${this._i18n.sort_by_forums_tooltip}"
                    aria-label="${this._i18n.sort_by_forums_tooltip}">
                  ${this._i18n.syn_discussion_heading}
                </a>
              </div>
              ${!this.siteId ? html`
              <div class="header">
                <a href="javascript:;"
                    @click=${this.sortBySite}
                    title="${this._i18n.sort_by_site_tooltip}"
                    aria-label="${this._i18n.sort_by_site_tooltip}">
                  ${this._i18n.syn_site_heading}
                </a>
              </div>
              ` : nothing}
            ${this.dataPage.map((m, i) => html`
              <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.messageUrl}">${m.messageCount}</a></div>
              <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.forumUrl}">${m.forumCount}</a></div>
              ${!this.siteId ? html`
              <div class="cell ${i % 2 === 0 ? "even" : "odd"}"><a href="${m.siteUrl}">${m.siteDescription || m.siteTitle}</a></div>
              ` : nothing}
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
      .messages {
        display: grid;
        grid-auto-rows: minmax(10px, auto);
      }
        .three-col {
          grid-template-columns: 1fr 1fr 1fr;
        }
        .two-col {
          grid-template-columns: 1fr 1fr;
        }
        .messages > div:nth-child(-n+3) {
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
        .cell {
          padding: 8px;
          font-size: var(--sakai-grades-title-font-size);
        }
        .even {
          background-color: var(--sakai-table-even-color);
        }
    `,
  ];
}
