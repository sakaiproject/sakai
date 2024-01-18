import { html, css, LitElement } from "lit";
import "@sakai-ui/sakai-icon";
import "@sakai-ui/sakai-course-card";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiCourseList extends LitElement {

  static properties = {

    userId: { attribute: "user-id", type: String },
    sites: { type: Array },

    _displayedSites: { state: true },
    _currentFilter: { state: true },
    _currentTermFilter: { state: true },
  };

  constructor() {

    super();

    this.sites = [];
    this.terms = [];
    this._displayedSites = [];
    this._currentFilter = "all";
    this._currentTermFilter = "none";
    loadProperties("courselist").then(r => this.i18n = r);
  }

  set userId(value) {

    const old = this.userId;
    this._userId = value;
    this.requestUpdate("userId", old);
    this.loadData();
  }

  get userId() { return this._userId; }

  loadData() {

    const url = `/api/users/${this.userId}/sites`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get sites data from ${url}`);

      })
      .then(r => {

        this.sites = r.sites;
        this.terms = r.terms;

        this.termCourses = new Map();

        for (let i = 0; i < this.sites.length; i++) {
          if (!this.sites[i].course) continue;
          const site = this.sites[i];
          if (!this.termCourses.has(site.term)) {
            this.termCourses.set(site.term, []);
          }
          this.termCourses.get(site.term).push(site);
        }
      })
      .catch(error => console.error(error));
  }

  set sites(value) {

    this._displayedSites = value;

    this._sites = value;

    this.filtered = {};
    this.filtered.all = [];
    this.filtered.favourites = [];
    this.filtered.courses = [];
    this.filtered.projects = [];
    this.filtered.active = [];

    this._sites.forEach(cd => {

      this.filtered.all.push(cd);

      if (cd.favourite) {
        this.filtered.favourites.push(cd);
      }
      if (cd.course) {
        this.filtered.courses.push(cd);
      }
      if (cd.project) {
        this.filtered.projects.push(cd);
      }
      if (cd.alerts && cd.alerts.length > 0) {
        this.filtered.active.push(cd);
      }
    });
  }

  get sites() { return this._sites; }

  shouldUpdate() {
    return this.i18n;
  }

  siteFilterChanged(e) {

    this._displayedSites = this.filtered[e.target.value];
    this._currentFilter = e.target.value;
    this._currentTermFilter = "none";
  }

  addFavourite(e) {

    const newFave = this.sites.find(cd => cd.id === e.detail.id);
    newFave.favourite = true;
    this.filtered.favourites.push(newFave);
  }

  removeFavourite(e) {

    const oldFaveIndex = this.filtered.favourites.findIndex(cd => cd.id === e.detail.id);
    this.filtered.favourites.splice(oldFaveIndex, 1)[0].favourite = false;
    if (this._currentFilter === "favourites") {
      this._displayedSites = [ ...this.filtered.favourites ];
    }
  }

  siteSortChanged(e) {

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

  termSelected(e) {

    this._displayedSites = this.termCourses.get(e.target.value);
    //this._currentFilter = "term";
  }

  render() {

    return html`
      <div id="course-list-controls">
        <div id="filter">
          <select aria-label="Course filter" @change=${this.siteFilterChanged} .value=${this._currentFilter}>
            <option value="all">${this.i18n.view_all_sites}</option>
            <option value="favourites">${this.i18n.favourites}</option>
            <option value="projects">${this.i18n.all_projects}</option>
            <option value="courses">${this.i18n.all_courses}</option>
            <option value="active">${this.i18n.new_activity}</option>
            <option value="term">Term</option>
          </select>
        </div>
        <div id="term-filter">
          <select aria-label="Term filter" @change=${this.termSelected} .value=${this._currentTermFilter}>
            <option value="none">Choose a term</option>
            ${this.terms.map(r => html`<option value="${r.id}">${r.name}</option>`)}
          </select>
          </div>
        <div id="sort">
          <select aria-label="Sort courses" @change=${this.siteSortChanged}>
            <option value="title_a_to_z">${this.i18n.title_a_to_z}</option>
            <option value="title_z_to_a">${this.i18n.title_z_to_a}</option>
            <option value="code_a_to_z">${this.i18n.code_a_to_z}</option>
            <option value="code_z_to_a">${this.i18n.code_z_to_a}</option>
          </select>
        </div>
      </div>
      <div>
        ${this._displayedSites.map(cd => html`<sakai-course-card @favourited=${this.addFavourite} @unfavourited=${this.removeFavourite} course-data="${JSON.stringify(cd)}">`)}
      </div>
    `;
  }

  static styles = css`
    :host {
      display: block;
      background-color: var(--sakai-tool-bg-color);
      width: var(--sakai-course-card-width);
    }

    sakai-course-card {
      margin-top: var(--sakai-course-list-course-top-margin);
    }

    #course-list-controls {
      display: flex;
      justify-content: space-between;
    }
      #filter {
        flex: 1;
        text-align: left;
      }
      #term-filter {
        flex: 1;
      #sort {
        flex: 1;
        text-align: right;
      }
  `;
}
