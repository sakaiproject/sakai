import "../sakai-calendar.js";
import * as data from "./data.js";
import * as sitePickerData from "../../sakai-site-picker/test/data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-calendar tests", () => {

  let events;

  beforeEach(() => {
    events = data.userCalendarEvents.events;
    fetchMock.mockGlobal()
      .get(data.i18nUrl, data.i18n)
      .get(sitePickerData.i18nUrl, sitePickerData.i18n)
      .get(data.userCalendarUrl, () => ({ events, sites: sitePickerData.sites }))
      .get(data.siteCalendarUrl, () => ({ events }))
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  [ undefined, data.siteId ].forEach(siteId => {
    it (`includes midnight events in the list and markers in ${siteId ? "site" : "user"} mode`, async () => {

      const first = new Date(2026, 10, 2);
      const second = new Date(2026, 10, 3);
      const third = new Date(2026, 10, 4);
      events = [
        { title: "First midnight", start: first.getTime() },
        { title: "Second midnight", start: second.getTime() },
      ];
      const el = await fixture(html`
        <sakai-calendar dashboard .siteId=${siteId} .centralDate=${first}
          style="--sakai-calendar-has-events-bg-color: green; --sakai-calendar-today-bg-color: blue;">
        </sakai-calendar>
      `);
      await waitUntil(() => el._events && el._i18n);
      await elementUpdated(el);

      const dayButton = date => [ ...el.renderRoot.querySelectorAll(".calendar__day-button") ]
        .find(button => button.date?.getTime() === date.getTime());
      dayButton(first).click();
      await elementUpdated(el);
      expect(el.renderRoot.querySelector("#days-events").textContent).to.contain("First midnight");
      expect(el.renderRoot.querySelector("#days-events").textContent).not.to.contain("Second midnight");
      expect(dayButton(first).classList.contains("has-events")).to.be.true;
      expect(dayButton(second).classList.contains("has-events")).to.be.true;
      expect(dayButton(third).classList.contains("has-events")).to.be.false;

      dayButton(second).click();
      await elementUpdated(el);
      expect(el.renderRoot.querySelector("#days-events").textContent).to.contain("Second midnight");
      expect(el.renderRoot.querySelector("#days-events").textContent).not.to.contain("First midnight");
      expect(getComputedStyle(dayButton(second)).backgroundColor).to.equal("rgb(0, 128, 0)");
      expect(getComputedStyle(dayButton(second)).outlineStyle).to.equal("solid");

      dayButton(third).click();
      await elementUpdated(el);
      expect(getComputedStyle(dayButton(third)).backgroundColor).to.equal("rgba(0, 0, 0, 0)");
      expect(getComputedStyle(dayButton(third)).outlineStyle).to.equal("solid");
    });
  });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    const el = await fixture(html`
      <sakai-calendar user-id="${data.userId}"></sakai-calendar>
    `);

    expect(el.shadowRoot.getElementById("container")).to.exist;

    await waitUntil(() => {
      const statusMessage = el.shadowRoot.querySelector("#widget-status");
      return el._events && statusMessage && statusMessage.textContent.includes(data.pinnedSitesMessage);
    });

    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelector("#widget-status")).to.exist;
    expect(el.shadowRoot.querySelector("#widget-status").textContent).to.contain(data.pinnedSitesMessage);

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

    await waitUntil(() => el._events && el._i18n && !el.shadowRoot.querySelector("#widget-status"));
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    expect(el.shadowRoot.querySelector("#widget-status")).to.not.exist;
  });
});
