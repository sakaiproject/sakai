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
        button {
          background-color: var(--sakai-background-color-1);
          color: var(--sakai-text-color-2);
          border: solid 1px var(--sakai-border-color);
          padding: 8px 12px;
          border-radius: 4px;
          cursor: pointer;
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
