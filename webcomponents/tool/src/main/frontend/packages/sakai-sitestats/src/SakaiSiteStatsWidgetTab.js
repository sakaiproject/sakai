import { css, html, nothing } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import "../sakai-sitestats-report-panel.js";

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

      .form-select {
        width: 100%;
      }
    `,
  ];

  constructor() {

    super();
    this.filters = [];
    this.open = false;
    this._activated = false;
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
        <div class="filters" @change=${this._filterChanged}>
          ${this.filters.map((filter, index) => html`
            <div class="filter">
              <label class="form-label" for=${`filter-${index}`}>${filter.label}</label>
              <select
                  class="form-select form-select-sm"
                  id=${`filter-${index}`}
                  data-report-filter=${filter.id}
                  .value=${this._filterValue(filter.id)}>
                ${filter.options.map(option => html`
                  <option value=${option.value}>${option.label}</option>
                `)}
              </select>
            </div>
          `)}
        </div>
      `;
    }

    if (this.querySelector("[slot='filter']")) {
      return html`
        <div class="filters" @change=${this._filterChanged}>
          <slot name="filter" @slotchange=${this._syncFilters}></slot>
        </div>
      `;
    }

    return nothing;
  }

  _toggle(event) {

    this.open = event.target.open;
    if (this.open) {
      this._activated = true;
    }
  }

  _filterChanged(event) {

    const filter = event.composedPath().find(element => element?.dataset?.reportFilter);
    if (!filter || !this.endpoint) {
      return;
    }

    const endpoint = new URL(this.endpoint, window.location.href);
    endpoint.searchParams.set(filter.dataset.reportFilter, filter.value);
    endpoint.searchParams.set("page", "1");
    this.endpoint = `${endpoint.pathname}${endpoint.search}`;
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
  }
}
