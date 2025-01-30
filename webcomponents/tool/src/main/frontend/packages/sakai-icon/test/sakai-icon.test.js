import "../sakai-icon.js";
import { expect, fixture } from "@open-wc/testing";
import { html } from "lit";

describe("sakai-icon tests", () => {

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-icon type="lock"></sakai-icon>`);

    expect(el.shadowRoot.querySelector("svg")).to.exist;
    expect(el.shadowRoot.querySelector("svg").dataset.icon).to.equal("lock");
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-icon type="lock"></sakai-icon>`);

    await expect(el).to.be.accessible();
  });
});
