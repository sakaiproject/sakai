import "../sakai-icon.js";
import { expect, fixture, html } from "@open-wc/testing";

describe("sakai-icon tests", () => {

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-icon type="lock"></sakai-icon>`);

    expect(el.shadowRoot.querySelector("svg")).to.exist;
    expect(el.shadowRoot.querySelector("svg").dataset.icon).to.equal("lock");
    await expect(el).to.be.accessible();
  });
});
