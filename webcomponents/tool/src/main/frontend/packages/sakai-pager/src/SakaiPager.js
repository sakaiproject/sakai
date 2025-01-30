import { LionPagination } from "@lion/pagination";

export class SakaiPager extends LionPagination {

  constructor() {

    super();

    this.addEventListener("current-changed", e => {

      e.stopPropagation();
      this.dispatchEvent(new CustomEvent("page-selected", { detail: { page: this.current }, bubbles: true }));
    });
  }
}
