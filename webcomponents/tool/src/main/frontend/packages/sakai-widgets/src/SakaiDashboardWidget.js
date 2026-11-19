import { css, html, nothing } from "lit";
import { ifDefined } from "lit/directives/if-defined.js";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "@sakai-ui/sakai-pager";
import { loadProperties } from "@sakai-ui/sakai-i18n";

export class SakaiDashboardWidget extends SakaiShadowElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    title: String,
    state: String,
    editing: { type: Boolean },
    tileW: { attribute: "tile-w", type: Number },
    tileH: { attribute: "tile-h", type: Number },
    displayW: { attribute: "display-w", type: Number },
    disableLeftAndUp: { attribute: "disable-left-and-up", type: Boolean },
    disableRightAndDown: { attribute: "disable-right-and-down", type: Boolean },
    hasFilters: { type: Boolean },
    filtersOpen: { state: true },
    _baseI18n: { state: true },
  };

  constructor() {

    super();

    this.state = "view";
    this.editing = false;
    this.tileW = 1;
    this.tileH = 1;
    this.hasOptions = true;
    this.hasFilters = false;
    this.filtersOpen = false;
    loadProperties("dashboard-widget").then(r => this._baseI18n = r || {});
  }

  set widgetId(value) {

    this._widgetId = value;
    loadProperties(value).then(r => this.title = r?.widget_title || value);
  }

  get widgetId() { return this._widgetId; }

  content() {}

  remove() {
    this.dispatchEvent(new CustomEvent("remove", { bubbles: true }));
  }

  shouldUpdate() {
    return this._baseI18n && this.title;
  }

  move(direction) {
    this.dispatchEvent(new CustomEvent("move", { detail: { widgetId: this.widgetId, direction }, bubbles: true }));
  }

  moveUp() { this.move("up"); }

  moveDown() { this.move("down"); }

  moveLeft() { this.move("left"); }

  moveRight() { this.move("right"); }

  _sizeSelected(e) {

    const [ w, h ] = e.target.value.split("x").map(n => parseInt(n, 10));
    this.dispatchEvent(new CustomEvent("size-change", {
      bubbles: true,
      detail: { widgetId: this.widgetId, w, h },
    }));
  }

  _currentSizeValue() {
    return `${this.tileW || 1}x${this.tileH || 1}`;
  }

  _pageSize() {
    return 5 * Math.max(1, this.tileH || 1) + 3 * Math.max(0, (this.tileH || 1) - 1);
  }

  _displayW() {
    return Math.max(1, this.displayW || this.tileW || 1);
  }

  _toggleFilters() {
    this.filtersOpen = !this.filtersOpen;
  }

  _closeFilters() {
    this.filtersOpen = false;
  }

  _t(key, fallback) {
    return this._baseI18n?.[key] || fallback;
  }

  _editControls() {

    return html`
      <div id="widget-mover">
        <div id="size-picker">
          <label class="visually-hidden" for="widget-size-${this.widgetId}">${this._t("widget_size", "Size")}</label>
          <select id="widget-size-${this.widgetId}"
              class="form-select form-select-sm"
              .value=${this._currentSizeValue()}
              @change=${this._sizeSelected}
              title="${this._t("widget_size", "Size")}"
              aria-label="${this._t("widget_size", "Size")}">
            <option value="1x1">${this._t("size_1x1", "1\u00d71")}</option>
            <option value="2x1">${this._t("size_2x1", "2\u00d71")}</option>
            <option value="1x2">${this._t("size_1x2", "1\u00d72")}</option>
            <option value="2x2">${this._t("size_2x2", "2\u00d72")}</option>
          </select>
        </div>
        <div class="${ifDefined(this.disableLeftAndUp ? "d-none" : undefined)}">
          <button type="button"
              class="btn btn-icon btn-sm"
              @click=${this.moveLeft}
              title="${this._t("left", "Move left")}"
              aria-label="${this._t("left", "Move left")}">
            <span class="si si-left fs-6" aria-hidden="true"></span>
          </button>
        </div>
        <div class="${ifDefined(this.disableRightAndDown ? "d-none" : undefined)}">
          <button type="button"
              class="btn btn-icon btn-sm"
              @click=${this.moveRight}
              title="${this._t("right", "Move right")}"
              aria-label="${this._t("right", "Move right")}">
            <span class="si si-right fs-6" aria-hidden="true"></span>
          </button>
        </div>
        <div>
          <button type="button"
              class="btn btn-icon btn-sm bg-danger"
              @click=${this.remove}
              title="${this._t("remove", "Remove")} ${this.title}"
              aria-label="${this._t("remove", "Remove")} ${this.title}">
            <span class="si si-close fs-6" aria-hidden="true"></span>
          </button>
        </div>
      </div>
    `;
  }

  render() {

    return html`
      <div id="container">
        <div id="title-bar" class="${this.editing ? "editing" : ""}">
          <div id="title-row">
            ${this.editing ? html`
              <div id="drag-handle" title="${this._t("drag", "Drag")}" role="img" aria-label="${this._t("drag", "Drag")}">
                <span class="si si-drag-handle fs-6" aria-hidden="true"></span>
              </div>
            ` : nothing}
            <div id="title">${this.title}</div>
            ${!this.editing && this.hasFilters ? html`
              <button type="button"
                  id="filters-button"
                  class="btn btn-icon btn-sm"
                  @click=${this._toggleFilters}
                  aria-expanded="${this.filtersOpen}"
                  title="${this._t("open_filters", "Show filters")}"
                  aria-label="${this._t("filters", "Filters")}">
                <span class="si si-filter fs-6" aria-hidden="true"></span>
              </button>
            ` : nothing}
          </div>
          ${this.editing ? this._editControls() : nothing}
        </div>
        <div id="content">${this.content()}</div>
        ${this.showPager && this.count > 1 ? html`
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
      :host {
        width: 100%;
        height: 100%;
        display: block;
      }
      a {
        color: var(--link-color);
      }
      a:hover {
        color: var(--link-hover-color);
      }
      a:active {
        color: var(--link-active-color);
      }
      a:visited {
        color: var(--link-visited-color);
      }

      #container {
        display: grid;
        grid-template-rows: auto 1fr auto;
        height: 100%;
        min-height: 0;
        background-color: var(--sakai-dashboard-widget-bg-color, white);
        border-radius: var(--sakai-course-card-border-radius, 4px);
        border: solid;
        border-width: var(--sakai-dashboard-widget-border-width, 1px);
        border-color: var(--sakai-dashboard-widget-border-color, rgb(224,224,224));
        overflow: hidden;
      }

      #title-bar {
        display: flex;
        flex-direction: column;
        gap: 0.35rem;
        background-color: var(--sakai-title-bar-bg-color);
        color: var(--sakai-title-bar-text-color);
        font-weight: 700;
        padding: 0.75rem 1rem;
        flex: 0 0 auto;
      }

      #title-row {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        min-width: 0;
        min-height: 2rem;
        height: 2rem;
      }

      #title {
        flex: 1 1 auto;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-weight: 700;
        line-height: 1.2;
        color: var(--sakai-title-bar-text-color, #ffffff);
      }

      #filters-button {
        flex: 0 0 auto;
        margin-left: auto;
        width: 1.75rem;
        height: 1.75rem;
        padding: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        --bs-btn-color: var(--sakai-title-bar-text-color, #ffffff);
        --bs-btn-hover-color: var(--sakai-title-bar-text-color, #ffffff);
        color: var(--sakai-title-bar-text-color, #ffffff);
      }

      #content {
        padding: 0;
        min-height: 0;
        overflow: hidden;
        position: relative;
        display: flex;
        flex-direction: column;
      }

      #content > * {
        flex: 1 1 auto;
        min-height: 0;
        height: 100%;
        max-height: 100%;
      }

      #pager {
        background-color: var(--sakai-dashboard-widget-secondary-bg-color, rgb(244, 244, 244));
        padding: 0.1rem 0.35rem;
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 0;
      }

      #widget-mover {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.25rem;
        width: 100%;
      }
      #widget-mover .btn:not(.bg-danger) {
        --bs-btn-color: var(--sakai-title-bar-text-color, #ffffff);
        --bs-btn-hover-color: var(--sakai-title-bar-text-color, #ffffff);
        color: var(--sakai-title-bar-text-color, #ffffff);
      }
      #drag-handle {
        cursor: grab;
        flex: 0 0 auto;
        color: var(--sakai-title-bar-text-color, #ffffff);
        padding: 0.15rem;
      }
      #size-picker {
        flex: 0 0 auto;
        min-width: 4.5rem;
        margin-right: auto;
      }
      #size-picker select {
        max-width: 5.5rem;
      }
    `
  ];
}
