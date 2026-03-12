import "../sakai-calendar.js";
import * as data from "./data.js";
import * as sitePickerData from "../../sakai-site-picker/test/data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-calendar tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal()
      .get(data.i18nUrl, data.i18n)
      .get(sitePickerData.i18nUrl, sitePickerData.i18n)
      .get(data.userCalendarUrl, { "events": data.userCalendarEvents.events, "sites": sitePickerData.sites })
      .get(data.siteCalendarUrl, { "events": data.userCalendarEvents.events })
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    const el = await fixture(html`
      <sakai-calendar user-id="${data.userId}"></sakai-calendar>
    `);

    expect(el.shadowRoot.getElementById("container")).to.exist;

    await waitUntil(() => {
      const calendarMessage = el.shadowRoot.querySelector(".calendar-msg");
      return el._events && calendarMessage && calendarMessage.textContent.includes(data.pinnedSitesMessage);
    });

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelector(".calendar-msg")).to.exist;
    expect(el.shadowRoot.querySelector(".calendar-msg").textContent).to.contain(data.pinnedSitesMessage);

    el.dispatchEvent(new CustomEvent("user-selected-date-changed", { detail: { selectedDate: data.selectedDate } }));

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelector("#days-events a")).to.exist;
    expect(el.shadowRoot.querySelectorAll("#days-events a span").item(0).innerHTML).to.contain(data.userCalendarEvents.events[0].title);
    expect(el.shadowRoot.querySelectorAll("#days-events a span").item(1).innerHTML).to.contain(data.userCalendarEvents.events[0].siteTitle);
  });

  it ("does not render the pinned sites message in site mode", async () => {

    const el = await fixture(html`
      <sakai-calendar site-id="${data.siteId}"></sakai-calendar>
    `);

    await waitUntil(() => el._events && el._i18n && !el.shadowRoot.querySelector(".calendar-msg"));
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelector(".calendar-msg")).to.not.exist;
  });
});
