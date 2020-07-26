import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

export class SakaiPager extends SakaiElement {

  constructor() {

    super();

    this.loadTranslations("pager").then(t => this.i18n = t);
  }

  static get properties() {

    return {
      totalThings: { attribute: "total-things", type: Number },
      pageSize: { attribute: "page-size", type:  Number },
      numPages: Number,
      currentPageNumbers: Array,
      i18n: Object,
    };
  }

  set totalThings(newValue) {

    this._totalThings = newValue;
    if (this.pageSize) {
      this.numPages = this._totalThings / this.pageSize;
      if (this.numPages < 1) this.numPages = 1;
      this.initSetsOfPages();
    }
  }

  get totalThings() { return this._totalThings; }

  set pageSize(newValue) {

    this._pageSize = newValue;
    if (this.totalThings) {
      this.numPages = this.totalThings / this._pageSize;
      if (this.numPages < 1) this.numPages = 1;
      this.initSetsOfPages();
    }
  }

  get pageSize() { return this._pageSize; }

  initSetsOfPages() {

    let allPages = [...Array(this.numPages).keys()].map(i => i + 1);

    this.setsOfPages = [];
    if (this.numPages < 10) {
      this.setsOfPages.push(allPages);
    } else {
      let i = 0;
      while (i < allPages.length) {
        if ((i + 10) < allPages.length) {
          this.setsOfPages.push(allPages.slice(i, i + 10));
        } else {
          this.setsOfPages.push(allPages.slice(i));
        }
        i = i + 10;
      }
    }

    this.currentPagesIndex = 0;

    this.currentPageNumbers = this.setsOfPages[this.currentPagesIndex];

    this.enablePrevious = this.enableNext = (this.setsOfPages.length > 1);
  }

  render() {

    return html`
      <div class="sakai-pager">
        ${this.enablePrevious ? html`
        <a href="javascript:;" class="pager-previous-link" @click=${this.showPreviousPageNumbers} title="${this.i18n["previous"]}">${this.i18n["previous"]}</a>
        ` : html`
        <span>${this.i18n["previous"]}</span>
        `}
        ${this.currentPageNumbers.map((i) => html`
        <div class="pager-page-link">
          <a href="javascript:;" data-page="${i}" @click=${this.pageClicked} title="${this.tr("page_tooltip", { page: i })}">${i}</a>
        </div>
        `)}
        ${this.enableNext ? html`
        <a href="javascript:;" class="pager-next-link" @click=${this.showNextPageNumbers} title="${this.i18n["next"]}">${this.i18n["next"]}</a>
        ` : html`
        <span>${this.i18n["next"]}</span>
        `}
      </div>
    `;
  }

  showPreviousPageNumbers(e) {

    this.currentPagesIndex = this.currentPagesIndex - 1;
    this.currentPageNumbers = this.setsOfPages[this.currentPagesIndex];
  }

  pageClicked(e) {
    this.dispatchEvent(new CustomEvent("page-clicked", {detail: {page: e.target.dataset.page}}));
  }

  showNextPageNumbers(e) {

    this.currentPagesIndex = this.currentPagesIndex + 1;
    this.currentPageNumbers = this.setsOfPages[this.currentPagesIndex];
  }
}

customElements.define("sakai-pager", SakaiPager);
