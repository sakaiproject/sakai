import "../sakai-widget-panel.js";
import * as data from "./data.js";
import * as gradesData from "../../sakai-grades/test/data.js";
import * as announcementsData from "../../sakai-announcements/test/data.js";
import * as forumsData from "../../sakai-forums/test/data.js";
import * as sitePickerData from "../../sakai-site-picker/test/data.js";
import { elementUpdated, expect, fixture, html, waitUntil } from "@open-wc/testing";
import fetchMock from "fetch-mock";

describe("sakai-widget-panel tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    fetchMock
      .get(data.i18nUrl, data.i18n)
      .get(data.dashboardWidgetI18nUrl, data.dashboardWidgetI18n)
      .get(data.toolnamesI18nUrl, data.toolnamesI18n)
      .get(data.widgetpickerI18nUrl, data.widgetpickerI18n)
      .get(gradesData.i18nUrl, gradesData.i18n)
      .get(gradesData.gradesUrl, { grades: gradesData.grades, sites: sitePickerData.sites })
      .get(announcementsData.i18nUrl, announcementsData.i18n)
      .get(announcementsData.announcementsUrl, { announcements: announcementsData.announcements, sites: sitePickerData.sites })
      .get(forumsData.i18nUrl, forumsData.i18n)
      .get(forumsData.userForumsUrl, { forums: forumsData.userForums, sites: sitePickerData.sites })
      .get(sitePickerData.i18nUrl, sitePickerData.i18n)
      .get("*", 500);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });

  window.top.portal = { locale: "en_GB" };

  it ("renders in user mode correctly", async () => {

    const el = await fixture(html`
      <sakai-widget-panel user-id="${data.userId}" .widgetIds=${data.widgetIds} .layout=${data.layout1}></sakai-widget-panel>
    `);

    await waitUntil(() => el.shadowRoot?.getElementById("grid"));
    await waitUntil(() => data.layout1.every(tile =>
      el.shadowRoot.querySelector(`sakai-${tile.id}-widget`)?.shadowRoot));
    await elementUpdated(el);

    const a11yIgnored = [ "landmark-unique", "color-contrast", "select-name", "label", "button-name" ];
    await expect(el).to.be.accessible({ ignoredRules: a11yIgnored });

    expect(el.shadowRoot.getElementById("grid")).to.exist;

    data.layout1.forEach(tile => expect(el.shadowRoot.querySelector(`sakai-${tile.id}-widget`)?.shadowRoot).to.exist);

    const layoutIds = data.layout1.map(tile => tile.id);
    data.widgetIds.filter(id => !layoutIds.includes(id))
      .forEach(id => expect(el.shadowRoot.querySelector(`sakai-${id}-widget`)).to.not.exist);

    el.editing = true;
    await elementUpdated(el);
    await expect(el).to.be.accessible({ ignoredRules: a11yIgnored });

    const available = el.renderRoot.querySelector("#available-widgets");
    expect(available).to.exist;
    const statusButton = [ ...available.querySelectorAll("button") ]
      .find(button => (button.getAttribute("aria-label") || "").includes("status"));
    expect(statusButton).to.exist;
    const beforeCount = el.layout.length;
    statusButton.click();
    await elementUpdated(el);
    expect(el.layout.length).to.equal(beforeCount + 1);
    expect(el.shadowRoot.querySelector("#grid sakai-status-widget")).to.exist;

    let forums = el.shadowRoot.querySelector("#grid sakai-forums-widget");
    expect(forums).to.exist;
    forums.dispatchEvent(new CustomEvent("move", { detail: { widgetId: "forums", direction: "left" }, bubbles: true }));
    await elementUpdated(el);
    forums = el.shadowRoot.querySelector("#grid sakai-forums-widget");
    expect(forums).to.exist;
    forums.dispatchEvent(new CustomEvent("move", { detail: { widgetId: "forums", direction: "right" }, bubbles: true }));
    await elementUpdated(el);
    forums = el.shadowRoot.querySelector("#grid sakai-forums-widget");
    expect(forums).to.exist;

    forums.dispatchEvent(new CustomEvent("remove"));
    await elementUpdated(el);
    await expect(el).to.be.accessible({ ignoredRules: a11yIgnored });
    expect(el.shadowRoot.querySelector("#grid sakai-forums-widget")).to.not.exist;
  });
});
