import "../sakai-options-menu.js";
import { expect, fixture } from "@open-wc/testing";
import { html } from "lit";

describe("sakai-options-menu tests", () => {

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-options-menu><div slot="invoker">Eggs</div></sakai-options-menu>`);

    expect(el.shadowRoot.querySelector("lion-dialog")).to.exist;
  });

  it ("is accessible", async () => {

    let el = await fixture(html`<sakai-options-menu></sakai-options-menu>`);
    await expect(el).to.not.be.accessible();

    el = await fixture(html`<sakai-options-menu><div slot="invoker">Eggs</div></sakai-options-menu>`);
    await expect(el).to.be.accessible();
  });
});
