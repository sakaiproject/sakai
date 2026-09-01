import { css, html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "../sakai-sitestats-report-panel.js";

const DATE_FILTER = "date";
const WHEN_CUSTOM = "when-custom";
const WHEN_FROM = "whenFrom";
const WHEN_TO = "whenTo";

export class SakaiSiteStatsWidgetTab extends SakaiShadowElement {

  static properties = {
    endpoint: { type: String },
    filters: { attribute: false },
    open: { type: Boolean, reflect: true, useDefault: true },
    _activated: { state: true },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host {
        display: block;
        padding: 0.5rem 1rem;
      }

      summary {
        cursor: pointer;
        font-weight: 600;
      }

      .content {
        padding-block-start: 1rem;
      }

      .filters {
        display: flex;
        flex-wrap: wrap;
        gap: 1rem;
        margin-block-end: 1rem;
      }

      .filter,
      ::slotted(.sitestats-widget-filter) {
        flex: 0 1 16rem;
      }

      .form-select,
      .form-control {
        width: 100%;
      }
    `,
  ];

  constructor() {

    super();
    this.filters = [];
    this.open = false;
    this._activated = false;
    this._onFilterEvent = event => this._filterChanged(event);
  }

  connectedCallback() {

    super.connectedCallback();
    this.addEventListener("change", this._onFilterEvent);
  }

  disconnectedCallback() {

    this.removeEventListener("change", this._onFilterEvent);
    super.disconnectedCallback();
  }

  firstUpdated() {

    this._syncFilters();
  }

  updated(changedProperties) {

    if (changedProperties.has("endpoint")) {
      this._syncFilters();
    }
  }

  render() {

    return html`
      <details ?open=${this.open} @toggle=${this._toggle}>
        <summary><slot name="title"></slot></summary>
        <div class="content">
          ${this._renderFilters()}
          ${this.open || this._activated ? html`
            <sakai-sitestats-report-panel .endpoint=${this.endpoint}></sakai-sitestats-report-panel>
          ` : nothing}
        </div>
      </details>
    `;
  }

  _renderFilters() {

    if (this.filters.length) {
      return html`
        <div class="filters">
          ${this.filters.filter(filter => this._showFilter(filter)).map(filter => html`
            <div class="filter">
              <label class="form-label" for=${`filter-${filter.id}`}>${filter.label}</label>
              ${this._renderFilterControl(filter)}
            </div>
          `)}
        </div>
      `;
    }

    if (this.querySelector("[slot='filter']")) {
      return html`
        <div class="filters">
          <slot name="filter" @slotchange=${this._syncFilters}></slot>
        </div>
      `;
    }

    return nothing;
  }

  _renderFilterControl(filter) {

    const id = `filter-${filter.id}`;
    const value = this._filterValue(filter.id) || filter.value || "";

    if (filter.type === "number") {
      return html`
        <input
            class="form-control form-control-sm"
            id=${id}
            type="number"
            data-report-filter=${filter.id}
            .value=${value}>
      `;
    }

    if (filter.type === "date") {
      return html`
        <input
            class="form-control form-control-sm"
            id=${id}
            type="date"
            data-report-filter=${filter.id}
            .value=${value}>
      `;
    }

    return html`
      <select
          class="form-select form-select-sm"
          id=${id}
          data-report-filter=${filter.id}
          .value=${value}>
        ${(filter.options || []).map(option => html`
          <option value=${option.value}>${option.label}</option>
        `)}
      </select>
    `;
  }

  _showFilter(filter) {

    if (filter.id === WHEN_FROM || filter.id === WHEN_TO) {
      return this._customDateSelected();
    }
    return true;
  }

  _customDateSelected() {

    return this._controlValue(DATE_FILTER) === WHEN_CUSTOM
      || this._filterValue(DATE_FILTER) === WHEN_CUSTOM;
  }

  _control(id) {

    return this.renderRoot?.querySelector(`[data-report-filter="${id}"]`)
      || this.querySelector(`[data-report-filter="${id}"]`);
  }

  _controlValue(id) {

    return this._control(id)?.value || "";
  }

  _toggle(event) {

    this.open = event.target.open;
    if (this.open) {
      this._activated = true;
    }
  }

  _filterChanged(event) {

    const filter = event.composedPath().find(element => element?.dataset?.reportFilter)
      || event.target?.closest?.("[data-report-filter]");
    if (!filter || !this.endpoint) {
      return;
    }

    if (filter.dataset.reportFilter === DATE_FILTER && filter.value === WHEN_CUSTOM) {
      this._showCustomDates();
      this.requestUpdate();
      this.updateComplete.then(() => {
        this._showCustomDates();
        this._commitEndpoint(filter);
      });
      return;
    }

    this._commitEndpoint(filter);
  }

  _showCustomDates() {

    this._syncCustomDateVisibility();
    [ WHEN_FROM, WHEN_TO ].forEach(id => {
      const control = this._control(id);
      // type=date inside [hidden] can report an empty .value; the value attribute is the same input.
      if (control && !control.value && control.hasAttribute("value")) {
        control.value = control.getAttribute("value");
      }
    });
  }

  _commitEndpoint(filter) {

    const id = filter.dataset.reportFilter;
    const endpoint = new URL(this.endpoint, window.location.href);
    this._writeParam(endpoint, id, filter.value);

    if (id === DATE_FILTER && filter.value !== WHEN_CUSTOM) {
      endpoint.searchParams.delete(WHEN_FROM);
      endpoint.searchParams.delete(WHEN_TO);
    }

    if (id === WHEN_FROM || id === WHEN_TO || (id === DATE_FILTER && filter.value === WHEN_CUSTOM)) {
      endpoint.searchParams.set(DATE_FILTER, WHEN_CUSTOM);
      this._writeParam(endpoint, WHEN_FROM, this._controlValue(WHEN_FROM));
      this._writeParam(endpoint, WHEN_TO, this._controlValue(WHEN_TO));
    }

    endpoint.searchParams.set("page", "1");
    const next = `${endpoint.pathname}${endpoint.search}`;
    if (next !== this.endpoint) {
      this.endpoint = next;
    }
  }

  _writeParam(endpoint, id, value) {

    if (value) {
      endpoint.searchParams.set(id, value);
    } else {
      endpoint.searchParams.delete(id);
    }
  }

  _filterValue(id) {

    if (!this.endpoint) {
      return "";
    }
    return new URL(this.endpoint, window.location.href).searchParams.get(id) ?? "";
  }

  _syncFilters() {

    if (!this.endpoint) {
      return;
    }

    const endpoint = new URL(this.endpoint, window.location.href);
    const filters = [
      ...this.renderRoot.querySelectorAll("[data-report-filter]"),
      ...this.querySelectorAll("[data-report-filter]"),
    ];
    filters.forEach(filter => {
      const value = endpoint.searchParams.get(filter.dataset.reportFilter);
      if (value !== null) {
        filter.value = value;
      }
    });
    this._syncCustomDateVisibility();
  }

  _syncCustomDateVisibility() {

    const show = this._customDateSelected();
    this.querySelectorAll(`[data-report-filter="${WHEN_FROM}"], [data-report-filter="${WHEN_TO}"]`).forEach(element => {
      const wrapper = element.closest("[slot='filter']");
      if (wrapper) {
        wrapper.hidden = !show;
      }
    });
  }
}
