import "../sakai-course-dashboard.js";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-course-dashboard tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.dialogContentI18nUrl, data.dialogContentI18n)
      .get(data.dashboardUrl, data.dashboardData)
      .get(data.widgetPanelI18nUrl, data.widgetPanelI18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders in user mode correctly", async () => {

    let el = await fixture(html`
      <sakai-course-dashboard
          user-id="${data.userId}"
          site-id="${data.siteId}">
      </sakai-course-dashboard>
    `);

    await waitUntil(() => el.data);

    await elementUpdated(el);

    await expect(el).to.be.accessible();
  });
});
