import { css, html } from "lit";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";
import { withQueryParam } from "./site-stats-url.js";

const ITEM_TYPE = "itemType";
const ALL = "all";

export class SakaiSiteStatsToolFilter extends SakaiShadowElement {

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem;
      }
    `,
  ];

  constructor() {

    super();
    this.loadTranslations("sitestats");
    this._onClick = event => this._toggle(event);
  }

  render() {
    return html`<slot></slot>`;
  }

  connectedCallback() {

    super.connectedCallback();
    this.addEventListener("click", this._onClick);
    this._syncChipState();
  }

  disconnectedCallback() {

    this.removeEventListener("click", this._onClick);
    super.disconnectedCallback();
  }

  _chips() {
    return [ ...this.querySelectorAll("[data-tool-id]") ];
  }

  _selected() {
    return this._chips().filter(chip => chip.getAttribute("aria-pressed") === "true");
  }

  _toggle(event) {

    const chip = event.target.closest("[data-tool-id]");
    if (!chip || !this.contains(chip)) {
      return;
    }

    const pressed = chip.getAttribute("aria-pressed") === "true";
    if (pressed && this._selected().length === 1) {
      return;
    }

    chip.setAttribute("aria-pressed", pressed ? "false" : "true");
    this._syncChipState();
    this._apply();
  }

  _syncChipState() {

    const selected = this._selected();
    this._chips().forEach(chip => {
      const on = chip.getAttribute("aria-pressed") === "true";
      const lastSelected = on && selected.length === 1;
      chip.toggleAttribute("aria-disabled", lastSelected);
      if (lastSelected && this._i18n?.overview_tool_filters_min_one) {
        chip.title = this._i18n.overview_tool_filters_min_one;
      } else {
        chip.title = chip.getAttribute("aria-label") || "";
        if (!chip.title) {
          chip.removeAttribute("title");
        }
      }
    });
  }

  _itemType() {

    const chips = this._chips();
    const selected = this._selected();
    if (!chips.length || selected.length === chips.length) {
      return ALL;
    }
    return selected.map(chip => chip.dataset.toolId).join(",");
  }

  _apply() {

    const widget = this.closest(".sitestats-widget");
    if (!widget) {
      return;
    }

    const itemType = this._itemType();
    const param = itemType === ALL ? "" : itemType;

    const metrics = widget.querySelector("sakai-sitestats-widget-metrics");
    if (metrics && widget.dataset.metricsEndpoint) {
      metrics.endpoint = withQueryParam(widget.dataset.metricsEndpoint, ITEM_TYPE, param);
    }

    const highlights = widget.querySelector("sakai-sitestats-highlights");
    if (highlights && widget.dataset.highlightsEndpoint) {
      highlights.endpoint = withQueryParam(widget.dataset.highlightsEndpoint, ITEM_TYPE, param);
    }

    widget.querySelectorAll("sakai-sitestats-widget-tab").forEach(tab => {
      if (tab.endpoint) {
        tab.endpoint = withQueryParam(tab.endpoint, ITEM_TYPE, param);
      }
    });
  }
}
