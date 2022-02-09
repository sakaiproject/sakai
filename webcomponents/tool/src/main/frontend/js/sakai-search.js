import { SakaiElement } from "./sakai-element.js";
import "./sakai-pager.js";
import { html } from "./assets/lit-element/lit-element.js";
import { unsafeHTML } from "./assets/lit-html/directives/unsafe-html.js";

class SakaiSearch extends SakaiElement {

  constructor() {

    super();

    this.pageSize = 10;

    this.iconMapping = {
      "announcement": "icon-sakai--sakai-announcements",
      "assignments": "icon-sakai--sakai-assignment-grades",
      "chat": "icon-sakai--sakai-chat",
      "forums": "icon-sakai--sakai-forums",
      "lessons": "icon-sakai--sakai-lessonbuildertool",
      "commons": "icon-sakai--sakai-commons",
      "content": "icon-sakai--sakai-resources",
      "wiki": "icon-sakai--sakai-rwiki",
    };

    this.searchTerms = sessionStorage.getItem("searchterms") || "";
    this.results = JSON.parse(sessionStorage.getItem("searchresults") || "[]");
    this.currentPageIndex = parseInt(sessionStorage.getItem("currentpageindex") || "0");

    this.showField = this.searchTerms.length > 3 && this.results.length > 0;

    this.loadTranslations("search").then(t => {

      this.i18n = t;
      this.toolNameMapping = {
        "announcement": this.i18n.toolname_announcement,
        "assignments": this.i18n.toolname_assignment,
        "chat": this.i18n.toolname_chat,
        "forums": this.i18n.toolname_forum,
        "lessons": this.i18n.toolname_lesson,
        "commons": this.i18n.toolname_commons,
        "content": this.i18n.toolname_resources,
        "wiki": this.i18n.toolname_wiki,
      };
    });
  }

  static get properties() {

    return {
      pageSize: { attribute: "page-size", type: Number },
      showField: { attribute: false, type: Boolean },
      results: { attribute: false, type: Array },
      pages: { attribute: false, type: Number },
      i18n: { attribute: false, type: Object },
    };
  }

  set pageSize(newValue) {

    this._pageSize = newValue;
    this.initSetsOfResults(this.results);
  }

  get pageSize() { return this._pageSize; }

  shouldUpdate() {
    return this.i18n;
  }

  render() {

    return html`
      <div class="sakai-search-input" role="search">
        ${this.showField ? html`
        <input type="text"
            id="sakai-search-input"
            role="searchbox"
            tabindex="0"
            @input=${this.search}
            @keyup=${this.search}
            .value=${this.searchTerms}
            aria-label="${this.i18n.search_placeholder}" />
        ` : ""}
        <a href="javascript:;" @click=${this.toggleField} title="${this.i18n.search_tooltip}">
          <span class="icon-sakai--sakai-search"></span>
        </a>
      </div>
      ${this.noResults && this.showField ? html`
        <div class="sakai-search-results" tabindex="1">
          <div class="search-result-container"><div class="search-result">No results</div></div>
        </div>
      ` : ""}
      ${this.results.length > 0 && this.showField ? html`
        <div class="sakai-search-results">
          ${this.currentPageOfResults.map(r => html`
          <div class="search-result-container">
            <a href="${r.url}">
              <div>
                <i class="search-result-tool-icon ${this.iconMapping[r.tool]}" title="${this.toolNameMapping[r.tool]}"></i>
                <span class="search-result-toolname">${this.toolNameMapping[r.tool]}</span>
                <span>${this.i18n.from_site}</span>
                <span class="search-result-site-title">${r.siteTitle}</span>
              </div>
              <div>
                <span class="search-result-title-label">${this.i18n.search_result_title}</span><span class="search-result-title">${r.title}</span>
              </div>
              <div class="search-result">${unsafeHTML(r.searchResult)}</div>
            </a>
          </div>
          `)}
          <sakai-pager count="${this.pages}" current"1" @page-selected=${this.pageSelected}></sakai-pager>
        </div>
      ` : ""}
    `;
  }

  toggleField() {

    this.showField = !this.showField;
    if (!this.showField) {
      this.clear();
    }

    this.requestUpdate();

    this.updateComplete.then(() => { if (this.showField) this.querySelector("#sakai-search-input").focus(); });
  }

  clear() {

    sessionStorage.removeItem("searchterms");
    sessionStorage.removeItem("searchresults");
    this.results = [];
    this.searchTerms = "";
    this.requestUpdate();
    this.noResults = false;
  }

  search(e) {

    const keycode = e.keyCode ? e.keyCode : e.which;
    if (keycode == "13" && e.target.value.length > 2) {
      sessionStorage.setItem("searchterms", e.target.value);
      fetch(`/direct/search/search.json?searchTerms=${e.target.value}`, {cache: "no-cache", credentials: "same-origin"})
        .then(r => {

          if (r.ok) {
            return r.json();
          }
          throw new Error("Failed to get search results.");
        }).then(data => {

          this.results = data;
          this.noResults = this.results.length === 0;
          this.results.forEach(r => { if (r.title.length === 0) r.title = r.tool; });
          this.initSetsOfResults(this.results);
          this.updateComplete.then(() => {

            const firstResult = document.querySelector(".search-result-container a");
            firstResult && firstResult.focus();

            document.querySelectorAll(".search-result-container").forEach(el => {

              el.addEventListener("keydown", ke => {

                ke.stopPropagation();
                switch (ke.code) {
                  case "ArrowDown":
                    if (el.nextElementSibling.classList.contains("search-result-container")) {
                      el.nextElementSibling.querySelector("a").focus();
                      ke.preventDefault();
                    }
                    break;
                  case "ArrowUp":
                    if (el.previousElementSibling.classList.contains("search-result-container")) {
                      el.previousElementSibling.querySelector("a").focus();
                      ke.preventDefault();
                    }
                    break;
                  default:
                }
              });
            });
          });
          sessionStorage.setItem("searchresults", JSON.stringify(this.results));
          this.requestUpdate();
        })
        .catch(error => console.error(error));
    } else {
      this.clear();
    }
  }

  initSetsOfResults(results) {

    this.setsOfResults = [];

    if (results) {
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
      this.pages = Math.ceil(results.length / this.pageSize);
    }

    this.currentPageIndex = 0;

    this.currentPageOfResults = this.setsOfResults[this.currentPageIndex];

    this.requestUpdate();
  }

  pageSelected(e) {

    this.currentPageIndex = e.detail.page;
    this.currentPageOfResults = this.setsOfResults[this.currentPageIndex];
    this.requestUpdate();
    sessionStorage.setItem("currentpageindex", this.currentPageIndex);
  }
}

const tagName = "sakai-search";
!customElements.get(tagName) && customElements.define(tagName, SakaiSearch);
