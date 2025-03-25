import "../sakai-button.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";

describe("sakai-button tests", () => {

  it ("renders correctly with text content", async () => {
 
    const el = await fixture(html`<sakai-button title="eggs button" primary>Eggs</sakai-button>`);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelector("button").classList.contains("primary")).to.be.true;
  });

  it ("renders correctly with no text", async () => {
 
    const title = "No text button";
    const el = await fixture(html`<sakai-button title="${title}"></sakai-button>`);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelector("button").getAttribute("title")).to.equal(title);
  });
});
