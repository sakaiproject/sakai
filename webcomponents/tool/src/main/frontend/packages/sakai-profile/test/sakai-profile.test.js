import "../sakai-profile.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import * as pronunciationPlayerData from "../../sakai-pronunciation-player/test/data.js";
import fetchMock from "fetch-mock";
describe("sakai-profile tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(pronunciationPlayerData.i18nUrl, pronunciationPlayerData.i18n)
      .get(data.profileUrl, data.profile)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  window.top.portal = { siteId: "test-site" };

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
    const imageUrl = new URL(el.renderRoot.querySelector(".photo").style.backgroundImage.slice(5, -2), window.location.href);
    expect(imageUrl.pathname).to.equal(`/api/users/${data.userId}/profile/image`);
    expect(imageUrl.searchParams.get("siteId")).to.equal("test-site");
  });

  it("uses the profile image endpoint without a site context", async () => {
    const siteId = window.top.portal.siteId;
    window.top.portal.siteId = null;
    try {
      const el = await fixture(html`<sakai-profile user-id="${data.userId}"></sakai-profile>`);
      el.fetchProfileData();
      await waitUntil(() => el.renderRoot.querySelector(".photo"));
      const imageUrl = new URL(el.renderRoot.querySelector(".photo").style.backgroundImage.slice(5, -2), window.location.href);
      expect(imageUrl.pathname).to.equal(`/api/users/${data.userId}/profile/image`);
      expect(imageUrl.search).to.equal("");
    } finally {
      window.top.portal.siteId = siteId;
    }
  });
});
