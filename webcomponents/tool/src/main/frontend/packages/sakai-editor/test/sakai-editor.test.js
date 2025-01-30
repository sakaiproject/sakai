import "../sakai-editor.js";
import { html } from "lit";
import { expect, fixture, oneEvent } from "@open-wc/testing";

describe("sakai-editor tests", () => {

  window.sakai = {};

  beforeEach(() =>  {

    window.top.portal = { locale: "en_GB" };
  });

  it ("renders with textarea correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-editor textarea content="eggs" set-focus></sakai-editor>
    `);

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
    let el = await fixture(html`
      <sakai-editor content="eggs"></sakai-editor>
    `);

    expect(el.getContent()).to.contain("eggs");

    el.setContent("vinegar");
    expect(el.getContent()).to.equal("vinegar");
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-editor textarea></sakai-editor>
    `);

    expect(el).to.be.accessible();
  });
});
