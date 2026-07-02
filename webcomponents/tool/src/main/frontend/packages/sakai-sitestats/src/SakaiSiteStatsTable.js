import { html, css, nothing } from "lit";
import { repeat } from "lit/directives/repeat.js";
import { SakaiShadowElement } from "@sakai-ui/sakai-element";

export class SakaiSiteStatsTable extends SakaiShadowElement {

  static properties = {
    table: { type: Object },
    compact: { type: Boolean },
  };

  static styles = [
    ...SakaiShadowElement.styles,
    css`
      :host {
        display: block;
      }

      .table-wrap {
        overflow-x: auto;
      }

      table {
        border-collapse: collapse;
        inline-size: 100%;
        min-inline-size: 32rem;
        background: var(--sakai-background-color-1, #fff);
      }

      caption {
        padding-block: 0.5rem;
        text-align: start;
        font-weight: 600;
      }

      th,
      td {
        border: 1px solid var(--sakai-border-color, #d8dde6);
        padding: 0.45rem 0.6rem;
        text-align: start;
        vertical-align: top;
      }

      th {
        background: var(--sakai-background-color-2, #f5f7f9);
        font-weight: 600;
        white-space: nowrap;
      }

      td[data-align="end"],
      th[data-align="end"] {
        text-align: end;
      }

      tbody tr:nth-child(even) {
        background: var(--sakai-background-color-2, #f8f9fb);
      }

      a:any-link {
        color: var(--sakai-primary-color-1, #1769aa);
      }

      a:focus-visible {
        outline: 3px solid var(--focus-outline-color, #1d6fc2);
        outline-offset: 2px;
      }

      .empty {
        border: 1px solid var(--sakai-border-color, #d8dde6);
        padding: 1rem;
        background: var(--sakai-background-color-2, #f8f9fb);
      }

      :host([compact]) th,
      :host([compact]) td {
        padding: 0.35rem 0.45rem;
      }
    `
  ];

  constructor() {

    super();

    this.loadTranslations("sitestats");
  }

  render() {

    if (!this._i18n) return nothing;

    if (!this.table || !Array.isArray(this.table.columns) || this.table.columns.length === 0) {
      return html`<div class="empty">${this._i18n.no_data_available}</div>`;
    }

    const rows = Array.isArray(this.table.rows) ? this.table.rows : [];

    return html`
      <div class="table-wrap">
        <table>
          ${this.table.caption ? html`<caption>${this.table.caption}</caption>` : nothing}
          <thead>
            <tr>
              ${repeat(this.table.columns, column => column.key, column => html`
                <th scope="col" data-align="${column.align || ""}">${column.label}</th>
              `)}
            </tr>
          </thead>
          <tbody>
            ${rows.length ? repeat(rows, (row, index) => index, row => html`
              <tr>
                ${repeat(this.table.columns, column => column.key, column => this._cell(row, column))}
              </tr>
            `) : html`
              <tr>
                <td colspan="${this.table.columns.length}" class="empty">${this._i18n.no_data_available}</td>
              </tr>
            `}
          </tbody>
        </table>
      </div>
    `;
  }

  _cell(row, column) {

    const cell = row.cells && row.cells[column.key] ? row.cells[column.key] : {};
    const display = cell.display ?? "";
    const content = cell.href ? html`
      <a href="${cell.href}" target="_blank" rel="noopener noreferrer">${display}</a>
    ` : display;

    return html`<td data-align="${column.align || ""}">${content}</td>`;
  }
}
