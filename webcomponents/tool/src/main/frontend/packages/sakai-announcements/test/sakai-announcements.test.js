import "../sakai-announcements.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture, waitUntil, aTimeout } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-announcements tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.announcementsUrl, data.announcements, { overwriteRoutes: true })
    .get(data.siteAnnouncementsUrl, data.siteAnnouncements, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-announcements user-id="${data.userId}"></sakai-announcements>
    `);

    await aTimeout(200);

    expect(el.shadowRoot.getElementById("topbar")).to.exist;
    expect(el.shadowRoot.querySelectorAll("div.title").length).to.equal(3);

    expect(el.shadowRoot.querySelectorAll("div.announcements > .header").length).to.equal(3);

    // Sort by title
    const sortByTitle = el.shadowRoot.querySelector("div.announcements > .header:first-child > a");
    expect(sortByTitle).to.exist;

    sortByTitle.click();
    await el.updateComplete;
    expect(el.shadowRoot.querySelector("div.announcements > .title").innerHTML).to.contain("Vavavoom");

    sortByTitle.click();
    await el.updateComplete;
    expect(el.shadowRoot.querySelector("div.announcements > .title").innerHTML).to.contain("Chips");

    // Sort by site
    const sortBySite = el.shadowRoot.querySelector("div.announcements > .header:nth-child(1) > a");
    expect(sortBySite).to.exist;

    sortBySite.click();
    await el.updateComplete;
    expect(el.shadowRoot.querySelector("div.announcements > .site").innerHTML).to.contain(data.vavavoomSite);

    sortBySite.click();
    await el.updateComplete;
    expect(el.shadowRoot.querySelector("div.announcements > .site").innerHTML).to.contain(data.siteTitle);

    // Select a site
    const siteSelect = el.shadowRoot.querySelector("#site-filter > select");
    expect(siteSelect).to.exist;
    siteSelect.value = data.siteId;
    siteSelect.dispatchEvent(new Event("change"));

    await el.updateComplete;

    expect(el.shadowRoot.querySelectorAll("div.title").length).to.equal(2);
  });

  it ("renders in site mode correctly", async () => {

    let el = await fixture(html`
      <sakai-announcements site-id="${data.siteId}"></sakai-announcements>
    `);

    await aTimeout(200);

    expect(el.shadowRoot.getElementById("topbar")).to.exist;
    expect(el.shadowRoot.querySelectorAll("div.title").length).to.equal(2);

    expect(el.shadowRoot.querySelectorAll("div.announcements > .header").length).to.equal(2);
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-announcements user-id="${data.userId}"></sakai-announcements>
    `);

    await aTimeout(200);

    expect(el.shadowRoot).to.be.accessible();
  });
});
