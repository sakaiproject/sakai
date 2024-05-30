import "../sakai-home-dashboard.js";
import { html } from "lit";
import * as data from "./data.js";
import * as dialogData from "../../sakai-dialog-content/test/data.js";
import * as widgetData from "../../sakai-widgets/test/data.js";
import * as announcementsData from "../../sakai-announcements/test/data.js";
import * as gradesData from "../../sakai-grades/test/data.js";
import * as forumsData from "../../sakai-forums/test/data.js";
import * as calendarData from "../../sakai-calendar/test/data.js";
import * as tasksData from "../../sakai-tasks/test/data.js";
import { expect, fixture, waitUntil, aTimeout } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-home-dashboard tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get(widgetData.widgetPanelI18nUrl, widgetData.widgetPanelI18n, { overwriteRoutes: true })
    .get(announcementsData.i18nUrl, announcementsData.i18n, { overwriteRoutes: true })
    .get(announcementsData.announcementsUrl, announcementsData.announcements, { overwriteRoutes: true })
    .get(gradesData.i18nUrl, gradesData.i18n, { overwriteRoutes: true })
    .get(forumsData.i18nUrl, forumsData.i18n, { overwriteRoutes: true })
    .get(calendarData.i18nUrl, calendarData.i18n, { overwriteRoutes: true })
    .get(tasksData.i18nUrl, tasksData.i18n, { overwriteRoutes: true })
    .get(tasksData.tasksUrl, tasksData.tasks, { overwriteRoutes: true })
    .get(widgetData.dashboardWidgetI18nUrl, widgetData.dashboardWidgetI18n, { overwriteRoutes: true })
    .get(data.dashboardUrl, data.dashboardData, { overwriteRoutes: true })
    .put(data.dashboardUrl, 200, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-home-dashboard user-id="${data.userId}"></sakai-home-dashboard>
    `);

    await waitUntil(() => el.i18n && el.data);

    expect(el.shadowRoot.querySelector("#welcome-and-edit-block")).to.exist;
    expect(el.shadowRoot.getElementById("motd")).to.exist;
    const widgetPanel = el.shadowRoot.querySelector("sakai-widget-panel");
    expect(widgetPanel).to.exist;
    expect(widgetPanel.shadowRoot.querySelector("sakai-announcements-widget")).to.exist;
    expect(widgetPanel.shadowRoot.querySelector("sakai-forums-widget")).to.exist;
    expect(widgetPanel.shadowRoot.querySelector("sakai-grades-widget")).to.exist;
    expect(widgetPanel.shadowRoot.querySelector("sakai-calendar-widget")).to.exist;
    expect(widgetPanel.shadowRoot.querySelector("sakai-tasks-widget")).to.exist;
    expect(el.shadowRoot.getElementById("toolbar")).to.exist;
    expect(el.shadowRoot.querySelector(`sakai-button`)).to.exist;
    expect(el.shadowRoot.querySelector(`sakai-button[href='${data.dashboardData.worksiteSetupUrl}']`)).to.exist;
  });

  it ("renders in editing mode correctly", async () => {

    let el = await fixture(html`
      <sakai-home-dashboard user-id="${data.userId}"></sakai-home-dashboard>
    `);

    await waitUntil(() => el.i18n && el.data);

    const editButton = el.shadowRoot.querySelector("#edit > sakai-button");
    expect(editButton).to.exist;

    expect(el.shadowRoot.querySelector("#save > sakai-button")).to.not.exist;

    editButton.click();

    await el.updateComplete;
    expect(el._editing).to.be.true;

    const saveButton = el.shadowRoot.querySelector("#save > sakai-button");
    expect(saveButton).to.exist;

    saveButton.click();
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-home-dashboard user-id="${data.userId}"></sakai-home-dashboard>
    `);

    await waitUntil(() => el.data);

    expect(el.shadowRoot).to.be.accessible();
  });
});
