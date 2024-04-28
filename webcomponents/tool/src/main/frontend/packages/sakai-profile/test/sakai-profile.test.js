import "../sakai-profile.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-profile tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.profileUrl, data.profile, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {
 
    let el = await fixture(html`<sakai-profile user-id="${data.userId}"></sakai-profile>`);
    await waitUntil(() => el._i18n);
    expect(el.shadowRoot.querySelector("div.container")).to.exist;
    expect(el.shadowRoot.querySelectorAll("div.body > div").length).to.equal(5);
    expect(el.shadowRoot.querySelector("div.role")).to.exist;
    expect(el.shadowRoot.querySelector("div.role").innerHTML).to.contain(data.profile.role);
    expect(el.shadowRoot.querySelector("div.pronunciation > div").innerHTML).to.contain(data.profile.pronunciation);
    expect(el.shadowRoot.querySelector("sakai-pronunciation-player")).to.exist;
    expect(el.shadowRoot.querySelector("div.url")).to.exist;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-profile user-id="${data.userId}"></sakai-profile>`);
    await waitUntil(() => el._i18n);
    await expect(el).to.be.accessible();
  });
});
