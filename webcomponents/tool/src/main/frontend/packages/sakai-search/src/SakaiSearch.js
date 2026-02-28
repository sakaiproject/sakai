import { SakaiElement } from "@sakai-ui/sakai-element";
import { html, nothing } from "lit";
import { unsafeHTML } from "lit/directives/unsafe-html.js";

export class SakaiSearch extends SakaiElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    tool: { type: String },

    _results: { state: true },
  };

  constructor() {

    super();

    this.searchTerms = "";
    this.searchMinLengthValue = 3;

    this.iconMapping = {
      "announcement": "si si-sakai-announcements",
      "assignments": "si si-sakai-assignment-grades",
      "chat": "si si-sakai-chat",
      "sakai.conversations": "si si-sakai-conversations",
      "forums": "si si-sakai-forums",
      "lessons": "si si-sakai-lessonbuildertool",
      "commons": "si si-sakai-commons",
      "content": "si si-sakai-resources",
      "syllabus": "si si-sakai-syllabus",
      "wiki": "si si-sakai-rwiki"
    };

    this.loadTranslations("search").then(t => {

      this._i18n = t;
      this.toolNameMapping = {
        "announcement": this._i18n.toolname_announcement,
        "assignments": this._i18n.toolname_assignment,
        "chat": this._i18n.toolname_chat,
        "sakai.conversations": this._i18n.toolname_conversations,
        "forums": this._i18n.toolname_forum,
        "lessons": this._i18n.toolname_lesson,
        "commons": this._i18n.toolname_commons,
        "content": this._i18n.toolname_resources,
        "syllabus": this._i18n.toolname_syllabus,
        "wiki": this._i18n.toolname_wiki
      };
    });
  }

  handleKeydownOnResult(e) {

    if (e.code === "Escape") {
      e.preventDefault();
      this.closeResults();
    }
  }

  closeResults() {

    this._results = [];
    this.dispatchEvent(new CustomEvent("hiding-search-results"));
    this.querySelector("input").focus();
  }

  shouldUpdate() {
    return this._i18n;
  }

  search(e) {

    e.preventDefault();

    const terms = document.getElementById("sakai-search-input")?.value;

    this.closeResults();

    if (terms.length > this.searchMinLengthValue - 1) {

      const url = `/api/search?terms=${terms}${this.siteId ? `&site=${this.siteId}` : ""}${this.tool ? `&tool=${this.tool}` : ""}`;
      fetch(url, {
        cache: "no-cache",
        credentials: "same-origin"
      })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Failed to get search results from ${url}.`);
      })
      .then(data => {

        this.dispatchEvent(new CustomEvent("showing-search-results"));
        this._results = data;

        this.noResults = this._results.length === 0;
        this._results.forEach(r => { if (r.title.length === 0) r.title = r.tool; });
        this.updateComplete.then(() => {

          if (!this.noResults) {
            const resultItem = this.querySelector(".search-result-link");
            resultItem.focus();
          }
          document.querySelectorAll(".search-result-link").forEach(el => {

            el.addEventListener("keydown", ke => {
              ke.stopPropagation();

              switch (ke.code) {
                case "ArrowDown":
                  if (el.nextElementSibling.classList.contains("search-result-link")) {
                    el.nextElementSibling.focus();
                    ke.preventDefault();
                  }

                  break;

                case "ArrowUp":
                  if (el.previousElementSibling.classList.contains("search-result-link")) {
                    el.previousElementSibling.focus();
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
      .catch (error => console.error(error));
    }
  }

  render() {

    return html`
      <form class="input-group position-relative" @submit=${this.search}>
        <input type="search"
          class="sakaiSearch form-control"
          autocomplete="off"
          id="sakai-search-input"
          pattern=".{${this.searchMinLengthValue},}"
          title="${this._i18n.search_min_length.replace("{}", this.searchMinLengthValue)}"
          placeholder="${this._i18n.search_placeholder}"
          value=${this.searchTerms}
          aria-label="${this._i18n.search_placeholder}"
        />
        <button class="btn btn-primary ms-1" type="submit" id="sakai-search-button">
          ${this._i18n.search}
        </button>
      </form>
      ${this.noResults ? html`
        <div class="list-group-item d-flex justify-content-between align-items-start mt-2 fw-bold">
          ${this._i18n.no_results}
        </div>
      ` : nothing}
      ${this._results?.length > 0 ? html`
        <div class="d-flex justify-content-end">
          <button type="button"
              class="btn icon-button mt-2 mb-1 fs-3 p-0"
              title="${this._i18n.close_results_tooltip}"
              aria-label="${this._i18n.close_results_tooltip}"
              @click=${this.closeResults}>
            <i class="si si-close"></i>
          </button>
        </div>
        <div>
          ${this._results && this._results.map(r => html`
            <a class="search-result-link" href="${r.url}" @click=${this.toggleField} @keydown=${this.handleKeydownOnResult}>
              <div class="card mb-2">
                <div class="card-body">
                  <div class="card-text">
                    <div class="mb-2">
                      <i class="search-result-tool-icon ${this.iconMapping[r.tool]}" title="${this.toolNameMapping[r.tool]}"></i>
                      <span class="search-result-toolname">${this.toolNameMapping[r.tool]}</span>
                      <span>${this._i18n.from_site}</span>
                      <span class="search-result-site-title">${r.siteTitle}</span>
                    </div>
                    <div>
                      <span class="search-result-title-label">${this._i18n.search_result_title}</span>
                      <span class="search-result-title">${unsafeHTML(r.title)}</span>
                    </div>
                    <div class="search-result">${unsafeHTML(r.searchResult)}</div>
                  </div>
                </div>
              </div>
            </a>
          `)}
        </div>
      ` : nothing}
    `;
  }
}
