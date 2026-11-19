import "../sakai-course-dashboard.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, fixtureCleanup, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-course-dashboard tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.dialogContentI18nUrl, data.dialogContentI18n)
      .get(data.dashboardUrl, data.dashboardData)
      .get(data.widgetPanelI18nUrl, data.widgetPanelI18n)
      .get(data.toolnamesI18nUrl, data.toolnamesI18n)
      .get(`/api/users/${data.userId}/dashboard`, { widgetFilters: {} });
  });

  afterEach(() => {
    fixtureCleanup();
    fetchMock.hardReset();
  });

  it("renders in user mode correctly", async () => {

    const el = await fixture(html`
      <sakai-course-dashboard
          user-id="${data.userId}"
          site-id="${data.siteId}">
      </sakai-course-dashboard>
    `);

    await waitUntil(() => el.data);
    await elementUpdated(el);
    await expect(el).to.be.accessible();

    const editBtn = el.querySelector("#course-dashboard-edit");
    expect(editBtn).to.exist;
    expect(editBtn.querySelector(".si-edit")).to.exist;

    const available = el.querySelector(".dashboard-widgets-available");
    expect(available).to.exist;
    expect(available.textContent).to.include("1");
  });
});
