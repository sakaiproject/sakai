import "../sakai-pronunciation-player.js";
import { elementUpdated, fixture, expect, html } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-pronunciation-player tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  window.top.portal = { siteId: data.siteId };

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-pronunciation-player></sakai-pronunciation-player>`);

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.shadowRoot.getElementById("play-button")).to.not.exist;
  });
});
