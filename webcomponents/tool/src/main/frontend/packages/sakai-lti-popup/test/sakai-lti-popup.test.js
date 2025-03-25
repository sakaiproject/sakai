import "../sakai-lti-popup.js";
import { elementUpdated, expect, fixture, waitUntil } from "@open-wc/testing";
import { html } from "lit";
import * as data from "./data.js";
import * as sinon from "sinon";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-lti-popup tests", () => {


  window.top.portal = { locale: "en_GB", siteId: data.siteId };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders buttons with the i18n default values", async () => {
 
    const el = await fixture(html`<sakai-lti-popup></sakai-lti-popup>`);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("div button:first-child").innerText).to.equal(el._i18n.pre_launch_text);
    expect(el.querySelector("div button:nth-child(2)").innerText).to.equal(el._i18n.post_launch_text);
  });

  it ("renders buttons with the supplied values", async () => {

    const launchUrl = "http://eggs.com";
    const preLaunch = "Pre Launch";
    const postLaunch = "Post Launch";

    const el = await fixture(html`
      <sakai-lti-popup pre-launch-text="${preLaunch}"
          post-launch-text="${postLaunch}"
          launch-url="${launchUrl}">
      </sakai-lti-popup>
    `);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("div button:first-child").innerText).to.equal(preLaunch);
    expect(el.querySelector("div button:nth-child(2)").innerText).to.equal(postLaunch);
  });

  it ("called window.open on clicking the first button", async () => {

    const launchUrl = "http://eggs.com";

    const el = await fixture(html`
      <sakai-lti-popup
          launch-url="${launchUrl}">
      </sakai-lti-popup>
    `);

    await elementUpdated(el);

    const spy = sinon.spy(window, "open");

    el.querySelector("div button:first-child").click();

    spy.withArgs(launchUrl, "_blank");

    spy.restore();
  });

  it ("calls window.open immediately if auto-launch is true", async () => {

    const launchUrl = "http://eggs.com";

    const spy = sinon.spy(window, "open");

    const el = await fixture(html`
      <sakai-lti-popup
          launch-url="${launchUrl}"
          auto>
      </sakai-lti-popup>
    `);

    await elementUpdated(el);

    spy.withArgs(launchUrl, "_blank");
  });
});
