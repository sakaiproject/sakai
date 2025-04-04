import "../sakai-pronunciation-player.js";
import { elementUpdated, fixture, expect, html } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-pronunciation-player tests", () => {

  window.top.portal = { siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {
 
    const el = await fixture(html`<sakai-pronunciation-player></sakai-pronunciation-player>`);

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.shadowRoot.getElementById("play-button")).to.not.exist;
  });
});
