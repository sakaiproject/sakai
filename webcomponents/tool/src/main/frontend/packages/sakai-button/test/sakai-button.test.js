import "../sakai-button.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";

describe("sakai-button tests", () => {

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-button primary>Eggs</sakai-button>`);

    expect(el.shadowRoot.querySelector("button").classList.contains("primary")).to.be.true;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-button>Eggs</sakai-button>`);

    await expect(el).to.be.accessible();
  });
});
