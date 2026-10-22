import { html, css, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { ifDefined } from "lit/directives/if-defined.js";
import { repeat } from "lit/directives/repeat.js";
import {
  defaultTileSize,
  isPickerTile,
  layoutIds,
  normalizeLayout,
  PICKER_TILE_ID,
  tileId,
  tileKey,
} from "./dashboard-layout.js";
import { loadToolnames } from "./toolnames.js";
import "../sakai-calendar-widget.js";
import "../sakai-courses-widget.js";
import "../sakai-tasks-widget.js";
import "../sakai-grades-widget.js";
import "../sakai-announcements-widget.js";
import "../sakai-forums-widget.js";
import "../sakai-sitestats-widget.js";
import "../sakai-status-widget.js";

export class SakaiWidgetPanel extends SakaiShadowElement {

  static properties = {

    siteId: { attribute: "site-id", type: String },
    userId: { attribute: "user-id", type: String },
    widgetIds: { type: Array },
    layout: { type: Array },
    i18n: Object,
    state: String,
    editing: { type: Boolean },
    _widgets: { state: true },
    _dragIndex: { state: true },
    _toolnames: { state: true },
    _narrowColumns: { state: true },
    widgetFilters: { type: Object },
    gradesRevealOnClick: { attribute: "grades-reveal-on-click", type: Boolean },
  };

  constructor() {

    super();
    this.state = "view";
    this._layout = [];
    this._dragIndex = undefined;
    this._narrowColumns = false;
    this.gradesRevealOnClick = true;
    this.loadTranslations("widgetpanel");
    loadToolnames().then(r => this._toolnames = r || {});
  }

  connectedCallback() {

    super.connectedCallback();
    this._resizeObserver?.disconnect();
    this._resizeObserver = new ResizeObserver(entries => {
      const width = entries[0]?.contentRect?.width;
      if (typeof width === "number") {
        this._updateNarrowColumns(width);
      }
    });
    this._resizeObserver.observe(this);
    this._updateNarrowColumns(this.clientWidth);
  }

  disconnectedCallback() {

    this._resizeObserver?.disconnect();
    this._resizeObserver = undefined;
    super.disconnectedCallback();
  }

  _updateNarrowColumns(width) {

    const styles = getComputedStyle(this);
    const minWidget = parseFloat(styles.getPropertyValue("--sakai-widget-panel-min-widget-width")) || 380;
    const gap = parseFloat(styles.getPropertyValue("--sakai-widget-panel-gutter-width")) || 14;
    const narrow = width > 0 && width < (minWidget * 2 + gap);
    if (narrow !== this._narrowColumns) {
      this._narrowColumns = narrow;
    }
  }

  set editing(value) {

    const old = this._editing;
    this._editing = value;
    if (!value) {
      this._layout = this._layout.filter(t => !isPickerTile(t));
    }
    this.requestUpdate("editing", old);
  }

  get editing() { return this._editing; }

  set layout(value) {

    let normalized = normalizeLayout(value);

    if (this.widgetIds) {
      normalized = normalized.filter(tile => this.widgetIds.includes(tile.id));
    }
    this._layout = normalized;
    this._notifyAvailableCount();
  }

  get layout() { return this._layout; }

  set widgetIds(value) {

    this._widgetIds = value;
    this._widgets = (value || []).map(v => ({ id: v }));
    this._notifyAvailableCount();
  }

  get widgetIds() { return this._widgetIds; }

  shouldUpdate() {
    return this._i18n;
  }

  fireChanged() {
    this.dispatchEvent(new CustomEvent("changed", {
      bubbles: true,
      detail: {
        layout: normalizeLayout(this._layout),
        availableCount: this.availableWidgetCount(),
      },
    }));
    this._notifyAvailableCount();
  }

  _availableWidgets() {
    const inLayout = new Set(layoutIds(this._layout));
    return (this.widgetIds || []).filter(id => id && id !== PICKER_TILE_ID && !inLayout.has(id));
  }

  availableWidgetCount() {
    return this._availableWidgets().length;
  }

  _notifyAvailableCount() {

    const count = this.availableWidgetCount();
    if (count === this._lastAvailableCount) {
      return;
    }
    this._lastAvailableCount = count;
    this.dispatchEvent(new CustomEvent("available-changed", {
      bubbles: true,
      detail: { count },
    }));
  }

  _widgetName(id) {
    return this._toolnames?.[id] || id;
  }

  _addWidget(id, atIndex = 0) {

    if (!id || layoutIds(this._layout).includes(id)) {
      return;
    }
    const { w, h } = defaultTileSize(id);
    this._layout.splice(atIndex, 0, { id, w, h });
    this.fireChanged();
    this.requestUpdate();
  }

  removeWidget(e) {

    const i = this._layout.findIndex(w => tileId(w) === e.target.id);
    if (i !== -1) this._layout.splice(i, 1);
    this.fireChanged();
    this.requestUpdate();
  }

  moveWidget(e) {

    const currentIndex = this._layout.findIndex(w => tileId(w) === e.detail.widgetId);
    if (currentIndex === -1) return;

    const movingBack = e.detail.direction === "up" || e.detail.direction === "left";
    const movingForward = e.detail.direction === "down" || e.detail.direction === "right";

    if ((movingBack && currentIndex === 0) || (movingForward && currentIndex === this._layout.length - 1)) {
      return;
    }

    const tmpTile = this._layout[currentIndex];

    switch (e.detail.direction) {
      case "up":
      case "left":
        this._layout[currentIndex] = this._layout[currentIndex - 1];
        this._layout[currentIndex - 1] = tmpTile;
        break;
      case "down":
      case "right":
        this._layout[currentIndex] = this._layout[currentIndex + 1];
        this._layout[currentIndex + 1] = tmpTile;
        break;
      default:
    }
    this.requestUpdate();
    this.fireChanged();
  }

  resizeWidget(e) {

    const index = this._layout.findIndex(w => tileId(w) === e.detail.widgetId);
    if (index === -1) {
      return;
    }
    const tile = this._layout[index];
    this._layout[index] = {
      id: tile.id,
      w: Math.max(1, Math.min(2, e.detail.w)),
      h: Math.max(1, Math.min(2, e.detail.h)),
    };
    this.requestUpdate();
    this.fireChanged();
  }

  _onDragStart(e, index) {

    if (!this.editing || isPickerTile(this._layout[index])) {
      e.preventDefault();
      return;
    }
    this._dragIndex = index;
    e.dataTransfer.effectAllowed = "move";
    e.dataTransfer.setData("text/plain", `move:${index}`);
  }

  _onAvailableDragStart(e, id) {

    e.dataTransfer.effectAllowed = "copy";
    e.dataTransfer.setData("text/plain", `add:${id}`);
  }

  _onDragOver(e) {
    e.preventDefault();
    e.dataTransfer.dropEffect = e.dataTransfer.effectAllowed === "copy" ? "copy" : "move";
  }

  _onDrop(e, targetIndex) {

    e.preventDefault();
    e.stopPropagation();
    const data = e.dataTransfer.getData("text/plain") || "";
    if (data.startsWith("add:")) {
      this._addWidget(data.substring(4), targetIndex);
      this._dragIndex = undefined;
      return;
    }

    const fromIndex = this._dragIndex ?? (data.startsWith("move:") ? parseInt(data.substring(5), 10) : undefined);
    if (fromIndex === undefined || Number.isNaN(fromIndex) || fromIndex === targetIndex) {
      this._dragIndex = undefined;
      return;
    }
    const moved = this._layout[fromIndex];
    this._layout.splice(fromIndex, 1);
    this._layout.splice(targetIndex, 0, moved);
    this._dragIndex = undefined;
    this.requestUpdate();
    this.fireChanged();
  }

  _onDragEnd() {
    this._dragIndex = undefined;
  }

  _tileSpan(tile) {
    if (typeof tile === "string") {
      return { w: 1, h: 1 };
    }
    return { w: tile.w || 1, h: tile.h || 1 };
  }

  _displaySpan(tile) {

    const { w, h } = this._tileSpan(tile);
    return {
      w: this._narrowColumns ? 1 : w,
      h,
      configuredW: w,
    };
  }

  _wrapTile(tile, index, content) {

    const { w, h } = this._displaySpan(tile);

    return html`
      <div
          class="widget-tile ${this._dragIndex === index ? "dragging" : ""}"
          style="grid-column: span ${w}; grid-row: span ${h};"
          draggable=${this.editing && !isPickerTile(tile) ? true : nothing}
          @dragstart=${e => this._onDragStart(e, index)}
          @dragover=${this._onDragOver}
          @drop=${e => this._onDrop(e, index)}
          @dragend=${this._onDragEnd}>
        ${content}
      </div>
    `;
  }

  _editBindings(index, tile) {

    const { w, h, configuredW } = this._displaySpan(tile);
    return {
      tileW: configuredW,
      displayW: w,
      tileH: h,
      disableLeftAndUp: index === 0,
      disableRightAndDown: index === this._layout.length - 1,
    };
  }

  getWidget(tile, index) {

    const r = tileId(tile);
    const w = this._widgets.find(widget => widget.id === r);
    const bindings = this._editBindings(index, tile);

    switch (r) {
      case "tasks":
        return this._wrapTile(tile, index, html`
          <sakai-tasks-widget
            id="${r}"
            site-id="${ifDefined(this.siteId ? this.siteId : "")}"
            user-id="${ifDefined(this.userId ? this.userId : "")}"
            class="widget"
            state="${w?.state}"
            tile-w="${bindings.tileW}"
            display-w="${bindings.displayW}"
            tile-h="${bindings.tileH}"
            @remove=${this.removeWidget}
            @move=${this.moveWidget}
            @size-change=${this.resizeWidget}
            ?disable-left-and-up=${bindings.disableLeftAndUp}
            ?disable-right-and-down=${bindings.disableRightAndDown}
            ?editing=${this.editing}>
          </sakai-tasks-widget>
        `);
      case "grades":
        return this._wrapTile(tile, index, html`
            <sakai-grades-widget
              id="${r}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              .savedFilters=${this.widgetFilters?.grades}
              ?reveal-on-click=${this.gradesRevealOnClick !== false}
              class="widget"
            state="${w?.state}"
            tile-w="${bindings.tileW}"
            display-w="${bindings.displayW}"
            tile-h="${bindings.tileH}"
            @remove=${this.removeWidget}
            @move=${this.moveWidget}
            @size-change=${this.resizeWidget}
            ?disable-left-and-up=${bindings.disableLeftAndUp}
            ?disable-right-and-down=${bindings.disableRightAndDown}
            ?editing=${this.editing}>
          </sakai-grades-widget>
        `);
      case "announcements":
        return this._wrapTile(tile, index, html`
            <sakai-announcements-widget
              id="${r}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              .savedFilters=${this.widgetFilters?.announcements}
              class="widget"
            state="${w?.state}"
            tile-w="${bindings.tileW}"
            display-w="${bindings.displayW}"
            tile-h="${bindings.tileH}"
            @remove=${this.removeWidget}
            @move=${this.moveWidget}
            @size-change=${this.resizeWidget}
            ?disable-left-and-up=${bindings.disableLeftAndUp}
            ?disable-right-and-down=${bindings.disableRightAndDown}
            ?editing=${this.editing}>
          </sakai-announcements-widget>
        `);
      case "calendar":
        return this._wrapTile(tile, index, html`
          <sakai-calendar-widget
            id="${r}"
            site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
            user-id="${ifDefined(this.userId ? this.userId : undefined)}"
            class="widget"
            state="${w?.state}"
            tile-w="${bindings.tileW}"
            display-w="${bindings.displayW}"
            tile-h="${bindings.tileH}"
            @remove=${this.removeWidget}
            @move=${this.moveWidget}
            @size-change=${this.resizeWidget}
            ?disable-left-and-up=${bindings.disableLeftAndUp}
            ?disable-right-and-down=${bindings.disableRightAndDown}
            ?editing=${this.editing}>
          </sakai-calendar-widget>
        `);
      case "courses":
        return this._wrapTile(tile, index, html`
            <sakai-courses-widget
              id="${r}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              .savedFilters=${this.widgetFilters?.courses}
              class="widget"
            state="${w?.state}"
            tile-w="${bindings.tileW}"
            display-w="${bindings.displayW}"
            tile-h="${bindings.tileH}"
            @remove=${this.removeWidget}
            @move=${this.moveWidget}
            @size-change=${this.resizeWidget}
            ?disable-left-and-up=${bindings.disableLeftAndUp}
            ?disable-right-and-down=${bindings.disableRightAndDown}
            ?editing=${this.editing}>
          </sakai-courses-widget>
        `);
      case "forums":
        return this._wrapTile(tile, index, html`
          <sakai-forums-widget
            id="${r}"
            site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
            user-id="${ifDefined(this.userId ? this.userId : undefined)}"
            state="${w?.state}"
            class="widget"
            tile-w="${bindings.tileW}"
            display-w="${bindings.displayW}"
            tile-h="${bindings.tileH}"
            @remove=${this.removeWidget}
            @move=${this.moveWidget}
            @size-change=${this.resizeWidget}
            ?disable-left-and-up=${bindings.disableLeftAndUp}
            ?disable-right-and-down=${bindings.disableRightAndDown}
            ?editing=${this.editing}>
          </sakai-forums-widget>
        `);
      case "status":
        return this._wrapTile(tile, index, html`
          <sakai-status-widget
            id="${r}"
            class="widget"
            state="${w?.state}"
            tile-w="${bindings.tileW}"
            display-w="${bindings.displayW}"
            tile-h="${bindings.tileH}"
            @remove=${this.removeWidget}
            @move=${this.moveWidget}
            @size-change=${this.resizeWidget}
            ?disable-left-and-up=${bindings.disableLeftAndUp}
            ?disable-right-and-down=${bindings.disableRightAndDown}
            ?editing=${this.editing}>
          </sakai-status-widget>
        `);
      case PICKER_TILE_ID:
        return nothing;
      default:
        if (typeof r === "string" && r.startsWith("sitestats-")) {
          return this._wrapTile(tile, index, html`
            <sakai-sitestats-widget
              id="${r}"
              sitestats-widget-id="${r.substring("sitestats-".length)}"
              site-id="${ifDefined(this.siteId ? this.siteId : undefined)}"
              user-id="${ifDefined(this.userId ? this.userId : undefined)}"
              class="widget"
              state="${w?.state}"
              tile-w="${bindings.tileW}"
              display-w="${bindings.displayW}"
              tile-h="${bindings.tileH}"
              @remove=${this.removeWidget}
              @move=${this.moveWidget}
              @size-change=${this.resizeWidget}
              ?disable-left-and-up=${bindings.disableLeftAndUp}
              ?disable-right-and-down=${bindings.disableRightAndDown}
              ?editing=${this.editing}>
            </sakai-sitestats-widget>
          `);
        }
        return "";
    }
  }

  render() {

    const available = this.editing ? this._availableWidgets() : [];

    return html`
      ${this.editing && available.length ? html`
        <div id="available-widgets" role="list" aria-label="${this._i18n.add_a_widget}">
          ${available.map(id => html`
            <div class="available-widget"
                role="listitem"
                draggable="true"
                @dragstart=${e => this._onAvailableDragStart(e, id)}>
              <span class="available-title">${this._widgetName(id)}</span>
              <button type="button"
                  class="btn btn-icon btn-sm"
                  @click=${() => this._addWidget(id)}
                  title="${this._i18n.add_a_widget}: ${this._widgetName(id)}"
                  aria-label="${this._i18n.add_a_widget}: ${this._widgetName(id)}">
                <span class="si si-add fs-6" aria-hidden="true"></span>
              </button>
            </div>
          `)}
        </div>
      ` : nothing}

      <div id="grid"
          @dragover=${this.editing ? this._onDragOver : nothing}
          @drop=${this.editing ? e => this._onDrop(e, this._layout.length) : nothing}>
        ${repeat(this._layout ?? [], tileKey, (tile, i) => html`
          ${this.getWidget(tile, i)}
        `)}
      </div>
    `;
  }

  static styles = [
    SakaiShadowElement.styles,
    css`
      :host {
        display: block;
        width: var(--sakai-widget-panel-width, 100%);
        background-color: var(--sakai-tool-bg-color);
        --sakai-widget-panel-row-height: 420px;
        --sakai-widget-panel-min-widget-width: 380px;
      }
      a {
        color: var(--link-color);
      }

      #available-widgets {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
        margin-bottom: 1rem;
        padding: 0.75rem;
        background-color: var(--sakai-dashboard-widget-secondary-bg-color, rgb(244, 244, 244));
        border: 1px solid var(--sakai-border-color, #ddd);
        border-radius: var(--bs-border-radius, 0.375rem);
      }

      .available-widget {
        display: inline-flex;
        align-items: center;
        gap: 0.35rem;
        padding: 0.35rem 0.5rem 0.35rem 1rem;
        background: var(--sakai-background-color-1, #fff);
        border: 1px solid var(--sakai-border-color, #ddd);
        border-radius: var(--bs-border-radius, 0.375rem);
        cursor: grab;
      }

      .available-title {
        font-size: 0.875rem;
        font-weight: 600;
      }

      #grid {
        display: grid;
        grid-template-columns: repeat(
          auto-fit,
          minmax(min(100%, var(--sakai-widget-panel-min-widget-width)), 1fr)
        );
        grid-auto-rows: var(--sakai-widget-panel-row-height);
        gap: var(--sakai-widget-panel-gutter-width, 1rem);
        min-height: 4rem;
      }

      .widget-tile {
        min-height: 0;
        max-height: 100%;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }

      .widget-tile.dragging {
        opacity: 0.5;
      }

      .widget-tile .widget {
        flex: 1 1 auto;
        min-height: 0;
        height: 100%;
        width: 100%;
        overflow: hidden;
      }
    `
  ];
}
