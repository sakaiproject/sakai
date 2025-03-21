import "../sakai-announcements.js";
import { html } from "lit";
import * as data from "./data.js";
import {
  TITLE_A_TO_Z,
  TITLE_Z_TO_A,
  SITE_A_TO_Z,
  SITE_Z_TO_A,
  EARLIEST_FIRST,
  LATEST_FIRST,
  INSTRUCTOR_ORDER,
} from "../src/sakai-announcements-constants.js";
import * as sitePickerData from "../../sakai-site-picker/test/data.js";
import { elementUpdated, expect, fixture, waitUntil, aTimeout } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-announcements tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(sitePickerData.i18nUrl, sitePickerData.i18n, { overwriteRoutes: true })
    .get(data.announcementsUrl, data.announcements, { overwriteRoutes: true })
    .get(data.siteAnnouncementsUrl, data.siteAnnouncements, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-announcements user-id="${data.userId}"></sakai-announcements>
    `);

    await waitUntil(() => el.dataPage);

    await elementUpdated(el);

    expect(el.shadowRoot.querySelectorAll("div.title").length).to.equal(3);

    expect(el.shadowRoot.querySelectorAll(".header").length).to.equal(3);

    expect(el.shadowRoot.querySelector(".title").innerHTML).to.contain(data.announcements[0].subject);

    const sortingSelect = el.shadowRoot.querySelector("#sorting > select");
    expect(sortingSelect).to.exist;

    // Sort by title
    sortingSelect.value = TITLE_A_TO_Z;
    sortingSelect.dispatchEvent(new Event("change"));
    await elementUpdated(el);
    expect(el.shadowRoot.querySelector(".title").innerHTML).to.contain("Chips");

    sortingSelect.value = TITLE_Z_TO_A;
    sortingSelect.dispatchEvent(new Event("change"));
    await elementUpdated(el);
    expect(el.shadowRoot.querySelector(".title").innerHTML).to.contain(data.vavavoom);

    // Sort by site
    sortingSelect.value = SITE_A_TO_Z;
    sortingSelect.dispatchEvent(new Event("change"));
    await elementUpdated(el);
    expect(el.shadowRoot.querySelector(".site").innerHTML).to.contain(data.vavavoomSite);

    sortingSelect.value = SITE_Z_TO_A;
    sortingSelect.dispatchEvent(new Event("change"));
    await elementUpdated(el);
    expect(el.shadowRoot.querySelector(".site").innerHTML).to.contain(data.siteTitle);

    // Instructor ordering
    sortingSelect.value = INSTRUCTOR_ORDER;
    sortingSelect.dispatchEvent(new Event("change"));
    await elementUpdated(el);
    // This makes no sense really. The order seems to be flipped in Sakai, so that's what we need to test.
    expect(el.shadowRoot.querySelector(".title").innerHTML).to.contain(data.announcements.filter(a => a.order === 3)[0].subject);

    // Select a site
    const siteSelect = el.shadowRoot.querySelector("#site-filter > sakai-site-picker");
    expect(siteSelect).to.exist;

    // We don't need to test the site picker here, it should have its own tests. So let's just fire
    // the event that would come from that component
    siteSelect.dispatchEvent(new CustomEvent("sites-selected", { detail: { value: data.vavavoom }, bubbles: true }));
    await elementUpdated(el);
    expect(el.shadowRoot.querySelectorAll("div.title").length).to.equal(1);
  });

  it ("renders in site mode correctly", async () => {

    let el = await fixture(html`
      <sakai-announcements site-id="${data.siteId}"></sakai-announcements>
    `);

    await waitUntil(() => el.dataPage);
    await elementUpdated(el);

    expect(el.shadowRoot.querySelectorAll(".title").length).to.equal(2);

    expect(el.shadowRoot.querySelectorAll(".header").length).to.equal(2);
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-announcements user-id="${data.userId}"></sakai-announcements>
    `);

    await waitUntil(() => el.dataPage);
    await elementUpdated(el);

    expect(el.shadowRoot).to.be.accessible();
  });
});
