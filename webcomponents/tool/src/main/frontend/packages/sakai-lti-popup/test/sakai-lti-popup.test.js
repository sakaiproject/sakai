import "../sakai-lti-popup.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-lti-popup tests", () => {

  const launchUrl = "http://eggs.com";

  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders correctly", async () => {
 
    let el = await fixture(html`<sakai-lti-popup></sakai-lti-popup>`);
    await waitUntil(() => el.i18n);
    expect(el.querySelector("div.sakai-popup-launcn")).to.not.exist;


    el = await fixture(html`<sakai-lti-popup pre-launch-text="Pre Launch" post-launch-text="Post Launch" launch-url="${launchUrl}"></sakai-lti-popup>`);
    await waitUntil(() => el.i18n);
    expect(document.getElementById(`sakai-lti-popup-${el.randomId}`)).to.exist;
  });

  it ("is accessible", async () => {

    const el = await fixture(html`<sakai-lti-popup></sakai-lti-popup>`);
    await waitUntil(() => el.i18n);
    await expect(el).to.be.accessible();
  });
});
