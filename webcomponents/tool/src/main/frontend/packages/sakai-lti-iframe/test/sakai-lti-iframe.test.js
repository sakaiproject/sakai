import "../sakai-lti-iframe.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-lti-iframe tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  const launchUrl = "http://eggs.com";

  window.top.portal = { siteId: data.siteId };

  it ("renders correctly", async () => {
 
    let el = await fixture(html`<sakai-lti-iframe></sakai-lti-iframe>`);
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.querySelector("div.sakai-iframe-launch")).to.not.exist;

    el = await fixture(html`<sakai-lti-iframe new-window-text="Eggs" launch-url="${launchUrl}"></sakai-lti-iframe>`);
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.querySelector("div.sakai-iframe-launch")).to.exist;
    expect(document.getElementById(`sakai-lti-button-${el.randomId}`)).to.exist;
    expect(document.getElementById(`sakai-lti-iframe-${el.randomId}`)).to.exist;
    expect(el.querySelector(`iframe[src='${launchUrl}']`)).to.exist;

    el = await fixture(html`<sakai-lti-iframe new-window-text="Eggs" launch-url="${launchUrl}" allow-resize="true"></sakai-lti-iframe>`);
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.querySelector("iframe[data-allow-resize='true']")).to.exist;
  });
});
