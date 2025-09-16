import { RubricsElement } from "./RubricsElement.js";
import { html } from "lit";
import { repeat } from "lit-html/directives/repeat.js";
import "../sakai-rubric.js";
import "@sakai-ui/sakai-pager/sakai-pager.js";
import { SharingChangeEvent } from "./SharingChangeEvent.js";

const rubricName = "name";
const rubricTitle = "title";
const rubricCreator = "creator";
const rubricModified = "modified";

export class SakaiRubricsList extends RubricsElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    enablePdfExport: { attribute: "enable-pdf-export", type: Boolean },

    _rubrics: { state: true },
    _currentPage: { state: true },
    _itemsPerPage: { state: true },
    _searchTerm: { state: true },
    _lastSortType: { state: true },
    _lastSortAscending: { state: true },
    _lastCreatedRubricId: { state: true },
    _paginatedRubrics: { state: true },
  };

  constructor() {

    super();

    this._currentPage = 1;
    this._itemsPerPage = 20;
    this._lastSortAscending = true;
  }

  set siteId(value) {

    this._siteId = value;
    this.getRubrics();
  }

  get siteId() { return this._siteId; }

  search(term) {

    this._searchTerm = term;
    this._currentPage = 1;
    this._lastCreatedRubricId = null;
    this.repage();
  }

  _getFilteredRubrics() {

    if (!this._rubrics) return [];
    if (!this._searchTerm) return this._rubrics;
    const term = this._searchTerm.toLowerCase();
    return this._rubrics.filter(r =>
      (r.id === this?._lastCreatedRubricId) ||
      (r?.title.toLowerCase().includes(term)) ||
      (r?.siteTitle.toLowerCase().includes(term)) ||
      (r?.creatorDisplayName.toLowerCase().includes(term))
    );
  }

  _onPageSelected(e) {

    this._currentPage = e.detail.page;
    this.repage();
  }

  repage() {

    const filteredRubrics = this._getFilteredRubrics();
    const filteredRubricTotal = filteredRubrics.length;
    this._totalPages = Math.ceil(filteredRubricTotal / this._itemsPerPage);
    const start = (this._currentPage - 1) * this._itemsPerPage;
    const end = start + this._itemsPerPage;
    this._paginatedRubrics = filteredRubrics.slice(start, end);
  }

  render() {

    if (!this._rubrics) {
      return html`
        <div class="sak-banner-warn">${this._i18n.loading}</div>
      `;
    }

    return html`
      <div role="presentation">
        <div role="tablist">
        ${repeat(this._paginatedRubrics || [], r => r.id, r => html`
          <sakai-rubric site-id="${this.siteId}"
              @clone-rubric=${this.cloneRubric}
              @delete-item=${this._rubricDeleted}
              .rubric=${r}
              ?enable-pdf-export=${this.enablePdfExport}>
          </sakai-rubric>
        `)}
        </div>
      </div>
      <sakai-pager
        .current=${this._currentPage}
        .count=${this._totalPages}
        @page-selected=${this._onPageSelected}
        ?hidden=${this._totalPages <= 1}>
      </sakai-pager>
      <br>
      <div class="act">
        <button type="button" class="active add-rubric" @click=${this.createNewRubric}>
          <span class="add fa fa-plus"></span>
          ${this._i18n.add_rubric}
        </button>
      </div>
    `;
  }

  refresh() {

    this.getRubrics();
  }

  getRubrics() {

    const url = `/api/sites/${this.siteId}/rubrics`;
    fetch(url, {
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Network error while loading rubrics at ${url}`);
    })
    .then(rubrics => {
      this._rubrics = rubrics;
      this._currentPage = 1;
      this.repage();
    })
    .catch (error => console.error(error));
  }

  createRubricResponse(nr) {

    nr.new = true;
    nr.expanded = true;

    // Make sure criterions are set, otherwise lit-html borks in sakai-rubric-criterion.js
    if (!nr.criterions) {
      nr.criterions = [];
    }

    this._rubrics.push(nr);

    if (this._lastSortType) {
      this.sortRubrics(this._lastSortType, this._lastSortAscending);
    }

    this._lastCreatedRubricId = nr.id;

    const index = this._getFilteredRubrics().findIndex(r => r.id === nr.id);
    if (index !== -1) {
      this._currentPage = Math.floor(index / this._itemsPerPage) + 1;
    }

    this.repage();
  }

  _rubricDeleted(e) {

    e.stopPropagation();
    this._rubrics.splice(this._rubrics.map(r => r.id).indexOf(e.detail.id), 1);

    const tmp = this._rubrics;
    this._rubrics = [];
    this._rubrics = tmp;

    this.dispatchEvent(new SharingChangeEvent());

    this.repage();
    if (this._currentPage > this._totalPages) {
      this._currentPage = Math.max(1, this._totalPages);
      this.repage();
    }
  }

  cloneRubric(e) {

    const url = `/api/sites/${this.siteId}/rubrics/${e.detail.id}/copyToSite`;
    fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while copying rubric");
    })
    .then(rubric => this.createRubricResponse(rubric))
    .catch (error => console.error(error));
  }

  createNewRubric() {

    const url = `/api/sites/${this.siteId}/rubrics/default`;
    fetch(url, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({}),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error(`Network error while creating rubric at ${url}`);
    })
    .then(rubric => this.createRubricResponse(rubric))
    .catch (error => console.error(error));
  }

  sortRubrics(rubricType, ascending) {

    this._lastSortType = rubricType;
    this._lastSortAscending = ascending;

    switch (rubricType) {
      case rubricName:
        this._rubrics.sort((a, b) => ascending ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));
        break;
      case rubricTitle:
        this._rubrics.sort((a, b) => ascending ? a.siteTitle.localeCompare(b.siteTitle) : b.siteTitle.localeCompare(a.siteTitle));
        break;
      case rubricCreator:
        this._rubrics.sort((a, b) => ascending ? a.creatorDisplayName.localeCompare(b.creatorDisplayName) : b.creatorDisplayName.localeCompare(a.creatorDisplayName));
        break;
      case rubricModified:
        this._rubrics.sort((a, b) => ascending ? a.modified - b.modified : b.modified - a.modified);
        break;
    }
    this.repage();
  }
}
