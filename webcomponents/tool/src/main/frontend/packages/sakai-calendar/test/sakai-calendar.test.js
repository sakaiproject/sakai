import "../sakai-calendar.js";
import { html } from "lit";
import * as data from "./data.js";
import { expect, fixture, waitUntil, aTimeout } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-calendar tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(data.userCalendarUrl, data.userCalendarEvents, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-calendar user-id="${data.userId}"></sakai-calendar>
    `);

    expect(el.shadowRoot.getElementById("container")).to.exist;

    waitUntil(() => el.events);

    el.dispatchEvent(new CustomEvent("user-selected-date-changed", { detail: { selectedDate: data.selectedDate } }));

    await el.updateComplete;

    expect(el.shadowRoot.querySelector("#days-events a")).to.exist;
    expect(el.shadowRoot.querySelectorAll("#days-events a span").item(0).innerHTML).to.contain(data.userCalendarEvents.events[0].title);
    expect(el.shadowRoot.querySelectorAll("#days-events a span").item(1).innerHTML).to.contain(data.userCalendarEvents.events[0].siteTitle);
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-calendar user-id="${data.userId}"></sakai-calendar>
    `);

    expect(el.shadowRoot).to.be.accessible();
  });
});
