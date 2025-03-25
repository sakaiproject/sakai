import { SakaiPageableElement } from "../src/SakaiPageableElement.js";
import { elementUpdated, expect, fixture, html } from "@open-wc/testing";

describe("sakai-pageable-element tests", () => {

  class MyPageable extends SakaiPageableElement {

    constructor() {

      super();

      this.showPager = true;
    }

    content() {
      
      return html`
        ${this.data.map(p => html`
          <div id="${p}">${p}</div>
        `)}
      `;
    }

    loadAllData() {

      this.data = [ "chips", "fries", "spuds", "potatoes", "frites" ];
      return Promise.resolve();
    }
  }
  customElements.define("my-pageable", MyPageable);

  it ("is subclassed and renders correctly", async () => {
 
    let el = await fixture(html`<my-pageable></my-pageable>`);

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.count).to.equal(1);
    expect(el.shadowRoot.getElementById("wrapper")).to.exist;
    expect(el.shadowRoot.getElementById("pager")).to.exist;
    expect(el.shadowRoot.querySelector("div#chips")).to.exist;
  });
});
