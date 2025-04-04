import { html, nothing } from "lit";
import { SakaiElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-icon/sakai-icon.js";
import "@sakai-ui/sakai-course-card/sakai-course-card.js";

export class SakaiCourseList extends SakaiElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    sites: { type: Array },

    _displayedSites: { state: true },
    _availableTerms: { state: true },
    _currentFilter: { state: true },
    _currentTermFilter: { state: true },
  };

  constructor() {

    super();

    this.sites = [];
    this.terms = [];
    this._availableTerms = [];
    this._displayedSites = [];
    this._currentFilter = "pinned";
    this._currentTermFilter = "none";

    this.loadTranslations("courselist");
  }

  connectedCallback() {

    super.connectedCallback();

    this._loadData();
  }

  _loadData() {

    const url = `/api/users/${this.userId}/sites?pinned=true`;
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
            this._availableTerms.push(this.terms.find(t => t.name === site.term));
          }
        }
      })
      .catch(error => console.error(error));
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

    if (this._selectedTerm) {
      filteredSites = [ ...filteredSites.filter(s => s.course && s.term && s.term === this._selectedTerm) ];
    }

    this._displayedSites = [ ...filteredSites ];
  }

  _siteFilterChanged(e) {

    this._currentFilter = e.target.value;
    this._filter();
  }

  _siteSortChanged(e) {

    this._displayedSites.sort((a, b) => {

      switch (e.target.value) {
        case "title_a_to_z":
          return a.title.localeCompare(b.title);
        case "title_z_to_a":
          return b.title.localeCompare(a.title);
        case "code_a_to_z":
          return a.code.localeCompare(b.code);
        case "code_z_to_a":
          return b.code.localeCompare(a.code);
        default:
          return 0;
      }
    });


    this.requestUpdate();
  }

  _termSelected(e) {

    this._selectedTerm = e.target.value;
    if (this._selectedTerm === "none") this._selectedTerm = undefined;
    this._filter();
  }

  shouldUpdate() {
    return this._i18n;
  }

  render() {

    return html`
      <div>
        <select class="w-100 mb-1" aria-label="${this._i18n.course_filter_label}" @change=${this._siteFilterChanged} .value=${this._currentFilter} ?disabled=${this.sites.length === 0}>
          <option value="pinned">${this._i18n.all_pinned_sites}</option>
          <option value="projects">${this._i18n.pinned_projects}</option>
          <option value="courses">${this._i18n.pinned_courses}</option>
          <option value="active">${this._i18n.pinned_activity}</option>
        </select>
        <select class="w-100 mb-1" id="course-list-term-filter"
            aria-label="${this._i18n.term_filter_label}" @change=${this._termSelected} .value=${this._currentTermFilter} ?disabled=${this._availableTerms.length === 0}>
          <option value="none">${this._i18n.term_filter_none_option}</option>
          ${this._availableTerms.map(term => html`
            <option value="${term.id}">${term.name}</option>
          `)}
        </select>
        <select class="w-100" aria-label="${this._i18n.course_sort_label}" @change=${this._siteSortChanged} ?disabled=${this.sites.length === 0}>
          <option value="title_a_to_z">${this._i18n.title_a_to_z}</option>
          <option value="title_z_to_a">${this._i18n.title_z_to_a}</option>
          <option value="code_a_to_z">${this._i18n.code_a_to_z}</option>
          <option value="code_z_to_a">${this._i18n.code_z_to_a}</option>
        </select>
      </div>
      <div>
        ${this.sites.length === 0 ? html`
          <div class="sak-banner-info">${this._i18n.no_pinned_sites_message}</div>
        ` : nothing}
        ${this._displayedSites.map(card => html`
          <sakai-course-card class="mt-3" .courseData=${card}></sakai-course-card>
        `)}
      </div>
    `;
  }
}
