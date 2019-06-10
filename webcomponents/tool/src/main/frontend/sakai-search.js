import {SakaiElement} from "./sakai-element.js";
import {SakaiPager} from "./sakai-pager.js";
import {html} from "./assets/lit-element/lit-element.js";
import {unsafeHTML} from "./assets/lit-html/directives/unsafe-html.js";

class SakaiSearch extends SakaiElement {

  constructor() {

    super();

    this.iconMapping = {
      "announcement": "icon-sakai--sakai-announcements",
      "chat": "icon-sakai--sakai-chat",
      "forums": "icon-sakai--sakai-forums",
      "lessons": "icon-sakai--sakai-lessonbuildertool",
    };

    this.searchTerms = sessionStorage.getItem("searchterms") || "";
    this.results = JSON.parse(sessionStorage.getItem("searchresults") || "[]");
    this.currentPageIndex = parseInt(sessionStorage.getItem("currentpageindex") || "0");

    this.showField = this.searchTerms.length > 3 && this.results.length > 0;

    this.loadTranslations({bundle: "search"}).then(t => { this.i18n = t; this.requestUpdate(); });
  }

  static get properties() {

    return {
      showField: Boolean,
      results: Array,
      pageSize: { attribute: "page-size", type: Number },
    };
  }

  set pageSize(newValue) {

    this._pageSize = newValue;
    this.initSetsOfResults(this.results);
  }

  get pageSize() { return this._pageSize; }

  shouldUpdate(changed) {
    return this.i18n;
  }

  render() {

    return html`
      <div class="sakai-search-input">
        ${this.showField ? html`
            <input type="text" id="sakai-search-input" tabindex="0" @keyup=${this.search} .value=${this.searchTerms} placeholder="${this.i18n["search_placeholder"]}" aria-label="${this.i18n["search_placeholder"]}"/>
          ` : html``}
        <a href="javascript:;" @click=${this.toggleField} title="${this.i18n["search_tooltip"]}"><span class="icon-sakai--sakai-search"></span></a>
      </div>
      ${this.results.length > 0 && this.showField ? html`
        <div class="sakai-search-results" tabindex="1">
          ${this.currentPageOfResults.map(r => html`
          <div class="search-result-container">
            <div>
              <i class="search-result-tool-icon ${this.iconMapping[r.tool]}" title="${r.tool}"></i>
              <a href="${r.url}">
                <span class="search-result-title">${r.title}</span>
              </a>
            </div>
            <div class="search-result">${unsafeHTML(r.searchResult)}</div>
            <div class="search-result-site-title"><span>${this.i18n["site_label"]}</span><a href="${r.siteUrl}" title="Click to visit ${this.siteTitle}">${r.siteTitle}</a></div>
          </div>
          `)}
        <sakai-pager total-things="${this.results.length}" page-size="${this.pageSize}" @page-clicked=${this.pageClicked}></sakai-pager>
        </div>
        ` : html``}
    `;
  }

  toggleField() {

    this.showField = !this.showField;
    if (!this.showField) {
      this.clear();
    }

    this.requestUpdate();

    this.updateComplete.then(() => this.querySelector("#sakai-search-input").focus());
  }

  clear() {

    sessionStorage.removeItem("searchterms");
    sessionStorage.removeItem("searchresults");
    this.results = [];
    this.searchTerms = "";
    this.requestUpdate();
  }

  search(e) {

    var keycode = e.keyCode ? e.keyCode : e.which;
    if (keycode == "13" && e.target.value.length > 3) {
      sessionStorage.setItem("searchterms", e.target.value);
      fetch(`/direct/search/search.json?searchTerms=${e.target.value}`, {cache: "no-cache", credentials: "same-origin"})
        .then(res => res.json() )
        .then(data => {

          this.results = data;
          this.results.forEach(r => { if (r.title.length === 0) r.title = r.tool; });
          this.initSetsOfResults(this.results);
          sessionStorage.setItem("searchresults", JSON.stringify(this.results));
          this.requestUpdate();
        })
        .catch(error => console.error(`Failed to search with ${e.target.value}`, error));
    } else {
      this.clear();
    }
  }

  initSetsOfResults(results) {

    this.setsOfResults = [];

    if (results.length < this.pageSize) {
      this.setsOfResults.push(results);
    } else {
      let i = 0;
      while (i < results.length) {
        if ((i + this.pageSize) < results.length) {
          this.setsOfResults.push(results.slice(i, i + this.pageSize));
        } else {
          this.setsOfResults.push(results.slice(i));
        }
        i = i + this.pageSize;
      }
    }

    this.currentPageIndex = 0;

    this.currentPageOfResults = this.setsOfResults[this.currentPageIndex];

    this.requestUpdate();
  }

  pageClicked(e) {

    this.currentPageIndex = parseInt(e.detail.page) - 1;
    this.currentPageOfResults = this.setsOfResults[this.currentPageIndex];
    this.requestUpdate();
    sessionStorage.setItem("currentpageindex", this.currentPageIndex);
  }
}

customElements.define("sakai-search", SakaiSearch);
