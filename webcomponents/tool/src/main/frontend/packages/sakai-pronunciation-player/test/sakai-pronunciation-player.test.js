import "../sakai-pronunciation-player.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-pronunciation-player tests", () => {

  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {
 
    let el = await fixture(html`<sakai-pronunciation-player></sakai-pronunciation-player>`);
    await el.updateComplete;
    expect(el.shadowRoot.getElementById("play-button")).to.not.exist;
  });

  it ("is accessible", async () => {

    let el = await fixture(html`<sakai-pronunciation-player></sakai-pronunciation-player>`);

    await el.updateComplete;

    expect(el.shadowRoot).to.be.accessible();
  });
});
