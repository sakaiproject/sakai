import "../sakai-home-dashboard.js";
import * as sinon from "sinon";
import * as data from "./data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";
describe("sakai-home-dashboard tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.courseListI18nUrl, data.courseListI18n)
      .get(data.dialogContentI18nUrl, data.dialogContentI18n)
      .get(data.courseListUrl, data.courseList)
      .get(data.dashboardUrl, data.dashboardData)
      .get(data.widgetPanelI18nUrl, data.widgetPanelI18n)
      .get(data.courseCardI18nUrl, data.courseCardI18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  it ("renders in user mode correctly", async () => {

    const swMock = sinon.mock(navigator.serviceWorker);

    let el = await fixture(html`
      <sakai-home-dashboard
          user-id="${data.userId}"
          site-id="${data.siteId}">
      </sakai-home-dashboard>
    `);

    await waitUntil(() => el._data);

    await elementUpdated(el);

    swMock.expects("register").once().returns(Promise.resolve());

    await expect(el).to.be.accessible();
  });
});
