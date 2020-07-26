import { html, css, LitElement } from "./assets/lit-element/lit-element.js";
import "./sakai-icon.js";
import "./sakai-course-card.js";
import { loadProperties, tr } from "./sakai-i18n.js";

export class SakaiCourseList extends LitElement {

  static get styles() {

    return css`
      :host {
        display: block;
        background-color: var(--sakai-course-list-bg-color, rgba(230,230,230,1));
        width: var(--sakai-course-card-width, 402px);
        font-family: var(--sakai-font-family, roboto, arial, sans-serif);
      }

      .course-cards {
        margin-top: 20px;
      }

      sakai-course-card {
        margin-top: var(--sakai-course-list-course-top-margin, 10px);
      }

      #course-list-controls {
        display: flex;
        justify-content: space-between;
      }
        #filter {
          flex: 1;
          text-align: left;
        }
        #sort {
          flex: 1;
          text-align: right;
        }
    `;
  }

  static get properties() {

    return {
      courseData: { attribute: "course-data", type: Array },
      i18n: Object,
      displayedCourses: Array,
    }
  }

  constructor() {

    super();
    this.courseData = [];
    this.displayedCourses = [];
    this.currentFilter = "all";
    loadProperties("courselist").then(r => this.i18n = r);
  }

  set courseData(value) {

    this.displayedCourses = value;

    this._courseData = value;

    this._filtered = {};
    this._filtered.all = [];
    this._filtered.favourites = [];
    this._filtered.courses = [];
    this._filtered.projects = [];
    this._filtered.active = [];

    this._courseData.forEach(cd => {

      this._filtered.all.push(cd);

      if (cd.favourite) {
        this._filtered.favourites.push(cd);
      }
      if (cd.course) {
        this._filtered.courses.push(cd);
      }
      if (cd.project) {
        this._filtered.projects.push(cd);
      }
      if (cd.alerts && cd.alerts.length > 0) {
        this._filtered.active.push(cd);
      }
    });
  }

  get courseData() { return this._courseData; }

  shouldUpdate(changed) {
    return this.i18n;
  }

  siteFilterChanged(e) {

    this.displayedCourses = this._filtered[e.target.value];
    this.currentFilter = e.target.value;
  }

  addFavourite(e) {

    let newFave = this.courseData.find(cd => cd.id === e.detail.id);
    newFave.favourite = true;
    this._filtered.favourites.push(newFave);
  }

  removeFavourite(e) {

    let oldFaveIndex = this._filtered.favourites.findIndex(cd => cd.id === e.detail.id);
    this._filtered.favourites.splice(oldFaveIndex, 1)[0].favourite = false;
    if (this.currentFilter === "favourites") {
      this.displayedCourses = [...this._filtered.favourites];
    }
  }

  siteSortChanged(e) {

    this.displayedCourses.sort((a, b) => {

      switch (e.target.value) {
        case "title_a_to_z":
          return a.title.localeCompare(b.title, "en");
        case "title_z_to_a":
          return b.title.localeCompare(a.title, "en");
        case "code_a_to_z":
          return a.code.localeCompare(b.code, "en");
        case "code_z_to_a":
          return b.code.localeCompare(a.code, "en");
        default:
          return 0;
      }
    });


    this.requestUpdate();
  }

  render() {

    return html`
      <div id="course-list-controls">
        <div id="filter">
          <select aria-label="Course filter" @change=${this.siteFilterChanged}>
            <option value="all">${this.i18n["view_all_sites"]}</option>
            <option value="favourites">${this.i18n["favourites"]}</option>
            <option value="projects">${this.i18n["all_projects"]}</option>
            <option value="courses">${this.i18n["all_courses"]}</option>
            <option value="active">${this.i18n["new_activity"]}</option>
          </select>
        </div>
        <div id="sort">
          <select aria-label="Sort courses" @change=${this.siteSortChanged}>
            <option value="title_a_to_z">${this.i18n["title_a_to_z"]}</option>
            <option value="title_z_to_a">${this.i18n["title_z_to_a"]}</option>
            <option value="code_a_to_z">${this.i18n["code_a_to_z"]}</option>
            <option value="code_z_to_a">${this.i18n["code_z_to_a"]}</option>
          </select>
        </div>
      </div>
      <div>
        ${this.displayedCourses.map(cd => html`<sakai-course-card @favourited=${this.addFavourite} @unfavourited=${this.removeFavourite} course-data="${JSON.stringify(cd)}">`)}
      </div>
    `;
  }
}

if (!customElements.get("sakai-course-list")) {
  customElements.define("sakai-course-list", SakaiCourseList);
}
