import "../sakai-toggle.js";
import { html } from "lit";
import { expect, fixture, waitUntil } from "@open-wc/testing";

describe("sakai-toggle tests", () => {

  it ("renders correctly", async () => {

    let el = await fixture(html`
      <sakai-toggle text-on="On" text-off="Off"></sakai-toggle>
    `);

    const toggle = el.shadowRoot.getElementById("toggle");
    expect(toggle).to.exist;
    expect(el.on).to.not.exist;
    expect(toggle.children.item(0).innerHTML).to.contain("Off");
    expect(toggle.children.item(1).innerHTML).to.contain("On");

    toggle.click();
    await el.updateComplete;
    expect(el.on).to.be.true;

    toggle.dispatchEvent(new KeyboardEvent('keyup', { 'keyCode': 13 }));
    await el.updateComplete;
    expect(el.on).to.be.false;

    expect(el.checked).to.be.false;
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-toggle></sakai-toggle>
    `);

    expect(el).to.be.accessible();
  });
});
