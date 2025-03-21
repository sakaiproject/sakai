import "../sakai-user-photo.js";
import { elementUpdated, expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import * as profileData from "../../sakai-profile/test/data.js";
import fetchMock from "fetch-mock/esm/client";

fetchMock
  .get(profileData.i18nUrl, profileData.i18n, { overwriteRoutes: true })
  .get("*", 500, { overwriteRoutes: true });

describe("sakai-user-photo tests", () => {

  it ("renders correctly", async () => {
 
    let el = await fixture(html`
      <sakai-user-photo user-id="${data.userId}"
          classes="small"
          label="eggs"
          profile-popup="on">
      </sakai-user-photo>
    `);

    await elementUpdated(el);

    const div = el.querySelector("div");
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

    let el = await fixture(html`
      <sakai-user-photo user-id="${data.userId}"
          label="eggs"
          profile-popup="on">
      </sakai-user-photo>
    `);

    await elementUpdated(el);
    await expect(el).to.be.accessible();
  });
});
