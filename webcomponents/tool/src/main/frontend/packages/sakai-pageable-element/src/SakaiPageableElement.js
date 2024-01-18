import { LitElement, css, html } from "lit";
import "@sakai-ui/sakai-pager/sakai-pager.js";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiPageableElement extends LitElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    dataPage: { type: Array },
    showPager: { type: Boolean },
  };

  constructor() {

    super();
    this.count = 0;
    this.pageSize = 5;
    this.currentPage = 1;
    this.allDataAtOnce = true;
  }

  set siteId(value) {
    this._siteId = value;
  }

  get siteId() { return this._siteId; }

  set userId(value) {
    this._userId = value;
  }

  get userId() { return this._userId; }

  loadTranslations(options) {
    return loadProperties(options);
  }

  _loadData() {

    if (this.allDataAtOnce) {
      this.loadAllData().then(() => {

        this.count = Math.ceil(this.data.length / this.pageSize);
        this._loadDataPage(1);
      });
    } else {
      this._loadDataPage(1);
    }
  }

  async loadAllData() {}

  _loadDataPage(page) {

    if (!this.data) {
      this.loadAllData();
    } else {
      this.currentPage = page;
      this.repage();
    }
  }

  pageClicked(e) {
    this._loadDataPage(e.detail.page);
  }

  // Override this method in components that extend SakaiPageableElement to apply custom data filtering before re-paging.
  getFilteredDataBeforeRepaging() {
    return this.data;
  }

  repage() {

    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    const filteredData = this.getFilteredDataBeforeRepaging();
    this.dataPage = filteredData.slice(start, end);
    this.count = Math.ceil(filteredData.length / this.pageSize);
    this.requestUpdate();
  }

  content() {}

  shouldUpdate() {
    return this.dataPage;
  }

  connectedCallback() {

    super.connectedCallback();
    this._loadData();
  }

  render() {

    return html`
      <div id="wrapper">
        <div id="content">${this.content()}</div>
        ${this.showPager ? html`
        <div id="pager">
          <sakai-pager count="${this.count}" current="1" @page-selected=${this.pageClicked}></sakai-pager>
        </div>
        ` : ""}
      </div>
    `;
  }

  static styles = css`
    #wrapper {
      display: flex;
      flex-direction: column;
      height: 100%;
      padding-bottom: 0;
    }

      #topbar {
        display: flex;
        margin-top: 8px;
        margin-bottom: 20px;
      }

      #content {
        background-color: var(--sakai-dashboard-widget-bg-color, white);
        padding: 8px;
        padding-bottom: 0;
      }

      #pager {
        margin-top: auto;
      }
  `;
}
