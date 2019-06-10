import {SakaiElement} from "./sakai-element.js";
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
    this.showField = this.searchTerms.length > 3 && this.results.length > 0;

    this.loadTranslations({bundle: "search"}).then(t => { this.i18n = t; this.requestUpdate(); });
  }

  get properties() {

    return {
      showField: Boolean,
      results: Array,
    };
  }

  shouldUpdate(changed) {
    return this.i18n;
  }

  render() {

    return html`
      <div class="sakai-search-input">
        ${this.showField ? html`<input type="text" tabindex="0" @keyup=${this.search} .value=${this.searchTerms} placeholder="${this.i18n["search_placeholder"]}"/>` : html``}
        <a href="javascript:;" @click=${this.toggleField} title="${this.i18n["search_tooltip"]}"><span class="fa fa-search"></span></a>
      </div>
      ${this.results.length > 0 && this.showField ? html`
        <div class="sakai-search-results" tabindex="1">
          ${this.results.map(r => html`
          <div class="search-result-container">
            <div>
                <!--i class="search-result-tool-icon fa ${this.iconMapping[r.tool]}" title="${r.tool}"></i-->
                <i class="search-result-tool-icon ${this.iconMapping[r.tool]}" title="${r.tool}"></i>
              <a href="${r.url}">
                <span class="search-result-title">${r.title}</span>
              </a>
            </div>
            <div class="search-result">${unsafeHTML(r.searchResult)}</div>
            <div class="search-result-site-title"><span>${this.i18n["site_label"]}</span><a href="${r.siteUrl}" title="Click to visit ${this.siteTitle}">${r.siteTitle}</a></div>
          </div>
          `)}
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
  }

  clear() {

    sessionStorage.removeItem("searchterms");
    sessionStorage.removeItem("searchresults");
    this.results = [];
    this.searchTerms = "";
    this.requestUpdate();
  }

  search(e) {

    if (e.target.value.length > 3) {
      sessionStorage.setItem("searchterms", e.target.value);
      fetch(`/direct/search/search.json?searchTerms=${e.target.value}`, {cache: "no-cache", credentials: "same-origin"})
        .then(res => res.json() )
        .then(data => {

          this.results = data;
          this.results.forEach(r => { if (r.title.length === 0) r.title = r.tool; });
          sessionStorage.setItem("searchresults", JSON.stringify(this.results));
          this.requestUpdate();
        })
        .catch(error => console.log(`Failed to search with ${e.target.value}`, error));
    } else {
      this.clear();
    }
  }
}

customElements.define("sakai-search", SakaiSearch);
