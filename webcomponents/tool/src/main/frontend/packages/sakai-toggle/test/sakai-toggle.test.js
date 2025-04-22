import "../sakai-toggle.js";
import { elementUpdated, expect, fixture, html } from "@open-wc/testing";

describe("sakai-toggle tests", () => {

  it ("renders correctly", async () => {

    const el = await fixture(html`
      <sakai-toggle text-on="On" text-off="Off"></sakai-toggle>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    const toggle = el.shadowRoot.getElementById("toggle");
    expect(toggle).to.exist;
    expect(el.on).to.not.exist;
    expect(toggle.children.item(0).innerHTML).to.contain("Off");
    expect(toggle.children.item(1).innerHTML).to.contain("On");

    toggle.click();
    await elementUpdated(el);
    expect(el.on).to.be.true;
    await expect(el).to.be.accessible();

    toggle.dispatchEvent(new KeyboardEvent('keyup', { 'keyCode': 13 }));
    await el.updateComplete;
    expect(el.on).to.be.false;

    expect(el.checked).to.be.false;
  });
});
