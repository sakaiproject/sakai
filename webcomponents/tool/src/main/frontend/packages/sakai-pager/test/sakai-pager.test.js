import "../sakai-pager.js";
import { html } from "lit";
import { elementUpdated, expect, fixture, oneEvent } from "@open-wc/testing";

describe("sakai-pager tests", () => {

  it ("to fire page-selected event on Lion Pager's current-changed event", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    const el = await fixture(html`
      <sakai-pager></sakai-sakai-pager>
    `);

    el.current = 3;

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    setTimeout(() => el.dispatchEvent(new CustomEvent("current-changed")));

    const { detail } = await oneEvent(el, "page-selected");

    expect(detail.page).to.equal(3);
  });
});
