import "../sakai-pager.js";
import { elementUpdated, expect, fixture, html, oneEvent } from "@open-wc/testing";

describe("sakai-pager tests", () => {

  it ("to fire page-selected event on Lion Pager's current-changed event", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    const el = await fixture(html`
      <sakai-pager></sakai-sakai-pager>
    `);

    el.current = 3;

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const listener = oneEvent(el, "page-selected");
    el.dispatchEvent(new CustomEvent("current-changed"));

    const { detail } = await listener;

    expect(detail.page).to.equal(3);
  });
});
