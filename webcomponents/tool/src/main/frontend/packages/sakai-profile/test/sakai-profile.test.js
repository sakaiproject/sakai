import "../sakai-profile.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import * as pronunciationPlayerData from "../../sakai-pronunciation-player/test/data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-profile tests", () => {

  window.top.portal = { siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(pronunciationPlayerData.i18nUrl, pronunciationPlayerData.i18n, { overwriteRoutes: true })
    .get(data.profileUrl, data.profile, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {
 
    let el = await fixture(html`<sakai-profile user-id="${data.userId}"></sakai-profile>`);

    await elementUpdated(el);

    el.fetchProfileData();
    await waitUntil(() => el._profile);

    await elementUpdated(el);

    expect(el.shadowRoot.querySelector("div.container")).to.exist;
    expect(el.shadowRoot.querySelectorAll("div.body > div").length).to.equal(5);
    expect(el.shadowRoot.querySelector("div.role")).to.exist;
    expect(el.shadowRoot.querySelector("div.role").innerHTML).to.contain(data.profile.role);
    expect(el.shadowRoot.querySelector("sakai-pronunciation-player")).to.exist;
    expect(el.shadowRoot.querySelector("div.url")).to.exist;
  });
});
