import { css } from "lit";
import { siteStatsVisuallyHiddenStyles } from "./site-stats-a11y-styles.js";

export const siteStatsCompactMetricsStyles = css`
  ${siteStatsVisuallyHiddenStyles}

  :host {
    display: block;
  }

  .sitestats-metrics-status {
    padding: 8px 16px;
    background: var(--sakai-background-color-2);
    border-block: 1px solid var(--sakai-border-color);
  }

  .sitestats-metrics {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
    margin: 0;
    background: var(--sakai-background-color-1);
    overflow: visible;
  }

  .sitestats-metric {
    min-width: 0;
    padding: 8px 16px;
    position: relative;
  }

  .sitestats-metric dt {
    font-size: 0.875rem;
    font-weight: 600;
  }

  .sitestats-metric.has-help {
    cursor: help;
  }

  .sitestats-metric-help-trigger {
    position: absolute;
    inset: 0;
    z-index: 1;
    padding: 0;
    border: 0;
    background: transparent;
    cursor: help;
  }

  .sitestats-metric-help-trigger:focus-visible {
    outline: 2px solid var(--focus-outline-color);
    outline-offset: -2px;
  }

  .sitestats-metric-help {
    position: absolute;
    inset-inline: 4px;
    inset-block-start: calc(100% - 4px);
    z-index: 4;
    padding: 4px 8px;
    border: 1px solid var(--sakai-border-color);
    border-radius: 4px;
    background: var(--sakai-background-color-1);
    color: var(--sakai-text-color-1);
    font-size: 0.8125rem;
    font-weight: 400;
    line-height: 1.35;
    max-width: 18rem;
    box-shadow: var(--elevation-4dp);
    opacity: 0;
    pointer-events: none;
    visibility: hidden;
  }

  .sitestats-metric.has-help:hover .sitestats-metric-help,
  .sitestats-metric.has-help:focus-within .sitestats-metric-help,
  .sitestats-metric.has-help.is-open .sitestats-metric-help {
    opacity: 1;
    visibility: visible;
  }

  .sitestats-metric-primary {
    line-height: 1.25;
    font-weight: 800;
    word-break: break-word;
  }

  .sitestats-metric-percentage {
    font-size: 1rem;
    margin-inline-start: 4px;
    color: var(--sakai-text-color-dimmed);
  }

  /* Dashboard widget only: card layout with inline expandable help. */
  :host([dashboard]) {
    height: 100%;
    min-height: 0;
    overflow: auto;
  }

  :host([dashboard]) .sitestats-metrics {
    grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
    gap: 0.5rem;
    padding: 0.5rem;
    align-content: start;
    background: transparent;
    overflow: visible;
  }

  :host([dashboard]) .sitestats-metric {
    display: block;
    box-sizing: border-box;
    margin: 0;
    padding: 0.6rem 0.75rem;
    position: static;
    background: var(--sakai-background-color-1, #fff);
    border: 1px solid var(--sakai-border-color, #ddd);
    border-radius: var(--bs-border-radius, 0.375rem);
    cursor: default;
  }

  :host([dashboard]) .sitestats-metric-header {
    display: flex;
    flex-direction: column;
    gap: 0.2rem;
    min-width: 0;
    margin-block-start: 0.35rem;
  }

  :host([dashboard]) .sitestats-metric dt {
    font-size: 0.8125rem;
    margin: 0;
  }

  :host([dashboard]) .sitestats-metric dd {
    margin: 0;
  }

  :host([dashboard]) .sitestats-metric-percentage {
    font-size: 0.875rem;
  }

  :host([dashboard]) .sitestats-metric-help {
    position: static;
    inset: auto;
    z-index: auto;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    overflow: hidden;
    margin: 0;
    padding: 0;
    border: 0;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
    max-width: none;
    opacity: 1;
    pointer-events: auto;
    visibility: visible;
    text-align: start;
    font: inherit;
    font-size: 0.75rem;
    font-weight: 400;
    line-height: 1.35;
    color: var(--sakai-text-color-dimmed);
    cursor: pointer;
  }

  :host([dashboard]) .sitestats-metric-help.is-expanded {
    display: block;
    -webkit-line-clamp: unset;
    line-clamp: unset;
    overflow: visible;
    white-space: normal;
  }
`;

export const siteStatsCompactHighlightsStyles = css`
  :host {
    display: block;
    width: 100%;
    padding: 8px;
    border-block-top: 1px solid var(--sakai-border-color);
    background: var(--sakai-background-color-2);
    box-sizing: border-box;
  }

  :host([hidden]) {
    display: none;
  }

  .metric-highlight {
    display: block;
    width: 100%;
    color: var(--sakai-text-color-dimmed);
    font-size: 0.875rem;
    font-weight: 400;
    text-align: center;
  }

  .metric-highlight + .metric-highlight {
    margin-block-start: 0.75rem;
  }

  :host([dashboard]) {
    padding: 0.35rem 0.5rem;
    background: var(--sakai-dashboard-widget-secondary-bg-color, var(--sakai-background-color-2));
    flex: 0 0 auto;
  }

  :host([dashboard]) .metric-highlight {
    font-size: 0.8125rem;
  }

  :host([dashboard]) .metric-highlight + .metric-highlight {
    margin-block-start: 0.5rem;
  }
`;
