import "../sakai-editor.js";
import { elementUpdated, expect, fixture, html, oneEvent } from "@open-wc/testing";

describe("sakai-editor tests", () => {

  window.sakai = {};

  beforeEach(() =>  {

    //window.top.portal = { locale: "en_GB" };
  });

  it ("renders with textarea correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    const el = await fixture(html`
      <sakai-editor textarea content="eggs" set-focus></sakai-editor>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelectorAll("textarea").length).to.equal(1);
    expect(el.getContent()).to.equal("eggs");
    const textarea = el.querySelector("textarea");
    expect(document.activeElement.tagName).to.equal("TEXTAREA");
    const listener = oneEvent(el, "changed");
    textarea.value = "chips";
    textarea.dispatchEvent(new Event("input", { bubbles: true, cancelable: true }));
    const { detail } = await listener;
    expect(detail.content).to.equal("chips");

    el.setContent("vinegar");
    expect(el.getContent()).to.equal("vinegar");
  });

  it ("renders with ckeditor correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    const el = await fixture(html`
      <sakai-editor content="eggs"></sakai-editor>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.getContent()).to.contain("eggs");

    el.setContent("vinegar");
    expect(el.getContent()).to.equal("vinegar");
  });
});
