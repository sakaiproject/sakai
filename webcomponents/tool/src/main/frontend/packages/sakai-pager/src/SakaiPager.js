import { LionPagination } from "@lion/pagination";
import { css } from "lit";

export class SakaiPager extends LionPagination {

  constructor() {

    super();

    this.addEventListener("current-changed", e => {

      e.stopPropagation();
      this.dispatchEvent(new CustomEvent("page-selected", { detail: { page: this.current }, bubbles: true }));
    });
  }

  static get styles() {
    return [
      LionPagination.styles,
      css`
        :host {
          display: block;
          margin: 0;
        }

        ul {
          margin: 0;
          padding: 0;
        }

        button {
          background-color: var(--sakai-background-color-1);
          color: var(--sakai-text-color-2);
          border: solid 1px var(--sakai-border-color);
          padding: var(--sakai-pager-button-padding, 8px 12px);
          font-size: var(--sakai-pager-font-size, inherit);
          border-radius: 4px;
          cursor: pointer;
          line-height: 1.2;
        }

        button:hover {
          font-weight: bold;
        }

        button:disabled {
          background-color: var(--button-disabled-border-color);
          cursor: not-allowed;
        }

        button[aria-current="true"] {
          background-color: var(--button-primary-background);
          color: var(--button-primary-active-text-color);
          font-weight: bold;
        }
      `,
    ];
  }
}
