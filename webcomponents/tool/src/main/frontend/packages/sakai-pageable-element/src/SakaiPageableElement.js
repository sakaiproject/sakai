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
    pageSize: { attribute: "page-size", type: Number },
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

  willUpdate(changedProperties) {

    if (changedProperties.has("pageSize")) {
      const size = Number(this.pageSize);
      this.pageSize = Number.isFinite(size) && size > 0 ? size : 5;
    }
  }

  updated(changedProperties) {

    if (changedProperties.has("pageSize") && this.data) {
      this.currentPage = 1;
      this.repage();
    }
  }

  loadData() {

    if (this.allDataAtOnce) {
      this.loadAllData().then(() => {

        if (!Array.isArray(this.data)) {
          this.data = [];
        }
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

  statusMessage() {
    return null;
  }

  shouldUpdate() {
    return this.dataPage;
  }

  render() {

    const status = this.statusMessage();
    const showPagerBar = this.showPager && this.count > 1;

    return html`
      <div id="wrapper">
        <div id="content">${this.content()}</div>
        ${status ? html`<div id="widget-status" class="small">${status}</div>` : nothing}
        ${showPagerBar ? html`
        <div id="pager">
          <sakai-pager count="${this.count}" current="${this.currentPage}" @page-selected=${this.pageClicked}></sakai-pager>
        </div>
        ` : nothing}
      </div>
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
      :host {
        display: block;
        height: 100%;
        min-height: 0;
      }

      #wrapper {
        display: flex;
        flex-direction: column;
        height: 100%;
        min-height: 0;
      }

      #topbar {
        display: flex;
        margin-top: 8px;
        margin-bottom: 20px;
      }

      #content {
        background-color: var(--sakai-dashboard-widget-bg-color, white);
        flex: 1 1 auto;
        min-height: 0;
        overflow: auto;
        padding: 0.5rem 0.75rem;
      }

      #widget-status {
        flex: 0 0 auto;
        background-color: var(--sakai-dashboard-widget-secondary-bg-color, rgb(244, 244, 244));
        color: var(--sakai-text-color-2, #555);
        padding: 0.25rem 0.75rem;
        border-top: 1px solid var(--sakai-border-color, #ddd);
        font-size: 0.8125rem;
        line-height: 1.3;
        text-align: center;
        min-height: 2.5rem;
        align-content: center;
      }

      #pager {
        flex: 0 0 auto;
        background-color: var(--sakai-dashboard-widget-secondary-bg-color, rgb(244, 244, 244));
        padding: 0.35rem 0.5rem;
        display: flex;
        justify-content: center;
        align-items: center;
        border-top: 1px solid var(--sakai-border-color, #ddd);
      }

      #pager sakai-pager {
        margin: 0;
        display: block;
      }

      #pager sakai-pager::part(pagination),
      #pager .pagination {
        margin: 0;
        padding: 0;
      }
    `
  ];
}
