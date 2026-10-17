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

    const editBtn = el.querySelector("#course-dashboard-edit");
    expect(editBtn).to.exist;
    expect(editBtn.querySelector(".si-edit")).to.exist;

    const available = el.querySelector(".dashboard-widgets-available");
    expect(available).to.exist;
    expect(available.textContent).to.include("1");
  });

  it ("hides site-only controls for members without site.upd", async () => {

    fetchMock.get(data.dashboardUrl, {
      ...data.dashboardData,
      siteEditable: false,
      layoutEditable: true,
    }, { overwriteRoutes: true });

    let el = await fixture(html`
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
