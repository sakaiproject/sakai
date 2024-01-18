import "../sakai-widget-panel.js";
import { html } from "lit";
import * as data from "./data.js";
import * as gradesData from "../../sakai-grades/test/data.js";
import * as announcementsData from "../../sakai-announcements/test/data.js";
import * as forumsData from "../../sakai-forums/test/data.js";
import { expect, fixture, waitUntil, aTimeout } from "@open-wc/testing";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-widget-panel tests", () => {

  window.top.portal = { locale: "en_GB" };

  fetchMock
    .get(data.i18nUrl, data.i18n, {overwriteRoutes: true})
    .get(data.dashboardWidgetI18nUrl, data.dashboardWidgetI18n, {overwriteRoutes: true})
    .get(data.toolnamesI18nUrl, data.toolnamesI18n, {overwriteRoutes: true})
    .get(data.widgetpickerI18nUrl, data.widgetpickerI18n, {overwriteRoutes: true})
    .get(gradesData.i18nUrl, gradesData.i18n, {overwriteRoutes: true})
    .get(announcementsData.i18nUrl, announcementsData.i18n, {overwriteRoutes: true})
    .get(announcementsData.announcementsUrl, announcementsData.announcements, {overwriteRoutes: true})
    .get(forumsData.i18nUrl, forumsData.i18n, {overwriteRoutes: true})
    .get("*", 500, {overwriteRoutes: true});

  it ("renders in user mode correctly", async () => {

    // In user mode, we'd expect to get announcements from multiple sites.
    let el = await fixture(html`
      <sakai-widget-panel user-id="${data.userId}" .widgetIds=${data.widgetIds} .layout="${data.layout1}"></sakai-widget-panel>
    `);

    await waitUntil(() => el.i18n);

    expect(el.shadowRoot.getElementById("grid")).to.exist;

    data.layout1.forEach(id => expect(el.shadowRoot.querySelector(`sakai-${id}-widget`)?.shadowRoot).to.exist);

    data.widgetIds.filter(id => !data.layout1.includes(id))
      .forEach(id => expect(el.shadowRoot.querySelector(`sakai-${id}-widget`)).to.not.exist);

    el.editing = true;
    await el.updateComplete;

    const addLink = el.shadowRoot.querySelector("#add-button a");
    expect(addLink).to.exist;
    // Open the picker
    addLink.click();
    await el.updateComplete;
    expect(el.shadowRoot.querySelector("sakai-widget-picker")?.shadowRoot).to.exist;
    // Close the picker
    addLink.click();
    await el.updateComplete;
    expect(el.shadowRoot.querySelector("sakai-widget-picker")).to.not.exist;

    let forums = el.shadowRoot.querySelector("#grid > div:nth-child(2) > sakai-forums-widget");
    expect(forums).to.exist;
    forums.dispatchEvent(new CustomEvent("move", { detail: { widgetId: "forums", direction: "left" }, bubbles: true }));
    await el.updateComplete;
    forums = el.shadowRoot.querySelector("#grid > div:first-child > sakai-forums-widget");
    expect(forums).to.exist;
    forums.dispatchEvent(new CustomEvent("move", { detail: { widgetId: "forums", direction: "right" }, bubbles: true }));
    await el.updateComplete;
    forums = el.shadowRoot.querySelector("#grid > div:nth-child(2) > sakai-forums-widget");
    expect(forums).to.exist;

    forums.dispatchEvent(new CustomEvent("remove"));
    await el.updateComplete;
    expect(el.shadowRoot.querySelector("#grid > div > sakai-forums-widget")).to.not.exist;
  });

  it ("is accessible", async () => {

    let el = await fixture(html`
      <sakai-widget-panel user-id="${data.userId}" .widgetIds="${data.widgetIds}" .layout="${data.layout1}"></sakai-widget-panel>
    `);

    await waitUntil(() => el.i18n);

    expect(el.shadowRoot).to.be.accessible();
  });
});
