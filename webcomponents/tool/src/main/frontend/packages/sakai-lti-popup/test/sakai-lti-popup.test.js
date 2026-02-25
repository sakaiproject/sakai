import "../sakai-lti-popup.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import { stub } from "sinon";
import fetchMock from "fetch-mock";
describe("sakai-lti-popup tests", () => {

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

  it ("renders buttons with the i18n default values", async () => {
 
    const el = await fixture(html`<sakai-lti-popup></sakai-lti-popup>`);

    await elementUpdated(el);

    await expect(el).to.be.accessible();

    expect(el.querySelector("div button:first-child").innerHTML).to.contain(el._i18n.pre_launch_text);
    expect(el.querySelector("div button:nth-child(2)").innerHTML).to.contain(el._i18n.post_launch_text);
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

    expect(el.querySelector("div button:first-child").innerHTML).to.contain(preLaunch);
    expect(el.querySelector("div button:nth-child(2)").innerHTML).to.contain(postLaunch);
  });

  it ("called window.open on clicking the first button", async () => {

    const launchUrl = "http://eggs.com";

    const el = await fixture(html`
      <sakai-lti-popup
          launch-url="${launchUrl}">
      </sakai-lti-popup>
    `);

    await elementUpdated(el);

    const windowOpenStub = stub(window, "open");

    el.querySelector("div button:first-child").click();

    expect(windowOpenStub.calledWith(launchUrl, "_blank")).to.be.true;
    windowOpenStub.restore();
  });

  it ("calls window.open immediately if auto-launch is true", async () => {

    const launchUrl = "http://eggs.com";

    const windowOpenStub = stub(window, "open");

    const el = await fixture(html`
      <sakai-lti-popup
          launch-url="${launchUrl}"
          auto-launch>
      </sakai-lti-popup>
    `);

    await elementUpdated(el);

    expect(windowOpenStub.calledWith(launchUrl, "_blank")).to.be.true;
    windowOpenStub.restore();
  });
});
