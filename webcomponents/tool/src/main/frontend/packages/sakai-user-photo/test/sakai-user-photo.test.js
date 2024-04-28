import "../sakai-user-photo.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";

describe("sakai-user-photo tests", () => {

  window.profile = { attachPopups: () => {} };

  it ("renders correctly", async () => {
 
    let el = await fixture(html`
      <sakai-user-photo user-id="${data.userId}"
          classes="small"
          label="eggs"
          profile-popup="on">
      </sakai-user-photo>
    `);

    await waitUntil(() => el._generatedId);

    const div = document.getElementById(el._generatedId);
    expect(div).to.exist;
    expect(div.classList.contains("small")).to.be.true;
    expect(div.style.cursor).to.equal("pointer");
    expect(div.getAttribute("aria-label")).to.equal("eggs");
    expect(div.dataset.userId).to.equal(data.userId);
  });

  it ("renders for print correctly", async () => {
 
    let el = await fixture(html`<sakai-user-photo user-id="${data.userId}" print></sakai-user-photo>`);
    expect(el.querySelector("img")).to.exist;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-user-photo user-id="${data.userId}"></sakai-user-photo>`);
    await expect(el).to.be.accessible();
  });
});
