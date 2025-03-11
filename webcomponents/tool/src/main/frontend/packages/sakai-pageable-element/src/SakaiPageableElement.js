import { css, html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-pager/sakai-pager.js";

export class SakaiPageableElement extends SakaiShadowElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    dataPage: { type: Array },
    showPager: { type: Boolean },
    defer: { type: Boolean },
  };

  constructor() {

    super();

    this.count = 0;
    this.pageSize = 5;
    this.currentPage = 1;
    this.allDataAtOnce = true;
  }

  connectedCallback() {

    super.connectedCallback();

    if (!this.defer) {
      this.loadData();
    }
  }

  loadData() {

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
  }

  content() {}

  shouldUpdate() {
    return this.dataPage;
  }

  render() {

    return html`
      <div id="wrapper">
        <div id="content">${this.content()}</div>
        ${this.showPager ? html`
        <div id="pager">
          <sakai-pager count="${this.count}" current="1" @page-selected=${this.pageClicked}></sakai-pager>
        </div>
        ` : nothing}
      </div>
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
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
        padding-bottom: 0;
      }

      #pager {
        margin-top: auto;
      }
    `
  ];
}
