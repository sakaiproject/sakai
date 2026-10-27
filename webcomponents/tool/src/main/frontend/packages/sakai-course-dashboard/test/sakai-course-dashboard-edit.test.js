import "../sakai-course-dashboard.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, fixtureCleanup, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-course-dashboard edit tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.dialogContentI18nUrl, data.dialogContentI18n)
      .get(data.dashboardUrl, {
        ...data.dashboardData,
        siteEditable: false,
        layoutEditable: true,
      })
      .get(data.widgetPanelI18nUrl, data.widgetPanelI18n)
      .get(data.toolnamesI18nUrl, data.toolnamesI18n)
      .get(`/api/users/${data.userId}/dashboard`, { widgetFilters: {} });
  });

  afterEach(() => {
    fixtureCleanup();
    fetchMock.hardReset();
  });

  it("hides site-only controls for members without site.upd", async () => {

    const el = await fixture(html`
      <sakai-course-dashboard
          user-id="${data.userId}"
          site-id="${data.siteId}">
      </sakai-course-dashboard>
    `);

    await waitUntil(() => el.data);
    await elementUpdated(el);

    el.querySelector("#course-dashboard-edit").click();
    await elementUpdated(el);

    expect(el.querySelector("#course-dashboard-show-image")).to.not.exist;
    expect(el.querySelector(".course-dashboard-hero-control select")).to.exist;
  });
});
