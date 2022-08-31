import { SakaiElement } from "./sakai-element.js";
import "./sakai-pager.js";
import { html } from "./assets/lit-element/lit-element.js";
import { unsafeHTML } from "./assets/lit-html/directives/unsafe-html.js";
import { getUserId } from "./sakai-portal-utils.js";

class SakaiSearch extends SakaiElement {

  constructor() {

    super();

    this.pageSize = 10;

    this.iconMapping = {
      "announcement": "icon-sakai--sakai-announcements",
      "assignments": "icon-sakai--sakai-assignment-grades",
      "chat": "icon-sakai--sakai-chat",
      "sakai.conversations": "icon-sakai--sakai-conversations",
      "forums": "icon-sakai--sakai-forums",
      "lessons": "icon-sakai--sakai-lessonbuildertool",
      "commons": "icon-sakai--sakai-commons",
      "content": "icon-sakai--sakai-resources",
      "wiki": "icon-sakai--sakai-rwiki",
    };

    this.loadTranslations("search").then(t => {

      this.i18n = t;
      this.toolNameMapping = {
        "announcement": this.i18n.toolname_announcement,
        "assignments": this.i18n.toolname_assignment,
        "chat": this.i18n.toolname_chat,
        "sakai.conversations": this.i18n.toolname_conversations,
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
      siteId: { attribute: "site-id", type: String },
      tool: { type: String },
      pageSize: { attribute: "page-size", type: Number },
      results: { attribute: false, type: Array },
      pages: { attribute: false, type: Number },
      i18n: { attribute: false, type: Object },
      portal: { type: Boolean},
    };
  }

  _handleKeydownOnResult(e) {

    if (e.code === "Escape") {
      e.preventDefault();
      this._closeResults();
    }
  }

  _getSessionStorageKey(type) {
    return `${type}-${getUserId()}-${this.siteId ? `-${this.siteId}` : ""}${this.tool ? `-${this.tool}` : ""}`;
  }

  _closeResults() {

    this.results = undefined;
    this.dispatchEvent(new CustomEvent("hiding-search-results"));
    const input = this.querySelector("input");
    input.focus();
    input.classList.remove("flat-bottom");
  }

  shouldUpdate() {
    return this.i18n;
  }

  render() {

    return html`
      <div class="sakai-search-input" role="search">
        <input type="text"
            autocomplete="off"
            id="sakai-search-input"
            role="searchbox"
            @keydown=${this.search}
            placeholder="${this.tool ? this.i18n.search_this_tool_placeholder : this.i18n.search_sakai_placeholder}"
            aria-label="${this.tool ? this.i18n.search_this_tool_placeholder : this.i18n.search_sakai_placeholder}" />
        <sakai-icon type="search" size="small"></sakai-icon>
      </div>
      ${this.results ? html`
        <div class="sakai-search-results" tabindex="1">
          <div class="search-results-header">
            <div class="sakai-search-results-title">${this.i18n.search_results}</div>
            <div>
              <button class="btn-transparent"
                  aria-label="${this.i18n.close_results_tooltip}"
                  title="${this.i18n.close_results_tooltip}"
                  @click=${this._closeResults}>
                <sakai-icon type="close"></sakai-icon>
              </button>
            </div>
          </div>
        ${this.results?.length > 0 ? html`
          ${this.currentPageOfResults.map(r => html`
          <div class="search-result-container">
            <a href="${r.url}" @click=${this.toggleField} @keydown=${this._handleKeydownOnResult}>
              ${!this.tool ? html`
              <div>
                <i class="search-result-tool-icon ${this.iconMapping[r.tool]}" title="${this.toolNameMapping[r.tool]}"></i>
                <span class="search-result-toolname">${this.toolNameMapping[r.tool]}</span>
                <span>${this.i18n.from_site}</span>
                <span class="search-result-site-title">${r.siteTitle}</span>
              </div>
              ` : ""}
              <div class="search-result-title-block">
                <span class="search-result-title-label">${this.i18n.search_result_title}</span>
                <span class="search-result-title">${r.title}</span>
              </div>
              <div class="search-result">${unsafeHTML(r.searchResult)}</div>
            </a>
          </div>
          `)}
          <sakai-pager count="${this.pages}" current"1" @page-selected=${this.pageSelected}></sakai-pager>
        ` : html`
          <div class="search-result-container no-results">
            <div>${this.i18n.no_results}</div>
          </div>
        `}
        </div>
      ` : ""}
    `;
  }

  clear() {
    this.results = undefined;
  }

  search(e) {

    const keycode = e.keyCode ? e.keyCode : e.which;

    this._closeResults();

    if (keycode == "13" && e.target.value.length > 4) {
      const terms = encodeURIComponent(e.target.value);
      fetch(`/api/search?terms=${terms}${this.siteId ? `&site=${this.siteId}` : ""}${this.tool ? `&tool=${this.tool}` : ""}`
        , {cache: "no-cache", credentials: "same-origin"})
        .then(r => {

          if (r.ok) {
            return r.json();
          }
          throw new Error("Failed to get search results.");
        }).then(data => {

          this.dispatchEvent(new CustomEvent("showing-search-results"));

          this.results = data;

          if (this.results.length > 0) {
            this.querySelector(".sakai-search-input").classList.add("flat-bottom");
          }
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

    this.currentPageOfResults = this.setsOfResults.length > 0 && this.setsOfResults[this.currentPageIndex];

    this.requestUpdate();
  }

  pageSelected(e) {

    this.currentPageIndex = e.detail.page;
    this.currentPageOfResults = this.setsOfResults[this.currentPageIndex];
    this.requestUpdate();
  }
}

const tagName = "sakai-search";
!customElements.get(tagName) && customElements.define(tagName, SakaiSearch);
