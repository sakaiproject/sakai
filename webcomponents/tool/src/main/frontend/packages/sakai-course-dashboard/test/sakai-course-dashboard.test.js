import "../sakai-course-dashboard.js";
import { html } from "lit";
import * as data from "./data.js";
import * as dialogData from "../../sakai-dialog-content/test/data.js";
import * as widgetData from "../../sakai-widgets/test/data.js";
import * as announcementsData from "../../sakai-announcements/test/data.js";
import * as gradesData from "../../sakai-grades/test/data.js";
import * as forumsData from "../../sakai-forums/test/data.js";
import * as tasksData from "../../sakai-tasks/test/data.js";
import * as pagerData from "../../sakai-pager/test/data.js";
import { expect, fixture, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-course-dashboard tests", () => {

  const minusFiveHours = -5 * 60 * 60 * 1000;
  window.top.portal = { locale: "en_GB", user: { offsetFromServerMillis: minusFiveHours } };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(widgetData.widgetPanelI18nUrl, widgetData.widgetPanelI18n, { overwriteRoutes: true })
    .get(widgetData.dashboardWidgetI18nUrl, widgetData.dashboardWidgetI18n, { overwriteRoutes: true })
    .get(announcementsData.i18nUrl, announcementsData.i18n, { overwriteRoutes: true })
    .get(announcementsData.siteAnnouncementsUrl, announcementsData.siteAnnouncements, { overwriteRoutes: true })
    .get(forumsData.i18nUrl, forumsData.i18n, { overwriteRoutes: true })
    .get(gradesData.i18nUrl, gradesData.i18n, { overwriteRoutes: true })
    .get(tasksData.i18nUrl, tasksData.i18n, { overwriteRoutes: true })
    .get(tasksData.siteTasksUrl, tasksData.siteTasks, { overwriteRoutes: true })
    .get(forumsData.siteForumsUrl, forumsData.siteForums, { overwriteRoutes: true })
    .get(pagerData.i18nUrl, pagerData.i18n, { overwriteRoutes: true })
    /*
    .get(calendarData.i18nUrl, calendarData.i18n, { overwriteRoutes: true })
    */
    .get(data.dashboardUrl, data.dashboardData, { overwriteRoutes: true })
    .get(dialogData.i18nUrl, dialogData.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-course-dashboard site-id="${data.siteId}"></sakai-course-dashboard>
    `);

    await waitUntil(() => el.i18n && el.data);

    expect(el.querySelector("#course-dashboard-title-and-edit-block")).to.exist;
  });

  /*
  if ("renders the header correctly", async () => {
    let el = await fixture(html`
      <sakai-course-header .site=${data.dashboardData}></sakai-course-header>
    `);

    await waitUntil(() => el.i18n);
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-course-dashboard site-id="${data.siteId}"></sakai-course-dashboard>
    `);

    await waitUntil(() => el.data);

    expect(el.shadowRoot).to.be.accessible();
  });
  */
});
